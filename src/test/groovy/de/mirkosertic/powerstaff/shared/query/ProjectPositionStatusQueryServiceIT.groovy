package de.mirkosertic.powerstaff.shared.query

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.shared.command.ProjectPositionStatus
import de.mirkosertic.powerstaff.shared.command.StammdatenCommandService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Subject

/**
 * Integrationstests fuer ProjectPositionStatusQueryService.
 * Prueft Sortierung der findAll()-Ergebnisse nach description ASC.
 */
@SpringBootTest
class ProjectPositionStatusQueryServiceIT extends AbstractContainerBaseIT {

    @Subject
    @Autowired
    ProjectPositionStatusQueryService queryService

    @Autowired
    StammdatenCommandService commandService

    def "findAll liefert alle Positionsstatus-Eintraege sortiert nach description ASC"() {
        given: "drei Positionsstatus-Eintraege mit unterschiedlichen Bezeichnungen"
        commandService.saveProjectPositionStatus(new ProjectPositionStatus("Zuletzt", "#aaa", "#000"))
        commandService.saveProjectPositionStatus(new ProjectPositionStatus("Alpha", "#bbb", "#111"))
        commandService.saveProjectPositionStatus(new ProjectPositionStatus("Mitte", "#ccc", "#222"))

        when: "alle Status abgerufen werden"
        def results = queryService.findAll()

        then: "die Ergebnisse enthalten mindestens die erstellten Status"
        results.size() >= 3

        and: "die Ergebnisse sind nach description aufsteigend sortiert"
        def descriptions = results*.description
        descriptions == descriptions.sort()
    }

    def "findAll liefert Eintraege mit allen Feldern inklusive colorText und isDefault"() {
        given: "ein Positionsstatus mit Farbwerten"
        commandService.saveProjectPositionStatus(
                new ProjectPositionStatus("Farbtest", "#d1fae5", "#065f46"))

        when: "alle Status abgerufen werden"
        def results = queryService.findAll()
        def farbtest = results.find { it.description == "Farbtest" }

        then: "der Farbtest-Eintrag ist enthalten und hat korrekte Farbwerte"
        farbtest != null
        farbtest.color == "#d1fae5"
        farbtest.colorText == "#065f46"
        !farbtest.isDefault()
    }

    def "findAll liefert isDefault=true fuer den gesetzten Standard-Status"() {
        given: "ein Positionsstatus als Standard"
        def pps = new ProjectPositionStatus("Query-Standard", "#e0f2fe", "#0c4a6e")
        pps.setDefaultStatus(true)
        commandService.saveProjectPositionStatus(pps)

        when: "alle Status abgerufen werden"
        def results = queryService.findAll()
        def standard = results.find { it.description == "Query-Standard" }

        then: "der Standard-Eintrag hat isDefault = true"
        standard != null
        standard.isDefault()
    }
}
