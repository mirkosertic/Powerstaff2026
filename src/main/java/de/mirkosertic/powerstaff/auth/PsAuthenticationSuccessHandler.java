package de.mirkosertic.powerstaff.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PsAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final PsUserRepository repository;

    public PsAuthenticationSuccessHandler(PsUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String username = authentication.getName();
        boolean mustChange = repository.findById(username)
                .map(PsUser::isMustChangePassword)
                .orElse(false);

        if (mustChange) {
            response.sendRedirect("/passwort-aendern");
        } else {
            response.sendRedirect("/");
        }
    }
}
