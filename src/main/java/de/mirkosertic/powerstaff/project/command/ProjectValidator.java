package de.mirkosertic.powerstaff.project.command;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ProjectValidator implements Validator {

    @Override
    public boolean supports(final Class<?> clazz) {
        return Project.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(final Object target, final Errors errors) {
        final Project project = (Project) target;
        if (project.getCustomerId() != null && project.getPartnerId() != null) {
            errors.reject("project.bothFks",
                    "Ein Projekt kann nicht gleichzeitig einem Kunden und einem Partner zugeordnet sein.");
        }
    }
}
