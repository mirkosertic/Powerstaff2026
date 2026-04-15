package de.mirkosertic.powerstaff.profilesearch.command;

import de.mirkosertic.powerstaff.auth.UserQueryService;
import de.mirkosertic.powerstaff.profilesearch.query.ProfileSearchQueryService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Configuration
@EnableConfigurationProperties({ProfileSearchProperties.class, McpConnectionProperties.class})
public class ProfileSearchConfig {

    @Bean
    public LlmService llmService(final List<ChatModel> modelsInContext, final McpClientFactory mcpClientFactory,
            final ProfileSearchCommandService commandService, final ProfileSearchQueryService queryService,
            final ObjectMapper objectMapper, final UserQueryService userQueryService, final OpenAiApi openAiApi) {
        for (final ChatModel model : modelsInContext) {
            if (model instanceof final OllamaChatModel ollamaModel) {
                // Ollama: local server, no API token concept — factory always returns the default client
                final ChatClient defaultClient = ChatClient.builder(ollamaModel).build();
                final LlmChatClientFactory factory = token -> defaultClient;
                return new SpringAILlmService(defaultClient, factory, mcpClientFactory, commandService, queryService, objectMapper, userQueryService);
            }
            if (model instanceof final OpenAiChatModel openAiModel) {
                final ChatClient defaultClient = ChatClient.builder(openAiModel).build();
                final LlmChatClientFactory factory = token -> {
                    if (token == null || token.isBlank()) {
                        return defaultClient;
                    }
                    final OpenAiApi perUserApi = openAiApi.mutate().apiKey(token).build();
                    final OpenAiChatModel perUserModel = openAiModel.mutate().openAiApi(perUserApi).build();
                    return ChatClient.builder(perUserModel).build();
                };
                return new SpringAILlmService(defaultClient, factory, mcpClientFactory, commandService, queryService, objectMapper, userQueryService);
            }
            // Future: AnthropicChatModel — add analogous block here when spring-ai-starter-model-anthropic
            // is added to pom.xml. No changes to SpringAILlmService needed.
        }
        return new MockLLmService();
    }
}
