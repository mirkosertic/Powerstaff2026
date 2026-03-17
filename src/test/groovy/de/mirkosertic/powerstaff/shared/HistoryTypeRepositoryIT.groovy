package de.mirkosertic.powerstaff.shared

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.shared.command.StammdatenCommandService
import de.mirkosertic.powerstaff.shared.query.HistoryTypeQueryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
class HistoryTypeRepositoryIT extends AbstractContainerBaseIT {

    @Autowired
    StammdatenCommandService commandService

    @Autowired
    HistoryTypeQueryService queryService

    @Autowired
    JdbcClient jdbcClient

    def cleanup() {
        jdbcClient.sql("DELETE FROM historytype WHERE description LIKE 'IT-Test%'").update()
    }

    def "HistoryType speichern und per QueryService lesen"() {
        given:
        def ht = commandService.saveHistoryType(newHistoryType("IT-Test Telefonat"))

        when:
        def all = queryService.findAll()

        then:
        all.any { it.id() == ht.id && it.description() == "IT-Test Telefonat" }
    }

    def "findAll liefert Eintraege alphabetisch sortiert"() {
        given:
        commandService.saveHistoryType(newHistoryType("IT-Test Zulu"))
        commandService.saveHistoryType(newHistoryType("IT-Test Alpha"))

        when:
        def itEntries = queryService.findAll().findAll { it.description().startsWith("IT-Test") }

        then:
        itEntries.size() >= 2
        itEntries.collect { it.description() } == itEntries.collect { it.description() }.sort()
    }

    def "findHistoryTypeById liefert Optional.empty fuer unbekannte ID"() {
        expect:
        commandService.findHistoryTypeById(-1L).isEmpty()
    }

    def "HistoryType update aendert die Bezeichnung"() {
        given:
        def ht = commandService.saveHistoryType(newHistoryType("IT-Test Original"))

        when:
        def loaded = commandService.findHistoryTypeById(ht.id).orElseThrow()
        loaded.setDescription("IT-Test Geaendert")
        commandService.saveHistoryType(loaded)

        then:
        commandService.findHistoryTypeById(ht.id).get().description == "IT-Test Geaendert"
    }

    def "HistoryType loeschen entfernt den Eintrag"() {
        given:
        def ht = commandService.saveHistoryType(newHistoryType("IT-Test Loeschen"))

        when:
        commandService.deleteHistoryType(ht.id)

        then:
        commandService.findHistoryTypeById(ht.id).isEmpty()
    }

    private static de.mirkosertic.powerstaff.shared.command.HistoryType newHistoryType(String description) {
        def ht = new de.mirkosertic.powerstaff.shared.command.HistoryType()
        ht.description = description
        ht
    }
}
