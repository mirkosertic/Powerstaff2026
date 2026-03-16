# Spezifikation Stammdaten (gemeinsam genutzte Tabellen)

Dieses Dokument beschreibt Datenbanktabellen und Enumerationen, die von mehreren Modulen der Applikation
gemeinsam genutzt werden. Änderungen hier wirken sich auf alle verweisenden Module aus.

Verweisende Spezifikationen:
- [FREIBERUFLER.md](FREIBERUFLER.md) – Kontakthistorie, Kontaktmöglichkeiten
- [PARTNER.md](PARTNER.md) – Kontakthistorie, Kontaktmöglichkeiten
- [KUNDEN.md](KUNDEN.md) – Kontakthistorie, Kontaktmöglichkeiten
- [PROJEKTE.md](PROJEKTE.md) – Projektpositionen (`project_position_status`)

---

## Historientypen (`historytype`)

Die Tabelle `historytype` typisiert die Einträge der Kontakthistorie. Sie wird von
`freelancer_history.type_id`, `partner_history.type_id` sowie `kunde_history.type_id` referenziert.

| Feld          | Datenbankspalte | Datentyp  | Länge | Prüfungen                    | Hinweise                         |
|---------------|-----------------|-----------|-------|------------------------------|----------------------------------|
| `id`          | `id`            | `BIGINT`  | —     | PK, NOT NULL, AUTO_INCREMENT |                                  |
| `description` | `description`   | `VARCHAR` | 255   | NOT NULL                     | Anzeigename des Typs             |

Die Verwaltung der Historientypen erfolgt im Administrationsbereich der Applikation.
Im Formular wird die Liste als Pflicht-Combobox angeboten; der Sachbearbeiter muss exakt
einen Typ auswählen.

---

## Projektpositions-Status (`project_position_status`)

Die Tabelle `project_position_status` definiert die möglichen Zustände einer Projektposition (Zuordnung
Freiberufler ↔ Projekt). Sie wird von `project_position.status_id` referenziert.

| Feld          | Datenbankspalte | Datentyp  | Länge | Prüfungen                    | Hinweise                                                                                            |
|---------------|-----------------|-----------|-------|------------------------------|-----------------------------------------------------------------------------------------------------|
| `id`          | `id`            | `BIGINT`  | —     | PK, NOT NULL, AUTO_INCREMENT |                                                                                                     |
| `description` | `description`   | `VARCHAR` | 255   | NOT NULL                     | Anzeigename des Status (z. B. „Vorgeschlagen", „Im Gespräch", „Abgesagt")                           |
| `color`       | `color`         | `VARCHAR` | 50    | NOT NULL                     | Hintergrundfarbe des Badges als CSS-Farbwert (z. B. `#d1fae5`); heller Ton empfohlen                |
| `color_text`  | `color_text`    | `VARCHAR` | 50    | NOT NULL                     | Textfarbe des Badges als CSS-Farbwert (z. B. `#065f46`); dunkler Ton desselben Farbtons für WCAG AA |

Neue Einträge und Änderungen an bestehenden Einträgen erfolgen im Administrationsbereich der Applikation.
Das Löschen von Einträgen ist ausschließlich auf Datenbankebene durch einen Datenbankadministrator möglich;
es gibt keine Löschfunktion in der Applikation. Im Formular wird die Liste als Pflicht-Combobox angeboten.
`color` (Hintergrundfarbe) und `color_text` (Textfarbe) werden in der Projektpositions-Liste als farbiges
Badge dargestellt (`.badge-dyn` mit `style="background:<color>; color:<color_text>"`). Im Administrationsbereich
werden beide Werte als CSS-Farbcodes gepflegt. Konvention: heller Hintergrundton + dunkler Textton desselben
Farbtons, um WCAG AA (Kontrastverhältnis ≥ 4,5:1) sicherzustellen.

---

## Kontaktmöglichkeiten – Typen und Darstellungsregeln

Die folgende Typliste ist für alle Module abschließend. Sie gilt für `freelancer_contact.type`,
`partner_contact.type` sowie `kunde_contact.type`.

| Enum-Wert | Anzeige-Label | Darstellung des Wertes  | Verlinkung | Link-Ziel                                   |
|-----------|---------------|-------------------------|------------|---------------------------------------------|
| `EMAIL`   | eMail         | E-Mail-Adresse als Text | ja         | `mailto:<wert>`                             |
| `WEB`     | Web           | URL als Text            | ja         | `<wert>` (öffnet in neuem Tab)              |
| `XING`    | Xing          | Profilname als Text     | ja         | `https://www.xing.com/profile/<wert>`       |
| `GULP`    | Gulp          | Profil-ID als Text      | ja         | `https://www.gulp.de/gulp2/g/profil/<wert>` |
| `TELEFON` | Telefon       | Telefonnummer als Text  | ja         | `tel:<wert>`                                |
| `FAX`     | Fax           | Faxnummer als Text      | nein       | —                                           |

**Anzeigereihenfolge:** eMail → Web → Xing → Gulp → Telefon → Fax.
Innerhalb desselben Typs erfolgt die Sortierung nach Erfassungsdatum aufsteigend (`id ASC`),
sodass ältere Einträge zuerst erscheinen.

Neuanlage und Bearbeitung einer Kontaktmöglichkeit erfolgen jeweils über ein modales Formular.
Das Löschen wird über einen Löschen-Button je Eintrag ausgelöst; vor der Ausführung wird ein
modaler Bestätigungsdialog mit dem Text „Sind Sie sicher?" angezeigt.
