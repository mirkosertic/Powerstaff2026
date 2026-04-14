package de.mirkosertic.powerstaff.profilesearch.query;

import java.util.List;

public record ProfileSearchPage(List<ProfileSearchResult> results, Long totalHits) {}
