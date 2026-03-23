package de.mirkosertic.powerstaff.customer

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.customer.command.Kunde
import de.mirkosertic.powerstaff.customer.command.KundeCommandService
import de.mirkosertic.powerstaff.customer.command.KundeContactEntry
import de.mirkosertic.powerstaff.customer.command.KundeHasProjectsException
import de.mirkosertic.powerstaff.customer.command.KundeHistoryEntry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class KundeCommandServiceIT extends AbstractContainerBaseIT {

    @Autowired
    KundeCommandService commandService

    @Autowired
    JdbcClient jdbcClient

    Long historyTypeId

    def setup() {
        jdbcClient.sql("INSERT INTO historytype (description) VALUES ('IT-Cmd-HistTyp')").update()
        historyTypeId = jdbcClient.sql("SELECT id FROM historytype WHERE description = 'IT-Cmd-HistTyp'")
                .query(Long.class).single()
    }

    def cleanup() {
        jdbcClient.sql("DELETE FROM kunde_history WHERE kunde_id IN (SELECT id FROM kunde WHERE company LIKE 'IT-Cmd%')").update()
        jdbcClient.sql("DELETE FROM kunde_contact WHERE kunde_id IN (SELECT id FROM kunde WHERE company LIKE 'IT-Cmd%')").update()
        jdbcClient.sql("DELETE FROM kunde WHERE company LIKE 'IT-Cmd%'").update()
        jdbcClient.sql("DELETE FROM historytype WHERE description = 'IT-Cmd-HistTyp'").update()
    }

    def "save aktualisiert bestehenden Kunden"() {
        given:
        def kunde = commandService.save(newKunde("IT-Cmd Update"))

        when:
        kunde.setCity("Berlin")
        commandService.save(kunde)

        then:
        commandService.findById(kunde.id).get().city == "Berlin"
    }

    def "delete ohne Projektzuordnung loescht den Kunden"() {
        given:
        def kunde = commandService.save(newKunde("IT-Cmd Delete"))

        when:
        commandService.deleteById(kunde.id)

        then:
        commandService.findById(kunde.id).isEmpty()
    }

    def "delete mit Projektzuordnung wirft KundeHasProjectsException"() {
        given:
        def kunde = commandService.save(newKunde("IT-Cmd Restrict"))

        jdbcClient.sql("""
            INSERT INTO project (db_version, project_number, customer_id)
            VALUES (0, 'IT-CMD-KPRO-001', :kundeId)
        """).param("kundeId", kunde.id).update()
        def projectId = jdbcClient.sql("SELECT id FROM project WHERE project_number = 'IT-CMD-KPRO-001'")
                .query(Long).single()

        when:
        commandService.deleteById(kunde.id)

        then:
        def ex = thrown(KundeHasProjectsException)
        ex.projectIds.contains(projectId)

        cleanup:
        jdbcClient.sql("DELETE FROM project WHERE project_number = 'IT-CMD-KPRO-001'").update()
    }

    def "Unified Save speichert Kontakte und Historie via Delta-Commands"() {
        given:
        def kunde = newKunde("IT-Cmd Unified")
        def contacts = [new KundeContactEntry("ADD", null, "EMAIL", "test@example.com")]
        def history = [new KundeHistoryEntry("ADD", null, historyTypeId, "Erstanlage")]

        when:
        def saved = commandService.save(kunde, contacts, history)

        then:
        saved.id != null

        and:
        def contactRows = jdbcClient.sql("SELECT COUNT(*) FROM kunde_contact WHERE kunde_id = :id")
                .param("id", saved.id).query(Integer).single()
        contactRows == 1

        and:
        def historyRows = jdbcClient.sql("SELECT COUNT(*) FROM kunde_history WHERE kunde_id = :id")
                .param("id", saved.id).query(Integer).single()
        historyRows == 1
    }

    def "save loescht Kontakt per DELETE-Delta-Command"() {
        given:
        def kunde = commandService.save(newKunde("IT-Cmd Delete-Contact"),
                [new KundeContactEntry("ADD", null, "EMAIL", "alt@example.com")], [])
        def contactId = jdbcClient.sql("SELECT id FROM kunde_contact WHERE kunde_id = :id")
                .param("id", kunde.id).query(Long.class).single()

        when:
        commandService.save(kunde, [new KundeContactEntry("DELETE", contactId, null, null)], [])

        then:
        jdbcClient.sql("SELECT COUNT(*) FROM kunde_contact WHERE kunde_id = :id")
                .param("id", kunde.id).query(Long.class).single() == 0
    }

    def "DELETE Kontakt ohne ID wirft IllegalArgumentException"() {
        given:
        def kunde = commandService.save(newKunde("IT-Cmd Delete-Contact-NoId"))

        when:
        commandService.save(kunde, [new KundeContactEntry("DELETE", null, null, null)], [])

        then:
        thrown(IllegalArgumentException)
    }

    def "save aktualisiert Kontakthistorieneintrag per UPDATE-Delta-Command"() {
        given:
        def kunde = commandService.save(newKunde("IT-Cmd Update-History"), [],
                [new KundeHistoryEntry("ADD", null, historyTypeId, "Original")])
        def historyId = jdbcClient.sql("SELECT id FROM kunde_history WHERE kunde_id = :id")
                .param("id", kunde.id).query(Long.class).single()

        when:
        commandService.save(kunde, [],
                [new KundeHistoryEntry("UPDATE", historyId, historyTypeId, "Geaendert")])

        then:
        jdbcClient.sql("SELECT description FROM kunde_history WHERE id = :id")
                .param("id", historyId).query(String.class).single() == "Geaendert"
    }

    def "save loescht Kontakthistorieneintrag per DELETE-Delta-Command"() {
        given:
        def kunde = commandService.save(newKunde("IT-Cmd Delete-History"), [],
                [new KundeHistoryEntry("ADD", null, historyTypeId, "Zu loeschen")])
        def historyId = jdbcClient.sql("SELECT id FROM kunde_history WHERE kunde_id = :id")
                .param("id", kunde.id).query(Long.class).single()

        when:
        commandService.save(kunde, [],
                [new KundeHistoryEntry("DELETE", historyId, null, null)])

        then:
        jdbcClient.sql("SELECT COUNT(*) FROM kunde_history WHERE kunde_id = :id")
                .param("id", kunde.id).query(Long.class).single() == 0
    }

    def "UPDATE Kontakthistorie ohne ID wirft IllegalArgumentException"() {
        given:
        def kunde = commandService.save(newKunde("IT-Cmd Update-History-NoId"))

        when:
        commandService.save(kunde, [],
                [new KundeHistoryEntry("UPDATE", null, historyTypeId, "Geaendert")])

        then:
        thrown(IllegalArgumentException)
    }

    def "DELETE Kontakthistorie ohne ID wirft IllegalArgumentException"() {
        given:
        def kunde = commandService.save(newKunde("IT-Cmd Delete-History-NoId"))

        when:
        commandService.save(kunde, [],
                [new KundeHistoryEntry("DELETE", null, null, null)])

        then:
        thrown(IllegalArgumentException)
    }

    private static Kunde newKunde(String company) {
        def k = new Kunde()
        k.company = company
        k.contactForbidden = false
        k.showAgain = false
        k
    }
}
