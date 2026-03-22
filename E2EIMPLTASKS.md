# E2E-Implementierungs-Todos

Jeder Task wird einzeln implementiert, mit `mvn clean verify -Pe2e` geprüft (sobald Profil existiert,
vorher `mvn clean verify`) und via Git committet.

Vollständiges Konzept: `specs/E2ETESTS.md`

---

## Phase 1 – Infrastruktur aufbauen

- [x] **E2E-Verzeichnis anlegen**
  `src/test/e2e/` mit `.gitignore` (enthält `node_modules/`, `playwright-report/`, `test-results/`, `fixtures/auth-state.json`)

- [x] **`package.json` und `playwright.config.ts` anlegen**
  - Playwright-Abhängigkeit (`@playwright/test`)
  - Browser: Chromium, Viewport 1440×900, Locale `de-DE`, Timezone `Europe/Berlin`
  - `screenshot: 'only-on-failure'`, `video: 'retain-on-failure'`, `trace: 'on-first-retry'`
  - `workers: CI ? 1 : 2`, `retries: CI ? 1 : 0`, `forbidOnly: !!CI`
  - `webServer` zeigt auf `target/powerstaff-1.0-SNAPSHOT.jar` mit `--server.port=8090 --spring.profiles.active=e2e`
  - Reporter: HTML nach `playwright-report/`

- [x] **`docker-compose-e2e.yml` anlegen**
  MySQL 8.0 auf Port 3316, Datenbank `powerstaff_e2e`, Healthcheck

- [x] **`global-setup.ts` und `global-teardown.ts` anlegen**
  `docker compose up -d --wait` / `docker compose down`

- [x] **`src/main/resources/application-e2e.properties` anlegen**
  Datasource zeigt auf MySQL:3316

- [x] **`StubLlmService` für `@Profile("e2e")` implementieren**
  Gibt deterministisch eine feste Antwort zurück, ohne echten LLM-Aufruf.
  Liegt in `src/main/java/.../profilesearch/` (Produktionscode, aber profilgebunden).

- [x] **`fixtures/test-data.sql` anlegen**
  Enthält:
  - Admin-User `testuser` mit BCrypt-Passwort `testpass`
  - Mindestens 2 Historientypen
  - Mindestens 2 Positionsstatus (einer davon `isDefault = true`)
  - Mindestens 2 Tags
  - 3 Freiberufler (davon einer mit Kontakthistorie und Kontaktmöglichkeit)
  - 1 Partner
  - 1 Kunde
  - 1 Projekt (mit mindestens einer Position)

- [x] **`auth.setup.ts` anlegen**
  Login mit `testuser`/`testpass`, speichert `fixtures/auth-state.json`

- [x] **Maven-Profil `e2e` in `pom.xml` einbauen**
  - `npm ci` in `pre-integration-test` (Verzeichnis `src/test/e2e`)
  - `npx playwright test` via exec-maven-plugin in `integration-test`-Phase
  - Exit-Code-Prüfung via failOnError

- [x] **GitHub Actions Workflow `.github/workflows/e2e.yml` anlegen**
  - Java 25, Node 22
  - Playwright-Browser-Cache auf `~/.cache/ms-playwright`
  - `npx playwright install --with-deps chromium`
  - `mvn clean verify -Pe2e`
  - Upload `playwright-report/` als Artifact (retention: 30 Tage)

---

## Phase 2 – data-testid Attribute in Templates vergeben

### Regel für Listen: ID-basiertes Schema

Listenelemente mit DB-ID erhalten `data-testid="{modul}-{typ}-{id}"`.
Listenelemente ohne DB-ID (JS-generiert, noch nicht gespeichert) erhalten `data-testid="{modul}-{typ}-new-{index}"` (Index wird von JS beim Erzeugen gesetzt).

### Fragment: `fragments/toolbar.html`

- [x] Toolbar-Buttons mit `data-testid` versehen:
  - `btn-save` (Speichern)
  - `btn-delete` (Löschen)
  - `btn-new` (Neu)
  - `btn-search` (Suchen)
  - `btn-clear` (Leeren)
  - `btn-assign-project` (Zuordnen)
  - `btn-nav-first`, `btn-nav-prev`, `btn-nav-next`, `btn-nav-last`
  - `input-nav-id` (ID-Eingabefeld)

### Fragment: `fragments/contact-list.html`

- [x] `<div class="citem">` erhält `th:attr="data-testid='contact-item-' + ${contact.id}"`
- [x] Bearbeiten-Link: `data-testid="contact-edit-{id}"` (via th:attr)
- [x] Löschen-Link: `data-testid="contact-delete-{id}"` (via th:attr)

### Template: `login.html`

- [x] `data-testid="field-username"` am Username-Input
- [x] `data-testid="field-password"` am Password-Input
- [x] `data-testid="btn-login"` am Submit-Button

### Template: `freelancer/form.html`

- [x] Formularfelder Adresse: `field-firstname`, `field-lastname`, `field-company`, `field-street`, `field-country`, `field-zip`, `field-city`
- [x] Formularfelder Profil: `field-code`, `field-availability`, `field-salary`, `field-skills`
- [x] Checkbox `field-contact-forbidden`
- [ ] Historien-Einträge: `th:attr="data-testid='history-item-' + ${entry.id}"`
- [x] Historien-Bearbeiten-Button: `data-testid="history-edit-{id}"`
- [x] Historien-Löschen-Button: `data-testid="history-delete-{id}"`
- [x] "Kontakthistorie hinzufügen"-Button: `data-testid="btn-add-history"`
- [x] Tag-Chips: `th:attr="data-testid='tag-chip-' + ${tag.tagId}"`

### Template: `freelancer/search-page.html` + `search-results.html`

- [x] Suchergebnis-Zeilen: `th:attr="data-testid='freelancer-row-' + ${r.id()}"`
- [x] QBE-Felder: `field-search-firstname`, `field-search-lastname`, `field-search-code` etc.

### Template: `partner/form.html`

- [x] Analog zu `freelancer/form.html`: Adressfelder, Historieneinträge, Kontaktliste
- [x] Zugeordnete Freiberufler-Liste: `th:attr="data-testid='partner-freelancer-row-' + ${f.id()}"`

### Template: `partner/search-results.html`

- [x] Suchergebnis-Zeilen: `th:attr="data-testid='partner-row-' + ${r.id()}"`

### Template: `kunde/form.html`

- [x] Analog zu `partner/form.html`
- [x] Zugeordnete Projekte-Liste: `th:attr="data-testid='kunde-project-row-' + ${p.id()}"`

### Template: `kunde/search-results.html`

- [x] Suchergebnis-Zeilen: `th:attr="data-testid='kunde-row-' + ${r.id()}"`

### Template: `project/form.html`

- [x] Formularfelder: `field-project-number`, `field-short-description`, `field-description`
- [x] Projektpositionen: `th:attr="data-testid='position-item-' + ${pos.id}"`
- [x] Position-Bearbeiten-Button: `data-testid="position-edit-{id}"`
- [x] Position-Löschen-Button: `data-testid="position-delete-{id}"`
- [x] Historieneinträge analog Freiberufler

### Template: `project/search-results.html`

- [x] Suchergebnis-Zeilen: `th:attr="data-testid='project-row-' + ${r.id()}"`

### Template: `profilesearch/form.html`

- [x] Chat-Seitenleisten-Einträge: `th:attr="data-testid='chat-session-' + ${chat.id()}"`
- [x] Chat-Eingabefeld: `data-testid="chat-input"`
- [x] Senden-Button: `data-testid="btn-chat-send"`
- [x] Nachrichten-Container: `data-testid="chat-messages"`
- [x] Einzelne Nachrichten: JS-seitig `data-testid="chat-msg-{id}"` beim Anhängen setzen

### Admin-Templates

- [x] `admin/historientypen.html`: Tabellenzeilen `data-testid="histtype-row-{id}"`
- [x] `admin/positionsstatus.html`: Tabellenzeilen `data-testid="posstatus-row-{id}"`
- [x] `admin/tags.html`: Tabellenzeilen `data-testid="tag-row-{id}"`

---

## Phase 3 – Testspezifikationen schreiben

Für jeden Test gilt: Playwright Codegen (`npx playwright codegen http://localhost:8090`) für Interaktionen verwenden, Assertions manuell ergänzen.

### `tests/auth.setup.ts`
- [x] Login-Flow, speichert `auth-state.json`

### `tests/freelancer.spec.ts`
- [x] Neuen Freiberufler anlegen und speichern (Pflichtfelder)
- [x] Freiberufler per Code suchen, in Ergebnisliste anklicken
- [x] Kontakthistorie-Eintrag hinzufügen und speichern
- [x] Kontaktmöglichkeit hinzufügen (E-Mail) und speichern
- [x] Freiberufler einem gemerkten Projekt zuordnen (Erfolgsfall)
- [x] Freiberufler einem Projekt zuordnen, der bereits zugeordnet ist (409-Banner prüfen)

### `tests/partner.spec.ts`
- [x] Partner anlegen und speichern
- [x] Partner suchen, Ergebnisliste prüfen
- [x] Kontakthistorie hinzufügen

### `tests/kunde.spec.ts`
- [x] Kunden anlegen und speichern
- [x] Kunden suchen, Ergebnisliste prüfen

### `tests/project.spec.ts`
- [x] Projekt suchen (QBE leer → alle Ergebnisse)
- [x] Projekt öffnen, Projekthistorie hinzufügen
- [x] Freiberufler per Code einem Projekt zuordnen
- [x] Projektposition bearbeiten und speichern

### `tests/profilesearch.spec.ts`
- [x] Chat anlegen, Nachricht senden, Stub-Antwort prüfen
- [x] Chat löschen, Redirect auf verbleibenden oder neuen Chat prüfen
- [x] Neuen Chat über Toolbar anlegen

### `tests/admin.spec.ts`
- [x] Historientyp anlegen und in Liste prüfen
- [x] Positionsstatus anlegen, als Default markieren
- [x] Tag anlegen und in Liste prüfen

---

## Phase 4 – Visuelle Regressionstests

- [ ] Golden Screenshots auf CI erzeugen (`--update-snapshots`) und einchecken:
  - Toolbar im Freiberufler-Formular (`toolbar-freelancer.png`)
  - Toolbar im Projekt-Formular (`toolbar-project.png`)
  - Chat-Layout (`chat-layout.png`)
- [ ] `maxDiffPixelRatio: 0.01` als globale Toleranz in `playwright.config.ts`
- [ ] Dokumentieren: Golden Screenshots werden nur via CI-Workflow aktualisiert, nie lokal

---

## Phase 5 – Stabilisierung

- [ ] Alle Tests mindestens 3× lokal ohne Fehler durchlaufen lassen
- [ ] Auf CI: E2E-Workflow mindestens 5× grün, ohne Retries
- [ ] Bekannte Flakiness-Ursachen identifizieren und beheben (häufig: fehlende AJAX-Waits)
- [ ] `E2EIMPLTASKS.md` vollständig abgehakt → Eintrag in `SWARCHITEKTUR.md` Abschnitt 9 (Testarchitektur) ergänzen
