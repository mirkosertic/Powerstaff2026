package de.mirkosertic.powerstaff.freelancer.api;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import de.mirkosertic.powerstaff.freelancer.command.DuplicateTagException;
import de.mirkosertic.powerstaff.freelancer.command.Freelancer;
import de.mirkosertic.powerstaff.freelancer.command.FreelancerCommandService;
import de.mirkosertic.powerstaff.freelancer.command.FreelancerContactEntry;
import de.mirkosertic.powerstaff.freelancer.command.FreelancerHistoryEntry;
import de.mirkosertic.powerstaff.freelancer.command.FreelancerHasPositionsException;
import de.mirkosertic.powerstaff.freelancer.command.FreelancerTagCommandService;
import de.mirkosertic.powerstaff.freelancer.query.FreelancerQueryService;
import de.mirkosertic.powerstaff.freelancer.query.FreelancerSearchCriteria;
import de.mirkosertic.powerstaff.freelancer.query.TagInfo;
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
import org.springframework.web.bind.annotation.DeleteMapping;
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
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/freelancer")
public class FreelancerController {

    private static final String COOKIE_LAST_FREELANCER_ID = "lastFreelancerId";
    private static final int COOKIE_MAX_AGE = 30 * 24 * 60 * 60; // 30 Tage
    private static final int PAGE_SIZE = 20;

    private final FreelancerCommandService commandService;
    private final FreelancerQueryService queryService;
    private final FreelancerTagCommandService tagCommandService;
    private final HistoryTypeQueryService historyTypeQueryService;
    private final ObjectMapper objectMapper;

    public FreelancerController(FreelancerCommandService commandService,
                                FreelancerQueryService queryService,
                                FreelancerTagCommandService tagCommandService,
                                HistoryTypeQueryService historyTypeQueryService,
                                ObjectMapper objectMapper) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.tagCommandService = tagCommandService;
        this.historyTypeQueryService = historyTypeQueryService;
        this.objectMapper = objectMapper;
    }

    // -------------------------------------------------------------------------
    // Navigation: index → redirect zu letztem oder erstem
    // -------------------------------------------------------------------------

    @GetMapping
    public String index(@CookieValue(name = COOKIE_LAST_FREELANCER_ID, required = false) Long lastId) {
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
    public String previous(@PathVariable long id) {
        return queryService.findPrevious(id)
                .map(f -> "redirect:/freelancer/" + f.id())
                .orElse("redirect:/freelancer/new");
    }

    @GetMapping("/next/{id}")
    public String next(@PathVariable long id) {
        return queryService.findNext(id)
                .map(f -> "redirect:/freelancer/" + f.id())
                .orElse("redirect:/freelancer/new");
    }

    // -------------------------------------------------------------------------
    // Anzeigen / Neuanlage
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    public String show(@PathVariable long id, HttpServletResponse response, Model model) {
        var freelancer = commandService.findById(id).orElseThrow();
        var cookie = new Cookie(COOKIE_LAST_FREELANCER_ID, String.valueOf(id));
        cookie.setPath("/freelancer");
        cookie.setMaxAge(COOKIE_MAX_AGE);
        response.addCookie(cookie);
        populateModel(model, freelancer, id);
        return "freelancer/form";
    }

    @GetMapping("/new")
    public String newForm(HttpServletResponse response, Model model) {
        var cookie = new Cookie(COOKIE_LAST_FREELANCER_ID, "");
        cookie.setPath("/freelancer");
        cookie.setMaxAge(0); // löschen
        response.addCookie(cookie);
        populateModel(model, new Freelancer(), null);
        return "freelancer/form";
    }

    private void populateModel(Model model, Freelancer freelancer, Long freelancerId) {
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
        model.addAttribute("activePage", "freelancer");
    }

    // -------------------------------------------------------------------------
    // Speichern (Unified Save: Stammdaten + Kontakte + Historie in einer Transaktion)
    // -------------------------------------------------------------------------

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> save(@ModelAttribute Freelancer freelancer,
                                  @RequestParam(required = false, defaultValue = "[]") String contactsJson,
                                  @RequestParam(required = false, defaultValue = "[]") String historyJson,
                                  HttpServletResponse response) throws IOException {
        try {
            List<FreelancerContactEntry> contacts = objectMapper.readValue(
                    contactsJson, new TypeReference<>() {});
            List<FreelancerHistoryEntry> newHistory = objectMapper.readValue(
                    historyJson, new TypeReference<>() {});
            var saved = commandService.save(freelancer, contacts, newHistory);
            response.sendRedirect("/freelancer/" + saved.getId() + "?saved=true");
            return null;
        } catch (OptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("conflict", true));
        } catch (JacksonException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid json"));
        }
    }

    // -------------------------------------------------------------------------
    // Löschen
    // -------------------------------------------------------------------------

    @PostMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable long id,
                                    HttpServletResponse response) throws IOException {
        try {
            commandService.deleteById(id);
            response.sendRedirect("/freelancer/new");
            return null;
        } catch (FreelancerHasPositionsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("blocked", true, "projectIds", e.getProjectIds()));
        }
    }

    // -------------------------------------------------------------------------
    // QBE-Suche
    // -------------------------------------------------------------------------

    @PostMapping("/search")
    public String search(@ModelAttribute FreelancerSearchCriteria criteria, Model model) {
        var results = queryService.search(criteria, 0, PAGE_SIZE);
        long total = queryService.countSearch(criteria);
        model.addAttribute("results", results);
        model.addAttribute("totalCount", total);
        String nextUrl = results.size() == PAGE_SIZE ? buildSearchMoreUrl(criteria, PAGE_SIZE) : null;
        model.addAttribute("nextUrl", nextUrl);
        return "freelancer/search-results :: results";
    }

    @GetMapping("/search-more")
    public String searchMore(@ModelAttribute FreelancerSearchCriteria criteria,
                             @RequestParam int offset,
                             Model model,
                             HttpServletResponse response) {
        var results = queryService.search(criteria, offset, PAGE_SIZE);
        int nextOffset = offset + PAGE_SIZE;
        long total = queryService.countSearch(criteria);
        if (nextOffset < total) {
            response.setHeader("X-Next-Url", buildSearchMoreUrl(criteria, nextOffset));
        }
        model.addAttribute("results", results);
        return "freelancer/search-results :: results";
    }

    private String buildSearchMoreUrl(FreelancerSearchCriteria c, int offset) {
        var b = UriComponentsBuilder.fromPath("/freelancer/search-more").queryParam("offset", offset);
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
        return b.build().toUriString();
    }

    // -------------------------------------------------------------------------
    // Tag-Verwaltung (AJAX)
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/tags")
    @ResponseBody
    public ResponseEntity<?> addTag(@PathVariable long id,
                                    @RequestBody Map<String, Long> body) {
        long tagId = body.get("tagId");
        try {
            tagCommandService.addTag(id, tagId);
            List<TagInfo> updated = queryService.findTagsByFreelancerId(id);
            return ResponseEntity.ok(Map.of("ok", true, "tags", updated));
        } catch (DuplicateTagException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("duplicate", true));
        }
    }

    @DeleteMapping("/{id}/tags/{freelancerTagId}")
    @ResponseBody
    public ResponseEntity<?> removeTag(@PathVariable long id,
                                       @PathVariable long freelancerTagId) {
        tagCommandService.removeTag(freelancerTagId);
        List<TagInfo> updated = queryService.findTagsByFreelancerId(id);
        return ResponseEntity.ok(Map.of("ok", true, "tags", updated));
    }

    @GetMapping("/{id}/available-tags/{type}")
    @ResponseBody
    public List<TagInfo> availableTags(@PathVariable long id,
                                       @PathVariable TagType type) {
        return queryService.findAvailableTagsByFreelancerIdAndType(id, type);
    }
}
