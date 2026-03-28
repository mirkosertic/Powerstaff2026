# UI Design System – Powerstaff 2026

**Version:** 1.3 · März 2026
**Zweck:** Verbindliche Gestaltungsrichtlinien für alle Formulare und Ansichten der Applikation.
Neue Formulare werden ausschließlich mit den CSS-Dateien `base.css`, `layout.css`, `components.css`,
`components2.css` und `chat.css` sowie den hier beschriebenen HTML-Mustern erstellt.

---

## Inhaltsverzeichnis

1. [Designprinzipien](#1-designprinzipien)
2. [Design Tokens](#2-design-tokens)
3. [Typografie](#3-typografie)
4. [Seitenstruktur (Shell-Layout)](#4-seitenstruktur-shell-layout)
5. [App-Navigation](#5-app-navigation)
6. [Form-Toolbar](#6-form-toolbar)
7. [Banners](#7-banners)
8. [Formular-Karten (fcard)](#8-formular-karten-fcard)
9. [Felder & Field-Grids](#9-felder--field-grids)
10. [Buttons](#10-buttons)
11. [Checkboxen (Pill-Style)](#11-checkboxen-pill-style)
12. [Subsection-Label & Divider](#12-subsection-label--divider)
13. [Kontaktliste](#13-kontaktliste)
14. [Tags / Chips](#14-tags--chips)
15. [Kontakthistorie](#15-kontakthistorie)
16. [Tabellen (Suchergebnisse)](#16-tabellen-suchergebnisse)
17. [Modals](#17-modals)
18. [Interaktionsmuster](#18-interaktionsmuster)
19. [Anleitung: Neues Formular erstellen](#19-anleitung-neues-formular-erstellen)
20. [Read-only Link-Feld](#20-read-only-link-feld)
21. [Gemerktes Projekt (Toolbar-Erweiterung)](#21-gemerktes-projekt-toolbar-erweiterung)
22. [Dynamisch gefärbte Status-Badges](#22-dynamisch-gefärbte-status-badges)
23. [Inline-Zuordnung](#23-inline-zuordnung)
24. [Chat: Split-Panel-Layout (Profilsuche)](#24-chat-split-panel-layout-profilsuche)
25. [Chat: Sidebar (Sitzungsliste)](#25-chat-sidebar-sitzungsliste)
26. [Chat: Nachrichten](#26-chat-nachrichten)
27. [Chat: Eingabebereich & Toolbar-Variante](#27-chat-eingabebereich--toolbar-variante)
28. [JavaScript-Architektur und Custom Elements](#28-javascript-architektur-und-custom-elements)
29. [Modul-Navigation (btn-row)](#29-modul-navigation-btn-row)
30. [Datentabelle (data-table)](#30-datentabelle-data-table)
31. [Utility-Klassen](#31-utility-klassen)

---

## 1 Designprinzipien

| Prinzip                         | Umsetzung                                                                                                                 |
|---------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| **Augenschonend**               | Kein reines Weiß (#fff) als Seitenhintergrund, kein reines Schwarz als Text. Palette optimiert für 6–8h Bildschirmarbeit. |
| **Business-first**              | Entsättigtes, professionelles Blau als Primärfarbe. Keine knalligen Akzentfarben.                                         |
| **WCAG AA**                     | Alle Text-/Hintergrund-Kombinationen ≥ 4,5:1 Kontrastverhältnis.                                                          |
| **Klare Hierarchie**            | 3-Ebenen-Sticky-Layout: App-Shell (dunkel) → Toolbar (hell) → Seiteninhalt.                                               |
| **Minimalismus**                | Nur notwendige visuelle Trenner. Kein Overengineering von Animationen.                                                    |
| **Desktop-first, Mobile-ready** | Ausgelegt auf Full-HD (1920×1080); 2 Breakpoints für Tablet/Mobil.                                                        |

---

## 2 Design Tokens

Alle Tokens sind als CSS Custom Properties in `:root` definiert und über `base.css` verfügbar.

### Oberflächen

| Token         | Wert      | Verwendung                               |
|---------------|-----------|------------------------------------------|
| `--bg`        | `#eaecf0` | Seitenhintergrund                        |
| `--surface`   | `#f9fafb` | Karten-Hintergrund                       |
| `--surface-2` | `#f1f3f6` | Kartenheader, Input-Hintergrund, Gruppen |
| `--surface-3` | `#ffffff` | Modale, erhöhte UI-Elemente              |

### Rahmen

| Token        | Wert      | Verwendung                        |
|--------------|-----------|-----------------------------------|
| `--border`   | `#c8cdd6` | Primärer Rahmen (Inputs, Gruppen) |
| `--border-l` | `#dde1e8` | Subtiler Rahmen (Karten, Divider) |

### Text

| Token      | Wert      | Verwendung                    |
|------------|-----------|-------------------------------|
| `--text`   | `#1c2333` | Primärtext                    |
| `--text-2` | `#4a5568` | Sekundärtext, Labels          |
| `--text-3` | `#8896aa` | Platzhalter, Metadaten, Icons |

### Primärfarbe

| Token        | Wert                 | Verwendung                           |
|--------------|----------------------|--------------------------------------|
| `--pri`      | `#2563a8`            | Primäre Buttons, Links, Fokus-Rahmen |
| `--pri-d`    | `#1a4a82`            | Hover-Zustand Primärbutton           |
| `--pri-l`    | `#e8f0fb`            | Hover-Hintergrund, aktive Zeilen     |
| `--pri-ring` | `rgba(37,99,168,.2)` | Fokus-Ring (box-shadow)              |

### Status

| Token                   | Wert                          | Verwendung                          |
|-------------------------|-------------------------------|-------------------------------------|
| `--danger`              | `#b91c1c`                     | Fehlerzustand, Löschen              |
| `--danger-l`            | `#fef2f2`                     | Gefahr-Hintergrund                  |
| `--danger-ring`         | `rgba(185,28,28,.15)`         | Fokus-Ring Danger                   |
| `--warn-bg/border/text` | `#fffbeb / #d97706 / #7c4a03` | Warnbanner (ungespeichert)          |
| `--ok-bg/border/text`   | `#f0fdf4 / #16a34a / #14532d` | Erfolgsbanner                       |

### Abstände & Schatten

| Token         | Wert                                                    |
|---------------|---------------------------------------------------------|
| `--r`         | `5px` – Standard-Borderradius                           |
| `--r-l`       | `8px` – großer Borderradius (Karten, Modals)            |
| `--shadow-xs` | `0 1px 2px rgba(0,0,0,.06)`                             |
| `--shadow-sm` | `0 1px 4px rgba(0,0,0,.08), 0 1px 2px rgba(0,0,0,.05)` |
| `--shadow-md` | `0 4px 12px rgba(0,0,0,.1)`                             |
| `--shadow-lg` | `0 16px 40px rgba(0,0,0,.15)`                           |
| `--nav-h`     | `44px`                                                  |
| `--toolbar-h` | `50px`                                                  |

---

## 3 Typografie

- **Schriftart:** `system-ui, -apple-system, "Segoe UI", Roboto, Arial, sans-serif`
  Keine externen Webfonts – maximale Performance und Offline-Fähigkeit.
- **Basis-Fontsize:** `14px` (auf `<html>`)
- **Line-height:** `1.55`

| Verwendung            | Größe       | Gewicht | Besonderheiten                                                              |
|-----------------------|-------------|---------|-----------------------------------------------------------------------------|
| Primärtext            | `.88rem`    | 400     | Input-Werte, `p`, `span`, `td`, `li`                                       |
| Labels                | `.72rem`    | 600     | `letter-spacing: .02em`, `color: var(--text-2)`                            |
| Kartenüberschriften   | `.7rem`     | 800     | `text-transform: uppercase`, `letter-spacing: .07em`                       |
| Subsection-Labels     | `.67rem`    | 800     | `text-transform: uppercase`, `letter-spacing: .1em`, `color: var(--text-3)`|
| Audit-Meta            | `.75rem`    | 400     | `color: var(--text-3)`                                                      |
| Badge-Text            | `.65–.68rem`| 700–800 | `text-transform: uppercase`                                                 |

---

## 4 Seitenstruktur (Shell-Layout)

```
┌─────────────────────────────────────────────────┐
│  #app-nav  (sticky, top:0, z:110, dunkel)        │
├─────────────────────────────────────────────────┤
│  #toolbar  (sticky, top:44px, z:100, weiß)       │
│  └── #toolbar-inner  (max-width:1400px)          │
├─────────────────────────────────────────────────┤
│  .banner-*  (optional)                           │
├─────────────────────────────────────────────────┤
│  #page  (max-width:1400px, padding:18px 20px)    │
│  └── .form-grid  (2-col CSS Grid, gap:14px)      │
│       ├── .fcard           (1 Spalte)            │
│       ├── .fcard           (1 Spalte)            │
│       └── .fcard.col-wide  (2 Spalten)           │
└─────────────────────────────────────────────────┘
```

**Hinweis:** Der `#toolbar`-Container enthält immer einen `#toolbar-inner`-Container, der den Inhalt auf
`max-width: 1400px` begrenzt und per `display: flex; align-items: center` die Kinder ausrichtet.

```html
<!DOCTYPE html>
<html lang="de">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Powerstaff 2026 – [Modulname]</title>
</head>
<body>

  <!-- App-Navigation -->
  <nav id="app-nav">…</nav>

  <!-- Form-Toolbar -->
  <div id="toolbar">
    <div id="toolbar-inner">
      …
    </div>
  </div>

  <!-- Banners (innerhalb #page oder direkt darunter) -->
  <div id="page">
    <div class="banner banner-forbidden" th:if="…">…</div>
    <div class="banner banner-success hidden" id="banner-success">…</div>

    <div class="form-grid">
      <!-- Karten hier einfügen -->
    </div>
  </div>

</body>
</html>
```

---

## 5 App-Navigation

Die App-Navigation ist die **dunkle Shell-Leiste** ganz oben. Sie gehört **nicht** zum Formular und muss sich optisch klar davon unterscheiden.

```html
<nav id="app-nav">
  <div id="app-nav-inner">
    <div id="nav-logo">Power<span>staff</span> 2026</div>
    <a class="menu-item active" href="/freelancer">Freiberuflerverwaltung</a>
    <a class="menu-item" href="/partner">Partnerverwaltung</a>
    <a class="menu-item" href="/customer">Kundenverwaltung</a>
    <a class="menu-item" href="/project">Projekte</a>
    <a class="menu-item" href="/profilesearch/chat">Profilsuche</a>
    <a class="menu-item" href="/admin/benutzer">Administration</a>
    <span class="flex-spacer"></span>
    <form method="post" action="/logout" class="inline">
      <button type="submit" class="menu-item btn-logout">Abmelden</button>
    </form>
  </div>
</nav>
```

**Regeln:**
- Aktiver Menüpunkt erhält Klasse `.active` (blauer Unterstrich `#60a5fa`).
- Logo: `Power` in Weiß, Akzentbuchstaben `staff` in `#60a5fa`.
- Max-Breite des Inners: `1400px`, zentriert.
- Horizontales Scrollen auf kleinen Bildschirmen (Scrollbar unsichtbar).
- `.flex-spacer` schiebt Abmelden-Button an den rechten Rand.
- `.btn-logout` macht den Submit-Button optisch zu einem normalen `menu-item`.

---

## 6 Form-Toolbar

Die Toolbar ist **modulspezifisch** und enthält Navigation, Audit-Info und Aktionen. Der
`#toolbar-inner` stellt die horizontale Ausrichtung sicher und begrenzt auf `max-width: 1400px`.

```html
<div id="toolbar">
  <div id="toolbar-inner">

    <!-- Modulbezeichnung -->
    <div id="tb-module">Freiberufler</div>

    <!-- Datensatz-Navigation (POST-Formulare, siehe §28) -->
    <div id="tb-nav">
      <form method="post" action="/freiberufler/navigate" style="display:inline">
        <input type="hidden" name="direction" value="first">
        <button type="submit" class="tb-nav-btn" title="Erster">⏮</button>
      </form>
      <form method="post" action="/freiberufler/navigate" style="display:inline">
        <input type="hidden" name="direction" value="prev">
        <button type="submit" class="tb-nav-btn" title="Vorheriger">◀</button>
      </form>
      <input type="text" id="tb-id-input" placeholder="ID">
      <form method="post" action="/freiberufler/navigate" style="display:inline">
        <input type="hidden" name="direction" value="next">
        <button type="submit" class="tb-nav-btn" title="Nächster">▶</button>
      </form>
      <form method="post" action="/freiberufler/navigate" style="display:inline">
        <input type="hidden" name="direction" value="last">
        <button type="submit" class="tb-nav-btn" title="Letzter">⏭</button>
      </form>
    </div>

    <!-- Audit-Informationen -->
    <div id="tb-audit">
      Erstellt <strong>12.01.2025</strong> · Geändert <strong>10.03.2026</strong> von <strong>m.müller</strong>
    </div>

    <!-- Aktionen -->
    <div id="tb-actions">
      <button class="btn btn-ghost">＋ Neu</button>
      <button class="btn btn-pri">💾 Speichern</button>
      <button class="btn btn-danger" onclick="openModal('modal-delete')">🗑 Löschen</button>
      <button class="btn btn-ghost">🔍 Suche</button>
    </div>

  </div>
</div>
```

**Regeln:**
- `#tb-module` trennt sich visuell mit `border-right` vom Rest. Höhe füllt `100%` des `#toolbar`.
- Audit-Info (`#tb-audit`) wächst flexibel (`flex: 1`), wird auf Tablets (< 900px) ausgeblendet.
- Aktionen stehen immer am rechten Rand (`margin-left: auto` auf `#tb-actions`).
- Löschen-Button öffnet stets ein Bestätigungs-Modal — kein direktes Ausführen ohne Bestätigung.

---

## 7 Banners

Banners erscheinen zwischen Toolbar und Formular (innerhalb von `#page`). Statische Banners werden
serverseitig per Thymeleaf-Bedingung gerendert; dynamische werden per JS mit der Klasse `.hidden`
ein- und ausgeblendet.

```html
<!-- Kontaktsperre (statisch, dauerhaft sichtbar) -->
<div class="banner banner-forbidden" th:if="${freelancer.contactForbidden}">
  🔒 Kontaktsperre aktiv – keine Kontaktaufnahme erlaubt.
</div>

<!-- Technischer Fehler (dynamisch) -->
<div class="banner banner-error hidden" id="banner-save-error">
  ✕ Beim Speichern ist ein technischer Fehler aufgetreten. Bitte erneut versuchen.
</div>

<!-- Ungespeicherte Änderungen – Custom Element (empfohlen) -->
<ps-dirty-banner>⚠ Ungespeicherte Änderungen vorhanden.</ps-dirty-banner>

<!-- Ungespeicherte Änderungen (klassisches Banner, alternativ) -->
<div class="banner banner-unsaved hidden" id="banner-unsaved">
  ⚠️ Es gibt ungespeicherte Änderungen.
</div>

<!-- Erfolgsmeldung -->
<div class="banner banner-success hidden" id="banner-success">
  ✓ Datensatz erfolgreich gespeichert.
</div>

<!-- Info (z. B. Zuordnung erfolgreich) -->
<div class="banner banner-info hidden" id="banner-assign-project">
  ✓ Freiberufler wurde dem gemerkten Projekt zugeordnet.
</div>
```

### Banner-Varianten

| Klasse              | Hintergrund         | Rahmen              | Textfarbe           | Verwendung                         |
|---------------------|---------------------|---------------------|---------------------|------------------------------------|
| `.banner-forbidden` | `--danger-l`        | `--danger`          | `--danger`          | Kontaktsperre (dauerhaft)          |
| `.banner-error`     | `--danger-l`        | `--danger`          | `--danger`          | Technischer Fehler                 |
| `.banner-unsaved`   | `--warn-bg`         | `--warn-border`     | `--warn-text`       | Ungespeicherte Änderungen          |
| `.banner-success`   | `--ok-bg`           | `--ok-border`       | `--ok-text`         | Erfolgreich gespeichert            |
| `.banner-info`      | `--pri-l`           | `--pri`             | `--pri-d`           | Neutral-Info (z. B. Zuordnung OK)  |

**Regeln:**
- Ungespeicherte Änderungen führen **nur** zu einem Hinweisbanner. Sie blockieren keine weiteren Aktionen.
- Erfolgsbanner wird nach ca. 3 Sekunden automatisch ausgeblendet.
- `ps-dirty-banner` ist das bevorzugte Custom Element für ungespeicherte Änderungen (wird per `.visible`-Klasse ein/ausgeblendet statt `.hidden`).
- Banners haben `max-width: 1400px` und sind zentriert — kein `#page`-Wrapper nötig, wenn sie direkt darunter stehen.

### ps-dirty-banner Custom Element

`ps-dirty-banner` ist standardmäßig `display: none`. Sobald das Custom Element die Klasse `.visible`
erhält, wird es als Flex-Banner mit denselben Stilen wie `.banner-unsaved` angezeigt.

```html
<!-- Muss INNERHALB des <form>-Elements stehen -->
<form id="freelancer-form" ...>
  <ps-dirty-banner>⚠ Ungespeicherte Änderungen vorhanden.</ps-dirty-banner>
  …
</form>
```

---

## 8 Formular-Karten (fcard)

Alle Formularinhalte werden in Karten gruppiert. Karten können eingeklappt werden.

```html
<div class="fcard">
  <div class="fcard-hd" onclick="toggleCard('fcard-section-id')">
    <span class="fcard-title">Abschnitt-Titel</span>
    <span class="fcard-chv open">▾</span>
  </div>
  <div class="fcard-body" id="fcard-section-id">
    <!-- Felder hier -->
  </div>
</div>
```

**Toggling (Vanilla JS):**
```javascript
function toggleCard(id) {
  const body = document.getElementById(id);
  const hd   = body.previousElementSibling;
  const chv  = hd.querySelector('.fcard-chv');
  const open = body.style.display !== 'none';
  body.style.display = open ? 'none' : '';
  chv.classList.toggle('open', !open);
}
```

**Platzierung im Grid:**
- Standard: 1 Spalte (füllt eine Grid-Zelle)
- Volle Breite: `<div class="fcard col-wide">`

**Regeln:**
- Karten-Titel immer in Großbuchstaben (CSS übernimmt das via `text-transform`).
- `fcard-body` hat `padding: 14px`.
- Chevron: `.fcard-chv.open` = nach unten (0°), ohne `.open` = nach rechts (−90°, eingeklappt).
- `fcard-hd:hover` zeigt `var(--pri-l)` als Hintergrund.
- `fcard-hd` ohne onclick-Handler (reine Überschrift ohne Toggle) ist ebenfalls möglich — dann kein Cursor-Pointer.

---

## 9 Felder & Field-Grids

### Basis-Feld

```html
<div class="fg">
  <label>Feldbezeichnung</label>
  <input type="text" value="Wert">
</div>
```

### Field-Grids

Innerhalb einer Karte werden Felder mit Grid-Klassen angeordnet:

| Klasse     | Spalten | Verwendung                          |
|------------|---------|-------------------------------------|
| `.fg2`     | 2       | Standard-Zweiteilung                |
| `.fg3`     | 3       | z. B. Suchbegriff / Von / Bis       |
| `.fg4`     | 4       | Datumsgruppen                       |
| `.fg5`     | 5       | Schmale Felder nebeneinander        |
| `.fg-addr` | 3       | Adresszeile: Land (5rem) / PLZ (9rem) / Ort (1fr) |

**Span-Klassen** (innerhalb eines Grid):

| Klasse | Wirkung               |
|--------|-----------------------|
| `.s2`  | Feld belegt 2 Spalten |
| `.s3`  | Feld belegt 3 Spalten |
| `.s4`  | Feld belegt 4 Spalten |

```html
<!-- Beispiel: 4-spaltiges Grid mit span -->
<div class="fg4 mt-md">
  <div class="fg">
    <label>Anrede</label>
    <input type="text" name="titel">
  </div>
  <div class="fg">
    <label>Vorname</label>
    <input type="text" name="name2">
  </div>
  <div class="fg s2">
    <label>Name</label>
    <input type="text" name="name1" required>
  </div>
</div>

<!-- Beispiel: Adresszeile -->
<div class="fg-addr mt-md">
  <div class="fg">
    <label>Land</label>
    <input type="text" name="land" placeholder="DE">
  </div>
  <div class="fg">
    <label>PLZ</label>
    <input type="text" name="plz">
  </div>
  <div class="fg">
    <label>Ort</label>
    <input type="text" name="ort">
  </div>
</div>
```

### Euro-Eingabe

```html
<div class="fg">
  <label>Stundensatz</label>
  <div class="ew">
    <input type="number" value="95">
  </div>
</div>
```

### Textarea

```html
<div class="fg">
  <label>Kommentar</label>
  <textarea rows="4">Freitext…</textarea>
</div>
```

### Auswahlliste (Select)

Für Felder mit einer festen Wertemenge (z. B. Kontaktart) wird `<select>` anstelle von `<input>` verwendet. Die Struktur ist identisch zum Basis-Feld.

```html
<div class="fg">
  <label>Kontaktart</label>
  <select>
    <option value="">— bitte wählen —</option>
    <option value="NL">NL</option>
    <option value="NL1">NL1</option>
  </select>
</div>
```

**Regeln:**
- Klasse `.mb` (`margin-bottom: 10px`) auf einer Grid-Zeile trennt Gruppen voneinander.
- Inputs im Fokus: blaue Border + `box-shadow: 0 0 0 3px var(--pri-ring)`.
- Auf mobilen Geräten (< 520px) kollabieren alle Field-Grids auf 1 Spalte.
- `.was-validated input:invalid` / `select:invalid` / `textarea:invalid`: rote Border + Danger-Ring.

---

## 10 Buttons

### Varianten

```html
<!-- Primär (Speichern) -->
<button class="btn btn-pri">💾 Speichern</button>

<!-- Ghost (Sekundäraktion) -->
<button class="btn btn-ghost">＋ Neu</button>

<!-- Gefahr (Löschen) -->
<button class="btn btn-danger" onclick="openModal('modal-delete')">🗑 Löschen</button>

<!-- Klein -->
<button class="btn btn-ghost btn-sm">Bearbeiten</button>

<!-- Hinzufügen (gestrichelt, inline) -->
<button class="btn-add">＋ Eintrag hinzufügen</button>

<!-- Icon-Button -->
<button class="ibtn" title="Bearbeiten">✎</button>
<button class="ibtn del" title="Löschen">🗑</button>
```

### Regeln
- Primär-Button immer für die Hauptaktion (Speichern).
- Ghost-Button für neutrale Aktionen (Neu, Abbrechen, Suche).
- Danger-Button für destruktive Aktionen (Löschen). Im Ruhezustand dezent (heller Hintergrund), beim Hover volles Rot. **Immer über Modal bestätigen.**
- `.btn-add` für das Hinzufügen von Listeneinträgen direkt im Formular (gestrichelte Border, keine Füllung).
- Icon-Buttons (`.ibtn`) für Edit/Delete innerhalb von Listen. `.ibtn.del:hover` zeigt Danger-Farbe.
- `.btn-sm` reduziert Padding auf `3px 8px`, Schrift auf `.72rem`.

---

## 11 Checkboxen (Pill-Style)

Checkboxen werden nicht als klassische Checkboxen dargestellt, sondern als klickbare Pill-Labels.

```html
<div class="cbrow">
  <label class="cbfield on">
    <input type="checkbox" checked>
    Aktiv
  </label>
  <label class="cbfield">
    <input type="checkbox">
    Remote
  </label>
  <label class="cbfield on-danger">
    <input type="checkbox" checked>
    Gesperrt
  </label>
</div>
```

**Zustände:**
- Standard: grauer Hintergrund (`--surface-2`), Border `--border`
- `.on`: blaue Border (`--pri`) + blauer Hintergrund (`--pri-l`) + `font-weight: 600`
- `.on-danger`: rote Border (`--danger`) + roter Hintergrund (`--danger-l`) + `font-weight: 600`

**Klasse per JS setzen:**
```javascript
cbfield.addEventListener('change', e => {
  e.currentTarget.classList.toggle('on', e.target.checked);
});
```

**Regeln:**
- `.cbrow` ist ein Flex-Wrapper mit `flex-wrap: wrap; gap: 6px; margin-top: 14px`.
- Die Checkbox innerhalb des `.cbfield` ist sichtbar (14×14px, `accent-color: var(--pri)`).
- Kann auch ohne `.cbrow` direkt als `<label class="cbfield">` verwendet werden (z. B. im Modal oder als alleinstehende Checkbox).

---

## 12 Subsection-Label & Divider

Zur Unterteilung innerhalb einer Karte ohne eigene Kartenebene:

```html
<p class="sublbl">Adressdaten</p>
<!-- Felder -->
<hr class="div">
<p class="sublbl">Weitere Angaben</p>
```

**Regeln:**
- `.sublbl`: `.67rem`, Gewicht 800, Großbuchstaben, `letter-spacing: .1em`, `color: var(--text-3)`, `margin: 10px 0 6px 0`.
- `hr.div`: `border-top: 1px solid var(--border-l)`, kein Rand oben/unten außer `margin: 10px 0`.

---

## 13 Kontaktliste

Für strukturierte Listen von Kontaktkanälen (E-Mail, Telefon, Web etc.):

```html
<div class="clist">
  <div class="citem">
    <span class="cbadge cb-email">E-Mail</span>
    <span class="cval"><a href="mailto:max@example.com">max@example.com</a></span>
    <div class="cacts">
      <button class="ibtn" title="Bearbeiten">✎</button>
      <button class="ibtn del" title="Löschen">🗑</button>
    </div>
  </div>
  <div class="citem">
    <span class="cbadge cb-telefon">Telefon</span>
    <span class="cval">+49 89 12345678</span>
    <div class="cacts">
      <button class="ibtn" title="Bearbeiten">✎</button>
      <button class="ibtn del" title="Löschen">🗑</button>
    </div>
  </div>
</div>
<button class="btn-add" style="margin-top:8px">＋ Kontakt hinzufügen</button>
```

### Badge-Klassen

| Klasse        | Typ     | Farbe                           |
|---------------|---------|---------------------------------|
| `.cb-email`   | E-Mail  | Blau (`#dbeafe` / `#1e40af`)    |
| `.cb-telefon` | Telefon | Lila (`#ede9fe` / `#5b21b6`)    |
| `.cb-web`     | Website | Grün (`#dcfce7` / `#166534`)    |
| `.cb-xing`    | XING    | Cyan (`#cffafe` / `#155e75`)    |
| `.cb-gulp`    | GULP    | Gelb (`#fef9c3` / `#713f12`)    |
| `.cb-fax`     | Fax     | Grau (`--surface-2` / `--text-3`)|

**Regeln:**
- `.cacts` ist im Ruhezustand unsichtbar (`opacity: 0`) und erscheint beim Hover der Zeile.
- Neuanlage und Bearbeitung erfolgen über ein Modal (nie inline-Edit).
- `.citem:hover` hebt die Zeile blau hervor (`--pri-l`).

---

## 14 Tags / Chips

Tags werden in Kategorien (z. B. Programmiersprachen, Branchen) gruppiert.

```html
<div class="tag-grid">
  <!-- Kategorie 1 -->
  <div>
    <p class="tg-label">Programmiersprachen</p>
    <div class="chip-row">
      <span class="chip">Java <button class="chip-x" title="Entfernen">✕</button></span>
      <span class="chip">Python <button class="chip-x" title="Entfernen">✕</button></span>
      <select class="chip-add">
        <option value="">＋ Hinzufügen</option>
        <option>Go</option>
        <option>Rust</option>
      </select>
    </div>
  </div>

  <!-- Kategorie 2 -->
  <div>
    <p class="tg-label">Branchen</p>
    <div class="chip-row">
      <span class="chip">Automotive <button class="chip-x" title="Entfernen">✕</button></span>
      <select class="chip-add">
        <option value="">＋ Hinzufügen</option>
        <option>Banking</option>
      </select>
    </div>
  </div>
</div>
```

**Ausgewählter Chip (Toggle-Zustand in Suchfiltern):**
```html
<!-- Chip als Button (Profilsuche) -->
<button type="button" class="chip selected" onclick="toggleTagChip(this)">Java</button>
<button type="button" class="chip" onclick="toggleTagChip(this)">Python</button>
```

`.chip.selected`: `background: var(--pri)`, `color: white`, `border-color: var(--pri-d)`.

**Kleine Chips (Tabellenzellen):**
```html
<td><span class="chip-xs">Java</span><span class="chip-xs">Python</span></td>
```

**Regeln:**
- `select.chip-add` wird nach Auswahl per JS zurückgesetzt und ein neuer Chip erzeugt.
- `.tag-grid` ist 2-spaltig, auf Mobil (< 520px) 1-spaltig.
- `.chip` hat immer Primärfarbe als Hintergrund (`--pri-l`) und `border: 1px solid #bfdbfe`.
- `.chip.selected` wird in Suchfiltern (Profilsuche) verwendet, nicht in Formularen.
- `.chip-xs` für kompakte Chips in Tabellenzellen: kleiner (`0.65rem`), grauer Hintergrund.

---

## 15 Kontakthistorie

Zeitliche Einträge (Anrufe, E-Mails, Notizen) in einem Flex-Stack.
Die Karte sollte immer `.col-wide` belegen, da Einträge längere Texte enthalten können.

### Mit Typ-Badge (Freiberufler, Partner, Kunden)

```html
<div class="hlist">
  <div class="hitem">
    <div class="hitem-hd">
      <span class="hbadge">Telefon</span>
      <span class="hmeta"><strong>12.03.2026</strong> · m.müller</span>
      <div class="hacts">
        <button class="ibtn" title="Bearbeiten">✎</button>
        <button class="ibtn del" title="Löschen">🗑</button>
      </div>
    </div>
    <div class="hbody">Kurzes Telefonat wegen Projektanfrage Q3.</div>
  </div>
</div>
<button class="btn-add" style="margin-top:8px">＋ Eintrag hinzufügen</button>
```

### Ohne Typ-Badge (Projekte)

Das Projekte-Modul hat keine Typisierung. Der `.hbadge` entfällt; `.hmeta` wächst auf volle Breite.

```html
<div class="hlist">
  <div class="hitem">
    <div class="hitem-hd">
      <span class="hmeta"><strong>12.03.2026</strong> · m.müller</span>
      <div class="hacts">
        <button class="ibtn" title="Bearbeiten">✎</button>
        <button class="ibtn del" title="Löschen">🗑</button>
      </div>
    </div>
    <div class="hbody">Abstimmungsgespräch mit Kunde geführt.</div>
  </div>
</div>
<button class="btn-add" style="margin-top:8px">＋ Eintrag hinzufügen</button>
```

**Regeln:**
- Neueste Einträge zuerst.
- `.hbody` mit `white-space: pre-wrap` – Zeilenumbrüche im Text bleiben erhalten.
- `.hacts` im Ruhezustand unsichtbar (`opacity: 0`), erscheint beim Hover des `.hitem`.
- Bearbeitung/Neuanlage über Modal.
- Typ-Badge (`.hbadge`) nur einbauen, wenn das Modul eine `historytype`-Verknüpfung hat.

---

## 16 Tabellen (Suchergebnisse)

Für die QBE-Suche (Query by Example) und Trefferlistenansichten. Die Tabelle wird in `.tbl-wrap`
eingebettet (horizontales Scrollen auf kleinen Screens).

```html
<div class="tbl-wrap">
  <table>
    <thead>
      <tr>
        <th class="srt srt-asc">Name</th>
        <th class="srt">Vorname</th>
        <th>Ort</th>
        <th class="srt">Stundensatz</th>
        <th>Tags</th>
        <th>Verfügbar</th>
      </tr>
    </thead>
    <tbody>
      <tr onclick="openRecord(1042)">
        <td class="td-name">Müller</td>
        <td>Max</td>
        <td>München</td>
        <td>95 €/h</td>
        <td>
          <span class="chip-xs">Java</span>
          <span class="chip-xs">Spring</span>
        </td>
        <td>01.04.2026</td>
      </tr>
      <tr class="row-forbidden">
        <td class="td-name">Gesperrt</td>
        <td colspan="5">— Kontaktsperre aktiv —</td>
      </tr>
    </tbody>
  </table>
</div>
```

**Sortierbare Spalten:** Klasse `.srt` auf dem `<th>`. Aktueller Zustand: `.srt-asc` (▲, blau) oder
`.srt-desc` (▼, blau). Ohne `.srt-asc`/`.srt-desc` zeigt ein blasses Platzhalter-Icon.

**Gesperrte Zeilen:** `.row-forbidden` auf `<tr>` färbt Hintergrund und Text rot (`--danger-l` / `--danger`).

**Regeln:**
- Name-Zelle mit `.td-name` (blau, fett) – signalisiert Klickbarkeit.
- Tabellenzeile ist per `onclick` anklickbar und öffnet den Datensatz.
- Keine festen Spaltenbreiten – Tabelle passt sich dem verfügbaren Platz an.
- Container `.tbl-wrap` scrollt horizontal auf kleinen Bildschirmen.

### Variante: Tabelle mit Aktionsspalte (Zuordnungslisten)

Für Zuordnungslisten (z. B. Freiberufler im Projekt, Projekte beim Kunden) sind Zeilen nicht als Ganzes
anklickbar. Die Klasse `.no-click` auf `<tr>` deaktiviert Hover-Effekt und Pointer-Cursor.

```html
<div class="tbl-wrap">
  <table>
    <thead>
      <tr>
        <th class="srt srt-asc">Kodierung</th>
        <th class="srt">Name</th>
        <th>Status</th>
        <th>Konditionen</th>
        <th></th>  <!-- Aktionsspalte: kein Label -->
      </tr>
    </thead>
    <tbody>
      <tr class="no-click">
        <td class="td-name">
          <a href="#" onclick="openRecord(42)">DEV-2024-07</a>
        </td>
        <td>Müller, Max</td>
        <td>
          <span class="badge-dyn" style="background:#fef9c3; color:#713f12;">Vorgeschlagen</span>
        </td>
        <td>95 €/h, Remote möglich</td>
        <td class="td-acts">
          <button class="ibtn" title="Bearbeiten">✎</button>
          <button class="ibtn del" title="Zuordnung löschen" onclick="openModal('modal-delete')">🗑</button>
        </td>
      </tr>
    </tbody>
  </table>
</div>
```

**Regeln:**
- `.no-click` auf Zeilen ohne Gesamt-onclick → kein Hover-Highlighting, kein Pointer.
- `.td-acts` auf die Aktionsspalte → minimale Breite, kein Zeilenumbruch.
- Links in einzelnen Zellen verwenden `<a href="#">` mit onclick statt des Zeilen-onclick.
- `.badge-dyn` mit inline-style für datenbankgesteuerte Farben (siehe [Abschnitt 22](#22-dynamisch-gefärbte-status-badges)).

---

## 17 Modals

Modale Dialoge für CRUD-Operationen auf Unterentitäten (Kontakte, Historieneinträge) und
Systemmeldungen (Konflikte, Bestätigungen).

### Standard-Modal

```html
<div class="mbk hidden" id="modal-kontakt">
  <div class="mbox">
    <div class="mhd">
      <h3>Kontaktmöglichkeit bearbeiten</h3>
      <button type="button" class="mx" onclick="closeModal('modal-kontakt')">✕</button>
    </div>
    <div class="mbody">
      <div class="mfld">
        <label>Typ</label>
        <select>
          <option>E-Mail</option>
          <option>Telefon</option>
          <option>Website</option>
        </select>
      </div>
      <div class="mfld">
        <label>Wert</label>
        <input type="text" placeholder="z. B. max@example.com">
      </div>
    </div>
    <div class="mft">
      <button type="button" class="btn btn-ghost" onclick="closeModal('modal-kontakt')">Abbrechen</button>
      <button type="button" class="btn btn-pri">Speichern</button>
    </div>
  </div>
</div>
```

### Großes Modal

Klasse `.mbox.lg` erweitert die max-Breite auf `580px`.

### Warn-Modal (Bestätigung/Konflikt)

```html
<div class="mbk hidden" id="modal-confirm">
  <div class="mbox">
    <div class="mhd">
      <h3>Datensatz löschen</h3>
      <button type="button" class="mx" onclick="closeModal('modal-confirm')">✕</button>
    </div>
    <div class="mbody">
      <p class="mwarn">
        Soll <strong>Max Müller (ID 1042)</strong> wirklich gelöscht werden?
        Diese Aktion kann nicht rückgängig gemacht werden.
      </p>
    </div>
    <div class="mft">
      <button type="button" class="btn btn-ghost" onclick="closeModal('modal-confirm')">Abbrechen</button>
      <button type="button" class="btn btn-danger">Löschen</button>
    </div>
  </div>
</div>
```

**Modal-Steuerung (Vanilla JS):**
```javascript
function openModal(id)  { document.getElementById(id).classList.remove('hidden'); }
function closeModal(id) { document.getElementById(id).classList.add('hidden'); }

// Klick auf Backdrop schließt Modal
document.querySelectorAll('.mbk').forEach(mbk => {
  mbk.addEventListener('click', e => { if (e.target === mbk) mbk.classList.add('hidden'); });
});
```

**Regeln:**
- Backdrop (`.mbk`): `rgba(15,23,42,.45)` mit `backdrop-filter: blur(3px)`.
- Einblende-Animation: `mbox-in` (Scale + Translate, 140ms).
- Footerleiste `.mft` hat `--surface-2` als Hintergrund.
- Felder im Modal: Klasse `.mfld` mit `margin-bottom: 12px`.
- Klick auf Backdrop schließt das Modal.
- Hilfsklasse `.hidden` ist `display: none !important` (in `components2.css`).

---

## 18 Interaktionsmuster

### Kollabierbare Karten

Alle Karten können über den Kopfbereich ein-/ausgeklappt werden. `toggleCard(id)` wird auf das
`fcard-hd`-Element gebunden und erhält die ID des `fcard-body`-Elements.

### Ungespeicherte Änderungen

- Jede Änderung an einem Formularfeld setzt den Dirty-State.
- `ps-dirty-banner` erhält Klasse `.visible` (Custom Element) oder `.banner-unsaved` wird sichtbar.
- **Keine** Blockierung von Navigation oder anderen Aktionen.
- Nach erfolgreichem Speichern: Banner ausblenden, Erfolgsbanner einblenden.

### Datensatz-Navigation

- Pfeil-Buttons in `#tb-nav` werden als POST-Formulare gerendert (ADR-014).
- Der Server berechnet die Ziel-ID frisch aus der Datenbank – keine vorberechneten `<a>`-Links.
- Deaktivierte Buttons (`disabled`-Attribut) bei erstem/letztem Datensatz.

### Optimistic Locking

Bei Speichern-Konflikten (Datensatz zwischenzeitlich geändert) erscheint ein Modal mit zwei Optionen:
1. Eigene Änderungen verwerfen und aktuellen Stand laden.
2. Eigene Version erzwingen (Überschreiben).

### Sortierung in Tabellen

- Klick auf `.srt`-Spaltenheader wechselt Sortierung: `srt-asc` → `srt-desc` → keine Sortierung.
- Aktueller Sortierstatus wird per CSS-Klasse visualisiert (▲/▼, Primärfarbe).

### Gemerktes Projekt

Das zuletzt im Projekte-Formular angezeigte Projekt wird server-seitig pro Sachbearbeiter gespeichert.
Es wird in der Toolbar der Formulare Freiberufler, Partner und Kunden angezeigt (Element `#tb-project`).
Im Freiberufler-Formular erscheint zusätzlich ein Zuordnungs-Button, wenn ein Datensatz geladen ist.
Beim Navigieren im Projekte-Formular aktualisiert der Server das gemerkte Projekt automatisch.

### Tooltips

Jedes Element mit `title="…"` zeigt den Browser-nativen Tooltip beim Hover. Kein Custom-Tooltip-CSS
notwendig.

---

## 19 Anleitung: Neues Formular erstellen

### Schritt 1: HTML-Grundstruktur

Kopiere das Shell-Template aus [Abschnitt 4](#4-seitenstruktur-shell-layout) und passe `<title>` sowie `#tb-module` an.

### Schritt 2: App-Navigation

Verwende die Navigation aus [Abschnitt 5](#5-app-navigation) unverändert. Setze die Klasse `.active` auf den zutreffenden Menüpunkt.

### Schritt 3: Toolbar konfigurieren

Passe `#tb-module` (Modulname), Audit-Inhalt und Aktions-Buttons an.
Nicht benötigte Toolbar-Elemente (z. B. Navigation bei Listenansichten) weglassen.

### Schritt 4: Form-Grid und Karten

Lege für jeden logischen Abschnitt eine `.fcard` an. Entscheide:
- 1-spaltig (Standard): keine zusätzliche Klasse nötig.
- 2-spaltig (volle Breite): `class="fcard col-wide"`.

Faustregel für die Spaltenanordnung:
- Zusammengehörige Felder (z. B. Name + Adresse) nebeneinander in 1 Spalte.
- Lange Freitexte oder Listen: `col-wide`.

### Schritt 5: Felder

Verwende `.fg` für jedes Feld und `.fg2`–`.fg5` bzw. `.fg-addr` als Container für Feldgruppen.
Orientiere dich an der Tabelle in [Abschnitt 9](#9-felder--field-grids).

### Schritt 6: Listen und Spezialkomponenten

- Kontaktliste → [Abschnitt 13](#13-kontaktliste)
- Tags/Chips → [Abschnitt 14](#14-tags--chips)
- Historieliste → [Abschnitt 15](#15-kontakthistorie)

### Schritt 7: Modals

Für jede CRUD-Operation auf Unterentitäten ein Modal anlegen (am Ende des `<body>`, vor `</body>`).

### Schritt 8: Vanilla JS

Mindest-JS pro Formular:
```javascript
// Kartentoggle
function toggleCard(id) {
  const body = document.getElementById(id);
  const hd   = body.previousElementSibling;
  const chv  = hd.querySelector('.fcard-chv');
  const open = body.style.display !== 'none';
  body.style.display = open ? 'none' : '';
  chv.classList.toggle('open', !open);
}

// Modal
function openModal(id)  { document.getElementById(id).classList.remove('hidden'); }
function closeModal(id) { document.getElementById(id).classList.add('hidden'); }

// Backdrop
document.querySelectorAll('.mbk').forEach(m =>
  m.addEventListener('click', e => { if (e.target === m) m.classList.add('hidden'); })
);
```

### Checkliste vor Fertigstellung

```
[ ] Alle Karten-Titel in Großbuchstaben und mit fcard-title?
[ ] Jedes Eingabefeld hat ein <label>?
[ ] FK-Felder (read-only) mit .fg-readonly statt <input> umgesetzt?
[ ] Destruktive Aktionen gehen durch ein Bestätigungs-Modal?
[ ] ps-dirty-banner ist INNERHALB des <form>-Elements?
[ ] Responsive getestet (900px und 520px Breakpoints)?
[ ] Fokus-Reihenfolge (Tab) sinnvoll?
[ ] Tooltips (title="…") auf Icon-Buttons gesetzt?
[ ] Zuordnungstabellen verwenden .no-click und .td-acts?
[ ] Dynamisch gefärbte Badges verwenden .badge-dyn mit inline-style?
[ ] Profilsuche: body > .btn-row und #chat-page statt .form-grid?
[ ] Profilsuche: Sidebar-Toggle für mobile Screens vorhanden (#sidebar-backdrop)?
[ ] Profilsuche: User/Assistant-Bubbles korrekt mit .chat-msg.user / .chat-msg.assistant?
[ ] Löschen-Button in Toolbar ruft openModal() auf (nicht direktes Löschen)?
[ ] Kontaktsperre-Banner nutzt .banner-forbidden (nicht .banner-error)?
[ ] Admin-Navigation nutzt .btn-row (nicht .tab-nav oder eigene Struktur)?
[ ] Tabellenzeilen mit Kontaktsperre haben .row-forbidden?
```

---

## 20 Read-only Link-Feld

Für FK-Felder, die einen verknüpften Datensatz anzeigen aber nicht editierbar sind
(z. B. `partner_id` im Freiberufler-Formular, `customer_id` / `partner_id` im Projekt-Formular).
Das Feld hat optisch dieselbe Höhe und denselben Hintergrund wie ein normales Input, enthält aber
einen anklickbaren Link statt eines Eingabeelements.

```html
<!-- Mit zugeordnetem Datensatz -->
<div class="fg">
  <label>Partner</label>
  <div class="fg-readonly">
    <a href="/partner/17">Mustermann GmbH</a>
  </div>
</div>

<!-- Ohne Zuordnung (Neuanlage oder leer) -->
<div class="fg">
  <label>Partner</label>
  <div class="fg-readonly empty">Kein Partner zugeordnet</div>
</div>
```

**Regeln:**
- Niemals `<input readonly>` verwenden – Screenreader und Tastaturfokus würden Bearbeitbarkeit suggerieren.
- Link öffnet den verknüpften Datensatz im entsprechenden Formular.
- Leerer Zustand: Klasse `.empty` ergänzen, Text in Kursiv und `--text-3`.
- Das Feld nimmt an der normalen Field-Grid-Struktur teil (`.fg` innerhalb `.fg2` etc.).

---

## 21 Gemerktes Projekt (Toolbar-Erweiterung)

Das gemerkte Projekt wird als kompaktes Element in der Toolbar angezeigt.
`#tb-project` wird zwischen Audit-Info und Actions-Bereich platziert.

### Nur Anzeige (Partner- und Kunden-Formular)

```html
<div id="tb-project" th:if="${rememberedProject != null}">
  📌 <a th:href="@{/project/{id}(id=${rememberedProject.projectId})}">
    <strong th:text="${rememberedProject.projectNumber}"></strong>
    – <span th:text="${rememberedProject.shortDescription}"></span>
  </a>
</div>
```

### Mit Zuordnungs-Button (Freiberufler-Formular, Datensatz geladen)

```html
<div id="tb-project" th:if="${rememberedProject != null}">
  📌 <a th:href="@{/project/{id}(id=${rememberedProject.projectId})}">
    <strong th:text="${rememberedProject.projectNumber}"></strong>
    – <span th:text="${rememberedProject.shortDescription}"></span>
  </a>
  <button class="btn btn-ghost btn-sm" onclick="openModal('modal-assign-project')">
    Zuordnen
  </button>
</div>
```

**Regeln:**
- `#tb-project` nur rendern, wenn ein gemerktes Projekt vorhanden ist (server-seitig via Thymeleaf `th:if`).
- `#tb-project` hat `border-left`/`border-right`, `height: 100%`, `max-width: 460px`.
- Zuordnungs-Button nur im Freiberufler-Formular und nur wenn ein Datensatz (ID) geladen ist.
- Auf Mobilgeräten (< 860px) wird `#tb-project` automatisch per CSS ausgeblendet.

---

## 22 Dynamisch gefärbte Status-Badges

Für Statii, deren Farbe in der Datenbank (`project_position_status.color`) definiert ist und sich
zur Laufzeit ändern kann. Die Farbe wird als inline-style gesetzt.

```html
<!-- Farbe aus DB: background und color als CSS-Werte gespeichert -->
<span class="badge-dyn" style="background:#d1fae5; color:#065f46;">Im Gespräch</span>
<span class="badge-dyn" style="background:#fef9c3; color:#713f12;">Vorgeschlagen</span>
<span class="badge-dyn" style="background:#fef2f2; color:#b91c1c;">Abgesagt</span>
<span class="badge-dyn" style="background:#dbeafe; color:#1e40af;">Platziert</span>
```

Alternativ existieren auch `.status-badge` (Alias) und `.badge-default` (grüner Default-Wert):
```html
<span class="badge-default">Aktiv</span>
```
`.badge-default` hat fix `--badge-bg: #d1fae5; --badge-fg: #065f46`.

**Regeln:**
- `.badge-dyn` liefert nur Form und Typografie; Farben kommen ausschließlich via inline-style.
- In der Datenbank sind zwei Felder definiert: `color` (Hintergrundfarbe) und `color_text` (Textfarbe),
  beide als CSS-Farbcodes (z. B. `#d1fae5` / `#065f46`). Beide sind NOT NULL.
- Rendering: `style="background: <color>; color: <color_text>;"` — beide Werte direkt aus der DB.
- Konvention im Administrationsbereich: heller Hintergrundton + dunkler Textton desselben Farbtons
  für WCAG AA (Kontrastverhältnis ≥ 4,5:1).
- Nicht für statische, unveränderliche Badges verwenden — dort die `.cbadge`-Klassen nutzen.

---

## 23 Inline-Zuordnung

Für die direkte Zuweisung eines bestehenden Datensatzes per Eingabefeld innerhalb einer Listenansicht
(z. B. Freiberufler per Kodierung einem Partner zuordnen).

```html
<div class="list-hd">
  <div class="btn-row-inline">
    <input type="text" id="fl-code-input" placeholder="Code eingeben" class="input-narrow">
    <button class="btn btn-ghost btn-sm" onclick="assignByCode()">Zuordnen</button>
  </div>
</div>

<!-- Fehlermeldung (erscheint nach erfolglosem Versuch) -->
<p class="text-muted" id="assign-error" style="display:none; color:var(--danger);">
  Kein Freiberufler mit dieser Kodierung gefunden.
</p>
```

**Regeln:**
- `.input-narrow` begrenzt das Eingabefeld auf `max-width: 7rem`.
- `.btn-row-inline` ist ein Flex-Container ohne `margin-top` (für Verwendung in `.list-hd`).
- `.list-hd` richtet Label/Titel links und Aktionen rechts aus (`justify-content: flex-end`).
- Fehlermeldung erscheint direkt unterhalb der Zeile (kein Modal).
- Nach erfolgreicher Zuordnung: Eingabefeld leeren, Liste neu laden, Fehlermeldung ausblenden.

---

## 24 Chat: Split-Panel-Layout (Profilsuche)

Die Profilsuche verwendet **kein** `.form-grid`. Stattdessen ersetzt `#chat-page` den `#page`-Container
und teilt den Inhaltsbereich in Sidebar und Hauptbereich auf. Vor `#chat-page` steht eine
Modul-Navigationsleiste (`.btn-row`), die direkt als Kind von `<body>` gerendert wird.

```html
<body>
  <nav id="app-nav">…</nav>
  <div id="toolbar"><div id="toolbar-inner">…</div></div>

  <!-- Modul-Navigation direkt unter Toolbar -->
  <div class="btn-row mb-md">
    <a href="/profilesearch/chat" class="btn btn-pri">Chat / interaktiver Modus</a>
    <a href="/profilesearch/search" class="btn btn-ghost">Volltextsuche</a>
  </div>

  <!-- Split-Panel: Sidebar + Chat-Hauptbereich -->
  <div id="chat-page">
    <aside id="chat-sidebar">…</aside>           <!-- Abschnitt 25 -->
    <div id="chat-main">
      <div id="chat-messages">…</div>            <!-- Abschnitt 26 -->
      <div id="chat-input-area">…</div>          <!-- Abschnitt 27 -->
    </div>
  </div>

  <!-- Backdrop für Sidebar-Overlay (mobile) -->
  <div id="sidebar-backdrop" onclick="closeSidebar()"></div>
</body>
```

**CSS-Details:**
- `body > .btn-row`: max-width 1400px, margin 18px auto 0, padding 10px 20px 18px 18px.
- `#chat-page`: `height: calc(100vh - var(--nav-h) - var(--toolbar-h) - 18px - 43px)`, `overflow: hidden`,
  Rahmen ohne `border-top`, abgerundete untere Ecken.
- Auf großen Screens (≥ 1024px): Sidebar ist fest sichtbar; kein Backdrop.
- Auf kleinen Screens (< 1024px): Sidebar als Overlay mit Backdrop (`#sidebar-backdrop`).

```javascript
// Sidebar-Toggle (großer Screen)
function toggleSidebar() {
  document.getElementById('chat-sidebar').classList.toggle('collapsed');
}

// Sidebar-Overlay (kleiner Screen)
function openSidebar() {
  document.getElementById('chat-sidebar').classList.add('open');
  document.getElementById('sidebar-backdrop').classList.add('visible');
}
function closeSidebar() {
  document.getElementById('chat-sidebar').classList.remove('open');
  document.getElementById('sidebar-backdrop').classList.remove('visible');
}
```

---

## 25 Chat: Sidebar (Sitzungsliste)

Die Sidebar listet alle Chat-Sitzungen des Sachbearbeiters.

```html
<aside id="chat-sidebar">
  <!-- Toggle-Button (immer sichtbar, auch eingeklappt) -->
  <button class="sidebar-toggle" onclick="toggleSidebar()" title="Verlauf ein-/ausblenden">☰</button>

  <p class="sidebar-label">Chat-Verlauf</p>

  <div class="chat-session-list" id="sidebar-session-list">

    <!-- Aktive Sitzung (mit Projektbezug) -->
    <div class="chat-session-item active">
      <div class="session-info">
        <div class="session-title">Java-Entwickler Senior, München</div>
        <div class="session-project">📁 2026-042</div>
        <div class="session-meta">Heute, 14:32</div>
      </div>
      <button class="ibtn del" title="Chat löschen"
              onclick="confirmDeleteChat(event, 1)">🗑</button>
    </div>

    <!-- Inaktive Sitzung (ohne Projektbezug) -->
    <div class="chat-session-item" onclick="location.href='/profilesearch/chat/2'">
      <div class="session-info">
        <div class="session-title">DevOps-Engineer mit K8s-Erfahrung</div>
        <!-- kein .session-project, da project_id = NULL -->
        <div class="session-meta">Gestern</div>
      </div>
      <button class="ibtn del" title="Chat löschen"
              onclick="confirmDeleteChat(event, 2)">🗑</button>
    </div>

    <!-- Infinite Scroll Sentinel (optional) -->
    <ps-infinite-scroll
        data-next-url="/profilesearch/chat/1?offset=20"
        data-target="#sidebar-session-list">
    </ps-infinite-scroll>

  </div>
</aside>
```

**Seitenleiste eingeklappt:** `.chat-session-item.collapsed` reduziert Sidebar auf 48px Breite;
`.sidebar-label` und `.chat-session-list` werden ausgeblendet.

**Regeln:**
- `.sidebar-toggle` ist immer sichtbar, auch wenn die Sidebar eingeklappt ist.
- `.session-title` wird mit `text-overflow: ellipsis` abgeschnitten.
- `.session-project` nur rendern, wenn `project_id ≠ NULL`.
- Der Löschen-Button (`.ibtn.del`) ist im Ruhezustand unsichtbar (`opacity: 0`) und erscheint
  beim Hover der Zeile.
- Die aktive Sitzung erhält die Klasse `.active`.
- Sitzungen werden nach `changed_date` absteigend sortiert (jüngste zuerst).
- `ps-infinite-scroll` übernimmt das Nachladen älterer Sitzungen per IntersectionObserver.

---

## 26 Chat: Nachrichten

Der Nachrichtenbereich zeigt User-, Assistenten-, Tool-Call- und Tool-Result-Nachrichten.

```html
<div id="chat-messages">

  <!-- Leerer Zustand -->
  <div class="empty-chat">
    Stellen Sie Ihre erste Frage zur Profilsuche.
  </div>

  <!-- Nutzernachricht -->
  <div class="chat-msg user">
    <div class="msg-bubble">
      Ich suche einen Senior Java-Entwickler mit Spring-Erfahrung für München.
    </div>
    <div class="msg-meta">14:32</div>
  </div>

  <!-- Assistenten-Antwort (Markdown gerendert) -->
  <div class="chat-msg assistant">
    <div class="msg-bubble">
      <p>Ich habe folgende passende Profile gefunden:</p>
      <ul>
        <li><a href="/freelancer/42">DEV-2024-07 – Max Müller</a> – Java/Spring, 95 €/h</li>
      </ul>
    </div>
    <div class="msg-meta">14:32</div>
  </div>

  <!-- Tool-Call-Nachricht -->
  <div class="msg-tool msg-tool-call">
    <button class="msg-tool-header" aria-expanded="false" onclick="toggleToolMsg(this)">
      <span>🔧</span>
      <span class="msg-tool-name">search_freelancers</span>
      <span class="msg-tool-chevron">▶</span>
    </button>
    <div class="msg-tool-body">
      <pre>{"query": "Java Senior München"}</pre>
    </div>
  </div>

  <!-- Tool-Result-Nachricht -->
  <div class="msg-tool msg-tool-result">
    <button class="msg-tool-header" aria-expanded="false" onclick="toggleToolMsg(this)">
      <span>✅</span>
      <span class="msg-tool-name">search_freelancers</span>
      <span class="msg-tool-chevron">▶</span>
    </button>
    <div class="msg-tool-body">
      <pre>{"results": [...]}</pre>
    </div>
  </div>

  <!-- Ladeindikator -->
  <div class="chat-msg assistant chat-typing">
    <div class="msg-bubble">
      <div class="typing-dots">
        <span></span><span></span><span></span>
      </div>
    </div>
  </div>

</div>
```

**Regeln:**
- User-Bubble: rechtsbündig (`align-self: flex-end`), Primärfarbe (`--pri`) als Hintergrund, weißer Text, `white-space: pre-wrap`.
- Assistenten-Bubble: linksbündig (`align-self: flex-start`), `--surface-3` Hintergrund mit Border, Markdown gerendert.
- Tool-Call (`.msg-tool-call`): linke Border `--text-3` (grau).
- Tool-Result (`.msg-tool-result`): linke Border `--ok-border` (grün).
- `.msg-tool-body` ist standardmäßig ausgeblendet; Klasse `.open` zeigt ihn.
- `.msg-tool-header[aria-expanded="true"] .msg-tool-chevron` rotiert 90°.
- Ladeindikator (`.chat-typing`) wird eingeblendet, während die Antwort generiert wird,
  und nach Erhalt der Antwort ersetzt.
- Freiberufler-Links im gespeicherten Content: Format `[freelancer:<id>:<anzeigetext>]` wird
  beim Rendering in `<a href="/freelancer/<id>"><anzeigetext></a>` umgewandelt.

---

## 27 Chat: Eingabebereich & Toolbar-Variante

### Eingabebereich

```html
<div id="chat-input-area">
  <ps-chat-input id="ps-chat-input-el">
    <textarea id="chat-input" rows="3"
              placeholder="Nachricht eingeben… (Enter = Senden, Shift+Enter = Zeilenumbruch)"></textarea>
    <div class="chat-input-btns">
      <span id="ctx-usage" class="ctx-usage" hidden></span>
      <button type="button" id="chat-stop" class="btn btn-ghost btn-sm" hidden>⏹ Stop</button>
      <button type="button" id="chat-send" class="btn btn-pri btn-sm">↑ Senden</button>
    </div>
  </ps-chat-input>
</div>
```

**Token-Anzeige (`.ctx-usage`):**
```html
<span class="ctx-usage">12.345 / 100.000 Tokens (12 %)</span>
```
`.ctx-usage` hat `font-size: 11px`, `color: var(--text-3)`, `white-space: nowrap`,
`font-variant-numeric: tabular-nums`. Wird per `hidden`-Attribut ein-/ausgeblendet.

**Chat-Input-Buttons (`.chat-input-btns`):**
Absolute Positionierung unten rechts im `ps-chat-input`-Container:
- `.ctx-usage` — Token-Verbrauchsanzeige, pusht Buttons nach rechts (`margin-right: auto`).
- `#chat-stop` — Stop-Button, nur während aktiver Anfrage sichtbar.
- `#chat-send` — Senden-Button.

**Regeln:**
- `ps-chat-input` ist das Custom Element, das Auto-Resize und Enter/Shift+Enter steuert.
- Textarea wächst automatisch bis zu 240px, danach intern scrollbar.
- `#chat-input` hat `padding: 10px 14px 48px` (Platz für `.chat-input-btns` unten).
- Senden- und Stop-Button sind während aktiver Anfrage per `disabled`-Attribut gesteuert.

### Toolbar-Variante Profilsuche

Die Toolbar der Profilsuche hat **keine** Datensatz-Navigation und **keine** Audit-Information.

```html
<div id="toolbar">
  <div id="toolbar-inner">
    <!-- Modulbezeichnung -->
    <div id="tb-module">Profilsuche</div>

    <!-- Sidebar-Toggle (nur auf kleinen Screens sichtbar) -->
    <button class="sidebar-toggle-mobile btn btn-ghost btn-sm"
            onclick="openSidebar()" title="Chat-Verlauf">☰</button>

    <!-- Gemerktes Projekt (falls vorhanden) -->
    <div id="tb-project" th:if="${rememberedProject != null}">
      📌 <a th:href="@{/project/{id}(id=${rememberedProject.projectId})}">
        <strong th:text="${rememberedProject.projectNumber}"></strong>
        – <span th:text="${rememberedProject.shortDescription}"></span>
      </a>
    </div>

    <!-- Aktionen -->
    <div id="tb-actions">
      <form th:action="@{/profilesearch/chat/new}" method="post" class="inline">
        <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
        <button type="submit" class="btn btn-ghost">＋ Neuer Chat</button>
      </form>
      <button type="button" class="btn btn-danger" onclick="openModal('modal-delete')">
        🗑 Chat löschen
      </button>
    </div>
  </div>
</div>
```

**Regeln:**
- Kein `#tb-nav` (keine Datensatz-Navigation).
- Kein `#tb-audit` (keine Audit-Information).
- `.sidebar-toggle-mobile` ist nur auf kleinen Screens (< 1024px) sichtbar (Desktop: `display: none`).
- `#tb-project` wird nur gerendert, wenn ein gemerktes Projekt vorhanden ist.
- `btn-danger` für „Chat löschen" öffnet einen Bestätigungsdialog (`.mbk`).

---

## 28 JavaScript-Architektur und Custom Elements

### Verhältnis: Design-System-HTML ↔ Custom Elements

Die HTML-Muster in diesem Dokument beschreiben die **statische Grundstruktur**, die der Server per
Thymeleaf rendert. Eine Teilmenge dieser Elemente wird durch **Custom Elements** (`ps-`-Präfix) mit
interaktivem Verhalten erweitert — gemäß `specs/SWARCHITEKTUR.md` Abschnitt 5 (ADR-011).

**Prinzip Progressive Enhancement:** Custom Elements sind keine Ersetzung der Design-System-Muster,
sondern deren JavaScript-Erweiterungsschicht. Der Server rendert die vollständige HTML-Struktur
innerhalb des Custom-Element-Tags. Ohne JS funktioniert die Seite als normales HTML. Mit JS
upgradet `customElements.define()` das Element und fügt Verhalten hinzu.

### Mapping: Design-System-Muster → Custom Element

| Design-System-Muster          | Abschnitt | Custom Element          | Kapseltes Verhalten                             |
|-------------------------------|-----------|-------------------------|-------------------------------------------------|
| `.mbk`-Modal-Struktur         | §17       | `<ps-modal>`            | open/close, Focus-Trap, Escape-Handler, ARIA    |
| `<table>`/`<ps-infinite-scroll>` | §16, §25 | `<ps-infinite-scroll>` | IntersectionObserver, AJAX-Nachladen            |
| `#chat-input`-Textarea        | §27       | `<ps-chat-input>`       | Auto-Resize, Enter/Shift+Enter, pending-Attribut|
| `ps-dirty-banner`             | §7        | `<ps-dirty-banner>`     | Dirty-State, .visible-Klasse per Change-Events  |

**Beispiel — Modal:**
```html
<!-- Server rendert Design-System-Struktur innerhalb des Custom-Element-Tags: -->
<ps-modal>
  <div class="mbk" id="modal-kontakt">
    <div class="mbox">
      <div class="mhd">
        <h3>Kontaktmöglichkeit bearbeiten</h3>
        <button class="mx">✕</button>
      </div>
      <div class="mbody"><!-- Felder --></div>
      <div class="mft">
        <button class="btn btn-ghost">Abbrechen</button>
        <button class="btn btn-pri">Speichern</button>
      </div>
    </div>
  </div>
</ps-modal>
<!-- Ohne JS: <ps-modal> ist ein Block-Element; .mbk steuert die Darstellung (hidden-Klasse) -->
<!-- Mit JS:  customElements.define('ps-modal', ...) kapselt open/close, Focus-Trap, ARIA   -->
```

**Beispiel — ps-infinite-scroll:**
```html
<!-- Sentinel am Ende der Liste: lädt automatisch die nächste Seite -->
<ps-infinite-scroll
    data-next-url="/freelancer/search?offset=20&q=Java"
    data-target="#results-tbody">
</ps-infinite-scroll>
```
`data-next-url`: URL der nächsten Seite (Partial-Fragment). `data-target`: CSS-Selektor des
Containers, in den die nachgeladenen Zeilen eingefügt werden.

### Globale JavaScript-Initialisierung (`main.js`)

`main.js` ist der Einstiegspunkt des Frontend-Builds und enthält systemweite Initialisierungen:

| Initialisierung                               | Zweck                                                            | Referenz                    |
|-----------------------------------------------|------------------------------------------------------------------|-----------------------------|
| `customElements.define('ps-modal', ...)` etc. | Registriert alle Custom Elements                                 | SWARCHITEKTUR.md §5 ADR-011 |
| `apiFetch`-Wrapper (CSRF-Token)               | Setzt `X-XSRF-TOKEN`-Header bei allen state-mutierenden Requests | SWARCHITEKTUR.md §5         |
| `pageshow`-Event-Handler                      | Reload bei Wiederherstellung aus bfcache                         | SWARCHITEKTUR.md §5         |

**Regel für Template-Autoren:** Diese globalen Handler müssen **nicht** in einzelnen Templates
implementiert werden. Templates nutzen `apiFetch` statt `fetch` für alle POST/PUT/PATCH/DELETE-Requests.

### POST-Navigation für Datensatz-Navigation

Die Navigations-Buttons (`#tb-nav`) werden als `<form method="post">`-Elemente gerendert (ADR-014),
**nicht** als vorberechnete `<a>`-Links. Der Server berechnet die Ziel-ID frisch aus der Datenbank.

---

## 29 Modul-Navigation (btn-row)

Innerhalb von `#page` oder als direktes Kind von `<body>` (bei Chat-Seiten) wird eine horizontale
Navigationsleiste aus `.btn-row` mit `.btn`-Elementen gebaut.

```html
<!-- Innerhalb #page (Admin-Bereich) -->
<div id="page">
  <div class="btn-row mb-md">
    <a class="btn btn-ghost" th:href="@{/admin/historientypen}">Historientypen</a>
    <a class="btn btn-ghost" th:href="@{/admin/positionsstatus}">Positionsstatus</a>
    <a class="btn btn-ghost" th:href="@{/admin/tags}">Tags</a>
    <a class="btn btn-pri"   th:href="@{/admin/benutzer}">Benutzer</a>
  </div>
  …
</div>

<!-- Als body-Kind (Profilsuche, über #chat-page) -->
<div class="btn-row mb-md">
  <a href="/profilesearch/chat" class="btn btn-pri">Chat / interaktiver Modus</a>
  <a href="/profilesearch/search" class="btn btn-ghost">Volltextsuche</a>
</div>
<div id="chat-page">…</div>
```

**CSS-Details:**
- `.btn-row`: `display: flex; gap: 8px; margin-top: 12px`.
- `body > .btn-row`: zusätzlich `max-width: 1400px; width: 100%; margin: 18px auto 0; padding: 10px 20px 18px 18px`.
- Aktiver Eintrag erhält Klasse `.btn-pri` (statt `.btn-ghost`).

---

## 30 Datentabelle (data-table)

Im Admin-Bereich und für einfache Listendarstellungen ohne QBE-Suchfilter wird `.data-table`
innerhalb von `.tbl-wrap` verwendet (statt der anonymen `<table>`-Variante aus §16).

```html
<div class="tbl-wrap">
  <table class="data-table">
    <thead>
      <tr>
        <th>Benutzername</th>
        <th>Aktiv</th>
        <th>Aktionen</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td>admin</td>
        <td><span class="chip-add">✔ Aktiv</span></td>
        <td>
          <button class="ibtn" title="Bearbeiten">✎</button>
          <button class="ibtn del" title="Löschen">🗑</button>
        </td>
      </tr>
      <tr th:if="${#lists.isEmpty(items)}">
        <td colspan="3" class="td-empty">Keine Einträge vorhanden.</td>
      </tr>
    </tbody>
  </table>
</div>
```

**Unterschied zu §16:**
- `.data-table` hat schwerere Kopfzeile: `font-size: .7rem`, `font-weight: 800`,
  `letter-spacing: .06em`, `border-bottom: 2px solid var(--border)`.
- `tbody tr.clickable` für optional klickbare Zeilen (Cursor: pointer).
- `.td-empty`: zentrierte Leerzeilen-Zelle (`color: var(--text-3)`).
- Keine `.srt`-Sortierklassen — Sortierung in Admin-Tabellen ist nicht vorgesehen.

---

## 31 Utility-Klassen

Häufig verwendete Hilfsklassen aus `components2.css`:

| Klasse             | Wirkung                                                          |
|--------------------|------------------------------------------------------------------|
| `.hidden`          | `display: none !important` — universelles Ausblenden            |
| `.input-narrow`    | `max-width: 7rem` — schmales Eingabefeld (z. B. PLZ)            |
| `.btn-row`         | Flex-Zeile, `gap: 8px`, `margin-top: 12px`                      |
| `.btn-row-inline`  | Flex-Zeile, `gap: 8px`, kein margin-top (für `.list-hd`)        |
| `.list-hd`         | Flex, `justify-content: flex-end`, `margin-bottom: .5rem`       |
| `.text-muted`      | Italics, `color: var(--text-3)`, `.82rem`                       |
| `.text-muted-block`| Wie `.text-muted` + `margin: 4px 6px` (für Listen-Leerzustände) |
| `.flex-spacer`     | `flex: 1` — schiebt folgende Elemente an den rechten Rand       |
| `.pt-label`        | `padding-top: 18px` — Ausrichtungshilfe neben Datumfeld         |
| `.mt-md`           | `margin-top: 1rem`                                               |
| `.mt-lg`           | `margin-top: 1.5rem`                                             |
| `.mb-md`           | `margin-bottom: 1rem`                                            |
| `.mb-lg`           | `margin-bottom: 1.5rem`                                          |
| `.mb`              | `margin-bottom: 10px` (aus `base.css`)                          |
| `form.inline`      | `display: inline` — Logout-Formular in Navigation               |
| `.btn-logout`      | Unstyled Submit-Button, erbt Styling vom Eltern-Kontext         |
| `.sort-btn`        | Unstyled Button in Tabellenkopf-Zellen, Text unterstrichen      |

---

*Letzte Änderung: März 2026*
