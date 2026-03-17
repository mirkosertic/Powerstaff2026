package de.mirkosertic.powerstaff.partner

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.partner.command.Partner
import de.mirkosertic.powerstaff.partner.command.PartnerCommandService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.dao.OptimisticLockingFailureException

@SpringBootTest
class PartnerRepositoryIT extends AbstractContainerBaseIT {

    @Autowired
    PartnerCommandService commandService

    @Autowired
    JdbcClient jdbcClient

    def cleanup() {
        jdbcClient.sql("DELETE FROM partner WHERE company LIKE 'IT-Test%'").update()
    }

    def "Partner speichern und per ID laden"() {
        given:
        def partner = newPartner("IT-Test GmbH")

        when:
        def saved = commandService.save(partner)

        then:
        saved.id != null
        saved.dbVersion != null
        saved.creationDate != null
        saved.creationUser != null

        and:
        def loaded = commandService.findById(saved.id).orElseThrow()
        loaded.company == "IT-Test GmbH"
        loaded.name1 == "Max"
        loaded.name2 == "Muster"
    }

    def "Partner update erhöht db_version"() {
        given:
        def saved = commandService.save(newPartner("IT-Test Version GmbH"))
        def versionBefore = saved.dbVersion

        when:
        saved.setCity("Berlin")
        def updated = commandService.save(saved)

        then:
        updated.dbVersion > versionBefore
        updated.city == "Berlin"
    }

    def "Optimistic Locking verhindert gleichzeitiges Update"() {
        given:
        def saved = commandService.save(newPartner("IT-Test OL GmbH"))

        def copy1 = commandService.findById(saved.id).orElseThrow()
        def copy2 = commandService.findById(saved.id).orElseThrow()

        when:
        copy1.setCity("Hamburg")
        commandService.save(copy1)

        copy2.setCity("München")
        commandService.save(copy2)

        then:
        thrown(OptimisticLockingFailureException)
    }

    def "Partner loeschen entfernt den Eintrag"() {
        given:
        def saved = commandService.save(newPartner("IT-Test Delete GmbH"))

        when:
        commandService.deleteById(saved.id)

        then:
        commandService.findById(saved.id).isEmpty()
    }

    def "Audit-Felder werden beim Speichern befüllt"() {
        when:
        def saved = commandService.save(newPartner("IT-Test Audit GmbH"))

        then:
        saved.creationDate != null
        saved.creationUser != null
        saved.changedDate != null
        saved.changedUser != null
    }

    private static Partner newPartner(String company) {
        def p = new Partner()
        p.company = company
        p.name1 = "Max"
        p.name2 = "Muster"
        p.street = "Musterstr. 1"
        p.country = "DE"
        p.plz = "12345"
        p.city = "Musterstadt"
        p.contactForbidden = false
        p.showAgain = true
        p
    }
}
