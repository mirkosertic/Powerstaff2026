package de.mirkosertic.powerstaff.partner.command;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
     * Speichert Partner-Stammdaten, Kontaktmöglichkeiten und Historieneinträge
     * in einer einzigen Transaktion (Unified Save).
     * Replace-Logik: DELETE für fehlende IDs, UPDATE für vorhandene, INSERT für id == null.
     */
    public Partner save(Partner partner,
                        List<PartnerContactEntry> contacts,
                        List<PartnerHistoryEntry> history) {
        Partner saved = partnerRepository.save(partner);
        Long partnerId = saved.getId();
        replaceContacts(partnerId, contacts);
        replaceHistory(partnerId, history);
        return saved;
    }

    private void replaceContacts(Long partnerId, List<PartnerContactEntry> entries) {
        Set<Long> submittedIds = entries.stream()
                .filter(e -> e.id() != null)
                .map(PartnerContactEntry::id)
                .collect(Collectors.toSet());

        contactRepository.findByPartnerId(partnerId).stream()
                .filter(c -> !submittedIds.contains(c.getId()))
                .forEach(c -> contactRepository.deleteById(c.getId()));

        for (PartnerContactEntry entry : entries) {
            PartnerContact contact;
            if (entry.id() != null) {
                contact = contactRepository.findById(entry.id()).orElseGet(PartnerContact::new);
            } else {
                contact = new PartnerContact();
            }
            contact.setType(entry.type());
            contact.setValue(entry.value());
            contact.setPartnerId(partnerId);
            contactRepository.save(contact);
        }
    }

    private void replaceHistory(Long partnerId, List<PartnerHistoryEntry> entries) {
        Set<Long> submittedIds = entries.stream()
                .filter(e -> e.id() != null)
                .map(PartnerHistoryEntry::id)
                .collect(Collectors.toSet());

        historyRepository.findByPartnerId(partnerId).stream()
                .filter(h -> !submittedIds.contains(h.getId()))
                .forEach(h -> historyRepository.deleteById(h.getId()));

        for (PartnerHistoryEntry entry : entries) {
            PartnerHistory history;
            if (entry.id() != null) {
                history = historyRepository.findById(entry.id()).orElseGet(PartnerHistory::new);
            } else {
                history = new PartnerHistory();
            }
            history.setDescription(entry.description());
            history.setTypeId(entry.typeId());
            history.setPartnerId(partnerId);
            historyRepository.save(history);
        }
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
