package de.mirkosertic.powerstaff.project

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.project.command.BothFKsException
import de.mirkosertic.powerstaff.project.command.Project
import de.mirkosertic.powerstaff.project.command.ProjectCommandService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class ProjectCommandServiceIT extends AbstractContainerBaseIT {

    @Autowired
    ProjectCommandService commandService

    @Autowired
    JdbcClient jdbcClient

    def cleanup() {
        jdbcClient.sql("DELETE FROM remembered_project WHERE project_id IN (SELECT id FROM project WHERE project_number LIKE 'IT-PCS%')").update()
        jdbcClient.sql("DELETE FROM project WHERE project_number LIKE 'IT-PCS%'").update()
    }

    def "save valides Projekt speichert korrekt"() {
        given:
        def project = newProject("IT-PCS-001")

        when:
        def saved = commandService.save(project)

        then:
        saved.id != null
        saved.projectNumber == "IT-PCS-001"
    }

    def "save mit customerId und partnerId gleichzeitig wirft BothFKsException"() {
        given:
        def project = newProject("IT-PCS-002")
        project.customerId = 999L
        project.partnerId = 999L

        when:
        commandService.save(project)

        then:
        thrown(BothFKsException)
    }

    def "save aktualisiert bestehendes Projekt"() {
        given:
        def saved = commandService.save(newProject("IT-PCS-003"))

        when:
        saved.setDescriptionShort("Geaenderte Beschreibung")
        commandService.save(saved)

        then:
        commandService.findById(saved.id).get().descriptionShort == "Geaenderte Beschreibung"
    }

    def "Optimistic Locking verhindert gleichzeitiges Update"() {
        given:
        def saved = commandService.save(newProject("IT-PCS-004"))
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

    def "delete loescht das Projekt"() {
        given:
        def saved = commandService.save(newProject("IT-PCS-005"))

        when:
        commandService.deleteById(saved.id)

        then:
        commandService.findById(saved.id).isEmpty()
    }

    private static Project newProject(String projectNumber) {
        def p = new Project()
        p.projectNumber = projectNumber
        p.status = 1
        p.visibleOnWebSite = false
        p
    }
}
