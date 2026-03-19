package de.mirkosertic.powerstaff.partner.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PartnerHistoryCommandService {

    private final PartnerHistoryRepository repository;

    public PartnerHistoryCommandService(PartnerHistoryRepository repository) {
        this.repository = repository;
    }

    public void create(Long partnerId, Long typeId, String description) {
        var history = new PartnerHistory();
        history.setPartnerId(partnerId);
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
