package de.mirkosertic.powerstaff.project.query;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProjectHistoryQueryService {

    private final JdbcClient jdbcClient;

    public ProjectHistoryQueryService(final JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<ProjectHistoryView> findByProjectId(final Long projectId) {
        return jdbcClient.sql("""
                SELECT id, creation_date, creation_user, changed_date, changed_user, description, project_id
                FROM project_history
                WHERE project_id = :projectId
                ORDER BY creation_date DESC
                """)
                .param("projectId", projectId)
                .query(ProjectHistoryView.class)
                .list();
    }
}
