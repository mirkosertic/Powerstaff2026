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
    private final PartnerContactRepository contactRepository;
    private final PartnerHistoryRepository historyRepository;
    private final JdbcClient jdbcClient;

    public PartnerCommandService(PartnerRepository partnerRepository,
                                 PartnerContactRepository contactRepository,
                                 PartnerHistoryRepository historyRepository,
                                 JdbcClient jdbcClient) {
        this.partnerRepository = partnerRepository;
        this.contactRepository = contactRepository;
        this.historyRepository = historyRepository;
        this.jdbcClient = jdbcClient;
    }

    /**
     * Speichert nur die Partner-Stammdaten (ohne Kontakte/Historie).
     */
    public Partner save(Partner partner) {
        return partnerRepository.save(partner);
    }

    /**
     * Speichert Partner-Stammdaten und verarbeitet Kontakt- und Historien-Delta-Commands.
     * Nur Einträge mit op="ADD" oder op="DELETE" werden verarbeitet;
     * unveränderte Einträge erhalten keinen neuen Audit-Timestamp.
     */
    public Partner save(Partner partner,
                        List<PartnerContactEntry> contactChanges,
                        List<PartnerHistoryEntry> historyChanges) {
        Partner saved = partnerRepository.save(partner);
        long partnerId = saved.getId();

        // Kontakt-Delta verarbeiten
        for (PartnerContactEntry cmd : contactChanges) {
            if ("ADD".equals(cmd.op())) {
                PartnerContact contact = new PartnerContact();
                contact.setType(cmd.type());
                contact.setValue(cmd.value());
                contact.setPartnerId(partnerId);
                contactRepository.save(contact);
            } else if ("DELETE".equals(cmd.op()) && cmd.id() != null) {
                contactRepository.deleteById(cmd.id());
            }
        }

        // Historie-Delta verarbeiten
        for (PartnerHistoryEntry cmd : historyChanges) {
            if ("ADD".equals(cmd.op())) {
                PartnerHistory history = new PartnerHistory();
                history.setDescription(cmd.description());
                history.setTypeId(cmd.typeId());
                history.setPartnerId(partnerId);
                historyRepository.save(history);
            } else if ("UPDATE".equals(cmd.op()) && cmd.id() != null) {
                historyRepository.findById(cmd.id()).ifPresent(history -> {
                    history.setDescription(cmd.description());
                    history.setTypeId(cmd.typeId());
                    historyRepository.save(history);
                });
            } else if ("DELETE".equals(cmd.op()) && cmd.id() != null) {
                historyRepository.deleteById(cmd.id());
            }
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<Partner> findById(long id) {
        return partnerRepository.findById(id);
    }

    /**
     * Löscht einen Partner. Wirft {@link PartnerHasProjectsException} wenn Projekte
     * auf diesen Partner verweisen (RESTRICT-Check vor dem eigentlichen Delete).
     */
    public void deleteById(long id) {
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
