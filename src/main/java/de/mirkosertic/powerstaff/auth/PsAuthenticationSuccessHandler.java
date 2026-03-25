package de.mirkosertic.powerstaff.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PsAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final PsUserRepository repository;

    public PsAuthenticationSuccessHandler(final PsUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request,
                                        final HttpServletResponse response,
                                        final Authentication authentication) throws IOException {

        final String username = authentication.getName();
        final boolean mustChange = repository.findById(username)
                .map(PsUser::isMustChangePassword)
                .orElse(false);

        if (mustChange) {
            response.sendRedirect("/passwort-aendern");
        } else {
            response.sendRedirect("/");
        }
    }
}
