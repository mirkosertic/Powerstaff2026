package de.mirkosertic.powerstaff

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Integrationstests fuer das Rendern der Thymeleaf-Basis-Fragmente und Templates.
 *
 * Prueft, dass:
 * - GET /login liefert HTTP 200 (Template parsed und rendert fehlerfrei)
 * - GET /      liefert 302 Redirect (laeuft ueber HomeController -> /freelancer)
 *
 * Spring Boot 4.0: @AutoConfigureMockMvc entfernt; MockMvc wird manuell aufgebaut.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class FragmentRenderingIT extends AbstractContainerBaseIT {

    @Autowired
    WebApplicationContext wac

    MockMvc mockMvc

    def setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build()
    }

    def "GET /login rendert das Login-Template ohne Fehler und liefert HTTP 200"() {
        when: "die Login-Seite ohne Authentifizierung aufgerufen wird"
        def result = mockMvc.perform(get("/login"))

        then: "das Thymeleaf-Template wird fehlerfrei gerendert und mit 200 ausgeliefert"
        result.andExpect(status().isOk())
    }

    def "GET / leitet unauthentifiziert zur Login-Seite weiter (302)"() {
        when: "die Wurzel-URL ohne Authentifizierung aufgerufen wird"
        def result = mockMvc.perform(get("/"))

        then: "Spring Security leitet auf /login weiter"
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/login"))
    }
}
