package de.mirkosertic.powerstaff.profilesearch.api

import de.mirkosertic.powerstaff.profilesearch.command.LlmService
import de.mirkosertic.powerstaff.profilesearch.command.ProfileSearchCommandService
import de.mirkosertic.powerstaff.profilesearch.command.ProfileSearchProperties
import de.mirkosertic.powerstaff.profilesearch.query.ProfileSearchQueryService
import de.mirkosertic.powerstaff.project.command.RememberedProjectService
import jakarta.servlet.http.HttpSession
import spock.lang.Specification
import spock.lang.Subject

import java.security.Principal

class ProfileSearchControllerSpec extends Specification {

    ProfileSearchCommandService commandService = Mock()
    ProfileSearchQueryService queryService = Mock()
    LlmService llmService = Mock()
    RememberedProjectService rememberedProjectService = Mock()
    ProfileSearchProperties profileSearchProperties = new ProfileSearchProperties()

    @Subject
    ProfileSearchController controller = new ProfileSearchController(
            commandService, queryService, llmService, rememberedProjectService, profileSearchProperties
    )

    Principal principal = Mock() {
        getName() >> "testuser"
    }
    HttpSession session = Mock() {
        getId() >> "test-session-id"
    }

    def "sendMessage gibt promptTokens aus Reply zurueck"() {
        given:
        queryService.buildLlmContext("testuser") >> Optional.empty()
        llmService.sendMessage(principal, "test-session-id", "1", Optional.empty(), "test") >>
                [new LlmService.Reply(1L, LlmService.ROLE_ASSISTANT, "Antwort", null, 150, 50)]

        when:
        def result = controller.sendMessage(1L, new ProfileSearchController.SendRequest("test"), principal, session)

        then:
        result.promptTokens() == 150
        result.completionTokens() == 50
    }

    def "sendMessage gibt maxContextTokens aus ProfileSearchProperties zurueck"() {
        given:
        queryService.buildLlmContext("testuser") >> Optional.empty()
        llmService.sendMessage(principal, "test-session-id", "1", Optional.empty(), "test") >>
                [new LlmService.Reply(1L, LlmService.ROLE_ASSISTANT, "Antwort", null, 150, 50)]

        when:
        def result = controller.sendMessage(1L, new ProfileSearchController.SendRequest("test"), principal, session)

        then:
        result.maxContextTokens() == 128000
    }

    def "sendMessage gibt messages-Liste mit allen Replies zurueck"() {
        given:
        queryService.buildLlmContext("testuser") >> Optional.empty()
        llmService.sendMessage(principal, "test-session-id", "1", Optional.empty(), "test") >>
                [new LlmService.Reply(1L, LlmService.ROLE_ASSISTANT, "KI-Antwort", "{}", 150, 50)]

        when:
        def result = controller.sendMessage(1L, new ProfileSearchController.SendRequest("test"), principal, session)

        then:
        result.messages().size() == 1
        result.messages()[0].role() == "assistant"
        result.messages()[0].content() == "KI-Antwort"
        result.messages()[0].jsonPayload() == "{}"
    }

    def "sendMessage liefert promptTokens=0 und completionTokens=0 wenn Reply keine Token-Infos hat"() {
        given:
        queryService.buildLlmContext("testuser") >> Optional.empty()
        llmService.sendMessage(principal, "test-session-id", "1", Optional.empty(), "test") >>
                [new LlmService.Reply(1L, LlmService.ROLE_ASSISTANT, "Antwort ohne Token-Info", null, null, null)]

        when:
        def result = controller.sendMessage(1L, new ProfileSearchController.SendRequest("test"), principal, session)

        then:
        result.promptTokens() == 0
        result.completionTokens() == 0
    }

    def "sendMessage nutzt Token-Werte aus letzter Reply mit gesetzten Tokens"() {
        given:
        queryService.buildLlmContext("testuser") >> Optional.empty()
        llmService.sendMessage(principal, "test-session-id", "1", Optional.empty(), "test") >>
                [
                        new LlmService.Reply(1L, LlmService.ROLE_TOOL_CALL, "tool-call", "{}", null, null),
                        new LlmService.Reply(2L, LlmService.ROLE_TOOL_RESULT, "tool-result", "{}", null, null),
                        new LlmService.Reply(3L, LlmService.ROLE_ASSISTANT, "Finale Antwort", null, 200, 75)
                ]

        when:
        def result = controller.sendMessage(1L, new ProfileSearchController.SendRequest("test"), principal, session)

        then:
        result.promptTokens() == 200
        result.completionTokens() == 75
        result.messages().size() == 3
    }
}
