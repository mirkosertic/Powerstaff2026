package de.mirkosertic.powerstaff.partner.api

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.partner.command.PartnerContact
import de.mirkosertic.powerstaff.partner.command.PartnerContactCommandService
import de.mirkosertic.powerstaff.partner.query.PartnerContactView
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class PartnerContactControllerIT extends AbstractContainerBaseIT {

    @Autowired
    WebApplicationContext wac

    MockMvc mockMvc

    @MockitoBean
    PartnerContactCommandService contactCommandService

    @MockitoBean
    PartnerQueryService queryService

    def setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        def savedContact = new PartnerContact()
        savedContact.id = 10L
        savedContact.type = "EMAIL"
        savedContact.value = "test@example.com"
        savedContact.partnerId = 42L

        when(contactCommandService.save(any())).thenReturn(savedContact)
        doNothing().when(contactCommandService).deleteById(anyLong())
        when(queryService.findContactsByPartner(anyLong())).thenReturn([
                new PartnerContactView(10L, "EMAIL", "test@example.com", 42L)
        ])
    }

    def "POST /partner/{partnerId}/contacts speichert Kontakt und gibt 200 JSON zurueck"() {
        when:
        def result = mockMvc.perform(
                post("/partner/42/contacts")
                        .with(csrf())
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content('{"type":"EMAIL","value":"test@example.com"}'))

        then:
        result.andExpect(status().isOk())
              .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath('$.ok').value(true))
    }

    def "DELETE /partner/{partnerId}/contacts/{contactId} loescht Kontakt und gibt 200 JSON zurueck"() {
        when:
        def result = mockMvc.perform(
                delete("/partner/42/contacts/10")
                        .with(csrf())
                        .with(user("testuser")))

        then:
        result.andExpect(status().isOk())
              .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath('$.ok').value(true))
    }
}
