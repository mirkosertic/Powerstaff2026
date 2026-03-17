package de.mirkosertic.powerstaff.freelancer.query;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class FreelancerQueryService {

    private static final String SELECT_FREELANCER = """
            SELECT id, db_version, creation_date, creation_user, changed_date, changed_user,
                   titel, name1, name2, company, street, country, plz, city,
                   nationalitaet, geburtsdatum, partner_id,
                   contactforbidden, show_again, comments, einsatzdetails,
                   contact_person, contact_type, contact_reason, last_contact_date,
                   kontaktart, availability_as_date,
                   salary_long, salary_per_day_long, salary_remote,
                   salary_partner_long, salary_partner_per_day_long,
                   datenschutz, debitor_nr, gulp_id, code, skills
            FROM freelancer
            """;

    private final JdbcClient jdbcClient;

    public FreelancerQueryService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<FreelancerDetailView> findById(long id) {
        return jdbcClient.sql(SELECT_FREELANCER + "WHERE id = :id")
                .param("id", id)
                .query(FreelancerDetailView.class)
                .optional();
    }

    public Optional<FreelancerDetailView> findFirst() {
        return jdbcClient.sql(SELECT_FREELANCER + "ORDER BY id ASC LIMIT 1")
                .query(FreelancerDetailView.class)
                .optional();
    }

    public Optional<FreelancerDetailView> findLast() {
        return jdbcClient.sql(SELECT_FREELANCER + "ORDER BY id DESC LIMIT 1")
                .query(FreelancerDetailView.class)
                .optional();
    }

    public Optional<FreelancerDetailView> findPrevious(long currentId) {
        return jdbcClient.sql(SELECT_FREELANCER + "WHERE id < :currentId ORDER BY id DESC LIMIT 1")
                .param("currentId", currentId)
                .query(FreelancerDetailView.class)
                .optional();
    }

    public Optional<FreelancerDetailView> findNext(long currentId) {
        return jdbcClient.sql(SELECT_FREELANCER + "WHERE id > :currentId ORDER BY id ASC LIMIT 1")
                .param("currentId", currentId)
                .query(FreelancerDetailView.class)
                .optional();
    }
}
