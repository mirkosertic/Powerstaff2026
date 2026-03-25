package de.mirkosertic.powerstaff.partner.query;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class PartnerQueryService {

    private static final Set<String> SORT_FIELDS_ALLOWLIST = Set.of("name1", "company", "city");
    private static final String DEFAULT_SORT = "company ASC";

    private static final String SELECT_PARTNER = """
            SELECT id, db_version, creation_date, creation_user, changed_date, changed_user,
                   company, name1, name2, street, country, plz, city,
                   contactforbidden, show_again, comments, debitor_nr, kreditor_nr
            FROM partner
            """;

    private final JdbcClient jdbcClient;

    public PartnerQueryService(final JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<PartnerDetailView> findById(final Long id) {
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

    public Optional<PartnerDetailView> findPrevious(final Long currentId) {
        return jdbcClient.sql(SELECT_PARTNER + "WHERE id < :currentId ORDER BY id DESC LIMIT 1")
                .param("currentId", currentId)
                .query(PartnerDetailView.class)
                .optional();
    }

    public Optional<PartnerDetailView> findNext(final Long currentId) {
        return jdbcClient.sql(SELECT_PARTNER + "WHERE id > :currentId ORDER BY id ASC LIMIT 1")
                .param("currentId", currentId)
                .query(PartnerDetailView.class)
                .optional();
    }

    /**
     * QBE search: all non-null/non-blank fields are combined with AND LIKE '%value%'.
     * Returns up to {@code limit} results starting at {@code offset}, sorted by company ASC.
     */
    public List<PartnerSearchResult> search(final PartnerSearchCriteria criteria, final int offset, final int limit) {
        final var sql = new StringBuilder("""
                SELECT id, company, name1, name2, city
                FROM partner
                WHERE 1=1
                """);
        final Map<String, Object> params = new LinkedHashMap<>();
        appendCriteria(sql, params, criteria);
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
        return stmt.query(PartnerSearchResult.class).list();
    }

    public long countSearch(final PartnerSearchCriteria criteria) {
        final var sql = new StringBuilder("SELECT COUNT(*) FROM partner WHERE 1=1");
        final Map<String, Object> params = new LinkedHashMap<>();
        appendCriteria(sql, params, criteria);

        var stmt = jdbcClient.sql(sql.toString());
        for (final var entry : params.entrySet()) {
            stmt = stmt.param(entry.getKey(), entry.getValue());
        }
        return stmt.query(Long.class).single();
    }

    private static void appendCriteria(final StringBuilder sql, final Map<String, Object> params, final PartnerSearchCriteria criteria) {
        appendLike(sql, params, "company", criteria.company());
        appendLike(sql, params, "name1", criteria.name1());
        appendLike(sql, params, "name2", criteria.name2());
        appendLike(sql, params, "street", criteria.street());
        appendLike(sql, params, "country", criteria.country());
        appendLike(sql, params, "plz", criteria.plz());
        appendLike(sql, params, "city", criteria.city());
        appendLike(sql, params, "comments", criteria.comments());
        appendLike(sql, params, "debitor_nr", criteria.debitorNr());
        appendLike(sql, params, "kreditor_nr", criteria.kreditorNr());
    }

    private static void appendLike(final StringBuilder sql, final Map<String, Object> params, final String column, final String value) {
        if (value != null && !value.isBlank()) {
            final String paramName = "p" + (params.size() + 1);
            sql.append(" AND ").append(column).append(" LIKE :").append(paramName);
            params.put(paramName, "%" + value + "%");
        }
    }

    public List<PartnerFreelancerView> findFreelancersByPartner(final Long partnerId) {
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

    public List<PartnerProjectView> findProjectsByPartner(final Long partnerId) {
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

    public List<PartnerContactView> findContactsByPartner(final Long partnerId) {
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

    public List<PartnerHistoryView> findHistoryByPartner(final Long partnerId) {
        return jdbcClient.sql("""
                SELECT ph.id, ph.creation_date, ph.creation_user, ph.changed_date, ph.changed_user,
                       ph.description, ph.type_id, ht.description AS type_description, ph.partner_id
                FROM partner_history ph
                JOIN historytype ht ON ht.id = ph.type_id
                WHERE ph.partner_id = :partnerId
                ORDER BY ph.creation_date DESC
                """)
                .param("partnerId", partnerId)
                .query(PartnerHistoryView.class)
                .list();
    }

}
