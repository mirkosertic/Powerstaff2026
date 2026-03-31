package de.mirkosertic.powerstaff.profilesearch.query;

import java.time.LocalDateTime;

/**
 * Projektion für den Batch-Load von Freiberuflerdaten anhand einer Code-Liste.
 * Wird von {@link ProfileSearchQueryService#findFreelancersByCodesInBatch(java.util.List)} zurückgegeben.
 */
record FreelancerBatchRow(
        Long id,
        String code,
        String name1,
        String name2,
        LocalDateTime lastContactDate,
        Long salaryPerDayLong,
        LocalDateTime availabilityAsDate,
        boolean contactForbidden
) {
}
