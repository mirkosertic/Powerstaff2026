package de.mirkosertic.powerstaff.profilesearch.api

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.profilesearch.command.LlmService
import de.mirkosertic.powerstaff.profilesearch.command.ProfileSearchCommandService
import de.mirkosertic.powerstaff.profilesearch.query.ChatListView
import de.mirkosertic.powerstaff.profilesearch.query.ProfileSearchQueryService
import de.mirkosertic.powerstaff.profilesearch.query.MessageView
import de.mirkosertic.powerstaff.project.command.RememberedProjectService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import java.time.LocalDateTime

import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.anyInt
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.when
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import static org.hamcrest.Matchers.containsString
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ProfileSearchControllerIT extends AbstractContainerBaseIT {

    @Autowired
    WebApplicationContext wac

    MockMvc mockMvc

    @MockitoBean
    ProfileSearchCommandService commandService

    @MockitoBean
    ProfileSearchQueryService queryService

    @MockitoBean
    LlmService llmService

    @MockitoBean
    RememberedProjectService rememberedProjectService

    def setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build()
    }

    def "GET /profilesearch ohne Auth liefert Redirect zu /login"() {
        expect:
        mockMvc.perform(get("/profilesearch"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
    }

    def "GET /profilesearch mit vorhandenem Chat redirectet zu chat"() {
        given:
        when(queryService.findLatestChatByUser("testuser")).thenReturn(Optional.of(42L))

        expect:
        mockMvc.perform(get("/profilesearch").with(user("testuser")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profilesearch/chat/42"))
    }

    def "GET /profilesearch ohne Chat legt neuen Chat an und redirectet"() {
        given:
        when(queryService.findLatestChatByUser("testuser")).thenReturn(Optional.empty())
        when(rememberedProjectService.get("testuser")).thenReturn(Optional.empty())
        when(commandService.createChat("testuser", null)).thenReturn(99L)

        expect:
        mockMvc.perform(get("/profilesearch").with(user("testuser")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profilesearch/chat/99"))
    }

    def "GET /profilesearch/chat/{chatId} rendert form-Template"() {
        given:
        def chatView = new ChatListView(42L, LocalDateTime.now(), "testuser", LocalDateTime.now(), "Titel", null, null)
        when(queryService.findChatsByUser(anyString(), anyInt(), anyInt())).thenReturn([chatView])
        when(queryService.countChatsByUser(anyString())).thenReturn(1L)
        when(queryService.findMessagesByChat(42L)).thenReturn([])

        expect:
        mockMvc.perform(get("/profilesearch/chat/42").with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(view().name("profilesearch/form"))
    }

    def "POST /profilesearch/chat/new legt Chat an und redirectet"() {
        given:
        when(rememberedProjectService.get("testuser")).thenReturn(Optional.empty())
        when(commandService.createChat("testuser", null)).thenReturn(77L)

        expect:
        mockMvc.perform(post("/profilesearch/chat/new").with(user("testuser")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profilesearch/chat/77"))
    }

    def "DELETE /profilesearch/chat/{chatId} loescht Chat und liefert JSON mit redirectTo"() {
        given:
        when(queryService.findLatestChatByUser("testuser")).thenReturn(Optional.of(55L))

        expect:
        mockMvc.perform(delete("/profilesearch/chat/42").with(user("testuser")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.redirectTo').value('/profilesearch/chat/55'))
    }

    def "POST /profilesearch/chat/{chatId}/send ruft LLM auf und liefert JSON"() {
        given:
        def reply = new LlmService.Reply(201L, "assistant", "Die KI-Profilsuche ist in Release 1.0 noch nicht aktiviert.", null)

        when(queryService.buildLlmContext("testuser")).thenReturn(Optional.empty())
        when(llmService.sendMessage(any(), anyString(), anyString(), any(), anyString()))
                .thenReturn(reply)

        expect:
        mockMvc.perform(post("/profilesearch/chat/42/send")
                .with(user("testuser")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"message":"Hallo"}'))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.id').value(201))
                .andExpect(jsonPath('$.role').value("assistant"))
                .andExpect(jsonPath('$.content').value("Die KI-Profilsuche ist in Release 1.0 noch nicht aktiviert."))
    }

    def "GET /profilesearch/chat/{chatId} mit offset > 0 liefert Sidebar-Fragment"() {
        given:
        def chatView = new ChatListView(42L, LocalDateTime.now(), "testuser", LocalDateTime.now(), "Titel", null, null)
        when(queryService.findChatsByUser(anyString(), anyInt(), anyInt())).thenReturn([chatView])
        when(queryService.countChatsByUser(anyString())).thenReturn(1L)
        when(queryService.findMessagesByChat(42L)).thenReturn([])

        expect:
        mockMvc.perform(get("/profilesearch/chat/42?offset=20").with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(view().name("profilesearch/sidebar-entry"))
    }

    def "GET /profilesearch/chat/{chatId} mit offset=0 setzt X-Next-Url Header wenn mehr Daten vorhanden"() {
        given:
        def chatView = new ChatListView(42L, LocalDateTime.now(), "testuser", LocalDateTime.now(), "Titel", null, null)
        when(queryService.findChatsByUser(anyString(), anyInt(), anyInt())).thenReturn([chatView])
        when(queryService.countChatsByUser(anyString())).thenReturn(50L)
        when(queryService.findMessagesByChat(42L)).thenReturn([])

        expect:
        mockMvc.perform(get("/profilesearch/chat/42").with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(view().name("profilesearch/form"))
                .andExpect(header().string("X-Next-Url", "/profilesearch/chat/42?offset=20"))
    }

    def "GET /profilesearch/chat/{chatId} rendert chat-page HTML-Struktur korrekt"() {
        given:
        def chatView = new ChatListView(42L, LocalDateTime.now(), "testuser", LocalDateTime.now(), "KI-Suche Test", null, null)
        when(queryService.findChatsByUser(anyString(), anyInt(), anyInt())).thenReturn([chatView])
        when(queryService.countChatsByUser(anyString())).thenReturn(1L)
        when(queryService.findMessagesByChat(42L)).thenReturn([
                new MessageView(1L, LocalDateTime.now(), 42L, "user", 1, "Hallo KI", null),
                new MessageView(2L, LocalDateTime.now(), 42L, "assistant", 2, "Antwort der KI", null)
        ])

        expect:
        mockMvc.perform(get("/profilesearch/chat/42").with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(view().name("profilesearch/form"))
                .andExpect(content().string(containsString('id="chat-page"')))
                .andExpect(content().string(containsString('id="chat-sidebar"')))
                .andExpect(content().string(containsString('id="chat-messages"')))
                .andExpect(content().string(containsString('id="chat-input-area"')))
                .andExpect(content().string(containsString('KI-Suche Test')))
                .andExpect(content().string(containsString('Hallo KI')))
    }

    def "POST /profilesearch/chat/{chatId}/send liefert role tool_call in der JSON-Response"() {
        given:
        def reply = new LlmService.Reply(303L, LlmService.ROLE_TOOL_CALL, '{"tool":"search","args":{}}', null)

        when(queryService.buildLlmContext("testuser")).thenReturn(Optional.empty())
        when(llmService.sendMessage(any(), anyString(), anyString(), any(), anyString()))
                .thenReturn(reply)

        expect:
        mockMvc.perform(post("/profilesearch/chat/42/send")
                .with(user("testuser")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"message":"Suche Freelancer"}'))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.id').value(303))
                .andExpect(jsonPath('$.role').value(LlmService.ROLE_TOOL_CALL))
                .andExpect(jsonPath('$.content').value('{"tool":"search","args":{}}'))
    }

    def "POST /profilesearch/chat/{chatId}/send liefert role tool_result in der JSON-Response"() {
        given:
        def reply = new LlmService.Reply(404L, LlmService.ROLE_TOOL_RESULT, '{"freelancers":[]}', null)

        when(queryService.buildLlmContext("testuser")).thenReturn(Optional.empty())
        when(llmService.sendMessage(any(), anyString(), anyString(), any(), anyString()))
                .thenReturn(reply)

        expect:
        mockMvc.perform(post("/profilesearch/chat/42/send")
                .with(user("testuser")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"message":"Zeig Ergebnis"}'))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.id').value(404))
                .andExpect(jsonPath('$.role').value(LlmService.ROLE_TOOL_RESULT))
                .andExpect(jsonPath('$.content').value('{"freelancers":[]}'))
    }

    def "POST /profilesearch/chat/{chatId}/send Response-JSON enthaelt immer Felder id, role und content"() {
        given:
        def reply = new LlmService.Reply(555L, LlmService.ROLE_SASSISTANT, "Fertig.", null)

        when(queryService.buildLlmContext("testuser")).thenReturn(Optional.empty())
        when(llmService.sendMessage(any(), anyString(), anyString(), any(), anyString()))
                .thenReturn(reply)

        when:
        def result = mockMvc.perform(post("/profilesearch/chat/42/send")
                .with(user("testuser")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"message":"Test"}'))

        then:
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$.id').exists())
        result.andExpect(jsonPath('$.role').exists())
        result.andExpect(jsonPath('$.content').exists())
        result.andExpect(jsonPath('$.id').value(555))
        result.andExpect(jsonPath('$.role').value(LlmService.ROLE_SASSISTANT))
    }
}
