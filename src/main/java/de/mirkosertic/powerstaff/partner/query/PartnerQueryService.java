package de.mirkosertic.powerstaff.partner.query;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PartnerQueryService {

    private static final String SELECT_PARTNER = """
            SELECT id, db_version, creation_date, creation_user, changed_date, changed_user,
                   company, name1, name2, street, country, plz, city,
                   contactforbidden, show_again, comments, debitor_nr, kreditor_nr
            FROM partner
            """;

    private final JdbcClient jdbcClient;

    public PartnerQueryService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<PartnerDetailView> findById(Long id) {
        return jdbcClient.sql(SELECT_PARTNER + "WHERE id = :id")
                .param("id", id)
                .query(PartnerDetailView.class)
                .optional();
    }

    public Optional<PartnerDetailView> findFirst() {
        return jdbcClient.sql(SELECT_PARTNER + "ORDER BY id ASC LIMIT 1")
                .query(PartnerDetailView.class)
                .optional();
    }

    public Optional<PartnerDetailView> findLast() {
        return jdbcClient.sql(SELECT_PARTNER + "ORDER BY id DESC LIMIT 1")
                .query(PartnerDetailView.class)
                .optional();
    }

    public Optional<PartnerDetailView> findPrevious(Long currentId) {
        return jdbcClient.sql(SELECT_PARTNER + "WHERE id < :currentId ORDER BY id DESC LIMIT 1")
                .param("currentId", currentId)
                .query(PartnerDetailView.class)
                .optional();
    }

    public Optional<PartnerDetailView> findNext(Long currentId) {
        return jdbcClient.sql(SELECT_PARTNER + "WHERE id > :currentId ORDER BY id ASC LIMIT 1")
                .param("currentId", currentId)
                .query(PartnerDetailView.class)
                .optional();
    }

    /**
     * QBE search: all non-null/non-blank fields are combined with AND LIKE '%value%'.
     * Returns up to {@code limit} results starting at {@code offset}, sorted by company ASC.
     */
    public List<PartnerSearchResult> search(PartnerSearchCriteria criteria, int offset, int limit) {
        var sql = new StringBuilder("""
                SELECT id, company, name1, name2, city
                FROM partner
                WHERE 1=1
                """);
        var params = new ArrayList<>();
        if (hasValue(criteria.company())) {
            sql.append(" AND company LIKE ?");
            params.add("%" + criteria.company() + "%");
        }
        if (hasValue(criteria.name1())) {
            sql.append(" AND name1 LIKE ?");
            params.add("%" + criteria.name1() + "%");
        }
        if (hasValue(criteria.name2())) {
            sql.append(" AND name2 LIKE ?");
            params.add("%" + criteria.name2() + "%");
        }
        if (hasValue(criteria.street())) {
            sql.append(" AND street LIKE ?");
            params.add("%" + criteria.street() + "%");
        }
        if (hasValue(criteria.country())) {
            sql.append(" AND country LIKE ?");
            params.add("%" + criteria.country() + "%");
        }
        if (hasValue(criteria.plz())) {
            sql.append(" AND plz LIKE ?");
            params.add("%" + criteria.plz() + "%");
        }
        if (hasValue(criteria.city())) {
            sql.append(" AND city LIKE ?");
            params.add("%" + criteria.city() + "%");
        }
        if (hasValue(criteria.comments())) {
            sql.append(" AND comments LIKE ?");
            params.add("%" + criteria.comments() + "%");
        }
        if (hasValue(criteria.debitorNr())) {
            sql.append(" AND debitor_nr LIKE ?");
            params.add("%" + criteria.debitorNr() + "%");
        }
        if (hasValue(criteria.kreditorNr())) {
            sql.append(" AND kreditor_nr LIKE ?");
            params.add("%" + criteria.kreditorNr() + "%");
        }
        sql.append(" ORDER BY company ASC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        var stmt = jdbcClient.sql(sql.toString());
        for (int i = 0; i < params.size(); i++) {
            stmt = stmt.param(i + 1, params.get(i));
        }
        return stmt.query(PartnerSearchResult.class).list();
    }

    public long countSearch(PartnerSearchCriteria criteria) {
        var sql = new StringBuilder("SELECT COUNT(*) FROM partner WHERE 1=1");
        var params = new ArrayList<>();
        if (hasValue(criteria.company())) {
            sql.append(" AND company LIKE ?");
            params.add("%" + criteria.company() + "%");
        }
        if (hasValue(criteria.name1())) {
            sql.append(" AND name1 LIKE ?");
            params.add("%" + criteria.name1() + "%");
        }
        if (hasValue(criteria.name2())) {
            sql.append(" AND name2 LIKE ?");
            params.add("%" + criteria.name2() + "%");
        }
        if (hasValue(criteria.street())) {
            sql.append(" AND street LIKE ?");
            params.add("%" + criteria.street() + "%");
        }
        if (hasValue(criteria.country())) {
            sql.append(" AND country LIKE ?");
            params.add("%" + criteria.country() + "%");
        }
        if (hasValue(criteria.plz())) {
            sql.append(" AND plz LIKE ?");
            params.add("%" + criteria.plz() + "%");
        }
        if (hasValue(criteria.city())) {
            sql.append(" AND city LIKE ?");
            params.add("%" + criteria.city() + "%");
        }
        if (hasValue(criteria.comments())) {
            sql.append(" AND comments LIKE ?");
            params.add("%" + criteria.comments() + "%");
        }
        if (hasValue(criteria.debitorNr())) {
            sql.append(" AND debitor_nr LIKE ?");
            params.add("%" + criteria.debitorNr() + "%");
        }
        if (hasValue(criteria.kreditorNr())) {
            sql.append(" AND kreditor_nr LIKE ?");
            params.add("%" + criteria.kreditorNr() + "%");
        }

        var stmt = jdbcClient.sql(sql.toString());
        for (int i = 0; i < params.size(); i++) {
            stmt = stmt.param(i + 1, params.get(i));
        }
        return stmt.query(Long.class).single();
    }

    public List<PartnerFreelancerView> findFreelancersByPartner(Long partnerId) {
        return jdbcClient.sql("""
                SELECT id, code, name1, name2, company, availability_as_date, salary_long
                FROM freelancer
                WHERE partner_id = :partnerId
                ORDER BY name1 ASC, name2 ASC
                """)
                .param("partnerId", partnerId)
                .query(PartnerFreelancerView.class)
                .list();
    }

    public List<PartnerProjectView> findProjectsByPartner(Long partnerId) {
        return jdbcClient.sql("""
                SELECT id, project_number, description_short, workplace, start_date, status
                FROM project
                WHERE partner_id = :partnerId
                ORDER BY start_date DESC
                """)
                .param("partnerId", partnerId)
                .query(PartnerProjectView.class)
                .list();
    }

    public List<PartnerContactView> findContactsByPartner(Long partnerId) {
        return jdbcClient.sql("""
                SELECT id, type, value, partner_id
                FROM partner_contact
                WHERE partner_id = :partnerId
                ORDER BY type ASC, value ASC
                """)
                .param("partnerId", partnerId)
                .query(PartnerContactView.class)
                .list();
    }

    private static boolean hasValue(String s) {
        return s != null && !s.isBlank();
    }
}
