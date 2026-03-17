package de.mirkosertic.powerstaff.partner

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.partner.command.Partner
import de.mirkosertic.powerstaff.partner.command.PartnerCommandService
import de.mirkosertic.powerstaff.partner.command.PartnerHistoryEntry
import de.mirkosertic.powerstaff.partner.query.PartnerQueryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class PartnerHistoryIT extends AbstractContainerBaseIT {

    @Autowired
    PartnerCommandService partnerService

    @Autowired
    PartnerQueryService queryService

    @Autowired
    JdbcClient jdbcClient

    Long historyTypeId

    def setup() {
        jdbcClient.sql("INSERT INTO historytype (description) VALUES ('IT-History-Typ')").update()
        historyTypeId = jdbcClient.sql("SELECT id FROM historytype WHERE description = 'IT-History-Typ'")
                .query(Long.class).single()
    }

    def cleanup() {
        jdbcClient.sql("DELETE FROM partner_history WHERE description LIKE 'IT-History%'").update()
        jdbcClient.sql("DELETE FROM partner WHERE company LIKE 'IT-History%'").update()
        jdbcClient.sql("DELETE FROM historytype WHERE description = 'IT-History-Typ'").update()
    }

    def "Historieneintrag anlegen und per findHistoryByPartner lesen"() {
        given:
        def partner = partnerService.save(newPartner("IT-History GmbH"),
                [],
                [new PartnerHistoryEntry(null, historyTypeId, "IT-History erster Eintrag")])

        when:
        def list = queryService.findHistoryByPartner(partner.id)

        then:
        list.size() == 1
        list[0].description() == "IT-History erster Eintrag"
        list[0].typeId() == historyTypeId
        list[0].typeDescription() == "IT-History-Typ"
        list[0].partnerId() == partner.id
    }

    def "Historieneintrag loeschen entfernt den Eintrag"() {
        given:
        def partner = partnerService.save(newPartner("IT-History Delete GmbH"),
                [],
                [new PartnerHistoryEntry(null, historyTypeId, "IT-History zum Loeschen")])

        when:
        partnerService.save(partner, [], [])

        then:
        queryService.findHistoryByPartner(partner.id).isEmpty()
    }

    def "Historieneintrag aktualisieren aendert die Beschreibung"() {
        given:
        def partner = partnerService.save(newPartner("IT-History Update GmbH"),
                [],
                [new PartnerHistoryEntry(null, historyTypeId, "IT-History alter Text")])
        def historyId = queryService.findHistoryByPartner(partner.id)[0].id()

        when:
        partnerService.save(partner,
                [],
                [new PartnerHistoryEntry(historyId, historyTypeId, "IT-History neuer Text")])

        then:
        def list = queryService.findHistoryByPartner(partner.id)
        list.size() == 1
        list[0].description() == "IT-History neuer Text"
    }

    def "mehrere Eintraege werden absteigend nach creation_date sortiert"() {
        given:
        def partner = partnerService.save(newPartner("IT-History Sort GmbH"), [], [])
        jdbcClient.sql("""
            INSERT INTO partner_history (creation_date, creation_user, changed_date, changed_user, description, type_id, partner_id)
            VALUES ('2024-01-01 10:00:00', 'system', '2024-01-01 10:00:00', 'system', 'IT-History aelterer Eintrag', :typeId, :partnerId)
        """).param("typeId", historyTypeId).param("partnerId", partner.id).update()
        jdbcClient.sql("""
            INSERT INTO partner_history (creation_date, creation_user, changed_date, changed_user, description, type_id, partner_id)
            VALUES ('2024-06-01 10:00:00', 'system', '2024-06-01 10:00:00', 'system', 'IT-History neuerer Eintrag', :typeId, :partnerId)
        """).param("typeId", historyTypeId).param("partnerId", partner.id).update()

        when:
        def list = queryService.findHistoryByPartner(partner.id)

        then:
        list.size() == 2
        list[0].description() == "IT-History neuerer Eintrag"
        list[1].description() == "IT-History aelterer Eintrag"
    }

    def "findHistoryByPartner fuer Partner ohne Eintraege liefert leere Liste"() {
        given:
        def partner = partnerService.save(newPartner("IT-History Empty GmbH"), [], [])

        expect:
        queryService.findHistoryByPartner(partner.id).isEmpty()
    }

    private static Partner newPartner(String company) {
        def p = new Partner()
        p.company = company
        p.contactForbidden = false
        p.showAgain = false
        p
    }
}
