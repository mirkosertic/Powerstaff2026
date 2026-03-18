package de.mirkosertic.powerstaff.project.api

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.project.command.ProjectHistory
import de.mirkosertic.powerstaff.project.command.ProjectHistoryCommandService
import de.mirkosertic.powerstaff.project.query.ProjectHistoryQueryService
import de.mirkosertic.powerstaff.project.query.ProjectHistoryView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import java.time.LocalDateTime

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ProjectHistoryControllerIT extends AbstractContainerBaseIT {

    @Autowired
    WebApplicationContext wac

    MockMvc mockMvc

    @MockitoBean
    ProjectHistoryCommandService commandService

    @MockitoBean
    ProjectHistoryQueryService queryService

    def setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build()
    }

    def "POST /project/{id}/history erstellt Eintrag und liefert Liste"() {
        given:
        def view = new ProjectHistoryView(1L, LocalDateTime.now(), 'user', null, null, 'Erster Kontakt', 42L)
        when(commandService.save(any(ProjectHistory))).thenReturn(new ProjectHistory())
        when(queryService.findByProjectId(42L)).thenReturn([view])

        when:
        def result = mockMvc.perform(
                post('/project/42/history')
                        .with(user('testuser').roles('USER'))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content('{"description":"Erster Kontakt"}')
        )

        then:
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$[0].description').value('Erster Kontakt'))
    }

    def "PUT /project/{id}/history/{hId} aktualisiert Eintrag"() {
        given:
        when(commandService.save(any(ProjectHistory))).thenReturn(new ProjectHistory())

        when:
        def result = mockMvc.perform(
                put('/project/42/history/5')
                        .with(user('testuser').roles('USER'))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content('{"description":"Geaendert"}')
        )

        then:
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$.ok').value(true))
    }

    def "DELETE /project/{id}/history/{hId} loescht Eintrag"() {
        given:
        doNothing().when(commandService).delete(anyLong())

        when:
        def result = mockMvc.perform(
                delete('/project/42/history/5')
                        .with(user('testuser').roles('USER'))
                        .with(csrf())
        )

        then:
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$.ok').value(true))
    }
}
