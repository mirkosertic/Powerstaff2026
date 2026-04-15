package de.mirkosertic.powerstaff.auth;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserQueryService {

    private final JdbcClient jdbcClient;

    public UserQueryService(final JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<UserView> findAll() {
        return jdbcClient
                .sql("SELECT username, must_change_password, enabled, profile_search_system_prompt, is_admin AS admin, llm_api_token FROM ps_user ORDER BY username ASC")
                .query(UserView.class)
                .list();
    }

    public Optional<UserView> findByUsername(final String username) {
        return jdbcClient
                .sql("SELECT username, must_change_password, enabled, profile_search_system_prompt, is_admin AS admin, llm_api_token FROM ps_user WHERE username = :username")
                .param("username", username)
                .query(UserView.class)
                .optional();
    }

    public Optional<String> findFirstAdminLlmApiToken() {
        return jdbcClient
                .sql("SELECT llm_api_token FROM ps_user WHERE is_admin = TRUE AND llm_api_token IS NOT NULL AND llm_api_token <> '' ORDER BY username ASC LIMIT 1")
                .query(String.class)
                .optional();
    }
}
