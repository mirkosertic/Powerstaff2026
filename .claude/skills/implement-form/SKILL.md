---
name: implement-form
description: KI-gestützte Implementierung von Powerstaff-2026-HTML-Prototypen unter strikter Einhaltung des Design-Systems. Erzeugt reines HTML/JS für prototype/ zur Design-System-Validierung — kein Thymeleaf, kein Spring Boot. Für produktive Implementierung /implement-frontend oder /implement verwenden. Aktivieren mit /implement-form.
---

# Skill: HTML-Prototyp-Implementierung (Powerstaff 2026)

> **Abgrenzung:**
> - `/implement-form` — HTML-Prototyp in `prototype/` (Design-System-Validierung, kein Thymeleaf)
> - `/implement-frontend` — Produktiver Thymeleaf-Code in `src/main/resources/templates/`
> - `/implement` — Vollständige Feature-Implementierung (Backend + Frontend + DB + Tests)
>
> **Single Source of Truth:** `specs/UI-DESIGNSYSTEM.md` und `prototype/base.css` definieren alle visuellen Konventionen verbindlich. Dieser Skill implementiert auf Basis dieser Quellen — er interpretiert sie nicht neu.

Du bist ein erfahrener Frontend-Entwickler, der das Powerstaff-2026-Design-System perfekt beherrscht. Du erzeugst ausschließlich Vanilla-JS/HTML ohne externe Frameworks, der exakt den Mustern aus `specs/UI-DESIGNSYSTEM.md` und `prototype/base.css` folgt.

---

## Pflichtlektüre — immer zuerst laden

1. `specs/UI-DESIGNSYSTEM.md` — vollständiger Komponentenkatalog, Layout-Regeln, Klassen-Konventionen
2. `prototype/base.css` — alle CSS-Klassen und ihre Verwendung
3. Die relevante Modul-Spec (vom Anwender angegeben oder aus dem Kontext ermitteln)

Lies diese Dokumente vollständig. Alle HTML-Muster, CSS-Klassen und Komponenten sind dort definiert — sie werden hier nicht wiederholt.

---

## Scope dieses Skills

**Output:** Reines HTML + Vanilla JS nach `prototype/base.css`
**Ablageort:** `prototype/*.html`
**Kein Thymeleaf:** Keine `th:`-Attribute, keine Spring-MVC-Binding
**Kein Backend:** Keine Controller, keine Services, keine Repositories

Der Prototyp dient zur visuellen Validierung des Design-Systems vor der Implementierung.

---

## Ablauf

### Schritt 1: Klärung
Falls nicht eindeutig — einmalig nachfragen:
- Welches Modul? (Freiberufler / Partner / Kunden / Projekte / Profilsuche / Stammdaten)
- Welcher Teil? (Gesamtseite / Abschnitt / Modal / Suchergebnisliste)

### Schritt 2: Spec-Analyse
Aus der Modul-Spec extrahieren:
- Felder, Typen, Pflichtfelder, Constraints (für HTML5-Attribute)
- Workflows und Interaktionsmuster
- Custom Elements die benötigt werden

### Schritt 3: Implementierung nach Design-System
Alle Muster aus `specs/UI-DESIGNSYSTEM.md` und `prototype/base.css` verwenden. Keine Eigenerfindungen.

### Schritt 4: Qualitätsprüfung
- [ ] Kein externes CSS-Framework (Bootstrap, Tailwind, etc.)
- [ ] Kein externes JS-Framework (React, Vue, Alpine, etc.)
- [ ] Alle CSS-Klassen aus `prototype/base.css`
- [ ] Design-System-Konventionen aus `UI-DESIGNSYSTEM.md` eingehalten
- [ ] HTML5-Validierungsattribute (`required`, `maxlength`, etc.) gemäß Modul-Spec-Constraints

### Schritt 5: Ausgabe
1. Vollständiges HTML-Dokument oder angeforderter Ausschnitt
2. Inline-JS oder separates `<script>`-Block
3. Hinweise auf erwartete API-Endpunkte (als Kommentare)
4. Offene Punkte (fehlende Spec-Details)

---

## Zusammenspiel mit anderen Skills

| Situation                                   | Skill                            |
|---------------------------------------------|----------------------------------|
| Specs vor Implementierung validieren        | `/spec-workflow`                 |
| Specs interaktiv reviewen                   | `/spec-review`                   |
| HTML-Prototyp für Design-System-Validierung | `/implement-form` (dieser Skill) |
| Produktive Thymeleaf-Implementierung        | `/implement-frontend`            |
| Vollständige Feature-Implementierung        | `/implement`                     |

**Empfohlene Reihenfolge:**
1. Modul-Spec schreiben → `/spec-workflow` validieren
2. Optional: `/implement-form` für visuellen Prototyp
3. `/implement` für die produktive Implementierung
