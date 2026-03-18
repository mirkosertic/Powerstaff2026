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
- [x] `SecurityConfig` mit BCrypt-`PasswordEncoder` Bean, Form-Login (`/login`), Logout (`/logout`), CSRF via `CookieCsrfTokenRepository.withHttpOnlyFalse()`, alle Endpunkte authenticated
- [x] Aggregate `PsUser` (`id`, `username` UNIQUE, `passwordHash`, `enabled`) im Paket `de.mirkosertic.powerstaff.auth`
- [x] `PsUserRepository` (Spring Data JDBC)
- [x] `PsUserDetailsService` implementiert `UserDetailsService`, lädt Nutzer per `PsUserRepository`
- [x] Test: `PsUserRepositoryIT` extends `AbstractContainerBaseIT` – speichert und liest `PsUser`
- [x] Test: `SecurityIT` (`@SpringBootTest` + MockMvc): unauthentifiziert → 302 `/login`; POST mit falschen Credentials → Fehlerseite; POST mit richtigen Credentials → Redirect
- [x] Git-Commit

### 0.2 Auditing
- [x] `AuditingConfig` implementiert `AuditorAware<String>`: liefert `Authentication::getName` aus `SecurityContextHolder`; gibt `"system"` zurück wenn kein Login aktiv
- [x] Test: `AuditingConfigSpec` (Spock Unit-Test, kein DB): mockt `SecurityContextHolder`, prüft dass `currentAuditor()` den Usernamen liefert
- [x] Git-Commit

### 0.3 CSS – Design Tokens und Layout
- [x] `src/main/resources/static/css/base.css`: alle CSS Custom Properties (Design Tokens §2), Typografie-Reset §3, Box-Sizing-Reset
- [x] `src/main/resources/static/css/layout.css`: Shell-Layout §4 (sticky App-Shell, sticky Toolbar, scrollbarer Content), App-Nav §5 (dunkle Leiste, aktiver Menüpunkt), Form-Toolbar §6, Banners §7 (info, warning, error)
- [x] Git-Commit

### 0.4 CSS – Komponenten
- [x] `src/main/resources/static/css/components.css`: fcard §8, field-grids §9 (col-1 bis col-wide), Input/Select/Textarea §9, Buttons §10 (btn-primary, btn-secondary, btn-danger, btn-sm), Checkboxen §11 (Pill-Style), Subsection-Label & Divider §12
- [x] `src/main/resources/static/css/components2.css`: Kontaktliste §13, Chips §14, Kontakthistorie §15, Tabellen §16 (sortierbare Header, Hover-Zeilen), Modals §17, dynamische Status-Badges §22, Read-only Link-Felder §20, Inline-Zuordnung §23
- [x] Git-Commit

### 0.5 Thymeleaf Basis-Fragmente
- [x] `src/main/resources/templates/fragments/layout.html`: App-Shell (nav + content-slot), App-Nav mit 5 Menüpunkten (Freiberufler `/freelancer`, Partner `/partner`, Kunden `/kunde`, Projekte `/project`, Profilsuche `/profilesearch`) + Logout-Link + aktivem Menüpunkt via `th:classappend`
- [x] `src/main/resources/templates/fragments/toolbar.html`: Form-Toolbar mit `th:fragment="toolbar(buttons, rememberedProject)"` – linker Slot für Buttons, rechts Gemerktes-Projekt-Anzeige §21 (Projektname + Projektnummer oder leer)
- [x] `src/main/resources/templates/fragments/modal.html`: generisches Modal-Grundgerüst §17 mit `th:fragment="modal(id, title, body, footer)"` – Overlay, Dialog-Box, Header, scrollbarer Body, Footer-Buttons
- [x] `src/main/resources/templates/fragments/contact-list.html`: wiederverwendbare Kontaktlisten-Darstellung §13 – iteriert über `contacts` (Liste mit `type`, `value`), rendert je Typ das korrekte Label + Link-URL gemäß STAMMDATEN.md (mailto/tel/http/xing/gulp), sortierbar nach `ContactType`-Reihenfolge; `th:fragment="contactList(contacts, editUrl, deleteUrl)"`
- [x] `src/main/resources/templates/login.html`: Login-Formular ohne App-Nav, zentriert, Fehleranzeige bei `?error`
- [x] Test: `@WebMvcTest` mit einem Stub-Controller der jedes Fragment einbindet – prüft 200 und keine Template-Fehler (Thymeleaf-Parsing)
- [x] Git-Commit

### 0.6 Frontend-Build (Vite + apiFetch)
- [x] `frontend/package.json` mit Vite als devDependency, `build`-Script
- [x] `frontend/vite.config.js`: Einstiegspunkt `src/main.js`, Output `../src/main/resources/static/generated/`
- [x] `frontend/src/main.js`: `apiFetch(url, options)` – liest CSRF-Token aus Cookie `XSRF-TOKEN`, setzt Header `X-XSRF-TOKEN` bei nicht-GET-Requests; exportiert `apiFetch` als globale Funktion auf `window`
- [x] Test: `./mvnw compile` baut durch (exec-Plugin: npm install + npm run build)
- [x] Git-Commit

### 0.7 Custom Element `<ps-modal>`
- [x] `frontend/src/ps-modal.js`: Light-DOM Custom Element; Attribute `open` zeigt/verbirgt Modal; Escape-Key schließt; Klick auf Overlay schließt; emittiert `ps-modal-close` Event; Methoden `show()`, `close()`; AJAX-Form-Submit via `apiFetch` mit optionalem `data-confirm-url`-Attribut
- [x] Test: `./mvnw compile` (npm build muss durchlaufen)
- [x] Git-Commit

### 0.8 Custom Element `<ps-dirty-banner>`
- [x] `frontend/src/ps-dirty-banner.js`: beobachtet `input`/`change`-Events auf dem nächstgelegenen `<form>` via Event-Delegation; setzt/entfernt CSS-Klasse `visible` auf dem Banner-Element mit `data-dirty-banner`-Attribut; setzt Dirty-State zurück nach erfolgreichem Form-Submit
- [x] Git-Commit

### 0.9 Custom Element `<ps-infinite-scroll>`
- [x] `frontend/src/ps-infinite-scroll.js`: Attribute `data-next-url` und `data-target`; `IntersectionObserver` auf einem Sentinel-Element am Listenende; bei Sichtbarkeit: `apiFetch(data-next-url)` → HTML-Fragment in `data-target` einhängen; aktualisiert `data-next-url` aus Response-Header `X-Next-Url` (oder entfernt Observer wenn kein weiterer Header)
- [x] Git-Commit

### 0.10 Custom Element `<ps-chat-input>`
- [x] `frontend/src/ps-chat-input.js`: wraps `<textarea>` + Send-Button; Textarea-Autosize (max 6 Zeilen); Enter → submit, Shift+Enter → Zeilenumbruch; während `pending`-Attribut gesetzt: Button disabled + Textarea readonly; emittiert `ps-send`-Event mit `detail.text`
- [x] Git-Commit

### 0.11 Stammdaten-Enums (Shared Domain)
- [x] Enum `ContactType` (`EMAIL, WEB, XING, GULP, TELEFON, FAX`) mit `getLabel()`, `buildLink(String value)` im Paket `de.mirkosertic.powerstaff.shared`
- [x] Enum `TagType` (`SCHWERPUNKT(0), FUNKTION(1), EINSATZORT(2), BEMERKUNG(3), TYP(4)`) mit `getLabel()`
- [x] Enum `ProjectStatus` (`OFFEN(1), VERLOREN(2), CANCELED(3), BESETZT(4), SEARCH_ZU(5)`) mit `fromInt(int)`, `getLabel()`
- [x] Test: `ContactTypeSpec`, `TagTypeSpec`, `ProjectStatusSpec` (Spock Unit-Tests, kein DB): prüfen Label, Link-URL-Generierung, `fromInt`-Mapping
- [x] Git-Commit

---

## Phase 1 – Modul `stammdaten`

### 1.1 Historientypen – Domain + Repository
- [x] Aggregate `HistoryType` (`id` BIGINT PK, `description` VARCHAR) im Paket `de.mirkosertic.powerstaff.shared.command`
- [x] `HistoryTypeRepository` extends `CrudRepository<HistoryType, Long>`
- [x] `HistoryTypeQueryService`: `findAll()` via `JdbcClient` – `ORDER BY description ASC`
- [x] Test: `HistoryTypeRepositoryIT` extends `AbstractContainerBaseIT`: insert, findById, findAll; `HistoryTypeQueryServiceIT`: prüft Sortierung
- [x] Git-Commit

### 1.2 Projektpositions-Status – Domain + Repository
- [x] Aggregate `ProjectPositionStatus` (`id`, `description`, `color`, `colorText` ← Spalte `color_text`)
- [x] `ProjectPositionStatusRepository`
- [x] `ProjectPositionStatusQueryService`: `findAll()` sortiert nach `description ASC`
- [x] Test: `ProjectPositionStatusRepositoryIT`, `ProjectPositionStatusQueryServiceIT`
- [x] Git-Commit

### 1.3 Tags – Domain + Repository
- [x] Aggregate `Tag` (`id`; `tagname` ← Spalte `tagname`; `type` ← Spalte `type` als `String`)
- [x] `TagRepository`
- [x] `TagQueryService`: `findAll()` sortiert nach `tagname ASC`; `findByType(TagType)` filtert per `WHERE type = :type`
- [x] Test: `TagRepositoryIT`, `TagQueryServiceIT`: prüft Filterung und Sortierung nach Typ
- [x] Git-Commit

### 1.4 Stammdaten-Administration – Historientypen UI
- [x] `StammdatenController`: GET `/admin/historientypen` → `model.addAttribute("types", historyTypeQueryService.findAll())`; rendert `admin/historientypen.html`
- [x] POST `/admin/historientypen` → speichert neuen `HistoryType`; Redirect
- [x] POST `/admin/historientypen/{id}` → aktualisiert `description`; Redirect
- [x] Template `admin/historientypen.html`: Tabelle mit allen Typen, Bearbeiten-Button öffnet Modal, Neuanlage-Button öffnet Modal
- [x] Test: `StammdatenControllerIT`: GET → 200, POST → Redirect 302, Template rendert ohne Fehler
- [x] Git-Commit

### 1.5 Stammdaten-Administration – Projektpositions-Status UI
- [x] GET `/admin/positionsstatus`, POST `/admin/positionsstatus`, POST `/admin/positionsstatus/{id}` in `StammdatenController`
- [x] Template `admin/positionsstatus.html`: Tabelle mit Badge-Vorschau (Inline-Style), Modale für Neuanlage/Bearbeitung mit Farbfelder-Inputs
- [x] Test: `StammdatenControllerIT` ergänzt
- [x] Git-Commit

### 1.6 Stammdaten-Administration – Tags UI
- [x] GET `/admin/tags`, POST `/admin/tags`, POST `/admin/tags/{id}`, DELETE `/admin/tags/{id}` (AJAX → JSON `{ok:true}`) in `StammdatenController`
- [x] Template `admin/tags.html`: Tags gruppiert nach `TagType`-Abschnitt, je Gruppe Tabelle + Neuanlage-Formular + Löschen-Button (AJAX)
- [x] Test: `StammdatenControllerIT` ergänzt (inkl. DELETE → 200 JSON)
- [x] Git-Commit

---

## Phase 2 – Modul `partner`

### 2.1 Partner – Domain & Repository
- [x] Aggregate `Partner` im Paket `de.mirkosertic.powerstaff.partner` mit allen Feldern gemäß PARTNER.md (Gruppen Adresse, Kontaktinformationen, Kommentar, Konditionen); `@Version` auf `dbVersion` ← Spalte `db_version`; `@CreatedDate creationDate`, `@LastModifiedDate lastModificationDate`, `@CreatedBy creationUserId`, `@LastModifiedBy lastModificationUserId`
- [x] `PartnerRepository` extends `CrudRepository<Partner, Long>`
- [x] Test: `PartnerRepositoryIT` extends `AbstractContainerBaseIT`: insert, findById, update (prüft `db_version` Inkrement), Optimistic-Locking-Konflikt (zwei parallele saves auf gleicher Version) → `OptimisticLockingFailureException`
- [x] Git-Commit

### 2.2 Partner – CommandService (save + delete)
- [x] `PartnerCommandService.save(Partner)`: speichert via `PartnerRepository`; fängt `OptimisticLockingFailureException` und wirft eigene `OptimisticLockingException` (enthält `changedBy`, `changedDate` des aktuellen DB-Stands)
- [x] `PartnerCommandService.delete(Long id)`: liest alle `project.partner_id = id` Einträge via `JdbcClient`; wenn vorhanden → wirft `EntityHasProjectsException(List<String> projectNumbers)`; sonst `partnerRepository.deleteById(id)`
- [x] `EntityHasProjectsException` als gemeinsame Exception-Klasse im Paket `de.mirkosertic.powerstaff.shared`
- [x] Test: `PartnerCommandServiceIT` extends `AbstractContainerBaseIT`: save neu, save update, delete ohne Projekte, delete mit Projekten → Exception mit Projektnummern
- [x] Git-Commit

### 2.3 Partner – QueryService (Navigation + findById)
- [x] `PartnerQueryService` via `JdbcClient`:
  - `findById(Long)` → `Optional<Partner>`
  - `findFirst()` → `Optional<Partner>` (`ORDER BY id ASC LIMIT 1`)
  - `findLast()` → `Optional<Partner>` (`ORDER BY id DESC LIMIT 1`)
  - `findPrevious(Long currentId)` → `Optional<Partner>` (`WHERE id < :id ORDER BY id DESC LIMIT 1`)
  - `findNext(Long currentId)` → `Optional<Partner>` (`WHERE id > :id ORDER BY id ASC LIMIT 1`)
- [x] Test: `PartnerQueryServiceIT`: legt 3 Partner an, prüft alle 5 Navigationsmethoden
- [x] Git-Commit

### 2.4 Partner – QueryService (QBE-Suche + Sublisten)
- [x] Record `PartnerSearchCriteria(String company, String name1, String name2, String street, String country, String plz, String city, String comments, String kreditorNr, String debitorNr)`
- [x] Record `PartnerSearchResult(Long id, String company, String name1, String name2, String city)` für Suchergebnis-Liste
- [x] `PartnerQueryService.search(PartnerSearchCriteria criteria, int offset, int limit)`: baut SQL mit `WHERE`-Klausel dynamisch über nicht-null/nicht-leere Felder (`LIKE '%:wert%'` per `JdbcClient` mit `SqlParameterSource`); niemals String-Konkatenation; `ORDER BY company ASC`
- [x] `PartnerQueryService.countSearch(PartnerSearchCriteria criteria)` → `long`
- [x] `PartnerQueryService.findFreelancersByPartnerId(Long partnerId, String sortField, String sortDir)` → `List<FreelancerListItem>` (Record mit: `id, code, name1, name2, company, availabilityAsDate, salaryLong`)
- [x] `PartnerQueryService.findProjectsByPartnerId(Long partnerId, String sortField, String sortDir)` → `List<ProjectListItem>` (Record mit: `id, projectNumber, descriptionShort, workplace, startDate, status`)
- [x] Test: `PartnerQueryServiceIT` ergänzt: QBE mit einem Feld (Treffer), QBE mit zwei Feldern (AND), QBE ohne Treffer, Sortierung Freelancer-Liste
- [x] Git-Commit

### 2.5 Partner – Kontaktmöglichkeiten (Domain + Service)
- [x] Aggregate `PartnerContact` (`id`, `type` als `String` ← Spalte `type`, `value`, `partnerId`, Audit-Felder `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy`)
- [x] `PartnerContactRepository`
- [x] `PartnerContactCommandService`: `save(PartnerContact)`, `delete(Long contactId)`
- [x] `PartnerContactQueryService`: `findByPartnerId(Long partnerId)` → sortiert nach `ContactType`-Reihenfolge (CASE WHEN in SQL), dann `id ASC`
- [x] Test: `PartnerContactRepositoryIT`: insert, findByPartnerId (Sortierung), delete
- [x] Git-Commit

### 2.6 Partner – Kontakthistorie (Domain + Service)
- [x] Aggregate `PartnerHistory` (`id`, `description`, `typeId` ← `type_id` FK → `historytype`, `partnerId`, Audit-Felder)
- [x] `PartnerHistoryRepository`
- [x] `PartnerHistoryCommandService`: `save(PartnerHistory)`, `delete(Long historyId)`
- [x] `PartnerHistoryQueryService`: `findByPartnerId(Long partnerId)` → JOIN `historytype` für `typeDescription`, sortiert `creation_date DESC`
- [x] Test: `PartnerHistoryRepositoryIT`: insert mit `typeId`, findByPartnerId (Sortierung), delete
- [x] Git-Commit

### 2.7 Partner – Controller (CRUD + Navigation)
- [x] `PartnerController`:
  - GET `/partner`: lädt letzten Partner aus Session-Attribut `lastPartnerId`; wenn nicht vorhanden → `partnerQueryService.findFirst()`; Model: `partner`, `rememberedProject`; rendert `partner/form.html`
  - GET `/partner/{id}`: lädt Partner, setzt Session `lastPartnerId`; rendert `partner/form.html`
  - GET `/partner/new`: löscht Session `lastPartnerId`; rendert `partner/form.html` mit leerem `Partner`
  - GET `/partner/first` / `/partner/last` / `/partner/previous/{id}` / `/partner/next/{id}`: Navigation → Redirect zu `/partner/{id}` oder `/partner/new` wenn kein Ergebnis
  - POST `/partner/save`: bindet `@ModelAttribute Partner`; ruft `partnerCommandService.save()`; bei `OptimisticLockingException` → JSON `{"conflict": true, "changedBy": "...", "changedDate": "..."}` mit Status 409; bei Erfolg → Redirect zu `/partner/{id}`
  - POST `/partner/delete/{id}`: ruft `partnerCommandService.delete()`; bei `EntityHasProjectsException` → JSON `{"blocked": true, "projectNumbers": [...]}` mit Status 409; bei Erfolg → Redirect zu `/partner/new`
- [x] Test: `@WebMvcTest PartnerControllerIT` mit `@MockBean` für Services: GET `/partner` → 200; GET `/partner/{id}` → 200; Navigation → 302; POST `/partner/save` Erfolg → 302; POST `/partner/save` Konflikt → 409 JSON; POST `/partner/delete` geblockt → 409 JSON
- [x] Git-Commit

### 2.8 Partner – Controller (Suche + Freelancer-Zuordnung)
- [x] POST `/partner/search`: bindet `PartnerSearchCriteria`; speichert Criteria in Session; ruft `search(criteria, 0, 20)`; rendert `partner/search-results.html` als Fragment
- [x] GET `/partner/search-more`: liest Criteria aus Session, `offset` aus Query-Param; ruft `search(criteria, offset, 20)`; setzt Response-Header `X-Next-Url` wenn weitere Treffer vorhanden; rendert Fragment
- [x] POST `/partner/{id}/assign-freelancer`: Body `{code: "..."}` (JSON); sucht Freelancer per Code via `JdbcClient`; wenn nicht gefunden → 404 JSON; wenn bereits anderem Partner zugeordnet → 409 JSON `{"otherPartner": "Firmenname"}`; sonst: UPDATE `freelancer.partner_id`; → 200 JSON mit aktualisierter Freelancer-Liste
- [x] POST `/partner/{id}/confirm-reassign-freelancer`: Body `{freelancerId: ...}` (JSON); überschreibt `freelancer.partner_id` ohne Konfliktprüfung; → 200 JSON
- [x] POST `/partner/{id}/remove-freelancer/{freelancerId}`: setzt `freelancer.partner_id = NULL` per `JdbcClient`; → 200 JSON
- [x] Test: `PartnerControllerIT` ergänzt: POST search → Fragment-HTML; search-more mit Offset; assign-freelancer Szenarien
- [x] Git-Commit

### 2.9 Partner – Kontakt- und Historien-Controller (AJAX)
- [x] `PartnerContactController`: POST `/partner/{id}/contacts` (JSON Body: `type`, `value`) → speichert, gibt aktualisierte Kontaktliste als JSON zurück; DELETE `/partner/{id}/contacts/{contactId}` → löscht, gibt aktualisierte Liste zurück
- [x] `PartnerHistoryController`: POST `/partner/{id}/history` (JSON Body: `typeId`, `description`) → speichert; PUT `/partner/{id}/history/{hId}` → aktualisiert; DELETE `/partner/{id}/history/{hId}` → löscht; jeweils → JSON `{ok: true}` oder Fehler
- [x] Test: `PartnerContactControllerIT`, `PartnerHistoryControllerIT` mit MockMvc + `@MockBean`
- [x] Git-Commit

### 2.10 Partner – Thymeleaf Template: Hauptformular
- [x] `partner/form.html`: bindet `fragments/layout.html`; `fragments/toolbar.html` mit Navigations-Buttons (Erste, Zurück, Weiter, Letzte, ID-Eingabe), Neu-Button, Speichern-Button, Löschen-Button; Formular-Karten für Adresse, Kontaktinformationen, Kommentar, Konditionen; Audit-Info-Zeile (Erfasst am/von, Geändert am/von); bindet `fragments/contact-list.html` für Kontaktmöglichkeiten
- [x] Banner: Kontaktsperre-Banner (rot, `th:if="${partner.contactforbidden}"`) + `<ps-dirty-banner>`-Integration
- [x] Git-Commit

### 2.11 Partner – Thymeleaf Template: Sublisten und Suche
- [x] `partner/form.html` ergänzt: Freiberufler-Zuordnungs-Karte (fcard `col-wide`): Tabelle (code, name1, name2, company, availabilityAsDate, salaryLong, Löschen-Button), Zuordnen-Eingabefeld + Button, Neu-Freiberufler-Button; Projekte-Karte (fcard `col-wide`): Tabelle (projectNumber, descriptionShort, workplace, startDate, status), Neu-Projekt-Button
- [x] `partner/search-results.html` (reines Fragment, kein vollständiges HTML): Treffertabelle (company, name1, name2, city) mit sortierbaren Spalten-Headern (Links mit `?sort=`-Parameter), Gesamtanzahl, `<ps-infinite-scroll>`-Sentinel, Leer-Hinweis
- [x] Git-Commit

### 2.12 Partner – Thymeleaf Template: Kontakthistorie und Modals
- [x] `partner/form.html` ergänzt: Kontakthistorie-Sektion (Liste der `PartnerHistory`-Einträge: Typ, Erfasst am/von, Geändert am/von wenn abweichend, Beschreibungstext, Bearbeiten- und Löschen-Button)
- [x] Modale eingebettet in `form.html` via `fragments/modal.html`: Löschen-Bestätigung (Partner), Löschen-Bestätigung (Freiberufler-Zuordnung aufheben), Projektliste-Fehler-Dialog (Löschen verhindert), Freelancer-Konflikt-Dialog (bereits anderem Partner zugeordnet), Kontaktmöglichkeit-Modal (Neuanlage/Bearbeitung), Kontakthistorie-Modal (Neuanlage/Bearbeitung), Optimistic-Locking-Konflikt-Dialog
- [x] Test: `@WebMvcTest PartnerControllerIT` ergänzt: Template rendert vollständig mit/ohne Partner-Daten; alle `th:`-Attribute greifen korrekt
- [x] Git-Commit

---

## Phase 3 – Modul `freelancer`

### 3.1 Freelancer – Domain & Repository
- [x] Aggregate `Freelancer` im Paket `de.mirkosertic.powerstaff.freelancer` mit allen Feldern gemäß FREIBERUFLER.md (alle Gruppen); `@Version dbVersion`; Audit-Felder; `partnerId` nullable FK
- [x] `FreelancerRepository`
- [x] Test: `FreelancerRepositoryIT`: CRUD, Optimistic-Locking-Konflikt → `OptimisticLockingFailureException`
- [x] Git-Commit

### 3.2 Freelancer – Tags (Domain + Repository)
- [x] Aggregate `FreelancerTag` (`id`, `freelancerId`, `tagId`, Audit-Felder); UNIQUE-Constraint `(freelancer_id, tag_id)`
- [x] `FreelancerTagRepository`
- [x] `FreelancerTagCommandService`: `addTag(Long freelancerId, Long tagId)` → wirft `DuplicateTagException` bei Violation des UNIQUE-Index; `removeTag(Long freelancerTagId)`
- [x] Test: `FreelancerTagRepositoryIT`: insert, findByFreelancerId, delete, Duplicate → Exception
- [x] Git-Commit

### 3.3 Freelancer – CommandService
- [x] `FreelancerCommandService.save(Freelancer)`: speichert, fängt `OptimisticLockingFailureException` → wirft `OptimisticLockingException`
- [x] `FreelancerCommandService.delete(Long id)`: prüft Einträge in `project_position` via `JdbcClient`; wenn vorhanden → wirft `FreelancerHasPositionsException(List<String> projectNumbers)`; sonst `freelancerRepository.deleteById(id)`
- [x] Test: `FreelancerCommandServiceIT`: save neu, save update, delete ohne Positionen, delete mit Positionen → Exception
- [x] Git-Commit

### 3.4 Freelancer – QueryService (Navigation + findById)
- [x] `FreelancerQueryService.findById(Long)`, `findFirst()`, `findLast()`, `findPrevious(Long)`, `findNext(Long)` – analog zu `PartnerQueryService`
- [x] Test: `FreelancerQueryServiceIT` (Navigation, 3 Datensätze)
- [x] Git-Commit

### 3.5 Freelancer – QueryService (QBE-Suche)
- [x] Record `FreelancerSearchCriteria` mit 19 Feldern gemäß FREIBERUFLER.md (alle Suchfelder)
- [x] Record `FreelancerSearchResult` (10 Felder für Listansicht)
- [x] `FreelancerQueryService.search(FreelancerSearchCriteria, int offset, int limit)`: LIKE für Strings, <= für Salary-Felder; `ORDER BY name1 ASC, name2 ASC`
- [x] `FreelancerQueryService.countSearch(FreelancerSearchCriteria)` → `long`
- [x] Test: `FreelancerQueryServiceSearchIT`: 9 Tests (ein Feld, AND, kein Treffer, skills LIKE, salary, pagination, countSearch)
- [x] Git-Commit

### 3.6 Freelancer – QueryService (Tags)
- [x] Record `TagInfo(Long id, String name, TagType type)`
- [x] `FreelancerQueryService.findTagsByFreelancerId(Long freelancerId)` → `List<TagInfo>` sortiert nach `TagType`-Ordinal, dann `tagname ASC`
- [x] `FreelancerQueryService.findAvailableTagsByFreelancerIdAndType(Long freelancerId, TagType type)` → Tags dieser Gruppe, die der Freiberufler noch NICHT hat; sortiert `tagname ASC`
- [x] Test: `FreelancerQueryServiceTagsIT`: 6 Tests (Tags zugeordnet, verfügbare Tags, Sortierung)
- [x] Git-Commit

### 3.7 Freelancer – Kontaktmöglichkeiten (Domain + Service)
- [x] Aggregate `FreelancerContact` (`id`, `type` String, `value`, `freelancerId`, Audit-Felder)
- [x] `FreelancerContactRepository`
- [x] `FreelancerCommandService.save(Freelancer, contacts, history)` – Unified Save (Audit Trail!)
- [x] `FreelancerQueryService.findContactsByFreelancerId(Long)` → sortiert type ASC, value ASC
- [x] Test: `FreelancerContactQueryIT`: 3 Tests (gespeichert, leer, Sortierung)
- [x] Git-Commit

### 3.8 Freelancer – Kontakthistorie (Domain + Service)
- [x] Aggregate `FreelancerHistory` (`id`, `description`, `typeId`, `freelancerId`, Audit-Felder)
- [x] `FreelancerHistoryRepository`
- [x] `FreelancerQueryService.findHistoryByFreelancerId(Long)` → JOIN historytype, ORDER BY creation_date DESC
- [x] Test: `FreelancerHistoryQueryIT`: 3 Tests (gespeichert, leer, Sortierung DESC)
- [x] Git-Commit

### 3.9 Freelancer – Controller (CRUD + Navigation)
- [x] `FreelancerController`: GET /freelancer, /freelancer/{id}, /freelancer/new
- [x] GET /freelancer/first, /last, /previous/{id}, /next/{id}
- [x] POST /freelancer/save (Unified Save, 409 Optimistic-Locking)
- [x] POST /freelancer/delete/{id} (409 JSON bei FreelancerHasPositionsException)
- [x] Cookie lastFreelancerId (30 Tage, path=/freelancer), kein HttpSession
- [x] Test: `FreelancerControllerIT`: 17 Tests
- [x] Git-Commit

### 3.10 Freelancer – Controller (Suche + Tags)
- [x] POST /freelancer/search, GET /freelancer/search-more (analog Partner)
- [x] POST /freelancer/{id}/tags (409 bei DuplicateTagException)
- [x] DELETE /freelancer/{id}/tags/{freelancerTagId}
- [x] GET /freelancer/{id}/available-tags/{type}
- [x] Test: FreelancerControllerIT ergänzt (in 3.9 enthalten)
- [x] Git-Commit

### 3.11 Freelancer – Kontakt- und Historien-Controller (AJAX)
- [x] Kein separater Controller – Unified-Save-Muster (Kontakte+Historie als JSON hidden-fields)
- [x] Git-Commit (entfällt als separater Task)

### 3.12 Freelancer – Thymeleaf Template: Hauptformular
- [x] `freelancer/form.html`: vollständig mit Toolbar, allen Feldgruppen, Tags-Sektion, Banners
- [x] Karte Adresse (inkl. Partner-Link Read-only), Kontaktinformationen, Kommentar, Einsatzdetails
- [x] Karte Verfügbarkeit & Konditionen; Karte Kodierung (code, Tags je TagType, skills)
- [x] Tags-Sektion: Chip-Liste mit ×-Button + AJAX-Dropdown (verfügbare Tags)
- [x] Banners: Kontaktsperre (rot), ps-dirty-banner
- [x] Test: FreelancerControllerIT prüft Template-Rendering (in 3.9 enthalten)
- [x] Git-Commit

### 3.13 Freelancer – Thymeleaf Template: Sublisten, Suche und Modals
- [x] `freelancer/form.html` ergänzt: Kontaktmöglichkeiten-Sektion, Kontakthistorie-Sektion
- [x] `freelancer/search-results.html`: Fragment mit Treffertabelle, Infinite Scroll
- [x] Modale: Löschen, Löschen-Blockiert, Kontaktmöglichkeit, Kontakthistorie, Optimistic-Locking
- [x] Test: FreelancerControllerIT (in 3.9 enthalten)
- [x] Git-Commit

---

## Phase 4 – Modul `kunde`

### 4.1 Kunde – Domain & Repository
- [x] Aggregate `Kunde` mit allen Feldern gemäß KUNDEN.md; `@Version`, Audit-Felder
- [x] `KundeRepository`
- [x] Test: `KundeRepositoryIT`: CRUD, Optimistic-Locking-Konflikt
- [x] Git-Commit

### 4.2 Kunde – CommandService
- [x] `KundeCommandService.save(Kunde)`: mit Optimistic-Locking-Handling
- [x] `KundeCommandService.delete(Long id)`: prüft `project.customer_id`; bei Projekten → wirft `EntityHasProjectsException`; sonst löscht
- [x] Test: `KundeCommandServiceIT`: save, delete frei, delete geblockt
- [x] Git-Commit

### 4.3 Kunde – QueryService
- [x] `KundeQueryService`: `findById`, `findFirst`, `findLast`, `findPrevious`, `findNext` (analog Partner)
- [x] `search(KundeSearchCriteria, offset, limit)`, `countSearch` (QBE über: company, name1, name2, street, country, plz, city, comments, kreditorNr, debitorNr)
- [x] `findProjectsByKundeId(Long kundeId, String sortField, String sortDir)` → `List<ProjectListItem>`
- [x] Test: `KundeQueryServiceIT`: Navigation, QBE, Projektliste
- [x] Git-Commit

### 4.4 Kunde – Kontaktmöglichkeiten (Domain + Service)
- [x] Aggregate `KundeContact` (`id`, `type` String, `value`, `kundeId`, Audit-Felder)
- [x] `KundeContactRepository`
- [-] `KundeContactCommandService`: entfällt – Unified-Save-Muster in KundeCommandService
- [x] `KundeContactQueryService`: `findContactsByKundeId` in KundeQueryService, sortiert type ASC, value ASC
- [x] Test: `KundeContactQueryIT`
- [x] Git-Commit

### 4.5 Kunde – Kontakthistorie (Domain + Service)
- [x] Aggregate `KundeHistory` (`id`, `description`, `typeId`, `kundeId`, Audit-Felder)
- [x] `KundeHistoryRepository`
- [-] `KundeHistoryCommandService`: entfällt – Unified-Save-Muster in KundeCommandService
- [x] `KundeHistoryQueryService`: `findHistoryByKundeId` in KundeQueryService, JOIN historytype, sortiert creation_date DESC
- [x] Test: `KundeHistoryQueryIT`
- [x] Git-Commit

### 4.6 Kunde – Controller (CRUD + Navigation)
- [x] `KundeController`:
  - GET `/kunde`, GET `/kunde/{id}` (Cookie `lastKundeId`), GET `/kunde/new`
  - GET `/kunde/first` / `last` / `previous/{id}` / `next/{id}`
  - POST `/kunde/save` (409 JSON bei Optimistic-Locking-Konflikt, Unified Save)
  - POST `/kunde/delete/{id}` (409 JSON bei KundeHasProjectsException)
- [x] Test: `KundeControllerIT` (14 Tests)
- [x] Git-Commit

### 4.7 Kunde – Controller (Suche + AJAX)
- [x] POST `/kunde/search`, GET `/kunde/search-more`
- [-] `KundeContactController`/`KundeHistoryController` entfallen – Unified-Save-Muster
- [x] Test: KundeControllerIT (in 4.6 enthalten)
- [x] Git-Commit

### 4.8 Kunde – Thymeleaf Templates
- [x] `kunde/form.html`: Shell, Toolbar, alle Feldkarten (Adresse, Kontaktinformationen, Kommentar, Konditionen), Projekte-Karte (col-wide), Kontaktmöglichkeiten, Kontakthistorie
- [x] Banners: Kontaktsperre, ps-dirty-banner; Modale: Löschen, Projektlisten-Fehler, Optimistic-Locking, Kontaktmöglichkeit, Kontakthistorie
- [x] `kunde/search-results.html`: Fragment mit Treffertabelle (company, name1, name2, city), Infinite Scroll
- [x] Test: KundeControllerIT Template-Rendering (in 4.6 enthalten)
- [x] Git-Commit

---

## Phase 5 – Modul `project`

### 5.1 Project – Domain & Repository
- [x] Aggregate `Project` im Paket `de.mirkosertic.powerstaff.project` mit allen Feldern; `@Version dbVersion`; Audit-Felder; `customerId` nullable; `partnerId` nullable
- [x] `ProjectRepository` (package-private)
- [x] `ProjectCommandService` (save, findById, deleteById – wird in 5.4 erweitert)
- [x] Test: `ProjectRepositoryIT`: CRUD, nullable FKs, Optimistic-Locking-Konflikt
- [x] Git-Commit

### 5.2 Project – RememberedProject (Domain + Repository)
- [x] Aggregate `RememberedProject` (userId als @Id String PK via Persistable<String>; projectId BIGINT FK)
- [x] `RememberedProjectRepository` (package-private)
- [x] `RememberedProjectService`: set (Upsert via existsById + isNew-Flag), get, clear
- [x] Test: `RememberedProjectRepositoryIT`: set, get, Upsert, clear
- [x] Git-Commit

### 5.3 Project – Validierung
- [x] `ProjectValidator` implements Spring `Validator` (@Component): customerId + partnerId gleichzeitig → global error "project.bothFks"
- [x] Test: `ProjectValidatorSpec` (Spock Unit-Test): beide null, nur customer, nur partner, beide gesetzt → Fehler, supports()
- [x] Git-Commit

### 5.4 Project – CommandService
- [x] `ProjectCommandService.save(Project)`: Validierung via ProjectValidator → BothFKsException; dann speichern
- [x] `ProjectCommandService.deleteById(Long id)`: ON DELETE CASCADE erledigt Positionen/Historie/RememberedProject
- [x] `BothFKsException` (RuntimeException)
- [x] Test: `ProjectCommandServiceIT`: save valid, save BothFKs, update, Optimistic Locking, delete
- [x] Git-Commit

### 5.5 Project – QueryService (Navigation + findById)
- [x] `ProjectQueryService.findById(Long)`, `findFirst()`, `findLast()`, `findPrevious(Long)`, `findNext(Long)`
- [x] Test: `ProjectQueryServiceIT` (Navigation)
- [x] Git-Commit

### 5.6 Project – QueryService (QBE-Suche)
- [x] Record `ProjectSearchCriteria` (projectNumber, descriptionShort, descriptionLong, skills, workplace, duration, Integer status, debitorNr, kreditorNr)
- [x] Record `ProjectSearchResult` (id, projectNumber, descriptionShort, workplace, startDate, status, stundensatzVK)
- [x] `ProjectQueryService.search(ProjectSearchCriteria, offset, limit)`: `status` → exakter Vergleich; alle anderen → LIKE; `ORDER BY entry_date DESC`
- [x] `ProjectQueryService.countSearch(ProjectSearchCriteria)`
- [x] Test: `ProjectQueryServiceIT` ergänzt: QBE mit status exakt, LIKE-Felder, kein Treffer
- [x] Git-Commit

### 5.7 Projektposition – Domain + Repository
- [x] Aggregate `ProjectPosition` (`id`, `@Version dbVersion`, Audit-Felder, `projectId`, `freelancerId`, `statusId`, `konditionen`, `kommentar`)
- [x] `ProjectPositionRepository`
- [x] Test: `ProjectPositionRepositoryIT`: insert, UNIQUE-Verletzung `(project_id, freelancer_id)` → Exception
- [x] Git-Commit

### 5.8 Projektposition – Services
- [x] `ProjectPositionCommandService.save(ProjectPosition)`: speichert, Optimistic-Locking-Handling
- [x] `ProjectPositionCommandService.delete(Long positionId)`
- [x] Record `ProjectPositionView` (id, dbVersion, freelancerId, code, name1, name2, statusId, statusDescription, statusColor, statusColorText, konditionen, kommentar)
- [x] `ProjectPositionQueryService.findByProjectId(Long projectId, String sortField, String sortDir)` → `List<ProjectPositionView>` via JOIN `freelancer` + `project_position_status`
- [x] `ProjectPositionQueryService.existsPosition(Long projectId, Long freelancerId)` → `boolean`
- [x] Test: `ProjectPositionCommandServiceIT`: save, save Konflikt, delete; `ProjectPositionQueryServiceIT`: findByProjectId mit JOIN-Daten
- [x] Git-Commit

### 5.9 Project – Kontakthistorie
- [x] Aggregate `ProjectHistory` (`id`, `description`, `projectId`, Audit-Felder; **kein** `typeId`)
- [x] `ProjectHistoryRepository`
- [x] `ProjectHistoryCommandService`: `save(ProjectHistory)`, `delete(Long id)`
- [x] `ProjectHistoryQueryService`: `findByProjectId(Long)` sortiert `creation_date DESC`
- [x] `ProjectHistoryController` (AJAX): POST `/project/{id}/history`, PUT `.../history/{hId}`, DELETE `.../history/{hId}`
- [x] Test: `ProjectHistoryRepositoryIT`, `ProjectHistoryControllerIT`
- [x] Git-Commit

### 5.10 Project – Controller (CRUD + Navigation + RememberedProject)
- [x] `ProjectController`:
  - GET `/project`: lädt gemerktes Projekt des Users (`rememberedProjectService.get(username)`); wenn vorhanden → lädt `Project` und zeigt Formular; sonst → leere QBE-Maske
  - GET `/project/{id}`: lädt Projekt, ruft `rememberedProjectService.set(username, id)`; rendert Formular
  - GET `/project/first` / `last` / `previous/{id}` / `next/{id}`: Navigation → Redirect → setzt gemerktes Projekt
  - POST `/project/save`: speichert; bei Neuanlage (kein `id` gesetzt): erst nach Speichern → `rememberedProjectService.set()`; Redirect zu `/project/{id}`; 409 JSON bei Konflikt
  - POST `/project/delete/{id}`: löscht; Redirect zu `/project`
  - GET `/project/new-from-kunde/{kundeId}`: rendert leeres Formular mit `customerId` vorausgefüllt (read-only)
  - GET `/project/new-from-partner/{partnerId}`: rendert leeres Formular mit `partnerId` vorausgefüllt (read-only)
- [x] Test: `@WebMvcTest ProjectControllerIT`: alle Endpunkte; insb. GET `/project` ohne gemerktes Projekt → leere Maske; GET `/project/{id}` → setzt gemerktes Projekt
- [x] Git-Commit

### 5.11 Project – Controller (Suche + Positionen)
- [x] POST `/project/search`, GET `/project/search-more`
- [x] GET `/project/{id}/positions` → JSON `List<ProjectPositionView>`
- [-] POST `/project/{id}/positions/{posId}` (in Template-JS via ProjectHistoryController pattern)
- [-] DELETE `/project/{id}/positions/{posId}` (in Template-JS via ProjectHistoryController pattern)
- [x] Test: `ProjectControllerIT` ergänzt
- [x] Git-Commit

### 5.12 Project – Thymeleaf Template: Hauptformular
- [x] `project/form.html`: Shell + Toolbar (Navigation, Speichern, Löschen, Gemerktes-Projekt-Anzeige – hier informativ, nicht änderbar; kein Neu-Button); Karte Allgemein (projectNumber, entryDate, startDate, duration, status-Dropdown, visibleOnWebSite-Checkbox); Karte Beschreibung (descriptionShort, descriptionLong, skills); Karte Einsatz (workplace)
- [x] Karte Zuordnung: `customerId` / `partnerId` als Read-only-Link §20 (nach Anlage nicht mehr änderbar), bei leerem Formular ausgeblendet wenn Kontext über `/new-from-xxx` gesetzt
- [x] Karte Konditionen (stundensatzVK, debitorNr, kreditorNr); Audit-Info-Zeile; `<ps-dirty-banner>`
- [x] Git-Commit

### 5.13 Project – Thymeleaf Template: Positionen und Kontakthistorie
- [x] `project/form.html` ergänzt: Positionen-Karte (`col-wide`): Tabelle (code, name1, name2, Status-Badge §22, konditionen, kommentar, Bearbeiten-Button, Löschen-Button), Leer-Hinweis
- [x] Kontakthistorie-Sektion: Einträge (Erfasst am/von, Geändert am/von wenn abweichend, Text, Bearbeiten/Löschen), Neu-Eintrag-Button
- [x] `project/search-results.html` (Fragment): Treffertabelle (projectNumber, descriptionShort, workplace, startDate, status-Label, stundensatzVK) mit Infinite Scroll
- [x] Git-Commit

### 5.14 Project – Thymeleaf Template: Modals
- [x] Modale in `project/form.html`: Löschen-Bestätigung (Projekt), Optimistic-Locking-Konflikt (Projekt-Stammdaten), Positions-Bearbeiten-Modal (statusId-Dropdown, konditionen, kommentar, dbVersion hidden, Optimistic-Locking-Konflikt auf Positions-Ebene), Positions-Löschen-Bestätigung, Kontakthistorie-Modal
- [x] Test: `ProjectControllerIT` Template-Rendering (Projekt mit Positionen, leere Maske, mit gemerktem Projekt)
- [x] Git-Commit

---

## Phase 6 – Modul `profilesearch`

### 6.1 Profilsuche – Domain & Repositories
- [x] Aggregate `ProfileSearchChat` (`id`, `creationDate`, `creationUser`, `changedDate`, `title`, `projectId` nullable FK) im Paket `de.mirkosertic.powerstaff.profilesearch`
- [x] Aggregate `ProfileSearchMessage` (`id`, `creationDate`, `chatId` FK, `role` String, `sequence`, `content`)
- [x] `ProfileSearchChatRepository`, `ProfileSearchMessageRepository`
- [x] Test: `ProfileSearchChatRepositoryIT`, `ProfileSearchMessageRepositoryIT`: insert, findByChatId, Cascade-Delete (Chat löschen → Messages weg)
- [x] Git-Commit

### 6.2 Profilsuche – CommandService
- [x] `ProfileSearchCommandService.createChat(String userId, Long projectId)` → legt `ProfileSearchChat` an, gibt `id` zurück
- [x] `ProfileSearchCommandService.deleteChat(Long chatId)` → `chatRepository.deleteById()` (Cascade erledigt Messages)
- [x] `ProfileSearchCommandService.addMessage(Long chatId, String role, String content)` → ermittelt nächstes `sequence` (`MAX(sequence)+1`), speichert `ProfileSearchMessage`; aktualisiert `changedDate` in Chat; wenn erste User-Nachricht → generiert `title` (erste 60 Zeichen)
- [x] Test: `ProfileSearchCommandServiceIT`: createChat, deleteChat, addMessage (sequence korrekt, title generiert)
- [x] Git-Commit

### 6.3 Profilsuche – QueryService
- [x] `ProfileSearchQueryService.findChatsByUser(String userId, int offset, int limit)` → sortiert `changed_date DESC`; JOIN `project` für `project_number` (nullable)
- [x] `ProfileSearchQueryService.countChatsByUser(String userId)` → `long`
- [x] `ProfileSearchQueryService.findMessagesByChat(Long chatId)` → sortiert `sequence ASC`
- [x] `ProfileSearchQueryService.findLatestChatByUser(String userId)` → `Optional<Long>` (nur die chatId)
- [x] Test: `ProfileSearchQueryServiceIT`: mehrere Chats, Sortierung, countByUser
- [x] Git-Commit

### 6.4 Profilsuche – LLM-Kontext
- [x] Record `LlmProjectContext(String projectNumber, String descriptionShort, String descriptionLong, String workplace, String skills, String duration, LocalDateTime startDate, String statusLabel, Long stundensatzVk, List<LlmFreelancerContext> positions)`
- [x] Record `LlmFreelancerContext(String code, String name1, String name2, String skills, List<String> tags, String positionStatus, String konditionen, String kommentar)`
- [x] `ProfileSearchQueryService.buildLlmContext(String userId)` → `Optional<LlmProjectContext>`: liest gemerktes Projekt des Users aus `remembered_project`; wenn vorhanden: lädt Projekt + Positionen + Freelancer + Tags via JdbcClient-JOINs
- [x] Test: `ProfileSearchQueryServiceIT` ergänzt: buildLlmContext ohne gemerktes Projekt → empty; mit Projekt und Positionen → vollständiger Kontext
- [x] Git-Commit

### 6.5 Profilsuche – LLM-Interface + Stub
- [x] Interface `LlmService` im Paket `de.mirkosertic.powerstaff.profilesearch`: `String sendMessage(Optional<LlmProjectContext> context, List<ProfileSearchMessage> history, String userMessage)`
- [x] `StubLlmService` implements `LlmService`: gibt `"Die KI-Profilsuche ist in Release 1.0 noch nicht aktiviert."` zurück
- [x] `ProfileSearchConfig` (`@Configuration`): `@Bean LlmService llmService() { return new StubLlmService(); }`
- [x] Test: `StubLlmServiceSpec` (Unit-Test): prüft Rückgabewert
- [x] Git-Commit

### 6.6 Profilsuche – Controller
- [ ] `ProfileSearchController`:
  - GET `/profilesearch`: lädt neueste Chat-ID via `findLatestChatByUser()`; wenn vorhanden → Redirect zu `/profilesearch/chat/{id}`; sonst → `createChat()` → Redirect
  - GET `/profilesearch/chat/{chatId}`: lädt Chat + Messages + Sidebar-Liste (erste 20); rendert `profilesearch/form.html`
  - POST `/profilesearch/chat/new`: `createChat(userId, rememberedProjectId)`; Redirect zu `/profilesearch/chat/{id}`
  - DELETE `/profilesearch/chat/{chatId}`: löscht Chat; wenn aktiver Chat → nächsten laden oder neuen anlegen; Redirect
  - POST `/profilesearch/chat/{chatId}/send` (JSON `{message: "..."}`) → speichert User-Nachricht, ruft `llmService.sendMessage()`, speichert Assistant-Antwort; gibt JSON `{id: ..., role: "assistant", content: "..."}` zurück
  - GET `/profilesearch/sidebar-more` (Query `?offset=N`) → HTMX Fragment mit nächsten 20 Chat-Einträgen; Response-Header `X-Next-Url` wenn weitere vorhanden
- [ ] Test: `@WebMvcTest ProfileSearchControllerIT`: alle Endpunkte mit `@MockBean`; POST send → JSON-Antwort
- [ ] Git-Commit

### 6.7 Profilsuche – CSS (Chat-Layout)
- [ ] `src/main/resources/static/css/chat.css`: Split-Panel-Layout §24 (Sidebar + Chat-Bereich), Sidebar-Einträge §25 (Titel, Projektbezug, Zeitstempel, aktiver Eintrag hervorgehoben, Löschen-Icon), Nachrichten §26 (user rechtsbündig, assistant linksbündig, Markdown-Container), Eingabebereich §27 (Textarea + Senden-Button fixiert am unteren Rand), responsive Sidebar (≥1024 px aufgeklappt, <1024 px Overlay)
- [ ] Git-Commit

### 6.8 Profilsuche – Thymeleaf Templates
- [ ] `profilesearch/form.html`: Shell + Toolbar (Gemerktes-Projekt-Anzeige, „＋ Neuer Chat"-Button, „Löschen"-Button); Split-Panel (Sidebar + Chat-Bereich)
- [ ] Sidebar: Liste der Chats (`creationUser` Einträge, `<ps-infinite-scroll>`), aktiver Chat hervorgehoben, Löschen-Icon je Eintrag
- [ ] Chat-Bereich: Nachrichtenbereich (scrollbar; Nachrichten nach Role; Markdown-Rendering für `assistant`-Nachrichten via `marked.js` oder `<pre>`-Fallback; `[freelancer:<id>:<text>]` → `<a href="/freelancer/{id}" target="_blank">`; Lade-Indikator); `<ps-chat-input>`-Element
- [ ] `profilesearch/sidebar-entry.html` (HTMX Fragment): ein Sidebar-Eintrag mit Titel, optionaler Projektnummer, Zeitstempel
- [ ] Modal: Chat-Löschen-Bestätigung
- [ ] Test: `ProfileSearchControllerIT` Template-Rendering
- [ ] Git-Commit

---

## Phase 7 – Modultest & Integration

### 7.1 Spring Modulith – Modulstruktur
- [ ] `ApplicationModulesSpec` (Spock): `ApplicationModules.of(PowerstaffApplication.class).verify()` → prüft dass keine zyklischen Abhängigkeiten und keine unerlaubten Paket-Querverweise existieren
- [ ] Git-Commit

### 7.2 Application-Start- und Flyway-Test
- [ ] `ApplicationStartIT` (`@SpringBootTest`) extends `AbstractContainerBaseIT`: Spring-Kontext startet vollständig (kein `fail()` durch Flyway-Fehler oder Bean-Konflikte)
- [ ] `FlywayMigrationIT`: prüft via `JdbcClient`, dass alle 21 erwarteten Tabellen in der DB existieren
- [ ] Git-Commit

### 7.3 Security-Integrationstests
- [ ] `SecurityIT` (`@SpringBootTest` + MockMvc, `@WithMockUser`): für jeden Controller-Basispfad prüfen: ohne Auth → 302 `/login`; mit Auth → 200 oder weitere 3xx
- [ ] CSRF: POST auf `/partner/save` ohne `X-XSRF-TOKEN` → 403
- [ ] Git-Commit

---

## Phase 8 – Abschluss und Abnahme

### 8.1 Abnahmekriterien
- [ ] `./mvnw clean verify` läuft vollständig durch (Unit-Tests + Integrationstests grün, Flyway sauber, npm build erfolgreich)
- [ ] Manuell: Login mit Testbenutzer funktioniert
- [ ] Manuell: alle 5 Hauptformulare (Freiberufler, Partner, Kunden, Projekte, Profilsuche) aufrufbar und navigierbar
- [ ] Manuell: QBE-Suche in mind. einem Modul liefert Treffer und Infinite Scrolling funktioniert
- [ ] Git-Commit „chore: release 1.0 – all tasks completed"
