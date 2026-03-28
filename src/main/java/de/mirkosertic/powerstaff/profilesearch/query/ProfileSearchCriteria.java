package de.mirkosertic.powerstaff.profilesearch.query;

public record ProfileSearchCriteria(
        String searchTerm,
        Long salaryPerDayFrom,
        Long salaryPerDayTo,
        String tagIds,
        String sortField,
        String sortDir
) {
}
