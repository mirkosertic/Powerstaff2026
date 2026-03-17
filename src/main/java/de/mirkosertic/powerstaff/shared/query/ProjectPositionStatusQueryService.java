package de.mirkosertic.powerstaff.shared.query;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectPositionStatusQueryService {

    private final JdbcClient jdbcClient;

    public ProjectPositionStatusQueryService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<ProjectPositionStatusView> findAll() {
        return jdbcClient
                .sql("SELECT id, description, color, color_text FROM project_position_status ORDER BY description ASC")
                .query(ProjectPositionStatusView.class)
                .list();
    }
}
