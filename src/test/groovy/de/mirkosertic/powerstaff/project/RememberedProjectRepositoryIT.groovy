package de.mirkosertic.powerstaff.project

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.project.command.Project
import de.mirkosertic.powerstaff.project.command.ProjectCommandService
import de.mirkosertic.powerstaff.project.command.RememberedProjectService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class RememberedProjectRepositoryIT extends AbstractContainerBaseIT {

    @Autowired
    RememberedProjectService service

    @Autowired
    ProjectCommandService projectCommandService

    @Autowired
    JdbcClient jdbcClient

    Long projectId1
    Long projectId2

    def setup() {
        def p1 = new Project()
        p1.projectNumber = "IT-REM-P001"
        p1.status = 1
        p1.visibleOnWebSite = false
        projectId1 = projectCommandService.save(p1).id

        def p2 = new Project()
        p2.projectNumber = "IT-REM-P002"
        p2.status = 1
        p2.visibleOnWebSite = false
        projectId2 = projectCommandService.save(p2).id
    }

    def cleanup() {
        jdbcClient.sql("DELETE FROM remembered_project WHERE user_id LIKE 'IT-REM-%'").update()
        jdbcClient.sql("DELETE FROM project WHERE project_number LIKE 'IT-REM-%'").update()
    }

    def "set speichert ein gemerktes Projekt fuer den User"() {
        when:
        service.set("IT-REM-user1", projectId1)

        then:
        service.get("IT-REM-user1").isPresent()
        service.get("IT-REM-user1").get() == projectId1
    }

    def "set ueberschreibt ein vorhandenes gemerktes Projekt (Upsert)"() {
        given:
        service.set("IT-REM-user2", projectId1)

        when:
        service.set("IT-REM-user2", projectId2)

        then:
        service.get("IT-REM-user2").get() == projectId2
    }

    def "get fuer unbekannten User liefert empty"() {
        expect:
        service.get("IT-REM-unbekannt").isEmpty()
    }

    def "clear entfernt den gemerkten Eintrag"() {
        given:
        service.set("IT-REM-user3", projectId1)

        when:
        service.clear("IT-REM-user3")

        then:
        service.get("IT-REM-user3").isEmpty()
    }

    // -------------------------------------------------------------------------
    // getRememberedProjectInfo
    // -------------------------------------------------------------------------

    def "getRememberedProjectInfo ohne gemerktes Projekt liefert empty"() {
        expect:
        service.getRememberedProjectInfo("IT-REM-unbekannt").isEmpty()
    }

    def "getRememberedProjectInfo mit gemerktem Projekt liefert RememberedProjectInfo mit projectNumber"() {
        given:
        service.set("IT-REM-user-info", projectId1)

        when:
        def info = service.getRememberedProjectInfo("IT-REM-user-info")

        then:
        info.isPresent()
        info.get().projectNumber() == "IT-REM-P001"
    }

    def "getRememberedProjectInfo nach clear liefert empty"() {
        given:
        service.set("IT-REM-user-clear", projectId1)
        service.clear("IT-REM-user-clear")

        expect:
        service.getRememberedProjectInfo("IT-REM-user-clear").isEmpty()
    }
}
