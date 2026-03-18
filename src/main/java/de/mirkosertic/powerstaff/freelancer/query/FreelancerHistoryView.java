package de.mirkosertic.powerstaff.freelancer.query;

import java.time.LocalDateTime;

public record FreelancerHistoryView(
        Long id,
        LocalDateTime creationDate,
        String creationUser,
        LocalDateTime changedDate,
        String changedUser,
        String description,
        Long typeId,
        String typeDescription,
        Long freelancerId
) {
}
