-- Administratorrolle für Benutzer
ALTER TABLE ps_user
    ADD COLUMN is_admin BOOLEAN NOT NULL DEFAULT FALSE
        COMMENT 'TRUE = Administrator; darf alle Admin-Funktionen nutzen';

-- Initialer Admin-Benutzer erhält die Administratorrolle
UPDATE ps_user SET is_admin = TRUE WHERE username = 'admin';
