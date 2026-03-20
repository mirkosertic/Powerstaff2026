package de.mirkosertic.powerstaff.config;

import de.mirkosertic.powerstaff.auth.MustChangePasswordFilter;
import de.mirkosertic.powerstaff.auth.PsAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final MustChangePasswordFilter mustChangePasswordFilter;
    private final PsAuthenticationSuccessHandler authenticationSuccessHandler;

    public SecurityConfig(MustChangePasswordFilter mustChangePasswordFilter,
                          PsAuthenticationSuccessHandler authenticationSuccessHandler) {
        this.mustChangePasswordFilter = mustChangePasswordFilter;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/login", "/error", "/css/**", "/generated/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(authenticationSuccessHandler)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            .addFilterAfter(mustChangePasswordFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * DelegatingPasswordEncoder: Standard-Algorithmus ist bcrypt.
     * Unterstützt {bcrypt} und {noop} Prefixe in der DB.
     * Passwörter ohne Prefix werden ebenfalls mit BCrypt geprüft (Legacy-Kompatibilität).
     * ADR: Neue Hashes werden immer mit {bcrypt} Prefix gespeichert.
     */
    @Bean
    @SuppressWarnings("deprecation")
    public PasswordEncoder passwordEncoder() {
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        encoders.put("noop", NoOpPasswordEncoder.getInstance());
        DelegatingPasswordEncoder delegating = new DelegatingPasswordEncoder("bcrypt", encoders);
        delegating.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
        return delegating;
    }
}
