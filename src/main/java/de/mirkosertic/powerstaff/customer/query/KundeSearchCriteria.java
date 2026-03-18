package de.mirkosertic.powerstaff.customer.query;

public record KundeSearchCriteria(
        String company,
        String name1,
        String name2,
        String street,
        String country,
        String plz,
        String city,
        String comments,
        String kreditorNr,
        String debitorNr
) {
}
