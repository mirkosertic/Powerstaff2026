package de.mirkosertic.powerstaff.freelancer.command;

public class DuplicateTagException extends RuntimeException {

    public DuplicateTagException(final long freelancerId, final long tagId) {
        super("Tag " + tagId + " ist dem Freiberufler " + freelancerId + " bereits zugeordnet");
    }
}
