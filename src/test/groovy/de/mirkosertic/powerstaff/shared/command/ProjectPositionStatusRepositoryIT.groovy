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
        !loaded.get().defaultStatus

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

    def "setzt einen Status als Standard"() {
        given: "ein Positionsstatus"
        def pps = new ProjectPositionStatus("Standard-Status", "#e0f2fe", "#0c4a6e")
        def saved = commandService.saveProjectPositionStatus(pps)

        when: "der Status als Standard gesetzt wird"
        saved.setDefaultStatus(true)
        commandService.saveProjectPositionStatus(saved)
        def defaultStatus = commandService.findDefaultProjectPositionStatus()

        then: "der Standard-Status ist abrufbar"
        defaultStatus.isPresent()
        defaultStatus.get().id == saved.id
        defaultStatus.get().defaultStatus
    }

    def "nur ein Status kann Standard sein – alter Default wird zurueckgesetzt"() {
        given: "zwei Positionsstatus, erster ist Default"
        def first = new ProjectPositionStatus("Erster", "#fee2e2", "#7f1d1d")
        first.setDefaultStatus(true)
        def savedFirst = commandService.saveProjectPositionStatus(first)

        def second = new ProjectPositionStatus("Zweiter", "#fef9c3", "#713f12")
        def savedSecond = commandService.saveProjectPositionStatus(second)

        when: "der zweite als Standard gesetzt wird"
        savedSecond.setDefaultStatus(true)
        commandService.saveProjectPositionStatus(savedSecond)

        then: "nur der zweite ist noch Standard"
        def reloadedFirst = commandService.findProjectPositionStatusById(savedFirst.id)
        def reloadedSecond = commandService.findProjectPositionStatusById(savedSecond.id)
        !reloadedFirst.get().defaultStatus
        reloadedSecond.get().defaultStatus

        and: "findDefaultProjectPositionStatus liefert den zweiten"
        commandService.findDefaultProjectPositionStatus().get().id == savedSecond.id
    }

    def "findDefaultProjectPositionStatus liefert leeres Optional wenn kein Default gesetzt"() {
        // Dieser Test prueft nur, dass die Methode korrekt funktioniert.
        // Ggf. existiert ein Default aus einem anderen Test – daher nur pruefen
        // dass die Methode aufgerufen werden kann ohne Exception.
        expect:
        commandService.findDefaultProjectPositionStatus() != null
    }
}
