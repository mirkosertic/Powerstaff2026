package de.mirkosertic.powerstaff.profilesearch

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.profilesearch.command.ProfileSearchCommandService
import de.mirkosertic.powerstaff.profilesearch.query.ProfileSearchQueryService
import de.mirkosertic.powerstaff.project.command.RememberedProjectService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import java.util.Optional

import static org.mockito.ArgumentMatchers.anyLong
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.ArgumentMatchers.isNull
import static org.mockito.Mockito.doNothing
import static org.mockito.Mockito.when
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ProfileSearchControllerIT extends AbstractContainerBaseIT {

    @Autowired
    WebApplicationContext wac

    MockMvc mockMvc

    @MockitoBean
    ProfileSearchCommandService commandService

    @MockitoBean
    ProfileSearchQueryService queryService

    @MockitoBean
    RememberedProjectService rememberedProjectService

    def setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build()
    }

    def "DELETE /profilesearch/chat/{id} liefert JSON mit redirectTo wenn weiterer Chat vorhanden"() {
        given:
        doNothing().when(commandService).deleteChat(anyLong())
        when(queryService.findLatestChatByUser(anyString())).thenReturn(Optional.of(99L))

        when:
        def result = mockMvc.perform(
                delete('/profilesearch/chat/42')
                        .with(user('testuser').roles('USER'))
                        .with(csrf())
        )

        then:
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$.redirectTo').value('/profilesearch/chat/99'))
    }

    def "DELETE /profilesearch/chat/{id} erstellt neuen Chat wenn kein weiterer Chat vorhanden"() {
        given:
        doNothing().when(commandService).deleteChat(anyLong())
        when(queryService.findLatestChatByUser(anyString())).thenReturn(Optional.empty())
        when(rememberedProjectService.get(anyString())).thenReturn(Optional.empty())
        when(commandService.createChat(anyString(), isNull())).thenReturn(77L)

        when:
        def result = mockMvc.perform(
                delete('/profilesearch/chat/42')
                        .with(user('testuser').roles('USER'))
                        .with(csrf())
        )

        then:
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$.redirectTo').value('/profilesearch/chat/77'))
    }
}
