package de.mirkosertic.powerstaff.profilesearch.query

import de.mirkosertic.powerstaff.AbstractContainerBaseIT
import de.mirkosertic.powerstaff.shared.query.TagView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient

@SpringBootTest
class ProfileSearchBatchLoadIT extends AbstractContainerBaseIT {

    @Autowired
    ProfileSearchQueryService queryService

    @Autowired
    JdbcClient jdbcClient

    // ── Hilfsmethoden für Testdaten ──────────────────────────────────────────

    private Long insertFreelancer(String code, String name1, String name2,
                                  Long salaryPerDayLong = null, boolean contactForbidden = false) {
        jdbcClient.sql("""
            INSERT INTO freelancer (db_version, code, name1, name2, salary_per_day_long, contactforbidden)
            VALUES (0, :code, :name1, :name2, :salary, :forbidden)
        """)
                .param("code", code)
                .param("name1", name1)
                .param("name2", name2)
                .param("salary", salaryPerDayLong)
                .param("forbidden", contactForbidden)
                .update()
        return jdbcClient.sql("SELECT LAST_INSERT_ID()").query(Long.class).single()
    }

    private Long insertTag(String tagname, String type = 'SCHWERPUNKT') {
        jdbcClient.sql("INSERT INTO tags (tagname, type) VALUES (:name, :type)")
                .param("name", tagname)
                .param("type", type)
                .update()
        return jdbcClient.sql("SELECT LAST_INSERT_ID()").query(Long.class).single()
    }

    private void assignTag(Long freelancerId, Long tagId) {
        jdbcClient.sql("INSERT INTO freelancer_tags (freelancer_id, tag_id) VALUES (:fid, :tid)")
                .param("fid", freelancerId)
                .param("tid", tagId)
                .update()
    }

    private void cleanup(List<Long> freelancerIds, List<Long> tagIds) {
        if (freelancerIds) {
            jdbcClient.sql("DELETE FROM freelancer_tags WHERE freelancer_id IN (:ids)")
                    .param("ids", freelancerIds).update()
            jdbcClient.sql("DELETE FROM freelancer WHERE id IN (:ids)")
                    .param("ids", freelancerIds).update()
        }
        if (tagIds) {
            jdbcClient.sql("DELETE FROM tags WHERE id IN (:ids)")
                    .param("ids", tagIds).update()
        }
    }

    // ── findFreelancersByCodesInBatch ────────────────────────────────────────

    def "findFreelancersByCodesInBatch mit leerer Liste liefert leere Map"() {
        expect:
        queryService.findFreelancersByCodesInBatch([]).isEmpty()
    }

    def "findFreelancersByCodesInBatch liefert Freiberufler fuer bekannte Codes"() {
        given:
        def id1 = insertFreelancer('BATCH-FL-001', 'Anna', 'Müller', 800L)
        def id2 = insertFreelancer('BATCH-FL-002', 'Bob', 'Schmidt', 950L)

        when:
        def result = queryService.findFreelancersByCodesInBatch(['BATCH-FL-001', 'BATCH-FL-002'])

        then:
        result.size() == 2
        with(result['BATCH-FL-001']) {
            id == id1
            name1 == 'Anna'
            name2 == 'Müller'
            salaryPerDayLong == 800L
            !contactForbidden
        }
        with(result['BATCH-FL-002']) {
            id == id2
            name1 == 'Bob'
            name2 == 'Schmidt'
            salaryPerDayLong == 950L
        }

        cleanup:
        cleanup([id1, id2], [])
    }

    def "findFreelancersByCodesInBatch ignoriert unbekannte Codes"() {
        given:
        def id1 = insertFreelancer('BATCH-FL-003', 'Claudia', 'Weber')

        when:
        def result = queryService.findFreelancersByCodesInBatch(['BATCH-FL-003', 'BATCH-NONEXISTENT'])

        then:
        result.size() == 1
        result.containsKey('BATCH-FL-003')
        !result.containsKey('BATCH-NONEXISTENT')

        cleanup:
        cleanup([id1], [])
    }

    def "findFreelancersByCodesInBatch liefert contactForbidden korrekt"() {
        given:
        def id1 = insertFreelancer('BATCH-FL-004', 'David', 'Meier', null, true)

        when:
        def result = queryService.findFreelancersByCodesInBatch(['BATCH-FL-004'])

        then:
        result['BATCH-FL-004'].contactForbidden

        cleanup:
        cleanup([id1], [])
    }

    // ── findTagsByFreelancerIdsInBatch ───────────────────────────────────────

    def "findTagsByFreelancerIdsInBatch mit leerer Liste liefert leere Map"() {
        expect:
        queryService.findTagsByFreelancerIdsInBatch([]).isEmpty()
    }

    def "findTagsByFreelancerIdsInBatch liefert Tags sortiert nach tagname"() {
        given:
        def fId = insertFreelancer('BATCH-FL-005', 'Eva', 'Klein')
        def tagJavaId = insertTag('Java', 'SCHWERPUNKT')
        def tagKotlinId = insertTag('Kotlin', 'SCHWERPUNKT')
        def tagSpringId = insertTag('Spring', 'BRANCHE')
        assignTag(fId, tagKotlinId)
        assignTag(fId, tagJavaId)
        assignTag(fId, tagSpringId)

        when:
        def result = queryService.findTagsByFreelancerIdsInBatch([fId])

        then:
        result.size() == 1
        List<TagView> tags = result[fId]
        tags.size() == 3
        tags[0].tagname() == 'Java'
        tags[1].tagname() == 'Kotlin'
        tags[2].tagname() == 'Spring'
        tags*.type() as Set == ['SCHWERPUNKT', 'BRANCHE'] as Set

        cleanup:
        cleanup([fId], [tagJavaId, tagKotlinId, tagSpringId])
    }

    def "findTagsByFreelancerIdsInBatch gruppiert Tags korrekt nach Freiberufler"() {
        given:
        def fId1 = insertFreelancer('BATCH-FL-006', 'Frank', 'Lauer')
        def fId2 = insertFreelancer('BATCH-FL-007', 'Gabi', 'Huber')
        def tagId1 = insertTag('Python', 'SCHWERPUNKT')
        def tagId2 = insertTag('AWS', 'BRANCHE')
        def tagId3 = insertTag('Docker', 'SCHWERPUNKT')
        assignTag(fId1, tagId1)
        assignTag(fId1, tagId2)
        assignTag(fId2, tagId3)

        when:
        def result = queryService.findTagsByFreelancerIdsInBatch([fId1, fId2])

        then:
        result.size() == 2
        result[fId1]*.tagname() as Set == ['Python', 'AWS'] as Set
        result[fId2]*.tagname() == ['Docker']

        cleanup:
        cleanup([fId1, fId2], [tagId1, tagId2, tagId3])
    }

    def "findTagsByFreelancerIdsInBatch liefert leere Eintraege fuer Freiberufler ohne Tags"() {
        given:
        def fId = insertFreelancer('BATCH-FL-008', 'Heinz', 'Bauer')

        when:
        def result = queryService.findTagsByFreelancerIdsInBatch([fId])

        then:
        // Kein Eintrag für freelancer ohne Tags – getOrDefault mit List.of() ist korrekt
        result.isEmpty() || result[fId] == null || result[fId].isEmpty()

        cleanup:
        cleanup([fId], [])
    }
}
