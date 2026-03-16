# Spezifikation Formular "Partner"

Dieses Dokument beschreibt die fachliche und technische Spezifikation des Formulars "Partner" und dient
als Grundlage für die KI-gestützte Implementierung. Es richtet sich an ein KI-System, das auf Basis dieser
Beschreibung den Programmcode eigenständig erzeugen soll. Die Spezifikation ist daher so präzise wie möglich
gehalten und enthält neben der Beschreibung der Geschäftslogik auch Datenbankstrukturen, Validierungsregeln
und Verhaltensanforderungen an die Benutzeroberfläche.

Gemeinsam genutzte Stammdaten (Historientypen, Kontaktmöglichkeiten-Typen) sind in
[STAMMDATEN.md](STAMMDATEN.md) beschrieben.

## Anwendungsfall

Dieses Formular dient der Erfassung von Partnerunternehmen (z. B. Vermittlungsagenturen) und deren Stammdaten.
Zu jedem Partner kann eine typisierte Kontakthistorie gepflegt werden, in der die Sachbearbeiter relevante
Informationen erfassen (neueste Einträge zuerst). Außerdem zeigt das Formular alle dem Partner zugeordneten
Freiberufler in einer Übersichtsliste.

Partner und Freiberufler sind eigenständige Entitäten, die über das Feld `freelancer.partner_id` verknüpft
werden. Die Verwaltung dieser Zuordnung (Anlegen, Löschen) erfolgt ausschließlich über das Partner-Formular.
Im Freiberufler-Formular ist der zugeordnete Partner nur lesend sichtbar (als anklickbarer Link) und dort
nicht direkt veränderbar.

## Stammdaten

Partner werden in der Tabelle `partner` gespeichert. Die Felder sind nach fachlicher Zugehörigkeit
gruppiert – diese Gruppen spiegeln auch die Struktur des Formulars wider.

**Systemfelder** (werden im Formular nicht angezeigt):

| Feld                     | Datenbankspalte  | Datentyp       | Prüfungen                    | Hinweise                         |
|--------------------------|------------------|----------------|------------------------------|----------------------------------|
| `id`                     | `id`             | `BIGINT`       | PK, NOT NULL, AUTO_INCREMENT | —                                |
| `version`                | `db_version`     | `BIGINT`       | NOT NULL, default `0`        | Optimistic Locking               |
| `creationDate`           | `creation_date`  | `DATETIME`     | nullable                     | Zeitpunkt der Erfassung          |
| `creationUserID`         | `creation_user`  | `VARCHAR(255)` | nullable                     | Erfassender Sachbearbeiter       |
| `lastModificationDate`   | `changed_date`   | `DATETIME`     | nullable                     | Zeitpunkt der letzten Änderung   |
| `lastModificationUserID` | `changed_user`   | `VARCHAR(255)` | nullable                     | Zuletzt ändernder Sachbearbeiter |

**Gruppe: Adresse**

| Feld      | Datenbankspalte | Datentyp  | Länge | Prüfungen | Label für die UI    | Hinweise                                      |
|-----------|-----------------|-----------|-------|-----------|---------------------|-----------------------------------------------|
| `company` | `company`       | `VARCHAR` | 255   | nullable  | Firma               | Name des Partnerunternehmens (Hauptfeld)      |
| `name1`   | `name1`         | `VARCHAR` | 255   | nullable  | Name                | Name des Ansprechpartners                     |
| `name2`   | `name2`         | `VARCHAR` | 255   | nullable  | Vorname             | Vorname des Ansprechpartners                  |
| `street`  | `street`        | `VARCHAR` | 255   | nullable  | Strasse             |                                               |
| `country` | `country`       | `VARCHAR` | 255   | nullable  | Land                | Gruppiert mit PLZ/Ort unter „Land PLZ/Ort"    |
| `plz`     | `plz`           | `VARCHAR` | 255   | nullable  | PLZ                 | Gruppiert mit Land/Ort                        |
| `city`    | `city`          | `VARCHAR` | 255   | nullable  | Ort                 |                                               |

**Gruppe: Kontaktinformationen**

Wenn `contactforbidden` gesetzt ist, wird oberhalb des Formulars ein rotes Banner mit dem Text „Kontaktsperre"
angezeigt.

| Feld               | Datenbankspalte    | Datentyp | Länge | Prüfungen                 | Label für die UI | Hinweise                        |
|--------------------|--------------------|----------|-------|---------------------------|------------------|---------------------------------|
| `contactforbidden` | `contactforbidden` | `BIT`    | 1     | NOT NULL, default `false` | Kontaktsperre    | Rotes Banner bei gesetztem Flag |
| `showAgain`        | `show_again`       | `BIT`    | 1     | NOT NULL, default `false` | Wiedervorlage    |                                 |

In dieser Gruppe werden ebenfalls die Kontaktmöglichkeiten angezeigt.

**Gruppe: Kommentar**

| Feld       | Datenbankspalte | Datentyp   | Länge     | Prüfungen | Label für die UI | Hinweise              |
|------------|-----------------|------------|-----------|-----------|------------------|-----------------------|
| `comments` | `comments`      | `LONGTEXT` | unlimited | nullable  | Kommentar        | Allgemeiner Freitext  |

**Gruppe: Konditionen**

| Feld         | Datenbankspalte | Datentyp  | Länge | Prüfungen | Label für die UI | Hinweise                       |
|--------------|-----------------|-----------|-------|-----------|------------------|--------------------------------|
| `debitorNr`  | `debitor_nr`    | `VARCHAR` | 255   | nullable  | Debitor          | Debitorennummer (Buchhaltung)  |
| `kreditorNr` | `kreditor_nr`   | `VARCHAR` | 255   | nullable  | Kreditor         | Kreditorennummer (Buchhaltung) |

## Zugeordnete Projekte

Das Partner-Formular zeigt innerhalb einer eigenen Formular-Karte (volle Breite, `col-wide`) alle Projekte,
die diesem Partner zugeordnet sind (`project.partner_id = partner.id`).

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
im Neuanlage-Modus. Das Feld `partner_id` ist mit der ID des aktuellen Partners vorausgefüllt und im
Projekt-Formular nicht änderbar. Nach dem Speichern des neuen Projekts erscheint dieses automatisch
in der Projektliste des Partners.

## Zugeordnete Freiberufler

Das Partner-Formular zeigt innerhalb einer eigenen Formular-Karte (volle Breite, `col-wide`) alle
Freiberufler, die diesem Partner zugeordnet sind (`freelancer.partner_id = partner.id`). Die Karte
bietet drei Aktionen zur Verwaltung dieser Zuordnungen.

### Listendarstellung

Tabellarische Listenansicht. Jede Zeile enthält:

| Spalte        | Feld                 | Sortierbar | Hinweis                                                                               |
|---------------|----------------------|------------|---------------------------------------------------------------------------------------|
| Kodierung     | `code`               | ja         | Fachlich eindeutiger, anonymisierter Bezeichner; anklickbar → öffnet den Freiberufler |
| Name          | `name1`              | ja         |                                                                                       |
| Vorname       | `name2`              | ja         |                                                                                       |
| Firma         | `company`            | nein       |                                                                                       |
| Verfügbarkeit | `availabilityAsDate` | nein       |                                                                                       |
| Stundensatz   | `salaryLong`         | nein       |                                                                                       |
| Aktionen      | —                    | nein       | Löschen-Button je Zeile (siehe unten)                                                 |

Die Liste ist standardmäßig nach Name (`name1`) und Vorname (`name2`) aufsteigend sortiert.
Sortierbare Spalten können per Klick auf den Spaltenheader umgeschaltet werden (asc → desc → unsortiert).

**Leere Liste:** Ist noch kein Freiberufler zugeordnet, wird ein entsprechender Hinweistext
(„Keine Freiberufler zugeordnet") angezeigt.

### Aktion: Zuordnung löschen

Jede Zeile enthält einen Löschen-Button. Vor der Ausführung wird ein modaler Bestätigungsdialog
mit dem Text angezeigt:

> „Die Zuordnung des Freiberuflers [Kodierung / Name] zu diesem Partner wird aufgehoben. Sind Sie sicher?"

Der Dialog bietet die Optionen „Ja, aufheben" und „Abbrechen". Nach Bestätigung wird
`freelancer.partner_id` auf `NULL` gesetzt. Der Freiberufler selbst wird nicht gelöscht.
Die Liste aktualisiert sich anschließend automatisch.

### Aktion: Neuen Freiberufler anlegen

Oberhalb der Liste befindet sich ein Button „＋ Neuen Freiberufler anlegen". Er öffnet das
Freiberufler-Formular im Neuanlage-Modus. Das Feld `partner_id` ist in diesem Fall mit der ID
des aktuellen Partners vorausgefüllt und wird im Freiberufler-Formular als lesend angezeigt
(analog der regulären Darstellung). Nach dem Speichern des neuen Freiberuflers erscheint dieser
automatisch in der Zuordnungsliste des Partners.

### Aktion: Bestehenden Freiberufler zuordnen

Oberhalb der Liste befindet sich ein Eingabefeld mit dem Label „Kodierung" und einem Button
„Zuordnen". Der Sachbearbeiter gibt die Kodierung (`code`) eines bestehenden Freiberuflers ein
und bestätigt. Das System verhält sich wie folgt:

* **Kodierung gefunden, noch kein Partner zugeordnet (`partner_id IS NULL`):** `freelancer.partner_id`
  wird auf die ID des aktuellen Partners gesetzt. Die Liste aktualisiert sich automatisch.

* **Kodierung gefunden, aber bereits einem anderen Partner zugeordnet:** Es erscheint ein modaler
  Hinweisdialog mit dem Text:
  > „Dieser Freiberufler ist bereits dem Partner [Firmenname] zugeordnet. Soll die Zuordnung zu diesem
  > Partner übernommen werden?"

  Der Dialog bietet „Ja, übernehmen" und „Abbrechen". Bei Bestätigung wird `freelancer.partner_id`
  auf den aktuellen Partner umgesetzt.

* **Kodierung nicht gefunden:** Es erscheint eine Fehlermeldung unterhalb des Eingabefelds:
  „Kein Freiberufler mit dieser Kodierung gefunden."

Das Eingabefeld wird nach erfolgreicher Zuordnung geleert.

## Kontaktmöglichkeiten

Zu einem Partner können beliebig viele Kontaktmöglichkeiten hinzugefügt, bearbeitet und gelöscht werden.
Typen, Darstellungsregeln, Sortierreihenfolge sowie das modale Formular für Neuanlage/Bearbeitung
sind in [STAMMDATEN.md](STAMMDATEN.md) beschrieben.

Die Kontaktmöglichkeiten der Partner werden in der Tabelle `partner_contact` gespeichert:

| Feld                     | Datenbankspalte | Datentyp   | Länge | Prüfungen                                     | Hinweise                                                    |
|--------------------------|-----------------|------------|-------|-----------------------------------------------|-------------------------------------------------------------|
| `id`                     | `id`            | `BIGINT`   | —     | PK, NOT NULL, AUTO_INCREMENT                  |                                                             |
| `creationDate`           | `creation_date` | `DATETIME` | —     | nullable                                      | Zeitpunkt der Erfassung                                     |
| `creationUserID`         | `creation_user` | `VARCHAR`  | 255   | nullable                                      | Erfassender Sachbearbeiter                                  |
| `lastModificationDate`   | `changed_date`  | `DATETIME` | —     | nullable                                      | Zeitpunkt der letzten Änderung                              |
| `lastModificationUserID` | `changed_user`  | `VARCHAR`  | 255   | nullable                                      | Zuletzt ändernder Sachbearbeiter                            |
| `type`                   | `type`          | `VARCHAR`  | 255   | NOT NULL                                      | Typ-Enum: `EMAIL`, `WEB`, `XING`, `GULP`, `TELEFON`, `FAX` |
| `value`                  | `value`         | `VARCHAR`  | 255   | NOT NULL                                      | Der eigentliche Kontaktwert                                 |
| `partner_id`             | `partner_id`    | `BIGINT`   | —     | FK → partner(id), NOT NULL, ON DELETE CASCADE | Zugehöriger Partner                                         |

## Kontakthistorie

Zu jedem Partner kann eine typisierte Kontakthistorie gepflegt werden. Zu dieser Historie können jederzeit
Einträge hinzugefügt, gelöscht oder geändert werden. Für jeden Eintrag wird ein Audit-Log geführt analog
zu den Partner-Stammdaten.

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

Die Kontakthistorie der Partner wird in der Tabelle `partner_history` gespeichert:

| Feld                     | Datenbankspalte | Datentyp   | Länge     | Prüfungen                                     | Label für die UI                 | Hinweise                              |
|--------------------------|-----------------|------------|-----------|-----------------------------------------------|----------------------------------|---------------------------------------|
| `id`                     | `id`            | `BIGINT`   | —         | PK, NOT NULL, AUTO_INCREMENT                  | —                                |                                       |
| `creationDate`           | `creation_date` | `DATETIME` | —         | nullable                                      | Zeitpunkt der Erfassung          |                                       |
| `creationUserID`         | `creation_user` | `VARCHAR`  | 255       | nullable                                      | Erfassender Sachbearbeiter       |                                       |
| `lastModificationDate`   | `changed_date`  | `DATETIME` | —         | nullable                                      | Zeitpunkt der letzten Änderung   |                                       |
| `lastModificationUserID` | `changed_user`  | `VARCHAR`  | 255       | nullable                                      | Zuletzt ändernder Sachbearbeiter |                                       |
| `description`            | `description`   | `LONGTEXT` | unlimited | NOT NULL                                      | Text                             | Freitextfeld für den Historieneintrag |
| `type_id`                | `type_id`       | `BIGINT`   | —         | FK → historytype(id), NOT NULL                | Typ                              | Pflichtauswahl aus Combobox           |
| `partner_id`             | `partner_id`    | `BIGINT`   | —         | FK → partner(id), NOT NULL, ON DELETE CASCADE | —                                | Zugehöriger Partner                   |

## Navigation

Das Formular bietet generell die folgenden Navigationsmöglichkeiten durch alle bekannten Partner:

* Springe zum ersten Partner (kleinste ID)
* Springe zum vorherigen Partner (die vorherige bekannte ID vor der ID des aktuellen Partners)
* Springe zum nächsten Partner (die nächste bekannte ID nach der ID des aktuellen Partners)
* Springe zum letzten Partner (höchste ID)
* Springe zu einem Partner mit einer bestimmten ID

Zu jedem Partner werden neben der ID die Audit-Informationen angezeigt: Zeitpunkt der ersten Erfassung
sowie der Name des Sachbearbeiters, und der Zeitpunkt der letzten Änderung sowie der Name des Sachbearbeiters.

### Gemerktes Projekt

Das System merkt sich pro Sachbearbeiter das zuletzt im Projekte-Formular angezeigte Projekt
(server-seitig, persistent). Dieses „gemerkte Projekt" wird in der Toolbar des Partner-Formulars
angezeigt, sofern eines vorhanden ist (z. B.: „Gemerktes Projekt: 2026-042 – Java-Entwickler München").
Die Anzeige ist rein informativ; im Partner-Formular sind keine Aktionen in Bezug auf das gemerkte Projekt
möglich.

## Neu, Bearbeiten, Speichern und Löschen

**Initialzustand**: Beim Öffnen des Formulars wird der zuletzt angezeigte Partner geladen. Existiert dieser
nicht mehr oder gibt es keinen zuletzt angezeigten Partner, wird das Formular leer angezeigt.

Das Formular kennt keinen expliziten Modus. Die einzige Unterscheidung ist, ob für die aktuell angezeigten Daten
eine Datenbank-ID existiert (bestehender Partner) oder nicht (leeres Formular). Ein leeres Formular kann
sowohl für die Neuanlage als auch für eine QBE-Suche genutzt werden.

Folgende Aktionen werden zusätzlich zur Navigation angeboten:

* Erstelle neuen Partner (leert das Formular)
* Speichere die aktuellen Daten
* Lösche den aktuellen Partner inkl. aller verknüpften Informationen

**Löschen**: Vor dem Löschen prüft das System, ob dem Partner Projekte zugeordnet sind
(Einträge in `project` mit `partner_id = partner.id`).

- **Projektzuordnung vorhanden**: Das Löschen wird verhindert. Es wird eine Fehlermeldung in einem
  modalen Dialog angezeigt:
  > „Partner kann nicht gelöscht werden, da er Bezug zu Projekt [anklickbarer Link mit Projektnummer]
  > hat."

  Sind mehrere Projekte zugeordnet, werden alle als separate Links aufgelistet. Der Dialog bietet
  nur „Schließen". Das Löschen wird nicht ausgeführt. Die FK-Constraint auf `project.partner_id`
  ist als `ON DELETE RESTRICT` definiert.

- **Keine Projektzuordnung**: Es wird der reguläre Bestätigungsdialog angezeigt:
  > „Es wird der Partner und alle verknüpften Informationen gelöscht. Sind Sie sicher?"

  Der Dialog bietet „Ja, löschen" und „Abbrechen". Erst nach Bestätigung wird die Löschung ausgeführt.
  Anschließend wird ein leeres Formular angezeigt.

  Beim Löschen eines Partners werden **kaskadierend mitgelöscht** (DB-seitiges `ON DELETE CASCADE`):
  - Alle Kontaktmöglichkeiten (`partner_contact`)
  - Alle Einträge der Kontakthistorie (`partner_history`)

  > **Hinweis:** `partner_history`- und `partner_contact`-Einträge sind separate Spring Data JDBC
  > Aggregate Roots (kein Teil des `partner`-Aggregats). Das Mitlöschen erfolgt daher ausschließlich
  > über die DB-seitige `ON DELETE CASCADE`-Constraint auf dem jeweiligen Fremdschlüssel `partner_id`.

> **Hinweis:** Das Löschen eines Partners entfernt **nicht** die Freiberufler, die dem Partner zugeordnet sind.
> Stattdessen wird das Feld `freelancer.partner_id` für alle betroffenen Freiberufler auf `NULL` gesetzt
> (referentielle Integrität: `ON DELETE SET NULL`).

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

Das Formular bietet eine Query-By-Example Suchfunktion. Ist das Formular leer (kein Partner geladen, keine
Datenbank-ID vorhanden), kann der Sachbearbeiter die Felder mit Suchausdrücken befüllen und über den Suchbutton
eine QBE-Suche starten. Alternativ können die eingetragenen Daten über die reguläre Speichern-Aktion als neuer
Partner gespeichert werden. Eine explizite Modustrennung zwischen Suche und Neuanlage gibt es nicht.

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
Zu jedem Partner wird ein Link zur Detailansicht angezeigt.

**Leeres Suchergebnis**: Liefert die Suche keinen Treffer, wird anstelle der Tabelle ein entsprechender Hinweis
angezeigt. Dieser enthält einen Link, der den Sachbearbeiter zurück zum ausgefüllten QBE-Formular führt, damit
die Suchkriterien angepasst werden können.

**Anzahl der Treffer und Nachladen**: Oberhalb der Ergebnistabelle wird die Gesamtanzahl der gefundenen Treffer
angezeigt. Initial werden maximal 20 Treffer geladen. Weitere Treffer werden automatisch nachgeladen, sobald der
Sachbearbeiter ans Ende der Liste scrollt (Infinite Scrolling).

Die einzelnen QBE-Felder werden durch einen `AND`-Ausdruck miteinander kombiniert. Innerhalb eines Feldes wird
eine LIKE-Suche im Fall von Zeichenfolgen gestartet; aus dem Suchausdruck `HAM` wird somit die SQL-Abfrage
`LIKE '%HAM%'`.

## Offene Punkte

### Datenbankintegrität

### UX / Usability

**Wiedervorlage (`showAgain`)**

- Analog zum Freiberufler-Formular: Ein reines Flag ohne Datum ist im Arbeitsalltag kaum nutzbar.
  Das Feld sollte um ein optionales Wiedervorlagedatum (`showAgainDate`, `DATETIME`, nullable) ergänzt werden.
- Es fehlt eine Beschreibung des Workflows: Gibt es eine gefilterte Ansicht oder Liste aller Partner
  mit gesetztem Wiedervorlage-Flag?
