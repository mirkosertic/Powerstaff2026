package de.mirkosertic.powerstaff.partner.api;

import de.mirkosertic.powerstaff.partner.command.Partner;
import de.mirkosertic.powerstaff.partner.command.PartnerCommandService;
import de.mirkosertic.powerstaff.partner.command.PartnerHasProjectsException;
import de.mirkosertic.powerstaff.partner.query.PartnerFreelancerView;
import de.mirkosertic.powerstaff.partner.query.PartnerQueryService;
import de.mirkosertic.powerstaff.partner.query.PartnerSearchCriteria;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
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

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/partner")
public class PartnerController {

    private static final String SESSION_LAST_PARTNER_ID = "lastPartnerId";
    private static final String SESSION_SEARCH_CRITERIA = "partnerSearchCriteria";
    private static final int PAGE_SIZE = 20;

    private final PartnerCommandService commandService;
    private final PartnerQueryService queryService;
    private final JdbcClient jdbcClient;

    public PartnerController(PartnerCommandService commandService,
                             PartnerQueryService queryService,
                             JdbcClient jdbcClient) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.jdbcClient = jdbcClient;
    }

    // -------------------------------------------------------------------------
    // Navigation: index → redirect to last or first
    // -------------------------------------------------------------------------

    @GetMapping
    public String index(HttpSession session) {
        Long lastId = (Long) session.getAttribute(SESSION_LAST_PARTNER_ID);
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
    public String previous(@PathVariable Long id) {
        return queryService.findPrevious(id)
                .map(p -> "redirect:/partner/" + p.id())
                .orElse("redirect:/partner/new");
    }

    @GetMapping("/next/{id}")
    public String next(@PathVariable Long id) {
        return queryService.findNext(id)
                .map(p -> "redirect:/partner/" + p.id())
                .orElse("redirect:/partner/new");
    }

    // -------------------------------------------------------------------------
    // Show / New
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    public String show(@PathVariable Long id, HttpSession session, Model model) {
        var partner = commandService.findById(id).orElseThrow();
        session.setAttribute(SESSION_LAST_PARTNER_ID, id);
        model.addAttribute("partner", partner);
        model.addAttribute("contacts", queryService.findContactsByPartner(id));
        model.addAttribute("history", queryService.findHistoryByPartner(id));
        model.addAttribute("freelancers", queryService.findFreelancersByPartner(id));
        model.addAttribute("projects", queryService.findProjectsByPartner(id));
        model.addAttribute("activePage", "partner");
        return "partner/form";
    }

    @GetMapping("/new")
    public String newForm(HttpSession session, Model model) {
        session.removeAttribute(SESSION_LAST_PARTNER_ID);
        model.addAttribute("partner", new Partner());
        model.addAttribute("contacts", java.util.List.of());
        model.addAttribute("history", java.util.List.of());
        model.addAttribute("freelancers", java.util.List.of());
        model.addAttribute("projects", java.util.List.of());
        model.addAttribute("activePage", "partner");
        return "partner/form";
    }

    // -------------------------------------------------------------------------
    // Save
    // -------------------------------------------------------------------------

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> save(@ModelAttribute Partner partner,
                                  HttpServletResponse response) throws IOException {
        try {
            var saved = commandService.save(partner);
            response.sendRedirect("/partner/" + saved.getId());
            return null;
        } catch (OptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("conflict", true));
        }
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    @PostMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable Long id,
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

    @PostMapping("/search")
    public String search(@ModelAttribute PartnerSearchCriteria criteria,
                         HttpSession session,
                         Model model) {
        session.setAttribute(SESSION_SEARCH_CRITERIA, criteria);
        var results = queryService.search(criteria, 0, PAGE_SIZE);
        long total = queryService.countSearch(criteria);
        model.addAttribute("results", results);
        model.addAttribute("totalCount", total);
        String nextUrl = results.size() == PAGE_SIZE ? "/partner/search-more?offset=" + PAGE_SIZE : null;
        model.addAttribute("nextUrl", nextUrl);
        return "partner/search-results :: results";
    }

    @GetMapping("/search-more")
    public String searchMore(@RequestParam int offset,
                             HttpSession session,
                             Model model,
                             HttpServletResponse response) {
        var criteria = (PartnerSearchCriteria) session.getAttribute(SESSION_SEARCH_CRITERIA);
        if (criteria == null) {
            criteria = PartnerSearchCriteria.empty();
        }
        var results = queryService.search(criteria, offset, PAGE_SIZE);
        int nextOffset = offset + PAGE_SIZE;
        long total = queryService.countSearch(criteria);
        if (nextOffset < total) {
            response.setHeader("X-Next-Url", "/partner/search-more?offset=" + nextOffset);
        }
        model.addAttribute("results", results);
        return "partner/search-results :: results";
    }

    // -------------------------------------------------------------------------
    // Freelancer-Zuordnung
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/assign-freelancer")
    @ResponseBody
    public ResponseEntity<?> assignFreelancer(@PathVariable Long id,
                                              @RequestBody Map<String, String> body) {
        String code = body.get("code");
        var freelancerOpt = jdbcClient
                .sql("SELECT id, partner_id, company FROM freelancer WHERE code = :code")
                .param("code", code)
                .query((rs, rowNum) -> Map.of(
                        "id", rs.getLong("id"),
                        "partnerId", rs.getObject("partner_id"),
                        "company", rs.getString("company") != null ? rs.getString("company") : ""))
                .optional();

        if (freelancerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("notFound", true));
        }

        @SuppressWarnings("unchecked")
        var freelancer = (Map<String, Object>) freelancerOpt.get();
        Object existingPartnerId = freelancer.get("partnerId");

        if (existingPartnerId != null && !existingPartnerId.equals(id)) {
            String otherCompany = jdbcClient
                    .sql("SELECT company FROM partner WHERE id = :partnerId")
                    .param("partnerId", existingPartnerId)
                    .query(String.class)
                    .optional()
                    .orElse("Unbekannter Partner");
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("otherPartner", otherCompany,
                                 "freelancerId", freelancer.get("id")));
        }

        Long freelancerId = (Long) freelancer.get("id");
        jdbcClient.sql("UPDATE freelancer SET partner_id = :partnerId WHERE id = :freelancerId")
                .param("partnerId", id)
                .param("freelancerId", freelancerId)
                .update();

        List<PartnerFreelancerView> updated = queryService.findFreelancersByPartner(id);
        return ResponseEntity.ok(Map.of("ok", true, "freelancers", updated));
    }

    @PostMapping("/{id}/confirm-reassign-freelancer")
    @ResponseBody
    public ResponseEntity<?> confirmReassignFreelancer(@PathVariable Long id,
                                                        @RequestBody Map<String, Long> body) {
        Long freelancerId = body.get("freelancerId");
        jdbcClient.sql("UPDATE freelancer SET partner_id = :partnerId WHERE id = :freelancerId")
                .param("partnerId", id)
                .param("freelancerId", freelancerId)
                .update();
        List<PartnerFreelancerView> updated = queryService.findFreelancersByPartner(id);
        return ResponseEntity.ok(Map.of("ok", true, "freelancers", updated));
    }

    @PostMapping("/{id}/remove-freelancer/{freelancerId}")
    @ResponseBody
    public ResponseEntity<?> removeFreelancer(@PathVariable Long id,
                                              @PathVariable Long freelancerId) {
        jdbcClient.sql("UPDATE freelancer SET partner_id = NULL WHERE id = :freelancerId AND partner_id = :partnerId")
                .param("freelancerId", freelancerId)
                .param("partnerId", id)
                .update();
        List<PartnerFreelancerView> updated = queryService.findFreelancersByPartner(id);
        return ResponseEntity.ok(Map.of("ok", true, "freelancers", updated));
    }
}
