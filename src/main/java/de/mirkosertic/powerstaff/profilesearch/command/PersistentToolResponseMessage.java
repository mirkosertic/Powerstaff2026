package de.mirkosertic.powerstaff.profilesearch.command;

import org.springframework.ai.chat.messages.ToolResponseMessage;

import java.util.List;
import java.util.Map;

public class PersistentToolResponseMessage extends ToolResponseMessage implements PersistentMessage {

    public PersistentToolResponseMessage(final List<ToolResponse> responses) {
        super(responses, Map.of());
    }
}
