package de.mirkosertic.powerstaff.partner.api;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import de.mirkosertic.powerstaff.freelancer.command.FreelancerCommandService;
import de.mirkosertic.powerstaff.freelancer.command.FreelancerLookupResult;
import de.mirkosertic.powerstaff.partner.command.Partner;
import de.mirkosertic.powerstaff.partner.command.PartnerCommandService;
import de.mirkosertic.powerstaff.partner.command.PartnerContactEntry;
import de.mirkosertic.powerstaff.partner.command.PartnerHistoryEntry;
import de.mirkosertic.powerstaff.partner.command.PartnerHasProjectsException;
import de.mirkosertic.powerstaff.partner.query.PartnerFreelancerView;
import de.mirkosertic.powerstaff.partner.query.PartnerQueryService;
import de.mirkosertic.powerstaff.partner.query.PartnerSearchCriteria;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/partner")
public class PartnerController {

    private static final String COOKIE_LAST_PARTNER_ID = "lastPartnerId";
    private static final int COOKIE_MAX_AGE = 30 * 24 * 60 * 60; // 30 days
    private static final int PAGE_SIZE = 20;

    private final PartnerCommandService commandService;
    private final PartnerQueryService queryService;
    private final FreelancerCommandService freelancerCommandService;
    private final HistoryTypeQueryService historyTypeQueryService;
    private final RememberedProjectService rememberedProjectService;
    private final ObjectMapper objectMapper;

    public PartnerController(PartnerCommandService commandService,
                             PartnerQueryService queryService,
                             FreelancerCommandService freelancerCommandService,
                             HistoryTypeQueryService historyTypeQueryService,
                             RememberedProjectService rememberedProjectService,
                             ObjectMapper objectMapper) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.freelancerCommandService = freelancerCommandService;
        this.historyTypeQueryService = historyTypeQueryService;
        this.rememberedProjectService = rememberedProjectService;
        this.objectMapper = objectMapper;
    }

    // -------------------------------------------------------------------------
    // Navigation: index → redirect to last or first
    // -------------------------------------------------------------------------

    @GetMapping
    public String index(@CookieValue(name = COOKIE_LAST_PARTNER_ID, required = false) Long lastId) {
        if (lastId != null && queryService.findById(lastId).isPresent()) {
            return "redirect:/partner/" + lastId;
        }
        return queryService.findFirst()
                .map(p -> "redirect:/partner/" + p.id())
                .orElse("redirect:/partner/new");
    }

    @GetMapping("/first")
    public String first() {
        return queryService.findFirst()
                .map(p -> "redirect:/partner/" + p.id())
                .orElse("redirect:/partner/new");
    }

    @GetMapping("/last")
    public String last() {
        return queryService.findLast()
                .map(p -> "redirect:/partner/" + p.id())
                .orElse("redirect:/partner/new");
    }

    @GetMapping("/previous/{id}")
    public String previous(@PathVariable long id) {
        return queryService.findPrevious(id)
                .map(p -> "redirect:/partner/" + p.id())
                .orElse("redirect:/partner/new");
    }

    @GetMapping("/next/{id}")
    public String next(@PathVariable long id) {
        return queryService.findNext(id)
                .map(p -> "redirect:/partner/" + p.id())
                .orElse("redirect:/partner/new");
    }

    // -------------------------------------------------------------------------
    // Show / New
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    public String show(@PathVariable long id, HttpServletResponse response, Model model, Principal principal) {
        var partner = commandService.findById(id).orElseThrow();
        var cookie = new Cookie(COOKIE_LAST_PARTNER_ID, String.valueOf(id));
        cookie.setPath("/partner");
        cookie.setMaxAge(COOKIE_MAX_AGE);
        response.addCookie(cookie);
        model.addAttribute("partner", partner);
        model.addAttribute("contacts", queryService.findContactsByPartner(id));
        model.addAttribute("history", queryService.findHistoryByPartner(id));
        model.addAttribute("freelancers", queryService.findFreelancersByPartner(id));
        model.addAttribute("projects", queryService.findProjectsByPartner(id));
        model.addAttribute("historyTypes", historyTypeQueryService.findAll());
        model.addAttribute("rememberedProject", buildRememberedProjectInfo(principal));
        model.addAttribute("activePage", "partner");
        return "partner/form";
    }

    @GetMapping("/new")
    public String newForm(HttpServletResponse response, Model model, Principal principal) {
        var cookie = new Cookie(COOKIE_LAST_PARTNER_ID, "");
        cookie.setPath("/partner");
        cookie.setMaxAge(0); // delete
        response.addCookie(cookie);
        model.addAttribute("partner", new Partner());
        model.addAttribute("contacts", List.of());
        model.addAttribute("history", List.of());
        model.addAttribute("freelancers", List.of());
        model.addAttribute("projects", List.of());
        model.addAttribute("historyTypes", historyTypeQueryService.findAll());
        model.addAttribute("rememberedProject", buildRememberedProjectInfo(principal));
        model.addAttribute("activePage", "partner");
        return "partner/form";
    }

    private RememberedProjectInfo buildRememberedProjectInfo(Principal principal) {
        if (principal == null) return null;
        return rememberedProjectService.getRememberedProjectInfo(principal.getName()).orElse(null);
    }

    // -------------------------------------------------------------------------
    // Save (Unified Save: Stammdaten + Kontakte + Historie in einer Transaktion)
    // -------------------------------------------------------------------------

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> save(@ModelAttribute Partner partner,
                                  @RequestParam(required = false, defaultValue = "[]") String contactsJson,
                                  @RequestParam(required = false, defaultValue = "[]") String historyJson,
                                  HttpServletResponse response) throws IOException {
        try {
            List<PartnerContactEntry> contacts = objectMapper.readValue(
                    contactsJson, new TypeReference<>() {});
            List<PartnerHistoryEntry> newHistory = objectMapper.readValue(
                    historyJson, new TypeReference<>() {});
            var saved = commandService.save(partner, contacts, newHistory);
            response.sendRedirect("/partner/" + saved.getId() + "?saved=true");
            return null;
        } catch (OptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("conflict", true));
        } catch (JacksonException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid json"));
        }
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    @PostMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable long id,
                                    HttpServletResponse response) throws IOException {
        try {
            commandService.deleteById(id);
            response.sendRedirect("/partner/new");
            return null;
        } catch (PartnerHasProjectsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("blocked", true, "projectIds", e.getProjectIds()));
        }
    }

    // -------------------------------------------------------------------------
    // QBE-Suche
    // -------------------------------------------------------------------------

    @GetMapping("/search")
    public String search(@ModelAttribute PartnerSearchCriteria criteria,
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
            return "partner/search-results :: results";
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
        return "partner/search-page";
    }

    private void appendCriteriaParams(UriComponentsBuilder b, PartnerSearchCriteria criteria) {
        if (criteria.company()    != null) b.queryParam("company",    criteria.company());
        if (criteria.name1()      != null) b.queryParam("name1",      criteria.name1());
        if (criteria.name2()      != null) b.queryParam("name2",      criteria.name2());
        if (criteria.street()     != null) b.queryParam("street",     criteria.street());
        if (criteria.country()    != null) b.queryParam("country",    criteria.country());
        if (criteria.plz()        != null) b.queryParam("plz",        criteria.plz());
        if (criteria.city()       != null) b.queryParam("city",       criteria.city());
        if (criteria.comments()   != null) b.queryParam("comments",   criteria.comments());
        if (criteria.debitorNr()  != null) b.queryParam("debitorNr",  criteria.debitorNr());
        if (criteria.kreditorNr() != null) b.queryParam("kreditorNr", criteria.kreditorNr());
        if (criteria.sortField()  != null) b.queryParam("sortField",  criteria.sortField());
        if (criteria.sortDir()    != null) b.queryParam("sortDir",    criteria.sortDir());
    }

    private String buildEditSearchUrl(PartnerSearchCriteria criteria) {
        var b = UriComponentsBuilder.fromPath("/partner/new");
        appendCriteriaParams(b, criteria);
        return b.encode().build().toUriString();
    }

    private String buildSearchMoreUrl(PartnerSearchCriteria criteria, int offset) {
        var b = UriComponentsBuilder.fromPath("/partner/search").queryParam("offset", offset);
        appendCriteriaParams(b, criteria);
        return b.encode().build().toUriString();
    }

    // -------------------------------------------------------------------------
    // Freelancer-Zuordnung
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/assign-freelancer")
    @ResponseBody
    public ResponseEntity<?> assignFreelancer(@PathVariable long id,
                                              @RequestBody Map<String, String> body) {
        String code = body.get("code");
        FreelancerLookupResult freelancer = freelancerCommandService.findByCode(code).orElse(null);

        if (freelancer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("notFound", true));
        }

        if (freelancer.partnerId() != null && freelancer.partnerId() != id) {
            String otherCompany = queryService.findById(freelancer.partnerId())
                    .map(p -> p.company())
                    .orElse("Unbekannter Partner");
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("otherPartner", otherCompany,
                                 "freelancerId", freelancer.id()));
        }

        freelancerCommandService.assignToPartner(freelancer.id(), id);
        List<PartnerFreelancerView> updated = queryService.findFreelancersByPartner(id);
        return ResponseEntity.ok(Map.of("ok", true, "freelancers", updated));
    }

    @PostMapping("/{id}/confirm-reassign-freelancer")
    @ResponseBody
    public ResponseEntity<?> confirmReassignFreelancer(@PathVariable long id,
                                                       @RequestBody Map<String, Long> body) {
        long freelancerId = body.get("freelancerId");
        freelancerCommandService.assignToPartner(freelancerId, id);
        List<PartnerFreelancerView> updated = queryService.findFreelancersByPartner(id);
        return ResponseEntity.ok(Map.of("ok", true, "freelancers", updated));
    }

    @PostMapping("/{id}/remove-freelancer/{freelancerId}")
    @ResponseBody
    public ResponseEntity<?> removeFreelancer(@PathVariable long id,
                                              @PathVariable long freelancerId) {
        freelancerCommandService.removeFromPartner(freelancerId, id);
        List<PartnerFreelancerView> updated = queryService.findFreelancersByPartner(id);
        return ResponseEntity.ok(Map.of("ok", true, "freelancers", updated));
    }
}
