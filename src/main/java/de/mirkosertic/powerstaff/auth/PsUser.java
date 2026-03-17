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

    public PsUser(String username, String passwordHash, boolean mustChangePassword, boolean enabled) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.mustChangePassword = mustChangePassword;
        this.enabled = enabled;
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
}
