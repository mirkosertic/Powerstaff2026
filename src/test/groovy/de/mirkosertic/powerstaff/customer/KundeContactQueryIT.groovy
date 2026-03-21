package de.mirkosertic.powerstaff.customer

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.customer.command.Kunde
import de.mirkosertic.powerstaff.customer.command.KundeCommandService
import de.mirkosertic.powerstaff.customer.command.KundeContactEntry
import de.mirkosertic.powerstaff.customer.query.KundeQueryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class KundeContactQueryIT extends AbstractContainerBaseIT {

    @Autowired
    KundeCommandService commandService

    @Autowired
    KundeQueryService queryService

    @Autowired
    JdbcClient jdbcClient

    def cleanup() {
        jdbcClient.sql("DELETE FROM kunde_contact WHERE kunde_id IN (SELECT id FROM kunde WHERE company LIKE 'IT-KCQ%')").update()
        jdbcClient.sql("DELETE FROM kunde WHERE company LIKE 'IT-KCQ%'").update()
    }

    def "Kontakt anlegen und per findContactsByKundeId lesen"() {
        given:
        def kunde = commandService.save(newKunde("IT-KCQ Contact GmbH"),
                [new KundeContactEntry("ADD", null, "EMAIL", "test@example.com")],
                [])

        when:
        def contacts = queryService.findContactsByKundeId(kunde.id)

        then:
        contacts.size() == 1
        contacts[0].type() == "EMAIL"
        contacts[0].value() == "test@example.com"
        contacts[0].kundeId() == kunde.id
    }

    def "findContactsByKundeId fuer Kunden ohne Kontakte liefert leere Liste"() {
        given:
        def kunde = commandService.save(newKunde("IT-KCQ Empty GmbH"), [], [])

        expect:
        queryService.findContactsByKundeId(kunde.id).isEmpty()
    }

    def "Kontakte werden nach type ASC dann value ASC sortiert"() {
        given:
        def kunde = commandService.save(newKunde("IT-KCQ Sort GmbH"),
                [
                    new KundeContactEntry("ADD", null, "TEL",   "+49 89 999"),
                    new KundeContactEntry("ADD", null, "EMAIL", "z@example.com"),
                    new KundeContactEntry("ADD", null, "EMAIL", "a@example.com"),
                ],
                [])

        when:
        def contacts = queryService.findContactsByKundeId(kunde.id)

        then:
        contacts.size() == 3
        contacts[0].type() == "EMAIL"
        contacts[0].value() == "a@example.com"
        contacts[1].type() == "EMAIL"
        contacts[1].value() == "z@example.com"
        contacts[2].type() == "TEL"
    }

    private static Kunde newKunde(String company) {
        def k = new Kunde()
        k.company = company
        k.contactForbidden = false
        k.showAgain = false
        k
    }
}
