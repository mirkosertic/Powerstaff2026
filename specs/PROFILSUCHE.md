# Spezifikation Formular "Profilsuche"

Dieses Dokument beschreibt die fachliche und technische Spezifikation des Formulars "Profilsuche" und dient
als Grundlage für die KI-gestützte Implementierung. Es richtet sich an ein KI-System, das auf Basis dieser
Beschreibung den Programmcode eigenständig erzeugen soll.

> **Hinweis:** Die Profilsuche ist kein CRUD-Formular. Es werden keine Datensätze direkt editiert.
> Die technische Umsetzung des Chat-Backends (inkl. KI-Anbindung und MCP-Tooling-Interface) ist
> nicht Bestandteil dieser Spezifikation. Powerstaff stellt ausschließlich das UI und die
> Persistenzschicht für Chat-Verläufe bereit.

## Anwendungsfall

Die Profilsuche bietet ein konversationelles Suchinterface (im Stil von Claude / ChatGPT) zur Suche
nach geeigneten Freiberufler-Profilen. Der Sachbearbeiter formuliert seine Anforderungen in natürlicher
Sprache; das System antwortet mit passenden Vorschlägen.

Ist ein gemerktes Projekt vorhanden, werden dessen Informationen in der Toolbar angezeigt und als
Kontext automatisch in den Chat eingebunden.

Aus den Antworten des Systems können Freiberufler-Datensätze direkt per Link geöffnet werden.

Alle Chats werden server-seitig pro Sachbearbeiter persistiert. Beim Öffnen der Profilsuche wird
stets der zuletzt aktive Chat angezeigt. Frühere Chats können ausgewählt und fortgesetzt werden.

## Datenbankstruktur

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

Das Formular folgt dem Standard-Shell-Layout (App-Nav → Form-Toolbar → scrollbarer Inhaltsbereich),
weicht jedoch in der Inhaltsstruktur von den CRUD-Formularen ab: Anstelle von Formular-Karten
besteht der Inhaltsbereich aus einem zweigeteilten Chat-Layout.

### Form-Toolbar

Die Toolbar enthält:

- **Gemerktes Projekt** (sofern vorhanden): Anzeige analog der anderen Formulare
  (z. B. „Gemerktes Projekt: 2026-042 – Java-Entwickler München"). Rein informativ, keine Aktion.
  Das gemerkte Projekt wird beim Öffnen eines neuen Chats als Kontext gesetzt.
- **„＋ Neuer Chat"**-Button: Legt eine neue leere Chat-Sitzung an und aktiviert diese.
- **„Löschen"**-Button: Löscht den aktuell angezeigten Chat (mit Bestätigungsdialog, siehe unten).

### Inhaltsbereich

Der Inhaltsbereich ist horizontal zweigeteilt. Die Sidebar ist **responsive** und verhält sich
je nach Bildschirmbreite unterschiedlich:

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

## Offene Punkte

### Offene Fragen


