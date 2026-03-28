package de.mirkosertic.powerstaff.profilesearch.command;

import de.mirkosertic.powerstaff.profilesearch.query.ProfileSearchQueryService;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SpringAIChatRepository implements ChatMemoryRepository {

    private static final Logger logger = LoggerFactory.getLogger(SpringAIChatRepository.class);

    private final String conversationId;
    private final ProfileSearchQueryService queryService;
    private final ProfileSearchCommandService commandService;
    private final List<Message> newMessages;
    private final ObjectMapper objectMapper;
    private final ChatProgressCollector chatProgressCollector;

    public SpringAIChatRepository(final String conversationId, final ProfileSearchQueryService queryService, final ProfileSearchCommandService commandService, final ObjectMapper objectMapper, final ChatProgressCollector chatProgressCollector) {
        this.conversationId = conversationId;
        this.queryService = queryService;
        this.commandService = commandService;
        this.newMessages = new ArrayList<>();
        this.objectMapper = objectMapper;
        this.chatProgressCollector = chatProgressCollector;
    }

    @Override
    public List<String> findConversationIds() {
        return List.of(conversationId);
    }

    @Override
    public List<Message> findByConversationId(final @NonNull String s) {
        if (!conversationId.equals(s)) {
            throw new IllegalArgumentException("Invalid conversationId: " + s);
        }

        return queryService.findMessagesByChat(Long.parseLong(s)).stream()
                .map(mv -> {
                    if (LlmService.ROLE_USER.equals(mv.role())) {
                        return new PersistentUserMessage(mv.content());
                    } else if (LlmService.ROLE_ASSISTANT.equals(mv.role())) {
                        return new PersistentAssistantMessage(mv.content());
                    } else if (LlmService.ROLE_TOOL_CALL.equals(mv.role())) {
                        final var toolCalls = new ArrayList<AssistantMessage.ToolCall>();
                        try {
                            final List<Map<String, Object>> unmarshalled = objectMapper.readValue(mv.jsonPayload(), new TypeReference<ArrayList<Map<String, Object>>>() {
                            });
                            for (final Map<String, Object> call : unmarshalled) {
                                toolCalls.add(new AssistantMessage.ToolCall(call.get("id").toString(), call.get("type").toString(), call.get("name").toString(), call.get("arguments").toString()));
                            }
                        } catch (final JacksonException ex) {
                            logger.warn("Cannot parse tool call payload: {}", mv.jsonPayload(), ex);
                        }
                        return new PersistentAssistantMessage(mv.content(), toolCalls);
                    } else if (LlmService.ROLE_TOOL_RESULT.equals(mv.role())) {
                        final var toolResponses = new ArrayList<ToolResponseMessage.ToolResponse>();
                        try {
                            final List<Map<String, Object>> unmarshalled = objectMapper.readValue(mv.jsonPayload(), new TypeReference<ArrayList<Map<String, Object>>>() {
                            });
                            for (final Map<String, Object> call : unmarshalled) {
                                toolResponses.add(new ToolResponseMessage.ToolResponse(call.get("id").toString(), call.get("name").toString(), call.get("responseData").toString()));
                            }
                        } catch (final JacksonException ex) {
                            logger.warn("Cannot parse tool call payload: {}", mv.jsonPayload(), ex);
                        }
                        return new PersistentToolResponseMessage(toolResponses);
                    } else {
                        logger.warn("Ignoring persistent message: {}", mv);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void saveAll(final @NonNull String s, final List<Message> list) {
        for (final Message message : list) {
            if (!(message instanceof PersistentMessage)) {
                if (message instanceof final UserMessage x) {
                    logger.info("Persisting UserMessage: {}", message);
                    commandService.addMessage(Long.parseLong(s), LlmService.ROLE_USER, x.getText());
                    newMessages.add(message);
                } else if (message instanceof final AssistantMessage x) {
                    logger.info("Persisting AssistantMessage: {}", message);
                    if (x.hasToolCalls()) {
                        final List<Map<String, Object>> toolCallOptions = new ArrayList<>();
                        for (final AssistantMessage.ToolCall toolCall : x.getToolCalls()) {
                            final Map<String, Object> call = new HashMap<>();
                            call.put("name", toolCall.name());
                            call.put("arguments", toolCall.arguments());
                            call.put("type", toolCall.type());
                            call.put("id", toolCall.id());
                            toolCallOptions.add(call);
                        }

                        final String jsonPayload = objectMapper.writeValueAsString(toolCallOptions);

                        commandService.addMessage(Long.parseLong(s), LlmService.ROLE_TOOL_CALL, x.getText(), jsonPayload);

                        chatProgressCollector.toolInvocation(x.getText(), jsonPayload);
                    } else {
                        commandService.addMessage(Long.parseLong(s), LlmService.ROLE_ASSISTANT, x.getText());
                    }
                    newMessages.add(message);
                } else if (message instanceof final ToolResponseMessage x) {
                    final List<Map<String, Object>> toolCallOptions = new ArrayList<>();
                    final List<String> toolCallNames = new ArrayList<>();
                    for (final ToolResponseMessage.ToolResponse toolResponse : x.getResponses()) {
                        final Map<String, Object> call = new HashMap<>();
                        call.put("name", toolResponse.name());
                        call.put("responseData", toolResponse.responseData());
                        call.put("id", toolResponse.id());
                        toolCallOptions.add(call);
                        toolCallNames.add(toolResponse.name());
                    }

                    final String toolnames = objectMapper.writeValueAsString(toolCallNames);
                    final String jsonPayload = objectMapper.writeValueAsString(toolCallOptions);

                    commandService.addMessage(Long.parseLong(s), LlmService.ROLE_TOOL_RESULT, toolnames, jsonPayload);

                    chatProgressCollector.toolResponses(toolnames, jsonPayload);

                    newMessages.add(message);
                } else {
                    logger.warn("Cannot persist message: {}", message);
                }
            }
        }
    }

    @Override
    public void deleteByConversationId(final @NonNull String s) {
        logger.info("Deleting messages for conversationId: {}", s);
    }

    public List<Message> getNewMessages() {
        return newMessages;
    }
}
