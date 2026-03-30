package de.mirkosertic.powerstaff.profilesearch.api

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.profilesearch.command.LlmService
import org.mockito.stubbing.Answer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.web.context.WebApplicationContext

import java.util.function.Consumer

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.doAnswer
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ProfileSearchControllerStreamIT extends AbstractContainerBaseIT {

    @Autowired
    WebApplicationContext wac

    @MockitoBean
    LlmService llmService

    MockMvc mockMvc

    def setup() {
        mockMvc = webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        // Standard-Mock: zwei ContentTokens + MessageComplete
        doAnswer({ invocation ->
            Consumer<LlmService.ChatStreamEvent> sink = invocation.getArgument(5)
            sink.accept(new LlmService.ChatStreamEvent.ContentToken("Hallo "))
            sink.accept(new LlmService.ChatStreamEvent.ContentToken("Welt"))
            sink.accept(new LlmService.ChatStreamEvent.MessageComplete(42L, 100, 30, 8192))
            null
        } as Answer).when(llmService).sendMessageStreaming(any(), any(), any(), any(), any(), any())
    }

    def "POST /stream liefert text/event-stream"() {
        when:
        def asyncResult = performStream('{"message":"Suche Entwickler"}')

        def result = mockMvc.perform(asyncDispatch(asyncResult))

        then:
        result.andExpect(status().isOk())
              .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
    }

    def "POST /stream enthält content_token Events mit Token-Daten"() {
        when:
        def body = streamBody('{"message":"Test"}')

        then:
        body.contains("event:content_token")
        body.contains('"token":"Hallo "')
        body.contains('"token":"Welt"')
    }

    def "POST /stream enthält message_complete Event mit ID und Token-Counts"() {
        when:
        def body = streamBody('{"message":"Test"}')

        then:
        body.contains("event:message_complete")
        body.contains('"id":42')
        body.contains('"promptTokens":100')
        body.contains('"completionTokens":30')
        body.contains('"maxContextTokens":')
    }

    def "POST /stream endet mit done-Event"() {
        when:
        def body = streamBody('{"message":"Test"}')

        then:
        body.contains("event:done")
    }

    def "POST /stream ohne CSRF liefert 403"() {
        when:
        def result = mockMvc.perform(
                post("/profilesearch/chat/1/stream")
                        .with(user("admin"))
                        // kein csrf()
                        .contentType(MediaType.APPLICATION_JSON)
                        .content('{"message":"test"}'))

        then:
        result.andExpect(status().isForbidden())
    }

    def "Events erscheinen in korrekter Reihenfolge: thinking -> tool_call -> tool_result -> content -> complete -> done"() {
        given:
        doAnswer({ invocation ->
            Consumer<LlmService.ChatStreamEvent> sink = invocation.getArgument(5)
            sink.accept(new LlmService.ChatStreamEvent.ThinkingToken("denke..."))
            sink.accept(new LlmService.ChatStreamEvent.ToolCall("searchFreelancers", '[{"name":"searchFreelancers"}]'))
            sink.accept(new LlmService.ChatStreamEvent.ToolResult('["searchFreelancers"]', '[{"id":"1"}]'))
            sink.accept(new LlmService.ChatStreamEvent.ThinkingToken("formuliere..."))
            sink.accept(new LlmService.ChatStreamEvent.ContentToken("Ergebnis"))
            sink.accept(new LlmService.ChatStreamEvent.MessageComplete(99L, 200, 80, 8192))
            null
        } as Answer).when(llmService).sendMessageStreaming(any(), any(), any(), any(), any(), any())

        when:
        def body = streamBody('{"message":"test"}')

        then:
        body.indexOf("event:thinking_token") < body.indexOf("event:tool_call")
        body.indexOf("event:tool_call")       < body.indexOf("event:tool_result")
        body.indexOf("event:tool_result")     < body.indexOf("event:content_token")
        body.indexOf("event:content_token")   < body.indexOf("event:message_complete")
        body.indexOf("event:message_complete") < body.indexOf("event:done")
    }

    def "thinking_token Events transportieren den Token-Text"() {
        given:
        doAnswer({ invocation ->
            Consumer<LlmService.ChatStreamEvent> sink = invocation.getArgument(5)
            sink.accept(new LlmService.ChatStreamEvent.ThinkingToken("Ich denke nach"))
            sink.accept(new LlmService.ChatStreamEvent.MessageComplete(1L, 10, 5, 8192))
            null
        } as Answer).when(llmService).sendMessageStreaming(any(), any(), any(), any(), any(), any())

        when:
        def body = streamBody('{"message":"test"}')

        then:
        body.contains("event:thinking_token")
        body.contains('"token":"Ich denke nach"')
    }

    // ── Hilfsmethoden ──────────────────────────────────────────────────────────

    private MvcResult performStream(String jsonBody) {
        mockMvc.perform(
                post("/profilesearch/chat/1/stream")
                        .with(csrf())
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted())
                .andReturn()
    }

    private String streamBody(String jsonBody) {
        def asyncResult = performStream(jsonBody)
        mockMvc.perform(asyncDispatch(asyncResult))
               .andReturn().response.contentAsString
    }
}
