package de.mirkosertic.powerstaff.project

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.project.command.Project
import de.mirkosertic.powerstaff.project.command.ProjectCommandService
import de.mirkosertic.powerstaff.project.query.ProjectPositionQueryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class ProjectPositionQueryServiceIT extends AbstractContainerBaseIT {

    @Autowired
    ProjectPositionQueryService queryService

    @Autowired
    ProjectCommandService projectCommandService

    @Autowired
    JdbcClient jdbcClient

    Long projectId
    Long freelancerId
    Long statusId

    def setup() {
        jdbcClient.sql("INSERT INTO project_position_status (description, color, color_text) VALUES ('IT-PPQ-Status', '#eee', '#111')").update()
        statusId = jdbcClient.sql("SELECT MAX(id) FROM project_position_status").query(Long).single()

        jdbcClient.sql("INSERT INTO freelancer (db_version, name1, name2, code, contactforbidden, show_again, datenschutz) VALUES (0, 'Müller', 'Hans', 'IT-PPQ-CODE', 0, 1, 0)").update()
        freelancerId = jdbcClient.sql("SELECT MAX(id) FROM freelancer").query(Long).single()

        def p = new Project()
        p.projectNumber = 'IT-PPQ-001'
        p.status = 1
        p.visibleOnWebSite = false
        projectCommandService.save(p)
        projectId = jdbcClient.sql("SELECT MAX(id) FROM project WHERE project_number = 'IT-PPQ-001'").query(Long).single()

        jdbcClient.sql("""
            INSERT INTO project_position (db_version, project_id, freelancer_id, status_id, konditionen, kommentar)
            VALUES (0, :pid, :fid, :sid, 'Konditionen Test', 'Kommentar Test')
        """).param("pid", projectId).param("fid", freelancerId).param("sid", statusId).update()
    }

    def cleanup() {
        jdbcClient.sql("DELETE FROM project_position WHERE project_id = :pid").param("pid", projectId).update()
        jdbcClient.sql("DELETE FROM project WHERE project_number LIKE 'IT-PPQ%'").update()
        jdbcClient.sql("DELETE FROM freelancer WHERE code = 'IT-PPQ-CODE'").update()
        jdbcClient.sql("DELETE FROM project_position_status WHERE id = :sid").param("sid", statusId).update()
    }

    def "findByProjectId liefert Position mit JOIN-Daten"() {
        when:
        def results = queryService.findByProjectId(projectId, "code", "asc")

        then:
        results.size() == 1
        results[0].freelancerId == freelancerId
        results[0].code == 'IT-PPQ-CODE'
        results[0].name1 == 'Müller'
        results[0].statusDescription == 'IT-PPQ-Status'
        results[0].konditionen == 'Konditionen Test'
    }

    def "findByProjectId fuer unbekanntes Projekt liefert leere Liste"() {
        expect:
        queryService.findByProjectId(-1L, null, null).isEmpty()
    }

    def "existsPosition liefert true wenn Position vorhanden"() {
        expect:
        queryService.existsPosition(projectId, freelancerId)
    }

    def "existsPosition liefert false wenn keine Position vorhanden"() {
        expect:
        !queryService.existsPosition(-1L, -1L)
    }
}
