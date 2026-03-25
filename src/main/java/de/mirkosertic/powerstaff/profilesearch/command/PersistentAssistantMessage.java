package de.mirkosertic.powerstaff.profilesearch.command;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.util.List;
import java.util.Map;

public class PersistentAssistantMessage extends AssistantMessage implements PersistentMessage {

    public PersistentAssistantMessage(@Nullable final String content) {
        super(content);
    }

    public PersistentAssistantMessage(final String content, final List<ToolCall> toolCalls) {
        super(content, Map.of(), toolCalls, List.of());
    }
}
