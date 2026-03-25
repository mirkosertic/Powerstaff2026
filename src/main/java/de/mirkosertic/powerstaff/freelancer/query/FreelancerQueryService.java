package de.mirkosertic.powerstaff.freelancer.query;

import de.mirkosertic.powerstaff.shared.TagType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class FreelancerQueryService {

    private static final Set<String> SORT_FIELDS_ALLOWLIST = Set.of(
            "name1", "name2", "company", "city", "availability_as_date",
            "salary_per_day_long", "code");
    private static final String DEFAULT_SORT = "name1 ASC, name2 ASC";

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

    public FreelancerQueryService(final JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<FreelancerDetailView> findById(final long id) {
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

    public Optional<FreelancerDetailView> findPrevious(final long currentId) {
        return jdbcClient.sql(SELECT_FREELANCER + "WHERE id < :currentId ORDER BY id DESC LIMIT 1")
                .param("currentId", currentId)
                .query(FreelancerDetailView.class)
                .optional();
    }

    public Optional<FreelancerDetailView> findNext(final long currentId) {
        return jdbcClient.sql(SELECT_FREELANCER + "WHERE id > :currentId ORDER BY id ASC LIMIT 1")
                .param("currentId", currentId)
                .query(FreelancerDetailView.class)
                .optional();
    }

    public List<FreelancerSearchResult> search(final FreelancerSearchCriteria criteria, final int offset, final int limit) {
        final var sql = new StringBuilder("""
                SELECT id, code, name1, name2, company, city,
                       availability_as_date, salary_long, salary_per_day_long, skills
                FROM freelancer
                WHERE 1=1
                """);
        final Map<String, Object> params = new LinkedHashMap<>();
        appendStringCriteria(sql, params, criteria);
        if (criteria.salaryLongMax() != null) {
            final String pName = "p" + (params.size() + 1);
            sql.append(" AND salary_long <= :").append(pName);
            params.put(pName, criteria.salaryLongMax());
        }
        if (criteria.salaryPerDayLongMax() != null) {
            final String pName = "p" + (params.size() + 1);
            sql.append(" AND salary_per_day_long <= :").append(pName);
            params.put(pName, criteria.salaryPerDayLongMax());
        }
        final String orderBy;
        if (criteria.sortField() != null && SORT_FIELDS_ALLOWLIST.contains(criteria.sortField())) {
            final String dir = "desc".equalsIgnoreCase(criteria.sortDir()) ? "DESC" : "ASC";
            orderBy = criteria.sortField() + " " + dir;
        } else {
            orderBy = DEFAULT_SORT;
        }
        final String pLimit = "p" + (params.size() + 1);
        final String pOffset = "p" + (params.size() + 2);
        sql.append(" ORDER BY ").append(orderBy).append(" LIMIT :").append(pLimit).append(" OFFSET :").append(pOffset);
        params.put(pLimit, limit);
        params.put(pOffset, offset);

        var stmt = jdbcClient.sql(sql.toString());
        for (final var entry : params.entrySet()) {
            stmt = stmt.param(entry.getKey(), entry.getValue());
        }
        return stmt.query(FreelancerSearchResult.class).list();
    }

    public long countSearch(final FreelancerSearchCriteria criteria) {
        final var sql = new StringBuilder("SELECT COUNT(*) FROM freelancer WHERE 1=1");
        final Map<String, Object> params = new LinkedHashMap<>();
        appendStringCriteria(sql, params, criteria);
        if (criteria.salaryLongMax() != null) {
            final String pName = "p" + (params.size() + 1);
            sql.append(" AND salary_long <= :").append(pName);
            params.put(pName, criteria.salaryLongMax());
        }
        if (criteria.salaryPerDayLongMax() != null) {
            final String pName = "p" + (params.size() + 1);
            sql.append(" AND salary_per_day_long <= :").append(pName);
            params.put(pName, criteria.salaryPerDayLongMax());
        }

        var stmt = jdbcClient.sql(sql.toString());
        for (final var entry : params.entrySet()) {
            stmt = stmt.param(entry.getKey(), entry.getValue());
        }
        return stmt.query(Long.class).single();
    }

    public List<FreelancerContactView> findContactsByFreelancerId(final long freelancerId) {
        return jdbcClient.sql("""
                SELECT id, type, value, freelancer_id
                FROM freelancer_contact
                WHERE freelancer_id = :freelancerId
                ORDER BY type ASC, value ASC
                """)
                .param("freelancerId", freelancerId)
                .query(FreelancerContactView.class)
                .list();
    }

    public List<FreelancerHistoryView> findHistoryByFreelancerId(final long freelancerId) {
        return jdbcClient.sql("""
                SELECT fh.id, fh.creation_date, fh.creation_user, fh.changed_date, fh.changed_user,
                       fh.description, fh.type_id, ht.description AS type_description, fh.freelancer_id
                FROM freelancer_history fh
                JOIN historytype ht ON ht.id = fh.type_id
                WHERE fh.freelancer_id = :freelancerId
                ORDER BY fh.creation_date DESC
                """)
                .param("freelancerId", freelancerId)
                .query(FreelancerHistoryView.class)
                .list();
    }

    public List<TagInfo> findTagsByFreelancerId(final long freelancerId) {
        return jdbcClient.sql("""
                SELECT t.id, t.tagname AS name, t.type
                FROM freelancer_tags ft
                JOIN tags t ON t.id = ft.tag_id
                WHERE ft.freelancer_id = :freelancerId
                """)
                .param("freelancerId", freelancerId)
                .query((rs, rowNum) -> new TagInfo(
                        rs.getLong("id"),
                        rs.getString("name"),
                        TagType.valueOf(rs.getString("type"))))
                .list()
                .stream()
                .sorted(Comparator.comparingInt((TagInfo ti) -> ti.type().getOrder())
                        .thenComparing(TagInfo::name))
                .toList();
    }

    public List<TagInfo> findAvailableTagsByFreelancerIdAndType(final long freelancerId, final TagType type) {
        return jdbcClient.sql("""
                SELECT t.id, t.tagname AS name, t.type
                FROM tags t
                WHERE t.type = :type
                  AND t.id NOT IN (
                      SELECT ft.tag_id FROM freelancer_tags ft WHERE ft.freelancer_id = :freelancerId
                  )
                ORDER BY t.tagname ASC
                """)
                .param("type", type.name())
                .param("freelancerId", freelancerId)
                .query((rs, rowNum) -> new TagInfo(
                        rs.getLong("id"),
                        rs.getString("name"),
                        TagType.valueOf(rs.getString("type"))))
                .list();
    }

    private static void appendStringCriteria(final StringBuilder sql, final Map<String, Object> params, final FreelancerSearchCriteria c) {
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

    private static void appendLike(final StringBuilder sql, final Map<String, Object> params, final String column, final String value) {
        if (value != null && !value.isBlank()) {
            final String paramName = "p" + (params.size() + 1);
            sql.append(" AND ").append(column).append(" LIKE :").append(paramName);
            params.put(paramName, "%" + value + "%");
        }
    }
}
