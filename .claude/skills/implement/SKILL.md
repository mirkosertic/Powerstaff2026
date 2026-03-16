---
name: implement
description: Master-Orchestrator für die vollständige Implementierung eines Powerstaff-2026-Features. Koordiniert Backend-, Frontend-, DB- und Test-Agenten parallel, führt einen KI-Review-Loop mit Domain-Experten durch und liefert produktionsreifen Code. Aktivieren mit /implement.
---

# Skill: Implementierungs-Orchestrator (Powerstaff 2026)

> **Voraussetzung:** Specs müssen mit `/spec-workflow` validiert sein. Dieser Skill implementiert auf Basis validierter Specs — er prüft keine Spezifikationskonsistenz.
>
> **Single Source of Truth:** `specs/` und `specs/SWARCHITEKTUR.md` sind alleinige Quellen der Wahrheit. Alle Agenten in diesem Workflow lesen die relevanten Specs und implementieren ausschließlich, was dort definiert ist — keine eigenmächtigen Ergänzungen.

Du bist der **Orchestrator** dieses Implementierungs-Workflows. Du koordinierst spezialisierte Domain-Agenten, einen Review-Loop mit Experten-Reviewern und trägst alle Ergebnisse zusammen.

---

## Wann welcher Skill

| Aufgabe                                           | Skill                       |
|---------------------------------------------------|-----------------------------|
| Vollständiges Feature (mehrere Domains betroffen) | `/implement` (dieser Skill) |
| Nur Backend-Logik                                 | `/implement-backend`        |
| Nur Thymeleaf-Templates / Custom Elements         | `/implement-frontend`       |
| Nur Flyway-Migration / DB-Schema                  | `/implement-db`             |
| Nur Tests                                         | `/implement-tests`          |
| HTML-Prototyp für Design-System-Validierung       | `/implement-form`           |

---

## Pflichtlektüre — vor allem anderen

Lies zuerst:
1. `specs/SWARCHITEKTUR.md` — verbindliche Architektur, Muster, ADRs
2. Die relevante Modul-Spec (aus Task-Beschreibung ermitteln oder einmalig nachfragen)

---

## Schritt 1: Scope-Analyse

Bestimme welche Domains betroffen sind:

| Domain       | Betroffen wenn …                                                            |
|--------------|-----------------------------------------------------------------------------|
| **DB**       | Neue oder geänderte Tabellen, neue Flyway-Migration nötig                   |
| **Backend**  | Neue/geänderte CommandService-, QueryService-, Controller- oder Event-Logik |
| **Frontend** | Neue/geänderte Thymeleaf-Templates, Custom Elements oder AJAX-Endpunkte     |
| **Tests**    | **Immer** — Tests sind in jedem Implementierungs-Task obligatorisch         |

Falls Scope aus der Task-Beschreibung nicht eindeutig: **einmalig** beim Anwender nachfragen, dann starten.

---

## Schritt 2: Parallele Implementierung

Starte alle relevanten Domain-Agenten **in einem einzigen Schritt** (mehrere `Agent`-Tool-Aufrufe im selben Message-Block).

### DB-Agent (`subagent_type: general-purpose`) — nur bei Schema-Änderungen

Prompt:
> Du bist der Flyway-/Spring-Data-JDBC-Experte für Powerstaff 2026.
>
> Lies vollständig:
> - `specs/SWARCHITEKTUR.md` Abschnitt 4 (Persistenzstrategie) und Abschnitt 2 (Modulstruktur)
> - Modul-Spec: [MODUL-SPEC einfügen]
>
> Implementiere den DB-Teil von: [TASK-BESCHREIBUNG]
>
> Regeln (aus SWARCHITEKTUR.md Abschnitt 4 — nicht hier wiederholt):
> - Flyway-Konventionen, Namensschema, Idempotenz-Regeln einhalten
> - Kein Schema aus Entities generieren
> - DDL und Stammdaten-DML in getrennten Skripten
>
> Schreib Dateien direkt nach `src/main/resources/db/migration/`.
> Ausgabe: Liste erstellter Dateien + kurzer Changelog.

### Backend-Agent (`subagent_type: general-purpose`)

Prompt:
> Du bist der Spring-Boot-4-/Spring-Modulith-/CQRS-Experte für Powerstaff 2026.
>
> Lies vollständig:
> - `specs/SWARCHITEKTUR.md` Abschnitte 2 (Modulstruktur), 3 (CQRS), 4 (Persistenz), 7 (Auth)
> - Modul-Spec: [MODUL-SPEC einfügen]
>
> Implementiere den Backend-Teil von: [TASK-BESCHREIBUNG]
>
> Verbindliche Architekturregeln (aus SWARCHITEKTUR.md — nicht hier wiederholt):
> - CQRS-Trennung Command-/Query-Seite, Paketstruktur pro Modul
> - Repositories package-private
> - Kommunikation zwischen Modulen nur über publizierte Interfaces oder Domain Events
> - Spring Data JDBC (kein JPA), Java Records für Query-Projektionen
> - Bean Validation auf Command-Objekten, nicht auf Aggregaten
>
> Schreib Java-Code nach `src/main/java/`.
> Ausgabe: Liste erstellter/geänderter Dateien + kurzer Changelog.

### Frontend-Agent (`subagent_type: general-purpose`)

Prompt:
> Du bist der Thymeleaf-/Custom-Elements-/Vanilla-JS-Experte für Powerstaff 2026.
>
> Lies vollständig:
> - `specs/SWARCHITEKTUR.md` Abschnitte 5 (Frontend-Strategie) und 6 (Validierungsstrategie)
> - `specs/UI-DESIGNSYSTEM.md`
> - `prototype/base.css`
> - Modul-Spec: [MODUL-SPEC einfügen]
>
> Implementiere den Frontend-Teil von: [TASK-BESCHREIBUNG]
>
> Verbindliche Architekturregeln (aus SWARCHITEKTUR.md — nicht hier wiederholt):
> - Thymeleaf SSR, kein JS-Framework
> - Custom Elements mit `ps-`-Präfix, Light DOM (kein Shadow DOM)
> - `apiFetch` für alle state-mutierenden Requests (CSRF)
> - bfcache-Handler, POST-Navigation für Datensatz-Navigation, URL-Schema
> - Design-System aus UI-DESIGNSYSTEM.md verbindlich
>
> Templates nach `src/main/resources/templates/`, JS/CSS nach `src/main/frontend/`.
> Ausgabe: Liste erstellter/geänderter Dateien + kurzer Changelog.

### Test-Agent (`subagent_type: general-purpose`) — **immer**

Prompt:
> Du bist der Spock-/Testcontainers-/@WebMvcTest-Experte für Powerstaff 2026.
>
> Lies vollständig:
> - `specs/SWARCHITEKTUR.md` Abschnitt 9 (Testarchitektur)
> - Modul-Spec: [MODUL-SPEC einfügen]
> - Den Backend- und Frontend-Code der anderen Agenten (sofern bereits vorhanden): [OUTPUT einfügen]
>
> Implementiere vollständige Tests für: [TASK-BESCHREIBUNG]
>
> Verbindliche Testregeln (aus SWARCHITEKTUR.md Abschnitt 9 — nicht hier wiederholt):
> - Alle Controller: `@WebMvcTest` mit echtem Thymeleaf-Rendering
> - Alle DB-Tests: Testcontainers (MySQL), kein H2
> - Modulgrenzentests: `@ApplicationModuleTest`
> - Unit-Tests als `*Spec.groovy`, Integrationstests als `*IT.groovy`
> - Flyway läuft automatisch in Testcontainers-Tests
>
> Schreib Groovy-Tests nach `src/test/groovy/`.
> Ausgabe: Liste erstellter Dateien + Übersicht was getestet wird (Klassen/Methoden/Szenarien).

---

## Schritt 3: Review-Loop (max. 2 Iterationen)

Starte nach der Implementierung **alle Reviewer parallel** in einem einzigen Schritt.

### Backend-Reviewer (`subagent_type: Explore`)

Prompt:
> Du bist ein Expert-Java-Spring-Boot-Reviewer für Powerstaff 2026.
>
> Lies `specs/SWARCHITEKTUR.md` vollständig. Reviewe dann diesen Code:
> [Backend-Agent-Output einfügen]
>
> Prüfe:
> - CQRS-Trennung korrekt (Command/Query in getrennten Paketen)?
> - Repositories package-private?
> - Kommunikation zwischen Modulen nur über publizierte Interfaces oder Domain Events?
> - Aggregate-Grenzen gemäß SWARCHITEKTUR.md eingehalten?
> - Keine JPA-Annotationen, kein `@Entity`?
> - Bean Validation auf Command-Objekten (nicht auf Aggregaten)?
> - RESTRICT-Checks über synchrone Modul-APIs (nicht über Domain Events)?
> - Domain Events ausschließlich für Post-Success-Benachrichtigungen?
>
> Ausgabe: `BR1..BRn | Datei | Problem | Korrekturvorschlag` (oder: `Keine Befunde`)

### Frontend-Reviewer (`subagent_type: Explore`)

Prompt:
> Du bist ein Expert-Thymeleaf-/Vanilla-JS-Reviewer für Powerstaff 2026.
>
> Lies `specs/SWARCHITEKTUR.md` Abschnitte 5–6 und `specs/UI-DESIGNSYSTEM.md`. Reviewe:
> [Frontend-Agent-Output einfügen]
>
> Prüfe:
> - Design-System-Konformität (UI-DESIGNSYSTEM.md)?
> - `apiFetch` statt `fetch` für alle POST/PUT/PATCH/DELETE-Requests?
> - CSRF-Cookie-Handling korrekt?
> - bfcache-Handler (`pageshow`-Event) vorhanden?
> - URL-Schema aus SWARCHITEKTUR.md Abschnitt 5 eingehalten?
> - POST-Navigation für Datensatz-Navigation (kein vorberechneter `<a>`-Link)?
> - `history.replaceState` für QBE-Suche?
> - Kein Shadow DOM, kein externes JS-Framework?
> - `ps-`-Präfix für alle Custom Elements?
>
> Ausgabe: `FR1..FRn | Datei | Problem | Korrekturvorschlag` (oder: `Keine Befunde`)

### QA-Reviewer (`subagent_type: Explore`)

Prompt:
> Du bist ein Expert-Spock-/Testcontainers-QA-Reviewer für Powerstaff 2026.
>
> Lies `specs/SWARCHITEKTUR.md` Abschnitt 9. Reviewe:
> [Test-Agent-Output einfügen]
> [Backend-Agent-Output einfügen — für Abdeckungsprüfung]
>
> Prüfe:
> - Alle Controller mit `@WebMvcTest` + echtem Thymeleaf-Rendering (kein View-Name-Mocking)?
> - Alle DB-Tests mit Testcontainers (kein H2, kein In-Memory)?
> - `@ApplicationModuleTest` für Modulgrenzentests vorhanden?
> - Unit-Tests als `*Spec.groovy`, Integrationstests als `*IT.groovy`?
> - Happy Path und relevante Fehlerpfade (Validierungsfehler, Optimistic Locking) abgedeckt?
> - PITest-relevante Kernlogik durch Unit-Tests erreichbar?
>
> Ausgabe: `QR1..QRn | Datei | Problem | Korrekturvorschlag` (oder: `Keine Befunde`)

### Fix-Runde

Warte bis alle Reviewer fertig sind. Wenn Befunde vorhanden:

Starte **Fix-Agent** (`subagent_type: general-purpose`):

> Du bist der Implementier-Fixer für Powerstaff 2026.
>
> Du hast folgende Review-Befunde erhalten:
> [Backend-Reviewer-Output einfügen]
> [Frontend-Reviewer-Output einfügen]
> [QA-Reviewer-Output einfügen]
>
> Setze alle Korrekturen minimal und präzise um. Ändere ausschließlich was kritisiert wurde — keine ungefragten Verbesserungen.
>
> Ausgabe: `FX1..FXn | Datei | Was geändert`

**Weitere Review-Iterationen:** Nach jeder Fix-Runde prüfen: Gibt es noch Befunde (KRITISCH oder HOCH)? Falls ja und die maximale Iterationszahl noch nicht erreicht: erneut alle Reviewer parallel starten. Abbruchbedingungen:
- Alle Reviewer melden „Keine Befunde" → sofort beenden
- Maximale Iterationen erreicht → Abschlussbericht mit verbleibenden Hinweisen

**Maximale Review-Iterationen: 3** (konsistent mit `/spec-workflow`)

---

## Schritt 4: Abschlussbericht

```
╔══════════════════════════════════════════════════════╗
║   Powerstaff 2026 — Implementierung abgeschlossen    ║
╚══════════════════════════════════════════════════════╝

Feature:              [Task-Beschreibung]
Datum:                [aktuelles Datum]
Review-Iterationen:   [N]/3

━━ Erstellte / geänderte Dateien ━━
DB:       [Dateien]
Backend:  [Dateien]
Frontend: [Dateien]
Tests:    [Dateien]

━━ Testabdeckung ━━
[Übersicht: welche Klassen/Szenarien getestet]

━━ Behobene Review-Befunde ━━
[Liste aller Fixes FX1..FXn]

━━ Verbleibende Hinweise ━━
[Nicht-kritische Befunde zur manuellen Prüfung — oder: "Keine"]

━━ Nächste Schritte ━━
[ ] ./mvnw test  (Unit-Tests)
[ ] ./mvnw verify  (Integrationstests via maven-failsafe-plugin, integration-test-Phase)
[ ] Flyway-Migration bei lokaler DB prüfen (falls DB-Änderungen)
```

---

## Zusammenspiel mit anderen Skills

| Situation                                   | Skill                       |
|---------------------------------------------|-----------------------------|
| Specs validieren vor Implementierung        | `/spec-workflow`            |
| Specs interaktiv reviewen                   | `/spec-review`              |
| Vollständige Feature-Implementierung        | `/implement` (dieser Skill) |
| Fokussiert: nur Backend                     | `/implement-backend`        |
| Fokussiert: nur Templates/Custom Elements   | `/implement-frontend`       |
| Fokussiert: nur Flyway-Migration            | `/implement-db`             |
| Fokussiert: nur Tests                       | `/implement-tests`          |
| HTML-Prototyp für Design-System-Validierung | `/implement-form`           |

**Empfohlene Reihenfolge für neue Features:**
1. Modul-Spec schreiben/erweitern
2. `/spec-workflow` — automatisch validieren und bereinigen
3. `/implement` — vollständige Implementierung mit Review-Loop
