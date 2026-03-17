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

# Alles zusammen
./mvnw clean verify

# Nur Integrationstests
./mvnw failsafe:integration-test failsafe:verify

# Mutation-Testing (nur Unit-Tests)
./mvnw test pitest:mutationCoverage
```

---

## Technologie-Entscheidungen (Kurzreferenz)

| Bereich | Entscheidung |
|---|---|
| Persistenz Command-Seite | Spring Data JDBC – kein JPA/Hibernate |
| Persistenz Query-Seite | `JdbcClient` + handgeschriebenes SQL – kein QueryDSL |
| SQL-Werte | Immer als gebundene Parameter (`:param`) – niemals String-Konkatenation |
| Datenbank-Tests | Testcontainers (echte MySQL) – kein H2, keine gemockte DB |
| Templates | Thymeleaf – kein React, kein Vue |
| JavaScript | Vanilla JS / Custom Elements (Light DOM) – kein Framework |
| Schema-Migration | Flyway – kein `ddl-auto`, kein `spring.sql.init.mode` |
| Caching | Keines – `@EnableCaching` nicht verwenden |
| Logging | Plain Text – kein JSON-Logging |

---

## Namenskonventionen

| Artefakt | Muster | Beispiel |
|---|---|---|
| Aggregate Root | `{Modul}Aggregate` | `FreelancerAggregate` |
| Repository | `{Modul}Repository` | `FreelancerRepository` (package-private) |
| Command Service | `{Modul}CommandService` | `FreelancerCommandService` |
| Query Service | `{Modul}QueryService` | `FreelancerQueryService` |
| Controller | `{Modul}Controller` | `FreelancerController` |
| Query Record | `{Modul}{Zweck}` | `FreelancerListView`, `FreelancerSearchResult` |
| Unit-Test | `{Klasse}Spec` | `FreelancerCommandServiceSpec` |
| Integrationstest | `{Klasse}IT` | `FreelancerQueryServiceIT` |
| Flyway-Migration | `V{n}__{beschreibung}.sql` | `V1__init_schema.sql` |

---

## Test-Regeln

- **Jede Klasse mit DB-Zugriff** (Repository, QueryService) braucht einen `@DataJdbcTest`-IT gegen Testcontainers
- **Jede QBE-Suche** muss einen IT haben, der alle Filterfelder mindestens einmal befüllt
- **Controller-Tests** mit `@WebMvcTest` – Thymeleaf-Rendering vollständig aktiv, kein Mocking der View
- **Modulgrenz-Tests** mit `@ApplicationModuleTest` für jedes Modul
- **Keine gemockte Datenbank** – H2 ist verboten
- Unit-Tests (`*Spec`) für isolierte Logik ohne DB-Zugriff
- Integrationstests (`*IT`) laufen in der `integration-test`-Phase via maven-failsafe

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
