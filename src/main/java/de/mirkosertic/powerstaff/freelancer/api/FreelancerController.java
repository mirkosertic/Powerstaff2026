package de.mirkosertic.powerstaff.freelancer.api;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import de.mirkosertic.powerstaff.freelancer.command.DuplicateCodeException;
import de.mirkosertic.powerstaff.freelancer.command.Freelancer;
import de.mirkosertic.powerstaff.freelancer.command.FreelancerCommandService;
import de.mirkosertic.powerstaff.freelancer.command.FreelancerContactEntry;
import de.mirkosertic.powerstaff.freelancer.command.FreelancerHistoryEntry;
import de.mirkosertic.powerstaff.freelancer.command.FreelancerHasPositionsException;
import de.mirkosertic.powerstaff.freelancer.command.FreelancerTagCommandService;
import de.mirkosertic.powerstaff.freelancer.command.FreelancerTagEntry;
import de.mirkosertic.powerstaff.freelancer.query.FreelancerQueryService;
import de.mirkosertic.powerstaff.freelancer.query.FreelancerSearchCriteria;
import de.mirkosertic.powerstaff.freelancer.query.TagInfo;
import de.mirkosertic.powerstaff.project.command.FreelancerAlreadyAssignedException;
import de.mirkosertic.powerstaff.project.command.ProjectPositionCommandService;
import de.mirkosertic.powerstaff.project.command.RememberedProjectInfo;
import de.mirkosertic.powerstaff.project.command.RememberedProjectService;
import de.mirkosertic.powerstaff.shared.TagType;
import de.mirkosertic.powerstaff.shared.query.HistoryTypeQueryService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
@RequestMapping("/freelancer")
public class FreelancerController {

    private static final String COOKIE_LAST_FREELANCER_ID = "lastFreelancerId";
    private static final int COOKIE_MAX_AGE = 30 * 24 * 60 * 60; // 30 Tage
    private static final int PAGE_SIZE = 20;
    private static final DateTimeFormatter AUDIT_DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final FreelancerCommandService commandService;
    private final FreelancerQueryService queryService;
    private final FreelancerTagCommandService tagCommandService;
    private final HistoryTypeQueryService historyTypeQueryService;
    private final RememberedProjectService rememberedProjectService;
    private final ProjectPositionCommandService positionCommandService;
    private final ObjectMapper objectMapper;

    public FreelancerController(final FreelancerCommandService commandService,
                                final FreelancerQueryService queryService,
                                final FreelancerTagCommandService tagCommandService,
                                final HistoryTypeQueryService historyTypeQueryService,
                                final RememberedProjectService rememberedProjectService,
                                final ProjectPositionCommandService positionCommandService,
                                final ObjectMapper objectMapper) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.tagCommandService = tagCommandService;
        this.historyTypeQueryService = historyTypeQueryService;
        this.rememberedProjectService = rememberedProjectService;
        this.positionCommandService = positionCommandService;
        this.objectMapper = objectMapper;
    }

    // -------------------------------------------------------------------------
    // Navigation: index → redirect zu letztem oder erstem
    // -------------------------------------------------------------------------

    @GetMapping
    public String index(@CookieValue(name = COOKIE_LAST_FREELANCER_ID, required = false) final Long lastId) {
        if (lastId != null && queryService.findById(lastId).isPresent()) {
            return "redirect:/freelancer/" + lastId;
        }
        return queryService.findFirst()
                .map(f -> "redirect:/freelancer/" + f.id())
                .orElse("redirect:/freelancer/new");
    }

    @GetMapping("/first")
    public String first() {
        return queryService.findFirst()
                .map(f -> "redirect:/freelancer/" + f.id())
                .orElse("redirect:/freelancer/new");
    }

    @GetMapping("/last")
    public String last() {
        return queryService.findLast()
                .map(f -> "redirect:/freelancer/" + f.id())
                .orElse("redirect:/freelancer/new");
    }

    @GetMapping("/previous/{id}")
    public String previous(@PathVariable final long id) {
        return queryService.findPrevious(id)
                .map(f -> "redirect:/freelancer/" + f.id())
                .orElse("redirect:/freelancer/new");
    }

    @GetMapping("/next/{id}")
    public String next(@PathVariable final long id) {
        return queryService.findNext(id)
                .map(f -> "redirect:/freelancer/" + f.id())
                .orElse("redirect:/freelancer/new");
    }

    // -------------------------------------------------------------------------
    // Anzeigen / Neuanlage
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    public String show(@PathVariable final long id, final HttpServletResponse response, final Model model, final Principal principal) {
        final var freelancer = commandService.findById(id).orElseThrow();
        final var cookie = new Cookie(COOKIE_LAST_FREELANCER_ID, String.valueOf(id));
        cookie.setPath("/freelancer");
        cookie.setMaxAge(COOKIE_MAX_AGE);
        response.addCookie(cookie);
        populateModel(model, freelancer, id, principal);
        return "freelancer/form";
    }

    @GetMapping("/new")
    public String newForm(final HttpServletResponse response, final Model model, final Principal principal) {
        final var cookie = new Cookie(COOKIE_LAST_FREELANCER_ID, "");
        cookie.setPath("/freelancer");
        cookie.setMaxAge(0); // löschen
        response.addCookie(cookie);
        populateModel(model, new Freelancer(), null, principal);
        return "freelancer/form";
    }

    private void populateModel(final Model model, final Freelancer freelancer, final Long freelancerId, final Principal principal) {
        model.addAttribute("freelancer", freelancer);
        if (freelancerId != null) {
            model.addAttribute("contacts", queryService.findContactsByFreelancerId(freelancerId));
            model.addAttribute("history", queryService.findHistoryByFreelancerId(freelancerId));
            model.addAttribute("tags", queryService.findTagsByFreelancerId(freelancerId));
        } else {
            model.addAttribute("contacts", List.of());
            model.addAttribute("history", List.of());
            model.addAttribute("tags", List.of());
        }
        model.addAttribute("historyTypes", historyTypeQueryService.findAll());
        model.addAttribute("tagTypes", TagType.values());
        model.addAttribute("rememberedProject", buildRememberedProjectInfo(principal));
        model.addAttribute("activePage", "freelancer");
        model.addAttribute("auditInfo", buildAuditInfo(
                freelancerId,
                freelancer.getCreationDate(), freelancer.getCreationUser(),
                freelancer.getChangedDate(), freelancer.getChangedUser()));
    }

    private String buildAuditInfo(final Long id, final LocalDateTime creationDate, final String creationUser,
                                  final LocalDateTime changedDate, final String changedUser) {
        if (id == null) return "Neu, noch nicht gespeichert";
        final String created = (creationDate != null ? creationDate.format(AUDIT_DATE_FMT) : "?")
                       + " " + (creationUser != null ? creationUser : "?");
        String result = "Erfasst: " + created;
        if (changedDate != null && !changedDate.equals(creationDate)) {
            result += "<br>Geändert: "
                   + changedDate.format(AUDIT_DATE_FMT)
                   + " " + (changedUser != null ? changedUser : "?");
        }
        return result;
    }

    private RememberedProjectInfo buildRememberedProjectInfo(final Principal principal) {
        if (principal == null) return null;
        return rememberedProjectService.getRememberedProjectInfo(principal.getName()).orElse(null);
    }

    // -------------------------------------------------------------------------
    // Speichern (Unified Save: Stammdaten + Kontakte + Historie + Tags in einer Transaktion)
    // -------------------------------------------------------------------------

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> save(@ModelAttribute final Freelancer freelancer,
                                  @RequestParam(required = false, defaultValue = "[]") final String contactsJson,
                                  @RequestParam(required = false, defaultValue = "[]") final String historyJson,
                                  @RequestParam(required = false, defaultValue = "[]") final String tagsJson,
                                  final HttpServletResponse response) throws IOException {
        try {
            final List<FreelancerContactEntry> contactChanges = objectMapper.readValue(
                    contactsJson, new TypeReference<>() {});
            final List<FreelancerHistoryEntry> historyChanges = objectMapper.readValue(
                    historyJson, new TypeReference<>() {});
            final List<FreelancerTagEntry> tagChanges = objectMapper.readValue(
                    tagsJson, new TypeReference<>() {});
            final var saved = commandService.save(freelancer, contactChanges, historyChanges, tagChanges);
            response.sendRedirect("/freelancer/" + saved.getId() + "?saved=true");
            return null;
        } catch (final DuplicateCodeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("duplicateCode", true));
        } catch (final OptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("conflict", true));
        } catch (final JacksonException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid json"));
        }
    }

    // -------------------------------------------------------------------------
    // Löschen
    // -------------------------------------------------------------------------

    @PostMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable final long id,
                                    final HttpServletResponse response) throws IOException {
        try {
            commandService.deleteById(id);
            response.sendRedirect("/freelancer/new");
            return null;
        } catch (final FreelancerHasPositionsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("blocked", true, "projectIds", e.getProjectIds()));
        }
    }

    // -------------------------------------------------------------------------
    // Zum gemerkten Projekt zuordnen
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/assign-to-remembered-project")
    @ResponseBody
    public ResponseEntity<?> assignToRememberedProject(@PathVariable final long id, final Principal principal) {
        final var projectId = rememberedProjectService.get(principal.getName());
        if (projectId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("noProject", true));
        }
        try {
            positionCommandService.assignFreelancerToProject(id, projectId.get(), null, null, null);
            return ResponseEntity.ok(Map.of("projectId", projectId.get()));
        } catch (final FreelancerAlreadyAssignedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("alreadyAssigned", true));
        }
    }

    // -------------------------------------------------------------------------
    // QBE-Suche
    // -------------------------------------------------------------------------

    @GetMapping("/search")
    public String search(@ModelAttribute final FreelancerSearchCriteria criteria,
                         @RequestParam(required = false, defaultValue = "0") final int offset,
                         final Model model,
                         final HttpServletResponse response) {
        if (offset > 0) {
            final var results = queryService.search(criteria, offset, PAGE_SIZE);
            final int nextOffset = offset + PAGE_SIZE;
            final long total = queryService.countSearch(criteria);
            if (nextOffset < total) {
                response.setHeader("X-Next-Url", buildSearchMoreUrl(criteria, nextOffset));
            }
            model.addAttribute("results", results);
            return "freelancer/search-results :: results";
        }

        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        final var results = queryService.search(criteria, 0, PAGE_SIZE);
        final long total = queryService.countSearch(criteria);
        model.addAttribute("results", results);
        model.addAttribute("totalCount", total);
        model.addAttribute("criteria", criteria);
        model.addAttribute("sortField", criteria.sortField());
        model.addAttribute("sortDir", criteria.sortDir());
        final String nextUrl = results.size() == PAGE_SIZE ? buildSearchMoreUrl(criteria, PAGE_SIZE) : null;
        model.addAttribute("nextUrl", nextUrl);
        model.addAttribute("editSearchUrl", buildEditSearchUrl(criteria));
        return "freelancer/search-page";
    }

    private void appendCriteriaParams(final UriComponentsBuilder b, final FreelancerSearchCriteria c) {
        if (c.name1()           != null) b.queryParam("name1",           c.name1());
        if (c.name2()           != null) b.queryParam("name2",           c.name2());
        if (c.company()         != null) b.queryParam("company",         c.company());
        if (c.street()          != null) b.queryParam("street",          c.street());
        if (c.country()         != null) b.queryParam("country",         c.country());
        if (c.plz()             != null) b.queryParam("plz",             c.plz());
        if (c.city()            != null) b.queryParam("city",            c.city());
        if (c.nationalitaet()   != null) b.queryParam("nationalitaet",   c.nationalitaet());
        if (c.comments()        != null) b.queryParam("comments",        c.comments());
        if (c.einsatzdetails()  != null) b.queryParam("einsatzdetails",  c.einsatzdetails());
        if (c.contactPerson()   != null) b.queryParam("contactPerson",   c.contactPerson());
        if (c.contactReason()   != null) b.queryParam("contactReason",   c.contactReason());
        if (c.kontaktart()      != null) b.queryParam("kontaktart",      c.kontaktart());
        if (c.debitorNr()       != null) b.queryParam("debitorNr",       c.debitorNr());
        if (c.gulpId()          != null) b.queryParam("gulpId",          c.gulpId());
        if (c.code()            != null) b.queryParam("code",            c.code());
        if (c.skills()          != null) b.queryParam("skills",          c.skills());
        if (c.salaryLongMax()   != null) b.queryParam("salaryLongMax",   c.salaryLongMax());
        if (c.salaryPerDayLongMax() != null) b.queryParam("salaryPerDayLongMax", c.salaryPerDayLongMax());
        if (c.sortField()       != null) b.queryParam("sortField",       c.sortField());
        if (c.sortDir()         != null) b.queryParam("sortDir",         c.sortDir());
    }

    private String buildEditSearchUrl(final FreelancerSearchCriteria c) {
        final var b = UriComponentsBuilder.fromPath("/freelancer/new");
        appendCriteriaParams(b, c);
        return b.encode().build().toUriString();
    }

    private String buildSearchMoreUrl(final FreelancerSearchCriteria c, final int offset) {
        final var b = UriComponentsBuilder.fromPath("/freelancer/search").queryParam("offset", offset);
        appendCriteriaParams(b, c);
        return b.encode().build().toUriString();
    }

    // -------------------------------------------------------------------------
    // Tag-Verfügbarkeit (GET – wird für Dropdown-Befüllung benötigt)
    // -------------------------------------------------------------------------

    @GetMapping("/{id}/available-tags/{type}")
    @ResponseBody
    public List<TagInfo> availableTags(@PathVariable final long id,
                                       @PathVariable final TagType type) {
        return queryService.findAvailableTagsByFreelancerIdAndType(id, type);
    }

    // -------------------------------------------------------------------------
    // Lookup per Code (für Cross-Modul-Suche ohne direkten Repository-Zugriff)
    // -------------------------------------------------------------------------

    @GetMapping("/lookup")
    @ResponseBody
    public ResponseEntity<?> lookupByCode(@RequestParam final String code) {
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "code required"));
        }
        final var result = commandService.findByCode(code.trim());
        if (result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("notFound", true));
        }
        return ResponseEntity.ok(result.get());
    }
}
