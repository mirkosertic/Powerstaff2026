package de.mirkosertic.powerstaff.profilesearch.query;

import java.time.LocalDateTime;

public record MessageView(
        Long id,
        LocalDateTime creationDate,
        Long chatId,
        String role,
        int sequence,
        String content,
        String jsonPayload,
        String assistantThoughts
) {
}
