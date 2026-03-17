package de.mirkosertic.powerstaff.partner.api

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.partner.command.PartnerHistory
import de.mirkosertic.powerstaff.partner.command.PartnerHistoryCommandService
import de.mirkosertic.powerstaff.partner.query.PartnerQueryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.anyLong
import static org.mockito.Mockito.doNothing
import static org.mockito.Mockito.when
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class PartnerHistoryControllerIT extends AbstractContainerBaseIT {

    @Autowired
    WebApplicationContext wac

    MockMvc mockMvc

    @MockitoBean
    PartnerHistoryCommandService historyCommandService

    @MockitoBean
    PartnerQueryService queryService

    def setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        def savedHistory = new PartnerHistory()
        savedHistory.id = 5L
        savedHistory.typeId = 1L
        savedHistory.description = "Telefonat geführt"
        savedHistory.partnerId = 42L

        when(historyCommandService.save(any())).thenReturn(savedHistory)
        when(historyCommandService.findById(5L)).thenReturn(Optional.of(savedHistory))
        when(historyCommandService.findById(-999L)).thenReturn(Optional.empty())
        doNothing().when(historyCommandService).deleteById(anyLong())
    }

    def "POST /partner/{partnerId}/history erstellt Eintrag und gibt 200 JSON zurueck"() {
        when:
        def result = mockMvc.perform(
                post("/partner/42/history")
                        .with(csrf())
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content('{"typeId":1,"description":"Telefonat geführt"}'))

        then:
        result.andExpect(status().isOk())
              .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath('$.ok').value(true))
    }

    def "PUT /partner/{partnerId}/history/{historyId} aktualisiert Eintrag und gibt 200 JSON zurueck"() {
        when:
        def result = mockMvc.perform(
                put("/partner/42/history/5")
                        .with(csrf())
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content('{"description":"Telefonat aktualisiert"}'))

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.ok').value(true))
    }

    def "PUT /partner/{partnerId}/history/{historyId} liefert 404 wenn Eintrag nicht existiert"() {
        when:
        def result = mockMvc.perform(
                put("/partner/42/history/-999")
                        .with(csrf())
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content('{"description":"Test"}'))

        then:
        result.andExpect(status().isNotFound())
    }

    def "DELETE /partner/{partnerId}/history/{historyId} loescht Eintrag und gibt 200 JSON zurueck"() {
        when:
        def result = mockMvc.perform(
                delete("/partner/42/history/5")
                        .with(csrf())
                        .with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.ok').value(true))
    }
}
