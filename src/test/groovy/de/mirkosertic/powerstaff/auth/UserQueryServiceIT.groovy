package de.mirkosertic.powerstaff.auth

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Subject

/**
 * Integrationstests fuer UserQueryService.
 * Prueft findAll() und findByUsername() gegen eine echte MySQL-Instanz via Testcontainers.
 */
@SpringBootTest
class UserQueryServiceIT extends AbstractContainerBaseIT {

    @Subject
    @Autowired
    UserQueryService queryService

    @Autowired
    UserCommandService commandService

    List<String> createdUsernames = []

    def cleanup() {
        createdUsernames.each { username -> commandService.deleteUser(username) }
        createdUsernames.clear()
    }

    def "findAll liefert alle angelegten Benutzer sortiert nach username ASC"() {
        given: "drei Benutzer mit unterschiedlichen Benutzernamen"
        commandService.createUser("zzz_user", "passwort123", false, true)
        commandService.createUser("aaa_user", "passwort123", false, true)
        commandService.createUser("mmm_user", "passwort123", false, true)
        createdUsernames.addAll(["zzz_user", "aaa_user", "mmm_user"])

        when: "alle Benutzer abgerufen werden"
        def results = queryService.findAll()

        then: "die Ergebnisse enthalten die angelegten Benutzer"
        results.any { it.username == "aaa_user" }
        results.any { it.username == "mmm_user" }
        results.any { it.username == "zzz_user" }

        and: "die Liste ist nach username aufsteigend sortiert"
        def names = results*.username
        names == names.sort()
    }

    def "findByUsername liefert korrekte UserView fuer existierenden Benutzer"() {
        given: "ein Benutzer mit mustChangePassword=true und enabled=false"
        commandService.createUser("query_test_user", "passwort123", true, false)
        createdUsernames.add("query_test_user")

        when: "der Benutzer per Username gesucht wird"
        def result = queryService.findByUsername("query_test_user")

        then: "ein Ergebnis vorhanden"
        result.isPresent()

        and: "alle Felder stimmen ueberein"
        result.get().username() == "query_test_user"
        result.get().mustChangePassword() == true
        result.get().enabled() == false
    }

    def "findByUsername liefert leeres Optional fuer unbekannten Benutzernamen"() {
        when: "ein nicht existierender Benutzername gesucht wird"
        def result = queryService.findByUsername("nicht_vorhanden_xyz")

        then: "das Ergebnis ist ein leeres Optional"
        !result.isPresent()
    }

    def "findAll liefert alle Felder der UserView korrekt befuellt"() {
        given: "ein Benutzer mit enabled=true und mustChangePassword=true"
        commandService.createUser("view_fields_user", "passwort123", true, true)
        createdUsernames.add("view_fields_user")

        when: "der Benutzer ueber findAll gesucht wird"
        def results = queryService.findAll()
        def view = results.find { it.username() == "view_fields_user" }

        then: "alle Felder der UserView sind korrekt gesetzt"
        view != null
        view.username() == "view_fields_user"
        view.mustChangePassword() == true
        view.enabled() == true
    }
}
