package de.mirkosertic.powerstaff.freelancer.query;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class FreelancerQueryService {

    private static final String SELECT_FREELANCER = """
            SELECT id, db_version, creation_date, creation_user, changed_date, changed_user,
                   titel, name1, name2, company, street, country, plz, city,
                   nationalitaet, geburtsdatum, partner_id,
                   contactforbidden, show_again, comments, einsatzdetails,
                   contact_person, contact_type, contact_reason, last_contact_date,
                   kontaktart, availability_as_date,
                   salary_long, salary_per_day_long, salary_remote,
                   salary_partner_long, salary_partner_per_day_long,
                   datenschutz, debitor_nr, gulp_id, code, skills
            FROM freelancer
            """;

    private final JdbcClient jdbcClient;

    public FreelancerQueryService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<FreelancerDetailView> findById(long id) {
        return jdbcClient.sql(SELECT_FREELANCER + "WHERE id = :id")
                .param("id", id)
                .query(FreelancerDetailView.class)
                .optional();
    }

    public Optional<FreelancerDetailView> findFirst() {
        return jdbcClient.sql(SELECT_FREELANCER + "ORDER BY id ASC LIMIT 1")
                .query(FreelancerDetailView.class)
                .optional();
    }

    public Optional<FreelancerDetailView> findLast() {
        return jdbcClient.sql(SELECT_FREELANCER + "ORDER BY id DESC LIMIT 1")
                .query(FreelancerDetailView.class)
                .optional();
    }

    public Optional<FreelancerDetailView> findPrevious(long currentId) {
        return jdbcClient.sql(SELECT_FREELANCER + "WHERE id < :currentId ORDER BY id DESC LIMIT 1")
                .param("currentId", currentId)
                .query(FreelancerDetailView.class)
                .optional();
    }

    public Optional<FreelancerDetailView> findNext(long currentId) {
        return jdbcClient.sql(SELECT_FREELANCER + "WHERE id > :currentId ORDER BY id ASC LIMIT 1")
                .param("currentId", currentId)
                .query(FreelancerDetailView.class)
                .optional();
    }

    public List<FreelancerSearchResult> search(FreelancerSearchCriteria criteria, int offset, int limit) {
        var sql = new StringBuilder("""
                SELECT id, code, name1, name2, company, city,
                       availability_as_date, salary_long, salary_per_day_long, skills
                FROM freelancer
                WHERE 1=1
                """);
        var params = new ArrayList<>();
        appendStringCriteria(sql, params, criteria);
        if (criteria.salaryLongMax() != null) {
            sql.append(" AND salary_long <= ?");
            params.add(criteria.salaryLongMax());
        }
        if (criteria.salaryPerDayLongMax() != null) {
            sql.append(" AND salary_per_day_long <= ?");
            params.add(criteria.salaryPerDayLongMax());
        }
        sql.append(" ORDER BY name1 ASC, name2 ASC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        var stmt = jdbcClient.sql(sql.toString());
        for (int i = 0; i < params.size(); i++) {
            stmt = stmt.param(i + 1, params.get(i));
        }
        return stmt.query(FreelancerSearchResult.class).list();
    }

    public long countSearch(FreelancerSearchCriteria criteria) {
        var sql = new StringBuilder("SELECT COUNT(*) FROM freelancer WHERE 1=1");
        var params = new ArrayList<>();
        appendStringCriteria(sql, params, criteria);
        if (criteria.salaryLongMax() != null) {
            sql.append(" AND salary_long <= ?");
            params.add(criteria.salaryLongMax());
        }
        if (criteria.salaryPerDayLongMax() != null) {
            sql.append(" AND salary_per_day_long <= ?");
            params.add(criteria.salaryPerDayLongMax());
        }

        var stmt = jdbcClient.sql(sql.toString());
        for (int i = 0; i < params.size(); i++) {
            stmt = stmt.param(i + 1, params.get(i));
        }
        return stmt.query(Long.class).single();
    }

    private static void appendStringCriteria(StringBuilder sql, List<Object> params, FreelancerSearchCriteria c) {
        appendLike(sql, params, "name1", c.name1());
        appendLike(sql, params, "name2", c.name2());
        appendLike(sql, params, "company", c.company());
        appendLike(sql, params, "street", c.street());
        appendLike(sql, params, "country", c.country());
        appendLike(sql, params, "plz", c.plz());
        appendLike(sql, params, "city", c.city());
        appendLike(sql, params, "nationalitaet", c.nationalitaet());
        appendLike(sql, params, "comments", c.comments());
        appendLike(sql, params, "einsatzdetails", c.einsatzdetails());
        appendLike(sql, params, "contact_person", c.contactPerson());
        appendLike(sql, params, "contact_reason", c.contactReason());
        appendLike(sql, params, "kontaktart", c.kontaktart());
        appendLike(sql, params, "debitor_nr", c.debitorNr());
        appendLike(sql, params, "gulp_id", c.gulpId());
        appendLike(sql, params, "code", c.code());
        appendLike(sql, params, "skills", c.skills());
    }

    private static void appendLike(StringBuilder sql, List<Object> params, String column, String value) {
        if (value != null && !value.isBlank()) {
            sql.append(" AND ").append(column).append(" LIKE ?");
            params.add("%" + value + "%");
        }
    }
}
