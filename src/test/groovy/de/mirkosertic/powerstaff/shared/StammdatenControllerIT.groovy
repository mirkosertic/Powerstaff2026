package de.mirkosertic.powerstaff.shared

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@WithMockUser
class StammdatenControllerIT extends AbstractContainerBaseIT {

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
        jdbcClient.sql("DELETE FROM historytype WHERE description LIKE 'IT-Ctrl%'").update()
        jdbcClient.sql("DELETE FROM project_position_status WHERE description LIKE 'IT-Ctrl%'").update()
        jdbcClient.sql("DELETE FROM tags WHERE tagname LIKE 'IT-Ctrl%'").update()
    }

    // -------------------------------------------------------------------------
    // /admin redirect
    // -------------------------------------------------------------------------

    def "GET /admin leitet auf /admin/historientypen um"() {
        when:
        def result = mockMvc.perform(get("/admin"))

        then:
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/admin/historientypen"))
    }

    // -------------------------------------------------------------------------
    // Historientypen – GET
    // -------------------------------------------------------------------------

    def "GET /admin/historientypen liefert 200 und rendert das Template fehlerfrei"() {
        when:
        def result = mockMvc.perform(get("/admin/historientypen"))

        then:
        result.andExpect(status().isOk())
              .andExpect(view().name("admin/historientypen"))
    }

    // -------------------------------------------------------------------------
    // Historientypen – POST Neuanlage
    // -------------------------------------------------------------------------

    def "POST /admin/historientypen legt Typ an und redirectet"() {
        when:
        def result = mockMvc.perform(
            post("/admin/historientypen")
                .param("description", "IT-Ctrl Besuch")
                .with(csrf())
        )

        then:
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/admin/historientypen"))

        and: "der Eintrag existiert in der DB"
        jdbcClient.sql("SELECT COUNT(*) FROM historytype WHERE description = 'IT-Ctrl Besuch'")
                  .query(Long).single() == 1L
    }

    // -------------------------------------------------------------------------
    // Historientypen – POST Update
    // -------------------------------------------------------------------------

    def "POST /admin/historientypen/{id} aktualisiert und redirectet"() {
        given: "ein existierender Eintrag"
        jdbcClient.sql("INSERT INTO historytype (description) VALUES ('IT-Ctrl Original')").update()
        def id = jdbcClient.sql("SELECT id FROM historytype WHERE description = 'IT-Ctrl Original'")
                           .query(Long).single()

        when:
        def result = mockMvc.perform(
            post("/admin/historientypen/$id")
                .param("description", "IT-Ctrl Geaendert")
                .with(csrf())
        )

        then:
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/admin/historientypen"))

        and:
        jdbcClient.sql("SELECT description FROM historytype WHERE id = :id").param("id", id)
                  .query(String).single() == "IT-Ctrl Geaendert"

        cleanup:
        jdbcClient.sql("DELETE FROM historytype WHERE id = :id").param("id", id).update()
    }

    // -------------------------------------------------------------------------
    // Positionsstatus – GET
    // -------------------------------------------------------------------------

    def "GET /admin/positionsstatus liefert 200 und rendert das Template fehlerfrei"() {
        when:
        def result = mockMvc.perform(get("/admin/positionsstatus"))

        then:
        result.andExpect(status().isOk())
              .andExpect(view().name("admin/positionsstatus"))
    }

    // -------------------------------------------------------------------------
    // Positionsstatus – POST Neuanlage
    // -------------------------------------------------------------------------

    def "POST /admin/positionsstatus legt Status an und redirectet"() {
        when:
        def result = mockMvc.perform(
            post("/admin/positionsstatus")
                .param("description", "IT-Ctrl Status")
                .param("color", "#d1fae5")
                .param("colorText", "#065f46")
                .with(csrf())
        )

        then:
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/admin/positionsstatus"))

        and:
        jdbcClient.sql("SELECT COUNT(*) FROM project_position_status WHERE description = 'IT-Ctrl Status'")
                  .query(Long).single() == 1L

        cleanup:
        jdbcClient.sql("DELETE FROM project_position_status WHERE description = 'IT-Ctrl Status'").update()
    }

    // -------------------------------------------------------------------------
    // Tags – GET
    // -------------------------------------------------------------------------

    def "GET /admin/tags liefert 200 und rendert das Template fehlerfrei"() {
        when:
        def result = mockMvc.perform(get("/admin/tags"))

        then:
        result.andExpect(status().isOk())
              .andExpect(view().name("admin/tags"))
    }

    // -------------------------------------------------------------------------
    // Tags – POST Neuanlage
    // -------------------------------------------------------------------------

    def "POST /admin/tags legt Tag an und redirectet"() {
        when:
        def result = mockMvc.perform(
            post("/admin/tags")
                .param("tagname", "IT-Ctrl Kotlin")
                .param("tagType", "SCHWERPUNKT")
                .with(csrf())
        )

        then:
        result.andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/admin/tags"))

        and:
        jdbcClient.sql("SELECT COUNT(*) FROM tags WHERE tagname = 'IT-Ctrl Kotlin'")
                  .query(Long).single() == 1L
    }

    // -------------------------------------------------------------------------
    // Tags – DELETE (AJAX)
    // -------------------------------------------------------------------------

    def "DELETE /admin/tags/{id} loescht und gibt JSON {ok:true} zurueck"() {
        given:
        jdbcClient.sql("INSERT INTO tags (tagname, type) VALUES ('IT-Ctrl Delete', 'TYP')").update()
        def id = jdbcClient.sql("SELECT id FROM tags WHERE tagname = 'IT-Ctrl Delete'")
                           .query(Long).single()

        when:
        def result = mockMvc.perform(
            delete("/admin/tags/$id").with(csrf())
        )

        then:
        result.andExpect(status().isOk())
              .andExpect(content().json('{"ok":true}'))

        and:
        jdbcClient.sql("SELECT COUNT(*) FROM tags WHERE id = :id").param("id", id)
                  .query(Long).single() == 0L
    }
}
