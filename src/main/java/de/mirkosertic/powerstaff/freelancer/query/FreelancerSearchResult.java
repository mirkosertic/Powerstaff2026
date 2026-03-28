package de.mirkosertic.powerstaff.freelancer.query;

import java.time.LocalDateTime;

public record FreelancerSearchResult(
        Long id,
        String code,
        String name1,
        String name2,
        String company,
        String city,
        LocalDateTime availabilityAsDate,
        Long salaryLong,
        Long salaryPerDayLong,
        String skills,
        Boolean contactForbidden
) {
}
