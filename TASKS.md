# TASKS.md – Powerstaff 2026 Implementierungsplan

Dieses Dokument listet alle Implementierungsaufgaben als granulare Checkpunkte.
Der Agent markiert jede abgeschlossene Task mit `[x]` und erstellt danach einen Git-Commit.

**Konventionen:**
- `[ ]` = offen · `[x]` = erledigt · `[-]` = übersprungen / nicht applicable
- Jede Task endet mit einem Git-Commit (vgl. CLAUDE.md – Commit-Konvention)
- Reihenfolge ist verbindlich: Abhängigkeiten nach oben hin auflösen

---

## Fehlerkorrekturen

- [x] Der vertikale Abstand über dem Bereich "Kontaktmöglichkeiten" ist auf allen Formularen zu klein, und sollte auf den gleichen Abstand gesetzt werden wie im Freiberufler-Formular zwischen "Kontaktmöglichkeiten" und "Kontakthistorie".
- [x] Das Datenmodell für "ProfileSearchMessage" muss erweitert werden. Es gibt jetzt auch noch messages, die Optional mit einem JSON-Payload in beliebiger länge gespeichert werden müssen. Dafür soll in der DB ein Langtextfeld angelegt werden.
- [x] Pro Benutzer soll ein Profilsuche Systemprompt gespeichert werden, also auf Ebene PsUSer. Dieser Systemprompt soll auch über die Admin-UI editierbar sein. Der Default-Wert für diesen Prompt soll "Du bist ein freundlicher KI-Assistent für den Benutzer {user} und antwortest immer auf deutsch. Dein Name ist Staffi." sein. Bitte aktualisiere bei dieser Gelegenheit auch das Flyway-Skript zur initialen Anlage des Admin-Benutzers. Dort soll der Prompt auch mit dem Default eingetragen werden.
- [x] Ergänze bitte einen E2E Playwright Tests für die Profilsuche. In der E2E Konfiguration wird der MockLLMService verwendet, die Testdaten sind also statisch und sind eine gute Grundlage für E2E Tests.
- [x] Schreibe bitte noch E2E TEsts für die Bearbeitung und das Löschen der Entitäten "Historientypen", "Tags" und "Positionsstatus" aus der Admin-UI. Die Tests hier scheinen etwas lückenhaft zu sein.
- [x] In der Profilsuche bzw. im CHatverlauf möchte ich zwei neue Subtypen anzeigen. Auf Seite des "Assistant" gibt es den Typen "Tool-Aufruf" sowie "Tool-Ergebnis". Der Name des Tools soll initial sichtbar sein, die Details dahinter allerdings eingeklappt, können aber ausgeklappt werden. Das Prinzip dahinter soll an Claude Code angelehnt sein. Von Seite Backend werden diese Einträge entsprechend dem Typen markiert.
  Passe also den UI Code an, sodass die Toolaufrufe und Toolergebnisse im Chatverlauf angezeigt werden. Benutzer und erweiter ggf. das Designsystem dafür, und orientiere Dich an der Darstellung von Claude Code.
- [x] Ich möchte im Chat-Verlauf links neben dem Senden bzw. Stop Button tie aktuelle Kontextgröße zw. den Verbauch in % und die maximale Kontextgröße anzeigen, also Analog OIllama. Ich meine den Tokemverbauch bekommt man als
  Antwort via Spring AI ChatClientResponse mit, ich bin mir aber nicht sicher, wie die Kontextgröße übermittelt wird. Falls das nicht automatisch ermittelt werden kann, so soll eine Konfigurationsoption für die Größe geschaffen werden.
---

## Profilsuche: Klassische Suche (Phase 1 - Mock-Implementation)

**Kontext**: Erweiterung der Profilsuche um eine klassische Google-Style Suche mit Filterfeldern.
Siehe detaillierte Spezifikation in `specs/PROFILSUCHE.md` (Abschnitt "Klassische Suche").

**Ziel Phase 1**: Mock-Service mit funktionsfähiger UI für Testing und Akzeptanz. Echte SQL-Suche in Phase 2.

### Backend: Data Transfer Objects

- [ ] `ProfileSearchCriteria` Record erstellen in `profilesearch.query` Package
  - Felder: `searchTerm`, `salaryPerDayFrom`, `salaryPerDayTo`, `tagIds`, `sortField`, `sortDir`
  - Location: `src/main/java/de/mirkosertic/powerstaff/profilesearch/query/ProfileSearchCriteria.java`

- [ ] `ProfileSearchResult` Record erstellen in `profilesearch.query` Package
  - Felder: `id`, `code`, `name1`, `name2`, `lastContactDate`, `salaryPerDayLong`, `availabilityAsDate`, `contactForbidden`, `tags`
  - Location: `src/main/java/de/mirkosertic/powerstaff/profilesearch/query/ProfileSearchResult.java`

### Backend: Query Service erweitern

- [ ] `ProfileSearchQueryService` um Mock-Suchmethoden erweitern
  - Methode `searchFreelancers(criteria, offset, limit)` hinzufügen mit Mock-Daten
  - Methode `countSearchFreelancers(criteria)` hinzufügen
  - Mock simuliert grundlegende Filterung (Name, Tagessatz)
  - 50 Mock-Ergebnisse generieren, jeder 7. hat `contactForbidden=true`
  - Location: `src/main/java/de/mirkosertic/powerstaff/profilesearch/query/ProfileSearchQueryService.java`

### Backend: Controller erweitern

- [ ] `ProfileSearchController` um Such-Endpoints erweitern
  - `index()` ändern: Redirect zu `/profilesearch/chat`
  - `chatIndex()` hinzufügen: Redirect zu neuester Chat-Sitzung
  - `search()` hinzufügen für klassische Suche
    - Validierung: Mindestens ein Suchkriterium muss angegeben sein
    - Offset=0: Vollständige Seite, Offset>0: Fragment für Infinite Scroll
    - Cache-Control Header setzen (ADR-019)
    - X-Next-Url Header für Infinite Scroll
  - `buildSearchMoreUrl()` Helper-Methode hinzufügen
  - `SharedQueryService` injizieren für Tag-Laden
  - Location: `src/main/java/de/mirkosertic/powerstaff/profilesearch/api/ProfileSearchController.java`

### Frontend: Templates

- [ ] Chat-Template `form.html` um Tab-Navigation erweitern
  - Tab-Navigation am Anfang von `#chat-page` hinzufügen
  - Buttons: "Chat" (`.btn-pri`), "Suche" (`.btn-ghost`)
  - Location: `src/main/resources/templates/profilesearch/form.html`

- [ ] `search-page.html` erstellen für klassische Suche
  - Toolbar mit Projekt-Pill
  - Tab-Navigation (Suche aktiv)
  - Suchformular in `.fcard` mit Feldern: searchTerm, salaryPerDayFrom/To, Tags (Chips)
  - JavaScript für Tag-Auswahl (`.chip.selected` Toggle)
  - Ergebnistabelle mit sortierbaren Headern
  - Kontaktsperre-Markierung (rote Zeilen)
  - Infinite Scroll Element
  - Location: `src/main/resources/templates/profilesearch/search-page.html`

- [ ] `search-results.html` erstellen für Infinite Scroll Fragment
  - Fragment mit `<tr>` Elementen
  - Kontaktsperre-Markierung
  - Tag-Klick Navigation
  - Location: `src/main/resources/templates/profilesearch/search-results.html`

### Frontend: CSS

- [ ] `components2.css` um `.chip.selected` State erweitern
  - Ausgewählte Tags: blauer Hintergrund, weißer Text
  - Location: `src/main/frontend/src/css/components2.css`

### Freiberufler-Modul: Integration

- [ ] `FreelancerSearchCriteria` um `tagId` Parameter erweitern
  - Location: `src/main/java/de/mirkosertic/powerstaff/freelancer/query/FreelancerSearchCriteria.java`

- [ ] `FreelancerSearchResult` prüfen und ggf. um `contactForbidden` erweitern
  - Falls Feld fehlt: hinzufügen als `Boolean contactForbidden`
  - Location: `src/main/java/de/mirkosertic/powerstaff/freelancer/query/FreelancerSearchResult.java`

- [ ] `FreelancerQueryService` um Tag-Filter erweitern
  - In `search()` und `countSearch()`: Tag-Filter-SQL hinzufügen
  - In `search()`: `contactForbidden` Feld laden
  - Location: `src/main/java/de/mirkosertic/powerstaff/freelancer/query/FreelancerQueryService.java`

- [ ] `FreelancerController` um `returnTo` Parameter erweitern
  - `show(id, returnTo)`: Parameter hinzufügen, an Model übergeben
  - `search(criteria, returnTo)`: Parameter hinzufügen, an Model übergeben
  - Location: `src/main/java/de/mirkosertic/powerstaff/freelancer/api/FreelancerController.java`

- [ ] Freiberufler-Template `search-page.html` erweitern
  - Zurück-Button: Conditional "Zurück zur Profilsuche" wenn `returnTo=profilesearch-search`
  - Location: `src/main/resources/templates/freelancer/search-page.html`

- [ ] Freiberufler-Template `search-results.html` erweitern
  - Kontaktsperre-Markierung: Rote Zeilen für `contactForbidden=true`
  - `returnTo` Parameter in Navigation übergeben
  - Location: `src/main/resources/templates/freelancer/search-results.html`

- [ ] Freiberufler-Template `form.html` erweitern
  - Toolbar: Conditional Zurück-Button wenn `returnTo=profilesearch-search`
  - Location: `src/main/resources/templates/freelancer/form.html`

### Tests

- [ ] Unit-Tests für `ProfileSearchQueryService`
  - Mock-Suche mit verschiedenen Kriterien testen
  - Filterung (Name, Tagessatz) verifizieren
  - Pagination testen
  - Location: `src/test/groovy/de/mirkosertic/powerstaff/profilesearch/query/ProfileSearchQueryServiceSpec.groovy`

- [ ] Unit-Tests für `ProfileSearchController`
  - Routing testen (index, chat, search)
  - Validierung testen (mindestens ein Kriterium)
  - Model-Attribute verifizieren
  - Location: `src/test/groovy/de/mirkosertic/powerstaff/profilesearch/api/ProfileSearchControllerSpec.groovy`

- [ ] E2E-Tests für klassische Suche
  - Tab-Navigation zwischen Chat und Suche
  - Suche ohne Kriterien → Validierungsfehler
  - Suche mit Suchbegriff → Ergebnisse anzeigen
  - Tag-Auswahl → visuelles Feedback
  - Sortierung testen
  - Infinite Scrolling testen
  - Kontaktsperre-Markierung verifizieren
  - Tag-Klick → Freiberufler-QBE
  - Zurück-Navigation testen
  - Location: `src/test/e2e/profilesearch-search.spec.ts`

### Verifikation & Dokumentation

- [ ] Manuelle Tests durchführen (siehe `velvet-honking-puffin-analysis.md` Abschnitt 7)
  - Tab-Navigation
  - Suchformular
  - Suchergebnisse
  - Kontaktsperre
  - Navigation

- [ ] `./mvnw clean verify` erfolgreich durchlaufen
  - Alle Unit-Tests grün
  - Alle Integrationstests grün

- [ ] `./mvnw verify -Pe2e` erfolgreich durchlaufen
  - Alle E2E-Tests grün

---

## Profilsuche: Klassische Suche (Phase 2 - Echte SQL-Implementation)

**Hinweis**: Diese Phase wird nach erfolgreichem Abschluss von Phase 1 durchgeführt.

### Backend: Echte SQL-Suche

- [ ] `ProfileSearchQueryService.searchFreelancers()` auf echte SQL umstellen
  - LIKE-Suche auf `name1`, `name2`, `skills`
  - Numerische Filter auf `salary_per_day_long`
  - JOIN auf `freelancer_tags` für Tag-Filter (AND-Verknüpfung)
  - SQL ORDER BY für Sortierung
  - Batch-Loading der Tags für Ergebnisse

- [ ] `ProfileSearchQueryService.countSearchFreelancers()` auf echte SQL umstellen
  - Gleiche WHERE-Clause wie `searchFreelancers()`
  - COUNT(DISTINCT freelancer.id)

### Tests aktualisieren

- [ ] Integrationstests für echte SQL-Suche
  - Testcontainers mit MySQL
  - Verschiedene Suchkriterien-Kombinationen
  - Tag-Filter mit mehreren Tags
  - Sortierung verifizieren
  - Location: `src/test/java/de/mirkosertic/powerstaff/profilesearch/query/ProfileSearchQueryServiceIT.java`

- [ ] E2E-Tests aktualisieren
  - Echte Daten statt Mock
  - Ergebnisanzahl verifizieren
