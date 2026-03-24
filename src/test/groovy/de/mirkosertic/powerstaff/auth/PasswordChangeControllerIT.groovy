package de.mirkosertic.powerstaff.auth

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.when
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class PasswordChangeControllerIT extends AbstractContainerBaseIT {

    @Autowired
    WebApplicationContext wac

    MockMvc mockMvc

    @MockitoBean
    PsUserRepository psUserRepository

    @MockitoBean
    PasswordEncoder passwordEncoder

    def setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build()
    }

    def "GET /passwort-aendern liefert HTTP 200 und rendert Formular"() {
        when:
        def result = mockMvc.perform(get("/passwort-aendern").with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(view().name("auth/password-change"))
    }

    def "POST /passwort-aendern mit nicht uebereinstimmenden Passwoertern zeigt Fehler (200)"() {
        when:
        def result = mockMvc.perform(
                post("/passwort-aendern")
                        .with(csrf())
                        .with(user("testuser"))
                        .param("oldPassword", "altes")
                        .param("newPassword", "neu1")
                        .param("newPasswordConfirm", "neu2"))

        then:
        result.andExpect(status().isOk())
              .andExpect(view().name("auth/password-change"))
    }

    def "POST /passwort-aendern mit zu kurzem Passwort zeigt Fehler (200)"() {
        when:
        def result = mockMvc.perform(
                post("/passwort-aendern")
                        .with(csrf())
                        .with(user("testuser"))
                        .param("oldPassword", "altes")
                        .param("newPassword", "kurz")
                        .param("newPasswordConfirm", "kurz"))

        then:
        result.andExpect(status().isOk())
              .andExpect(view().name("auth/password-change"))
    }

    def "POST /passwort-aendern mit falschem altem Passwort zeigt Fehler (200)"() {
        given:
        def psUser = new PsUser("testuser", "somehash", false, true, PsUser.DEFAULT_SYSTEM_PROMPT)
        when(psUserRepository.findById("testuser")).thenReturn(Optional.of(psUser))
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false)

        when:
        def result = mockMvc.perform(
                post("/passwort-aendern")
                        .with(csrf())
                        .with(user("testuser"))
                        .param("oldPassword", "wrongpassword")
                        .param("newPassword", "neuesPasswort1")
                        .param("newPasswordConfirm", "neuesPasswort1"))

        then:
        result.andExpect(status().isOk())
              .andExpect(view().name("auth/password-change"))
    }

    def "POST /passwort-aendern mit korrektem Passwort leitet auf / weiter (302)"() {
        given:
        def psUser = new PsUser("testuser", "hashed", false, true, PsUser.DEFAULT_SYSTEM_PROMPT)
        when(psUserRepository.findById("testuser")).thenReturn(Optional.of(psUser))
        when(passwordEncoder.matches("richtig123", "hashed")).thenReturn(true)
        when(passwordEncoder.encode("neuesPasswort1")).thenReturn("newerHash")

        when:
        def result = mockMvc.perform(
                post("/passwort-aendern")
                        .with(csrf())
                        .with(user("testuser"))
                        .param("oldPassword", "richtig123")
                        .param("newPassword", "neuesPasswort1")
                        .param("newPasswordConfirm", "neuesPasswort1"))

        then:
        result.andExpect(status().is3xxRedirection())
              .andExpect(header().string("Location", "/"))
    }
}
