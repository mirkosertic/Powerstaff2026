package de.mirkosertic.powerstaff.customer.command;

/**
 * Delta-Command für einen Historieneintrag im Unified-Save-Request.
 * op="ADD": id==null, typeId und description gesetzt → neuer Eintrag.
 * op="UPDATE": id gesetzt, typeId und description gesetzt → bestehender Eintrag wird aktualisiert; Anlage-Audit bleibt erhalten.
 * op="DELETE": id gesetzt → bestehender Eintrag wird gelöscht.
 */
public record KundeHistoryEntry(String op, Long id, Long typeId, String description) {
}
