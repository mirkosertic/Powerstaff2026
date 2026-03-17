package de.mirkosertic.powerstaff.partner.command;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PartnerCommandService {

    private final PartnerRepository partnerRepository;
    private final JdbcClient jdbcClient;

    public PartnerCommandService(PartnerRepository partnerRepository, JdbcClient jdbcClient) {
        this.partnerRepository = partnerRepository;
        this.jdbcClient = jdbcClient;
    }

    public Partner save(Partner partner) {
        return partnerRepository.save(partner);
    }

    @Transactional(readOnly = true)
    public Optional<Partner> findById(Long id) {
        return partnerRepository.findById(id);
    }

    /**
     * Deletes a partner. Throws {@link PartnerHasProjectsException} if any project
     * references this partner (RESTRICT check performed before the actual delete).
     */
    public void deleteById(Long id) {
        List<Long> linkedProjectIds = jdbcClient
                .sql("SELECT id FROM project WHERE partner_id = :partnerId")
                .param("partnerId", id)
                .query(Long.class)
                .list();

        if (!linkedProjectIds.isEmpty()) {
            throw new PartnerHasProjectsException(linkedProjectIds);
        }

        partnerRepository.deleteById(id);
    }
}
