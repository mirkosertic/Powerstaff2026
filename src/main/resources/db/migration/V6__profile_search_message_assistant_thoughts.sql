-- =============================================================================
-- V6: assistant_thoughts-Feld für ProfileSearchMessage
-- =============================================================================
-- Optionales Feld für interne Gedanken/Überlegungen des KI-Agenten,
-- die nicht als sichtbare Antwortnachricht erscheinen.
-- =============================================================================

ALTER TABLE profile_search_message
    ADD COLUMN assistant_thoughts LONGTEXT NULL COMMENT 'Gedanken des Agenten';
