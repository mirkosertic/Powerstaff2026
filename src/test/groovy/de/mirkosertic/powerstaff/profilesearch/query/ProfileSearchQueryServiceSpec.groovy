package de.mirkosertic.powerstaff.profilesearch.query

import org.springframework.jdbc.core.simple.JdbcClient
import spock.lang.Specification
import spock.lang.Subject

class ProfileSearchQueryServiceSpec extends Specification {

    JdbcClient jdbcClient = Mock()

    @Subject
    ProfileSearchQueryService service = new ProfileSearchQueryService(jdbcClient)

    def "searchFreelancers delegiert an JdbcClient mit korrekten Parametern"() {
        given:
        def criteria = new ProfileSearchCriteria(null, null, null, null, null, null)
        def sqlSpec = Mock(JdbcClient.StatementSpec)
        def paramSpec = Mock(JdbcClient.StatementSpec)
        def paramSpec2 = Mock(JdbcClient.StatementSpec)
        def mappedQuery = Mock(JdbcClient.MappedQuerySpec)

        jdbcClient.sql(_ as String) >> sqlSpec
        sqlSpec.param("limit", 20) >> paramSpec
        paramSpec.param("offset", 0) >> paramSpec2
        paramSpec2.query(_ as Class) >> mappedQuery
        mappedQuery.list() >> []

        when:
        def result = service.searchFreelancers(criteria, 0, 20)

        then:
        result == []
    }

    def "countSearchFreelancers delegiert an JdbcClient"() {
        given:
        def criteria = new ProfileSearchCriteria(null, null, null, null, null, null)
        def sqlSpec = Mock(JdbcClient.StatementSpec)
        def mappedQuery = Mock(JdbcClient.MappedQuerySpec)

        jdbcClient.sql(_ as String) >> sqlSpec
        sqlSpec.query(Long.class) >> mappedQuery
        mappedQuery.single() >> 42L

        when:
        def count = service.countSearchFreelancers(criteria)

        then:
        count == 42L
    }
}
