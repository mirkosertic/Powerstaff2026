package de.mirkosertic.powerstaff.customer.query;

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
public class KundeQueryService {

    private static final Set<String> SORT_FIELDS_ALLOWLIST = Set.of("name1", "company", "city");
    private static final String DEFAULT_SORT = "company ASC";

    private static final String SELECT_KUNDE = """
            SELECT id, db_version, creation_date, creation_user, changed_date, changed_user,
                   company, name1, name2, street, country, plz, city,
                   contactforbidden, show_again, comments, debitor_nr, kreditor_nr
            FROM kunde
            """;

    private final JdbcClient jdbcClient;

    public KundeQueryService(final JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<KundeDetailView> findById(final Long id) {
        return jdbcClient.sql(SELECT_KUNDE + "WHERE id = :id")
                .param("id", id)
                .query(KundeDetailView.class)
                .optional();
    }

    public Optional<KundeDetailView> findFirst() {
        return jdbcClient.sql(SELECT_KUNDE + "ORDER BY id ASC LIMIT 1")
                .query(KundeDetailView.class)
                .optional();
    }

    public Optional<KundeDetailView> findLast() {
        return jdbcClient.sql(SELECT_KUNDE + "ORDER BY id DESC LIMIT 1")
                .query(KundeDetailView.class)
                .optional();
    }

    public Optional<KundeDetailView> findPrevious(final Long currentId) {
        return jdbcClient.sql(SELECT_KUNDE + "WHERE id < :currentId ORDER BY id DESC LIMIT 1")
                .param("currentId", currentId)
                .query(KundeDetailView.class)
                .optional();
    }

    public Optional<KundeDetailView> findNext(final Long currentId) {
        return jdbcClient.sql(SELECT_KUNDE + "WHERE id > :currentId ORDER BY id ASC LIMIT 1")
                .param("currentId", currentId)
                .query(KundeDetailView.class)
                .optional();
    }

    public List<KundeSearchResult> search(final KundeSearchCriteria criteria, final int offset, final int limit) {
        final var sql = new StringBuilder("""
                SELECT id, company, name1, name2, city
                FROM kunde
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
        return stmt.query(KundeSearchResult.class).list();
    }

    public long countSearch(final KundeSearchCriteria criteria) {
        final var sql = new StringBuilder("SELECT COUNT(*) FROM kunde WHERE 1=1");
        final Map<String, Object> params = new LinkedHashMap<>();
        appendCriteria(sql, params, criteria);

        var stmt = jdbcClient.sql(sql.toString());
        for (final var entry : params.entrySet()) {
            stmt = stmt.param(entry.getKey(), entry.getValue());
        }
        return stmt.query(Long.class).single();
    }

    public List<KundeProjectListItem> findProjectsByKundeId(final Long kundeId, final String sortField, final String sortDir) {
        final String safeField = allowedSortField(sortField);
        final String safeDir = "DESC".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
        return jdbcClient.sql("SELECT id, project_number, description_short, workplace, start_date, status"
                + " FROM project"
                + " WHERE customer_id = :kundeId"
                + " ORDER BY " + safeField + " " + safeDir)
                .param("kundeId", kundeId)
                .query(KundeProjectListItem.class)
                .list();
    }

    public List<KundeContactView> findContactsByKundeId(final Long kundeId) {
        return jdbcClient.sql("""
                SELECT id, type, value, kunde_id
                FROM kunde_contact
                WHERE kunde_id = :kundeId
                ORDER BY type ASC, value ASC
                """)
                .param("kundeId", kundeId)
                .query(KundeContactView.class)
                .list();
    }

    public List<KundeHistoryView> findHistoryByKundeId(final Long kundeId) {
        return jdbcClient.sql("""
                SELECT kh.id, kh.creation_date, kh.creation_user, kh.changed_date, kh.changed_user,
                       kh.description, kh.type_id, ht.description AS type_description, kh.kunde_id
                FROM kunde_history kh
                JOIN historytype ht ON ht.id = kh.type_id
                WHERE kh.kunde_id = :kundeId
                ORDER BY kh.creation_date DESC
                """)
                .param("kundeId", kundeId)
                .query(KundeHistoryView.class)
                .list();
    }

    private static String allowedSortField(final String field) {
        return switch (field == null ? "" : field) {
            case "projectNumber" -> "project_number";
            case "descriptionShort" -> "description_short";
            case "workplace" -> "workplace";
            case "status" -> "status";
            default -> "start_date";
        };
    }

    private static void appendCriteria(final StringBuilder sql, final Map<String, Object> params, final KundeSearchCriteria criteria) {
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
}
