package de.mirkosertic.powerstaff.partner.api

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.freelancer.command.FreelancerCommandService
import de.mirkosertic.powerstaff.freelancer.command.FreelancerLookupResult
import de.mirkosertic.powerstaff.partner.command.Partner
import de.mirkosertic.powerstaff.partner.command.PartnerCommandService
import de.mirkosertic.powerstaff.partner.command.PartnerHasProjectsException
import de.mirkosertic.powerstaff.partner.query.PartnerContactView
import de.mirkosertic.powerstaff.partner.query.PartnerDetailView
import de.mirkosertic.powerstaff.partner.query.PartnerFreelancerView
import de.mirkosertic.powerstaff.partner.query.PartnerHistoryView
import de.mirkosertic.powerstaff.partner.query.PartnerProjectView
import de.mirkosertic.powerstaff.partner.query.PartnerQueryService
import de.mirkosertic.powerstaff.partner.query.PartnerSearchResult
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
class PartnerControllerIT extends AbstractContainerBaseIT {

    @Autowired
    WebApplicationContext wac

    MockMvc mockMvc

    @MockitoBean
    PartnerCommandService commandService

    @MockitoBean
    PartnerQueryService queryService

    @MockitoBean
    FreelancerCommandService freelancerCommandService

    def setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        def testPartner = new Partner()
        testPartner.id = 42L
        testPartner.dbVersion = 0L
        testPartner.company = "Test GmbH"
        testPartner.contactForbidden = false
        testPartner.showAgain = false

        def detailView = new PartnerDetailView(
                42L, 0L,
                LocalDateTime.now(), "system",
                LocalDateTime.now(), "system",
                "Test GmbH", null, null, null, null, null, null,
                false, false, null, null, null
        )

        when(commandService.findById(42L)).thenReturn(Optional.of(testPartner))
        when(commandService.findById(-999L)).thenReturn(Optional.empty())
        when(commandService.save(any())).thenReturn(testPartner)
        when(commandService.save(any(), any(), any())).thenReturn(testPartner)

        when(queryService.findFirst()).thenReturn(Optional.of(detailView))
        when(queryService.findLast()).thenReturn(Optional.of(detailView))
        when(queryService.findPrevious(anyLong())).thenReturn(Optional.empty())
        when(queryService.findNext(anyLong())).thenReturn(Optional.empty())
        when(queryService.findById(42L)).thenReturn(Optional.of(detailView))
        when(queryService.findContactsByPartner(anyLong())).thenReturn([] as List<PartnerContactView>)
        when(queryService.findHistoryByPartner(anyLong())).thenReturn([] as List<PartnerHistoryView>)
        when(queryService.findFreelancersByPartner(anyLong())).thenReturn([] as List<PartnerFreelancerView>)
        when(queryService.findProjectsByPartner(anyLong())).thenReturn([] as List<PartnerProjectView>)
        when(queryService.search(any(), anyInt(), anyInt())).thenReturn([
                new PartnerSearchResult(1L, "Test GmbH", null, null, "Berlin")
        ])
        when(queryService.countSearch(any())).thenReturn(1L)

        when(freelancerCommandService.findByCode("FL-001")).thenReturn(Optional.of(new FreelancerLookupResult(10L, null, "Test Freelancer GmbH")))
        when(freelancerCommandService.findByCode("UNKNOWN")).thenReturn(Optional.empty())
        when(freelancerCommandService.findByCode("FL-OTHER")).thenReturn(Optional.of(new FreelancerLookupResult(11L, 99L, "Anderer Freelancer GmbH")))
    }

    def "GET /partner ohne Cookie leitet auf ersten Partner weiter (302)"() {
        when:
        def result = mockMvc.perform(get("/partner").with(user("testuser")))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "GET /partner mit lastPartnerId-Cookie leitet auf diesen Partner weiter (302)"() {
        when:
        def result = mockMvc.perform(
                get("/partner")
                        .with(user("testuser"))
                        .cookie(new Cookie("lastPartnerId", "42")))

        then:
        result.andExpect(status().is3xxRedirection())
              .andExpect(header().string("Location", "/partner/42"))
    }

    def "GET /partner/{id} liefert HTTP 200, rendert Template und setzt lastPartnerId-Cookie"() {
        when:
        def result = mockMvc.perform(get("/partner/42").with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(view().name("partner/form"))
              .andExpect(cookie().value("lastPartnerId", "42"))
    }

    def "GET /partner/new liefert HTTP 200, rendert Template und loescht lastPartnerId-Cookie"() {
        when:
        def result = mockMvc.perform(get("/partner/new").with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(view().name("partner/form"))
              .andExpect(cookie().maxAge("lastPartnerId", 0))
    }

    def "GET /partner/first leitet auf ersten Partner weiter (302)"() {
        when:
        def result = mockMvc.perform(get("/partner/first").with(user("testuser")))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "GET /partner/last leitet auf letzten Partner weiter (302)"() {
        when:
        def result = mockMvc.perform(get("/partner/last").with(user("testuser")))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "GET /partner/previous/{id} leitet weiter (302)"() {
        when:
        def result = mockMvc.perform(get("/partner/previous/42").with(user("testuser")))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "GET /partner/next/{id} leitet weiter (302)"() {
        when:
        def result = mockMvc.perform(get("/partner/next/42").with(user("testuser")))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "POST /partner/save ohne Kontakte und Historie leitet auf Partner-Formular weiter (302)"() {
        when:
        def result = mockMvc.perform(
                post("/partner/save")
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

    def "POST /partner/save mit Kontakten und Historieneintraegen leitet weiter (302)"() {
        when:
        def result = mockMvc.perform(
                post("/partner/save")
                        .with(csrf())
                        .with(user("testuser"))
                        .param("id", "42")
                        .param("dbVersion", "0")
                        .param("company", "Test GmbH")
                        .param("contactsJson", '[{"id":null,"type":"EMAIL","value":"info@example.com"}]')
                        .param("historyJson", '[{"id":null,"typeId":1,"description":"Erstkontakt"}]'))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "POST /partner/save ohne contactsJson und historyJson verwendet leere Listen (302)"() {
        when:
        def result = mockMvc.perform(
                post("/partner/save")
                        .with(csrf())
                        .with(user("testuser"))
                        .param("id", "42")
                        .param("dbVersion", "0"))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "POST /partner/save bei OptimisticLockingFailureException gibt 409 JSON zurueck"() {
        given:
        when(commandService.save(any(), any(), any())).thenThrow(new OptimisticLockingFailureException("conflict"))

        when:
        def result = mockMvc.perform(
                post("/partner/save")
                        .with(csrf())
                        .with(user("testuser"))
                        .param("id", "42")
                        .param("dbVersion", "0")
                        .param("contactsJson", "[]")
                        .param("historyJson", "[]"))

        then:
        result.andExpect(status().isConflict())
              .andExpect(content().contentTypeCompatibleWith("application/json"))
              .andExpect(jsonPath('$.conflict').value(true))
    }

    def "POST /partner/delete/{id} Erfolg leitet auf /partner/new weiter (302)"() {
        when:
        def result = mockMvc.perform(
                post("/partner/delete/42")
                        .with(csrf())
                        .with(user("testuser")))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "POST /partner/delete/{id} bei PartnerHasProjectsException gibt 409 JSON zurueck"() {
        given:
        doThrow(new PartnerHasProjectsException([1L, 2L]))
                .when(commandService).deleteById(anyLong())

        when:
        def result = mockMvc.perform(
                post("/partner/delete/42")
                        .with(csrf())
                        .with(user("testuser")))

        then:
        result.andExpect(status().isConflict())
              .andExpect(content().contentTypeCompatibleWith("application/json"))
              .andExpect(jsonPath('$.blocked').value(true))
    }

    // -------------------------------------------------------------------------
    // Suche
    // -------------------------------------------------------------------------

    def "POST /partner/search gibt HTML-Fragment mit Treffern zurueck"() {
        when:
        def result = mockMvc.perform(
                post("/partner/search")
                        .with(csrf())
                        .with(user("testuser"))
                        .param("company", "Test"))

        then:
        result.andExpect(status().isOk())
    }

    def "GET /partner/search-more gibt HTML-Fragment zurueck"() {
        when:
        def result = mockMvc.perform(
                get("/partner/search-more?offset=20")
                        .with(user("testuser")))

        then:
        result.andExpect(status().isOk())
    }

    // -------------------------------------------------------------------------
    // Freelancer-Zuordnung
    // -------------------------------------------------------------------------

    def "POST /partner/42/assign-freelancer mit bekanntem Code liefert 200"() {
        when:
        def result = mockMvc.perform(
                post("/partner/42/assign-freelancer")
                        .with(csrf())
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content('{"code":"FL-001"}'))

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.ok').value(true))
    }

    def "POST /partner/42/assign-freelancer mit unbekanntem Code liefert 404"() {
        when:
        def result = mockMvc.perform(
                post("/partner/42/assign-freelancer")
                        .with(csrf())
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content('{"code":"UNKNOWN"}'))

        then:
        result.andExpect(status().isNotFound())
              .andExpect(jsonPath('$.notFound').value(true))
    }

    def "POST /partner/42/assign-freelancer mit Freelancer der anderem Partner zugeordnet ist liefert 409"() {
        given:
        when(queryService.findById(99L)).thenReturn(Optional.of(new PartnerDetailView(
                99L, 0L, null, null, null, null,
                "Anderer Partner GmbH", null, null, null, null, null, null,
                false, false, null, null, null)))

        when:
        def result = mockMvc.perform(
                post("/partner/42/assign-freelancer")
                        .with(csrf())
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content('{"code":"FL-OTHER"}'))

        then:
        result.andExpect(status().isConflict())
              .andExpect(jsonPath('$.otherPartner').value("Anderer Partner GmbH"))
    }

    def "POST /partner/42/confirm-reassign-freelancer liefert 200"() {
        when:
        def result = mockMvc.perform(
                post("/partner/42/confirm-reassign-freelancer")
                        .with(csrf())
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content('{"freelancerId":10}'))

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.ok').value(true))
    }

    def "POST /partner/42/remove-freelancer/10 liefert 200"() {
        when:
        def result = mockMvc.perform(
                post("/partner/42/remove-freelancer/10")
                        .with(csrf())
                        .with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.ok').value(true))
    }
}
