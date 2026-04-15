package de.mirkosertic.powerstaff.profilesearch.command;

import de.mirkosertic.powerstaff.auth.UserQueryService;
import de.mirkosertic.powerstaff.auth.PsUser;
import de.mirkosertic.powerstaff.auth.UserView;
import de.mirkosertic.powerstaff.profilesearch.query.LlmFreelancerContext;
import de.mirkosertic.powerstaff.profilesearch.query.LlmProjectContext;
import de.mirkosertic.powerstaff.profilesearch.query.ProfileSearchQueryService;
import io.modelcontextprotocol.client.McpSyncClient;
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
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import tools.jackson.databind.ObjectMapper;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class SpringAILlmService implements LlmService {

    private static final Logger logger = LoggerFactory.getLogger(SpringAILlmService.class);

    private final ChatClient chatClient;
    private final LlmChatClientFactory chatClientFactory;
    private final McpClientFactory mcpClientFactory;
    private final ProfileSearchCommandService commandService;
    private final ProfileSearchQueryService queryService;
    private final ObjectMapper objectMapper;
    private final UserQueryService userQueryService;

    public SpringAILlmService(final ChatClient chatClient, final LlmChatClientFactory chatClientFactory,
            final McpClientFactory mcpClientFactory, final ProfileSearchCommandService commandService,
            final ProfileSearchQueryService queryService, final ObjectMapper objectMapper,
            final UserQueryService userQueryService) {
        this.chatClient = chatClient;
        this.chatClientFactory = chatClientFactory;
        this.mcpClientFactory = mcpClientFactory;
        this.commandService = commandService;
        this.queryService = queryService;
        this.objectMapper = objectMapper;
        this.userQueryService = userQueryService;
    }

    private ChatClient resolveChatClient(final Principal principal) {
        final String token = userQueryService.findByUsername(principal.getName())
                .map(UserView::llmApiToken)
                .filter(t -> t != null && !t.isBlank())
                .or(() -> userQueryService.findFirstAdminLlmApiToken())
                .orElse(null);
        return chatClientFactory.create(token);
    }

    private McpSyncClient tryCreateMcpClient() {
        try {
            final McpSyncClient client = mcpClientFactory.createClient();
            client.initialize();
            return client;
        } catch (final McpConnectionException e) {
            logger.warn("MCP nicht verfügbar, Chat läuft ohne Tools: {}", e.getMessage());
            return null;
        }
    }

    private static void closeMcpClientQuietly(final McpSyncClient client) {
        if (client != null) {
            try {
                client.close();
            } catch (final Exception e) {
                logger.warn("Fehler beim Schließen des MCP Clients", e);
            }
        }
    }

    // package-private für Tests: Routing von ChatResponse-Tokens auf ChatProgressCollector-Callbacks
    void routeTokenToCollector(final ChatResponse response, final ChatProgressCollector collector) {
        final Usage usage = response.getMetadata().getUsage();
        if (!(usage instanceof EmptyUsage)) {
            collector.reportUsage(usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
        }
        for (final var result : response.getResults()) {
            final AssistantMessage output = result.getOutput();
            if (output.getText() == null) {
                final Object reasoningContent = output.getMetadata().get("reasoningContent");
                if (reasoningContent != null && !reasoningContent.toString().isEmpty()) {
                    collector.thinkingToken(reasoningContent.toString());
                } else {
                    logger.debug("ChatResponse ohne Text und ohne reasoningContent – ignoriert");
                }
            } else if (!output.getText().isEmpty()) {
                collector.assistantResponseToken(output.getText());
            }
            if (result.getMetadata().getFinishReason() != null && "STOP".equals(result.getMetadata().getFinishReason())) {
                collector.stopped();
            }
        }
    }

    private static void nullsafeAttributeAppend(final String attribute, final String value, final StringBuilder sb) {
        sb.append(attribute).append(": ").append(value != null ? value : "--Unbekannt--").append("\n");
    }

    private String resolveSystemPrompt(final Principal principal, final Optional<LlmProjectContext> context) {
        final var template = userQueryService.findByUsername(principal.getName())
                .flatMap(u -> Optional.ofNullable(u.profileSearchSystemPrompt()))
                .orElse(PsUser.DEFAULT_SYSTEM_PROMPT);

        final StringBuilder result = new StringBuilder(new PromptTemplate(template).render(Map.of("user", principal.getName())));
        result.append("\nWenn Du nach Freiberuflern oder Kandidaten suchst, kannst Du im Lucene Index suchen!\n");
        if (context.isPresent()) {
            final LlmProjectContext project = context.get();
            result.append("\nDas aktuelle Projekt ist:\n");
            nullsafeAttributeAppend("**Projektnummer**", project.projectNumber(), result);
            nullsafeAttributeAppend("**Kurzbeschreibung**", project.descriptionShort(), result);
            nullsafeAttributeAppend("**Beschreibung**", project.descriptionLong(), result);
            nullsafeAttributeAppend("**Einsatzort**", project.workplace(), result);
            nullsafeAttributeAppend("**Benötigte Skills**", project.skills(), result);
            nullsafeAttributeAppend("**Dauer**", project.duration(), result);

            if (project.positions() != null && !project.positions().isEmpty()) {
                result.append("\n\nBereits dem aktuellen Projekt zugewiesen\n");
                for (final LlmFreelancerContext position : project.positions()) {
                    result.append(" - ");
                    nullsafeAttributeAppend("**Kodierung bzw. Dateiname**", position.code(), result);
                    result.append("   ");
                    nullsafeAttributeAppend("**Status**", position.positionStatus(), result);
                    result.append("   ");
                    nullsafeAttributeAppend("**Skills**", position.skills(), result);
                }
            }
        }
        return result.toString();
    }

    @Override
    public List<Reply> sendMessage(final Principal principal, final String sessionId, final String conversationId, final Optional<LlmProjectContext> context, final String userMessage) {
        final var progressCollector = new ChatProgressCollector() {
            final StringBuilder assistantThoughts = new StringBuilder();

            @Override
            public void thinkingToken(final String token) {
                assistantThoughts.append(token);
            }

            @Override
            public String getAssistantThoughtsAndReset() {
                if (!assistantThoughts.isEmpty()) {
                    final String result = assistantThoughts.toString();
                    assistantThoughts.setLength(0);
                    return result;
                }
                return "";
            }
        };

        final var chatRepository = new SpringAIChatRepository(conversationId, queryService, commandService, objectMapper, progressCollector);

        final McpSyncClient mcpClient = tryCreateMcpClient();
        final ChatResponse chatClientResponse;
        try {
            var promptSpec = resolveChatClient(principal).prompt()
                    .advisors(
                            ToolCallAdvisor.builder()
                                    .disableInternalConversationHistory()
                                    .advisorOrder(BaseAdvisor.HIGHEST_PRECEDENCE + 300)
                                    .build(),
                            MessageChatMemoryAdvisor.builder(
                                    MessageWindowChatMemory.builder()
                                            .maxMessages(10)
                                            .chatMemoryRepository(chatRepository)
                                            .build())
                                    .conversationId(conversationId)
                                    .build(),
                            new SimpleLoggerAdvisor()
                    )
                    .system(resolveSystemPrompt(principal, context))
                    .user(userMessage);
            if (mcpClient != null) {
                promptSpec = promptSpec.toolCallbacks(SyncMcpToolCallbackProvider.builder().addMcpClient(mcpClient).build());
            }
            chatClientResponse = promptSpec
                    .stream()
                    .chatResponse()
                    .filter(t -> {
                        routeTokenToCollector(t, progressCollector);
                        return true;
                    })
                    .blockLast();
        } finally {
            closeMcpClientQuietly(mcpClient);
        }

        int promptTokens = 0;
        int completionTokens = 0;
        if (chatClientResponse != null && chatClientResponse.getMetadata() != null) {
            final Usage usage = chatClientResponse.getMetadata().getUsage();
            if (usage != null) {
                logger.info("Collected chat usage: {}", usage);
                promptTokens = usage.getPromptTokens() != null ? usage.getPromptTokens() : 0;
                completionTokens = usage.getCompletionTokens() != null ? usage.getCompletionTokens() : 0;
            }
        }

        final List<Message> persistedMessages = chatRepository.getNewMessages();
        final List<Reply> replies = new ArrayList<>();
        // Skip the first message (index 0 = user message, already shown on frontend)
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

    @Override
    public void sendMessageStreaming(final Principal principal, final String sessionId, final String conversationId,
            final Optional<LlmProjectContext> context, final String userMessage,
            final Consumer<LlmService.ChatStreamEvent> eventSink) {

        final var progressCollector = new StreamingChatProgressCollector(eventSink);
        final var chatRepository = new SpringAIChatRepository(conversationId, queryService, commandService, objectMapper, progressCollector);

        final McpSyncClient mcpClient = tryCreateMcpClient();
        final ChatResponse chatClientResponse;
        try {
            var promptSpec = resolveChatClient(principal).prompt()
                    .advisors(
                            ToolCallAdvisor.builder()
                                    .disableInternalConversationHistory()
                                    .advisorOrder(BaseAdvisor.HIGHEST_PRECEDENCE + 300)
                                    .build(),
                            MessageChatMemoryAdvisor.builder(
                                    MessageWindowChatMemory.builder()
                                            .maxMessages(10)
                                            .chatMemoryRepository(chatRepository)
                                            .build())
                                    .conversationId(conversationId)
                                    .build(),
                            new SimpleLoggerAdvisor()
                    )
                    .system(resolveSystemPrompt(principal, context))
                    .user(userMessage);
            if (mcpClient != null) {
                promptSpec = promptSpec.toolCallbacks(SyncMcpToolCallbackProvider.builder().addMcpClient(mcpClient).build());
            }
            chatClientResponse = promptSpec
                    .stream()
                    .chatResponse()
                    .filter(t -> {
                        routeTokenToCollector(t, progressCollector);
                        return true;
                    })
                    .blockLast();
        } finally {
            closeMcpClientQuietly(mcpClient);
        }

        int promptTokens = 0;
        int completionTokens = 0;
        if (chatClientResponse != null && chatClientResponse.getMetadata() != null) {
            final Usage usage = chatClientResponse.getMetadata().getUsage();
            if (usage != null) {
                logger.info("Collected streaming chat usage: {}", usage);
                promptTokens = usage.getPromptTokens() != null ? usage.getPromptTokens() : 0;
                completionTokens = usage.getCompletionTokens() != null ? usage.getCompletionTokens() : 0;
            }
        }

        // maxContextTokens wird vom Controller ergänzt (er kennt ProfileSearchProperties)
        eventSink.accept(new LlmService.ChatStreamEvent.MessageComplete(
                chatRepository.getLastAssistantMessageId(), promptTokens, completionTokens, 0));
    }

    // package-private für Tests
    static final class StreamingChatProgressCollector implements ChatProgressCollector {

        private final Consumer<LlmService.ChatStreamEvent> eventSink;
        private final StringBuilder assistantThoughts = new StringBuilder();

        StreamingChatProgressCollector(final Consumer<LlmService.ChatStreamEvent> eventSink) {
            this.eventSink = eventSink;
        }

        @Override
        public void thinkingToken(final String token) {
            assistantThoughts.append(token);
            eventSink.accept(new LlmService.ChatStreamEvent.ThinkingToken(token));
        }

        @Override
        public void assistantResponseToken(final String token) {
            eventSink.accept(new LlmService.ChatStreamEvent.ContentToken(token));
        }

        @Override
        public void toolInvocation(final String toolName, final String jsonPayload) {
            eventSink.accept(new LlmService.ChatStreamEvent.ToolCall(toolName, jsonPayload));
        }

        @Override
        public void toolResponses(final String toolNames, final String jsonPayload) {
            eventSink.accept(new LlmService.ChatStreamEvent.ToolResult(toolNames, jsonPayload));
        }

        @Override
        public String getAssistantThoughtsAndReset() {
            if (!assistantThoughts.isEmpty()) {
                final String result = assistantThoughts.toString();
                assistantThoughts.setLength(0);
                return result;
            }
            return "";
        }
    }
}
