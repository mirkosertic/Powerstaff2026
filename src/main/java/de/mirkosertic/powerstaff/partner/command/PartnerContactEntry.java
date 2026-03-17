package de.mirkosertic.powerstaff.partner.command;

/**
 * DTO für einen Kontakteintrag im Unified-Save-Request.
 * id == null bedeutet: neuer Eintrag (INSERT); id != null: bestehender Eintrag (UPDATE).
 */
public record PartnerContactEntry(Long id, String type, String value) {
}
