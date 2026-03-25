package de.mirkosertic.powerstaff.customer.command;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class KundeCommandService {

    private final KundeRepository kundeRepository;
    private final KundeContactRepository contactRepository;
    private final KundeHistoryRepository historyRepository;
    private final JdbcClient jdbcClient;

    public KundeCommandService(final KundeRepository kundeRepository,
                               final KundeContactRepository contactRepository,
                               final KundeHistoryRepository historyRepository,
                               final JdbcClient jdbcClient) {
        this.kundeRepository = kundeRepository;
        this.contactRepository = contactRepository;
        this.historyRepository = historyRepository;
        this.jdbcClient = jdbcClient;
    }

    public Kunde save(final Kunde kunde) {
        return kundeRepository.save(kunde);
    }

    /**
     * Speichert Kunden-Stammdaten und verarbeitet Kontakt- und Historien-Delta-Commands.
     * Nur Einträge mit op="ADD" oder op="DELETE" werden verarbeitet;
     * unveränderte Einträge erhalten keinen neuen Audit-Timestamp.
     */
    public Kunde save(final Kunde kunde,
                      final List<KundeContactEntry> contactChanges,
                      final List<KundeHistoryEntry> historyChanges) {
        final Kunde saved = kundeRepository.save(kunde);
        final long kundeId = saved.getId();

        // Kontakt-Delta verarbeiten
        for (final KundeContactEntry cmd : contactChanges) {
            if ("ADD".equals(cmd.op())) {
                final KundeContact contact = new KundeContact();
                contact.setType(cmd.type());
                contact.setValue(cmd.value());
                contact.setKundeId(kundeId);
                contactRepository.save(contact);
            } else if ("DELETE".equals(cmd.op())) {
                if (cmd.id() == null) {
                    throw new IllegalArgumentException("DELETE Kontakt erfordert eine ID");
                }
                contactRepository.deleteById(cmd.id());
            }
        }

        // Historie-Delta verarbeiten
        for (final KundeHistoryEntry cmd : historyChanges) {
            if ("ADD".equals(cmd.op())) {
                final KundeHistory history = new KundeHistory();
                history.setDescription(cmd.description());
                history.setTypeId(cmd.typeId());
                history.setKundeId(kundeId);
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
    public Optional<Kunde> findById(final long id) {
        return kundeRepository.findById(id);
    }

    public void deleteById(final long id) {
        final List<Long> linkedProjectIds = jdbcClient
                .sql("SELECT id FROM project WHERE customer_id = :kundeId")
                .param("kundeId", id)
                .query(Long.class)
                .list();

        if (!linkedProjectIds.isEmpty()) {
            throw new KundeHasProjectsException(linkedProjectIds);
        }

        kundeRepository.deleteById(id);
    }
}
