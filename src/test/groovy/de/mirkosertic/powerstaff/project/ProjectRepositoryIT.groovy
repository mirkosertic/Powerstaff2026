package de.mirkosertic.powerstaff.project

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.project.command.Project
import de.mirkosertic.powerstaff.project.command.ProjectCommandService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class ProjectRepositoryIT extends AbstractContainerBaseIT {

    @Autowired
    ProjectCommandService commandService

    @Autowired
    JdbcClient jdbcClient

    def cleanup() {
        jdbcClient.sql("DELETE FROM project WHERE project_number LIKE 'IT-PRJ%'").update()
    }

    def "Project speichern und per ID laden"() {
        given:
        def project = newProject("IT-PRJ-001")

        when:
        def saved = commandService.save(project)

        then:
        saved.id != null
        saved.dbVersion != null
        saved.creationDate != null
        saved.creationUser != null

        and:
        def loaded = commandService.findById(saved.id).orElseThrow()
        loaded.projectNumber == "IT-PRJ-001"
        loaded.status == 1
    }

    def "Project update erhoeht db_version"() {
        given:
        def saved = commandService.save(newProject("IT-PRJ-002"))
        def versionBefore = saved.dbVersion

        when:
        saved.setDescriptionShort("Neue Beschreibung")
        def updated = commandService.save(saved)

        then:
        updated.dbVersion > versionBefore
        updated.descriptionShort == "Neue Beschreibung"
    }

    def "Optimistic Locking verhindert gleichzeitiges Update"() {
        given:
        def saved = commandService.save(newProject("IT-PRJ-003"))
        def copy1 = commandService.findById(saved.id).orElseThrow()
        def copy2 = commandService.findById(saved.id).orElseThrow()

        when:
        copy1.setWorkplace("München")
        commandService.save(copy1)

        copy2.setWorkplace("Berlin")
        commandService.save(copy2)

        then:
        thrown(OptimisticLockingFailureException)
    }

    def "Project mit customerId (nullable FK) speichern"() {
        given:
        def project = newProject("IT-PRJ-004")
        project.customerId = null
        project.partnerId = null

        when:
        def saved = commandService.save(project)

        then:
        saved.id != null
        saved.customerId == null
        saved.partnerId == null
    }

    def "Project loeschen entfernt den Eintrag"() {
        given:
        def saved = commandService.save(newProject("IT-PRJ-005"))

        when:
        commandService.deleteById(saved.id)

        then:
        commandService.findById(saved.id).isEmpty()
    }

    def "Audit-Felder werden beim Speichern befuellt"() {
        when:
        def saved = commandService.save(newProject("IT-PRJ-006"))

        then:
        saved.creationDate != null
        saved.creationUser != null
        saved.changedDate != null
        saved.changedUser != null
    }

    private static Project newProject(String projectNumber) {
        def p = new Project()
        p.projectNumber = projectNumber
        p.status = 1
        p.visibleOnWebSite = false
        p
    }
}
