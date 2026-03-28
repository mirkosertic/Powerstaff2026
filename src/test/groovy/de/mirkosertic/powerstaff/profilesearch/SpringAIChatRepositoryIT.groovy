package de.mirkosertic.powerstaff.profilesearch

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.profilesearch.command.ChatProgressCollector
import de.mirkosertic.powerstaff.profilesearch.command.PersistentAssistantMessage
import de.mirkosertic.powerstaff.profilesearch.command.PersistentToolResponseMessage
import de.mirkosertic.powerstaff.profilesearch.command.PersistentUserMessage
import de.mirkosertic.powerstaff.profilesearch.command.ProfileSearchCommandService
import de.mirkosertic.powerstaff.profilesearch.command.SpringAIChatRepository
import de.mirkosertic.powerstaff.profilesearch.query.ProfileSearchQueryService
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.ToolResponseMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient
import tools.jackson.databind.ObjectMapper

@SpringBootTest
class SpringAIChatRepositoryIT extends AbstractContainerBaseIT {

    @Autowired
    ProfileSearchCommandService commandService

    @Autowired
    ProfileSearchQueryService queryService

    @Autowired
    JdbcClient jdbcClient

    @Autowired
    ObjectMapper objectMapper

    List<Long> chatIds = []

    def cleanup() {
        chatIds.each { id ->
            jdbcClient.sql("DELETE FROM profile_search_message WHERE chat_id = :cid").param("cid", id).update()
            jdbcClient.sql("DELETE FROM profile_search_chat WHERE id = :id").param("id", id).update()
        }
    }

    private SpringAIChatRepository createRepo(Long chatId) {
        new SpringAIChatRepository(String.valueOf(chatId), queryService, commandService, objectMapper, new ChatProgressCollector() {
        })
    }

    def "UserMessage wird persistiert und korrekt als PersistentUserMessage geladen"() {
        given:
        def chatId = commandService.createChat('test-user', null)
        chatIds << chatId
        def repo = createRepo(chatId)
        def original = new UserMessage("Suche einen erfahrenen Java-Entwickler")

        when:
        repo.saveAll(String.valueOf(chatId), [original])
        def loaded = repo.findByConversationId(String.valueOf(chatId))

        then:
        loaded.size() == 1
        loaded[0] instanceof PersistentUserMessage
        loaded[0].getText() == original.getText()
    }

    def "AssistantMessage ohne ToolCalls wird persistiert und korrekt als PersistentAssistantMessage geladen"() {
        given:
        def chatId = commandService.createChat('test-user', null)
        chatIds << chatId
        def repo = createRepo(chatId)
        def original = new AssistantMessage("Ich habe folgende Kandidaten gefunden: ...")

        when:
        repo.saveAll(String.valueOf(chatId), [original])
        def loaded = repo.findByConversationId(String.valueOf(chatId))

        then:
        loaded.size() == 1
        loaded[0] instanceof PersistentAssistantMessage
        loaded[0].getText() == original.getText()
        !(loaded[0] as AssistantMessage).hasToolCalls()
    }

    def "AssistantMessage mit ToolCalls wird persistiert und alle ToolCall-Felder korrekt geladen"() {
        given:
        def chatId = commandService.createChat('test-user', null)
        chatIds << chatId
        def repo = createRepo(chatId)
        def toolCall = new AssistantMessage.ToolCall("call-007", "function", "searchFreelancer", '{"skill":"Java","minYears":5}')
        def original = new AssistantMessage("", Map.of(), [toolCall], List.of())

        when:
        repo.saveAll(String.valueOf(chatId), [original])
        def loaded = repo.findByConversationId(String.valueOf(chatId))

        then:
        loaded.size() == 1
        loaded[0] instanceof PersistentAssistantMessage
        def loadedMsg = loaded[0] as AssistantMessage
        loadedMsg.hasToolCalls()
        loadedMsg.getToolCalls().size() == 1
        def loadedCall = loadedMsg.getToolCalls()[0]
        loadedCall.id()        == toolCall.id()
        loadedCall.type()      == toolCall.type()
        loadedCall.name()      == toolCall.name()
        loadedCall.arguments() == toolCall.arguments()
    }

    def "AssistantMessage mit mehreren ToolCalls wird vollstaendig persistiert und geladen"() {
        given:
        def chatId = commandService.createChat('test-user', null)
        chatIds << chatId
        def repo = createRepo(chatId)
        def tc1 = new AssistantMessage.ToolCall("call-1", "function", "searchFreelancer", '{"skill":"Java"}')
        def tc2 = new AssistantMessage.ToolCall("call-2", "function", "searchFreelancer", '{"skill":"Python"}')
        def original = new AssistantMessage("", Map.of(), [tc1, tc2], List.of())

        when:
        repo.saveAll(String.valueOf(chatId), [original])
        def loaded = repo.findByConversationId(String.valueOf(chatId))

        then:
        loaded.size() == 1
        def loadedMsg = loaded[0] as AssistantMessage
        loadedMsg.getToolCalls().size() == 2

        def calls = loadedMsg.getToolCalls()
        calls.find { it.id() == "call-1" }?.name()      == "searchFreelancer"
        calls.find { it.id() == "call-1" }?.arguments() == '{"skill":"Java"}'
        calls.find { it.id() == "call-2" }?.name()      == "searchFreelancer"
        calls.find { it.id() == "call-2" }?.arguments() == '{"skill":"Python"}'
    }

    def "ToolResponseMessage wird persistiert und alle ToolResponse-Felder korrekt geladen"() {
        given:
        def chatId = commandService.createChat('test-user', null)
        chatIds << chatId
        def repo = createRepo(chatId)
        def toolResponse = new ToolResponseMessage.ToolResponse("call-007", "searchFreelancer", '{"name":"Max Muster","rate":95}')
        def original = new ToolResponseMessage([toolResponse], Map.of())

        when:
        repo.saveAll(String.valueOf(chatId), [original])
        def loaded = repo.findByConversationId(String.valueOf(chatId))

        then:
        loaded.size() == 1
        loaded[0] instanceof PersistentToolResponseMessage
        def loadedMsg = loaded[0] as ToolResponseMessage
        loadedMsg.getResponses().size() == 1
        def loadedResp = loadedMsg.getResponses()[0]
        loadedResp.id()           == toolResponse.id()
        loadedResp.name()         == toolResponse.name()
        loadedResp.responseData() == toolResponse.responseData()
    }

    def "ToolResponseMessage mit mehreren Responses wird vollstaendig persistiert und geladen"() {
        given:
        def chatId = commandService.createChat('test-user', null)
        chatIds << chatId
        def repo = createRepo(chatId)
        def tr1 = new ToolResponseMessage.ToolResponse("call-1", "searchFreelancer", '{"name":"Max Muster"}')
        def tr2 = new ToolResponseMessage.ToolResponse("call-2", "getProjectDetails", '{"title":"Java-Projekt 2026"}')
        def original = new ToolResponseMessage([tr1, tr2], Map.of())

        when:
        repo.saveAll(String.valueOf(chatId), [original])
        def loaded = repo.findByConversationId(String.valueOf(chatId))

        then:
        loaded.size() == 1
        def loadedMsg = loaded[0] as ToolResponseMessage
        loadedMsg.getResponses().size() == 2

        def responses = loadedMsg.getResponses()
        responses.find { it.id() == "call-1" }?.name()         == "searchFreelancer"
        responses.find { it.id() == "call-1" }?.responseData() == '{"name":"Max Muster"}'
        responses.find { it.id() == "call-2" }?.name()         == "getProjectDetails"
        responses.find { it.id() == "call-2" }?.responseData() == '{"title":"Java-Projekt 2026"}'
    }

    def "Vollstaendige Konversation mit allen Message-Typen wird in korrekter Reihenfolge persistiert und geladen"() {
        given:
        def chatId = commandService.createChat('test-user', null)
        chatIds << chatId
        def repo = createRepo(chatId)

        def userMsg       = new UserMessage("Suche Java-Entwickler mit Spring-Erfahrung")
        def toolCall      = new AssistantMessage.ToolCall("call-42", "function", "searchFreelancer", '{"skill":"Java","framework":"Spring"}')
        def assistantTool = new AssistantMessage("", Map.of(), [toolCall], List.of())
        def toolResp      = new ToolResponseMessage.ToolResponse("call-42", "searchFreelancer", '{"name":"Anna Schmidt","rate":110}')
        def toolResult    = new ToolResponseMessage([toolResp], Map.of())
        def assistantFinal = new AssistantMessage("Ich habe Anna Schmidt gefunden. Stundensatz: 110 EUR.")

        when:
        repo.saveAll(String.valueOf(chatId), [userMsg, assistantTool, toolResult, assistantFinal])
        def loaded = repo.findByConversationId(String.valueOf(chatId))

        then:
        loaded.size() == 4

        // Seq 1 – UserMessage
        loaded[0] instanceof PersistentUserMessage
        loaded[0].getText() == "Suche Java-Entwickler mit Spring-Erfahrung"

        // Seq 2 – AssistantMessage mit ToolCall
        loaded[1] instanceof PersistentAssistantMessage
        with(loaded[1] as AssistantMessage) {
            hasToolCalls()
            getToolCalls().size() == 1
            getToolCalls()[0].id()        == "call-42"
            getToolCalls()[0].type()      == "function"
            getToolCalls()[0].name()      == "searchFreelancer"
            getToolCalls()[0].arguments() == '{"skill":"Java","framework":"Spring"}'
        }

        // Seq 3 – ToolResponseMessage
        loaded[2] instanceof PersistentToolResponseMessage
        with(loaded[2] as ToolResponseMessage) {
            getResponses().size() == 1
            getResponses()[0].id()           == "call-42"
            getResponses()[0].name()         == "searchFreelancer"
            getResponses()[0].responseData() == '{"name":"Anna Schmidt","rate":110}'
        }

        // Seq 4 – AssistantMessage ohne ToolCall
        loaded[3] instanceof PersistentAssistantMessage
        loaded[3].getText() == "Ich habe Anna Schmidt gefunden. Stundensatz: 110 EUR."
        !(loaded[3] as AssistantMessage).hasToolCalls()
    }

    def "PersistentMessages werden beim saveAll ignoriert und nicht doppelt gespeichert"() {
        given:
        def chatId = commandService.createChat('test-user', null)
        chatIds << chatId
        def repo = createRepo(chatId)
        repo.saveAll(String.valueOf(chatId), [new UserMessage("Erste Nachricht")])

        when:
        // Geladene PersistentMessages nochmal übergeben – dürfen nicht erneut persistiert werden
        def alreadyLoaded = repo.findByConversationId(String.valueOf(chatId))
        repo.saveAll(String.valueOf(chatId), alreadyLoaded)
        def afterSecondSave = repo.findByConversationId(String.valueOf(chatId))

        then:
        afterSecondSave.size() == 1
        afterSecondSave[0].getText() == "Erste Nachricht"
    }

    def "findByConversationId wirft Exception bei falscher conversationId"() {
        given:
        def chatId = commandService.createChat('test-user', null)
        chatIds << chatId
        def repo = createRepo(chatId)

        when:
        repo.findByConversationId("999999")

        then:
        thrown(IllegalArgumentException)
    }

    def "findConversationIds gibt genau die eine conversationId zurueck"() {
        given:
        def chatId = commandService.createChat('test-user', null)
        chatIds << chatId
        def repo = createRepo(chatId)

        expect:
        repo.findConversationIds() == [String.valueOf(chatId)]
    }
}
