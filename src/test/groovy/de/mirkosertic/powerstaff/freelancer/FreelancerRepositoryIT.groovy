package de.mirkosertic.powerstaff.freelancer

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.freelancer.command.Freelancer
import de.mirkosertic.powerstaff.freelancer.command.FreelancerCommandService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class FreelancerRepositoryIT extends AbstractContainerBaseIT {

    @Autowired
    FreelancerCommandService commandService

    @Autowired
    JdbcClient jdbcClient

    def cleanup() {
        jdbcClient.sql("DELETE FROM freelancer WHERE name1 LIKE 'IT-Repo%'").update()
    }

    def "Freiberufler speichern und per ID laden"() {
        given:
        def freelancer = newFreelancer("IT-Repo-Mustermann")

        when:
        def saved = commandService.save(freelancer)

        then:
        saved.id != null
        saved.dbVersion != null
        saved.creationDate != null
        saved.creationUser != null

        and:
        def loaded = commandService.findById(saved.id).orElseThrow()
        loaded.name1 == "IT-Repo-Mustermann"
        loaded.name2 == "Hans"
        loaded.city == "Berlin"
    }

    def "Freiberufler update erhöht db_version"() {
        given:
        def saved = commandService.save(newFreelancer("IT-Repo-Version"))
        def versionBefore = saved.dbVersion

        when:
        saved.setCity("München")
        def updated = commandService.save(saved)

        then:
        updated.dbVersion > versionBefore
        updated.city == "München"
    }

    def "Optimistic Locking verhindert gleichzeitiges Update"() {
        given:
        def saved = commandService.save(newFreelancer("IT-Repo-OL"))

        def copy1 = commandService.findById(saved.id).orElseThrow()
        def copy2 = commandService.findById(saved.id).orElseThrow()

        when:
        copy1.setCity("Hamburg")
        commandService.save(copy1)

        copy2.setCity("Köln")
        commandService.save(copy2)

        then:
        thrown(OptimisticLockingFailureException)
    }

    def "Audit-Felder werden beim Speichern befüllt"() {
        when:
        def saved = commandService.save(newFreelancer("IT-Repo-Audit"))

        then:
        saved.creationDate != null
        saved.creationUser != null
        saved.changedDate != null
        saved.changedUser != null
    }

    def "Code-Feld wird gespeichert und ist eindeutig lesbar"() {
        given:
        def f = newFreelancer("IT-Repo-Code")
        f.code = "IT-REPO-CODE-001"

        when:
        def saved = commandService.save(f)

        then:
        saved.code == "IT-REPO-CODE-001"
    }

    def "partnerId ist nullable"() {
        when:
        def saved = commandService.save(newFreelancer("IT-Repo-NoPartner"))

        then:
        saved.partnerId == null
    }

    private static Freelancer newFreelancer(String name1) {
        def f = new Freelancer()
        f.name1 = name1
        f.name2 = "Hans"
        f.street = "Teststr. 1"
        f.city = "Berlin"
        f.plz = "10115"
        f.country = "DE"
        f.contactForbidden = false
        f.showAgain = false
        f.datenschutz = false
        f
    }
}
