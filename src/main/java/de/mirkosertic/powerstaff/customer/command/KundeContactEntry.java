package de.mirkosertic.powerstaff.customer.command;

/**
 * Delta-Command für einen Kontakteintrag im Unified-Save-Request.
 * op="ADD": id==null, type und value gesetzt → neuer Eintrag.
 * op="DELETE": id gesetzt → bestehender Eintrag wird gelöscht.
 */
public record KundeContactEntry(String op, Long id, String type, String value) {
}
