package de.mirkosertic.powerstaff.customer

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.customer.command.Kunde
import de.mirkosertic.powerstaff.customer.command.KundeCommandService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class KundeRepositoryIT extends AbstractContainerBaseIT {

    @Autowired
    KundeCommandService commandService

    @Autowired
    JdbcClient jdbcClient

    def cleanup() {
        jdbcClient.sql("DELETE FROM kunde WHERE company LIKE 'IT-Test%'").update()
    }

    def "Kunde speichern und per ID laden"() {
        given:
        def kunde = newKunde("IT-Test GmbH")

        when:
        def saved = commandService.save(kunde)

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

    def "Kunde update erhoeht db_version"() {
        given:
        def saved = commandService.save(newKunde("IT-Test Version GmbH"))
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
        def saved = commandService.save(newKunde("IT-Test OL GmbH"))

        def copy1 = commandService.findById(saved.id).orElseThrow()
        def copy2 = commandService.findById(saved.id).orElseThrow()

        when:
        copy1.setCity("Hamburg")
        commandService.save(copy1)

        copy2.setCity("Muenchen")
        commandService.save(copy2)

        then:
        thrown(OptimisticLockingFailureException)
    }

    def "Kunde loeschen entfernt den Eintrag"() {
        given:
        def saved = commandService.save(newKunde("IT-Test Delete GmbH"))

        when:
        commandService.deleteById(saved.id)

        then:
        commandService.findById(saved.id).isEmpty()
    }

    def "Audit-Felder werden beim Speichern befuellt"() {
        when:
        def saved = commandService.save(newKunde("IT-Test Audit GmbH"))

        then:
        saved.creationDate != null
        saved.creationUser != null
        saved.changedDate != null
        saved.changedUser != null
    }

    private static Kunde newKunde(String company) {
        def k = new Kunde()
        k.company = company
        k.name1 = "Max"
        k.name2 = "Muster"
        k.street = "Musterstr. 1"
        k.country = "DE"
        k.plz = "12345"
        k.city = "Musterstadt"
        k.contactForbidden = false
        k.showAgain = true
        k
    }
}
