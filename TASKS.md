# TASKS.md – Powerstaff 2026 Implementierungsplan

Dieses Dokument listet alle Implementierungsaufgaben als granulare Checkpunkte.
Der Agent markiert jede abgeschlossene Task mit `[x]` und erstellt danach einen Git-Commit.

**Konventionen:**
- `[ ]` = offen · `[x]` = erledigt · `[-]` = übersprungen / nicht applicable
- Jede Task endet mit einem Git-Commit (vgl. CLAUDE.md – Commit-Konvention)
- Reihenfolge ist verbindlich: Abhängigkeiten nach oben hin auflösen

---

## Phase 0 – Querschnittsinfrastruktur

### 0.1 Spring Security – Formularlogin
- [ ] `SecurityConfig` mit BCrypt, Form-Login (`/login`), Logout (`/logout`), CSRF via `CookieCsrfTokenRepository`, alle Endpunkte authenticated
- [ ] `ps_user`-Tabelle: Aggregate `PsUser` + Repository `PsUserRepository`
- [ ] `PsUserDetailsService` implementiert `UserDetailsService`, lädt Nutzer über `PsUserRepository`
- [ ] Thymeleaf Login-Template (`login.html`) gemäß UI-DESIGNSYSTEM.md (keine App-Nav, einfaches Zentriertes Formular)
- [ ] Test: `@WebMvcTest SecurityIT` prüft: unauthentifizierter Zugriff → Redirect zu `/login`, POST `/login` mit korrekten Credentials → Redirect, falsche Credentials → Fehlerseite
- [ ] Git-Commit

### 0.2 Auditing
- [ ] `AuditingConfig` implementiert `AuditorAware<String>`, liefert aktuellen `UserDetails::getUsername`
- [ ] `@EnableJdbcAuditing` ist bereits in `PowerstaffApplication` – keine Änderung nötig
- [ ] Test: `AuditingConfigSpec` prüft, dass `currentAuditor()` den eingeloggten Benutzernamen zurückgibt
- [ ] Git-Commit

### 0.3 Basis-Layout und Design-System
- [ ] `base.css` gemäß UI-DESIGNSYSTEM.md §2 (Design Tokens als CSS Custom Properties) + §3 (Typografie) in `src/main/resources/static/css/`
- [ ] `layout.css` (Shell-Layout §4, App-Nav §5, Form-Toolbar §6, Banners §7) in `src/main/resources/static/css/`
- [ ] `components.css` (fcard §8, field-grids §9, Buttons §10, Checkboxen §11, Divider §12, Kontaktliste §13, Chips §14, Kontakthistorie §15, Tabellen §16, Modals §17) in `src/main/resources/static/css/`
- [ ] Thymeleaf Fragment `fragments/layout.html` (App-Shell, App-Nav mit allen 5 Menüpunkten: Freiberufler, Partner, Kunden, Projekte, Profilsuche; Logout-Link)
- [ ] Thymeleaf Fragment `fragments/toolbar.html` (Form-Toolbar mit Slot für Seitenspezifische Buttons + Gemerktes-Projekt-Anzeige §21)
- [ ] Thymeleaf Fragment `fragments/modal.html` (wiederverwendbares Modal-Grundgerüst §17)
- [ ] `main.js` mit `apiFetch` CSRF-Wrapper (liest CSRF-Token aus Cookie `XSRF-TOKEN`, setzt Header `X-XSRF-TOKEN`)
- [ ] Test: `@WebMvcTest LayoutIT` prüft, dass `/login` 200 liefert, kein Layout-Fragment fehlt
- [ ] Git-Commit

### 0.4 Stammdaten-Enums (Shared Domain)
- [ ] Enum `ContactType` mit Werten `EMAIL, WEB, XING, GULP, TELEFON, FAX` + Hilfsmethoden (Label, Link-URL)
- [ ] Enum `TagType` mit Werten `SCHWERPUNKT, FUNKTION, EINSATZORT, BEMERKUNG, TYP` (Ordinalwerte 0–4)
- [ ] Enum `ProjectStatus` mit Werten 1–5 + `fromInt()` + `getLabel()` (kein DB-Lookup)
- [ ] Test: `ContactTypeSpec`, `TagTypeSpec`, `ProjectStatusSpec` (reine Unit-Tests, kein DB)
- [ ] Git-Commit

---

## Phase 1 – Modul `stammdaten` (gemeinsam genutzte Lookup-Tabellen)

### 1.1 Historientypen – Backend
- [ ] Aggregate `HistoryType` (`id`, `description`) im Paket `de.mirkosertic.powerstaff.stammdaten`
- [ ] `HistoryTypeRepository` (Spring Data JDBC, kein Custom SQL nötig)
- [ ] `HistoryTypeQueryService` mit `findAll()` (sortiert nach `description ASC`) via `JdbcClient`
- [ ] Test: `HistoryTypeRepositoryIT` extends `AbstractContainerBaseIT`: speichert/liest HistoryType; `HistoryTypeQueryServiceIT` prüft Sortierung
- [ ] Git-Commit

### 1.2 Projektpositions-Status – Backend
- [ ] Aggregate `ProjectPositionStatus` (`id`, `description`, `color`, `colorText`)
- [ ] `ProjectPositionStatusRepository`
- [ ] `ProjectPositionStatusQueryService` mit `findAll()` (sortiert nach `description ASC`)
- [ ] Test: `ProjectPositionStatusRepositoryIT`, `ProjectPositionStatusQueryServiceIT`
- [ ] Git-Commit

### 1.3 Tags – Backend
- [ ] Aggregate `Tag` (`id`, `name` → Spalte `tagname`, `type` → `TagType`)
- [ ] `TagRepository`
- [ ] `TagQueryService` mit `findAll()` und `findByType(TagType)` via `JdbcClient`
- [ ] Test: `TagRepositoryIT`, `TagQueryServiceIT` prüft Filterung nach Typ
- [ ] Git-Commit

### 1.4 Administration – Historientypen UI
- [ ] `StammdatenController` GET `/admin/historientypen` → Liste aller Historientypen
- [ ] POST `/admin/historientypen` → Neuanlage (Felder: `description`)
- [ ] POST `/admin/historientypen/{id}` → Bearbeiten
- [ ] Thymeleaf-Template `admin/historientypen.html` (Tabelle + Modal für Neuanlage/Bearbeitung)
- [ ] Test: `@WebMvcTest StammdatenControllerIT` prüft GET/POST-Endpunkte
- [ ] Git-Commit

### 1.5 Administration – Projektpositions-Status UI
- [ ] GET `/admin/positionsstatus` → Liste
- [ ] POST `/admin/positionsstatus` → Neuanlage (Felder: `description`, `color`, `colorText`)
- [ ] POST `/admin/positionsstatus/{id}` → Bearbeiten
- [ ] Thymeleaf-Template `admin/positionsstatus.html` (Tabelle + Modal; Badge-Vorschau mit CSS-Inline-Style)
- [ ] Test: `@WebMvcTest StammdatenControllerIT` ergänzt
- [ ] Git-Commit

### 1.6 Administration – Tags UI
- [ ] GET `/admin/tags` → Liste aller Tags (gruppiert nach TagType)
- [ ] POST `/admin/tags` → Neuanlage (Felder: `name`, `type`)
- [ ] POST `/admin/tags/{id}` → Bearbeiten
- [ ] DELETE `/admin/tags/{id}` → Löschen (AJAX, kein Bestätigungsdialog im Backend)
- [ ] Thymeleaf-Template `admin/tags.html`
- [ ] Test: `@WebMvcTest StammdatenControllerIT` ergänzt
- [ ] Git-Commit

---

## Phase 2 – Modul `partner`

### 2.1 Partner – Domain & Repository
- [ ] Aggregate `Partner` mit allen Feldern laut PARTNER.md (Gruppe Adresse, Kontaktinformationen, Kommentar, Konditionen); `@Version db_version`, `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy`
- [ ] `PartnerRepository` (Spring Data JDBC)
- [ ] Test: `PartnerRepositoryIT` extends `AbstractContainerBaseIT`: CRUD, Optimistic-Locking-Konflikt löst `OptimisticLockingFailureException` aus
- [ ] Git-Commit

### 2.2 Partner – CommandService
- [ ] `PartnerCommandService` mit:
  - `save(Partner)` → prüft Optimistic Locking, speichert
  - `delete(Long id)` → prüft FK-Constraint (`project.partner_id RESTRICT`), wirft `PartnerHasProjectsException` mit Projektnummern-Liste; sonst löscht
  - `assignFreelancer(Long partnerId, String code)` → sucht Freelancer per `code`, setzt `freelancer.partner_id`
  - `removeFreelancer(Long freelancerId)` → setzt `freelancer.partner_id = NULL`
- [ ] Test: `PartnerCommandServiceIT` (Testcontainers): speichert Partner, Delete mit und ohne Projektzuordnung, Freelancer zuordnen/entfernen
- [ ] Git-Commit

### 2.3 Partner – QueryService
- [ ] `PartnerQueryService` via `JdbcClient`:
  - `findById(Long)` → Optional
  - `findFirst()` / `findLast()` / `findPrevious(Long)` / `findNext(Long)` (Navigation)
  - `search(PartnerSearchCriteria)` → QBE (LIKE `%wert%` AND-verknüpft, parametrisiert, niemals String-Konkatenation); Felder: `company`, `name1`, `name2`, `street`, `country`, `plz`, `city`, `comments`, `kreditorNr`, `debitorNr`; Ergebnis: `List<PartnerSearchResult>` (paginiert, offset/limit)
  - `countSearch(PartnerSearchCriteria)` → Gesamtanzahl für Infinite Scrolling
  - `findFreelancersByPartnerId(Long, String sortField, String sortDir)` → Liste zugeordneter Freelancer
  - `findProjectsByPartnerId(Long, String sortField, String sortDir)` → Liste zugeordneter Projekte
- [ ] Test: `PartnerQueryServiceIT` prüft Navigation, QBE-Suche (Treffer, kein Treffer, mehrere Felder)
- [ ] Git-Commit

### 2.4 Partner – Controller
- [ ] `PartnerController` mit:
  - GET `/partner` → lädt zuletzt angezeigten Partner (Session-Attribut `lastPartnerId`), sonst leer; rendert `partner/form.html`
  - GET `/partner/{id}` → lädt Partner, setzt `lastPartnerId`; rendert `partner/form.html`
  - POST `/partner/save` → speichert, Redirect zu `/partner/{id}`; Optimistic-Locking-Konflikt → JSON-Antwort mit Konfliktinfo
  - POST `/partner/delete/{id}` → löscht oder gibt Projektliste zurück (JSON)
  - GET `/partner/new` → leert Session, Redirect zu `/partner`
  - GET `/partner/first` / `/partner/last` / `/partner/previous/{id}` / `/partner/next/{id}` → Navigation
  - POST `/partner/search` → ruft `PartnerQueryService.search()`, rendert `partner/search-results.html` (HTMX/AJAX Fragment)
  - GET `/partner/search-more` → Infinite Scrolling (offset-Parameter)
  - POST `/partner/{id}/assign-freelancer` → `PartnerCommandService.assignFreelancer()`; JSON-Antwort
  - POST `/partner/{id}/remove-freelancer/{freelancerId}` → `PartnerCommandService.removeFreelancer()`
- [ ] Test: `@WebMvcTest PartnerControllerIT` mit MockMvc: alle Endpunkte, Security, CSRF
- [ ] Git-Commit

### 2.5 Partner – Kontaktmöglichkeiten (AJAX)
- [ ] Aggregate `PartnerContact` (`id`, `type` → `ContactType`, `value`, `partnerId`, Audit-Felder)
- [ ] `PartnerContactRepository`
- [ ] `PartnerContactCommandService`: `save(PartnerContact)`, `delete(Long id)`
- [ ] `PartnerContactQueryService`: `findByPartnerId(Long)` → sortiert nach `ContactType`-Reihenfolge, dann `id ASC`
- [ ] `PartnerContactController` (AJAX): POST `/partner/{id}/contacts` → speichert; DELETE `/partner/{id}/contacts/{contactId}`; GET `/partner/{id}/contacts` → JSON-Liste
- [ ] Test: `PartnerContactRepositoryIT`, `PartnerContactControllerIT`
- [ ] Git-Commit

### 2.6 Partner – Kontakthistorie (AJAX)
- [ ] Aggregate `PartnerHistory` (`id`, `description`, `typeId` → FK historytype, `partnerId`, Audit-Felder)
- [ ] `PartnerHistoryRepository`
- [ ] `PartnerHistoryCommandService`: `save(PartnerHistory)`, `delete(Long id)`
- [ ] `PartnerHistoryQueryService`: `findByPartnerId(Long)` → sortiert `creation_date DESC`
- [ ] `PartnerHistoryController` (AJAX): POST `/partner/{id}/history`, PUT `/partner/{id}/history/{hId}`, DELETE `/partner/{id}/history/{hId}`
- [ ] Test: `PartnerHistoryRepositoryIT`, `PartnerHistoryControllerIT`
- [ ] Git-Commit

### 2.7 Partner – Thymeleaf-Templates
- [ ] `partner/form.html`: alle Feldgruppen (Adresse, Kontaktinformationen inkl. Kontaktmöglichkeiten-Liste, Kommentar, Konditionen), Toolbar mit Navigation + Gemerktes-Projekt-Anzeige, Kontakthistorie-Sektion, Freiberufler-Zuordnungs-Karte (Liste + Zuordnen-Eingabe + Löschen), Projekte-Karte (Liste)
- [ ] `partner/search-results.html` (HTMX Fragment): Treffertabelle (company, name1, name2, city) + sortierbare Header + Pagination-Trigger + Leer-Hinweis
- [ ] Banners: Kontaktsperre (rot), Ungespeicherte Änderungen (gelb, JS-gesteuert)
- [ ] Modale: Bestätigungs-Dialog (Löschen Partner), Konfliktdialog (Optimistic Locking), Projektliste-Fehler (Löschen verhindert), Freelancer-Partner-Konflikt-Dialog, Kontaktmöglichkeit-Modal, Historie-Modal
- [ ] Test: `@WebMvcTest PartnerControllerIT` prüft Template-Rendering (Thymeleaf-Integration)
- [ ] Git-Commit

---

## Phase 3 – Modul `freelancer`

### 3.1 Freelancer – Domain & Repository
- [ ] Aggregate `Freelancer` mit allen Feldern laut FREIBERUFLER.md (alle Gruppen); `@Version`, Audit-Felder; `partnerId` nullable FK
- [ ] `FreelancerRepository`
- [ ] Test: `FreelancerRepositoryIT`: CRUD, Optimistic-Locking-Fehler
- [ ] Git-Commit

### 3.2 Freelancer – CommandService
- [ ] `FreelancerCommandService`:
  - `save(Freelancer)` → mit Optimistic-Locking-Schutz
  - `delete(Long id)` → prüft `project_position.freelancer_id RESTRICT`, wirft `FreelancerHasPositionsException`; sonst löscht
  - `assignToProject(Long freelancerId, Long projectId, Long statusId, String konditionen, String kommentar)` → legt `ProjectPosition` an; prüft Duplicate-Zuordnung
- [ ] Test: `FreelancerCommandServiceIT`
- [ ] Git-Commit

### 3.3 Freelancer – QueryService
- [ ] `FreelancerQueryService`:
  - `findById(Long)`, `findFirst()`, `findLast()`, `findPrevious(Long)`, `findNext(Long)`
  - `search(FreelancerSearchCriteria)` → QBE über 19 Felder (LIKE für Strings, exakt für `kontaktart`), paginiert
  - `countSearch(FreelancerSearchCriteria)`
  - `findTagsByFreelancerId(Long)` → sortiert nach TagType-Reihenfolge, dann alphabetisch
  - `findAvailableTagsByFreelancerIdAndType(Long freelancerId, TagType)` → nur noch nicht zugeordnete Tags
- [ ] Test: `FreelancerQueryServiceIT`
- [ ] Git-Commit

### 3.4 Freelancer – Tags CommandService
- [ ] `FreelancerTagCommandService`:
  - `addTag(Long freelancerId, Long tagId)` → legt `FreelancerTag` an; wirft bei Duplicate
  - `removeTag(Long freelancerTagId)`
- [ ] Aggregate `FreelancerTag` (`id`, `freelancerId`, `tagId`, Audit-Felder); UNIQUE `(freelancer_id, tag_id)`
- [ ] `FreelancerTagRepository`
- [ ] Test: `FreelancerTagCommandServiceIT`
- [ ] Git-Commit

### 3.5 Freelancer – Controller
- [ ] `FreelancerController` mit:
  - GET `/freelancer`, GET `/freelancer/{id}`, POST `/freelancer/save`, POST `/freelancer/delete/{id}`, GET `/freelancer/new`
  - GET `/freelancer/first` / `last` / `previous/{id}` / `next/{id}`
  - POST `/freelancer/search`, GET `/freelancer/search-more`
  - POST `/freelancer/{id}/tags` → Tag hinzufügen; DELETE `/freelancer/{id}/tags/{tagId}` → Tag entfernen
  - GET `/freelancer/{id}/available-tags/{type}` → verfügbare Tags für Dropdown (JSON)
  - POST `/freelancer/{id}/assign-to-project` → Projektposition anlegen
- [ ] Test: `@WebMvcTest FreelancerControllerIT`
- [ ] Git-Commit

### 3.6 Freelancer – Kontaktmöglichkeiten
- [ ] Aggregate `FreelancerContact` + `FreelancerContactRepository` + `FreelancerContactCommandService` + `FreelancerContactQueryService`
- [ ] `FreelancerContactController` (AJAX)
- [ ] Test: `FreelancerContactRepositoryIT`, `FreelancerContactControllerIT`
- [ ] Git-Commit

### 3.7 Freelancer – Kontakthistorie
- [ ] Aggregate `FreelancerHistory` + Repository + `FreelancerHistoryCommandService` + `FreelancerHistoryQueryService`
- [ ] `FreelancerHistoryController` (AJAX)
- [ ] Test: `FreelancerHistoryRepositoryIT`, `FreelancerHistoryControllerIT`
- [ ] Git-Commit

### 3.8 Freelancer – Thymeleaf-Templates
- [ ] `freelancer/form.html`: alle Feldgruppen (Adresse inkl. Partner-Link, Kontaktinformationen inkl. Kontaktliste, Kommentar, Einsatzdetails, Zusatzinformationen, Verfügbarkeit & Konditionen, Kodierung + Tags + Skills), Toolbar + Navigation + Gemerktes-Projekt + „Dem Projekt zuordnen"-Button, Kontakthistorie-Sektion
- [ ] `freelancer/search-results.html` (HTMX Fragment): Treffertabelle (name1, name2, availabilityAsDate, salaryLong, skills, code, Tags als Chips)
- [ ] Banners, Modale (Löschen, Optimistic-Locking, Projektzuordnung-Modal, Tag-Chip-Entfernen, Duplicate-Positionsfehler)
- [ ] Test: `@WebMvcTest FreelancerControllerIT` Template-Rendering
- [ ] Git-Commit

---

## Phase 4 – Modul `kunde`

### 4.1 Kunde – Domain & Repository
- [ ] Aggregate `Kunde` mit allen Feldern laut KUNDEN.md; `@Version`, Audit-Felder
- [ ] `KundeRepository`
- [ ] Test: `KundeRepositoryIT`
- [ ] Git-Commit

### 4.2 Kunde – CommandService
- [ ] `KundeCommandService`: `save(Kunde)`, `delete(Long id)` (prüft `project.customer_id RESTRICT`)
- [ ] Test: `KundeCommandServiceIT`
- [ ] Git-Commit

### 4.3 Kunde – QueryService
- [ ] `KundeQueryService`: `findById`, Navigation (first/last/prev/next), `search(KundeSearchCriteria)`, `countSearch`, `findProjectsByKundeId(Long, sortField, sortDir)`
- [ ] Test: `KundeQueryServiceIT`
- [ ] Git-Commit

### 4.4 Kunde – Controller
- [ ] `KundeController`: GET/POST für alle CRUD-, Such- und Navigationsendpunkte (analog PartnerController)
- [ ] Test: `@WebMvcTest KundeControllerIT`
- [ ] Git-Commit

### 4.5 Kunde – Kontaktmöglichkeiten & Kontakthistorie
- [ ] `KundeContact` (Aggregate + Repository + CommandService + QueryService + Controller)
- [ ] `KundeHistory` (Aggregate + Repository + CommandService + QueryService + Controller)
- [ ] Tests: `KundeContactRepositoryIT`, `KundeHistoryRepositoryIT`, Controller-Tests
- [ ] Git-Commit

### 4.6 Kunde – Thymeleaf-Templates
- [ ] `kunde/form.html`: alle Feldgruppen, Toolbar, Projekte-Karte (Liste + „Neues Projekt erfassen"-Button), Kontakthistorie, Modale
- [ ] `kunde/search-results.html` (HTMX Fragment)
- [ ] Test: `@WebMvcTest KundeControllerIT` Template-Rendering
- [ ] Git-Commit

---

## Phase 5 – Modul `project`

### 5.1 Project – Domain & Repository
- [ ] Aggregate `Project` mit allen Feldern laut PROJEKTE.md (Gruppen: Allgemein, Beschreibung, Einsatz, Zuordnung, Konditionen); `@Version`, Audit-Felder; `customerId` und `partnerId` nullable FKs
- [ ] `ProjectRepository`
- [ ] Aggregate `RememberedProject` (`userId` PK, `projectId` FK)
- [ ] `RememberedProjectRepository`
- [ ] Test: `ProjectRepositoryIT`, `RememberedProjectRepositoryIT`
- [ ] Git-Commit

### 5.2 Project – Validierung (Zuordnung customer/partner)
- [ ] `ProjectValidator` (Spring `Validator`): `customerId` und `partnerId` dürfen nicht gleichzeitig gesetzt sein; wirft `ValidationException`
- [ ] Test: `ProjectValidatorSpec` (Unit-Test)
- [ ] Git-Commit

### 5.3 Project – CommandService
- [ ] `ProjectCommandService`:
  - `save(Project)` → validiert, speichert mit Optimistic Locking
  - `delete(Long id)` → löscht (keine RESTRICT-Checks nötig: kein FK auf project von außen mit RESTRICT); setzt `RememberedProject`-Einträge über `ON DELETE CASCADE` automatisch
  - `setRememberedProject(String userId, Long projectId)` → upsert in `remembered_project`
  - `getRememberedProject(String userId)` → Optional<Project>
- [ ] Test: `ProjectCommandServiceIT`
- [ ] Git-Commit

### 5.4 Project – QueryService
- [ ] `ProjectQueryService`: `findById`, Navigation (first/last/prev/next), `search(ProjectSearchCriteria)`, `countSearch` (QBE über: `projectNumber`, `descriptionShort`, `descriptionLong`, `skills`, `workplace`, `duration`, `status` exakt, `debitorNr`, `kreditorNr`); Suchergebnis-Felder: projectNumber, descriptionShort, workplace, startDate, status, stundensatzVK
- [ ] Test: `ProjectQueryServiceIT`
- [ ] Git-Commit

### 5.5 Project – Positionen (CommandService + QueryService)
- [ ] Aggregate `ProjectPosition` (`id`, `@Version db_version`, Audit-Felder, `projectId`, `freelancerId`, `statusId`, `konditionen`, `kommentar`)
- [ ] `ProjectPositionRepository`
- [ ] `ProjectPositionCommandService`: `save(ProjectPosition)` (Optimistic Locking), `delete(Long id)`
- [ ] `ProjectPositionQueryService`: `findByProjectId(Long, sortField, sortDir)` → JOIN freelancer + project_position_status; prüft Duplicate für neue Zuordnung
- [ ] Test: `ProjectPositionRepositoryIT`, `ProjectPositionCommandServiceIT`
- [ ] Git-Commit

### 5.6 Project – Kontakthistorie
- [ ] Aggregate `ProjectHistory` (`id`, `description`, `projectId` FK, Audit-Felder; **kein** `typeId` – keine Typisierung)
- [ ] `ProjectHistoryRepository` + `ProjectHistoryCommandService` + `ProjectHistoryQueryService`
- [ ] `ProjectHistoryController` (AJAX)
- [ ] Test: `ProjectHistoryRepositoryIT`, `ProjectHistoryControllerIT`
- [ ] Git-Commit

### 5.7 Project – Controller
- [ ] `ProjectController`:
  - GET `/project` → lädt zuletzt gemerktes Projekt (via `RememberedProjectRepository`), sonst leer (nur Suchmaske)
  - GET `/project/{id}` → lädt Projekt, setzt `rememberProject`
  - POST `/project/save` → speichert, Redirect zu `/project/{id}`; erst nach erstem Speichern wird Projekt gemerkt
  - POST `/project/delete/{id}` → löscht
  - GET `/project/first` / `last` / `previous/{id}` / `next/{id}` → Navigation + `rememberProject`
  - POST `/project/search`, GET `/project/search-more`
  - POST `/project/{id}/positions/{posId}` → Bearbeiten einer Position (Optimistic Locking)
  - DELETE `/project/{id}/positions/{posId}` → Löschen einer Position
  - GET `/project/{id}/positions` → JSON-Liste der Positionen (AJAX-Refresh nach Änderung)
  - GET `/project/new` → kein Neuanlage-Formular (Redirect zu `/project`); Zugang nur via Kunden/Partner-Formular-Link
  - GET `/project/new-from-kunde/{kundeId}` → öffnet leeres Formular mit vorausgefüllter `customerId`
  - GET `/project/new-from-partner/{partnerId}` → öffnet leeres Formular mit vorausgefüllter `partnerId`
- [ ] Test: `@WebMvcTest ProjectControllerIT`
- [ ] Git-Commit

### 5.8 Project – Thymeleaf-Templates
- [ ] `project/form.html`: alle Feldgruppen (Allgemein mit Status-Combobox, Beschreibung, Einsatz, Zuordnung als Read-only-Link nach Anlage, Konditionen), Toolbar mit Navigation + Gemerktes-Projekt setzen, Positionen-Karte (Tabelle mit Badges, Bearbeiten/Löschen), Kontakthistorie
- [ ] `project/search-results.html` (HTMX Fragment): Treffertabelle (projectNumber, descriptionShort, workplace, startDate, status, stundensatzVK)
- [ ] Modale: Löschen-Bestätigung, Optimistic-Locking-Konflikt, Positions-Bearbeiten-Modal (mit Optimistic-Locking), Positions-Löschen-Bestätigung
- [ ] Test: `@WebMvcTest ProjectControllerIT` Template-Rendering
- [ ] Git-Commit

---

## Phase 6 – Modul `profilesearch`

### 6.1 Profilsuche – Domain & Repository
- [ ] Aggregate `ProfileSearchChat` (`id`, `creationDate`, `creationUser`, `changedDate`, `title`, `projectId` nullable FK)
- [ ] Aggregate `ProfileSearchMessage` (`id`, `creationDate`, `chatId` FK, `role` Enum/String, `sequence`, `content`)
- [ ] `ProfileSearchChatRepository`, `ProfileSearchMessageRepository`
- [ ] Test: `ProfileSearchChatRepositoryIT`, `ProfileSearchMessageRepositoryIT`
- [ ] Git-Commit

### 6.2 Profilsuche – CommandService
- [ ] `ProfileSearchCommandService`:
  - `createChat(String userId, Long projectId)` → legt neue Sitzung an, gibt `id` zurück
  - `deleteChat(Long chatId)` → löscht Sitzung + alle Nachrichten (ON DELETE CASCADE)
  - `addMessage(Long chatId, String role, String content)` → legt Nachricht an, generiert `sequence`, aktualisiert `changedDate` der Sitzung; generiert `title` aus erster User-Nachricht (erste 60 Zeichen)
- [ ] Test: `ProfileSearchCommandServiceIT`
- [ ] Git-Commit

### 6.3 Profilsuche – QueryService
- [ ] `ProfileSearchQueryService`:
  - `findChatsByUser(String userId, int offset, int limit)` → sortiert nach `changed_date DESC`
  - `countChatsByUser(String userId)`
  - `findMessagesByChat(Long chatId)` → sortiert `sequence ASC`
  - `findLatestChatByUser(String userId)` → Optional (für Initialzustand)
  - `buildLlmContext(String userId)` → liest `remembered_project`, konstruiert Kontext-Objekt mit Projektdaten + Positionen + Freelancer + Tags (für KI-Anbindung vorbereitet, aber in Release 1.0 Stub)
- [ ] Test: `ProfileSearchQueryServiceIT`
- [ ] Git-Commit

### 6.4 Profilsuche – Controller
- [ ] `ProfileSearchController`:
  - GET `/profilesearch` → lädt neueste Sitzung des Nutzers oder erzeugt leere Sitzung; rendert `profilesearch/form.html`
  - GET `/profilesearch/chat/{chatId}` → wechselt zur Sitzung
  - POST `/profilesearch/chat/new` → `createChat`, Redirect zu `/profilesearch/chat/{id}`
  - DELETE `/profilesearch/chat/{chatId}` → löscht Sitzung; Redirect zu `/profilesearch`
  - GET `/profilesearch/chat/{chatId}/messages` → JSON-Liste aller Nachrichten (AJAX-Refresh)
  - POST `/profilesearch/chat/{chatId}/send` → speichert User-Nachricht; ruft (in Release 1.0 stubbed) LLM-Logik auf, speichert Assistant-Antwort; gibt JSON zurück
  - GET `/profilesearch/sidebar-more` → Infinite Scrolling für Sidebar-Liste (HTMX Fragment)
- [ ] Test: `@WebMvcTest ProfileSearchControllerIT`
- [ ] Git-Commit

### 6.5 Profilsuche – LLM-Stub (Release 1.0)
- [ ] Interface `LlmService` mit `sendMessage(LlmContext context, List<ChatMessage> history, String userMessage)` → `String`
- [ ] `StubLlmService` implements `LlmService`: gibt immer eine generische Antwort zurück (z. B. „Profilsuche-KI ist noch nicht konfiguriert.")
- [ ] `@Bean`-Konfiguration in `ProfileSearchConfig`
- [ ] Test: `StubLlmServiceSpec` (Unit-Test)
- [ ] Git-Commit

### 6.6 Profilsuche – Thymeleaf-Templates
- [ ] `profilesearch/form.html`: Split-Panel-Layout (§24 UI-DESIGNSYSTEM), Sidebar (§25), Nachrichten (§26), Eingabebereich (§27); responsive Sidebar-Toggle; Freelancer-Link-Rendering (`[freelancer:<id>:<text>]` → `<a href="/freelancer/{id}" target="_blank">`)
- [ ] Toolbar: Gemerktes-Projekt-Anzeige, „＋ Neuer Chat", „Löschen"-Button
- [ ] `profilesearch/sidebar-entry.html` (HTMX Fragment für Infinite Scroll)
- [ ] Modale: Chat-Löschen-Bestätigungsdialog
- [ ] Test: `@WebMvcTest ProfileSearchControllerIT` Template-Rendering
- [ ] Git-Commit

---

## Phase 7 – Modultest & Integration

### 7.1 Spring Modulith – Modulstruktur-Test
- [ ] `@ApplicationModuleTest` für jedes Modul: `partner`, `freelancer`, `kunde`, `project`, `profilesearch`, `stammdaten`
- [ ] Prüft, dass keine unerlaubten Querverweise zwischen Modulen existieren (kein cross-module Repository-Zugriff)
- [ ] Git-Commit

### 7.2 End-to-End-Integrationstest
- [ ] `ApplicationStartIT` extends `AbstractContainerBaseIT`: prüft, dass der Spring-Kontext mit MySQL hochfährt (`@SpringBootTest`)
- [ ] `FlywayMigrationIT`: prüft, dass alle Flyway-Migrationen erfolgreich angewendet werden und alle erwarteten Tabellen existieren
- [ ] Git-Commit

### 7.3 Sicherheitstest
- [ ] `SecurityIT` mit `@SpringBootTest` + `MockMvc`: prüft für alle Controller-Endpunkte, dass unauthentifizierter Zugriff zu `/login` redirected
- [ ] Test: CSRF-Schutz aktiv (POST ohne Token → 403)
- [ ] Git-Commit

---

## Phase 8 – Frontend-Build-Integration

### 8.1 Vite-Build
- [ ] `frontend/` Verzeichnis mit `package.json` (Vite als Dev-Dependency), `vite.config.js` (Output: `../src/main/resources/static/generated/`)
- [ ] Einstiegspunkt `frontend/src/main.js` (enthält `apiFetch`-Wrapper + Custom Elements für: Infinite Scroll, Modal-Management, Dirty-State-Banner, Chat-Textarea-Autosize)
- [ ] `mvn exec:exec` (npm install + npm run build) konfiguriert in `pom.xml` (bereits vorhanden)
- [ ] Test: Maven-Build läuft ohne Fehler durch (`./mvnw compile`)
- [ ] Git-Commit

### 8.2 Custom Elements
- [ ] `<ps-infinite-scroll>` Light-DOM Custom Element: beobachtet Intersection Observer am Ende einer Liste, lädt via `apiFetch` nächste Seite nach, fügt HTML-Fragment in Liste ein
- [ ] `<ps-modal>` Custom Element: zeigt/verbirgt Modal, fängt Escape-Key ab, sendet AJAX-Formulare via `apiFetch`
- [ ] `<ps-dirty-banner>` Custom Element: beobachtet Formular-Input-Events, zeigt/verbirgt „Ungespeicherte Änderungen"-Banner
- [ ] `<ps-chat-input>` Custom Element: Textarea-Autosize, Enter-zum-Senden, Shift+Enter-Zeilenumbruch, Senden-Button während Verarbeitung deaktiviert
- [ ] Test: (manuelle UI-Verifikation; kein automatischer JS-Test im Scope von Release 1.0)
- [ ] Git-Commit

---

## Abschluss

### 9.1 Abnahmekriterien
- [ ] `./mvnw clean verify` läuft durch (alle Unit-Tests + Integrationstests grün)
- [ ] Flyway-Migration startet sauber gegen frische MySQL-Instanz
- [ ] Login funktioniert mit `ps_user`-Testbenutzer
- [ ] Alle 5 Hauptmodule (Freiberufler, Partner, Kunden, Projekte, Profilsuche) sind per Browser nutzbar
- [ ] Git-Commit „chore: release 1.0 – all tasks completed"
