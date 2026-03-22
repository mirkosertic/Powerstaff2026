# Gefundene Fehler

Die folgende Liste beschreibt die gefundenen Fehler aus den manuellen Tests. Jeder Befund soll einzeln korrigiert und via MAven build überprüft. Im Anschlus darf der EIntrag in der Liste als erledigt markiert werden, und der Change via Git in die Versionskontrolle aufgenommen werden.

- [x] Die Anzeige des gemerkten Projektes ist auf der Freiberufler-Seite redundant. Es wird sowohl oberhalb des Formulars
  als auch in der Nav-Bar angezeigt. Auf der Freiberufler-Seite soll nur die Navbar für die Aanzeige des gemerkten Projektes
  verwendet werden, genau wie in den andern Formularen. Der Button "Freiberufler dem gemerkten Projekt" zuordnen
  soll rechts nebenm dem generktem Projekt in der Navbar angezeit werden.
- [x] Das Feld PLZ ist noch immer zu groß. Es muss nur platz für max. 5 Zeichen haben, und soll deshalb für
  alle Formulare angepasstt werden.
- [x] Auf der Freiberufler-Seite (und vermutlich auch allen anderen) kann ein Kontakthistorie-Eintrag nicht editiert werden. Es erscheint der Javascript-Fehler "Uncaught ReferenceError: editHistory is not defined
  onclick http://localhost:8080/freelancer/1:1
  " in der Browserkonsole.
- [x] Für Partner kann im Formular keine Kontakthistorie gepflegt werden. Das ist falsch und muss analog der Freiberufler-Seite umgesetzt werden, inkl. der Korrekturen aus dieser Fehlerliste.
- [x] Beim hinzufügen einer Kontakthistorie zu einem Projekt passiert nichts. Ich würde erwarten, dass der Historien-Eintrag im UI angezteigt wird und analog dem Unified-Save Ansatz der anderen Formulare funktioniert.
- [x] Im Administrationsbereich fehlen die Links zu den Historientypen, dem Positionsstatus und den Tags in der Navigation. Ich würde erwarten, dass diese Links in der Toolbar / Navbar für den Admin-Bereich angezeigt werde.
- [x] Die Elemente in der Toolbar sollen sich an der Breite des Haubtformulars orientieren, do dass die Fluchtlinlien auf der linken und der rechten Seite passen. Gerade auf Wide-Screen-Displays fürt das aktuelle Layout zu sehr vielen unnötigen Mousebewerungen.
- [x] Der Löschen Button neben einem Chat-Eintrag und der Löschen Button in der Toolbar haben keine Funktion. Beide sollen den gleichen Warnhinweis anzeigen, und bei Bestätigung den Chat löschen. Im Fal von Löschen soll der älteste Chat
  des Sachbearbeiters angezeigt werden, falls es keinen Chat mehr gibt ein neuer Chat, der dann gespeichert werden kann.
- [x] Ich möchte generell keine Inline-Styles in den Thymeleaf-Templates. Alles muss über das Designsystem erledigt werden. Bitte sorge dafür, dass diese Regel nachhaltig angewendet wird, das ist jetz schon das zwite Mal, dass ich darauf aufmerksam machen muss!
- [x] Prüfe, ob wir alle Änderungen vonm oben Unit-Tests oder IT-Tests umgesetzt wurden, falls notwendig.
- [x] Nach dem Löschen eines Chat-Eintrages soll auf den jüngsten Chat-Eintrag gewechselt werden.
- [x] Der CQRS-Schush via apiFetch() ist falsch umgesetzt. In den Formularen steht im _cqrs Feld ein Hash des CQRS-Tokens, im Cookie der Klartext. Der X-XSRF-TOKEN Header überschreibt den Wert des _cqrs Feldes, weshalb immer ein CQRS Fehler erzeugt wird. Vermutlich wird die apiFetch()
  Methode so gar nicht benötigt. Bitte überprüfe dass, und falls das so stimmt, soll überall nur fetch() verwendet werden, und die SWARCHITEKTUR.md Beschreibung angepasst werden. Ich habe zu Testzwecken das Setzen des Headers in apiFetch() auskommentiert, und damit
  funktioniert der CQRS-Schutz wie gewünscht.
- [x] Die Projektnummer bzw. das Projekt in dem "Gemerkten Projekt" Fragment soll ein Link auf das Projekt sein. Falls das aktuelle Formular ungespeicherte Änderungen hat, soll ein Warnhinweis vor der Navigation angezeigt werden. Diese Änderung soll für alle Formulare angepasst werden.
- [x] Das PLZ Feld ist jetzt überall schmaler, das Ort-Feld rechts daneben nimmt allerdings nicht den neu freigewordenen Platz sein.
- [x] Das Land-Feld links nebem der PLZ muss nor einem 3LC-ISO-Code aufnehmen; es kann schmaler gemacht werden. Das PLZ Feld soll weiter nach links rücken, und den frei gewordenen Patz aufnehmen. Das Ort-Feld rechts neben der PLZ soll die restliche Breite des Blockes auffüllen.
- [x] In der Profilsuche ist das Layout kaputt. Links oben in der Navbar werden mehrere Buttons übereinander angezeigt. Die Navbar / Toolbar soll analog der anderen Formulare über die komplette Seitenbreite gehen.
- [x] Das Anhängen des extra _csrf Parameters bei fetch() Methoden aus dem vorherigen Bug-Fix war falsch. Es darf kein Extra Parameter übergeben werden.
- [x] Das Profilsuche Chatformular hat eine andere Beeite als z.B. dasd Freiberufler formular. Ich möchte, dass die Breite an das Freiberufler-Formular angepasst wird, sodass
  überall die gleichen Fluchtlinien verwendet werden.
- [x] Bein Zuordnen eines neuen Freiberuflers zu einem Projekt kommt es zu einem Fehker,
  weil der Positionsstatus null ist, und die Datenbank einen Fehler meldet. Das eigentliche
  Problem ist, dass die Menge der Positionsstatus via Admin UI konfigurierbar ist, das Feld
  aber ein Pflichtfeld ist. Ich möchte deshalb, dass der Positionsstatus um ein Feld "default" werweitert wird.
  Dieses Feld ist ein boolean, und kann nur bei einem der Positionsstatus in der Admin-UI gesetzt werden.
  Wenn eine neue Position für ein Projekt angelegt wird, und kein Status bekannt ist, soll der Status
  aus den konfigurierten Positionstatus-Datensätzen genommen werden, der mit Default markiert ist.
  Schreibe bitte auch entsprechende IT-Tests, und passe die Spezifikation entsprechend an.
- [x] Beim Zuordnen eines Freiberuflers zu einem Projekt soll ein Hinweis angezeigt werden, wenn die Zuordnung erfolgreich war. Im Fall einer doppeklten Zuordnung soll ein Warnhinweis angezeigt werden (HTTO Status Code Conflice = 209?)
- [x] Der Status-Badge für den Projekpositionsstatus soll auch den Text des Status-Labels anzeigen, im Moment wird nur die Farbe verwendet.
- [x] Beim Bearbeiten einer Projektposition passiert nichts. Es sollen aber Daten gespeichert werden und ein Hinweis angezeigt werden, dass die Daten gespeichert wurden. Im Fehlerfall soll ein Warnhinweis angezeigt werden.
- [x] Beim Bearbeiten eines Projektkontakthistorieneintrages wird dieser gelöscht im Sinne von der Text geht verloren, und es werden keine Audit-Informationen angezeigt.
- [x] Beim Bearbeiten eines Freelancerhistorieneintrages wird dieser komplett neu erzeugt; es sollte aber der bestehende Eintrag editiert werden, also die ID bleibt gleich, Werte ändern sich und die Audit-Informationen für die Änderung werden aktualisiert; die Audit-Informationen für die Anlage bleiben unverändert.
  Prüfe bitte durch entsprechende Tests, ob diese Problematik auch bei den anderen Formularen existiert, und ob dies auch die Kontaktmöglichkeiten betrifft. Implementiere bitte entsprechende Fixes und Tests.
- [x] Der Button "Freiberufler über Code zuordnen" auf der Projektseite macht nichts. Eigentlich sollte dieser einen Dialog öffnen, in dem der Sachbearbeiter den Code(Kodierung) eines Freiberuflers eingibt, und bei Bestätigung wird dieser Freiberufler dem Projekt zugeordnet, und ein Hinweis angezeigt.
  Im Fehlerfall soll ein Warnhinweis angezeigt werden.
- [x] Im Formular "Projekte" funktioniert der Button "Projekthistorie hinzufügen" nicht bzw. nicht mehr. Ebenso funktioniert das Editieren eines Projekthistorie-Eintrages nicht. Löschen funktioniert auch nicht. Es werden folgende JavaScript-Fehler in der Browserkonsole angezeigt:
  Hinzufügen: Uncaught ReferenceError: openAddHistoryModal is not defined
  onclick http://localhost:8080/project/1:1
  1:1:1
  Bearbeiten: Uncaught ReferenceError: openEditHistoryModal is not defined
  onclick http://localhost:8080/project/1:1
  Löschen: ncaught ReferenceError: deleteHistoryEntry is not defined
  onclick http://localhost:8080/project/1:1
- [x] Wenn ein Freiberufler zu einem Projekt im Freiberufler-Formular zugeordnet wird, dieser aber dem Projekt schon zugeordnet ist (HTTP Return Code 409), so soll ein entsprechender Hinweis im Formular angezeigt werden.
- [x] Im Projekte-Formular fehlt in der Navbar ein "Formular leeren" Button. Diese Funktion wird benötigt, um die QBE Suche für Projekte zu ermöglichen. Es ist im eigentlichen Sinne kein "Neu" Button, da neue Projekte nur über die Partner und Kunden Formulare angelegt werden können. Die Toolbar für Projekte
  soll analog der anderen Formulare um den "Suchen" Button erweitert werden.
- [x] Der Button "In Datenbank suchen" unten in den Formularen Freiberufler, Partner, Kunde und Projekt ist redundant zum "Suchen" Button in der Toolbar. Es soll nur ein Button "Suchen" in der Toolbar existieren, und dieser startet die Suche-Funktion in allen Formularen.
- [x] Um das komplette Chat-Formular sollte ein rahmen sein, analog der "Blöcke" z.B. im Freiberufler-Formular. Ebenfalls sollte in der Profilsuche der gleiche Abstand zur Toolbar eingehalten werden wie in den anderen Formularen.
- [x] Der vertikale Abstand zwischen der"Als Standart-Status verwenden" Checkbox und den Farben in dem Projektstatus-Bearbeiten Dialog ist zu klein; er sollte etwas größer sein.
- [x] Der Unified-Search Ansatz für Freiberufler, Partner und Kunden ist nicht ganz korrekt. Es ist nicht möglich, einem neuen Freiberufler, Partner oder Kunden direkt Kontaktmöglichkeiten oder Historieneinträge zuzuordnen, und diesen dann zu speichern. Ich muss zuerst
  z.B. einen neuen Freiberufler anlegen und speichern, und erst dann kann ich Kontaktmöglichkeiten oder Historieneinträge erfassen. Das ist ineffizient im Ablauf. Auch für neue Datensätze möchte ich direkt Kontaktmöglichkeiten und Historieneinträge erfassen können.
- [x] Beim Löschen eines Chats sehe ich nur die JavaScript-Fehlermeldung "Uncaught (in promise) TypeError: NetworkError when attempting to fetch resource. " in der Console. Es scheint, als wurde der Chat in der DB gelöscht, aber die UI nicht aktualisiert.
- [x] Der "In Datenbank suchen" Button ist redundant auf der Projektseite; Die Suche wird über die Toolbar gestartet.
- [x] Projektpositionen können noch immer nicht auf der Projektseite edidiert werden. Beim Klick auf den Bearbeiten Button erscheint eine JavaScript-Fehlermeldung in der Browserkonsole: Uncaught ReferenceError: openEditPositionModal is not defined
  onclick http://localhost:8080/project/1:1
- [x] Projektpositionen können nicht gelöscht werden; der Button auf dem Formular hat keine Funktion.
- [x] Ein leeres Projektformular kann nicht gespeichert werden, der Button "Speichern" soll nur sichtbar sein, wenn ein bestehendes Projekt bearbeitet wird. Ein leeres Projekt-Formular dient zur Eingabe der QBE Suchparameter. Neue Projekte werden über das Kunden bzw. Partner Formular angelegt.
- [ ] Die Buttons "Bearbeiten", "Löschen" und "Freiberufler über Code hinzufügen" im Profil-Formular haben noch immer keine Funktion, werfen aber keinen JavaScript-Fehler mehr.