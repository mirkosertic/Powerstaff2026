package de.mirkosertic.powerstaff.profilesearch.command;

import de.mirkosertic.powerstaff.profilesearch.query.LlmProjectContext;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

public interface LlmService {

    record Reply(long id, String role, String message, String jsonPayload, Integer promptTokens, Integer completionTokens) {}

    String ROLE_USER = "user";
    String ROLE_ASSISTANT = "assistant";
    String ROLE_TOOL_CALL = "tool_call";
    String ROLE_TOOL_RESULT = "tool_result";

    List<Reply> sendMessage(final Principal principal,
                             final String sessionId,
                             final String conversationId,
                             final Optional<LlmProjectContext> context,
                             final String userMessage);

}
