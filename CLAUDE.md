# Powerstaff 2026 – Claude Code Instruktionen

## Pflichtlektüre vor Implementierungsarbeit

1. `specs/SWARCHITEKTUR.md` – verbindliche Architektur, alle ADRs
2. `specs/UEBERSICHT.md` – fachliche Struktur und Modulübersicht
3. `TASKS.md` – aktueller Implementierungsstand, Recovery-Punkte

Vor jedem neuen Task: `TASKS.md` lesen und prüfen, welche Tasks bereits abgehakt sind.

---

## Projektstruktur

```
de.mirkosertic.powerstaff          ← Root-Package (Spring Boot Application)
├── freelancer                     ← Modul: Freiberuflerverwaltung
│   ├── command                    ← Aggregate, Repository (package-private), CommandService
│   ├── query                      ← QueryService (JdbcClient), Java Records
│   └── api                        ← Controller (Thymeleaf MVC)
├── partner                        ← Modul: Partnerverwaltung (analog)
├── customer                       ← Modul: Kundenverwaltung (analog)
├── project                        ← Modul: Projektverwaltung (analog)
├── profilesearch                  ← Modul: KI-gestützte Profilsuche
└── shared                         ← Stammdaten (Tags, HistoryTypes), SharedQueryService
```

Repositories sind immer `package-private`. Kein direkter Repository-Zugriff zwischen Modulen.

---

## Build-Befehle

```bash
# Kompilieren
./mvnw compile

# Unit-Tests (*Spec) ausführen
./mvnw test

# Integrationstests (*IT) ausführen – benötigt Docker für Testcontainers
./mvnw verify

# E2E TEsts ausführen – benötigt Docker für Testcontainers
./mvnw verify -Pe2e

# Alles zusammen ohne E2E-Tests
./mvnw clean verify

# Alles zusammen mit E2E-Tests
./mvnw clean verify -Pe2e

# Nur Integrationstests
./mvnw failsafe:integration-test failsafe:verify

# Mutation-Testing (nur Unit-Tests)
./mvnw test pitest:mutationCoverage
```

---

## Technologie-Entscheidungen (Kurzreferenz)

| Bereich                  | Entscheidung                                                            |
|--------------------------|-------------------------------------------------------------------------|
| Persistenz Command-Seite | Spring Data JDBC – kein JPA/Hibernate                                   |
| Persistenz Query-Seite   | `JdbcClient` + handgeschriebenes SQL – kein QueryDSL                    |
| SQL-Werte                | Immer als gebundene Parameter (`:param`) – niemals String-Konkatenation |
| Datenbank-Tests          | Testcontainers (echte MySQL) – kein H2, keine gemockte DB               |
| Templates                | Thymeleaf – kein React, kein Vue                                        |
| JavaScript               | Vanilla JS / Custom Elements (Light DOM) – kein Framework               |
| Schema-Migration         | Flyway – kein `ddl-auto`, kein `spring.sql.init.mode`                   |
| Caching                  | Keines – `@EnableCaching` nicht verwenden                               |
| Logging                  | Plain Text – kein JSON-Logging                                          |

---

## Namenskonventionen

| Artefakt         | Muster                     | Beispiel                                       |
|------------------|----------------------------|------------------------------------------------|
| Aggregate Root   | `{Modul}Aggregate`         | `FreelancerAggregate`                          |
| Repository       | `{Modul}Repository`        | `FreelancerRepository` (package-private)       |
| Command Service  | `{Modul}CommandService`    | `FreelancerCommandService`                     |
| Query Service    | `{Modul}QueryService`      | `FreelancerQueryService`                       |
| Controller       | `{Modul}Controller`        | `FreelancerController`                         |
| Query Record     | `{Modul}{Zweck}`           | `FreelancerListView`, `FreelancerSearchResult` |
| Unit-Test        | `{Klasse}Spec`             | `FreelancerCommandServiceSpec`                 |
| Integrationstest | `{Klasse}IT`               | `FreelancerQueryServiceIT`                     |
| Flyway-Migration | `V{n}__{beschreibung}.sql` | `V1__init_schema.sql`                          |

---

## Test-Regeln

- **Jede Klasse mit DB-Zugriff** (Repository, QueryService) braucht einen `@DataJdbcTest`-IT gegen Testcontainers
- **Jede QBE-Suche** muss einen IT haben, der alle Filterfelder mindestens einmal befüllt
- **Controller-Tests** mit `@WebMvcTest` – Thymeleaf-Rendering vollständig aktiv, kein Mocking der View
- **Modulgrenz-Tests** mit `@ApplicationModuleTest` für jedes Modul
- **Keine gemockte Datenbank** – H2 ist verboten
- Unit-Tests (`*Spec`) für isolierte Logik ohne DB-Zugriff
- Integrationstests (`*IT`) laufen in der `integration-test`-Phase via maven-failsafe
- Testabdeckung sollte mind. 80% sein.
- E2E Tests ersetzen keine Unit-Tests, sondern ergänzen sie, um die Funktionalität im Frontend zu prüfen!
- Wenn zur Entscheidung steht, wie ein Test implementiert wird (Unit- vs. IT-Test), so soll die Testart
  genommen werden, die die beste Testabdeckung erzeugt und am schnellsten ausführbar ist.

---

## Template-Implementierung – Pflichtregeln

**Vor dem Schreiben jedes Thymeleaf-Templates:**

1. Alle 5 CSS-Dateien lesen: `src/main/frontend/src/css/base.css`, `layout.css`,
   `components.css`, `components2.css`, `chat.css`
2. **Nur CSS-Klassen verwenden, die dort definiert sind** – keine neuen erfinden
3. `prototype/freiberufler.html` als visuelle Referenz für Layout und HTML-Struktur nutzen

**Checkliste vor jedem Template-Commit:**

```
[ ] Alle verwendeten CSS-Klassen per Grep in src/main/frontend/src/css/ verifiziert?
[ ] .fcard hat .fcard-hd (mit .fcard-title + .fcard-chv) + .fcard-body?
[ ] fcard-Content ist in .form-grid eingebettet?
[ ] Field-Grids nutzen .fg2 / .fg3 / .fg4 (nicht .field-grid)?
[ ] Felder sind <div class="fg"><label>…</label><input/></div> Struktur?
[ ] Checkboxen nutzen .cbfield (nicht .checkbox-pill)?
[ ] Buttons nutzen .btn-ghost / .btn-pri / .btn-danger (nicht .btn-secondary / .btn-primary)?
[ ] Kontakthistorie nutzt .hlist / .hitem / .hitem-hd / .hbody (nicht .chist-*)?
[ ] Tags nutzen .tag-grid / .tg-label / .chip-row / .chip-x / .chip-add (nicht .tag-group / .chip-list)?
[ ] ps-dirty-banner ist INNERHALB des <form> Elements?
[ ] Toolbar-Löschen-Button ruft openModal() auf (nicht href="#...")?
[ ] Kontaktsperre-Banner nutzt .banner-forbidden (nicht .banner-error)?
```

---

## Verbotene Muster

```java
// ❌ String-Konkatenation in SQL
"WHERE name LIKE '%" + searchTerm + "%'"

// ❌ Direkte Repository-Aufrufe zwischen Modulen
// (im package customer darf kein FreelancerRepository injiziert werden)

// ❌ JPA/Hibernate Annotationen
@Entity, @OneToMany, @ManyToOne, @JoinColumn (JPA-Variante)

// ❌ Automatische Schema-Generierung
spring.jpa.hibernate.ddl-auto=create
spring.sql.init.mode=always

// ❌ H2 in Tests
@AutoConfigureTestDatabase  // ohne replace=NONE – verboten

// ❌ HttpSession in Controllers (ADR-017)
// session.getAttribute(...), session.setAttribute(...)
// Stattdessen: Cookie für "last viewed", URL-Parameter für Suchkriterien-Pagination

// ❌ JdbcClient oder Repository direkt in Controllers
// jdbcClient.sql("SELECT ...") in einem @Controller
// Stattdessen: immer über CommandService oder QueryService delegieren

// ❌ Cross-Modul-Tabellenzugriff (ADR-002)
// Modul A darf nicht direkt auf Tabellen von Modul B zugreifen
// Stattdessen: öffentliche Service-Methoden des Zielmoduls aufrufen
```

---

## Commit-Konvention

Nach jedem abgeschlossenen Task in `TASKS.md`:
1. `./mvnw clean verify` muss erfolgreich durchlaufen. 
2. Task in `TASKS.md` als erledigt markieren (`[x]`)
3. Git-Commit mit aussagekräftiger Message
4. Commit-Message endet mit `Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>`

Kein `git push` – das macht der Entwickler manuell.

---

## Recovery nach Abbruch

Falls ein Agent-Lauf abbricht:
1. `TASKS.md` öffnen – abgehakte Tasks (`[x]`) sind vollständig abgeschlossen
2. Den ersten nicht abgehakten Task (`[ ]`) als Einstiegspunkt verwenden
3. `git log --oneline -10` – zeigt den letzten stabilen Zustand
4. Bei Unsicherheit über den Zustand: `./mvnw clean verify` ausführen und Fehler beheben bevor neue Tasks begonnen werden
