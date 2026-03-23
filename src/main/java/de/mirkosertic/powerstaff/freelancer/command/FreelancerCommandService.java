package de.mirkosertic.powerstaff.freelancer.command;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FreelancerCommandService {

    private final FreelancerRepository freelancerRepository;
    private final FreelancerContactRepository contactRepository;
    private final FreelancerHistoryRepository historyRepository;
    private final FreelancerTagCommandService tagCommandService;
    private final JdbcClient jdbcClient;

    public FreelancerCommandService(FreelancerRepository freelancerRepository,
                                    FreelancerContactRepository contactRepository,
                                    FreelancerHistoryRepository historyRepository,
                                    FreelancerTagCommandService tagCommandService,
                                    JdbcClient jdbcClient) {
        this.freelancerRepository = freelancerRepository;
        this.contactRepository = contactRepository;
        this.historyRepository = historyRepository;
        this.tagCommandService = tagCommandService;
        this.jdbcClient = jdbcClient;
    }

    /**
     * Speichert Freiberufler-Stammdaten, Kontakte, Historie und Tags via Delta-Commands.
     * Nur Einträge mit op="ADD" oder op="DELETE" werden verarbeitet;
     * unveränderte Einträge erhalten keinen neuen Audit-Timestamp.
     * Leerer Code wird als NULL gespeichert. Wirft {@link DuplicateCodeException}
     * wenn der Code bereits von einem anderen Freiberufler verwendet wird.
     */
    /** Convenience-Methode für Tests und interne Aufrufe ohne Delta-Listen. */
    public Freelancer save(Freelancer freelancer) {
        return save(freelancer, List.of(), List.of(), List.of());
    }

    public Freelancer save(Freelancer freelancer,
                           List<FreelancerContactEntry> contactChanges,
                           List<FreelancerHistoryEntry> historyChanges,
                           List<FreelancerTagEntry> tagChanges) {
        // Leeren Code → NULL normalisieren (verhindert UNIQUE-Constraint-Verletzung)
        if (freelancer.getCode() != null && freelancer.getCode().isBlank()) {
            freelancer.setCode(null);
        }
        // Dubletten-Prüfung nur wenn Code gesetzt
        if (freelancer.getCode() != null) {
            freelancerRepository.findByCode(freelancer.getCode()).ifPresent(existing -> {
                if (!existing.getId().equals(freelancer.getId())) {
                    throw new DuplicateCodeException(freelancer.getCode());
                }
            });
        }

        Freelancer saved = freelancerRepository.save(freelancer);
        long freelancerId = saved.getId();

        // Kontakt-Delta verarbeiten
        for (FreelancerContactEntry cmd : contactChanges) {
            if ("ADD".equals(cmd.op())) {
                FreelancerContact contact = new FreelancerContact();
                contact.setType(cmd.type());
                contact.setValue(cmd.value());
                contact.setFreelancerId(freelancerId);
                contactRepository.save(contact);
            } else if ("DELETE".equals(cmd.op())) {
                if (cmd.id() == null) {
                    throw new IllegalArgumentException("DELETE Kontakt erfordert eine ID");
                }
                contactRepository.deleteById(cmd.id());
            }
        }

        // Historie-Delta verarbeiten
        for (FreelancerHistoryEntry cmd : historyChanges) {
            if ("ADD".equals(cmd.op())) {
                FreelancerHistory history = new FreelancerHistory();
                history.setDescription(cmd.description());
                history.setTypeId(cmd.typeId());
                history.setFreelancerId(freelancerId);
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

        // Tag-Delta verarbeiten
        for (FreelancerTagEntry cmd : tagChanges) {
            if ("ADD".equals(cmd.op()) && cmd.tagId() != null) {
                try {
                    tagCommandService.addTag(freelancerId, cmd.tagId());
                } catch (DuplicateTagException ignored) {
                    // Tag bereits zugeordnet – ignorieren (idempotent)
                }
            } else if ("DELETE".equals(cmd.op()) && cmd.tagId() != null) {
                tagCommandService.removeTagByTagId(freelancerId, cmd.tagId());
            }
        }

        return saved;
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
