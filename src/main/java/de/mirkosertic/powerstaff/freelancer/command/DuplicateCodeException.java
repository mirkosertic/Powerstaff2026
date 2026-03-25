package de.mirkosertic.powerstaff.freelancer.command;

public class DuplicateCodeException extends RuntimeException {

    public DuplicateCodeException(final String code) {
        super("Freelancer with code '" + code + "' already exists");
    }
}
