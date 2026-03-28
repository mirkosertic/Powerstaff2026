# TASKS.md – Powerstaff 2026 Implementierungsplan

Dieses Dokument listet alle Implementierungsaufgaben als granulare Checkpunkte.
Der Agent markiert jede abgeschlossene Task mit `[x]` und erstellt danach einen Git-Commit.

**Konventionen:**
- `[ ]` = offen · `[x]` = erledigt · `[-]` = übersprungen / nicht applicable
- Jede Task endet mit einem Git-Commit (vgl. CLAUDE.md – Commit-Konvention)
- Reihenfolge ist verbindlich: Abhängigkeiten nach oben hin auflösen

---

## Fehlerkorrekturen

- [ ] Treffer in der Freiberufler, Partner und Kundensuche sollten bei Kontaktsperre
  rot markiert sein, analog den Treffern in der Vollltextsuche. Das muss noch korrigiert werden.
- [ ] Ich erhalte bei nicht abgefangenen Applikationsfehlern die standard Spring Boot
  While Label Error Page. Ich möchte eine Error Page im Designsystem der Applikation
  mit einer Fehlermeldung und dem vollständigen Stacktrace, damit diese einfach z.B. via
  Copy-Paste dem Support übergeben werden kann. Die Fehlermeldung soll also mit
  StackTrace angezeigt werden, und idealerweise mit einem "In Zwischenablage kopieren" Button daneben,
  damit der Anwender nicht mekr zuerst alles markieren muss, um den Inhalt z.B. in eine Mail zu packen.