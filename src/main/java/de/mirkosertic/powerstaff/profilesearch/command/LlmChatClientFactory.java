package de.mirkosertic.powerstaff.profilesearch.command;

import org.springframework.ai.chat.client.ChatClient;

@FunctionalInterface
interface LlmChatClientFactory {

    /**
     * Returns a {@link ChatClient} configured for the given API token.
     * If {@code apiToken} is {@code null} or blank, the default client must be returned.
     */
    ChatClient create(String apiToken);
}
