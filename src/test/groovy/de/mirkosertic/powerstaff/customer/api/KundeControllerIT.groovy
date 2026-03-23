package de.mirkosertic.powerstaff.customer.api

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.customer.command.Kunde
import de.mirkosertic.powerstaff.customer.command.KundeCommandService
import de.mirkosertic.powerstaff.customer.command.KundeHasProjectsException
import de.mirkosertic.powerstaff.customer.query.KundeContactView
import de.mirkosertic.powerstaff.customer.query.KundeDetailView
import de.mirkosertic.powerstaff.customer.query.KundeHistoryView
import de.mirkosertic.powerstaff.customer.query.KundeProjectListItem
import de.mirkosertic.powerstaff.customer.query.KundeQueryService
import de.mirkosertic.powerstaff.customer.query.KundeSearchResult
import de.mirkosertic.powerstaff.shared.query.HistoryTypeQueryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import jakarta.servlet.http.Cookie
import java.time.LocalDateTime

import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.not
import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.anyInt
import static org.mockito.ArgumentMatchers.anyLong
import static org.mockito.Mockito.doThrow
import static org.mockito.Mockito.when
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class KundeControllerIT extends AbstractContainerBaseIT {

    @Autowired
    WebApplicationContext wac

    MockMvc mockMvc

    @MockitoBean
    KundeCommandService commandService

    @MockitoBean
    KundeQueryService queryService

    @MockitoBean
    HistoryTypeQueryService historyTypeQueryService

    def setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        def testKunde = new Kunde()
        testKunde.id = 42L
        testKunde.dbVersion = 0L
        testKunde.company = "Test GmbH"
        testKunde.contactForbidden = false
        testKunde.showAgain = false

        def detailView = new KundeDetailView(
                42L, 0L,
                LocalDateTime.now(), "system",
                LocalDateTime.now(), "system",
                "Test GmbH", null, null, null, null, null, null,
                false, false, null, null, null
        )

        when(commandService.findById(42L)).thenReturn(Optional.of(testKunde))
        when(commandService.save(any())).thenReturn(testKunde)
        when(commandService.save(any(), any(), any())).thenReturn(testKunde)

        when(queryService.findFirst()).thenReturn(Optional.of(detailView))
        when(queryService.findLast()).thenReturn(Optional.of(detailView))
        when(queryService.findPrevious(anyLong())).thenReturn(Optional.empty())
        when(queryService.findNext(anyLong())).thenReturn(Optional.empty())
        when(queryService.findById(42L)).thenReturn(Optional.of(detailView))
        when(queryService.findContactsByKundeId(anyLong())).thenReturn([] as List<KundeContactView>)
        when(queryService.findHistoryByKundeId(anyLong())).thenReturn([] as List<KundeHistoryView>)
        when(queryService.findProjectsByKundeId(anyLong(), any(), any())).thenReturn([] as List<KundeProjectListItem>)
        when(queryService.search(any(), anyInt(), anyInt())).thenReturn([
                new KundeSearchResult(1L, "Test GmbH", null, null, "Berlin")
        ])
        when(queryService.countSearch(any())).thenReturn(1L)

        when(historyTypeQueryService.findAll()).thenReturn([])
    }

    def "GET /kunde ohne Cookie leitet auf ersten Kunden weiter (302)"() {
        when:
        def result = mockMvc.perform(get("/kunde").with(user("testuser")))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "GET /kunde mit lastKundeId-Cookie leitet auf diesen Kunden weiter (302)"() {
        when:
        def result = mockMvc.perform(
                get("/kunde")
                        .with(user("testuser"))
                        .cookie(new Cookie("lastKundeId", "42")))

        then:
        result.andExpect(status().is3xxRedirection())
              .andExpect(header().string("Location", "/kunde/42"))
    }

    def "GET /kunde/{id} liefert HTTP 200, rendert Template und setzt lastKundeId-Cookie"() {
        when:
        def result = mockMvc.perform(get("/kunde/42").with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(view().name("kunde/form"))
              .andExpect(cookie().value("lastKundeId", "42"))
    }

    def "GET /kunde/new liefert HTTP 200, rendert Template und loescht lastKundeId-Cookie"() {
        when:
        def result = mockMvc.perform(get("/kunde/new").with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(view().name("kunde/form"))
              .andExpect(cookie().maxAge("lastKundeId", 0))
    }

    def "GET /kunde/first leitet auf ersten Kunden weiter (302)"() {
        when:
        def result = mockMvc.perform(get("/kunde/first").with(user("testuser")))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "GET /kunde/last leitet auf letzten Kunden weiter (302)"() {
        when:
        def result = mockMvc.perform(get("/kunde/last").with(user("testuser")))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "GET /kunde/previous/{id} leitet weiter (302)"() {
        when:
        def result = mockMvc.perform(get("/kunde/previous/42").with(user("testuser")))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "GET /kunde/next/{id} leitet weiter (302)"() {
        when:
        def result = mockMvc.perform(get("/kunde/next/42").with(user("testuser")))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "POST /kunde/save leitet auf Kunden-Formular weiter (302)"() {
        when:
        def result = mockMvc.perform(
                post("/kunde/save")
                        .with(csrf())
                        .with(user("testuser"))
                        .param("id", "42")
                        .param("dbVersion", "0")
                        .param("company", "Test GmbH")
                        .param("contactsJson", "[]")
                        .param("historyJson", "[]"))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "POST /kunde/save bei Optimistic-Locking-Konflikt liefert 409 JSON"() {
        given:
        when(commandService.save(any(), any(), any())).thenThrow(new OptimisticLockingFailureException("conflict"))

        when:
        def result = mockMvc.perform(
                post("/kunde/save")
                        .with(csrf())
                        .with(user("testuser"))
                        .param("id", "42")
                        .param("dbVersion", "0")
                        .param("company", "Test GmbH")
                        .param("contactsJson", "[]")
                        .param("historyJson", "[]"))

        then:
        result.andExpect(status().isConflict())
              .andExpect(jsonPath('$.conflict').value(true))
    }

    def "POST /kunde/save mit ungueltigem JSON liefert 400 mit error-Feld"() {
        when:
        def result = mockMvc.perform(
                post("/kunde/save")
                        .with(csrf())
                        .with(user("testuser"))
                        .param("id", "42")
                        .param("dbVersion", "0")
                        .param("contactsJson", "NOT_VALID_JSON")
                        .param("historyJson", "[]"))

        then:
        result.andExpect(status().isBadRequest())
              .andExpect(jsonPath('$.error').value("invalid json"))
    }

    def "POST /kunde/delete/{id} leitet auf /kunde/new weiter (302)"() {
        when:
        def result = mockMvc.perform(
                post("/kunde/delete/42")
                        .with(csrf())
                        .with(user("testuser")))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "POST /kunde/delete/{id} bei KundeHasProjectsException liefert 409 JSON"() {
        given:
        doThrow(new KundeHasProjectsException([100L, 200L])).when(commandService).deleteById(42L)

        when:
        def result = mockMvc.perform(
                post("/kunde/delete/42")
                        .with(csrf())
                        .with(user("testuser")))

        then:
        result.andExpect(status().isConflict())
              .andExpect(jsonPath('$.blocked').value(true))
    }

    def "GET /kunde/search mit company-Parameter liefert search-page (200)"() {
        when:
        def result = mockMvc.perform(
                get("/kunde/search")
                        .with(user("testuser"))
                        .param("company", "Test"))

        then:
        result.andExpect(status().isOk())
              .andExpect(view().name("kunde/search-page"))
    }

    def "GET /kunde/search mit offset gibt Fragment zurueck (200)"() {
        when:
        def result = mockMvc.perform(
                get("/kunde/search")
                        .with(user("testuser"))
                        .param("offset", "20"))

        then:
        result.andExpect(status().isOk())
    }

    def "GET /kunde/search ohne Parameter liefert 200 und search-page Template"() {
        when:
        def result = mockMvc.perform(
                get("/kunde/search")
                        .with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(view().name("kunde/search-page"))
    }

    def "GET /kunde/search setzt Cache-Control Header no-store"() {
        when:
        def result = mockMvc.perform(
                get("/kunde/search")
                        .with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(header().string("Cache-Control", containsString("no-store")))
    }

    def "GET /kunde/search mit company-Parameter liefert 200 und kein Exception"() {
        when:
        def result = mockMvc.perform(
                get("/kunde/search")
                        .param("company", "Test")
                        .param("sortField", "company")
                        .param("sortDir", "asc")
                        .with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(header().string("Cache-Control", containsString("no-store")))
              .andExpect(content().string(not(containsString('Exception'))))
    }

    def "GET /kunde/search rendert HTML-Seite ohne Exception"() {
        when:
        def result = mockMvc.perform(
                get("/kunde/search")
                        .with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(content().string(not(containsString('Exception'))))
              .andExpect(content().string(not(containsString('Whitelabel Error'))))
    }

    def "GET /kunde/search mit offset-Parameter gibt Fragment zurueck (200)"() {
        when:
        def result = mockMvc.perform(
                get("/kunde/search")
                        .param("offset", "20")
                        .param("company", "Test")
                        .with(user("testuser")))

        then:
        result.andExpect(status().isOk())
    }

    def "GET /kunde/search mit offset gibt X-Next-Url Header zurueck (200)"() {
        given:
        when(queryService.countSearch(any())).thenReturn(100L)

        when:
        def result = mockMvc.perform(
                get("/kunde/search")
                        .param("offset", "20")
                        .param("company", "Test")
                        .with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(header().exists("X-Next-Url"))
    }

    def "GET /kunde/search ohne offset gibt kein X-Next-Url Header (200)"() {
        when:
        def result = mockMvc.perform(
                get("/kunde/search")
                        .param("company", "Test")
                        .with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(header().doesNotExist("X-Next-Url"))
    }

    // -------------------------------------------------------------------------
    // Thymeleaf-Rendering HTML-Inhalte pruefen
    // -------------------------------------------------------------------------

    def "GET /kunde/{id} rendert HTML mit Formular ohne Exception"() {
        when:
        def result = mockMvc.perform(get("/kunde/42").with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(content().string(containsString('<form')))
              .andExpect(content().string(not(containsString('Exception'))))
              .andExpect(content().string(not(containsString('Whitelabel Error'))))
    }

    def "GET /kunde/new rendert HTML mit leerem Formular ohne Exception"() {
        when:
        def result = mockMvc.perform(get("/kunde/new").with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(content().string(containsString('<form')))
              .andExpect(content().string(not(containsString('Exception'))))
    }

    def "GET /kunde/search rendert HTML-Seite mit Treffern ohne Exception"() {
        when:
        def result = mockMvc.perform(
                get("/kunde/search")
                        .with(user("testuser"))
                        .param("company", "Test"))

        then:
        result.andExpect(status().isOk())
              .andExpect(content().string(not(containsString('Exception'))))
    }
}
