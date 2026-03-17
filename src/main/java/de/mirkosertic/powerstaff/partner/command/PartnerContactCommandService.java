package de.mirkosertic.powerstaff.partner.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class PartnerContactCommandService {

    private final PartnerContactRepository contactRepository;

    public PartnerContactCommandService(PartnerContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public PartnerContact save(PartnerContact contact) {
        return contactRepository.save(contact);
    }

    @Transactional(readOnly = true)
    public Optional<PartnerContact> findById(Long id) {
        return contactRepository.findById(id);
    }

    public void deleteById(Long id) {
        contactRepository.deleteById(id);
    }
}
