package de.mirkosertic.powerstaff.profilesearch.query;

import de.mirkosertic.powerstaff.shared.query.TagView;

import java.time.LocalDateTime;
import java.util.List;

public record ProfileSearchResult(
        Long id,
        String code,
        String name1,
        String name2,
        LocalDateTime lastContactDate,
        Long salaryPerDayLong,
        LocalDateTime availabilityAsDate,
        boolean contactForbidden,
        List<TagView> tags
) {
}
