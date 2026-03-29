package de.mirkosertic.powerstaff.project.query;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProjectPositionQueryService {

    private final JdbcClient jdbcClient;

    public ProjectPositionQueryService(final JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<ProjectPositionView> findByProjectId(final Long projectId, final String sortField, final String sortDir) {
        final var safeField = switch (sortField != null ? sortField : "code") {
            case "name1" -> "f.name1";
            case "name2" -> "f.name2";
            case "statusDescription" -> "pps.description";
            default -> "f.code";
        };
        final var safeDir = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";

        return jdbcClient.sql(
                "SELECT pp.id, pp.db_version, pp.freelancer_id, f.code, f.name1, f.name2,"
                + " pp.status_id, pps.description AS status_description, pps.color AS status_color, pps.color_text AS status_color_text,"
                + " pp.konditionen, pp.kommentar, f.contactforbidden"
                + " FROM project_position pp"
                + " JOIN freelancer f ON f.id = pp.freelancer_id"
                + " JOIN project_position_status pps ON pps.id = pp.status_id"
                + " WHERE pp.project_id = :projectId"
                + " ORDER BY " + safeField + " " + safeDir)
                .param("projectId", projectId)
                .query(ProjectPositionView.class)
                .list();
    }

    public boolean existsPosition(final Long projectId, final Long freelancerId) {
        final var count = jdbcClient.sql(
                "SELECT COUNT(*) FROM project_position WHERE project_id = :projectId AND freelancer_id = :freelancerId")
                .param("projectId", projectId)
                .param("freelancerId", freelancerId)
                .query(Long.class)
                .single();
        return count > 0;
    }
}
