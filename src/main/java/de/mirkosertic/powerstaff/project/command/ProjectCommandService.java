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

    public ProjectCommandService(final ProjectRepository projectRepository,
                                 final ProjectValidator projectValidator) {
        this.projectRepository = projectRepository;
        this.projectValidator = projectValidator;
    }

    public Project save(final Project project) {
        final var errors = new BeanPropertyBindingResult(project, "project");
        projectValidator.validate(project, errors);
        if (errors.hasErrors()) {
            throw new BothFKsException();
        }
        return projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public Optional<Project> findById(final long id) {
        return projectRepository.findById(id);
    }

    public void deleteById(final long id) {
        projectRepository.deleteById(id);
    }
}
