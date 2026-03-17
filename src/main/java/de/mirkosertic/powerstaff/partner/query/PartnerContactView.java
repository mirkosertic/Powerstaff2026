package de.mirkosertic.powerstaff.partner.query;

public record PartnerContactView(
        Long id,
        String type,
        String value,
        Long partnerId
) {
}
