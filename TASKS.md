# TASKS.md – Powerstaff 2026 Implementierungsplan

Dieses Dokument listet alle Implementierungsaufgaben als granulare Checkpunkte.
Der Agent markiert jede abgeschlossene Task mit `[x]` und erstellt danach einen Git-Commit.

**Konventionen:**
- `[ ]` = offen · `[x]` = erledigt · `[-]` = übersprungen / nicht applicable
- Jede Task endet mit einem Git-Commit (vgl. CLAUDE.md – Commit-Konvention)
- Reihenfolge ist verbindlich: Abhängigkeiten nach oben hin auflösen

---

## Fehlerkorrekturen

[ ] Die Testabdeckung von FreelancerController.java ist nicht optimal. Die Methode buildSearchMoreUrl ist gar nicht getestet.
[ ] Die Testabdeckung von FreelancerController.java ist nicht optimal. Die Methode buildEditSearchUrl ist nur partiell getestet.
[ ] FreelancerController.java: Hier stellt sich auch die Frage, ob der /search-more nicht redundant zum /search Endpunkt ist, 
    /search-more hat ja die gleiche API, aber zusätzlich einen offset. /search und /search-more verhalten sich gleich, wenn der offset 0 ist.
    Ich wäre dafür, den Offset in die API von /search als optionalen Parameter aufzunehmen, und den Default auf 0 zu setzen.
[ ] FreelancerCommandService: Das Löschen einer Kontaktmöglichkeit sollte nicht möglich sein und einen Fehler werden. Das gilt auch für die Kontakthistorie.
    Dieser Fehler muss für die Formulare Freelander, Partner und Kunden korrigiert werden. Die Korrektur soll über entsprechende
    Tests abgesichert werden.
[ ] FreelancerCommandService: Es gibt keine Tests für das Hinzufügen und Entfernen von Tags zu einem Freiberufler. Diese
    Tests müssen noch implementiert werden, so wie es am Sinnvollsten ist (Unit vs. IT Test)
[ ] FreelancerCommandService.java: Es gibt keine Tests für die Aktualisierung und das Löschen eines Kontakthistorieneintrages.
    Diese Tests müssen implementiert werden, und auch für Partner, Kunden und Projekte.
[ ] FreelancerCommandService: Es darf nicht möglich sein, einen Kontakthistorieneintrag ohne ID zu Ändern oder zu Löschen. Dies
    muss einen Fehler werden, und durch entsprechende Tests überprüft werden.
[ ] FreelancerCOmmandService: Es gibt keine Tests für die Zuweisung eines Freiberuflers zu einem Partner, und für das
    Löschen einer derartigen Zuweisung.
[ ] ProjectController.java: Es gibt keine Tests für die Abfrage der Projektpositionen sowie für die Zuordnung und die Löschung.
[ ] ProjectController.java: Die Methode buildEditSearchUrl ist nur partiell getestet.
[ ] Prüfe bitte, dass alle QBE-Varianten für Freiberufler, Partner, Kunden und Projekte vollständig getestet werden. Besonders
    in Verbindung mit der countSearch() Implementierung habe ich mögliche Probleme gefunden; search() und countSearch()
    sollten die gleichen Abfragen benutzen, korrekt? In diesem Fall sollte die Abfrage an einem Ort zusammengebaut werden,
    z.B. in der appendStringCriteria Methode. Prüfe diese Inkonsistenzen bitte für alle Formulare und korrigiere sie entsprechend.
[ ] ProjectPositionCommandService.java: die Methode updateEditable ist nur partiell getestet.