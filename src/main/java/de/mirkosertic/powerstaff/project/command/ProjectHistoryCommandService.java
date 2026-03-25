package de.mirkosertic.powerstaff.project.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class ProjectHistoryCommandService {

    private final ProjectHistoryRepository repository;

    public ProjectHistoryCommandService(final ProjectHistoryRepository repository) {
        this.repository = repository;
    }

    public ProjectHistory save(final ProjectHistory history) {
        return repository.save(history);
    }

    @Transactional(readOnly = true)
    public Optional<ProjectHistory> findById(final Long id) {
        return repository.findById(id);
    }

    public void delete(final Long id) {
        repository.deleteById(id);
    }
}
