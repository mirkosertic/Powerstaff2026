package de.mirkosertic.powerstaff.partner.query;

import java.time.LocalDateTime;

public record PartnerFreelancerView(
        Long id,
        String code,
        String name1,
        String name2,
        String company,
        LocalDateTime availabilityAsDate,
        Long salaryLong
) {
}
