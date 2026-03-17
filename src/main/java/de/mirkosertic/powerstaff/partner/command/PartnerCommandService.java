package de.mirkosertic.powerstaff.partner.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class PartnerCommandService {

    private final PartnerRepository partnerRepository;

    public PartnerCommandService(PartnerRepository partnerRepository) {
        this.partnerRepository = partnerRepository;
    }

    public Partner save(Partner partner) {
        return partnerRepository.save(partner);
    }

    @Transactional(readOnly = true)
    public Optional<Partner> findById(Long id) {
        return partnerRepository.findById(id);
    }

    public void deleteById(Long id) {
        partnerRepository.deleteById(id);
    }
}
