package de.mirkosertic.powerstaff.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserCommandService {

    private final PsUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserCommandService(final PsUserRepository repository, final PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void createUser(final String username, final String plainPassword, final boolean mustChangePassword, final boolean enabled) {
        final String hash = passwordEncoder.encode(plainPassword);
        final PsUser user = new PsUser(username, hash, mustChangePassword, enabled, PsUser.DEFAULT_SYSTEM_PROMPT);
        repository.save(user);
    }

    @Transactional
    public void updateSystemPrompt(final String username, final String prompt) {
        repository.updateSystemPrompt(username, prompt != null ? prompt : PsUser.DEFAULT_SYSTEM_PROMPT);
    }

    @Transactional
    public void updateUser(final String username, final boolean mustChangePassword, final boolean enabled) {
        repository.updateFlags(username, mustChangePassword, enabled);
    }

    @Transactional
    public void resetPassword(final String username, final String newPlainPassword) {
        final String hash = passwordEncoder.encode(newPlainPassword);
        repository.updatePassword(username, hash);
    }

    @Transactional
    public void deleteUser(final String username) {
        repository.deleteById(username);
    }

    public Optional<PsUser> findByUsername(final String username) {
        return repository.findById(username);
    }
}
