package de.mirkosertic.powerstaff.project.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectHistoryCommandService {

    private final ProjectHistoryRepository repository;

    public ProjectHistoryCommandService(ProjectHistoryRepository repository) {
        this.repository = repository;
    }

    public ProjectHistory save(ProjectHistory history) {
        return repository.save(history);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
