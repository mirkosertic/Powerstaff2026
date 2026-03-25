package de.mirkosertic.powerstaff.customer.query;

import de.mirkosertic.powerstaff.shared.ProjectStatus;

import java.time.LocalDateTime;

public record KundeProjectListItem(
        Long id,
        String projectNumber,
        String descriptionShort,
        String workplace,
        LocalDateTime startDate,
        int status
) {
    public String statusLabel() {
        return ProjectStatus.fromInt(status).getLabel();
    }
}
