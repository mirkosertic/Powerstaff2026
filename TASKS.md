# TASKS.md – Powerstaff 2026 Implementierungsplan

Dieses Dokument listet alle Implementierungsaufgaben als granulare Checkpunkte.
Der Agent markiert jede abgeschlossene Task mit `[x]` und erstellt danach einen Git-Commit.

**Konventionen:**
- `[ ]` = offen · `[x]` = erledigt · `[-]` = übersprungen / nicht applicable
- Jede Task endet mit einem Git-Commit (vgl. CLAUDE.md – Commit-Konvention)
- Reihenfolge ist verbindlich: Abhängigkeiten nach oben hin auflösen

---

## Fehlerkorrekturen

- [x] Die Testabdeckung von FreelancerController.java ist nicht optimal. Die Methode buildSearchMoreUrl ist gar nicht getestet.
- [x] Die Testabdeckung von FreelancerController.java ist nicht optimal. Die Methode buildEditSearchUrl ist nur partiell getestet.
- [x] FreelancerController.java: Hier stellt sich auch die Frage, ob der /search-more nicht redundant zum /search Endpunkt ist,
    /search-more hat ja die gleiche API, aber zusätzlich einen offset. /search und /search-more verhalten sich gleich, wenn der offset 0 ist.
    Ich wäre dafür, den Offset in die API von /search als optionalen Parameter aufzunehmen, und den Default auf 0 zu setzen.
- [x] FreelancerCommandService: Das Löschen einer Kontaktmöglichkeit sollte nicht möglich sein und einen Fehler werden. Das gilt auch für die Kontakthistorie.
    Dieser Fehler muss für die Formulare Freelander, Partner und Kunden korrigiert werden. Die Korrektur soll über entsprechende
    Tests abgesichert werden.
- [x] FreelancerCommandService: Es gibt keine Tests für das Hinzufügen und Entfernen von Tags zu einem Freiberufler. Diese
    Tests müssen noch implementiert werden, so wie es am Sinnvollsten ist (Unit vs. IT Test)
- [x] FreelancerCommandService.java: Es gibt keine Tests für die Aktualisierung und das Löschen eines Kontakthistorieneintrages.
    Diese Tests müssen implementiert werden, und auch für Partner, Kunden und Projekte.
- [x] FreelancerCommandService: Es darf nicht möglich sein, einen Kontakthistorieneintrag ohne ID zu Ändern oder zu Löschen. Dies
    muss einen Fehler werden, und durch entsprechende Tests überprüft werden.
- [x] FreelancerCOmmandService: Es gibt keine Tests für die Zuweisung eines Freiberuflers zu einem Partner, und für das
    Löschen einer derartigen Zuweisung.
- [x] ProjectController.java: Es gibt keine Tests für die Abfrage der Projektpositionen sowie für die Zuordnung und die Löschung.
- [x] ProjectController.java: Die Methode buildEditSearchUrl ist nur partiell getestet.
- [x] Prüfe bitte, dass alle QBE-Varianten für Freiberufler, Partner, Kunden und Projekte vollständig getestet werden. Besonders
    in Verbindung mit der countSearch() Implementierung habe ich mögliche Probleme gefunden; search() und countSearch()
    sollten die gleichen Abfragen benutzen, korrekt? In diesem Fall sollte die Abfrage an einem Ort zusammengebaut werden,
    z.B. in der appendStringCriteria Methode. Prüfe diese Inkonsistenzen bitte für alle Formulare und korrigiere sie entsprechend.
- [x] ProjectPositionCommandService.java: die Methode updateEditable ist nur partiell getestet.
- [x] Make sure you created E2E Tests für the changes from above
- [x] FreelancerController.java: the methods buildEditSearchUrl and buildSearchMoreUrl are still redundant, can be unified, and not
    every path is covered by tests. I am also not sure if the fromPath() in buildEditSearchUrl is correct. This issue likely
    exisis for the Customer, Partner and Project controllers as well.
- [x] The appendLike() Method for QBE is prone to SQL injection attacks. This must be fixed for every form and every search
    operation, so please check the Freelancer, Partner, Customer and Project services and repositories for this
    misaligned pattern.
- [x] PartnerController.buildSearchMoreUrl is completely untested, this can be unified while fixing the issued above.
- [x] KundeController.buildSearchMoreUrl is completely untested, this can be unified while fixing the issued above.
- [x] The E2E / Docker Tests sometimes leave dangling volumes and do not properly clean up. This should be fixed.
- [x] FreeelancerCommandService.findByCode is completely untested.
- [x] Bearbeiten und Löschen von Projektpositionen ist nicht möglich. In der Browser-Console werden die JavaScript Fehler
  Bearbeiten: Uncaught ReferenceError: openEditPositionModal is not defined
  onclick http://localhost:8080/project/1:1
  Löschen: Uncaught ReferenceError: openDeletePositionModal is not defined
  onclick http://localhost:8080/project/1:1
  Dieser Fehle muss korrigiert und durch E2E Playwright Tests validiert werden!
- [x] Der Button "Freiberufler diesem Projekt zuordnen" auf dem Freiberufler-Formular ist zu groß. Es soll der Text
  "Projekt zuordnen" verwendet werden!
- [x] Der "Suchen" Button auf dem Freiberufler, Partner und Kunden Formular soll nur bei leeren Formularen angezeigt
  werden, also wenn ich auf "Neu" klicke bzw. der Datensatz noch keine Id hat. Dieses Verhalten soll auch über E2E
  Tests mittels Playwright getestet werden.
- [x] Die Audit-Info in der Navbar soll aus Platzgründen zweizeilig angezeigt werden, und mit etwas kleinerer Schrift.
  Die Erste Zeile enweder "Neu, noch nicht gespeichert" bzw. "Erfast ...", die zweite Zeile dann mit "Geändert ..."
  Diese Anpassung soll für die Formulare Freiberufler, Partner und Kunden umgesetzt werden.
- [x] Der Profilsuche-Chat Bereich im Profilsuche Formular hat links und rechts noch einen kleinen Rand, weshalb
  er ein wenig breiter ist als die Fluchtlinien und die Darstellung z.B. auf dem Freiberufler-Formular. Dieser
  rand bzw. abstand hat auch eine komische Farbe (gräulich), was irgendwie unpassend wirkt. Der Abstand soll
  entfernt werden.
- [x] Der Administrationsbereich soll um eine Benutzerverwaltung erweitert werden. Es soll eine Liste aller
  Benutzer angezeigt werden, und es sollen auch neue Benutzer angelegt, bestehende Benutzer bearbeitet und
  auch gelöscht werden können. Bearbeitet werden sollen alle Merkmale eines Benutzers, für die boolean Attribute
  sollen Checkboxen verwendet werden.
- [x] Fehler bei der Validierung von Datumsfeldern. Dieser Fehler betrifft vermutlich alle Orte, wo ein Datum bzw.
  ein Zeitstempel / Localdatetime eingegeben werden kann, entweder über Freitext oder über ein Datepicker.
  Im Backend wird folgende Fehlermeldung ausgegeben:  [Failed to convert property value of type 'java.lang.String' to required type 'java.time.LocalDateTime' for property 'lastContactDate'; Failed to convert from type [java.lang.String] to type [@org.springframework.data.relational.core.mapping.Column java.time.LocalDateTime] for value [2005-01-01]]]
  im Frontend habe ich allerdings keinen Validierungsfehler vor dem Speichern des Formulars bekommen. Im Fall
  von falschen Datumsangaben möchte ich im Frontend einen Validierungsfehler angezeigt bekommen, und natürlich
  muss das Backend diese Eingabe auch bearbeiten können. Als Datumsformat soll das Format dd.MM.yyyy verwendet werden,
  die Zeitzone ist die aktuelle Server-Zeitzone. Für diesen Fehler müssen Tests und idealerweise auch E2E Playwright
  Tests für alle Formulare mit Datumsfeldern implementiert werden.
