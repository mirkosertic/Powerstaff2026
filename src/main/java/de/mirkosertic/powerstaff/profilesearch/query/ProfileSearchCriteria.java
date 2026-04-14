package de.mirkosertic.powerstaff.profilesearch.query;

public record ProfileSearchCriteria(
        String searchTerm,
        Long salaryPerDayFrom,
        Long salaryPerDayTo,
        String tagIds,
        String sortField,
        String sortDir,
        Boolean semanticSearch,
        Float similarityThreshold
) {
    public static ProfileSearchCriteria empty() {
        return new ProfileSearchCriteria(null, null, null, null, null, null, null, null);
    }

    public boolean isSemanticSearchActive() {
        return Boolean.TRUE.equals(semanticSearch);
    }

    public float effectiveSimilarityThreshold() {
        return similarityThreshold != null ? similarityThreshold : 0.80f;
    }

    public ProfileSearchCriteria withSearchTerm(final String v)         { return new ProfileSearchCriteria(v, salaryPerDayFrom, salaryPerDayTo, tagIds, sortField, sortDir, semanticSearch, similarityThreshold); }
    public ProfileSearchCriteria withSalaryPerDayFrom(final Long v)     { return new ProfileSearchCriteria(searchTerm, v, salaryPerDayTo, tagIds, sortField, sortDir, semanticSearch, similarityThreshold); }
    public ProfileSearchCriteria withSalaryPerDayTo(final Long v)       { return new ProfileSearchCriteria(searchTerm, salaryPerDayFrom, v, tagIds, sortField, sortDir, semanticSearch, similarityThreshold); }
    public ProfileSearchCriteria withTagIds(final String v)             { return new ProfileSearchCriteria(searchTerm, salaryPerDayFrom, salaryPerDayTo, v, sortField, sortDir, semanticSearch, similarityThreshold); }
    public ProfileSearchCriteria withSortField(final String v)          { return new ProfileSearchCriteria(searchTerm, salaryPerDayFrom, salaryPerDayTo, tagIds, v, sortDir, semanticSearch, similarityThreshold); }
    public ProfileSearchCriteria withSortDir(final String v)            { return new ProfileSearchCriteria(searchTerm, salaryPerDayFrom, salaryPerDayTo, tagIds, sortField, v, semanticSearch, similarityThreshold); }
    public ProfileSearchCriteria withSemanticSearch(final Boolean v)    { return new ProfileSearchCriteria(searchTerm, salaryPerDayFrom, salaryPerDayTo, tagIds, sortField, sortDir, v, similarityThreshold); }
    public ProfileSearchCriteria withSimilarityThreshold(final Float v) { return new ProfileSearchCriteria(searchTerm, salaryPerDayFrom, salaryPerDayTo, tagIds, sortField, sortDir, semanticSearch, v); }
}
