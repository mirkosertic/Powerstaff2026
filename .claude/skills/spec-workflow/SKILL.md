---
name: spec-workflow
description: Vollautomatischer Multi-Agenten-Workflow für Powerstaff-2026-Spezifikationsarbeit. Startet Logic-Agent, UX-Agent, Reviewer-Agent und Implementer-Agent in koordinierten Iterationen. Läuft bis alle Probleme behoben sind, maximal 3 Iterationen. Abschlussbericht am Ende. Aktivieren mit /spec-workflow.
---

# Skill: Multi-Agenten-Spezifikations-Workflow (Powerstaff 2026)

Du bist der **Orchestrator** dieses Workflows. Du koordinierst vier spezialisierte Subagenten über mehrere Iterationen und trägst alle Ergebnisse zusammen.

---

## Grundregel: Single Source of Truth

Die **`specs/`-Dateien** sind die alleinige Quelle der Wahrheit für dieses System:

- `prototype/base.css` **implementiert** `specs/UI-DESIGNSYSTEM.md` — nicht umgekehrt
- Bei Konflikten zwischen Spec und CSS/Prototype gilt **immer die Spec**
- Der **Implementer-Agent** ändert **ausschließlich `specs/*.md`** (und ggf. `prototype/base.css` nur bei expliziten Design-System-Korrekturen, niemals andere Dateien)
- Code-Dateien (HTML, JS) sind nicht Gegenstand dieses Workflows

---

## Spezifikationsdateien

| Datei                      | Domäne                             | Primär für  |
|----------------------------|------------------------------------|-------------|
| `specs/UEBERSICHT.md`      | Architekturübersicht               | Cross-Check |
| `specs/FREIBERUFLER.md`    | Freiberufler-Formular & DB         | Logic + UX  |
| `specs/PARTNER.md`         | Partner-Formular & DB              | Logic + UX  |
| `specs/KUNDEN.md`          | Kunden-Formular & DB               | Logic + UX  |
| `specs/PROJEKTE.md`        | Projekt-Formular & DB              | Logic + UX  |
| `specs/PROFILSUCHE.md`     | KI-Profilsuche & Chat-UI           | Logic + UX  |
| `specs/STAMMDATEN.md`      | Konfigurationstabellen & Typen     | Logic       |
| `specs/UI-DESIGNSYSTEM.md` | Design-System-Regeln & Komponenten | UX          |
| `prototype/base.css`       | CSS-Implementierung (abgeleitet)   | UX          |

---

## Agenten-Rollen

| Agent                 | Subagent-Typ      | Aufgabe                                                                       | Lese-/Schreibrecht     |
|-----------------------|-------------------|-------------------------------------------------------------------------------|------------------------|
| **Logic-Agent**       | `Explore`         | Prüft Datenbankstruktur, FK-Integrität, Geschäftslogik, Cross-Spec-Konsistenz | Nur lesen              |
| **UX-Agent**          | `Explore`         | Prüft Design-System-Konformität, Workflows, Barrierefreiheit, Modulkonsistenz | Nur lesen              |
| **Reviewer-Agent**    | `general-purpose` | Konsolidiert Befunde, priorisiert, erstellt Korrekturenanweisungen            | Nur lesen              |
| **Implementer-Agent** | `general-purpose` | Setzt Korrekturen in `specs/*.md` um (minimal, präzise)                       | Schreiben (nur specs/) |

**Wichtig zur Architektur**: Agenten kommunizieren nicht direkt miteinander. Du (der Orchestrator) nimmst die Ergebnisse entgegen und gibst sie als Kontext an den nächsten Agenten weiter.

---

## Workflow-Ablauf

```
Start
  │
  ▼
Iteration 1..3
  │
  ├──[PARALLEL]──────────────────────────────────────┐
  │  Logic-Agent                    UX-Agent          │
  │  (liest alle specs)             (liest specs+css)  │
  └──────────────────────────────────────────────────┘
  │         Befunde sammeln
  ▼
Reviewer-Agent
(konsolidiert, priorisiert, erstellt Korrekturenanweisungen)
  │
  ├── Keine Probleme? ──► FERTIG → Abschlussbericht
  │
  ├── Iteration < 3? ──► Implementer-Agent
  │                       (ändert nur specs/*.md)
  │                       └──► nächste Iteration
  │
  └── Iteration = 3 ──► Abschlussbericht
                         (auch wenn Probleme verbleiben)
```

---

## Orchestrator-Schritte

### Schritt 0 — Initialisierung

Gib aus:
```
╔══════════════════════════════════════════════════════╗
║  Powerstaff 2026 — Spezifikations-Workflow gestartet ║
╚══════════════════════════════════════════════════════╝
Iteration 1/3 beginnt…
```

Initialisiere intern (verwalte im Gesprächskontext):
- `iteration = 1`
- `allFindings = []` — gesammelte Befunde aller Iterationen
- `allFixes = []` — vorgenommene Korrekturen aller Iterationen
- `workflowDone = false`

---

### Schritt 1 — Logic-Agent und UX-Agent parallel starten

Starte **beide Agenten in einem einzigen Schritt** (zwei parallele Agent-Tool-Aufrufe im selben Message).

**Logic-Agent** (`subagent_type: Explore`) erhält diesen Prompt:

> Du bist ein Spezialist für Datenbankintegrität und Systemlogik. Lies vollständig alle Dateien in `specs/` (UEBERSICHT.md, FREIBERUFLER.md, PARTNER.md, KUNDEN.md, PROJEKTE.md, PROFILSUCHE.md, STAMMDATEN.md, UI-DESIGNSYSTEM.md). Prüfe systematisch:
>
> **A — Interne Konsistenz (je Spec):**
> - Alle im Text erwähnten Felder in DB-Tabellen definiert?
> - FK-Constraints korrekt (referenzierte Tabelle/Spalte existiert)?
> - UNIQUE, NOT NULL, CHECK-Constraints vollständig und korrekt?
> - UI-Beschreibung stimmt mit DB-Struktur überein?
> - Workflows (Anlegen, Bearbeiten, Löschen) vollständig ohne Lücken?
>
> **B — Cross-Spec-Konsistenz:**
> - FK-Referenzen zwischen Specs konsistent?
> - Löschverhalten (RESTRICT/CASCADE/SET NULL) in allen betroffenen Specs übereinstimmend?
> - „Gemerktes Projekt"-Konzept korrekt und konsistent in allen relevanten Specs beschrieben?
> - UEBERSICHT.md deckt sich mit Detailspecs?
>
> **C — Systemlogik:**
> - Exklusivität Kunde/Partner pro Projekt in allen Szenarien einhaltbar?
> - Zirkuläre Abhängigkeiten?
> - Projektstatus-Übergänge vollständig und korrekt?
> - Race Conditions in beschriebenen Workflows?
> - Optimistic Locking (db_version) überall erwähnt wo nötig?
>
> **Bekannte Kernregeln validieren:**
> - Projekte nur aus Kunden-/Partner-Formular anlegbar (nie direkt)
> - Projekt → Kunde/Partner: exklusiv, nach Anlage unveränderlich
> - Freiberufler löschen: RESTRICT wenn project_position-Einträge existieren
> - Kunde/Partner löschen: RESTRICT wenn Projekte existieren
> - Partner löschen: freelancer.partner_id → SET NULL
> - Projekt löschen: CASCADE auf project_history, project_position, profile_search_chat
> - project_position_status löschen: nur DB-Admin, kein UI
> - Chat-Kontext: live aus DB bei jeder Anfrage (nicht einmalig bei Sitzungsstart)
> - changedDate: aktualisiert bei jeder Chat-Interaktion, NICHT bei externen Datenänderungen
>
> **Ausgabeformat (strikt einhalten):**
> ```
> ## LOGIC-AGENT-BEFUNDE
>
> ### [L-KRITISCH] Kritische Logikfehler
> L1 | Datei/Abschnitt | Problem | Korrekturvorschlag
> ...
>
> ### [L-INKONSISTENZ] Inkonsistenzen zwischen Specs
> L10 | betroffene Dateien | Widerspruch | Auflösungsvorschlag
> ...
>
> ### [L-LUECKE] Fehlende/unvollständige Spezifikationen
> L20 | Datei | Was fehlt | Empfehlung
> ...
>
> ### [L-OK] Geprüfte Bereiche ohne Befund
> [Liste der problemfreien Bereiche]
> ```

**UX-Agent** (`subagent_type: Explore`) erhält diesen Prompt:

> Du bist ein Spezialist für User Experience und Design-System-Konformität. Lies vollständig: alle Dateien in `specs/` und `prototype/base.css`. Prüfe systematisch:
>
> **A — Design-System-Konformität:**
> - UI-Komponenten korrekt aus dem Design-System eingesetzt?
> - CSS-Klassen konsistent: .fcard, .fg, .fg-label, .fg-input, .fg-readonly, .fg-full, .badge-dyn, .modal-overlay, .modal-box, .modal-header, .modal-footer, .modal-close?
> - 3-Ebenen-Layout (App-Nav → Toolbar → Inhalt) in allen Modulen beschrieben?
> - FK-Felder als .fg-readonly (anklickbare Links) — niemals als input readonly?
> - Dynamische Status-Badges als .badge-dyn mit inline-style (Farben aus DB)?
> - Profilsuche korrekt als #chat-page (kein form-grid)?
> - Design-System-Beschreibungen in UI-DESIGNSYSTEM.md mit base.css konsistent?
>
> **B — UX-Workflows:**
> - Unnötig viele Schritte in Workflows?
> - Bestätigungs- und Fehlerdialoge für alle kritischen Aktionen vorhanden?
> - Fehlerzustände abgedeckt: leere Suche, keine Ergebnisse, Netzwerkfehler, Optimistic-Locking-Konflikt?
> - Navigationssackgassen (kein Weg zurück)?
> - „Gemerktes Projekt"-Workflow schlüssig: Profilsuche-Chat-Auswahl setzt Projekt?
> - Chat-Workflow vollständig: Sitzungsauswahl → Projektbindung → Freiberufler-Link → neuer Tab → Zuordnen?
> - Infinite Scrolling (20 Einträge initial) konsistent in allen Listendarstellungen?
>
> **C — Barrierefreiheit:**
> - Pflichtfelder mit `<span class="required">*</span>` markiert?
> - Modale mit Fokus-Management und Schließen-Button beschrieben?
> - WCAG AA (≥4,5:1 Kontrast) als Designregel erwähnt?
>
> **D — Modulübergreifende Konsistenz:**
> - Toolbar-Elemente in allen Formularen konsistent (inkl. Gemerktes-Projekt-Pill)?
> - Modale in allen Modulen gleich beschrieben?
> - QBE-Prinzip (leeres Formular = Suchmaske) konsistent?
>
> **Ausgabeformat (strikt einhalten):**
> ```
> ## UX-AGENT-BEFUNDE
>
> ### [U-DESIGN] Design-System-Abweichungen
> U1 | Datei/Abschnitt | Abweichung | Korrekturvorschlag
> ...
>
> ### [U-UX] UX-Schwachstellen
> U10 | Datei/Abschnitt | Problem | Verbesserungsvorschlag
> ...
>
> ### [U-A11Y] Barrierefreiheits-Lücken
> U20 | Datei/Abschnitt | Problem | Lösung
> ...
>
> ### [U-KONSISTENZ] Modulübergreifende Inkonsistenzen
> U30 | betroffene Dateien | Unterschied | Empfehlung
> ...
>
> ### [U-OK] Geprüfte Bereiche ohne Befund
> [Liste der problemfreien Bereiche]
> ```

---

### Schritt 2 — Reviewer-Agent

Warte bis beide Agenten fertig sind. Starte dann den Reviewer-Agent (`subagent_type: general-purpose`) mit den **vollständigen Befunden beider Agenten** sowie der Liste bisheriger Korrekturen (`allFixes`):

> Du bist der Qualitäts-Reviewer des Powerstaff-2026-Spezifikations-Workflows. Du hast folgende Befunde erhalten:
>
> [LOGIC-AGENT-BEFUNDE einfügen]
>
> [UX-AGENT-BEFUNDE einfügen]
>
> Bisherige Korrekturen aus früheren Iterationen:
> [allFixes einfügen oder "Keine (erste Iteration)"]
>
> Deine Aufgaben:
> 1. **Konsolidiere** alle Befunde — entferne Duplikate, fasse verwandte Punkte zusammen
> 2. **Prüfe** ob bisherige Korrekturen neue Probleme eingeführt haben
> 3. **Priorisiere** nach Kritikalität:
>    - KRITISCH: Logikfehler die Systemfunktion brechen oder Datenverlust riskieren
>    - HOCH: Inkonsistenzen zwischen Specs die zu widersprüchlichem Verhalten führen
>    - MITTEL: UX-Schwachstellen, Design-Abweichungen
>    - NIEDRIG: Stilfragen, optionale Verbesserungen
> 4. **Entscheide**: Gibt es noch behebbare Probleme (KRITISCH oder HOCH)?
> 5. **Erstelle** für jedes zu behebende Problem eine präzise Korrekturenanweisung für den Implementer-Agent
>
> Ausgabeformat:
> ```
> ## REVIEWER-BEFUNDE — Iteration [N]
>
> ### STATUS
> [PROBLEME VORHANDEN — Implementierung erforderlich]
> ODER
> [KEINE BEHEBUNGSPFLICHTIGEN PROBLEME — Workflow kann beendet werden]
>
> ### Konsolidierte Befundliste
>
> #### KRITISCH
> R1 | Quelle: L3/U7 | Datei | Problem | Korrekturanweisung für Implementer
>
> #### HOCH
> R5 | ...
>
> #### MITTEL
> R10 | ...
>
> #### NIEDRIG (optional)
> R15 | ...
>
> ### Bewertung bisheriger Korrekturen
> [Erfolgreich / Hat neue Probleme eingeführt: ...]
>
> ### Implementer-Anweisungen (geordnet nach Priorität)
> I1: [Datei] [Abschnitt] [Exakte Beschreibung der Änderung]
> I2: ...
> ```

---

### Schritt 3 — Entscheidung

- Reviewer meldet `KEINE BEHEBUNGSPFLICHTIGEN PROBLEME` → `workflowDone = true` → gehe zu Schritt 5
- Reviewer meldet `PROBLEME VORHANDEN` UND `iteration < 3` → gehe zu Schritt 4
- `iteration == 3` → `workflowDone = false` (Probleme verbleiben) → gehe zu Schritt 5

---

### Schritt 4 — Implementer-Agent

Starte den Implementer-Agent (`subagent_type: general-purpose`) mit den Implementer-Anweisungen vom Reviewer:

> Du bist der Spec-Implementer des Powerstaff-2026-Spezifikations-Workflows.
>
> **GRUNDREGEL — Single Source of Truth:**
> Die Spezifikationsdateien in `specs/` sind die alleinige Quelle der Wahrheit.
> Du änderst AUSSCHLIESSLICH Dateien in `specs/` (und prototype/base.css NUR wenn explizit angewiesen).
> Du machst KEINE ungefragten Verbesserungen über die Anweisungsliste hinaus.
> Du änderst KEINE Code-Dateien (HTML, JS, etc.).
>
> **Implementier-Anweisungen:**
> [Reviewer-Anweisungen I1..IN einfügen]
>
> **Vorgehen für jede Anweisung:**
> 1. Lies den betroffenen Abschnitt (Read-Tool)
> 2. Setze die Änderung minimal und präzise um (Edit-Tool)
> 3. Dokumentiere was du geändert hast
>
> **Ausgabeformat:**
> ```
> ## IMPLEMENTER-BERICHT — Iteration [N]
>
> ### Vorgenommene Korrekturen
> I1 ✓ | [Datei] | [Abschnitt] | [Was wurde geändert]
> I2 ✓ | ...
>
> ### Nicht umgesetzte Anweisungen
> I5 ✗ | [Grund: z. B. Anweisung unklar / Abschnitt nicht gefunden]
>
> ### Hinweise
> [Auffälligkeiten die dem Reviewer gemeldet werden sollten]
> ```

Sammle alle vorgenommenen Änderungen in `allFixes`. Erhöhe `iteration` um 1. Gehe zurück zu Schritt 1.

---

### Schritt 5 — Abschlussbericht

Gib den vollständigen Abschlussbericht aus:

```
╔══════════════════════════════════════════════════════╗
║  Powerstaff 2026 — Spezifikations-Workflow Abschluss ║
╚══════════════════════════════════════════════════════╝

Datum: [aktuelles Datum]
Durchgeführte Iterationen: [N]/3
Ergebnis: [ALLE PROBLEME BEHOBEN / MAX. ITERATIONEN ERREICHT / KEINE PROBLEME GEFUNDEN]

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## Behobene Probleme

### Iteration 1
[Liste aller Fixes mit ID und Beschreibung]

### Iteration 2 (falls durchgeführt)
[...]

### Iteration 3 (falls durchgeführt)
[...]

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## Verbleibende offene Punkte

[Nur wenn workflowDone = false]
[ID | Datei | Problem | Warum nicht behoben]
[oder: "Keine — alle identifizierten Probleme wurden behoben."]

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## Empfehlungen für manuelle Nacharbeit

[Falls verbleibende Punkte vorhanden: konkrete Handlungsempfehlungen]
[Falls alles behoben: "Spezifikationen sind konsistent. Nächster Schritt: /implement-form für Formularimplementierung."]

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## Gesamtstatistik
- Befunde Logic-Agent gesamt: [N]
- Befunde UX-Agent gesamt: [N]
- Davon behoben: [N]
- Verbleibend: [N]
```

---

## Zusammenspiel mit anderen Skills

| Situation | Empfohlener Skill |
|---|---|
| Vollautomatische Prüfung + Korrektur (dieser Skill) | `/spec-workflow` |
| Schnelle manuelle Prüfung mit interaktiver Korrektur | `/spec-review` |
| Vollständige Feature-Implementierung (alle Domains) | `/implement` |
| Nur Backend-Implementierung | `/implement-backend` |
| Nur Thymeleaf-Templates / Custom Elements | `/implement-frontend` |
| Nur Flyway-Migration / DB-Schema | `/implement-db` |
| Nur Tests | `/implement-tests` |
| HTML-Prototyp für Design-System-Validierung | `/implement-form` |

**Reihenfolge für neue Features:**
1. Modul-Spec schreiben/erweitern
2. `/spec-workflow` — automatisch validieren und bereinigen
3. `/implement` — vollständige Implementierung mit parallelen Domain-Agenten und Review-Loop
