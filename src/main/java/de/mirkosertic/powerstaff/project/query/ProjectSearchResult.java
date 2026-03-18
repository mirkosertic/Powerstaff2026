package de.mirkosertic.powerstaff.project.query;

import java.time.LocalDateTime;

public record ProjectSearchResult(
        Long id,
        String projectNumber,
        String descriptionShort,
        String workplace,
        LocalDateTime startDate,
        int status,
        Long stundensatzVk
) {
}
