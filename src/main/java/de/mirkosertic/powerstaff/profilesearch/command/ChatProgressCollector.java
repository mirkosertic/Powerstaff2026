package de.mirkosertic.powerstaff.profilesearch.command;

public interface ChatProgressCollector {

    default void toolInvocation(final String toolName, final String jsonPayload) {

    }

    default void toolResponses(final String toolNames, final String jsonPayload) {

    }

    default void thinkingToken(final String token) {

    }

    default void assistantResponseToken(final String token) {

    }

    default void stopped() {

    }

    default void reportUsage(final Integer promptTokens, final Integer completionTokens, final Integer totalTokens) {

    }
}
