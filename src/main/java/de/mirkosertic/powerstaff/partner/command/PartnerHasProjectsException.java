package de.mirkosertic.powerstaff.partner.command;

import java.util.List;

public class PartnerHasProjectsException extends RuntimeException {

    private final List<Long> projectIds;

    public PartnerHasProjectsException(List<Long> projectIds) {
        super("Partner cannot be deleted: linked to projects " + projectIds);
        this.projectIds = projectIds;
    }

    public List<Long> getProjectIds() {
        return projectIds;
    }
}
