# UI Design System – Powerstaff 2026

**Version:** 1.2 · März 2026
**Zweck:** Verbindliche Gestaltungsrichtlinien für alle Formulare und Ansichten der Applikation.
Neue Formulare werden ausschließlich mit `base.css` und den hier beschriebenen HTML-Mustern erstellt.

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

| Token                   | Wert                          | Verwendung                 |
|-------------------------|-------------------------------|----------------------------|
| `--danger`              | `#b91c1c`                     | Fehlerzustand, Löschen     |
| `--danger-l`            | `#fef2f2`                     | Gefahr-Hintergrund         |
| `--warn-bg/border/text` | `#fffbeb / #d97706 / #7c4a03` | Warnbanner (ungespeichert) |
| `--ok-bg/border/text`   | `#f0fdf4 / #16a34a / #14532d` | Erfolgsbanner              |

### Abstände & Schatten

| Token         | Wert                                                   |
|---------------|--------------------------------------------------------|
| `--r`         | `5px` – Standard-Borderradius                          |
| `--r-l`       | `8px` – großer Borderradius (Karten, Modals)           |
| `--shadow-xs` | `0 1px 2px rgba(0,0,0,.06)`                            |
| `--shadow-sm` | `0 1px 4px rgba(0,0,0,.08), 0 1px 2px rgba(0,0,0,.05)` |
| `--shadow-md` | `0 4px 12px rgba(0,0,0,.1)`                            |
| `--shadow-lg` | `0 16px 40px rgba(0,0,0,.15)`                          |
| `--nav-h`     | `44px`                                                 |
| `--toolbar-h` | `50px`                                                 |

---

## 3 Typografie

- **Schriftart:** `system-ui, -apple-system, "Segoe UI", Roboto, Arial, sans-serif`
  Keine externen Webfonts – maximale Performance und Offline-Fähigkeit.
- **Basis-Fontsize:** `14px` (auf `<html>`)
- **Line-height:** `1.55`

| Verwendung            | Größe        | Gewicht | Besonderheiten                                                              |
|-----------------------|--------------|---------|-----------------------------------------------------------------------------|
| Primärtext            | `.88rem`     | 400     | Input-Werte                                                                 |
| Sekundärtext / Labels | `.72–.75rem` | 600     | `letter-spacing: .02em`                                                     |
| Kartenüberschriften   | `.7rem`      | 800     | `text-transform: uppercase`, `letter-spacing: .07em`                        |
| Subsection-Labels     | `.67rem`     | 800     | `text-transform: uppercase`, `letter-spacing: .1em`, `color: var(--text-3)` |
| Audit-Meta            | `.75rem`     | 400     | `color: var(--text-3)`                                                      |
| Badge-Text            | `.65–.7rem`  | 700–800 | `text-transform: uppercase`                                                 |

---

## 4 Seitenstruktur (Shell-Layout)

```
┌─────────────────────────────────────────────────┐
│  #app-nav  (sticky, top:0, z:110, dunkel)        │
├─────────────────────────────────────────────────┤
│  #toolbar  (sticky, top:44px, z:100, weiß)       │
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

```html
<!DOCTYPE html>
<html lang="de">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Powerstaff 2026 – [Modulname]</title>
  <link rel="stylesheet" href="../prototype/base.css">
</head>
<body>

  <!-- App-Navigation -->
  <nav id="app-nav">…</nav>

  <!-- Form-Toolbar -->
  <div id="toolbar">…</div>

  <!-- Banners -->
  <div id="banner-unsaved" class="banner banner-unsaved hidden">…</div>

  <!-- Seiteninhalt -->
  <div id="page">
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
    <span class="menu-item active">Freiberuflerverwaltung</span>
    <span class="menu-item">Partnerverwaltung</span>
    <span class="menu-item">Kundenverwaltung</span>
    <span class="menu-item">Projekte</span>
    <span class="menu-item">Profilsuche</span>
    <span class="menu-item">Administration</span>
  </div>
</nav>
```

**Regeln:**
- Aktiver Menüpunkt erhält Klasse `.active` (blauer Unterstrich `#60a5fa`).
- Logo: `Power` in Weiß, Akzentbuchstaben `staff` in `#60a5fa`.
- Max-Breite des Inners: `1400px`, zentriert.
- Horizontales Scrollen auf kleinen Bildschirmen (Scrollbar unsichtbar).

---

## 6 Form-Toolbar

Die Toolbar ist **modulspezifisch** und enthält Navigation, Audit-Info und Aktionen.

```html
<div id="toolbar">
  <!-- Modulbezeichnung -->
  <div id="tb-module">Freiberufler</div>

  <!-- Datensatz-Navigation -->
  <div id="tb-nav">
    <button class="tb-nav-btn" title="Erster">⏮</button>
    <button class="tb-nav-btn" title="Vorheriger">◀</button>
    <input type="text" id="tb-id-input" placeholder="ID">
    <button class="tb-nav-btn" title="Nächster">▶</button>
    <button class="tb-nav-btn" title="Letzter">⏭</button>
  </div>

  <!-- Audit-Informationen -->
  <div id="tb-audit">
    Erstellt <strong>12.01.2025</strong> · Geändert <strong>10.03.2026</strong> von <strong>m.müller</strong>
  </div>

  <!-- Aktionen -->
  <div id="tb-actions">
    <button class="btn btn-ghost">＋ Neu</button>
    <button class="btn btn-pri">💾 Speichern</button>
    <button class="btn btn-danger">🗑 Löschen</button>
    <button class="btn btn-ghost">🔍 Suche</button>
  </div>
</div>
```

**Regeln:**
- `#tb-module` trennt sich visuell mit `border-right` vom Rest.
- Audit-Info (`#tb-audit`) wächst flexibel (`flex: 1`), wird auf Tablets ausgeblendet.
- Aktionen stehen immer am rechten Rand (`margin-left: auto`).

---

## 7 Banners

Banners erscheinen zwischen Toolbar und Formular. Sie werden per JS mit der Klasse `.hidden` ein- und ausgeblendet.

```html
<!-- Gesperrter Datensatz -->
<div class="banner banner-forbidden hidden">
  🔒 Dieser Datensatz ist gesperrt und kann nicht bearbeitet werden.
</div>

<!-- Ungespeicherte Änderungen (nur Hinweis, keine Blockierung) -->
<div class="banner banner-unsaved hidden">
  ⚠️ Es gibt ungespeicherte Änderungen.
</div>

<!-- Erfolgsmeldung -->
<div class="banner banner-success hidden">
  ✓ Datensatz erfolgreich gespeichert.
</div>
```

**Regeln:**
- Ungespeicherte Änderungen führen **nur** zu einem Hinweisbanner. Sie blockieren keine weiteren Aktionen.
- Erfolgsbanner wird nach ca. 3 Sekunden automatisch ausgeblendet.
- Maximal ein Banner gleichzeitig sichtbar (außer Forbidden bleibt permanent).

---

## 8 Formular-Karten (fcard)

Alle Formularinhalte werden in Karten gruppiert. Karten können eingeklappt werden.

```html
<div class="fcard">
  <div class="fcard-hd" onclick="toggleCard(this)">
    <span class="fcard-title">Abschnitt-Titel</span>
    <span class="fcard-chv open">▼</span>
  </div>
  <div class="fcard-body">
    <!-- Felder hier -->
  </div>
</div>
```

**Toggling (Vanilla JS):**
```javascript
function toggleCard(hd) {
  const body = hd.nextElementSibling;
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
- Karten starten eingeklappt oder ausgeklappt je nach Relevanz.

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

| Klasse | Spalten | Verwendung                   |
|--------|---------|------------------------------|
| `.fg2` | 2       | Standard-Zweiteilung         |
| `.fg3` | 3       | z. B. PLZ / Ort / Land       |
| `.fg4` | 4       | Datumsgruppen                |
| `.fg5` | 5       | Schmale Felder nebeneinander |

**Span-Klassen** (innerhalb eines Grid):

| Klasse | Wirkung               |
|--------|-----------------------|
| `.s2`  | Feld belegt 2 Spalten |
| `.s3`  | Feld belegt 3 Spalten |
| `.s4`  | Feld belegt 4 Spalten |

```html
<div class="fg3 mb">
  <div class="fg s2">
    <label>Straße</label>
    <input type="text">
  </div>
  <div class="fg">
    <label>Hausnummer</label>
    <input type="text">
  </div>
</div>
<div class="fg3">
  <div class="fg">
    <label>PLZ</label>
    <input type="text">
  </div>
  <div class="fg s2">
    <label>Ort</label>
    <input type="text">
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
    <option value="NL2">NL2</option>
    <option value="X">X</option>
    <option value="NO">NO</option>
    <option value="LL">LL</option>
  </select>
</div>
```

In der QBE-Suchmaske erscheint dasselbe `<select>`; ist kein Wert gewählt (leere Option), wird das Feld nicht in die WHERE-Klausel aufgenommen. Ist ein Wert gewählt, wird **exakt** danach gefiltert (kein LIKE).

**Regeln:**
- Klasse `.mb` (`margin-bottom: 10px`) auf einer Grid-Zeile trennt Gruppen voneinander.
- Inputs im Fokus: blaue Border + `box-shadow: 0 0 0 3px var(--pri-ring)`.
- Auf mobilen Geräten (< 520px) kollabieren alle Field-Grids auf 1 Spalte.

---

## 10 Buttons

### Varianten

```html
<!-- Primär (Speichern) -->
<button class="btn btn-pri">💾 Speichern</button>

<!-- Ghost (Sekundäraktion) -->
<button class="btn btn-ghost">＋ Neu</button>

<!-- Gefahr (Löschen) -->
<button class="btn btn-danger">🗑 Löschen</button>

<!-- Klein -->
<button class="btn btn-ghost btn-sm">Bearbeiten</button>

<!-- Hinzufügen (gestrichelt, inline) -->
<button class="btn-add">＋ Eintrag hinzufügen</button>
```

### Regeln
- Primär-Button immer für die Hauptaktion (Speichern).
- Ghost-Button für neutrale Aktionen (Neu, Abbrechen, Suche).
- Danger-Button für destruktive Aktionen (Löschen). Im Ruhezustand dezent (heller Hintergrund), beim Hover volles Rot.
- `.btn-add` für das Hinzufügen von Listeneinträgen direkt im Formular (gestrichelte Border, keine Füllung).
- Icon-Buttons (`.ibtn`) für Edit/Delete innerhalb von Listen.

```html
<!-- Icon-Button -->
<button class="ibtn" title="Bearbeiten">✎</button>
<button class="ibtn del" title="Löschen">🗑</button>
```

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
- Standard: grauer Hintergrund (`--surface-2`)
- `.on`: blaue Border + blauer Hintergrund (`--pri-l`)
- `.on-danger`: rote Border + roter Hintergrund (`--danger-l`)

**Klasse per JS setzen:**
```javascript
cbfield.addEventListener('change', e => {
  e.currentTarget.classList.toggle('on', e.target.checked);
});
```

---

## 12 Subsection-Label & Divider

Zur Unterteilung innerhalb einer Karte ohne eigene Kartenebene:

```html
<p class="sublbl">Adressdaten</p>
<!-- Felder -->
<hr class="div">
<p class="sublbl">Weitere Angaben</p>
```

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

| Klasse        | Typ     | Farbe |
|---------------|---------|-------|
| `.cb-email`   | E-Mail  | Blau  |
| `.cb-telefon` | Telefon | Lila  |
| `.cb-web`     | Website | Grün  |
| `.cb-xing`    | XING    | Cyan  |
| `.cb-gulp`    | GULP    | Gelb  |
| `.cb-fax`     | Fax     | Grau  |

**Regeln:**
- Neuanlage und Bearbeitung erfolgen über ein Modal (nie inline-Edit).
- `.citem:hover` hebt die Zeile blau hervor.

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

**Kleine Chips (Tabellenzellen):**
```html
<td><span class="chip-xs">Java</span><span class="chip-xs">Python</span></td>
```

**Regeln:**
- `select.chip-add` wird nach Auswahl per JS zurückgesetzt und ein neuer Chip erzeugt.
- `.tag-grid` ist 2-spaltig, auf Mobil 1-spaltig.

---

## 15 Kontakthistorie

Zeitliche Einträge (Anrufe, E-Mails, Notizen) in einem 2-spaltigen Grid.
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
- Hbody mit `white-space: pre-wrap` – Zeilenumbrüche im Text bleiben erhalten.
- Bearbeitung/Neuanlage über Modal.
- Typ-Badge nur einbauen, wenn das Modul eine `historytype`-Verknüpfung hat.

---

## 16 Tabellen (Suchergebnisse)

Für die QBE-Suche (Query by Example) und Trefferlistenansichten.

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
    </tbody>
  </table>
</div>
```

**Sortierbare Spalten:** Klasse `.srt` + aktueller Zustand `.srt-asc` oder `.srt-desc` (CSS fügt Pfeil-Indikator ein).

**Regeln:**
- Name-Zelle mit `.td-name` (blau, fett) – signalisiert Klickbarkeit.
- Tabellenzeile ist per `onclick` anklickbar und öffnet den Datensatz.
- Keine festen Spaltenbreiten – Tabelle passt sich dem verfügbaren Platz an.
- Container `.tbl-wrap` scrollt horizontal auf kleinen Bildschirmen.

### Variante: Tabelle mit Aktionsspalte (Zuordnungslisten)

Für Zuordnungslisten (z. B. Freiberufler im Projekt, Projekte beim Kunden) sind Zeilen nicht als Ganzes
anklickbar. Nur bestimmte Zellen oder Buttons lösen Aktionen aus. Die Klasse `.no-click` auf `<tr>`
deaktiviert den Zeilen-Hover-Effekt und den Pointer-Cursor.

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
          <button class="ibtn del" title="Zuordnung löschen">🗑</button>
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

Modale Dialoge für CRUD-Operationen auf Unterentitäten (Kontakte, Historieneinträge) und Systemmeldungen (Konflikte, Bestätigungen).

### Standard-Modal

```html
<div class="mbk hidden" id="modal-kontakt">
  <div class="mbox">
    <div class="mhd">
      <h3>Kontaktmöglichkeit bearbeiten</h3>
      <button class="mx" onclick="closeModal('modal-kontakt')">✕</button>
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
      <button class="btn btn-ghost" onclick="closeModal('modal-kontakt')">Abbrechen</button>
      <button class="btn btn-pri">Speichern</button>
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
      <button class="mx" onclick="closeModal('modal-confirm')">✕</button>
    </div>
    <div class="mbody">
      <p class="mwarn">
        Soll <strong>Max Müller (ID 1042)</strong> wirklich gelöscht werden?
        Diese Aktion kann nicht rückgängig gemacht werden.
      </p>
    </div>
    <div class="mft">
      <button class="btn btn-ghost" onclick="closeModal('modal-confirm')">Abbrechen</button>
      <button class="btn btn-danger">Löschen</button>
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
- Backdrop: `rgba(15,23,42,.45)` mit `backdrop-filter: blur(3px)`.
- Einblende-Animation: `min-in` (Scale + Translate, 140ms).
- Footerleiste `.mft` hat `--surface-2` als Hintergrund.
- Felder im Modal: Klasse `.mfld` mit `margin-bottom: 12px`.
- Klick auf Backdrop schließt das Modal.

---

## 18 Interaktionsmuster

### Kollabierbare Karten

Alle Karten können über den Kopfbereich ein-/ausgeklappt werden. `toggleCard(hd)` wird auf das `fcard-hd`-Element gebunden.

### Ungespeicherte Änderungen

- Jede Änderung an einem Formularfeld setzt `unsavedChanges = true`.
- Der Banner `#banner-unsaved` wird eingeblendet.
- **Keine** Blockierung von Navigation oder anderen Aktionen.
- Nach erfolgreichem Speichern: Banner ausblenden, Erfolgsbanner einblenden.

### Datensatz-Navigation

- Pfeil-Buttons in `#tb-nav` navigieren durch bekannte IDs.
- ID-Eingabe im Textfeld lädt den Datensatz direkt.
- Deaktivierte Buttons (`disabled`-Attribut) bei erstem/letztem Datensatz.

### Optimistic Locking

Bei Speichern-Konflikten (Datensatz zwischenzeitlich geändert) erscheint ein Modal mit zwei Optionen:
1. Eigene Änderungen verwerfen und aktuellen Stand laden.
2. Eigene Version erzwingen (Überschreiben).

### Sortierung in Tabellen

- Klick auf `.srt`-Spaltenheader togglet `srt-asc` → `srt-desc` → keine Sortierung.
- Aktueller Sortierstatus wird per CSS-Klasse visualisiert (▲/▼).

### Gemerktes Projekt

Das zuletzt im Projekte-Formular angezeigte Projekt wird server-seitig pro Sachbearbeiter gespeichert.
Es wird in der Toolbar der Formulare Freiberufler, Partner und Kunden angezeigt (Element `#tb-project`).
Im Freiberufler-Formular erscheint zusätzlich ein Zuordnungs-Button, wenn ein Datensatz geladen ist.
Beim Navigieren im Projekte-Formular aktualisiert der Server das gemerkte Projekt automatisch.

### Tooltips

Jedes Element mit `data-tip="..."` zeigt per CSS-Pseudo-Element einen Tooltip beim Hover.

```html
<button class="ibtn" data-tip="Bearbeiten">✎</button>
```

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

Verwende `.fg` für jedes Feld und `.fg2`–`.fg5` als Container für Feldgruppen.
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
function toggleCard(hd) {
  const body = hd.nextElementSibling;
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

// Ungespeicherte Änderungen
let dirty = false;
document.querySelectorAll('input, select, textarea').forEach(el =>
  el.addEventListener('change', () => {
    dirty = true;
    document.getElementById('banner-unsaved').classList.remove('hidden');
  })
);
```

### Checkliste vor Fertigstellung

- [ ] Alle Karten-Titel in Großbuchstaben und mit `fcard-title`?
- [ ] Jedes Eingabefeld hat ein `<label>`?
- [ ] FK-Felder (read-only) mit `.fg-readonly` statt `<input>` umgesetzt?
- [ ] Destruktive Aktionen gehen durch ein Bestätigungs-Modal?
- [ ] Banner-IDs sind korrekt vergeben und starten mit `.hidden`?
- [ ] Responsive getestet (860px und 520px Breakpoints)?
- [ ] Fokus-Reihenfolge (Tab) sinnvoll?
- [ ] Tooltips (`data-tip`) auf Icon-Buttons gesetzt?
- [ ] Zuordnungstabellen verwenden `.no-click` und `.td-acts`?
- [ ] Dynamisch gefärbte Badges verwenden `.badge-dyn` mit inline-style?
- [ ] Profilsuche: `#chat-page` statt `.form-grid` als Wurzelelement?
- [ ] Profilsuche: Sidebar-Toggle für mobile Screens vorhanden (`#sidebar-backdrop`)?
- [ ] Profilsuche: User/Assistant-Bubbles korrekt mit `.chat-msg.user` / `.chat-msg.assistant`?

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
    <a href="#" onclick="openPartner(17)">Mustermann GmbH</a>
  </div>
</div>

<!-- Ohne Zuordnung (Neuanlage oder leer) -->
<div class="fg">
  <label>Partner</label>
  <div class="fg-readonly empty">Kein Partner zugeordnet</div>
</div>

<!-- Vorausgefüllt aus Kontext (z. B. Projekt aus Kunden-Formular angelegt) -->
<div class="fg">
  <label>Kunde</label>
  <div class="fg-readonly">
    <a href="#" onclick="openKunde(5)">Acme Corp</a>
  </div>
</div>
```

**Regeln:**
- Niemals `<input readonly>` verwenden – Screenreader und Tastaturfokus würden Bearbeitbarkeit suggerieren.
- Link öffnet den verknüpften Datensatz im entsprechenden Formular.
- Leerer Zustand: Klasse `.empty` ergänzen, Text in Kursiv und `--text-3`.
- Das Feld nimmt an der normalen Field-Grid-Struktur teil (`.fg` innerhalb `.fg2` etc.).

---

## 21 Gemerktes Projekt (Toolbar-Erweiterung)

Das gemerkte Projekt wird als kompaktes Pill-Element in der Toolbar angezeigt. Im Freiberufler-Formular
ergänzt ein Button direkt daneben die Zuordnungsfunktion.

### Nur Anzeige (Partner- und Kunden-Formular)

```html
<div id="toolbar">
  <div id="tb-module">Partner</div>
  <div id="tb-nav">…</div>
  <div id="tb-audit">…</div>

  <!-- Gemerktes Projekt (nur Anzeige) -->
  <div id="tb-project">
    📌 Gemerktes Projekt: <strong>2026-042</strong> – Java-Entwickler München
  </div>

  <div id="tb-actions">…</div>
</div>
```

### Mit Zuordnungs-Button (Freiberufler-Formular, Datensatz geladen)

```html
<div id="toolbar">
  <div id="tb-module">Freiberufler</div>
  <div id="tb-nav">…</div>
  <div id="tb-audit">…</div>

  <!-- Gemerktes Projekt + Aktion -->
  <div id="tb-project">
    📌 <strong>2026-042</strong> – Java-Entwickler München
    <button class="btn btn-ghost btn-sm" onclick="openModal('modal-assign-project')">
      Zuordnen
    </button>
  </div>

  <div id="tb-actions">…</div>
</div>
```

**Regeln:**
- `#tb-project` nur rendern, wenn ein gemerktes Projekt vorhanden ist (server-seitig gesteuert).
- Zuordnungs-Button nur im Freiberufler-Formular und nur wenn ein Datensatz (ID) geladen ist.
- Das Element ordnet sich zwischen Audit-Info und Actions-Bereich ein.
- Auf Mobilgeräten (< 860px) kann `#tb-project` ausgeblendet werden, wenn der Platz nicht reicht.

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
<div class="inline-assign">
  <input type="text" placeholder="Kodierung eingeben…" id="assign-code-input">
  <button class="btn btn-ghost btn-sm" onclick="assignByCode()">Zuordnen</button>
</div>

<!-- Fehlermeldung (erscheint nach erfolglosem Versuch) -->
<p class="empty" id="assign-error" style="display:none; color:var(--danger);">
  Kein Freiberufler mit dieser Kodierung gefunden.
</p>
```

**Regeln:**
- Das Eingabefeld hat eine feste Breite (`180px`), wächst nicht flex.
- Fehlermeldung erscheint direkt unterhalb der Zeile (kein Modal).
- Nach erfolgreicher Zuordnung: Eingabefeld leeren, Liste neu laden, Fehlermeldung ausblenden.
- Für komplexere Zuordnungen (z. B. mit weiteren Pflichtfeldern) stattdessen ein Modal verwenden.

---

---

## 24 Chat: Split-Panel-Layout (Profilsuche)

Die Profilsuche verwendet **kein** `.form-grid`. Stattdessen ersetzt `#chat-page` den `#page`-Container
und teilt den Inhaltsbereich in Sidebar und Hauptbereich auf.

```html
<body>
  <nav id="app-nav">…</nav>
  <div id="toolbar">…</div>  <!-- Toolbar-Variante, siehe Abschnitt 27 -->

  <!-- Kein #page / .form-grid hier! -->
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

**Regeln:**
- `#chat-page` nimmt die gesamte verbleibende Viewport-Höhe ein (`calc(100vh - nav-h - toolbar-h)`).
- Auf großen Screens (≥ 1024 px): Sidebar ist fixiert sichtbar; kein Backdrop.
- Auf kleinen Screens (< 1024 px): Sidebar als Overlay mit Backdrop (`#sidebar-backdrop`).
- Der Sidebar-Zustand (auf-/eingeklappt) wird in `localStorage` gespeichert.

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

  <div class="chat-session-list">

    <!-- Aktive Sitzung (mit Projektbezug) -->
    <div class="chat-session-item active">
      <div class="session-info">
        <div class="session-title">Java-Entwickler Senior, München</div>
        <div class="session-project">📁 2026-042</div>
        <div class="session-meta">Heute, 14:32</div>
      </div>
      <button class="ibtn del" title="Chat löschen" onclick="deleteSession(1)">🗑</button>
    </div>

    <!-- Inaktive Sitzung (ohne Projektbezug) -->
    <div class="chat-session-item" onclick="loadSession(2)">
      <div class="session-info">
        <div class="session-title">DevOps-Engineer mit K8s-Erfahrung</div>
        <!-- kein .session-project, da project_id = NULL -->
        <div class="session-meta">Gestern</div>
      </div>
      <button class="ibtn del" title="Chat löschen" onclick="deleteSession(2)">🗑</button>
    </div>

  </div>
</aside>
```

**Regeln:**
- `.sidebar-toggle` ist immer sichtbar, auch wenn die Sidebar eingeklappt ist.
- `.session-title` wird mit `text-overflow: ellipsis` abgeschnitten — kein manuelles Bearbeiten.
- `.session-project` nur rendern, wenn `project_id ≠ NULL`; zeigt die Projektnummer mit 📁-Icon.
  Schrift: kleiner als `.session-meta`, Farbe `var(--text-3)`.
- Der Löschen-Button (`.ibtn.del`) ist im Ruhezustand unsichtbar (`opacity: 0`) und erscheint
  beim Hover der Zeile.
- Die aktive Sitzung erhält die Klasse `.active`.
- Sitzungen werden nach `changed_date` absteigend sortiert (jüngste zuerst).

---

## 26 Chat: Nachrichten

Der Nachrichtenbereich zeigt User- und Assistenten-Nachrichten als Bubbles.

```html
<div id="chat-messages">

  <!-- Leerer Zustand -->
  <div class="empty-chat">
    Stellen Sie Ihre erste Frage zur Profilsuche.
  </div>

  <!-- Nutzernachricht -->
  <div class="chat-msg user">
    <div class="msg-bubble">
      Ich suche einen Senior Java-Entwickler mit Spring-Erfahrung für München, ab April verfügbar.
    </div>
    <div class="msg-meta">14:32</div>
  </div>

  <!-- Assistenten-Antwort mit Freiberufler-Link -->
  <div class="chat-msg assistant">
    <div class="msg-bubble">
      <p>Ich habe folgende passende Profile gefunden:</p>
      <ul>
        <li><a href="#" onclick="openFreiberufler(42)">DEV-2024-07 – Max Müller</a>
            – Java/Spring, verfügbar ab 01.04., 95 €/h</li>
        <li><a href="#" onclick="openFreiberufler(87)">DEV-2023-15 – Anna Schmidt</a>
            – Java/Quarkus, verfügbar ab 15.04., 105 €/h</li>
      </ul>
    </div>
    <div class="msg-meta">14:32</div>
  </div>

  <!-- Ladeindikator (während Antwort generiert wird) -->
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
- User-Bubble: rechtsbündig, Primärfarbe (`--pri`) als Hintergrund, weißer Text.
- Assistenten-Bubble: linksbündig, `--surface` Hintergrund mit Border, Markdown gerendert.
- Freiberufler-Links in Assistenten-Antworten öffnen den Datensatz im Freiberufler-Formular.
  Format im gespeicherten `content`: `[freelancer:<id>:<anzeigetext>]` — wird beim Rendering
  in `<a href="#" onclick="openFreiberufler(<id>)"><anzeigetext></a>` umgewandelt.
- Ladeindikator (`.chat-typing`) wird eingeblendet, während die Antwort generiert wird,
  und nach Erhalt der Antwort ersetzt.
- Der Bereich scrollt automatisch zur letzten Nachricht (`scrollTop = scrollHeight`).

---

## 27 Chat: Eingabebereich & Toolbar-Variante

### Eingabebereich

```html
<div id="chat-input-area">
  <textarea id="chat-input" rows="1" placeholder="Nachricht eingeben… (Enter = Senden, Shift+Enter = Zeilenumbruch)"></textarea>
  <button id="chat-send" class="btn btn-pri" onclick="sendMessage()">Senden</button>
</div>
```

```javascript
// Textarea wächst automatisch mit dem Inhalt
const input = document.getElementById('chat-input');
input.addEventListener('input', () => {
  input.style.height = 'auto';
  input.style.height = Math.min(input.scrollHeight, 144) + 'px';
});
// Enter sendet, Shift+Enter fügt Zeilenumbruch ein
input.addEventListener('keydown', e => {
  if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendMessage(); }
});
```

**Regeln:**
- Textarea wächst automatisch bis zu 6 Zeilen (144 px), danach intern scrollbar.
- Senden-Button während aktiver Antwortgenerierung deaktiviert (`disabled`-Attribut).
- Eingabefeld nach dem Absenden leeren und Höhe zurücksetzen.

### Toolbar-Variante Profilsuche

Die Toolbar der Profilsuche hat **keine** Datensatz-Navigation und **keine** Audit-Information.

```html
<div id="toolbar">
  <!-- Modulbezeichnung -->
  <div id="tb-module">Profilsuche</div>

  <!-- Sidebar-Toggle (nur auf kleinen Screens sichtbar) -->
  <button class="sidebar-toggle-mobile btn btn-ghost btn-sm"
          onclick="openSidebar()" title="Chat-Verlauf">☰</button>

  <!-- Gemerktes Projekt (falls vorhanden) -->
  <div id="tb-project">
    📌 Gemerktes Projekt: <strong>2026-042</strong> – Java-Entwickler München
  </div>

  <!-- Aktionen -->
  <div id="tb-actions">
    <button class="btn btn-ghost" onclick="newChat()">＋ Neuer Chat</button>
    <button class="btn btn-danger" onclick="deleteCurrentChat()">🗑 Chat löschen</button>
  </div>
</div>
```

**Regeln:**
- Kein `#tb-nav` (keine Datensatz-Navigation).
- Kein `#tb-audit` (keine Audit-Information).
- `.sidebar-toggle-mobile` ist nur auf kleinen Screens (< 1024 px) sichtbar (CSS: `display: none` auf großen Screens).
- `#tb-project` wird nur gerendert, wenn ein gemerktes Projekt vorhanden ist.
- `btn-danger` für „Chat löschen" öffnet zunächst einen Bestätigungsdialog (`.mbk`).

---

## 28 JavaScript-Architektur und Custom Elements

### Verhältnis: Design-System-HTML ↔ Custom Elements

Die HTML-Muster in diesem Dokument beschreiben die **statische Grundstruktur**, die der Server per Thymeleaf rendert. Eine Teilmenge dieser Elemente wird durch **Custom Elements** (`ps-`-Präfix) mit interaktivem Verhalten erweitert — gemäß `specs/SWARCHITEKTUR.md` Abschnitt 5 (ADR-011).

**Prinzip Progressive Enhancement:** Custom Elements sind keine Ersetzung der Design-System-Muster, sondern deren JavaScript-Erweiterungsschicht. Der Server rendert die vollständige, Design-System-konforme HTML-Struktur innerhalb des Custom-Element-Tags. Ohne JS funktioniert die Seite als normales HTML. Mit JS upgradet `customElements.define()` das Element und fügt Verhalten hinzu — ohne die innere Struktur zu verändern.

### Mapping: Design-System-Muster → Custom Element

| Design-System-Muster     | Abschnitt | Custom Element          | Kapseltes Verhalten                          |
|--------------------------|-----------|-------------------------|----------------------------------------------|
| `.mbk`-Modal-Struktur    | §17       | `<ps-modal>`            | open/close, Focus-Trap, Escape-Handler, ARIA |
| `<table>`-Suchergebnisse | §16       | `<ps-infinite-scroll>`  | IntersectionObserver, AJAX-Nachladen         |
| `#chat-input`-Textarea   | §27       | `<ps-growing-textarea>` | Auto-Resize bis 6 Zeilen                     |
| `.badge-dyn`-Badge       | §22       | `<ps-status-badge>`     | Farbsetzung aus `data-bg`/`data-fg`          |
| `#tb-project`-Pill       | §21       | `<ps-project-pill>`     | Sichtbarkeit, Dismiss                        |
| `.chip`-Tag              | §14       | `<ps-tag-chip>`         | Entfernen mit Bestätigungsschritt            |

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

### Globale JavaScript-Initialisierung (`main.js`)

`main.js` ist der Einstiegspunkt des Frontend-Builds und enthält systemweite Initialisierungen, die in **jedem Template aktiv** sind — ohne explizites Einbinden in einzelnen Seiten:

| Initialisierung                               | Zweck                                                            | Referenz                    |
|-----------------------------------------------|------------------------------------------------------------------|-----------------------------|
| `customElements.define('ps-modal', ...)` etc. | Registriert alle Custom Elements                                 | SWARCHITEKTUR.md §5 ADR-011 |
| `apiFetch`-Wrapper (CSRF-Token)               | Setzt `X-XSRF-TOKEN`-Header bei allen state-mutierenden Requests | SWARCHITEKTUR.md §5         |
| `pageshow`-Event-Handler                      | Reload bei Wiederherstellung aus bfcache                         | SWARCHITEKTUR.md §5         |

**Regel für Template-Autoren:** Diese globalen Handler müssen **nicht** in einzelnen Templates implementiert werden — sie sind durch `main.js` systemweit aktiv. Templates nutzen `apiFetch` statt `fetch` für alle `POST`/`PUT`/`PATCH`/`DELETE`-Requests.

### POST-Navigation für Datensatz-Navigation

Die Navigations-Buttons (`#tb-nav`) werden gemäß `SWARCHITEKTUR.md` ADR-014 als `<form method="post">`-Elemente gerendert, **nicht** als vorberechnete `<a>`-Links. Der Design-System-Klassenname für Navigations-Buttons bleibt `tb-nav-btn`:

```html
<!-- Korrekt: POST-Formular mit tb-nav-btn -->
<div id="tb-nav">
  <form method="post" action="/freiberufler/navigate" style="display:inline">
    <input type="hidden" name="currentId" th:value="${freelancer.id}">
    <input type="hidden" name="direction" value="first">
    <button type="submit" class="tb-nav-btn" title="Erster">⏮</button>
  </form>
  <form method="post" action="/freiberufler/navigate" style="display:inline">
    <input type="hidden" name="currentId" th:value="${freelancer.id}">
    <input type="hidden" name="direction" value="prev">
    <button type="submit" class="tb-nav-btn" title="Vorheriger">◀</button>
  </form>
  <input type="text" id="tb-id-input" placeholder="ID">
  <form method="post" action="/freiberufler/navigate" style="display:inline">
    <input type="hidden" name="currentId" th:value="${freelancer.id}">
    <input type="hidden" name="direction" value="next">
    <button type="submit" class="tb-nav-btn" title="Nächster">▶</button>
  </form>
  <form method="post" action="/freiberufler/navigate" style="display:inline">
    <input type="hidden" name="currentId" th:value="${freelancer.id}">
    <input type="hidden" name="direction" value="last">
    <button type="submit" class="tb-nav-btn" title="Letzter">⏭</button>
  </form>
</div>
```

Begründung: Vorberechnete `<a>`-Links würden auf einen möglicherweise zwischenzeitlich gelöschten Datensatz verweisen. Der Server berechnet die Ziel-ID erst zum Klick-Zeitpunkt frisch aus der Datenbank. Details: `SWARCHITEKTUR.md` ADR-014.

---

*Letzte Änderung: März 2026*
