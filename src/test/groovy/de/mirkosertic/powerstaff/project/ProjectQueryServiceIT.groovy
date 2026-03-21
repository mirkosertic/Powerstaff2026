package de.mirkosertic.powerstaff.project

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.project.command.Project
import de.mirkosertic.powerstaff.project.command.ProjectCommandService
import de.mirkosertic.powerstaff.project.query.ProjectQueryService
import de.mirkosertic.powerstaff.project.query.ProjectSearchCriteria
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class ProjectQueryServiceIT extends AbstractContainerBaseIT {

    @Autowired
    ProjectCommandService commandService

    @Autowired
    ProjectQueryService queryService

    @Autowired
    JdbcClient jdbcClient

    def cleanup() {
        jdbcClient.sql("DELETE FROM project WHERE project_number LIKE 'IT-PQS%'").update()
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    def "findById liefert korrekte Felder"() {
        given:
        def saved = commandService.save(newProject("IT-PQS-001", "Testprojekt Alpha"))

        when:
        def result = queryService.findById(saved.id)

        then:
        result.isPresent()
        result.get().id() == saved.id
        result.get().projectNumber() == "IT-PQS-001"
        result.get().descriptionShort() == "Testprojekt Alpha"
    }

    def "findById fuer unbekannte ID liefert empty"() {
        expect:
        queryService.findById(-1L).isEmpty()
    }

    def "findFirst liefert den Project mit der kleinsten ID"() {
        given:
        def p1 = commandService.save(newProject("IT-PQS-002"))
        def p2 = commandService.save(newProject("IT-PQS-003"))

        when:
        def first = queryService.findFirst()

        then:
        first.isPresent()
        first.get().id() <= p1.id
        first.get().id() <= p2.id
    }

    def "findLast liefert den Project mit der groessten ID"() {
        given:
        def p1 = commandService.save(newProject("IT-PQS-004"))
        def p2 = commandService.save(newProject("IT-PQS-005"))

        when:
        def last = queryService.findLast()

        then:
        last.isPresent()
        last.get().id() >= p1.id
        last.get().id() >= p2.id
    }

    def "findNext liefert das naechste Projekt nach ID"() {
        given:
        def p1 = commandService.save(newProject("IT-PQS-006"))
        commandService.save(newProject("IT-PQS-007"))

        when:
        def next = queryService.findNext(p1.id)

        then:
        next.isPresent()
        next.get().id() > p1.id
    }

    def "findPrevious liefert das vorherige Projekt vor ID"() {
        given:
        commandService.save(newProject("IT-PQS-008"))
        def p2 = commandService.save(newProject("IT-PQS-009"))

        when:
        def prev = queryService.findPrevious(p2.id)

        then:
        prev.isPresent()
        prev.get().id() < p2.id
    }

    // ─── QBE-Suche ────────────────────────────────────────────────────────────

    def "Suche ohne Kriterien liefert alle Projekte"() {
        given:
        commandService.save(newProject("IT-PQS-010"))
        commandService.save(newProject("IT-PQS-011"))
        def criteria = new ProjectSearchCriteria(null, null, null, null, null, null, null, null, null, null, null)

        when:
        def results = queryService.search(criteria, 0, 100)

        then:
        results.size() >= 2
    }

    def "Suche nach projectNumber filtert korrekt (LIKE)"() {
        given:
        commandService.save(newProject("IT-PQS-012-ALPHA"))
        commandService.save(newProject("IT-PQS-013-BETA"))
        def criteria = new ProjectSearchCriteria("ALPHA", null, null, null, null, null, null, null, null, null, null)

        when:
        def results = queryService.search(criteria, 0, 100)

        then:
        results.size() == 1
        results[0].projectNumber() == "IT-PQS-012-ALPHA"
    }

    def "Suche nach status filtert exakt"() {
        given:
        def p1 = newProject("IT-PQS-014")
        p1.status = 1
        commandService.save(p1)
        def p2 = newProject("IT-PQS-015")
        p2.status = 2
        commandService.save(p2)
        def criteria = new ProjectSearchCriteria(null, null, null, null, null, null, 2, null, null, null, null)

        when:
        def results = queryService.search(criteria, 0, 100)

        then:
        results.every { it.status() == 2 }
        results.any { it.projectNumber() == "IT-PQS-015" }
        results.every { it.projectNumber() != "IT-PQS-014" }
    }

    def "Suche ohne Treffer liefert leere Liste"() {
        given:
        def criteria = new ProjectSearchCriteria("XXXXXXXX_NICHT_VORHANDEN", null, null, null, null, null, null, null, null, null, null)

        expect:
        queryService.search(criteria, 0, 100).isEmpty()
        queryService.countSearch(criteria) == 0
    }

    def "countSearch liefert korrekte Anzahl"() {
        given:
        commandService.save(newProject("IT-PQS-016"))
        commandService.save(newProject("IT-PQS-017"))
        def criteria = new ProjectSearchCriteria("IT-PQS-01", null, null, null, null, null, null, null, null, null, null)

        when:
        def count = queryService.countSearch(criteria)

        then:
        count >= 2
    }

    // ─── Sort-Tests ───────────────────────────────────────────────────────────

    def "Suche mit sortField=project_number asc liefert aufsteigende Reihenfolge"() {
        given:
        commandService.save(newProject("IT-PQS-SORT-C"))
        commandService.save(newProject("IT-PQS-SORT-A"))
        commandService.save(newProject("IT-PQS-SORT-B"))

        when:
        def results = queryService.search(
            new ProjectSearchCriteria("IT-PQS-SORT-", null, null, null, null, null, null, null, null, "project_number", "asc"),
            0, 20
        )

        then:
        results.size() == 3
        results[0].projectNumber() == "IT-PQS-SORT-A"
        results[1].projectNumber() == "IT-PQS-SORT-B"
        results[2].projectNumber() == "IT-PQS-SORT-C"
    }

    def "Suche mit sortField=project_number desc liefert absteigende Reihenfolge"() {
        given:
        commandService.save(newProject("IT-PQS-DESC-C"))
        commandService.save(newProject("IT-PQS-DESC-A"))
        commandService.save(newProject("IT-PQS-DESC-B"))

        when:
        def results = queryService.search(
            new ProjectSearchCriteria("IT-PQS-DESC-", null, null, null, null, null, null, null, null, "project_number", "desc"),
            0, 20
        )

        then:
        results.size() == 3
        results[0].projectNumber() == "IT-PQS-DESC-C"
        results[1].projectNumber() == "IT-PQS-DESC-B"
        results[2].projectNumber() == "IT-PQS-DESC-A"
    }

    def "Suche mit ungueltigem sortField faellt auf Default-Sortierung zurueck (keine Exception)"() {
        when:
        def results = queryService.search(
            new ProjectSearchCriteria(null, null, null, null, null, null, null, null, null, 'INVALID; DROP TABLE project; --', "asc"),
            0, 20
        )

        then:
        notThrown(Exception)
        results != null
    }

    private static Project newProject(String projectNumber, String descriptionShort = null) {
        def p = new Project()
        p.projectNumber = projectNumber
        p.descriptionShort = descriptionShort
        p.status = 1
        p.visibleOnWebSite = false
        p
    }
}
