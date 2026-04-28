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
            final Environment env) {
        for (final ChatModel model : modelsInContext) {
            if (model instanceof final OllamaChatModel ollamaModel) {
                // Ollama: local server, no API token concept — factory always returns the default client
                final ChatClient defaultClient = ChatClient.builder(ollamaModel).build();
                final LlmChatClientFactory factory = token -> defaultClient;
                return new SpringAILlmService(defaultClient, factory, mcpClientFactory, commandService, queryService, objectMapper, userQueryService);
            }
            if (model instanceof final OpenAiChatModel openAiModel) {
                // Default-Client: verwendet das Auto-konfigurierte OpenAiChatModel Bean (mit allen Properties aus application.yml)
                final ChatClient defaultClient = ChatClient.builder(openAiModel).build();

                // Properties aus Environment lesen (nur wenn OpenAiChatModel Bean existiert)
                final String baseUrl = env.getProperty("spring.ai.openai.base-url", "https://api.openai.com");
                final String modelName = env.getProperty("spring.ai.openai.chat.options.model", "gpt-4o");
                final Double temperature = env.getProperty("spring.ai.openai.chat.options.temperature", Double.class, 0.7);
                final Boolean streamUsage = env.getProperty("spring.ai.openai.chat.options.stream-usage", Boolean.class, false);

                // Per-User-Client Factory: erstellt neue Instanzen mit User-Token
                final LlmChatClientFactory factory = token -> {
                    if (token == null || token.isBlank()) {
                        return defaultClient;
                    }

                    // OpenAiChatOptions aus Properties bauen (identisch zum Auto-konfigurierten Bean)
                    final OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                        .model(modelName)
                        .temperature(temperature)
                        .streamUsage(streamUsage)
                        .build();

                    // OpenAI Java SDK Client mit User-Token und Base-URL erstellen
                    final OpenAIClient perUserClient = OpenAIOkHttpClient.builder()
                        .baseUrl(baseUrl)  // KRITISCH: localhost:1234 in application-local.yml
                        .apiKey(token)
                        .timeout(Timeout.builder()
                            .read(Duration.ZERO)           // Kein Timeout zwischen Datenpaketen
                            .request(Duration.ofMinutes(5)) // Max 5 Minuten Gesamtzeit
                            .build())
                        .build();

                    // OpenAiChatModel mit per-User Client und Options erstellen
                    final OpenAiChatModel perUserModel = OpenAiChatModel.builder()
                        .openAiClient(perUserClient)
                        .options(chatOptions)
                        .build();

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
