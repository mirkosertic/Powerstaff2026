package de.mirkosertic.powerstaff.freelancer.command;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class FreelancerCommandService {

    private final FreelancerRepository freelancerRepository;
    private final FreelancerContactRepository contactRepository;
    private final FreelancerHistoryRepository historyRepository;
    private final JdbcClient jdbcClient;

    public FreelancerCommandService(FreelancerRepository freelancerRepository,
                                    FreelancerContactRepository contactRepository,
                                    FreelancerHistoryRepository historyRepository,
                                    JdbcClient jdbcClient) {
        this.freelancerRepository = freelancerRepository;
        this.contactRepository = contactRepository;
        this.historyRepository = historyRepository;
        this.jdbcClient = jdbcClient;
    }

    /**
     * Speichert nur die Freiberufler-Stammdaten.
     */
    public Freelancer save(Freelancer freelancer) {
        return freelancerRepository.save(freelancer);
    }

    /**
     * Speichert Freiberufler-Stammdaten und Kontakte. Neue History-Einträge (id==null)
     * werden appended; bestehende History-Einträge bleiben unberührt.
     */
    public Freelancer save(Freelancer freelancer, List<FreelancerContactEntry> contacts,
                           List<FreelancerHistoryEntry> newHistoryEntries) {
        Freelancer saved = freelancerRepository.save(freelancer);
        long freelancerId = saved.getId();
        replaceContacts(freelancerId, contacts);
        for (FreelancerHistoryEntry entry : newHistoryEntries) {
            if (entry.id() == null) {
                FreelancerHistory history = new FreelancerHistory();
                history.setDescription(entry.description());
                history.setTypeId(entry.typeId());
                history.setFreelancerId(freelancerId);
                historyRepository.save(history);
            }
        }
        return saved;
    }

    private void replaceContacts(long freelancerId, List<FreelancerContactEntry> entries) {
        Set<Long> submittedIds = entries.stream()
                .filter(e -> e.id() != null)
                .map(FreelancerContactEntry::id)
                .collect(Collectors.toSet());

        contactRepository.findByFreelancerId(freelancerId).stream()
                .filter(c -> !submittedIds.contains(c.getId()))
                .forEach(c -> contactRepository.deleteById(c.getId()));

        for (FreelancerContactEntry entry : entries) {
            if (entry.id() != null) {
                contactRepository.findById(entry.id()).ifPresent(contact -> {
                    boolean changed = !Objects.equals(entry.type(), contact.getType())
                            || !Objects.equals(entry.value(), contact.getValue());
                    if (changed) {
                        contact.setType(entry.type());
                        contact.setValue(entry.value());
                        contactRepository.save(contact);
                    }
                });
            } else {
                FreelancerContact contact = new FreelancerContact();
                contact.setType(entry.type());
                contact.setValue(entry.value());
                contact.setFreelancerId(freelancerId);
                contactRepository.save(contact);
            }
        }
    }

    private void replaceHistory(long freelancerId, List<FreelancerHistoryEntry> entries) {
        Set<Long> submittedIds = entries.stream()
                .filter(e -> e.id() != null)
                .map(FreelancerHistoryEntry::id)
                .collect(Collectors.toSet());

        historyRepository.findByFreelancerId(freelancerId).stream()
                .filter(h -> !submittedIds.contains(h.getId()))
                .forEach(h -> historyRepository.deleteById(h.getId()));

        for (FreelancerHistoryEntry entry : entries) {
            if (entry.id() != null) {
                historyRepository.findById(entry.id()).ifPresent(history -> {
                    boolean changed = !Objects.equals(entry.description(), history.getDescription())
                            || !Objects.equals(entry.typeId(), history.getTypeId());
                    if (changed) {
                        history.setDescription(entry.description());
                        history.setTypeId(entry.typeId());
                        historyRepository.save(history);
                    }
                });
            } else {
                FreelancerHistory history = new FreelancerHistory();
                history.setDescription(entry.description());
                history.setTypeId(entry.typeId());
                history.setFreelancerId(freelancerId);
                historyRepository.save(history);
            }
        }
    }

    @Transactional(readOnly = true)
    public Optional<Freelancer> findById(long id) {
        return freelancerRepository.findById(id);
    }

    /**
     * Sucht einen Freiberufler anhand seines anonymisierten Codes.
     * Liefert ein öffentliches Lookup-Ergebnis für Cross-Modul-Nutzung.
     */
    @Transactional(readOnly = true)
    public Optional<FreelancerLookupResult> findByCode(String code) {
        return freelancerRepository.findByCode(code)
                .map(f -> new FreelancerLookupResult(f.getId(), f.getPartnerId(), f.getCompany()));
    }

    /**
     * Löscht einen Freiberufler. Wirft {@link FreelancerHasPositionsException} wenn aktive
     * Projektpositionen auf diesen Freiberufler verweisen.
     */
    public void deleteById(long id) {
        List<Long> linkedProjectIds = jdbcClient
                .sql("SELECT project_id FROM project_position WHERE freelancer_id = :freelancerId")
                .param("freelancerId", id)
                .query(Long.class)
                .list();

        if (!linkedProjectIds.isEmpty()) {
            throw new FreelancerHasPositionsException(linkedProjectIds);
        }

        freelancerRepository.deleteById(id);
    }

    /**
     * Ordnet einen Freiberufler einem Partner zu.
     * Targeted UPDATE — kein vollständiges Aggregate-Save, um den Audit-Trail zu schützen.
     */
    public void assignToPartner(long freelancerId, long partnerId) {
        jdbcClient.sql("UPDATE freelancer SET partner_id = :partnerId WHERE id = :freelancerId")
                .param("partnerId", partnerId)
                .param("freelancerId", freelancerId)
                .update();
    }

    /**
     * Löst die Zuordnung eines Freiberuflers zu einem Partner auf.
     */
    public void removeFromPartner(long freelancerId, long partnerId) {
        jdbcClient.sql("UPDATE freelancer SET partner_id = NULL WHERE id = :freelancerId AND partner_id = :partnerId")
                .param("freelancerId", freelancerId)
                .param("partnerId", partnerId)
                .update();
    }
}
