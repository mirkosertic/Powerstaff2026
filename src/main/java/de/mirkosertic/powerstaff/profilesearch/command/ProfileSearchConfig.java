package de.mirkosertic.powerstaff.profilesearch.command;

import de.mirkosertic.powerstaff.auth.UserQueryService;
import de.mirkosertic.powerstaff.profilesearch.query.ProfileSearchQueryService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.Timeout;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableConfigurationProperties({ProfileSearchProperties.class, McpConnectionProperties.class})
public class ProfileSearchConfig {

    @Bean
    public LlmService llmService(final List<ChatModel> modelsInContext, final McpClientFactory mcpClientFactory,
            final ProfileSearchCommandService commandService, final ProfileSearchQueryService queryService,
            final ObjectMapper objectMapper, final UserQueryService userQueryService,
            final Environment env, final ProfileSearchProperties profileSearchProperties) {
        for (final ChatModel model : modelsInContext) {
            if (model instanceof final OllamaChatModel ollamaModel) {
                // Ollama: local server, no API token concept — factory always returns the default client
                final ChatClient defaultClient = ChatClient.builder(ollamaModel).build();
                final LlmChatClientFactory factory = token -> defaultClient;
                return new SpringAILlmService(defaultClient, factory, mcpClientFactory, commandService, queryService, objectMapper, userQueryService);
            }
            if (model instanceof OpenAiChatModel) {
                // Properties aus Environment lesen
                final String baseUrl = env.getProperty("spring.ai.openai.base-url", "https://api.openai.com");
                final String apiKey = env.getProperty("spring.ai.openai.api-key", "dummy");
                final String modelName = env.getProperty("spring.ai.openai.chat.options.model", "gpt-4o");
                final Double temperature = env.getProperty("spring.ai.openai.chat.options.temperature", Double.class, 0.7);
                final Boolean streamUsage = env.getProperty("spring.ai.openai.chat.options.stream-usage", Boolean.class, false);

                final Timeout timeout = Timeout.builder()
                    .read(Duration.ZERO)
                    .request(profileSearchProperties.getStreamingTimeout())
                    .build();

                final OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                    .model(modelName)
                    .temperature(temperature)
                    .streamUsage(streamUsage)
                    .build();

                // Default-Client mit explizitem Timeout — verhindert SDK-Default von 1 Minute
                final OpenAIClient defaultApiClient = OpenAIOkHttpClient.builder()
                    .baseUrl(baseUrl)
                    .apiKey(apiKey)
                    .timeout(timeout)
                    .build();
                final ChatClient defaultClient = ChatClient.builder(
                    OpenAiChatModel.builder().openAiClient(defaultApiClient).options(chatOptions).build()
                ).build();

                // Per-User-Client Factory: User-Token überschreibt den konfigurierten API-Key
                final LlmChatClientFactory factory = token -> {
                    if (token == null || token.isBlank()) {
                        return defaultClient;
                    }
                    final OpenAIClient perUserClient = OpenAIOkHttpClient.builder()
                        .baseUrl(baseUrl)
                        .apiKey(token)
                        .timeout(timeout)
                        .build();
                    return ChatClient.builder(
                        OpenAiChatModel.builder().openAiClient(perUserClient).options(chatOptions).build()
                    ).build();
                };

                return new SpringAILlmService(defaultClient, factory, mcpClientFactory, commandService, queryService, objectMapper, userQueryService);
            }
            // Future: AnthropicChatModel — add analogous block here when spring-ai-starter-model-anthropic
            // is added to pom.xml. No changes to SpringAILlmService needed.
        }
        return new MockLLmService();
    }
}
