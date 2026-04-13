# Powerstaff 2026

## Was ist Powerstaff 2026?

Powerstaff 2026 ist ein webbasiertes **Personalvermittlungs-Managementsystem** für Unternehmen, die selbstständige Freiberufler an Kundenprojekte vermitteln. Es digitalisiert und beschleunigt den gesamten Vermittlungsprozess – von der Erfassung und Pflege der Stammdaten über die strukturierte Kontakthistorie bis hin zur Besetzung offener Projektstellen.

## Fachlicher Kern

Das System dreht sich um vier zentrale Entitäten und deren Beziehungen:

| Entität          | Fachliche Rolle                                                                         |
|------------------|-----------------------------------------------------------------------------------------|
| **Freiberufler** | Die zu vermittelnden Spezialisten mit Profilen, Skills, Verfügbarkeiten und Konditionen |
| **Partner**      | Vermittlungsagenturen, die eine Gruppe eigener Freiberufler repräsentieren              |
| **Kunden**       | Unternehmen, die konkrete Projektanfragen stellen                                       |
| **Projekte**     | Offene Stellen und laufende Aufträge, für die passende Freiberufler gesucht werden      |

Die **Projektposition** ist die zentrale Vermittlungsbeziehung: Sie verbindet einen Freiberufler mit einem Projekt und trägt Farbstatus (z. B. „Vorgeschlagen", „Im Gespräch", „Besetzt"), Konditionen und Kommentar.

## Alleinstellungsmerkmale

### 1. Konversationelle KI-Profilsuche

Das Herzstück der Innovation: Sachbearbeiter beschreiben in natürlicher Sprache, welches Profil sie suchen – das System antwortet mit passenden Freiberufler-Vorschlägen als anklickbare Links. Der KI-Kontext wird bei **jeder Anfrage live aus der Datenbank** zusammengestellt und enthält automatisch alle Daten des aktuell geöffneten Projekts sowie die bereits zugeordneten Freiberufler. Damit arbeitet das KI-System immer auf dem aktuellen Stand – ohne manuelle Kontextpflege.

Ergänzend steht eine **klassische filterbasierte Suche** (Name, Tagessatz, Skills-Tags) mit sortierbarer Ergebnistabelle und Infinite Scrolling zur Verfügung.

### 2. Workflow-Beschleuniger: „Gemerktes Projekt"

Ein durchgängiges Bedienkonzept eliminiert das aufwändige Navigieren zwischen Formularen: Sobald ein Sachbearbeiter ein Projekt öffnet, wird es systemweit als „gemerktes Projekt" gespeichert und in der Toolbar aller Formulare sichtbar. Im Freiberufler-Formular erscheint dann direkt der Button „Dem Projekt zuordnen" – ein Klick öffnet ein Zuordnungsmodal. Der vollständige Workflow lautet: **Profilsuche → Profil im neuen Tab prüfen → Projekt zuordnen**, ohne einen einzigen Formularwechsel.

### 3. Robuste Datenintegrität mit nutzerfreundlichen Fehlermeldungen

Statt stillem Kaskadieren nach dem „Löschen verhindern"-Prinzip: Beim Versuch, einen Freiberufler mit aktiven Projektpositionen zu löschen, erscheint eine Fehlermeldung mit **direkten Links** zu den blockierenden Projekten. Gleiches gilt für Partner und Kunden mit zugeordneten Projekten. Parallelzugriffe werden durch **Optimistic Locking** (`db_version`) auf allen Entitäten abgesichert.

### 4. Eigenentwickeltes, WCAG-AA-konformes Design-System

Alle Formulare basieren auf einem einheitlichen, framework-unabhängigen Design-System – ohne Bootstrap, ohne Tailwind. Die Palette ist augenschonend für 6–8 Stunden Bildschirmarbeit optimiert und erfüllt WCAG AA (Kontrastverhältnis ≥ 4,5:1). Dynamische Statusfarben für Projektpositionen kommen direkt aus der Datenbank – kein CSS-Deployment bei Konfigurationsänderungen.

### 5. Moderner, wartungsarmer Technologie-Stack

| Merkmal                      | Umsetzung                                                                                         |
|------------------------------|---------------------------------------------------------------------------------------------------|
| **Zero-Framework-Frontend**  | Vanilla JS + Custom Elements (Light DOM) – keine npm-Abhängigkeitsprobleme                        |
| **Klar getrennte Lesepfade** | Logisches CQRS: Spring Data JDBC für Schreiboperationen, `JdbcClient` für optimierte Leseabfragen |
| **Modulare Architektur**     | Spring Boot Modulith erzwingt und dokumentiert Modulgrenzen – automatisch verifiziert durch CI    |
| **Keine Schema-Drift**       | Ausschließlich Flyway-Migrationen – kein `ddl-auto`, keine Überraschungen im Produktivbetrieb     |
| **Echte Datenbanktest**      | Testcontainers mit echter MySQL in allen Integrationstests – kein H2, kein Mock-Verhalten         |

## Architekturdiagramm (Modulübersicht)

```
de.powerstaff
├── freelancer        ← Freiberuflerverwaltung (Aggregate, CommandService, QueryService, Controller)
├── partner           ← Partnerverwaltung inkl. Freiberufler-Zuordnung
├── customer          ← Kundenverwaltung
├── project           ← Projektverwaltung inkl. Positionen und Status-Workflow
├── profilesearch     ← KI-Profilsuche: Chat-UI, Persistenz, klassische Filtersuche
└── shared            ← Stammdaten: Tags, Historientypen, Admin-UI
```

Kommunikation zwischen Modulen ausschließlich über publizierte Service-Interfaces (keine direkten Repository-Aufrufe quer durch Module). Modulgrenzen werden automatisch durch `@ApplicationModuleTest` in der CI-Pipeline verifiziert.

## Qualitätssicherung und Testergebnisse

Powerstaff 2026 setzt auf eine mehrschichtige Teststrategie: Unit-Tests (Spock/Groovy), Integrationstests gegen echte MySQL via Testcontainers, Spring Modulith-Modultests sowie End-to-End-Tests mit Playwright. Die E2E-Tests laufen bei jedem Pull Request automatisch in der CI-Pipeline und prüfen die vollständige Benutzeroberflächenfunktionalität im Browser.

Die jeweils aktuellen **E2E-Testergebnisse** (Playwright-Report mit Screenshots, Traces und Testergebnissen pro Modul) sind öffentlich einsehbar:

**[https://mirkosertic.github.io/Powerstaff2026/](https://mirkosertic.github.io/Powerstaff2026/)**

---

## Konfiguration für JDBC Metadaten-Extraktion im MCPLuceneServer

```
lucene:
    metadata:
        jdbc:
            enabled: true
            url: "jdbc:mysql://localhost:3306/powerstaff?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Berlin"
            username: "powerstaff"
            password: "powerstaff"              # env-var substitution supported
            poolSize: 5
            connectionTimeout: 30000            # ms
            queryTimeout: 5000                  # ms
            query: |
                SELECT JSON_OBJECT(
                    'fields', JSON_ARRAY(
                        JSON_OBJECT('name', 'tagessatz', 'type', 'long', 'value', f.salary_per_day_long, 'faceted', CAST(FALSE AS JSON)),
                        JSON_OBJECT('name', 'code', 'type', 'keyword', 'value', f.code, 'faceted', CAST(FALSE AS JSON)),
                        JSON_OBJECT('name', 'name1', 'type', 'keyword', 'value', f.name1, 'faceted', CAST(FALSE AS JSON)),
                        JSON_OBJECT('name', 'name2', 'type', 'keyword', 'value', f.name2, 'faceted', CAST(FALSE AS JSON)),
                        JSON_OBJECT('name', 'company', 'type', 'keyword', 'value', f.company, 'faceted', CAST(FALSE AS JSON)),
                        JSON_OBJECT('name', 'availability_as_date', 'type', 'keyword', 'value', f.availability_as_date, 'faceted', CAST(FALSE AS JSON)),                         
                        JSON_OBJECT('name', 'tags',        'type', 'long', 'values', (
                            SELECT JSON_ARRAYAGG(ft.tag_id)
                                FROM freelancer_tags ft
                            WHERE ft.freelancer_id = f.id
                        ), 'faceted', CAST(FALSE AS JSON))
                    )
                ) AS metadata_json
                FROM
                    freelancer f
                WHERE
                    f.code = REGEXP_SUBSTR(:file_path, '[A-Z]+-[0-9]+')
            parameters:
                - name: file_path
                  sourceField: file_path        # Lucene field to use as query parameter
            json:
                columnName: metadata_json       # Column in the result set containing the JSON

            sync:
                enabled: false
                intervalMinutes: 5
                query: |
                  SELECT code AS dbmeta_code
                  FROM freelancer
                  WHERE creation_date > :last_sync_timestamp OR creation_date > :last_sync_timestamp                
