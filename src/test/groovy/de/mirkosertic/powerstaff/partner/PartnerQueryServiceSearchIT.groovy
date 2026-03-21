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
class PartnerQueryServiceSearchIT extends AbstractContainerBaseIT {

    @Autowired
    PartnerCommandService commandService

    @Autowired
    PartnerQueryService queryService

    @Autowired
    JdbcClient jdbcClient

    def cleanup() {
        jdbcClient.sql("DELETE FROM project WHERE project_number LIKE 'IT-QBE-PROJ%'").update()
        jdbcClient.sql("UPDATE freelancer SET partner_id = NULL WHERE code LIKE 'IT-QBE%'").update()
        jdbcClient.sql("DELETE FROM freelancer WHERE code LIKE 'IT-QBE%'").update()
        jdbcClient.sql("DELETE FROM partner WHERE company LIKE 'IT-Qbe%'").update()
    }

    // ─── QBE: company filter ───────────────────────────────────────────────

    def "search mit company LIKE findet Treffer"() {
        given:
        commandService.save(newPartner("IT-Qbe Hamburg GmbH", "Hamburg"))
        commandService.save(newPartner("IT-Qbe Berlin AG", "Berlin"))

        when:
        def results = queryService.search(
            new PartnerSearchCriteria("Hamburg", null, null, null, null, null, null, null, null, null, null, null),
            0, 20
        )

        then:
        results.any { it.company() == "IT-Qbe Hamburg GmbH" }
        results.every { it.company().contains("Hamburg") }
    }

    def "search mit city LIKE findet Treffer"() {
        given:
        commandService.save(newPartner("IT-Qbe CityTest GmbH", "München"))

        when:
        def results = queryService.search(
            new PartnerSearchCriteria(null, null, null, null, null, null, "München", null, null, null, null, null),
            0, 20
        )

        then:
        results.any { it.company() == "IT-Qbe CityTest GmbH" }
    }

    def "search mit mehreren Feldern kombiniert AND"() {
        given:
        commandService.save(newPartner("IT-Qbe Multi A", "Hamburg"))
        commandService.save(newPartner("IT-Qbe Multi B", "Hamburg"))
        commandService.save(newPartner("IT-Qbe Multi C", "Berlin"))

        when:
        def results = queryService.search(
            new PartnerSearchCriteria("IT-Qbe Multi", null, null, null, null, null, "Hamburg", null, null, null, null, null),
            0, 20
        )

        then:
        results.size() == 2
        results.every { it.city() == "Hamburg" }
    }

    def "search mit leerem Kriterium liefert alle Partner (paginiert)"() {
        given:
        commandService.save(newPartner("IT-Qbe Paged A"))
        commandService.save(newPartner("IT-Qbe Paged B"))
        commandService.save(newPartner("IT-Qbe Paged C"))

        when:
        def results = queryService.search(PartnerSearchCriteria.empty(), 0, 20)

        then:
        results.size() >= 3
    }

    def "countSearch liefert korrekte Gesamtanzahl"() {
        given:
        commandService.save(newPartner("IT-Qbe Count X"))
        commandService.save(newPartner("IT-Qbe Count Y"))

        when:
        def total = queryService.countSearch(
            new PartnerSearchCriteria("IT-Qbe Count", null, null, null, null, null, null, null, null, null, null, null)
        )

        then:
        total == 2L
    }

    def "search ohne Treffer liefert leere Liste"() {
        when:
        def results = queryService.search(
            new PartnerSearchCriteria("ZZZNOMATCH999", null, null, null, null, null, null, null, null, null, null, null),
            0, 20
        )

        then:
        results.isEmpty()
    }

    // ─── Sublisten ────────────────────────────────────────────────────────

    def "findFreelancersByPartner liefert zugeordnete Freiberufler"() {
        given:
        def partner = commandService.save(newPartner("IT-Qbe FreelancerSub"))
        jdbcClient.sql("""
            INSERT INTO freelancer (db_version, code, name1, name2, partner_id)
            VALUES (0, 'IT-QBE-FL-001', 'Muster', 'Max', :pid)
        """).param("pid", partner.id).update()

        when:
        def list = queryService.findFreelancersByPartner(partner.id)

        then:
        list.size() == 1
        list[0].code() == 'IT-QBE-FL-001'
        list[0].name1() == 'Muster'
    }

    def "findProjectsByPartner liefert zugeordnete Projekte"() {
        given:
        def partner = commandService.save(newPartner("IT-Qbe ProjectSub"))
        jdbcClient.sql("""
            INSERT INTO project (db_version, project_number, description_short, partner_id)
            VALUES (0, 'IT-QBE-PROJ-001', 'Test Projekt', :pid)
        """).param("pid", partner.id).update()

        when:
        def list = queryService.findProjectsByPartner(partner.id)

        then:
        list.size() == 1
        list[0].projectNumber() == 'IT-QBE-PROJ-001'
    }

    def "findFreelancersByPartner fuer Partner ohne Freiberufler liefert leere Liste"() {
        given:
        def partner = commandService.save(newPartner("IT-Qbe EmptyFL"))

        expect:
        queryService.findFreelancersByPartner(partner.id).isEmpty()
    }

    // ─── Sort-Tests ────────────────────────────────────────────────────────────

    def "search mit sortField=company asc liefert aufsteigende Reihenfolge"() {
        given:
        commandService.save(newPartner("IT-Qbe Sort C"))
        commandService.save(newPartner("IT-Qbe Sort A"))
        commandService.save(newPartner("IT-Qbe Sort B"))

        when:
        def results = queryService.search(
            new PartnerSearchCriteria("IT-Qbe Sort", null, null, null, null, null, null, null, null, null, "company", "asc"),
            0, 20
        )

        then:
        results.size() == 3
        results[0].company() == "IT-Qbe Sort A"
        results[1].company() == "IT-Qbe Sort B"
        results[2].company() == "IT-Qbe Sort C"
    }

    def "search mit sortField=company desc liefert absteigende Reihenfolge"() {
        given:
        commandService.save(newPartner("IT-Qbe Desc C"))
        commandService.save(newPartner("IT-Qbe Desc A"))
        commandService.save(newPartner("IT-Qbe Desc B"))

        when:
        def results = queryService.search(
            new PartnerSearchCriteria("IT-Qbe Desc", null, null, null, null, null, null, null, null, null, "company", "desc"),
            0, 20
        )

        then:
        results.size() == 3
        results[0].company() == "IT-Qbe Desc C"
        results[1].company() == "IT-Qbe Desc B"
        results[2].company() == "IT-Qbe Desc A"
    }

    def "search mit ungueltigem sortField faellt auf Default-Sortierung zurueck (keine Exception)"() {
        when:
        def results = queryService.search(
            new PartnerSearchCriteria(null, null, null, null, null, null, null, null, null, null, 'DROP TABLE partner; --', "asc"),
            0, 20
        )

        then:
        notThrown(Exception)
        results != null
    }

    private static Partner newPartner(String company, String city = null) {
        def p = new Partner()
        p.company = company
        p.city = city
        p.contactForbidden = false
        p.showAgain = false
        p
    }
}
