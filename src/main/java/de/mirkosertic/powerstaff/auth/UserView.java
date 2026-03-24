package de.mirkosertic.powerstaff.auth;

public record UserView(
        String username,
        boolean mustChangePassword,
        boolean enabled,
        String profileSearchSystemPrompt
) {
}
