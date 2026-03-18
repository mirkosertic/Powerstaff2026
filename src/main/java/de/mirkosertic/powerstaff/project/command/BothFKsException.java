package de.mirkosertic.powerstaff.project.command;

public class BothFKsException extends RuntimeException {

    public BothFKsException() {
        super("Ein Projekt kann nicht gleichzeitig einem Kunden und einem Partner zugeordnet sein.");
    }
}
