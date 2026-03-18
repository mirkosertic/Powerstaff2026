package de.mirkosertic.powerstaff.profilesearch;

import de.mirkosertic.powerstaff.profilesearch.command.ProfileSearchMessage;
import de.mirkosertic.powerstaff.profilesearch.query.LlmProjectContext;

import java.util.List;
import java.util.Optional;

public class StubLlmService implements LlmService {

    @Override
    public String sendMessage(Optional<LlmProjectContext> context,
                              List<ProfileSearchMessage> history,
                              String userMessage) {
        return "Die KI-Profilsuche ist in Release 1.0 noch nicht aktiviert.";
    }
}
