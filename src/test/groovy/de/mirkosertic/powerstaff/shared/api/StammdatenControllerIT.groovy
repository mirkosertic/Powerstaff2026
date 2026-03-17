package de.mirkosertic.powerstaff.shared.api

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.shared.TagType
import de.mirkosertic.powerstaff.shared.command.HistoryType
import de.mirkosertic.powerstaff.shared.command.ProjectPositionStatus
import de.mirkosertic.powerstaff.shared.command.StammdatenCommandService
import de.mirkosertic.powerstaff.shared.command.Tag
import de.mirkosertic.powerstaff.shared.query.HistoryTypeQueryService
import de.mirkosertic.powerstaff.shared.query.HistoryTypeView
import de.mirkosertic.powerstaff.shared.query.ProjectPositionStatusQueryService
import de.mirkosertic.powerstaff.shared.query.ProjectPositionStatusView
import de.mirkosertic.powerstaff.shared.query.TagQueryService
import de.mirkosertic.powerstaff.shared.query.TagView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.doNothing
import static org.mockito.Mockito.when
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * Controller-Tests fuer StammdatenController mit vollstaendigem Thymeleaf-Rendering.
 *
 * Nutzt @SpringBootTest mit ManualMockMvc-Aufbau (analog SecurityIT/FragmentRenderingIT),
 * da @WebMvcTest in Spring Boot 4 entfernt wurde. Alle Service-Abhaengigkeiten werden
 * mit @MockitoBean ersetzt.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class StammdatenControllerIT extends AbstractContainerBaseIT {

    @Autowired
    WebApplicationContext wac

    MockMvc mockMvc

    @MockitoBean
    HistoryTypeQueryService historyTypeQueryService

    @MockitoBean
    ProjectPositionStatusQueryService projectPositionStatusQueryService

    @MockitoBean
    TagQueryService tagQueryService

    @MockitoBean
    StammdatenCommandService stammdatenCommandService

    def setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        // Standard-Stubs fuer alle Service-Methoden
        when(historyTypeQueryService.findAll()).thenReturn([
                new HistoryTypeView(1L, "Telefonat"),
                new HistoryTypeView(2L, "E-Mail")
        ])
        when(projectPositionStatusQueryService.findAll()).thenReturn([
                new ProjectPositionStatusView(1L, "Vorgeschlagen", "#d1fae5", "#065f46")
        ])
        TagType.values().each { tagType ->
            when(tagQueryService.findByType(tagType)).thenReturn([])
        }
        when(tagQueryService.findByType(TagType.SCHWERPUNKT)).thenReturn([
                new TagView(1L, "Java", "SCHWERPUNKT")
        ])
        def savedHt = new HistoryType("Telefonat")
        when(stammdatenCommandService.saveHistoryType(any())).thenReturn(savedHt)
        when(stammdatenCommandService.findHistoryTypeById(1L))
                .thenReturn(Optional.of(new HistoryType("Telefonat")))
        when(stammdatenCommandService.findHistoryTypeById(-999L))
                .thenReturn(Optional.empty())
        when(stammdatenCommandService.saveProjectPositionStatus(any()))
                .thenReturn(new ProjectPositionStatus("Vorgeschlagen", "#d1fae5", "#065f46"))
        when(stammdatenCommandService.findProjectPositionStatusById(1L))
                .thenReturn(Optional.of(new ProjectPositionStatus("Vorgeschlagen", "#d1fae5", "#065f46")))
        def savedTag = new Tag("Java", "SCHWERPUNKT")
        when(stammdatenCommandService.saveTag(any())).thenReturn(savedTag)
        when(stammdatenCommandService.findTagById(1L))
                .thenReturn(Optional.of(new Tag("Java", "SCHWERPUNKT")))
        doNothing().when(stammdatenCommandService).deleteHistoryType(any())
        doNothing().when(stammdatenCommandService).deleteTag(any())
    }

    // -------------------------------------------------------------------------
    // GET /admin – Redirect
    // -------------------------------------------------------------------------

    def "GET /admin leitet unauthentifiziert auf /login weiter"() {
        when: "die Admin-Wurzel ohne Login aufgerufen wird"
        def result = mockMvc.perform(get("/admin"))

        then: "Spring Security leitet auf /login weiter"
        result.andExpect(status().is3xxRedirection())
    }

    // -------------------------------------------------------------------------
    // Historientypen
    // -------------------------------------------------------------------------

    def "GET /admin/historientypen mit Login liefert HTTP 200 und rendert das Template"() {
        given: "ein eingeloggter Testbenutzer"

        when: "die Historientypen-Seite mit forwardedHeader aufgerufen wird"
        def result = mockMvc.perform(
                get("/admin/historientypen")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("testuser")))

        then: "HTTP 200 und das Template wird fehlerfrei gerendert"
        result.andExpect(status().isOk())
              .andExpect(view().name("admin/historientypen"))
    }

    def "GET /admin/historientypen befuellt das Model mit types und newType"() {
        when: "die Historientypen-Seite aufgerufen wird"
        def result = mockMvc.perform(
                get("/admin/historientypen")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("testuser")))

        then: "das Model enthaelt types und newType"
        result.andExpect(status().isOk())
              .andExpect(model().attributeExists("types", "newType"))
    }

    def "POST /admin/historientypen speichert und leitet weiter (302)"() {
        when: "ein neuer Historientyp per POST erstellt wird"
        def result = mockMvc.perform(
                post("/admin/historientypen")
                        .with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("testuser"))
                        .param("description", "Neuer Typ"))

        then: "ein Redirect auf /admin/historientypen erfolgt"
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/admin/historientypen"))
    }

    def "POST /admin/historientypen/{id} aktualisiert und leitet weiter (302)"() {
        when: "ein Historientyp per POST aktualisiert wird"
        def result = mockMvc.perform(
                post("/admin/historientypen/1")
                        .with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("testuser"))
                        .param("description", "Aktualisierter Typ"))

        then: "ein Redirect auf /admin/historientypen erfolgt"
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/admin/historientypen"))
    }

    def "POST /admin/historientypen/{id}/delete loescht und leitet weiter (302)"() {
        when: "ein Historientyp per POST geloescht wird"
        def result = mockMvc.perform(
                post("/admin/historientypen/1/delete")
                        .with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("testuser")))

        then: "ein Redirect auf /admin/historientypen erfolgt"
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/admin/historientypen"))
    }

    // -------------------------------------------------------------------------
    // Projektpositions-Status
    // -------------------------------------------------------------------------

    def "GET /admin/positionsstatus liefert HTTP 200 und rendert das Template"() {
        when: "die Positionsstatus-Seite aufgerufen wird"
        def result = mockMvc.perform(
                get("/admin/positionsstatus")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("testuser")))

        then: "HTTP 200 und das Template wird fehlerfrei gerendert"
        result.andExpect(status().isOk())
              .andExpect(view().name("admin/positionsstatus"))
    }

    def "GET /admin/positionsstatus befuellt das Model mit statusList und newStatus"() {
        when: "die Positionsstatus-Seite aufgerufen wird"
        def result = mockMvc.perform(
                get("/admin/positionsstatus")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("testuser")))

        then: "das Model enthaelt statusList und newStatus"
        result.andExpect(status().isOk())
              .andExpect(model().attributeExists("statusList", "newStatus"))
    }

    def "POST /admin/positionsstatus speichert und leitet weiter (302)"() {
        when: "ein neuer Positionsstatus per POST erstellt wird"
        def result = mockMvc.perform(
                post("/admin/positionsstatus")
                        .with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("testuser"))
                        .param("description", "Im Gespraech")
                        .param("color", "#fef3c7")
                        .param("colorText", "#92400e"))

        then: "ein Redirect auf /admin/positionsstatus erfolgt"
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/admin/positionsstatus"))
    }

    def "POST /admin/positionsstatus/{id} aktualisiert und leitet weiter (302)"() {
        when: "ein Positionsstatus per POST aktualisiert wird"
        def result = mockMvc.perform(
                post("/admin/positionsstatus/1")
                        .with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("testuser"))
                        .param("description", "Besetzt")
                        .param("color", "#dbeafe")
                        .param("colorText", "#1e40af"))

        then: "ein Redirect auf /admin/positionsstatus erfolgt"
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/admin/positionsstatus"))
    }

    // -------------------------------------------------------------------------
    // Tags
    // -------------------------------------------------------------------------

    def "GET /admin/tags liefert HTTP 200 und rendert das Template"() {
        when: "die Tags-Seite aufgerufen wird"
        def result = mockMvc.perform(
                get("/admin/tags")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("testuser")))

        then: "HTTP 200 und das Template wird fehlerfrei gerendert"
        result.andExpect(status().isOk())
              .andExpect(view().name("admin/tags"))
    }

    def "GET /admin/tags befuellt das Model mit tagsByType tagTypes und newTag"() {
        when: "die Tags-Seite aufgerufen wird"
        def result = mockMvc.perform(
                get("/admin/tags")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("testuser")))

        then: "das Model enthaelt tagsByType, tagTypes und newTag"
        result.andExpect(status().isOk())
              .andExpect(model().attributeExists("tagsByType", "tagTypes", "newTag"))
    }

    def "POST /admin/tags erstellt einen neuen Tag und leitet weiter (302)"() {
        when: "ein neuer Tag per POST erstellt wird"
        def result = mockMvc.perform(
                post("/admin/tags")
                        .with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("testuser"))
                        .param("tagname", "Python")
                        .param("tagType", "SCHWERPUNKT"))

        then: "ein Redirect auf /admin/tags erfolgt"
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/admin/tags"))
    }

    def "POST /admin/tags/{id} aktualisiert einen Tag und leitet weiter (302)"() {
        when: "ein Tag per POST aktualisiert wird"
        def result = mockMvc.perform(
                post("/admin/tags/1")
                        .with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("testuser"))
                        .param("tagname", "Python aktualisiert"))

        then: "ein Redirect auf /admin/tags erfolgt"
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/admin/tags"))
    }

    def "DELETE /admin/tags/{id} loescht einen Tag und gibt JSON ok:true zurueck (200)"() {
        when: "ein Tag per DELETE geloescht wird"
        def result = mockMvc.perform(
                delete("/admin/tags/1")
                        .with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("testuser")))

        then: "HTTP 200 und JSON-Antwort ok:true"
        result.andExpect(status().isOk())
              .andExpect(content().contentTypeCompatibleWith("application/json"))
              .andExpect(jsonPath('$.ok').value(true))
    }
}
