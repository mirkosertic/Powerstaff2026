package de.mirkosertic.powerstaff.project.command;

import de.mirkosertic.powerstaff.shared.query.ProjectPositionStatusQueryService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectPositionCommandService {

    private final ProjectPositionRepository repository;
    private final ProjectPositionStatusQueryService positionStatusQueryService;

    public ProjectPositionCommandService(ProjectPositionRepository repository,
                                         ProjectPositionStatusQueryService positionStatusQueryService) {
        this.repository = repository;
        this.positionStatusQueryService = positionStatusQueryService;
    }

    public ProjectPosition save(ProjectPosition position) {
        try {
            return repository.save(position);
        } catch (DataIntegrityViolationException e) {
            if (position.getProjectId() != null && position.getFreelancerId() != null) {
                throw new FreelancerAlreadyAssignedException(position.getFreelancerId(), position.getProjectId());
            }
            throw e;
        }
    }

    /**
     * Assigns a freelancer to a project. Delegates from FreelancerController (ADR-018).
     * If statusId is null, the configured default ProjectPositionStatus is used.
     */
    public void assignFreelancerToProject(long freelancerId, long projectId, Long statusId, String konditionen, String kommentar) {
        Long resolvedStatusId = statusId;
        if (resolvedStatusId == null) {
            resolvedStatusId = positionStatusQueryService.findDefault()
                    .map(s -> s.id())
                    .orElseThrow(() -> new IllegalStateException(
                            "Kein Standard-Positionsstatus konfiguriert. Bitte im Administrationsbereich einen Standard-Status festlegen."));
        }
        var position = new ProjectPosition();
        position.setProjectId(projectId);
        position.setFreelancerId(freelancerId);
        position.setStatusId(resolvedStatusId);
        position.setKonditionen(konditionen);
        position.setKommentar(kommentar);
        try {
            repository.save(position);
        } catch (DataIntegrityViolationException e) {
            throw new FreelancerAlreadyAssignedException(freelancerId, projectId);
        }
    }

    public void delete(Long positionId) {
        repository.deleteById(positionId);
    }
}
