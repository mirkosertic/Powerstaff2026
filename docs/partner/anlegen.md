# Partner anlegen

## Neuen Partner erstellen

1. Klicken Sie in der Navigation auf **Partner**
2. Klicken Sie in der Toolbar auf **＋ Neu**

![Partner Suchmaske](../screenshots/partner-suchmaske.png)

---

## Felder ausfüllen

### Adresse

![Formular-Karte Adresse](../screenshots/components/partner-fcard-adresse.png)

| Feld       | Pflicht | Beschreibung                       |
|------------|---------|------------------------------------|
| **Firma**  | **Ja**  | Firmenname der Vermittlungsagentur |
| **Name 1** | Nein    | Nachname Ansprechpartner           |
| **Name 2** | Nein    | Vorname Ansprechpartner            |
| **Straße** | Nein    | Straße und Hausnummer              |
| **Land**   | Nein    | Länderkürzel, max. 3 Zeichen       |
| **PLZ**    | Nein    | Postleitzahl, max. 5 Zeichen       |
| **Ort**    | Nein    | Ort                                |

### Konditionen

![Formular-Karte Konditionen](../screenshots/components/partner-fcard-konditionen.png)

| Feld                 | Beschreibung                                            |
|----------------------|---------------------------------------------------------|
| **Debitor-Nr**       | Interne Debitorennummer                                 |
| **Kreditor-Nr**      | Interne Kreditorennummer                                |
| **🚫 Kontaktsperre** | Wenn aktiv: roter Banner, Kontaktaufnahme nicht erlaubt |
| **🔔 Wiedervorlage** | Markiert den Partner zur erneuten Kontaktaufnahme       |

Ist die Kontaktsperre aktiv, erscheint oben im Formular ein roter Warnbanner:

![Banner Kontaktsperre](../screenshots/components/partner-banner-kontaktsperre.png)

### Kommentar

Freies Textfeld für interne Notizen zum Partner.

---

## Speichern

Klicken Sie auf **💾 Speichern**.

---

## Kontaktmöglichkeiten verwalten

Im Abschnitt **Kontaktmöglichkeiten** können Sie Kontaktdaten hinterlegen:

![Formular-Karte Kontaktmöglichkeiten](../screenshots/components/partner-fcard-kontaktmoeglichkeiten.png)

Ein einzelner Kontakteintrag:

![Kontakt-Eintrag](../screenshots/components/partner-contact-item.png)

Klicken Sie auf **+ Hinzufügen**, um den Dialog zu öffnen:

![Dialog Kontakt hinzufügen](../screenshots/components/partner-modal-kontakt-hinzufuegen.png)

---

## Zugeordnete Freiberufler

Bei bestehenden Partnern zeigt der Abschnitt **Zugeordnete Freiberufler** eine Liste aller
Freiberufler, die diesem Partner zugeordnet sind. Die Zuordnung erfolgt im Freiberufler-Formular
(Feld **Partner** in der Adresse).

![Formular-Karte Zugeordnete Freiberufler](../screenshots/components/partner-fcard-freiberufler.png)

---

## Partner löschen

1. Öffnen Sie den gewünschten Partner
2. Klicken Sie auf **🗑 Löschen** in der Toolbar
3. Bestätigen Sie den Dialog mit **Löschen**

> **Hinweis:** Ein Partner kann nicht gelöscht werden, wenn noch Projekte zugeordnet sind.
