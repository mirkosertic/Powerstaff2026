package de.mirkosertic.powerstaff.freelancer

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.freelancer.command.Freelancer
import de.mirkosertic.powerstaff.freelancer.command.FreelancerCommandService
import de.mirkosertic.powerstaff.freelancer.command.FreelancerContactEntry
import de.mirkosertic.powerstaff.freelancer.command.FreelancerHasPositionsException
import de.mirkosertic.powerstaff.freelancer.command.FreelancerHistoryEntry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class FreelancerCommandServiceIT extends AbstractContainerBaseIT {

    @Autowired
    FreelancerCommandService commandService

    @Autowired
    JdbcClient jdbcClient

    Long historyTypeId

    def setup() {
        jdbcClient.sql("INSERT INTO historytype (description) VALUES ('IT-Cmd-HistoryTyp')").update()
        historyTypeId = jdbcClient.sql("SELECT id FROM historytype WHERE description = 'IT-Cmd-HistoryTyp'")
                .query(Long.class).single()
    }

    def cleanup() {
        jdbcClient.sql("DELETE FROM freelancer WHERE name1 LIKE 'IT-Cmd%'").update()
        jdbcClient.sql("DELETE FROM historytype WHERE description = 'IT-Cmd-HistoryTyp'").update()
    }

    def "Unified Save speichert Stammdaten, Kontakte und Historie transaktional"() {
        given:
        def contacts = [new FreelancerContactEntry(null, "EMAIL", "it-cmd@example.com")]
        def history = [new FreelancerHistoryEntry(null, historyTypeId, "IT-Cmd Ersteintrag")]

        when:
        def saved = commandService.save(newFreelancer("IT-Cmd Unified"), contacts, history)

        then:
        saved.id != null

        and:
        def savedContacts = jdbcClient.sql("SELECT value FROM freelancer_contact WHERE freelancer_id = :id")
                .param("id", saved.id).query(String.class).list()
        savedContacts == ["it-cmd@example.com"]

        and:
        def savedHistory = jdbcClient.sql("SELECT description FROM freelancer_history WHERE freelancer_id = :id")
                .param("id", saved.id).query(String.class).list()
        savedHistory == ["IT-Cmd Ersteintrag"]
    }

    def "Unified Save loescht entfernte Kontakte (Replace-Logik)"() {
        given:
        def freelancer = commandService.save(newFreelancer("IT-Cmd Replace"),
                [new FreelancerContactEntry(null, "EMAIL", "alt@example.com")], [])
        def contactId = jdbcClient.sql("SELECT id FROM freelancer_contact WHERE freelancer_id = :id")
                .param("id", freelancer.id).query(Long.class).single()

        when:
        commandService.save(freelancer, [], [])

        then:
        jdbcClient.sql("SELECT COUNT(*) FROM freelancer_contact WHERE freelancer_id = :id")
                .param("id", freelancer.id).query(Long.class).single() == 0
    }

    def "Unified Save aktualisiert geaenderte Kontakte und beruehrt unveraenderte nicht"() {
        given:
        def freelancer = commandService.save(newFreelancer("IT-Cmd Update"),
                [new FreelancerContactEntry(null, "EMAIL", "alt@example.com")], [])
        def contactId = jdbcClient.sql("SELECT id FROM freelancer_contact WHERE freelancer_id = :id")
                .param("id", freelancer.id).query(Long.class).single()
        def changedBefore = jdbcClient.sql("SELECT changed_date FROM freelancer_contact WHERE id = :id")
                .param("id", contactId).query(String.class).single()

        when: "unveraenderten Kontakt speichern"
        commandService.save(freelancer,
                [new FreelancerContactEntry(contactId, "EMAIL", "alt@example.com")], [])

        then: "Audit-Datum bleibt identisch"
        jdbcClient.sql("SELECT changed_date FROM freelancer_contact WHERE id = :id")
                .param("id", contactId).query(String.class).single() == changedBefore
    }

    def "delete ohne Projektposition loescht den Freiberufler"() {
        given:
        def freelancer = commandService.save(newFreelancer("IT-Cmd Delete"))

        when:
        commandService.deleteById(freelancer.id)

        then:
        commandService.findById(freelancer.id).isEmpty()
    }

    def "delete mit Projektposition wirft FreelancerHasPositionsException"() {
        given:
        def freelancer = commandService.save(newFreelancer("IT-Cmd Restrict"))

        jdbcClient.sql("""
            INSERT INTO project (db_version, project_number)
            VALUES (0, 'IT-CMD-FL-PROJ-001')
        """).update()
        def projectId = jdbcClient.sql("SELECT id FROM project WHERE project_number = 'IT-CMD-FL-PROJ-001'")
                .query(Long.class).single()

        jdbcClient.sql("INSERT INTO project_position_status (description, color, color_text) VALUES ('IT-Cmd-Status', '#000', '#fff')")
                .update()
        def statusId = jdbcClient.sql("SELECT id FROM project_position_status WHERE description = 'IT-Cmd-Status'")
                .query(Long.class).single()

        jdbcClient.sql("""
            INSERT INTO project_position (db_version, project_id, freelancer_id, status_id)
            VALUES (0, :projectId, :freelancerId, :statusId)
        """).param("projectId", projectId).param("freelancerId", freelancer.id).param("statusId", statusId).update()

        when:
        commandService.deleteById(freelancer.id)

        then:
        def ex = thrown(FreelancerHasPositionsException)
        ex.projectIds.contains(projectId)

        cleanup:
        jdbcClient.sql("DELETE FROM project_position WHERE project_id = :id").param("id", projectId).update()
        jdbcClient.sql("DELETE FROM project WHERE id = :id").param("id", projectId).update()
        jdbcClient.sql("DELETE FROM freelancer WHERE id = :id").param("id", freelancer.id).update()
        jdbcClient.sql("DELETE FROM project_position_status WHERE description = 'IT-Cmd-Status'").update()
    }

    private static Freelancer newFreelancer(String name1) {
        def f = new Freelancer()
        f.name1 = name1
        f.name2 = "Test"
        f.contactForbidden = false
        f.showAgain = false
        f.datenschutz = false
        f
    }
}
