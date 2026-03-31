# TASKS.md – Powerstaff 2026 Implementierungsplan

Dieses Dokument listet alle Implementierungsaufgaben als granulare Checkpunkte.
Der Agent markiert jede abgeschlossene Task mit `[x]` und erstellt danach einen Git-Commit.

**Konventionen:**
- `[ ]` = offen · `[x]` = erledigt · `[-]` = übersprungen / nicht applicable
- Jede Task endet mit einem Git-Commit (vgl. CLAUDE.md – Commit-Konvention)
- Reihenfolge ist verbindlich: Abhängigkeiten nach oben hin auflösen

---

## Fehlerliste

- [x] Wenn im interaktivem Chat ein Thinking-Block mit Tokens gefüllt
  wird, wird unterhalb davon ein leerer ASssistant Block angezeigt. Das ist irreführent,
  der leere Block sollte nicht angezeigt werden.
- [x] Nach einer Tool Invocation ist das Modell noch mit dem Parsen der
  Antwort beschäftigt, hat also das Thinking noch gar nicht gestartet. Für
  Diese Zwischenzeit soll der Ladeindikator angezeigt werden, also der Gleiche
  der auch angezeigt wird, wenn ich den ersten Chateintrag mache und auf eine
  Reaktion warte. Sobald das Thinking startet oder eine Assistant-Antwort kommt
  soll der Ladeindikator verschwinden, also gleiches Verhalten wie initial im Chat.
- [x] Der Backlink aus einem Volltext-Suchergebnis zu einem Freiberufler funktioniert nicht.
  Der Button "Zur Profilsuche" zeigt auf das leere Suchformular, und nicht auf
  die Suchergebnissliste mit allen Einträgen, also das Bookmark des Suchergebnisses.
- [ ] Ich möchte, dass bei technischen Fehlern im Chat-Streaming eine entsprechende
  Fehlermeldung angezeigt wird. Falls beim Chat-Streaming ein HTTP Unauthorized
  gesendet wird, soll die Anmeldemaske angezeigt werden.

## Erweiterungen

- [x] Vor dem Markdown-Highlighten der Chat Agenten-Antworten im interaktivem Chat
  soll der Inhalt des Markdowns auf das Muster "Profil <Kodierung>" untersucht werden,
  wobei <Kodierung> sich zusammensetzt aus <Min. 2 Zeichen>-<Zahl>. Wenn dieses
  Muster erkannt wird, soll aus dem Treffer ein Link generiert werden. Der Link
  zeigt auf eine Freiberufler-Suche, in der nach Kodierung=<Min. 2 Zeichen>-<Zahl>
  gesucht wird, also der vollständige Treffer nach dem "Profil " Text. Als
  "ReturnTo" URL im Freiberufler-Suchergebnis soll die Bookmark-URL des aktuellen
  Chats verwendet werden. Wenn der Chat noch nicht abgeschlossen ist, also gerade
  ein Streaming von Thinking oder Agenten-Tokens erfolgt oder der "Warten"
  Indikator angezeigt wird, soll vor dem Auslösen des Links ein Warnhinweis
  angezeigt werden, dass hierdurch der aktuelle Chat abgebrochen wird.
