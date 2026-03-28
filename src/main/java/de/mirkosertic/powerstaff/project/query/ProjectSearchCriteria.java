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
        String kreditorNr,
        String sortField,
        String sortDir
) {
    public static ProjectSearchCriteria empty() {
        return new ProjectSearchCriteria(null, null, null, null, null, null, null, null, null, null, null);
    }

    public ProjectSearchCriteria withProjectNumber(String v) { return new ProjectSearchCriteria(v, descriptionShort, descriptionLong, skills, workplace, duration, status, debitorNr, kreditorNr, sortField, sortDir); }
    public ProjectSearchCriteria withDescriptionShort(String v) { return new ProjectSearchCriteria(projectNumber, v, descriptionLong, skills, workplace, duration, status, debitorNr, kreditorNr, sortField, sortDir); }
    public ProjectSearchCriteria withDescriptionLong(String v) { return new ProjectSearchCriteria(projectNumber, descriptionShort, v, skills, workplace, duration, status, debitorNr, kreditorNr, sortField, sortDir); }
    public ProjectSearchCriteria withSkills(String v) { return new ProjectSearchCriteria(projectNumber, descriptionShort, descriptionLong, v, workplace, duration, status, debitorNr, kreditorNr, sortField, sortDir); }
    public ProjectSearchCriteria withWorkplace(String v) { return new ProjectSearchCriteria(projectNumber, descriptionShort, descriptionLong, skills, v, duration, status, debitorNr, kreditorNr, sortField, sortDir); }
    public ProjectSearchCriteria withDuration(String v) { return new ProjectSearchCriteria(projectNumber, descriptionShort, descriptionLong, skills, workplace, v, status, debitorNr, kreditorNr, sortField, sortDir); }
    public ProjectSearchCriteria withStatus(Integer v) { return new ProjectSearchCriteria(projectNumber, descriptionShort, descriptionLong, skills, workplace, duration, v, debitorNr, kreditorNr, sortField, sortDir); }
    public ProjectSearchCriteria withDebitorNr(String v) { return new ProjectSearchCriteria(projectNumber, descriptionShort, descriptionLong, skills, workplace, duration, status, v, kreditorNr, sortField, sortDir); }
    public ProjectSearchCriteria withKreditorNr(String v) { return new ProjectSearchCriteria(projectNumber, descriptionShort, descriptionLong, skills, workplace, duration, status, debitorNr, v, sortField, sortDir); }
    public ProjectSearchCriteria withSortField(String v) { return new ProjectSearchCriteria(projectNumber, descriptionShort, descriptionLong, skills, workplace, duration, status, debitorNr, kreditorNr, v, sortDir); }
    public ProjectSearchCriteria withSortDir(String v) { return new ProjectSearchCriteria(projectNumber, descriptionShort, descriptionLong, skills, workplace, duration, status, debitorNr, kreditorNr, sortField, v); }
}
