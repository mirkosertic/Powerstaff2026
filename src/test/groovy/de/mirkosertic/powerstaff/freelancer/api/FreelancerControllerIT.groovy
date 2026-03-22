package de.mirkosertic.powerstaff.freelancer.api

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.freelancer.command.Freelancer
import de.mirkosertic.powerstaff.freelancer.command.FreelancerCommandService
import de.mirkosertic.powerstaff.freelancer.command.FreelancerHasPositionsException
import de.mirkosertic.powerstaff.freelancer.command.FreelancerTagCommandService
import de.mirkosertic.powerstaff.freelancer.query.FreelancerDetailView
import de.mirkosertic.powerstaff.freelancer.query.FreelancerQueryService
import de.mirkosertic.powerstaff.freelancer.query.FreelancerSearchResult
import de.mirkosertic.powerstaff.project.command.FreelancerAlreadyAssignedException
import de.mirkosertic.powerstaff.project.command.ProjectPositionCommandService
import de.mirkosertic.powerstaff.project.command.RememberedProjectService
import de.mirkosertic.powerstaff.shared.query.HistoryTypeQueryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import jakarta.servlet.http.Cookie
import java.time.LocalDateTime

import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.anyInt
import static org.mockito.ArgumentMatchers.anyLong
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.doNothing
import static org.mockito.Mockito.doThrow
import static org.mockito.Mockito.when
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.not
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
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

    @MockitoBean
    RememberedProjectService rememberedProjectService

    @MockitoBean
    ProjectPositionCommandService positionCommandService

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
        when(commandService.save(any(), any(), any(), any())).thenReturn(testFreelancer)

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

        when(rememberedProjectService.get(anyString())).thenReturn(Optional.empty())
        when(rememberedProjectService.getRememberedProjectInfo(anyString())).thenReturn(Optional.empty())
        doNothing().when(positionCommandService).assignFreelancerToProject(anyLong(), anyLong(), any(), any(), any())
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
                        .cookie(new Cookie("lastFreelancerId", "42")))

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
    // Save (Unified Save mit Delta-Commands)
    // -------------------------------------------------------------------------

    def "POST /freelancer/save mit Kontakt- und Tag-Delta leitet auf Freelancer-Formular weiter (302)"() {
        when:
        def result = mockMvc.perform(
                post("/freelancer/save")
                        .with(csrf())
                        .with(user("testuser"))
                        .param("id", "42")
                        .param("dbVersion", "0")
                        .param("name1", "Mustermann")
                        .param("contactsJson", '[{"op":"ADD","id":null,"type":"EMAIL","value":"test@example.com"}]')
                        .param("historyJson", "[]")
                        .param("tagsJson", '[{"op":"ADD","tagId":1}]'))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "POST /freelancer/save ohne Delta-Parameter leitet auf Freelancer-Formular weiter (302)"() {
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
        when(commandService.save(any(), any(), any(), any())).thenThrow(new OptimisticLockingFailureException("conflict"))

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

    def "GET /freelancer/search gibt search-page zurueck (200)"() {
        when:
        def result = mockMvc.perform(
                get("/freelancer/search")
                        .with(user("testuser"))
                        .param("name1", "Mustermann"))

        then:
        result.andExpect(status().isOk())
    }

    def "GET /freelancer/search-more gibt Fragment zurueck (200)"() {
        when:
        def result = mockMvc.perform(
                get("/freelancer/search-more?offset=20&name1=Mustermann")
                        .with(user("testuser")))

        then:
        result.andExpect(status().isOk())
    }

    def "GET /freelancer/search ohne Parameter liefert 200 und search-page Template"() {
        when:
        def result = mockMvc.perform(
                get("/freelancer/search")
                        .with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(view().name("freelancer/search-page"))
    }

    def "GET /freelancer/search setzt Cache-Control Header no-store"() {
        when:
        def result = mockMvc.perform(
                get("/freelancer/search")
                        .with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(header().string("Cache-Control", containsString("no-store")))
    }

    def "GET /freelancer/search mit name1-Parameter liefert 200 und kein Exception"() {
        when:
        def result = mockMvc.perform(
                get("/freelancer/search")
                        .param("name1", "Müller")
                        .param("sortField", "name1")
                        .param("sortDir", "asc")
                        .with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(header().string("Cache-Control", containsString("no-store")))
              .andExpect(content().string(not(containsString('Exception'))))
    }

    def "GET /freelancer/search rendert HTML-Seite ohne Exception"() {
        when:
        def result = mockMvc.perform(
                get("/freelancer/search")
                        .with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(content().string(not(containsString('Exception'))))
              .andExpect(content().string(not(containsString('Whitelabel Error'))))
    }

    def "GET /freelancer/search-more mit offset-Parameter gibt Fragment zurueck (200)"() {
        when:
        def result = mockMvc.perform(
                get("/freelancer/search-more")
                        .param("offset", "0")
                        .param("name1", "Müller")
                        .with(user("testuser")))

        then:
        result.andExpect(status().isOk())
    }

    // -------------------------------------------------------------------------
    // Thymeleaf-Rendering HTML-Inhalte pruefen
    // -------------------------------------------------------------------------

    def "GET /freelancer/{id} rendert HTML mit Formular ohne Exception"() {
        when:
        def result = mockMvc.perform(get("/freelancer/42").with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(content().string(containsString('<form')))
              .andExpect(content().string(not(containsString('Exception'))))
              .andExpect(content().string(not(containsString('Whitelabel Error'))))
    }

    def "GET /freelancer/new rendert HTML mit leerem Formular ohne Exception"() {
        when:
        def result = mockMvc.perform(get("/freelancer/new").with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(content().string(containsString('<form')))
              .andExpect(content().string(not(containsString('Exception'))))
    }

    def "GET /freelancer/search rendert HTML-Seite ohne Exception"() {
        when:
        def result = mockMvc.perform(
                get("/freelancer/search")
                        .with(user("testuser"))
                        .param("name1", "Mustermann"))

        then:
        result.andExpect(status().isOk())
              .andExpect(content().string(not(containsString('Exception'))))
    }

    // -------------------------------------------------------------------------
    // Lookup per Code
    // -------------------------------------------------------------------------

    def "GET /freelancer/lookup?code=CODE-001 liefert JSON mit id-Feld (200)"() {
        given:
        def lookupResult = new de.mirkosertic.powerstaff.freelancer.command.FreelancerLookupResult(42L, null, "Muster GmbH")
        when(commandService.findByCode("CODE-001")).thenReturn(Optional.of(lookupResult))

        when:
        def result = mockMvc.perform(
                get("/freelancer/lookup")
                        .with(user("testuser"))
                        .param("code", "CODE-001"))

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.id').value(42))
    }

    def "GET /freelancer/lookup?code=UNBEKANNT liefert 404 mit notFound:true"() {
        given:
        when(commandService.findByCode("UNBEKANNT")).thenReturn(Optional.empty())

        when:
        def result = mockMvc.perform(
                get("/freelancer/lookup")
                        .with(user("testuser"))
                        .param("code", "UNBEKANNT"))

        then:
        result.andExpect(status().isNotFound())
              .andExpect(jsonPath('$.notFound').value(true))
    }

    // -------------------------------------------------------------------------
    // Verfuegbare Tags (GET-Endpoint bleibt erhalten)
    // -------------------------------------------------------------------------

    def "GET /freelancer/{id}/available-tags/{type} liefert verfuegbare Tags als JSON"() {
        when:
        def result = mockMvc.perform(
                get("/freelancer/42/available-tags/SCHWERPUNKT")
                        .with(user("testuser")))

        then:
        result.andExpect(status().isOk())
    }

    // -------------------------------------------------------------------------
    // Zum gemerkten Projekt zuordnen
    // -------------------------------------------------------------------------

    def "POST /freelancer/{id}/assign-to-remembered-project ohne gemerktes Projekt gibt 404 zurueck"() {
        given:
        when(rememberedProjectService.get("testuser")).thenReturn(Optional.empty())

        when:
        def result = mockMvc.perform(
                post("/freelancer/42/assign-to-remembered-project")
                        .with(csrf())
                        .with(user("testuser")))

        then:
        result.andExpect(status().isNotFound())
              .andExpect(jsonPath('$.noProject').value(true))
    }

    def "POST /freelancer/{id}/assign-to-remembered-project mit gemerktem Projekt gibt projectId zurueck (200)"() {
        given:
        when(rememberedProjectService.get("testuser")).thenReturn(Optional.of(99L))

        when:
        def result = mockMvc.perform(
                post("/freelancer/42/assign-to-remembered-project")
                        .with(csrf())
                        .with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.projectId').value(99))
    }

    def "POST /freelancer/{id}/assign-to-remembered-project bei bereits zugeordnetem Freiberufler gibt 409 zurueck"() {
        given:
        when(rememberedProjectService.get("testuser")).thenReturn(Optional.of(99L))
        doThrow(new FreelancerAlreadyAssignedException(42L, 99L))
                .when(positionCommandService).assignFreelancerToProject(anyLong(), anyLong(), any(), any(), any())

        when:
        def result = mockMvc.perform(
                post("/freelancer/42/assign-to-remembered-project")
                        .with(csrf())
                        .with(user("testuser")))

        then:
        result.andExpect(status().isConflict())
              .andExpect(jsonPath('$.alreadyAssigned').value(true))
    }
}
