package de.mirkosertic.powerstaff.auth

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.not
import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.anyBoolean
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.doNothing
import static org.mockito.Mockito.when
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view

/**
 * Controller-Tests fuer UserController mit vollstaendigem Thymeleaf-Rendering.
 * Nutzt @SpringBootTest mit MockMvc-Aufbau analog zu StammdatenControllerIT.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class UserControllerIT extends AbstractContainerBaseIT {

    @Autowired
    WebApplicationContext wac

    MockMvc mockMvc

    @MockitoBean
    UserQueryService userQueryService

    @MockitoBean
    UserCommandService userCommandService

    def setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        // Standard-Stubs
        when(userQueryService.findAll()).thenReturn([
                new UserView("admin", false, true, PsUser.DEFAULT_SYSTEM_PROMPT, true, null),
                new UserView("sachbearbeiter1", true, true, PsUser.DEFAULT_SYSTEM_PROMPT, false, null)
        ])
        when(userQueryService.findByUsername("admin"))
                .thenReturn(Optional.of(new UserView("admin", false, true, PsUser.DEFAULT_SYSTEM_PROMPT, true, null)))
        when(userQueryService.findByUsername("sachbearbeiter1"))
                .thenReturn(Optional.of(new UserView("sachbearbeiter1", true, true, PsUser.DEFAULT_SYSTEM_PROMPT, false, null)))
        when(userQueryService.findByUsername("nicht_vorhanden"))
                .thenReturn(Optional.empty())

        def adminUser = new PsUser("admin",
                '{bcrypt}$2a$10$abcdefghijklmnopqrstuuVGlGNBdm6/GQ4DV.jMRfF4wrfHOFuq6',
                false, true, PsUser.DEFAULT_SYSTEM_PROMPT, true, null)
        when(userCommandService.findByUsername("admin")).thenReturn(Optional.of(adminUser))
        when(userCommandService.findByUsername("sachbearbeiter1"))
                .thenReturn(Optional.of(new PsUser("sachbearbeiter1",
                        '{bcrypt}$2a$10$abcdefghijklmnopqrstuuVGlGNBdm6/GQ4DV.jMRfF4wrfHOFuq6',
                        true, true, PsUser.DEFAULT_SYSTEM_PROMPT, false, null)))
        when(userCommandService.findByUsername("nicht_vorhanden")).thenReturn(Optional.empty())

        doNothing().when(userCommandService).createUser(anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean())
        doNothing().when(userCommandService).updateUser(anyString(), anyBoolean(), anyBoolean())
        doNothing().when(userCommandService).resetPassword(anyString(), anyString())
        doNothing().when(userCommandService).deleteUser(anyString())
    }

    // -------------------------------------------------------------------------
    // Sicherheit
    // -------------------------------------------------------------------------

    def "GET /admin/benutzer ohne Login leitet auf /login weiter"() {
        when:
        def result = mockMvc.perform(get("/admin/benutzer"))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    // -------------------------------------------------------------------------
    // GET /admin/benutzer
    // -------------------------------------------------------------------------

    def "GET /admin/benutzer mit Login liefert HTTP 200 und rendert das Template"() {
        when:
        def result = mockMvc.perform(get("/admin/benutzer").with(user("admin").roles("USER", "ADMIN")))

        then:
        result.andExpect(status().isOk())
              .andExpect(view().name("admin/users"))
    }

    def "GET /admin/benutzer befuellt das Model mit users"() {
        when:
        def result = mockMvc.perform(get("/admin/benutzer").with(user("admin").roles("USER", "ADMIN")))

        then:
        result.andExpect(status().isOk())
              .andExpect(model().attributeExists("users"))
    }

    def "GET /admin/benutzer rendert HTML ohne Exception"() {
        when:
        def result = mockMvc.perform(get("/admin/benutzer").with(user("admin").roles("USER", "ADMIN")))

        then:
        result.andExpect(status().isOk())
              .andExpect(content().string(containsString("admin")))
              .andExpect(content().string(not(containsString("Exception"))))
              .andExpect(content().string(not(containsString("Whitelabel Error"))))
    }

    // -------------------------------------------------------------------------
    // POST /admin/benutzer – Neuanlage
    // -------------------------------------------------------------------------

    def "POST /admin/benutzer legt neuen Benutzer an und leitet weiter (302)"() {
        given: "ein Benutzername der noch nicht existiert"
        when(userQueryService.findByUsername("neuer_user")).thenReturn(Optional.empty())

        when:
        def result = mockMvc.perform(
                post("/admin/benutzer")
                        .with(csrf())
                        .with(user("admin").roles("USER", "ADMIN"))
                        .param("username", "neuer_user")
                        .param("password", "sicheresPasswort1")
                        .param("enabled", "true"))

        then:
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/admin/benutzer"))
    }

    def "POST /admin/benutzer mit zu kurzem Passwort leitet mit Fehlermeldung weiter"() {
        when:
        def result = mockMvc.perform(
                post("/admin/benutzer")
                        .with(csrf())
                        .with(user("admin").roles("USER", "ADMIN"))
                        .param("username", "neuer_user")
                        .param("password", "kurz"))

        then:
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/admin/benutzer"))
    }

    def "POST /admin/benutzer mit bereits existierendem Benutzernamen leitet mit Fehlermeldung weiter"() {
        when:
        def result = mockMvc.perform(
                post("/admin/benutzer")
                        .with(csrf())
                        .with(user("admin").roles("USER", "ADMIN"))
                        .param("username", "admin")
                        .param("password", "passwort123"))

        then:
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/admin/benutzer"))
    }

    // -------------------------------------------------------------------------
    // POST /admin/benutzer/{username} – Bearbeiten
    // -------------------------------------------------------------------------

    def "POST /admin/benutzer/{username} aktualisiert Benutzer und leitet weiter (302)"() {
        when:
        def result = mockMvc.perform(
                post("/admin/benutzer/sachbearbeiter1")
                        .with(csrf())
                        .with(user("admin").roles("USER", "ADMIN"))
                        .param("enabled", "true")
                        .param("mustChangePassword", "false"))

        then:
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/admin/benutzer"))
    }

    def "POST /admin/benutzer/{username} mit neuem Passwort aktualisiert und leitet weiter (302)"() {
        when:
        def result = mockMvc.perform(
                post("/admin/benutzer/sachbearbeiter1")
                        .with(csrf())
                        .with(user("admin").roles("USER", "ADMIN"))
                        .param("enabled", "true")
                        .param("mustChangePassword", "false")
                        .param("newPassword", "neuesPasswort1"))

        then:
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/admin/benutzer"))
    }

    def "POST /admin/benutzer/{username} fuer nicht existierenden Benutzer leitet mit Fehler weiter"() {
        when:
        def result = mockMvc.perform(
                post("/admin/benutzer/nicht_vorhanden")
                        .with(csrf())
                        .with(user("admin").roles("USER", "ADMIN"))
                        .param("enabled", "true"))

        then:
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/admin/benutzer"))
    }

    // -------------------------------------------------------------------------
    // DELETE /admin/benutzer/{username}
    // -------------------------------------------------------------------------

    def "DELETE /admin/benutzer/{username} loescht Benutzer und gibt JSON ok:true zurueck"() {
        when:
        def result = mockMvc.perform(
                delete("/admin/benutzer/sachbearbeiter1")
                        .with(csrf())
                        .with(user("admin").roles("USER", "ADMIN")))

        then:
        result.andExpect(status().isOk())
              .andExpect(content().contentTypeCompatibleWith("application/json"))
              .andExpect(jsonPath('$.ok').value(true))
    }

    def "DELETE /admin/benutzer/{username} eigenes Konto gibt Fehler zurueck"() {
        when:
        def result = mockMvc.perform(
                delete("/admin/benutzer/admin")
                        .with(csrf())
                        .with(user("admin").roles("USER", "ADMIN")))

        then:
        result.andExpect(status().is4xxClientError())
              .andExpect(jsonPath('$.ok').value(false))
    }

    def "DELETE /admin/benutzer/{username} fuer nicht existierenden Benutzer gibt Fehler zurueck"() {
        when:
        def result = mockMvc.perform(
                delete("/admin/benutzer/nicht_vorhanden")
                        .with(csrf())
                        .with(user("admin").roles("USER", "ADMIN")))

        then:
        result.andExpect(status().is4xxClientError())
              .andExpect(jsonPath('$.ok').value(false))
    }
}
