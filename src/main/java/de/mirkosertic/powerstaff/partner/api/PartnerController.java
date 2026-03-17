package de.mirkosertic.powerstaff.partner.api;

import de.mirkosertic.powerstaff.partner.command.Partner;
import de.mirkosertic.powerstaff.partner.command.PartnerCommandService;
import de.mirkosertic.powerstaff.partner.command.PartnerHasProjectsException;
import de.mirkosertic.powerstaff.partner.query.PartnerQueryService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/partner")
public class PartnerController {

    private static final String SESSION_LAST_PARTNER_ID = "lastPartnerId";

    private final PartnerCommandService commandService;
    private final PartnerQueryService queryService;

    public PartnerController(PartnerCommandService commandService, PartnerQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
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
}
