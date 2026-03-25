package de.mirkosertic.powerstaff.profilesearch.command;

import de.mirkosertic.powerstaff.profilesearch.query.LlmProjectContext;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

public class MockLLmService implements LlmService {

    public List<Reply> sendMessage(final Principal principal,
                                    final String sessionId,
                                    final String conversationId,
                                    final Optional<LlmProjectContext> context,
                                    final String userMessage) {
        return List.of(new Reply(10, ROLE_ASSISTANT, "Mock Response Nummer 0", null));
    }
}
