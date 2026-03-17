package de.mirkosertic.powerstaff.freelancer.query;

import java.time.LocalDateTime;

public record FreelancerDetailView(
        Long id,
        Long dbVersion,
        LocalDateTime creationDate,
        String creationUser,
        LocalDateTime changedDate,
        String changedUser,
        String titel,
        String name1,
        String name2,
        String company,
        String street,
        String country,
        String plz,
        String city,
        String nationalitaet,
        String geburtsdatum,
        Long partnerId,
        boolean contactForbidden,
        boolean showAgain,
        String comments,
        String einsatzdetails,
        String contactPerson,
        String contactType,
        String contactReason,
        LocalDateTime lastContactDate,
        String kontaktart,
        LocalDateTime availabilityAsDate,
        Long salaryLong,
        Long salaryPerDayLong,
        Long salaryRemote,
        Long salaryPartnerLong,
        Long salaryPartnerPerDayLong,
        boolean datenschutz,
        String debitorNr,
        String gulpId,
        String code,
        String skills
) {
}
