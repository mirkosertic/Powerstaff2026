package de.mirkosertic.powerstaff.project.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

    @Transactional(readOnly = true)
    public Optional<ProjectHistory> findById(Long id) {
        return repository.findById(id);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
