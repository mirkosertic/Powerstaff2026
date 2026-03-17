package de.mirkosertic.powerstaff.partner

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.partner.command.Partner
import de.mirkosertic.powerstaff.partner.command.PartnerCommandService
import de.mirkosertic.powerstaff.partner.query.PartnerQueryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class PartnerQueryServiceNavigationIT extends AbstractContainerBaseIT {

    @Autowired
    PartnerCommandService commandService

    @Autowired
    PartnerQueryService queryService

    @Autowired
    JdbcClient jdbcClient

    def cleanup() {
        jdbcClient.sql("DELETE FROM partner WHERE company LIKE 'IT-Nav%'").update()
    }

    def "findById liefert korrekte Felder"() {
        given:
        def saved = commandService.save(newPartner("IT-Nav FindById", "Mueller", "Hans"))

        when:
        def result = queryService.findById(saved.id)

        then:
        result.isPresent()
        result.get().id() == saved.id
        result.get().company() == "IT-Nav FindById"
        result.get().name1() == "Mueller"
        result.get().name2() == "Hans"
    }

    def "findById fuer unbekannte ID liefert empty"() {
        expect:
        queryService.findById(-1L).isEmpty()
    }

    def "findFirst liefert den Partner mit der kleinsten ID"() {
        given:
        def p1 = commandService.save(newPartner("IT-Nav First A"))
        def p2 = commandService.save(newPartner("IT-Nav First B"))

        when:
        def first = queryService.findFirst()

        then:
        first.isPresent()
        first.get().id() <= p1.id
        first.get().id() <= p2.id
    }

    def "findLast liefert den Partner mit der groessten ID"() {
        given:
        def p1 = commandService.save(newPartner("IT-Nav Last A"))
        def p2 = commandService.save(newPartner("IT-Nav Last B"))

        when:
        def last = queryService.findLast()

        then:
        last.isPresent()
        last.get().id() >= p1.id
        last.get().id() >= p2.id
    }

    def "findNext liefert den naechsten Partner nach ID"() {
        given:
        def p1 = commandService.save(newPartner("IT-Nav Next A"))
        def p2 = commandService.save(newPartner("IT-Nav Next B"))

        when:
        def next = queryService.findNext(p1.id)

        then:
        next.isPresent()
        next.get().id() > p1.id
    }

    def "findPrevious liefert den vorherigen Partner vor ID"() {
        given:
        def p1 = commandService.save(newPartner("IT-Nav Prev A"))
        def p2 = commandService.save(newPartner("IT-Nav Prev B"))

        when:
        def prev = queryService.findPrevious(p2.id)

        then:
        prev.isPresent()
        prev.get().id() < p2.id
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

    private static Partner newPartner(String company, String name1 = null, String name2 = null) {
        def p = new Partner()
        p.company = company
        p.name1 = name1
        p.name2 = name2
        p.contactForbidden = false
        p.showAgain = false
        p
    }
}
