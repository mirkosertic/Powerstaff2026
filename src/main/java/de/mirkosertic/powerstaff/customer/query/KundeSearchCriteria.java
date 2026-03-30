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
        String debitorNr,
        String sortField,
        String sortDir
) {
    public static KundeSearchCriteria empty() {
        return new KundeSearchCriteria(null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public KundeSearchCriteria withCompany(final String v) { return new KundeSearchCriteria(v, name1, name2, street, country, plz, city, comments, kreditorNr, debitorNr, sortField, sortDir); }
    public KundeSearchCriteria withName1(final String v) { return new KundeSearchCriteria(company, v, name2, street, country, plz, city, comments, kreditorNr, debitorNr, sortField, sortDir); }
    public KundeSearchCriteria withName2(final String v) { return new KundeSearchCriteria(company, name1, v, street, country, plz, city, comments, kreditorNr, debitorNr, sortField, sortDir); }
    public KundeSearchCriteria withStreet(final String v) { return new KundeSearchCriteria(company, name1, name2, v, country, plz, city, comments, kreditorNr, debitorNr, sortField, sortDir); }
    public KundeSearchCriteria withCountry(final String v) { return new KundeSearchCriteria(company, name1, name2, street, v, plz, city, comments, kreditorNr, debitorNr, sortField, sortDir); }
    public KundeSearchCriteria withPlz(final String v) { return new KundeSearchCriteria(company, name1, name2, street, country, v, city, comments, kreditorNr, debitorNr, sortField, sortDir); }
    public KundeSearchCriteria withCity(final String v) { return new KundeSearchCriteria(company, name1, name2, street, country, plz, v, comments, kreditorNr, debitorNr, sortField, sortDir); }
    public KundeSearchCriteria withComments(final String v) { return new KundeSearchCriteria(company, name1, name2, street, country, plz, city, v, kreditorNr, debitorNr, sortField, sortDir); }
    public KundeSearchCriteria withKreditorNr(final String v) { return new KundeSearchCriteria(company, name1, name2, street, country, plz, city, comments, v, debitorNr, sortField, sortDir); }
    public KundeSearchCriteria withDebitorNr(final String v) { return new KundeSearchCriteria(company, name1, name2, street, country, plz, city, comments, kreditorNr, v, sortField, sortDir); }
    public KundeSearchCriteria withSortField(final String v) { return new KundeSearchCriteria(company, name1, name2, street, country, plz, city, comments, kreditorNr, debitorNr, v, sortDir); }
    public KundeSearchCriteria withSortDir(final String v) { return new KundeSearchCriteria(company, name1, name2, street, country, plz, city, comments, kreditorNr, debitorNr, sortField, v); }
}
