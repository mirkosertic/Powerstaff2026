package de.mirkosertic.powerstaff.freelancer

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.freelancer.command.Freelancer
import de.mirkosertic.powerstaff.freelancer.command.FreelancerCommandService
import de.mirkosertic.powerstaff.freelancer.command.FreelancerContactEntry
import de.mirkosertic.powerstaff.freelancer.query.FreelancerQueryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class FreelancerContactQueryIT extends AbstractContainerBaseIT {

    @Autowired
    FreelancerCommandService commandService

    @Autowired
    FreelancerQueryService queryService

    @Autowired
    JdbcClient jdbcClient

    Long freelancerId

    def setup() {
        def f = new Freelancer()
        f.name1 = "IT-CQ-Test"
        f.name2 = "Kontakt"
        f.contactForbidden = false
        f.showAgain = false
        f.datenschutz = false
        freelancerId = commandService.save(f).id
    }

    def cleanup() {
        jdbcClient.sql("DELETE FROM freelancer WHERE id = :id").param("id", freelancerId).update()
    }

    def "findContactsByFreelancerId liefert gespeicherte Kontakte"() {
        given:
        def contacts = [
                new FreelancerContactEntry(null, "EMAIL", "it-cq@example.com"),
                new FreelancerContactEntry(null, "PHONE", "+49 000 1234")
        ]
        commandService.save(commandService.findById(freelancerId).get(), contacts, [])

        when:
        def result = queryService.findContactsByFreelancerId(freelancerId)

        then:
        result.size() == 2
        result.any { it.type() == "EMAIL" && it.value() == "it-cq@example.com" }
        result.any { it.type() == "PHONE" && it.value() == "+49 000 1234" }
    }

    def "findContactsByFreelancerId liefert leere Liste ohne Kontakte"() {
        expect:
        queryService.findContactsByFreelancerId(freelancerId).isEmpty()
    }

    def "findContactsByFreelancerId sortiert nach type ASC dann value ASC"() {
        given:
        def contacts = [
                new FreelancerContactEntry(null, "PHONE", "222"),
                new FreelancerContactEntry(null, "EMAIL", "bbb@example.com"),
                new FreelancerContactEntry(null, "EMAIL", "aaa@example.com")
        ]
        commandService.save(commandService.findById(freelancerId).get(), contacts, [])

        when:
        def result = queryService.findContactsByFreelancerId(freelancerId)

        then:
        result.size() == 3
        result[0].type() == "EMAIL" && result[0].value() == "aaa@example.com"
        result[1].type() == "EMAIL" && result[1].value() == "bbb@example.com"
        result[2].type() == "PHONE"
    }
}
