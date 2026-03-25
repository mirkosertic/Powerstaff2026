package de.mirkosertic.powerstaff.profilesearch.command;

import de.mirkosertic.powerstaff.profilesearch.query.LlmProjectContext;

import java.security.Principal;
import java.util.Optional;

public class MockLLmService implements LlmService {

    public Reply sendMessage(final Principal principal,
                              final String sessionId,
                              final String conversationId,
                              final Optional<LlmProjectContext> context,
                              final String userMessage) {
        return new Reply(10, ROLE_SASSISTANT, "Mock Response Nummer 0", null);
    }
}
