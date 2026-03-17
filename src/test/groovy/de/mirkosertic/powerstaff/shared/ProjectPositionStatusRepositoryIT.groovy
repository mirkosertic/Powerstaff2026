package de.mirkosertic.powerstaff.shared

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.shared.command.ProjectPositionStatus
import de.mirkosertic.powerstaff.shared.command.StammdatenCommandService
import de.mirkosertic.powerstaff.shared.query.ProjectPositionStatusQueryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class ProjectPositionStatusRepositoryIT extends AbstractContainerBaseIT {

    @Autowired
    StammdatenCommandService commandService

    @Autowired
    ProjectPositionStatusQueryService queryService

    @Autowired
    JdbcClient jdbcClient

    def cleanup() {
        jdbcClient.sql("DELETE FROM project_position_status WHERE description LIKE 'IT-Test%'").update()
    }

    def "ProjectPositionStatus speichern und per QueryService lesen"() {
        given:
        def pps = commandService.saveProjectPositionStatus(newStatus("IT-Test Vorgeschlagen", "#d1fae5", "#065f46"))

        when:
        def all = queryService.findAll()

        then:
        all.any { it.id() == pps.id && it.description() == "IT-Test Vorgeschlagen" }
    }

    def "findAll liefert Eintraege alphabetisch sortiert"() {
        given:
        commandService.saveProjectPositionStatus(newStatus("IT-Test Zulu", "#fff", "#000"))
        commandService.saveProjectPositionStatus(newStatus("IT-Test Alpha", "#fff", "#000"))

        when:
        def itEntries = queryService.findAll().findAll { it.description().startsWith("IT-Test") }

        then:
        itEntries.size() >= 2
        itEntries.collect { it.description() } == itEntries.collect { it.description() }.sort()
    }

    def "Badge-Farben werden korrekt gespeichert und gelesen"() {
        given:
        commandService.saveProjectPositionStatus(newStatus("IT-Test Farbe", "#aabbcc", "#112233"))

        when:
        def found = queryService.findAll().find { it.description() == "IT-Test Farbe" }

        then:
        found != null
        found.color() == "#aabbcc"
        found.colorText() == "#112233"
    }

    def "ProjectPositionStatus update aendert alle Felder"() {
        given:
        def pps = commandService.saveProjectPositionStatus(newStatus("IT-Test Update", "#fff", "#000"))

        when:
        def loaded = commandService.findProjectPositionStatusById(pps.id).orElseThrow()
        loaded.setDescription("IT-Test Geaendert")
        loaded.setColor("#123456")
        loaded.setColorText("#abcdef")
        commandService.saveProjectPositionStatus(loaded)

        then:
        def updated = commandService.findProjectPositionStatusById(pps.id).get()
        updated.description == "IT-Test Geaendert"
        updated.color == "#123456"
        updated.colorText == "#abcdef"
    }

    private static ProjectPositionStatus newStatus(String description, String color, String colorText) {
        def s = new ProjectPositionStatus()
        s.description = description
        s.color = color
        s.colorText = colorText
        s
    }
}
