package de.mirkosertic.powerstaff.freelancer.command;

public class DuplicateTagException extends RuntimeException {

    public DuplicateTagException(long freelancerId, long tagId) {
        super("Tag " + tagId + " ist dem Freiberufler " + freelancerId + " bereits zugeordnet");
    }
}
