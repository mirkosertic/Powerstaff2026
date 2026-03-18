package de.mirkosertic.powerstaff

import org.springframework.modulith.core.ApplicationModules
import spock.lang.Specification

class ApplicationModulesSpec extends Specification {

    def "Spring Modulith: keine zyklischen Abhaengigkeiten und keine unerlaubten Paket-Querverweise"() {
        given:
        def modules = ApplicationModules.of(PowerstaffApplication.class)

        expect:
        modules.verify()
    }
}
