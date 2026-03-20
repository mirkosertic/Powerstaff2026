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
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/kunde")
public class KundeController {

    private static final String COOKIE_LAST_KUNDE_ID = "lastKundeId";
    private static final int COOKIE_MAX_AGE = 30 * 24 * 60 * 60; // 30 Tage
    private static final int PAGE_SIZE = 20;

    private final KundeCommandService commandService;
    private final KundeQueryService queryService;
    private final HistoryTypeQueryService historyTypeQueryService;
    private final ObjectMapper objectMapper;

    public KundeController(KundeCommandService commandService,
                           KundeQueryService queryService,
                           HistoryTypeQueryService historyTypeQueryService,
                           ObjectMapper objectMapper) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.historyTypeQueryService = historyTypeQueryService;
        this.objectMapper = objectMapper;
    }

    // -------------------------------------------------------------------------
    // Navigation: index → redirect zu letztem oder erstem
    // -------------------------------------------------------------------------

    @GetMapping
    public String index(@CookieValue(name = COOKIE_LAST_KUNDE_ID, required = false) Long lastId) {
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
    public String previous(@PathVariable long id) {
        return queryService.findPrevious(id)
                .map(k -> "redirect:/kunde/" + k.id())
                .orElse("redirect:/kunde/new");
    }

    @GetMapping("/next/{id}")
    public String next(@PathVariable long id) {
        return queryService.findNext(id)
                .map(k -> "redirect:/kunde/" + k.id())
                .orElse("redirect:/kunde/new");
    }

    // -------------------------------------------------------------------------
    // Anzeigen / Neuanlage
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    public String show(@PathVariable long id, HttpServletResponse response, Model model) {
        var kunde = commandService.findById(id).orElseThrow();
        var cookie = new Cookie(COOKIE_LAST_KUNDE_ID, String.valueOf(id));
        cookie.setPath("/kunde");
        cookie.setMaxAge(COOKIE_MAX_AGE);
        response.addCookie(cookie);
        populateModel(model, kunde, id);
        return "kunde/form";
    }

    @GetMapping("/new")
    public String newForm(HttpServletResponse response, Model model) {
        var cookie = new Cookie(COOKIE_LAST_KUNDE_ID, "");
        cookie.setPath("/kunde");
        cookie.setMaxAge(0); // löschen
        response.addCookie(cookie);
        populateModel(model, new Kunde(), null);
        return "kunde/form";
    }

    private void populateModel(Model model, Kunde kunde, Long kundeId) {
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
        model.addAttribute("activePage", "kunde");
    }

    // -------------------------------------------------------------------------
    // Speichern (Unified Save)
    // -------------------------------------------------------------------------

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> save(@ModelAttribute Kunde kunde,
                                  @RequestParam(required = false, defaultValue = "[]") String contactsJson,
                                  @RequestParam(required = false, defaultValue = "[]") String historyJson,
                                  HttpServletResponse response) throws IOException {
        try {
            List<KundeContactEntry> contacts = objectMapper.readValue(
                    contactsJson, new TypeReference<>() {});
            List<KundeHistoryEntry> newHistory = objectMapper.readValue(
                    historyJson, new TypeReference<>() {});
            var saved = commandService.save(kunde, contacts, newHistory);
            response.sendRedirect("/kunde/" + saved.getId());
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
            response.sendRedirect("/kunde/new");
            return null;
        } catch (KundeHasProjectsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("blocked", true, "projectIds", e.getProjectIds()));
        }
    }

    // -------------------------------------------------------------------------
    // QBE-Suche
    // -------------------------------------------------------------------------

    @PostMapping("/search")
    public String search(@ModelAttribute KundeSearchCriteria criteria, Model model) {
        var results = queryService.search(criteria, 0, PAGE_SIZE);
        long total = queryService.countSearch(criteria);
        model.addAttribute("results", results);
        model.addAttribute("totalCount", total);
        String nextUrl = results.size() == PAGE_SIZE ? buildSearchMoreUrl(criteria, PAGE_SIZE) : null;
        model.addAttribute("nextUrl", nextUrl);
        return "kunde/search-results :: results";
    }

    @GetMapping("/search-more")
    public String searchMore(@ModelAttribute KundeSearchCriteria criteria,
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
        return "kunde/search-results :: results";
    }

    private String buildSearchMoreUrl(KundeSearchCriteria c, int offset) {
        var b = UriComponentsBuilder.fromPath("/kunde/search-more").queryParam("offset", offset);
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
        return b.build().toUriString();
    }
}
