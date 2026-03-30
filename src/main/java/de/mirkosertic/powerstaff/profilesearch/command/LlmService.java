package de.mirkosertic.powerstaff.profilesearch.command;

import de.mirkosertic.powerstaff.profilesearch.query.LlmProjectContext;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface LlmService {

    record Reply(long id, String role, String message, String jsonPayload, Integer promptTokens, Integer completionTokens) {}

    sealed interface ChatStreamEvent permits
            ChatStreamEvent.ThinkingToken,
            ChatStreamEvent.ContentToken,
            ChatStreamEvent.ToolCall,
            ChatStreamEvent.ToolResult,
            ChatStreamEvent.MessageComplete,
            ChatStreamEvent.StreamError {

        // token = ein Reasoning/Thinking-Chunk des Modells
        record ThinkingToken(String token) implements ChatStreamEvent {}

        // token = ein Antwort-Chunk des Modells
        record ContentToken(String token) implements ChatStreamEvent {}

        // name = msg.content() im Template, jsonPayload = msg.jsonPayload()
        record ToolCall(String name, String jsonPayload) implements ChatStreamEvent {}

        // names = msg.content() im Template, jsonPayload = msg.jsonPayload()
        record ToolResult(String names, String jsonPayload) implements ChatStreamEvent {}

        // id = data-msg-id der persistierten Assistant-Nachricht
        record MessageComplete(long id, int promptTokens, int completionTokens, int maxContextTokens) implements ChatStreamEvent {}

        record StreamError(String message) implements ChatStreamEvent {}
    }

    String ROLE_USER = "user";
    String ROLE_ASSISTANT = "assistant";
    String ROLE_TOOL_CALL = "tool_call";
    String ROLE_TOOL_RESULT = "tool_result";

    List<Reply> sendMessage(final Principal principal,
                             final String sessionId,
                             final String conversationId,
                             final Optional<LlmProjectContext> context,
                             final String userMessage);

    void sendMessageStreaming(final Principal principal,
                               final String sessionId,
                               final String conversationId,
                               final Optional<LlmProjectContext> context,
                               final String userMessage,
                               final Consumer<ChatStreamEvent> eventSink);

}
