package de.mirkosertic.powerstaff.partner.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PartnerHistoryCommandService {

    private final PartnerHistoryRepository historyRepository;

    public PartnerHistoryCommandService(PartnerHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public PartnerHistory save(PartnerHistory history) {
        return historyRepository.save(history);
    }

    public void deleteById(Long historyId) {
        historyRepository.deleteById(historyId);
    }
}
