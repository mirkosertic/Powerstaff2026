# UITASKS – UI-Angleichung Templates an Prototyp

**Ziel:** Alle Thymeleaf-Templates entsprechen dem Prototyp `prototype/freiberufler.html`
und dem Design-System `specs/UI-DESIGNSYSTEM.md`. Nur CSS-Klassen aus
`src/main/frontend/src/css/` werden verwendet.

**Ausgangszustand:** 2026-03-18 · Branch `main` · 320 Tests grün

---

## Legende

- `[x]` – abgeschlossen (git-commit vorhanden)
- `[ ]` – offen
- Betroffen: Dateien die geändert werden

---

## U.1 – CSS-Klassen-Fixes: Field-Grids, Buttons, Banner

**Betroffen:** `freelancer/form.html`, `partner/form.html`, `kunde/form.html`, `project/form.html`

- [ ] `class="field-grid col-2"` → `class="fg2"` (alle Vorkommen, alle 4 Templates)
- [ ] Label-Struktur angleichen:
  - Vorher: `<label>Text<input/></label>`
  - Nachher: `<div class="fg"><label>Text</label><input/></div>`
- [ ] `class="checkbox-pill"` → `class="cbfield"` (in `.cbrow` Container einbetten)
- [ ] `class="btn-secondary"` → `class="btn-ghost"` (alle Vorkommen)
- [ ] `class="banner banner-error"` → `class="banner banner-forbidden"` (Kontaktsperre-Banner)

**Commit:** `fix(ui): CSS-Klassennamen an Design-System angleichen (fg2, cbfield, btn-ghost)`

---

## U.2 – ps-dirty-banner reparieren

**Betroffen:** `freelancer/form.html`, `partner/form.html`, `kunde/form.html`, `project/form.html`,
`src/main/frontend/src/css/layout.css`

- [ ] `<ps-dirty-banner>` von außerhalb in den `<form>` verschieben (nach den Hidden-Inputs,
  vor dem ersten `.fcard`)
- [ ] CSS ergänzen in `layout.css`:
  ```css
  ps-dirty-banner { display: none; }
  ps-dirty-banner.visible {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 20px;
    font-size: .82rem;
    font-weight: 500;
    background: var(--warn-bg);
    border-bottom: 2px solid var(--warn-border);
    color: var(--warn-text);
  }
  ```

**Commit:** `fix(ui): ps-dirty-banner in form verschieben und CSS hinzufügen`

---

## U.3 – Kontakthistorie HTML an Design-System §15 angleichen

**Betroffen:** `freelancer/form.html`, `partner/form.html`, `kunde/form.html`, `project/form.html`

- [ ] `.chist` → `.hlist`
- [ ] `.chist-entry` → `.hitem`
- [ ] `.chist-meta` → `.hitem-hd` (enthält `.hbadge` + `.hmeta`)
- [ ] `.chist-text` → `.hbody`
- [ ] `.chist-actions` → `.hacts`
- [ ] Buttons in `.hacts` als `.ibtn` (icon-Buttons)

**Commit:** `fix(ui): Kontakthistorie-HTML an Design-System §15 (hlist/hitem) angleichen`

---

## U.4 – Tags HTML an Design-System §14 angleichen

**Betroffen:** `freelancer/form.html`

- [x] Tags-Container: `<div id="tags-section">` erhält `.tag-grid` als direkten Kind-Wrapper
  um alle Tag-Kategorien
- [x] Kategorie-Label: `<h3 class="subsection-label">` → `<div class="tg-label">`
- [x] `.chip-list` → `.chip-row`
- [x] `.chip-remove` → `.chip-x`
- [x] `.tag-add-select` → `.chip-add`

**Commit:** `fix(ui): Tag-HTML-Struktur an Design-System §14 (tag-grid/chip-row/chip-x) angleichen`

---

## U.5 – fcard Akkordeon-Toggle (alle 4 Templates)

**Betroffen:** `freelancer/form.html`, `partner/form.html`, `kunde/form.html`, `project/form.html`

- [x] Jedes `<div class="fcard">` erhält `.fcard-hd` Header mit `.fcard-title` + `.fcard-chv`
- [x] Gesamter bisheriger Content in `.fcard-body` wrappen
- [x] JS `toggleCard(id)` Funktion (in `main.js` als `window.toggleCard`)
- [x] Standard: alle Cards geöffnet (`.fcard-chv.open`, `.fcard-body` sichtbar)
- [x] Accordion-State: `toggleCard` toggelt `.open` auf Chevron und `display:none` auf Body

**Commit:** `feat(ui): Akkordeon-Toggle für fcard-Sektionen in allen Formular-Templates`

---

## U.6 – 2-spaltiges .form-grid Layout (alle 4 Templates)

**Betroffen:** `freelancer/form.html`, `partner/form.html`, `kunde/form.html`, `project/form.html`

- [ ] `<div id="page">` Inhalt in `<div class="form-grid">` einbetten
- [ ] Cards auf `.col-wide` (volle Breite) vs. halbe Spalte aufteilen:
  - Adresse: halbe Breite
  - Kontaktinformationen: halbe Breite
  - Kommentar + Einsatzdetails: halbe Breite
  - Verfügbarkeit & Konditionen: volle Breite (`.col-wide`)
  - Kodierung & Skills: volle Breite (`.col-wide`)
  - Kontaktmöglichkeiten: volle Breite (`.col-wide`)
  - Kontakthistorie: volle Breite (`.col-wide`)
- [ ] Analoges Mapping für partner, kunde, project

**Commit:** `feat(ui): 2-spaltiges form-grid Layout für Formular-Cards`

---

## U.7 – Toolbar: Löschen-Button als Modal-Trigger

**Betroffen:** `fragments/toolbar.html`, alle 4 `form.html`

- [ ] `toolbar.html`: Parameter `deleteUrl` → `deleteModalId` umbenennen
- [ ] `<a class="btn btn-danger" th:href="${deleteUrl}">` ersetzen durch:
  `<button type="button" class="btn btn-danger" th:if="${deleteModalId != null}"
   th:attr="onclick='openModal(\'' + ${deleteModalId} + '\'')">`
- [ ] Alle 4 `form.html`: Übergabewert von `'#modal-delete'` → `'modal-delete'` (ohne `#`)
- [ ] `openModal(id)` und `closeModal(id)` in `main.js` als `window`-Funktionen auslagern
  (derzeit dupliziert in jedem Template)

**Commit:** `fix(ui): Toolbar-Löschen-Button öffnet Modal statt href-Navigation`

---

## U.8 – Profilsuche Chat-Layout validieren und reparieren

**Betroffen:** `profilesearch/form.html`, `src/main/frontend/src/css/chat.css`

- [ ] Prüfen ob `#chat-page`, `#chat-sidebar`, `#chat-messages`, `#chat-input-area` korrekt strukturiert
- [ ] `--toolbar-h` Variable: wird in `chat.css:14` als Fallback `52px` genutzt –
  sicherstellen dass der Custom-Toolbar der Profilsuche diese Höhe tatsächlich hat
- [ ] Sicherstellen dass `chat.css` nun korrekt über `generated/app.css` geladen wird
  (Vite-Integration wurde in separatem Commit gefixt)
- [ ] Mobile-Overlay-Sidebar funktioniert (`#sidebar-backdrop.visible`)

**Commit:** `fix(ui): Profilsuche Chat-Layout nach Vite-Integration validiert`

---

## U.9 – Pflichtfeld-Validierung

**Betroffen:** alle 4 `form.html`, `src/main/frontend/src/css/components.css`

- [ ] `required` auf Pflichtfeldern:
  - Freiberufler: `name1`
  - Partner: `company`
  - Kunde: `company`
  - Projekt: `projectNumber`, `descriptionShort`
- [ ] CSS in `components.css` ergänzen:
  ```css
  .was-validated input:invalid,
  .was-validated select:invalid,
  .was-validated textarea:invalid {
    border-color: var(--danger);
    box-shadow: 0 0 0 3px var(--danger-ring);
  }
  ```
- [ ] JS in jedem Template: submit-Handler setzt `form.classList.add('was-validated')`
  und bricht ab wenn `!form.checkValidity()`

**Commit:** `feat(ui): HTML5-Pflichtfeld-Validierung mit visueller Rückmeldung`

---

## U.10 – Controller-ITs nach UI-Refactoring validieren

**Betroffen:** alle `*ControllerIT.groovy`

- [ ] `./mvnw clean verify` muss nach allen Änderungen grün sein
- [ ] `ProfileSearchControllerIT`: `containsString('id="chat-page"')` etc. weiterhin gültig
- [ ] Ggf. neue Assertions für `.fcard-hd` Struktur ergänzen

**Commit:** `test: Controller-ITs nach UI-Refactoring validiert`

---

## Fortschritt

| Task   | Status      | Commit   |
|--------|-------------|----------|
| U.1    | ✅ erledigt | ac62cb3  |
| U.2    | ✅ erledigt | a6c4244  |
| U.3    | ✅ erledigt | e606861  |
| U.4    | ✅ erledigt | eec5fb0  |
| U.5    | ⬜ offen    | –        |
| U.6    | ⬜ offen    | –        |
| U.7    | ⬜ offen    | –        |
| U.8    | ⬜ offen    | –        |
| U.9    | ⬜ offen    | –        |
| U.10   | ⬜ offen    | –        |
