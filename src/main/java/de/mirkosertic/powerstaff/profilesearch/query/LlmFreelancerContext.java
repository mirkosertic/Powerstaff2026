package de.mirkosertic.powerstaff.profilesearch.query;

import java.util.List;

public record LlmFreelancerContext(
        String code,
        String name1,
        String name2,
        String skills,
        List<String> tags,
        String positionStatus,
        String konditionen,
        String kommentar
) {
}
