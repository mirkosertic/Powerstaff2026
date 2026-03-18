package de.mirkosertic.powerstaff.project.query;

public record ProjectSearchCriteria(
        String projectNumber,
        String descriptionShort,
        String descriptionLong,
        String skills,
        String workplace,
        String duration,
        Integer status,
        String debitorNr,
        String kreditorNr
) {
}
