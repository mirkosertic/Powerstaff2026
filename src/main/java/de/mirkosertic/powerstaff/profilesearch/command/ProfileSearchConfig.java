package de.mirkosertic.powerstaff.profilesearch.command;

import de.mirkosertic.powerstaff.auth.UserQueryService;
import de.mirkosertic.powerstaff.profilesearch.query.ProfileSearchQueryService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Configuration
@EnableConfigurationProperties({ProfileSearchProperties.class, McpConnectionProperties.class})
public class ProfileSearchConfig {

    @Bean
    public LlmService llmService(final List<ChatModel> modelsInContext, final McpClientFactory mcpClientFactory, final ProfileSearchCommandService commandService, final ProfileSearchQueryService queryService, final ObjectMapper objectMapper, final UserQueryService userQueryService) {
        for (final ChatModel model : modelsInContext) {
            if (model instanceof OllamaChatModel) {
                final ChatClient chatClient = ChatClient.builder(model).build();
                return new SpringAILlmService(chatClient, mcpClientFactory, commandService, queryService, objectMapper, userQueryService);
            }
            if (model instanceof OpenAiChatModel) {
                final ChatClient chatClient = ChatClient.builder(model).build();
                return new SpringAILlmService(chatClient, mcpClientFactory, commandService, queryService, objectMapper, userQueryService);
            }
        }
        return new MockLLmService();
    }
}
