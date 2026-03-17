package de.mirkosertic.powerstaff.partner.query;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PartnerQueryService {

    private static final String SELECT_PARTNER = """
            SELECT id, db_version, creation_date, creation_user, changed_date, changed_user,
                   company, name1, name2, street, country, plz, city,
                   contactforbidden, show_again, comments, debitor_nr, kreditor_nr
            FROM partner
            """;

    private final JdbcClient jdbcClient;

    public PartnerQueryService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<PartnerDetailView> findById(Long id) {
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

    public Optional<PartnerDetailView> findPrevious(Long currentId) {
        return jdbcClient.sql(SELECT_PARTNER + "WHERE id < :currentId ORDER BY id DESC LIMIT 1")
                .param("currentId", currentId)
                .query(PartnerDetailView.class)
                .optional();
    }

    public Optional<PartnerDetailView> findNext(Long currentId) {
        return jdbcClient.sql(SELECT_PARTNER + "WHERE id > :currentId ORDER BY id ASC LIMIT 1")
                .param("currentId", currentId)
                .query(PartnerDetailView.class)
                .optional();
    }
}
