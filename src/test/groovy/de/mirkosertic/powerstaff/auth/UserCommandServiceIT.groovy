package de.mirkosertic.powerstaff.auth

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Subject

/**
 * Integrationstests fuer UserCommandService.
 * Prueft Anlage, Flag-Update, Passwort-Reset und Loeschung.
 */
@SpringBootTest
class UserCommandServiceIT extends AbstractContainerBaseIT {

    @Subject
    @Autowired
    UserCommandService commandService

    @Autowired
    UserQueryService queryService

    @Autowired
    PasswordEncoder passwordEncoder

    List<String> createdUsernames = []

    def cleanup() {
        createdUsernames.each { username ->
            try { commandService.deleteUser(username) } catch (ignored) {}
        }
        createdUsernames.clear()
    }

    def "createUser legt einen neuen Benutzer an und er ist per findByUsername auffindbar"() {
        when: "ein neuer Benutzer angelegt wird"
        commandService.createUser("cmd_new_user", "passwort123", true, true)
        createdUsernames.add("cmd_new_user")

        then: "der Benutzer ist per findByUsername auffindbar"
        def view = queryService.findByUsername("cmd_new_user")
        view.isPresent()
        view.get().username() == "cmd_new_user"
        view.get().mustChangePassword() == true
        view.get().enabled() == true
    }

    def "createUser speichert Passwort als BCrypt-Hash"() {
        when: "ein Benutzer mit Klartext-Passwort angelegt wird"
        commandService.createUser("cmd_hash_user", "geheimesPasswort", false, true)
        createdUsernames.add("cmd_hash_user")

        then: "das gespeicherte Passwort ist gehasht und stimmt mit dem Klartext ueberein"
        def user = commandService.findByUsername("cmd_hash_user")
        user.isPresent()
        passwordEncoder.matches("geheimesPasswort", user.get().passwordHash)
    }

    def "updateUser aendert enabled und mustChangePassword korrekt"() {
        given: "ein Benutzer mit enabled=true und mustChangePassword=false"
        commandService.createUser("cmd_update_user", "passwort123", false, true)
        createdUsernames.add("cmd_update_user")

        when: "die Flags auf enabled=false und mustChangePassword=true geaendert werden"
        commandService.updateUser("cmd_update_user", true, false)

        then: "die geaenderten Flags sind gespeichert"
        def view = queryService.findByUsername("cmd_update_user")
        view.isPresent()
        view.get().mustChangePassword() == true
        view.get().enabled() == false
    }

    def "resetPassword aendert das Passwort und setzt must_change_password auf false"() {
        given: "ein Benutzer"
        commandService.createUser("cmd_reset_user", "altesPasswort", true, true)
        createdUsernames.add("cmd_reset_user")

        when: "das Passwort zurueckgesetzt wird"
        commandService.resetPassword("cmd_reset_user", "neuesPasswort1")

        then: "das neue Passwort ist korrekt gesetzt"
        def user = commandService.findByUsername("cmd_reset_user")
        user.isPresent()
        passwordEncoder.matches("neuesPasswort1", user.get().passwordHash)

        and: "must_change_password wurde auf false gesetzt (by updatePassword)"
        !user.get().mustChangePassword
    }

    def "deleteUser entfernt den Benutzer vollstaendig"() {
        given: "ein existierender Benutzer"
        commandService.createUser("cmd_del_user", "passwort123", false, true)

        when: "der Benutzer geloescht wird"
        commandService.deleteUser("cmd_del_user")

        then: "der Benutzer ist nicht mehr auffindbar"
        !queryService.findByUsername("cmd_del_user").isPresent()
    }

    def "findByUsername liefert leeres Optional fuer unbekannten Benutzernamen"() {
        when: "ein nicht existierender Benutzername gesucht wird"
        def result = commandService.findByUsername("cmd_unknown_xyz")

        then: "das Ergebnis ist leer"
        !result.isPresent()
    }
}
