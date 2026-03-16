-- =============================================================================
-- Powerstaff 2026 – Initiales Datenbankschema
-- Migration:  V1__init_schema.sql
-- Erstellt:   März 2026
--
-- Tabellenreihenfolge: Eltern vor Kindern (FK-Abhängigkeiten).
--
-- Shared-Stammdaten (historytype, project_position_status, tags)
--   → partner, freelancer, kunde
--   → project
--   → project_position, project_history, remembered_project
--   → freelancer_contact, freelancer_history, freelancer_tags
--   → partner_contact, partner_history
--   → kunde_contact, kunde_history
--   → profile_search_chat, profile_search_message
--   → ps_user
--   → event_publication (Spring Modulith)
-- =============================================================================

SET NAMES utf8mb4;
SET time_zone = '+00:00';

-- =============================================================================
-- SHARED-MODUL: Gemeinsam genutzte Stammdaten
-- =============================================================================

-- -----------------------------------------------------------------------------
-- historytype
-- Konfigurierbare Typen für die Kontakthistorie von Freiberuflern, Partnern
-- und Kunden. Wird vom shared-Modul verwaltet und von allen drei Modulen
-- über den SharedQueryService gelesen.
-- Referenziert von: freelancer_history.type_id, partner_history.type_id,
--                   kunde_history.type_id
-- -----------------------------------------------------------------------------
CREATE TABLE historytype (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'Technischer Primärschlüssel',
    description VARCHAR(255) NOT NULL               COMMENT 'Anzeigename des Historientyps (z. B. „Telefonat", „E-Mail", „Gespräch")',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Konfigurierbare Typen für die Kontakthistorie (Freiberufler, Partner, Kunden). Verwaltung im Admin-Bereich.';


-- -----------------------------------------------------------------------------
-- project_position_status
-- Konfigurierbare Status-Werte für Projektpositionen (Zuordnung Freiberufler
-- ↔ Projekt). Jeder Status trägt Hintergrund- und Textfarbe für das farbige
-- Badge in der Positionsliste.
-- Referenziert von: project_position.status_id
-- Löschen nur direkt auf DB-Ebene durch Admin (keine Lösch-UI).
-- -----------------------------------------------------------------------------
CREATE TABLE project_position_status (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'Technischer Primärschlüssel',
    description VARCHAR(255) NOT NULL               COMMENT 'Anzeigename des Status (z. B. „Vorgeschlagen", „Im Gespräch", „Besetzt")',
    color       VARCHAR(50)  NOT NULL               COMMENT 'Hintergrundfarbe des Badges als CSS-Farbwert (z. B. #d1fae5); heller Ton empfohlen',
    color_text  VARCHAR(50)  NOT NULL               COMMENT 'Textfarbe des Badges als CSS-Farbwert (z. B. #065f46); dunkler Ton für WCAG-AA-Kontrast ≥ 4,5:1',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Konfigurierbare Status-Werte für Projektpositionen inkl. Badge-Farben. Verwaltung im Admin-Bereich; Löschen nur per DB-Admin.';


-- -----------------------------------------------------------------------------
-- tags
-- Fachliche Tags, die Freiberuflern zugeordnet werden können.
-- Die Spalte `type` entspricht dem TagType-Enum (Ordinalwert als String-Name):
--   SCHWERPUNKT (0), FUNKTION (1), EINSATZORT (2), BEMERKUNG (3), TYP (4)
-- Referenziert von: freelancer_tags.tag_id
-- -----------------------------------------------------------------------------
CREATE TABLE tags (
    id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Technischer Primärschlüssel',
    tagname VARCHAR(255) NOT NULL               COMMENT 'Anzeigename des Tags (z. B. „Java", „München", „Senior")',
    type    VARCHAR(255) NOT NULL               COMMENT 'TagType-Enum-Name: SCHWERPUNKT | FUNKTION | EINSATZORT | BEMERKUNG | TYP',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Fachliche Tags für Freiberufler, gruppiert nach Kategorie (TagType-Enum). Verwaltung im Admin-Bereich.';

-- Index für Suche/Sortierung nach Typ (Gruppenfilter im Formular)
CREATE INDEX idx_tags_type ON tags (type)
    COMMENT 'Beschleunigt Filterung nach Tag-Kategorie (SCHWERPUNKT, FUNKTION, …)';


-- =============================================================================
-- PARTNER-MODUL
-- =============================================================================

-- -----------------------------------------------------------------------------
-- partner
-- Aggregate Root des Partner-Moduls.
-- Partnerunternehmen (z. B. Vermittlungsagenturen), die eigene Freiberufler
-- mitbringen und Projekte initiieren können.
-- Kinder (mitgeladen): partner_contact
-- Separate Aggregate Roots: partner_history
-- -----------------------------------------------------------------------------
CREATE TABLE partner (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Technischer Primärschlüssel; Aggregate-Root-ID',
    db_version   BIGINT       NOT NULL DEFAULT 0      COMMENT 'Optimistic-Locking-Zähler; wird bei jedem UPDATE inkrementiert',
    creation_date     DATETIME     NULL               COMMENT 'Zeitpunkt der ersten Erfassung; durch Spring Data JDBC Auditing befüllt',
    creation_user     VARCHAR(255) NULL               COMMENT 'Sachbearbeiter, der den Datensatz angelegt hat (ps_user.username)',
    changed_date      DATETIME     NULL               COMMENT 'Zeitpunkt der letzten Änderung; durch Spring Data JDBC Auditing befüllt',
    changed_user      VARCHAR(255) NULL               COMMENT 'Sachbearbeiter, der den Datensatz zuletzt geändert hat (ps_user.username)',
    company      VARCHAR(255) NULL                    COMMENT 'Firmenname des Partnerunternehmens (Hauptfeld)',
    name1        VARCHAR(255) NULL                    COMMENT 'Nachname des Ansprechpartners beim Partner',
    name2        VARCHAR(255) NULL                    COMMENT 'Vorname des Ansprechpartners beim Partner',
    street       VARCHAR(255) NULL                    COMMENT 'Straße und Hausnummer',
    country      VARCHAR(255) NULL                    COMMENT 'Land (gruppiert mit PLZ/Ort im Formular)',
    plz          VARCHAR(255) NULL                    COMMENT 'Postleitzahl',
    city         VARCHAR(255) NULL                    COMMENT 'Ort',
    contactforbidden BIT(1)   NOT NULL DEFAULT 0      COMMENT 'Kontaktsperre-Flag; bei true: rotes Banner im Formular',
    show_again   BIT(1)       NOT NULL DEFAULT 0      COMMENT 'Wiedervorlage-Flag; Workflow noch offen (siehe PARTNER.md – Offene Punkte)',
    comments     LONGTEXT     NULL                    COMMENT 'Allgemeiner Freitext-Kommentar',
    debitor_nr   VARCHAR(255) NULL                    COMMENT 'Debitorennummer für die Buchhaltung',
    kreditor_nr  VARCHAR(255) NULL                    COMMENT 'Kreditorennummer für die Buchhaltung',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Partnerunternehmen (Vermittlungsagenturen). Aggregate Root des partner-Moduls. Löschen verhindert, wenn Projekte zugeordnet sind (project.partner_id RESTRICT).';

-- Suche/Sortierung nach Firmenname (Standardsortierung der QBE-Ergebnisliste)
CREATE INDEX idx_partner_company ON partner (company)
    COMMENT 'Beschleunigt QBE-Suche und Standardsortierung nach Firmenname';


-- -----------------------------------------------------------------------------
-- partner_contact
-- Kontaktmöglichkeiten eines Partners (Aggregate-Kind von partner).
-- Spring Data JDBC löscht Kinder automatisch beim Löschen des Aggregate Root.
-- Typen (Enum): EMAIL, WEB, XING, GULP, TELEFON, FAX
-- -----------------------------------------------------------------------------
CREATE TABLE partner_contact (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Technischer Primärschlüssel',
    creation_date DATETIME     NULL                    COMMENT 'Zeitpunkt der Erfassung',
    creation_user VARCHAR(255) NULL                    COMMENT 'Erfassender Sachbearbeiter (ps_user.username)',
    changed_date  DATETIME     NULL                    COMMENT 'Zeitpunkt der letzten Änderung',
    changed_user  VARCHAR(255) NULL                    COMMENT 'Zuletzt ändernder Sachbearbeiter (ps_user.username)',
    type          VARCHAR(255) NOT NULL                COMMENT 'Kontakttyp-Enum: EMAIL | WEB | XING | GULP | TELEFON | FAX',
    value         VARCHAR(255) NOT NULL                COMMENT 'Der eigentliche Kontaktwert (E-Mail-Adresse, URL, Profil-ID, …)',
    partner_id    BIGINT       NOT NULL                COMMENT 'FK → partner; Zugehöriger Partner',
    PRIMARY KEY (id),
    -- Fremdschlüssel: Kaskadierendes Löschen, da partner_contact Aggregat-Kind von partner ist
    CONSTRAINT fk_partner_contact_partner
        FOREIGN KEY (partner_id) REFERENCES partner (id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Kontaktmöglichkeiten (E-Mail, Telefon, Web, …) eines Partners. Aggregat-Kind von partner; wird kaskadierend mitgelöscht.';

CREATE INDEX idx_partner_contact_partner_id ON partner_contact (partner_id)
    COMMENT 'Beschleunigt Laden aller Kontakte eines Partners';


-- -----------------------------------------------------------------------------
-- partner_history
-- Typisierte Kontakthistorie eines Partners. Separater Aggregate Root
-- (wächst unbegrenzt; kein Mitladen beim Öffnen des partner-Aggregats).
-- Löschen per DB-CASCADE wenn Partner gelöscht wird.
-- -----------------------------------------------------------------------------
CREATE TABLE partner_history (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Technischer Primärschlüssel; Aggregate-Root-ID',
    creation_date DATETIME     NULL                    COMMENT 'Zeitpunkt der Erfassung des Historieneintrags',
    creation_user VARCHAR(255) NULL                    COMMENT 'Sachbearbeiter, der den Eintrag angelegt hat (ps_user.username)',
    changed_date  DATETIME     NULL                    COMMENT 'Zeitpunkt der letzten Änderung',
    changed_user  VARCHAR(255) NULL                    COMMENT 'Sachbearbeiter, der den Eintrag zuletzt geändert hat (ps_user.username)',
    description   LONGTEXT     NOT NULL                COMMENT 'Freitextinhalt des Historieneintrags (mehrzeilig)',
    type_id       BIGINT       NOT NULL                COMMENT 'FK → historytype; Pflichtauswahl aus konfigurierter Typ-Liste',
    partner_id    BIGINT       NOT NULL                COMMENT 'FK → partner; Zugehöriger Partner',
    PRIMARY KEY (id),
    -- Historientyp: RESTRICT – Typ darf nicht gelöscht werden, solange Einträge darauf verweisen
    CONSTRAINT fk_partner_history_type
        FOREIGN KEY (type_id) REFERENCES historytype (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    -- Partner: CASCADE – Einträge werden mitgelöscht, wenn der Partner gelöscht wird
    CONSTRAINT fk_partner_history_partner
        FOREIGN KEY (partner_id) REFERENCES partner (id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Typisierte Kontakthistorie eines Partners. Separater Aggregate Root (unbegrenzt wachsend). Wird kaskadierend mitgelöscht wenn Partner gelöscht wird.';

-- Absteigend nach Erfassungsdatum (Standardsortierung: neueste zuerst)
CREATE INDEX idx_partner_history_partner_date ON partner_history (partner_id, creation_date DESC)
    COMMENT 'Beschleunigt Laden der Kontakthistorie eines Partners sortiert nach Erfassungsdatum';


-- =============================================================================
-- FREELANCER-MODUL
-- =============================================================================

-- -----------------------------------------------------------------------------
-- freelancer
-- Aggregate Root des freelancer-Moduls.
-- Freiberufler mit vollständigem Profil, Skills, Verfügbarkeit, Konditionen
-- und optionaler Partner-Zuordnung.
-- Kinder (mitgeladen): freelancer_contact
-- Separate Aggregate Roots: freelancer_history
-- ID-Referenzen (nur Wert, keine Entität): freelancer_tags.tag_id
-- -----------------------------------------------------------------------------
CREATE TABLE freelancer (
    id                       BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Technischer Primärschlüssel; Aggregate-Root-ID',
    db_version               BIGINT       NOT NULL DEFAULT 0      COMMENT 'Optimistic-Locking-Zähler; wird bei jedem UPDATE inkrementiert',
    creation_date            DATETIME     NULL                    COMMENT 'Zeitpunkt der ersten Erfassung; durch Spring Data JDBC Auditing befüllt',
    creation_user            VARCHAR(255) NULL                    COMMENT 'Sachbearbeiter, der den Datensatz angelegt hat (ps_user.username)',
    changed_date             DATETIME     NULL                    COMMENT 'Zeitpunkt der letzten Änderung; durch Spring Data JDBC Auditing befüllt',
    changed_user             VARCHAR(255) NULL                    COMMENT 'Sachbearbeiter, der den Datensatz zuletzt geändert hat (ps_user.username)',
    -- Adressgruppe
    titel                    VARCHAR(255) NULL                    COMMENT 'Anrede/Titel (z. B. „Dr.", „Prof.")',
    name1                    VARCHAR(255) NULL                    COMMENT 'Nachname des Freiberuflers',
    name2                    VARCHAR(255) NULL                    COMMENT 'Vorname des Freiberuflers',
    company                  VARCHAR(255) NULL                    COMMENT 'Firmenname des Freiberuflers (falls selbstständig unter Firma tätig)',
    street                   VARCHAR(255) NULL                    COMMENT 'Straße und Hausnummer',
    country                  VARCHAR(255) NULL                    COMMENT 'Land (gruppiert mit PLZ/Ort im Formular)',
    plz                      VARCHAR(255) NULL                    COMMENT 'Postleitzahl',
    city                     VARCHAR(255) NULL                    COMMENT 'Ort',
    nationalitaet            VARCHAR(255) NULL                    COMMENT 'Nationalität',
    geburtsdatum             VARCHAR(255) NULL                    COMMENT 'Geburtsdatum als Freitext (erlaubt Teilangaben wie „Mai 1980")',
    partner_id               BIGINT       NULL                    COMMENT 'FK → partner; optionale Zuordnung zu einem Partnerunternehmen. Nur über Partner-Formular änderbar.',
    -- Kontaktinformationsgruppe
    contactforbidden         BIT(1)       NOT NULL DEFAULT 0      COMMENT 'Kontaktsperre-Flag; bei true: rotes Banner im Formular',
    show_again               BIT(1)       NOT NULL DEFAULT 0      COMMENT 'Wiedervorlage-Flag; Workflow noch offen (siehe FREIBERUFLER.md – Offene Punkte)',
    -- Kommentargruppe
    comments                 LONGTEXT     NULL                    COMMENT 'Allgemeiner Freitext-Kommentar',
    -- Einsatzdetailsgruppe
    einsatzdetails           LONGTEXT     NULL                    COMMENT 'Einsatz- und Auftragsdetails (Freitext)',
    -- Zusatzinformationsgruppe
    contact_person           VARCHAR(255) NULL                    COMMENT 'Name des betreuenden Sachbearbeiters',
    contact_type             VARCHAR(255) NULL                    COMMENT 'Kontaktkanal-Freitext (z. B. „E-Mail", „Telefon", „Messe")',
    contact_reason           VARCHAR(255) NULL                    COMMENT 'Kontaktgrund-Freitext (Grund des letzten Kontakts)',
    last_contact_date        DATETIME     NULL                    COMMENT 'Datum des letzten Kontakts',
    kontaktart               VARCHAR(10)  NULL                    COMMENT 'Kontaktart-Enum: NL | NL1 | NL2 | X | NO | LL; NULL = kein Wert gesetzt',
    -- Verfügbarkeit & Konditionen
    availability_as_date     DATETIME     NULL                    COMMENT 'Verfügbarkeitsdatum: ab wann der Freiberufler verfügbar ist',
    salary_long              BIGINT       NULL                    COMMENT 'Gewünschter Stundensatz in ganzen Euro',
    salary_per_day_long      BIGINT       NULL                    COMMENT 'Gewünschter Tagessatz in ganzen Euro',
    salary_remote            BIGINT       NULL                    COMMENT 'Gewünschter Stundensatz Remote in ganzen Euro',
    salary_partner_long      BIGINT       NULL                    COMMENT 'Verhandelter Stundensatz (Einkaufspreis) in ganzen Euro',
    salary_partner_per_day_long BIGINT    NULL                    COMMENT 'Verhandelter Tagessatz (Einkaufspreis) in ganzen Euro',
    datenschutz              BIT(1)       NOT NULL DEFAULT 0      COMMENT 'DSGVO-Einwilligungsflag',
    debitor_nr               VARCHAR(255) NULL                    COMMENT 'Debitorennummer für die Buchhaltung',
    gulp_id                  VARCHAR(255) NULL                    COMMENT 'Profil-ID auf GULP.de (ggf. redundant mit GULP-Kontaktmöglichkeit; siehe FREIBERUFLER.md – Offene Punkte)',
    -- Kodierungsgruppe
    code                     VARCHAR(255) NULL                    COMMENT 'Interner, anonymisierter Freiberufler-Code; eindeutig; für Kundenkommunikation ohne Namensnennung',
    skills                   LONGTEXT     NULL                    COMMENT 'Freitext-Skillbeschreibung; durchsuchbar per QBE',
    PRIMARY KEY (id),
    -- Partner-Zuordnung: SET NULL – Freiberufler bleibt erhalten, wenn Partner gelöscht wird
    CONSTRAINT fk_freelancer_partner
        FOREIGN KEY (partner_id) REFERENCES partner (id)
        ON DELETE SET NULL
        ON UPDATE RESTRICT,
    -- Kontaktart: erlaubte Werte gemäß Spec
    CONSTRAINT chk_freelancer_kontaktart
        CHECK (kontaktart IN ('NL', 'NL1', 'NL2', 'X', 'NO', 'LL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Freiberufler mit vollständigem Profil. Aggregate Root des freelancer-Moduls. Löschen verhindert, wenn aktive Projektpositionen existieren (project_position.freelancer_id RESTRICT).';

-- Eindeutiger Code (anonymisierter Bezeichner für Kundenkommunikation)
CREATE UNIQUE INDEX idx_freelancer_code ON freelancer (code)
    COMMENT 'Eindeutigkeit des Freiberufler-Codes; Basis für Zuordnung im Partner-Formular';

-- Suche nach Name/Vorname (häufigste QBE-Felder, Standardsortierung)
CREATE INDEX idx_freelancer_name ON freelancer (name1, name2)
    COMMENT 'Beschleunigt QBE-Suche und Standardsortierung nach Name/Vorname';

-- Partner-Zuordnung (Abruf aller Freiberufler eines Partners)
CREATE INDEX idx_freelancer_partner_id ON freelancer (partner_id)
    COMMENT 'Beschleunigt Laden der Freiberufler-Liste im Partner-Formular';


-- -----------------------------------------------------------------------------
-- freelancer_contact
-- Kontaktmöglichkeiten eines Freiberuflers (Aggregat-Kind von freelancer).
-- Typen (Enum): EMAIL, WEB, XING, GULP, TELEFON, FAX
-- -----------------------------------------------------------------------------
CREATE TABLE freelancer_contact (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Technischer Primärschlüssel',
    creation_date DATETIME     NULL                    COMMENT 'Zeitpunkt der Erfassung',
    creation_user VARCHAR(255) NULL                    COMMENT 'Erfassender Sachbearbeiter (ps_user.username)',
    changed_date  DATETIME     NULL                    COMMENT 'Zeitpunkt der letzten Änderung',
    changed_user  VARCHAR(255) NULL                    COMMENT 'Zuletzt ändernder Sachbearbeiter (ps_user.username)',
    type          VARCHAR(255) NOT NULL                COMMENT 'Kontakttyp-Enum: EMAIL | WEB | XING | GULP | TELEFON | FAX',
    value         VARCHAR(255) NOT NULL                COMMENT 'Der eigentliche Kontaktwert (E-Mail-Adresse, URL, GULP-ID, …)',
    freelancer_id BIGINT       NOT NULL                COMMENT 'FK → freelancer; Zugehöriger Freiberufler',
    PRIMARY KEY (id),
    -- Aggregat-Kind: kaskadierendes Löschen
    CONSTRAINT fk_freelancer_contact_freelancer
        FOREIGN KEY (freelancer_id) REFERENCES freelancer (id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Kontaktmöglichkeiten (E-Mail, Telefon, Web, …) eines Freiberuflers. Aggregat-Kind; kaskadierend mitgelöscht.';

CREATE INDEX idx_freelancer_contact_freelancer_id ON freelancer_contact (freelancer_id)
    COMMENT 'Beschleunigt Laden aller Kontakte eines Freiberuflers';


-- -----------------------------------------------------------------------------
-- freelancer_history
-- Typisierte Kontakthistorie eines Freiberuflers. Separater Aggregate Root.
-- -----------------------------------------------------------------------------
CREATE TABLE freelancer_history (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Technischer Primärschlüssel; Aggregate-Root-ID',
    creation_date DATETIME     NULL                    COMMENT 'Zeitpunkt der Erfassung des Historieneintrags',
    creation_user VARCHAR(255) NULL                    COMMENT 'Sachbearbeiter, der den Eintrag angelegt hat (ps_user.username)',
    changed_date  DATETIME     NULL                    COMMENT 'Zeitpunkt der letzten Änderung',
    changed_user  VARCHAR(255) NULL                    COMMENT 'Sachbearbeiter, der den Eintrag zuletzt geändert hat (ps_user.username)',
    description   LONGTEXT     NOT NULL                COMMENT 'Freitextinhalt des Historieneintrags (mehrzeilig)',
    type_id       BIGINT       NOT NULL                COMMENT 'FK → historytype; Pflichtauswahl',
    freelancer_id BIGINT       NOT NULL                COMMENT 'FK → freelancer; Zugehöriger Freiberufler',
    PRIMARY KEY (id),
    CONSTRAINT fk_freelancer_history_type
        FOREIGN KEY (type_id) REFERENCES historytype (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    CONSTRAINT fk_freelancer_history_freelancer
        FOREIGN KEY (freelancer_id) REFERENCES freelancer (id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Typisierte Kontakthistorie eines Freiberuflers. Separater Aggregate Root. Kaskadierend mitgelöscht wenn Freiberufler gelöscht wird.';

CREATE INDEX idx_freelancer_history_freelancer_date ON freelancer_history (freelancer_id, creation_date DESC)
    COMMENT 'Beschleunigt Laden der Kontakthistorie eines Freiberuflers sortiert nach Erfassungsdatum';


-- -----------------------------------------------------------------------------
-- freelancer_tags
-- Zuordnung von Tags zu Freiberuflern (n:m, jedoch als eigener Aggregate Root
-- im Spring-Data-JDBC-Sinne: nur ID-Referenzen, keine Entitätsklasse).
-- UNIQUE auf (freelancer_id, tag_id) – ein Tag kann nur einmal zugeordnet sein.
-- -----------------------------------------------------------------------------
CREATE TABLE freelancer_tags (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Technischer Primärschlüssel',
    creation_date DATETIME     NULL                    COMMENT 'Zeitpunkt der Zuordnung',
    creation_user VARCHAR(255) NULL                    COMMENT 'Sachbearbeiter, der die Zuordnung angelegt hat (ps_user.username)',
    changed_date  DATETIME     NULL                    COMMENT 'Zeitpunkt der letzten Änderung der Zuordnung',
    changed_user  VARCHAR(255) NULL                    COMMENT 'Sachbearbeiter, der die Zuordnung zuletzt geändert hat (ps_user.username)',
    freelancer_id BIGINT       NOT NULL                COMMENT 'FK → freelancer; Zugeordneter Freiberufler',
    tag_id        BIGINT       NOT NULL                COMMENT 'FK → tags; Zugeordneter Tag (nur ID-Referenz, keine Entitätsklasse)',
    PRIMARY KEY (id),
    -- Ein Tag kann einem Freiberufler nur einmal zugeordnet sein
    CONSTRAINT uq_freelancer_tags UNIQUE (freelancer_id, tag_id),
    CONSTRAINT fk_freelancer_tags_freelancer
        FOREIGN KEY (freelancer_id) REFERENCES freelancer (id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT,
    -- RESTRICT: Tag-Löschen verhindert, solange Zuordnungen existieren (kein stiller Datenverlust)
    CONSTRAINT fk_freelancer_tags_tag
        FOREIGN KEY (tag_id) REFERENCES tags (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Tag-Zuordnungen eines Freiberuflers. Nur ID-Referenzen (keine eigene Entitätsklasse). Kaskadierend mitgelöscht wenn Freiberufler gelöscht wird.';

CREATE INDEX idx_freelancer_tags_freelancer_id ON freelancer_tags (freelancer_id)
    COMMENT 'Beschleunigt Laden aller Tags eines Freiberuflers';

CREATE INDEX idx_freelancer_tags_tag_id ON freelancer_tags (tag_id)
    COMMENT 'Beschleunigt RESTRICT-Prüfung beim Löschen eines Tags';


-- =============================================================================
-- CUSTOMER-MODUL
-- =============================================================================

-- -----------------------------------------------------------------------------
-- kunde
-- Aggregate Root des customer-Moduls.
-- Kundenunternehmen (Auftraggeber). Keine direkte Beziehung zu Freiberuflern;
-- Verbindung entsteht ausschließlich über Projekte.
-- Kinder (mitgeladen): kunde_contact
-- Separate Aggregate Roots: kunde_history
-- -----------------------------------------------------------------------------
CREATE TABLE kunde (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Technischer Primärschlüssel; Aggregate-Root-ID',
    db_version    BIGINT       NOT NULL DEFAULT 0      COMMENT 'Optimistic-Locking-Zähler; wird bei jedem UPDATE inkrementiert',
    creation_date DATETIME     NULL                    COMMENT 'Zeitpunkt der ersten Erfassung; durch Spring Data JDBC Auditing befüllt',
    creation_user VARCHAR(255) NULL                    COMMENT 'Sachbearbeiter, der den Datensatz angelegt hat (ps_user.username)',
    changed_date  DATETIME     NULL                    COMMENT 'Zeitpunkt der letzten Änderung; durch Spring Data JDBC Auditing befüllt',
    changed_user  VARCHAR(255) NULL                    COMMENT 'Sachbearbeiter, der den Datensatz zuletzt geändert hat (ps_user.username)',
    company       VARCHAR(255) NULL                    COMMENT 'Firmenname des Kundenunternehmens (Hauptfeld)',
    name1         VARCHAR(255) NULL                    COMMENT 'Nachname des Ansprechpartners beim Kunden',
    name2         VARCHAR(255) NULL                    COMMENT 'Vorname des Ansprechpartners beim Kunden',
    street        VARCHAR(255) NULL                    COMMENT 'Straße und Hausnummer',
    country       VARCHAR(255) NULL                    COMMENT 'Land',
    plz           VARCHAR(255) NULL                    COMMENT 'Postleitzahl',
    city          VARCHAR(255) NULL                    COMMENT 'Ort',
    contactforbidden BIT(1)    NOT NULL DEFAULT 0      COMMENT 'Kontaktsperre-Flag; bei true: rotes Banner im Formular',
    show_again    BIT(1)       NOT NULL DEFAULT 0      COMMENT 'Wiedervorlage-Flag; Workflow noch offen (siehe KUNDEN.md – Offene Punkte)',
    comments      LONGTEXT     NULL                    COMMENT 'Allgemeiner Freitext-Kommentar',
    debitor_nr    VARCHAR(255) NULL                    COMMENT 'Debitorennummer für die Buchhaltung',
    kreditor_nr   VARCHAR(255) NULL                    COMMENT 'Kreditorennummer für die Buchhaltung',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Kundenunternehmen (Auftraggeber). Aggregate Root des customer-Moduls. Keine direkte Freiberufler-Beziehung. Löschen verhindert, wenn Projekte zugeordnet sind (project.customer_id RESTRICT).';

CREATE INDEX idx_kunde_company ON kunde (company)
    COMMENT 'Beschleunigt QBE-Suche und Standardsortierung nach Firmenname';


-- -----------------------------------------------------------------------------
-- kunde_contact
-- Kontaktmöglichkeiten eines Kunden (Aggregat-Kind von kunde).
-- -----------------------------------------------------------------------------
CREATE TABLE kunde_contact (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Technischer Primärschlüssel',
    creation_date DATETIME     NULL                    COMMENT 'Zeitpunkt der Erfassung',
    creation_user VARCHAR(255) NULL                    COMMENT 'Erfassender Sachbearbeiter (ps_user.username)',
    changed_date  DATETIME     NULL                    COMMENT 'Zeitpunkt der letzten Änderung',
    changed_user  VARCHAR(255) NULL                    COMMENT 'Zuletzt ändernder Sachbearbeiter (ps_user.username)',
    type          VARCHAR(255) NOT NULL                COMMENT 'Kontakttyp-Enum: EMAIL | WEB | XING | GULP | TELEFON | FAX',
    value         VARCHAR(255) NOT NULL                COMMENT 'Der eigentliche Kontaktwert',
    kunde_id      BIGINT       NOT NULL                COMMENT 'FK → kunde; Zugehöriger Kunde',
    PRIMARY KEY (id),
    CONSTRAINT fk_kunde_contact_kunde
        FOREIGN KEY (kunde_id) REFERENCES kunde (id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Kontaktmöglichkeiten eines Kunden. Aggregat-Kind; kaskadierend mitgelöscht.';

CREATE INDEX idx_kunde_contact_kunde_id ON kunde_contact (kunde_id)
    COMMENT 'Beschleunigt Laden aller Kontakte eines Kunden';


-- -----------------------------------------------------------------------------
-- kunde_history
-- Typisierte Kontakthistorie eines Kunden. Separater Aggregate Root.
-- -----------------------------------------------------------------------------
CREATE TABLE kunde_history (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Technischer Primärschlüssel; Aggregate-Root-ID',
    creation_date DATETIME     NULL                    COMMENT 'Zeitpunkt der Erfassung des Historieneintrags',
    creation_user VARCHAR(255) NULL                    COMMENT 'Sachbearbeiter, der den Eintrag angelegt hat (ps_user.username)',
    changed_date  DATETIME     NULL                    COMMENT 'Zeitpunkt der letzten Änderung',
    changed_user  VARCHAR(255) NULL                    COMMENT 'Sachbearbeiter, der den Eintrag zuletzt geändert hat (ps_user.username)',
    description   LONGTEXT     NOT NULL                COMMENT 'Freitextinhalt des Historieneintrags (mehrzeilig)',
    type_id       BIGINT       NOT NULL                COMMENT 'FK → historytype; Pflichtauswahl',
    kunde_id      BIGINT       NOT NULL                COMMENT 'FK → kunde; Zugehöriger Kunde',
    PRIMARY KEY (id),
    CONSTRAINT fk_kunde_history_type
        FOREIGN KEY (type_id) REFERENCES historytype (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    CONSTRAINT fk_kunde_history_kunde
        FOREIGN KEY (kunde_id) REFERENCES kunde (id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Typisierte Kontakthistorie eines Kunden. Separater Aggregate Root. Kaskadierend mitgelöscht wenn Kunde gelöscht wird.';

CREATE INDEX idx_kunde_history_kunde_date ON kunde_history (kunde_id, creation_date DESC)
    COMMENT 'Beschleunigt Laden der Kontakthistorie eines Kunden sortiert nach Erfassungsdatum';


-- =============================================================================
-- PROJECT-MODUL
-- =============================================================================

-- -----------------------------------------------------------------------------
-- project
-- Aggregate Root des project-Moduls.
-- Konkrete Projektanfragen oder laufende Aufträge. Kann einem Kunden oder
-- einem Partner zugeordnet sein – niemals beiden gleichzeitig.
-- Kinder (mitgeladen): keine (remembered_project ist separater Aggregate Root)
-- Separate Aggregate Roots: project_position, project_history
-- Constraints: customer_id und partner_id dürfen nicht gleichzeitig gesetzt sein
--              (zusätzlich durch Backend-Validierung gesichert).
-- -----------------------------------------------------------------------------
CREATE TABLE project (
    id                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Technischer Primärschlüssel; Aggregate-Root-ID',
    db_version           BIGINT       NOT NULL DEFAULT 0      COMMENT 'Optimistic-Locking-Zähler; wird bei jedem UPDATE inkrementiert',
    creation_date        DATETIME     NULL                    COMMENT 'Zeitpunkt der ersten Erfassung; durch Spring Data JDBC Auditing befüllt',
    creation_user        VARCHAR(255) NULL                    COMMENT 'Sachbearbeiter, der das Projekt angelegt hat (ps_user.username)',
    changed_date         DATETIME     NULL                    COMMENT 'Zeitpunkt der letzten Änderung; durch Spring Data JDBC Auditing befüllt',
    changed_user         VARCHAR(255) NULL                    COMMENT 'Sachbearbeiter, der das Projekt zuletzt geändert hat (ps_user.username)',
    -- Allgemeine Projektfelder
    project_number       VARCHAR(255) NULL                    COMMENT 'Fachliche Projektnummer (frei vergebbar, z. B. „2026-042")',
    entry_date           DATETIME     NULL                    COMMENT 'Fachliches Eingangsdatum (unabhängig vom technischen creation_date)',
    start_date           DATETIME     NULL                    COMMENT 'Geplanter oder vereinbarter Projektbeginn',
    duration             VARCHAR(255) NULL                    COMMENT 'Laufzeit als Freitext (z. B. „3 Monate", „bis 31.12.2026", „unbefristet")',
    status               INT          NOT NULL DEFAULT 1      COMMENT 'Projektstatus: 1=Offen | 2=Verloren | 3=Canceled | 4=Besetzt | 5=Search zu',
    visible_on_web_site  BIT(1)       NOT NULL DEFAULT 0      COMMENT 'Steuert Veröffentlichung auf der öffentlichen Website',
    -- Beschreibungsfelder
    description_short    VARCHAR(255) NULL                    COMMENT 'Einzeiliger Titeltext; wird in Listen und Übersichten angezeigt',
    description_long     LONGTEXT     NULL                    COMMENT 'Ausführliche Projektbeschreibung (mehrzeiliger Freitext)',
    skills               LONGTEXT     NULL                    COMMENT 'Gesuchte Fähigkeiten und Qualifikationen (Anforderungen)',
    -- Einsatz
    workplace            VARCHAR(255) NULL                    COMMENT 'Einsatzort (z. B. „München", „Remote", „hybrid")',
    -- Zuordnung (entweder Kunde oder Partner – nie beide)
    customer_id          BIGINT       NULL                    COMMENT 'FK → kunde; optionale Kundenzuordnung. Nach Anlage nicht mehr änderbar.',
    partner_id           BIGINT       NULL                    COMMENT 'FK → partner; optionale Partnerzuordnung. Nach Anlage nicht mehr änderbar.',
    -- Konditionen
    stundensatz_vk       BIGINT       NULL                    COMMENT 'Verkaufs-Stundensatz in ganzen Euro',
    debitor_nr           VARCHAR(255) NULL                    COMMENT 'Debitorennummer für die Buchhaltung',
    kreditor_nr          VARCHAR(255) NULL                    COMMENT 'Kreditorennummer für die Buchhaltung',
    PRIMARY KEY (id),
    -- Statuspflicht: 1–5
    CONSTRAINT chk_project_status
        CHECK (status BETWEEN 1 AND 5),
    -- Kunde: RESTRICT – Projekt verhindert Löschen des Kunden
    CONSTRAINT fk_project_customer
        FOREIGN KEY (customer_id) REFERENCES kunde (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    -- Partner: RESTRICT – Projekt verhindert Löschen des Partners
    CONSTRAINT fk_project_partner
        FOREIGN KEY (partner_id) REFERENCES partner (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Projekte (Anfragen/Aufträge). Aggregate Root des project-Moduls. Entweder Kunden- oder Partnerzuordnung, nie beide. Neuanlage nur über Kunden-/Partner-Formular.';

CREATE INDEX idx_project_customer_id ON project (customer_id)
    COMMENT 'Beschleunigt Laden der Projektliste im Kunden-Formular';

CREATE INDEX idx_project_partner_id ON project (partner_id)
    COMMENT 'Beschleunigt Laden der Projektliste im Partner-Formular';

CREATE INDEX idx_project_entry_date ON project (entry_date DESC)
    COMMENT 'Beschleunigt Standardsortierung der QBE-Ergebnisliste nach Eingangsdatum';

CREATE INDEX idx_project_project_number ON project (project_number)
    COMMENT 'Beschleunigt QBE-Suche nach Projektnummer';


-- -----------------------------------------------------------------------------
-- project_position
-- Zuordnung eines Freiberuflers zu einem Projekt (Vermittlungsbeziehung).
-- Separater Aggregate Root (Statuswechsel unabhängig vom Projekt-Aggregat).
-- UNIQUE auf (project_id, freelancer_id): ein Freiberufler kann einem Projekt
-- nur einmal zugeordnet sein.
-- Eigenes db_version-Feld für Optimistic Locking auf Positionsebene.
-- -----------------------------------------------------------------------------
CREATE TABLE project_position (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Technischer Primärschlüssel; Aggregate-Root-ID',
    db_version    BIGINT       NOT NULL DEFAULT 0      COMMENT 'Optimistic-Locking-Zähler auf Positionsebene (unabhängig vom Projekt-Aggregat)',
    creation_date DATETIME     NULL                    COMMENT 'Zeitpunkt der Zuordnung; durch Spring Data JDBC Auditing befüllt',
    creation_user VARCHAR(255) NULL                    COMMENT 'Sachbearbeiter, der die Zuordnung angelegt hat (ps_user.username)',
    changed_date  DATETIME     NULL                    COMMENT 'Zeitpunkt der letzten Änderung; durch Spring Data JDBC Auditing befüllt',
    changed_user  VARCHAR(255) NULL                    COMMENT 'Sachbearbeiter, der die Zuordnung zuletzt geändert hat (ps_user.username)',
    project_id    BIGINT       NOT NULL                COMMENT 'FK → project; Zugehöriges Projekt',
    freelancer_id BIGINT       NOT NULL                COMMENT 'FK → freelancer; Zugeordneter Freiberufler',
    status_id     BIGINT       NOT NULL                COMMENT 'FK → project_position_status; Pflichtfeld (z. B. „Vorgeschlagen", „Im Gespräch")',
    konditionen   LONGTEXT     NULL                    COMMENT 'Verhandelte Konditionen als Freitext (z. B. Stundensatz, Laufzeit)',
    kommentar     LONGTEXT     NULL                    COMMENT 'Interner Kommentar zur Zuordnung',
    PRIMARY KEY (id),
    -- Ein Freiberufler kann einem Projekt nur einmal zugeordnet sein
    CONSTRAINT uq_project_position UNIQUE (project_id, freelancer_id),
    -- Projekt: CASCADE – Positionen werden mitgelöscht, wenn Projekt gelöscht wird
    CONSTRAINT fk_project_position_project
        FOREIGN KEY (project_id) REFERENCES project (id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT,
    -- Freiberufler: RESTRICT – Freiberufler kann nicht gelöscht werden, solange Positionen existieren
    CONSTRAINT fk_project_position_freelancer
        FOREIGN KEY (freelancer_id) REFERENCES freelancer (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    -- Status: RESTRICT – Status kann nicht gelöscht werden, solange Positionen darauf verweisen
    CONSTRAINT fk_project_position_status
        FOREIGN KEY (status_id) REFERENCES project_position_status (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Zuordnung Freiberufler ↔ Projekt (Vermittlungsbeziehung). Separater Aggregate Root mit eigenem Optimistic Locking. RESTRICT auf Freiberufler verhindert versehentliches Löschen.';

CREATE INDEX idx_project_position_project_id ON project_position (project_id)
    COMMENT 'Beschleunigt Laden aller Positionen eines Projekts';

CREATE INDEX idx_project_position_freelancer_id ON project_position (freelancer_id)
    COMMENT 'Beschleunigt RESTRICT-Prüfung beim Löschen eines Freiberuflers';


-- -----------------------------------------------------------------------------
-- project_history
-- Typenlose Kontakthistorie eines Projekts (kein historytype-Bezug).
-- Separater Aggregate Root.
-- -----------------------------------------------------------------------------
CREATE TABLE project_history (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Technischer Primärschlüssel; Aggregate-Root-ID',
    creation_date DATETIME     NULL                    COMMENT 'Zeitpunkt der Erfassung des Historieneintrags',
    creation_user VARCHAR(255) NULL                    COMMENT 'Sachbearbeiter, der den Eintrag angelegt hat (ps_user.username)',
    changed_date  DATETIME     NULL                    COMMENT 'Zeitpunkt der letzten Änderung',
    changed_user  VARCHAR(255) NULL                    COMMENT 'Sachbearbeiter, der den Eintrag zuletzt geändert hat (ps_user.username)',
    description   LONGTEXT     NOT NULL                COMMENT 'Freitextinhalt des Historieneintrags (mehrzeilig; bewusst typenlos, kein historytype-Bezug)',
    project_id    BIGINT       NOT NULL                COMMENT 'FK → project; Zugehöriges Projekt',
    PRIMARY KEY (id),
    CONSTRAINT fk_project_history_project
        FOREIGN KEY (project_id) REFERENCES project (id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Typenlose Kontakthistorie eines Projekts (keine historytype-Verknüpfung). Separater Aggregate Root. Kaskadierend mitgelöscht wenn Projekt gelöscht wird.';

CREATE INDEX idx_project_history_project_date ON project_history (project_id, creation_date DESC)
    COMMENT 'Beschleunigt Laden der Projekthistorie sortiert nach Erfassungsdatum';


-- -----------------------------------------------------------------------------
-- remembered_project
-- Pro Sachbearbeiter genau ein gemerktes Projekt (kein Verlauf).
-- Separater Aggregate Root im project-Modul.
-- Mechanismus: jeder GET /projekte/{id} setzt diesen Eintrag (Upsert).
-- ON DELETE CASCADE: Wird das Projekt gelöscht, entfällt der Eintrag automatisch
-- (RESTRICT wäre fachlich falsch – Projektlöschen soll nicht blockiert werden).
-- -----------------------------------------------------------------------------
CREATE TABLE remembered_project (
    user_id    VARCHAR(255) NOT NULL COMMENT 'PK; ps_user.username – pro Sachbearbeiter genau ein Eintrag',
    project_id BIGINT       NOT NULL COMMENT 'FK → project; Das aktuell gemerkte Projekt',
    PRIMARY KEY (user_id),
    CONSTRAINT fk_remembered_project_project
        FOREIGN KEY (project_id) REFERENCES project (id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Gemerktes Projekt pro Sachbearbeiter (ein Eintrag pro User, kein Verlauf). Teil des project-Moduls. ON DELETE CASCADE: Projektlöschen entfernt Eintrag automatisch.';

CREATE INDEX idx_remembered_project_project_id ON remembered_project (project_id)
    COMMENT 'Beschleunigt Suche aller Sachbearbeiter, die ein bestimmtes Projekt gemerkt haben';


-- =============================================================================
-- PROFILESEARCH-MODUL: KI-gestützte Profilsuche
-- =============================================================================

-- -----------------------------------------------------------------------------
-- profile_search_chat
-- Chat-Sitzungen der KI-gestützten Profilsuche.
-- Kein Optimistic Locking (Sitzungen gehören genau einem Sachbearbeiter).
-- project_id: nur zur Sidebar-Anzeige; ON DELETE SET NULL (Chat bleibt erhalten).
-- -----------------------------------------------------------------------------
CREATE TABLE profile_search_chat (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Technischer Primärschlüssel',
    creation_date DATETIME     NULL                    COMMENT 'Zeitpunkt der Anlage der Sitzung',
    creation_user VARCHAR(255) NULL                    COMMENT 'Sachbearbeiter, dem die Sitzung gehört (ps_user.username)',
    changed_date  DATETIME     NULL                    COMMENT 'Wird bei jeder Chat-Interaktion aktualisiert; Basis für Sidebar-Sortierung',
    title         VARCHAR(255) NULL                    COMMENT 'Sitzungstitel; automatisch aus ersten ~60 Zeichen der ersten Nutzernachricht generiert',
    project_id    BIGINT       NULL                    COMMENT 'FK → project; Projektbezug zum Anlagezeitpunkt (nur Sidebar-Anzeige; kein strukturelles Bindungsmerkmal)',
    PRIMARY KEY (id),
    -- SET NULL: Chat-Verlauf bleibt erhalten, wenn das referenzierte Projekt gelöscht wird
    CONSTRAINT fk_profile_search_chat_project
        FOREIGN KEY (project_id) REFERENCES project (id)
        ON DELETE SET NULL
        ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Chat-Sitzungen der KI-gestützten Profilsuche. Kein Optimistic Locking. Projektbezug nur zur Sidebar-Anzeige (ON DELETE SET NULL).';

-- Laden aller Sitzungen eines Sachbearbeiters (sortiert nach letzter Nutzung)
CREATE INDEX idx_profile_search_chat_user_date ON profile_search_chat (creation_user, changed_date DESC)
    COMMENT 'Beschleunigt Laden der Sidebar-Sitzungsliste eines Sachbearbeiters';

CREATE INDEX idx_profile_search_chat_project_id ON profile_search_chat (project_id)
    COMMENT 'Beschleunigt SET-NULL-Update beim Löschen eines Projekts';


-- -----------------------------------------------------------------------------
-- profile_search_message
-- Einzelne Nachrichten innerhalb einer Chat-Sitzung.
-- role: 'user' (Sachbearbeiter) oder 'assistant' (KI-Antwort).
-- sequence: Reihenfolge innerhalb der Sitzung (aufsteigend).
-- content: bei role='assistant' kann Markdown und [freelancer:<id>:<text>]-Links enthalten.
-- -----------------------------------------------------------------------------
CREATE TABLE profile_search_message (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Technischer Primärschlüssel',
    creation_date DATETIME     NULL                    COMMENT 'Zeitpunkt der Nachricht',
    chat_id       BIGINT       NOT NULL                COMMENT 'FK → profile_search_chat; Zugehörige Sitzung',
    role          VARCHAR(20)  NOT NULL                COMMENT 'Absenderrolle: user (Sachbearbeiter) | assistant (KI)',
    sequence      INT          NOT NULL                COMMENT 'Sequenznummer innerhalb der Sitzung (aufsteigend, für chronologische Darstellung)',
    content       LONGTEXT     NOT NULL                COMMENT 'Nachrichtentext; bei role=assistant: Markdown + [freelancer:<id>:<text>]-Links möglich',
    PRIMARY KEY (id),
    -- Kaskadierendes Löschen: Alle Nachrichten werden mitgelöscht, wenn die Sitzung gelöscht wird
    CONSTRAINT fk_profile_search_message_chat
        FOREIGN KEY (chat_id) REFERENCES profile_search_chat (id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Einzelne Nachrichten einer Profilsuche-Chat-Sitzung. Kaskadierend mitgelöscht wenn Sitzung gelöscht wird.';

CREATE INDEX idx_profile_search_message_chat_seq ON profile_search_message (chat_id, sequence ASC)
    COMMENT 'Beschleunigt Laden aller Nachrichten einer Sitzung in chronologischer Reihenfolge';


-- =============================================================================
-- AUTHENTIFIZIERUNG: Benutzertabelle
-- =============================================================================

-- -----------------------------------------------------------------------------
-- ps_user
-- Anwendungsbenutzer (Sachbearbeiter). Passwort als BCrypt-Hash.
-- Verwaltung ausschließlich direkt in der Datenbank (kein Admin-UI).
-- must_change_password: Erzwingt Passwortänderung beim ersten Login.
-- username dient systemweit als Benutzeridentität (user_id in anderen Tabellen).
-- -----------------------------------------------------------------------------
CREATE TABLE ps_user (
    username             VARCHAR(100) NOT NULL COMMENT 'PK; Login-Name und systemweite Benutzeridentität (entspricht creation_user / changed_user in allen Aggregaten)',
    password_hash        VARCHAR(255) NOT NULL COMMENT 'BCrypt-Hash des Passworts im Spring-Security-Format ({bcrypt}…)',
    must_change_password BOOLEAN      NOT NULL DEFAULT TRUE  COMMENT 'Bei true: Weiterleitung auf /passwort-aendern nach Login; blockiert Zugriff auf alle anderen Seiten',
    enabled              BOOLEAN      NOT NULL DEFAULT TRUE  COMMENT 'Bei false: Benutzer kann sich nicht einloggen (deaktiviertes Konto)',
    PRIMARY KEY (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Anwendungsbenutzer (Sachbearbeiter). Verwaltung direkt per DB; kein Admin-UI. Passwort als BCrypt-Hash. SSO-Migration (Microsoft Entra ID) in späterem Release geplant.';


-- =============================================================================
-- SPRING MODULITH: Event Publication Log
-- =============================================================================

-- -----------------------------------------------------------------------------
-- event_publication
-- Persistenter Log für Spring Modulith Domain Events.
-- Wird von Spring Modulith für zuverlässige Eventauslieferung verwendet:
-- Events werden vor dem Commit geschrieben und nach erfolgreicher Verarbeitung
-- als abgeschlossen markiert (completion_date). Nicht abgeschlossene Events
-- können bei Neustart erneut zugestellt werden.
-- -----------------------------------------------------------------------------
CREATE TABLE event_publication (
    id                CHAR(36)     NOT NULL                COMMENT 'UUID des Publikations-Eintrags',
    listener_id       VARCHAR(512) NOT NULL                COMMENT 'Vollqualifizierter Name des @ApplicationModuleListener, der das Event verarbeitet',
    event_type        VARCHAR(512) NOT NULL                COMMENT 'Vollqualifizierter Java-Klassenname des Domain Events',
    serialized_event  LONGTEXT     NOT NULL                COMMENT 'JSON-serialisiertes Domain-Event-Objekt',
    publication_date  DATETIME(6)  NOT NULL                COMMENT 'Zeitpunkt der Event-Publikation',
    completion_date   DATETIME(6)  NULL                    COMMENT 'Zeitpunkt der erfolgreichen Verarbeitung; NULL = noch ausstehend oder fehlgeschlagen',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Spring Modulith Event Publication Log. Sichert zuverlässige Eventauslieferung über Anwendungsneustarts hinweg. Nicht manuell befüllen.';

CREATE INDEX idx_event_publication_completion ON event_publication (completion_date)
    COMMENT 'Beschleunigt Abfrage ausstehender Events (completion_date IS NULL) beim Anwendungsstart';
