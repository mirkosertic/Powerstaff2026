package de.mirkosertic.powerstaff.partner.api;

import de.mirkosertic.powerstaff.partner.command.PartnerContactCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/partner/{partnerId}/contacts")
public class PartnerContactController {

    private final PartnerContactCommandService commandService;

    public PartnerContactController(PartnerContactCommandService commandService) {
        this.commandService = commandService;
    }

    record ContactRequest(String type, String value) {}

    @PostMapping
    public ResponseEntity<Map<String, Boolean>> create(@PathVariable Long partnerId,
                                                        @RequestBody ContactRequest request) {
        commandService.create(partnerId, request.type(), request.value());
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<Map<String, Boolean>> delete(@PathVariable Long partnerId,
                                                       @PathVariable Long contactId) {
        commandService.delete(contactId);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
