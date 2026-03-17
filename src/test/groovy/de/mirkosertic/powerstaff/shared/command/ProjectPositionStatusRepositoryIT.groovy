package de.mirkosertic.powerstaff.shared.command

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Subject

/**
 * Integrationstests fuer ProjectPositionStatusRepository via StammdatenCommandService.
 */
@SpringBootTest
class ProjectPositionStatusRepositoryIT extends AbstractContainerBaseIT {

    @Subject
    @Autowired
    StammdatenCommandService commandService

    def "speichert einen ProjectPositionStatus und liest ihn zurueck"() {
        given: "ein neuer Positionsstatus"
        def pps = new ProjectPositionStatus("Vorgeschlagen", "#d1fae5", "#065f46")

        when: "der Status gespeichert und per ID geladen wird"
        def saved = commandService.saveProjectPositionStatus(pps)
        def loaded = commandService.findProjectPositionStatusById(saved.id)

        then: "der Status ist vorhanden und alle Felder stimmen ueberein"
        loaded.isPresent()
        loaded.get().id == saved.id
        loaded.get().description == "Vorgeschlagen"
        loaded.get().color == "#d1fae5"
        loaded.get().colorText == "#065f46"

        // Kein delete im CommandService fuer PPS – Daten verbleiben in der DB.
        // Acceptable fuer Integrationstests mit Testcontainer-Isolation.
    }

    def "findProjectPositionStatusById liefert leeres Optional fuer unbekannte ID"() {
        when: "eine nicht existierende ID gesucht wird"
        def result = commandService.findProjectPositionStatusById(-999L)

        then: "das Ergebnis ist ein leeres Optional"
        !result.isPresent()
    }

    def "aktualisiert einen ProjectPositionStatus"() {
        given: "ein gespeicherter Status"
        def pps = new ProjectPositionStatus("Original", "#ffffff", "#000000")
        def saved = commandService.saveProjectPositionStatus(pps)

        when: "der Status aktualisiert wird"
        saved.setDescription("Aktualisiert")
        saved.setColor("#ff0000")
        saved.setColorText("#ffffff")
        def updated = commandService.saveProjectPositionStatus(saved)
        def loaded = commandService.findProjectPositionStatusById(updated.id)

        then: "die aktualisierten Werte sind gespeichert"
        loaded.isPresent()
        loaded.get().description == "Aktualisiert"
        loaded.get().color == "#ff0000"
        loaded.get().colorText == "#ffffff"
    }
}
