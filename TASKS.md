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
- [ ] Ich möchte, dass bei technischen Fehlern im Chat-Streaming eine entsprechende
  Fehlermeldung angezeigt wird. Falls beim Chat-Streaming ein HTTP Unauthorized
  gesendet wird, soll die Anmeldemaske angezeigt werden.


## Erweiterungen

- [ ] Die Buttons für neuer Chat und Chat löschen sind in der Toolbar im Chat unlogisch
  angeordnet. Unter der Toolbar sind die Auswahlbuttons für Chat bzw. Volltextsuche, und
  die Funktionen zum Chat löschen und neuen Chat anlegen gehören nur zum Chat-Bereich.
  Eventuell wären diese Buttons besser in der linken Navi des Chat-Bereichs platziert, dort
  sind ja auch die Buttons zum Löschen eines einzelnen Chats. Vermutlich kann hier auch
  ein Button für "Neuer Chat" angelegt werden; er soll  dann die gleiche Funktion haben
  wie der Button in der Toolbar, also neuen Chat anlegen und ggf. mit dem selektierten
  Projekt verknüpfen.
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
- [ ] Ich möchte die Benutzerverwaltung um ein einfaches Rollenkonzept erweitern. Ein Benutzer kann
  Administrator sein, und nur wenn der aktuell angemeldete Benutzer Administrator ist, kann er
  im Admin-Bereich die unterschiedlichen Funktionen nutzen. Für nicht-Admin Benutzer ist im
  Admin bereich nur die Benutzerverwaltung sichtbar, es wird allerdings nur der aktuelle Benutzer angezeigt,
  und für diesen kann auch nur der Systemprompt bearbeitet werden.
- [ ] Ich möchte aus Suchergebnissen für Freiberufler, Partner, Kunden oder Projekte
  oder aus der Profilsuche / Chat die Links zu den Freiberuflern auch in einem neuen
  Browser-Tab öffnen können. Dies würde die Navigation durch große Listen sehr erleichtern. Im
  Moment ist das nicht möglich, da die Navigation über onClick-Handler vorgenommen wird.