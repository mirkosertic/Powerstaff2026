package de.mirkosertic.powerstaff.project.query;

import java.time.LocalDateTime;

public record ProjectDetailView(
        Long id,
        Long dbVersion,
        LocalDateTime creationDate,
        String creationUser,
        LocalDateTime changedDate,
        String changedUser,
        String projectNumber,
        LocalDateTime entryDate,
        LocalDateTime startDate,
        String duration,
        int status,
        boolean visibleOnWebSite,
        String descriptionShort,
        String descriptionLong,
        String skills,
        String workplace,
        Long customerId,
        Long partnerId,
        Long stundensatzVk,
        String debitorNr,
        String kreditorNr
) {
}
