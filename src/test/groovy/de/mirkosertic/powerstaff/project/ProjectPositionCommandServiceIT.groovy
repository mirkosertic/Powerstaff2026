package de.mirkosertic.powerstaff.project

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.project.command.FreelancerAlreadyAssignedException
import de.mirkosertic.powerstaff.project.command.Project
import de.mirkosertic.powerstaff.project.command.ProjectCommandService
import de.mirkosertic.powerstaff.project.command.ProjectPosition
import de.mirkosertic.powerstaff.project.command.ProjectPositionCommandService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class ProjectPositionCommandServiceIT extends AbstractContainerBaseIT {

    @Autowired
    ProjectPositionCommandService commandService

    @Autowired
    ProjectCommandService projectCommandService

    @Autowired
    JdbcClient jdbcClient

    Long projectId
    Long freelancerId
    Long statusId

    def setup() {
        jdbcClient.sql("INSERT INTO project_position_status (description, color, color_text) VALUES ('Test-Status', '#fff', '#000')").update()
        statusId = jdbcClient.sql("SELECT MAX(id) FROM project_position_status").query(Long).single()

        jdbcClient.sql("INSERT INTO freelancer (db_version, name1, code, contactforbidden, show_again, datenschutz) VALUES (0, 'PPS-IT', 'IT-PPS-CODE', 0, 1, 0)").update()
        freelancerId = jdbcClient.sql("SELECT MAX(id) FROM freelancer").query(Long).single()

        def p = new Project()
        p.projectNumber = 'IT-PPS-001'
        p.status = 1
        p.visibleOnWebSite = false
        projectCommandService.save(p)
        projectId = jdbcClient.sql("SELECT MAX(id) FROM project WHERE project_number = 'IT-PPS-001'").query(Long).single()
    }

    def cleanup() {
        jdbcClient.sql("DELETE FROM project_position WHERE project_id = :pid").param("pid", projectId).update()
        jdbcClient.sql("DELETE FROM project WHERE project_number LIKE 'IT-PPS%'").update()
        jdbcClient.sql("DELETE FROM freelancer WHERE code = 'IT-PPS-CODE'").update()
        jdbcClient.sql("DELETE FROM project_position_status WHERE id = :sid").param("sid", statusId).update()
    }

    def "save erstellt neue Projektposition"() {
        given:
        def pos = new ProjectPosition()
        pos.projectId = projectId
        pos.freelancerId = freelancerId
        pos.statusId = statusId
        pos.konditionen = "750 EUR/Tag"

        when:
        def saved = commandService.save(pos)

        then:
        saved.id != null
        saved.dbVersion != null
        saved.creationDate != null
    }

    def "save mit duplizierten (project_id, freelancer_id) wirft FreelancerAlreadyAssignedException"() {
        given:
        def pos1 = new ProjectPosition()
        pos1.projectId = projectId
        pos1.freelancerId = freelancerId
        pos1.statusId = statusId
        commandService.save(pos1)

        def pos2 = new ProjectPosition()
        pos2.projectId = projectId
        pos2.freelancerId = freelancerId
        pos2.statusId = statusId

        when:
        commandService.save(pos2)

        then:
        thrown(FreelancerAlreadyAssignedException)
    }

    def "delete entfernt Projektposition"() {
        given:
        def pos = new ProjectPosition()
        pos.projectId = projectId
        pos.freelancerId = freelancerId
        pos.statusId = statusId
        def saved = commandService.save(pos)

        when:
        commandService.delete(saved.id)

        then:
        def count = jdbcClient.sql("SELECT COUNT(*) FROM project_position WHERE id = :id").param("id", saved.id).query(Long).single()
        count == 0
    }

    def "assignFreelancerToProject erstellt Position"() {
        when:
        commandService.assignFreelancerToProject(freelancerId, projectId, statusId, "800 EUR/Tag", "Test")

        then:
        def count = jdbcClient.sql("SELECT COUNT(*) FROM project_position WHERE project_id = :pid AND freelancer_id = :fid")
                .param("pid", projectId).param("fid", freelancerId).query(Long).single()
        count == 1
    }

    def "assignFreelancerToProject mit statusId=null verwendet Default-Status"() {
        given: "alle anderen Defaults zuruecksetzen und den Test-Status als Default markieren"
        jdbcClient.sql("UPDATE project_position_status SET is_default = FALSE").update()
        jdbcClient.sql("UPDATE project_position_status SET is_default = TRUE WHERE id = :id")
                .param("id", statusId).update()

        when: "ein Freiberufler ohne expliziten Status zugeordnet wird"
        commandService.assignFreelancerToProject(freelancerId, projectId, null, null, null)

        then: "die Position wurde mit dem Default-Status angelegt"
        def assignedStatusId = jdbcClient
                .sql("SELECT status_id FROM project_position WHERE project_id = :pid AND freelancer_id = :fid")
                .param("pid", projectId).param("fid", freelancerId)
                .query(Long).single()
        assignedStatusId == statusId

        cleanup:
        jdbcClient.sql("UPDATE project_position_status SET is_default = FALSE WHERE id = :id")
                .param("id", statusId).update()
    }

    def "assignFreelancerToProject mit statusId=null und ohne Default wirft IllegalStateException"() {
        given: "kein Default-Status konfiguriert"
        jdbcClient.sql("UPDATE project_position_status SET is_default = FALSE").update()

        when:
        commandService.assignFreelancerToProject(freelancerId, projectId, null, null, null)

        then:
        thrown(IllegalStateException)
    }
}
