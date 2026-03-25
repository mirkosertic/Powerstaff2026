package de.mirkosertic.powerstaff.shared.query;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoryTypeQueryService {

    private final JdbcClient jdbcClient;

    public HistoryTypeQueryService(final JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<HistoryTypeView> findAll() {
        return jdbcClient
                .sql("SELECT id, description FROM historytype ORDER BY description ASC")
                .query(HistoryTypeView.class)
                .list();
    }
}
