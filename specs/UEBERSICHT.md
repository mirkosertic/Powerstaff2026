# Powerstaff 2026 – Architekturübersicht

**Stand:** März 2026
**Zweck:** Management-taugliche Übersicht über fachliche Motivation, Systemstruktur und Besonderheiten.
**Softwarearchitektur:** [SWARCHITEKTUR.md](SWARCHITEKTUR.md)
**Detailspezifikationen:** [FREIBERUFLER.md](FREIBERUFLER.md) · [PARTNER.md](PARTNER.md) · [KUNDEN.md](KUNDEN.md) · [PROJEKTE.md](PROJEKTE.md) · [PROFILSUCHE.md](PROFILSUCHE.md) · [STAMMDATEN.md](STAMMDATEN.md) · [UI-DESIGNSYSTEM.md](UI-DESIGNSYSTEM.md)

---

## Fachliche Motivation

Powerstaff 2026 ist ein webbasiertes **Personalvermittlungs-Managementsystem** für Unternehmen, die selbstständige Freiberufler an Kundenprojekte vermitteln. Es löst das zentrale operative Problem dieser Branche: die effiziente und nachvollziehbare Zuordnung von Freiberuflerprofilen zu konkreten Projektanforderungen.

Die Kernaufgabe der Sachbearbeiter ist die tägliche Arbeit mit vier Entitäten:

- **Freiberufler** – die zu vermittelnden Spezialisten mit Profilen, Skills und Konditionen
- **Partner** – Vermittlungsagenturen, die eigene Freiberufler mitbringen
- **Kunden** – Unternehmen, die Projekte ausschreiben
- **Projekte** – konkrete Anfragen oder laufende Aufträge, für die Freiberufler gesucht werden

Das System unterstützt den gesamten Prozess: von der Erfassung und Pflege der Stammdaten über die strukturierte Kontakthistorie bis hin zur Besetzung von Projekten. Als moderne Erweiterung bietet Powerstaff 2026 eine **KI-gestützte Profilsuche**, die den zeitaufwändigsten Schritt – das Finden passender Freiberufler für ein Projekt – durch konversationelle Suche beschleunigt.

---

## Fachliche Struktur

### Entitäten und ihre Beziehungen

```
┌──────────────┐        ┌──────────────┐
│   Partner    │        │    Kunden    │
│              │        │              │
│ ▸ Stammdaten │        │ ▸ Stammdaten │
│ ▸ Kontakte   │        │ ▸ Kontakte   │
│ ▸ Historie   │        │ ▸ Historie   │
└──────┬───────┘        └──────┬───────┘
       │ 1:n                   │ 1:n
       │ Projekte              │ Projekte
       └──────────┬────────────┘
                  ▼
         ┌────────────────┐
         │    Projekte    │
         │                │
         │ ▸ Stammdaten   │
         │ ▸ Historie     │
         │ ▸ Positionen   │◄──── project_position
         └────────────────┘           │ n:m
                                      │
                  ┌───────────────────┘
                  ▼
         ┌────────────────┐
         │  Freiberufler  │
         │                │
         │ ▸ Stammdaten   │
         │ ▸ Kontakte     │
         │ ▸ Historie     │
         │ ▸ Tags         │
         └────────┬───────┘
                  │ n:1
                  │ (optional)
                  ▼
         ┌────────────────┐
         │    Partner     │
         └────────────────┘
```

Ein **Projekt** gehört entweder einem Kunden oder einem Partner – niemals beiden gleichzeitig. Die Zuordnung wird bei der Anlage festgelegt und ist danach nicht mehr änderbar, um die Datenintegrität zu gewährleisten.

Ein **Freiberufler** kann optional einem Partner zugeordnet sein. Diese Zuordnung wird ausschließlich vom Partner-Formular verwaltet.

Die **Projektposition** (`project_position`) ist die zentrale Vermittlungsbeziehung: Sie verbindet einen Freiberufler mit einem Projekt und trägt Konditionen, Kommentar und einen konfigurierbaren Farbstatus (z. B. „Vorgeschlagen", „Im Gespräch", „Besetzt").

---

## Die fünf Module

### 1. Freiberuflerverwaltung
→ [FREIBERUFLER.md](FREIBERUFLER.md)

Das Herzstück des Systems. Erfasst vollständige Profile mit Adresse, Kontaktmöglichkeiten, Skills, Verfügbarkeit, Stundensätzen und einem flexiblen Tag-System (Schwerpunkt, Funktion, Einsatzort, Bemerkung, Typ). Eine typisierte **Kontakthistorie** protokolliert alle Interaktionen mit dem Freiberufler.

Die **Kodierung** (`code`) ist ein fachlich eindeutiger, anonymisierter Bezeichner für den Freiberufler – er wird in der Kundenkommunikation und in Profilen ohne Namensnennung verwendet und dient als Schlüssel für die Partner-Zuordnung.

Die Zuordnung eines Freiberuflers zu einem Projekt erfolgt über das **„Gemerktes Projekt"**-Konzept (siehe unten).

### 2. Partnerverwaltung
→ [PARTNER.md](PARTNER.md)

Partner sind Vermittlungsagenturen, die eine Gruppe von Freiberuflern repräsentieren. Das Partner-Formular verwaltet neben Stammdaten und Kontakthistorie auch die Liste der zugeordneten Freiberufler direkt: neue Freiberufler können angelegt, bestehende per Kodierung zugeordnet und Zuordnungen aufgelöst werden. Partner können ebenfalls Projekte initiieren.

### 3. Kundenverwaltung
→ [KUNDEN.md](KUNDEN.md)

Kunden sind die Auftraggeber. Sie haben keine direkte Beziehung zu Freiberuflern – die Verbindung entsteht ausschließlich über Projekte. Das Formular ist strukturell analog zum Partner-Formular, jedoch ohne die Freiberufler-Verwaltungsfunktionen.

### 4. Projektverwaltung
→ [PROJEKTE.md](PROJEKTE.md)

Projekte können **ausschließlich aus dem Kunden- oder Partner-Formular heraus** angelegt werden – nie direkt über das Projekt-Formular. Damit ist der Projektkunde bzw. -partner von Anfang an eindeutig festgelegt.

Das Projekt-Formular zeigt alle zugeordneten Freiberufler mit ihren Positionsstatus-Badges und ermöglicht die Pflege der Zuordnungsmerkmale. Gelöscht werden darf ein Projekt jederzeit; dabei werden Projekthistorie, Positionen und verknüpfte Chat-Sitzungen der Profilsuche kaskadierend mitgelöscht.

**Projektstatus** (fest kodiert): Offen → In Bearbeitung → Besetzt → Abgeschlossen / Storniert.

### 5. Profilsuche (KI-gestützt)
→ [PROFILSUCHE.md](PROFILSUCHE.md)

Das innovative Kernstück von Powerstaff 2026: eine konversationelle Suchoberfläche im Stil moderner KI-Assistenten (Claude, ChatGPT). Der Sachbearbeiter beschreibt in natürlicher Sprache, welches Profil er sucht – das System antwortet mit passenden Freiberufler-Vorschlägen als anklickbare Links, die den Datensatz direkt in einem neuen Tab öffnen.

Besonderheiten sind nachfolgend ausführlich beschrieben.

---

## Modulübergreifendes Konzept: „Gemerktes Projekt"

Das **„Gemerktes Projekt"**-Konzept ist der zentrale Workflow-Beschleuniger der Applikation. Es löst das Problem, wie ein Sachbearbeiter einen Freiberufler ohne umständliche Navigation einem konkreten Projekt zuweisen kann.

**Mechanismus:**
- Jedes Mal, wenn ein Sachbearbeiter ein Projekt im Projekte-Formular aufruft oder in der Profilsuche einen Chat mit Projektbezug öffnet, wird dieses Projekt **server-seitig, persistent und pro Sachbearbeiter** als „gemerktes Projekt" gespeichert.
- Das gemerkte Projekt wird in der **Toolbar aller Formulare** (Freiberufler, Partner, Kunden, Profilsuche) als kompaktes Pill-Element angezeigt.

**Zuordnungs-Workflow:**
1. Sachbearbeiter öffnet ein Projekt (Projekte-Formular oder Profilsuche-Chat)
2. Projekt wird automatisch „gemerkt" und in allen Toolbars sichtbar
3. Im **Freiberufler-Formular** erscheint zusätzlich der Button „Dem Projekt [Nr.] zuordnen"
4. Ein Klick öffnet ein Modal mit Status (Pflicht), Konditionen und Kommentar
5. Die Projektposition wird angelegt – ohne dass der Sachbearbeiter das Formular gewechselt hat

Dieses Konzept ermöglicht einen durchgängigen Arbeitsfluss: Profilsuche → Profil prüfen (neuer Tab) → Zuordnen – alles ohne Navigation durch mehrere Formulare.

---

## Das KI-System: Profilsuche im Detail
→ [PROFILSUCHE.md](PROFILSUCHE.md)

### Architektur

Die Profilsuche ist bewusst als **UI- und Persistenz-Schicht** konzipiert. Die eigentliche KI-Logik (Modellanbindung, MCP-Tooling-Interface) ist entkoppelt und nicht Bestandteil dieser Spezifikation. Powerstaff stellt bereit:

- Eine **Chat-UI** (Split-Panel: Sidebar + Nachrichtenbereich + Eingabefeld)
- Eine **Persistenzschicht** für Chat-Sitzungen (`profile_search_chat`) und Nachrichten (`profile_search_message`)
- Einen **dynamischen Kontextmechanismus**, der bei jeder Nutzernachricht frisch aus der Datenbank zusammengestellt wird

### Dynamischer Projektkontext

Der Kontext, den das KI-System bei jeder Anfrage erhält, umfasst – sofern ein Projekt gemerkt ist:

| Bereich                          | Felder                                                                                              |
|----------------------------------|-----------------------------------------------------------------------------------------------------|
| Projektdaten                     | Projektnummer, Kurz- und Langbeschreibung, Einsatzort, Anforderungen, Laufzeit, Status, Stundensatz |
| Bereits zugeordnete Freiberufler | Kodierung, Name, Skills, Tags (nach Kategorie), Positionsstatus, Konditionen, Kommentar             |

Da sich Projektdaten und Freiberufler-Zuordnungen jederzeit ändern können, wird der Kontext **bei jeder Anfrage live aus der Datenbank gelesen** – nicht einmalig bei Sitzungsanlage fixiert. Das KI-System arbeitet damit immer auf dem aktuellen Stand.

### Chat-Sitzungen und Projektbindung

Jede Sitzung ist optional an ein Projekt gebunden. Die Sidebar zeigt den Projektbezug direkt an. Das Auswählen einer Sitzung in der Sidebar setzt gleichzeitig das zugehörige Projekt als „gemerktes Projekt" – die Profilsuche ist damit ein **zweiter Einstiegspunkt für die Projektauswahl** neben dem Projekte-Formular.

Freiberufler-Links in Chat-Antworten öffnen den Datensatz in einem **neuen Tab**, damit der Chat-Kontext erhalten bleibt und der Sachbearbeiter das gefundene Profil direkt dem gemerkten Projekt zuordnen kann.

---

## Gemeinsame Systemmerkmale

### Formularprinzip: QBE und Modalität
Alle CRUD-Formulare (Freiberufler, Partner, Kunden, Projekte) folgen demselben Prinzip: Ein **leeres Formular dient als QBE-Suchmaske** (Query By Example) – die eingetragenen Werte werden als UND-verknüpfte LIKE-Suche ausgeführt. Es gibt keinen expliziten Modus-Wechsel zwischen Suche und Neuanlage. Alle Unteroperationen (Kontakte, Historie, Zuordnungen) laufen über **modale Dialoge**.

### Optimistic Locking
Alle Entitäten werden über ein `db_version`-Feld gegen gleichzeitige Bearbeitung geschützt. Im Konfliktfall entscheidet der Sachbearbeiter, ob er die eigene Version durchsetzt oder den aktuellen Stand neu lädt.

### Kontakthistorie
Freiberufler, Partner und Kunden führen eine **typisierte Kontakthistorie** (Typ aus konfigurierbarer `historytype`-Tabelle). Projekte führen eine **typenlose Kontakthistorie** – bewusst schlanker gehalten, da Projekte kürzere Lebenszyklen haben.

### Datenbankintegrität
Zentrale Integritätsregeln — durchgängig nach dem Prinzip „Löschen verhindern statt still kaskadieren":

| Entität          | Bedingung                    | Verhalten                                                                                                                      |
|------------------|------------------------------|--------------------------------------------------------------------------------------------------------------------------------|
| **Freiberufler** | Hat aktive Projektpositionen | Löschen verhindert (RESTRICT); Fehlermeldung mit Links zu den blockierenden Projekten                                          |
| **Partner**      | Hat zugeordnete Projekte     | Löschen verhindert (RESTRICT); Fehlermeldung mit Links zu den blockierenden Projekten                                          |
| **Kunden**       | Hat zugeordnete Projekte     | Löschen verhindert (RESTRICT); Fehlermeldung mit Links zu den blockierenden Projekten                                          |
| **Partner**      | Hat zugeordnete Freiberufler | Freiberufler bleiben erhalten; `freelancer.partner_id` wird auf NULL gesetzt (SET NULL)                                        |
| **Projekt**      | Immer                        | Kaskadiert: Projekthistorie, Positionen, Chat-Sitzungen werden mitgelöscht; Freiberufler, Kunden und Partner bleiben unberührt |

### Infinite Scrolling
Suchergebnisse und Chat-Sitzungslisten werden initial mit 20 Einträgen geladen und bei Bedarf automatisch nachgeladen – ohne Seitenumbrüche.

---

## Design-System
→ [UI-DESIGNSYSTEM.md](UI-DESIGNSYSTEM.md) · [prototype/base.css](../prototype/base.css)

Powerstaff 2026 verwendet ein **einheitliches, eigenentwickeltes Design-System** ohne externe UI-Frameworks. Alle Formulare werden aus einer einzigen Stylesheet-Datei (`base.css`) und einem verbindlichen HTML-Musterkatalog aufgebaut.

### Kernprinzipien

| Prinzip            | Umsetzung                                                                                   |
|--------------------|---------------------------------------------------------------------------------------------|
| **Augenschonend**  | Entsättigte Palette, kein reines Weiß/Schwarz – optimiert für 6–8 Stunden Bildschirmarbeit  |
| **Business-first** | Gedämpftes Blau als Primärfarbe, keine Signalfarben                                         |
| **WCAG AA**        | Alle Text-/Hintergrund-Kombinationen mit ≥ 4,5:1 Kontrastverhältnis                         |
| **Konsistenz**     | Alle Module teilen dasselbe 3-Ebenen-Layout, dieselben Tokens, dieselben Interaktionsmuster |
| **Vanilla JS**     | Keine Frontend-Frameworks – maximale Kontrolle, minimale Abhängigkeiten                     |

### Layout-Hierarchie

```
App-Navigation (dunkel, sticky, z:110)
    └── Form-Toolbar (hell, sticky, z:100)  ← Modulname, Navigation, Aktionen, Gemerktes Projekt
            └── Seiteninhalt
                    ├── CRUD-Formulare: 2-Spalten-Grid aus Formular-Karten (.fcard)
                    └── Profilsuche: Split-Panel (Sidebar + Chat-Bereich)
```

### Besondere Komponenten

- **Dynamische Status-Badges** (`.badge-dyn`): Farben für Projektpositions-Status kommen aus der Datenbank und werden per inline-style gesetzt – ohne feste CSS-Klassen.
- **Read-only Link-Felder** (`.fg-readonly`): FK-Felder werden nie als `<input readonly>` dargestellt, sondern als stilisierter anklickbarer Link – barrierefrei und semantisch korrekt.
- **Chat-Layout** (`.chat-sidebar` / `#chat-messages`): Vollständig responsives Split-Panel-Layout für die Profilsuche mit kollabierender Sidebar, Nachrichten-Bubbles und wachsender Textarea.

---

## Offene Punkte (systemweit)

Die folgenden Punkte sind in den Einzelspezifikationen dokumentiert und noch nicht abschließend entschieden:

| Modul                         | Thema                                                                            |
|-------------------------------|----------------------------------------------------------------------------------|
| Freiberufler, Partner, Kunden | **Wiedervorlage (`showAgain`)**: Workflow und ggf. Datumsfeld fehlen noch        |
| Projekte                      | **Projektstatus-Bezeichnungen**: Abnahme der Statuswerte mit Anwendern steht aus |
