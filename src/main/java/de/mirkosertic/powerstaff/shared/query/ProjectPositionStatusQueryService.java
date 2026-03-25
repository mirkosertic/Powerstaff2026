package de.mirkosertic.powerstaff.shared.query;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectPositionStatusQueryService {

    private final JdbcClient jdbcClient;

    public ProjectPositionStatusQueryService(final JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<ProjectPositionStatusView> findAll() {
        return jdbcClient
                .sql("SELECT id, description, color, color_text, is_default FROM project_position_status ORDER BY description ASC")
                .query(ProjectPositionStatusView.class)
                .list();
    }

    public java.util.Optional<ProjectPositionStatusView> findDefault() {
        return jdbcClient
                .sql("SELECT id, description, color, color_text, is_default FROM project_position_status WHERE is_default = TRUE LIMIT 1")
                .query(ProjectPositionStatusView.class)
                .optional();
    }
}
