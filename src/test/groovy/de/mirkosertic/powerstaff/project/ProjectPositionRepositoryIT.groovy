package de.mirkosertic.powerstaff.project

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.project.command.ProjectPosition
import de.mirkosertic.powerstaff.project.command.ProjectCommandService
import de.mirkosertic.powerstaff.project.command.Project
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class ProjectPositionRepositoryIT extends AbstractContainerBaseIT {

    @Autowired
    ProjectCommandService projectCommandService

    @Autowired
    JdbcClient jdbcClient

    Long projectId
    Long freelancerId
    Long statusId

    def setup() {
        // Insert project_position_status
        jdbcClient.sql("INSERT INTO project_position_status (description, color, color_text) VALUES ('Test-Status', '#ffffff', '#000000')")
                .update()
        statusId = jdbcClient.sql("SELECT MAX(id) FROM project_position_status").query(Long).single()

        // Insert freelancer
        jdbcClient.sql("""
            INSERT INTO freelancer (db_version, name1, code, contactforbidden, show_again, datenschutz)
            VALUES (0, 'IT-PPR-Freelancer', 'IT-PPR-CODE', 0, 1, 0)
        """).update()
        freelancerId = jdbcClient.sql("SELECT MAX(id) FROM freelancer").query(Long).single()

        // Insert project
        def p = new Project()
        p.projectNumber = 'IT-PPR-001'
        p.status = 1
        p.visibleOnWebSite = false
        projectCommandService.save(p)
        projectId = jdbcClient.sql("SELECT MAX(id) FROM project WHERE project_number = 'IT-PPR-001'").query(Long).single()
    }

    def cleanup() {
        jdbcClient.sql("DELETE FROM project_position WHERE project_id = :pid").param("pid", projectId).update()
        jdbcClient.sql("DELETE FROM project WHERE project_number LIKE 'IT-PPR%'").update()
        jdbcClient.sql("DELETE FROM freelancer WHERE code = 'IT-PPR-CODE'").update()
        jdbcClient.sql("DELETE FROM project_position_status WHERE id = :sid").param("sid", statusId).update()
    }

    def "insert und findById liefert korrekte Felder"() {
        given:
        def pos = new ProjectPosition()
        pos.projectId = projectId
        pos.freelancerId = freelancerId
        pos.statusId = statusId
        pos.konditionen = "800 EUR/Tag"
        pos.kommentar = "Gut geeignet"

        when:
        jdbcClient.sql("""
            INSERT INTO project_position (db_version, project_id, freelancer_id, status_id, konditionen, kommentar)
            VALUES (0, :projectId, :freelancerId, :statusId, :konditionen, :kommentar)
        """).param("projectId", projectId)
           .param("freelancerId", freelancerId)
           .param("statusId", statusId)
           .param("konditionen", pos.konditionen)
           .param("kommentar", pos.kommentar)
           .update()
        def id = jdbcClient.sql("SELECT MAX(id) FROM project_position WHERE project_id = :pid")
                .param("pid", projectId).query(Long).single()
        def row = jdbcClient.sql("SELECT * FROM project_position WHERE id = :id").param("id", id)
                .query(ProjectPosition).optional()

        then:
        row.isPresent()
        row.get().projectId == projectId
        row.get().freelancerId == freelancerId
        row.get().konditionen == "800 EUR/Tag"
    }

    def "UNIQUE-Verletzung auf (project_id, freelancer_id) wirft Exception"() {
        given:
        jdbcClient.sql("""
            INSERT INTO project_position (db_version, project_id, freelancer_id, status_id)
            VALUES (0, :projectId, :freelancerId, :statusId)
        """).param("projectId", projectId)
           .param("freelancerId", freelancerId)
           .param("statusId", statusId)
           .update()

        when:
        jdbcClient.sql("""
            INSERT INTO project_position (db_version, project_id, freelancer_id, status_id)
            VALUES (0, :projectId, :freelancerId, :statusId)
        """).param("projectId", projectId)
           .param("freelancerId", freelancerId)
           .param("statusId", statusId)
           .update()

        then:
        thrown(DataIntegrityViolationException)
    }
}
