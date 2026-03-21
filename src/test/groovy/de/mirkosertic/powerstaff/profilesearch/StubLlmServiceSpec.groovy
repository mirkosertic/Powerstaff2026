package de.mirkosertic.powerstaff.profilesearch

import de.mirkosertic.powerstaff.profilesearch.query.LlmProjectContext
import spock.lang.Specification

class StubLlmServiceSpec extends Specification {

    def "sendMessage gibt den Stub-Text zurueck unabhaengig von Parametern"() {
        given:
        def service = new StubLlmService()

        when:
        def result = service.sendMessage(Optional.empty(), [], "Irgendeine Frage")

        then:
        result == "Die KI-Profilsuche ist in Release 1.0 noch nicht aktiviert."
    }

    def "sendMessage gibt denselben Text auch mit Kontext und History zurueck"() {
        given:
        def service = new StubLlmService()
        def context = new LlmProjectContext("PRJ-001", "Testprojekt", null, null, null, null, null, null, null, [])

        when:
        def result = service.sendMessage(Optional.of(context), [], "Frage mit Kontext")

        then:
        result == "Die KI-Profilsuche ist in Release 1.0 noch nicht aktiviert."
    }
}
