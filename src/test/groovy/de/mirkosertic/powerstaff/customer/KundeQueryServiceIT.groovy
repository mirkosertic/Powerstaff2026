package de.mirkosertic.powerstaff.customer

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.customer.command.Kunde
import de.mirkosertic.powerstaff.customer.command.KundeCommandService
import de.mirkosertic.powerstaff.customer.query.KundeQueryService
import de.mirkosertic.powerstaff.customer.query.KundeSearchCriteria
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class KundeQueryServiceIT extends AbstractContainerBaseIT {

    @Autowired
    KundeCommandService commandService

    @Autowired
    KundeQueryService queryService

    @Autowired
    JdbcClient jdbcClient

    def cleanup() {
        jdbcClient.sql("DELETE FROM project WHERE project_number LIKE 'IT-KQS%'").update()
        jdbcClient.sql("DELETE FROM kunde WHERE company LIKE 'IT-KQS%'").update()
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    def "findById liefert korrekte Felder"() {
        given:
        def saved = commandService.save(newKunde("IT-KQS FindById", "Schmidt", "Klaus"))

        when:
        def result = queryService.findById(saved.id)

        then:
        result.isPresent()
        result.get().id() == saved.id
        result.get().company() == "IT-KQS FindById"
        result.get().name1() == "Schmidt"
        result.get().name2() == "Klaus"
    }

    def "findById fuer unbekannte ID liefert empty"() {
        expect:
        queryService.findById(-1L).isEmpty()
    }

    def "findFirst liefert den Kunden mit der kleinsten ID"() {
        given:
        def k1 = commandService.save(newKunde("IT-KQS First A"))
        def k2 = commandService.save(newKunde("IT-KQS First B"))

        when:
        def first = queryService.findFirst()

        then:
        first.isPresent()
        first.get().id() <= k1.id
        first.get().id() <= k2.id
    }

    def "findLast liefert den Kunden mit der groessten ID"() {
        given:
        def k1 = commandService.save(newKunde("IT-KQS Last A"))
        def k2 = commandService.save(newKunde("IT-KQS Last B"))

        when:
        def last = queryService.findLast()

        then:
        last.isPresent()
        last.get().id() >= k1.id
        last.get().id() >= k2.id
    }

    def "findNext liefert den naechsten Kunden nach ID"() {
        given:
        def k1 = commandService.save(newKunde("IT-KQS Next A"))
        commandService.save(newKunde("IT-KQS Next B"))

        when:
        def next = queryService.findNext(k1.id)

        then:
        next.isPresent()
        next.get().id() > k1.id
    }

    def "findPrevious liefert den vorherigen Kunden vor ID"() {
        given:
        commandService.save(newKunde("IT-KQS Prev A"))
        def k2 = commandService.save(newKunde("IT-KQS Prev B"))

        when:
        def prev = queryService.findPrevious(k2.id)

        then:
        prev.isPresent()
        prev.get().id() < k2.id
    }

    def "findNext am letzten Eintrag liefert empty"() {
        given:
        def last = queryService.findLast()
        long lastId = last.map { it.id() }.orElse(Long.MAX_VALUE)

        expect:
        queryService.findNext(lastId).isEmpty()
    }

    def "findPrevious am ersten Eintrag liefert empty"() {
        given:
        def first = queryService.findFirst()
        long firstId = first.map { it.id() }.orElse(1L)

        expect:
        queryService.findPrevious(firstId).isEmpty()
    }

    // ─── QBE Suche ────────────────────────────────────────────────────────────

    def "Suche ohne Kriterien liefert alle Kunden"() {
        given:
        commandService.save(newKunde("IT-KQS Search A"))
        commandService.save(newKunde("IT-KQS Search B"))
        def criteria = new KundeSearchCriteria(null, null, null, null, null, null, null, null, null, null)

        when:
        def results = queryService.search(criteria, 0, 100)

        then:
        results.size() >= 2
    }

    def "Suche nach company filtert korrekt"() {
        given:
        commandService.save(newKunde("IT-KQS Alpha GmbH"))
        commandService.save(newKunde("IT-KQS Beta AG"))
        def criteria = new KundeSearchCriteria("Alpha", null, null, null, null, null, null, null, null, null)

        when:
        def results = queryService.search(criteria, 0, 100)

        then:
        results.size() == 1
        results[0].company() == "IT-KQS Alpha GmbH"
    }

    def "Suche nach name1 und city kombiniert (AND)"() {
        given:
        def k = newKunde("IT-KQS Kombination AG")
        k.name1 = "Meier"
        k.city = "Hamburg"
        commandService.save(k)
        def criteria = new KundeSearchCriteria(null, "Meier", null, null, null, null, "Hamburg", null, null, null)

        when:
        def results = queryService.search(criteria, 0, 100)

        then:
        results.any { it.name1() == "Meier" }
    }

    def "countSearch liefert korrekte Anzahl"() {
        given:
        commandService.save(newKunde("IT-KQS Count A GmbH"))
        commandService.save(newKunde("IT-KQS Count B GmbH"))
        def criteria = new KundeSearchCriteria("IT-KQS Count", null, null, null, null, null, null, null, null, null)

        when:
        def count = queryService.countSearch(criteria)

        then:
        count == 2
    }

    def "Suche ohne Treffer liefert leere Liste"() {
        given:
        def criteria = new KundeSearchCriteria("XXXXXXX_NICHT_VORHANDEN", null, null, null, null, null, null, null, null, null)

        expect:
        queryService.search(criteria, 0, 100).isEmpty()
        queryService.countSearch(criteria) == 0
    }

    // ─── Projektliste ─────────────────────────────────────────────────────────

    def "findProjectsByKundeId liefert zugeordnete Projekte"() {
        given:
        def kunde = commandService.save(newKunde("IT-KQS ProjKunde"))
        jdbcClient.sql("""
            INSERT INTO project (db_version, project_number, customer_id, status)
            VALUES (0, 'IT-KQS-P001', :kundeId, 1)
        """).param("kundeId", kunde.id).update()
        jdbcClient.sql("""
            INSERT INTO project (db_version, project_number, customer_id, status)
            VALUES (0, 'IT-KQS-P002', :kundeId, 2)
        """).param("kundeId", kunde.id).update()

        when:
        def projects = queryService.findProjectsByKundeId(kunde.id, null, null)

        then:
        projects.size() == 2
        projects.any { it.projectNumber() == 'IT-KQS-P001' }
        projects.any { it.projectNumber() == 'IT-KQS-P002' }
    }

    def "findProjectsByKundeId fuer Kunden ohne Projekte liefert leere Liste"() {
        given:
        def kunde = commandService.save(newKunde("IT-KQS NoProjKunde"))

        expect:
        queryService.findProjectsByKundeId(kunde.id, null, null).isEmpty()
    }

    private static Kunde newKunde(String company, String name1 = null, String name2 = null) {
        def k = new Kunde()
        k.company = company
        k.name1 = name1
        k.name2 = name2
        k.contactForbidden = false
        k.showAgain = false
        k
    }
}
