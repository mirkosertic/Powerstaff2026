package de.mirkosertic.powerstaff.project.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;

import java.util.Optional;

@Service
@Transactional
public class ProjectCommandService {

    private final ProjectRepository projectRepository;
    private final ProjectValidator projectValidator;

    public ProjectCommandService(ProjectRepository projectRepository,
                                 ProjectValidator projectValidator) {
        this.projectRepository = projectRepository;
        this.projectValidator = projectValidator;
    }

    public Project save(Project project) {
        var errors = new BeanPropertyBindingResult(project, "project");
        projectValidator.validate(project, errors);
        if (errors.hasErrors()) {
            throw new BothFKsException();
        }
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
