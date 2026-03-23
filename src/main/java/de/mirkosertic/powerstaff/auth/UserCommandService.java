package de.mirkosertic.powerstaff.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserCommandService {

    private final PsUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserCommandService(PsUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void createUser(String username, String plainPassword, boolean mustChangePassword, boolean enabled) {
        String hash = passwordEncoder.encode(plainPassword);
        PsUser user = new PsUser(username, hash, mustChangePassword, enabled);
        repository.save(user);
    }

    @Transactional
    public void updateUser(String username, boolean mustChangePassword, boolean enabled) {
        repository.updateFlags(username, mustChangePassword, enabled);
    }

    @Transactional
    public void resetPassword(String username, String newPlainPassword) {
        String hash = passwordEncoder.encode(newPlainPassword);
        repository.updatePassword(username, hash);
    }

    @Transactional
    public void deleteUser(String username) {
        repository.deleteById(username);
    }

    public Optional<PsUser> findByUsername(String username) {
        return repository.findById(username);
    }
}
