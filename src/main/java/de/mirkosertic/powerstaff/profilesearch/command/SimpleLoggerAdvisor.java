package de.mirkosertic.powerstaff.profilesearch.command;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import reactor.core.publisher.Flux;

public class SimpleLoggerAdvisor implements CallAdvisor, StreamAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(SimpleLoggerAdvisor.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return BaseAdvisor.HIGHEST_PRECEDENCE + 100;
    }


    @Override
    public ChatClientResponse adviseCall(@NonNull final ChatClientRequest chatClientRequest, final CallAdvisorChain callAdvisorChain) {
        logRequest(chatClientRequest);

        final ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);

        logResponse(chatClientResponse);

        return chatClientResponse;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(@NonNull final ChatClientRequest chatClientRequest,
                                                 final StreamAdvisorChain streamAdvisorChain) {
        logRequest(chatClientRequest);

        final Flux<ChatClientResponse> chatClientResponses = streamAdvisorChain.nextStream(chatClientRequest);

        return new ChatClientMessageAggregator().aggregateChatClientResponse(chatClientResponses, this::logResponse);
    }

    private void logRequest(final ChatClientRequest request) {
        logger.debug("request: {}", request);
    }

    private void logResponse(final ChatClientResponse chatClientResponse) {
        logger.debug("response: {}", chatClientResponse);
    }

}