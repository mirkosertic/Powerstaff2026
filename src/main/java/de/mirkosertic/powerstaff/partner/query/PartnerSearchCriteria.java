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
        String kreditorNr,
        String sortField,
        String sortDir
) {
    public static PartnerSearchCriteria empty() {
        return new PartnerSearchCriteria(null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public PartnerSearchCriteria withCompany(final String v) { return new PartnerSearchCriteria(v, name1, name2, street, country, plz, city, comments, debitorNr, kreditorNr, sortField, sortDir); }
    public PartnerSearchCriteria withName1(final String v) { return new PartnerSearchCriteria(company, v, name2, street, country, plz, city, comments, debitorNr, kreditorNr, sortField, sortDir); }
    public PartnerSearchCriteria withName2(final String v) { return new PartnerSearchCriteria(company, name1, v, street, country, plz, city, comments, debitorNr, kreditorNr, sortField, sortDir); }
    public PartnerSearchCriteria withStreet(final String v) { return new PartnerSearchCriteria(company, name1, name2, v, country, plz, city, comments, debitorNr, kreditorNr, sortField, sortDir); }
    public PartnerSearchCriteria withCountry(final String v) { return new PartnerSearchCriteria(company, name1, name2, street, v, plz, city, comments, debitorNr, kreditorNr, sortField, sortDir); }
    public PartnerSearchCriteria withPlz(final String v) { return new PartnerSearchCriteria(company, name1, name2, street, country, v, city, comments, debitorNr, kreditorNr, sortField, sortDir); }
    public PartnerSearchCriteria withCity(final String v) { return new PartnerSearchCriteria(company, name1, name2, street, country, plz, v, comments, debitorNr, kreditorNr, sortField, sortDir); }
    public PartnerSearchCriteria withComments(final String v) { return new PartnerSearchCriteria(company, name1, name2, street, country, plz, city, v, debitorNr, kreditorNr, sortField, sortDir); }
    public PartnerSearchCriteria withDebitorNr(final String v) { return new PartnerSearchCriteria(company, name1, name2, street, country, plz, city, comments, v, kreditorNr, sortField, sortDir); }
    public PartnerSearchCriteria withKreditorNr(final String v) { return new PartnerSearchCriteria(company, name1, name2, street, country, plz, city, comments, debitorNr, v, sortField, sortDir); }
    public PartnerSearchCriteria withSortField(final String v) { return new PartnerSearchCriteria(company, name1, name2, street, country, plz, city, comments, debitorNr, kreditorNr, v, sortDir); }
    public PartnerSearchCriteria withSortDir(final String v) { return new PartnerSearchCriteria(company, name1, name2, street, country, plz, city, comments, debitorNr, kreditorNr, sortField, v); }
}
