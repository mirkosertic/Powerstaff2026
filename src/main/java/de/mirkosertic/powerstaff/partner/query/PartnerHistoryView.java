package de.mirkosertic.powerstaff.partner.query;

import java.time.LocalDateTime;

public record PartnerHistoryView(
        Long id,
        LocalDateTime creationDate,
        String creationUser,
        LocalDateTime changedDate,
        String changedUser,
        String description,
        Long typeId,
        String typeDescription,
        Long partnerId
) {
}
