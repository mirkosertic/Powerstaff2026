package de.mirkosertic.powerstaff.profilesearch.command;

import de.mirkosertic.powerstaff.profilesearch.query.LlmProjectContext;

import java.security.Principal;
import java.util.Optional;

public interface LlmService {

    record Reply(long id, String role, String message, String jsonPayload) {}

    String ROLE_USER = "user";
    String ROLE_SASSISTANT = "assistant";
    String ROLE_TOOL_CALL = "tool_call";
    String ROLE_TOOL_RESULT = "tool_result";

    Reply sendMessage(final Principal principal,
                       final String sessionId,
                       final String conversationId,
                       final Optional<LlmProjectContext> context,
                       final String userMessage);

}
