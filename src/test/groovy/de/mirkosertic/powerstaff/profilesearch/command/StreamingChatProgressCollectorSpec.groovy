package de.mirkosertic.powerstaff.profilesearch.command

import de.mirkosertic.powerstaff.profilesearch.command.LlmService.ChatStreamEvent
import spock.lang.Specification

class StreamingChatProgressCollectorSpec extends Specification {

    def "thinkingToken emittiert ThinkingToken-Event UND akkumuliert assistantThoughts"() {
        given:
        def events = []
        def collector = new SpringAILlmService.StreamingChatProgressCollector({ e -> events << e })

        when:
        collector.thinkingToken("Lass ")
        collector.thinkingToken("mich überlegen")

        then:
        events.size() == 2
        events[0] instanceof ChatStreamEvent.ThinkingToken
        events[0].token() == "Lass "
        events[1] instanceof ChatStreamEvent.ThinkingToken
        events[1].token() == "mich überlegen"
        collector.getAssistantThoughtsAndReset() == "Lass mich überlegen"
    }

    def "getAssistantThoughtsAndReset leert den Buffer nach dem Aufruf"() {
        given:
        def collector = new SpringAILlmService.StreamingChatProgressCollector({ e -> })

        when:
        collector.thinkingToken("denke...")
        def first = collector.getAssistantThoughtsAndReset()
        def second = collector.getAssistantThoughtsAndReset()

        then:
        first == "denke..."
        second == ""
    }

    def "assistantResponseToken emittiert ContentToken"() {
        given:
        def events = []
        def collector = new SpringAILlmService.StreamingChatProgressCollector({ e -> events << e })

        when:
        collector.assistantResponseToken("Hallo ")
        collector.assistantResponseToken("Welt")

        then:
        events.size() == 2
        events[0] instanceof ChatStreamEvent.ContentToken
        events[0].token() == "Hallo "
        events[1] instanceof ChatStreamEvent.ContentToken
        events[1].token() == "Welt"
    }

    def "toolInvocation emittiert ToolCall-Event"() {
        given:
        def events = []
        def collector = new SpringAILlmService.StreamingChatProgressCollector({ e -> events << e })

        when:
        collector.toolInvocation("searchFreelancers", '[{"name":"searchFreelancers"}]')

        then:
        events.size() == 1
        events[0] instanceof ChatStreamEvent.ToolCall
        events[0].name() == "searchFreelancers"
        events[0].jsonPayload() == '[{"name":"searchFreelancers"}]'
    }

    def "toolResponses emittiert ToolResult-Event"() {
        given:
        def events = []
        def collector = new SpringAILlmService.StreamingChatProgressCollector({ e -> events << e })

        when:
        collector.toolResponses('["searchFreelancers"]', '[{"id":"1","responseData":"..."}]')

        then:
        events.size() == 1
        events[0] instanceof ChatStreamEvent.ToolResult
        events[0].names() == '["searchFreelancers"]'
        events[0].jsonPayload() == '[{"id":"1","responseData":"..."}]'
    }

    def "thinkingToken akkumuliert nicht in assistantThoughts wenn nur ContentTokens gesendet werden"() {
        given:
        def collector = new SpringAILlmService.StreamingChatProgressCollector({ e -> })

        when:
        collector.assistantResponseToken("Text ohne Thinking")

        then:
        collector.getAssistantThoughtsAndReset() == ""
    }

    def "reportUsage erzeugt kein Event (Tokens kommen spaeter via MessageComplete)"() {
        given:
        def events = []
        def collector = new SpringAILlmService.StreamingChatProgressCollector({ e -> events << e })

        when:
        collector.reportUsage(100, 50, 150)

        then:
        events.isEmpty()
    }
}
