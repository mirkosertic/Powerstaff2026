package de.mirkosertic.powerstaff.freelancer.query;

import de.mirkosertic.powerstaff.shared.query.TagView;

import java.time.LocalDateTime;
import java.util.List;

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
        Boolean contactForbidden,
        List<TagView> tags
) {
}
