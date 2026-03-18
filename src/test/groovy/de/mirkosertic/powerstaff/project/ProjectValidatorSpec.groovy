package de.mirkosertic.powerstaff.project

import de.mirkosertic.powerstaff.project.command.Project
import de.mirkosertic.powerstaff.project.command.ProjectValidator
import org.springframework.validation.BeanPropertyBindingResult
import spock.lang.Specification

class ProjectValidatorSpec extends Specification {

    ProjectValidator validator = new ProjectValidator()

    def "beide FKs null → keine Fehler"() {
        given:
        def project = new Project()
        project.customerId = null
        project.partnerId = null
        def errors = new BeanPropertyBindingResult(project, "project")

        when:
        validator.validate(project, errors)

        then:
        !errors.hasErrors()
    }

    def "nur customerId gesetzt → keine Fehler"() {
        given:
        def project = new Project()
        project.customerId = 1L
        project.partnerId = null
        def errors = new BeanPropertyBindingResult(project, "project")

        when:
        validator.validate(project, errors)

        then:
        !errors.hasErrors()
    }

    def "nur partnerId gesetzt → keine Fehler"() {
        given:
        def project = new Project()
        project.customerId = null
        project.partnerId = 2L
        def errors = new BeanPropertyBindingResult(project, "project")

        when:
        validator.validate(project, errors)

        then:
        !errors.hasErrors()
    }

    def "beide FKs gesetzt → Fehler"() {
        given:
        def project = new Project()
        project.customerId = 1L
        project.partnerId = 2L
        def errors = new BeanPropertyBindingResult(project, "project")

        when:
        validator.validate(project, errors)

        then:
        errors.hasErrors()
        errors.globalErrors[0].code == "project.bothFks"
    }

    def "supports gibt true fuer Project zurueck"() {
        expect:
        validator.supports(Project.class)
    }
}
