package de.mirkosertic.powerstaff

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Integrationstests fuer die Spring-Security-Formularkonfiguration.
 *
 * Prueft das Zusammenspiel von Login-Formular, CSRF-Schutz, Redirect-Logik
 * und der datenbankgestuetzten Benutzerauthentifizierung.
 *
 * Spring Boot 4.0: @AutoConfigureMockMvc wurde entfernt. MockMvc wird manuell
 * via MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()) aufgebaut.
 * Die MySQL-Instanz kommt von AbstractContainerBaseIT (Flyway laeuft automatisch).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SecurityIT extends AbstractContainerBaseIT {

    @Autowired
    WebApplicationContext wac

    @Autowired
    JdbcClient jdbcClient

    MockMvc mockMvc

    def setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build()
    }

    def cleanup() {
        // Testisolation: Testbenutzer nach jeder Feature-Method entfernen
        jdbcClient.sql("DELETE FROM ps_user WHERE username = 'sectest'").update()
    }

    def "unauthentifizierter Zugriff auf / wird auf die Login-Seite weitergeleitet"() {
        when: "ein nicht eingeloggter Client die Wurzel-URL aufruft"
        def result = mockMvc.perform(get("/"))

        then: "Spring Security antwortet mit einem 3xx-Redirect zur Login-Seite"
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/login"))
    }

    def "die Login-Seite ist ohne Authentifizierung erreichbar und liefert HTTP 200"() {
        when: "GET /login ohne Session aufgerufen wird"
        def result = mockMvc.perform(get("/login"))

        then: "die Seite wird mit 200 OK ausgeliefert"
        result.andExpect(status().isOk())
    }

    def "ein Login mit falschen Credentials leitet zu /login?error weiter"() {
        when: "ein POST auf /login mit unbekannten Zugangsdaten und CSRF-Token abgesendet wird"
        def result = mockMvc.perform(
                post("/login")
                        .param("username", "wrong")
                        .param("password", "wrong")
                        .with(csrf())
        )

        then: "Spring Security leitet auf die Login-Seite mit dem error-Parameter weiter"
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/login?error"))
    }

    def "ein Login mit korrekten Credentials leitet nicht zu /login?error weiter"() {
        given: "ein aktiver Testbenutzer mit bekanntem BCrypt-Passwort existiert in der Datenbank"
        def encoder = new BCryptPasswordEncoder()
        def hash = encoder.encode("password123")
        jdbcClient.sql("""
            INSERT INTO ps_user (username, password_hash, must_change_password, enabled)
            VALUES (:username, :hash, false, true)
        """)
                .param("username", "sectest")
                .param("hash", hash)
                .update()

        when: "ein POST auf /login mit den korrekten Zugangsdaten und CSRF-Token gesendet wird"
        def result = mockMvc.perform(
                post("/login")
                        .param("username", "sectest")
                        .param("password", "password123")
                        .with(csrf())
        )

        then: "Spring Security leitet weiter – jedoch nicht zu /login?error (Authentifizierung erfolgreich)"
        def response = result.andExpect(status().is3xxRedirection()).andReturn().getResponse()
        !response.getRedirectedUrl().contains("/login?error")
    }
}
