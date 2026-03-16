# Spezifikation Formular "Projekte"

Dieses Dokument beschreibt die fachliche und technische Spezifikation des Formulars "Projekte" und dient
als Grundlage für die KI-gestützte Implementierung. Es richtet sich an ein KI-System, das auf Basis dieser
Beschreibung den Programmcode eigenständig erzeugen soll. Die Spezifikation ist daher so präzise wie möglich
gehalten und enthält neben der Beschreibung der Geschäftslogik auch Datenbankstrukturen, Validierungsregeln
und Verhaltensanforderungen an die Benutzeroberfläche.

## Anwendungsfall

Dieses Formular dient der Erfassung und Verwaltung von Projekten. Ein Projekt beschreibt eine konkrete
Anfrage oder einen laufenden Auftrag. Es kann einem Kunden oder einem Partner zugeordnet sein und enthält
alle relevanten Eckdaten wie Einsatzort, Laufzeit, Anforderungen und Konditionen.

## Stammdaten

Projekte werden in der Tabelle `project` gespeichert. Die Felder sind nach fachlicher Zugehörigkeit
gruppiert – diese Gruppen spiegeln auch die Struktur des Formulars wider.

**Systemfelder** (werden im Formular nicht angezeigt):

| Feld                     | Datenbankspalte  | Datentyp       | Prüfungen                      | Hinweise                         |
|--------------------------|------------------|----------------|--------------------------------|----------------------------------|
| `id`                     | `id`             | `BIGINT`       | PK, NOT NULL, AUTO_INCREMENT   | —                                |
| `version`                | `db_version`     | `BIGINT`       | NOT NULL, default `0`          | Optimistic Locking               |
| `creationDate`           | `creation_date`  | `DATETIME`     | nullable                       | Zeitpunkt der Erfassung          |
| `creationUserID`         | `creation_user`  | `VARCHAR(255)` | nullable                       | Erfassender Sachbearbeiter       |
| `lastModificationDate`   | `changed_date`   | `DATETIME`     | nullable                       | Zeitpunkt der letzten Änderung   |
| `lastModificationUserID` | `changed_user`   | `VARCHAR(255)` | nullable                       | Zuletzt ändernder Sachbearbeiter |

**Gruppe: Allgemein**

| Feld               | Datenbankspalte    | Datentyp   | Länge | Prüfungen                                               | Label für die UI | Hinweise                                                                            |
|--------------------|--------------------|-----------:|-------|---------------------------------------------------------|------------------|-------------------------------------------------------------------------------------|
| `projectNumber`    | `project_number`   | `VARCHAR`  | 255   | nullable                                                | Projektnummer    | Fachliche Projektnummer; frei vergebbar                                             |
| `entryDate`        | `entry_date`       | `DATETIME` | —     | nullable                                                | Eingangsdatum    | Fachliches Datum des Projekteingangs; unabhängig vom technischen `creation_date`    |
| `startDate`        | `start_date`       | `DATETIME` | —     | nullable                                                | Startdatum       | Geplanter oder vereinbarter Projektbeginn (dd.MM.yyyy)                              |
| `duration`         | `duration`         | `VARCHAR`  | 255   | nullable                                                | Laufzeit         | Freitext; erlaubt flexible Angaben wie „3 Monate", „bis 31.12.2026", „unbefristet"  |
| `status`           | `status`           | `INT`      | —     | NOT NULL, default `1`, CHECK (`status` BETWEEN 1 AND 5) | Status           | Projektstatus; Werteliste siehe [Projektstatus](#projektstatus)                     |
| `visibleOnWebSite` | `visible_on_web_site` | `BIT`   | 1     | NOT NULL, default `false`                               | Auf Website      | Steuert, ob das Projekt auf der öffentlichen Website veröffentlicht wird            |

**Gruppe: Beschreibung**

| Feld               | Datenbankspalte     | Datentyp   | Länge     | Prüfungen | Label für die UI | Hinweise                                                        |
|--------------------|---------------------|------------|-----------|-----------|------------------|-----------------------------------------------------------------|
| `descriptionShort` | `description_short` | `VARCHAR`  | 255       | nullable  | Kurzbeschreibung | Einzeiliger Titeltext; wird in Listen und Übersichten angezeigt |
| `descriptionLong`  | `description_long`  | `LONGTEXT` | unlimited | nullable  | Beschreibung     | Ausführliche Projektbeschreibung; mehrzeiliger Freitext         |
| `skills`           | `skills`            | `LONGTEXT` | unlimited | nullable  | Anforderungen    | Gesuchte Fähigkeiten und Qualifikationen; mehrzeiliger Freitext |

**Gruppe: Einsatz**

| Feld        | Datenbankspalte | Datentyp  | Länge | Prüfungen | Label für die UI | Hinweise                                                       |
|-------------|-----------------|-----------|-------|-----------|------------------|----------------------------------------------------------------|
| `workplace` | `workplace`     | `VARCHAR` | 255   | nullable  | Einsatzort       | Ort des Projekteinsatzes (z. B. „München", „Remote", „hybrid") |

**Gruppe: Zuordnung**

Ein Projekt wird entweder einem Kunden oder einem Partner zugeordnet – niemals beiden gleichzeitig.
Eine gleichzeitige Belegung beider Felder ist systemseitig ausgeschlossen (siehe Validierungsregeln unten).
Beide Felder sind optional; es ist möglich, dass weder Kunde noch Partner zugeordnet sind.

| Feld          | Datenbankspalte | Datentyp | Prüfungen                  | Label für die UI | Hinweise                                                                                                                                                          |
|---------------|-----------------|----------|----------------------------|------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `customer_id` | `customer_id`   | `BIGINT` | FK → kunde(id), nullable   | Kunde            | Optionale Zuordnung zu einem Kunden. Wird als anklickbarer Link (Firmenname) dargestellt. Bei Anlage über das Kunden-Formular vorausgefüllt und nicht änderbar.   |
| `partner_id`  | `partner_id`    | `BIGINT` | FK → partner(id), nullable | Partner          | Optionale Zuordnung zu einem Partner. Wird als anklickbarer Link (Firmenname) dargestellt. Bei Anlage über das Partner-Formular vorausgefüllt und nicht änderbar. |

> **Validierungsregeln Zuordnung:**
> - `customer_id` und `partner_id` dürfen niemals gleichzeitig gesetzt sein. Das System verhindert dies auf zwei Ebenen:
>   - **UI**: Sobald ein Wert in einem der beiden Felder gesetzt ist, wird das jeweils andere Feld ausgeblendet bzw. deaktiviert.
>   - **Backend**: Beim Speichern wird geprüft, dass nicht beide Felder gleichzeitig einen Wert enthalten. Ein Verstoß führt zu einem Validierungsfehler.
> - Die Links öffnen den jeweiligen Datensatz im entsprechenden Formular.
> - Die Felder `customer_id` und `partner_id` sind nach der Anlage nicht mehr editierbar, um die Zuordnung konsistent zu halten.

**Gruppe: Konditionen**

| Feld            | Datenbankspalte  | Datentyp  | Länge | Prüfungen | Label für die UI | Hinweise                            |
|-----------------|------------------|-----------|-------|-----------|------------------|-------------------------------------|
| `stundensatzVK` | `stundensatz_vk` | `BIGINT`  | —     | nullable  | Stundensatz VK   | Verkaufs-Stundensatz in ganzen Euro |
| `debitorNr`     | `debitor_nr`     | `VARCHAR` | 255   | nullable  | Debitor          | Debitorennummer (Buchhaltung)       |
| `kreditorNr`    | `kreditor_nr`    | `VARCHAR` | 255   | nullable  | Kreditor         | Kreditorennummer (Buchhaltung)      |

### Projektstatus

Der Status eines Projekts wird als ganzzahliger Wert gespeichert und im Formular als Combobox angeboten.
Die folgende Liste ist abschließend:

| Wert | Bezeichnung | Bedeutung                                                   |
|------|-------------|-------------------------------------------------------------|
| `1`  | Offen       | Projekt ist eingegangen, Suche noch nicht gestartet         |
| `2`  | Verloren    | Projekt wurde an einen Wettbewerber verloren                |
| `3`  | Canceled    | Projekt wurde vom Auftraggeber storniert                    |
| `4`  | Besetzt     | Projekt ist mit einem Freiberufler besetzt                  |
| `5`  | Search zu   | Suche für dieses Projekt ist abgeschlossen / pausiert       |

Die Werte sind mit den Anwendern abgestimmt und abschließend. In der Datenbank wird ausschließlich der Integer-Wert gespeichert; das Label wird in der UI aus dieser festen Zuordnung ermittelt (kein DB-Lookup). Neue Projekte erhalten automatisch den Standardwert `1` (Offen).

## Zugeordnete Freiberufler (Projektpositionen)

Das Projekt-Formular zeigt innerhalb einer eigenen Formular-Karte (volle Breite, `col-wide`) alle
Freiberufler, die diesem Projekt zugeordnet sind. Die Zuordnung selbst erfolgt ausschließlich über das
Freiberufler-Formular (siehe [FREIBERUFLER.md](FREIBERUFLER.md) – Gemerktes Projekt). Im Projekt-Formular
können die Merkmale einer Zuordnung bearbeitet und Zuordnungen gelöscht werden.

### Listendarstellung

Tabellarische Listenansicht. Jede Zeile enthält:

| Spalte      | Feld               | Sortierbar | Hinweis                                                                        |
|-------------|--------------------|------------|--------------------------------------------------------------------------------|
| Kodierung   | `freelancer.code`  | ja         | Anklickbar → öffnet den Freiberufler                                           |
| Name        | `freelancer.name1` | ja         |                                                                                |
| Vorname     | `freelancer.name2` | ja         |                                                                                |
| Status      | `status_id`        | nein       | Farbiges Badge mit dem Statusnamen (Farbe aus `project_position_status.color`) |
| Konditionen | `konditionen`      | nein       |                                                                                |
| Kommentar   | `kommentar`        | nein       |                                                                                |
| Aktionen    | —                  | nein       | Bearbeiten- und Löschen-Button je Zeile                                        |

Die Liste ist standardmäßig nach Name (`freelancer.name1`) und Vorname (`freelancer.name2`) aufsteigend sortiert.

**Leere Liste:** Ist noch kein Freiberufler zugeordnet, wird ein entsprechender Hinweistext angezeigt
(„Keine Freiberufler zugeordnet").

### Aktion: Merkmale bearbeiten

Jede Zeile enthält einen Bearbeiten-Button. Er öffnet ein modales Formular mit den folgenden Feldern:

| Feld          | Label       | Pflichtfeld | Hinweis                                                |
|---------------|-------------|-------------|--------------------------------------------------------|
| `status_id`   | Status      | ja          | Combobox; Werte aus `project_position_status`          |
| `konditionen` | Konditionen | nein        | Freitext (z. B. verhandelter Stundensatz, Laufzeit)    |
| `kommentar`   | Kommentar   | nein        | Mehrzeiliger Freitext                                  |

Der Dialog bietet „Speichern" und „Abbrechen". Nach dem Speichern aktualisiert sich die Liste automatisch.

**Gleichzeitige Bearbeitung (Optimistic Locking):** Das Modal überträgt beim Speichern den `db_version`-Wert
der Position. Wurde die Position zwischenzeitlich von einem anderen Sachbearbeiter geändert, schlägt das
Speichern fehl und es erscheint ein modaler Konfliktdialog:

> „Die Zuordnung von [Kodierung / Name] wurde zwischenzeitlich von [Sachbearbeiter] geändert."

Der Dialog bietet:
- **Neu laden**: Die eigenen Änderungen werden verworfen; das Modal lädt den aktuellen Stand der Position neu,
  sodass der Sachbearbeiter seine Änderungen auf Basis des aktuellen Stands wiederholen kann.
- **Abbrechen**: Das Modal wird geschlossen; keine Änderung wird gespeichert.

### Aktion: Zuordnung löschen

Jede Zeile enthält einen Löschen-Button. Vor der Ausführung wird ein modaler Bestätigungsdialog angezeigt:

> „Die Zuordnung des Freiberuflers [Kodierung / Name] zu diesem Projekt wird aufgehoben. Sind Sie sicher?"

Der Dialog bietet „Ja, aufheben" und „Abbrechen". Nach Bestätigung wird die Projektposition gelöscht.
Der Freiberufler selbst wird nicht gelöscht. Die Liste aktualisiert sich anschließend automatisch.

### Datenbankstruktur (`project_position`)

Die Zuordnung zwischen Freiberuflern und Projekten wird in der Tabelle `project_position` gespeichert.
Ein Freiberufler kann einem Projekt nur einmal zugeordnet sein (UNIQUE auf `project_id` + `freelancer_id`).

> **Optimistic Locking:** Positionsänderungen erfolgen über eigene Modals und werden unabhängig
> vom Projekt-Stammdatensatz gespeichert — das `db_version` des Projekts schützt daher **nicht**
> vor gleichzeitigen Positionskonflikten. Konflikte auf Positionsebene werden über das `db_version`-Feld
> der Position selbst erkannt und dem Sachbearbeiter mit einem Konfliktdialog gemeldet (siehe
> „Aktion: Merkmale bearbeiten" oben).

| Feld                     | Datenbankspalte | Datentyp   | Länge     | Prüfungen                                         | Hinweise                                                                                            |
|--------------------------|-----------------|------------|-----------|---------------------------------------------------|-----------------------------------------------------------------------------------------------------|
| `id`                     | `id`            | `BIGINT`   | —         | PK, NOT NULL, AUTO_INCREMENT                      |                                                                                                     |
| `version`                | `db_version`    | `BIGINT`   | —         | NOT NULL, default `0`                             | Optimistic Locking; siehe Hinweis oben                                                              |
| `creationDate`           | `creation_date` | `DATETIME` | —         | nullable                                          | Zeitpunkt der Erfassung                                                                             |
| `creationUserID`         | `creation_user` | `VARCHAR`  | 255       | nullable                                          | Erfassender Sachbearbeiter                                                                          |
| `lastModificationDate`   | `changed_date`  | `DATETIME` | —         | nullable                                          | Zeitpunkt der letzten Änderung                                                                      |
| `lastModificationUserID` | `changed_user`  | `VARCHAR`  | 255       | nullable                                          | Zuletzt ändernder Sachbearbeiter                                                                    |
| `project_id`             | `project_id`    | `BIGINT`   | —         | FK → project(id), NOT NULL, ON DELETE CASCADE     | Zugehöriges Projekt                                                                                 |
| `freelancer_id`          | `freelancer_id` | `BIGINT`   | —         | FK → freelancer(id), NOT NULL, ON DELETE RESTRICT | Zugeordneter Freiberufler; Löschen des Freiberuflers wird verhindert, solange Positionen existieren |
| `status_id`              | `status_id`     | `BIGINT`   | —         | FK → project_position_status(id), NOT NULL        | Pflichtauswahl; siehe [STAMMDATEN.md](STAMMDATEN.md)                                               |
| `konditionen`            | `konditionen`   | `LONGTEXT` | unlimited | nullable                                          | Verhandelte Konditionen (Freitext)                                                                  |
| `kommentar`              | `kommentar`     | `LONGTEXT` | unlimited | nullable                                          | Interner Kommentar zur Zuordnung                                                                    |

## Kontakthistorie

Zu jedem Projekt kann eine Kontakthistorie gepflegt werden. Einträge können jederzeit hinzugefügt,
bearbeitet und gelöscht werden. Für jeden Eintrag wird ein Audit-Log geführt analog zu den
Projekt-Stammdaten.

Im Unterschied zur Kontakthistorie anderer Module (Freiberufler, Partner, Kunden) gibt es **keine
Typisierung** der Einträge. Jeder Eintrag besteht ausschließlich aus einem Freitextfeld.

Die Neuanlage und Bearbeitung eines Eintrags erfolgt jeweils über ein modales Formular. Das Löschen wird
über einen Löschen-Button je Eintrag ausgelöst; vor der Ausführung wird ein modaler Bestätigungsdialog
mit dem Text „Sind Sie sicher?" angezeigt.

Die Einträge werden absteigend nach Erfassungsdatum sortiert angezeigt (neueste Einträge zuerst).

Jeder Eintrag wird in der Listenansicht mit folgenden Informationen angezeigt:

* **Erfasst am** (`creationDate`) und **von** (`creationUserID`)
* **Zuletzt geändert am** (`lastModificationDate`) und **von** (`lastModificationUserID`) – nur wenn abweichend von der Erfassung
* **Text** (`description`) – vollständig, da mehrzeilige Einträge möglich sind

Die Kontakthistorie der Projekte wird in der Tabelle `project_history` gespeichert:

| Feld                     | Datenbankspalte | Datentyp   | Länge     | Prüfungen                                     | Label für die UI                 | Hinweise                              |
|--------------------------|-----------------|------------|-----------|-----------------------------------------------|----------------------------------|---------------------------------------|
| `id`                     | `id`            | `BIGINT`   | —         | PK, NOT NULL, AUTO_INCREMENT                  | —                                |                                       |
| `creationDate`           | `creation_date` | `DATETIME` | —         | nullable                                      | Zeitpunkt der Erfassung          |                                       |
| `creationUserID`         | `creation_user` | `VARCHAR`  | 255       | nullable                                      | Erfassender Sachbearbeiter       |                                       |
| `lastModificationDate`   | `changed_date`  | `DATETIME` | —         | nullable                                      | Zeitpunkt der letzten Änderung   |                                       |
| `lastModificationUserID` | `changed_user`  | `VARCHAR`  | 255       | nullable                                      | Zuletzt ändernder Sachbearbeiter |                                       |
| `description`            | `description`   | `LONGTEXT` | unlimited | NOT NULL                                      | Text                             | Freitextfeld für den Historieneintrag |
| `project_id`             | `project_id`    | `BIGINT`   | —         | FK → project(id), NOT NULL, ON DELETE CASCADE | —                                | Zugehöriges Projekt                   |

## Navigation

Das Formular bietet generell die folgenden Navigationsmöglichkeiten durch alle bekannten Projekte:

* Springe zum ersten Projekt (kleinste ID)
* Springe zum vorherigen Projekt (die vorherige bekannte ID vor der ID des aktuellen Projekts)
* Springe zum nächsten Projekt (die nächste bekannte ID nach der ID des aktuellen Projekts)
* Springe zum letzten Projekt (höchste ID)
* Springe zu einem Projekt mit einer bestimmten ID

Zu jedem Projekt werden neben der ID die Audit-Informationen angezeigt: Zeitpunkt der ersten Erfassung
sowie der Name des Sachbearbeiters, und der Zeitpunkt der letzten Änderung sowie der Name des Sachbearbeiters.

## Neu, Bearbeiten, Speichern und Löschen

**Neuanlage ausschließlich über Kunden- oder Partner-Formular**: Das Projekt-Formular bietet **keine**
Neuanlage-Funktion. Projekte werden ausschließlich aus dem Kunden- oder Partner-Formular heraus angelegt
(Aktion „Neues Projekt erfassen", siehe [KUNDEN.md](KUNDEN.md) und [PARTNER.md](PARTNER.md)). Dabei wird
das Projekt-Formular mit dem entsprechenden Kunden- oder Partner-Datensatz vorausgefüllt geöffnet.

**Gemerktes Projekt**: Jedes Mal, wenn ein Sachbearbeiter ein Projekt im Projekte-Formular aufruft oder
durch die Navigation zu einem Projekt wechselt, wird dieses Projekt als „gemerktes Projekt" des
Sachbearbeiters server-seitig gespeichert. Das gemerkte Projekt wird in der Toolbar der Formulare
Freiberufler, Partner, Kunden und Profilsuche angezeigt und ermöglicht im Freiberufler-Formular die direkte Zuordnung
eines Freiberuflers zu diesem Projekt (siehe [FREIBERUFLER.md](FREIBERUFLER.md) – Gemerktes Projekt).

Bei **Neuanlage** wird das Projekt erst nach dem ersten erfolgreichen Speichern (sobald eine Datenbank-ID
vergeben ist) als gemerktes Projekt gesetzt. Ein noch nicht gespeichertes Projekt wird nicht gemerkt.

**Initialzustand**: Beim Öffnen des Formulars (ohne Kontextaufruf) wird das zuletzt angezeigte Projekt
geladen. Existiert dieses nicht mehr oder wurde noch kein Projekt geöffnet, bleibt das Formular leer und
zeigt nur die Navigation an. Ein leeres Formular dient in diesem Fall ausschließlich als QBE-Suchmaske —
eine Speicherung aus dem leeren Zustand heraus (ohne vorherigen Kontextaufruf) ist nicht zulässig und wird
verhindert.

Bei Kontextaufruf (Neuanlage aus Kunden- oder Partner-Formular) wird das Formular mit folgenden Vorbelegen
geöffnet:
- `customer_id` bzw. `partner_id` mit dem aufrufenden Datensatz vorausgefüllt (nicht änderbar)
- `status` auf „Offen" (Wert `1`) vorbelegt
- Alle übrigen Felder leer

Folgende Aktionen werden zusätzlich zur Navigation angeboten:

* Speichere die aktuellen Daten
* Lösche das aktuelle Projekt inkl. aller verknüpften Informationen

**Löschen**: Vor dem Löschen wird ein modaler Bestätigungsdialog angezeigt mit dem Text:
> „Es wird das Projekt und alle verknüpften Informationen gelöscht. Sind Sie sicher?"

Der Dialog bietet die Optionen „Ja, löschen" und „Abbrechen". Erst nach Bestätigung wird die Löschung ausgeführt.
Anschließend wird ein leeres Formular angezeigt.

Beim Löschen eines Projekts werden **kaskadierend mitgelöscht** (`ON DELETE CASCADE`):
- Alle Einträge der Kontakthistorie (`project_history`)
- Alle Freiberufler-Zuordnungen (`project_position`)

Bei Chat-Sitzungen der Profilsuche, die diesem Projekt zugeordnet waren, wird `profile_search_chat.project_id` auf `NULL` gesetzt (`ON DELETE SET NULL`). Der Chat-Verlauf bleibt erhalten.

**Nicht gelöscht** werden die referenzierten Stammdatensätze:
- Der zugeordnete Kunde (`kunde`) bleibt unberührt
- Der zugeordnete Partner (`partner`) bleibt unberührt
- Die zugeordneten Freiberufler (`freelancer`) bleiben unberührt

**Gemerktes Projekt nach Löschung**: Beim Löschen eines Projekts wird das „gemerkte Projekt" aller
Sachbearbeiter, die dieses Projekt gemerkt hatten, automatisch entfernt. Damit wird verhindert, dass
die Toolbar auf einen nicht mehr existierenden Datensatz verweist. Dies erfolgt durch `ON DELETE CASCADE`
auf der Tabelle `remembered_project` (siehe unten).

**Ungespeicherte Änderungen**: Sobald der Sachbearbeiter Änderungen am Formular vornimmt, die noch nicht
gespeichert wurden, wird oberhalb des Formulars ein farblich hervorgehobener Banner angezeigt (gelb/orange
mit dem Text „Es gibt ungespeicherte Änderungen"). Der Banner ist rein informativ und blockiert keine weiteren
Aktionen.

**Gleichzeitige Bearbeitung**: Das Formular kann von mehreren Sachbearbeitern gleichzeitig genutzt werden.
Das Optimistic Locking über das `version`-Feld erkennt Konflikte beim Speichern. Im Konfliktfall wird ein
modaler Dialog angezeigt, der den Namen des Sachbearbeiters und den Zeitpunkt der konkurrierenden Änderung
enthält. Der Sachbearbeiter hat dann folgende Optionen:

* **Änderungen verwerfen und neu laden**: Die eigenen Änderungen werden verworfen und der Datensatz wird in
  der aktuell in der Datenbank gespeicherten Version neu geladen.
* **Trotzdem speichern**: Die eigenen Änderungen werden gespeichert und überschreiben damit die Daten des
  anderen Sachbearbeiters. Das `version`-Feld wird dabei entsprechend aktualisiert.

### Datenbankstruktur (`remembered_project`)

Das gemerkte Projekt wird pro Sachbearbeiter in der Tabelle `remembered_project` persistent gespeichert.
Die Benutzeridentität ist der `username` aus `ps_user` des eingeloggten Benutzers (vgl. SWARCHITEKTUR.md –
Authentifizierung).

| Feld         | Datenbankspalte | Datentyp       | Prüfungen                                     | Hinweise                                     |
|--------------|-----------------|----------------|-----------------------------------------------|----------------------------------------------|
| `userId`     | `user_id`       | `VARCHAR(255)` | PK, NOT NULL                                  | `ps_user.username`; ein Eintrag pro Benutzer |
| `project_id` | `project_id`    | `BIGINT`       | FK → project(id), NOT NULL, ON DELETE CASCADE | Das gemerkte Projekt                         |

**Designentscheidungen:**
- `user_id` ist der Primary Key – pro Benutzer gibt es genau einen Eintrag (kein Verlauf, nur aktueller Stand).
- `ON DELETE CASCADE`: Wird ein Projekt gelöscht, entfällt der `remembered_project`-Eintrag automatisch. Das ist eine der wenigen Stellen im System, an denen Kaskadierung explizit gewünscht ist – eine RESTRICT-Sperre beim Projektlöschen wäre fachlich falsch.
- Das `remembered_project`-Aggregat gehört zum `project`-Modul (direkte FK-Beziehung, semantischer Bezug).

## Suche

Das Formular bietet eine Query-By-Example Suchfunktion. Ist das Formular leer (kein Projekt geladen, keine
Datenbank-ID vorhanden), kann der Sachbearbeiter die Felder mit Suchausdrücken befüllen und über den Suchbutton
eine QBE-Suche starten. Da keine Neuanlage über das Projekt-Formular möglich ist, dient das leere Formular
ausschließlich als Suchmaske.

Auf folgenden Feldern kann mit QBE gesucht werden:

* `projectNumber`
* `descriptionShort`
* `descriptionLong`
* `skills`
* `workplace`
* `duration`
* `status` (exakter Vergleich, keine LIKE-Suche)
* `debitorNr`
* `kreditorNr`

Das Suchergebnis wird in einer Tabelle mit den folgenden Spalten angezeigt:

| Spalte           | Feld               | Sortierbar |
|------------------|--------------------|------------|
| Projektnummer    | `projectNumber`    | ja         |
| Kurzbeschreibung | `descriptionShort` | ja         |
| Einsatzort       | `workplace`        | ja         |
| Startdatum       | `startDate`        | ja         |
| Status           | `status`           | ja         |
| Stundensatz VK   | `stundensatzVK`    | ja         |

Die Ergebnistabelle ist standardmäßig nach Eingangsdatum (`entryDate`) absteigend sortiert (neueste Projekte
zuerst). Der Sachbearbeiter kann die Sortierung durch Klick auf eine sortierbare Spaltenüberschrift ändern;
ein erneuter Klick kehrt die Reihenfolge um. Zu jedem Projekt wird ein Link zur Detailansicht angezeigt.

**Leeres Suchergebnis**: Liefert die Suche keinen Treffer, wird anstelle der Tabelle ein entsprechender Hinweis
angezeigt. Dieser enthält einen Link, der den Sachbearbeiter zurück zum ausgefüllten QBE-Formular führt, damit
die Suchkriterien angepasst werden können.

**Anzahl der Treffer und Nachladen**: Oberhalb der Ergebnistabelle wird die Gesamtanzahl der gefundenen Treffer
angezeigt. Initial werden maximal 20 Treffer geladen. Weitere Treffer werden automatisch nachgeladen, sobald der
Sachbearbeiter ans Ende der Liste scrollt (Infinite Scrolling).

Die einzelnen QBE-Felder werden durch einen `AND`-Ausdruck miteinander kombiniert. Innerhalb eines Textfeldes
wird eine LIKE-Suche gestartet; aus dem Suchausdruck `München` wird somit die SQL-Abfrage `LIKE '%München%'`.
Der `status`-Filter wird als exakter Vergleich (`=`) ausgeführt.

## Offene Punkte

### Datenbankintegrität

### Offene Fragen

**`entryDate` in der QBE-Suche**
- Das Eingangsdatum (`entryDate`) ist Standard-Sortierkriterium der Suchergebnisliste, ist jedoch nicht als QBE-Suchfeld definiert. Zu klären: Soll nach Eingangsdatum (z. B. Datumsbereich) gesucht werden können?

