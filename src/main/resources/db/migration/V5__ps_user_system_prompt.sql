-- =============================================================================
-- V5: Profilsuche-Systemprompt pro Benutzer
-- =============================================================================
-- Jeder Benutzer bekommt einen individuellen Systemprompt für den KI-Assistenten
-- der Profilsuche. Der Default-Wert wird für alle bestehenden Benutzer gesetzt.
-- VARCHAR(4000) statt TEXT, da MySQL TEXT-Spalten keinen DEFAULT-Wert erlaubt.
-- =============================================================================

ALTER TABLE ps_user
    ADD COLUMN profile_search_system_prompt VARCHAR(4000) NOT NULL DEFAULT ''
        COMMENT 'Individueller Systemprompt für den Profilsuche-KI-Assistenten';

-- Allen bestehenden Benutzern den Default-Prompt setzen
UPDATE ps_user
SET profile_search_system_prompt =
        'Du bist ein freundlicher KI-Assistent für den Benutzer {user} und antwortest immer auf deutsch. Dein Name ist Staffi.'
WHERE profile_search_system_prompt = '';
