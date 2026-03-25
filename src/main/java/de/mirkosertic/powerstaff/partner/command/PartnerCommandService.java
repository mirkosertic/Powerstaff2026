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

    public PartnerCommandService(final PartnerRepository partnerRepository,
                                 final PartnerContactRepository contactRepository,
                                 final PartnerHistoryRepository historyRepository,
                                 final JdbcClient jdbcClient) {
        this.partnerRepository = partnerRepository;
        this.contactRepository = contactRepository;
        this.historyRepository = historyRepository;
        this.jdbcClient = jdbcClient;
    }

    /**
     * Speichert nur die Partner-Stammdaten (ohne Kontakte/Historie).
     */
    public Partner save(final Partner partner) {
        return partnerRepository.save(partner);
    }

    /**
     * Speichert Partner-Stammdaten und verarbeitet Kontakt- und Historien-Delta-Commands.
     * Nur Einträge mit op="ADD" oder op="DELETE" werden verarbeitet;
     * unveränderte Einträge erhalten keinen neuen Audit-Timestamp.
     */
    public Partner save(final Partner partner,
                        final List<PartnerContactEntry> contactChanges,
                        final List<PartnerHistoryEntry> historyChanges) {
        final Partner saved = partnerRepository.save(partner);
        final long partnerId = saved.getId();

        // Kontakt-Delta verarbeiten
        for (final PartnerContactEntry cmd : contactChanges) {
            if ("ADD".equals(cmd.op())) {
                final PartnerContact contact = new PartnerContact();
                contact.setType(cmd.type());
                contact.setValue(cmd.value());
                contact.setPartnerId(partnerId);
                contactRepository.save(contact);
            } else if ("DELETE".equals(cmd.op())) {
                if (cmd.id() == null) {
                    throw new IllegalArgumentException("DELETE Kontakt erfordert eine ID");
                }
                contactRepository.deleteById(cmd.id());
            }
        }

        // Historie-Delta verarbeiten
        for (final PartnerHistoryEntry cmd : historyChanges) {
            if ("ADD".equals(cmd.op())) {
                final PartnerHistory history = new PartnerHistory();
                history.setDescription(cmd.description());
                history.setTypeId(cmd.typeId());
                history.setPartnerId(partnerId);
                historyRepository.save(history);
            } else if ("UPDATE".equals(cmd.op())) {
                if (cmd.id() == null) {
                    throw new IllegalArgumentException("UPDATE Kontakthistorie erfordert eine ID");
                }
                historyRepository.findById(cmd.id()).ifPresent(history -> {
                    history.setDescription(cmd.description());
                    history.setTypeId(cmd.typeId());
                    historyRepository.save(history);
                });
            } else if ("DELETE".equals(cmd.op())) {
                if (cmd.id() == null) {
                    throw new IllegalArgumentException("DELETE Kontakthistorie erfordert eine ID");
                }
                historyRepository.deleteById(cmd.id());
            }
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<Partner> findById(final long id) {
        return partnerRepository.findById(id);
    }

    /**
     * Löscht einen Partner. Wirft {@link PartnerHasProjectsException} wenn Projekte
     * auf diesen Partner verweisen (RESTRICT-Check vor dem eigentlichen Delete).
     */
    public void deleteById(final long id) {
        final List<Long> linkedProjectIds = jdbcClient
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
