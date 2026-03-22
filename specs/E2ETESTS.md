# Powerstaff 2026 – E2E-Teststrategie mit Playwright

**Stand:** März 2026
**Status:** Konzept – noch nicht implementiert

---

## Inhaltsverzeichnis

1. [Ziele und Abgrenzung](#1-ziele-und-abgrenzung)
2. [Technologie-Entscheidungen](#2-technologie-entscheidungen)
3. [Verzeichnisstruktur](#3-verzeichnisstruktur)
4. [Infrastruktur-Orchestrierung](#4-infrastruktur-orchestrierung)
5. [Testdaten-Strategie](#5-testdaten-strategie)
6. [Authentifizierung](#6-authentifizierung)
7. [data-testid Konvention](#7-data-testid-konvention)
8. [Stabile Assertions und Wartestrategien](#8-stabile-assertions-und-wartestrategien)
9. [Visuelle Regressionstests](#9-visuelle-regressionstests)
10. [Parallelität und Wiederholbarkeit](#10-parallelität-und-wiederholbarkeit)
11. [Maven-Integration](#11-maven-integration)
12. [GitHub Actions CI](#12-github-actions-ci)
13. [Testreport und Artefakte](#13-testreport-und-artefakte)

---

## 1. Ziele und Abgrenzung

E2E-Tests prüfen die Applikation aus Sicht des Sachbearbeiters im Browser. Sie ergänzen — aber ersetzen nicht — die bestehende Testpyramide:

| Ebene | Werkzeug | Prüft |
|---|---|---|
| Unit-Tests | Spock (`*Spec`) | Isolierte Logik ohne DB |
| Integrationstests | Spock + Testcontainers (`*IT`) | DB-Zugriff, Controller, Modulmodule |
| **E2E-Tests** | **Playwright** | Vollständiger Browser-Ablauf inkl. JS |

**In Scope:**
- Anlegen, Bearbeiten, Speichern, Löschen für alle Hauptformulare
- QBE-Suche und Suchergebnisse
- Navigationsflüsse (Toolbar, Seitenleiste, Profilsuche-Chat)
- Fehlerfälle die im Browser sichtbar sind (Warnbanner, Konflikt-Meldungen)
- Layout-Stabilität für kritische Bereiche (visueller Regressionstest)

**Out of Scope:**
- LLM-Qualität der Profilsuche (nicht deterministisch — wird gemockt)
- Inhalte die sich täglich ändern (Datumsfelder in Audit-Informationen)
- Lastverhalten und Performance

---

## 2. Technologie-Entscheidungen

### JavaScript/TypeScript Playwright (nicht playwright-java)

**Begründung:** Das Playwright-Ökosystem für JS/TS ist deutlich ausgereifter:
- Automatische Screenshot-, Video- und Trace-Aufzeichnung bei Testfehlern (`playwright.config.ts`)
- Trace Viewer (`npx playwright show-trace`) als interaktiver Debugger mit DOM-Snapshots, Netzwerk-Log und Console pro Test-Schritt
- Codegen (`playwright codegen`) erzeugt direkt lauffähige TypeScript-Tests
- Playwright-eigener HTML-Report mit eingebetteten Traces

playwright-java würde Screenshots/Traces manuell in Spock-`cleanup:`-Blöcken erfordern und hätte keinen nativen Trace Viewer.

### Eigenes Verzeichnis `src/test/e2e/`

`src/main/frontend/` ist Produktionscode (CSS, Vite-Build). Playwright ist Testinfrastruktur. Trennung verhindert, dass Playwright-`node_modules` (~400 MB mit Browser-Binaries) in den Frontend-Build einlaufen.

### Opt-in Maven-Profil `e2e`

E2E-Tests dauern typisch 3–10 Minuten. Der normale `mvn clean verify` bleibt schnell (~30 Sekunden). E2E läuft explizit mit `mvn clean verify -Pe2e`.

---

## 3. Verzeichnisstruktur

```
src/test/e2e/
├── package.json                   ← Playwright-Abhängigkeit
├── playwright.config.ts           ← Haupt-Konfiguration (webServer, Browser, Viewport)
├── global-setup.ts                ← Startet MySQL via Docker Compose; erzeugt Auth-State
├── global-teardown.ts             ← Stoppt Docker Compose
├── docker-compose-e2e.yml         ← MySQL auf Port 3316 (kein Konflikt mit lokalem 3306)
├── fixtures/
│   ├── test-data.sql              ← Stammdaten + Testdatensätze (Flyway-unabhängig)
│   └── auth-state.json            ← Gespeicherter Login-State (gitignore, wird zur Laufzeit erzeugt)
└── tests/
    ├── auth.setup.ts              ← Login-Fixture (erzeugt auth-state.json)
    ├── freelancer.spec.ts
    ├── partner.spec.ts
    ├── kunde.spec.ts
    ├── project.spec.ts
    ├── profilesearch.spec.ts
    └── admin.spec.ts
```

`auth-state.json` wird in `.gitignore` aufgenommen — er wird bei jedem Testlauf neu erzeugt.

---

## 4. Infrastruktur-Orchestrierung

Maven startet nur `npx playwright test`. Playwright selbst orchestriert DB und Applikation.

### `global-setup.ts` — startet MySQL

```typescript
import { execSync } from 'child_process';
import * as path from 'path';

export default async function globalSetup() {
    const composeFile = path.resolve(__dirname, 'docker-compose-e2e.yml');
    execSync(`docker compose -f ${composeFile} up -d --wait`, { stdio: 'inherit' });
}
```

### `global-teardown.ts` — stoppt MySQL

```typescript
import { execSync } from 'child_process';
import * as path from 'path';

export default async function globalTeardown() {
    const composeFile = path.resolve(__dirname, 'docker-compose-e2e.yml');
    execSync(`docker compose -f ${composeFile} down`, { stdio: 'inherit' });
}
```

### `docker-compose-e2e.yml` — MySQL auf Port 3316

```yaml
services:
  mysql-e2e:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: powerstaff_e2e
      MYSQL_USER: powerstaff
      MYSQL_PASSWORD: powerstaff
    ports:
      - "3316:3306"
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 2s
      timeout: 5s
      retries: 15
```

### `playwright.config.ts` — startet Spring Boot JAR

```typescript
import { defineConfig, devices } from '@playwright/test';
import * as path from 'path';

const JAR = path.resolve(__dirname, '../../../target/powerstaff-1.0-SNAPSHOT.jar');
const PORT = 8090;

export default defineConfig({
    globalSetup: './global-setup.ts',
    globalTeardown: './global-teardown.ts',

    webServer: {
        command: `java -jar ${JAR} --server.port=${PORT} --spring.profiles.active=e2e`,
        url: `http://localhost:${PORT}/actuator/health`,
        reuseExistingServer: !process.env.CI,
        timeout: 60_000,
    },

    use: {
        baseURL: `http://localhost:${PORT}`,
        locale: 'de-DE',
        timezoneId: 'Europe/Berlin',
        viewport: { width: 1440, height: 900 },
        screenshot: 'only-on-failure',
        video: 'retain-on-failure',
        trace: 'on-first-retry',
        storageState: 'fixtures/auth-state.json',
    },

    projects: [
        // Login-Fixture läuft zuerst, ohne storageState
        { name: 'setup', testMatch: /auth\.setup\.ts/, use: { storageState: undefined } },
        // Alle anderen Tests verwenden den gespeicherten Auth-State
        {
            name: 'chromium',
            use: { ...devices['Desktop Chrome'] },
            dependencies: ['setup'],
        },
    ],

    reporter: [['html', { outputFolder: 'playwright-report' }]],
    retries: process.env.CI ? 1 : 0,
    workers: process.env.CI ? 1 : 2,
    forbidOnly: !!process.env.CI,
});
```

### `application-e2e.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3316/powerstaff_e2e
spring.datasource.username=powerstaff
spring.datasource.password=powerstaff
```

### LlmService-Stub für das e2e-Profil

Im E2E-Profil wird `LlmService` durch eine `@Profile("e2e")`-Implementierung ersetzt, die deterministisch antwortet:

```java
@Service
@Profile("e2e")
class StubLlmService implements LlmService {
    @Override
    public String sendMessage(Optional<Long> context, List<ProfileSearchMessage> history, String message) {
        return "E2E-Stub: Keine KI-Antwort im Testmodus.";
    }
}
```

---

## 5. Testdaten-Strategie

`fixtures/test-data.sql` wird von `global-setup.ts` nach dem MySQL-Start per `mysql`-CLI eingespielt. Es enthält:

- Einen Admin-User `testuser` mit bekanntem Passwort (BCrypt-Hash)
- Stammdaten: Historientypen, Positionsstatus (inkl. Default), Tags
- Eine Handvoll Basisdatensätze (2–3 Freiberufler, 1 Partner, 1 Kunde, 1 Projekt) für Lesetests

**Regel:** Jeder Test der Daten schreibt, erzeugt seine eigenen Datensätze mit eindeutigem Bezeichner (z.B. Timestamp-Suffix im Namen) und löscht sie im `afterEach`-Block oder toleriert den Folgelauf. Lesetests arbeiten ausschließlich auf den Stammdaten aus `test-data.sql`.

**Kein Flyway im E2E-Test-Datensatz:** `test-data.sql` ist unabhängig vom Flyway-Schema und wird nach der Flyway-Migration eingespielt. Damit ist er nicht in der Versionierungskette der Migrationen und kann frei angepasst werden.

---

## 6. Authentifizierung

Login erfolgt einmalig in `auth.setup.ts` und wird als Cookie-State in `fixtures/auth-state.json` gespeichert:

```typescript
// auth.setup.ts
import { test as setup } from '@playwright/test';

setup('authenticate', async ({ page }) => {
    await page.goto('/login');
    await page.getByTestId('field-username').fill('testuser');
    await page.getByTestId('field-password').fill('testpass');
    await page.getByTestId('btn-login').click();
    await page.waitForURL('/profilesearch/**');
    await page.context().storageState({ path: 'fixtures/auth-state.json' });
});
```

Alle anderen Tests starten mit dem gespeicherten Auth-State. Kein einzelner Test loggt sich selbst ein.

---

## 7. data-testid Konvention

`data-testid`-Attribute werden in Thymeleaf-Templates und JS-generierten DOM-Elementen vergeben. Sie sind ausschließlich für Tests bestimmt und haben keine semantische oder visuelle Bedeutung.

### Statische Elemente (einmalig pro Seite)

Einfacher, beschreibender Bezeichner:

```html
<!-- Toolbar-Buttons -->
<button data-testid="btn-save">💾 Speichern</button>
<button data-testid="btn-delete">🗑 Löschen</button>
<button data-testid="btn-new">＋ Neu</button>
<button data-testid="btn-search">🔍 Suchen</button>

<!-- Formularfelder -->
<input data-testid="field-firstname" ...>
<input data-testid="field-code" ...>

<!-- Banners -->
<!-- Bestehende IDs (banner-success, banner-duplicate-code etc.) werden beibehalten;
     data-testid ist redundant wenn eine ID bereits vorhanden ist. -->
```

### Listenelemente mit Datenbank-ID (Entitäten)

Das Entity-ID wird in den `data-testid`-Wert eingebettet. Damit ist jedes Element eindeutig adressierbar:

```html
<!-- Suchergebnistabelle (search-results.html) -->
<tr th:each="r : ${results}"
    th:attr="data-testid='freelancer-row-' + ${r.id()}">

<!-- Kontaktliste (contact-list.html) -->
<div class="citem"
     th:each="contact : ${contacts}"
     th:attr="data-testid='contact-item-' + ${contact.id}">

<!-- Historien-Einträge -->
<div class="hitem"
     th:each="entry : ${history}"
     th:attr="data-testid='history-item-' + ${entry.id}">

<!-- Chat-Seitenleiste -->
<div class="chat-session-item"
     th:attr="data-testid='chat-session-' + ${chat.id()}">

<!-- Projektpositionen -->
<div class="position-item"
     th:attr="data-testid='position-item-' + ${pos.id}">
```

### Listenelemente ohne persistente ID (JS-generiert, noch nicht gespeichert)

Für neue Kontakte oder Historieneinträge die noch nicht in der DB sind, vergibt JavaScript einen Index-basierten `data-testid` beim Erzeugen des DOM-Elements:

```javascript
// Beim Hinzufügen eines neuen Kontakts im Delta-Command-Pattern:
const index = document.querySelectorAll('[data-testid^="contact-item-new-"]').length;
item.dataset.testid = `contact-item-new-${index}`;
```

### Playwright-Zugriff auf Listenelemente

```typescript
// Spezifisches Element per Entity-ID (wenn ID aus Test-Setup bekannt):
await page.locator('[data-testid="freelancer-row-42"]').click();

// Erstes Element einer Liste:
await page.locator('[data-testid^="freelancer-row-"]').first().click();

// Anzahl prüfen:
await expect(page.locator('[data-testid^="freelancer-row-"]')).toHaveCount(3);

// Inhalt eines spezifischen Listenelements:
await expect(page.locator('[data-testid="contact-item-7"] .cval')).toContainText('test@example.com');
```

---

## 8. Stabile Assertions und Wartestrategien

### Kein hartes Warten (`page.waitForTimeout`)

`waitForTimeout` ist immer falsch. Playwright wartet automatisch auf Sichtbarkeit und Interaktivität. Für AJAX-Operationen explizit auf das Ergebnis warten:

```typescript
// Nach Speichern via fetch(): auf Erfolgsbanner warten
await page.getByTestId('btn-save').click();
await expect(page.locator('#banner-success')).toBeVisible();

// Oder auf Netzwerkantwort warten:
const [response] = await Promise.all([
    page.waitForResponse(r => r.url().includes('/freelancer/save') && r.status() === 200),
    page.getByTestId('btn-save').click(),
]);
```

### Keine exakten Assertions auf dynamische Inhalte

```typescript
// Falsch — Datum ändert sich täglich:
await expect(page.locator('#tb-audit')).toHaveText('Erfasst: 22.03.2026 testuser');

// Richtig — nur prüfen ob Audit-Info vorhanden:
await expect(page.locator('#tb-audit')).toContainText('Erfasst:');

// Richtig — auf Sichtbarkeit statt Inhalt prüfen:
await expect(page.locator('#tb-audit')).toBeVisible();
```

### ARIA-Rollen und semantische Locators bevorzugen

```typescript
// Besser als CSS-Selektoren:
await page.getByRole('button', { name: 'Speichern' }).click();
await page.getByLabel('Vorname').fill('Max');
```

---

## 9. Visuelle Regressionstests

Playwright's `toHaveScreenshot()` wird **sparsam** und nur für layout-kritische Bereiche verwendet:

```typescript
// Toolbar-Layout auf korrekter Breite und Ausrichtung
await expect(page.locator('#toolbar')).toHaveScreenshot('toolbar-freelancer.png');

// Chat-Formular mit fcard-Rahmen
await expect(page.locator('#chat-page')).toHaveScreenshot('chat-layout.png');
```

**Golden Screenshots werden ausschließlich auf CI erzeugt** (`--update-snapshots` nur im CI-Workflow) um Plattformdifferenzen (macOS vs. Linux Font-Rendering) zu vermeiden. Lokal dienen sie nur zur Verifizierung.

Toleranz für minimale Rendering-Unterschiede:
```typescript
await expect(page.locator('#toolbar')).toHaveScreenshot('toolbar.png', {
    maxDiffPixelRatio: 0.01,
});
```

---

## 10. Parallelität und Wiederholbarkeit

| Einstellung | Lokal | CI |
|---|---|---|
| `workers` | 2 | 1 |
| `retries` | 0 | 1 |
| `forbidOnly` | false | true |

**Begründung `workers: 1` auf CI:** Schreibende Tests gegen eine gemeinsame DB können bei paralleler Ausführung Race Conditions erzeugen. Serielle Ausführung ist zunächst die sichere Wahl; Parallelität kann schrittweise erhöht werden sobald Test-Isolation nachgewiesen ist.

**`retries: 1` auf CI:** Fängt echte Infrastruktur-Flakiness ab (Container-Startup-Timing, Netzwerk). Retries niemals als Lösung für inhärent instabile Tests verwenden.

---

## 11. Maven-Integration

Die E2E-Tests sind in einem eigenen Maven-Profil `e2e` gekapselt. Das Profil bindet `exec-maven-plugin` an die `integration-test`-Phase und wird nach den bestehenden Failsafe-`*IT`-Tests ausgeführt (Reihenfolge durch Plugin-Deklarationsreihenfolge in `pom.xml`).

**Ablauf mit `-Pe2e`:**
```
mvn clean verify -Pe2e

generate-resources  → npm install + vite build (Frontend)
package             → Spring Boot JAR wird gebaut  ← Pflicht: JAR muss vor E2E existieren
integration-test    → Failsafe: alle *IT (eigene Testcontainers-DBs, unberührt)
                    → exec-plugin: npx playwright test
                         └─ globalSetup:   docker compose up (MySQL:3316)
                         └─ auth.setup.ts: Login, speichert auth-state.json
                         └─ webServer:     java -jar ... --spring.profiles.active=e2e --server.port=8090
                         └─ Tests laufen
                         └─ globalTeardown: docker compose down
verify              → Failsafe prüft *IT-Ergebnisse
                    → exec-plugin prüft Playwright-Exit-Code
```

**Ohne `-Pe2e`:** Identisch mit dem bisherigen Verhalten — kein Playwright, keine Docker-Compose-DB.

---

## 12. GitHub Actions CI

```yaml
# .github/workflows/e2e.yml
name: E2E Tests

on:
  push:
    branches: [main]
  pull_request:

jobs:
  e2e:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          java-version: '25'
          distribution: 'temurin'

      - uses: actions/setup-node@v4
        with:
          node-version: '22'

      - name: Cache Playwright-Browser
        uses: actions/cache@v4
        with:
          path: ~/.cache/ms-playwright
          key: playwright-${{ hashFiles('src/test/e2e/package-lock.json') }}

      - name: Playwright-Abhängigkeiten installieren
        run: npm ci
        working-directory: src/test/e2e

      - name: Playwright-Browser installieren
        run: npx playwright install --with-deps chromium
        working-directory: src/test/e2e

      - name: Maven Build + E2E Tests
        run: ./mvnw clean verify -Pe2e

      - name: Playwright-Report hochladen (immer, auch bei Fehler)
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: playwright-report
          path: src/test/e2e/playwright-report/
          retention-days: 30
```

**Hinweise für CI:**
- Docker ist auf `ubuntu-latest` nativ verfügbar — kein DinD nötig
- `TESTCONTAINERS_RYUK_DISABLED=true` muss nicht gesetzt werden (Ryuk läuft auf GitHub Actions)
- `--update-snapshots` für visuelle Golden Screenshots wird manuell ausgeführt und die Ergebnisse eingecheckt

---

## 13. Testreport und Artefakte

Playwright erzeugt automatisch bei Testfehlern:

| Artefakt | Inhalt | Konfiguration |
|---|---|---|
| Screenshot | PNG des Browser-Zustands beim Fehler | `screenshot: 'only-on-failure'` |
| Video | MP4 des vollständigen Testlaufs | `video: 'retain-on-failure'` |
| Trace | ZIP mit DOM-Snapshots, Netzwerk, Console | `trace: 'on-first-retry'` |
| HTML-Report | Zusammenfassung aller Tests mit eingebetteten Artefakten | `reporter: 'html'` |

Trace-Datei lokal öffnen:
```bash
npx playwright show-trace src/test/e2e/test-results/.../trace.zip
```

Der HTML-Report wird von GitHub Actions als Artifact archiviert und ist 30 Tage abrufbar.
