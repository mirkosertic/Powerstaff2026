package de.mirkosertic.powerstaff.partner.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

    @Transactional(readOnly = true)
    public Optional<PartnerHistory> findById(Long historyId) {
        return historyRepository.findById(historyId);
    }

    public void deleteById(Long historyId) {
        historyRepository.deleteById(historyId);
    }
}
