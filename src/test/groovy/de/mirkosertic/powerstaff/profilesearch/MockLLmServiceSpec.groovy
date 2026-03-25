package de.mirkosertic.powerstaff.profilesearch

import de.mirkosertic.powerstaff.profilesearch.command.LlmService
import de.mirkosertic.powerstaff.profilesearch.command.MockLLmService
import de.mirkosertic.powerstaff.profilesearch.query.LlmProjectContext
import spock.lang.Specification

class MockLLmServiceSpec extends Specification {

    def "sendMessage gibt eine Liste mit genau einer Reply zurueck"() {
        given:
        def service = new MockLLmService()

        when:
        def result = service.sendMessage(null, "session-1", "conv-1", Optional.empty(), "Irgendeine Frage")

        then:
        result != null
        result.size() == 1
    }

    def "die einzige Reply hat Rolle assistant und den erwarteten Text"() {
        given:
        def service = new MockLLmService()

        when:
        def reply = service.sendMessage(null, "session-1", "conv-1", Optional.empty(), "Irgendeine Frage")[0]

        then:
        reply.role() == LlmService.ROLE_ASSISTANT
        reply.message() == "Mock Response Nummer 0"
    }

    def "sendMessage gibt denselben Text auch mit Projektkontext zurueck"() {
        given:
        def service = new MockLLmService()
        def context = new LlmProjectContext("PRJ-001", "Testprojekt", null, null, null, null, null, null, null, [])

        when:
        def reply = service.sendMessage(null, "session-2", "conv-2", Optional.of(context), "Frage mit Kontext")[0]

        then:
        reply.role() == LlmService.ROLE_ASSISTANT
        reply.message() == "Mock Response Nummer 0"
    }

    def "sendMessage ist unabhaengig von sessionId und conversationId"() {
        given:
        def service = new MockLLmService()

        when:
        def r1 = service.sendMessage(null, "s1", "c1", Optional.empty(), "Frage A")[0]
        def r2 = service.sendMessage(null, "s2", "c2", Optional.empty(), "Frage B")[0]

        then:
        r1.message() == r2.message()
        r1.role() == r2.role()
    }

    def "Reply enthaelt eine id groesser gleich null"() {
        given:
        def service = new MockLLmService()

        when:
        def reply = service.sendMessage(null, "session-3", "conv-3", Optional.empty(), "Beliebige Frage")[0]

        then:
        reply.id() >= 0
    }
}
