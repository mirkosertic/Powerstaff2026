# Spezifikation Formular "Kunden"

Dieses Dokument beschreibt die fachliche und technische Spezifikation des Formulars "Kunden" und dient
als Grundlage für die KI-gestützte Implementierung. Es richtet sich an ein KI-System, das auf Basis dieser
Beschreibung den Programmcode eigenständig erzeugen soll. Die Spezifikation ist daher so präzise wie möglich
gehalten und enthält neben der Beschreibung der Geschäftslogik auch Datenbankstrukturen, Validierungsregeln
und Verhaltensanforderungen an die Benutzeroberfläche.

Gemeinsam genutzte Stammdaten (Historientypen, Kontaktmöglichkeiten-Typen) sind in
[STAMMDATEN.md](STAMMDATEN.md) beschrieben.

## Anwendungsfall

Dieses Formular dient der Erfassung von Kundenunternehmen und deren Stammdaten. Zu jedem Kunden kann eine
typisierte Kontakthistorie gepflegt werden, in der die Sachbearbeiter relevante Informationen erfassen
(neueste Einträge zuerst).

Kunden haben keine direkte Beziehung zu Freiberuflern. Die Kundenverwaltung ist eine eigenständige Entität.

## Stammdaten

Kunden werden in der Tabelle `kunde` gespeichert. Die Felder sind nach fachlicher Zugehörigkeit
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

| Feld      | Datenbankspalte | Datentyp  | Länge | Prüfungen | Label für die UI | Hinweise                                   |
|-----------|-----------------|-----------|-------|-----------|------------------|--------------------------------------------|
| `company` | `company`       | `VARCHAR` | 255   | nullable  | Firma            | Name des Kundenunternehmens (Hauptfeld)    |
| `name1`   | `name1`         | `VARCHAR` | 255   | nullable  | Name             | Name des Ansprechpartners                  |
| `name2`   | `name2`         | `VARCHAR` | 255   | nullable  | Vorname          | Vorname des Ansprechpartners               |
| `street`  | `street`        | `VARCHAR` | 255   | nullable  | Strasse          |                                            |
| `country` | `country`       | `VARCHAR` | 255   | nullable  | Land             | Gruppiert mit PLZ/Ort unter „Land PLZ/Ort" |
| `plz`     | `plz`           | `VARCHAR` | 255   | nullable  | PLZ              | Gruppiert mit Land/Ort                     |
| `city`    | `city`          | `VARCHAR` | 255   | nullable  | Ort              |                                            |

**Gruppe: Kontaktinformationen**

Wenn `contactforbidden` gesetzt ist, wird oberhalb des Formulars ein rotes Banner mit dem Text „Kontaktsperre"
angezeigt.

| Feld               | Datenbankspalte    | Datentyp | Länge | Prüfungen                 | Label für die UI | Hinweise                        |
|--------------------|--------------------|----------|-------|---------------------------|------------------|---------------------------------|
| `contactforbidden` | `contactforbidden` | `BIT`    | 1     | NOT NULL, default `false` | Kontaktsperre    | Rotes Banner bei gesetztem Flag |
| `showAgain`        | `show_again`       | `BIT`    | 1     | NOT NULL, default `false` | Wiedervorlage    |                                 |

In dieser Gruppe werden ebenfalls die Kontaktmöglichkeiten angezeigt.

**Gruppe: Kommentar**

| Feld       | Datenbankspalte | Datentyp   | Länge     | Prüfungen | Label für die UI | Hinweise             |
|------------|-----------------|------------|-----------|-----------|------------------|----------------------|
| `comments` | `comments`      | `LONGTEXT` | unlimited | nullable  | Kommentar        | Allgemeiner Freitext |

**Gruppe: Konditionen**

| Feld         | Datenbankspalte | Datentyp  | Länge | Prüfungen | Label für die UI | Hinweise                       |
|--------------|-----------------|-----------|-------|-----------|------------------|--------------------------------|
| `debitorNr`  | `debitor_nr`    | `VARCHAR` | 255   | nullable  | Debitor          | Debitorennummer (Buchhaltung)  |
| `kreditorNr` | `kreditor_nr`   | `VARCHAR` | 255   | nullable  | Kreditor         | Kreditorennummer (Buchhaltung) |

## Zugeordnete Projekte

Das Kunden-Formular zeigt innerhalb einer eigenen Formular-Karte (volle Breite, `col-wide`) alle Projekte,
die diesem Kunden zugeordnet sind (`project.customer_id = kunde.id`).

### Listendarstellung

Tabellarische Listenansicht. Jede Zeile enthält:

| Spalte           | Feld               | Sortierbar | Hinweis                                             |
|------------------|--------------------|------------|-----------------------------------------------------|
| Projektnummer    | `projectNumber`    | ja         | Anklickbar → öffnet das Projekt im Projekt-Formular |
| Kurzbeschreibung | `descriptionShort` | nein       |                                                     |
| Einsatzort       | `workplace`        | nein       |                                                     |
| Startdatum       | `startDate`        | ja         |                                                     |
| Status           | `status`           | nein       | Wird als lesbarer Statustext dargestellt            |

Die Liste ist standardmäßig nach Startdatum (`startDate`) absteigend sortiert (neueste Projekte zuerst).

**Leere Liste:** Sind noch keine Projekte zugeordnet, wird ein entsprechender Hinweistext
(„Keine Projekte vorhanden") angezeigt.

### Aktion: Neues Projekt erfassen

Oberhalb der Liste befindet sich ein Button „＋ Neues Projekt erfassen". Er öffnet das Projekt-Formular
im Neuanlage-Modus. Das Feld `customer_id` ist mit der ID des aktuellen Kunden vorausgefüllt und im
Projekt-Formular nicht änderbar. Nach dem Speichern des neuen Projekts erscheint dieses automatisch
in der Projektliste des Kunden.

## Kontaktmöglichkeiten

Zu einem Kunden können beliebig viele Kontaktmöglichkeiten hinzugefügt, bearbeitet und gelöscht werden.
Typen, Darstellungsregeln, Sortierreihenfolge sowie das modale Formular für Neuanlage/Bearbeitung
sind in [STAMMDATEN.md](STAMMDATEN.md) beschrieben.

### Speicherverhalten

Änderungen an Kontaktmöglichkeiten (Hinzufügen, Bearbeiten, Löschen) werden **ausschließlich beim
Drücken des Speichern-Buttons** persistent gespeichert – gemeinsam mit den Kunden-Stammdaten in
einer einzigen Transaktion. Bis dahin werden die Änderungen nur im Client-State (JavaScript) gehalten
und in der UI dargestellt, ohne die Datenbank zu berühren.

Dies gilt sowohl für bestehende Kunden (Bearbeitung) als auch für neue Kunden (Neuanlage). Bei einem
neuen Kunden können daher Kontaktmöglichkeiten bereits vor dem ersten Speichern erfasst werden; sie
werden beim Speichern des Kunden zusammen mit den Stammdaten angelegt.

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
1. Alle existierenden `kunde_contact`-Einträge mit `kunde_id = :id` werden geladen.
2. Einträge, deren `id` nicht im gesendeten Array enthalten ist → DELETE.
3. Einträge mit `id != null` → UPDATE (type, value).
4. Einträge mit `id == null` → INSERT.

Die Kontaktmöglichkeiten der Kunden werden in der Tabelle `kunde_contact` gespeichert:

| Feld                     | Datenbankspalte | Datentyp   | Länge | Prüfungen                                   | Hinweise                                                    |
|--------------------------|-----------------|------------|-------|---------------------------------------------|-------------------------------------------------------------|
| `id`                     | `id`            | `BIGINT`   | —     | PK, NOT NULL, AUTO_INCREMENT                |                                                             |
| `creationDate`           | `creation_date` | `DATETIME` | —     | nullable                                    | Zeitpunkt der Erfassung                                     |
| `creationUserID`         | `creation_user` | `VARCHAR`  | 255   | nullable                                    | Erfassender Sachbearbeiter                                  |
| `lastModificationDate`   | `changed_date`  | `DATETIME` | —     | nullable                                    | Zeitpunkt der letzten Änderung                              |
| `lastModificationUserID` | `changed_user`  | `VARCHAR`  | 255   | nullable                                    | Zuletzt ändernder Sachbearbeiter                            |
| `type`                   | `type`          | `VARCHAR`  | 255   | NOT NULL                                    | Typ-Enum: `EMAIL`, `WEB`, `XING`, `GULP`, `TELEFON`, `FAX` |
| `value`                  | `value`         | `VARCHAR`  | 255   | NOT NULL                                    | Der eigentliche Kontaktwert                                 |
| `kunde_id`               | `kunde_id`      | `BIGINT`   | —     | FK → kunde(id), NOT NULL, ON DELETE CASCADE | Zugehöriger Kunde                                           |

## Kontakthistorie

Zu jedem Kunden kann eine typisierte Kontakthistorie gepflegt werden. Zu dieser Historie können jederzeit
Einträge hinzugefügt, gelöscht oder geändert werden. Für jeden Eintrag wird ein Audit-Log geführt analog
zu den Kunden-Stammdaten.

Die Neuanlage und Bearbeitung eines Eintrags erfolgt jeweils über ein modales Formular. Das Löschen wird über
einen Löschen-Button je Eintrag ausgelöst; vor der Ausführung wird ein modaler Bestätigungsdialog mit dem Text
„Sind Sie sicher?" angezeigt.

Die Einträge werden absteigend nach Erfassungsdatum sortiert angezeigt (neueste Einträge zuerst).

Jeder Eintrag besteht aus einem Pflicht-Typ (aus der Tabelle `historytype`, siehe [STAMMDATEN.md](STAMMDATEN.md))
sowie einem Freitextfeld, das beliebig viele Zeilen enthalten kann. Jeder Eintrag wird in der Listenansicht
mit folgenden Informationen angezeigt:

* **Typ** (aus `historytype`)
* **Erfasst am** (`creationDate`) und **von** (`creationUserID`)
* **Zuletzt geändert am** (`lastModificationDate`) und **von** (`lastModificationUserID`) – nur wenn abweichend von der Erfassung
* **Text** (`description`) – vollständig, da mehrzeilige Einträge möglich sind

### Speicherverhalten

Identisch zu den Kontaktmöglichkeiten: Änderungen an Historieneinträgen (Hinzufügen, Bearbeiten, Löschen)
werden **ausschließlich beim Drücken des Speichern-Buttons** persistent gespeichert – in derselben
Transaktion wie die Kunden-Stammdaten. Bis dahin existieren die Änderungen nur im Client-State.

Dies gilt auch für neue Kunden: Historieneinträge können bereits vor dem ersten Speichern erfasst werden.

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

Die Kontakthistorie der Kunden wird in der Tabelle `kunde_history` gespeichert:

| Feld                     | Datenbankspalte | Datentyp   | Länge     | Prüfungen                                   | Label für die UI                 | Hinweise                              |
|--------------------------|-----------------|------------|-----------|---------------------------------------------|----------------------------------|---------------------------------------|
| `id`                     | `id`            | `BIGINT`   | —         | PK, NOT NULL, AUTO_INCREMENT                | —                                |                                       |
| `creationDate`           | `creation_date` | `DATETIME` | —         | nullable                                    | Zeitpunkt der Erfassung          |                                       |
| `creationUserID`         | `creation_user` | `VARCHAR`  | 255       | nullable                                    | Erfassender Sachbearbeiter       |                                       |
| `lastModificationDate`   | `changed_date`  | `DATETIME` | —         | nullable                                    | Zeitpunkt der letzten Änderung   |                                       |
| `lastModificationUserID` | `changed_user`  | `VARCHAR`  | 255       | nullable                                    | Zuletzt ändernder Sachbearbeiter |                                       |
| `description`            | `description`   | `LONGTEXT` | unlimited | NOT NULL                                    | Text                             | Freitextfeld für den Historieneintrag |
| `type_id`                | `type_id`       | `BIGINT`   | —         | FK → historytype(id), NOT NULL              | Typ                              | Pflichtauswahl aus Combobox           |
| `kunde_id`               | `kunde_id`      | `BIGINT`   | —         | FK → kunde(id), NOT NULL, ON DELETE CASCADE | —                                | Zugehöriger Kunde                     |

## Navigation

Das Formular bietet generell die folgenden Navigationsmöglichkeiten durch alle bekannten Kunden:

* Springe zum ersten Kunden (kleinste ID)
* Springe zum vorherigen Kunden (die vorherige bekannte ID vor der ID des aktuellen Kunden)
* Springe zum nächsten Kunden (die nächste bekannte ID nach der ID des aktuellen Kunden)
* Springe zum letzten Kunden (höchste ID)
* Springe zu einem Kunden mit einer bestimmten ID

Zu jedem Kunden werden neben der ID die Audit-Informationen angezeigt: Zeitpunkt der ersten Erfassung
sowie der Name des Sachbearbeiters, und der Zeitpunkt der letzten Änderung sowie der Name des Sachbearbeiters.

### Gemerktes Projekt

Das System merkt sich pro Sachbearbeiter das zuletzt im Projekte-Formular angezeigte Projekt
(server-seitig, persistent). Dieses „gemerkte Projekt" wird in der Toolbar des Kunden-Formulars
angezeigt, sofern eines vorhanden ist (z. B.: „Gemerktes Projekt: 2026-042 – Java-Entwickler München").
Die Anzeige ist rein informativ; im Kunden-Formular sind keine Aktionen in Bezug auf das gemerkte Projekt
möglich.

## Neu, Bearbeiten, Speichern und Löschen

**Initialzustand**: Beim Öffnen des Formulars wird der zuletzt angezeigte Kunde geladen. Existiert dieser
nicht mehr oder gibt es keinen zuletzt angezeigten Kunden, wird das Formular leer angezeigt.

Das Formular kennt keinen expliziten Modus. Die einzige Unterscheidung ist, ob für die aktuell angezeigten Daten
eine Datenbank-ID existiert (bestehender Kunde) oder nicht (leeres Formular). Ein leeres Formular kann
sowohl für die Neuanlage als auch für eine QBE-Suche genutzt werden.

**Unified Save:** Sämtliche Änderungen am Formular – Stammdaten, Kontaktmöglichkeiten und Historieneinträge –
werden **ausschließlich beim Drücken des Speichern-Buttons** in einer einzigen Transaktion persistiert.
Kein AJAX-Endpunkt für Kontakte oder Historieneinträge schreibt direkt in die Datenbank; alle Mutationen
werden client-seitig im JavaScript-State gehalten und erst mit dem Speichern-Submit an den Server übertragen.
Dies gilt einheitlich für neue und bestehende Kunden.

Folgende Aktionen werden zusätzlich zur Navigation angeboten:

* Erstelle neuen Kunden (leert das Formular)
* Speichere die aktuellen Daten (Stammdaten + Kontakte + Historieneinträge in einer Transaktion)
* Lösche den aktuellen Kunden inkl. aller verknüpften Informationen

**Löschen**: Vor dem Löschen prüft das System, ob dem Kunden Projekte zugeordnet sind
(Einträge in `project` mit `customer_id = kunde.id`).

- **Projektzuordnung vorhanden**: Das Löschen wird verhindert. Es wird eine Fehlermeldung in einem
  modalen Dialog angezeigt:
  > „Kunde kann nicht gelöscht werden, da er Bezug zu Projekt [anklickbarer Link mit Projektnummer]
  > hat."

  Sind mehrere Projekte zugeordnet, werden alle als separate Links aufgelistet. Der Dialog bietet
  nur „Schließen". Das Löschen wird nicht ausgeführt. Die FK-Constraint auf `project.customer_id`
  ist als `ON DELETE RESTRICT` definiert.

- **Keine Projektzuordnung**: Es wird der reguläre Bestätigungsdialog angezeigt:
  > „Es wird der Kunde und alle verknüpften Informationen gelöscht. Sind Sie sicher?"

  Der Dialog bietet „Ja, löschen" und „Abbrechen". Erst nach Bestätigung wird die Löschung ausgeführt.
  Anschließend wird ein leeres Formular angezeigt.

  Beim Löschen eines Kunden werden **kaskadierend mitgelöscht** (DB-seitiges `ON DELETE CASCADE`):
  - Alle Kontaktmöglichkeiten (`kunde_contact`)
  - Alle Einträge der Kontakthistorie (`kunde_history`)

  > **Hinweis:** `kunde_history`- und `kunde_contact`-Einträge sind separate Spring Data JDBC
  > Aggregate Roots (kein Teil des `kunde`-Aggregats). Das Mitlöschen erfolgt daher ausschließlich
  > über die DB-seitige `ON DELETE CASCADE`-Constraint auf dem jeweiligen Fremdschlüssel `kunde_id`.

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

## Suche

Das Formular bietet eine Query-By-Example Suchfunktion. Ist das Formular leer (kein Kunde geladen, keine
Datenbank-ID vorhanden), kann der Sachbearbeiter die Felder mit Suchausdrücken befüllen und über den Suchbutton
eine QBE-Suche starten. Alternativ können die eingetragenen Daten über die reguläre Speichern-Aktion als neuer
Kunde gespeichert werden. Eine explizite Modustrennung zwischen Suche und Neuanlage gibt es nicht.

Auf folgenden Feldern kann mit QBE gesucht werden:

* `company`
* `name1`
* `name2`
* `street`
* `country`
* `plz`
* `city`
* `comments`
* `kreditorNr`
* `debitorNr`

Das Suchergebnis wird in einer Tabelle mit den folgenden Spalten angezeigt:

| Spalte  | Feld      | Sortierbar |
|---------|-----------|------------|
| Firma   | `company` | ja         |
| Name    | `name1`   | ja         |
| Vorname | `name2`   | ja         |
| Ort     | `city`    | nein       |

Die Ergebnistabelle ist standardmäßig nach Firma (`company`) aufsteigend sortiert. Der Sachbearbeiter kann die
Sortierung durch Klick auf eine sortierbare Spaltenüberschrift ändern; ein erneuter Klick kehrt die Reihenfolge um.
Zu jedem Kunden wird ein Link zur Detailansicht angezeigt.

**Leeres Suchergebnis**: Liefert die Suche keinen Treffer, wird anstelle der Tabelle ein entsprechender Hinweis
angezeigt. Dieser enthält einen Link, der den Sachbearbeiter zurück zum ausgefüllten QBE-Formular führt, damit
die Suchkriterien angepasst werden können.

**Anzahl der Treffer und Nachladen**: Oberhalb der Ergebnistabelle wird die Gesamtanzahl der gefundenen Treffer
angezeigt. Initial werden maximal 20 Treffer geladen. Weitere Treffer werden automatisch nachgeladen, sobald der
Sachbearbeiter ans Ende der Liste scrollt (Infinite Scrolling).

Die einzelnen QBE-Felder werden durch einen `AND`-Ausdruck miteinander kombiniert. Innerhalb eines Feldes wird
eine LIKE-Suche im Fall von Zeichenfolgen gestartet; aus dem Suchausdruck `HAM` wird somit die SQL-Abfrage
`LIKE '%HAM%'`.

## Technische Hinweise zur Implementierung

### Speichern-Endpunkt

`POST /kunde/save` empfängt neben den Stammdaten-Feldern zwei zusätzliche Formularfelder:

* `contactsJson` – JSON-Array der aktuellen Kontaktmöglichkeiten (siehe oben)
* `historyJson` – JSON-Array der aktuellen Historieneinträge (siehe oben)

Der Controller deserialisiert beide Arrays und führt die Replace-Logik in einer Transaktion mit dem
Kunden-Save durch.

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

### UX / Usability

**Wiedervorlage (`showAgain`)**

- Analog zu den anderen Modulen: Ein reines Flag ohne Datum ist im Arbeitsalltag kaum nutzbar.
  Das Feld sollte um ein optionales Wiedervorlagedatum (`showAgainDate`, `DATETIME`, nullable) ergänzt werden.
- Es fehlt eine Beschreibung des Workflows: Gibt es eine gefilterte Ansicht oder Liste aller Kunden
  mit gesetztem Wiedervorlage-Flag?
