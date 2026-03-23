package de.mirkosertic.powerstaff.auth;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserQueryService {

    private final JdbcClient jdbcClient;

    public UserQueryService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<UserView> findAll() {
        return jdbcClient
                .sql("SELECT username, must_change_password, enabled FROM ps_user ORDER BY username ASC")
                .query(UserView.class)
                .list();
    }

    public Optional<UserView> findByUsername(String username) {
        return jdbcClient
                .sql("SELECT username, must_change_password, enabled FROM ps_user WHERE username = :username")
                .param("username", username)
                .query(UserView.class)
                .optional();
    }
}
