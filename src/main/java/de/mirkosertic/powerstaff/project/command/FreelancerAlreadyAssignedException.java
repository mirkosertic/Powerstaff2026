package de.mirkosertic.powerstaff.project.command;

public class FreelancerAlreadyAssignedException extends RuntimeException {

    public FreelancerAlreadyAssignedException(final long freelancerId, final long projectId) {
        super("Freelancer " + freelancerId + " is already assigned to project " + projectId);
    }
}
