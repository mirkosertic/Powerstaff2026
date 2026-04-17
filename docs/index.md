# Powerstaff 2026 – Benutzerhandbuch

Willkommen bei **Powerstaff 2026**, dem webbasierten Personalvermittlungs-Managementsystem.

## Inhalt

| Bereich | Beschreibung |
|---------|-------------|
| [Erste Schritte](erste-schritte.md) | Login, Oberfläche, Navigation, Grundkonzepte |
| [Freiberufler](freiberufler/index.md) | Profile anlegen, suchen und verwalten |
| [Partner](partner/index.md) | Partnervermittlungsagenturen verwalten |
| [Kunden](kunden/index.md) | Kundenunternehmen verwalten |
| [Projekte](projekte/index.md) | Projekte und Positionen verwalten |
| [Profilsuche](profilsuche/ki-chat.md) | KI-gestützte und klassische Profilsuche |
| [Administration](admin/stammdaten.md) | Stammdaten, Benutzer, API-Tokens |

## Kurzübersicht: Die vier zentralen Entitäten

```
Freiberufler ──── wird zugeordnet zu ──── Projektposition ──── gehört zu ──── Projekt
     │                                                                            │
     └── wird vermittelt über ──── Partner          Kunde ── beauftragt ─────────┘
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
