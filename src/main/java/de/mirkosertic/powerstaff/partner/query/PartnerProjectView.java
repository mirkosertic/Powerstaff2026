package de.mirkosertic.powerstaff.partner.query;

import java.time.LocalDateTime;

public record PartnerProjectView(
        Long id,
        String projectNumber,
        String descriptionShort,
        String workplace,
        LocalDateTime startDate,
        int status
) {
}
