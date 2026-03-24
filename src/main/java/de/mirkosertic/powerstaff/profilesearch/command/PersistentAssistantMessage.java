package de.mirkosertic.powerstaff.profilesearch.command;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.messages.AssistantMessage;

public class PersistentAssistantMessage extends AssistantMessage implements PersistentMessage {

    public PersistentAssistantMessage(@Nullable String content) {
        super(content);
    }
}
