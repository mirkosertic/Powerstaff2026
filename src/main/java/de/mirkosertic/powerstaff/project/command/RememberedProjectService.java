package de.mirkosertic.powerstaff.project.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class RememberedProjectService {

    private final RememberedProjectRepository repository;

    public RememberedProjectService(RememberedProjectRepository repository) {
        this.repository = repository;
    }

    /**
     * Setzt das gemerkte Projekt für einen User (Upsert).
     * Existiert bereits ein Eintrag, wird er überschrieben.
     */
    public void set(String userId, Long projectId) {
        boolean exists = repository.existsById(userId);
        repository.save(new RememberedProject(userId, projectId, !exists));
    }

    @Transactional(readOnly = true)
    public Optional<Long> get(String userId) {
        return repository.findById(userId).map(RememberedProject::getProjectId);
    }

    public void clear(String userId) {
        repository.deleteById(userId);
    }
}
