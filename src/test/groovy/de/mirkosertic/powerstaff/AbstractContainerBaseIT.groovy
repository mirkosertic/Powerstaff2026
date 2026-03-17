package de.mirkosertic.powerstaff

import spock.lang.Specification

/**
 * Abstrakte Basisklasse für alle Integrationstests mit Datenbankzugriff.
 *
 * Die MySQL-Instanz wird über die TC JDBC URL in src/test/resources/application.yaml
 * automatisch durch Testcontainers gestartet – kein programmatischer Container-Start
 * nötig. Flyway-Migrationen laufen automatisch beim Hochfahren des Spring-Kontexts.
 */
abstract class AbstractContainerBaseIT extends Specification {
}
