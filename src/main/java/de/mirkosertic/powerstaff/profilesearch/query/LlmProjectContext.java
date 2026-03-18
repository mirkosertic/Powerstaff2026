package de.mirkosertic.powerstaff.profilesearch.query;

import java.time.LocalDateTime;
import java.util.List;

public record LlmProjectContext(
        String projectNumber,
        String descriptionShort,
        String descriptionLong,
        String workplace,
        String skills,
        String duration,
        LocalDateTime startDate,
        String statusLabel,
        Long stundensatzVk,
        List<LlmFreelancerContext> positions
) {
}
