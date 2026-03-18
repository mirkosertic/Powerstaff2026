package de.mirkosertic.powerstaff

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class FlywayMigrationIT extends AbstractContainerBaseIT {

    @Autowired
    JdbcClient jdbcClient

    static final List<String> EXPECTED_TABLES = [
        'event_publication',
        'freelancer',
        'freelancer_contact',
        'freelancer_history',
        'freelancer_tags',
        'historytype',
        'kunde',
        'kunde_contact',
        'kunde_history',
        'partner',
        'partner_contact',
        'partner_history',
        'profile_search_chat',
        'profile_search_message',
        'project',
        'project_history',
        'project_position',
        'project_position_status',
        'ps_user',
        'remembered_project',
        'tags'
    ]

    def "Flyway-Migration: alle 21 erwarteten Tabellen existieren in der DB"() {
        when:
        def existingTables = jdbcClient.sql(
            "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_TYPE = 'BASE TABLE'"
        ).query(String.class).list().collect { it.toLowerCase() }

        then:
        EXPECTED_TABLES.every { table ->
            assert existingTables.contains(table), "Tabelle '${table}' fehlt in der Datenbank"
            true
        }
        existingTables.size() >= EXPECTED_TABLES.size()
    }
}
