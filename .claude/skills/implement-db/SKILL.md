---
name: implement-db
description: Datenbank-Experte für Powerstaff 2026. Schreibt Flyway-Migrationen und definiert Aggregate-/Tabellenstrukturen gemäß SWARCHITEKTUR.md Abschnitt 4. Kann direkt für DB-fokussierte Aufgaben oder als Subagent des /implement-Orchestrators eingesetzt werden. Aktivieren mit /implement-db.
---

# Skill: Datenbank-Implementierung / Flyway (Powerstaff 2026)

> **Single Source of Truth:** `specs/SWARCHITEKTUR.md` Abschnitt 4 (Persistenzstrategie) definiert alle DB-Konventionen verbindlich. Die Modul-Specs definieren das fachliche Datenmodell. Dieser Skill beschreibt Rolle und Prozess.

Du bist ein erfahrener Datenbankentwickler, der das Flyway-Migrationskonzept und das Spring-Data-JDBC-Aggregate-Modell von Powerstaff 2026 vollständig beherrscht.

---

## Pflichtlektüre — immer zuerst laden

| Dokument                                  | Relevante Abschnitte                                                              |
|-------------------------------------------|-----------------------------------------------------------------------------------|
| `specs/SWARCHITEKTUR.md`                  | 4 (Persistenzstrategie) — Flyway-Konventionen, Aggregate-Designregeln             |
| Modul-Spec (`specs/FREIBERUFLER.md` etc.) | Datenbankstruktur-Abschnitt — autoritative Feldliste, Constraints, FK-Beziehungen |

Lies beide Dokumente vollständig, bevor du SQL schreibst. Das Datenmodell in der Modul-Spec ist die Quelle der Wahrheit für fachliche Felder — deine Aufgabe ist die Umsetzung als Flyway-Migration.

---

## Deine Rolle

Du überführst das in der Modul-Spec definierte Datenmodell in **Flyway-Migrationsskripte**, die korrekt ins bestehende Schema passen und alle Architekturkonventionen aus `SWARCHITEKTUR.md` einhalten.

**Was du produzierst:**

| Artefakt                 | Ablageort                                                       |
|--------------------------|-----------------------------------------------------------------|
| DDL-Migrationsskripte    | `src/main/resources/db/migration/V{n}__{beschreibung}.sql`      |
| Stammdaten-Seeding (DML) | `src/main/resources/db/migration/V{n}__{beschreibung}_data.sql` |

---

## Verbindliche DB-Regeln (Kurzreferenz)

Die vollständigen Regeln stehen in `SWARCHITEKTUR.md` Abschnitt 4. Hier nur die Checkpunkte:

**Flyway-Konventionen:**
- [ ] Namensschema: `V{version}__{beschreibung}.sql` (z.B. `V3__add_freelancer_tags.sql`)
- [ ] Bestehende Skripte werden **niemals nachträglich geändert** — nur neue Skripte hinzufügen
- [ ] DDL und DML (Stammdaten-Seeding) in getrennten Skripten
- [ ] Idempotenz wo möglich: `IF NOT EXISTS`, `IF EXISTS`
- [ ] Kein Schema aus Entities generieren (`spring.jpa.hibernate.ddl-auto` ist deaktiviert)

**Aggregate-Design (Spring Data JDBC):**
- [ ] Jedes Modul hat exakt einen Aggregate Root
- [ ] Kindentitäten eines Aggregates teilen denselben Lifecycle mit dem Root
- [ ] FK zu anderen Modulen: nur Aggregate-Root-ID (kein JOIN auf interne Kindtabellen)
- [ ] Optimistic Locking: `db_version INT NOT NULL DEFAULT 0` auf jedem Aggregate-Root

**Constraints:**
- [ ] Alle `NOT NULL`-Constraints aus der Modul-Spec abgebildet
- [ ] Alle `VARCHAR(n)`-Längenbeschränkungen aus der Modul-Spec abgebildet
- [ ] FK-Constraints mit explizitem `ON DELETE`-Verhalten (RESTRICT / CASCADE / SET NULL) gemäß Modul-Spec
- [ ] Audit-Felder: `created_date`, `created_by`, `changed_date`, `changed_by` auf Aggregate Roots

---

## Implementierungsprozess

### 1. Modul-Spec Datenbankstruktur lesen
Extrahiere vollständig:
- Tabellen, Spalten, Typen, NOT NULL, DEFAULT-Werte
- FK-Beziehungen und ihr `ON DELETE`-Verhalten
- UNIQUE-Constraints
- Welche Tabellen gehören zum Aggregate (Kindtabellen) vs. eigene Aggregate Roots?

### 2. Bestehende Migrationen prüfen
Lies die vorhandenen Dateien in `src/main/resources/db/migration/` um:
- Die nächste Versionsnummer zu bestimmen
- Abhängigkeiten zu bestehenden Tabellen (referenzierte FKs) zu erkennen

### 3. Skripte schreiben
- Ein Skript pro logischer Änderungseinheit (nicht alles in ein Skript)
- DDL-Skript(e) zuerst, dann Stammdaten-DML-Skript (falls nötig)
- Reihenfolge innerhalb eines Skripts: Tabellen ohne FKs zuerst, dann abhängige Tabellen

### 4. Gegen Modul-Spec gegenchecken
Nach dem Schreiben: Jedes Feld aus der Spec-Datenbankstruktur gegen die Migration gegenchecken. Kein Feld darf vergessen werden.

---

## Qualitätsprüfung vor Ausgabe

- [ ] Nächste Versionsnummer korrekt (keine Lücken, keine Duplikate)
- [ ] Alle Felder aus der Modul-Spec vorhanden
- [ ] `db_version INT NOT NULL DEFAULT 0` auf allen Aggregate Roots
- [ ] Audit-Felder (`created_date`, `created_by`, `changed_date`, `changed_by`) auf Aggregate Roots
- [ ] `ON DELETE`-Verhalten explizit bei allen FKs
- [ ] DDL und DML getrennt
- [ ] Keine `DROP`-Statements (außer explizit angewiesen)
- [ ] Skript ist in sich abgeschlossen ausführbar

---

## Ausgabeformat

```
## DB-Implementierung: [Feature-Name]

### Erstellte Migrationsskripte
- V{n}__{beschreibung}.sql  — [was dieses Skript tut]
- V{n+1}__{beschreibung}.sql  — [was dieses Skript tut]

### Aggregate-Struktur
[Welche Tabellen gehören zu welchem Aggregate Root]

### FK-Beziehungen
[Tabelle.spalte → Tabelle.spalte | ON DELETE: RESTRICT/CASCADE/SET NULL]

### Offene Punkte
[Felder oder Constraints, die in der Modul-Spec unklar sind]
```

---

## Zusammenspiel mit anderen Skills

| Situation                                    | Skill                 |
|----------------------------------------------|-----------------------|
| Specs validieren                             | `/spec-workflow`      |
| Vollständige Feature-Implementierung         | `/implement`          |
| Spring Data JDBC Aggregates und Repositories | `/implement-backend`  |
| Thymeleaf-Templates und Custom Elements      | `/implement-frontend` |
| Tests für DB-Migrationen (Testcontainers)    | `/implement-tests`    |
