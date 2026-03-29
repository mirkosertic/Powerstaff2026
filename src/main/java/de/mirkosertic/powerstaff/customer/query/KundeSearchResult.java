package de.mirkosertic.powerstaff.customer.query;

public record KundeSearchResult(
        Long id,
        String company,
        String name1,
        String name2,
        String city,
        Boolean contactForbidden
) {
}
