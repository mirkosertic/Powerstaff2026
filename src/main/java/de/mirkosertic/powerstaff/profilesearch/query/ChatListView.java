package de.mirkosertic.powerstaff.profilesearch.query;

import java.time.LocalDateTime;

public record ChatListView(
        Long id,
        LocalDateTime creationDate,
        String creationUser,
        LocalDateTime changedDate,
        String title,
        Long projectId,
        String projectNumber
) {
}
