# Powerstaff 2026 – Softwarearchitektur-Spezifikation

**Stand:** März 2026
**Status:** Verbindlich – alle ADRs final, KI-Integration vorläufig offen (siehe ADR-005)

---

## Inhaltsverzeichnis

1. [Technologie-Stack](#1-technologie-stack)
2. [Modulstruktur (Spring Modulith)](#2-modulstruktur-spring-modulith)
3. [Architekturprinzip: Logisches CQRS](#3-architekturprinzip-logisches-cqrs)
4. [Persistenzstrategie](#4-persistenzstrategie)
5. [Frontend-Strategie](#5-frontend-strategie)
6. [Validierungsstrategie](#6-validierungsstrategie)
7. [Authentifizierung und Autorisierung](#7-authentifizierung-und-autorisierung)
8. [KI-Integration (Profilsuche)](#8-ki-integration-profilsuche)
9. [Testarchitektur](#9-testarchitektur)
10. [Lokale Entwicklungsumgebung](#10-lokale-entwicklungsumgebung)
11. [Deployment](#11-deployment)
12. [Architekturentscheidungen (ADR)](#12-architekturentscheidungen-adr)

---

## 1. Technologie-Stack

| Schicht          | Technologie                                     | Version                  |
|------------------|-------------------------------------------------|--------------------------|
| Laufzeit         | Java                                            | 25                       |
| Framework        | Spring Boot                                     | 4.x                      |
| Modularchitektur | Spring Boot Modulith                            | aktuell zu Spring Boot 4 |
| Persistenz       | Spring Data JDBC                                | aktuell zu Spring Boot 4 |
| Template-Engine  | Thymeleaf                                       | aktuell zu Spring Boot 4 |
| Datenbank        | MySQL                                           | 8.x                      |
| Sicherheit       | Spring Security (Form Login)                    | aktuell zu Spring Boot 4 |
| Schema-Migration | Flyway                                          | aktuell zu Spring Boot 4 |
| Validierung      | Jakarta Bean Validation                         | aktuell zu Spring Boot 4 |
| Build (Backend)  | Maven + Maven Wrapper                           | 3.x                      |
| Build (Frontend) | Vite                                            | aktuell                  |
| Frontend         | Vanilla JS / Custom Elements (Light DOM) / AJAX | –                        |
| Test-Framework   | Spock Framework (Groovy)                        | 2.x                      |
| Mutation-Testing | PITest + pitest-spock-plugin                    | aktuell                  |

---

## 2. Modulstruktur (Spring Modulith)

Das System ist als **Spring Boot Modulith** organisiert. Jedes fachliche Modul entspricht einem Java-Paket unterhalb des Root-Pakets `de.powerstaff`. Spring Modulith erzwingt und dokumentiert Modulgrenzen automatisch.

```
de.powerstaff
├── freelancer        # Freiberuflerverwaltung
├── partner           # Partnerverwaltung
├── customer          # Kundenverwaltung
├── project           # Projektverwaltung inkl. Projektpositionen
├── profilesearch     # Profilsuche (UI + Persistenz + KI-Schnittstelle)
└── shared            # Systemweite Stammdaten + Admin-UI
```

### Das `shared`-Modul

`shared` ist ein vollwertiges Modul mit Command-Seite, Query-Seite und eigenem Controller – strukturell identisch zu den fachlichen Modulen.

**Inhalt:**

| Entität                 | Beschreibung                                                 | Genutzt von                         |
|-------------------------|--------------------------------------------------------------|-------------------------------------|
| `historytype`           | Konfigurierbare Typen für Kontakthistorie-Einträge           | `freelancer`, `partner`, `customer` |
| Tag-Kategorien          | Schwerpunkt, Funktion, Einsatzort, Bemerkung, Typ            | `freelancer`                        |
| Gemeinsame Werteobjekte | Z.B. `Address` falls mehrere Module dieselbe Struktur nutzen | alle Module                         |

**Admin-UI:**
`shared` stellt einen eigenen Verwaltungsbereich bereit, über den alle eingeloggten Sachbearbeiter die Stammdaten pflegen können (Kontakthistorie-Typen anlegen/umbenennen, Tag-Kategorien verwalten). Die Admin-UI ist nicht rollengeschützt – sie folgt dem flachen Autorisierungsmodell (siehe Abschnitt 7) und denselben Design-System-Konventionen wie alle anderen Formulare.

**Zugriff durch andere Module:**
Andere Module lesen `shared`-Daten ausschließlich über den **Query-Service von `shared`** – kein direkter Tabellenzugriff aus fachlichen Modulen auf `shared`-Tabellen. Das ist die einzige Ausnahme vom generellen Prinzip der Query-Seite, die modul-übergreifende Joins erlaubt: `shared`-Tabellen werden über die publizierte Query-API konsumiert, um Änderungen an der `shared`-Datenstruktur zentral kontrollierbar zu halten.

### Modulregeln

- Jedes Modul **kapselt seine eigenen Aggregate und Repositories** vollständig.
- **Zwischen Modulen sind keine direkten Repository-Aufrufe erlaubt.** Kommunikation läuft ausschließlich über publizierte Interfaces oder Domain Events.
- **Ausnahme Query-Seite:** Die Lese-Schicht (Query-Seite des logischen CQRS, siehe Abschnitt 3) darf modul-übergreifend auf Datenbanktabellen zugreifen. Diese Lesezugriffe sind explizit als Query-Services gekennzeichnet und dürfen keine schreibenden Operationen enthalten.
- Spring Modulith Integrationstests (`@ApplicationModuleTest`) verifizieren die Modulgrenzen automatisch als Teil der CI-Pipeline.

### Modul-Kommunikation

| Art                | Mechanismus                                                  | Einsatzbereich                                        |
|--------------------|--------------------------------------------------------------|-------------------------------------------------------|
| Zustandsänderungen | Spring Modulith Domain Events (`@ApplicationModuleListener`) | Löschen mit RESTRICT-Logik, kaskadierende Operationen |
| Lesezugriffe       | Query-Services mit `JdbcClient` (direkte SQL-Projektionen)   | Suchergebnisse, Cross-Modul-Ansichten, KI-Kontext     |

**Wichtig – Synchronität bei RESTRICT-Checks:**
Spring Modulith publiziert `@ApplicationModuleListener` standardmäßig nach dem Transaktions-Commit. Für RESTRICT-Checks (Löschen verhindern) ist das zu spät – der Datensatz wäre bereits gelöscht. RESTRICT-Checks müssen daher über **synchrone Modul-APIs** (publizierte Interfaces) implementiert werden, nicht über asynchrone Events.

Konkretes Muster:
- Das `freelancer`-Modul ruft vor der Löschung ein publiziertes Interface `ProjectIntegrityGuard` aus dem `project`-Modul auf
- `ProjectIntegrityGuard.assertFreelancerDeletable(freelancerId)` prüft synchron und wirft eine fachliche Exception mit Links zu blockierenden Projekten
- **Domain Events** sind ausschließlich für Benachrichtigungen nach erfolgreichen Operationen vorgesehen (z.B. Audit-Log-Einträge, Caches invalidieren), nicht für Integritätsprüfungen

| Kommunikationsart           | Mechanismus                                                  | Einsatzbereich                                    |
|-----------------------------|--------------------------------------------------------------|---------------------------------------------------|
| RESTRICT-Integritätsprüfung | Synchroner Aufruf eines publizierten Modul-Interface         | Löschen verhindern, bevor es passiert             |
| Nachricht nach Erfolg       | Spring Modulith Domain Events (`@ApplicationModuleListener`) | Audit-Logging, kaskadierende Folgeaktionen        |
| Lesezugriffe                | Query-Services mit `JdbcClient`                              | Suchergebnisse, Cross-Modul-Ansichten, KI-Kontext |

---

## 3. Architekturprinzip: Logisches CQRS

Powerstaff 2026 wendet **logisches CQRS** (Command Query Responsibility Segregation) konsequent an. Es gibt **keine** separaten Read/Write-Stores (kein physisches CQRS, kein Event Sourcing) – die Trennung erfolgt ausschließlich auf Code-Ebene.

### Motivation

Das System hat zwei strukturell verschiedene Leseanforderungen:

1. **Formular-Schreiboperationen** arbeiten auf vollständigen Aggregaten mit Geschäftsregeln (Optimistic Locking, Validierung, Integritätsprüfungen).
2. **Suchergebnisse, Listenansichten und der KI-Kontext** benötigen schlanke Projektionen aus mehreren Tabellen – ohne Aggregate laden zu müssen und ggf. modul-übergreifend.

Das direkte Mischen beider Anforderungen in einem Modell führt zu unnötig komplexen Aggregaten, unbeabsichtigtem Laden von Daten und schwer testbarem Code.

### Command-Seite

```
[Controller] → [CommandService] → [Aggregate] → [Repository (Spring Data JDBC)]
                                      ↓
                              [Domain Event]
                                      ↓
                       [ApplicationModuleListener]
```

- Arbeitet ausschließlich mit Aggregaten (Spring Data JDBC Entities)
- Erzwingt Geschäftsregeln und Optimistic Locking (`db_version`)
- Emittiert Domain Events für Seiteneffekte in anderen Modulen
- Repositories sind `package-private` – nur der eigene CommandService hat Zugriff

### Query-Seite

```
[Controller] → [QueryService] → [JdbcClient] → SQL mit Projektionen → [Java Record]
```

- Arbeitet ausschließlich mit `JdbcClient` und handgeschriebenem SQL
- Gibt **Java Records** als Projektionen zurück (unveränderlich, kein Domain-Overhead)
- Darf modul-übergreifend SQL-Joins verwenden (z.B. `profilesearch` liest aus `freelancer`- und `project`-Tabellen)
- Keinerlei schreibende Operationen
- Kein Optimistic Locking, keine Aggregat-Instantiierung

**Modulzugehörigkeit cross-modularer Query-Services:**
Ein Query-Service, dessen SQL Tabellen mehrerer Module verknüpft, gehört immer zum **Modul, das den Use Case besitzt und das Ergebnis rendert** – also zum konsumierenden Modul. Beispiel: `ProfileSearchQueryService` liegt in `de.powerstaff.profilesearch`, obwohl er `freelancer`-, `project`- und `tag`-Tabellen joined.

Da die Query-Seite ausschließlich `JdbcClient` und eigene Java Records verwendet, entstehen **keine Java-Package-Abhängigkeiten** auf andere Modulpakete. Spring Modulith prüft Java-Typabhängigkeiten, nicht SQL-Tabellennamen – cross-modulare SQL-JOINs erzeugen daher keinen Modulgrenzverstoß.

**`@ApplicationModuleTest`-Verhalten:** Ein `@ApplicationModuleTest(mode = STANDALONE)` auf ein Modul bootstrapt nur dessen eigenen Spring-Kontext. Der `JdbcClient`-SQL-JOIN läuft gegen die Testcontainer-Datenbank, in der alle Tabellen existieren. Die Modulgrenzvalidierung prüft Java-Typen – der Test schlägt nicht fehl, solange keine Java-Typen aus anderen Modulpaketen importiert werden.

**Ausnahme `shared`:** Tabellen des `shared`-Moduls werden **nicht** direkt per SQL-JOIN aus anderen Modulen gelesen. Zugriff erfolgt ausschließlich über den publizierten `SharedQueryService`. Damit bleiben Änderungen an der `shared`-Datenstruktur zentral kontrollierbar (siehe Abschnitt 2).

### Paketstruktur pro Modul (Beispiel `freelancer`)

```
de.powerstaff.freelancer
├── command
│   ├── FreelancerCommandService.java
│   ├── FreelancerAggregate.java           # Aggregate Root (inkl. FreelancerContact-Kinder)
│   ├── FreelancerRepository.java          # package-private
│   ├── FreelancerHistoryEntry.java        # eigener Aggregate Root
│   └── FreelancerHistoryRepository.java   # package-private
├── query
│   ├── FreelancerQueryService.java
│   ├── FreelancerListView.java            # Java Record
│   └── FreelancerSearchResult.java        # Java Record
└── api
    └── FreelancerController.java          # Thymeleaf MVC Controller
```

### Thymeleaf + CQRS: Doppel-Binding-Muster

In einem CQRS-Controller müssen für eine Seite zwei verschiedene Objekttypen ins Spring-MVC-`Model` geladen werden:

- Das **Command-Objekt** – aus dem Aggregat befüllt, dient als `th:object`-Ziel für Formular-Binding und Bean Validation
- **Query-Records** – aus QueryServices geladen, dienen für Dropdown-Listen, Kontextinformationen etc.

**Standard-Muster im GET-Handler:**

```java
@GetMapping("/{id}")
public String show(@PathVariable Long id, Model model) {
    FreelancerAggregate freelancer = freelancerCommandService.findById(id);
    model.addAttribute("command", FreelancerUpdateCommand.from(freelancer));
    prepareModel(model);
    return "freelancer/form";
}
```

**Fehlerfall – Model beim Re-Render neu befüllen:**

Bei `BindingResult`-Fehlern rendert Spring MVC das Template erneut, ohne Query-Records automatisch beizubehalten. Dropdowns und Kontextdaten müssen explizit wieder ins Model geladen werden:

```java
@PostMapping("/{id}")
public String update(@PathVariable Long id,
                     @Valid @ModelAttribute("command") FreelancerUpdateCommand command,
                     BindingResult result, Model model) {
    if (result.hasErrors()) {
        prepareModel(model);
        return "freelancer/form";
    }
    freelancerCommandService.update(id, command);
    return "redirect:/freiberufler/" + id;
}
```

**Konvention `prepareModel`:**

Jeder Controller, der Query-Daten für das Rendering benötigt, kapselt das Befüllen in einer privaten `prepareModel(Model model)`-Methode. Diese wird sowohl im GET-Handler als auch im Fehler-Re-Render des POST-Handlers aufgerufen – keine Duplikation, keine eigene Abstraktion.

```java
private void prepareModel(Model model) {
    model.addAttribute("historytypes", sharedQueryService.findAllHistoryTypes());
    model.addAttribute("tags", sharedQueryService.findAllTags());
}
```

### QBE-Suche (Query By Example)

Alle Suchmasken sind als QBE (Query By Example) implementiert. Die eingetragenen Formularfelder werden serverseitig zu einer dynamischen `WHERE`-Klausel mit `LIKE`-Prüfungen (AND-verknüpft) zusammengesetzt. Die Implementierung erfolgt ausschließlich auf der Query-Seite via `JdbcClient` und dynamischem SQL – kein Spring Data Specifications oder QueryDSL.

**SQL-Injection-Sicherheit:** Feldnamen der WHERE-Klausel sind stets Compile-Zeit-Konstanten im Code – niemals aus Benutzereingaben abgeleitet. Werte werden ausnahmslos als gebundene Parameter übergeben (`:paramName`-Syntax von `JdbcClient`). String-Konkatenation von Nutzereingaben in SQL-Fragmenten ist verboten.

**Testpflicht:** Jede QBE-Suche muss durch einen `@DataJdbcTest`-Integrationstest gegen Testcontainers (echte MySQL) abgedeckt sein. Dabei müssen alle Filterfelder in mindestens einem Testfall befüllt sein, um Schema-Drifts und Typ-Abweichungen sofort zu erkennen (vgl. Abschnitt 9).

---

## 4. Persistenzstrategie

### Spring Data JDBC

Die Persistenz erfolgt über **Spring Data JDBC** (nicht JPA/Hibernate). Repositories leiten von `CrudRepository` oder `ListCrudRepository` ab.

**Designregeln für Aggregate:**

- Jedes Modul definiert seinen eigenen **Aggregate Root** – die einzige Klasse, über die das Aggregate persistent gemacht wird.
- Spring Data JDBC hat **kein Lazy Loading**: Alle Kindentitäten innerhalb eines Aggregates werden bei jedem `findById`-Aufruf vollständig mitgeladen. Daraus folgt:
  - Entitäten, die unbegrenzt wachsen (z.B. Historien), müssen **eigene Aggregate Roots** sein – sonst führt jedes Laden des Haupt-Aggregates zu vollständigem Laden aller Historieneinträge.
  - Entitäten, die immer vollständig benötigt werden und mengenmäßig begrenzt sind (z.B. Kontakte), können Kinder des Haupt-Aggregates sein.
- **Tags** (`freelancer_tags` etc.) speichern ausschließlich eine `tag_id`-Referenz als primitiven Wert (`Long`) – keine eigene Entitätsklasse. Die Tag-Definition liegt im `shared`-Modul und wird über den `SharedQueryService` abgerufen.
- **Historien** (`freelancer_history`, `partner_history` etc.) sind eigene Aggregate Roots mit eigenem Repository. Löschkaskaden werden auf Datenbankebene per `ON DELETE CASCADE` sichergestellt.
- **Projektpositionen** (`project_position`) sind ein eigener Aggregate Root im `project`-Modul. Der Status einer Position kann unabhängig vom Projekt geändert werden; ein gemeinsames Aggregat würde jeden Statuswechsel mit einem vollständigen Projekt-Lock belasten.
- Die konkreten Aggregate-Grenzen pro Modul sind im nächsten Abschnitt tabellarisch dokumentiert.

### Aggregate-Grenzen pro Modul

| Modul        | Aggregate Root        | Kinder (immer mitgeladen)                         | Separate Aggregate Roots                     | Nur ID-Referenz                        |
|--------------|-----------------------|---------------------------------------------------|----------------------------------------------|----------------------------------------|
| `freelancer` | `FreelancerAggregate` | `FreelancerContact` (Liste, mengenmäßig begrenzt) | `FreelancerHistoryEntry` (wächst unbegrenzt) | `tag_id`-Liste (aus `freelancer_tags`) |
| `partner`    | `PartnerAggregate`    | `PartnerContact`                                  | `PartnerHistoryEntry`                        | –                                      |
| `customer`   | `CustomerAggregate`   | `CustomerContact`                                 | `CustomerHistoryEntry`                       | –                                      |
| `project`    | `ProjectAggregate`    | –                                                 | `ProjectPosition`, `ProjectHistoryEntry`     | –                                      |
| `shared`     | `Tag`, `HistoryType`  | –                                                 | –                                            | –                                      |

**Löschkaskaden:**

Beim Löschen eines Aggregate Roots werden abhängige separate Aggregate Roots auf Datenbankebene per `ON DELETE CASCADE` mitgelöscht:

| Gelöschter Root       | Kaskadiert zu                                                                 |
|-----------------------|-------------------------------------------------------------------------------|
| `FreelancerAggregate` | `freelancer_contact` (Aggregat-Kind), `freelancer_history`, `freelancer_tags` |
| `PartnerAggregate`    | `partner_contact` (Aggregat-Kind), `partner_history`                          |
| `CustomerAggregate`   | `kunde_contact` (Aggregat-Kind), `kunde_history`                              |
| `ProjectAggregate`    | `project_position`, `project_history`, `remembered_project`                   |

Spring Data JDBC löscht Aggregat-Kinder (z.B. `FreelancerContact`) automatisch beim Löschen des Roots. Für separate Aggregate Roots (z.B. `FreelancerHistoryEntry`) übernimmt die Datenbank-Cascade per FK-Constraint.

### Optimistic Locking

Alle Aggregate-Roots führen ein `@Version`-Feld (`db_version`). Bei Konflikt wird eine `OptimisticLockingFailureException` geworfen, die der Controller als Fehlermeldung mit der Wahlmöglichkeit „Eigene Version durchsetzen" oder „Aktuellen Stand laden" präsentiert.

### Schema-Management mit Flyway

Das Datenbankschema wird ausschließlich durch **Flyway**-Migrationsskripte verwaltet. Eine automatische Schema-Generierung aus Entitäten oder DTOs (`spring.jpa.hibernate.ddl-auto`, `spring.sql.init.mode` o.ä.) ist explizit deaktiviert und darf nicht verwendet werden.

Migrationsskripte liegen unter `src/main/resources/db/migration` und folgen dem Namensschema `V{version}__{beschreibung}.sql` (Beispiel: `V1__init_schema.sql`, `V2__add_freelancer_tags.sql`).

Pflichtregeln für Migrationsskripte:
- Bestehende Migrationsskripte werden **niemals nachträglich geändert** – nur neue Skripte hinzufügen
- Jedes Skript ist in sich abgeschlossen und idempotent ausführbar, wo möglich (`IF NOT EXISTS`, `IF EXISTS`)
- DDL und DML (Stammdaten-Seeding) werden in getrennten Skripten gehalten

### Caching

Powerstaff 2026 verzichtet vollständig auf Caching. Es wird kein `@EnableCaching`, kein Spring Cache Abstraction Layer und kein externer Cache (Redis o.ä.) eingesetzt.

**Begründung:** Die Benutzermenge ist überschaubar, die Datenmengen sind gering und es gibt keine rechenintensiven oder häufig wiederholten Abfragen, die einen Cache rechtfertigen würden. Der Profilsuche-Kontext muss ohnehin live aus der Datenbank gelesen werden. Für `shared`-Stammdaten (historytype, Tag-Kategorien) reicht die Datenbankperformance bei der gegebenen Last aus. Das Einführen von Caching ohne konkreten Bedarf würde unnötige Komplexität (Cache-Invalidierung, Konsistenzprobleme) einbringen.

### Gemerktes Projekt – Implementierungsdetails

Das gemerkte Projekt (`remembered_project`) ist ein Aggregat-Kind von `ProjectAggregate` im **`project`-Modul** (direkte FK-Beziehung, semantischer Bezug). Die schreibenden Operationen liegen auf der Command-Seite bei `ProjectCommandService`.

**Setzen – automatischer Side-Effect der Navigation:**
Ein separater POST-Endpunkt zum Setzen des gemerkten Projekts existiert nicht. Das Merken ist ein automatischer Side-Effect jedes `GET /projekte/{id}`-Aufrufs im `ProjectController`:

```java
@GetMapping("/{id}")
public String show(@PathVariable Long id,
                   @AuthenticationPrincipal UserDetails user,
                   Model model) {
    projectCommandService.rememberProject(user.getUsername(), id);
    // ...
}
```

`ProjectCommandService.rememberProject` führt einen Upsert auf `remembered_project` durch (INSERT … ON CONFLICT DO UPDATE). Die `user_id` ist der `username` aus `ps_user`, der per `@AuthenticationPrincipal UserDetails` direkt im Controller abgerufen wird – kein Helper, kein eigener Service.

**Entfernen (Dismiss):**
Ein dedizierter Endpunkt erlaubt das explizite Entfernen durch den Nutzer (Dismiss-Aktion in `<ps-project-pill>`):

```
DELETE /projekte/gemerkt  →  204 No Content
```

Der Aufruf erfolgt per `fetch()` aus dem Custom Element, ohne Page-Reload.

**Back-Button-Verhalten:**
Der Browser-Back-Button löst bei gecachten Seiten keinen neuen Server-Request aus. Das gemerkte Projekt auf dem Server bleibt unverändert – es zeigt weiterhin das zuletzt aktiv per GET-Request navigierte Projekt. Kein Sonderverhalten erforderlich.

**Zusammenspiel mit `<ps-project-pill>`:**
Der Server rendert `#tb-project` konditional (nur wenn ein gemerktes Projekt existiert):

```html
<!-- Thymeleaf: th:if="${rememberedProject != null}" -->
<ps-project-pill>
  <div id="tb-project">
    <span>Projektname</span>
    <button class="dismiss-btn">✕</button>
  </div>
</ps-project-pill>
```

Das Custom Element kapselt ausschließlich die Dismiss-Logik: Klick auf `.dismiss-btn` → `DELETE /projekte/gemerkt` → bei 204 entfernt sich das Element selbst aus dem DOM (`this.remove()`). Eine AJAX-Aktualisierung der restlichen Toolbar ist nicht erforderlich.

**Löschkaskade:**
Wird ein Projekt gelöscht, entfällt der `remembered_project`-Eintrag aller betroffenen Sachbearbeiter automatisch per `ON DELETE CASCADE` auf Datenbankebene (vgl. Löschkaskaden-Tabelle oben). Eine RESTRICT-Sperre beim Projektlöschen wäre fachlich falsch.

---

## 5. Frontend-Strategie

### Server-Side Rendering mit Thymeleaf

Alle Seiten werden serverseitig mit **Thymeleaf** gerendert. Es gibt kein JavaScript-Frontend-Framework. Das HTML wird vollständig vom Server geliefert.

- Templates liegen unter `src/main/resources/templates`
- Das UI-Design-System (`base.css`, HTML-Musterkatalog) gemäß `UI-DESIGNSYSTEM.md` ist verbindlich
- Alle Thymeleaf-Fragmente werden in `templates/fragments/` abgelegt und per `th:replace` eingebunden

### Custom Elements (Light DOM Web Components)

Wiederverwendbare interaktive UI-Elemente werden als **Custom Elements ohne Shadow DOM** implementiert. Der Tag-Name ist der stabile Vertrag zwischen Thymeleaf-Template und JavaScript – keine CSS-Klassen als JS-Hook.

**Prinzip – Progressive Enhancement:**
Der Server rendert das vollständige HTML inklusive Custom-Element-Tags. Der Browser zeigt das Element sofort als normales HTML. Erst nach dem JS-Load "upgraded" der Browser das Element durch `customElements.define()` und fügt interaktives Verhalten hinzu.

```html
<!-- Server rendert (Thymeleaf): -->
<ps-modal trigger="edit-btn" title="Freiberufler bearbeiten">
  <form th:fragment="...">...</form>
</ps-modal>

<!-- JS upgraded das Element, kapselt open/close, Focus-Trap, ARIA -->
```

**Vorgesehene Custom Elements:**

| Custom Element          | Kapseltes Verhalten                                 | Entspricht Design-System-Muster                        |
|-------------------------|-----------------------------------------------------|--------------------------------------------------------|
| `<ps-modal>`            | open/close, Focus-Trap, Escape-Handler, ARIA-Rollen | `.mbk`-Struktur (UI-DESIGNSYSTEM.md §17)               |
| `<ps-infinite-scroll>`  | IntersectionObserver, AJAX-Nachladen, Loading-State | `<table>`-Wrapper (UI-DESIGNSYSTEM.md §16)             |
| `<ps-growing-textarea>` | auto-wachsende Textarea (max. 6 Zeilen)             | `#chat-input`-Textarea (UI-DESIGNSYSTEM.md §27)        |
| `<ps-status-badge>`     | Farbsetzung aus `data-bg`/`data-fg`-Attributen      | `.badge-dyn` mit inline-style (UI-DESIGNSYSTEM.md §22) |
| `<ps-project-pill>`     | Sichtbarkeit, Dismiss-Logik                         | `#tb-project` (UI-DESIGNSYSTEM.md §21)                 |
| `<ps-tag-chip>`         | Entfernen mit Bestätigungsschritt                   | `.chip`-Element (UI-DESIGNSYSTEM.md §14)               |

Shadow DOM wird **nicht** eingesetzt – `base.css` soll global für alle Elemente gelten.

**Verhältnis Custom Elements ↔ Design-System:**
Custom Elements sind **keine Ersetzung** der HTML-Muster aus `UI-DESIGNSYSTEM.md`, sondern deren **Progressive-Enhancement-Schicht**. Der Server rendert die vollständige Design-System-konforme HTML-Struktur (`.mbk`, `.chip`, `.badge-dyn` etc.) innerhalb des Custom-Element-Tags. Ohne JS zeigt der Browser das Element als normales HTML; mit JS upgradet `customElements.define()` das Element und fügt Verhalten hinzu — ohne die innere HTML-Struktur zu verändern.

```html
<!-- ps-modal: Design-System-Struktur (§17) innerhalb des Custom-Element-Tags -->
<ps-modal>
  <div class="mbk" id="modal-kontakt">
    <div class="mbox">
      <div class="mhd">
        <h3>Kontaktmöglichkeit bearbeiten</h3>
        <button class="mx">✕</button>
      </div>
      <div class="mbody"><!-- Felder --></div>
      <div class="mft">
        <button class="btn btn-ghost">Abbrechen</button>
        <button class="btn btn-pri">Speichern</button>
      </div>
    </div>
  </div>
</ps-modal>
<!-- Ohne JS: <ps-modal> rendert als Block-Element, .mbk steuert Darstellung -->
<!-- Mit JS:  customElements.define('ps-modal', ...) kapselt open/close/Focus-Trap/ARIA -->
```

### Frontend Build mit Vite

JavaScript-Quellen und CSS liegen unter `src/main/frontend/`. Vite bündelt, tree-shakt und gibt versionierte Assets unter `src/main/resources/static/assets/` aus.

**Verzeichnisstruktur:**

```
src/main/frontend/
├── js/
│   ├── components/          ← Custom Elements
│   │   ├── ps-modal.js
│   │   ├── ps-infinite-scroll.js
│   │   ├── ps-status-badge.js
│   │   └── ...
│   └── main.js              ← Einstiegspunkt, registriert alle Custom Elements
└── css/
    └── base.css             ← Design-System Stylesheet

vite.config.js               ← im Repository-Root (neben pom.xml und package.json)
package.json                 ← im Repository-Root
.nvmrc                       ← im Repository-Root
```

**Asset-Auflösung via Vite Manifest:**

Vite erzeugt in Production gehashte Dateinamen (`main-Dq8fKmNz.js`) für optimales Browser-Caching. Thymeleaf muss diese Hashes kennen. Vite generiert dafür eine `manifest.json`, die ein Spring-`@Component` beim Start einliest und über einen Thymeleaf-Dialect als `#vite.asset('js/main.js')` verfügbar macht. In Development zeigt derselbe Aufruf auf den Vite Dev Server – der Template-Code ist damit in beiden Umgebungen identisch.

**Entwicklungs-Workflow:**

```
Spring Boot (Port 8080)      → SSR HTML + Business-Logik
Vite Dev Server (Port 5173)  → JS/CSS mit Hot Module Replacement

Vite proxied alle HTML-Requests an Spring Boot:
Browser → :5173 → Spring Boot :8080  (für HTML-Seiten)
               → Vite selbst         (für JS/CSS/HMR-Websocket)
```

Minimale `vite.config.js` mit Proxy-Konfiguration:

```js
import { defineConfig } from 'vite'

export default defineConfig({
  root: 'src/main/frontend',
  build: {
    outDir: '../resources/static/assets',
    emptyOutDir: true,
    manifest: true,
    rollupOptions: {
      input: 'src/main/frontend/js/main.js'
    }
  },
  server: {
    port: 5173,
    proxy: {
      // Alle Nicht-Asset-Requests an Spring Boot weiterleiten
      '^(?!/assets|/@vite|/src).*': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

**Maven-Integration:**

`maven-exec-plugin` ruft `npm install` und `npm run build` in der `generate-resources`-Phase auf – vor dem Spring Boot Ressourcen-Packaging. `./mvnw package` produziert damit ein vollständiges, deploymentfähiges JAR inklusive gebündelter Frontend-Assets. Node.js muss auf der Entwicklermaschine und im CI-System installiert sein; die gewünschte Version ist in `.nvmrc` dokumentiert.

### Vanilla JS / AJAX

Außerhalb der Custom Elements werden einfache Interaktionen direkt als Vanilla JS in `main.js` implementiert. Es gibt keine Abhängigkeit zu jQuery, Alpine.js oder anderen JS-Bibliotheken.

AJAX-Endpunkte liefern entweder:
- **HTML-Fragmente** (Thymeleaf-Fragment-Rendering, bevorzugt) oder
- **JSON** (ausschließlich für strukturierte Daten wie Chat-Nachrichten)

### CSRF-Schutz für AJAX-Requests

Spring Security schützt alle zustandsverändernden Requests (`POST`, `PUT`, `PATCH`, `DELETE`) mit CSRF-Tokens. Thymeleaf bettet diese automatisch in HTML-Formulare ein. Für AJAX-Requests muss der Token manuell mitgesendet werden.

**Strategie:** Spring Security wird mit `CookieCsrfTokenRepository.withHttpOnlyFalse()` und `XorCsrfTokenRequestAttributeHandler` konfiguriert. Der Token im Attribut `${_csrf.token}` ist der XOR-maskierte Token, den Spring Security erwartet.

**Token-Bereitstellung im Frontend:** Das Layout-Fragment (`layout.html`) rendert den maskierten Token in einem Meta-Tag:
```html
<meta name="csrf-token" th:content="${_csrf.token}">
```
Die globale Funktion `getCsrfToken()` in `main.js` liest diesen Wert aus:
```javascript
function getCsrfToken() {
  return document.querySelector('meta[name="csrf-token"]')?.content ?? null;
}
```
**Wichtig:** Nicht den rohen Cookie-Wert (`XSRF-TOKEN`) lesen – dieser ist der unmaskierte Token und wird vom Backend mit `XorCsrfTokenRequestAttributeHandler` abgelehnt. Nicht als URL-Query-Parameter übergeben – das würde den Token in Server-Logs kompromittieren.

- **Formular-basierte Requests** (`FormData` / `URLSearchParams`): Thymeleaf bettet `_csrf` automatisch als Hidden-Field ein – kein manueller Aufwand. Das Hidden-Field enthält `${_csrf.token}` (den maskierten Token).
- **Bodylose POSTs, DELETE-Requests und JSON-POSTs**: Den `X-XSRF-TOKEN`-Header mit dem maskierten Token setzen:
  ```javascript
  fetch('/resource/delete/' + id, {
    method: 'POST',
    headers: { 'X-XSRF-TOKEN': getCsrfToken() }
  });
  ```

**Regel:** Alle AJAX-Aufrufe nutzen `fetch()` direkt (kein `apiFetch()`). Formular-Submits übergeben CSRF über das Hidden-Field. Bodylose POSTs, DELETE-Requests und JSON-POSTs setzen `'X-XSRF-TOKEN': getCsrfToken()` im Header.

### Infinite Scrolling

Suchergebnisse und Sitzungslisten werden mit Initial-Load von 20 Einträgen und automatischem Nachladen via `<ps-infinite-scroll>` implementiert. Der Server liefert paginierte HTML-Fragmente.

**URL-Schema:** Seite 0 wird direkt in die Vollseite gerendert. Das Custom Element lädt ab Seite 1. QBE-Parameter sind Teil der URL und werden durchgereicht:

```
GET /freiberufler/results?page=1&size=20&name=Müller&skill=Java
```

**Response-Format:** HTML-Fragment – `<tr>`-Zeilen, die direkt in `<tbody>` eingefügt werden. Kein JSON.

**Letzte Seite:** Leere Response (HTTP 200, leerer Body). Das Custom Element entfernt den Sentinel und stoppt den IntersectionObserver.

**`<ps-infinite-scroll>`-API:**

```html
<ps-infinite-scroll
  data-src="/freiberufler/results?page=1&size=20&name=Müller"
  data-container="#results-tbody">
  <table>
    <tbody id="results-tbody"><!-- Seite 0 vom Server gerendert --></tbody>
  </table>
  <div class="scroll-sentinel"></div>
</ps-infinite-scroll>
```

- `data-src`: URL der nächsten zu ladenden Seite; das Element inkrementiert den `page`-Parameter nach jedem erfolgreichen Fetch selbst
- `data-container`: CSS-Selektor des Elements, in das neue Zeilen eingefügt werden
- QBE-Parameter sind beim initialen Render server-seitig in `data-src` eingebettet und werden automatisch durch alle Folgeseiten mitgeschleppt

### URL-Schema und Bookmarkbarkeit

**Grundsatz:** Jedes Aggregat und jede Chat-Sitzung hat eine eigene, bookmarkbare URL. Kein fachlicher Zustand ist ausschließlich in Browser-Session oder JS-State gespeichert – der Server kann jeden Zustand anhand der URL allein rekonstruieren.

**URL-Schema:**

| Modul              | Leerformular / QBE-Maske | Datensatz                  | Suchergebnis (nach replaceState)   |
|--------------------|--------------------------|----------------------------|------------------------------------|
| Freiberufler       | `/freiberufler`          | `/freiberufler/{id}`       | `/freiberufler?name=...&skill=...` |
| Partner            | `/partner`               | `/partner/{id}`            | `/partner?name=...`                |
| Kunden             | `/kunden`                | `/kunden/{id}`             | `/kunden?name=...`                 |
| Projekte           | `/projekte`              | `/projekte/{id}`           | `/projekte?nummer=...&status=...`  |
| Profilsuche        | `/profilsuche`           | `/profilsuche/{sessionId}` | –                                  |
| Admin / Stammdaten | `/admin`                 | `/admin/{bereich}`         | –                                  |

**Admin-Bereiche** (`/admin/{bereich}`):

| Pfad                     | Inhalt                                       |
|--------------------------|----------------------------------------------|
| `/admin/historientypen`  | Kontakthistorie-Typen verwalten              |
| `/admin/tag-kategorien`  | Tag-Kategorien verwalten                     |
| `/admin/positionsstatus` | Projektpositions-Status und Farben verwalten |

### Browser Back Button – Strategie

**Grundsatz:** Der Back Button navigiert in der URL-History. Er macht keine Datenbankoperationen rückgängig – das ist akzeptiertes und dem Sachbearbeiter bekanntes Browser-Verhalten.

**QBE-Suche – POST + `history.replaceState`:**
QBE-Formulare senden per `POST` (kein Browser-Caching, kein Resubmit-Dialog). Nach dem Rendern der Ergebnisse ersetzt ein JS-Snippet die aktuelle History-Entry mit der entsprechenden GET-URL inkl. Suchparametern – ohne Server-Request:

```js
// Wird nach dem Seitenrendering ausgeführt, z.B. via data-Attribut im Template
const params = document.querySelector('[data-search-params]')?.dataset.searchParams;
if (params) history.replaceState({}, '', location.pathname + '?' + params);
```

Der Server muss GET-Requests mit Suchparametern identisch zu POST-Requests auswerten. Back Button führt damit zur vorherigen Seite – kein Resubmit-Dialog, URL ist bookmarkbar.

**Modals – kein History-Eintrag:**
Modals erzeugen keinen History-Eintrag. Der Back Button schließt ein geöffnetes Modal nicht, sondern navigiert zur vorherigen Seite. Modals werden ausschließlich über Escape oder Abbrechen-Button geschlossen. `<ps-modal>` implementiert kein `history.pushState`.

**Profilsuche-Chat:**
Jede Chat-Sitzung hat eine eigene URL (`/profilsuche/{sessionId}`). Der Wechsel zwischen Sitzungen in der Sidebar ändert die URL und erzeugt einen History-Eintrag. Der Back Button navigiert rückwärts durch die Sitzungshistory. Chat-Nachrichten werden durch Back-Navigation nicht rückgängig gemacht – sie sind server-seitig persistiert und bleiben unverändert erhalten.

**bfcache (Back/Forward Cache):**
Moderne Browser cachen ganze Seiten für sofortige Back-Navigation. In einer Daten-Pflegeanwendung können dadurch veraltete Daten angezeigt werden. Gegenmaßnahme: Bei Wiederherstellung aus dem bfcache wird die Seite neu geladen:

```js
// main.js – systemweit aktiv
window.addEventListener('pageshow', (event) => {
    if (event.persisted) {
        window.location.reload();
    }
});
```

### Datensatz-Navigation (Prev/Next/First/Last)

Die Navigation zwischen Datensätzen (erster, vorheriger, nächster, letzter, Sprung zu ID) darf die Ziel-URL **nicht beim Seitenrendering** als `<a>`-Link vorberechnen. Zwischen Render und Klick können Datensätze gelöscht, neu angelegt oder geändert worden sein – ein vorgerenderter Link verweist damit möglicherweise auf einen nicht mehr existierenden Datensatz.

**Muster: POST-Navigation mit serverseitigem Redirect (siehe ADR-014)**

Der Server rendert für jeden Navigations-Button ein `<form>` statt eines `<a>`-Tags:

```html
<form method="post" action="/freiberufler/navigate">
    <input type="hidden" name="currentId" th:value="${freelancer.id}">
    <input type="hidden" name="direction" value="next">
    <button type="submit" class="tb-nav-btn" title="Nächster">▶</button>
</form>
```

Der Endpunkt `/freiberufler/navigate` berechnet die Ziel-ID **zum Klick-Zeitpunkt** frisch aus der Datenbank und antwortet mit `302` auf die korrekte URL. Typische Queries:

| Richtung     | SQL                                                      |
|--------------|----------------------------------------------------------|
| Nächster     | `SELECT id WHERE id > :current ORDER BY id ASC LIMIT 1`  |
| Vorheriger   | `SELECT id WHERE id < :current ORDER BY id DESC LIMIT 1` |
| Erster       | `SELECT id ORDER BY id ASC LIMIT 1`                      |
| Letzter      | `SELECT id ORDER BY id DESC LIMIT 1`                     |
| Sprung zu ID | Existenzprüfung, dann direkt `/modul/{id}`               |

**Sonderfälle:**
- Kein Nachfolger/Vorgänger vorhanden: `302` zur aktuellen URL, Navigations-Button deaktiviert gerendert
- Aktueller Datensatz zwischenzeitlich gelöscht: `/modul/{id}` liefert `404` mit Hinweis „Dieser Datensatz wurde zwischenzeitlich gelöscht" und Link zurück zur Suche

Dieses Muster gilt für alle Module mit Datensatz-Navigation: Freiberufler, Partner, Kunden, Projekte.

---

## 6. Validierungsstrategie

### Grundprinzip: Defense in Depth

Jede Feldeinschränkung, die in den Modul-Spezifikationen (FREIBERUFLER.md, PARTNER.md, KUNDEN.md, PROJEKTE.md) definiert ist, wird auf **drei aufeinanderfolgenden Ebenen** durchgesetzt. Keine Ebene allein ist ausreichend – jede hat eine klar umrissene Verantwortung:

```
┌─────────────────────────────────────────────────────────┐
│  Modul-Spezifikation (FREIBERUFLER.md etc.)             │
│  → autoritative Quelle für alle Constraints             │
└────────────────────────┬────────────────────────────────┘
                         │ wird umgesetzt in
          ┌──────────────┼──────────────────┐
          ▼              ▼                  ▼
   HTML-Ebene      Backend-Ebene      Datenbank-Ebene
   (Frontend)      (Bean Validation)  (Flyway Migration)
   Sofortfeedback  Autoritativ        Letzte Sicherheitslinie
```

Die Spec ist die einzige Stelle, an der eine Constraint **definiert** wird. Alle drei Ebenen **implementieren** dieselbe Constraint. Weicht eine Ebene ab, ist das ein Bug.

---

### Ebene 1: HTML-Validierung (Frontend)

HTML5-native Validierungsattribute sind die erste Verteidigungslinie. Sie greifen **vor dem Absenden des Formulars**, ohne Server-Round-Trip, und liefern dem Sachbearbeiter sofortiges, visuelles Feedback direkt am betroffenen Feld.

**Pflichtattribute je Constraint-Typ:**

| Constraint in Spec          | HTML-Attribut     | Beispiel                             |
|-----------------------------|-------------------|--------------------------------------|
| Pflichtfeld (`NOT NULL`)    | `required`        | `<input required>`                   |
| Maximallänge (`VARCHAR(n)`) | `maxlength="n"`   | `<input maxlength="100">`            |
| Minimallänge                | `minlength="n"`   | `<input minlength="2">`              |
| Numerischer Bereich         | `min="x" max="y"` | `<input type="number" min="0">`      |
| E-Mail-Format               | `type="email"`    | `<input type="email">`               |
| URL-Format                  | `type="url"`      | `<input type="url">`                 |
| Datum                       | `type="date"`     | `<input type="date">`                |
| Regulärer Ausdruck          | `pattern="..."`   | `<input pattern="[A-Z]{3}[0-9]{3}">` |

HTML5-Validierung wird **nicht** durch JavaScript nachgebaut oder ersetzt. Die Browser-native Implementierung ist ausreichend, barrierefrei (ARIA-Integration) und wartungsfrei.

**Warum nicht JavaScript-Validierung:**
JS-Validierungslogik ist duplizierter Code – dieselbe Regel existiert dann in drei Sprachen (JS, Java, SQL). HTML5-Attribute sind deklarativ, vom Browser verwaltet und können nicht versehentlich aus dem Sync geraten, weil sie direkt im Template stehen.

**Thymeleaf-Beispiel für ein Pflichtfeld mit Längenbeschränkung:**

```html
<div class="fg">
  <label th:for="*{lastName}">Nachname <span class="req">*</span></label>
  <input type="text"
         th:field="*{lastName}"
         required
         maxlength="100">
  <span th:if="${#fields.hasErrors('lastName')}"
        th:errors="*{lastName}"
        class="field-error">
  </span>
</div>
```

---

### Ebene 2: Backend-Validierung (Bean Validation)

Die Backend-Validierung ist **autoritativ und nicht umgehbar**. Sie schützt das System gegen manipulierte HTTP-Requests, die die HTML-Validierung umgehen. Kein Datensatz gelangt in die Datenbank, der nicht die Backend-Validierung passiert hat.

Bean Validation (`jakarta.validation`) wird auf den **Command-Objekten** der Command-Seite annotiert – nicht auf den Aggregaten oder Datenbankentitäten direkt.

```java
public record FreelancerCreateCommand(
    @NotBlank
    @Size(max = 100)
    String lastName,

    @NotBlank
    @Size(max = 100)
    String firstName,

    @Email
    @Size(max = 255)
    String email,

    @Size(max = 10)
    @Pattern(regexp = "[A-Z]{3}[0-9]{3}", message = "Kodierung muss dem Format AAA999 entsprechen")
    String code,

    @NotNull
    @Min(0)
    Integer hourlyRate
) {}
```

Der Controller-Endpunkt nimmt das Command-Objekt mit `@Valid` entgegen. Bei Validierungsfehlern gibt Spring MVC automatisch zurück zum Formular mit befülltem `BindingResult` – Thymeleaf rendert die Fehlermeldungen via `th:errors` direkt am jeweiligen Feld.

```java
@PostMapping("/freelancers")
public String create(@Valid FreelancerCreateCommand command,
                     BindingResult bindingResult,
                     Model model) {
    if (bindingResult.hasErrors()) {
        return "freelancer/form"; // Formular mit Fehlern neu rendern
    }
    commandService.create(command);
    return "redirect:/freelancers";
}
```

**Fehlermeldungen:** Standard-Fehlermeldungen aus dem Bean-Validation-Messageformat werden in `messages.properties` überschrieben, um deutschsprachige, fachlich verständliche Texte zu liefern.

---

### Ebene 3: Datenbankebene (Flyway-Migrationen)

Die Datenbank ist die **letzte Sicherheitslinie**. MySQL-Constraints (`NOT NULL`, `VARCHAR(n)`, `CHECK`) stellen sicher, dass selbst bei einem Fehler in der Applikationsschicht (z.B. ein vergessenes `@Valid`) keine inkonsistenten Daten persistiert werden können.

```sql
-- Beispiel: V1__init_schema.sql (Auszug)
CREATE TABLE freelancer (
    id          BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    db_version  INT             NOT NULL DEFAULT 0,
    last_name   VARCHAR(100)    NOT NULL,
    first_name  VARCHAR(100)    NOT NULL,
    email       VARCHAR(255)    NULL,
    code        VARCHAR(10)     NULL,
    hourly_rate INT             NULL CHECK (hourly_rate >= 0),
    ...
);
```

Die Spaltentypen und Constraints in der Migration **müssen** exakt den Angaben in der Modul-Spezifikation entsprechen. Abweichungen zwischen Spec und Flyway-Migration sind Bugs.

---

### Konsistenz-Regel: Die Spec ist die Wahrheit

Da drei Ebenen dieselbe Constraint implementieren müssen, besteht die Gefahr, dass sie auseinanderdriften – z.B. wenn eine Feldlänge in der Spec geändert wird, aber nur das HTML angepasst wird. Folgende Regel gilt verbindlich:

> **Jede Änderung einer Feldconstraint in einer Modul-Spec erfordert gleichzeitig die Anpassung aller drei Ebenen: HTML-Attribut, Bean-Validation-Annotation und Flyway-Migrationsskript.**

Eine Änderung an nur einer Ebene ist kein vollständiges Ticket. Code-Reviews prüfen explizit auf diese Konsistenz.

---

### Sonderfall: Fehlermeldungen bei Backend-Validierung

Wenn die HTML-Validierung greift, zeigt der Browser eine native Meldung – kein zusätzlicher Aufwand. Wenn trotzdem ein Fehler die Backend-Validierung erreicht (manipulierter Request, Race Condition), zeigt Thymeleaf die Fehlermeldung via `th:errors` inline am Feld. Es gibt **keinen** zentralen Error-Banner für Feldvalidierungsfehler – Fehler werden immer am verursachenden Feld dargestellt, damit der Sachbearbeiter sofort weiß, was zu korrigieren ist.

Ausnahme: Systemfehler (Datenbankfehler, Optimistic Locking Conflicts) werden über den Banner-Mechanismus gemäß UI-DESIGNSYSTEM.md dargestellt, da sie keinem einzelnen Feld zugeordnet werden können.

---

## 7. Authentifizierung und Autorisierung

### Strategie

Powerstaff 2026 setzt für Release 1.0 auf **formularbasiertes Login** (Username + Passwort) über Spring Security. Benutzer werden in einer eigenen Datenbanktabelle verwaltet; das Passwort wird mit BCrypt gehasht gespeichert. Eine SSO-Integration (Microsoft Entra ID / Keycloak) ist für eine spätere Version geplant und in [SSOLOGIN.md](SSOLOGIN.md) spezifiziert.

### Benutzertabelle (`ps_user`)

Alle Benutzer werden in der Tabelle `ps_user` gespeichert. Die Verwaltung erfolgt ausschließlich durch direktes Editieren der Datenbanktabelle (kein UI).

| Spalte                 | Datentyp       | Prüfungen              | Hinweise                                                                                                  |
|------------------------|----------------|------------------------|-----------------------------------------------------------------------------------------------------------|
| `username`             | `VARCHAR(100)` | PK, NOT NULL           | Login-Name; technische Benutzeridentität im gesamten System (entspricht `user_id` in anderen Tabellen)    |
| `password_hash`        | `VARCHAR(255)` | NOT NULL               | BCrypt-Hash des Kennworts (`{bcrypt}...` – Spring Security kompatibles Format)                            |
| `must_change_password` | `BOOLEAN`      | NOT NULL, DEFAULT TRUE | Bei `true`: Redirect auf `/passwort-aendern` nach dem Login; verhindert Zugriff auf andere Seiten         |
| `enabled`              | `BOOLEAN`      | NOT NULL, DEFAULT TRUE | Deaktivierte Benutzer (`false`) können sich nicht einloggen                                               |

**Default-Passwort:** Neue Benutzer erhalten ein systemweit konfiguriertes Default-Passwort (als BCrypt-Hash direkt in die Tabelle eintragen). `must_change_password` wird auf `TRUE` gesetzt. Beim ersten Login wird der Benutzer zwingend auf `/passwort-aendern` umgeleitet; erst nach erfolgreicher Passwortänderung (`must_change_password = FALSE`) erhält er Zugriff auf die Anwendung.

### Spring Security Konfiguration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/passwort-aendern").authenticated()
                .requestMatchers("/login", "/error").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

```java
@Service
@RequiredArgsConstructor
public class PsUserDetailsService implements UserDetailsService {

    private final JdbcClient jdbcClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return jdbcClient.sql("SELECT * FROM ps_user WHERE username = :username AND enabled = TRUE")
            .param("username", username)
            .query((rs, rowNum) -> User.builder()
                .username(rs.getString("username"))
                .password(rs.getString("password_hash"))
                .roles("USER")
                .build())
            .optional()
            .orElseThrow(() -> new UsernameNotFoundException("Benutzer nicht gefunden: " + username));
    }
}
```

### CSRF-Schutz bei AJAX-Requests

Spring Security schützt alle zustandsverändernden Requests mit CSRF-Tokens. Thymeleaf bettet diese automatisch in Formulare ein.

**Spring Security Konfiguration:** `CookieCsrfTokenRepository.withHttpOnlyFalse()` mit `XorCsrfTokenRequestAttributeHandler`. `${_csrf.token}` liefert den XOR-maskierten Token, den Spring Security für Header und Hidden-Fields erwartet.

**Frontend:** Formular-Submits nutzen `fetch()` direkt (CSRF im Body via Hidden-Field). Bodylose POST-, DELETE- und JSON-POST-Requests setzen `X-XSRF-TOKEN: getCsrfToken()` im Header. Token kommt aus `<meta name="csrf-token">` (nie aus dem rohen Cookie). Details: Abschnitt 5 (CSRF-Schutz für AJAX-Requests).

### Passwort-Änderung beim ersten Login

Nach erfolgreichem Login prüft ein `AuthenticationSuccessHandler`, ob `must_change_password = TRUE` gesetzt ist. Falls ja, wird der Benutzer auf `/passwort-aendern` weitergeleitet. Solange `must_change_password = TRUE`, werden alle Requests (außer `/passwort-aendern` und `/logout`) auf `/passwort-aendern` umgeleitet – realisiert über einen `OncePerRequestFilter`.

Der Controller unter `/passwort-aendern` nimmt das neue Passwort entgegen, validiert es (Mindestlänge, Bestätigung), speichert den neuen BCrypt-Hash und setzt `must_change_password = FALSE`.

### Autorisierung

Powerstaff 2026 verwendet ein **flaches Autorisierungsmodell**: Jeder authentifizierte Benutzer hat vollen Zugriff auf alle Funktionen der Applikation. Es gibt keine Rollen, keine feingranulare Zugriffskontrolle und keine funktionalen Einschränkungen je Benutzer.

Diese Entscheidung entspricht dem aktuellen fachlichen Stand – das System wird von einem homogenen Team von Sachbearbeitern betrieben, die alle gleichberechtigt arbeiten. Spring Security schützt alle Endpunkte gegen unauthentifizierte Zugriffe (`anyRequest().authenticated()`); eine darüber hinausgehende Autorisierungslogik entfällt bewusst.

Das gilt ausdrücklich auch für die Admin-UI des `shared`-Moduls (Stammdatenpflege): Alle eingeloggten Benutzer können Stammdaten anlegen und ändern – kein dedizierter Admin-Benutzer oder keine Admin-Rolle ist vorgesehen.

**Bewusste Entscheidung – kein Schutz vor Insecure Direct Object References (IDOR):** Da alle authentifizierten Benutzer gleichberechtigten Vollzugriff haben, sind direkte Ressourcen-URLs (`/freiberufler/123`, `/projekte/42`) für jeden eingeloggten Benutzer zugänglich. Das ist fachlich gewollt und kein Sicherheitsfehler. Eine objektbezogene Zugriffsprüfung (z.B. „nur eigene Datensätze") ist nicht vorgesehen.

### Benutzeridentität im System

Die Benutzeridentität ist der `username` aus der `ps_user`-Tabelle (`UserDetails::getUsername`). Dieser Wert wird in der Datenbank als `user_id` gespeichert und dient als technischer Schlüssel für:

- Das **„Gemerktes Projekt"**-Konzept – Datenbankschema und fachliche Spezifikation: [PROJEKTE.md](PROJEKTE.md)
- Das **technische Audit-Logging** – `createdBy` / `lastModifiedBy` auf allen Aggregaten (befüllt durch Spring Data JDBC Auditing, siehe Abschnitt Logging)

### Fehlerbehandlung

Unerwartete Fehler werden zentral über einen `@ControllerAdvice` abgefangen und einheitlich behandelt.

**Synchrone Requests (Seitennavigation, Formular-Submit):**
- Alle nicht behandelten Exceptions landen im `@ControllerAdvice`
- Dieser rendert eine einheitliche Fehlerseite gemäß Design-System mit einer allgemeinen Fehlermeldung – kein Stack-Trace, keine technischen Details für den Benutzer
- Der vollständige Stack-Trace wird ausschließlich ins Applikations-Log geschrieben
- Spring Boots Standard-`/error`-Endpunkt wird durch eine eigene `ErrorController`-Implementierung ersetzt, die die Design-System-konforme Fehlerseite rendert

**AJAX-Requests (Custom Elements, Infinite Scroll, Chat):**
- Bei einem serverseitigen Fehler antwortet der Endpunkt mit HTTP 5xx und einem strukturierten JSON-Body: `{"message": "Ein unerwarteter Fehler ist aufgetreten."}`
- Das aufrufende Custom Element wertet den HTTP-Status aus und zeigt eine kontextuelle Inline-Fehlermeldung an
- Kein unkontrolliertes Abbrechen ohne Nutzerfeedback

**Fachliche Fehler** (Optimistic Locking, RESTRICT-Verletzungen) werden nicht über den `@ControllerAdvice` behandelt, sondern direkt im jeweiligen Controller mit spezifischen Fehlermeldungen gemäß UI-DESIGNSYSTEM.md.

**`@ControllerAdvice`-Scope – Abgrenzung:**

| Exception-Typ                          | Behandlung                                   |
|----------------------------------------|----------------------------------------------|
| `DeletionBlockedException`             | try/catch direkt im jeweiligen Controller    |
| `OptimisticLockingFailureException`    | try/catch direkt im jeweiligen Controller    |
| Alle übrigen (`RuntimeException` etc.) | `@ControllerAdvice` → generische Fehlerseite |

**Exception-Hierarchie:**
Keine gemeinsame Basisklasse. Zwei unabhängige fachliche Exception-Typen:

```java
// Geworfen von Integrity-Guards bei RESTRICT-Verletzungen
public class DeletionBlockedException extends RuntimeException {
    private final List<BlockingReference> blockingReferences;

    public record BlockingReference(String label, String url) {}
}
```

- `label`: Anzeigename des blockierenden Datensatzes (z.B. `"Projekt: Entwicklung Portal 2026"`)
- `url`: Edit-URL des blockierenden Datensatzes (z.B. `/projekte/42`)
- Der Meldungstext ist ein Literal direkt in der Exception – kein `messages.properties`

**Controller-Muster für RESTRICT-Verletzungen:**

```java
@PostMapping("/{id}/loeschen")
public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
        freelancerCommandService.delete(id);
        return "redirect:/freiberufler";
    } catch (DeletionBlockedException e) {
        redirectAttributes.addFlashAttribute("deletionError", e);
        return "redirect:/freiberufler/" + id;
    }
}
```

Der GET-Handler rendert das Flash-Attribut `deletionError` als Fehlerbanner mit klickbaren Links zu den blockierenden Datensätzen (gemäß UI-DESIGNSYSTEM.md). `OptimisticLockingFailureException` folgt demselben try/catch-Muster.

### Spring Boot Actuator

Der Actuator ist ausschließlich auf `localhost` (`127.0.0.1`) erreichbar. Monitoring-Werkzeuge (z.B. Prometheus-Scraper, Health-Checks des Deployment-Systems) greifen lokal auf dem Server zu – kein externer Zugriff.

**`application.yml`:**

```yaml
management:
  server:
    port: 8090
    address: 127.0.0.1
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus
  endpoint:
    health:
      show-details: always
```

Durch die Bindung an `127.0.0.1` ist der Management-Port strukturell nicht über externe Netzwerkinterfaces erreichbar – unabhängig von Spring Security Regeln. Eine zusätzliche Spring-Security-Absicherung der `/actuator`-Pfade ist damit nicht erforderlich.

### HTTP-Status-Konvention

Da HTTP-Statuscodes kein einheitliches Industriestandard-Mapping haben, legt Powerstaff 2026 eine verbindliche projekteigene Konvention fest. Ziel ist Konsistenz über alle Endpunkte – ohne Diskussion im Einzelfall.

**SSR-Endpunkte (Thymeleaf, Vollseiten-Rendering):**

| Situation                                      | Status | Begründung                                                                 |
|------------------------------------------------|--------|----------------------------------------------------------------------------|
| Seite normal gerendert                         | `200`  | Standard                                                                   |
| Formular mit Validierungsfehlern neu gerendert | `200`  | Spring MVC Default; `400` würde Browser-History-Verhalten stören           |
| Erfolgreicher POST (Speichern, Löschen)        | `302`  | PRG-Pattern (Post/Redirect/Get) – verhindert doppeltes Absenden bei Reload |
| Entität nicht gefunden                         | `404`  | Semantisch korrekt, eigene Fehlerseite                                     |
| Unerwarteter Serverfehler                      | `500`  | Eigene Fehlerseite via `@ControllerAdvice`                                 |

**AJAX-Endpunkte (HTML-Fragmente, JSON):**

| Situation                                | Status         | Begründung                                                       |
|------------------------------------------|----------------|------------------------------------------------------------------|
| Erfolg                                   | `200`          | Standard                                                         |
| Validierungsfehler                       | `400`          | Semantisch korrekt; kein Browser-History-Problem bei AJAX        |
| Entität nicht gefunden                   | `404`          | –                                                                |
| Optimistic Locking Konflikt              | `409 Conflict` | Zustandskonflikt – der Datensatz wurde zwischenzeitlich geändert |
| RESTRICT-Verletzung (Löschen verhindert) | `409 Conflict` | Zustandskonflikt – der Datensatz hat noch aktive Abhängigkeiten  |
| Unerwarteter Serverfehler                | `500`          | –                                                                |

**Bewusst nicht verwendet:**
- `422 Unprocessable Entity` – `400` ist für Validierungsfehler ausreichend und weniger erklärungsbedürftig
- `401` / `403` – werden ausschließlich von Spring Security automatisch gesetzt, kein manueller Einsatz in Controllern

### Logging-Strategie

**Format:** Plain Text in allen Umgebungen. Kein strukturiertes JSON-Logging.

**Log-Level je Umgebung:**

| Logger                            | Lokal / Dev | Produktion |
|-----------------------------------|-------------|------------|
| `de.powerstaff`                   | `DEBUG`     | `INFO`     |
| Spring Framework, Hibernate, etc. | `INFO`      | `WARN`     |
| Root Logger                       | `INFO`      | `WARN`     |

Konfiguration erfolgt in `application-local.yml` und `application-prod.yml` unter `logging.level`.

**Technisches Audit-Logging via Spring Data JDBC Auditing:**

Die Aggregate-Roots tragen Audit-Felder, die im Datenmodell bereits vorgesehen sind (`created_at`, `created_by`, `last_modified_at`, `last_modified_by`). Diese werden durch Spring Data JDBC Auditing automatisch befüllt – kein manueller Aufwand pro Service-Methode.

```java
// Konfiguration
@Configuration
@EnableJdbcAuditing
public class AuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
            .filter(Authentication::isAuthenticated)
            .map(auth -> (UserDetails) auth.getPrincipal())
            .map(UserDetails::getUsername);
    }
}
```

Alle Aggregate-Roots annotieren ihre Audit-Felder mit `@CreatedBy`, `@LastModifiedBy`, `@CreatedDate`, `@LastModifiedDate`. Damit ist lückenlos nachvollziehbar, welcher Benutzer (identifiziert über `ps_user.username`) einen Datensatz angelegt oder zuletzt verändert hat – ergänzend zur fachlichen Kontakthistorie, die Sachbearbeiter manuell pflegen.

**Security-Event-Logging:**

Sicherheitsrelevante Ereignisse werden ins Applikations-Log geschrieben. Dies dient primär der DSGVO-Nachweispflicht (das System verwaltet personenbezogene Daten von Freiberuflern) und der Betriebsnachvollziehbarkeit.

Zu protokollierende Ereignisse:

| Ereignis                   | Log-Level | Inhalt                              |
|----------------------------|-----------|-------------------------------------|
| Fehlgeschlagener Login      | `WARN`    | Username, Zeitstempel               |
| Erfolgreicher Login         | `INFO`    | Username, Zeitstempel               |
| Passwortänderung            | `INFO`    | Username, Zeitstempel               |
| Benutzer deaktiviert        | `INFO`    | Betroffener Username, Zeitstempel   |

Die Implementierung erfolgt über Spring Security `ApplicationEventPublisher`-Events (`AuthenticationFailureBadCredentialsEvent`, `AuthenticationSuccessEvent`) in einer dedizierten `SecurityEventListener`-Komponente. Kein Logging direkt in Controllern oder Services.

---

## 8. KI-Integration (Profilsuche)

### Architekturprinzip

Powerstaff stellt für die KI-gestützte Profilsuche ausschließlich **UI und Persistenz** bereit. Die Orchestrierungslogik (LLM-Aufruf, Tool-Auswahl, Kontext-Management) liegt beim externen LLM-Server (Ollama / LM Studio).

Der **Service-Layer** von Powerstaff ist die primäre API-Grenze. Darüber liegen zwei Adapter:

```
Service-Layer (interne API)
    ├── Spring MVC Controller → Thymeleaf SSR (Frontend)
    └── [zukünftig optional] MCP-Server → LLM-Server (Ollama / LM Studio)
```

### Dynamischer LLM-Kontext

Bei jeder Nutzeranfrage liest der `ProfileSearchQueryService` den Kontext **live aus der Datenbank** (kein Caching). Grundlage ist das **aktuell gemerkte Projekt des Sachbearbeiters** (`remembered_project`), nicht die in `profile_search_chat.project_id` gespeicherte Anlage-Referenz. Ist kein Projekt gemerkt, wird kein Projektkontext übergeben.

Der Kontext umfasst: Projektdaten (Nummer, Beschreibung, Einsatzort, Skills, Laufzeit, Status, Stundensatz) sowie alle dem Projekt zugeordneten Freiberufler mit Skills, Tags und Positionsstatus. Details siehe PROFILSUCHE.md §Dynamischer LLM-Kontext.

**Chat-URL:** Jede Chat-Sitzung ist unter `/profilsuche/{chatId}` erreichbar, wobei `chatId` der Primärschlüssel (`id`) des `profile_search_chat`-Datensatzes ist. Bei Aufruf einer nicht (mehr) existierenden `chatId` (z.B. nach manueller Löschung) erfolgt ein Redirect zu `/profilsuche`, das die zuletzt aktive Sitzung lädt oder eine neue anlegt.

**Projektbindung:** `profile_search_chat.project_id` speichert das Projekt zum Anlagezeitpunkt ausschließlich zur Sidebar-Anzeige (`ON DELETE SET NULL` – kein Cascade-Delete). Der LLM-Kontext basiert stets auf dem aktuell gemerkten Projekt, nicht auf diesem gespeicherten Bezug.

### MCP-Integration (vorläufig offen)

Ob und wie ein MCP-Server exponiert wird, ist noch nicht abschließend entschieden. Bei zukünftiger Implementierung gilt ADR-005.

---

## 9. Testarchitektur

### Test-Framework: Spock

Alle Tests werden mit dem **Spock Framework (Groovy)** geschrieben. Die `given/when/then`-Struktur ist syntaktisch erzwungen und macht Tests zu lesbaren Spezifikationen. Data-driven Tests nutzen Spocks `where:`-Tabellen.

```groovy
// Beispiel: Integrationstest einer QBE-Suche gegen Testcontainers-MySQL
@DataJdbcTest
@AutoConfigureTestDatabase(replace = NONE)
class FreelancerQueryServiceIT extends Specification {

    @Autowired JdbcClient jdbcClient
    FreelancerQueryService queryService

    def setup() {
        queryService = new FreelancerQueryService(jdbcClient)
    }

    def "QBE-Suche findet Freiberufler anhand kombinierter Filterfelder"() {
        given:
        def params = new FreelancerSearchParams(name: name, skill: skill)

        when:
        def result = queryService.search(params)

        then:
        result.size() == expectedCount

        where:
        name      | skill  | expectedCount
        "Müller"  | null   | 3
        null      | "Java" | 7
        "Müller"  | "Java" | 1
    }
}
```

### Maven-Phasen

| Phase              | Plugin                | Testklassen-Suffix | Zweck                                                                            |
|--------------------|-----------------------|--------------------|----------------------------------------------------------------------------------|
| `test`             | maven-surefire-plugin | `*Spec`            | Unit-Tests, Controller-Tests (`@WebMvcTest`)                                     |
| `integration-test` | maven-failsafe-plugin | `*IT`              | Integrationstests (`@SpringBootTest`, `@DataJdbcTest`, `@ApplicationModuleTest`) |

### Teststufen

| Teststufe                | Annotation                        | Phase              | Zweck                                                             |
|--------------------------|-----------------------------------|--------------------|-------------------------------------------------------------------|
| Unit-Test                | –                                 | `test`             | Isolierte Logik, Berechnungen, Validierungsregeln ohne DB-Zugriff |
| Controller + Template    | `@WebMvcTest` mit Thymeleaf aktiv | `test`             | HTML-Rendering, Routing, Model-Attribute                          |
| Modul-Integration        | `@ApplicationModuleTest`          | `integration-test` | Modulgrenzen, Event-Kommunikation                                 |
| Service + Repository     | `@DataJdbcTest`                   | `integration-test` | Repositories, QueryServices (JdbcClient), QBE-Suchen              |
| Vollständige Integration | `@SpringBootTest`                 | `integration-test` | End-to-End kritischer Workflows                                   |

**Pflicht:** Jede Klasse, die auf die Datenbank zugreift – Repositories (Spring Data JDBC) ebenso wie QueryServices (`JdbcClient`) –, muss durch einen `@DataJdbcTest`-Integrationstest gegen Testcontainers abgedeckt sein. Unit-Tests mit gemockter Datenbankschicht sind für datenbanknahe Klassen **nicht zulässig**.

### Mutation-Testing mit PITest

PITest bewertet die Qualität der Unit-Tests (`test`-Phase) durch gezielte Code-Mutationen. Reports werden in HTML und XML generiert. Integrationstests sind von der Mutation-Analyse ausgeschlossen.

PITest wird über `./mvnw test pitest:mutationCoverage` ausgeführt. Im CI läuft PITest als separater Schritt nach der `test`-Phase.

### Testcontainers

Alle Klassen mit Datenbankzugriff – sowohl Spring Data JDBC Repositories als auch `JdbcClient`-basierte QueryServices – werden ausschließlich gegen **Testcontainers mit einer echten MySQL-Instanz** getestet. Es gibt **keine gemockten Datenbankzugriffe** und keine H2- oder In-Memory-Datenbanken. Die Testdatenbank wird automatisch mit Flyway auf den aktuellen Migrationsstand gebracht, bevor Tests ausgeführt werden.

Diese Regel gilt umfassend: QBE-Suchen, einfache `findById`-Aufrufe, komplexe Cross-Modul-Projektionen – jeder SQL-Aufruf muss mindestens einmal gegen die echte Datenbank laufen. Nur so sind Schema-Drifts, Typ-Inkompatibilitäten und MySQL-spezifische SQL-Eigenheiten zuverlässig erkennbar.

---

## 10. Lokale Entwicklungsumgebung

Die lokale Entwicklungsumgebung wird über **Docker Compose** bereitgestellt. Eine einzelne Datei `compose.yml` im Projekt-Root startet alle externen Abhängigkeiten.

```yaml
# compose.yml
services:

  mysql:
    image: mysql:8
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: powerstaff
      MYSQL_USER: powerstaff
      MYSQL_PASSWORD: powerstaff
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql

  keycloak:
    image: quay.io/keycloak/keycloak:latest
    command: start-dev --import-realm
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
    ports:
      - "9090:8080"
    volumes:
      - ./dev/keycloak-realm.json:/opt/keycloak/data/import/realm.json

volumes:
  mysql-data:
```

Die Datei `dev/keycloak-realm.json` enthält einen vorkonfigurierten Realm `powerstaff` mit einem Test-Client und Test-Benutzern. Sie wird im Repository eingecheckt.

Spring Boot erkennt `compose.yml` automatisch und startet die Abhängigkeiten beim Anwendungsstart (`spring.docker.compose.enabled=true` ist Standard).

---

## 11. Deployment

### Layered JAR

Die Produktionsauslieferung erfolgt als **Spring Boot Layered JAR**. Das Layered-Format trennt Abhängigkeiten von Anwendungscode – dies beschleunigt Docker-Builds und ermöglicht effizienteres Caching, falls zu einem späteren Zeitpunkt Container eingesetzt werden.

Maven-Konfiguration in `pom.xml`:

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <layers>
            <enabled>true</enabled>
        </layers>
    </configuration>
</plugin>
```

### Build

```bash
./mvnw clean package -DskipTests
java -jar target/powerstaff-2026.jar --spring.profiles.active=prod
```

### Datenbankverbindungspool (HikariCP)

Spring Boot verwendet HikariCP als Standard-Connection-Pool. Für die Produktionsumgebung werden die Defaults in `application-prod.yml` explizit überschrieben:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10       # ausreichend für überschaubare Benutzermenge
      minimum-idle: 5             # spart Ressourcen bei geringer Last
      connection-timeout: 5000    # 5 s – schnelles Feedback bei DB-Ausfall statt 30 s hängen
      idle-timeout: 300000        # 5 min – idle Verbindungen früher freigeben
      max-lifetime: 1800000       # 30 min – unterhalb von MySQL wait_timeout (default 8 h)
```

Der `max-lifetime`-Wert muss kleiner als der MySQL-Parameter `wait_timeout` sein, um "connection closed"-Fehler zu vermeiden. Der MySQL-Default (28.800 s / 8 h) lässt hierfür ausreichend Spielraum.

### Umgebungsvariablen (Produktion)

| Variable                     | Beschreibung                 |
|------------------------------|------------------------------|
| `SPRING_DATASOURCE_URL`      | JDBC-URL der MySQL-Datenbank |
| `SPRING_DATASOURCE_USERNAME` | Datenbankbenutzer            |
| `SPRING_DATASOURCE_PASSWORD` | Datenbankpasswort            |

---

## 12. Architekturentscheidungen (ADR)

---

### ADR-001: Spring Data JDBC statt Spring Data JPA

**Status:** Akzeptiert

**Kontext:**
Die Anwendung nutzt Spring Boot Modulith, das auf klare Aggregate-Grenzen angewiesen ist. Es war zu entscheiden, ob die Persistenz über Spring Data JPA (Hibernate) oder Spring Data JDBC erfolgt.

**Entscheidung:**
Spring Data JDBC wird als Persistenzframework eingesetzt.

**Begründung:**
Spring Data JDBC erzwingt Aggregate-Grenzen auf Persistenzebene: Es gibt kein Lazy Loading und keinen Session-Kontext, der versehentlich über Modulgrenzen hinweg navigiert. Jeder Datenzugriff ist explizit – das passt strukturell zu Spring Modulith. Optimistic Locking via `@Version` wird nativ unterstützt. Für komplexe Leseanforderungen (Profilsuche, QBE-Suche) wird `JdbcClient` mit handgeschriebenem SQL eingesetzt, was zusätzliche ORM-Magie überflüssig macht.

**Konsequenzen:**
- Keine JPQL, kein Criteria API – komplexe Queries werden in SQL geschrieben
- Kein Lazy Loading – alle Daten müssen explizit abgerufen werden
- Aggregate-Grenzen müssen sorgfältig modelliert werden

---

### ADR-002: Logisches CQRS als Architekturprinzip

**Status:** Akzeptiert

**Kontext:**
Das System hat zwei strukturell verschiedene Zugriffsmuster: schreibende Operationen auf vollständigen Aggregaten mit Geschäftsregeln und lesende Operationen für Listenansichten, Suchergebnisse und KI-Kontext, die schlanke modul-übergreifende Projektionen benötigen. Das direkte Mischen beider Muster in einem Modell führt zu unnötiger Komplexität.

**Entscheidung:**
Logisches CQRS wird konsequent angewendet. Es gibt separate Command- und Query-Seiten im Code, aber keinen separaten Read-Store und kein Event Sourcing.

**Begründung:**
- Die Command-Seite arbeitet mit Spring Data JDBC Aggregaten und Repositories – sicher, validiert, mit Optimistic Locking.
- Die Query-Seite arbeitet mit `JdbcClient`, handgeschriebenem SQL und Java Records als Projektionen – flexibel, performant, modul-übergreifend lesbar.
- Die Trennung macht die Absicht im Code explizit: ein `QueryService` darf nie schreiben, ein `CommandService` darf nie direkte SQL-Abfragen für Listenansichten enthalten.
- Modul-übergreifende Lesezugriffe (z.B. `profilesearch` liest Freiberufler- und Projektdaten) sind auf der Query-Seite erlaubt und müssen nicht über Modul-APIs geroutet werden.

**Konsequenzen:**
- Jedes Modul hat mindestens einen `CommandService` und einen `QueryService`
- Repositories sind `package-private` – nur der `CommandService` des eigenen Moduls hat Zugriff
- `JdbcClient` ist kein Ersatz für Aggregate bei schreibenden Operationen – dieser Pfad ist ausschließlich der Query-Seite vorbehalten

---

### ADR-003: Thymeleaf als Template-Engine mit verpflichtenden WebMvcTests

**Status:** Akzeptiert

**Kontext:**
Das Frontend wird per Server-Side Rendering erzeugt. Es war eine Template-Engine zu wählen, und es war festzulegen, wie Template-Fehler frühzeitig erkannt werden.

**Entscheidung:**
Thymeleaf wird als Template-Engine eingesetzt. Für jeden Controller-Endpunkt existiert ein `@WebMvcTest`-Test, der das Template tatsächlich rendert und den HTML-Output validiert.

**Begründung:**
Thymeleaf ist der de-facto Standard im Spring-Ökosystem mit exzellenter Spring Security Integration (automatische CSRF-Token-Einbettung) und dem Natural-Template-Ansatz. Das Testen des gerenderten HTML – nicht nur des View-Namens – stellt sicher, dass Fehler wie fehlende Modellattribute oder kaputte Template-Expressions im CI sofort sichtbar werden und nicht erst zur Laufzeit.

**Konsequenzen:**
- `@WebMvcTest` aktiviert Thymeleaf-Rendering vollständig (kein Mocking der View-Schicht)
- Jeder neue Endpunkt erfordert einen zugehörigen Test, der den HTML-Output prüft
- Template-Fehler brechen den Build

---

### ADR-004: Formularbasiertes Login für Release 1.0; SSO für spätere Version

**Status:** Akzeptiert

**Kontext:**
Das System muss Benutzer identifizieren (Gemerktes Projekt ist pro Sachbearbeiter persistent). Perspektivisch soll SSO im Microsoft-Umfeld (Entra ID) unterstützt werden. Es war zu entscheiden, ob die SSO-Integration bereits in Release 1.0 umgesetzt wird oder zunächst mit einem einfachen formularbasierten Login gestartet wird.

**Entscheidung:**
Release 1.0 setzt auf Spring Security Form Login mit einer eigenen Benutzertabelle (`ps_user`) und BCrypt-Passwörtern. Die SSO-Integration (Microsoft Entra ID / Keycloak) ist für eine spätere Version vorgesehen und in [SSOLOGIN.md](SSOLOGIN.md) spezifiziert.

**Begründung:**
Die SSO-Integration erfordert Abstimmung mit der Microsoft-Umgebung (App-Registrierung, Tenant-Konfiguration) und externe Abhängigkeiten in der lokalen Entwicklung (Keycloak Docker). Beides ist für Release 1.0 unverhältnismäßig aufwändig. Das formularbasierte Login ist sofort lauffähig, benötigt keine externe Infrastruktur und lässt sich später durch SSO ersetzen, da die Benutzeridentität (`username`) als einfacher String im System gespeichert ist und der Austausch der Authentifizierungsschicht auf Spring Security beschränkt bleibt.

**Konsequenzen:**
- `ps_user`-Tabelle enthält `username`, `password_hash` (BCrypt), `must_change_password`, `enabled`
- `username` ist die systemweite Benutzeridentität (in `remembered_project`, Audit-Felder)
- Benutzerverwaltung ausschließlich per direktem DB-Edit (kein UI)
- Default-Passwort mit Forced-Change-Flow beim ersten Login
- Spätere SSO-Migration: Spring Security Konfiguration austauschen; `username` bleibt als Identifier erhalten (Mapping auf SSO-Claim erforderlich)

---

### ADR-005: Service-Layer als primäre API-Grenze; MCP-Exposition vorläufig offen

**Status:** Vorläufig offen

**Kontext:**
Die Profilsuche bindet einen externen LLM-Server (Ollama / LM Studio) an. Es war zu entscheiden, ob Powerstaff eine klassische REST-API oder einen MCP-Server als Schnittstelle für den LLM-Server bereitstellt.

**Entscheidung:**
Der **Service-Layer** ist die primäre interne API-Grenze. Darüber liegen ausschließlich dünne Adapter: Spring MVC Controller für das SSR-Frontend und – bei Bedarf – ein MCP-Server für den LLM-Server. Powerstaff konsumiert selbst keine MCP-Tools.

Die Entscheidung, ob und wann ein MCP-Server exponiert wird, ist noch offen.

**Begründung:**
Die Entkopplung des Service-Layers von der Transportschicht (HTTP/MVC vs. MCP) hält beide Optionen offen, ohne Vorab-Investition in eine möglicherweise nicht benötigte Infrastruktur. Der LLM-Server-seitige MCP-Ansatz (Ollama/LM Studio integriert die MCP-Tools) ist die bevorzugte Richtung, da Powerstaff damit keine KI-Orchestrierungslogik übernimmt.

**Einschränkung bei Implementierung:**
Falls ein MCP-Server in Powerstaff implementiert wird, ist das **MCP Reference SDK** (Java SDK) zu verwenden. Spring AI MCP-Wrapper sind explizit ausgeschlossen, da sie dem MCP Reference SDK erfahrungsgemäß im Funktionsumfang hinterherhängen.

**Konsequenzen:**
- Kein REST-API-Layer als separates Artefakt – der Service-Layer ist direkt erreichbar
- Bei MCP-Implementierung: Abhängigkeit auf das offizielle MCP Java SDK, keine Spring AI MCP-Starter

---

### ADR-006: Maven mit Maven Wrapper als Build-Tool

**Status:** Akzeptiert

**Kontext:**
Es war ein Build-Tool für das Projekt festzulegen.

**Entscheidung:**
Maven wird als Build-Tool eingesetzt. Der Maven Wrapper (`mvnw`) wird ins Repository eingecheckt. Das `maven-enforcer-plugin` ist verpflichtend aktiviert und bricht den Build bei Verletzung einer der festgelegten Regeln ab.

**Begründung:**
Maven ist das primäre Build-Tool im Spring-Ökosystem mit bestmöglicher Unterstützung durch Spring Initializr und Spring Boot Parent POM. Der Maven Wrapper stellt sicher, dass alle Entwickler und CI-Systeme dieselbe Maven-Version verwenden, ohne eine lokale Maven-Installation vorauszusetzen.

Das `maven-enforcer-plugin` macht implizite Build-Voraussetzungen explizit und maschinell prüfbar. Ohne Enforcer können inkompatible Java-Versionen, Dependency-Konflikte oder versehentlich eingeschleuste verbotene Abhängigkeiten unbemerkt in den Build gelangen und erst zur Laufzeit oder im CI-System sichtbar werden. Der Enforcer schlägt früh, laut und mit klarer Fehlermeldung an.

**Aktivierte Enforcer-Regeln:**

| Regel                               | Konfiguration             | Grund                                                                                                      |
|-------------------------------------|---------------------------|------------------------------------------------------------------------------------------------------------|
| `requireJavaVersion`                | `[25,)`                   | Sicherstellt, dass keine ältere JVM den Build ausführt                                                     |
| `requireMavenVersion`               | `[3.9,)`                  | Maven 3.9+ für vollständige Wrapper- und BOM-Unterstützung                                                 |
| `dependencyConvergence`             | –                         | Alle transitiven Abhängigkeiten müssen auf dieselbe Version konvergieren – keine stillen Versionskonflikte |
| `banDuplicatePomDependencyVersions` | –                         | Keine doppelten Dependency-Deklarationen im POM                                                            |
| `bannedDependencies`                | H2 außerhalb `test`-Scope | H2 ist ausschließlich in Tests erlaubt (ADR-009) – verhindert versehentliches H2 in Production             |
| `bannedDependencies`                | `spring-ai-mcp-*`         | Spring AI MCP Wrapper sind explizit verboten (ADR-005)                                                     |
| `requireEncoding`                   | `UTF-8`                   | Einheitliche Datei-Kodierung im gesamten Projekt                                                           |

**Konsequenzen:**
- `./mvnw` für alle Build-Operationen (kein lokales `mvn` erforderlich)
- Spring Boot Parent POM (`spring-boot-starter-parent`) verwaltet Dependency-Versionen
- Ein Build mit einer nicht konvergierenden Dependency oder einer verbotenen Abhängigkeit schlägt sofort in der `validate`-Phase fehl – bevor ein einziges Byte kompiliert wird
- Neue `bannedDependencies`-Einträge können jederzeit ergänzt werden, wenn weitere Abhängigkeiten projektspezifisch ausgeschlossen werden sollen

---

### ADR-007: Spring Boot Layered JAR als Deployment-Artefakt

**Status:** Akzeptiert

**Kontext:**
Es war eine Deployment-Strategie festzulegen. Anforderung ist ein unkomplizierter Betrieb ohne Container-Orchestrierung.

**Entscheidung:**
Die Applikation wird als **Spring Boot Layered JAR** gebaut und direkt via `java -jar` auf dem Zielsystem ausgeführt.

**Begründung:**
Das Layered-Format bietet gegenüber dem klassischen Fat-JAR Vorteile beim Layer-Caching (relevant falls später Container eingesetzt werden), ohne operativen Mehraufwand zu erzeugen. Konfiguration erfolgt vollständig über Umgebungsvariablen gemäß 12-Factor-App-Prinzip.

**Konsequenzen:**
- Kein Application-Server, kein WAR-Deployment
- MySQL muss extern bereitgestellt werden
- Konfiguration ausschließlich über Umgebungsvariablen (keine Produktions-`application.yml` im Repository)

---

### ADR-008: Explizites Schema-Management mit Flyway; keine automatische Schema-Generierung

**Status:** Akzeptiert

**Kontext:**
Spring Data JDBC (und JPA) bieten die Möglichkeit, das Datenbankschema automatisch aus Entitäten zu generieren. Es war zu entscheiden, ob diese Funktion genutzt wird oder das Schema explizit verwaltet wird.

**Entscheidung:**
Das Datenbankschema wird ausschließlich durch Flyway-Migrationsskripte verwaltet. Automatische Schema-Generierung ist deaktiviert und darf nicht verwendet werden.

**Begründung:**
Automatisch generierte Schemas sind nicht reproduzierbar, schwer nachvollziehbar und lassen sich nicht kontrolliert auf Produktionsdatenbanken anwenden. Flyway stellt sicher, dass Entwicklungs-, Test- und Produktionsdatenbank immer denselben, nachvollziehbaren Stand haben. Migrationsskripte sind Teil des Repositories und unterliegen dem gleichen Review-Prozess wie Anwendungscode.

**Konsequenzen:**
- `spring.sql.init.mode=never` und jegliche DDL-Auto-Konfiguration sind deaktiviert
- Jede Schemaänderung erfordert ein neues Flyway-Migrationsskript
- Testcontainers-basierte Tests führen Flyway-Migrationen automatisch vor Testausführung durch – die Testdatenbank ist damit immer auf dem aktuellen Produktionsstand

---

### ADR-009: Testcontainers für alle Datenbankintegrationstests

**Status:** Akzeptiert

**Kontext:**
Integrationstests, die Datenbankzugriffe testen, benötigen eine Datenbankinstanz. Zur Wahl standen In-Memory-Datenbanken (H2), eine extern installierte MySQL-Instanz oder Testcontainers mit echter MySQL.

**Entscheidung:**
Testcontainers wird für alle Tests eingesetzt, die Datenbankzugriffe beinhalten – sowohl `@DataJdbcTest` (Repositories und QueryServices) als auch `@SpringBootTest`. Das gilt ausnahmslos: jede Klasse, die ein Spring Data JDBC Repository oder einen `JdbcClient` verwendet, muss durch einen Testcontainers-basierten Integrationstest abgedeckt sein. H2 und andere In-Memory-Datenbanken sind ausgeschlossen.

**Begründung:**
H2 und MySQL verhalten sich in Randfällen unterschiedlich (SQL-Dialekt, Constraints, Zeichensatz). Tests gegen H2 können bestehen, während dieselbe Query auf MySQL scheitert. Testcontainers startet eine echte MySQL-Instanz als Container – die Tests laufen gegen dieselbe Datenbankversion wie die Produktion. Flyway-Migrationen werden automatisch ausgeführt, sodass das Testschema immer dem Produktionsschema entspricht. Insbesondere bei dynamischen QBE-Queries und `JdbcClient`-Projektionen ist das der einzig zuverlässige Schutz vor Schema-Drifts und Typ-Inkompatibilitäten.

**Konsequenzen:**
- Docker muss in der Entwicklungsumgebung und in der CI-Pipeline verfügbar sein
- Tests sind langsamer als mit H2, dafür zuverlässig und frei von DB-Dialekt-Abweichungen
- Kein separates Test-DDL oder H2-Kompatibilitäts-Workaround nötig
- Unit-Tests für datenbanknahe Klassen mit gemockter DB sind nicht zulässig

---

### ADR-010: Spock/Groovy als Test-Framework, PITest für Mutation-Testing

**Status:** Akzeptiert

**Kontext:**
Es war ein Test-Framework zu wählen. Zur Wahl standen JUnit 5 (Java) und Spock (Groovy). Eine Anforderung ist Mutation-Testing mit PITest. Die Kombination PITest + Spock ist über ein Community-Plugin (`pitest-spock-plugin`) möglich und wurde im Projektzusammenhang bereits erfolgreich eingesetzt.

**Entscheidung:**
Spock Framework (Groovy) wird als einziges Test-Framework verwendet. PITest mit `pitest-spock-plugin` übernimmt das Mutation-Testing der Unit-Tests. JUnit 5 wird nicht parallel eingesetzt.

**Begründung:**
Spocks `given/when/then`-Struktur ist syntaktisch erzwungen – Tests werden zu lesbaren Spezifikationen, nicht nur zu kommentierten Code-Blöcken. Besonders für data-driven Tests (QBE-Suche, Validierungslogik) sind Spocks `where:`-Tabellen JUnit 5 `@ParameterizedTest` deutlich überlegen in Lesbarkeit und Wartbarkeit. Die Kombination PITest + Spock ist im Projektzusammenhang erprobt.

**Maven-Konfiguration (Übersicht):**
- `maven-surefire-plugin`: führt `*Spec`-Klassen in der `test`-Phase aus
- `maven-failsafe-plugin`: führt `*IT`-Klassen in der `integration-test`-Phase aus
- `pitest-maven` + `pitest-spock-plugin`: Mutation-Coverage der Unit-Tests, Reports in HTML + XML
- Groovy-Quellen liegen in `src/test/groovy`; Produktionscode bleibt reines Java

**PITest CI-Schwellwerte:**

```xml
<plugin>
  <groupId>org.pitest</groupId>
  <artifactId>pitest-maven</artifactId>
  <configuration>
    <mutationThreshold>70</mutationThreshold>
    <coverageThreshold>80</coverageThreshold>
    <targetClasses>
      <param>de.powerstaff.*</param>
    </targetClasses>
    <excludedTestClasses>
      <param>*IT</param>
    </excludedTestClasses>
  </configuration>
</plugin>
```

Schwellwerte gelten **global** (nicht pro Modul). Vor dem ersten Aktivieren im CI: PITest einmalig ohne Schwellwert ausführen (`./mvnw test pitest:mutationCoverage`), tatsächlichen Score ablesen und den Schwellwert initial knapp darunter ansetzen. Danach schrittweise erhöhen.

**Konsequenzen:**
- Das Projekt ist mixed-language: Java (Produktion) + Groovy (Tests)
- Groovy-Kompilierung muss im Maven-Build konfiguriert sein (`gmavenplus-plugin`)
- Alle Testklassen enden auf `Spec` (Unit) oder `IT` (Integration) – keine JUnit-`Test`-Klassen
- PITest analysiert ausschließlich Unit-Tests (`*Spec`); Integrationstests sind ausgeschlossen
- Der CI-Build bricht ab, wenn `mutationThreshold` oder `coverageThreshold` unterschritten wird

---

### ADR-011: Custom Elements (Light DOM) für interaktive UI-Komponenten

**Status:** Akzeptiert

**Kontext:**
Das Frontend verwendet Vanilla JS ohne Framework. Interaktive Komponenten (Modals, Infinite Scrolling, dynamische Badges, Chat-Elemente) müssen strukturiert und wartbar implementiert werden. Web Components (Custom Elements, Shadow DOM, HTML Templates) standen zur Diskussion.

**Entscheidung:**
Interaktive, wiederverwendbare UI-Elemente werden als **Custom Elements ohne Shadow DOM** (Light DOM) implementiert. Shadow DOM und HTML Templates werden nicht eingesetzt.

**Begründung:**

*Warum Custom Elements (Light DOM):*
Ohne Custom Elements konzentriert sich das gesamte Vanilla JS typischerweise auf CSS-Klassen als Selektoren. CSS-Klassen sind kein stabiler Vertrag – eine Umbenennung aus gestalterischen Gründen bricht das JavaScript stillschweigend. Custom Element Tag-Namen sind semantisch eindeutig, vom Browser nativ verwaltet und ändern sich nicht mit dem Design. Der Lebenszyklus (`connectedCallback`, `disconnectedCallback`, `attributeChangedCallback`) ersetzt manuelle DOM-Beobachtung und Event-Listener-Verwaltung. Das Ergebnis ist strukturierter, klar abgegrenzter JS-Code statt einer globalen Sammlung von Funktionen.

Progressive Enhancement ist der zentrale Vorteil für SSR: Thymeleaf rendert das vollständige HTML inklusive Custom-Element-Tags. Der Browser zeigt die Inhalte sofort – ohne JS-Abhängigkeit für die Darstellung. Erst nach dem JS-Load "upgraded" `customElements.define()` die Elemente und fügt interaktives Verhalten hinzu. Das macht die Anwendung auch bei langsamen Verbindungen oder temporär deaktiviertem JS nutzbar.

*Warum kein Shadow DOM:*
Das Design-System lebt von einer einzigen globalen `base.css`. Shadow DOM würde eine harte CSS-Kapselungsgrenze ziehen: Klassen, Custom Properties und globale Selektoren aus `base.css` würden das Shadow Root nicht mehr durchdringen (außer CSS Custom Properties). Der Aufwand für Shadow-Part-Styling und CSS-Property-Forwarding überwiegt den Kapselungsgewinn bei weitem, solange ein einheitliches zentrales Design-System gepflegt wird.

*Warum keine HTML Templates (`<template>`):*
Thymeleaf übernimmt serverseitiges Templating vollständig und besser. Client-seitige `<template>`-Elemente wären eine redundante, parallel gepflegte Templating-Schicht.

**Konsequenzen:**
- Custom Element Tag-Namen beginnen mit dem Präfix `ps-` (Präfix-Konvention gemäß HTML-Spezifikation für Custom Elements)
- Tag-Namen sind der stabile Vertrag zwischen Thymeleaf-Templates und JavaScript – CSS-Klassen dürfen nicht als JS-Selektoren verwendet werden
- Jedes Custom Element ist eine eigene Datei unter `src/main/frontend/js/components/`
- Custom Elements müssen ohne JS sinnvoll darstellbar sein (Progressive Enhancement)

---

### ADR-012: Vite als Frontend-Build-Tool, integriert via frontend-maven-plugin

**Status:** Akzeptiert

**Kontext:**
Die Custom Elements und das zentrale `base.css` müssen gebündelt, versioniert und in das Spring Boot JAR integriert werden. Es war ein Frontend-Build-Tool zu wählen und dessen Integration in den Maven-Build festzulegen.

**Entscheidung:**
Vite wird als Frontend-Build-Tool eingesetzt. Die Integration in den Maven-Build erfolgt über `maven-exec-plugin` (nicht `frontend-maven-plugin`). Asset-Pfade in Thymeleaf-Templates werden über eine `ViteManifest`-Komponente aufgelöst, die Vites `manifest.json` beim Anwendungsstart einliest.

**Begründung:**
Vite ist auf Geschwindigkeit optimiert: ES-Module-basiertes Dev-Serving ohne vorheriges Bündeln, Hot Module Replacement für sofortige JS-Updates während der Entwicklung, und schnelle Produktions-Builds via Rollup. Für ein Vanilla-JS-Projekt ohne Framework-Compiler ist Vite minimal-invasiv.

Die Manifest-basierte Asset-Auflösung löst das Cache-Busting-Problem sauber: Vite erzeugt in Production gehashte Dateinamen und eine `manifest.json` als Mapping. Ein Spring `@Component` liest dieses Manifest beim Start ein. Ein Thymeleaf-Dialect stellt die Auflösung als `#vite.asset('js/main.js')` bereit – identischer Template-Code in Development (Vite Dev Server) und Production (gehashte Dateien aus `static/assets/`).

`maven-exec-plugin` wird gegenüber `frontend-maven-plugin` (eirslett) bevorzugt, weil letzteres ein Problem löst, das 2026 nicht mehr existiert: das automatische Herunterladen von Node.js. Node ist heute auf jeder Entwicklermaschine und in jedem CI-System genauso selbstverständlich vorhanden wie Java. `maven-exec-plugin` ruft schlicht `npm` auf dem System-PATH auf – transparenter, ohne versteckte `.node`-Verzeichnisse im Projekt und ohne zusätzlichen Plugin-Lifecycle-Overhead. Node-Versionsanforderungen werden über das `engines`-Feld in `package.json` und `.nvmrc` kommuniziert.

**Konsequenzen:**
- Frontend-Quellen liegen in `src/main/frontend/` (außerhalb von `src/main/resources`)
- Vite-Build-Output geht nach `src/main/resources/static/assets/` (in `.gitignore`)
- `maven-exec-plugin` ruft `npm install` und `npm run build` in der Maven-Phase `generate-resources` auf
- Node.js muss auf der Entwicklermaschine und im CI-System installiert sein (Standardvoraussetzung)
- Gewünschte Node-Version wird in `.nvmrc` und im `engines`-Feld von `package.json` dokumentiert
- In Development laufen Spring Boot (Port 8080) und Vite Dev Server (Port 5173) parallel
- `package.json` und `vite.config.js` liegen im Repository-Root und unterliegen dem normalen Review-Prozess

---

### ADR-013: Dreistufige Validierungsstrategie (HTML → Backend → Datenbank)

**Status:** Akzeptiert

**Kontext:**
Die Modul-Spezifikationen (FREIBERUFLER.md etc.) definieren Felder mit Typen, Längenbeschränkungen und Pflichtfeldkennzeichnungen. Es war zu entscheiden, auf welchen Ebenen diese Constraints umgesetzt und durchgesetzt werden. Technisch wäre es möglich, Validierung ausschließlich im Backend zu implementieren und das Frontend unvalidiert zu lassen, oder umgekehrt ausschließlich auf HTML5-Browservalidierung zu setzen.

**Entscheidung:**
Jede Feldconstraint aus den Modul-Spezifikationen wird verbindlich auf drei Ebenen implementiert: HTML-Validierungsattribute (Frontend), Bean Validation Annotationen auf Command-Objekten (Backend) und Spalten-Constraints in Flyway-Migrationen (Datenbank). Die Modul-Spezifikation ist die einzige autoritative Quelle für alle Constraints.

**Begründung:**

*Warum drei Ebenen statt nur Backend:*
Reine Backend-Validierung bedeutet, dass jede Eingabe erst nach einem Server-Round-Trip auf Fehler geprüft wird. Für eine Business-Applikation, in der Sachbearbeiter täglich viele Datensätze anlegen, ist das eine spürbare UX-Verschlechterung. Ein `required`-Attribut oder `maxlength` im HTML kostet null Implementierungsaufwand und liefert sofortiges Feedback, bevor der Nutzer überhaupt absendet. Das HTML5-Validierungs-API ist standardisiert, barrierefrei (ARIA-Integration ist eingebaut) und wartungsfrei.

*Warum nicht nur HTML5-Validierung:*
HTML5-Validierung ist clientseitig und kann trivial umgangen werden – ein einfacher `curl`-Aufruf oder ein manipulierter Request mit deaktiviertem JavaScript reicht. Die Backend-Validierung via Bean Validation ist die einzige Schicht, die nicht umgangen werden kann. Sie ist nicht optional, sondern die Basis für Datenkorrektheit.

*Warum zusätzlich Datenbankconstraints:*
Selbst sorgfältig implementierter Anwendungscode kann Fehler enthalten – ein vergessenes `@Valid` im Controller, ein direkter Datenbankzugriff in einem Testsetup, eine zukünftige Batch-Importfunktion. Datenbankconstraints sind die letzte Sicherheitslinie und unabhängig von der Applikationsschicht. Sie kosten im laufenden Betrieb nichts und schützen die Datenintegrität unabhängig davon, über welchen Pfad Daten eingefügt werden.

*Warum keine JS-basierte Frontendvalidierung:*
JavaScript-Validierungslogik wäre eine vierte Implementierung derselben Constraint – in einer dritten Sprache. Sie muss manuell synchron gehalten werden, kann Timing-Probleme haben und ist für die Barrierefreiheit schlechter als native HTML5-Validierung. HTML5-Attribute sind deklarativ und direkt im Template lesbar; ein späterer Entwickler sieht auf den ersten Blick, welche Constraints für ein Feld gelten.

*Konsistenzregel als Prozessvorgabe:*
Die eigentliche Herausforderung bei mehrschichtiger Validierung ist nicht die initiale Implementierung, sondern das spätere Synchronhalten. Deshalb gilt als verbindliche Prozessregel: Eine Constraint-Änderung in der Spec ist erst vollständig umgesetzt, wenn alle drei Ebenen angepasst sind. Code-Reviews prüfen diese Konsistenz explizit.

**Konsequenzen:**
- Jedes Formularfeld mit einer Constraint in der Spec hat mindestens ein entsprechendes HTML-Attribut (`required`, `maxlength`, `min`, `max`, `type`, `pattern`)
- Alle Command-Objekte auf der Command-Seite des CQRS tragen Bean-Validation-Annotationen für jede Constraint
- Controller-Methoden, die Command-Objekte entgegennehmen, verwenden immer `@Valid` – das Fehlen von `@Valid` ist ein Bug
- Flyway-Migrationen bilden alle Constraints als Spalten-Constraints ab (`NOT NULL`, `VARCHAR(n)`, `CHECK`)
- Fehlermeldungen bei Backend-Validierungsfehlern werden via `th:errors` inline am verursachenden Feld angezeigt – kein zentraler Error-Banner für Feldvalidierungsfehler
- Deutsche Fehlermeldungen für Bean-Validation-Fehler werden in `messages.properties` hinterlegt (Begründung und Scope: ADR-015)
- Systemfehler (Optimistic Locking, Datenbankfehler) werden weiterhin über den Banner-Mechanismus gemäß UI-DESIGNSYSTEM.md dargestellt, da sie keinem einzelnen Feld zugeordnet werden können

---

### ADR-014: POST-Navigation für Datensatz-Navigation (Prev/Next/First/Last)

**Status:** Akzeptiert

**Kontext:**
Alle CRUD-Module (Freiberufler, Partner, Kunden, Projekte) bieten eine Navigation zwischen Datensätzen (erster, vorheriger, nächster, letzter, Sprung zu ID). Die naheliegende Implementierung wäre, beim Seitenrendering die Ziel-IDs per SQL zu ermitteln und als `<a>`-Links direkt in das HTML einzubetten.

**Entscheidung:**
Navigations-Buttons werden als `<form method="post">`-Elemente mit versteckten Feldern (`currentId`, `direction`) gerendert. Die Ziel-ID wird **ausschließlich zum Zeitpunkt des Klicks** serverseitig berechnet. Der Server antwortet mit `302` auf die aufgelöste Ziel-URL.

**Begründung:**
Zwischen dem Rendering einer Seite und dem Klick eines Navigations-Buttons können andere Sachbearbeiter Datensätze anlegen, ändern oder löschen. Ein beim Rendering vorberechneter `<a href="/freiberufler/125">`-Link verweist damit möglicherweise auf einen nicht mehr existierenden Datensatz, überspringt neu angelegte Datensätze oder liefert inkonsistente Ergebnisse. Dieses Problem ist bei einer Mehrbenutzer-Anwendung strukturell unvermeidbar, wenn die Ziel-URL clientseitig fixiert wird.

Die POST-Navigation löst das Problem vollständig: Der Server berechnet den korrekten Nachfolger oder Vorgänger zum Klick-Zeitpunkt auf Basis des aktuellen Datenbankzustands. Der `302`-Redirect erzeugt einen normalen History-Eintrag – der Back Button funktioniert natürlich. Das Muster erfordert kein JavaScript.

**Alternativen und warum sie abgelehnt wurden:**
- `<a>`-Links mit vorberechneten IDs: Fehleranfällig bei gleichzeitiger Bearbeitung durch mehrere Sachbearbeiter (s.o.)
- AJAX-Navigation mit JS: Funktional äquivalent, aber komplexer und ohne Mehrwert gegenüber der reinen SSR-Lösung
- Keine Navigation zwischen Datensätzen: Fachlich nicht akzeptabel, Navigation ist explizit spezifiziert

**Konsequenzen:**
- Jeder Navigations-Button ist ein `<form method="post" action="/{modul}/navigate">` mit `currentId` und `direction` als hidden inputs
- Der `/navigate`-Endpunkt ist ein reiner POST-to-Redirect-Endpunkt ohne eigenes Template
- Gibt es keinen Nachfolger/Vorgänger, antwortet der Server mit `302` zur aktuellen URL; der Button wird beim nächsten Render deaktiviert dargestellt
- Verweist eine URL auf einen gelöschten Datensatz, antwortet der Server mit `404` und einer aussagekräftigen Fehlermeldung mit Link zur Suche
- Das Muster gilt einheitlich für alle vier navigierbaren Module

---

### ADR-015: Keine I18N – Einsprachige deutsche Anwendung mit `messages.properties` nur für Bean Validation

**Status:** Akzeptiert

**Kontext:**
Powerstaff 2026 richtet sich ausschließlich an deutschsprachige Sachbearbeiter einer Personalvermittlung. Alle UI-Texte, Fehlermeldungen und fachlichen Benachrichtigungen sind auf Deutsch. Bean Validation produziert standardmäßig englische Constraint-Meldungen (`"must not be null"`, `"size must be between 0 and 100"`). Es war zu entscheiden, ob eine vollständige I18N-Infrastruktur aufgebaut oder ein gezielter Minimalansatz gewählt wird.

**Entscheidung:**
Powerstaff 2026 implementiert **kein Internationalisierungs-Framework**. Eine einzige `src/main/resources/messages.properties` (ohne Locale-Suffix) enthält ausschließlich deutsche Übersetzungen für Bean-Validation-Constraint-Meldungen. Alle anderen Texte (UI-Labels, Controller-Meldungen, Thymeleaf-Templates) bleiben als deutsche Literale im Quellcode.

**Begründung:**

*Warum kein vollständiges I18N:*
I18N für eine einzige Sprache ist YAGNI. Eine vollständige I18N-Infrastruktur (Thymeleaf `#{...}`-Ausdrücke, `MessageSource`-Lookup im Controller, Locale-Negotiation via `Accept-Language` oder URL-Parameter) fügt für eine einsprachige Anwendung ausschließlich Komplexität hinzu: Templates werden schwerer lesbar, da Texte aus Properties-Dateien statt inline vorliegen; Änderungen an UI-Texten erfordern Änderungen in Properties-Dateien statt direkt im Template; neue Entwickler müssen das Indirektionssystem verstehen, bevor sie einfache Textänderungen vornehmen können.

Das Fachvokabular (Freiberufler, Partner, Projektstatus, Einsatzort) ist inhärent deutsch. Eine spätere Internationalisierung wäre kein reines Übersetzungsproblem, sondern würde fachliche Modellierungsentscheidungen berühren – zum Beispiel ob Stammdaten (Tag-Kategorien, Projektstatusbezeichnungen) lokalisierbar sein müssen. Dieser Aufwand ist unabhängig davon, ob heute Thymeleaf `#{...}` oder Inline-Texte verwendet werden.

*Warum `messages.properties` dennoch für Bean Validation:*
Bean Validations Default-Meldungen sind englisch und stammen aus der Referenzimplementierung Hibernate Validator. Ohne Überschreibung würden im UI Meldungen wie `"must not be null"` erscheinen – inkonsistent mit der durchgängig deutschen Oberfläche. Spring Boots `MessageSource` greift automatisch auf `messages.properties` zurück, wenn Standard-Constraint-Codes (z.B. `NotNull`, `Size`, `NotBlank`) aufgelöst werden. Eine einzige Datei ohne Locale-Suffix reicht aus, um alle Bean-Validation-Meldungen zu lokalisieren – kein Locale-Switching, keine Fallback-Kette, keine Spracherkennung. Der Aufwand ist minimal, der Effekt (deutsches UI ohne englische Einsprengsel) ist unmittelbar sichtbar.

*Abgrenzung:* Controller- und Service-Fehlermeldungen (z.B. RESTRICT-Meldungen bei blockiertem Löschen, Optimistic-Locking-Hinweise) werden **nicht** über `messages.properties` verwaltet. Diese Meldungen sind fachlich und architektonisch spezifisch; ein Properties-Key wie `freelancer.deletion.blocked` würde eine künstliche Indirektion einführen ohne den Code lesbarer zu machen. Die Meldungstexte liegen direkt als Literale in den jeweiligen Exception-Klassen oder im `@ControllerAdvice`.

**Konsequenzen:**
- `src/main/resources/messages.properties` enthält ausschließlich Überschreibungen für Bean-Validation-Standardcodes (`NotNull.default`, `Size.default`, `NotBlank.default` etc.) auf Deutsch
- Thymeleaf-Templates verwenden **keine** `#{...}`-Ausdrücke für UI-Texte – Texte stehen inline im Template
- `MessageSource` wird nicht manuell in Controller oder Services injiziert
- Keine Locale-Konfiguration, kein `LocaleChangeInterceptor`, kein `LocaleResolver`
- Neue UI-Texte werden direkt im Template auf Deutsch geschrieben – kein Properties-Key anlegen
- Wenn zukünftig Mehrsprachigkeit benötigt wird, ist eine Migration erforderlich; diese Entscheidung wird dann bewusst und mit bekanntem Aufwand getroffen

---

### ADR-016: Lowercase Snake Case für alle Datenbankobjekte; lower_case_table_names=1

**Status:** Akzeptiert

**Kontext:**
MySQL verhält sich bei Tabellennamen je nach Betriebssystem unterschiedlich: Auf Linux sind Tabellennamen standardmäßig case-sensitive (`lower_case_table_names=0`), auf Windows und macOS case-insensitive. Diese Plattformabhängigkeit führt zu schwer reproduzierbaren Fehlern, wenn lokal auf macOS entwickelt und auf Linux deployt wird. Zusätzlich verwendeten die Spezifikationen bisher inkonsistente Konventionen (camelCase für manche Spalten, snake_case für andere), was im Widerspruch zur Spring-Data-JDBC-Standardkonfiguration steht.

**Entscheidung:**
Alle Datenbankobjekte (Tabellen, Spalten, Indizes, Constraints) werden ausschließlich in `lowercase_snake_case` benannt. MySQL wird mit `lower_case_table_names=1` initialisiert – sowohl in Docker Compose (lokale Entwicklung) als auch in der Produktionsdatenbank.

**Begründung:**
- `lowercase_snake_case` ist der SQL-Industriestandard und eliminiert plattformabhängiges Verhalten vollständig
- `lower_case_table_names=1` muss bei MySQL 8 zwingend zum Initialisierungszeitpunkt gesetzt werden – eine nachträgliche Änderung ist nicht möglich
- Spring Data JDBC übersetzt Java-camelCase-Felder standardmäßig in DB-snake_case (`NamingStrategy`), sodass keine `@Column`-Annotationen nötig sind
- Flyway-Migrationen sind in reinem lowercase zu schreiben, was die Portabilität und Lesbarkeit erhöht

**Konsequenzen:**
- Alle Tabellen- und Spaltennamen in Flyway-Migrationen sind lowercase snake_case
- Spring Data JDBC `NamingStrategy` wird nicht überschrieben – camelCase Java-Felder werden automatisch auf snake_case-Spalten gemappt
- `lower_case_table_names=1` in `docker-compose.yml` (MySQL-Service): `command: --lower-case-table-names=1`
- `lower_case_table_names=1` muss bei der Produktionsdatenbank zum Initialisierungszeitpunkt gesetzt sein
- Bestehende Audit-Spalten folgen der Spring-Data-JDBC-Konvention: `created_at`, `created_by`, `last_modified_at`, `last_modified_by`

### ADR-017: Stateless HTTP-Endpunkte – kein HttpSession

**Status:** Akzeptiert

**Kontext:**
Der initiale `PartnerController` speicherte zwei Arten von Zustand in der HTTP-Session: (1) die zuletzt
angezeigte Partner-ID zur Navigation und (2) die QBE-Suchkriterien für Infinite-Scroll-Pagination.
Sessions erfordern serverseitigen Speicher, erschweren horizontales Skalieren und stehen im Widerspruch
zum Grundsatz (Abschnitt 5), dass "kein fachlicher Zustand ausschließlich in Browser-Session oder
JS-State gespeichert" sein soll.

**Entscheidung:**
Alle HTTP-Endpunkte der Anwendung sind zustandslos (kein `HttpSession`). Stattdessen:

1. **Zuletzt angezeigter Datensatz** (z. B. "last partner"): Wird als kurzlebiges Browser-Cookie
   (30 Tage, gleiches Path-Präfix) gespeichert. Der Server schreibt das Cookie beim Laden eines
   Datensatzes (`GET /{modul}/{id}`) und löscht es beim Öffnen des Neuanlage-Formulars.
   Der `GET /{modul}`-Einstiegspunkt liest den Wert via `@CookieValue(required = false)`.

2. **QBE-Suchkriterien für Pagination** (Infinite Scroll): Alle Suchfelder werden als URL-Query-
   Parameter in den `X-Next-Url`-Header kodiert. Der Client übergibt dieselben Parameter beim
   nächsten `GET /{modul}/search-more`-Request. Spring MVC bindet sie via `@ModelAttribute`.

**Begründung:**
- Cookies sind explizit erlaubt (Authentifizierungstoken ist ein Cookie); sie stellen keinen
  serverseitigen Zustand dar
- URL-Parameter sind bookmarkbar, transparent und reproduzierbar (Grundsatz Abschnitt 5)
- Kein Session-Affinity beim Skalieren notwendig; vereinfacht die Testbarkeit

**Konsequenzen:**
- `HttpSession` ist in Controllers **verboten** — kein `session.getAttribute`/`session.setAttribute`
- Zuletzt-angezeigt-Cookies werden pro Modul und pro Browser gespeichert (Path-Cookie)
- Jeder Controller implementiert eine `buildSearchMoreUrl`-Hilfsmethode, die alle Criteria-Felder
  als Query-Parameter kodiert (null-Felder werden weggelassen)
- `@CookieValue(required = false)` für optionale Cookie-Lesezugriffe verwenden

---

### ADR-018: Kein JdbcClient und kein direkter Tabellenzugriff in Controllers

**Status:** Akzeptiert

**Kontext:**
Im initialen `PartnerController` wurden drei Freelancer-Zuordnungs-Endpunkte
(`assign-freelancer`, `confirm-reassign-freelancer`, `remove-freelancer`) direkt mit
`JdbcClient` implementiert: Lookup per Code, gezielter `UPDATE freelancer SET partner_id`
und Abfrage des Partner-Namens für die Konfliktmeldung. Das verletzt zwei Architekturprinzipien
gleichzeitig: (1) Controller dürfen keine Datenbanklogik enthalten; (2) das Partner-Modul darf
nicht direkt auf Tabellen des Freelancer-Moduls zugreifen.

**Entscheidung:**
Controllers sind dünne Adapter zwischen HTTP und dem Service-Layer. Sie dürfen:
- `CommandService`-Methoden aufrufen (schreibende Operationen)
- `QueryService`-Methoden aufrufen (lesende Operationen für das Rendering)

Sie dürfen **nicht**:
- `JdbcClient` injizieren und SQL ausführen
- `Repository`-Interfaces direkt injizieren
- Tabellen eines anderen Moduls adressieren (weder per JdbcClient noch per Repository)

Cross-Modul-Operationen laufen ausschließlich über öffentliche Service-Methoden des Zielmoduls.
Für die Freelancer-Zuordnung wurde `FreelancerCommandService` mit `findByCode`,
`assignToPartner` und `removeFromPartner` eingeführt. Der `PartnerController` nutzt diesen
Service; der Freelancer-interne `JdbcClient` bleibt innerhalb des Freelancer-Moduls.

**Begründung:**
- Modulgrenze: Das Freelancer-Modul ist Eigentümer der `freelancer`-Tabelle. Jede
  Schreiboperation darauf muss durch das Freelancer-Modul laufen.
- Testbarkeit: Controller-Tests (`@SpringBootTest(webEnvironment = MOCK)`) können Service-Mocks
  verwenden, ohne echte SQL-Ausführung.
- Single Responsibility: SQL-Logik gehört in Services, nicht in Controllers.

**Konsequenzen:**
- `JdbcClient` ist in Controllers **verboten** (kein `@Autowired JdbcClient`)
- Jedes Modul, das Daten eines anderen Moduls mutieren muss, stellt dafür einen öffentlichen
  `CommandService` bereit
- Lesende Cross-Modul-Abfragen (z. B. `findFreelancersByPartner` im `PartnerQueryService`)
  sind weiterhin erlaubt — die Query-Seite darf per CQRS (ADR-002) denormalisierte Abfragen
  über Modulgrenzen hinweg stellen

