package de.mirkosertic.powerstaff.project

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.project.command.Project
import de.mirkosertic.powerstaff.project.command.ProjectCommandService
import de.mirkosertic.powerstaff.project.command.ProjectHistory
import de.mirkosertic.powerstaff.project.command.ProjectHistoryCommandService
import de.mirkosertic.powerstaff.project.query.ProjectHistoryQueryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class ProjectHistoryRepositoryIT extends AbstractContainerBaseIT {

    @Autowired
    ProjectHistoryCommandService commandService

    @Autowired
    ProjectHistoryQueryService queryService

    @Autowired
    ProjectCommandService projectCommandService

    @Autowired
    JdbcClient jdbcClient

    Long projectId

    def setup() {
        def p = new Project()
        p.projectNumber = 'IT-PHR-001'
        p.status = 1
        p.visibleOnWebSite = false
        projectCommandService.save(p)
        projectId = jdbcClient.sql("SELECT MAX(id) FROM project WHERE project_number = 'IT-PHR-001'").query(Long).single()
    }

    def cleanup() {
        jdbcClient.sql("DELETE FROM project_history WHERE project_id = :pid").param("pid", projectId).update()
        jdbcClient.sql("DELETE FROM project WHERE project_number LIKE 'IT-PHR%'").update()
    }

    def "save erstellt neuen Historien-Eintrag"() {
        given:
        def h = new ProjectHistory()
        h.projectId = projectId
        h.description = "Erster Kontakt"

        when:
        def saved = commandService.save(h)

        then:
        saved.id != null
        saved.creationDate != null
        saved.creationUser != null
    }

    def "findByProjectId liefert Eintraege sortiert nach creation_date DESC"() {
        given:
        def h1 = new ProjectHistory()
        h1.projectId = projectId
        h1.description = "Erster Eintrag"
        commandService.save(h1)

        def h2 = new ProjectHistory()
        h2.projectId = projectId
        h2.description = "Zweiter Eintrag"
        commandService.save(h2)

        when:
        def results = queryService.findByProjectId(projectId)

        then:
        results.size() >= 2
        // most recent first
        results[0].creationDate >= results[1].creationDate
    }

    def "delete entfernt Historien-Eintrag"() {
        given:
        def h = new ProjectHistory()
        h.projectId = projectId
        h.description = "Zu löschen"
        def saved = commandService.save(h)

        when:
        commandService.delete(saved.id)

        then:
        def count = jdbcClient.sql("SELECT COUNT(*) FROM project_history WHERE id = :id").param("id", saved.id).query(Long).single()
        count == 0
    }

    def "findByProjectId fuer unbekanntes Projekt liefert leere Liste"() {
        expect:
        queryService.findByProjectId(-1L).isEmpty()
    }
}
