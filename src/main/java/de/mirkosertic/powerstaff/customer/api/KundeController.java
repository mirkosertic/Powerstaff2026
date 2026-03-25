package de.mirkosertic.powerstaff.customer.api;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import de.mirkosertic.powerstaff.customer.command.Kunde;
import de.mirkosertic.powerstaff.customer.command.KundeCommandService;
import de.mirkosertic.powerstaff.customer.command.KundeContactEntry;
import de.mirkosertic.powerstaff.customer.command.KundeHistoryEntry;
import de.mirkosertic.powerstaff.customer.command.KundeHasProjectsException;
import de.mirkosertic.powerstaff.customer.query.KundeQueryService;
import de.mirkosertic.powerstaff.customer.query.KundeSearchCriteria;
import de.mirkosertic.powerstaff.project.command.RememberedProjectInfo;
import de.mirkosertic.powerstaff.project.command.RememberedProjectService;
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
@RequestMapping("/kunde")
public class KundeController {

    private static final String COOKIE_LAST_KUNDE_ID = "lastKundeId";
    private static final int COOKIE_MAX_AGE = 30 * 24 * 60 * 60; // 30 Tage
    private static final int PAGE_SIZE = 20;
    private static final DateTimeFormatter AUDIT_DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final KundeCommandService commandService;
    private final KundeQueryService queryService;
    private final HistoryTypeQueryService historyTypeQueryService;
    private final RememberedProjectService rememberedProjectService;
    private final ObjectMapper objectMapper;

    public KundeController(final KundeCommandService commandService,
                           final KundeQueryService queryService,
                           final HistoryTypeQueryService historyTypeQueryService,
                           final RememberedProjectService rememberedProjectService,
                           final ObjectMapper objectMapper) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.historyTypeQueryService = historyTypeQueryService;
        this.rememberedProjectService = rememberedProjectService;
        this.objectMapper = objectMapper;
    }

    // -------------------------------------------------------------------------
    // Navigation: index → redirect zu letztem oder erstem
    // -------------------------------------------------------------------------

    @GetMapping
    public String index(@CookieValue(name = COOKIE_LAST_KUNDE_ID, required = false) final Long lastId) {
        if (lastId != null && queryService.findById(lastId).isPresent()) {
            return "redirect:/kunde/" + lastId;
        }
        return queryService.findFirst()
                .map(k -> "redirect:/kunde/" + k.id())
                .orElse("redirect:/kunde/new");
    }

    @GetMapping("/first")
    public String first() {
        return queryService.findFirst()
                .map(k -> "redirect:/kunde/" + k.id())
                .orElse("redirect:/kunde/new");
    }

    @GetMapping("/last")
    public String last() {
        return queryService.findLast()
                .map(k -> "redirect:/kunde/" + k.id())
                .orElse("redirect:/kunde/new");
    }

    @GetMapping("/previous/{id}")
    public String previous(@PathVariable final long id) {
        return queryService.findPrevious(id)
                .map(k -> "redirect:/kunde/" + k.id())
                .orElse("redirect:/kunde/new");
    }

    @GetMapping("/next/{id}")
    public String next(@PathVariable final long id) {
        return queryService.findNext(id)
                .map(k -> "redirect:/kunde/" + k.id())
                .orElse("redirect:/kunde/new");
    }

    // -------------------------------------------------------------------------
    // Anzeigen / Neuanlage
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    public String show(@PathVariable final long id, final HttpServletResponse response, final Model model, final Principal principal) {
        final var kunde = commandService.findById(id).orElseThrow();
        final var cookie = new Cookie(COOKIE_LAST_KUNDE_ID, String.valueOf(id));
        cookie.setPath("/kunde");
        cookie.setMaxAge(COOKIE_MAX_AGE);
        response.addCookie(cookie);
        populateModel(model, kunde, id, principal);
        return "kunde/form";
    }

    @GetMapping("/new")
    public String newForm(final HttpServletResponse response, final Model model, final Principal principal) {
        final var cookie = new Cookie(COOKIE_LAST_KUNDE_ID, "");
        cookie.setPath("/kunde");
        cookie.setMaxAge(0); // löschen
        response.addCookie(cookie);
        populateModel(model, new Kunde(), null, principal);
        return "kunde/form";
    }

    private void populateModel(final Model model, final Kunde kunde, final Long kundeId, final Principal principal) {
        model.addAttribute("kunde", kunde);
        if (kundeId != null) {
            model.addAttribute("contacts", queryService.findContactsByKundeId(kundeId));
            model.addAttribute("history", queryService.findHistoryByKundeId(kundeId));
            model.addAttribute("projects", queryService.findProjectsByKundeId(kundeId, null, null));
        } else {
            model.addAttribute("contacts", List.of());
            model.addAttribute("history", List.of());
            model.addAttribute("projects", List.of());
        }
        model.addAttribute("historyTypes", historyTypeQueryService.findAll());
        model.addAttribute("rememberedProject", buildRememberedProjectInfo(principal));
        model.addAttribute("activePage", "kunde");
        model.addAttribute("auditInfo", buildAuditInfo(
                kundeId,
                kunde.getCreationDate(), kunde.getCreationUser(),
                kunde.getChangedDate(), kunde.getChangedUser()));
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
    // Speichern (Unified Save)
    // -------------------------------------------------------------------------

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> save(@ModelAttribute final Kunde kunde,
                                  @RequestParam(required = false, defaultValue = "[]") final String contactsJson,
                                  @RequestParam(required = false, defaultValue = "[]") final String historyJson,
                                  final HttpServletResponse response) throws IOException {
        try {
            final List<KundeContactEntry> contacts = objectMapper.readValue(
                    contactsJson, new TypeReference<>() {});
            final List<KundeHistoryEntry> newHistory = objectMapper.readValue(
                    historyJson, new TypeReference<>() {});
            final var saved = commandService.save(kunde, contacts, newHistory);
            response.sendRedirect("/kunde/" + saved.getId() + "?saved=true");
            return null;
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
            response.sendRedirect("/kunde/new");
            return null;
        } catch (final KundeHasProjectsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("blocked", true, "projectIds", e.getProjectIds()));
        }
    }

    // -------------------------------------------------------------------------
    // QBE-Suche
    // -------------------------------------------------------------------------

    @GetMapping("/search")
    public String search(@ModelAttribute final KundeSearchCriteria criteria,
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
            return "kunde/search-results :: results";
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
        return "kunde/search-page";
    }

    private void appendCriteriaParams(final UriComponentsBuilder b, final KundeSearchCriteria c) {
        if (c.company()    != null) b.queryParam("company",    c.company());
        if (c.name1()      != null) b.queryParam("name1",      c.name1());
        if (c.name2()      != null) b.queryParam("name2",      c.name2());
        if (c.street()     != null) b.queryParam("street",     c.street());
        if (c.country()    != null) b.queryParam("country",    c.country());
        if (c.plz()        != null) b.queryParam("plz",        c.plz());
        if (c.city()       != null) b.queryParam("city",       c.city());
        if (c.comments()   != null) b.queryParam("comments",   c.comments());
        if (c.kreditorNr() != null) b.queryParam("kreditorNr", c.kreditorNr());
        if (c.debitorNr()  != null) b.queryParam("debitorNr",  c.debitorNr());
        if (c.sortField()  != null) b.queryParam("sortField",  c.sortField());
        if (c.sortDir()    != null) b.queryParam("sortDir",    c.sortDir());
    }

    private String buildEditSearchUrl(final KundeSearchCriteria c) {
        final var b = UriComponentsBuilder.fromPath("/kunde/new");
        appendCriteriaParams(b, c);
        return b.encode().build().toUriString();
    }

    private String buildSearchMoreUrl(final KundeSearchCriteria c, final int offset) {
        final var b = UriComponentsBuilder.fromPath("/kunde/search").queryParam("offset", offset);
        appendCriteriaParams(b, c);
        return b.encode().build().toUriString();
    }
}
