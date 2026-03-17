package de.mirkosertic.powerstaff.config

import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.Specification
import spock.lang.Subject

class AuditingConfigSpec extends Specification {

    @Subject
    def auditorAware = new AuditingConfig().auditorProvider()

    def cleanup() {
        SecurityContextHolder.clearContext()
    }

    def "liefert den Benutzernamen wenn ein authentifizierter User im SecurityContext ist"() {
        given:
        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("alice", "password", "ROLE_USER")
        )

        when:
        def result = auditorAware.getCurrentAuditor()

        then:
        result.isPresent()
        result.get() == "alice"
    }

    def "liefert 'system' wenn kein SecurityContext gesetzt ist"() {
        given:
        SecurityContextHolder.clearContext()

        when:
        def result = auditorAware.getCurrentAuditor()

        then:
        result.isPresent()
        result.get() == "system"
    }

    def "liefert 'system' fuer den anonymousUser"() {
        given:
        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("anonymousUser", null)
        )

        when:
        def result = auditorAware.getCurrentAuditor()

        then:
        result.isPresent()
        result.get() == "system"
    }
}
