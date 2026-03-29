package de.mirkosertic.powerstaff.profilesearch.command;

import de.mirkosertic.powerstaff.auth.UserQueryService;
import de.mirkosertic.powerstaff.auth.PsUser;
import de.mirkosertic.powerstaff.profilesearch.query.LlmProjectContext;
import de.mirkosertic.powerstaff.profilesearch.query.ProfileSearchQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallAdvisor;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.prompt.PromptTemplate;
import tools.jackson.databind.ObjectMapper;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SpringAILlmService implements LlmService {

    private static final Logger logger = LoggerFactory.getLogger(SpringAILlmService.class);

    private final ChatClient chatClient;
    private final ProfileSearchCommandService commandService;
    private final ProfileSearchQueryService queryService;
    private final ObjectMapper objectMapper;
    private final UserQueryService userQueryService;

    public SpringAILlmService(final ChatClient chatClient, final ProfileSearchCommandService commandService, final ProfileSearchQueryService queryService, final ObjectMapper objectMapper, final UserQueryService userQueryService) {
        this.chatClient = chatClient;
        this.commandService = commandService;
        this.queryService = queryService;
        this.objectMapper = objectMapper;
        this.userQueryService = userQueryService;
    }

    @Override
    public List<Reply> sendMessage(final Principal principal, final String sessionId, final String conversationId, final Optional<LlmProjectContext> context, final String userMessage) {
        final var loggingAdvisor = new SimpleLoggerAdvisor();

        final var toolCallAdvisor = ToolCallAdvisor.builder()
                .disableInternalConversationHistory()
                .advisorOrder(BaseAdvisor.HIGHEST_PRECEDENCE + 300)
                .build();

        final var progressCollector = new ChatProgressCollector() {

            final StringBuilder assistantThoughts = new StringBuilder();

            @Override
            public void toolInvocation(final String toolName, final String jsonPayload) {
            }

            @Override
            public void toolResponses(final String toolNames, final String jsonPayload) {
            }

            @Override
            public void thinkingToken(final String token) {
                assistantThoughts.append(token);
            }

            @Override
            public void assistantResponseToken(final String token) {
            }

            @Override
            public void stopped() {
            }

            @Override
            public void reportUsage(final Integer promptTokens, final Integer completionTokens, final Integer totalTokens) {
            }

            @Override
            public String getAssistantThoughtsAndReset() {
                if (!assistantThoughts.isEmpty()) {
                    final String result = assistantThoughts.toString();
                    assistantThoughts.setLength(0);
                    return result;
                }
                return assistantThoughts.toString();
            }
        };

        final var chatRepository = new SpringAIChatRepository(conversationId, queryService, commandService, objectMapper, progressCollector);

        final var chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)
                .chatMemoryRepository(chatRepository)
                .build();

        final var chatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory)
                .conversationId(conversationId)
                .build();

        final var systemPromptTemplate = userQueryService.findByUsername(principal.getName())
                .flatMap(u -> Optional.ofNullable(u.profileSearchSystemPrompt()))
                .orElse(PsUser.DEFAULT_SYSTEM_PROMPT);
        final var systemPrompt = new PromptTemplate(systemPromptTemplate).render(Map.of("user", principal.getName()));
        final var chatClientResponse = chatClient.prompt()
                .advisors(
                        toolCallAdvisor,
                        chatMemoryAdvisor,
                        loggingAdvisor
                )
                .system(systemPrompt)
                .user(userMessage)
                .stream()
                .chatResponse()
                .filter(t -> {

                    final Usage usage = t.getMetadata().getUsage();
                    if (!(usage instanceof EmptyUsage)) {
                        progressCollector.reportUsage(usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
                    }

                    for (final var result : t.getResults()) {
                        final AssistantMessage output = result.getOutput();
                        final Map<String, Object> metadata = output.getMetadata();
                        if (output.getText() == null) {
                            // No Response from Assistant, maybe it is thinking?
                            final Object reasoningContent = metadata.get("reasoningContent");
                            if (reasoningContent != null) {
                                progressCollector.thinkingToken(reasoningContent.toString());
                            } else {
                                System.out.println("Dont know what to do ....");
                            }
                        } else {
                            progressCollector.assistantResponseToken(output.getText());
                        }
                        if (result.getMetadata().getFinishReason() != null && "STOP".equals(result.getMetadata().getFinishReason())) {
                            progressCollector.stopped();
                        }
                    }
                    return true;
                })
                .blockLast();

        int promptTokens = 0;
        int completionTokens = 0;

        if (chatClientResponse.getMetadata() != null) {
            final Usage usage = chatClientResponse.getMetadata().getUsage();
            if (usage != null) {
                logger.info("Collected chat usage: {}", usage);
                promptTokens = usage.getPromptTokens() != null ? usage.getPromptTokens() : 0;
                completionTokens = usage.getCompletionTokens() != null ? usage.getCompletionTokens() : 0;
            }
        }

        final List<Message> persistedMessages = chatRepository.getNewMessages();
        final List<Reply> replies = new ArrayList<>();
        // Skip the first message, as we already have it on frontend side, but it was persisted by the repository.
        for (int i = 1; i < persistedMessages.size(); i++) {
            final var message = persistedMessages.get(i);

            if (message instanceof final UserMessage msg) {
                replies.add(new Reply(-1, LlmService.ROLE_USER, msg.getText(), null, null, null));
            } else if (message instanceof final AssistantMessage msg) {
                if (msg.hasToolCalls()) {
                    final List<Map<String, Object>> toolCallOptions = new ArrayList<>();
                    for (final AssistantMessage.ToolCall toolCall : msg.getToolCalls()) {
                        final Map<String, Object> call = new HashMap<>();
                        call.put("name", toolCall.name());
                        call.put("arguments", toolCall.arguments());
                        call.put("type", toolCall.type());
                        call.put("id", toolCall.id());
                        toolCallOptions.add(call);
                    }
                    replies.add(new Reply(-1, LlmService.ROLE_TOOL_CALL, msg.getText(), objectMapper.writeValueAsString(toolCallOptions), null, null));
                } else {
                    replies.add(new Reply(-1, LlmService.ROLE_ASSISTANT, msg.getText(), null, promptTokens, completionTokens));
                }
            } else if (message instanceof final ToolResponseMessage msg) {
                final List<Map<String, Object>> toolCallResponses = new ArrayList<>();
                final List<String> toolCallNames = new ArrayList<>();
                for (final ToolResponseMessage.ToolResponse toolResponse : msg.getResponses()) {
                    final Map<String, Object> response = new HashMap<>();
                    response.put("id", toolResponse.id());
                    response.put("name", toolResponse.name());
                    response.put("responseData", toolResponse.responseData());
                    toolCallResponses.add(response);
                    toolCallNames.add(toolResponse.name());
                }
                replies.add(new Reply(-1, LlmService.ROLE_TOOL_RESULT, objectMapper.writeValueAsString(toolCallNames), objectMapper.writeValueAsString(toolCallResponses), null, null));
            } else {
                logger.warn("Unsupported message type: {}", message);
            }
        }
        return replies;
    }
}
