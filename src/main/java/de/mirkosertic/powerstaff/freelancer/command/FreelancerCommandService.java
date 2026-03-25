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

    public FreelancerCommandService(final FreelancerRepository freelancerRepository,
                                    final FreelancerContactRepository contactRepository,
                                    final FreelancerHistoryRepository historyRepository,
                                    final FreelancerTagCommandService tagCommandService,
                                    final JdbcClient jdbcClient) {
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
    public Freelancer save(final Freelancer freelancer) {
        return save(freelancer, List.of(), List.of(), List.of());
    }

    public Freelancer save(final Freelancer freelancer,
                           final List<FreelancerContactEntry> contactChanges,
                           final List<FreelancerHistoryEntry> historyChanges,
                           final List<FreelancerTagEntry> tagChanges) {
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

        final Freelancer saved = freelancerRepository.save(freelancer);
        final long freelancerId = saved.getId();

        // Kontakt-Delta verarbeiten
        for (final FreelancerContactEntry cmd : contactChanges) {
            if ("ADD".equals(cmd.op())) {
                final FreelancerContact contact = new FreelancerContact();
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
        for (final FreelancerHistoryEntry cmd : historyChanges) {
            if ("ADD".equals(cmd.op())) {
                final FreelancerHistory history = new FreelancerHistory();
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
        for (final FreelancerTagEntry cmd : tagChanges) {
            if ("ADD".equals(cmd.op()) && cmd.tagId() != null) {
                try {
                    tagCommandService.addTag(freelancerId, cmd.tagId());
                } catch (final DuplicateTagException ignored) {
                    // Tag bereits zugeordnet – ignorieren (idempotent)
                }
            } else if ("DELETE".equals(cmd.op()) && cmd.tagId() != null) {
                tagCommandService.removeTagByTagId(freelancerId, cmd.tagId());
            }
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<Freelancer> findById(final long id) {
        return freelancerRepository.findById(id);
    }

    /**
     * Sucht einen Freiberufler anhand seines anonymisierten Codes.
     * Liefert ein öffentliches Lookup-Ergebnis für Cross-Modul-Nutzung.
     */
    @Transactional(readOnly = true)
    public Optional<FreelancerLookupResult> findByCode(final String code) {
        return freelancerRepository.findByCode(code)
                .map(f -> new FreelancerLookupResult(f.getId(), f.getPartnerId(), f.getCompany()));
    }

    /**
     * Löscht einen Freiberufler. Wirft {@link FreelancerHasPositionsException} wenn aktive
     * Projektpositionen auf diesen Freiberufler verweisen.
     */
    public void deleteById(final long id) {
        final List<Long> linkedProjectIds = jdbcClient
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
    public void assignToPartner(final long freelancerId, final long partnerId) {
        jdbcClient.sql("UPDATE freelancer SET partner_id = :partnerId WHERE id = :freelancerId")
                .param("partnerId", partnerId)
                .param("freelancerId", freelancerId)
                .update();
    }

    /**
     * Löst die Zuordnung eines Freiberuflers zu einem Partner auf.
     */
    public void removeFromPartner(final long freelancerId, final long partnerId) {
        jdbcClient.sql("UPDATE freelancer SET partner_id = NULL WHERE id = :freelancerId AND partner_id = :partnerId")
                .param("freelancerId", freelancerId)
                .param("partnerId", partnerId)
                .update();
    }
}
