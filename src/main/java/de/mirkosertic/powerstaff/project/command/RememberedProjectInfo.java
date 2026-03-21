package de.mirkosertic.powerstaff.project.command;

/**
 * Öffentliches Record für Cross-Modul-Kommunikation: enthält Anzeige-Infos
 * des aktuell gemerkten Projekts für andere Module (freelancer, partner, customer).
 */
public record RememberedProjectInfo(Long projectId, String projectNumber, String shortDescription) {
}
