package de.mirkosertic.powerstaff.customer.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class KundeHistoryCommandService {

    private final KundeHistoryRepository repository;

    public KundeHistoryCommandService(KundeHistoryRepository repository) {
        this.repository = repository;
    }

    public void create(Long kundeId, Long typeId, String description) {
        var history = new KundeHistory();
        history.setKundeId(kundeId);
        history.setTypeId(typeId);
        history.setDescription(description);
        repository.save(history);
    }

    public void update(Long historyId, Long typeId, String description) {
        repository.findById(historyId).ifPresent(history -> {
            history.setTypeId(typeId);
            history.setDescription(description);
            repository.save(history);
        });
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
