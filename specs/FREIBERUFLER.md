# Spezifikation Formular "Freiberufler"

Dieses Dokument beschreibt die fachliche und technische Spezifikation des Formulars "Freiberufler" und dient
als Grundlage für die KI-gestützte Implementierung. Es richtet sich an ein KI-System, das auf Basis dieser
Beschreibung den Programmcode eigenständig erzeugen soll. Die Spezifikation ist daher so präzise wie möglich
gehalten und enthält neben der Beschreibung der Geschäftslogik auch Datenbankstrukturen, Validierungsregeln
und Verhaltensanforderungen an die Benutzeroberfläche.

Gemeinsam genutzte Stammdaten (Historientypen, Kontaktmöglichkeiten-Typen) sind in
[STAMMDATEN.md](STAMMDATEN.md) beschrieben.

## Anwendungsfall

Dieses Formular dient der Erfassung von Freiberuflern und deren Stammdaten. Zusätzlich kann zu jedem Freiberufler eine
typisierte Kontakthistorie gepflegt werden, in der die Sachbearbeiter Informationen zu Freiberuflern erfassen
(neueste Einträge zuerst).

## Stammdaten

Freiberufler werden in der Tabelle `freelancer` gespeichert. Die Felder sind nach fachlicher Zugehörigkeit
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

**Gruppe: Adresse**

| Feld            | Datenbankspalte  | Datentyp    | Länge  | Prüfungen                    | Label für die UI | Hinweise                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
|-----------------|------------------|-------------|--------|------------------------------|------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `titel`         | `titel`          | `VARCHAR`   | 255    | nullable                     | Titel            | Anrede / Titel (z. B. "Dr.", "Prof.")                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| `name1`         | `name1`          | `VARCHAR`   | 255    | nullable                     | Name             |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| `name2`         | `name2`          | `VARCHAR`   | 255    | nullable                     | Vorname          |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| `company`       | `company`        | `VARCHAR`   | 255    | nullable                     | Firma            |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| `street`        | `street`         | `VARCHAR`   | 255    | nullable                     | Strasse          |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| `country`       | `country`        | `VARCHAR`   | 255    | nullable                     | Land             | Gruppiert mit PLZ/Ort unter "Land PLZ/Ort"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| `plz`           | `plz`            | `VARCHAR`   | 255    | nullable                     | PLZ              | Gruppiert mit Land/Ort                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| `city`          | `city`           | `VARCHAR`   | 255    | nullable                     | Ort              |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| `nationalitaet` | `nationalitaet`  | `VARCHAR`   | 255    | nullable                     | Nationalität     |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| `geburtsdatum`  | `geburtsdatum`   | `VARCHAR`   | 255    | nullable                     | Geburtsdatum     | Freitextfeld; erlaubt Teilangaben wie "Mai 1980"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| `partner_id`    | `partner_id`     | `BIGINT`    | —      | FK → partner(id), nullable   | Partner          | Optionale Zuordnung zu einem Partnerunternehmen. Wird im Formular als **nicht bearbeitbares** Anzeigefeld dargestellt. Ist ein Partner zugeordnet, wird dessen Firmenname als anklickbarer Link angezeigt; der Link öffnet den zugehörigen Partner-Datensatz. Bei der Neuanlage eines Freiberuflers über das Partner-Formular wird dieses Feld mit der ID des aufrufenden Partners vorausgefüllt, ist aber weiterhin nicht direkt bearbeitbar. Die Zuordnung und Auflösung einer Partner-Beziehung erfolgt ausschließlich über das Partner-Formular. |

**Gruppe: Kontaktinformationen**

Wenn `contactforbidden` gesetzt ist, wird oberhalb des Formulars ein rotes Banner mit dem Text „Kontaktsperre"
angezeigt.

| Feld               | Datenbankspalte    | Datentyp | Länge | Prüfungen                 | Label für die UI | Hinweise                        |
|--------------------|--------------------|----------|-------|---------------------------|------------------|---------------------------------|
| `contactforbidden` | `contactforbidden` | `BIT`    | 1     | NOT NULL, default `false` | Kontaktsperre    | Rotes Banner bei gesetztem Flag |
| `showAgain`        | `show_again`       | `BIT`    | 1     | NOT NULL, default `false` | Wiedervorlage    |                                 |

In dieser Gruppe werden ebenfalls die Kontaktmöglichkeiten angezeigt.

**Gruppe: Kommentar**

| Feld             | Datenbankspalte  | Datentyp   | Länge     | Prüfungen | Label für die UI | Hinweise                        |
|------------------|------------------|------------|-----------|-----------|------------------|---------------------------------|
| `comments`       | `comments`       | `LONGTEXT` | unlimited | nullable  | Kommentar        | Allgemeiner Freitext            |

**Gruppe: Einsatzdetails**

| Feld             | Datenbankspalte  | Datentyp   | Länge     | Prüfungen | Label für die UI | Hinweise                        |
|------------------|------------------|------------|-----------|-----------|------------------|---------------------------------|
| `einsatzdetails` | `einsatzdetails` | `LONGTEXT` | unlimited | nullable  | Einsatzdetails   | Einsatz- / Auftragsdetails      |

**Gruppe: Zusatzinformationen**

| Feld              | Datenbankspalte     | Datentyp   | Länge | Prüfungen                                                          | Label für die UI | Hinweise                                                                        |
|-------------------|---------------------|------------|-------|--------------------------------------------------------------------|------------------|---------------------------------------------------------------------------------|
| `contactPerson`   | `contact_person`    | `VARCHAR`  | 255   | nullable                                                           | Kontaktperson    | Name des betreuenden Sachbearbeiters                                            |
| `contactType`     | `contact_type`      | `VARCHAR`  | 255   | nullable                                                           | Kontaktkanal     | Freitext: wie wurde Kontakt aufgenommen (z. B. E-Mail, Telefon, Messe)          |
| `contactReason`   | `contact_reason`    | `VARCHAR`  | 255   | nullable                                                           | Kontaktgrund     | Freitext: Grund des letzten Kontakts                                            |
| `lastContactDate` | `last_contact_date` | `DATETIME` | —     | nullable                                                           | Letzter Kontakt  | Datumseingabe (dd.MM.yyyy)                                                      |
| `kontaktart`      | `kontaktart`        | `VARCHAR`  | 10    | nullable, CHECK (`kontaktart` IN ('NL','NL1','NL2','X','NO','LL')) | Kontaktart       | Auswahlliste; erlaubte Werte: NL, NL1, NL2, X, NO, LL; leer = kein Wert gesetzt |

**Gruppe: Verfügbarkeit & Konditionen**

| Feld                      | Datenbankspalte               | Datentyp   | Länge | Prüfungen                 | Label für die UI | Hinweise                                                                                   |
|---------------------------|-------------------------------|------------|-------|---------------------------|------------------|--------------------------------------------------------------------------------------------|
| `availabilityAsDate`      | `availability_as_date`        | `DATETIME` | —     | nullable                  | Verfügbarkeit    | Datumseingabe (dd.MM.yyyy): ab wann verfügbar                                              |
| `salaryLong`              | `salary_long`                 | `BIGINT`   | —     | nullable                  | Stundensatz      | Gewünschter Stundensatz in ganzen Euro                                                     |
| `salaryPerDayLong`        | `salary_per_day_long`         | `BIGINT`   | —     | nullable                  | Tagessatz        | Gewünschter Tagessatz in ganzen Euro                                                       |
| `salaryRemote`            | `salary_remote`               | `BIGINT`   | —     | nullable                  | Stds. Remote     | Tooltip: "Stundensatz Remote"; gewünschter Remote-Stundensatz in ganzen Euro               |
| `salaryPartnerLong`       | `salary_partner_long`         | `BIGINT`   | —     | nullable                  | Stds. verhandelt | Tooltip: "Stundensatz verhandelt"; verhandelter Stundensatz (Einkaufspreis) in ganzen Euro |
| `salaryPartnerPerDayLong` | `salary_partner_per_day_long` | `BIGINT`   | —     | nullable                  | Tgs. Partner     | Tooltip: "Tagessatz Partner"; verhandelter Tagessatz (Einkaufspreis) in ganzen Euro        |
| `datenschutz`             | `datenschutz`                 | `BIT`      | 1     | NOT NULL, default `false` | Datenschutz      | DSGVO-Einwilligung                                                                         |
| `debitorNr`               | `debitor_nr`                  | `VARCHAR`  | 255   | nullable                  | Debitor          | Debitorennummer (Buchhaltung)                                                              |
| `gulpID`                  | `gulp_id`                     | `VARCHAR`  | 255   | nullable                  | Gulp ID          | Profil-ID auf GULP.de                                                                      |

**Gruppe: Kodierung**

| Feld            | Datenbankspalte  | Datentyp     | Länge     | Prüfungen        | Label für die UI  | Hinweise                                                                                                                                                                                                                                                                                                                                                                            |
|-----------------|------------------|--------------|-----------|------------------|-------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `code`          | `code`           | `VARCHAR`    | 255       | nullable, UNIQUE | Kodierung         | Interner, fachlich eindeutiger Freiberufler-Code. Dient als anonymisiertes Merkmal für Referenzen und die Kommunikation mit Kunden (z. B. in Profilen und Angeboten ohne Namensnennung). Die Eindeutigkeit wird auf Datenbankebene durch einen UNIQUE-Index sichergestellt. Über diesen Code kann ein bestehender Freiberufler im Partner-Formular einem Partner zugeordnet werden. |
| `skills`        | `skills`         | `LONGTEXT`   | unlimited | nullable         | Skills            | Freitext-Skillbeschreibung                                                                                                                                                                                                                                                                                                                                                          |

In dieser Gruppe werden ebenfalls die Zuordnungen zu den Tags angezeigt, und zwar zwischen `code` und `skills`.

### Formularstruktur / weitere Informationen

Zu jedem Freiberufler können beliebig viele Tags aus jeder der folgenden Gruppen hinzugefügt werden. Innerhalb einer Gruppe ist die Zuordnung von mehreren Tags möglich:

* Schwerpunkt
* Funktion
* Einsatzort
* Bemerkung
* Typ

Die zur Verfügung stehenden Tags sind eine endliche Liste und werden in einer Datenbanktabelle gespeichert. Im
Freiberufler-Formular können Tags einem Freiberufler zugeordnet oder wieder entfernt werden.

Zugeordnete Tags werden überall als Chips dargestellt. Jeder Chip zeigt den Tag-Namen und enthält einen
Entfernen-Button (×), über den die Zuordnung direkt aufgehoben werden kann. Neue Tags werden über ein
Auswahlfeld (z. B. Dropdown) je Gruppe hinzugefügt, das nur die noch nicht zugeordneten Tags der jeweiligen
Gruppe anzeigt.

Die Gruppen werden in der folgenden festen Reihenfolge angezeigt: Schwerpunkt, Funktion, Einsatzort, Bemerkung,
Typ. Die Chips innerhalb einer Gruppe werden alphabetisch nach Tag-Name sortiert.

Die zur Verfügung stehenden Tags sind in der Tabelle `tags` in folgender Struktur gespeichert:

| Feld             | Datenbankspalte | Datentyp       | Länge    | Prüfungen                    | Hinweise             |
|------------------|-----------------|----------------|----------|------------------------------|----------------------|
| `id`             | `id`            | `BIGINT`       | —        | PK, NOT NULL, AUTO_INCREMENT |                      |
| `name`           | `tagname`       | `VARCHAR`      | 255      | NOT NULL                     | Anzeigename des Tags |
| `type`           | `type`          | `VARCHAR`      | 255      | NOT NULL                     | TagType Enum-Name    |

**TagType Enum:**

| Ordinalwert | Name          | Bedeutung                   |
|-------------|---------------|-----------------------------|
| 0           | `SCHWERPUNKT` | Fachlicher Schwerpunkt      |
| 1           | `FUNKTION`    | Jobrolle / Funktion         |
| 2           | `EINSATZORT`  | Einsatzort                  |
| 3           | `BEMERKUNG`   | Bemerkung / Anmerkung       |
| 4           | `TYP`         | Typ / Kategorie             |

Die Zuordnung von Tags zu Freiberuflern wird in der Tabelle `freelancer_tags` in folgender Struktur gespeichert:

UNIQUE-Constraint: `(freelancer_id, tag_id)` — ein Tag kann einem Freiberufler nur einmal zugeordnet werden.

| Feld                     | Datenbankspalte | Datentyp   | Länge | Prüfungen                                        | Hinweise                         |
|--------------------------|-----------------|------------|-------|--------------------------------------------------|----------------------------------|
| `id`                     | `id`            | `BIGINT`   | —     | PK, NOT NULL, AUTO_INCREMENT                     |                                  |
| `creationDate`           | `creation_date` | `DATETIME` | —     | nullable                                         | Zeitpunkt der Erfassung          |
| `creationUserID`         | `creation_user` | `VARCHAR`  | 255   | nullable                                         | Erfassender Sachbearbeiter       |
| `lastModificationDate`   | `changed_date`  | `DATETIME` | —     | nullable                                         | Zeitpunkt der letzten Änderung   |
| `lastModificationUserID` | `changed_user`  | `VARCHAR`  | 255   | nullable                                         | Zuletzt ändernder Sachbearbeiter |
| `freelancer_id`          | `freelancer_id` | `BIGINT`   | —     | FK → freelancer(id), NOT NULL, ON DELETE CASCADE | Zugeordneter Freiberufler        |
| `tag_id`                 | `tag_id`        | `BIGINT`   | —     | FK → tags(id), NOT NULL                          | Zugeordneter Tag                 |

## Kontaktmöglichkeiten

Zu einem Freiberufler können beliebig viele Kontaktmöglichkeiten hinzugefügt, bearbeitet und gelöscht werden.
Jede Kontaktmöglichkeit hat genau einen Typ. Typen, Darstellungsregeln, Sortierreihenfolge sowie das modale
Formular für Neuanlage/Bearbeitung sind in [STAMMDATEN.md](STAMMDATEN.md) beschrieben.

### Speicherverhalten

Änderungen an Kontaktmöglichkeiten (Hinzufügen, Bearbeiten, Löschen) werden **ausschließlich beim
Drücken des Speichern-Buttons** persistent gespeichert – gemeinsam mit den Freiberufler-Stammdaten in
einer einzigen Transaktion. Bis dahin werden die Änderungen nur im Client-State (JavaScript) gehalten
und in der UI dargestellt, ohne die Datenbank zu berühren.

Dies gilt sowohl für bestehende Freiberufler (Bearbeitung) als auch für neue Freiberufler (Neuanlage). Bei einem
neuen Freiberufler können daher Kontaktmöglichkeiten bereits vor dem ersten Speichern erfasst werden; sie
werden beim Speichern des Freiberuflers zusammen mit den Stammdaten angelegt.

Die Kontaktmöglichkeiten werden als JSON-Array im versteckten Formularfeld `contactsJson` an den
Server übertragen. Jedes Element enthält:

```json
[
  { "id": 17, "type": "EMAIL",   "value": "info@example.com" },
  { "id": null, "type": "TELEFON", "value": "+49 89 123456" }
]
```

* `id` ist `null` für neu angelegte Einträge (Server erzeugt die ID beim INSERT).
* `id` ist eine positive Ganzzahl für bestehende Einträge, die beibehalten oder geändert wurden.
* Einträge, die in der ursprünglichen DB-Liste vorhanden waren, im gesendeten Array aber **fehlen**,
  werden vom Server gelöscht.

Der Server führt beim Speichern eine vollständige **Replace-Logik** durch:
1. Alle existierenden `freelancer_contact`-Einträge mit `freelancer_id = :id` werden geladen.
2. Einträge, deren `id` nicht im gesendeten Array enthalten ist → DELETE.
3. Einträge mit `id != null` → UPDATE (type, value).
4. Einträge mit `id == null` → INSERT.

Die Kontaktmöglichkeiten werden in der Tabelle `freelancer_contact` in folgender Struktur gespeichert:

| Feld                     | Datenbankspalte | Datentyp   | Länge | Prüfungen                                        | Hinweise                                                   |
|--------------------------|-----------------|------------|-------|--------------------------------------------------|------------------------------------------------------------|
| `id`                     | `id`            | `BIGINT`   | —     | PK, NOT NULL, AUTO_INCREMENT                     |                                                            |
| `creationDate`           | `creation_date` | `DATETIME` | —     | nullable                                         | Zeitpunkt der Erfassung                                    |
| `creationUserID`         | `creation_user` | `VARCHAR`  | 255   | nullable                                         | Erfassender Sachbearbeiter                                 |
| `lastModificationDate`   | `changed_date`  | `DATETIME` | —     | nullable                                         | Zeitpunkt der letzten Änderung                             |
| `lastModificationUserID` | `changed_user`  | `VARCHAR`  | 255   | nullable                                         | Zuletzt ändernder Sachbearbeiter                           |
| `type`                   | `type`          | `VARCHAR`  | 255   | NOT NULL                                         | Typ-Enum: `EMAIL`, `WEB`, `XING`, `GULP`, `TELEFON`, `FAX` |
| `value`                  | `value`         | `VARCHAR`  | 255   | NOT NULL                                         | Der eigentliche Kontaktwert                                |
| `freelancer_id`          | `freelancer_id` | `BIGINT`   | —     | FK → freelancer(id), NOT NULL, ON DELETE CASCADE | Zugehöriger Freiberufler                                   |

## Kontakthistorie

Zu jedem Freiberufler kann eine typisierte Kontakthistorie gepflegt werden. Zu dieser Historie können jederzeit
Einträge hinzugefügt, gelöscht oder auch geändert werden. Für jeden Eintrag gibt es ein Audit-Log, d. h. analog
der Freiberufler-Stammdaten wird gespeichert, wann und von wem ein Eintrag angelegt wurde, und wann und von wem
ein Eintrag zuletzt geändert wurde.

Die Neuanlage und Bearbeitung eines Eintrags erfolgt jeweils über ein modales Formular. Das Löschen wird über
einen Löschen-Button je Eintrag ausgelöst; vor der Ausführung wird ein modaler Bestätigungsdialog mit dem Text
„Sind Sie sicher?" angezeigt.

Die Einträge werden absteigend nach Erfassungsdatum sortiert angezeigt (neueste Einträge zuerst).

Jeder Eintrag der Kontakthistorie besteht aus einem Pflicht-Typ (aus der Tabelle `historytype`) sowie einem
Freitextfeld, das beliebig viele Zeilen enthalten kann. Jeder Eintrag wird in der Listenansicht mit folgenden
Informationen angezeigt:

* **Typ** (aus `historytype`)
* **Erfasst am** (`creationDate`) und **von** (`creationUserID`)
* **Zuletzt geändert am** (`lastModificationDate`) und **von** (`lastModificationUserID`) – nur wenn abweichend von der Erfassung
* **Text** (`description`) – vollständig, da mehrzeilige Einträge möglich sind

### Speicherverhalten

Identisch zu den Kontaktmöglichkeiten: Änderungen an Historieneinträgen (Hinzufügen, Bearbeiten, Löschen)
werden **ausschließlich beim Drücken des Speichern-Buttons** persistent gespeichert – in derselben
Transaktion wie die Freiberufler-Stammdaten. Bis dahin existieren die Änderungen nur im Client-State.

Dies gilt auch für neue Freiberufler: Historieneinträge können bereits vor dem ersten Speichern erfasst werden.

Die Historieneinträge werden als JSON-Array im versteckten Formularfeld `historyJson` übertragen.
Jedes Element enthält:

```json
[
  { "id": 5,    "typeId": 2, "description": "Telefonat geführt" },
  { "id": null, "typeId": 1, "description": "Neuer Eintrag" }
]
```

* `id` ist `null` für neu angelegte Einträge.
* `id` ist eine positive Ganzzahl für bestehende, beibehaltene oder geänderte Einträge.
* Einträge, die in der DB-Liste vorhanden waren, im gesendeten Array aber **fehlen**, werden gelöscht.

Der Server führt beim Speichern dieselbe **Replace-Logik** wie bei Kontaktmöglichkeiten durch:
DELETE für fehlende IDs, UPDATE für vorhandene IDs, INSERT für `id == null`.

**Audit-Felder bei Historieneinträgen:** `creationDate`/`creationUser` werden beim INSERT durch das
Spring-Auditing gesetzt und danach nicht mehr verändert. `changedDate`/`changedUser` werden bei
jedem UPDATE aktualisiert.

Die Kontakthistorie der Freiberufler wird in der Tabelle `freelancer_history` in folgender Struktur gespeichert:

| Feld                     | Datenbankspalte | Datentyp   | Länge     | Prüfungen                                        | Label für die UI                 | Hinweise                              |
|--------------------------|-----------------|------------|-----------|--------------------------------------------------|----------------------------------|---------------------------------------|
| `id`                     | `id`            | `BIGINT`   | —         | PK, NOT NULL, AUTO_INCREMENT                     | —                                |                                       |
| `creationDate`           | `creation_date` | `DATETIME` | —         | nullable                                         | Zeitpunkt der Erfassung          |                                       |
| `creationUserID`         | `creation_user` | `VARCHAR`  | 255       | nullable                                         | Erfassender Sachbearbeiter       |                                       |
| `lastModificationDate`   | `changed_date`  | `DATETIME` | —         | nullable                                         | Zeitpunkt der letzten Änderung   |                                       |
| `lastModificationUserID` | `changed_user`  | `VARCHAR`  | 255       | nullable                                         | Zuletzt ändernder Sachbearbeiter |                                       |
| `description`            | `description`   | `LONGTEXT` | unlimited | NOT NULL                                         | Text                             | Freitextfeld für den Historieneintrag |
| `type_id`                | `type_id`       | `BIGINT`   | —         | FK → historytype(id), NOT NULL                   | Typ                              | Pflichtauswahl aus Combobox           |
| `freelancer_id`          | `freelancer_id` | `BIGINT`   | —         | FK → freelancer(id), NOT NULL, ON DELETE CASCADE | —                                | Zugehöriger Freiberufler              |

Die Typisierung der Kontakthistorie erfolgt über die gemeinsam genutzte Tabelle `historytype`,
die in [STAMMDATEN.md](STAMMDATEN.md) beschrieben ist.

## Navigation

Das Formular bietet generell die folgenden Navigationsmöglichkeiten durch alle bekannten Freiberufler:

* Springe zum ersten Freiberufler (kleinste ID)
* Springe zum vorherigen Freiberufler (die vorherige bekannte ID vor der ID des aktuellen Freiberuflers)
* Springe zum nächsten Freiberufler (die nächste bekannte ID nach der ID des aktuellen Freiberuflers)
* Springe zum letzten Freiberufler (höchste ID)
* Springe zu einem Freiberufler mit einer bestimmten ID

Die Navigation erfolgt also analog zu einem Videoplayer. Zu jedem Freiberufler sollen neben der ID zusätzliche
Audit-Informationen angezeigt werden. Das wäre der Zeitpunkt der ersten Erfassung sowie der Name des Sachbearbeiters,
und auch der Zeitpunkt der letzten Änderung sowie der Name des Sachbearbeiters.

### Gemerktes Projekt

Das System merkt sich pro Sachbearbeiter das zuletzt im Projekte-Formular angezeigte Projekt
(server-seitig, persistent über Sessions hinweg). Dieses „gemerkte Projekt" wird in der Toolbar des
Freiberufler-Formulars sichtbar angezeigt, sofern ein gemerktes Projekt vorhanden ist (z. B.:
„Gemerktes Projekt: 2026-042 – Java-Entwickler München").

Ist ein gemerktes Projekt vorhanden **und** wird ein bestehender Freiberufler angezeigt (Datensatz mit ID),
erscheint in der Toolbar zusätzlich der Button **„Dem Projekt [Projektnummer] zuordnen"**. Ein Klick darauf
öffnet ein modales Formular mit den folgenden Feldern:

| Feld          | Label       | Pflichtfeld | Hinweis                                             |
|---------------|-------------|-------------|-----------------------------------------------------|
| `status_id`   | Status      | ja          | Combobox; Werte aus `project_position_status`       |
| `konditionen` | Konditionen | nein        | Freitext (z. B. verhandelter Stundensatz, Laufzeit) |
| `kommentar`   | Kommentar   | nein        | Mehrzeiliger Freitext                               |

Nach Bestätigung wird eine neue Projektposition (`project_position`) angelegt. Existiert bereits eine
Zuordnung dieses Freiberuflers zu dem gemerkten Projekt, erscheint eine Fehlermeldung:
„Dieser Freiberufler ist diesem Projekt bereits zugeordnet."

Das gemerkte Projekt wird nicht automatisch gelöscht, wenn ein Freiberufler zugeordnet wurde. Es bleibt
so lange erhalten, bis der Sachbearbeiter im Projekte-Formular zu einem anderen Projekt navigiert.

## Neu, Bearbeiten, Speichern und Löschen

**Initialzustand**: Beim Öffnen des Formulars wird der zuletzt angezeigte Freiberufler geladen. Existiert dieser
nicht mehr oder gibt es keinen zuletzt angezeigten Freiberufler, wird das Formular leer angezeigt.

Das Formular kennt keinen expliziten Modus. Die einzige Unterscheidung ist, ob für die aktuell angezeigten Daten
eine Datenbank-ID existiert (bestehender Freiberufler) oder nicht (leeres Formular). Ein leeres Formular kann
sowohl für die Neuanlage als auch für eine QBE-Suche genutzt werden. Der einzige Hinweis für den Sachbearbeiter
auf den aktuellen Zustand ist der Banner für ungespeicherte Änderungen, der in allen Fällen (Neuanlage, QBE,
Bearbeitung) gleich funktioniert.

**Unified Save:** Sämtliche Änderungen am Formular – Stammdaten, Kontaktmöglichkeiten und Historieneinträge –
werden **ausschließlich beim Drücken des Speichern-Buttons** in einer einzigen Transaktion persistiert.
Kein AJAX-Endpunkt für Kontakte oder Historieneinträge schreibt direkt in die Datenbank; alle Mutationen
werden client-seitig im JavaScript-State gehalten und erst mit dem Speichern-Submit an den Server übertragen.
Dies gilt einheitlich für neue und bestehende Freiberufler.

Folgende Aktionen werden zusätzlich zur Navigation angeboten:

* Erstelle neuen Freiberufler (leert das Formular)
* Speichere die aktuellen Daten (Stammdaten + Kontakte + Historieneinträge in einer Transaktion)
* Lösche den aktuellen Freiberufler inkl. aller verknüpften Informationen

**Löschen**: Vor dem Löschen prüft das System, ob der Freiberufler einem oder mehreren Projekten
zugewiesen ist (Einträge in `project_position`).

- **Projektzuordnung vorhanden**: Das Löschen wird verhindert. Es wird eine Fehlermeldung in einem
  modalen Dialog angezeigt:
  > „Freiberufler ist Projekt [anklickbarer Link mit Projektnummer] zugewiesen. Bitte löschen Sie
  > zuerst die Projektzuordnung für diesen Freiberufler."

  Sind mehrere Projektzuordnungen vorhanden, werden alle betroffenen Projekte als separate Links
  aufgelistet. Der Dialog bietet nur „Schließen". Das Löschen wird nicht ausgeführt.

  Die FK-Constraint auf `project_position.freelancer_id` ist als `ON DELETE RESTRICT` definiert,
  sodass die Datenbank das Löschen ebenfalls verhindert.

- **Keine Projektzuordnung**: Es wird der reguläre Bestätigungsdialog angezeigt:
  > „Es wird der Freiberufler und alle verknüpften Informationen gelöscht. Sind Sie sicher?"

  Der Dialog bietet „Ja, löschen" und „Abbrechen". Erst nach Bestätigung wird die Löschung
  ausgeführt. Anschließend wird ein leeres Formular angezeigt.

  Beim Löschen eines Freiberuflers werden **kaskadierend mitgelöscht** (DB-seitiges `ON DELETE CASCADE`):
  - Alle Kontaktmöglichkeiten (`freelancer_contact`)
  - Alle Tag-Zuordnungen (`freelancer_tags`)
  - Alle Einträge der Kontakthistorie (`freelancer_history`)

  > **Hinweis:** `freelancer_history`- und `freelancer_contact`-Einträge sind separate Spring Data JDBC
  > Aggregate Roots (kein Teil des `freelancer`-Aggregats). Das Mitlöschen erfolgt daher ausschließlich
  > über die DB-seitige `ON DELETE CASCADE`-Constraint auf dem jeweiligen Fremdschlüssel `freelancer_id`.

**Ungespeicherte Änderungen**: Sobald der Sachbearbeiter Änderungen am Formular vornimmt, die noch nicht
gespeichert wurden, wird oberhalb des Formulars ein farblich hervorgehobener Banner angezeigt (z. B. gelb/orange
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

## Suche

Das Formular bietet eine Query-By-Example Suchfunktion. Ist das Formular leer (kein Freiberufler geladen, keine
Datenbank-ID vorhanden), kann der Sachbearbeiter die Felder mit Suchausdrücken befüllen und über den Suchbutton
eine QBE-Suche starten. Alternativ können die eingetragenen Daten über die reguläre Speichern-Aktion als neuer
Freiberufler gespeichert werden. Eine explizite Modustrennung zwischen Suche und Neuanlage gibt es nicht.

Auf folgenden Feldern kann mit QBE gesucht werden:

* `name1`
* `name2`
* `company`
* `street`
* `country`
* `plz`
* `city`
* `comments`
* `salaryLong`
* `code`
* `contactPerson`
* `contactType`
* `contactReason`
* `skills`
* `gulpID`
* `kontaktart` (exakter Vergleich, keine LIKE-Suche)
* `titel`
* `nationalitaet`
* `einsatzdetails`

Das Suchergebnis wird in einer Tabelle mit den folgenden Spalten angezeigt. Die Spaltenbezeichnungen entsprechen
den UI-Labels des Formulars:

| Spalte         | Feld                 | Sortierbar |
|----------------|----------------------|------------|
| Name           | `name1`              | ja         |
| Vorname        | `name2`              | ja         |
| Verfügbarkeit  | `availabilityAsDate` | nein       |
| Stundensatz    | `salaryLong`         | ja         |
| Skills         | `skills`             | nein       |
| Kodierung      | `code`               | ja         |
| Tags           | `freelancer_tags`    | nein       |

Die Tags werden je Gruppe in der festen Gruppenreihenfolge (Schwerpunkt, Funktion, Einsatzort, Bemerkung, Typ)
als Chips dargestellt, innerhalb einer Gruppe alphabetisch sortiert.

Die Ergebnistabelle ist standardmäßig nach Name (`name1`) und Vorname (`name2`) aufsteigend sortiert. Der
Sachbearbeiter kann die Sortierung durch Klick auf eine sortierbare Spaltenüberschrift ändern; ein erneuter
Klick kehrt die Reihenfolge um. Zu jedem Freiberufler wird ein Link zur Detailansicht angezeigt.

**Leeres Suchergebnis**: Liefert die Suche keinen Treffer, wird anstelle der Tabelle ein entsprechender Hinweis
angezeigt. Dieser enthält einen Link, der den Sachbearbeiter zurück zum ausgefüllten QBE-Formular führt, damit
die Suchkriterien angepasst werden können.

**Anzahl der Treffer und Nachladen**: Oberhalb der Ergebnistabelle wird die Gesamtanzahl der gefundenen Treffer
angezeigt. Initial werden maximal 20 Treffer geladen. Weitere Treffer werden automatisch nachgeladen, sobald der
Sachbearbeiter ans Ende der Liste scrollt (Infinite Scrolling).

Die einzelnen QBE-Felder werden durch einen `AND` Ausdruck miteinander kombiniert. Innerhalb eines Feldes wird
allerdings eine LIKE Suche im Fall von Zeichenfolgen gestartet, aus dem Suchausdruck `HAM` wird somit die SQL-Abfrage
`LIKE '%HAM%'`.

## Technische Hinweise zur Implementierung

### Speichern-Endpunkt

`POST /freelancer/save` empfängt neben den Stammdaten-Feldern zwei zusätzliche Formularfelder:

* `contactsJson` – JSON-Array der aktuellen Kontaktmöglichkeiten (siehe oben)
* `historyJson` – JSON-Array der aktuellen Historieneinträge (siehe oben)

Der Controller deserialisiert beide Arrays und führt die Replace-Logik in einer Transaktion mit dem
Freiberufler-Save durch.

### Entfallende AJAX-Endpunkte

Da alle Mutationen über den Speichern-Endpunkt laufen, entfallen separate AJAX-Controller für Kontakte
und Historieneinträge. Sollten solche Endpunkte existieren, werden sie entfernt.

### Client-seitiger State

Das Formular hält den aktuellen Stand von Kontaktmöglichkeiten und Historieneinträgen als
JavaScript-Arrays. Modale Formulare für Neuanlage/Bearbeitung mutieren ausschließlich diesen
lokalen State und rendern die Listen neu. Vor dem Absenden serialisiert das Formular beide Arrays
in die versteckten JSON-Felder.

Gelöschte Einträge werden aus dem lokalen Array entfernt; da sie nicht im gesendeten JSON erscheinen,
löscht der Server sie automatisch (Replace-Logik).

## Offene Punkte

### Administration

- **`showAgain` ohne Datum**: Ein reines Flag ohne Datum ist im Arbeitsalltag kaum nutzbar. Das Feld sollte um ein optionales Wiedervorlagedatum (`showAgainDate`, `DATETIME`, nullable) ergänzt werden.

### Datenbankintegrität

**`gulpID`-Feld vs. GULP-Kontakttyp**
- Der Freiberufler hat sowohl ein dediziertes `gulpID`-Datenbankfeld (Gruppe Kodierung) als auch die Möglichkeit, eine GULP-Kontaktmöglichkeit anzulegen. Zu klären: Ist das gewollte Redundanz (Legacy-Feld), oder soll das `gulpID`-Feld zugunsten des Kontakttyps entfernt werden?

### UX / Usability

**Wiedervorlage (`showAgain`)**

- Es fehlt eine Beschreibung des Workflows: Gibt es eine gefilterte Ansicht oder Liste aller Freiberufler mit gesetztem Wiedervorlage-Flag? Wie wird der Sachbearbeiter auf fällige Wiedervorlagen aufmerksam gemacht?

