package de.mirkosertic.powerstaff.project.api;

import de.mirkosertic.powerstaff.project.command.FreelancerAlreadyAssignedException;
import de.mirkosertic.powerstaff.project.command.BothFKsException;
import de.mirkosertic.powerstaff.project.command.Project;
import de.mirkosertic.powerstaff.project.command.ProjectCommandService;
import de.mirkosertic.powerstaff.project.command.ProjectPositionCommandService;
import de.mirkosertic.powerstaff.project.command.RememberedProjectService;
import de.mirkosertic.powerstaff.project.query.ProjectHistoryQueryService;
import de.mirkosertic.powerstaff.project.query.ProjectPositionQueryService;
import de.mirkosertic.powerstaff.project.query.ProjectQueryService;
import de.mirkosertic.powerstaff.project.command.RememberedProjectInfo;
import de.mirkosertic.powerstaff.project.query.ProjectSearchCriteria;
import de.mirkosertic.powerstaff.shared.query.ProjectPositionStatusQueryService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/project")
public class ProjectController {

    private static final int PAGE_SIZE = 20;
    private static final DateTimeFormatter AUDIT_DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final ProjectCommandService commandService;
    private final ProjectQueryService queryService;
    private final ProjectHistoryQueryService historyQueryService;
    private final ProjectPositionQueryService positionQueryService;
    private final ProjectPositionCommandService positionCommandService;
    private final ProjectPositionStatusQueryService statusQueryService;
    private final RememberedProjectService rememberedProjectService;

    public ProjectController(ProjectCommandService commandService,
                             ProjectQueryService queryService,
                             ProjectHistoryQueryService historyQueryService,
                             ProjectPositionQueryService positionQueryService,
                             ProjectPositionCommandService positionCommandService,
                             ProjectPositionStatusQueryService statusQueryService,
                             RememberedProjectService rememberedProjectService) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.historyQueryService = historyQueryService;
        this.positionQueryService = positionQueryService;
        this.positionCommandService = positionCommandService;
        this.statusQueryService = statusQueryService;
        this.rememberedProjectService = rememberedProjectService;
    }

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------

    @GetMapping
    public String index(Principal principal, Model model) {
        var rememberedId = rememberedProjectService.get(principal.getName());
        if (rememberedId.isPresent() && commandService.findById(rememberedId.get()).isPresent()) {
            return "redirect:/project/" + rememberedId.get();
        }
        populateBlankModel(model, new Project(), principal);
        return "project/form";
    }

    @GetMapping("/first")
    public String first(Principal principal) {
        return queryService.findFirst()
                .map(p -> setAndRedirect(principal, p.id()))
                .orElse("redirect:/project");
    }

    @GetMapping("/last")
    public String last(Principal principal) {
        return queryService.findLast()
                .map(p -> setAndRedirect(principal, p.id()))
                .orElse("redirect:/project");
    }

    @GetMapping("/previous/{id}")
    public String previous(@PathVariable long id, Principal principal) {
        return queryService.findPrevious(id)
                .map(p -> setAndRedirect(principal, p.id()))
                .orElse("redirect:/project");
    }

    @GetMapping("/next/{id}")
    public String next(@PathVariable long id, Principal principal) {
        return queryService.findNext(id)
                .map(p -> setAndRedirect(principal, p.id()))
                .orElse("redirect:/project");
    }

    private String setAndRedirect(Principal principal, long id) {
        rememberedProjectService.set(principal.getName(), id);
        return "redirect:/project/" + id;
    }

    // -------------------------------------------------------------------------
    // Anzeigen / Neuanlage
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    public String show(@PathVariable long id, Principal principal, Model model) {
        var project = commandService.findById(id).orElseThrow();
        rememberedProjectService.set(principal.getName(), id);
        populateModel(model, project, id, principal);
        return "project/form";
    }

    @GetMapping("/new")
    public String newForm(Principal principal, Model model) {
        populateBlankModel(model, new Project(), principal);
        return "project/form";
    }

    @GetMapping("/new-from-kunde/{kundeId}")
    public String newFromKunde(@PathVariable Long kundeId, Principal principal, Model model) {
        var project = new Project();
        project.setCustomerId(kundeId);
        populateBlankModel(model, project, principal);
        return "project/form";
    }

    @GetMapping("/new-from-partner/{partnerId}")
    public String newFromPartner(@PathVariable Long partnerId, Principal principal, Model model) {
        var project = new Project();
        project.setPartnerId(partnerId);
        populateBlankModel(model, project, principal);
        return "project/form";
    }

    private void populateModel(Model model, Project project, Long projectId, Principal principal) {
        model.addAttribute("project", project);
        model.addAttribute("history", projectId != null ? historyQueryService.findByProjectId(projectId) : List.of());
        model.addAttribute("positions", projectId != null ? positionQueryService.findByProjectId(projectId, null, null) : List.of());
        model.addAttribute("positionStatuses", statusQueryService.findAll());
        model.addAttribute("rememberedProject", buildRememberedProjectInfo(principal));
        model.addAttribute("activePage", "project");
        model.addAttribute("auditInfo", buildAuditInfo(
                projectId,
                project.getCreationDate(), project.getCreationUser(),
                project.getChangedDate(), project.getChangedUser()));
    }

    private String buildAuditInfo(Long id, LocalDateTime creationDate, String creationUser,
                                   LocalDateTime changedDate, String changedUser) {
        if (id == null) return "Neu, noch nicht gespeichert";
        String created = (creationDate != null ? creationDate.format(AUDIT_DATE_FMT) : "?")
                       + " " + (creationUser != null ? creationUser : "?");
        String result = "Erfasst: " + created;
        if (changedDate != null && !changedDate.equals(creationDate)) {
            result += "<br>Geändert: "
                   + changedDate.format(AUDIT_DATE_FMT)
                   + " " + (changedUser != null ? changedUser : "?");
        }
        return result;
    }

    private void populateBlankModel(Model model, Project project, Principal principal) {
        populateModel(model, project, null, principal);
    }

    private RememberedProjectInfo buildRememberedProjectInfo(Principal principal) {
        return rememberedProjectService.getRememberedProjectInfo(principal.getName()).orElse(null);
    }

    // -------------------------------------------------------------------------
    // Speichern
    // -------------------------------------------------------------------------

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> save(@ModelAttribute Project project,
                                  Principal principal,
                                  HttpServletResponse response) throws IOException {
        try {
            var saved = commandService.save(project);
            rememberedProjectService.set(principal.getName(), saved.getId());
            response.sendRedirect("/project/" + saved.getId() + "?saved=true");
            return null;
        } catch (OptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("conflict", true));
        } catch (BothFKsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("bothFks", true));
        }
    }

    // -------------------------------------------------------------------------
    // Löschen
    // -------------------------------------------------------------------------

    @PostMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable long id,
                                    Principal principal,
                                    HttpServletResponse response) throws IOException {
        commandService.deleteById(id);
        rememberedProjectService.clear(principal.getName());
        response.sendRedirect("/project");
        return null;
    }

    // -------------------------------------------------------------------------
    // QBE-Suche
    // -------------------------------------------------------------------------

    @GetMapping("/search")
    public String search(@ModelAttribute ProjectSearchCriteria criteria,
                         @RequestParam(required = false, defaultValue = "0") int offset,
                         Model model,
                         HttpServletResponse response) {
        if (offset > 0) {
            var results = queryService.search(criteria, offset, PAGE_SIZE);
            int nextOffset = offset + PAGE_SIZE;
            long total = queryService.countSearch(criteria);
            if (nextOffset < total) {
                response.setHeader("X-Next-Url", buildSearchMoreUrl(criteria, nextOffset));
            }
            model.addAttribute("results", results);
            return "project/search-results :: results";
        }

        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        var results = queryService.search(criteria, 0, PAGE_SIZE);
        long total = queryService.countSearch(criteria);
        model.addAttribute("results", results);
        model.addAttribute("totalCount", total);
        model.addAttribute("criteria", criteria);
        model.addAttribute("sortField", criteria.sortField());
        model.addAttribute("sortDir", criteria.sortDir());
        String nextUrl = results.size() == PAGE_SIZE ? buildSearchMoreUrl(criteria, PAGE_SIZE) : null;
        model.addAttribute("nextUrl", nextUrl);
        model.addAttribute("editSearchUrl", buildEditSearchUrl(criteria));
        return "project/search-page";
    }

    // -------------------------------------------------------------------------
    // Positionen (AJAX)
    // -------------------------------------------------------------------------

    @GetMapping("/{id}/positions")
    @ResponseBody
    public ResponseEntity<?> getPositions(@PathVariable long id,
                                          @RequestParam(required = false) String sortField,
                                          @RequestParam(required = false) String sortDir) {
        return ResponseEntity.ok(positionQueryService.findByProjectId(id, sortField, sortDir));
    }

    record PositionRequest(Long statusId, String konditionen, String kommentar, Long dbVersion) {}

    @PostMapping("/{projectId}/positions/{posId}")
    @ResponseBody
    public ResponseEntity<?> savePosition(@PathVariable long projectId,
                                          @PathVariable long posId,
                                          @RequestBody PositionRequest request) {
        try {
            positionCommandService.updateEditable(posId, request.statusId(), request.konditionen(), request.kommentar(), request.dbVersion());
            return ResponseEntity.ok(positionQueryService.findByProjectId(projectId, null, null));
        } catch (OptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("conflict", true));
        }
    }

    @PostMapping("/{projectId}/positions/{posId}/delete")
    @ResponseBody
    public ResponseEntity<?> deletePosition(@PathVariable long projectId,
                                            @PathVariable long posId) {
        positionCommandService.delete(posId);
        return ResponseEntity.ok(positionQueryService.findByProjectId(projectId, null, null));
    }

    record AssignByIdRequest(Long freelancerId) {}

    @PostMapping("/{projectId}/positions/assign")
    @ResponseBody
    public ResponseEntity<?> assignById(@PathVariable long projectId,
                                        @RequestBody AssignByIdRequest request) {
        if (request.freelancerId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "freelancerId required"));
        }
        try {
            positionCommandService.assignFreelancerToProject(
                    request.freelancerId(), projectId, null, null, null);
            return ResponseEntity.ok(positionQueryService.findByProjectId(projectId, null, null));
        } catch (FreelancerAlreadyAssignedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("alreadyAssigned", true));
        }
    }

    private void appendCriteriaParams(UriComponentsBuilder b, ProjectSearchCriteria c) {
        if (c.projectNumber()    != null) b.queryParam("projectNumber",    c.projectNumber());
        if (c.descriptionShort() != null) b.queryParam("descriptionShort", c.descriptionShort());
        if (c.descriptionLong()  != null) b.queryParam("descriptionLong",  c.descriptionLong());
        if (c.skills()           != null) b.queryParam("skills",           c.skills());
        if (c.workplace()        != null) b.queryParam("workplace",        c.workplace());
        if (c.duration()         != null) b.queryParam("duration",         c.duration());
        if (c.status()           != null) b.queryParam("status",           c.status());
        if (c.debitorNr()        != null) b.queryParam("debitorNr",        c.debitorNr());
        if (c.kreditorNr()       != null) b.queryParam("kreditorNr",       c.kreditorNr());
        if (c.sortField()        != null) b.queryParam("sortField",        c.sortField());
        if (c.sortDir()          != null) b.queryParam("sortDir",          c.sortDir());
    }

    private String buildEditSearchUrl(ProjectSearchCriteria c) {
        var b = UriComponentsBuilder.fromPath("/project/new");
        appendCriteriaParams(b, c);
        return b.encode().build().toUriString();
    }

    private String buildSearchMoreUrl(ProjectSearchCriteria c, int offset) {
        var b = UriComponentsBuilder.fromPath("/project/search").queryParam("offset", offset);
        appendCriteriaParams(b, c);
        return b.encode().build().toUriString();
    }
}
