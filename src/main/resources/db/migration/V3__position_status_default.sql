-- ADR: Positionsstatus kann als Standard markiert werden.
-- Beim Zuordnen eines Freiberuflers zu einem Projekt wird automatisch der Default-Status verwendet,
-- wenn kein expliziter Status angegeben wird.
ALTER TABLE project_position_status
    ADD COLUMN is_default BOOLEAN NOT NULL DEFAULT FALSE
        COMMENT 'Genau ein Datensatz darf true sein; wird beim Anlegen einer neuen Projektposition als Standardstatus verwendet.';
