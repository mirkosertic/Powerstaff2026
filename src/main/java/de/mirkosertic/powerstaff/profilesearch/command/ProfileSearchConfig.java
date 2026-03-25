package de.mirkosertic.powerstaff.profilesearch.command;

import de.mirkosertic.powerstaff.profilesearch.query.ProfileSearchQueryService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Configuration
public class ProfileSearchConfig {

    @Bean
    public LlmService llmService(final List<ChatModel> modelsInContext, final List<ToolCallbackProvider> toolCallbackProviders, final ProfileSearchCommandService commandService, final ProfileSearchQueryService queryService, final ObjectMapper objectMapper) {
        for (final ChatModel model : modelsInContext) {
            if (model instanceof OllamaChatModel) {
                ChatClient.Builder builder = ChatClient.builder(model);
                builder = builder.defaultToolCallbacks(toolCallbackProviders.toArray(new ToolCallbackProvider[0]));
                return new SpringAILlmService(builder.build(), commandService, queryService, objectMapper);
            }
        }
        return new MockLLmService();
    }
}
