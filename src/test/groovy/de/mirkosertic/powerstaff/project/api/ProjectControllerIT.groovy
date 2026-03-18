package de.mirkosertic.powerstaff.project.api

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.project.command.BothFKsException
import de.mirkosertic.powerstaff.project.command.Project
import de.mirkosertic.powerstaff.project.command.ProjectCommandService
import de.mirkosertic.powerstaff.project.command.RememberedProjectService
import de.mirkosertic.powerstaff.project.query.ProjectDetailView
import de.mirkosertic.powerstaff.project.query.ProjectHistoryQueryService
import de.mirkosertic.powerstaff.project.query.ProjectPositionQueryService
import de.mirkosertic.powerstaff.project.query.ProjectQueryService
import de.mirkosertic.powerstaff.project.query.ProjectSearchResult
import de.mirkosertic.powerstaff.shared.query.ProjectPositionStatusQueryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import java.time.LocalDateTime

import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.anyInt
import static org.mockito.ArgumentMatchers.anyLong
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.doNothing
import static org.mockito.Mockito.doThrow
import static org.mockito.Mockito.when
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ProjectControllerIT extends AbstractContainerBaseIT {

    @Autowired
    WebApplicationContext wac

    MockMvc mockMvc

    @MockitoBean
    ProjectCommandService commandService

    @MockitoBean
    ProjectQueryService queryService

    @MockitoBean
    ProjectHistoryQueryService historyQueryService

    @MockitoBean
    ProjectPositionQueryService positionQueryService

    @MockitoBean
    ProjectPositionStatusQueryService statusQueryService

    @MockitoBean
    RememberedProjectService rememberedProjectService

    def setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        def testProject = new Project()
        testProject.id = 42L
        testProject.dbVersion = 0L
        testProject.projectNumber = 'PRJ-2026-001'
        testProject.status = 1
        testProject.visibleOnWebSite = false

        def detailView = new ProjectDetailView(
                42L, 0L,
                LocalDateTime.now(), 'system',
                LocalDateTime.now(), 'system',
                'PRJ-2026-001', null, null, null, 1, false,
                'Test Projekt', null, null, null, null, null, null, null, null
        )

        when(commandService.findById(42L)).thenReturn(Optional.of(testProject))
        when(commandService.save(any())).thenReturn(testProject)

        when(queryService.findFirst()).thenReturn(Optional.of(detailView))
        when(queryService.findLast()).thenReturn(Optional.of(detailView))
        when(queryService.findPrevious(anyLong())).thenReturn(Optional.empty())
        when(queryService.findNext(anyLong())).thenReturn(Optional.empty())
        when(queryService.findById(42L)).thenReturn(Optional.of(detailView))
        when(queryService.search(any(), anyInt(), anyInt())).thenReturn([
                new ProjectSearchResult(1L, 'PRJ-2026-001', 'Test Projekt', null, null, 1, null)
        ])
        when(queryService.countSearch(any())).thenReturn(1L)

        when(historyQueryService.findByProjectId(anyLong())).thenReturn([])
        when(positionQueryService.findByProjectId(anyLong(), any(), any())).thenReturn([])
        when(statusQueryService.findAll()).thenReturn([])

        when(rememberedProjectService.get(anyString())).thenReturn(Optional.empty())
        doNothing().when(rememberedProjectService).set(anyString(), anyLong())
        doNothing().when(rememberedProjectService).clear(anyString())
    }

    def "GET /project ohne gemerktes Projekt zeigt leere Maske (200)"() {
        when:
        def result = mockMvc.perform(get('/project').with(user('testuser')))

        then:
        result.andExpect(status().isOk())
              .andExpect(view().name('project/form'))
    }

    def "GET /project mit gemerktem Projekt leitet auf dieses Projekt weiter (302)"() {
        given:
        when(rememberedProjectService.get('testuser')).thenReturn(Optional.of(42L))

        when:
        def result = mockMvc.perform(get('/project').with(user('testuser')))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "GET /project/{id} liefert HTTP 200 und rendert Template"() {
        when:
        def result = mockMvc.perform(get('/project/42').with(user('testuser')))

        then:
        result.andExpect(status().isOk())
              .andExpect(view().name('project/form'))
    }

    def "GET /project/new liefert HTTP 200 und rendert leeres Formular"() {
        when:
        def result = mockMvc.perform(get('/project/new').with(user('testuser')))

        then:
        result.andExpect(status().isOk())
              .andExpect(view().name('project/form'))
    }

    def "GET /project/new-from-kunde/{kundeId} liefert HTTP 200"() {
        when:
        def result = mockMvc.perform(get('/project/new-from-kunde/10').with(user('testuser')))

        then:
        result.andExpect(status().isOk())
              .andExpect(view().name('project/form'))
    }

    def "GET /project/new-from-partner/{partnerId} liefert HTTP 200"() {
        when:
        def result = mockMvc.perform(get('/project/new-from-partner/20').with(user('testuser')))

        then:
        result.andExpect(status().isOk())
              .andExpect(view().name('project/form'))
    }

    def "GET /project/first leitet auf erstes Projekt weiter (302)"() {
        when:
        def result = mockMvc.perform(get('/project/first').with(user('testuser')))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "GET /project/last leitet auf letztes Projekt weiter (302)"() {
        when:
        def result = mockMvc.perform(get('/project/last').with(user('testuser')))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "GET /project/previous/{id} ohne Vorgaenger leitet auf /project weiter"() {
        when:
        def result = mockMvc.perform(get('/project/previous/42').with(user('testuser')))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "GET /project/next/{id} ohne Nachfolger leitet auf /project weiter"() {
        when:
        def result = mockMvc.perform(get('/project/next/42').with(user('testuser')))

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "POST /project/save Erfolg leitet auf Projektseite weiter (302)"() {
        when:
        def result = mockMvc.perform(
                post('/project/save')
                        .with(user('testuser'))
                        .with(csrf())
                        .param('projectNumber', 'PRJ-2026-002')
                        .param('status', '1')
        )

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "POST /project/save OptimisticLocking gibt 409 JSON zurueck"() {
        given:
        when(commandService.save(any())).thenThrow(new OptimisticLockingFailureException('conflict'))

        when:
        def result = mockMvc.perform(
                post('/project/save')
                        .with(user('testuser'))
                        .with(csrf())
                        .param('projectNumber', 'PRJ-2026-002')
                        .param('status', '1')
        )

        then:
        result.andExpect(status().isConflict())
        result.andExpect(jsonPath('$.conflict').value(true))
    }

    def "POST /project/save BothFKs gibt 409 JSON zurueck"() {
        given:
        when(commandService.save(any())).thenThrow(new BothFKsException())

        when:
        def result = mockMvc.perform(
                post('/project/save')
                        .with(user('testuser'))
                        .with(csrf())
                        .param('projectNumber', 'PRJ-2026-002')
                        .param('status', '1')
        )

        then:
        result.andExpect(status().isConflict())
        result.andExpect(jsonPath('$.bothFks').value(true))
    }

    def "POST /project/delete/{id} loescht und leitet auf /project weiter"() {
        when:
        def result = mockMvc.perform(
                post('/project/delete/42')
                        .with(user('testuser'))
                        .with(csrf())
        )

        then:
        result.andExpect(status().is3xxRedirection())
    }

    def "POST /project/search liefert Suchergebnis-Fragment"() {
        when:
        def result = mockMvc.perform(
                post('/project/search')
                        .with(user('testuser'))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        )

        then:
        result.andExpect(status().isOk())
              .andExpect(view().name('project/search-results :: results'))
    }
}
