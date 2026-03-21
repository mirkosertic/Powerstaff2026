package de.mirkosertic.powerstaff.customer

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.customer.command.Kunde
import de.mirkosertic.powerstaff.customer.command.KundeCommandService
import de.mirkosertic.powerstaff.customer.command.KundeHistoryEntry
import de.mirkosertic.powerstaff.customer.query.KundeQueryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class KundeHistoryQueryIT extends AbstractContainerBaseIT {

    @Autowired
    KundeCommandService commandService

    @Autowired
    KundeQueryService queryService

    @Autowired
    JdbcClient jdbcClient

    Long historyTypeId

    def setup() {
        jdbcClient.sql("INSERT INTO historytype (description) VALUES ('IT-KHQ-Typ')").update()
        historyTypeId = jdbcClient.sql("SELECT id FROM historytype WHERE description = 'IT-KHQ-Typ'")
                .query(Long.class).single()
    }

    def cleanup() {
        jdbcClient.sql("DELETE FROM kunde_history WHERE kunde_id IN (SELECT id FROM kunde WHERE company LIKE 'IT-KHQ%')").update()
        jdbcClient.sql("DELETE FROM kunde WHERE company LIKE 'IT-KHQ%'").update()
        jdbcClient.sql("DELETE FROM historytype WHERE description = 'IT-KHQ-Typ'").update()
    }

    def "Historieneintrag anlegen und per findHistoryByKundeId lesen"() {
        given:
        def kunde = commandService.save(newKunde("IT-KHQ History GmbH"),
                [],
                [new KundeHistoryEntry("ADD", null, historyTypeId, "Erstanlage")])

        when:
        def history = queryService.findHistoryByKundeId(kunde.id)

        then:
        history.size() == 1
        history[0].description() == "Erstanlage"
        history[0].typeId() == historyTypeId
        history[0].typeDescription() == "IT-KHQ-Typ"
        history[0].kundeId() == kunde.id
    }

    def "findHistoryByKundeId fuer Kunden ohne Eintraege liefert leere Liste"() {
        given:
        def kunde = commandService.save(newKunde("IT-KHQ Empty GmbH"), [], [])

        expect:
        queryService.findHistoryByKundeId(kunde.id).isEmpty()
    }

    def "Eintraege werden absteigend nach creation_date sortiert"() {
        given:
        def kunde = commandService.save(newKunde("IT-KHQ Sort GmbH"), [], [])
        jdbcClient.sql("""
            INSERT INTO kunde_history (creation_date, creation_user, changed_date, changed_user, description, type_id, kunde_id)
            VALUES ('2024-01-01 10:00:00', 'system', '2024-01-01 10:00:00', 'system', 'IT-KHQ aelterer Eintrag', :typeId, :kundeId)
        """).param("typeId", historyTypeId).param("kundeId", kunde.id).update()
        jdbcClient.sql("""
            INSERT INTO kunde_history (creation_date, creation_user, changed_date, changed_user, description, type_id, kunde_id)
            VALUES ('2024-06-01 10:00:00', 'system', '2024-06-01 10:00:00', 'system', 'IT-KHQ neuerer Eintrag', :typeId, :kundeId)
        """).param("typeId", historyTypeId).param("kundeId", kunde.id).update()

        when:
        def history = queryService.findHistoryByKundeId(kunde.id)

        then:
        history.size() == 2
        history[0].description() == "IT-KHQ neuerer Eintrag"
        history[1].description() == "IT-KHQ aelterer Eintrag"
    }

    private static Kunde newKunde(String company) {
        def k = new Kunde()
        k.company = company
        k.contactForbidden = false
        k.showAgain = false
        k
    }
}
