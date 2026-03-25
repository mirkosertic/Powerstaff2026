package de.mirkosertic.powerstaff.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class MustChangePasswordFilter extends OncePerRequestFilter {

    private static final List<String> EXCLUDED_PREFIXES = List.of(
            "/passwort-aendern", "/logout", "/error", "/css/", "/js/", "/generated/"
    );

    private final PsUserRepository repository;

    public MustChangePasswordFilter(final PsUserRepository repository) {
        this.repository = repository;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String)) {

            final String requestUri = request.getRequestURI();
            final boolean isExcluded = EXCLUDED_PREFIXES.stream().anyMatch(requestUri::startsWith);

            if (!isExcluded) {
                final String username = authentication.getName();
                final boolean mustChange = repository.findById(username)
                        .map(PsUser::isMustChangePassword)
                        .orElse(false);

                if (mustChange) {
                    response.sendRedirect("/passwort-aendern");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
