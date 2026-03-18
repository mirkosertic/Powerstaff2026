package de.mirkosertic.powerstaff.freelancer

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.freelancer.command.Freelancer
import de.mirkosertic.powerstaff.freelancer.command.FreelancerCommandService
import de.mirkosertic.powerstaff.freelancer.command.FreelancerTagCommandService
import de.mirkosertic.powerstaff.freelancer.query.FreelancerQueryService
import de.mirkosertic.powerstaff.shared.TagType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class FreelancerQueryServiceTagsIT extends AbstractContainerBaseIT {

    @Autowired
    FreelancerCommandService commandService

    @Autowired
    FreelancerTagCommandService tagCommandService

    @Autowired
    FreelancerQueryService queryService

    @Autowired
    JdbcClient jdbcClient

    Long freelancerId
    Long tagJavaId
    Long tagMuenchenId
    Long tagBerlinId

    def setup() {
        def f = new Freelancer()
        f.name1 = "IT-Tags-Test"
        f.name2 = "QS"
        f.contactForbidden = false
        f.showAgain = false
        f.datenschutz = false
        freelancerId = commandService.save(f).id

        jdbcClient.sql("INSERT INTO tags (tagname, type) VALUES ('IT-Tags-Java', 'SCHWERPUNKT')").update()
        tagJavaId = jdbcClient.sql("SELECT id FROM tags WHERE tagname = 'IT-Tags-Java'").query(Long).single()

        jdbcClient.sql("INSERT INTO tags (tagname, type) VALUES ('IT-Tags-München', 'EINSATZORT')").update()
        tagMuenchenId = jdbcClient.sql("SELECT id FROM tags WHERE tagname = 'IT-Tags-München'").query(Long).single()

        jdbcClient.sql("INSERT INTO tags (tagname, type) VALUES ('IT-Tags-Berlin', 'EINSATZORT')").update()
        tagBerlinId = jdbcClient.sql("SELECT id FROM tags WHERE tagname = 'IT-Tags-Berlin'").query(Long).single()
    }

    def cleanup() {
        jdbcClient.sql("DELETE FROM freelancer_tags WHERE freelancer_id = :id").param("id", freelancerId).update()
        jdbcClient.sql("DELETE FROM freelancer WHERE id = :id").param("id", freelancerId).update()
        jdbcClient.sql("DELETE FROM tags WHERE tagname LIKE 'IT-Tags-%'").update()
    }

    def "findTagsByFreelancerId liefert zugeordnete Tags"() {
        given:
        tagCommandService.addTag(freelancerId, tagJavaId)
        tagCommandService.addTag(freelancerId, tagMuenchenId)

        when:
        def tags = queryService.findTagsByFreelancerId(freelancerId)

        then:
        tags.size() == 2
        tags.any { it.name() == "IT-Tags-Java" && it.type() == TagType.SCHWERPUNKT }
        tags.any { it.name() == "IT-Tags-München" && it.type() == TagType.EINSATZORT }
    }

    def "findTagsByFreelancerId liefert leere Liste ohne Zuordnungen"() {
        expect:
        queryService.findTagsByFreelancerId(freelancerId).isEmpty()
    }

    def "findTagsByFreelancerId sortiert nach TagType-Ordinal dann tagname"() {
        given:
        tagCommandService.addTag(freelancerId, tagMuenchenId)   // EINSATZORT order=2
        tagCommandService.addTag(freelancerId, tagBerlinId)     // EINSATZORT order=2
        tagCommandService.addTag(freelancerId, tagJavaId)       // SCHWERPUNKT order=0

        when:
        def tags = queryService.findTagsByFreelancerId(freelancerId)

        then:
        tags.size() == 3
        tags[0].type() == TagType.SCHWERPUNKT           // order=0 kommt zuerst
        tags[1].type() == TagType.EINSATZORT
        tags[1].name() == "IT-Tags-Berlin"              // alphabetisch: Berlin < München
        tags[2].name() == "IT-Tags-München"
    }

    def "findAvailableTagsByFreelancerIdAndType liefert nicht-zugeordnete Tags des Typs"() {
        given:
        tagCommandService.addTag(freelancerId, tagMuenchenId)   // EINSATZORT bereits zugeordnet

        when:
        def available = queryService.findAvailableTagsByFreelancerIdAndType(freelancerId, TagType.EINSATZORT)

        then:
        available.size() == 1
        available[0].name() == "IT-Tags-Berlin"
        available[0].type() == TagType.EINSATZORT
    }

    def "findAvailableTagsByFreelancerIdAndType liefert nichts wenn alle zugeordnet"() {
        given:
        tagCommandService.addTag(freelancerId, tagMuenchenId)
        tagCommandService.addTag(freelancerId, tagBerlinId)

        when:
        def available = queryService.findAvailableTagsByFreelancerIdAndType(freelancerId, TagType.EINSATZORT)

        then:
        available.isEmpty()
    }

    def "findAvailableTagsByFreelancerIdAndType filtert korrekt nach Typ"() {
        when:
        def available = queryService.findAvailableTagsByFreelancerIdAndType(freelancerId, TagType.SCHWERPUNKT)

        then:
        available.every { it.type() == TagType.SCHWERPUNKT }
        available.any { it.name() == "IT-Tags-Java" }
    }
}
