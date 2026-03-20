package de.mirkosertic.powerstaff.freelancer.command;

/**
 * Delta-Command für eine Tag-Zuordnung im Unified-Save-Request.
 * op="ADD": tagId gesetzt → Tag wird zugeordnet.
 * op="DELETE": tagId gesetzt → Tag-Zuordnung wird entfernt.
 */
public record FreelancerTagEntry(String op, Long tagId) {}
