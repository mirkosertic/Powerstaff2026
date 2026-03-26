# Spezifikation Formular "Profilsuche"

Dieses Dokument beschreibt die fachliche und technische Spezifikation des Formulars "Profilsuche" und dient
als Grundlage für die KI-gestützte Implementierung. Es richtet sich an ein KI-System, das auf Basis dieser
Beschreibung den Programmcode eigenständig erzeugen soll.

> **Hinweis:** Die Profilsuche ist kein CRUD-Formular. Es werden keine Datensätze direkt editiert.
> Die technische Umsetzung des Chat-Backends (inkl. KI-Anbindung und MCP-Tooling-Interface) ist
> nicht Bestandteil dieser Spezifikation. Powerstaff stellt ausschließlich das UI und die
> Persistenzschicht für Chat-Verläufe bereit.

## Anwendungsfall

Die Profilsuche bietet zwei Such-Modi:

1. **Chat** (konversationell): Suchinterface im Stil von Claude / ChatGPT zur Suche nach geeigneten
   Freiberufler-Profilen. Der Sachbearbeiter formuliert seine Anforderungen in natürlicher Sprache;
   das System antwortet mit passenden Vorschlägen.

2. **Klassische Suche** (filterbasiert): Google-Style Suchinterface mit direkten Filterfeldern
   (Suchbegriff, Tagessatz, Tags) und sortierbarer Ergebnistabelle.

Beide Modi sind über eine Tab-Navigation zugänglich. Der Standardmodus beim Öffnen von `/profilesearch`
ist der Chat-Modus.

**Chat-Modus:**
- Ist ein gemerktes Projekt vorhanden, werden dessen Informationen in der Toolbar angezeigt und als
  Kontext automatisch in den Chat eingebunden.
- Aus den Antworten des Systems können Freiberufler-Datensätze direkt per Link geöffnet werden.
- Alle Chats werden server-seitig pro Sachbearbeiter persistiert. Beim Öffnen der Profilsuche wird
  stets der zuletzt aktive Chat angezeigt. Frühere Chats können ausgewählt und fortgesetzt werden.

**Klassische Suche:**
- Filterformular mit Suchkriterien (Suchbegriff, Tagessatz-Range, Tags)
- Sortierbare Ergebnistabelle mit Infinite Scrolling
- Kontaktsperre-Markierung (rote Zeilen)
- Klick auf Tag startet Freiberufler-QBE mit Tag-Filter

## URL-Struktur

| URL                                  | Beschreibung                                         |
|--------------------------------------|------------------------------------------------------|
| `/profilesearch`                     | Redirect zu `/profilesearch/chat` (Standardmodus)    |
| `/profilesearch/chat`                | Redirect zum neuesten Chat oder neuen Chat erstellen |
| `/profilesearch/chat/{chatId}`       | Chat-Ansicht (bestehend)                             |
| `/profilesearch/search`              | Klassische Suche (neu, filterbasiert)                |
| `/profilesearch/search?offset=N&...` | Infinite Scroll Fragment für klassische Suche        |

## Datenbankstruktur

Die Profilsuche verwendet zwei Tabellen für den Chat-Modus. Die klassische Suche ist **stateless**
und nutzt keine dedizierten Tabellen – alle Suchkriterien werden über URL-Parameter übergeben
(ADR-017, ADR-019).

### Chat-Sitzungen (`profile_search_chat`)

Jede Chat-Sitzung wird als eigenständiger Datensatz gespeichert.

> **Kein Optimistic Locking (`db_version`):** Chat-Sitzungen werden nicht gleichzeitig von
> mehreren Sachbearbeitern bearbeitet (jede Sitzung gehört genau einem `creation_user`).
> Auf `db_version` wird daher bewusst verzichtet.

| Feld           | Datenbankspalte | Datentyp   | Länge | Prüfungen                                      | Hinweise                                                                                                                                                                                  |
|----------------|-----------------|------------|-------|------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `id`           | `id`            | `BIGINT`   | —     | PK, NOT NULL, AUTO_INCREMENT                   |                                                                                                                                                                                           |
| `creationDate` | `creation_date` | `DATETIME` | —     | nullable                                       | Zeitpunkt der Anlage                                                                                                                                                                      |
| `creationUser` | `creation_user` | `VARCHAR`  | 255   | nullable                                       | Sachbearbeiter, dem die Sitzung gehört                                                                                                                                                    |
| `changedDate`  | `changed_date`  | `DATETIME` | —     | nullable                                       | Wird bei jeder Chat-Interaktion (Nutzernachricht oder Assistenten-Antwort) aktualisiert. Änderungen an verknüpften Projekt- oder Freiberuflerdaten haben keinen Einfluss auf diesen Wert. |
| `title`        | `title`         | `VARCHAR`  | 255   | nullable                                       | Wird automatisch aus der ersten Nutzernachricht generiert (erste ~60 Zeichen); kann leer bleiben                                                                                          |
| `project_id`   | `project_id`    | `BIGINT`   | —     | FK → project(id), nullable, ON DELETE SET NULL | Das zum Zeitpunkt der Chat-Erstellung gemerkte Projekt (nur zur Anzeige in der Sidebar; kein Cascade-Delete)                                                                              |

### Nachrichten (`profile_search_message`)

Jede Nachricht innerhalb einer Chat-Sitzung wird als eigener Datensatz gespeichert.

| Feld           | Datenbankspalte | Datentyp   | Länge     | Prüfungen                                                 | Hinweise                                                                |
|----------------|-----------------|------------|-----------|-----------------------------------------------------------|-------------------------------------------------------------------------|
| `id`           | `id`            | `BIGINT`   | —         | PK, NOT NULL, AUTO_INCREMENT                              |                                                                         |
| `creationDate` | `creation_date` | `DATETIME` | —         | nullable                                                  | Zeitpunkt der Nachricht                                                 |
| `chat_id`      | `chat_id`       | `BIGINT`   | —         | FK → profile_search_chat(id), NOT NULL, ON DELETE CASCADE | Zugehörige Sitzung                                                      |
| `role`         | `role`          | `VARCHAR`  | 20        | NOT NULL                                                  | `user` oder `assistant`                                                 |
| `sequence`     | `sequence`      | `INT`      | —         | NOT NULL                                                  | Reihenfolge der Nachricht innerhalb der Sitzung (aufsteigend)           |
| `content`      | `content`       | `LONGTEXT` | unlimited | NOT NULL                                                  | Nachrichtentext; bei `role=assistant` kann Markdown und Links enthalten |

## Layout und Navigation

### Tab-Navigation

Beide Modi (Chat und Suche) zeigen am oberen Rand der Seite eine Tab-Navigation:

```
┌──────────────────────────────────────────────┐
│  Toolbar: Profilsuche | Gemerktes Projekt    │
├──────────────────────────────────────────────┤
│  [Chat]  [Suche]  ← Tab-Navigation           │
├──────────────────────────────────────────────┤
│  ... Inhaltsbereich ...                      │
└──────────────────────────────────────────────┘
```

- **Aktiver Tab**: Primär-Button-Style (`.btn-pri`)
- **Inaktiver Tab**: Ghost-Button-Style (`.btn-ghost`)
- Klick wechselt zwischen Modi

### Form-Toolbar

Die Toolbar enthält unterschiedliche Aktionen je nach Modus:

**Im Chat-Modus:**
- **Gemerktes Projekt** (sofern vorhanden): Anzeige analog der anderen Formulare
  (z. B. „Gemerktes Projekt: 2026-042 – Java-Entwickler München"). Rein informativ, keine Aktion.
  Das gemerkte Projekt wird beim Öffnen eines neuen Chats als Kontext gesetzt.
- **„＋ Neuer Chat"**-Button: Legt eine neue leere Chat-Sitzung an und aktiviert diese.
- **„Löschen"**-Button: Löscht den aktuell angezeigten Chat (mit Bestätigungsdialog, siehe unten).

**Im Such-Modus:**
- **Gemerktes Projekt** (sofern vorhanden): Anzeige (informativ, kein Einfluss auf Suche)
- Keine weiteren Aktionen in der Toolbar

### Inhaltsbereich (Chat-Modus)

Der Inhaltsbereich im Chat-Modus ist horizontal zweigeteilt. Die Sidebar ist **responsive** und
verhält sich je nach Bildschirmbreite unterschiedlich:

- **Großer Bildschirm** (≥ 1024 px): Sidebar ist standardmäßig aufgeklappt und fixiert neben dem
  Chat sichtbar. Ein Toggle-Button (☰) am Sidebar-Rand ermöglicht das manuelle Einklappen.
- **Kleiner Bildschirm** (< 1024 px): Sidebar ist standardmäßig eingeklappt. Ein Toggle-Button
  in der Toolbar öffnet sie als Overlay über dem Chat-Bereich; ein Klick außerhalb schließt sie.

Der Zustand (auf-/eingeklappt) wird pro Sachbearbeiter browser-seitig gespeichert (localStorage).

**Aufgeklappt (großer Bildschirm):**

```
┌─────────────────┬──────────────────────────────────────────┐
│ ☰ Chat-Verlauf  │  Aktiver Chat                            │
│                 │                                          │
│  [Chat 1]       │  ┌────────────────────────────────────┐  │
│  14.03.2026     │  │ Nachrichtenbereich (scrollbar)     │  │
│  [Chat 2]       │  │                                    │  │
│  13.03.2026     │  │  [user]   Ich suche einen...       │  │
│  [Chat 3]       │  │  [asst.]  Hier sind passende...    │  │
│  12.03.2026     │  │                                    │  │
│  ...            │  └────────────────────────────────────┘  │
│                 │  ┌────────────────────────────────────┐  │
│                 │  │ [Textarea              ] [Senden]  │  │
│                 │  └────────────────────────────────────┘  │
└─────────────────┴──────────────────────────────────────────┘
```

**Eingeklappt:**

```
┌───┬──────────────────────────────────────────────────────┐
│ ☰ │  Aktiver Chat                                        │
│   │  ┌──────────────────────────────────────────────┐    │
│   │  │ Nachrichtenbereich (scrollbar)               │    │
│   │  └──────────────────────────────────────────────┘    │
│   │  ┌──────────────────────────────────────────────┐    │
│   │  │ [Textarea                        ] [Senden]  │    │
│   │  └──────────────────────────────────────────────┘    │
└───┴──────────────────────────────────────────────────────┘
```

## Chat-Verlauf (Sidebar)

Die Sidebar listet alle Chat-Sitzungen des Sachbearbeiters, sortiert nach `changed_date` absteigend —
die zuletzt genutzte Sitzung erscheint ganz oben und ist damit sofort erreichbar.

Jeder Eintrag zeigt:

- **Titel** der Sitzung (aus der ersten Nutzernachricht generiert, erste ~60 Zeichen; falls noch
  leer: „Neuer Chat"). Der Titel ist nicht manuell bearbeitbar.
- **Projektbezug**: Ist der Sitzung ein Projekt zugeordnet (`project_id ≠ NULL`), wird die
  Projektnummer (`project_number`) als zusätzliche Zeile unter dem Titel angezeigt (z. B. „📁 2026-042").
  Sitzungen ohne Projektbezug zeigen an dieser Stelle nichts.
- **Zeitpunkt der letzten Nutzung** (`changed_date`), angezeigt als relatives oder absolutes Datum
  (z. B. „Heute, 14:32", „Gestern", „12.03.2026")

Die aktive Sitzung ist visuell hervorgehoben. Ein Klick auf einen Eintrag wechselt zu dieser Sitzung; der bestehende Chat-Verlauf wird vollständig geladen und kann fortgesetzt werden. Das gemerkte Projekt bleibt dabei unverändert.

Jeder Eintrag enthält einen Löschen-Button (Papierkorb-Icon), über den die jeweilige Sitzung
gelöscht werden kann (mit Bestätigungsdialog).

Die Sidebar lädt alle vorhandenen Sitzungen des Sachbearbeiters. Bei langen Listen wird beim
Scrollen ans Ende automatisch nachgeladen (Infinite Scrolling). Es gibt kein Limit für die
Anzahl gespeicherter Sitzungen.

## Aktiver Chat

### Nachrichtenbereich

Der Nachrichtenbereich zeigt alle Nachrichten der aktiven Sitzung in chronologischer Reihenfolge
(`sequence ASC`). Jede Nachricht wird entsprechend ihrer Rolle dargestellt:

| Rolle       | Darstellung                                                                                |
|-------------|--------------------------------------------------------------------------------------------|
| `user`      | Rechtsbündig, farblich abgesetzt (eigene Nachrichten)                                      |
| `assistant` | Linksbündig, Markdown gerendert; enthält ggf. anklickbare Freiberufler-Links (siehe unten) |

Der Nachrichtenbereich scrollt automatisch zur letzten Nachricht, wenn eine neue Antwort eintrifft.

**Leerer Chat:** Ist die Sitzung noch leer (kein Nachrichtenverlauf), wird ein Hinweistext
angezeigt, z. B. „Stellen Sie Ihre erste Frage zur Profilsuche."

**Ladezustand:** Während das System eine Antwort generiert, wird ein Ladindikator (z. B.
animierte Punkte) im Nachrichtenbereich angezeigt.

### Freiberufler-Links

Das System kann in seinen Antworten Links zu Freiberufler-Datensätzen einbetten. Diese werden
im gerenderten Markdown als anklickbarer Link dargestellt (z. B. „[FB-2024-007 – Max Muster](#)").
Ein Klick öffnet den entsprechenden Freiberufler-Datensatz im Freiberufler-Formular **in einem
neuen Browser-Tab**. Der Chat-Verlauf bleibt dadurch im ursprünglichen Tab erhalten, und der
Sachbearbeiter kann nach dem Prüfen des Profils direkt zum Chat zurückkehren — z. B. um den
Freiberufler über das gemerkte Projekt zuzuordnen.

Das Link-Format im Nachrichteninhalt (`content`) folgt einer definierten Konvention, die das
Frontend bei der Darstellung erkennt und in klickbare Navigation umwandelt:

```
[freelancer:<id>:<anzeigetext>]
```

Beispiel: `[freelancer:42:FB-2024-007 – Max Muster]` wird gerendert als anklickbarer Link,
der den Freiberufler mit ID 42 im Freiberufler-Formular öffnet.

### Eingabebereich

Der Eingabebereich befindet sich am unteren Rand des aktiven Chats und enthält:

- **Textarea**: Mehrzeiliges Eingabefeld für die Nutzernachricht. Wächst automatisch mit dem
  Inhalt (max. ca. 6 Zeilen, danach scrollbar). Absenden mit Enter (Zeilenumbruch mit Shift+Enter).
- **Senden-Button**: Schickt die Nachricht ab. Ist während der Verarbeitung einer Antwort deaktiviert.

## Aktionen

### Neuer Chat

Ein Klick auf „＋ Neuer Chat" in der Toolbar:

1. Legt einen neuen `profile_search_chat`-Datensatz an (`creation_user` = aktueller Sachbearbeiter,
   `project_id` = aktuell gemerktes Projekt, falls vorhanden).
2. Aktiviert die neue leere Sitzung im Inhaltsbereich.
3. Fügt die Sitzung an erster Stelle der Sidebar-Liste ein.

### Chat löschen

Das Löschen einer Sitzung (über Toolbar-Button oder Sidebar-Löschen-Icon) zeigt einen modalen
Bestätigungsdialog mit dem Text:

> „Der Chat-Verlauf wird unwiderruflich gelöscht. Sind Sie sicher?"

Der Dialog bietet „Ja, löschen" und „Abbrechen". Nach Bestätigung wird die Sitzung inkl. aller
zugehörigen Nachrichten gelöscht (`ON DELETE CASCADE` auf `profile_search_message.chat_id`).

- Wird der **aktuell aktive Chat** gelöscht, wird anschließend die nächste verfügbare Sitzung
  geladen. Gibt es keine weitere, wird automatisch eine neue leere Sitzung angelegt.
- Wird ein **inaktiver Chat** aus der Sidebar gelöscht, bleibt der aktive Chat unverändert.

## Projektkontext

### Anzeigebezug

`profile_search_chat.project_id` speichert das zum Zeitpunkt der Chat-Anlage gemerkte Projekt – ausschließlich zur Anzeige der Projektnummer in der Sidebar. Die Spalte ist nach der Anlage nicht mehr änderbar.

`project_id` ist **kein strukturelles Bindungsmerkmal**: Der Chat gehört nicht einem Projekt, sondern einem Sachbearbeiter. Wird das referenzierte Projekt gelöscht, wird `project_id` auf `NULL` gesetzt (`ON DELETE SET NULL`); der Chat-Verlauf bleibt vollständig erhalten.

### Dynamischer LLM-Kontext — wird bei jeder Anfrage aktuell abgerufen

Der LLM-Kontext basiert auf dem **aktuell gemerkten Projekt des Sachbearbeiters** (aus `remembered_project`), nicht auf `profile_search_chat.project_id`. Er wird bei jeder gesendeten Nutzernachricht frisch aus der Datenbank gelesen – unabhängig davon, mit welchem Projekt der Chat ursprünglich angelegt wurde.

Dadurch spiegelt der Kontext immer den aktuellen Stand wider, auch wenn sich Projektdaten oder Freiberufler-Zuordnungen zwischenzeitlich geändert haben.

Der Kontext umfasst, sofern ein Projekt gemerkt ist:

**Projektdaten** (aus `project`):
- Projektnummer (`project_number`)
- Kurzbeschreibung (`description_short`) und ausführliche Beschreibung (`description_long`)
- Einsatzort (`workplace`)
- Anforderungen (`skills`)
- Laufzeit (`duration`) und Startdatum (`start_date`)
- Status (`status`, als Klartextbezeichnung)
- Verkaufs-Stundensatz (`stundensatz_vk`)

**Zugewiesene Freiberufler** (aus `project_position` JOIN `freelancer` JOIN `project_position_status`
JOIN `freelancer_tags` JOIN `tags`):

Für jeden dem Projekt zugeordneten Freiberufler werden folgende Informationen übergeben:
- Kodierung (`freelancer.code`)
- Name und Vorname (`freelancer.name1`, `freelancer.name2`)
- Skills (`freelancer.skills`)
- Tags (`tags.tagname` gruppiert nach `tags.type`, alle zugeordneten Tags des Freiberuflers)
- Aktueller Positionsstatus (`project_position_status.description`)
- Konditionen der Zuordnung (`project_position.konditionen`)
- Kommentar zur Zuordnung (`project_position.kommentar`)

### Kein Projekt gemerkt

Ist kein Projekt gemerkt (`remembered_project` hat keinen Eintrag für den Sachbearbeiter), wird kein Projektkontext übergeben. Das Chat-Backend arbeitet dann ohne Projektbezug.

---

## Klassische Suche (filterbasiert)

**Version 1.1 (März 2026)**

Zusätzlich zum Chat-Modus bietet die Profilsuche eine klassische filterbasierte Suche.

### Aufbau

Der klassische Such-Modus besteht aus zwei Bereichen:

1. **Suchformular** (oben): Filterkarte mit Suchkriterien
2. **Suchergebnisse** (unten): Sortierbare Tabelle mit Infinite Scrolling

### Suchformular

Das Formular zeigt folgende Felder:

| Feld               | Typ                  | Validierung | Hinweise                                                                      |
|--------------------|----------------------|-------------|-------------------------------------------------------------------------------|
| `searchTerm`       | Text                 | Optional    | Durchsucht `freelancer.name1`, `freelancer.name2`, `freelancer.skills` (LIKE) |
| `salaryPerDayFrom` | Number               | Optional    | Minimum-Tagessatz in €                                                        |
| `salaryPerDayTo`   | Number               | Optional    | Maximum-Tagessatz in €                                                        |
| `tagIds`           | Multi-Select (Chips) | Optional    | Tags mit `TagType.TYP`, comma-separated IDs                                   |

**Validierung**: Mindestens eines der vier Felder muss ausgefüllt sein.

**Formular-Aktionen**:
- **Suchen**: Führt Suche aus, zeigt Ergebnisse
- **Zurücksetzen**: Lädt leeres Formular ohne Suche auszuführen

### Suchergebnisse

Die Ergebnisse werden in einer sortierbaren Tabelle angezeigt:

| Spalte          | Quelle                            | Sortierbar | Hinweise                            |
|-----------------|-----------------------------------|------------|-------------------------------------|
| Name 1          | `freelancer.name1`                | Ja         |                                     |
| Name 2          | `freelancer.name2`                | Ja         |                                     |
| Letzter Kontakt | `freelancer.last_contact_date`    | Ja         | Format: `dd.MM.yyyy`                |
| Tagessatz       | `freelancer.salary_per_day_long`  | Ja         | Format: `XXX €`                     |
| Verfügbar ab    | `freelancer.availability_as_date` | Ja         | Format: `dd.MM.yyyy`                |
| Code            | `freelancer.code`                 | Ja         |                                     |
| Tags            | `tags` (JOIN)                     | Nein       | Kleine Chips (`.chip-xs`), klickbar |

**Sortierung**:
- Default: `name1 ASC, name2 ASC`
- Klick auf Spaltenheader wechselt Sortierung
- URL-Parameter: `sortField`, `sortDir` (asc/desc)
- Visueller Indikator: `.srt-asc` / `.srt-desc` CSS-Klasse

**Kontaktsperre-Markierung**:
- Freiberufler mit `contactforbidden=true` haben roten Hintergrund (`background: var(--danger-l)`)
- Text in rot (`color: var(--danger)`)
- Gilt für Profilsuche UND Freiberufler-QBE-Suche

**Infinite Scrolling**:
- Initial: 20 Ergebnisse (PAGE_SIZE)
- Bei Scroll ans Ende: Automatisches Nachladen
- Custom Element: `<ps-infinite-scroll>`

### Ergebnis-Interaktionen

1. **Klick auf Tabellenzeile**:
   - Öffnet Freiberufler-Detail: `/freelancer/{id}?returnTo=profilesearch-search`
   - Zurück-Button im Freiberufler-Formular führt zurück zur Suche

2. **Klick auf Tag**:
   - Startet Freiberufler-QBE mit Tag-Filter: `/freelancer/search?tagId={id}&returnTo=profilesearch-search`
   - Zeigt alle Freiberufler mit diesem Tag
   - Zurück-Button führt zurück zur Profilsuche-Suche

### Technische Implementierung

#### Backend-Komponenten

**ProfileSearchCriteria** (neu):
```java
public record ProfileSearchCriteria(
    String searchTerm,
    Long salaryPerDayFrom,
    Long salaryPerDayTo,
    String tagIds,  // comma-separated
    String sortField,
    String sortDir
)
```

**ProfileSearchResult** (neu):
```java
public record ProfileSearchResult(
    Long id,
    String code,
    String name1,
    String name2,
    LocalDateTime lastContactDate,
    Long salaryPerDayLong,
    LocalDateTime availabilityAsDate,
    boolean contactForbidden,
    List<TagView> tags
)
```

**ProfileSearchQueryService** (erweitert):
- `searchFreelancers(criteria, offset, limit)` → `List<ProfileSearchResult>`
- `countSearchFreelancers(criteria)` → `long`

**ProfileSearchController** (erweitert):
- `GET /profilesearch` → Redirect zu `/profilesearch/chat`
- `GET /profilesearch/chat` → Redirect zu neuester Chat-Sitzung
- `GET /profilesearch/search` → Klassische Suche (neu)
  - Offset = 0: Vollständige Seite mit Formular und Ergebnissen
  - Offset > 0: Fragment (nur `<tr>` Elemente) für Infinite Scroll
  - Header: `X-Next-Url` wenn mehr Ergebnisse verfügbar

#### Frontend-Templates

**search-page.html** (neu):
- Vollständige Suchseite mit Formular und Ergebnistabelle
- Tab-Navigation zu Chat
- JavaScript für Tag-Auswahl (`.chip.selected` Toggle)

**search-results.html** (neu):
- Fragment mit `<tr>` Elementen für Infinite Scroll

**form.html** (erweitert):
- Tab-Navigation zu Suche

#### CSS-Erweiterungen

**components2.css**:
```css
.chip.selected {
  background: var(--pri);
  color: white;
  border-color: var(--pri-d);
}
```

### Integration mit Freiberufler-Modul

Die klassische Suche integriert sich mit dem Freiberufler-Modul:

**FreelancerSearchCriteria** (erweitert):
- Neuer Parameter: `Long tagId`

**FreelancerQueryService** (erweitert):
- Filter: `WHERE EXISTS (SELECT 1 FROM freelancer_tags WHERE freelancer_id = f.id AND tag_id = :tagId)`

**FreelancerSearchResult** (erweitert):
- Neues Feld: `Boolean contactForbidden`

**FreelancerController** (erweitert):
- `show(id, returnTo)`: Zurück-Button zeigt "Zurück zur Profilsuche" wenn `returnTo=profilesearch-search`
- `search(criteria, returnTo)`: Zurück-Button führt zur Profilsuche wenn `returnTo=profilesearch-search`

**Freiberufler-Templates** (erweitert):
- Kontaktsperre-Markierung in Suchergebnissen (rote Zeilen)
- Conditional Zurück-Button basierend auf `returnTo` Parameter

### Cache-Control und Bookmarking

Gemäß ADR-019:
- Initial: `Cache-Control: no-store, no-cache, must-revalidate`
- Infinite Scroll: Kein Cache-Control (Browser entscheidet)
- URLs sind bookmarkable und shareable

### Gemerktes Projekt

Das gemerkte Projekt wird in der Toolbar angezeigt (wie im Chat-Modus), hat aber **keinen Einfluss
auf die Suchergebnisse**. Die klassische Suche durchsucht **alle Freiberufler**, unabhängig vom
gemerkten Projekt.

**Rationale**: Die Chat-Suche nutzt das gemerkte Projekt als Kontext für die KI. Die klassische
Suche ist ein allgemeines Tool ohne Projektbezug.

### Implementierungsphasen

**Phase 1 (aktuell)**: Mock-Service
- Liefert feste Mock-Daten (50 Einträge)
- Grundlegende Filterung simuliert (Name, Tagessatz)
- Für UI-Testing und Akzeptanztests ausreichend

**Phase 2 (später)**: Echte SQL-Implementierung
- LIKE-Suche auf `name1`, `name2`, `skills`
- Numerische Filter auf `salary_per_day_long`
- JOIN auf `freelancer_tags` für Tag-Filter
- Batch-Loading der Tags für Ergebnisse

### Zukünftige Erweiterungen

- **Erweiterte Filter**: Verfügbarkeit, Stadt, Partner-Zuordnung
- **Gespeicherte Suchen**: User kann häufige Suchen speichern
- **Export**: Suchergebnisse als CSV/PDF exportieren
- **Multi-Tag-Filter**: UND/ODER-Verknüpfung von Tags

---

## Offene Punkte

### Offene Fragen


