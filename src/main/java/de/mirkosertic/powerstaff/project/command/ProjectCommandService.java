package de.mirkosertic.powerstaff.project.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class ProjectCommandService {

    private final ProjectRepository projectRepository;

    public ProjectCommandService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Project save(Project project) {
        return projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public Optional<Project> findById(long id) {
        return projectRepository.findById(id);
    }

    public void deleteById(long id) {
        projectRepository.deleteById(id);
    }
}
