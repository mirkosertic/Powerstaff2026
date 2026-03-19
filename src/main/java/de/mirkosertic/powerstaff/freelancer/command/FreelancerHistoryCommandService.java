package de.mirkosertic.powerstaff.freelancer.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FreelancerHistoryCommandService {

    private final FreelancerHistoryRepository repository;

    public FreelancerHistoryCommandService(FreelancerHistoryRepository repository) {
        this.repository = repository;
    }

    public void create(Long freelancerId, Long typeId, String description) {
        var history = new FreelancerHistory();
        history.setFreelancerId(freelancerId);
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
