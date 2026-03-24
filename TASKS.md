# TASKS.md – Powerstaff 2026 Implementierungsplan

Dieses Dokument listet alle Implementierungsaufgaben als granulare Checkpunkte.
Der Agent markiert jede abgeschlossene Task mit `[x]` und erstellt danach einen Git-Commit.

**Konventionen:**
- `[ ]` = offen · `[x]` = erledigt · `[-]` = übersprungen / nicht applicable
- Jede Task endet mit einem Git-Commit (vgl. CLAUDE.md – Commit-Konvention)
- Reihenfolge ist verbindlich: Abhängigkeiten nach oben hin auflösen

---

## Fehlerkorrekturen

- [ ] Der vertikale Abstand über dem Bereich "Kontaktmöglichkeiten" ist auf allen Formularen zu klein,
  und sollte auf den gleichen Abstand gesetzt werden wie im Freiberufler-Formuar zwischen "Kontaktmöglichkeiten"
  und "Kontakthistorie
- [ ] Das Datenmodell für "ProfileSearchMessage" muss erweitert werden. Es gibt jetzt auch noch messages, die Optional mit einem JSON-Payload in beliebiger länge gespeichert werden müssen. Dafür soll in der DB ein Langtextfeld angelegt werden.
- [ ] Pro Benutzer soll ein Profilsuche Systemprompt gespeichert werden, also auf Ebene PsUSer. Dieser Systemprompt soll auch über die Admin-UI editierbar sein. Der Default-Wert für diesen Prompt soll "Du bist ein freundlicher KI-Assistent für den Benutzer {user} und antwortest immer auf deutsch. Dein Name ist Staffi." sein.
  Bitte aktualisiere bei dieser Gelegenheit auch das Flyway-Skript zur initialen Anlage des Admin-Benutzers. Dort soll der Prompt auch mit dem Default eingetragen werden.
- [ ] Ergänze bitte einen E2E Playwright Tests für die Profilsuche. In der E2E Konfiguration wird der MockLLMService verwendet, die Testdaten sind also statisch und sind eine gute Grundlage für E2E Tests.
- [ ] Schreibe bitte noch E2E TEsts für die Bearbeitung und das Löschen der Entitäten "Historientypen", "Tags" und "Positionsstatus" aus der Admin-UI. Die Tests hier scheinen etwas lückenhaft zu sein.