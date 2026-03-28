package de.mirkosertic.powerstaff.profilesearch.api

import de.mirkosertic.powerstaff.profilesearch.command.LlmService
import de.mirkosertic.powerstaff.profilesearch.command.ProfileSearchCommandService
import de.mirkosertic.powerstaff.profilesearch.command.ProfileSearchProperties
import de.mirkosertic.powerstaff.profilesearch.query.ProfileSearchCriteria
import de.mirkosertic.powerstaff.profilesearch.query.ProfileSearchQueryService
import de.mirkosertic.powerstaff.profilesearch.query.ProfileSearchResult
import de.mirkosertic.powerstaff.project.command.RememberedProjectService
import de.mirkosertic.powerstaff.shared.query.TagQueryService
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.ui.Model
import spock.lang.Specification
import spock.lang.Subject

import java.security.Principal

class ProfileSearchControllerSpec extends Specification {

    ProfileSearchCommandService commandService = Mock()
    ProfileSearchQueryService queryService = Mock()
    LlmService llmService = Mock()
    RememberedProjectService rememberedProjectService = Mock()
    ProfileSearchProperties profileSearchProperties = new ProfileSearchProperties()
    TagQueryService tagQueryService = Mock()

    @Subject
    ProfileSearchController controller = new ProfileSearchController(
            commandService, queryService, llmService, rememberedProjectService, profileSearchProperties, tagQueryService
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

    // ── index ────────────────────────────────────────────────────────────────

    def "GET /profilesearch leitet weiter zu /profilesearch/chat"() {
        when:
        def view = controller.index()

        then:
        view == "redirect:/profilesearch/chat"
    }

    // ── chatIndex ────────────────────────────────────────────────────────────

    def "GET /profilesearch/chat leitet zu vorhandenem Chat weiter wenn einer existiert"() {
        given:
        def response = Mock(HttpServletResponse)
        queryService.findLatestChatByUser("testuser") >> Optional.of(42L)

        when:
        controller.chatIndex(principal, response)

        then:
        1 * response.sendRedirect("/profilesearch/chat/42")
    }

    def "GET /profilesearch/chat erstellt neuen Chat und leitet weiter wenn kein Chat vorhanden"() {
        given:
        def response = Mock(HttpServletResponse)
        queryService.findLatestChatByUser("testuser") >> Optional.empty()
        rememberedProjectService.get("testuser") >> Optional.empty()
        commandService.createChat("testuser", null) >> 99L

        when:
        controller.chatIndex(principal, response)

        then:
        1 * response.sendRedirect("/profilesearch/chat/99")
    }

    def "GET /profilesearch/chat uebergibt gemerktes Projekt beim Erstellen eines neuen Chats"() {
        given:
        def response = Mock(HttpServletResponse)
        queryService.findLatestChatByUser("testuser") >> Optional.empty()
        rememberedProjectService.get("testuser") >> Optional.of(77L)
        commandService.createChat("testuser", 77L) >> 88L

        when:
        controller.chatIndex(principal, response)

        then:
        1 * commandService.createChat("testuser", 77L) >> 88L
        1 * response.sendRedirect("/profilesearch/chat/88")
    }

    // ── search ───────────────────────────────────────────────────────────────

    def "GET /profilesearch/search ohne Kriterien setzt validationError im Model"() {
        given:
        def model = Mock(Model)
        def response = Mock(HttpServletResponse)
        def criteria = ProfileSearchCriteria.empty()
        tagQueryService.findAll() >> []
        rememberedProjectService.getRememberedProjectInfo("testuser") >> Optional.empty()

        when:
        def view = controller.search(criteria, 0, principal, model, response)

        then:
        view == "profilesearch/search-page"
        1 * model.addAttribute("validationError", _ as String)
        1 * model.addAttribute("results", [])
        1 * model.addAttribute("totalCount", 0L)
    }

    def "GET /profilesearch/search mit leerem searchTerm setzt validationError"() {
        given:
        def model = Mock(Model)
        def response = Mock(HttpServletResponse)
        def criteria = ProfileSearchCriteria.empty().withSearchTerm("   ")
        tagQueryService.findAll() >> []
        rememberedProjectService.getRememberedProjectInfo("testuser") >> Optional.empty()

        when:
        def view = controller.search(criteria, 0, principal, model, response)

        then:
        view == "profilesearch/search-page"
        1 * model.addAttribute("validationError", _ as String)
    }

    def "GET /profilesearch/search mit searchTerm ruft queryService auf und befuellt Ergebnisse"() {
        given:
        def model = Mock(Model)
        def response = Mock(HttpServletResponse)
        def criteria = ProfileSearchCriteria.empty().withSearchTerm("Java")
        def mockResults = [
                new ProfileSearchResult(100L, "MOCK-100", "Mock Freelancer 0", null, null, 400L, null, false, []),
                new ProfileSearchResult(101L, "MOCK-101", "Mock Freelancer 1", null, null, 410L, null, false, [])
        ]
        queryService.searchFreelancers(criteria, 0, 20) >> mockResults
        queryService.countSearchFreelancers(criteria) >> 2L
        tagQueryService.findAll() >> []
        rememberedProjectService.getRememberedProjectInfo("testuser") >> Optional.empty()

        when:
        def view = controller.search(criteria, 0, principal, model, response)

        then:
        view == "profilesearch/search-page"
        1 * model.addAttribute("results", mockResults)
        1 * model.addAttribute("totalCount", 2L)
        0 * model.addAttribute("validationError", _)
    }

    def "GET /profilesearch/search mit salaryPerDayFrom und offset=0 liefert vollstaendige Suchseite"() {
        given:
        def model = Mock(Model)
        def response = Mock(HttpServletResponse)
        def criteria = ProfileSearchCriteria.empty().withSalaryPerDayFrom(500L)
        queryService.searchFreelancers(criteria, 0, 20) >> []
        queryService.countSearchFreelancers(criteria) >> 0L
        tagQueryService.findAll() >> []
        rememberedProjectService.getRememberedProjectInfo("testuser") >> Optional.empty()

        when:
        def view = controller.search(criteria, 0, principal, model, response)

        then:
        view == "profilesearch/search-page"
        0 * model.addAttribute("validationError", _)
    }

    def "GET /profilesearch/search mit offset > 0 liefert nur Fragment"() {
        given:
        def model = Mock(Model)
        def response = Mock(HttpServletResponse)
        def criteria = ProfileSearchCriteria.empty().withSearchTerm("Java")
        queryService.searchFreelancers(criteria, 20, 20) >> []
        queryService.countSearchFreelancers(criteria) >> 15L
        rememberedProjectService.getRememberedProjectInfo(_) >> Optional.empty()

        when:
        def view = controller.search(criteria, 20, principal, model, response)

        then:
        view == "profilesearch/search-results :: results"
        1 * model.addAttribute("results", _)
    }

    def "GET /profilesearch/search setzt X-Next-Url Header wenn weitere Ergebnisse vorhanden"() {
        given:
        def model = Mock(Model)
        def response = Mock(HttpServletResponse)
        def results = (1..20).collect { i ->
            new ProfileSearchResult((long) i, "MOCK-$i", "Freelancer $i", null, null, 400L + i * 10L, null, false, [])
        }
        def criteria = ProfileSearchCriteria.empty().withSearchTerm("Mock")
        queryService.searchFreelancers(criteria, 0, 20) >> results
        queryService.countSearchFreelancers(criteria) >> 50L
        tagQueryService.findAll() >> []
        rememberedProjectService.getRememberedProjectInfo("testuser") >> Optional.empty()

        when:
        controller.search(criteria, 0, principal, model, response)

        then:
        // nextUrl must be set in model (not null) when results.size() == PAGE_SIZE
        1 * model.addAttribute("nextUrl", { it != null })
    }
}
