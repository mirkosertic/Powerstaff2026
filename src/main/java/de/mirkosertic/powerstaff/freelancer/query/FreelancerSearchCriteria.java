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
        Long salaryPerDayLongMax
) {
}
