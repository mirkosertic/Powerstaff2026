package de.mirkosertic.powerstaff.freelancer.command;

/**
 * Öffentliches Ergebnis-Record für Cross-Modul-Lookups.
 * Wird von PartnerController genutzt, um Freiberufler per Code zu suchen,
 * ohne direkten Tabellenzugriff aus einem anderen Modul.
 */
public record FreelancerLookupResult(Long id, Long partnerId, String company) {}
