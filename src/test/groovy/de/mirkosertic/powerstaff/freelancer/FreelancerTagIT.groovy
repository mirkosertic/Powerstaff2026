package de.mirkosertic.powerstaff.freelancer

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.freelancer.command.DuplicateTagException
import de.mirkosertic.powerstaff.freelancer.command.Freelancer
import de.mirkosertic.powerstaff.freelancer.command.FreelancerCommandService
import de.mirkosertic.powerstaff.freelancer.command.FreelancerTagCommandService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class FreelancerTagIT extends AbstractContainerBaseIT {

    @Autowired
    FreelancerCommandService freelancerService

    @Autowired
    FreelancerTagCommandService tagCommandService

    @Autowired
    JdbcClient jdbcClient

    Long tagId

    def setup() {
        jdbcClient.sql("INSERT INTO tags (tagname, type) VALUES ('IT-Tag-Java', 'SCHWERPUNKT')").update()
        tagId = jdbcClient.sql("SELECT id FROM tags WHERE tagname = 'IT-Tag-Java'")
                .query(Long.class).single()
    }

    def cleanup() {
        jdbcClient.sql("DELETE FROM freelancer_tags WHERE tag_id = :tagId").param("tagId", tagId).update()
        jdbcClient.sql("DELETE FROM freelancer WHERE name1 LIKE 'IT-Tag%'").update()
        jdbcClient.sql("DELETE FROM tags WHERE tagname = 'IT-Tag-Java'").update()
    }

    def "Tag zuordnen und per Repository prüfen"() {
        given:
        def freelancer = freelancerService.save(newFreelancer("IT-Tag-Assign"))

        when:
        def assignment = tagCommandService.addTag(freelancer.id, tagId)

        then:
        assignment.id != null
        assignment.freelancerId == freelancer.id
        assignment.tagId == tagId
    }

    def "Duplicate Tag-Zuordnung wirft DuplicateTagException"() {
        given:
        def freelancer = freelancerService.save(newFreelancer("IT-Tag-Dup"))
        tagCommandService.addTag(freelancer.id, tagId)

        when:
        tagCommandService.addTag(freelancer.id, tagId)

        then:
        thrown(DuplicateTagException)
    }

    def "Tag-Zuordnung entfernen loescht den Eintrag"() {
        given:
        def freelancer = freelancerService.save(newFreelancer("IT-Tag-Remove"))
        def assignment = tagCommandService.addTag(freelancer.id, tagId)

        when:
        tagCommandService.removeTag(assignment.id)

        then:
        def count = jdbcClient.sql("SELECT COUNT(*) FROM freelancer_tags WHERE id = :id")
                .param("id", assignment.id).query(Long.class).single()
        count == 0
    }

    private static Freelancer newFreelancer(String name1) {
        def f = new Freelancer()
        f.name1 = name1
        f.name2 = "Test"
        f.contactForbidden = false
        f.showAgain = false
        f.datenschutz = false
        f
    }
}
