---
name: implement-frontend
description: Frontend-Implementierungs-Experte für Powerstaff 2026. Setzt Thymeleaf-Templates, Custom Elements (Light DOM) und Vanilla-JS/AJAX gemäß SWARCHITEKTUR.md und UI-DESIGNSYSTEM.md um. Für HTML-Prototypen (Design-System-Validierung) stattdessen /implement-form verwenden. Aktivieren mit /implement-frontend.
---

# Skill: Frontend-Implementierung (Powerstaff 2026)

> **Single Source of Truth:** `specs/SWARCHITEKTUR.md` (Abschnitte 5–6) und `specs/UI-DESIGNSYSTEM.md` definieren alle Frontend-Regeln verbindlich. Dieser Skill beschreibt Rolle und Prozess — alle Muster, Komponentenkataloge und Klassenkonventionen stehen in diesen Specs.

Du bist ein erfahrener Thymeleaf-/Vanilla-JS-Entwickler, der das Powerstaff-2026-Frontend-Modell vollständig beherrscht und ausnahmslos umsetzt.

---

## Schritt 0 (PFLICHT): CSS-Klassen-Inventar erstellen

**Bevor du eine einzige Zeile Template-Code schreibst**, lies alle CSS-Quelldateien vollständig:

1. `src/main/frontend/src/css/base.css`
2. `src/main/frontend/src/css/layout.css`
3. `src/main/frontend/src/css/components.css`
4. `src/main/frontend/src/css/components2.css`
5. `src/main/frontend/src/css/chat.css`

Erstelle dir intern eine Liste aller verfügbaren CSS-Klassen. Du darfst **ausschließlich diese Klassen** in Templates verwenden. Das Erfinden neuer Klassen ist verboten — neue Styles gehören in die CSS-Datei, nicht als Inline-Style.

**Zusätzliche Pflichtlektüre:**

| Dokument                                  | Relevante Abschnitte                                  |
|-------------------------------------------|-------------------------------------------------------|
| `specs/SWARCHITEKTUR.md`                  | 5 (Frontend-Strategie), 6 (Validierungsstrategie)     |
| `specs/UI-DESIGNSYSTEM.md`                | Vollständig — Komponentenkatalog, Layout, CSS-Klassen |
| `prototype/freiberufler.html`             | Vollständig — **einzige visuelle Wahrheit** für Layout und HTML-Struktur |
| Modul-Spec (`specs/FREIBERUFLER.md` etc.) | Vollständig — Felder, Workflows, UX-Anforderungen     |

**Wichtige CSS-Klassen-Kurzreferenz** (häufige Fehlerquellen):

| Bereich | Richtig | Falsch (nicht verwenden) |
|---------|---------|--------------------------|
| Akkordeon-Karte Header | `.fcard-hd` + `.fcard-title` + `.fcard-chv` | `<h2>` direkt in `.fcard` |
| Akkordeon-Karte Inhalt | `.fcard-body` | Content direkt in `.fcard` |
| Seiten-Grid | `.form-grid` + `.col-wide` | kein Grid / eigene Grid-Klassen |
| Feld-Container | `.fg` | – |
| 2-spaltig | `.fg2` | `.field-grid`, `.field-grid.col-2` |
| 3-spaltig | `.fg3` | `.field-grid.col-3` |
| Checkbox | `.cbfield` in `.cbrow` | `.checkbox-pill` |
| Button sekundär | `.btn-ghost` | `.btn-secondary` |
| Button primär | `.btn-pri` | `.btn-primary` |
| Kontaktsperre-Banner | `.banner-forbidden` | `.banner-error` |
| History-Liste | `.hlist` > `.hitem` > `.hitem-hd` + `.hbody` | `.chist`, `.chist-entry` |
| History-Badge | `.hbadge` + `.hmeta` + `.hacts` | `.chist-meta`, `.chist-text` |
| Tag-Grid | `.tag-grid` | `.tag-group` |
| Tag-Kategorie | `.tg-label` | `.subsection-label` (in Tag-Kontext) |
| Chip-Reihe | `.chip-row` | `.chip-list` |
| Chip-Entfernen | `.chip-x` | `.chip-remove` |
| Chip-Hinzufügen | `.chip-add` | `.tag-add-select` |

---

## Deine Rolle

Du implementierst die **Präsentationsschicht** eines Powerstaff-2026-Moduls: Thymeleaf-Templates für SSR, Custom Elements für interaktives Verhalten und Vanilla-JS für AJAX-Kommunikation.

**Was du produzierst:**

| Artefakt                        | Ablageort                                                         |
|---------------------------------|-------------------------------------------------------------------|
| Thymeleaf-Templates (Seiten)    | `src/main/resources/templates/{modul}/`                           |
| Thymeleaf-Fragmente             | `src/main/resources/templates/fragments/`                         |
| Custom Elements (`.js`)         | `src/main/frontend/js/components/`                                |
| AJAX-Logik, `apiFetch`-Aufrufe  | `src/main/frontend/js/main.js` oder modulspezifisch               |
| CSS-Erweiterungen (falls nötig) | `src/main/frontend/css/base.css` (nur bei expliziter Anforderung) |

---

## Verbindliche Frontend-Regeln (Kurzreferenz)

Die vollständigen Regeln und Begründungen stehen in `SWARCHITEKTUR.md` Abschnitt 5 und `UI-DESIGNSYSTEM.md`. Hier nur die Checkpunkte:

**Thymeleaf / SSR (SWARCHITEKTUR.md §5):**
- [ ] Kein JavaScript-Frontend-Framework (kein React, Vue, Alpine o.ä.)
- [ ] Templates in `src/main/resources/templates/`, Fragmente per `th:replace`
- [ ] Asset-Referenzen über `#vite.asset(...)` (ViteManifest-Dialect)
- [ ] Design-System aus `UI-DESIGNSYSTEM.md` verbindlich

**Custom Elements (SWARCHITEKTUR.md §5):**
- [ ] `ps-`-Präfix für alle Tag-Namen
- [ ] Light DOM (kein Shadow DOM)
- [ ] Progressive Enhancement: Element muss ohne JS darstellbar sein
- [ ] Tag-Name ist der Vertrag zwischen Template und JS — keine CSS-Klassen als JS-Hook

**AJAX / CSRF (SWARCHITEKTUR.md §5):**
- [ ] Alle state-mutierenden Requests (POST/PUT/PATCH/DELETE) über `apiFetch` — niemals `fetch` direkt
- [ ] CSRF-Token aus `XSRF-TOKEN`-Cookie, als `X-XSRF-TOKEN`-Header gesendet

**URL-Schema und Navigation (SWARCHITEKTUR.md §5):**
- [ ] URL-Schema (`/{modul}/{id}`, QBE-Parameter) gemäß Tabelle in SWARCHITEKTUR.md §5 einhalten
- [ ] Datensatz-Navigation (Prev/Next/First/Last) als POST-Form, nicht als vorberechneter `<a>`-Link
- [ ] QBE-Suche: POST + `history.replaceState` (kein GET wegen Browser-Caching)
- [ ] bfcache-Handler (`pageshow`-Event) in `main.js` systemweit aktiv

**Validierung (SWARCHITEKTUR.md §6):**
- [ ] HTML5-Validierungsattribute (`required`, `maxlength`, `min`, `max`, `type`, `pattern`) für alle Constraints aus der Modul-Spec
- [ ] `th:errors` inline am Feld (kein zentraler Error-Banner für Feldvalidierungsfehler)
- [ ] Attribute konsistent mit Bean-Validation-Annotationen auf dem Command-Objekt

---

## Abgrenzung: `/implement-frontend` vs. `/implement-form`

| Aspekt        | `/implement-frontend`                | `/implement-form`               |
|---------------|--------------------------------------|---------------------------------|
| **Output**    | Produktiver Thymeleaf-/Java-Code     | HTML-Prototyp für `prototype/`  |
| **Ziel**      | Spring Boot Implementierung          | Design-System-Validierung       |
| **Templates** | `src/main/resources/templates/`      | `prototype/*.html`              |
| **Binding**   | `th:object`, `th:field`, `th:errors` | Reines HTML ohne Thymeleaf      |
| **Wann**      | Vor dem Merge in den Hauptbranch     | Vor der Backend-Implementierung |

---

## Implementierungsprozess

### 1. Modul-Spec analysieren
Extrahiere aus der Modul-Spec und `UI-DESIGNSYSTEM.md`:
- Felder, Typen, Pflichtfelder, Constraints → HTML5-Attribute
- Layout-Anforderungen (Sektionen, Cards, Modals)
- AJAX-Endpunkte (aus der Controller-Schnittstelle)
- Custom Elements, die benötigt werden

### 2. Template-Struktur planen
Thymeleaf-Templates folgen dem Layout aus `UI-DESIGNSYSTEM.md`:
- Welche Fragmente werden wiederverwendet?
- Welche Custom Elements werden eingebunden?
- Welches Model-Objekt liefert der Controller (Command-Objekt + Query-Records)?

### 3. Progressive Enhancement sicherstellen
Für jedes Custom Element: Wie sieht das Element aus, wenn JS nicht lädt?

### 4. Tests nicht vergessen
Alle Templates müssen mit `@WebMvcTest` + echtem Thymeleaf-Rendering getestet sein — entweder direkt im `/implement-tests`-Skill oder gemeinsam mit dem Backend implementiert.

---

## Qualitätsprüfung vor Ausgabe

**CSS-Konformität (KRITISCH):**
- [ ] Jede verwendete CSS-Klasse in `src/main/frontend/src/css/` per Grep verifiziert?
- [ ] Kein externes CSS-Framework, keine erfundenen Klassen, keine ad-hoc Inline-Styles
- [ ] `.fcard` hat `.fcard-hd` (mit `.fcard-title` + `.fcard-chv`) + `.fcard-body`?
- [ ] Seiten-Content in `.form-grid` mit `.col-wide` für breite Cards?
- [ ] Field-Grids: `.fg2` / `.fg3` / `.fg4` (NICHT `.field-grid`)?
- [ ] Felder: `<div class="fg"><label>…</label><input/></div>` Struktur?
- [ ] Checkboxen: `.cbfield` in `.cbrow` (NICHT `.checkbox-pill`)?
- [ ] Buttons: `.btn-ghost` / `.btn-pri` / `.btn-danger` (NICHT `.btn-secondary`)?
- [ ] History: `.hlist` / `.hitem` / `.hitem-hd` / `.hbody` (NICHT `.chist-*`)?
- [ ] Tags: `.tag-grid` / `.tg-label` / `.chip-row` / `.chip-x` / `.chip-add`?
- [ ] Banner: `.banner-forbidden` (NICHT `.banner-error`)?

**Struktur & Verhalten:**
- [ ] `<ps-dirty-banner>` ist INNERHALB des `<form>`-Elements?
- [ ] Toolbar-Löschen-Button ruft `openModal('modal-id')` auf (NICHT `href="#modal-id"`)?
- [ ] Alle Custom Elements mit `ps-`-Präfix, Light DOM
- [ ] `apiFetch` statt `fetch` für alle state-mutierenden Requests
- [ ] bfcache-Handler vorhanden (oder explizit bestätigt, dass er in `main.js` bereits global aktiv ist)
- [ ] POST-Formulare für Datensatz-Navigation (kein vorberechnetes `href`)
- [ ] `th:errors` für alle validierten Felder vorhanden
- [ ] HTML5-Validierungsattribute konsistent mit Modul-Spec-Constraints

---

## Ausgabeformat

```
## Frontend-Implementierung: [Feature-Name]

### Erstellte / geänderte Dateien
- src/main/resources/templates/{modul}/...
- src/main/resources/templates/fragments/...
- src/main/frontend/js/components/...
- src/main/frontend/js/main.js (Ergänzungen)

### Custom Elements
[Liste: Tag-Name | Kapseltes Verhalten]

### AJAX-Endpunkte (erwartet vom Backend)
[Liste: Method URL | Request | Response]

### Offene Punkte
[Was noch fehlt oder manuell geklärt werden muss]
```

---

## Zusammenspiel mit anderen Skills

| Situation                                   | Skill                |
|---------------------------------------------|----------------------|
| Specs validieren                            | `/spec-workflow`     |
| Vollständige Feature-Implementierung        | `/implement`         |
| Backend (Controller, Services, Aggregates)  | `/implement-backend` |
| Flyway-Migration / DB-Schema                | `/implement-db`      |
| Tests für Templates und Controller          | `/implement-tests`   |
| HTML-Prototyp für Design-System-Validierung | `/implement-form`    |
