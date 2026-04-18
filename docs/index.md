# Powerstaff 2026 – Benutzerhandbuch

Willkommen bei **Powerstaff 2026**, dem webbasierten Personalvermittlungs-Managementsystem.

## Was ist Powerstaff 2026?

Powerstaff 2026 unterstützt Unternehmen, die selbstständige Freiberufler an Kundenprojekte vermitteln.
Es digitalisiert den gesamten Vermittlungsprozess – von der Pflege der Freiberufler- und Kundenprofile
über die strukturierte Kontakthistorie bis hin zur Besetzung offener Projektstellen.

## Wesentliche Funktionen im Überblick

**Freiberufler-, Partner- und Kundenverwaltung**
Alle Stammdaten, Kontaktinformationen und Konditionen werden zentral verwaltet.
Eine typisierte Kontakthistorie dokumentiert alle Interaktionen lückenlos.

**Projektverwaltung mit Positionsstatus**
Projekte werden direkt aus dem Kunden- oder Partner-Formular heraus angelegt.
Jede Freiberufler-Zuordnung trägt einen konfigurierbaren Farbstatus
(z. B. „Vorgeschlagen", „Im Gespräch", „Besetzt") und individuelle Konditionen.

**KI-gestützte Profilsuche (Chat)**
Der schnellste Weg zum passenden Kandidaten: Anforderungen in natürlicher Sprache
eingeben – das System antwortet mit passenden Freiberufler-Links.
Der Kontext (Projektdaten, bereits zugeordnete Freiberufler) wird bei jeder Anfrage
automatisch und aktuell aus der Datenbank bezogen.

**Klassische Filtersuche**
Gezielte Suche nach Name, Tagessatz, Skills-Tags und Verfügbarkeit mit sortierbarer
Ergebnistabelle. Freiberufler mit Kontaktsperre werden visuell hervorgehoben.

**Workflow-Beschleuniger: Gemerktes Projekt**
Ein Projekt einmal öffnen – und es steht in der gesamten Oberfläche als
„gemerktes Projekt" bereit. Im Freiberufler-Formular erscheint dann direkt
der Button „Dem Projekt zuordnen": ein Klick, kein Formularwechsel.
Vollständiger Arbeitsfluss: *Profilsuche → Profil prüfen → Projekt zuordnen*.

**Datenintegrität mit verständlichen Fehlermeldungen**
Beim Versuch, einen Freiberufler, Partner oder Kunden mit aktiven Projektbezügen
zu löschen, erscheint eine Fehlermeldung mit direkten Links zu den blockierenden
Datensätzen – statt stillem Abbruch.

---

## Inhalt

| Bereich                               | Beschreibung                                 |
|---------------------------------------|----------------------------------------------|
| [Erste Schritte](erste-schritte.md)   | Login, Oberfläche, Navigation, Grundkonzepte |
| [Freiberufler](freiberufler/index.md) | Profile anlegen, suchen und verwalten        |
| [Partner](partner/index.md)           | Partnervermittlungsagenturen verwalten       |
| [Kunden](kunden/index.md)             | Kundenunternehmen verwalten                  |
| [Projekte](projekte/index.md)         | Projekte und Positionen verwalten            |
| [Profilsuche – KI-Chat](profilsuche/ki-chat.md) | Freiberufler in natürlicher Sprache beschreiben; ideal für offene, exploratorische Suchen |
| [Profilsuche – Klassisch](profilsuche/klassisch.md) | Gezielte Filterung nach Tagessatz, Tags und Verfügbarkeit; ideal wenn die Kriterien bereits feststehen |
| [Administration](admin/stammdaten.md) | Stammdaten, Benutzer, API-Tokens             |

## Kurzübersicht: Die vier zentralen Entitäten

```
Freiberufler ──── wird zugeordnet zu ──── Projektposition ──── gehört zu ──── Projekt
     │                                                                            │
     └── wird vermittelt über ──── Partner          Kunde ── beauftragt ──────────┘
```

- **Freiberufler** – Die zu vermittelnden Spezialisten mit Profilen, Skills und Konditionen
- **Partner** – Vermittlungsagenturen, die eigene Freiberufler repräsentieren
- **Kunden** – Unternehmen, die Projektanfragen stellen
- **Projekte** – Offene Stellen und Aufträge, für die Freiberufler gesucht werden

Die **Projektposition** ist die zentrale Verbindung: Sie trägt Status, Konditionen und Kommentar.

## Screenshots aktualisieren

```bash
# Voraussetzung: JAR gebaut (mvnw package -DskipTests)
npx playwright test --config src/test/e2e/playwright.config.docs.ts
```
