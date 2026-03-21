package de.mirkosertic.powerstaff.project.command;

import de.mirkosertic.powerstaff.project.query.ProjectQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class RememberedProjectService {

    private final RememberedProjectRepository repository;
    private final ProjectQueryService projectQueryService;

    public RememberedProjectService(RememberedProjectRepository repository,
                                    ProjectQueryService projectQueryService) {
        this.repository = repository;
        this.projectQueryService = projectQueryService;
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

    /**
     * Gibt die Anzeige-Infos des gemerkten Projekts zurück.
     * Für andere Module – vermeidet direkten Zugriff auf ProjectQueryService aus Fremd-Modulen.
     */
    @Transactional(readOnly = true)
    public Optional<RememberedProjectInfo> getRememberedProjectInfo(String userId) {
        return get(userId)
                .flatMap(projectQueryService::findById)
                .map(p -> new RememberedProjectInfo(p.id(), p.projectNumber(), p.descriptionShort()));
    }
}
