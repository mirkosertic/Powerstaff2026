package de.mirkosertic.powerstaff.customer.query;

import java.time.LocalDateTime;

public record KundeProjectListItem(
        Long id,
        String projectNumber,
        String descriptionShort,
        String workplace,
        LocalDateTime startDate,
        int status
) {
}
