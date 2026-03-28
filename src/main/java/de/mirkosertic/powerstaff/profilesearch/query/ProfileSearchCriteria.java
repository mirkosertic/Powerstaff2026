package de.mirkosertic.powerstaff.profilesearch.query;

public record ProfileSearchCriteria(
        String searchTerm,
        Long salaryPerDayFrom,
        Long salaryPerDayTo,
        String tagIds,
        String sortField,
        String sortDir,
        Boolean semanticSearch
) {
    public static ProfileSearchCriteria empty() {
        return new ProfileSearchCriteria(null, null, null, null, null, null, null);
    }

    public boolean isSemanticSearchActive() {
        return Boolean.TRUE.equals(semanticSearch);
    }

    public ProfileSearchCriteria withSearchTerm(String v) { return new ProfileSearchCriteria(v, salaryPerDayFrom, salaryPerDayTo, tagIds, sortField, sortDir, semanticSearch); }
    public ProfileSearchCriteria withSalaryPerDayFrom(Long v) { return new ProfileSearchCriteria(searchTerm, v, salaryPerDayTo, tagIds, sortField, sortDir, semanticSearch); }
    public ProfileSearchCriteria withSalaryPerDayTo(Long v) { return new ProfileSearchCriteria(searchTerm, salaryPerDayFrom, v, tagIds, sortField, sortDir, semanticSearch); }
    public ProfileSearchCriteria withTagIds(String v) { return new ProfileSearchCriteria(searchTerm, salaryPerDayFrom, salaryPerDayTo, v, sortField, sortDir, semanticSearch); }
    public ProfileSearchCriteria withSortField(String v) { return new ProfileSearchCriteria(searchTerm, salaryPerDayFrom, salaryPerDayTo, tagIds, v, sortDir, semanticSearch); }
    public ProfileSearchCriteria withSortDir(String v) { return new ProfileSearchCriteria(searchTerm, salaryPerDayFrom, salaryPerDayTo, tagIds, sortField, v, semanticSearch); }
    public ProfileSearchCriteria withSemanticSearch(Boolean v) { return new ProfileSearchCriteria(searchTerm, salaryPerDayFrom, salaryPerDayTo, tagIds, sortField, sortDir, v); }
}
