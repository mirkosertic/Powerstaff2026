package de.mirkosertic.powerstaff.freelancer.command;

/**
 * Delta-Command für einen Historieneintrag im Unified-Save-Request.
 * op="ADD": id==null, typeId und description gesetzt → neuer Eintrag.
 * op="DELETE": id gesetzt → bestehender Eintrag wird gelöscht.
 */
public record FreelancerHistoryEntry(String op, Long id, Long typeId, String description) {}
