package de.mirkosertic.powerstaff.profilesearch;

import de.mirkosertic.powerstaff.profilesearch.command.ProfileSearchMessage;
import de.mirkosertic.powerstaff.profilesearch.query.LlmProjectContext;

import java.util.List;
import java.util.Optional;

public interface LlmService {

    String sendMessage(Optional<LlmProjectContext> context,
                       List<ProfileSearchMessage> history,
                       String userMessage);
}
