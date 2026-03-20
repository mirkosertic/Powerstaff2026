-- =============================================================================
-- V2: Initialer Admin-Benutzer
-- =============================================================================
-- Legt einen Standard-Admin-Benutzer an, der sofort aktiv ist und kein
-- Passwort-Reset erfordert.
-- Passwort: "admin" (BCrypt-Hash, cost 10)
-- Das Hash-Format folgt ADR-018: {bcrypt} Prefix für DelegatingPasswordEncoder.
-- =============================================================================

INSERT INTO ps_user (username, password_hash, must_change_password, enabled)
VALUES ('admin',
        '{bcrypt}$2y$10$3jzssHdOuIoUJs3syUjfxeD2Bj.chURCbSyrDuI8KEGWnntbX/2XS',
        FALSE,
        TRUE)
ON DUPLICATE KEY UPDATE username = username;
