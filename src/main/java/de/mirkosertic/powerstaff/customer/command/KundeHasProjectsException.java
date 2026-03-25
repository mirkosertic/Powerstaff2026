package de.mirkosertic.powerstaff.customer.command;

import java.util.List;

public class KundeHasProjectsException extends RuntimeException {

    private final List<Long> projectIds;

    public KundeHasProjectsException(final List<Long> projectIds) {
        super("Kunde ist Projekten zugeordnet: " + projectIds);
        this.projectIds = projectIds;
    }

    public List<Long> getProjectIds() {
        return projectIds;
    }
}
