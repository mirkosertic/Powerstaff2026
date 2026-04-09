package de.mirkosertic.powerstaff.profilesearch.command

import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.metadata.ChatGenerationMetadata
import org.springframework.ai.chat.metadata.ChatResponseMetadata
import org.springframework.ai.chat.metadata.DefaultUsage
import org.springframework.ai.chat.metadata.EmptyUsage
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.Generation
import spock.lang.Specification
import tools.jackson.databind.ObjectMapper

class SpringAILlmServiceTokenRoutingSpec extends Specification {

    // routeTokenToCollector() greift nicht auf ChatClient, CommandService etc. zu —
    // wir übergeben null für ungenutzte Dependencies, um das JDK-25/Mockito-
    // Byte-Buddy-Problem zu umgehen.
    SpringAILlmService service = new SpringAILlmService(null, null, null, null, new ObjectMapper(), null)

    def "normaler Text-Token wird als assistantResponseToken weitergeleitet"() {
        given:
        def receivedTokens = []
        def collector = [assistantResponseToken: { t -> receivedTokens << t }] as ChatProgressCollector
        def response = buildChatResponse("Hallo Welt", null, null)

        when:
        service.routeTokenToCollector(response, collector)

        then:
        receivedTokens == ["Hallo Welt"]
    }

    def "reasoningContent-Metadata wird als thinkingToken weitergeleitet"() {
        given:
        def receivedThinking = []
        def collector = [thinkingToken: { t -> receivedThinking << t }] as ChatProgressCollector
        def response = buildChatResponse(null, "Lass mich überlegen", null)

        when:
        service.routeTokenToCollector(response, collector)

        then:
        receivedThinking == ["Lass mich überlegen"]
    }

    def "STOP-FinishReason ruft stopped() auf"() {
        given:
        def stoppedCalled = false
        def collector = [stopped: { -> stoppedCalled = true }] as ChatProgressCollector
        def response = buildChatResponse("Fertig", null, "STOP")

        when:
        service.routeTokenToCollector(response, collector)

        then:
        stoppedCalled
    }

    def "kein stopped()-Aufruf ohne STOP-FinishReason"() {
        given:
        def stoppedCalled = false
        def collector = [stopped: { -> stoppedCalled = true }] as ChatProgressCollector
        def response = buildChatResponse("Token", null, null)

        when:
        service.routeTokenToCollector(response, collector)

        then:
        !stoppedCalled
    }

    def "Usage-Daten werden an reportUsage weitergeleitet wenn nicht EmptyUsage"() {
        given:
        def reportedArgs = null
        def collector = [reportUsage: { pt, ct, tt -> reportedArgs = [pt, ct, tt] }] as ChatProgressCollector
        def usage = new DefaultUsage(100, 50)
        def metadata = ChatResponseMetadata.builder().usage(usage).build()
        def response = buildChatResponseWithMetadata("Text", null, null, metadata)

        when:
        service.routeTokenToCollector(response, collector)

        then:
        reportedArgs == [100, 50, 150]
    }

    def "EmptyUsage wird nicht an reportUsage weitergeleitet"() {
        given:
        def reportUsageCalled = false
        def collector = [reportUsage: { pt, ct, tt -> reportUsageCalled = true }] as ChatProgressCollector
        def metadata = ChatResponseMetadata.builder().usage(new EmptyUsage()).build()
        def response = buildChatResponseWithMetadata("Text", null, null, metadata)

        when:
        service.routeTokenToCollector(response, collector)

        then:
        !reportUsageCalled
    }

    def "Text null ohne reasoningContent erzeugt keine Callbacks"() {
        given:
        def callCount = 0
        def collector = [
            thinkingToken: { t -> callCount++ },
            assistantResponseToken: { t -> callCount++ }
        ] as ChatProgressCollector
        def response = buildChatResponse(null, null, null)

        when:
        service.routeTokenToCollector(response, collector)

        then:
        callCount == 0
    }

    // ── Hilfsmethoden ──────────────────────────────────────────────────────────

    private static ChatResponse buildChatResponse(String text, String reasoningContent, String finishReason) {
        def metadata = ChatResponseMetadata.builder().usage(new EmptyUsage()).build()
        return buildChatResponseWithMetadata(text, reasoningContent, finishReason, metadata)
    }

    private static ChatResponse buildChatResponseWithMetadata(String text, String reasoningContent,
                                                               String finishReason, ChatResponseMetadata metadata) {
        def msgMetadata = reasoningContent != null ? Map.of("reasoningContent", reasoningContent) : Map.of()
        def assistantMessage = AssistantMessage.builder().content(text).properties(msgMetadata).build()
        def genMetadata = finishReason != null
                ? ChatGenerationMetadata.builder().finishReason(finishReason).build()
                : ChatGenerationMetadata.NULL
        def generation = new Generation(assistantMessage, genMetadata)
        return new ChatResponse([generation], metadata)
    }
}
