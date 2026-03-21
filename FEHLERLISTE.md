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
- [ ] Das Anhängen des extra _csrf Parameters bei fetch() Methoden aus dem vorherigen Bug-Fix war falsch. Es darf kein Extra Parameter übergeben werden.
- [ ] Das Profilsuche Chatformular hat eine andere Beeite als z.B. dasd Freiberufler formular. Ich möchte, dass die Breite an das Freiberufler-Formular angepasst wird, sodass
  überall die gleichen Fluchtlinien verwendet werden.