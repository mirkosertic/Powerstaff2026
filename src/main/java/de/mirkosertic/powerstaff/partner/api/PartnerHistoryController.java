package de.mirkosertic.powerstaff.partner.api;

import de.mirkosertic.powerstaff.partner.command.PartnerHistory;
import de.mirkosertic.powerstaff.partner.command.PartnerHistoryCommandService;
import de.mirkosertic.powerstaff.partner.query.PartnerQueryService;
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
@RequestMapping("/partner/{partnerId}/history")
public class PartnerHistoryController {

    private final PartnerHistoryCommandService historyCommandService;
    private final PartnerQueryService queryService;

    public PartnerHistoryController(PartnerHistoryCommandService historyCommandService,
                                    PartnerQueryService queryService) {
        this.historyCommandService = historyCommandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<?> addHistory(@PathVariable Long partnerId,
                                        @RequestBody Map<String, Object> body) {
        var history = new PartnerHistory();
        history.setTypeId(((Number) body.get("typeId")).longValue());
        history.setDescription((String) body.get("description"));
        history.setPartnerId(partnerId);
        historyCommandService.save(history);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PutMapping("/{historyId}")
    public ResponseEntity<?> updateHistory(@PathVariable Long partnerId,
                                           @PathVariable Long historyId,
                                           @RequestBody Map<String, Object> body) {
        var existing = historyCommandService.findById(historyId);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var history = existing.get();
        if (body.containsKey("typeId")) {
            history.setTypeId(((Number) body.get("typeId")).longValue());
        }
        if (body.containsKey("description")) {
            history.setDescription((String) body.get("description"));
        }
        historyCommandService.save(history);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @DeleteMapping("/{historyId}")
    public ResponseEntity<?> deleteHistory(@PathVariable Long partnerId,
                                           @PathVariable Long historyId) {
        historyCommandService.deleteById(historyId);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
