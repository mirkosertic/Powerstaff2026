package de.mirkosertic.powerstaff.profilesearch.command;

import de.mirkosertic.powerstaff.profilesearch.query.LlmProjectContext;
import de.mirkosertic.powerstaff.profilesearch.query.ProfileSearchQueryService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallAdvisor;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

public class SpringAILlmService implements LlmService {

    private final ChatClient chatClient;
    private final ProfileSearchCommandService commandService;
    private final ProfileSearchQueryService queryService;

    public SpringAILlmService(final ChatClient chatClient, final ProfileSearchCommandService commandService, final ProfileSearchQueryService queryService) {
        this.chatClient = chatClient;
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @Override
    public Reply sendMessage(final Principal principal, final String sessionId, final String conversationId, final Optional<LlmProjectContext> context, final String userMessage) {
        final var loggingAdvisor = new SimpleLoggerAdvisor();

        final var toolCallAdvisor = ToolCallAdvisor.builder()
                .disableInternalConversationHistory()
                .advisorOrder(BaseAdvisor.HIGHEST_PRECEDENCE + 300)
                .build();

        final var chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)
                .chatMemoryRepository(new SpringAIChatRepository(conversationId, queryService, commandService))
                .build();

        final var chatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory)
                .conversationId(conversationId)
                .build();

        final var systemPrompt = new PromptTemplate("Du bist ein freundlicher KI-Assistent für den Benutzer {user} und antwortest immer auf deutsch. Dein Name ist Staffi.").render(Map.of("user", principal.getName()));
        final String content = this.chatClient.prompt()
                .advisors(
                        toolCallAdvisor,
                        chatMemoryAdvisor,
                        loggingAdvisor
                )
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();

        return new Reply(-1, ROLE_SASSISTANT, content);
    }
}
