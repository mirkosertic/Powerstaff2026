package de.mirkosertic.powerstaff.project.query;

import java.time.LocalDateTime;

public record ProjectHistoryView(
        Long id,
        LocalDateTime creationDate,
        String creationUser,
        LocalDateTime changedDate,
        String changedUser,
        String description,
        Long projectId
) {
}
