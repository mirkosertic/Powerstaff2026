package de.mirkosertic.powerstaff.shared.query

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.shared.TagType
import de.mirkosertic.powerstaff.shared.command.StammdatenCommandService
import de.mirkosertic.powerstaff.shared.command.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Subject

/**
 * Integrationstests fuer TagQueryService.
 * Prueft findAll(), findByType() – Filterung und Sortierung.
 */
@SpringBootTest
class TagQueryServiceIT extends AbstractContainerBaseIT {

    @Subject
    @Autowired
    TagQueryService queryService

    @Autowired
    StammdatenCommandService commandService

    List<Long> createdIds = []

    def cleanup() {
        createdIds.each { id -> commandService.deleteTag(id) }
        createdIds.clear()
    }

    def "findAll liefert alle Tags sortiert nach tagname ASC"() {
        given: "Tags verschiedener Typen"
        def t1 = commandService.saveTag(new Tag("Zuerich", "EINSATZORT"))
        def t2 = commandService.saveTag(new Tag("Backend", "SCHWERPUNKT"))
        def t3 = commandService.saveTag(new Tag("Architekt", "FUNKTION"))
        createdIds.addAll([t1.id, t2.id, t3.id])

        when: "alle Tags abgerufen werden"
        def results = queryService.findAll()

        then: "die Ergebnisse sind nach tagname aufsteigend sortiert"
        def tagnames = results*.tagname
        tagnames == tagnames.sort()
    }

    def "findByType liefert nur Tags des angegebenen Typs"() {
        given: "Tags mit SCHWERPUNKT und FUNKTION"
        def t1 = commandService.saveTag(new Tag("Java-Schwerpunkt", "SCHWERPUNKT"))
        def t2 = commandService.saveTag(new Tag("Projektleitung", "FUNKTION"))
        def t3 = commandService.saveTag(new Tag("Kotlin-Schwerpunkt", "SCHWERPUNKT"))
        createdIds.addAll([t1.id, t2.id, t3.id])

        when: "nur SCHWERPUNKT-Tags abgerufen werden"
        def results = queryService.findByType(TagType.SCHWERPUNKT)

        then: "nur Tags vom Typ SCHWERPUNKT sind enthalten"
        results.every { it.type == "SCHWERPUNKT" }

        and: "die SCHWERPUNKT-Tags sind nach tagname sortiert"
        def tagnames = results*.tagname
        tagnames == tagnames.sort()

        and: "der FUNKTION-Tag ist nicht im Ergebnis"
        !results.any { it.tagname == "Projektleitung" }
    }

    def "findByType liefert eine leere Liste wenn keine Tags des Typs vorhanden sind"() {
        when: "Tags fuer BEMERKUNG abgerufen werden (kein solcher Tag erstellt)"
        def results = queryService.findByType(TagType.BEMERKUNG)

        then: "kein Fehler wird geworfen"
        results != null

        and: "es werden keine unerwarteten Typen zurueckgegeben"
        results.every { it.type == "BEMERKUNG" }
    }

    def "findByType filtert korrekt nach allen TagType-Werten"() {
        given: "je ein Tag pro TagType"
        def created = TagType.values().collect { tagType ->
            commandService.saveTag(new Tag("TestTag-${tagType.name()}", tagType.name()))
        }
        createdIds.addAll(created*.id)

        when: "fuer jeden TagType gefiltert wird"
        def resultMap = TagType.values().collectEntries { tagType ->
            [(tagType): queryService.findByType(tagType)]
        }

        then: "jeder Typ liefert mindestens den erstellten Tag zurueck"
        TagType.values().every { tagType ->
            resultMap[tagType].any { it.tagname == "TestTag-${tagType.name()}" }
        }
    }
}
