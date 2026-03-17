package de.mirkosertic.powerstaff.partner.command;

/**
 * DTO für einen Historieneintrag im Unified-Save-Request.
 * id == null bedeutet: neuer Eintrag (INSERT); id != null: bestehender Eintrag (UPDATE).
 */
public record PartnerHistoryEntry(Long id, Long typeId, String description) {
}
