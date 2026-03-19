package de.mirkosertic.powerstaff.freelancer.api;

import de.mirkosertic.powerstaff.freelancer.command.FreelancerHistoryCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/freelancer/{freelancerId}/history")
public class FreelancerHistoryController {

    private final FreelancerHistoryCommandService commandService;

    public FreelancerHistoryController(FreelancerHistoryCommandService commandService) {
        this.commandService = commandService;
    }

    record HistoryRequest(Long typeId, String description) {}

    @PostMapping
    public ResponseEntity<Map<String, Boolean>> create(@PathVariable Long freelancerId,
                                                        @RequestBody HistoryRequest request) {
        commandService.create(freelancerId, request.typeId(), request.description());
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PutMapping("/{historyId}")
    public ResponseEntity<Map<String, Boolean>> update(@PathVariable Long freelancerId,
                                                       @PathVariable Long historyId,
                                                       @RequestBody HistoryRequest request) {
        commandService.update(historyId, request.typeId(), request.description());
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @DeleteMapping("/{historyId}")
    public ResponseEntity<Map<String, Boolean>> delete(@PathVariable Long freelancerId,
                                                       @PathVariable Long historyId) {
        commandService.delete(historyId);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
