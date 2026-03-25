package de.mirkosertic.powerstaff.freelancer.command;

import java.util.List;

public class FreelancerHasPositionsException extends RuntimeException {

    private final List<Long> projectIds;

    public FreelancerHasPositionsException(final List<Long> projectIds) {
        super("Freiberufler ist aktiven Projekten zugeordnet: " + projectIds);
        this.projectIds = projectIds;
    }

    public List<Long> getProjectIds() {
        return projectIds;
    }
}
