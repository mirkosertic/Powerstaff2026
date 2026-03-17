package de.mirkosertic.powerstaff.partner.query;

public record PartnerSearchResult(
        Long id,
        String company,
        String name1,
        String name2,
        String city
) {
}
