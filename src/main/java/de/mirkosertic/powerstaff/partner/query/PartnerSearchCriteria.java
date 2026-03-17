package de.mirkosertic.powerstaff.partner.query;

public record PartnerSearchCriteria(
        String company,
        String name1,
        String name2,
        String street,
        String country,
        String plz,
        String city,
        String comments,
        String debitorNr,
        String kreditorNr
) {
    public static PartnerSearchCriteria empty() {
        return new PartnerSearchCriteria(null, null, null, null, null, null, null, null, null, null);
    }
}
