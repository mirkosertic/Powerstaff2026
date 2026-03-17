package de.mirkosertic.powerstaff.partner

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.partner.command.Partner
import de.mirkosertic.powerstaff.partner.command.PartnerCommandService
import de.mirkosertic.powerstaff.partner.command.PartnerHasProjectsException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class PartnerCommandServiceIT extends AbstractContainerBaseIT {

    @Autowired
    PartnerCommandService commandService

    @Autowired
    JdbcClient jdbcClient

    def cleanup() {
        jdbcClient.sql("DELETE FROM partner WHERE company LIKE 'IT-Cmd%'").update()
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

        // create a project referencing this partner directly in DB
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

    private static Partner newPartner(String company) {
        def p = new Partner()
        p.company = company
        p.contactForbidden = false
        p.showAgain = false
        p
    }
}
