package de.mirkosertic.powerstaff.customer.command;

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
public class KundeCommandService {

    private final KundeRepository kundeRepository;
    private final KundeContactRepository contactRepository;
    private final KundeHistoryRepository historyRepository;
    private final JdbcClient jdbcClient;

    public KundeCommandService(KundeRepository kundeRepository,
                               KundeContactRepository contactRepository,
                               KundeHistoryRepository historyRepository,
                               JdbcClient jdbcClient) {
        this.kundeRepository = kundeRepository;
        this.contactRepository = contactRepository;
        this.historyRepository = historyRepository;
        this.jdbcClient = jdbcClient;
    }

    public Kunde save(Kunde kunde) {
        return kundeRepository.save(kunde);
    }

    public Kunde save(Kunde kunde,
                      List<KundeContactEntry> contacts,
                      List<KundeHistoryEntry> history) {
        Kunde saved = kundeRepository.save(kunde);
        long kundeId = saved.getId();
        replaceContacts(kundeId, contacts);
        replaceHistory(kundeId, history);
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<Kunde> findById(long id) {
        return kundeRepository.findById(id);
    }

    public void deleteById(long id) {
        List<Long> linkedProjectIds = jdbcClient
                .sql("SELECT id FROM project WHERE customer_id = :kundeId")
                .param("kundeId", id)
                .query(Long.class)
                .list();

        if (!linkedProjectIds.isEmpty()) {
            throw new KundeHasProjectsException(linkedProjectIds);
        }

        kundeRepository.deleteById(id);
    }

    private void replaceContacts(long kundeId, List<KundeContactEntry> entries) {
        Set<Long> submittedIds = entries.stream()
                .filter(e -> e.id() != null)
                .map(KundeContactEntry::id)
                .collect(Collectors.toSet());

        contactRepository.findByKundeId(kundeId).stream()
                .filter(c -> !submittedIds.contains(c.getId()))
                .forEach(c -> contactRepository.deleteById(c.getId()));

        for (KundeContactEntry entry : entries) {
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
                KundeContact contact = new KundeContact();
                contact.setType(entry.type());
                contact.setValue(entry.value());
                contact.setKundeId(kundeId);
                contactRepository.save(contact);
            }
        }
    }

    private void replaceHistory(long kundeId, List<KundeHistoryEntry> entries) {
        Set<Long> submittedIds = entries.stream()
                .filter(e -> e.id() != null)
                .map(KundeHistoryEntry::id)
                .collect(Collectors.toSet());

        historyRepository.findByKundeId(kundeId).stream()
                .filter(h -> !submittedIds.contains(h.getId()))
                .forEach(h -> historyRepository.deleteById(h.getId()));

        for (KundeHistoryEntry entry : entries) {
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
                KundeHistory history = new KundeHistory();
                history.setDescription(entry.description());
                history.setTypeId(entry.typeId());
                history.setKundeId(kundeId);
                historyRepository.save(history);
            }
        }
    }
}
