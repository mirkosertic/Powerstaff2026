package de.mirkosertic.powerstaff.partner

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.partner.command.Partner
import de.mirkosertic.powerstaff.partner.command.PartnerCommandService
import de.mirkosertic.powerstaff.partner.query.PartnerQueryService
import de.mirkosertic.powerstaff.partner.query.PartnerSearchCriteria
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class PartnerQueryServiceIT extends AbstractContainerBaseIT {

    @Autowired
    PartnerCommandService commandService

    @Autowired
    PartnerQueryService queryService

    @Autowired
    JdbcClient jdbcClient

    def cleanup() {
        jdbcClient.sql("DELETE FROM partner WHERE company LIKE 'IT-PQS%'").update()
    }

    // ─── QBE Suche ────────────────────────────────────────────────────────────

    def "Suche ohne Kriterien liefert alle Partner"() {
        given:
        commandService.save(newPartner("IT-PQS Search A"))
        commandService.save(newPartner("IT-PQS Search B"))

        when:
        def results = queryService.search(PartnerSearchCriteria.empty(), 0, 100)

        then:
        results.size() >= 2
    }

    def "Suche nach company filtert korrekt"() {
        given:
        commandService.save(newPartner("IT-PQS Alpha GmbH"))
        commandService.save(newPartner("IT-PQS Beta AG"))

        when:
        def results = queryService.search(PartnerSearchCriteria.empty().withCompany("IT-PQS Alpha"), 0, 100)

        then:
        results.size() == 1
        results[0].company() == "IT-PQS Alpha GmbH"
    }

    def "countSearch liefert korrekte Anzahl"() {
        given:
        commandService.save(newPartner("IT-PQS Count A GmbH"))
        commandService.save(newPartner("IT-PQS Count B GmbH"))

        when:
        def count = queryService.countSearch(PartnerSearchCriteria.empty().withCompany("IT-PQS Count"))

        then:
        count == 2
    }

    // ─── Sort-Tests ───────────────────────────────────────────────────────────

    def "Suche mit sortField=company asc liefert aufsteigende Reihenfolge"() {
        given:
        commandService.save(newPartner("IT-PQS Sort C"))
        commandService.save(newPartner("IT-PQS Sort A"))
        commandService.save(newPartner("IT-PQS Sort B"))

        when:
        def results = queryService.search(
            PartnerSearchCriteria.empty().withCompany("IT-PQS Sort").withSortField("company").withSortDir("asc"),
            0, 20
        )

        then:
        results.size() == 3
        results[0].company() == "IT-PQS Sort A"
        results[1].company() == "IT-PQS Sort B"
        results[2].company() == "IT-PQS Sort C"
    }

    def "Suche mit sortField=name2 asc liefert aufsteigende Reihenfolge nach name2"() {
        given:
        def pC = newPartner("IT-PQS SortN2 X", null, "C-Nachname")
        def pA = newPartner("IT-PQS SortN2 X", null, "A-Nachname")
        def pB = newPartner("IT-PQS SortN2 X", null, "B-Nachname")
        commandService.save(pC)
        commandService.save(pA)
        commandService.save(pB)

        when:
        def results = queryService.search(
            PartnerSearchCriteria.empty().withCompany("IT-PQS SortN2").withSortField("name2").withSortDir("asc"),
            0, 20
        )

        then:
        results.size() == 3
        results[0].name2() == "A-Nachname"
        results[1].name2() == "B-Nachname"
        results[2].name2() == "C-Nachname"
    }

    def "Suche mit sortField=name2 desc liefert absteigende Reihenfolge nach name2"() {
        given:
        def pC = newPartner("IT-PQS SortN2Desc X", null, "C-Nachname")
        def pA = newPartner("IT-PQS SortN2Desc X", null, "A-Nachname")
        def pB = newPartner("IT-PQS SortN2Desc X", null, "B-Nachname")
        commandService.save(pC)
        commandService.save(pA)
        commandService.save(pB)

        when:
        def results = queryService.search(
            PartnerSearchCriteria.empty().withCompany("IT-PQS SortN2Desc").withSortField("name2").withSortDir("desc"),
            0, 20
        )

        then:
        results.size() == 3
        results[0].name2() == "C-Nachname"
        results[1].name2() == "B-Nachname"
        results[2].name2() == "A-Nachname"
    }

    def "Suche mit ungueltigem sortField faellt auf Default-Sortierung zurueck (keine Exception)"() {
        when:
        def results = queryService.search(
            PartnerSearchCriteria.empty().withSortField('DROP TABLE partner; --').withSortDir("asc"),
            0, 20
        )

        then:
        notThrown(Exception)
        results != null
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
