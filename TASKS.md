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