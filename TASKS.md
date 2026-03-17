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
- [ ] Aggregate `Partner` im Paket `de.mirkosertic.powerstaff.partner` mit allen Feldern gemäß PARTNER.md (Gruppen Adresse, Kontaktinformationen, Kommentar, Konditionen); `@Version` auf `dbVersion` ← Spalte `db_version`; `@CreatedDate creationDate`, `@LastModifiedDate lastModificationDate`, `@CreatedBy creationUserId`, `@LastModifiedBy lastModificationUserId`
- [ ] `PartnerRepository` extends `CrudRepository<Partner, Long>`
- [ ] Test: `PartnerRepositoryIT` extends `AbstractContainerBaseIT`: insert, findById, update (prüft `db_version` Inkrement), Optimistic-Locking-Konflikt (zwei parallele saves auf gleicher Version) → `OptimisticLockingFailureException`
- [ ] Git-Commit

### 2.2 Partner – CommandService (save + delete)
- [ ] `PartnerCommandService.save(Partner)`: speichert via `PartnerRepository`; fängt `OptimisticLockingFailureException` und wirft eigene `OptimisticLockingException` (enthält `changedBy`, `changedDate` des aktuellen DB-Stands)
- [ ] `PartnerCommandService.delete(Long id)`: liest alle `project.partner_id = id` Einträge via `JdbcClient`; wenn vorhanden → wirft `EntityHasProjectsException(List<String> projectNumbers)`; sonst `partnerRepository.deleteById(id)`
- [ ] `EntityHasProjectsException` als gemeinsame Exception-Klasse im Paket `de.mirkosertic.powerstaff.shared`
- [ ] Test: `PartnerCommandServiceIT` extends `AbstractContainerBaseIT`: save neu, save update, delete ohne Projekte, delete mit Projekten → Exception mit Projektnummern
- [ ] Git-Commit

### 2.3 Partner – QueryService (Navigation + findById)
- [ ] `PartnerQueryService` via `JdbcClient`:
  - `findById(Long)` → `Optional<Partner>`
  - `findFirst()` → `Optional<Partner>` (`ORDER BY id ASC LIMIT 1`)
  - `findLast()` → `Optional<Partner>` (`ORDER BY id DESC LIMIT 1`)
  - `findPrevious(Long currentId)` → `Optional<Partner>` (`WHERE id < :id ORDER BY id DESC LIMIT 1`)
  - `findNext(Long currentId)` → `Optional<Partner>` (`WHERE id > :id ORDER BY id ASC LIMIT 1`)
- [ ] Test: `PartnerQueryServiceIT`: legt 3 Partner an, prüft alle 5 Navigationsmethoden
- [ ] Git-Commit

### 2.4 Partner – QueryService (QBE-Suche + Sublisten)
- [ ] Record `PartnerSearchCriteria(String company, String name1, String name2, String street, String country, String plz, String city, String comments, String kreditorNr, String debitorNr)`
- [ ] Record `PartnerSearchResult(Long id, String company, String name1, String name2, String city)` für Suchergebnis-Liste
- [ ] `PartnerQueryService.search(PartnerSearchCriteria criteria, int offset, int limit)`: baut SQL mit `WHERE`-Klausel dynamisch über nicht-null/nicht-leere Felder (`LIKE '%:wert%'` per `JdbcClient` mit `SqlParameterSource`); niemals String-Konkatenation; `ORDER BY company ASC`
- [ ] `PartnerQueryService.countSearch(PartnerSearchCriteria criteria)` → `long`
- [ ] `PartnerQueryService.findFreelancersByPartnerId(Long partnerId, String sortField, String sortDir)` → `List<FreelancerListItem>` (Record mit: `id, code, name1, name2, company, availabilityAsDate, salaryLong`)
- [ ] `PartnerQueryService.findProjectsByPartnerId(Long partnerId, String sortField, String sortDir)` → `List<ProjectListItem>` (Record mit: `id, projectNumber, descriptionShort, workplace, startDate, status`)
- [ ] Test: `PartnerQueryServiceIT` ergänzt: QBE mit einem Feld (Treffer), QBE mit zwei Feldern (AND), QBE ohne Treffer, Sortierung Freelancer-Liste
- [ ] Git-Commit

### 2.5 Partner – Kontaktmöglichkeiten (Domain + Service)
- [ ] Aggregate `PartnerContact` (`id`, `type` als `String` ← Spalte `type`, `value`, `partnerId`, Audit-Felder `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy`)
- [ ] `PartnerContactRepository`
- [ ] `PartnerContactCommandService`: `save(PartnerContact)`, `delete(Long contactId)`
- [ ] `PartnerContactQueryService`: `findByPartnerId(Long partnerId)` → sortiert nach `ContactType`-Reihenfolge (CASE WHEN in SQL), dann `id ASC`
- [ ] Test: `PartnerContactRepositoryIT`: insert, findByPartnerId (Sortierung), delete
- [ ] Git-Commit

### 2.6 Partner – Kontakthistorie (Domain + Service)
- [ ] Aggregate `PartnerHistory` (`id`, `description`, `typeId` ← `type_id` FK → `historytype`, `partnerId`, Audit-Felder)
- [ ] `PartnerHistoryRepository`
- [ ] `PartnerHistoryCommandService`: `save(PartnerHistory)`, `delete(Long historyId)`
- [ ] `PartnerHistoryQueryService`: `findByPartnerId(Long partnerId)` → JOIN `historytype` für `typeDescription`, sortiert `creation_date DESC`
- [ ] Test: `PartnerHistoryRepositoryIT`: insert mit `typeId`, findByPartnerId (Sortierung), delete
- [ ] Git-Commit

### 2.7 Partner – Controller (CRUD + Navigation)
- [ ] `PartnerController`:
  - GET `/partner`: lädt letzten Partner aus Session-Attribut `lastPartnerId`; wenn nicht vorhanden → `partnerQueryService.findFirst()`; Model: `partner`, `rememberedProject`; rendert `partner/form.html`
  - GET `/partner/{id}`: lädt Partner, setzt Session `lastPartnerId`; rendert `partner/form.html`
  - GET `/partner/new`: löscht Session `lastPartnerId`; rendert `partner/form.html` mit leerem `Partner`
  - GET `/partner/first` / `/partner/last` / `/partner/previous/{id}` / `/partner/next/{id}`: Navigation → Redirect zu `/partner/{id}` oder `/partner/new` wenn kein Ergebnis
  - POST `/partner/save`: bindet `@ModelAttribute Partner`; ruft `partnerCommandService.save()`; bei `OptimisticLockingException` → JSON `{"conflict": true, "changedBy": "...", "changedDate": "..."}` mit Status 409; bei Erfolg → Redirect zu `/partner/{id}`
  - POST `/partner/delete/{id}`: ruft `partnerCommandService.delete()`; bei `EntityHasProjectsException` → JSON `{"blocked": true, "projectNumbers": [...]}` mit Status 409; bei Erfolg → Redirect zu `/partner/new`
- [ ] Test: `@WebMvcTest PartnerControllerIT` mit `@MockBean` für Services: GET `/partner` → 200; GET `/partner/{id}` → 200; Navigation → 302; POST `/partner/save` Erfolg → 302; POST `/partner/save` Konflikt → 409 JSON; POST `/partner/delete` geblockt → 409 JSON
- [ ] Git-Commit

### 2.8 Partner – Controller (Suche + Freelancer-Zuordnung)
- [ ] POST `/partner/search`: bindet `PartnerSearchCriteria`; speichert Criteria in Session; ruft `search(criteria, 0, 20)`; rendert `partner/search-results.html` als Fragment
- [ ] GET `/partner/search-more`: liest Criteria aus Session, `offset` aus Query-Param; ruft `search(criteria, offset, 20)`; setzt Response-Header `X-Next-Url` wenn weitere Treffer vorhanden; rendert Fragment
- [ ] POST `/partner/{id}/assign-freelancer`: Body `{code: "..."}` (JSON); sucht Freelancer per Code via `JdbcClient`; wenn nicht gefunden → 404 JSON; wenn bereits anderem Partner zugeordnet → 409 JSON `{"otherPartner": "Firmenname"}`; sonst: UPDATE `freelancer.partner_id`; → 200 JSON mit aktualisierter Freelancer-Liste
- [ ] POST `/partner/{id}/confirm-reassign-freelancer`: Body `{freelancerId: ...}` (JSON); überschreibt `freelancer.partner_id` ohne Konfliktprüfung; → 200 JSON
- [ ] POST `/partner/{id}/remove-freelancer/{freelancerId}`: setzt `freelancer.partner_id = NULL` per `JdbcClient`; → 200 JSON
- [ ] Test: `PartnerControllerIT` ergänzt: POST search → Fragment-HTML; search-more mit Offset; assign-freelancer Szenarien
- [ ] Git-Commit

### 2.9 Partner – Kontakt- und Historien-Controller (AJAX)
- [ ] `PartnerContactController`: POST `/partner/{id}/contacts` (JSON Body: `type`, `value`) → speichert, gibt aktualisierte Kontaktliste als JSON zurück; DELETE `/partner/{id}/contacts/{contactId}` → löscht, gibt aktualisierte Liste zurück
- [ ] `PartnerHistoryController`: POST `/partner/{id}/history` (JSON Body: `typeId`, `description`) → speichert; PUT `/partner/{id}/history/{hId}` → aktualisiert; DELETE `/partner/{id}/history/{hId}` → löscht; jeweils → JSON `{ok: true}` oder Fehler
- [ ] Test: `PartnerContactControllerIT`, `PartnerHistoryControllerIT` mit MockMvc + `@MockBean`
- [ ] Git-Commit

### 2.10 Partner – Thymeleaf Template: Hauptformular
- [ ] `partner/form.html`: bindet `fragments/layout.html`; `fragments/toolbar.html` mit Navigations-Buttons (Erste, Zurück, Weiter, Letzte, ID-Eingabe), Neu-Button, Speichern-Button, Löschen-Button; Formular-Karten für Adresse, Kontaktinformationen, Kommentar, Konditionen; Audit-Info-Zeile (Erfasst am/von, Geändert am/von); bindet `fragments/contact-list.html` für Kontaktmöglichkeiten
- [ ] Banner: Kontaktsperre-Banner (rot, `th:if="${partner.contactforbidden}"`) + `<ps-dirty-banner>`-Integration
- [ ] Git-Commit

### 2.11 Partner – Thymeleaf Template: Sublisten und Suche
- [ ] `partner/form.html` ergänzt: Freiberufler-Zuordnungs-Karte (fcard `col-wide`): Tabelle (code, name1, name2, company, availabilityAsDate, salaryLong, Löschen-Button), Zuordnen-Eingabefeld + Button, Neu-Freiberufler-Button; Projekte-Karte (fcard `col-wide`): Tabelle (projectNumber, descriptionShort, workplace, startDate, status), Neu-Projekt-Button
- [ ] `partner/search-results.html` (reines Fragment, kein vollständiges HTML): Treffertabelle (company, name1, name2, city) mit sortierbaren Spalten-Headern (Links mit `?sort=`-Parameter), Gesamtanzahl, `<ps-infinite-scroll>`-Sentinel, Leer-Hinweis
- [ ] Git-Commit

### 2.12 Partner – Thymeleaf Template: Kontakthistorie und Modals
- [ ] `partner/form.html` ergänzt: Kontakthistorie-Sektion (Liste der `PartnerHistory`-Einträge: Typ, Erfasst am/von, Geändert am/von wenn abweichend, Beschreibungstext, Bearbeiten- und Löschen-Button)
- [ ] Modale eingebettet in `form.html` via `fragments/modal.html`: Löschen-Bestätigung (Partner), Löschen-Bestätigung (Freiberufler-Zuordnung aufheben), Projektliste-Fehler-Dialog (Löschen verhindert), Freelancer-Konflikt-Dialog (bereits anderem Partner zugeordnet), Kontaktmöglichkeit-Modal (Neuanlage/Bearbeitung), Kontakthistorie-Modal (Neuanlage/Bearbeitung), Optimistic-Locking-Konflikt-Dialog
- [ ] Test: `@WebMvcTest PartnerControllerIT` ergänzt: Template rendert vollständig mit/ohne Partner-Daten; alle `th:`-Attribute greifen korrekt
- [ ] Git-Commit

---

## Phase 3 – Modul `freelancer`

### 3.1 Freelancer – Domain & Repository
- [ ] Aggregate `Freelancer` im Paket `de.mirkosertic.powerstaff.freelancer` mit allen Feldern gemäß FREIBERUFLER.md (alle Gruppen); `@Version dbVersion`; Audit-Felder; `partnerId` nullable FK
- [ ] `FreelancerRepository`
- [ ] Test: `FreelancerRepositoryIT`: CRUD, Optimistic-Locking-Konflikt → `OptimisticLockingFailureException`
- [ ] Git-Commit

### 3.2 Freelancer – Tags (Domain + Repository)
- [ ] Aggregate `FreelancerTag` (`id`, `freelancerId`, `tagId`, Audit-Felder); UNIQUE-Constraint `(freelancer_id, tag_id)`
- [ ] `FreelancerTagRepository`
- [ ] `FreelancerTagCommandService`: `addTag(Long freelancerId, Long tagId)` → wirft `DuplicateTagException` bei Violation des UNIQUE-Index; `removeTag(Long freelancerTagId)`
- [ ] Test: `FreelancerTagRepositoryIT`: insert, findByFreelancerId, delete, Duplicate → Exception
- [ ] Git-Commit

### 3.3 Freelancer – CommandService
- [ ] `FreelancerCommandService.save(Freelancer)`: speichert, fängt `OptimisticLockingFailureException` → wirft `OptimisticLockingException`
- [ ] `FreelancerCommandService.delete(Long id)`: prüft Einträge in `project_position` via `JdbcClient`; wenn vorhanden → wirft `FreelancerHasPositionsException(List<String> projectNumbers)`; sonst `freelancerRepository.deleteById(id)`
- [ ] Test: `FreelancerCommandServiceIT`: save neu, save update, delete ohne Positionen, delete mit Positionen → Exception
- [ ] Git-Commit

### 3.4 Freelancer – QueryService (Navigation + findById)
- [ ] `FreelancerQueryService.findById(Long)`, `findFirst()`, `findLast()`, `findPrevious(Long)`, `findNext(Long)` – analog zu `PartnerQueryService`
- [ ] Test: `FreelancerQueryServiceIT` (Navigation, 3 Datensätze)
- [ ] Git-Commit

### 3.5 Freelancer – QueryService (QBE-Suche)
- [ ] Record `FreelancerSearchCriteria` mit 19 Feldern gemäß FREIBERUFLER.md (alle Suchfelder)
- [ ] Record `FreelancerSearchResult(Long id, String name1, String name2, LocalDateTime availabilityAsDate, Long salaryLong, String skills, String code)` + `List<TagInfo> tags`
- [ ] `FreelancerQueryService.search(FreelancerSearchCriteria, int offset, int limit)`: QBE – LIKE für Strings, exakter Vergleich für `kontaktart`; JOIN `freelancer_tags` + `tags` für Tag-Daten; GROUP BY / Aggregation für Tags per Freelancer; `ORDER BY name1 ASC, name2 ASC`
- [ ] `FreelancerQueryService.countSearch(FreelancerSearchCriteria)` → `long`
- [ ] Test: `FreelancerQueryServiceIT` ergänzt: QBE ein Feld, QBE `kontaktart` exakt, QBE ohne Treffer, Tags erscheinen in Ergebnis
- [ ] Git-Commit

### 3.6 Freelancer – QueryService (Tags)
- [ ] Record `TagInfo(Long id, String name, TagType type)`
- [ ] `FreelancerQueryService.findTagsByFreelancerId(Long freelancerId)` → `List<TagInfo>` sortiert nach `TagType`-Ordinal, dann `tagname ASC`
- [ ] `FreelancerQueryService.findAvailableTagsByFreelancerIdAndType(Long freelancerId, TagType type)` → Tags dieser Gruppe, die der Freiberufler noch NICHT hat; sortiert `tagname ASC`
- [ ] Test: `FreelancerQueryServiceIT` ergänzt: Tags zugeordnet, verfügbare Tags für Gruppe, Sortierung
- [ ] Git-Commit

### 3.7 Freelancer – Kontaktmöglichkeiten (Domain + Service)
- [ ] Aggregate `FreelancerContact` (`id`, `type` String, `value`, `freelancerId`, Audit-Felder)
- [ ] `FreelancerContactRepository`
- [ ] `FreelancerContactCommandService`: `save(FreelancerContact)`, `delete(Long contactId)`
- [ ] `FreelancerContactQueryService`: `findByFreelancerId(Long)` → sortiert nach `ContactType`-Reihenfolge, dann `id ASC`
- [ ] Test: `FreelancerContactRepositoryIT`
- [ ] Git-Commit

### 3.8 Freelancer – Kontakthistorie (Domain + Service)
- [ ] Aggregate `FreelancerHistory` (`id`, `description`, `typeId`, `freelancerId`, Audit-Felder)
- [ ] `FreelancerHistoryRepository`
- [ ] `FreelancerHistoryCommandService`: `save(FreelancerHistory)`, `delete(Long historyId)`
- [ ] `FreelancerHistoryQueryService`: `findByFreelancerId(Long)` → JOIN `historytype`, sortiert `creation_date DESC`
- [ ] Test: `FreelancerHistoryRepositoryIT`
- [ ] Git-Commit

### 3.9 Freelancer – Controller (CRUD + Navigation)
- [ ] `FreelancerController`:
  - GET `/freelancer`, GET `/freelancer/{id}` (setzt Session `lastFreelancerId`), GET `/freelancer/new`
  - GET `/freelancer/first` / `last` / `previous/{id}` / `next/{id}`
  - POST `/freelancer/save`: inkl. 409 JSON bei Optimistic-Locking-Konflikt
  - POST `/freelancer/delete/{id}`: inkl. 409 JSON `{"blocked": true, "projectNumbers": [...]}` bei `FreelancerHasPositionsException`
- [ ] Test: `@WebMvcTest FreelancerControllerIT`: alle Endpunkte mit `@MockBean`
- [ ] Git-Commit

### 3.10 Freelancer – Controller (Suche + Tags + Projektzuordnung)
- [ ] POST `/freelancer/search`, GET `/freelancer/search-more` (analog Partner)
- [ ] POST `/freelancer/{id}/tags` (JSON `{tagId: ...}`) → `freelancerTagCommandService.addTag()`; 409 bei Duplicate; → JSON mit aktualisierter Tag-Liste
- [ ] DELETE `/freelancer/{id}/tags/{tagId}` → `removeTag()`
- [ ] GET `/freelancer/{id}/available-tags/{type}` → JSON `List<TagInfo>` für Dropdown
- [ ] POST `/freelancer/{id}/assign-to-project` (JSON `{statusId, konditionen, kommentar}`) → legt `ProjectPosition` an (via `JdbcClient` direkt – kein cross-module Repository-Zugriff); 409 bei Duplicate-Zuordnung; → JSON `{ok: true}`
- [ ] Test: `FreelancerControllerIT` ergänzt
- [ ] Git-Commit

### 3.11 Freelancer – Kontakt- und Historien-Controller (AJAX)
- [ ] `FreelancerContactController`: POST / DELETE `/freelancer/{id}/contacts`, `/freelancer/{id}/contacts/{contactId}` – analog `PartnerContactController`
- [ ] `FreelancerHistoryController`: POST `/freelancer/{id}/history`, PUT `.../history/{hId}`, DELETE `.../history/{hId}`
- [ ] Test: `FreelancerContactControllerIT`, `FreelancerHistoryControllerIT`
- [ ] Git-Commit

### 3.12 Freelancer – Thymeleaf Template: Hauptformular
- [ ] `freelancer/form.html`: Shell + Toolbar (Navigation, Neu, Speichern, Löschen, Gemerktes-Projekt-Anzeige, „Dem Projekt zuordnen"-Button wenn Projekt gemerkt); Karte Adresse (inkl. Partner-Link als Read-only §20 oder leer); Karte Kontaktinformationen (`fragments/contact-list.html`); Karte Kommentar; Karte Einsatzdetails; Karte Zusatzinformationen (inkl. `kontaktart`-Dropdown)
- [ ] Karte Verfügbarkeit & Konditionen; Karte Kodierung (code-Feld, dann Tags-Sektion, dann skills-Feld)
- [ ] Tags-Sektion: je `TagType` eine Subsection mit Chip-Liste der zugeordneten Tags (×-Button) + Dropdown zum Hinzufügen (nur verfügbare Tags, AJAX-befüllt)
- [ ] Banners: Kontaktsperre (rot), `<ps-dirty-banner>`
- [ ] Test: `@WebMvcTest FreelancerControllerIT` Template-Rendering (Freiberufler mit und ohne Partner, mit Tags)
- [ ] Git-Commit

### 3.13 Freelancer – Thymeleaf Template: Sublisten, Suche und Modals
- [ ] `freelancer/form.html` ergänzt: Kontakthistorie-Sektion (Einträge: Typ, Erfasst am/von, Geändert am/von wenn abweichend, Text, Bearbeiten/Löschen)
- [ ] `freelancer/search-results.html` (Fragment): Treffertabelle (name1, name2, availabilityAsDate, salaryLong, skills, code, Tags als Chips) mit Infinite Scroll
- [ ] Modale: Löschen-Bestätigung (Freiberufler), Projektliste-Fehler (Löschen verhindert), Optimistic-Locking-Konflikt, Projektzuordnungs-Modal (statusId-Dropdown aus `project_position_status`, konditionen, kommentar), Duplicate-Zuordnung-Fehler, Kontaktmöglichkeit-Modal, Kontakthistorie-Modal
- [ ] Test: `FreelancerControllerIT` ergänzt (Templates prüfen)
- [ ] Git-Commit

---

## Phase 4 – Modul `kunde`

### 4.1 Kunde – Domain & Repository
- [ ] Aggregate `Kunde` mit allen Feldern gemäß KUNDEN.md; `@Version`, Audit-Felder
- [ ] `KundeRepository`
- [ ] Test: `KundeRepositoryIT`: CRUD, Optimistic-Locking-Konflikt
- [ ] Git-Commit

### 4.2 Kunde – CommandService
- [ ] `KundeCommandService.save(Kunde)`: mit Optimistic-Locking-Handling
- [ ] `KundeCommandService.delete(Long id)`: prüft `project.customer_id`; bei Projekten → wirft `EntityHasProjectsException`; sonst löscht
- [ ] Test: `KundeCommandServiceIT`: save, delete frei, delete geblockt
- [ ] Git-Commit

### 4.3 Kunde – QueryService
- [ ] `KundeQueryService`: `findById`, `findFirst`, `findLast`, `findPrevious`, `findNext` (analog Partner)
- [ ] `search(KundeSearchCriteria, offset, limit)`, `countSearch` (QBE über: company, name1, name2, street, country, plz, city, comments, kreditorNr, debitorNr)
- [ ] `findProjectsByKundeId(Long kundeId, String sortField, String sortDir)` → `List<ProjectListItem>`
- [ ] Test: `KundeQueryServiceIT`: Navigation, QBE, Projektliste
- [ ] Git-Commit

### 4.4 Kunde – Kontaktmöglichkeiten (Domain + Service)
- [ ] Aggregate `KundeContact` (`id`, `type` String, `value`, `kundeId`, Audit-Felder)
- [ ] `KundeContactRepository`
- [ ] `KundeContactCommandService`: `save`, `delete`
- [ ] `KundeContactQueryService`: `findByKundeId(Long)` sortiert nach `ContactType`-Reihenfolge
- [ ] Test: `KundeContactRepositoryIT`
- [ ] Git-Commit

### 4.5 Kunde – Kontakthistorie (Domain + Service)
- [ ] Aggregate `KundeHistory` (`id`, `description`, `typeId`, `kundeId`, Audit-Felder)
- [ ] `KundeHistoryRepository`
- [ ] `KundeHistoryCommandService`: `save`, `delete`
- [ ] `KundeHistoryQueryService`: `findByKundeId(Long)` → JOIN `historytype`, sortiert `creation_date DESC`
- [ ] Test: `KundeHistoryRepositoryIT`
- [ ] Git-Commit

### 4.6 Kunde – Controller (CRUD + Navigation)
- [ ] `KundeController`:
  - GET `/kunde`, GET `/kunde/{id}` (Session `lastKundeId`), GET `/kunde/new`
  - GET `/kunde/first` / `last` / `previous/{id}` / `next/{id}`
  - POST `/kunde/save` (409 JSON bei Optimistic-Locking-Konflikt)
  - POST `/kunde/delete/{id}` (409 JSON bei Projekten)
- [ ] Test: `@WebMvcTest KundeControllerIT`
- [ ] Git-Commit

### 4.7 Kunde – Controller (Suche + AJAX)
- [ ] POST `/kunde/search`, GET `/kunde/search-more`
- [ ] `KundeContactController`: POST / DELETE `/kunde/{id}/contacts`, `/kunde/{id}/contacts/{contactId}`
- [ ] `KundeHistoryController`: POST / PUT / DELETE `/kunde/{id}/history`, `.../history/{hId}`
- [ ] Test: `KundeControllerIT`, `KundeContactControllerIT`, `KundeHistoryControllerIT` ergänzt
- [ ] Git-Commit

### 4.8 Kunde – Thymeleaf Templates
- [ ] `kunde/form.html`: Shell, Toolbar (Navigation, Neu, Speichern, Löschen, Gemerktes-Projekt-Anzeige), alle Feldkarten (Adresse, Kontaktinformationen + `fragments/contact-list.html`, Kommentar, Konditionen), Projekte-Karte (`col-wide`, Tabelle + Neu-Projekt-Button), Kontakthistorie-Sektion
- [ ] Banners: Kontaktsperre, `<ps-dirty-banner>`; Modale: Löschen-Bestätigung, Projektlisten-Fehler, Optimistic-Locking-Konflikt, Kontaktmöglichkeit-Modal, Kontakthistorie-Modal
- [ ] `kunde/search-results.html` (Fragment): Treffertabelle (company, name1, name2, city) mit Infinite Scroll
- [ ] Test: `KundeControllerIT` Template-Rendering
- [ ] Git-Commit

---

## Phase 5 – Modul `project`

### 5.1 Project – Domain & Repository
- [ ] Aggregate `Project` im Paket `de.mirkosertic.powerstaff.project` mit allen Feldern gemäß PROJEKTE.md; `@Version dbVersion`; Audit-Felder; `customerId` nullable; `partnerId` nullable
- [ ] `ProjectRepository`
- [ ] Test: `ProjectRepositoryIT`: CRUD, beide FK-Felder nullable, Optimistic-Locking-Konflikt
- [ ] Git-Commit

### 5.2 Project – RememberedProject (Domain + Repository)
- [ ] Aggregate `RememberedProject` (`userId` als `@Id` String PK ← Spalte `user_id`; `projectId` BIGINT FK)
- [ ] `RememberedProjectRepository`
- [ ] `RememberedProjectService`: `set(String userId, Long projectId)` → upsert (`save` via Repository: da `userId` PK, überschreibt Spring Data JDBC bei `isNew=false`); `get(String userId)` → `Optional<Long>`; `clear(String userId)` → `deleteById`
- [ ] Test: `RememberedProjectRepositoryIT`: set, get, set überschreibt, clear
- [ ] Git-Commit

### 5.3 Project – Validierung
- [ ] `ProjectValidator` implements Spring `Validator`: `customerId` und `partnerId` gleichzeitig gesetzt → `ValidationException`; registriert als `@Component`
- [ ] Test: `ProjectValidatorSpec` (Spock Unit-Test): beide null → ok; einer gesetzt → ok; beide gesetzt → Exception
- [ ] Git-Commit

### 5.4 Project – CommandService
- [ ] `ProjectCommandService.save(Project)`: ruft `projectValidator.validate()`, speichert, fängt Optimistic-Locking-Fehler
- [ ] `ProjectCommandService.delete(Long id)`: löscht (kein RESTRICT von außen); `ON DELETE CASCADE` auf `project_history`, `project_position`, `remembered_project` erledigt Rest DB-seitig
- [ ] Test: `ProjectCommandServiceIT`: save valid, save invalid (beide FKs gesetzt), save Konflikt, delete
- [ ] Git-Commit

### 5.5 Project – QueryService (Navigation + findById)
- [ ] `ProjectQueryService.findById(Long)`, `findFirst()`, `findLast()`, `findPrevious(Long)`, `findNext(Long)`
- [ ] Test: `ProjectQueryServiceIT` (Navigation)
- [ ] Git-Commit

### 5.6 Project – QueryService (QBE-Suche)
- [ ] Record `ProjectSearchCriteria` (projectNumber, descriptionShort, descriptionLong, skills, workplace, duration, Integer status, debitorNr, kreditorNr)
- [ ] Record `ProjectSearchResult` (id, projectNumber, descriptionShort, workplace, startDate, status, stundensatzVK)
- [ ] `ProjectQueryService.search(ProjectSearchCriteria, offset, limit)`: `status` → exakter Vergleich; alle anderen → LIKE; `ORDER BY entry_date DESC`
- [ ] `ProjectQueryService.countSearch(ProjectSearchCriteria)`
- [ ] Test: `ProjectQueryServiceIT` ergänzt: QBE mit status exakt, LIKE-Felder, kein Treffer
- [ ] Git-Commit

### 5.7 Projektposition – Domain + Repository
- [ ] Aggregate `ProjectPosition` (`id`, `@Version dbVersion`, Audit-Felder, `projectId`, `freelancerId`, `statusId`, `konditionen`, `kommentar`)
- [ ] `ProjectPositionRepository`
- [ ] Test: `ProjectPositionRepositoryIT`: insert, UNIQUE-Verletzung `(project_id, freelancer_id)` → Exception
- [ ] Git-Commit

### 5.8 Projektposition – Services
- [ ] `ProjectPositionCommandService.save(ProjectPosition)`: speichert, Optimistic-Locking-Handling
- [ ] `ProjectPositionCommandService.delete(Long positionId)`
- [ ] Record `ProjectPositionView` (id, dbVersion, freelancerId, code, name1, name2, statusId, statusDescription, statusColor, statusColorText, konditionen, kommentar)
- [ ] `ProjectPositionQueryService.findByProjectId(Long projectId, String sortField, String sortDir)` → `List<ProjectPositionView>` via JOIN `freelancer` + `project_position_status`
- [ ] `ProjectPositionQueryService.existsPosition(Long projectId, Long freelancerId)` → `boolean`
- [ ] Test: `ProjectPositionCommandServiceIT`: save, save Konflikt, delete; `ProjectPositionQueryServiceIT`: findByProjectId mit JOIN-Daten
- [ ] Git-Commit

### 5.9 Project – Kontakthistorie
- [ ] Aggregate `ProjectHistory` (`id`, `description`, `projectId`, Audit-Felder; **kein** `typeId`)
- [ ] `ProjectHistoryRepository`
- [ ] `ProjectHistoryCommandService`: `save(ProjectHistory)`, `delete(Long id)`
- [ ] `ProjectHistoryQueryService`: `findByProjectId(Long)` sortiert `creation_date DESC`
- [ ] `ProjectHistoryController` (AJAX): POST `/project/{id}/history`, PUT `.../history/{hId}`, DELETE `.../history/{hId}`
- [ ] Test: `ProjectHistoryRepositoryIT`, `ProjectHistoryControllerIT`
- [ ] Git-Commit

### 5.10 Project – Controller (CRUD + Navigation + RememberedProject)
- [ ] `ProjectController`:
  - GET `/project`: lädt gemerktes Projekt des Users (`rememberedProjectService.get(username)`); wenn vorhanden → lädt `Project` und zeigt Formular; sonst → leere QBE-Maske
  - GET `/project/{id}`: lädt Projekt, ruft `rememberedProjectService.set(username, id)`; rendert Formular
  - GET `/project/first` / `last` / `previous/{id}` / `next/{id}`: Navigation → Redirect → setzt gemerktes Projekt
  - POST `/project/save`: speichert; bei Neuanlage (kein `id` gesetzt): erst nach Speichern → `rememberedProjectService.set()`; Redirect zu `/project/{id}`; 409 JSON bei Konflikt
  - POST `/project/delete/{id}`: löscht; Redirect zu `/project`
  - GET `/project/new-from-kunde/{kundeId}`: rendert leeres Formular mit `customerId` vorausgefüllt (read-only)
  - GET `/project/new-from-partner/{partnerId}`: rendert leeres Formular mit `partnerId` vorausgefüllt (read-only)
- [ ] Test: `@WebMvcTest ProjectControllerIT`: alle Endpunkte; insb. GET `/project` ohne gemerktes Projekt → leere Maske; GET `/project/{id}` → setzt gemerktes Projekt
- [ ] Git-Commit

### 5.11 Project – Controller (Suche + Positionen)
- [ ] POST `/project/search`, GET `/project/search-more`
- [ ] GET `/project/{id}/positions` → JSON `List<ProjectPositionView>`
- [ ] POST `/project/{id}/positions/{posId}` (JSON Body: statusId, konditionen, kommentar, dbVersion) → speichert, 409 bei Optimistic-Locking-Konflikt
- [ ] DELETE `/project/{id}/positions/{posId}` → löscht
- [ ] Test: `ProjectControllerIT` ergänzt
- [ ] Git-Commit

### 5.12 Project – Thymeleaf Template: Hauptformular
- [ ] `project/form.html`: Shell + Toolbar (Navigation, Speichern, Löschen, Gemerktes-Projekt-Anzeige – hier informativ, nicht änderbar; kein Neu-Button); Karte Allgemein (projectNumber, entryDate, startDate, duration, status-Dropdown, visibleOnWebSite-Checkbox); Karte Beschreibung (descriptionShort, descriptionLong, skills); Karte Einsatz (workplace)
- [ ] Karte Zuordnung: `customerId` / `partnerId` als Read-only-Link §20 (nach Anlage nicht mehr änderbar), bei leerem Formular ausgeblendet wenn Kontext über `/new-from-xxx` gesetzt
- [ ] Karte Konditionen (stundensatzVK, debitorNr, kreditorNr); Audit-Info-Zeile; `<ps-dirty-banner>`
- [ ] Git-Commit

### 5.13 Project – Thymeleaf Template: Positionen und Kontakthistorie
- [ ] `project/form.html` ergänzt: Positionen-Karte (`col-wide`): Tabelle (code, name1, name2, Status-Badge §22, konditionen, kommentar, Bearbeiten-Button, Löschen-Button), Leer-Hinweis
- [ ] Kontakthistorie-Sektion: Einträge (Erfasst am/von, Geändert am/von wenn abweichend, Text, Bearbeiten/Löschen), Neu-Eintrag-Button
- [ ] `project/search-results.html` (Fragment): Treffertabelle (projectNumber, descriptionShort, workplace, startDate, status-Label, stundensatzVK) mit Infinite Scroll
- [ ] Git-Commit

### 5.14 Project – Thymeleaf Template: Modals
- [ ] Modale in `project/form.html`: Löschen-Bestätigung (Projekt), Optimistic-Locking-Konflikt (Projekt-Stammdaten), Positions-Bearbeiten-Modal (statusId-Dropdown, konditionen, kommentar, dbVersion hidden, Optimistic-Locking-Konflikt auf Positions-Ebene), Positions-Löschen-Bestätigung, Kontakthistorie-Modal
- [ ] Test: `ProjectControllerIT` Template-Rendering (Projekt mit Positionen, leere Maske, mit gemerktem Projekt)
- [ ] Git-Commit

---

## Phase 6 – Modul `profilesearch`

### 6.1 Profilsuche – Domain & Repositories
- [ ] Aggregate `ProfileSearchChat` (`id`, `creationDate`, `creationUser`, `changedDate`, `title`, `projectId` nullable FK) im Paket `de.mirkosertic.powerstaff.profilesearch`
- [ ] Aggregate `ProfileSearchMessage` (`id`, `creationDate`, `chatId` FK, `role` String, `sequence`, `content`)
- [ ] `ProfileSearchChatRepository`, `ProfileSearchMessageRepository`
- [ ] Test: `ProfileSearchChatRepositoryIT`, `ProfileSearchMessageRepositoryIT`: insert, findByChatId, Cascade-Delete (Chat löschen → Messages weg)
- [ ] Git-Commit

### 6.2 Profilsuche – CommandService
- [ ] `ProfileSearchCommandService.createChat(String userId, Long projectId)` → legt `ProfileSearchChat` an, gibt `id` zurück
- [ ] `ProfileSearchCommandService.deleteChat(Long chatId)` → `chatRepository.deleteById()` (Cascade erledigt Messages)
- [ ] `ProfileSearchCommandService.addMessage(Long chatId, String role, String content)` → ermittelt nächstes `sequence` (`MAX(sequence)+1`), speichert `ProfileSearchMessage`; aktualisiert `changedDate` in Chat; wenn erste User-Nachricht → generiert `title` (erste 60 Zeichen)
- [ ] Test: `ProfileSearchCommandServiceIT`: createChat, deleteChat, addMessage (sequence korrekt, title generiert)
- [ ] Git-Commit

### 6.3 Profilsuche – QueryService
- [ ] `ProfileSearchQueryService.findChatsByUser(String userId, int offset, int limit)` → sortiert `changed_date DESC`; JOIN `project` für `project_number` (nullable)
- [ ] `ProfileSearchQueryService.countChatsByUser(String userId)` → `long`
- [ ] `ProfileSearchQueryService.findMessagesByChat(Long chatId)` → sortiert `sequence ASC`
- [ ] `ProfileSearchQueryService.findLatestChatByUser(String userId)` → `Optional<Long>` (nur die chatId)
- [ ] Test: `ProfileSearchQueryServiceIT`: mehrere Chats, Sortierung, countByUser
- [ ] Git-Commit

### 6.4 Profilsuche – LLM-Kontext
- [ ] Record `LlmProjectContext(String projectNumber, String descriptionShort, String descriptionLong, String workplace, String skills, String duration, LocalDateTime startDate, String statusLabel, Long stundensatzVk, List<LlmFreelancerContext> positions)`
- [ ] Record `LlmFreelancerContext(String code, String name1, String name2, String skills, List<String> tags, String positionStatus, String konditionen, String kommentar)`
- [ ] `ProfileSearchQueryService.buildLlmContext(String userId)` → `Optional<LlmProjectContext>`: liest gemerktes Projekt des Users aus `remembered_project`; wenn vorhanden: lädt Projekt + Positionen + Freelancer + Tags via JdbcClient-JOINs
- [ ] Test: `ProfileSearchQueryServiceIT` ergänzt: buildLlmContext ohne gemerktes Projekt → empty; mit Projekt und Positionen → vollständiger Kontext
- [ ] Git-Commit

### 6.5 Profilsuche – LLM-Interface + Stub
- [ ] Interface `LlmService` im Paket `de.mirkosertic.powerstaff.profilesearch`: `String sendMessage(Optional<LlmProjectContext> context, List<ProfileSearchMessage> history, String userMessage)`
- [ ] `StubLlmService` implements `LlmService`: gibt `"Die KI-Profilsuche ist in Release 1.0 noch nicht aktiviert."` zurück
- [ ] `ProfileSearchConfig` (`@Configuration`): `@Bean LlmService llmService() { return new StubLlmService(); }`
- [ ] Test: `StubLlmServiceSpec` (Unit-Test): prüft Rückgabewert
- [ ] Git-Commit

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
