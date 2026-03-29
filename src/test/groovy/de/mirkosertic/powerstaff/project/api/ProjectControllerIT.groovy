package de.mirkosertic.powerstaff.project.api

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.project.command.BothFKsException
import de.mirkosertic.powerstaff.project.command.FreelancerAlreadyAssignedException
import de.mirkosertic.powerstaff.project.command.Project
import de.mirkosertic.powerstaff.project.command.ProjectCommandService
import de.mirkosertic.powerstaff.project.command.ProjectPositionCommandService
import de.mirkosertic.powerstaff.project.command.RememberedProjectService
import de.mirkosertic.powerstaff.project.query.ProjectDetailView
import de.mirkosertic.powerstaff.project.query.ProjectHistoryQueryService
import de.mirkosertic.powerstaff.project.query.ProjectPositionQueryService
import de.mirkosertic.powerstaff.project.query.ProjectPositionView
import de.mirkosertic.powerstaff.project.command.ProjectPosition
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

import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.not
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
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
    ProjectPositionCommandService positionCommandService

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
        when(rememberedProjectService.getRememberedProjectInfo(anyString())).thenReturn(Optional.empty())
        doNothing().when(rememberedProjectService).set(anyString(), anyLong())
        doNothing().when(rememberedProjectService).clear(anyString())

        doNothing().when(positionCommandService).assignFreelancerToProject(anyLong(), anyLong(), any(), any(), any())
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

    def "GET /project/search mit projectNumber-Parameter liefert search-page (200)"() {
        when:
        def result = mockMvc.perform(
                get('/project/search')
                        .with(user('testuser'))
                        .param('projectNumber', '2026')
        )

        then:
        result.andExpect(status().isOk())
              .andExpect(view().name('project/search-page'))
    }

    def "GET /project/search ohne Parameter liefert 200 und search-page Template"() {
        when:
        def result = mockMvc.perform(
                get('/project/search')
                        .with(user('testuser')))

        then:
        result.andExpect(status().isOk())
              .andExpect(view().name('project/search-page'))
    }

    def "GET /project/search setzt Cache-Control Header no-store"() {
        when:
        def result = mockMvc.perform(
                get('/project/search')
                        .with(user('testuser')))

        then:
        result.andExpect(status().isOk())
              .andExpect(header().string('Cache-Control', containsString('no-store')))
    }

    def "GET /project/search mit projectTitle-Parameter liefert 200 und kein Exception"() {
        when:
        def result = mockMvc.perform(
                get('/project/search')
                        .param('projectTitle', 'Test')
                        .param('sortField', 'projectTitle')
                        .param('sortDir', 'asc')
                        .with(user('testuser')))

        then:
        result.andExpect(status().isOk())
              .andExpect(header().string('Cache-Control', containsString('no-store')))
              .andExpect(content().string(not(containsString('Exception'))))
    }

    def "GET /project/search rendert HTML-Seite ohne Exception"() {
        when:
        def result = mockMvc.perform(
                get('/project/search')
                        .with(user('testuser')))

        then:
        result.andExpect(status().isOk())
              .andExpect(content().string(not(containsString('Exception'))))
              .andExpect(content().string(not(containsString('Whitelabel Error'))))
    }

    def "GET /project/search mit offset-Parameter gibt Fragment zurueck (200)"() {
        when:
        def result = mockMvc.perform(
                get('/project/search')
                        .param('offset', '20')
                        .with(user('testuser')))

        then:
        result.andExpect(status().isOk())
    }

    // -------------------------------------------------------------------------
    // Freiberufler per ID zuordnen (assign)
    // -------------------------------------------------------------------------

    def "POST /project/{id}/positions/assign ohne freelancerId gibt 400 zurueck"() {
        when:
        def result = mockMvc.perform(
                post('/project/42/positions/assign')
                        .with(user('testuser'))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content('{"freelancerId":null}'))

        then:
        result.andExpect(status().isBadRequest())
    }

    def "POST /project/{id}/positions/assign Erfolg gibt Positions-Liste zurueck (200)"() {
        when:
        def result = mockMvc.perform(
                post('/project/42/positions/assign')
                        .with(user('testuser'))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content('{"freelancerId":77}'))

        then:
        result.andExpect(status().isOk())
    }

    def "POST /project/{id}/positions/assign bei bereits zugeordnetem Freiberufler gibt 409 zurueck"() {
        given:
        doThrow(new FreelancerAlreadyAssignedException(77L, 42L))
                .when(positionCommandService).assignFreelancerToProject(anyLong(), anyLong(), any(), any(), any())

        when:
        def result = mockMvc.perform(
                post('/project/42/positions/assign')
                        .with(user('testuser'))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content('{"freelancerId":77}'))

        then:
        result.andExpect(status().isConflict())
              .andExpect(jsonPath('$.alreadyAssigned').value(true))
    }

    // -------------------------------------------------------------------------
    // Thymeleaf-Rendering HTML-Inhalte pruefen
    // -------------------------------------------------------------------------

    def "GET /project/{id} rendert HTML mit Formular ohne Exception"() {
        when:
        def result = mockMvc.perform(get('/project/42').with(user('testuser')))

        then:
        result.andExpect(status().isOk())
              .andExpect(content().string(containsString('<form')))
              .andExpect(content().string(not(containsString('Exception'))))
              .andExpect(content().string(not(containsString('Whitelabel Error'))))
    }

    def "GET /project/new rendert HTML mit leerem Formular ohne Exception"() {
        when:
        def result = mockMvc.perform(get('/project/new').with(user('testuser')))

        then:
        result.andExpect(status().isOk())
              .andExpect(content().string(containsString('<form')))
              .andExpect(content().string(not(containsString('Exception'))))
    }

    def "GET /project/search rendert HTML-Seite mit Treffern ohne Exception"() {
        when:
        def result = mockMvc.perform(
                get('/project/search')
                        .with(user('testuser'))
                        .param('projectNumber', '2026'))

        then:
        result.andExpect(status().isOk())
              .andExpect(content().string(not(containsString('Exception'))))
    }

    def "GET /project/new zeigt Speichern-Button fuer neues Projekt"() {
        when:
        def result = mockMvc.perform(get('/project/new').with(user('testuser')))

        then:
        result.andExpect(status().isOk())
              .andExpect(content().string(containsString('form="project-form"')))
    }

    def "GET /project/{id} zeigt Speichern-Button fuer bestehendes Projekt"() {
        when:
        def result = mockMvc.perform(get('/project/42').with(user('testuser')))

        then:
        result.andExpect(status().isOk())
              .andExpect(content().string(containsString('form="project-form"')))
    }

    def "GET /project/new btn-qbe-search ist vorhanden aber versteckt"() {
        when:
        def result = mockMvc.perform(get('/project/new').with(user('testuser')))

        then:
        result.andExpect(status().isOk())
              .andExpect(content().string(containsString('id="btn-qbe-search"')))
              .andExpect(content().string(not(containsString('In Datenbank suchen'))))
    }

    // -------------------------------------------------------------------------
    // Positionen – AJAX-Endpunkte
    // -------------------------------------------------------------------------

    def "GET /project/{id}/positions liefert Positions-Liste als JSON (200)"() {
        given:
        def pos = new ProjectPositionView(1L, 0L, 10L, 'MK-01', 'Mustermann', 'Max',
                1L, 'Offen', '#00aa00', '#ffffff', '500 EUR', 'Kommentar', false)
        when(positionQueryService.findByProjectId(42L, null, null)).thenReturn([pos])

        when:
        def result = mockMvc.perform(
                get('/project/42/positions')
                        .with(user('testuser')))

        then:
        result.andExpect(status().isOk())
    }

    def "GET /project/{id}/positions mit sortField und sortDir gibt sortierte Liste (200)"() {
        when:
        def result = mockMvc.perform(
                get('/project/42/positions')
                        .param('sortField', 'name1')
                        .param('sortDir', 'asc')
                        .with(user('testuser')))

        then:
        result.andExpect(status().isOk())
    }

    def "POST /project/{projectId}/positions/{posId} speichert Position und gibt Liste zurueck (200)"() {
        given:
        when(positionCommandService.updateEditable(anyLong(), any(), any(), any(), any()))
                .thenReturn(new ProjectPosition())

        when:
        def result = mockMvc.perform(
                post('/project/42/positions/1')
                        .with(user('testuser'))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content('{"statusId":1,"konditionen":"600 EUR/Tag","kommentar":"Test","dbVersion":0}'))

        then:
        result.andExpect(status().isOk())
    }

    def "POST /project/{projectId}/positions/{posId} OptimisticLocking gibt 409 zurueck"() {
        given:
        doThrow(new OptimisticLockingFailureException('conflict'))
                .when(positionCommandService).updateEditable(anyLong(), any(), any(), any(), any())

        when:
        def result = mockMvc.perform(
                post('/project/42/positions/1')
                        .with(user('testuser'))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content('{"statusId":1,"konditionen":"600 EUR/Tag","kommentar":"Test","dbVersion":0}'))

        then:
        result.andExpect(status().isConflict())
              .andExpect(jsonPath('$.conflict').value(true))
    }

    def "POST /project/{projectId}/positions/{posId}/delete loescht Position und gibt Liste zurueck (200)"() {
        given: "delete ist void – Default-Mock macht bereits nichts"
        when:
        def result = mockMvc.perform(
                post('/project/42/positions/1/delete')
                        .with(user('testuser'))
                        .with(csrf()))

        then:
        result.andExpect(status().isOk())
    }

    // -------------------------------------------------------------------------
    // buildSearchMoreUrl – X-Next-Url Header bei Fragment-Modus
    // -------------------------------------------------------------------------

    def "buildSearchMoreUrl mit offset=20 setzt X-Next-Url Header wenn weitere Treffer vorhanden"() {
        given:
        when(queryService.search(any(), anyInt(), anyInt())).thenReturn(
                (1..20).collect { i ->
                    new ProjectSearchResult(i as Long, "PRJ-$i", "Projekt $i", null, null, 1, null)
                }
        )
        when(queryService.countSearch(any())).thenReturn(100L)

        when:
        def result = mockMvc.perform(
                get('/project/search')
                        .param('offset', '20')
                        .param('projectNumber', '2026')
                        .param('skills', 'Java')
                        .with(user('testuser')))

        then:
        result.andExpect(status().isOk())
              .andExpect(header().string('X-Next-Url', containsString('/project/search')))
              .andExpect(header().string('X-Next-Url', containsString('offset=40')))
              .andExpect(header().string('X-Next-Url', containsString('projectNumber=')))
    }

    def "buildSearchMoreUrl kein X-Next-Url Header wenn keine weiteren Treffer"() {
        given:
        when(queryService.search(any(), anyInt(), anyInt())).thenReturn([
                new ProjectSearchResult(1L, 'PRJ-001', 'Projekt 1', null, null, 1, null)
        ])
        when(queryService.countSearch(any())).thenReturn(1L)

        when:
        def result = mockMvc.perform(
                get('/project/search')
                        .param('offset', '20')
                        .with(user('testuser')))

        then:
        result.andExpect(status().isOk())
        result.andExpect(header().doesNotExist('X-Next-Url'))
    }

    // -------------------------------------------------------------------------
    // buildEditSearchUrl – URL enthaelt alle gesetzten Kriterien
    // -------------------------------------------------------------------------

    def "buildEditSearchUrl enthaelt alle gesetzten Kriterien in der Seite"() {
        when:
        def result = mockMvc.perform(
                get('/project/search')
                        .with(user('testuser'))
                        .param('projectNumber', '2026')
                        .param('skills', 'Java')
                        .param('workplace', 'Berlin')
                        .param('sortField', 'project_number')
                        .param('sortDir', 'asc'))

        then:
        result.andExpect(status().isOk())
              .andExpect(content().string(containsString('/project/new')))
              .andExpect(content().string(containsString('projectNumber=')))
              .andExpect(content().string(containsString('skills=')))
              .andExpect(content().string(containsString('workplace=')))
    }

    // -------------------------------------------------------------------------
    // Thymeleaf-Rendering HTML-Inhalte pruefen
    // -------------------------------------------------------------------------

    def "GET /project/{id} rendert Positions-Buttons mit onclick"() {
        given:
        def pos = new ProjectPositionView(1L, 0L, 10L, 'MK-01', 'Mustermann', 'Max',
                1L, 'Offen', '#00aa00', '#ffffff', '500 EUR', 'Kommentar', false)
        when(positionQueryService.findByProjectId(42L, null, null)).thenReturn([pos])

        when:
        def result = mockMvc.perform(get('/project/42').with(user('testuser')))

        then:
        result.andExpect(status().isOk())
              .andExpect(content().string(containsString('onclick="openEditPositionModal(this)"')))
              .andExpect(content().string(containsString('onclick="openDeletePositionModal(this)"')))
    }
}
