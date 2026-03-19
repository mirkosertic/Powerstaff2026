package de.mirkosertic.powerstaff.customer.api;

import de.mirkosertic.powerstaff.customer.command.KundeHistoryCommandService;
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
@RequestMapping("/kunde/{kundeId}/history")
public class KundeHistoryController {

    private final KundeHistoryCommandService commandService;

    public KundeHistoryController(KundeHistoryCommandService commandService) {
        this.commandService = commandService;
    }

    record HistoryRequest(Long typeId, String description) {}

    @PostMapping
    public ResponseEntity<Map<String, Boolean>> create(@PathVariable Long kundeId,
                                                        @RequestBody HistoryRequest request) {
        commandService.create(kundeId, request.typeId(), request.description());
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PutMapping("/{historyId}")
    public ResponseEntity<Map<String, Boolean>> update(@PathVariable Long kundeId,
                                                       @PathVariable Long historyId,
                                                       @RequestBody HistoryRequest request) {
        commandService.update(historyId, request.typeId(), request.description());
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @DeleteMapping("/{historyId}")
    public ResponseEntity<Map<String, Boolean>> delete(@PathVariable Long kundeId,
                                                       @PathVariable Long historyId) {
        commandService.delete(historyId);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
