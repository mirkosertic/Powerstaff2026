# TASKS.md – Powerstaff 2026 Implementierungsplan

Dieses Dokument listet alle Implementierungsaufgaben als granulare Checkpunkte.
Der Agent markiert jede abgeschlossene Task mit `[x]` und erstellt danach einen Git-Commit.

**Konventionen:**
- `[ ]` = offen · `[x]` = erledigt · `[-]` = übersprungen / nicht applicable
- Jede Task endet mit einem Git-Commit (vgl. CLAUDE.md – Commit-Konvention)
- Reihenfolge ist verbindlich: Abhängigkeiten nach oben hin auflösen

---

## Fehlerkorrekturen

- [x] Treffer in der Freiberufler, Partner und Kundensuche sollten bei Kontaktsperre
  rot markiert sein, analog den Treffern in der Vollltextsuche. Das muss noch korrigiert werden.
- [x] Ich erhalte bei nicht abgefangenen Applikationsfehlern die standard Spring Boot
  While Label Error Page. Ich möchte eine Error Page im Designsystem der Applikation
  mit einer Fehlermeldung und dem vollständigen Stacktrace, damit diese einfach z.B. via
  Copy-Paste dem Support übergeben werden kann. Die Fehlermeldung soll also mit
  StackTrace angezeigt werden, und idealerweise mit einem "In Zwischenablage kopieren" Button daneben,
  damit der Anwender nicht mekr zuerst alles markieren muss, um den Inhalt z.B. in eine Mail zu packen.

## Neue Features
- [ ] Die einzeln generierten Tokens einer Assistant-Antwort im Chat sollen ins
  den Chat gestreamed werden, so dass die sofort sichtbar werden. Im Moment wird die
  Antworet erst angezeigt, wenn die vollständig generiert wurde. Durch das
  Streaming soll das System auch bei "langsamerer" Token-Generierung responsiv und
  aktiv wirken. In diesem Setup soll auch der Abbrechen-Button im Chat aktiviert werden.
  Er soll genau den laufenden Token-Stream abbrechen. Der Button war vorher zur 
  zu Testzwecken da, macht aber eigentlich nur in Verbindung mit Streaming sinn.
  Achte bitte darauf, dass beim Streaming auch die Token-Nutzung bzw. die Statistik
  dafür laufend aktualisiert wird. 
- [ ] Ich möchte das Designsystem der Applikation auch als Dark-Mode zur Verfügung haben. 
  Die Darstellung soll sich an der aktuellen System-Einstellung anpassen, kann aber auch durch 
  Button-Klick in der Toolbar umgeschaltet werden. Achte bitte auch bei der 
  Darkmode-Darstellung darauf, dass die Anwendung für Bildschirmarbeitsplätze und
  6-8 Stunden Nutzung pro Tag optimiert ist. Ändere bitte die UI-DESIGNSYSTEM.md Spezifikation
  und natürlich auch die Implementierung für alle! Seiten des Projektes, vergiss also
  Chat und Stammdatenerwaltung und die damit verbundenen Unterseiten und Dialoge nicht,