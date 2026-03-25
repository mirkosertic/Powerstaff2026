package de.mirkosertic.powerstaff.shared.api;

import de.mirkosertic.powerstaff.shared.TagType;
import de.mirkosertic.powerstaff.shared.command.HistoryType;
import de.mirkosertic.powerstaff.shared.command.ProjectPositionStatus;
import de.mirkosertic.powerstaff.shared.command.StammdatenCommandService;
import de.mirkosertic.powerstaff.shared.command.Tag;
import de.mirkosertic.powerstaff.shared.query.HistoryTypeQueryService;
import de.mirkosertic.powerstaff.shared.query.ProjectPositionStatusQueryService;
import de.mirkosertic.powerstaff.shared.query.TagQueryService;
import de.mirkosertic.powerstaff.shared.query.TagView;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class StammdatenController {

    private final HistoryTypeQueryService historyTypeQueryService;
    private final ProjectPositionStatusQueryService projectPositionStatusQueryService;
    private final TagQueryService tagQueryService;
    private final StammdatenCommandService stammdatenCommandService;

    public StammdatenController(
            final HistoryTypeQueryService historyTypeQueryService,
            final ProjectPositionStatusQueryService projectPositionStatusQueryService,
            final TagQueryService tagQueryService,
            final StammdatenCommandService stammdatenCommandService) {
        this.historyTypeQueryService = historyTypeQueryService;
        this.projectPositionStatusQueryService = projectPositionStatusQueryService;
        this.tagQueryService = tagQueryService;
        this.stammdatenCommandService = stammdatenCommandService;
    }

    // -------------------------------------------------------------------------
    // Redirect /admin → /admin/historientypen
    // -------------------------------------------------------------------------

    @GetMapping
    public String adminRoot() {
        return "redirect:/admin/historientypen";
    }

    // -------------------------------------------------------------------------
    // Historientypen
    // -------------------------------------------------------------------------

    @GetMapping("/historientypen")
    public String historientypen(final Model model) {
        model.addAttribute("types", historyTypeQueryService.findAll());
        model.addAttribute("newType", new HistoryType());
        model.addAttribute("activePage", "admin");
        return "admin/historientypen";
    }

    @PostMapping("/historientypen")
    public String createHistoryType(@ModelAttribute final HistoryType newType) {
        stammdatenCommandService.saveHistoryType(newType);
        return "redirect:/admin/historientypen";
    }

    @PostMapping("/historientypen/{id}")
    public String updateHistoryType(@PathVariable final long id,
                                    @RequestParam final String description) {
        stammdatenCommandService.findHistoryTypeById(id).ifPresent(ht -> {
            ht.setDescription(description);
            stammdatenCommandService.saveHistoryType(ht);
        });
        return "redirect:/admin/historientypen";
    }

    @PostMapping("/historientypen/{id}/delete")
    public String deleteHistoryType(@PathVariable final long id) {
        stammdatenCommandService.deleteHistoryType(id);
        return "redirect:/admin/historientypen";
    }

    // -------------------------------------------------------------------------
    // Projektpositions-Status
    // -------------------------------------------------------------------------

    @GetMapping("/positionsstatus")
    public String positionsstatus(final Model model) {
        model.addAttribute("statusList", projectPositionStatusQueryService.findAll());
        model.addAttribute("newStatus", new ProjectPositionStatus());
        model.addAttribute("activePage", "admin");
        return "admin/positionsstatus";
    }

    @PostMapping("/positionsstatus")
    public String createPositionsStatus(@ModelAttribute final ProjectPositionStatus newStatus) {
        stammdatenCommandService.saveProjectPositionStatus(newStatus);
        return "redirect:/admin/positionsstatus";
    }

    @PostMapping("/positionsstatus/{id}/delete")
    public String deletePositionsStatus(@PathVariable final long id) {
        stammdatenCommandService.deleteProjectPositionStatus(id);
        return "redirect:/admin/positionsstatus";
    }

    @PostMapping("/positionsstatus/{id}")
    public String updatePositionsStatus(@PathVariable final long id,
                                        @RequestParam final String description,
                                        @RequestParam final String color,
                                        @RequestParam final String colorText,
                                        @RequestParam(defaultValue = "false") final boolean defaultStatus) {
        stammdatenCommandService.findProjectPositionStatusById(id).ifPresent(pps -> {
            pps.setDescription(description);
            pps.setColor(color);
            pps.setColorText(colorText);
            pps.setDefaultStatus(defaultStatus);
            stammdatenCommandService.saveProjectPositionStatus(pps);
        });
        return "redirect:/admin/positionsstatus";
    }

    // -------------------------------------------------------------------------
    // Tags
    // -------------------------------------------------------------------------

    @GetMapping("/tags")
    public String tags(final Model model) {
        final Map<TagType, List<TagView>> tagsByType = new LinkedHashMap<>();
        for (final TagType type : TagType.values()) {
            tagsByType.put(type, tagQueryService.findByType(type));
        }
        model.addAttribute("tagsByType", tagsByType);
        model.addAttribute("tagTypes", TagType.values());
        model.addAttribute("newTag", new Tag());
        model.addAttribute("activePage", "admin");
        return "admin/tags";
    }

    @PostMapping("/tags")
    public String createTag(@RequestParam final String tagname,
                            @RequestParam final String tagType) {
        final Tag tag = new Tag(tagname, tagType);
        stammdatenCommandService.saveTag(tag);
        return "redirect:/admin/tags";
    }

    @PostMapping("/tags/{id}")
    public String updateTag(@PathVariable final long id,
                            @RequestParam final String tagname) {
        stammdatenCommandService.findTagById(id).ifPresent(tag -> {
            tag.setTagname(tagname);
            stammdatenCommandService.saveTag(tag);
        });
        return "redirect:/admin/tags";
    }

    @DeleteMapping("/tags/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> deleteTag(@PathVariable final long id) {
        stammdatenCommandService.deleteTag(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
