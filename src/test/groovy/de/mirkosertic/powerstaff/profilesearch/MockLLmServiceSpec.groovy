package de.mirkosertic.powerstaff.profilesearch

import de.mirkosertic.powerstaff.profilesearch.command.LlmService
import de.mirkosertic.powerstaff.profilesearch.command.MockLLmService
import de.mirkosertic.powerstaff.profilesearch.query.LlmProjectContext
import spock.lang.Specification

class MockLLmServiceSpec extends Specification {

    def "sendMessage gibt immer eine Reply mit Rolle assistant zurueck"() {
        given:
        def service = new MockLLmService()

        when:
        def result = service.sendMessage(null, "session-1", "conv-1", Optional.empty(), "Irgendeine Frage")

        then:
        result != null
        result.role() == LlmService.ROLE_SASSISTANT
        result.message() == "Mock Response Nummer 0"
    }

    def "sendMessage gibt denselben Text auch mit Projektkontext zurueck"() {
        given:
        def service = new MockLLmService()
        def context = new LlmProjectContext("PRJ-001", "Testprojekt", null, null, null, null, null, null, null, [])

        when:
        def result = service.sendMessage(null, "session-2", "conv-2", Optional.of(context), "Frage mit Kontext")

        then:
        result != null
        result.role() == LlmService.ROLE_SASSISTANT
        result.message() == "Mock Response Nummer 0"
    }

    def "sendMessage ist unabhaengig von sessionId und conversationId"() {
        given:
        def service = new MockLLmService()

        when:
        def r1 = service.sendMessage(null, "s1", "c1", Optional.empty(), "Frage A")
        def r2 = service.sendMessage(null, "s2", "c2", Optional.empty(), "Frage B")

        then:
        r1.message() == r2.message()
        r1.role() == r2.role()
    }

    def "Reply enthaelt eine id groesser gleich null"() {
        given:
        def service = new MockLLmService()

        when:
        def result = service.sendMessage(null, "session-3", "conv-3", Optional.empty(), "Beliebige Frage")

        then:
        result.id() >= 0
    }
}
