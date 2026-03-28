package de.mirkosertic.powerstaff.profilesearch.query

import spock.lang.Specification
import spock.lang.Subject

class ProfileSearchQueryServiceSpec extends Specification {

    // JdbcClient is not used by the mock-data methods under test, so null is safe here
    @Subject
    ProfileSearchQueryService service = new ProfileSearchQueryService(null)

    // ── searchFreelancers ────────────────────────────────────────────────────

    def "searchFreelancers mit leerem Criteria liefert genau PAGE_SIZE=20 Ergebnisse"() {
        given:
        def criteria = new ProfileSearchCriteria(null, null, null, null, null, null)

        when:
        def results = service.searchFreelancers(criteria, 0, 20)

        then:
        results.size() == 20
    }

    def "searchFreelancers mit offset=0 und limit=20 liefert maximal 20 Ergebnisse"() {
        given:
        def criteria = new ProfileSearchCriteria(null, null, null, null, null, null)

        when:
        def results = service.searchFreelancers(criteria, 0, 20)

        then:
        results.size() <= 20
        results.size() == 20
    }

    def "searchFreelancers mit offset=20 und limit=20 liefert die naechsten 20 Eintraege"() {
        given:
        def criteria = new ProfileSearchCriteria(null, null, null, null, null, null)

        when:
        def page1 = service.searchFreelancers(criteria, 0, 20)
        def page2 = service.searchFreelancers(criteria, 20, 20)

        then:
        page2.size() == 20
        // Pages must not overlap
        page1.every { r1 -> page2.every { r2 -> r1.id() != r2.id() } }
    }

    def "searchFreelancers mit searchTerm filtert auf passende Namen"() {
        given:
        def criteria = new ProfileSearchCriteria("Mock Freelancer 1", null, null, null, null, null)

        when:
        def results = service.searchFreelancers(criteria, 0, 50)

        then:
        results.every { it.name1().toLowerCase().contains("1") }
        // "Mock Freelancer 1", "Mock Freelancer 10".."Mock Freelancer 19", "Mock Freelancer 21" etc.
        results.size() >= 1
    }

    def "searchFreelancers mit salaryPerDayFrom filtert auf Tagessatz >= Untergrenze"() {
        given:
        def criteria = new ProfileSearchCriteria(null, 500L, null, null, null, null)

        when:
        def results = service.searchFreelancers(criteria, 0, 50)

        then:
        results.every { it.salaryPerDayLong() >= 500L }
    }

    def "searchFreelancers mit salaryPerDayTo filtert auf Tagessatz <= Obergrenze"() {
        given:
        def criteria = new ProfileSearchCriteria(null, null, 500L, null, null, null)

        when:
        def results = service.searchFreelancers(criteria, 0, 50)

        then:
        results.every { it.salaryPerDayLong() <= 500L }
    }

    def "searchFreelancers: jeder 7. Eintrag (Index 0,7,14,...) hat contactForbidden=true"() {
        given:
        def criteria = new ProfileSearchCriteria(null, null, null, null, null, null)

        when:
        // Fetch all 50 mock entries without limit
        def results = service.searchFreelancers(criteria, 0, 50)

        then:
        // Indices 0, 7, 14, 21, 28, 35, 42, 49 have contactForbidden=true (i % 7 == 0)
        def forbidden = results.findAll { it.contactForbidden() }
        // The code field of mock entries is "MOCK-{100+i}", so "MOCK-100","MOCK-107",…
        forbidden.every { r ->
            def idx = (r.id() - 100L) as int
            idx % 7 == 0
        }
        forbidden.size() > 0
    }

    def "searchFreelancers: Eintraege ohne contactForbidden=false haben Index != Vielfaches von 7"() {
        given:
        def criteria = new ProfileSearchCriteria(null, null, null, null, null, null)

        when:
        def results = service.searchFreelancers(criteria, 0, 50)

        then:
        def notForbidden = results.findAll { !it.contactForbidden() }
        notForbidden.every { r ->
            def idx = (r.id() - 100L) as int
            idx % 7 != 0
        }
    }

    // ── countSearchFreelancers ───────────────────────────────────────────────

    def "countSearchFreelancers ohne Filter liefert Gesamtanzahl 50"() {
        given:
        def criteria = new ProfileSearchCriteria(null, null, null, null, null, null)

        when:
        def count = service.countSearchFreelancers(criteria)

        then:
        count == 50L
    }

    def "countSearchFreelancers mit salaryPerDayFrom=500 liefert weniger als 50"() {
        given:
        // salary = 400 + i*10; >= 500 means i >= 10 → 40 entries (i=10..49)
        def criteria = new ProfileSearchCriteria(null, 500L, null, null, null, null)

        when:
        def count = service.countSearchFreelancers(criteria)

        then:
        count == 40L
    }

    def "countSearchFreelancers entspricht searchFreelancers-Gesamtmenge vor Pagination"() {
        given:
        def criteria = new ProfileSearchCriteria(null, 600L, null, null, null, null)

        when:
        def total = service.countSearchFreelancers(criteria)
        def page1 = service.searchFreelancers(criteria, 0, 20)
        def page2 = service.searchFreelancers(criteria, 20, 20)

        then:
        // salary = 400 + i*10 >= 600 → i >= 20 → 30 entries
        total == 30L
        page1.size() == 20
        page2.size() == 10
        page1.size() + page2.size() == total
    }

    // ── Sortierung ───────────────────────────────────────────────────────────

    def "searchFreelancers mit sortField=name1 sortDir=asc liefert alphabetisch aufsteigende Namen"() {
        given:
        def criteria = new ProfileSearchCriteria(null, null, null, null, "name1", "asc")

        when:
        def results = service.searchFreelancers(criteria, 0, 20)

        then:
        def names = results.collect { it.name1() }
        names == names.sort(false, String.CASE_INSENSITIVE_ORDER)
    }

    def "searchFreelancers mit sortField=salaryPerDayLong sortDir=desc liefert absteigenden Tagessatz"() {
        given:
        def criteria = new ProfileSearchCriteria(null, null, null, null, "salaryPerDayLong", "desc")

        when:
        def results = service.searchFreelancers(criteria, 0, 20)

        then:
        def salaries = results.collect { it.salaryPerDayLong() }
        salaries == salaries.sort(false).reverse()
    }
}
