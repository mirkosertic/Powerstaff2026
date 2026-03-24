package de.mirkosertic.powerstaff.freelancer

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.freelancer.command.Freelancer
import de.mirkosertic.powerstaff.freelancer.command.FreelancerCommandService
import de.mirkosertic.powerstaff.freelancer.command.FreelancerContactEntry
import de.mirkosertic.powerstaff.freelancer.command.FreelancerHasPositionsException
import de.mirkosertic.powerstaff.freelancer.command.FreelancerHistoryEntry
import de.mirkosertic.powerstaff.freelancer.command.FreelancerTagEntry
import de.mirkosertic.powerstaff.partner.command.PartnerCommandService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class FreelancerCommandServiceIT extends AbstractContainerBaseIT {

    @Autowired
    FreelancerCommandService commandService

    @Autowired
    PartnerCommandService partnerCommandService

    @Autowired
    JdbcClient jdbcClient

    Long historyTypeId
    Long tagId

    def setup() {
        jdbcClient.sql("INSERT INTO historytype (description) VALUES ('IT-Cmd-HistoryTyp')").update()
        historyTypeId = jdbcClient.sql("SELECT id FROM historytype WHERE description = 'IT-Cmd-HistoryTyp'")
                .query(Long.class).single()

        jdbcClient.sql("INSERT INTO tags (tagname, type) VALUES ('IT-Cmd-Tag', 'SCHWERPUNKT')").update()
        tagId = jdbcClient.sql("SELECT id FROM tags WHERE tagname = 'IT-Cmd-Tag'").query(Long.class).single()
    }

    def cleanup() {
        jdbcClient.sql("DELETE FROM freelancer_tags WHERE tag_id = :id").param("id", tagId).update()
        jdbcClient.sql("DELETE FROM freelancer_history WHERE freelancer_id IN (SELECT id FROM freelancer WHERE name1 LIKE 'IT-Cmd%')").update()
        jdbcClient.sql("DELETE FROM freelancer_contact WHERE freelancer_id IN (SELECT id FROM freelancer WHERE name1 LIKE 'IT-Cmd%')").update()
        jdbcClient.sql("DELETE FROM freelancer WHERE name1 LIKE 'IT-Cmd%'").update()
        jdbcClient.sql("DELETE FROM tags WHERE tagname = 'IT-Cmd-Tag'").update()
        jdbcClient.sql("DELETE FROM historytype WHERE description = 'IT-Cmd-HistoryTyp'").update()
        jdbcClient.sql("DELETE FROM partner WHERE company LIKE 'IT-Cmd%'").update()
    }

    def "Unified Save speichert Stammdaten, Kontakte und Historie transaktional"() {
        given:
        def contacts = [new FreelancerContactEntry("ADD", null, "EMAIL", "it-cmd@example.com")]
        def history = [new FreelancerHistoryEntry("ADD", null, historyTypeId, "IT-Cmd Ersteintrag")]

        when:
        def saved = commandService.save(newFreelancer("IT-Cmd Unified"), contacts, history, [])

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

    def "Unified Save loescht Kontakt per DELETE-Delta-Command"() {
        given:
        def freelancer = commandService.save(newFreelancer("IT-Cmd Delete-Contact"),
                [new FreelancerContactEntry("ADD", null, "EMAIL", "alt@example.com")], [], [])
        def contactId = jdbcClient.sql("SELECT id FROM freelancer_contact WHERE freelancer_id = :id")
                .param("id", freelancer.id).query(Long.class).single()

        when:
        commandService.save(freelancer, [new FreelancerContactEntry("DELETE", contactId, null, null)], [], [])

        then:
        jdbcClient.sql("SELECT COUNT(*) FROM freelancer_contact WHERE freelancer_id = :id")
                .param("id", freelancer.id).query(Long.class).single() == 0
    }

    def "DELETE Kontakt ohne ID wirft IllegalArgumentException"() {
        given:
        def freelancer = commandService.save(newFreelancer("IT-Cmd Delete-Contact-NoId"),
                [new FreelancerContactEntry("ADD", null, "EMAIL", "test@example.com")], [], [])

        when:
        commandService.save(freelancer, [new FreelancerContactEntry("DELETE", null, null, null)], [], [])

        then:
        thrown(IllegalArgumentException)
    }

    def "Unified Save beruehrt unveraenderte Kontakte nicht (kein Delta-Eintrag)"() {
        given:
        def freelancer = commandService.save(newFreelancer("IT-Cmd No-Touch"),
                [new FreelancerContactEntry("ADD", null, "EMAIL", "alt@example.com")], [], [])
        def contactId = jdbcClient.sql("SELECT id FROM freelancer_contact WHERE freelancer_id = :id")
                .param("id", freelancer.id).query(Long.class).single()
        def changedBefore = jdbcClient.sql("SELECT changed_date FROM freelancer_contact WHERE id = :id")
                .param("id", contactId).query(String.class).single()

        when: "leere Kontakt-Delta-Liste → kein Eingriff"
        commandService.save(freelancer, [], [], [])

        then: "Audit-Datum bleibt identisch"
        jdbcClient.sql("SELECT changed_date FROM freelancer_contact WHERE id = :id")
                .param("id", contactId).query(String.class).single() == changedBefore
    }

    def "Unified Save aktualisiert Kontakthistorieneintrag per UPDATE-Delta-Command"() {
        given:
        def freelancer = commandService.save(newFreelancer("IT-Cmd Update-History"),
                [], [new FreelancerHistoryEntry("ADD", null, historyTypeId, "Original")], [])
        def historyId = jdbcClient.sql("SELECT id FROM freelancer_history WHERE freelancer_id = :id")
                .param("id", freelancer.id).query(Long.class).single()

        when:
        commandService.save(freelancer, [],
                [new FreelancerHistoryEntry("UPDATE", historyId, historyTypeId, "Geaendert")], [])

        then:
        def desc = jdbcClient.sql("SELECT description FROM freelancer_history WHERE id = :id")
                .param("id", historyId).query(String.class).single()
        desc == "Geaendert"
    }

    def "Unified Save loescht Kontakthistorieneintrag per DELETE-Delta-Command"() {
        given:
        def freelancer = commandService.save(newFreelancer("IT-Cmd Delete-History"),
                [], [new FreelancerHistoryEntry("ADD", null, historyTypeId, "Zu loeschen")], [])
        def historyId = jdbcClient.sql("SELECT id FROM freelancer_history WHERE freelancer_id = :id")
                .param("id", freelancer.id).query(Long.class).single()

        when:
        commandService.save(freelancer, [],
                [new FreelancerHistoryEntry("DELETE", historyId, null, null)], [])

        then:
        jdbcClient.sql("SELECT COUNT(*) FROM freelancer_history WHERE freelancer_id = :id")
                .param("id", freelancer.id).query(Long.class).single() == 0
    }

    def "UPDATE Kontakthistorie ohne ID wirft IllegalArgumentException"() {
        given:
        def freelancer = commandService.save(newFreelancer("IT-Cmd Update-History-NoId"))

        when:
        commandService.save(freelancer, [],
                [new FreelancerHistoryEntry("UPDATE", null, historyTypeId, "Geaendert")], [])

        then:
        thrown(IllegalArgumentException)
    }

    def "DELETE Kontakthistorie ohne ID wirft IllegalArgumentException"() {
        given:
        def freelancer = commandService.save(newFreelancer("IT-Cmd Delete-History-NoId"))

        when:
        commandService.save(freelancer, [],
                [new FreelancerHistoryEntry("DELETE", null, null, null)], [])

        then:
        thrown(IllegalArgumentException)
    }

    def "Unified Save fuegt Tag per ADD-Delta-Command hinzu"() {
        given:
        def freelancer = commandService.save(newFreelancer("IT-Cmd Add-Tag"))

        when:
        commandService.save(freelancer, [], [], [new FreelancerTagEntry("ADD", tagId)])

        then:
        jdbcClient.sql("SELECT COUNT(*) FROM freelancer_tags WHERE freelancer_id = :fid AND tag_id = :tid")
                .param("fid", freelancer.id).param("tid", tagId).query(Long.class).single() == 1
    }

    def "Unified Save entfernt Tag per DELETE-Delta-Command"() {
        given:
        def freelancer = commandService.save(newFreelancer("IT-Cmd Remove-Tag"),
                [], [], [new FreelancerTagEntry("ADD", tagId)])

        when:
        commandService.save(freelancer, [], [], [new FreelancerTagEntry("DELETE", tagId)])

        then:
        jdbcClient.sql("SELECT COUNT(*) FROM freelancer_tags WHERE freelancer_id = :fid AND tag_id = :tid")
                .param("fid", freelancer.id).param("tid", tagId).query(Long.class).single() == 0
    }

    def "Wiederholtes ADD desselben Tags (idempotent) wirft keine Exception"() {
        given:
        def freelancer = commandService.save(newFreelancer("IT-Cmd Idempotent-Tag"),
                [], [], [new FreelancerTagEntry("ADD", tagId)])

        when:
        commandService.save(freelancer, [], [], [new FreelancerTagEntry("ADD", tagId)])

        then:
        noExceptionThrown()
        jdbcClient.sql("SELECT COUNT(*) FROM freelancer_tags WHERE freelancer_id = :fid AND tag_id = :tid")
                .param("fid", freelancer.id).param("tid", tagId).query(Long.class).single() == 1
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

    def "assignToPartner ordnet Freiberufler einem Partner zu"() {
        given:
        def freelancer = commandService.save(newFreelancer("IT-Cmd Assign-Partner"))
        def partner = partnerCommandService.save(newPartner("IT-Cmd Assign-Partner"))

        when:
        commandService.assignToPartner(freelancer.id, partner.id)

        then:
        def partnerId = jdbcClient.sql("SELECT partner_id FROM freelancer WHERE id = :id")
                .param("id", freelancer.id).query(Long.class).single()
        partnerId == partner.id
    }

    def "removeFromPartner hebt Zuweisung auf"() {
        given:
        def freelancer = commandService.save(newFreelancer("IT-Cmd Remove-Partner"))
        def partner = partnerCommandService.save(newPartner("IT-Cmd Remove-Partner"))
        commandService.assignToPartner(freelancer.id, partner.id)

        when:
        commandService.removeFromPartner(freelancer.id, partner.id)

        then:
        def count = jdbcClient.sql("SELECT COUNT(*) FROM freelancer WHERE id = :id AND partner_id IS NULL")
                .param("id", freelancer.id).query(Long.class).single()
        count == 1
    }

    def "findByCode mit existierendem Code gibt Freelancer zurueck"() {
        given:
        def freelancer = newFreelancer("IT-Cmd FindByCode")
        freelancer.code = "IT-CMD-CODE-001"
        def saved = commandService.save(freelancer)

        when:
        def result = commandService.findByCode("IT-CMD-CODE-001")

        then:
        result.isPresent()
        result.get().id() == saved.id
    }

    def "findByCode mit unbekanntem Code gibt Optional.empty() zurueck"() {
        when:
        def result = commandService.findByCode("UNBEKANNT-9999")

        then:
        result.isEmpty()
    }

    def "zweites Speichern loescht creationDate und creationUser in der DB nicht"() {
        given:
        // Erster Speichervorgang (INSERT) setzt Audit-Felder
        def first = commandService.save(newFreelancer("IT-Cmd AuditPreserve"))
        assert first.creationDate != null
        assert first.creationUser != null

        when:
        // Simuliert Controller @ModelAttribute-Binding: neues Objekt ohne Audit-Felder, nur ID gesetzt
        def fromForm = newFreelancer("IT-Cmd AuditPreserve")
        fromForm.id = first.id
        fromForm.dbVersion = first.dbVersion
        fromForm.name2 = "Geändert via Form"
        // creationDate und creationUser bleiben bewusst null (so wie beim Form-Submit)
        commandService.save(fromForm)

        then:
        // DB-Werte müssen erhalten bleiben
        def dbCreationDate = jdbcClient
            .sql("SELECT creation_date FROM freelancer WHERE id = :id")
            .param("id", first.id)
            .query(java.time.LocalDateTime.class).single()
        def dbCreationUser = jdbcClient
            .sql("SELECT creation_user FROM freelancer WHERE id = :id")
            .param("id", first.id)
            .query(String.class).single()
        dbCreationDate != null
        dbCreationUser != null
        dbCreationUser == first.creationUser
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

    private static de.mirkosertic.powerstaff.partner.command.Partner newPartner(String company) {
        def p = new de.mirkosertic.powerstaff.partner.command.Partner()
        p.company = company
        p.contactForbidden = false
        p.showAgain = false
        p
    }
}
