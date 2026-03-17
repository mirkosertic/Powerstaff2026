package de.mirkosertic.powerstaff.partner.api;

import de.mirkosertic.powerstaff.partner.command.PartnerContact;
import de.mirkosertic.powerstaff.partner.command.PartnerContactCommandService;
import de.mirkosertic.powerstaff.partner.query.PartnerQueryService;
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

    private final PartnerContactCommandService contactCommandService;
    private final PartnerQueryService queryService;

    public PartnerContactController(PartnerContactCommandService contactCommandService,
                                    PartnerQueryService queryService) {
        this.contactCommandService = contactCommandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<?> addContact(@PathVariable Long partnerId,
                                        @RequestBody Map<String, String> body) {
        var contact = new PartnerContact();
        contact.setType(body.get("type"));
        contact.setValue(body.get("value"));
        contact.setPartnerId(partnerId);
        contactCommandService.save(contact);
        return ResponseEntity.ok(Map.of("ok", true, "contacts", queryService.findContactsByPartner(partnerId)));
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<?> deleteContact(@PathVariable Long partnerId,
                                           @PathVariable Long contactId) {
        contactCommandService.deleteById(contactId);
        return ResponseEntity.ok(Map.of("ok", true, "contacts", queryService.findContactsByPartner(partnerId)));
    }
}
