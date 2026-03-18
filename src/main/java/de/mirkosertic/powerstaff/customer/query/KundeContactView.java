package de.mirkosertic.powerstaff.customer.query;

public record KundeContactView(
        Long id,
        String type,
        String value,
        Long kundeId
) {
}
