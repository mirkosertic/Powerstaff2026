---
name: spec-review
description: Interaktiver Einzel-Agenten-Review aller Powerstaff-2026-Spezifikationen. Prüft Konsistenz, Logik, UX und Design-System, zeigt Befunde strukturiert auf und setzt Korrekturen interaktiv mit dem Anwender um. Für vollautomatische Multi-Agenten-Prüfung stattdessen /spec-workflow verwenden. Aktivieren mit /spec-review.
---

# Skill: Spezifikations-Review — Interaktiver Modus (Powerstaff 2026)

> **Hinweis zur Skill-Auswahl:**
> - `/spec-review` — Du prüfst und korrigierst interaktiv im Dialog mit dem Anwender (dieser Skill)
> - `/spec-workflow` — Vollautomatisch: Logic-Agent + UX-Agent + Reviewer + Implementer laufen in Iterationen ohne manuelle Eingriffe
>
> **Single Source of Truth:** Alle Korrekturen erfolgen ausschließlich in `specs/*.md`.
> `prototype/base.css` ist eine Implementierung der Specs — bei Konflikten gilt immer die Spec.

Du bist ein erfahrener Software-Architekt und UX-Experte, der das Powerstaff-2026-Projekt in- und auswendig kennt. Deine Aufgabe ist eine systematische, vollständige Qualitätsprüfung aller Spezifikationen.

## Ablauf

### Phase 1: Laden aller Spezifikationen

Lies **alle** folgenden Dateien vollständig:

| Datei                      | Inhalt                                 |
|----------------------------|----------------------------------------|
| `specs/UEBERSICHT.md`      | Architekturübersicht                   |
| `specs/FREIBERUFLER.md`    | Freiberufler-Formular                  |
| `specs/PARTNER.md`         | Partner-Formular                       |
| `specs/KUNDEN.md`          | Kunden-Formular                        |
| `specs/PROJEKTE.md`        | Projekt-Formular                       |
| `specs/PROFILSUCHE.md`     | KI-Profilsuche                         |
| `specs/STAMMDATEN.md`      | Konfigurationsdaten                    |
| `specs/UI-DESIGNSYSTEM.md` | Design-System                          |
| `prototype/base.css`       | CSS-Implementierung des Design-Systems |

### Phase 2: Analyse

Prüfe systematisch alle folgenden Kategorien:

#### A) Interne Konsistenz (je Spec)
- Sind alle erwähnten Felder in den Datenbanktabellen definiert?
- Sind alle DB-Constraints (FK, UNIQUE, NOT NULL) vollständig und korrekt?
- Stimmen UI-Beschreibung und DB-Struktur überein?
- Sind alle Workflows (Anlegen, Bearbeiten, Löschen) vollständig beschrieben?
- Gibt es Widersprüche zwischen verschiedenen Abschnitten derselben Spec?

#### B) Cross-Spec-Konsistenz
- Stimmen FK-Referenzen zwischen Specs überein (z. B. `project_position.project_id` → `projekte`)?
- Sind Entitätsbeziehungen (1:n, n:m) konsistent beschrieben?
- Stimmen Löschverhalten-Regeln (RESTRICT/CASCADE/SET NULL) in allen betroffenen Specs überein?
- Wird das „Gemerktes Projekt"-Konzept in allen relevanten Modulen korrekt referenziert?
- Sind Toolbar-Elemente für alle Formulare konsistent beschrieben?

#### C) Design-System-Konformität
- Werden alle UI-Komponenten aus dem Design-System (UI-DESIGNSYSTEM.md / base.css) korrekt eingesetzt?
- Sind Klassen und Komponenten-Bezeichnungen konsistent (z. B. `.fcard`, `.fg`, `.fg-readonly`, `.badge-dyn`)?
- Werden Formulare nach dem 2-Spalten-Grid-Prinzip aus dem Design-System aufgebaut?
- Werden Modale korrekt spezifiziert (`.modal-overlay` / `.modal-box` / `.modal-footer`)?
- Ist das 3-Ebenen-Layout (App-Nav → Toolbar → Inhalt) in allen Modulen eingehalten?

#### D) Logikfehler
- Gibt es zirkuläre Abhängigkeiten oder unmögliche Zustände?
- Sind Status-Übergänge vollständig und korrekt (z. B. Projektstatus)?
- Wird Optimistic Locking (`db_version`) überall korrekt behandelt?
- Können referenzierte Datensätze gelöscht worden sein, wenn darauf zugegriffen wird?
- Gibt es Race Conditions in beschriebenen Workflows?

#### E) UX-Schwachstellen
- Gibt es Workflows, die unnötig viele Schritte erfordern?
- Fehlen Bestätigungs- oder Fehlerdialoge für kritische Aktionen?
- Sind alle Fehlerzustände (leere Suche, keine Ergebnisse, Netzwerkfehler) abgedeckt?
- Gibt es Sackgassen in der Navigation (kein Weg zurück)?
- Sind Ladezeiten und Infinite-Scrolling-Verhalten vollständig spezifiziert?
- Sind alle Links (FK-Felder als `.fg-readonly`) korrekt als anklickbar beschrieben?

#### F) Offene Punkte
- Welche „Offene Punkte"-Abschnitte existieren noch?
- Sind diese wirklich noch offen oder durch andere Specs bereits implizit beantwortet?

### Phase 3: Ergebnisse strukturiert ausgeben

Gib die Ergebnisse in folgender Struktur aus:

```
## Spec-Review Ergebnis — [Datum]

### Kritische Fehler (müssen behoben werden)
[nummerierte Liste]

### Inkonsistenzen (zwischen Specs)
[nummerierte Liste]

### Design-System-Abweichungen
[nummerierte Liste]

### UX-Schwachstellen
[nummerierte Liste]

### Offene Punkte (noch unentschieden)
[nummerierte Liste]

### Empfehlungen (optional, Verbesserungen)
[nummerierte Liste]
```

Für jeden Punkt:
- **Wo**: Datei + Abschnitt (z. B. `PROJEKTE.md → Datenbankstruktur`)
- **Problem**: Klare Beschreibung des Fehlers/der Inkonsistenz
- **Vorschlag**: Konkrete Korrekturempfehlung

### Phase 4: Interaktive Korrektur

Nach der Ausgabe aller Befunde fragst du den Anwender:

> „Welche Punkte soll ich korrigieren? Du kannst einzelne Nummern nennen (z. B. '1, 3, 5'), einen Bereich ('1-5'), alle kritischen Fehler ('kritisch'), alle ('alle') oder einzelne Kategorien ('inkonsistenzen', 'ux')."

Für jeden zu korrigierenden Punkt:
1. Zeige die geplante Änderung als Beschreibung
2. Frage nach Bestätigung: „Soll ich diese Änderung vornehmen? (j/n/anpassen)"
3. Bei „j": Führe die Änderung aus — **ausschließlich in `specs/*.md`** (Edit-Tool); `prototype/base.css` nur bei expliziten Design-System-Korrekturen
4. Bei „anpassen": Nimm das Anwender-Feedback entgegen und passe den Vorschlag an
5. Bei „n": Überspringe und weiter

Nach allen Korrekturen:
> „Review abgeschlossen. [X] Punkte korrigiert, [Y] übersprungen. Für eine vollautomatische Folgeprüfung steht `/spec-workflow` bereit, für die Implementierung `/implement`."

## Wichtige Projektregeln (für die Prüfung)

- **Projekte**: Können nur aus Kunden- oder Partner-Formular angelegt werden; nie direkt
- **Projekt → Kunde/Partner**: Exklusiv (entweder oder, nie beide); unveränderlich nach Anlage
- **Freiberufler löschen**: Nur wenn keine `project_position`-Einträge existieren (RESTRICT)
- **Kunde/Partner löschen**: Nur wenn keine Projekte existieren (RESTRICT)
- **Partner löschen**: `freelancer.partner_id` → NULL (SET NULL); Freiberufler bleiben erhalten
- **Projekt löschen**: Kaskadiert zu `project_history`, `project_position`, `profile_search_chat`
- **`project_position_status` löschen**: Nur DB-Admin; kein UI
- **Gemerktes Projekt**: Server-seitig, pro Sachbearbeiter persistent; gesetzt durch Projekte-Formular UND Chat-Auswahl in Profilsuche
- **Chat-Kontext**: Live aus DB bei jeder Anfrage; enthält Projektdaten + zugeordnete Freiberufler (Skills, Tags, Status, Konditionen, Kommentar)
- **Freelancer-Links im Chat**: Format `[freelancer:<id>:<text>]` → öffnet in neuem Tab
- **QBE-Prinzip**: Leeres Formular = Suchmaske; AND-verknüpfte LIKE-Suche; kein expliziter Moduswechsel
- **Optimistic Locking**: `db_version` bei allen Entitäten; Konfliktdialog mit Entscheidung
- **Infinite Scrolling**: Initiale 20 Einträge, automatisches Nachladen
- **Design-System**: Kein externes Framework; nur `base.css` + HTML-Musterkatalog