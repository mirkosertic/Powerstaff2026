package de.mirkosertic.powerstaff.partner.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PartnerContactCommandService {

    private final PartnerContactRepository repository;

    public PartnerContactCommandService(PartnerContactRepository repository) {
        this.repository = repository;
    }

    public void create(Long partnerId, String type, String value) {
        var contact = new PartnerContact();
        contact.setPartnerId(partnerId);
        contact.setType(type);
        contact.setValue(value);
        repository.save(contact);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
