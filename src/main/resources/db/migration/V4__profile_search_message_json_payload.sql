-- =============================================================================
-- V4: JSON-Payload-Feld für ProfileSearchMessage
-- =============================================================================
-- Nachrichten im Profilsuche-Chat können jetzt einen optionalen strukturierten
-- JSON-Payload (beliebige Länge) tragen, z. B. für Tool-Call-Ergebnisse.
-- =============================================================================

ALTER TABLE profile_search_message
    ADD COLUMN json_payload LONGTEXT NULL COMMENT 'Optionaler JSON-Payload einer Nachricht (z. B. Tool-Call-Ergebnis)';
