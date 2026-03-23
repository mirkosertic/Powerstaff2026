package de.mirkosertic.powerstaff.project.query;

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
public class ProjectQueryService {

    private static final Set<String> SORT_FIELDS_ALLOWLIST = Set.of(
            "project_number", "description_short", "status");
    private static final String DEFAULT_SORT = "entry_date DESC";

    private static final String SELECT_PROJECT = """
            SELECT id, db_version, creation_date, creation_user, changed_date, changed_user,
                   project_number, entry_date, start_date, duration, status, visible_on_web_site,
                   description_short, description_long, skills, workplace,
                   customer_id, partner_id, stundensatz_vk, debitor_nr, kreditor_nr
            FROM project
            """;

    private final JdbcClient jdbcClient;

    public ProjectQueryService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<ProjectDetailView> findById(Long id) {
        return jdbcClient.sql(SELECT_PROJECT + "WHERE id = :id")
                .param("id", id)
                .query(ProjectDetailView.class)
                .optional();
    }

    public Optional<ProjectDetailView> findFirst() {
        return jdbcClient.sql(SELECT_PROJECT + "ORDER BY id ASC LIMIT 1")
                .query(ProjectDetailView.class)
                .optional();
    }

    public Optional<ProjectDetailView> findLast() {
        return jdbcClient.sql(SELECT_PROJECT + "ORDER BY id DESC LIMIT 1")
                .query(ProjectDetailView.class)
                .optional();
    }

    public Optional<ProjectDetailView> findPrevious(Long currentId) {
        return jdbcClient.sql(SELECT_PROJECT + "WHERE id < :currentId ORDER BY id DESC LIMIT 1")
                .param("currentId", currentId)
                .query(ProjectDetailView.class)
                .optional();
    }

    public Optional<ProjectDetailView> findNext(Long currentId) {
        return jdbcClient.sql(SELECT_PROJECT + "WHERE id > :currentId ORDER BY id ASC LIMIT 1")
                .param("currentId", currentId)
                .query(ProjectDetailView.class)
                .optional();
    }

    public List<ProjectSearchResult> search(ProjectSearchCriteria criteria, int offset, int limit) {
        var sql = new StringBuilder("""
                SELECT id, project_number, description_short, workplace, start_date, status, stundensatz_vk
                FROM project
                WHERE 1=1
                """);
        Map<String, Object> params = new LinkedHashMap<>();
        appendLike(sql, params, "project_number", criteria.projectNumber());
        appendLike(sql, params, "description_short", criteria.descriptionShort());
        appendLike(sql, params, "description_long", criteria.descriptionLong());
        appendLike(sql, params, "skills", criteria.skills());
        appendLike(sql, params, "workplace", criteria.workplace());
        appendLike(sql, params, "duration", criteria.duration());
        appendLike(sql, params, "debitor_nr", criteria.debitorNr());
        appendLike(sql, params, "kreditor_nr", criteria.kreditorNr());
        if (criteria.status() != null) {
            String pName = "p" + (params.size() + 1);
            sql.append(" AND status = :").append(pName);
            params.put(pName, criteria.status());
        }
        String orderBy;
        if (criteria.sortField() != null && SORT_FIELDS_ALLOWLIST.contains(criteria.sortField())) {
            String dir = "desc".equalsIgnoreCase(criteria.sortDir()) ? "DESC" : "ASC";
            orderBy = criteria.sortField() + " " + dir;
        } else {
            orderBy = DEFAULT_SORT;
        }
        String pLimit = "p" + (params.size() + 1);
        String pOffset = "p" + (params.size() + 2);
        sql.append(" ORDER BY ").append(orderBy).append(" LIMIT :").append(pLimit).append(" OFFSET :").append(pOffset);
        params.put(pLimit, limit);
        params.put(pOffset, offset);

        var stmt = jdbcClient.sql(sql.toString());
        for (var entry : params.entrySet()) {
            stmt = stmt.param(entry.getKey(), entry.getValue());
        }
        return stmt.query(ProjectSearchResult.class).list();
    }

    public long countSearch(ProjectSearchCriteria criteria) {
        var sql = new StringBuilder("SELECT COUNT(*) FROM project WHERE 1=1");
        Map<String, Object> params = new LinkedHashMap<>();
        appendLike(sql, params, "project_number", criteria.projectNumber());
        appendLike(sql, params, "description_short", criteria.descriptionShort());
        appendLike(sql, params, "description_long", criteria.descriptionLong());
        appendLike(sql, params, "skills", criteria.skills());
        appendLike(sql, params, "workplace", criteria.workplace());
        appendLike(sql, params, "duration", criteria.duration());
        appendLike(sql, params, "debitor_nr", criteria.debitorNr());
        appendLike(sql, params, "kreditor_nr", criteria.kreditorNr());
        if (criteria.status() != null) {
            String pName = "p" + (params.size() + 1);
            sql.append(" AND status = :").append(pName);
            params.put(pName, criteria.status());
        }

        var stmt = jdbcClient.sql(sql.toString());
        for (var entry : params.entrySet()) {
            stmt = stmt.param(entry.getKey(), entry.getValue());
        }
        return stmt.query(Long.class).single();
    }

    private static void appendLike(StringBuilder sql, Map<String, Object> params, String column, String value) {
        if (value != null && !value.isBlank()) {
            String paramName = "p" + (params.size() + 1);
            sql.append(" AND ").append(column).append(" LIKE :").append(paramName);
            params.put(paramName, "%" + value + "%");
        }
    }
}
