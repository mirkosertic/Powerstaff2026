# Spezifikations-Erweiterung PROFILSUCHE.md

**Hinweis**: Dieser Inhalt soll in die bestehende `specs/PROFILSUCHE.md` eingefügt werden.

---

## Erweiterung: Klassische Suche (Google-Style)

**Ergänzung ab Version 1.1 (März 2026)**

Zusätzlich zur konversationellen Chat-Suche bietet die Profilsuche eine klassische filterbasierte Suche mit direkt sichtbaren Suchfeldern und tabellarischen Ergebnissen.

### Anwendungsfall (erweitert)

Die Profilsuche bietet zwei Such-Modi:

1. **Chat** (bestehend): Konversationelles Interface für komplexe, iterative Suchen
2. **Klassische Suche** (neu): Filterbasiertes Interface für strukturierte Abfragen

Beide Modi sind über eine Tab-Navigation zugänglich. Der Standardmodus beim Öffnen von `/profilesearch` ist der Chat-Modus.

### Datenbankstruktur (unverändert)

Die bestehenden Tabellen `profile_search_chat` und `profile_search_message` bleiben unverändert. Die klassische Suche benötigt keine zusätzliche Persistenz – alle Suchkriterien werden über URL-Parameter übergeben (stateless, ADR-017, ADR-019).

### Layout und Navigation (erweitert)

#### URL-Struktur

| URL | Beschreibung |
|-----|--------------|
| `/profilesearch` | Redirect zu `/profilesearch/chat` (Standardmodus) |
| `/profilesearch/chat` | Redirect zum neuesten Chat oder neuen Chat erstellen |
| `/profilesearch/chat/{chatId}` | Bestehende Chat-Ansicht (unverändert) |
| `/profilesearch/search` | Klassische Suche (neu) |

#### Tab-Navigation

Beide Modi (Chat und Suche) zeigen am oberen Rand der Seite eine Tab-Navigation:

```
┌──────────────────────────────────────────────┐
│  Toolbar: Profilsuche | Gemerktes Projekt    │
├──────────────────────────────────────────────┤
│  [Chat]  [Suche]  ← Tab-Navigation           │
├──────────────────────────────────────────────┤
│  ... Inhaltsbereich ...                      │
└──────────────────────────────────────────────┘
```

- **Aktiver Tab**: Primär-Button-Style (`.btn-pri`)
- **Inaktiver Tab**: Ghost-Button-Style (`.btn-ghost`)
- Klick wechselt zwischen Modi

### Klassische Suche: Aufbau

Der klassische Such-Modus besteht aus zwei Bereichen:

1. **Suchformular** (oben): Filterkarte mit Suchkriterien
2. **Suchergebnisse** (unten): Sortierbare Tabelle mit Infinite Scrolling

#### Suchformular

Das Formular zeigt folgende Felder:

| Feld | Typ | Validierung | Hinweise |
|------|-----|-------------|----------|
| `searchTerm` | Text | Optional | Durchsucht `freelancer.name1`, `freelancer.name2`, `freelancer.skills` (LIKE) |
| `salaryPerDayFrom` | Number | Optional | Minimum-Tagessatz in € |
| `salaryPerDayTo` | Number | Optional | Maximum-Tagessatz in € |
| `tagIds` | Multi-Select (Chips) | Optional | Tags mit `TagType.TYP`, comma-separated IDs |

**Validierung**: Mindestens eines der vier Felder muss ausgefüllt sein.

**Formular-Aktionen**:
- **Suchen**: Führt Suche aus, zeigt Ergebnisse
- **Zurücksetzen**: Lädt leeres Formular ohne Suche auszuführen

#### Suchergebnisse

Die Ergebnisse werden in einer sortierbaren Tabelle angezeigt:

| Spalte | Quelle | Sortierbar | Hinweise |
|--------|--------|------------|----------|
| Name 1 | `freelancer.name1` | Ja | |
| Name 2 | `freelancer.name2` | Ja | |
| Letzter Kontakt | `freelancer.last_contact_date` | Ja | Format: `dd.MM.yyyy` |
| Tagessatz | `freelancer.salary_per_day_long` | Ja | Format: `XXX €` |
| Verfügbar ab | `freelancer.availability_as_date` | Ja | Format: `dd.MM.yyyy` |
| Code | `freelancer.code` | Ja | |
| Tags | `tags` (JOIN) | Nein | Kleine Chips (`.chip-xs`), klickbar |

**Sortierung**:
- Default: `name1 ASC, name2 ASC`
- Klick auf Spaltenheader wechselt Sortierung
- URL-Parameter: `sortField`, `sortDir` (asc/desc)
- Visueller Indikator: `.srt-asc` / `.srt-desc` CSS-Klasse

**Kontaktsperre-Markierung**:
- Freiberufler mit `contactforbidden=true` haben roten Hintergrund (`background: var(--danger-l)`)
- Text in rot (`color: var(--danger)`)
- Gilt für Profilsuche UND Freiberufler-QBE-Suche

**Infinite Scrolling**:
- Initial: 20 Ergebnisse (PAGE_SIZE)
- Bei Scroll ans Ende: Automatisches Nachladen
- Custom Element: `<ps-infinite-scroll>`

#### Ergebnis-Interaktionen

1. **Klick auf Tabellenzeile**:
   - Öffnet Freiberufler-Detail: `/freelancer/{id}?returnTo=profilesearch-search`
   - Zurück-Button im Freiberufler-Formular führt zurück zur Suche

2. **Klick auf Tag**:
   - Startet Freiberufler-QBE mit Tag-Filter: `/freelancer/search?tagId={id}&returnTo=profilesearch-search`
   - Zeigt alle Freiberufler mit diesem Tag
   - Zurück-Button führt zurück zur Profilsuche-Suche

### Technische Implementierung

#### Backend-Komponenten

**ProfileSearchCriteria** (neu):
```java
public record ProfileSearchCriteria(
    String searchTerm,
    Long salaryPerDayFrom,
    Long salaryPerDayTo,
    String tagIds,  // comma-separated
    String sortField,
    String sortDir
)
```

**ProfileSearchResult** (neu):
```java
public record ProfileSearchResult(
    Long id,
    String code,
    String name1,
    String name2,
    LocalDateTime lastContactDate,
    Long salaryPerDayLong,
    LocalDateTime availabilityAsDate,
    boolean contactForbidden,
    List<TagView> tags
)
```

**ProfileSearchQueryService** (erweitert):
- `searchFreelancers(criteria, offset, limit)` → `List<ProfileSearchResult>`
- `countSearchFreelancers(criteria)` → `long`

**ProfileSearchController** (erweitert):
- `GET /profilesearch` → Redirect zu `/profilesearch/chat`
- `GET /profilesearch/chat` → Redirect zu neuester Chat-Sitzung
- `GET /profilesearch/search` → Klassische Suche (neu)
  - Offset = 0: Vollständige Seite mit Formular und Ergebnissen
  - Offset > 0: Fragment (nur `<tr>` Elemente) für Infinite Scroll
  - Header: `X-Next-Url` wenn mehr Ergebnisse verfügbar

#### Frontend-Templates

**search-page.html** (neu):
- Vollständige Suchseite mit Formular und Ergebnistabelle
- Tab-Navigation zu Chat
- JavaScript für Tag-Auswahl (`.chip.selected` Toggle)

**search-results.html** (neu):
- Fragment mit `<tr>` Elementen für Infinite Scroll

**form.html** (erweitert):
- Tab-Navigation zu Suche

#### CSS-Erweiterungen

**components2.css**:
```css
.chip.selected {
  background: var(--pri);
  color: white;
  border-color: var(--pri-d);
}
```

### Integration mit Freiberufler-Modul

Die klassische Suche integriert sich mit dem Freiberufler-Modul:

#### Freiberufler-QBE erweitert um Tag-Filter

**FreelancerSearchCriteria** (erweitert):
- Neuer Parameter: `Long tagId`

**FreelancerQueryService** (erweitert):
- Filter: `WHERE EXISTS (SELECT 1 FROM freelancer_tags WHERE freelancer_id = f.id AND tag_id = :tagId)`

**FreelancerController** (erweitert):
- `show(id, returnTo)`: Zurück-Button zeigt "Zurück zur Profilsuche" wenn `returnTo=profilesearch-search`
- `search(criteria, returnTo)`: Zurück-Button führt zur Profilsuche wenn `returnTo=profilesearch-search`

### Cache-Control und Bookmarking

Gemäß ADR-019:
- Initial: `Cache-Control: no-store, no-cache, must-revalidate`
- Infinite Scroll: Kein Cache-Control (Browser entscheidet)
- URLs sind bookmarkable und shareable

### Gemerktes Projekt

Das gemerkte Projekt wird in der Toolbar angezeigt (wie im Chat-Modus), hat aber **keinen Einfluss auf die Suchergebnisse**. Die klassische Suche durchsucht **alle Freiberufler**, unabhängig vom gemerkten Projekt.

Rationale: Die Chat-Suche nutzt das gemerkte Projekt als Kontext für die KI. Die klassische Suche ist ein allgemeines Tool ohne Projektbezug.

### Offene Punkte

#### Mock-Implementierung vs. Echte Suche

**Phase 1 (aktuell)**: Mock-Service
- Liefert feste Mock-Daten (50 Einträge)
- Grundlegende Filterung simuliert
- Für UI-Testing und Akzeptanztests ausreichend

**Phase 2 (später)**: Echte SQL-Implementierung
- LIKE-Suche auf `name1`, `name2`, `skills`
- Numerische Filter auf `salary_per_day_long`
- JOIN auf `freelancer_tags` für Tag-Filter
- Batch-Loading der Tags für Ergebnisse

#### Zukünftige Erweiterungen

- **Erweiterte Filter**: Verfügbarkeit, Stadt, Partner-Zuordnung
- **Gespeicherte Suchen**: User kann häufige Suchen speichern
- **Export**: Suchergebnisse als CSV/PDF exportieren
- **Multi-Tag-Filter**: UND/ODER-Verknüpfung von Tags

---

## Aktualisierte Übersicht

### Datenbankstruktur (komplett)

Die Profilsuche verwendet zwei Tabellen:

1. **profile_search_chat**: Chat-Sitzungen (nur für Chat-Modus)
2. **profile_search_message**: Nachrichten (nur für Chat-Modus)

Die klassische Suche ist **stateless** und nutzt keine dedizierten Tabellen.

### URL-Struktur (komplett)

| URL | Funktion |
|-----|----------|
| `/profilesearch` | Redirect zu `/profilesearch/chat` |
| `/profilesearch/chat` | Redirect zu neuester Chat-Sitzung oder neuen Chat erstellen |
| `/profilesearch/chat/{chatId}` | Chat-Ansicht mit Nachrichten und Sidebar |
| `/profilesearch/search` | Klassische Suche mit Filterformular |
| `/profilesearch/search?offset=N&...` | Infinite Scroll Fragment |

### Toolbar-Aktionen (komplett)

#### Im Chat-Modus
- **Gemerktes Projekt**: Anzeige (informativ)
- **"＋ Neuer Chat"**: Neue Chat-Sitzung anlegen
- **"Löschen"**: Aktuelle Chat-Sitzung löschen

#### Im Such-Modus
- **Gemerktes Projekt**: Anzeige (informativ, kein Einfluss auf Suche)
- Keine weiteren Aktionen

---

**Änderungshistorie**:
- **Version 1.0** (Februar 2026): Initiale Spezifikation (nur Chat-Modus)
- **Version 1.1** (März 2026): Ergänzung um klassische Suche
