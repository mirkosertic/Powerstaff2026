package de.mirkosertic.powerstaff.freelancer

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.freelancer.command.Freelancer
import de.mirkosertic.powerstaff.freelancer.command.FreelancerCommandService
import de.mirkosertic.powerstaff.freelancer.command.FreelancerHistoryEntry
import de.mirkosertic.powerstaff.freelancer.query.FreelancerQueryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class FreelancerHistoryQueryIT extends AbstractContainerBaseIT {

    @Autowired
    FreelancerCommandService commandService

    @Autowired
    FreelancerQueryService queryService

    @Autowired
    JdbcClient jdbcClient

    Long freelancerId
    Long historyTypeId

    def setup() {
        def f = new Freelancer()
        f.name1 = "IT-HQ-Test"
        f.name2 = "Historie"
        f.contactForbidden = false
        f.showAgain = false
        f.datenschutz = false
        freelancerId = commandService.save(f).id

        jdbcClient.sql("INSERT INTO historytype (description) VALUES ('IT-HQ-HistoryTyp')").update()
        historyTypeId = jdbcClient.sql("SELECT id FROM historytype WHERE description = 'IT-HQ-HistoryTyp'")
                .query(Long).single()
    }

    def cleanup() {
        jdbcClient.sql("DELETE FROM freelancer WHERE id = :id").param("id", freelancerId).update()
        jdbcClient.sql("DELETE FROM historytype WHERE description = 'IT-HQ-HistoryTyp'").update()
    }

    def "findHistoryByFreelancerId liefert gespeicherte Historieneintraege"() {
        given:
        def history = [new FreelancerHistoryEntry("ADD", null, historyTypeId, "IT-HQ Ersteintrag")]
        commandService.save(commandService.findById(freelancerId).get(), [], history, [])

        when:
        def result = queryService.findHistoryByFreelancerId(freelancerId)

        then:
        result.size() == 1
        result[0].description() == "IT-HQ Ersteintrag"
        result[0].typeDescription() == "IT-HQ-HistoryTyp"
        result[0].typeId() == historyTypeId
        result[0].freelancerId() == freelancerId
    }

    def "findHistoryByFreelancerId liefert leere Liste ohne Eintraege"() {
        expect:
        queryService.findHistoryByFreelancerId(freelancerId).isEmpty()
    }

    def "findHistoryByFreelancerId sortiert nach creation_date DESC"() {
        given:
        // Insert two entries with explicit creation_date difference using raw SQL
        jdbcClient.sql("""
            INSERT INTO freelancer_history (creation_date, creation_user, changed_date, changed_user,
                description, type_id, freelancer_id)
            VALUES ('2020-01-01 10:00:00', 'system', '2020-01-01 10:00:00', 'system',
                'IT-HQ erster', :typeId, :fId)
        """).param("typeId", historyTypeId).param("fId", freelancerId).update()

        jdbcClient.sql("""
            INSERT INTO freelancer_history (creation_date, creation_user, changed_date, changed_user,
                description, type_id, freelancer_id)
            VALUES ('2022-06-15 10:00:00', 'system', '2022-06-15 10:00:00', 'system',
                'IT-HQ zweiter', :typeId, :fId)
        """).param("typeId", historyTypeId).param("fId", freelancerId).update()

        when:
        def result = queryService.findHistoryByFreelancerId(freelancerId)

        then:
        result.size() == 2
        result[0].description() == "IT-HQ zweiter"    // neuerer kommt zuerst
        result[1].description() == "IT-HQ erster"
    }
}
