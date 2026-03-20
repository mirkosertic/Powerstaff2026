# Gefundene Fehler

Die folgende Liste beschreibt die gefundenen Fehler aus den manuellen Tests. Jeder Befund soll einzeln korrigiert und via MAven build überprüft. Im Anschlus darf der EIntrag in der Liste als erledigt markiert werden, und der Change via Git in die Versionskontrolle aufgenommen werden.

- [x] Die Anzeige der Freiberufler Seite erzeugt folgenden JavaScript-Fehler im Browser: Uncaught ReferenceError: apiFetch is not defined
  loadAvailableTags http://localhost:8080/freelancer/1:914
  <anonymous> http://localhost:8080/freelancer/1:991
  <anonymous> http://localhost:8080/freelancer/1:990
- [x] Im Prototypen im Verzeichnis "prototype/freiberufler.html" haben die DSVO oder Kontaktsperre
  Checkbox tatsächlich auch eine sichtbare Checkbox. Die entsprechenden Checkboen
  in der Implementierung sind nicht sichtbar. Bitte passe die Darstellung für Checkboxen
  der Implementierung dem Prototypen an.
- [x] Die Audit-Info wird jetzt in der Navbar als auch unten auf der Seite in
  allen Formularen angezeigt. Das ist redundant; sie soll nur in der Navbar angezeigt werden, der untere
  Block auf den Seiten kann also entfernt werden.
- [x] Das gemeerkte Projekt wird auf der Freiberufler, Kunden, Partner und Profilsuche Seite nicht angezeigt,
  dafür abber auf de Projektseite selber, wo es nicht hingehört und redundant ist, da es immer identisch mit dem
  aktuellen Projekt ist.
- [x] Auf der Projektseite fehlt die Zuordnungsmöglichkeit eines Freiberuflers über den Code.
- [x] Auf der Freiberufler-Seite fehlt ein Button für die Zuordnung des Freiberuflers zum aktuell gemerkten Projekt, falls vorhanden.
- [x] Das gemerkte Projekt wird zu schmal in der Navbar engezeigt. Es könnte größer sein.
  Die Audit-Informationen und das ggf. gemerkte Projekt können sich den Platz teilen, wobei das gemerkte
  Projekt gerne mehr Platz bekommen darf, die Audit-Informationen müssen aber jederzeit sichtbar sein.
- [x] Für Partner und Kunden gibt es keine Kontaktmöglichkeiten und keine Kontakthistorie. Das ist falsch.
- [x] Die Feldreihenfolge der Adressfelder für Freiberufler, Paertner und Kunden ist nicht schlüssig.
  Die Reihenfolge müsste sein Straße (eine Zeile), dann eine zweite Zeile mit Land(klein), Plz (etwas größer,
  Platz für max. 5 Zeichen, und der Rest des Platzes wird für den Ort verwendet.
- [x] Die Feldreihenfolge für den Namen der Freiberufler ist nicht ganz logisch. Zuerst muss
  das Feld für die Anrede(etwas kleiner), dann Vorname und dann Name kommen, alles in einer Zeile.
- [x] Vor dem Speichern eines Freiberuflers muss geprüft werden, ob das Feld "Kontaktart" mit einem Value <> ""
  gefüllt ist. Es handelt sich um ein Pflichtfeld.
- [x] Bei technischen Fehlern beim Speichern eines Aggregates im Projekt soll eine
  rote Fehlermeldung im Formular angezeigt werden. Im Moment werden solche Fehler verschluckt.
- [x] Beim Hinzufügen einer Kontaktmöglichkeit zu einem Freiberufler, Partner oder Kunden
  wird nach dem Speichern die Liste der Kontaktmöglichkeiten nicht aktualisiert.
- [x] Beim Hinzufügen einer Kontakthistorie zu einem Freiberufler, Partner oder Kunden
  wird nach dem Speichern die Liste der Historieneinträge nicht aktualisiert.
- [x] Es gibt einen Thymeleaf-Fehler beim Anzeigen der Adminseite für Historientypen: 2026-03-20 20:03:08 [http-nio-127.0.0.1-8080-exec-1] ERROR org.thymeleaf.TemplateEngine - [THYMELEAF][http-nio-127.0.0.1-8080-exec-1] Exception processing template "admin/historientypen": An error happened during template parsing (template: "class path resource [templates/admin/historientypen.html]")
  org.thymeleaf.exceptions.TemplateInputException: An error happened during template parsing (template: "class path resource [templates/admin/historientypen.html]")
  at org.thymeleaf.templateparser.markup.AbstractMarkupTemplateParser.parse(AbstractMarkupTemplateParser.java:241)
  at org.thymeleaf.templateparser.markup.AbstractMarkupTemplateParser.parseStandalone(AbstractMarkupTemplateParser.java:100)
- [x] Es wurden MVC / API Endpunkte geändert oder hinzugefügt, ohne einen UNit oder IT Test dafür zu schreiben.
  Die Skills bzw. die Claude Instruktionen sagen aber, dass das sein muss. Damit könnten auch Thymeleaf-Fehler besser und
  sofort erkannt werden.
- [x] Die QBE Suche bzw. das Drücken des Suche-Buttons in allen Formularen öffnet
  ein neues Modal, in dem die Suchfelder ausgefüllt werden. Die Suchergebnislisrte wird
  dann darunter angezeigt. Die Idee war, dass die QBE Suche das gleiche
  Formular nutzt, welches für die Bearbeitung der Daten genutzt wird. Der Ablauf ist also
  Neu(Formular leeren)->Suchfelder befüllen->QBE-Suche starten->Suchergebnis wird angezeigt.
  Die Ergebnisliste soll via Infinite Scroll geladen werden, mit Sortierung und Navigation auf
  den Freiberufler. Das Suchkonzept wurde hier nicht richtig umgesetzt. Bitte achte
  bei der Implementierung für Initie-Scroll auf die Sortiermöglichkeiten. Ein
  Umsortieren der Liste muss das Ergebnis komplett neu laden, eine Sortierung auf HTML Seite wäre sinnlos.
