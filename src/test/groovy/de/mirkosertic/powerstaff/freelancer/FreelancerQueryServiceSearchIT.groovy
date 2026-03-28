package de.mirkosertic.powerstaff.freelancer

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.freelancer.command.Freelancer
import de.mirkosertic.powerstaff.freelancer.command.FreelancerCommandService
import de.mirkosertic.powerstaff.freelancer.query.FreelancerQueryService
import de.mirkosertic.powerstaff.freelancer.query.FreelancerSearchCriteria
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class FreelancerQueryServiceSearchIT extends AbstractContainerBaseIT {

    @Autowired
    FreelancerCommandService commandService

    @Autowired
    FreelancerQueryService queryService

    @Autowired
    JdbcClient jdbcClient

    def setup() {
        def f1 = newFreelancer("IT-Srch-Alpha", "Muster GmbH", "Berlin", "Java Kotlin Spring", 800L, 700L)
        def f2 = newFreelancer("IT-Srch-Beta", "Beispiel AG", "Hamburg", "Python DevOps", 600L, 500L)
        def f3 = newFreelancer("IT-Srch-Gamma", "Test OHG", "München", "Java React", 900L, 800L)
        commandService.save(f1)
        commandService.save(f2)
        commandService.save(f3)
    }

    def cleanup() {
        jdbcClient.sql("DELETE FROM freelancer WHERE name1 LIKE 'IT-Srch%'").update()
    }

    def "leere Kriterien liefern alle angelegten Datensätze"() {
        given:
        def criteria = emptyCriteria()

        when:
        def results = queryService.search(criteria, 0, 100)
        def count = queryService.countSearch(criteria)

        then:
        results.findAll { it.name1().startsWith("IT-Srch") }.size() == 3
        count >= 3
    }

    def "Suche nach name1 liefert genau einen Treffer"() {
        given:
        def criteria = new FreelancerSearchCriteria(
                "IT-Srch-Alpha", null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null)

        when:
        def results = queryService.search(criteria, 0, 100)

        then:
        results.size() == 1
        results[0].name1() == "IT-Srch-Alpha"
    }

    def "Suche nach company liefert genau einen Treffer"() {
        given:
        def criteria = new FreelancerSearchCriteria(
                null, null, "Beispiel", null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null)

        when:
        def results = queryService.search(criteria, 0, 100)

        then:
        results.findAll { it.name1().startsWith("IT-Srch") }.size() == 1
        results.find { it.name1() == "IT-Srch-Beta" } != null
    }

    def "Suche nach name1 UND city kombiniert AND-Logik"() {
        given:
        def criteria = new FreelancerSearchCriteria(
                "IT-Srch-Gamma", null, null, null, null, null, "München",
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null)

        when:
        def results = queryService.search(criteria, 0, 100)

        then:
        results.size() == 1
        results[0].name1() == "IT-Srch-Gamma"
    }

    def "Suche nach name1 UND city ohne Schnittmenge liefert leere Liste"() {
        given:
        def criteria = new FreelancerSearchCriteria(
                "IT-Srch-Alpha", null, null, null, null, null, "Hamburg",
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null)

        when:
        def results = queryService.search(criteria, 0, 100)

        then:
        results.findAll { it.name1().startsWith("IT-Srch") }.isEmpty()
    }

    def "Suche nach skills (LIKE) trifft mehrere Treffer"() {
        given:
        def criteria = new FreelancerSearchCriteria(
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, "Java",
                null, null, null, null, null)

        when:
        def results = queryService.search(criteria, 0, 100)

        then:
        results.findAll { it.name1().startsWith("IT-Srch") }.size() == 2
    }

    def "Suche mit salaryLongMax filtert korrekt"() {
        given:
        def criteria = new FreelancerSearchCriteria(
                "IT-Srch", null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                700L, null, null, null, null)

        when:
        def results = queryService.search(criteria, 0, 100)

        then:
        results.every { it.salaryLong() == null || it.salaryLong() <= 700L }
        results.findAll { it.name1().startsWith("IT-Srch") }.size() == 1
    }

    def "Suche mit salaryPerDayLongMax filtert korrekt"() {
        given:
        def criteria = new FreelancerSearchCriteria(
                "IT-Srch", null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, 600L, null, null, null)

        when:
        def results = queryService.search(criteria, 0, 100)

        then:
        results.every { it.salaryPerDayLong() == null || it.salaryPerDayLong() <= 600L }
    }

    def "Paginierung liefert korrekte Teilmenge"() {
        given:
        def criteria = new FreelancerSearchCriteria(
                "IT-Srch", null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null)

        when:
        def page1 = queryService.search(criteria, 0, 2)
        def page2 = queryService.search(criteria, 2, 2)
        def count = queryService.countSearch(criteria)

        then:
        page1.size() == 2
        page2.size() == 1
        count == 3
    }

    private static Freelancer newFreelancer(String name1, String company, String city,
                                            String skills, Long salaryLong, Long salaryPerDayLong) {
        def f = new Freelancer()
        f.name1 = name1
        f.name2 = "Test"
        f.company = company
        f.city = city
        f.skills = skills
        f.salaryLong = salaryLong
        f.salaryPerDayLong = salaryPerDayLong
        f.contactForbidden = false
        f.showAgain = false
        f.datenschutz = false
        f
    }

    // ─── Sort-Tests ───────────────────────────────────────────────────────────

    def "Suche mit sortField=name1 asc liefert korrekte Reihenfolge"() {
        given:
        def criteria = new FreelancerSearchCriteria(
                "IT-Srch", null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, "name1", "asc", null)

        when:
        def results = queryService.search(criteria, 0, 100)

        then:
        def relevant = results.findAll { it.name1().startsWith("IT-Srch") }
        relevant.size() == 3
        relevant == relevant.sort(false) { it.name1() }
    }

    def "Suche mit sortField=name1 desc liefert absteigende Reihenfolge"() {
        given:
        def criteria = new FreelancerSearchCriteria(
                "IT-Srch", null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, "name1", "desc", null)

        when:
        def results = queryService.search(criteria, 0, 100)

        then:
        def relevant = results.findAll { it.name1().startsWith("IT-Srch") }
        relevant.size() == 3
        relevant == relevant.sort(false) { a, b -> b.name1() <=> a.name1() }
    }

    def "Suche mit ungueltigem sortField faellt auf Default-Sortierung zurueck (keine Exception)"() {
        given:
        def criteria = new FreelancerSearchCriteria(
                "IT-Srch", null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, 'INVALID_FIELD__$(rm -rf /)', "asc", null)

        when:
        def results = queryService.search(criteria, 0, 100)

        then:
        notThrown(Exception)
        results.findAll { it.name1().startsWith("IT-Srch") }.size() == 3
    }

    private static FreelancerSearchCriteria emptyCriteria() {
        new FreelancerSearchCriteria(
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null)
    }
}
