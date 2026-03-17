package de.mirkosertic.powerstaff.shared.command

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Subject

/**
 * Integrationstests fuer TagRepository via StammdatenCommandService.
 */
@SpringBootTest
class TagRepositoryIT extends AbstractContainerBaseIT {

    @Subject
    @Autowired
    StammdatenCommandService commandService

    def "speichert einen Tag und liest ihn zurueck"() {
        given: "ein neuer Tag"
        def tag = new Tag("Java", "SCHWERPUNKT")

        when: "der Tag gespeichert und per ID geladen wird"
        def saved = commandService.saveTag(tag)
        def loaded = commandService.findTagById(saved.id)

        then: "der Tag ist vorhanden und alle Felder stimmen ueberein"
        loaded.isPresent()
        loaded.get().id == saved.id
        loaded.get().tagname == "Java"
        loaded.get().type == "SCHWERPUNKT"

        cleanup:
        commandService.deleteTag(saved.id)
    }

    def "loescht einen Tag"() {
        given: "ein gespeicherter Tag"
        def saved = commandService.saveTag(new Tag("ZuLoeschen", "FUNKTION"))

        when: "der Tag geloescht wird"
        commandService.deleteTag(saved.id)

        then: "der Tag ist nicht mehr auffindbar"
        !commandService.findTagById(saved.id).isPresent()
    }

    def "findTagById liefert leeres Optional fuer unbekannte ID"() {
        when: "eine nicht existierende ID gesucht wird"
        def result = commandService.findTagById(-999L)

        then: "das Ergebnis ist ein leeres Optional"
        !result.isPresent()
    }

    def "aktualisiert den tagname eines Tags"() {
        given: "ein gespeicherter Tag"
        def saved = commandService.saveTag(new Tag("OriginalName", "TYP"))

        when: "der tagname geaendert und gespeichert wird"
        saved.setTagname("NeuerName")
        commandService.saveTag(saved)
        def loaded = commandService.findTagById(saved.id)

        then: "der geaenderte tagname ist gespeichert"
        loaded.isPresent()
        loaded.get().tagname == "NeuerName"

        cleanup:
        commandService.deleteTag(saved.id)
    }
}
