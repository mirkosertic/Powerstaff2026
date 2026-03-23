package de.mirkosertic.powerstaff.partner

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.partner.command.Partner
import de.mirkosertic.powerstaff.partner.command.PartnerCommandService
import de.mirkosertic.powerstaff.partner.command.PartnerContactEntry
import de.mirkosertic.powerstaff.partner.command.PartnerHasProjectsException
import de.mirkosertic.powerstaff.partner.command.PartnerHistoryEntry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class PartnerCommandServiceIT extends AbstractContainerBaseIT {

    @Autowired
    PartnerCommandService commandService

    @Autowired
    JdbcClient jdbcClient

    Long historyTypeId

    def setup() {
        jdbcClient.sql("INSERT INTO historytype (description) VALUES ('IT-PCmdTyp')").update()
        historyTypeId = jdbcClient.sql("SELECT id FROM historytype WHERE description = 'IT-PCmdTyp'")
                .query(Long.class).single()
    }

    def cleanup() {
        jdbcClient.sql("DELETE FROM partner_history WHERE partner_id IN (SELECT id FROM partner WHERE company LIKE 'IT-Cmd%')").update()
        jdbcClient.sql("DELETE FROM partner_contact WHERE partner_id IN (SELECT id FROM partner WHERE company LIKE 'IT-Cmd%')").update()
        jdbcClient.sql("DELETE FROM partner WHERE company LIKE 'IT-Cmd%'").update()
        jdbcClient.sql("DELETE FROM historytype WHERE description = 'IT-PCmdTyp'").update()
    }

    def "delete ohne Projektzuordnung loescht den Partner"() {
        given:
        def partner = commandService.save(newPartner("IT-Cmd Delete"))

        when:
        commandService.deleteById(partner.id)

        then:
        commandService.findById(partner.id).isEmpty()
    }

    def "delete mit Projektzuordnung wirft PartnerHasProjectsException"() {
        given:
        def partner = commandService.save(newPartner("IT-Cmd Restrict"))

        jdbcClient.sql("""
            INSERT INTO project (db_version, project_number, partner_id)
            VALUES (0, 'IT-CMD-PROJ-001', :partnerId)
        """).param("partnerId", partner.id).update()
        def projectId = jdbcClient.sql("SELECT id FROM project WHERE project_number = 'IT-CMD-PROJ-001'")
                .query(Long).single()

        when:
        commandService.deleteById(partner.id)

        then:
        def ex = thrown(PartnerHasProjectsException)
        ex.projectIds.contains(projectId)

        cleanup:
        jdbcClient.sql("DELETE FROM project WHERE id = :id").param("id", projectId).update()
        jdbcClient.sql("DELETE FROM partner WHERE id = :id").param("id", partner.id).update()
    }

    def "save aktualisiert bestehenden Partner"() {
        given:
        def partner = commandService.save(newPartner("IT-Cmd Update"))

        when:
        partner.setCompany("IT-Cmd Geaendert")
        commandService.save(partner)

        then:
        commandService.findById(partner.id).get().company == "IT-Cmd Geaendert"
    }

    def "save fuegt Kontakt per ADD-Delta-Command hinzu"() {
        given:
        def partner = commandService.save(newPartner("IT-Cmd Add-Contact"))

        when:
        commandService.save(partner,
                [new PartnerContactEntry("ADD", null, "EMAIL", "partner@example.com")], [])

        then:
        jdbcClient.sql("SELECT COUNT(*) FROM partner_contact WHERE partner_id = :id")
                .param("id", partner.id).query(Long.class).single() == 1
    }

    def "save loescht Kontakt per DELETE-Delta-Command"() {
        given:
        def partner = commandService.save(newPartner("IT-Cmd Delete-Contact"),
                [new PartnerContactEntry("ADD", null, "EMAIL", "partner@example.com")], [])
        def contactId = jdbcClient.sql("SELECT id FROM partner_contact WHERE partner_id = :id")
                .param("id", partner.id).query(Long.class).single()

        when:
        commandService.save(partner,
                [new PartnerContactEntry("DELETE", contactId, null, null)], [])

        then:
        jdbcClient.sql("SELECT COUNT(*) FROM partner_contact WHERE partner_id = :id")
                .param("id", partner.id).query(Long.class).single() == 0
    }

    def "DELETE Kontakt ohne ID wirft IllegalArgumentException"() {
        given:
        def partner = commandService.save(newPartner("IT-Cmd Delete-Contact-NoId"))

        when:
        commandService.save(partner,
                [new PartnerContactEntry("DELETE", null, null, null)], [])

        then:
        thrown(IllegalArgumentException)
    }

    def "save fuegt Kontakthistorieneintrag per ADD-Delta-Command hinzu"() {
        given:
        def partner = commandService.save(newPartner("IT-Cmd Add-History"))

        when:
        commandService.save(partner, [],
                [new PartnerHistoryEntry("ADD", null, historyTypeId, "Erstanlage")])

        then:
        jdbcClient.sql("SELECT COUNT(*) FROM partner_history WHERE partner_id = :id")
                .param("id", partner.id).query(Long.class).single() == 1
    }

    def "save aktualisiert Kontakthistorieneintrag per UPDATE-Delta-Command"() {
        given:
        def partner = commandService.save(newPartner("IT-Cmd Update-History"), [],
                [new PartnerHistoryEntry("ADD", null, historyTypeId, "Original")])
        def historyId = jdbcClient.sql("SELECT id FROM partner_history WHERE partner_id = :id")
                .param("id", partner.id).query(Long.class).single()

        when:
        commandService.save(partner, [],
                [new PartnerHistoryEntry("UPDATE", historyId, historyTypeId, "Geaendert")])

        then:
        jdbcClient.sql("SELECT description FROM partner_history WHERE id = :id")
                .param("id", historyId).query(String.class).single() == "Geaendert"
    }

    def "save loescht Kontakthistorieneintrag per DELETE-Delta-Command"() {
        given:
        def partner = commandService.save(newPartner("IT-Cmd Delete-History"), [],
                [new PartnerHistoryEntry("ADD", null, historyTypeId, "Zu loeschen")])
        def historyId = jdbcClient.sql("SELECT id FROM partner_history WHERE partner_id = :id")
                .param("id", partner.id).query(Long.class).single()

        when:
        commandService.save(partner, [],
                [new PartnerHistoryEntry("DELETE", historyId, null, null)])

        then:
        jdbcClient.sql("SELECT COUNT(*) FROM partner_history WHERE partner_id = :id")
                .param("id", partner.id).query(Long.class).single() == 0
    }

    def "UPDATE Kontakthistorie ohne ID wirft IllegalArgumentException"() {
        given:
        def partner = commandService.save(newPartner("IT-Cmd Update-History-NoId"))

        when:
        commandService.save(partner, [],
                [new PartnerHistoryEntry("UPDATE", null, historyTypeId, "Geaendert")])

        then:
        thrown(IllegalArgumentException)
    }

    def "DELETE Kontakthistorie ohne ID wirft IllegalArgumentException"() {
        given:
        def partner = commandService.save(newPartner("IT-Cmd Delete-History-NoId"))

        when:
        commandService.save(partner, [],
                [new PartnerHistoryEntry("DELETE", null, null, null)])

        then:
        thrown(IllegalArgumentException)
    }

    private static Partner newPartner(String company) {
        def p = new Partner()
        p.company = company
        p.contactForbidden = false
        p.showAgain = false
        p
    }
}
