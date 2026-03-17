package de.mirkosertic.powerstaff.shared.command

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Subject

/**
 * Integrationstests fuer HistoryTypeRepository und HistoryTypeQueryService.
 *
 * Verwendet @SpringBootTest, da HistoryTypeRepository package-private ist und
 * der vollstaendige Spring-Kontext benoetigt wird, um via StammdatenCommandService
 * auf das Repository zuzugreifen.
 */
@SpringBootTest
class HistoryTypeRepositoryIT extends AbstractContainerBaseIT {

    @Subject
    @Autowired
    StammdatenCommandService commandService

    def cleanup() {
        // Alle Testdaten entfernen – findAll ueber CommandService nicht verfuegbar,
        // daher nutzen wir den QueryService via ApplicationContext.
    }

    def "speichert einen HistoryType und liest ihn anschliessend zurueck"() {
        given: "ein neuer HistoryType"
        def ht = new HistoryType("Telefonat")

        when: "der Typ gespeichert und per ID geladen wird"
        def saved = commandService.saveHistoryType(ht)
        def loaded = commandService.findHistoryTypeById(saved.id)

        then: "der Typ ist vorhanden und description stimmt ueberein"
        loaded.isPresent()
        loaded.get().id == saved.id
        loaded.get().description == "Telefonat"

        cleanup:
        commandService.deleteHistoryType(saved.id)
    }

    def "findHistoryTypeById liefert ein leeres Optional fuer eine unbekannte ID"() {
        when: "eine nicht existierende ID gesucht wird"
        def result = commandService.findHistoryTypeById(-999L)

        then: "das Ergebnis ist ein leeres Optional"
        !result.isPresent()
    }

    def "deleteHistoryType loescht den Eintrag aus der Datenbank"() {
        given: "ein gespeicherter HistoryType"
        def saved = commandService.saveHistoryType(new HistoryType("Zu loeschen"))

        when: "der Typ geloescht wird"
        commandService.deleteHistoryType(saved.id)

        then: "der Typ ist nicht mehr auffindbar"
        !commandService.findHistoryTypeById(saved.id).isPresent()
    }
}
