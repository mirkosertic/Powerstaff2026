package de.mirkosertic.powerstaff.auth;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PsUserDetailsService implements UserDetailsService {

    private final PsUserRepository repository;

    public PsUserDetailsService(final PsUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        final PsUser user = repository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("Benutzer nicht gefunden: " + username));
        final User.UserBuilder builder = User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .disabled(!user.isEnabled());
        if (user.isAdmin()) {
            builder.roles("USER", "ADMIN");
        } else {
            builder.roles("USER");
        }
        return builder.build();
    }
}
