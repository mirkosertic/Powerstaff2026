-- =============================================================================
-- E2E Testdaten für Powerstaff 2026
-- Wird via Flyway nur im e2e-Profil eingespielt (spring.flyway.locations).
-- Idempotent: jede INSERT nutzt INSERT IGNORE oder ON DUPLICATE KEY.
-- =============================================================================

-- Testbenutzer ({noop}testpass = Spring Security DelegatingPasswordEncoder-Format)
-- testuser ist Administrator (wird in E2E-Tests für Admin-Funktionen verwendet)
INSERT INTO ps_user (username, password_hash, must_change_password, enabled, is_admin)
VALUES ('testuser', '{noop}testpass', FALSE, TRUE, TRUE)
ON DUPLICATE KEY UPDATE password_hash = '{noop}testpass', must_change_password = FALSE, enabled = TRUE, is_admin = TRUE;

-- Nicht-Administrator-Testbenutzer (für Rollen-E2E-Tests)
INSERT INTO ps_user (username, password_hash, must_change_password, enabled, is_admin)
VALUES ('testuser-noadmin', '{noop}testpass', FALSE, TRUE, FALSE)
ON DUPLICATE KEY UPDATE password_hash = '{noop}testpass', must_change_password = FALSE, enabled = TRUE, is_admin = FALSE;

-- Historientypen
INSERT IGNORE INTO historytype (description) VALUES
    ('Telefonat'),
    ('E-Mail');

-- Positionsstatus (einer davon als Default)
INSERT IGNORE INTO project_position_status (description, color, color_text, is_default) VALUES
    ('Vorgeschlagen', '#dbeafe', '#1e40af', TRUE),
    ('Im Gespräch',   '#fef9c3', '#713f12', FALSE);

-- Tags
INSERT IGNORE INTO tags (tagname, type) VALUES
    ('Java',    'SCHWERPUNKT'),
    ('München', 'EINSATZORT');

-- Partner (3 Stück für Sortierungstests)
INSERT IGNORE INTO partner
    (id, db_version, name1, company, street, country, plz, city, contactforbidden,
     creation_date, creation_user, changed_date, changed_user)
VALUES
    (2001, 0, 'Partner', 'Partner GmbH', 'Partnerstr. 10', 'DEU', '50667', 'Köln', FALSE,
     NOW(), 'testuser', NOW(), 'testuser'),
    (2003, 0, 'Alpha', 'Alpha Partner AG', 'Alphaweg 1', 'DEU', '60311', 'Frankfurt', FALSE,
     NOW(), 'testuser', NOW(), 'testuser'),
    (2004, 0, 'Zeta', 'Zeta GmbH', 'Zetastr. 99', 'DEU', '70173', 'Stuttgart', FALSE,
     NOW(), 'testuser', NOW(), 'testuser')
ON DUPLICATE KEY UPDATE db_version = db_version;

-- Freiberufler (3 Stück)
INSERT IGNORE INTO freelancer
    (id, db_version, name1, name2, company, street, country, plz, city,
     code, contactforbidden, kontaktart, partner_id,
     creation_date, creation_user, changed_date, changed_user)
VALUES
    (1001, 0, 'Mustermann', 'Max', NULL, 'Musterstr. 1', 'DEU', '80331', 'München',
     'E2E-001', FALSE, 'NL', 2001,
     NOW(), 'testuser', NOW(), 'testuser'),
    (1002, 0, 'Beispiel', 'Erika', NULL, 'Beispielweg 2', 'DEU', '10115', 'Berlin',
     'E2E-002', FALSE, 'NL', NULL,
     NOW(), 'testuser', NOW(), 'testuser'),
    (1003, 0, 'Test', 'Klaus', 'TestGmbH', 'Testgasse 3', 'DEU', '20095', 'Hamburg',
     'E2E-003', FALSE, 'NL', NULL,
     NOW(), 'testuser', NOW(), 'testuser')
ON DUPLICATE KEY UPDATE db_version = db_version;

-- Kunde (3 Stück für Sortierungstests)
INSERT IGNORE INTO kunde
    (id, db_version, name1, company, street, country, plz, city, contactforbidden,
     creation_date, creation_user, changed_date, changed_user)
VALUES
    (3001, 0, 'Kunde', 'Kunde AG', 'Kundenallee 5', 'DEU', '70173', 'Stuttgart', FALSE,
     NOW(), 'testuser', NOW(), 'testuser'),
    (3003, 0, 'Beta', 'Beta Kunden GmbH', 'Betaweg 20', 'DEU', '80331', 'München', FALSE,
     NOW(), 'testuser', NOW(), 'testuser'),
    (3004, 0, 'Gamma', 'Gamma AG', 'Gammastr. 7', 'DEU', '10115', 'Berlin', FALSE,
     NOW(), 'testuser', NOW(), 'testuser')
ON DUPLICATE KEY UPDATE db_version = db_version;

-- Projekt (4 Stück für Sortierungstests)
INSERT IGNORE INTO project
    (id, db_version, project_number, description_short, status, customer_id, partner_id,
     creation_date, creation_user, changed_date, changed_user)
VALUES
    (4001, 0, 'E2E-P001', 'E2E Testprojekt', 1, 3001, NULL,
     NOW(), 'testuser', NOW(), 'testuser'),
    (4002, 0, 'E2E-P002', 'E2E Partnerprojekt', 4, NULL, 2001,
     NOW(), 'testuser', NOW(), 'testuser'),
    (4003, 0, 'E2E-P003', 'E2E Alpha Projekt', 2, 3001, NULL,
     NOW(), 'testuser', NOW(), 'testuser'),
    (4004, 0, 'E2E-P004', 'E2E Beta Projekt', 1, 3003, NULL,
     NOW(), 'testuser', NOW(), 'testuser')
ON DUPLICATE KEY UPDATE db_version = db_version;

-- Projektposition (Freiberufler 1001 dem Projekt 4001 zugeordnet)
INSERT IGNORE INTO project_position
    (id, db_version, project_id, freelancer_id, status_id, konditionen, kommentar,
     creation_date, creation_user, changed_date, changed_user)
VALUES
    (5001, 0, 4001, 1001,
     (SELECT id FROM project_position_status WHERE is_default = TRUE LIMIT 1),
     '500 EUR/Tag', 'E2E Testposition',
     NOW(), 'testuser', NOW(), 'testuser')
ON DUPLICATE KEY UPDATE db_version = db_version;

-- Kontakthistorie für Freiberufler 1001 (freelancer_history hat keine db_version-Spalte)
INSERT IGNORE INTO freelancer_history
    (id, freelancer_id, type_id, description,
     creation_date, creation_user, changed_date, changed_user)
VALUES
    (6001, 1001,
     (SELECT id FROM historytype WHERE description = 'Telefonat' LIMIT 1),
     'Erstgespräch via Telefon.',
     NOW(), 'testuser', NOW(), 'testuser');

-- Kontaktmöglichkeit für Freiberufler 1001 (freelancer_contact hat keine db_version-Spalte)
INSERT IGNORE INTO freelancer_contact
    (id, freelancer_id, type, value,
     creation_date, creation_user, changed_date, changed_user)
VALUES
    (7001, 1001, 'EMAIL', 'max.mustermann@e2e.test',
     NOW(), 'testuser', NOW(), 'testuser');

-- Kontaktsperre-Testdaten für E2E-Tests
INSERT IGNORE INTO freelancer
    (id, db_version, name1, name2, company, street, country, plz, city,
     code, contactforbidden, kontaktart,
     creation_date, creation_user, changed_date, changed_user)
VALUES
    (1004, 0, 'Gesperrt', 'Franz', NULL, 'Sperrstr. 1', 'DEU', '12345', 'Berlin',
     'E2E-004', TRUE, 'NL',
     NOW(), 'testuser', NOW(), 'testuser')
ON DUPLICATE KEY UPDATE db_version = db_version;

INSERT IGNORE INTO partner
    (id, db_version, name1, company, street, country, plz, city, contactforbidden,
     creation_date, creation_user, changed_date, changed_user)
VALUES
    (2002, 0, 'Gesperrt', 'Gesperrt GmbH', 'Sperrweg 2', 'DEU', '50667', 'Köln', TRUE,
     NOW(), 'testuser', NOW(), 'testuser')
ON DUPLICATE KEY UPDATE db_version = db_version;

INSERT IGNORE INTO kunde
    (id, db_version, name1, company, street, country, plz, city, contactforbidden,
     creation_date, creation_user, changed_date, changed_user)
VALUES
    (3002, 0, 'Gesperrt', 'Gesperrt AG', 'Sperrplatz 3', 'DEU', '70173', 'Stuttgart', TRUE,
     NOW(), 'testuser', NOW(), 'testuser')
ON DUPLICATE KEY UPDATE db_version = db_version;

INSERT IGNORE INTO project_position
    (id, db_version, project_id, freelancer_id, status_id, konditionen, kommentar,
     creation_date, creation_user, changed_date, changed_user)
VALUES
    (5002, 0, 4001, 1004,
     (SELECT id FROM project_position_status WHERE is_default = TRUE LIMIT 1),
     '400 EUR/Tag', 'E2E Testposition Kontaktsperre',
     NOW(), 'testuser', NOW(), 'testuser')
ON DUPLICATE KEY UPDATE db_version = db_version;

-- Tag-Zuordnungen für E2E-Tests (Freelancer 1001 hat Java + München)
INSERT IGNORE INTO freelancer_tags (freelancer_id, tag_id)
VALUES
    (1001, (SELECT id FROM tags WHERE tagname = 'Java' LIMIT 1)),
    (1001, (SELECT id FROM tags WHERE tagname = 'München' LIMIT 1));
