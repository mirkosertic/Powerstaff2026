package de.mirkosertic.powerstaff.profilesearch.command;

import de.mirkosertic.powerstaff.profilesearch.query.LlmProjectContext;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class MockLLmService implements LlmService {

    public List<Reply> sendMessage(final Principal principal,
                                    final String sessionId,
                                    final String conversationId,
                                    final Optional<LlmProjectContext> context,
                                    final String userMessage) {
        return List.of(new Reply(10, ROLE_ASSISTANT, "Mock Response Nummer 0", null, 150, 50));
    }

    @Override
    public void sendMessageStreaming(final Principal principal, final String sessionId,
            final String conversationId, final Optional<LlmProjectContext> context,
            final String userMessage, final Consumer<ChatStreamEvent> eventSink) {
        for (final String word : "Mock streaming response Nummer 0. Profil E2E-001 wurde als Kandidat identifiziert.".split(" ")) {
            eventSink.accept(new ChatStreamEvent.ContentToken(word + " "));
        }
        eventSink.accept(new ChatStreamEvent.MessageComplete(10L, 150, 50, 0));
    }
}
