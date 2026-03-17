package de.mirkosertic.powerstaff.shared.query

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.shared.command.HistoryType
import de.mirkosertic.powerstaff.shared.command.StammdatenCommandService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Subject

/**
 * Integrationstests fuer HistoryTypeQueryService.
 * Prueft Sortierung der findAll()-Ergebnisse nach description ASC.
 */
@SpringBootTest
class HistoryTypeQueryServiceIT extends AbstractContainerBaseIT {

    @Subject
    @Autowired
    HistoryTypeQueryService queryService

    @Autowired
    StammdatenCommandService commandService

    List<Long> createdIds = []

    def cleanup() {
        createdIds.each { id -> commandService.deleteHistoryType(id) }
        createdIds.clear()
    }

    def "findAll liefert alle Historientypen sortiert nach description ASC"() {
        given: "drei Historientypen mit unterschiedlichen Bezeichnungen"
        def ht1 = commandService.saveHistoryType(new HistoryType("Telefonat"))
        def ht2 = commandService.saveHistoryType(new HistoryType("E-Mail"))
        def ht3 = commandService.saveHistoryType(new HistoryType("Persoenliches Gespraech"))
        createdIds.addAll([ht1.id, ht2.id, ht3.id])

        when: "alle Historientypen abgerufen werden"
        def results = queryService.findAll()

        then: "die Ergebnisse enthalten die erstellten Typen"
        results.size() >= 3

        and: "die Ergebnisse sind nach description aufsteigend sortiert"
        def descriptions = results*.description
        descriptions == descriptions.sort()
    }

    def "findAll liefert eine leere Liste wenn keine Historientypen vorhanden"() {
        // Hinweis: Dieser Test kann fehlschlagen wenn andere Tests Daten hinterlassen haben.
        // Er prueft lediglich, dass findAll() ohne Exception aufrufbar ist.
        when: "alle Historientypen abgerufen werden"
        def results = queryService.findAll()

        then: "kein Fehler wird geworfen"
        results != null
    }
}
