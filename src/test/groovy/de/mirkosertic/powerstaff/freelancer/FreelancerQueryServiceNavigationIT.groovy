package de.mirkosertic.powerstaff.freelancer

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.freelancer.command.Freelancer
import de.mirkosertic.powerstaff.freelancer.command.FreelancerCommandService
import de.mirkosertic.powerstaff.freelancer.query.FreelancerQueryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class FreelancerQueryServiceNavigationIT extends AbstractContainerBaseIT {

    @Autowired
    FreelancerCommandService commandService

    @Autowired
    FreelancerQueryService queryService

    @Autowired
    JdbcClient jdbcClient

    List<Long> savedIds = []

    def setup() {
        def f1 = commandService.save(newFreelancer("IT-Nav-Alpha"))
        def f2 = commandService.save(newFreelancer("IT-Nav-Beta"))
        def f3 = commandService.save(newFreelancer("IT-Nav-Gamma"))
        savedIds = [f1.id, f2.id, f3.id].sort()
    }

    def cleanup() {
        jdbcClient.sql("DELETE FROM freelancer WHERE name1 LIKE 'IT-Nav%'").update()
    }

    def "findById liefert den korrekten Freiberufler"() {
        expect:
        queryService.findById(savedIds[0]).get().id() == savedIds[0]
    }

    def "findFirst liefert den Freiberufler mit der kleinsten ID"() {
        when:
        def result = queryService.findFirst()

        then:
        result.isPresent()
        result.get().id() <= savedIds[0]
    }

    def "findLast liefert den Freiberufler mit der groessten ID"() {
        when:
        def result = queryService.findLast()

        then:
        result.isPresent()
        result.get().id() >= savedIds[2]
    }

    def "findPrevious liefert den Vorgaenger"() {
        when:
        def result = queryService.findPrevious(savedIds[1])

        then:
        result.isPresent()
        result.get().id() == savedIds[0]
    }

    def "findNext liefert den Nachfolger"() {
        when:
        def result = queryService.findNext(savedIds[1])

        then:
        result.isPresent()
        result.get().id() == savedIds[2]
    }

    def "findPrevious vom ersten Datensatz liefert empty"() {
        expect:
        queryService.findPrevious(savedIds[0]).isEmpty()
    }

    def "findNext vom letzten Datensatz liefert empty"() {
        expect:
        queryService.findNext(savedIds[2]).isEmpty()
    }

    def "findById mit unbekannter ID liefert empty"() {
        expect:
        queryService.findById(-999L).isEmpty()
    }

    private static Freelancer newFreelancer(String name1) {
        def f = new Freelancer()
        f.name1 = name1
        f.name2 = "Test"
        f.contactForbidden = false
        f.showAgain = false
        f.datenschutz = false
        f
    }
}
