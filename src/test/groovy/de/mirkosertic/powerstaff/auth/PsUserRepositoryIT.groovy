package de.mirkosertic.powerstaff.auth

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import spock.lang.Subject

/**
 * Integrationstests für PsUserRepository.
 *
 * Verwendet @SpringBootTest (statt @DataJdbcTest), weil PsUserRepository
 * package-private ist und der Spring-Kontext vollständig geladen werden muss,
 * damit die Testklasse per @Autowired darauf zugreifen kann.
 *
 * Die MySQL-Testcontainer-Instanz wird von AbstractContainerBaseIT verwaltet.
 * Flyway führt V1__init_schema.sql automatisch aus.
 */
@SpringBootTest
class PsUserRepositoryIT extends AbstractContainerBaseIT {

    @Subject
    @Autowired
    PsUserRepository repository

    def cleanup() {
        // Testisolation: alle Testbenutzer nach jedem Feature-Method entfernen
        repository.deleteAll()
    }

    def "speichert einen Benutzer und liest ihn anschliessend vollstaendig zurueck"() {
        given: "ein neuer PsUser mit allen Pflichtfeldern"
        def user = new PsUser(
                "testuser",
                "{bcrypt}\$2a\$10\$abcdefghijklmnopqrstuuVGlGNBdm6/GQ4DV.jMRfF4wrfHOFuq6",
                true,
                true,
                PsUser.DEFAULT_SYSTEM_PROMPT,
                false
        )

        when: "der Benutzer gespeichert und per ID geladen wird"
        repository.save(user)
        def loaded = repository.findById("testuser")

        then: "der Benutzer ist vorhanden und alle Felder stimmen exakt ueberein"
        loaded.isPresent()
        loaded.get().getUsername()          == "testuser"
        loaded.get().getPasswordHash()      == "{bcrypt}\$2a\$10\$abcdefghijklmnopqrstuuVGlGNBdm6/GQ4DV.jMRfF4wrfHOFuq6"
        loaded.get().isMustChangePassword()
        loaded.get().isEnabled()
    }

    def "findById liefert ein leeres Optional fuer einen nicht existierenden Benutzernamen"() {
        when: "ein Benutzer gesucht wird, der nie gespeichert wurde"
        def result = repository.findById("unknown")

        then: "das Ergebnis ist ein leeres Optional"
        !result.isPresent()
    }

    def "das Speichern zweier Benutzer mit identischem username wirft eine DataIntegrityViolationException"() {
        given: "zwei PsUser-Objekte mit demselben Primaerschluessel"
        def user1 = new PsUser("duplicate", "{bcrypt}hash1", false, true, PsUser.DEFAULT_SYSTEM_PROMPT, false)
        def user2 = new PsUser("duplicate", "{bcrypt}hash2", false, true, PsUser.DEFAULT_SYSTEM_PROMPT, false)
        repository.save(user1)

        when: "der zweite Benutzer mit dem bereits vergebenen username gespeichert wird"
        repository.save(user2)

        then: "Spring Data wirft eine DataIntegrityViolationException (PK-Konflikt)"
        thrown(DataIntegrityViolationException)
    }
}
