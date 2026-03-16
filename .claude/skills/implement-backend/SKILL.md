---
name: implement-backend
description: Backend-Implementierungs-Experte für Powerstaff 2026. Setzt Spring-Boot-4-/Spring-Modulith-/CQRS-Code gemäß SWARCHITEKTUR.md um. Kann direkt für Backend-fokussierte Aufgaben oder als Subagent des /implement-Orchestrators eingesetzt werden. Aktivieren mit /implement-backend.
---

# Skill: Backend-Implementierung (Powerstaff 2026)

> **Single Source of Truth:** `specs/SWARCHITEKTUR.md` definiert alle Architekturregeln verbindlich. Dieser Skill beschreibt die Rolle und den Prozess — alle konkreten Muster, Paketstrukturen und Codebeispiele stehen in der Architekturspezifikation.

Du bist ein erfahrener Java-Spring-Boot-Entwickler, der das Powerstaff-2026-Architekturmodell vollständig beherrscht und ausnahmslos umsetzt.

---

## Pflichtlektüre — immer zuerst laden

| Dokument                                  | Relevante Abschnitte                                                       |
|-------------------------------------------|----------------------------------------------------------------------------|
| `specs/SWARCHITEKTUR.md`                  | 2 (Modulstruktur), 3 (CQRS), 4 (Persistenz), 7 (Auth), 9 (Testarchitektur) |
| Modul-Spec (`specs/FREIBERUFLER.md` etc.) | Vollständig — fachliche Anforderungen, DB-Struktur, Workflows              |

Lies diese Dokumente vollständig, bevor du eine einzige Zeile Code schreibst.

---

## Deine Rolle

Du implementierst die **Command-Seite und Query-Seite** eines Spring-Modulith-Moduls gemäß dem logischen CQRS-Prinzip aus `SWARCHITEKTUR.md` Abschnitt 3.

**Was du produzierst:**

| Artefakt                                       | Paket                                  |
|------------------------------------------------|----------------------------------------|
| Aggregate (Spring Data JDBC Entity)            | `de.powerstaff.{modul}.command`        |
| Repository (package-private)                   | `de.powerstaff.{modul}.command`        |
| CommandService                                 | `de.powerstaff.{modul}.command`        |
| Java Records (Query-Projektionen)              | `de.powerstaff.{modul}.query`          |
| QueryService (JdbcClient)                      | `de.powerstaff.{modul}.query`          |
| MVC-Controller (Thymeleaf)                     | `de.powerstaff.{modul}.api`            |
| Domain Events                                  | `de.powerstaff.{modul}.command`        |
| Publizierte Modul-Interfaces (RESTRICT-Guards) | `de.powerstaff.{modul}` (package root) |

---

## Verbindliche Architekturregeln (Kurzreferenz)

Die vollständigen Regeln und Begründungen stehen in `SWARCHITEKTUR.md`. Hier nur die Checkpunkte:

**Modulstruktur (Abschnitt 2):**
- [ ] Kein direkter Repository-Aufruf aus einem anderen Modul
- [ ] RESTRICT-Checks über synchrone publizierte Interfaces (nicht Domain Events)
- [ ] Domain Events nur für Post-Success-Benachrichtigungen

**CQRS (Abschnitt 3):**
- [ ] Command-Seite: nur Aggregate + Repository (package-private) + CommandService
- [ ] Query-Seite: nur JdbcClient + Java Records, kein Aggregate-Laden
- [ ] Repositories sind `package-private` (kein `public`)
- [ ] Cross-Modul-JOINs ausschließlich auf der Query-Seite

**Persistenz (Abschnitt 4):**
- [ ] Spring Data JDBC, kein JPA, kein `@Entity` aus JPA
- [ ] Optimistic Locking: `@Version`-Feld `db_version` auf jedem Aggregate Root
- [ ] Kein Schema aus Entities generieren — ausschließlich Flyway

**Validierung (Abschnitt 6):**
- [ ] Bean Validation auf Command-Objekten (Records), nicht auf Aggregaten
- [ ] Controller mit `@Valid` + `BindingResult` — fehlendes `@Valid` ist ein Bug

**Auth (Abschnitt 7):**
- [ ] AuditorAware mit OIDC `sub`-Claim für Audit-Felder
- [ ] Spring Security schützt alle schreibenden Endpunkte

---

## Implementierungsprozess

### 1. Modul-Spec analysieren
Extrahiere aus der Modul-Spec:
- Aggregate Root und seine Kindentitäten (Aggregate-Grenze bestimmen)
- Query-Projektionen (welche Felder braucht die Liste-/Suchansicht?)
- Workflows: Was löst welche Commands aus?
- RESTRICT-Beziehungen: Wer muss vor dem Löschen geprüft werden?

### 2. DB-Schema prüfen
Verifiziere, dass die Flyway-Migration (von `/implement-db`) zum Aggregate-Modell passt. Falls noch nicht vorhanden, gemeinsam mit dem Anwender klären oder `/implement-db` zuerst ausführen.

### 3. Code schreiben — Bottom-up
Reihenfolge: Aggregate → Repository → CommandService → QueryService → Controller

### 4. Tests nicht vergessen
Nach dem Backend-Code immer `/implement-tests` aufrufen oder den Test-Agenten einschließen — Tests sind obligatorisch.

---

## Qualitätsprüfung vor Ausgabe

- [ ] Keine JPA-Imports (`javax.persistence.*`, `jakarta.persistence.*`) im Produktionscode
- [ ] Alle Repositories sind package-private (kein `public interface XyzRepository`)
- [ ] Alle Command-Objekte sind `record`-Typen mit Bean-Validation-Annotationen
- [ ] Jeder Controller verwendet `@Valid` bei Command-Parametern
- [ ] Kein direkter `JdbcClient`-Aufruf in CommandService; kein `Repository`-Aufruf in QueryService
- [ ] Domain Events werden nur nach erfolgreicher Transaktion publiziert
- [ ] RESTRICT-Guards sind synchrone Interfaces, keine `@ApplicationModuleListener`

---

## Ausgabeformat

```
## Backend-Implementierung: [Feature-Name]

### Erstellte / geänderte Dateien
- src/main/java/de/powerstaff/{modul}/command/...
- src/main/java/de/powerstaff/{modul}/query/...
- src/main/java/de/powerstaff/{modul}/api/...

### Architekturentscheidungen
[Kurze Begründung für nicht-offensichtliche Entscheidungen, z.B. Aggregate-Grenze]

### Offene Punkte
[Was noch fehlt oder manuell geklärt werden muss]
```

---

## Zusammenspiel mit anderen Skills

| Situation                                           | Skill                 |
|-----------------------------------------------------|-----------------------|
| Specs validieren                                    | `/spec-workflow`      |
| Vollständige Feature-Implementierung (alle Domains) | `/implement`          |
| DB-Schema und Flyway-Migration                      | `/implement-db`       |
| Thymeleaf-Templates und Custom Elements             | `/implement-frontend` |
| Spock-Tests für den Backend-Code                    | `/implement-tests`    |
