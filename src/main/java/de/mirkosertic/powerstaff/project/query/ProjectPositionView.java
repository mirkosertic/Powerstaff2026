package de.mirkosertic.powerstaff.project.query;

public record ProjectPositionView(
        Long id,
        Long dbVersion,
        Long freelancerId,
        String code,
        String name1,
        String name2,
        Long statusId,
        String statusDescription,
        String statusColor,
        String statusColorText,
        String konditionen,
        String kommentar
) {
}
