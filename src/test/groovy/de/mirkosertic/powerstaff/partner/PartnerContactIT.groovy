package de.mirkosertic.powerstaff.partner

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.partner.command.Partner
import de.mirkosertic.powerstaff.partner.command.PartnerCommandService
import de.mirkosertic.powerstaff.partner.command.PartnerContact
import de.mirkosertic.powerstaff.partner.command.PartnerContactCommandService
import de.mirkosertic.powerstaff.partner.query.PartnerQueryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class PartnerContactIT extends AbstractContainerBaseIT {

    @Autowired
    PartnerCommandService partnerService

    @Autowired
    PartnerContactCommandService contactService

    @Autowired
    PartnerQueryService queryService

    @Autowired
    JdbcClient jdbcClient

    def cleanup() {
        jdbcClient.sql("DELETE FROM partner_contact WHERE value LIKE 'IT-Contact%'").update()
        jdbcClient.sql("DELETE FROM partner WHERE company LIKE 'IT-Contact%'").update()
    }

    def "Kontakt anlegen und per findContactsByPartner lesen"() {
        given:
        def partner = partnerService.save(newPartner("IT-Contact GmbH"))
        def contact = contactService.save(newContact("EMAIL", "IT-Contact test@example.com", partner.id))

        when:
        def list = queryService.findContactsByPartner(partner.id)

        then:
        list.size() == 1
        list[0].id() == contact.id
        list[0].type() == "EMAIL"
        list[0].value() == "IT-Contact test@example.com"
        list[0].partnerId() == partner.id
    }

    def "Kontakt loeschen entfernt den Eintrag"() {
        given:
        def partner = partnerService.save(newPartner("IT-Contact Delete GmbH"))
        def contact = contactService.save(newContact("TELEFON", "IT-Contact 0800-123456", partner.id))

        when:
        contactService.deleteById(contact.id)

        then:
        contactService.findById(contact.id).isEmpty()
        queryService.findContactsByPartner(partner.id).isEmpty()
    }

    def "Kontakt aktualisieren aendert den Wert"() {
        given:
        def partner = partnerService.save(newPartner("IT-Contact Update GmbH"))
        def contact = contactService.save(newContact("WEB", "IT-Contact http://old.example.com", partner.id))

        when:
        contact.setValue("IT-Contact http://new.example.com")
        contactService.save(contact)

        then:
        def loaded = contactService.findById(contact.id).orElseThrow()
        loaded.value == "IT-Contact http://new.example.com"
    }

    def "findContactsByPartner fuer Partner ohne Kontakte liefert leere Liste"() {
        given:
        def partner = partnerService.save(newPartner("IT-Contact Empty GmbH"))

        expect:
        queryService.findContactsByPartner(partner.id).isEmpty()
    }

    def "mehrere Kontakte werden nach type und value sortiert zurueckgegeben"() {
        given:
        def partner = partnerService.save(newPartner("IT-Contact Sort GmbH"))
        contactService.save(newContact("XING", "IT-Contact xing.com/max", partner.id))
        contactService.save(newContact("EMAIL", "IT-Contact b@example.com", partner.id))
        contactService.save(newContact("EMAIL", "IT-Contact a@example.com", partner.id))

        when:
        def list = queryService.findContactsByPartner(partner.id)

        then:
        list.size() == 3
        list[0].type() == "EMAIL"
        list[0].value() == "IT-Contact a@example.com"
        list[1].type() == "EMAIL"
        list[2].type() == "XING"
    }

    private static Partner newPartner(String company) {
        def p = new Partner()
        p.company = company
        p.contactForbidden = false
        p.showAgain = false
        p
    }

    private static PartnerContact newContact(String type, String value, Long partnerId) {
        def c = new PartnerContact()
        c.type = type
        c.value = value
        c.partnerId = partnerId
        c
    }
}
