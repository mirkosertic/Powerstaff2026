package de.mirkosertic.powerstaff.profilesearch.command;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.messages.UserMessage;

public class PersistentUserMessage extends UserMessage implements PersistentMessage {

    public PersistentUserMessage(@Nullable String textContent) {
        super(textContent);
    }
}
