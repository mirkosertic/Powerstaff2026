package de.mirkosertic.powerstaff.project.command;

public class FreelancerAlreadyAssignedException extends RuntimeException {

    public FreelancerAlreadyAssignedException(long freelancerId, long projectId) {
        super("Freelancer " + freelancerId + " is already assigned to project " + projectId);
    }
}
