package de.mirkosertic.powerstaff.shared.query;

import de.mirkosertic.powerstaff.shared.TagType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagQueryService {

    private final JdbcClient jdbcClient;

    public TagQueryService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<TagView> findAll() {
        return jdbcClient
                .sql("SELECT id, tagname, type FROM tags ORDER BY tagname ASC")
                .query(TagView.class)
                .list();
    }

    public List<TagView> findByType(TagType type) {
        return jdbcClient
                .sql("SELECT id, tagname, type FROM tags WHERE type = :type ORDER BY tagname ASC")
                .param("type", type.name())
                .query(TagView.class)
                .list();
    }
}
