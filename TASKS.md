# TASKS.md – Powerstaff 2026 Implementierungsplan

Dieses Dokument listet alle Implementierungsaufgaben als granulare Checkpunkte.
Der Agent markiert jede abgeschlossene Task mit `[x]` und erstellt danach einen Git-Commit.

**Konventionen:**
- `[ ]` = offen · `[x]` = erledigt · `[-]` = übersprungen / nicht applicable
- Jede Task endet mit einem Git-Commit (vgl. CLAUDE.md – Commit-Konvention)
- Reihenfolge ist verbindlich: Abhängigkeiten nach oben hin auflösen

---

## Fehlerliste

- [ ] Texte wie "Profil MS-002.txt" sollten im Profilsuche-Chat als Markdown-Link angezeigt
  werden, ich sehe allerdings nur den Markdown-Code, und nicht den Link. Als
  Markdown-Code wird "[Profil DEV-20017.txt](/freelancer/search?code=DEV-20017&returnTo=profilesearch-chat-17)" angezeigt.  
  Eventuell funktioniert die Formatierung nicht. Details siehe profilesearch/form.html.
- [ ] In der Profil-Volltextsuche kann ich Tags auswählen. Manchmal passier es, dass mehrere Tags ausgewählt
  werden, wenn ich einen selektiere. Ich weis noch nicht, warum das passiert. Eventuell gibt es
  Verwirrung mit den IDs (tag vs. freelancer_tag vs. ?)?

## Erweiterungen

- [ ] Ich benötige noch ein MCP Tool, welches in diesem Projekt verbunden ist, und die Abfrage von
  Profiltexten für eine Kodierung erlaubt. Die Eingabe ist die Kodierung eines Freiberuflers, die Ausgabe sind die
  gefundenen Profile mit Dateiname und Text. Das Tool ruft im Hintergrund ein anderes MCP Tool mit
  umgeformten Parametern auf. Die Implementierung der Tool-Delegation werde ich selber machen, ich möchte allerdings,
  dass Du hier schonmal den Rumpf und die Integration in the Chat-Client vorbereitest. Die Gesamtgröße
  der MCP Antwort dürfte auf 1 MB gedeckelt werden müssen, da dies das Protokoll erzwingt. Bereite für
  diese Prüfung bitte alles vor, oder treffere bessere Vorkehrungen, die mit dem Spring AI Framework
  kompatibel sind. Mit diesem Tool soll es dann dem LLM möglich sein, z.B. auf den Prompt
  "Analysiere den Inhalt von MSE-002" oder "Was kann der Freiberufler MSE-002 besonders gut?" den
  Inhalt des Profils als Text zu extrahieren und diesen dann zu untersuchen. Prüfe bitte, ob
  der Inhalt als Tool-Antwort oder als Referenz auf eine Ressource geliefert werden soll, da
  der Text beliebig lang werden kann. Mach einen Vorschlag, der zur aktuellsten MCP Protkollversion passt
  und von den gängigsten LLMs auch verarbeitet werden kann. Reicht hier die Definition einer
  dynamischen Ressource, oder muss es ein Tool sein?
- [ ] Anzeige des Treffer-Scores in der Volltextsuche und der semantischen Suche
- [ ] Möglichkeit des Löschen von Chat-Nachrichten in einem Chat; Branching ab einem bestimmten Punkt
  zu einem neuen Chat.
- [ ] Alle als "Auf der Homepage sichtbar" gekennzeichneten Projekte sollen auf der Homepage angezeigt werden.
  Dafür müssen diese Projekte in eine eigenständige MySQL Datenbank in eine spezielle Tabelle exportiert werden.
  Es handelt sich hier um eine Master-Slave Replikation, die Datenbank für die Homepage ist Read-Only für die
  Homepage. Die Replikation soll laufend erfolgen. Projekte, die als nicht mehr sichtbar gekennzeichnet sind,
  müssen aus der Homepage zuverlässig entfernt werden. Ob dieser Replikationsjob Teil der Anwendung ist
  oder ob das, analog dem MySQL-Backup-Job, ein eigenständiger Container wird, gilt es zu definieren. Wichtig ist,
  dass auf Ebene Powerstaff das fachliche Merkmal für die Sichtbarkeit von den Anwendern gepflegt werden kann.
- [ ] Die API-Tokens für die Chat/LLM Integration sollen vom Administrator und nur dem Administrator
  auf Ebene Benutzer pflegbar sein. Jeder Benutzer verwendet seinen eigenen API-Token für den Chat. Damit
  soll eine individuelle Abrechnung der Arbeitsplätze erfolgen.