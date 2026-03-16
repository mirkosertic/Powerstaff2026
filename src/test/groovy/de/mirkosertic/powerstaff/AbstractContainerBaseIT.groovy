package de.mirkosertic.powerstaff

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import spock.lang.Specification

import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE

/**
 * Abstrakte Basisklasse für alle Integrationstests mit Datenbankzugriff.
 *
 * Alle *IT-Klassen erben von dieser Basis. Dadurch läuft die MySQL-Instanz
 * einmal pro Test-Suite (shared container), nicht einmal pro Testklasse.
 *
 * Flyway-Migrationen werden automatisch beim Hochfahren des Containers
 * durch Spring Boot ausgeführt – das Testschema entspricht immer dem
 * Produktionsschema (ADR-009).
 */
@Testcontainers
@AutoConfigureTestDatabase(replace = NONE)
abstract class AbstractContainerBaseIT extends Specification {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("powerstaff")
            .withUsername("powerstaff")
            .withPassword("powerstaff")

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl)
        registry.add("spring.datasource.username", mysql::getUsername)
        registry.add("spring.datasource.password", mysql::getPassword)
        registry.add("spring.datasource.driver-class-name", { "com.mysql.cj.jdbc.Driver" })
    }
}
