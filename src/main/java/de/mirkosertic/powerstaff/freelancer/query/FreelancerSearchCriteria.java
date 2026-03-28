package de.mirkosertic.powerstaff.freelancer.query;

public record FreelancerSearchCriteria(
        String name1,
        String name2,
        String company,
        String street,
        String country,
        String plz,
        String city,
        String nationalitaet,
        String comments,
        String einsatzdetails,
        String contactPerson,
        String contactReason,
        String kontaktart,
        String debitorNr,
        String gulpId,
        String code,
        String skills,
        Long salaryLongMax,
        Long salaryPerDayLongMax,
        String sortField,
        String sortDir,
        Long tagId
) {
    public static FreelancerSearchCriteria empty() {
        return new FreelancerSearchCriteria(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public FreelancerSearchCriteria withName1(String v) { return new FreelancerSearchCriteria(v, name2, company, street, country, plz, city, nationalitaet, comments, einsatzdetails, contactPerson, contactReason, kontaktart, debitorNr, gulpId, code, skills, salaryLongMax, salaryPerDayLongMax, sortField, sortDir, tagId); }
    public FreelancerSearchCriteria withName2(String v) { return new FreelancerSearchCriteria(name1, v, company, street, country, plz, city, nationalitaet, comments, einsatzdetails, contactPerson, contactReason, kontaktart, debitorNr, gulpId, code, skills, salaryLongMax, salaryPerDayLongMax, sortField, sortDir, tagId); }
    public FreelancerSearchCriteria withCompany(String v) { return new FreelancerSearchCriteria(name1, name2, v, street, country, plz, city, nationalitaet, comments, einsatzdetails, contactPerson, contactReason, kontaktart, debitorNr, gulpId, code, skills, salaryLongMax, salaryPerDayLongMax, sortField, sortDir, tagId); }
    public FreelancerSearchCriteria withStreet(String v) { return new FreelancerSearchCriteria(name1, name2, company, v, country, plz, city, nationalitaet, comments, einsatzdetails, contactPerson, contactReason, kontaktart, debitorNr, gulpId, code, skills, salaryLongMax, salaryPerDayLongMax, sortField, sortDir, tagId); }
    public FreelancerSearchCriteria withCountry(String v) { return new FreelancerSearchCriteria(name1, name2, company, street, v, plz, city, nationalitaet, comments, einsatzdetails, contactPerson, contactReason, kontaktart, debitorNr, gulpId, code, skills, salaryLongMax, salaryPerDayLongMax, sortField, sortDir, tagId); }
    public FreelancerSearchCriteria withPlz(String v) { return new FreelancerSearchCriteria(name1, name2, company, street, country, v, city, nationalitaet, comments, einsatzdetails, contactPerson, contactReason, kontaktart, debitorNr, gulpId, code, skills, salaryLongMax, salaryPerDayLongMax, sortField, sortDir, tagId); }
    public FreelancerSearchCriteria withCity(String v) { return new FreelancerSearchCriteria(name1, name2, company, street, country, plz, v, nationalitaet, comments, einsatzdetails, contactPerson, contactReason, kontaktart, debitorNr, gulpId, code, skills, salaryLongMax, salaryPerDayLongMax, sortField, sortDir, tagId); }
    public FreelancerSearchCriteria withNationalitaet(String v) { return new FreelancerSearchCriteria(name1, name2, company, street, country, plz, city, v, comments, einsatzdetails, contactPerson, contactReason, kontaktart, debitorNr, gulpId, code, skills, salaryLongMax, salaryPerDayLongMax, sortField, sortDir, tagId); }
    public FreelancerSearchCriteria withComments(String v) { return new FreelancerSearchCriteria(name1, name2, company, street, country, plz, city, nationalitaet, v, einsatzdetails, contactPerson, contactReason, kontaktart, debitorNr, gulpId, code, skills, salaryLongMax, salaryPerDayLongMax, sortField, sortDir, tagId); }
    public FreelancerSearchCriteria withEinsatzdetails(String v) { return new FreelancerSearchCriteria(name1, name2, company, street, country, plz, city, nationalitaet, comments, v, contactPerson, contactReason, kontaktart, debitorNr, gulpId, code, skills, salaryLongMax, salaryPerDayLongMax, sortField, sortDir, tagId); }
    public FreelancerSearchCriteria withContactPerson(String v) { return new FreelancerSearchCriteria(name1, name2, company, street, country, plz, city, nationalitaet, comments, einsatzdetails, v, contactReason, kontaktart, debitorNr, gulpId, code, skills, salaryLongMax, salaryPerDayLongMax, sortField, sortDir, tagId); }
    public FreelancerSearchCriteria withContactReason(String v) { return new FreelancerSearchCriteria(name1, name2, company, street, country, plz, city, nationalitaet, comments, einsatzdetails, contactPerson, v, kontaktart, debitorNr, gulpId, code, skills, salaryLongMax, salaryPerDayLongMax, sortField, sortDir, tagId); }
    public FreelancerSearchCriteria withKontaktart(String v) { return new FreelancerSearchCriteria(name1, name2, company, street, country, plz, city, nationalitaet, comments, einsatzdetails, contactPerson, contactReason, v, debitorNr, gulpId, code, skills, salaryLongMax, salaryPerDayLongMax, sortField, sortDir, tagId); }
    public FreelancerSearchCriteria withDebitorNr(String v) { return new FreelancerSearchCriteria(name1, name2, company, street, country, plz, city, nationalitaet, comments, einsatzdetails, contactPerson, contactReason, kontaktart, v, gulpId, code, skills, salaryLongMax, salaryPerDayLongMax, sortField, sortDir, tagId); }
    public FreelancerSearchCriteria withGulpId(String v) { return new FreelancerSearchCriteria(name1, name2, company, street, country, plz, city, nationalitaet, comments, einsatzdetails, contactPerson, contactReason, kontaktart, debitorNr, v, code, skills, salaryLongMax, salaryPerDayLongMax, sortField, sortDir, tagId); }
    public FreelancerSearchCriteria withCode(String v) { return new FreelancerSearchCriteria(name1, name2, company, street, country, plz, city, nationalitaet, comments, einsatzdetails, contactPerson, contactReason, kontaktart, debitorNr, gulpId, v, skills, salaryLongMax, salaryPerDayLongMax, sortField, sortDir, tagId); }
    public FreelancerSearchCriteria withSkills(String v) { return new FreelancerSearchCriteria(name1, name2, company, street, country, plz, city, nationalitaet, comments, einsatzdetails, contactPerson, contactReason, kontaktart, debitorNr, gulpId, code, v, salaryLongMax, salaryPerDayLongMax, sortField, sortDir, tagId); }
    public FreelancerSearchCriteria withSalaryLongMax(Long v) { return new FreelancerSearchCriteria(name1, name2, company, street, country, plz, city, nationalitaet, comments, einsatzdetails, contactPerson, contactReason, kontaktart, debitorNr, gulpId, code, skills, v, salaryPerDayLongMax, sortField, sortDir, tagId); }
    public FreelancerSearchCriteria withSalaryPerDayLongMax(Long v) { return new FreelancerSearchCriteria(name1, name2, company, street, country, plz, city, nationalitaet, comments, einsatzdetails, contactPerson, contactReason, kontaktart, debitorNr, gulpId, code, skills, salaryLongMax, v, sortField, sortDir, tagId); }
    public FreelancerSearchCriteria withSortField(String v) { return new FreelancerSearchCriteria(name1, name2, company, street, country, plz, city, nationalitaet, comments, einsatzdetails, contactPerson, contactReason, kontaktart, debitorNr, gulpId, code, skills, salaryLongMax, salaryPerDayLongMax, v, sortDir, tagId); }
    public FreelancerSearchCriteria withSortDir(String v) { return new FreelancerSearchCriteria(name1, name2, company, street, country, plz, city, nationalitaet, comments, einsatzdetails, contactPerson, contactReason, kontaktart, debitorNr, gulpId, code, skills, salaryLongMax, salaryPerDayLongMax, sortField, v, tagId); }
    public FreelancerSearchCriteria withTagId(Long v) { return new FreelancerSearchCriteria(name1, name2, company, street, country, plz, city, nationalitaet, comments, einsatzdetails, contactPerson, contactReason, kontaktart, debitorNr, gulpId, code, skills, salaryLongMax, salaryPerDayLongMax, sortField, sortDir, v); }
}
