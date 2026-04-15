package de.mirkosertic.powerstaff.auth;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("ps_user")
public class PsUser implements Persistable<String> {

    @Id
    private final String username;

    @Column("password_hash")
    private final String passwordHash;

    @Column("must_change_password")
    private final boolean mustChangePassword;

    @Column("enabled")
    private final boolean enabled;

    @Column("profile_search_system_prompt")
    private final String profileSearchSystemPrompt;

    @Column("is_admin")
    private final boolean admin;

    @Column("llm_api_token")
    private final String llmApiToken;

    public static final String DEFAULT_SYSTEM_PROMPT =
            "Du bist ein freundlicher KI-Assistent für den Benutzer {user} und antwortest immer auf deutsch. Dein Name ist Staffi.";

    public PsUser(final String username, final String passwordHash, final boolean mustChangePassword, final boolean enabled,
                  final String profileSearchSystemPrompt, final boolean admin, final String llmApiToken) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.mustChangePassword = mustChangePassword;
        this.enabled = enabled;
        this.profileSearchSystemPrompt = profileSearchSystemPrompt != null
                ? profileSearchSystemPrompt : DEFAULT_SYSTEM_PROMPT;
        this.admin = admin;
        this.llmApiToken = llmApiToken;
    }

    /**
     * Spring Data JDBC: String-PKs sind niemals null, daher würde isNew() ohne
     * Persistable-Implementierung immer false liefern und save() ein UPDATE statt
     * INSERT ausführen. Da alle Passwort-Änderungen direkt per JdbcClient laufen,
     * geht save() im Repository ausschließlich für Neuanlage – daher immer true.
     */
    @Override
    public String getId() {
        return username;
    }

    @Override
    public boolean isNew() {
        return true;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getProfileSearchSystemPrompt() {
        return profileSearchSystemPrompt;
    }

    public boolean isAdmin() {
        return admin;
    }

    public String getLlmApiToken() {
        return llmApiToken;
    }
}
