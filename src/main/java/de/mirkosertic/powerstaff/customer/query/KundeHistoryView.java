package de.mirkosertic.powerstaff.customer.query;

import java.time.LocalDateTime;

public record KundeHistoryView(
        Long id,
        LocalDateTime creationDate,
        String creationUser,
        LocalDateTime changedDate,
        String changedUser,
        String description,
        Long typeId,
        String typeDescription,
        Long kundeId
) {
}
