package de.mirkosertic.powerstaff.shared

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.shared.command.StammdatenCommandService
import de.mirkosertic.powerstaff.shared.command.Tag
import de.mirkosertic.powerstaff.shared.query.TagQueryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class TagRepositoryIT extends AbstractContainerBaseIT {

    @Autowired
    StammdatenCommandService commandService

    @Autowired
    TagQueryService queryService

    @Autowired
    JdbcClient jdbcClient

    def cleanup() {
        jdbcClient.sql("DELETE FROM tags WHERE tagname LIKE 'IT-Test%'").update()
    }

    def "Tag speichern und per QueryService lesen"() {
        given:
        def tag = commandService.saveTag(newTag("IT-Test Java", "SCHWERPUNKT"))

        when:
        def all = queryService.findAll()

        then:
        all.any { it.id() == tag.id && it.tagname() == "IT-Test Java" }
    }

    def "findAll liefert alphabetische Sortierung"() {
        given:
        commandService.saveTag(newTag("IT-Test Zulu", "FUNKTION"))
        commandService.saveTag(newTag("IT-Test Alpha", "FUNKTION"))

        when:
        def itTags = queryService.findAll().findAll { it.tagname().startsWith("IT-Test") }

        then:
        itTags.size() >= 2
        itTags.collect { it.tagname() } == itTags.collect { it.tagname() }.sort()
    }

    def "findByType filtert korrekt nach Typ"() {
        given:
        commandService.saveTag(newTag("IT-Test Schwerpunkt-Tag", "SCHWERPUNKT"))
        commandService.saveTag(newTag("IT-Test Funktion-Tag", "FUNKTION"))

        when:
        def schwerpunkte = queryService.findByType(TagType.SCHWERPUNKT).findAll { it.tagname().startsWith("IT-Test") }
        def funktionen = queryService.findByType(TagType.FUNKTION).findAll { it.tagname().startsWith("IT-Test") }

        then:
        schwerpunkte.every { it.type() == "SCHWERPUNKT" }
        funktionen.every { it.type() == "FUNKTION" }
        !schwerpunkte.any { it.tagname() == "IT-Test Funktion-Tag" }
    }

    def "findByType liefert alle 5 TagType-Varianten korrekt"() {
        given:
        TagType.values().each { type ->
            commandService.saveTag(newTag("IT-Test ${type.name()}", type.name()))
        }

        when: "jeden Typ einzeln abfragen"
        def results = TagType.values().collectEntries { type ->
            [(type): queryService.findByType(type).findAll { it.tagname().startsWith("IT-Test") }]
        }

        then:
        results.every { type, tags -> tags.any { it.type() == type.name() } }
    }

    def "Tag loeschen entfernt den Eintrag"() {
        given:
        def tag = commandService.saveTag(newTag("IT-Test Loeschen", "TYP"))

        when:
        commandService.deleteTag(tag.id)

        then:
        commandService.findTagById(tag.id).isEmpty()
    }

    def "Tag update aendert den Namen"() {
        given:
        def tag = commandService.saveTag(newTag("IT-Test Original", "BEMERKUNG"))

        when:
        def loaded = commandService.findTagById(tag.id).orElseThrow()
        loaded.setTagname("IT-Test Geaendert")
        commandService.saveTag(loaded)

        then:
        commandService.findTagById(tag.id).get().tagname == "IT-Test Geaendert"
    }

    private static Tag newTag(String tagname, String type) {
        new Tag(tagname, type)
    }
}
