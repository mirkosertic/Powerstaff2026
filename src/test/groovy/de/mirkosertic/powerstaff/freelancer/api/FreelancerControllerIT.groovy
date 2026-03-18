package de.mirkosertic.powerstaff.freelancer.api

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.freelancer.command.DuplicateTagException
import de.mirkosertic.powerstaff.freelancer.command.Freelancer
import de.mirkosertic.powerstaff.freelancer.command.FreelancerCommandService
import de.mirkosertic.powerstaff.freelancer.command.FreelancerHasPositionsException
import de.mirkosertic.powerstaff.freelancer.command.FreelancerTagCommandService
import de.mirkosertic.powerstaff.freelancer.query.FreelancerDetailView
import de.mirkosertic.powerstaff.freelancer.query.FreelancerQueryService
import de.mirkosertic.powerstaff.freelancer.query.FreelancerSearchResult
import de.mirkosertic.powerstaff.shared.query.HistoryTypeQueryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import java.time.LocalDateTime

import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.anyInt
import static org.mockito.ArgumentMatchers.anyLong
import static org.mockito.Mockito.doThrow
import static org.mockito.Mockito.when
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class FreelancerControllerIT extends AbstractContainerBaseIT {

    @Autowired
    WebApplicationContext wac

    MockMvc mockMvc

    @MockitoBean
    FreelancerCommandService commandService

    @MockitoBean
    FreelancerQueryService queryService

    @MockitoBean
    FreelancerTagCommandService tagCommandService

    @MockitoBean
    HistoryTypeQueryService historyTypeQueryService

    def setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        def testFreelancer = new Freelancer()
        testFreelancer.id = 42L
        testFreelancer.dbVersion = 0L
        testFreelancer.name1 = "Mustermann"
        testFreelancer.contactForbidden = false
        testFreelancer.showAgain = false
        testFreelancer.datenschutz = false

        def detailView = new FreelancerDetailView(
                42L, 0L,
                LocalDateTime.now(), "system",
                LocalDateTime.now(), "system",
                null, "Mustermann", "Max", null, null, null, null, null,
                null, null, null,
                false, false, null, null,
                null, null, null, null,
                null, null,
                null, null, null, null, null,
                false, null, null, null, null
        )

        when(commandService.findById(42L)).thenReturn(Optional.of(testFreelancer))
        when(commandService.findById(-999L)).thenReturn(Optional.empty())
        when(commandService.save(any())).thenReturn(testFreelancer)
        when(commandService.save(any(), any(), any())).thenReturn(testFreelancer)

        when(queryService.findFirst()).thenReturn(Optional.of(detailView))
        when(queryService.findLast()).thenReturn(Optional.of(detailView))
        when(queryService.findPrevious(anyLong())).thenReturn(Optional.empty())
        when(queryService.findNext(anyLong())).thenReturn(Optional.empty())
        when(queryService.findById(42L)).thenReturn(Optional.of(detailView))
        when(queryService.findContactsByFreelancerId(anyLong())).thenReturn([])
        when(queryService.findHistoryByFreelancerId(anyLong())).thenReturn([])
        when(queryService.findTagsByFreelancerId(anyLong())).thenReturn([])
        when(queryService.findAvailableTagsByFreelancerIdAndType(anyLong(), any())).thenReturn([])
        when(queryService.search(any(), anyInt(), anyInt())).thenReturn([
                new FreelancerSearchResult(1L, "CODE-001", "Mustermann", "Max", null, "Berlin", null, null, null, null)
        ])
        when(queryService.countSearch(any())).thenReturn(1L)

        when(historyTypeQueryService.findAll()).thenReturn([])
    }

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------

    def "GET /freelancer ohne Cookie leitet auf ersten Freiberufler weiter (302)"() {
        when:
        def result = mockMvc.perform(get("/freelancer").with(user("testuser")))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "GET /freelancer mit lastFreelancerId-Cookie leitet auf diesen Freiberufler weiter (302)"() {
        when:
        def result = mockMvc.perform(
                get("/freelancer")
                        .with(user("testuser"))
                        .cookie(new jakarta.servlet.http.Cookie("lastFreelancerId", "42")))

        then:
        result.andExpect(status().is3xxRedirection())
              .andExpect(header().string("Location", "/freelancer/42"))
    }

    def "GET /freelancer/{id} liefert HTTP 200, rendert Template und setzt lastFreelancerId-Cookie"() {
        when:
        def result = mockMvc.perform(get("/freelancer/42").with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(view().name("freelancer/form"))
              .andExpect(cookie().value("lastFreelancerId", "42"))
    }

    def "GET /freelancer/new liefert HTTP 200, rendert Template und loescht lastFreelancerId-Cookie"() {
        when:
        def result = mockMvc.perform(get("/freelancer/new").with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(view().name("freelancer/form"))
              .andExpect(cookie().maxAge("lastFreelancerId", 0))
    }

    def "GET /freelancer/first leitet auf ersten Freiberufler weiter (302)"() {
        when:
        def result = mockMvc.perform(get("/freelancer/first").with(user("testuser")))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "GET /freelancer/last leitet auf letzten Freiberufler weiter (302)"() {
        when:
        def result = mockMvc.perform(get("/freelancer/last").with(user("testuser")))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "GET /freelancer/previous/{id} leitet weiter (302)"() {
        when:
        def result = mockMvc.perform(get("/freelancer/previous/42").with(user("testuser")))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "GET /freelancer/next/{id} leitet weiter (302)"() {
        when:
        def result = mockMvc.perform(get("/freelancer/next/42").with(user("testuser")))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    // -------------------------------------------------------------------------
    // Save
    // -------------------------------------------------------------------------

    def "POST /freelancer/save ohne Kontakte und Historie leitet auf Freelancer-Formular weiter (302)"() {
        when:
        def result = mockMvc.perform(
                post("/freelancer/save")
                        .with(csrf())
                        .with(user("testuser"))
                        .param("id", "42")
                        .param("dbVersion", "0")
                        .param("name1", "Mustermann")
                        .param("contactsJson", "[]")
                        .param("historyJson", "[]"))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "POST /freelancer/save bei OptimisticLockingFailureException gibt 409 JSON zurueck"() {
        given:
        when(commandService.save(any(), any(), any())).thenThrow(new OptimisticLockingFailureException("conflict"))

        when:
        def result = mockMvc.perform(
                post("/freelancer/save")
                        .with(csrf())
                        .with(user("testuser"))
                        .param("id", "42")
                        .param("dbVersion", "0")
                        .param("contactsJson", "[]")
                        .param("historyJson", "[]"))

        then:
        result.andExpect(status().isConflict())
              .andExpect(jsonPath('$.conflict').value(true))
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    def "POST /freelancer/delete/{id} leitet auf /freelancer/new weiter (302)"() {
        when:
        def result = mockMvc.perform(
                post("/freelancer/delete/42")
                        .with(csrf())
                        .with(user("testuser")))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "POST /freelancer/delete/{id} bei FreelancerHasPositionsException gibt 409 JSON zurueck"() {
        given:
        doThrow(new FreelancerHasPositionsException([7L, 8L]))
                .when(commandService).deleteById(42L)

        when:
        def result = mockMvc.perform(
                post("/freelancer/delete/42")
                        .with(csrf())
                        .with(user("testuser")))

        then:
        result.andExpect(status().isConflict())
              .andExpect(jsonPath('$.blocked').value(true))
    }

    // -------------------------------------------------------------------------
    // QBE-Suche
    // -------------------------------------------------------------------------

    def "POST /freelancer/search gibt search-results Fragment zurueck (200)"() {
        when:
        def result = mockMvc.perform(
                post("/freelancer/search")
                        .with(csrf())
                        .with(user("testuser"))
                        .param("name1", "Mustermann"))

        then:
        result.andExpect(status().isOk())
    }

    // -------------------------------------------------------------------------
    // Tag-Verwaltung
    // -------------------------------------------------------------------------

    def "POST /freelancer/{id}/tags fuegt Tag hinzu und liefert 200 JSON"() {
        when:
        def result = mockMvc.perform(
                post("/freelancer/42/tags")
                        .with(csrf())
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content('{"tagId": 5}'))

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.ok').value(true))
    }

    def "POST /freelancer/{id}/tags bei DuplicateTagException gibt 409 JSON zurueck"() {
        given:
        doThrow(new DuplicateTagException(42L, 5L)).when(tagCommandService).addTag(42L, 5L)

        when:
        def result = mockMvc.perform(
                post("/freelancer/42/tags")
                        .with(csrf())
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content('{"tagId": 5}'))

        then:
        result.andExpect(status().isConflict())
              .andExpect(jsonPath('$.duplicate').value(true))
    }

    def "DELETE /freelancer/{id}/tags/{tagId} entfernt Tag und liefert 200 JSON"() {
        when:
        def result = mockMvc.perform(
                delete("/freelancer/42/tags/10")
                        .with(csrf())
                        .with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.ok').value(true))
    }

    def "GET /freelancer/{id}/available-tags/{type} liefert verfuegbare Tags als JSON"() {
        when:
        def result = mockMvc.perform(
                get("/freelancer/42/available-tags/SCHWERPUNKT")
                        .with(user("testuser")))

        then:
        result.andExpect(status().isOk())
    }
}
