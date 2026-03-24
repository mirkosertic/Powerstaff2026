package de.mirkosertic.powerstaff.profilesearch.command;

import de.mirkosertic.powerstaff.profilesearch.query.ProfileSearchQueryService;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SpringAIChatRepository implements ChatMemoryRepository {

    private static final Logger logger = LoggerFactory.getLogger(SpringAIChatRepository.class);

    private final String conversationId;
    private final ProfileSearchQueryService queryService;
    private final ProfileSearchCommandService commandService;

    public SpringAIChatRepository(final String conversationId, final ProfileSearchQueryService queryService, final ProfileSearchCommandService commandService) {
        this.conversationId = conversationId;
        this.queryService = queryService;
        this.commandService = commandService;
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

        final List<Message> messages = queryService.findMessagesByChat(Long.parseLong(s)).stream()
                .map(mv -> {
                    if (LlmService.ROLE_USER.equals(mv.role())) {
                        return new PersistentUserMessage(mv.content());
                    } else if (LlmService.ROLE_SASSISTANT.equals(mv.role())) {
                        return new PersistentAssistantMessage(mv.content());
                    } else {
                        logger.warn("Ignoring persistant message: {}", mv);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return messages;
    }

    @Override
    public void saveAll(final @NonNull String s, final List<Message> list) {
        for (final Message message : list) {
            if (!(message instanceof PersistentMessage)) {
                if (message instanceof UserMessage request) {
                    logger.info("Persisting UserMessage: {}", message);
                    commandService.addMessage(Long.parseLong(s), LlmService.ROLE_USER, request.getText());
                } else if (message instanceof AssistantMessage request) {
                    logger.info("Persisting AssistantMessage: {}", message);
                    commandService.addMessage(Long.parseLong(s), LlmService.ROLE_SASSISTANT, request.getText());
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
}
