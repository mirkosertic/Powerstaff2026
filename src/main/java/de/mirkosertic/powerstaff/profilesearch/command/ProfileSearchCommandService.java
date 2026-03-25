package de.mirkosertic.powerstaff.profilesearch.command;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class ProfileSearchCommandService {

    private final ProfileSearchChatRepository chatRepository;
    private final ProfileSearchMessageRepository messageRepository;
    private final JdbcClient jdbcClient;

    public ProfileSearchCommandService(final ProfileSearchChatRepository chatRepository,
                                       final ProfileSearchMessageRepository messageRepository,
                                       final JdbcClient jdbcClient) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.jdbcClient = jdbcClient;
    }

    public Long createChat(final String userId, final Long projectId) {
        final var chat = new ProfileSearchChat();
        chat.setProjectId(projectId);
        final var saved = chatRepository.save(chat);
        // @CreatedBy sets creation_user from SecurityContextHolder (may be "system" in tests).
        // Override explicitly with the passed userId.
        jdbcClient.sql("UPDATE profile_search_chat SET creation_user = :userId, changed_date = :now WHERE id = :id")
                .param("userId", userId)
                .param("now", LocalDateTime.now())
                .param("id", saved.getId())
                .update();
        return saved.getId();
    }

    public void deleteChat(final Long chatId) {
        chatRepository.deleteById(chatId);
    }

    public ProfileSearchMessage addMessage(final Long chatId, final String role, final String content) {
        return addMessage(chatId, role, content, null);
    }

    public ProfileSearchMessage addMessage(final Long chatId, final String role, final String content, final String jsonPayload) {
        // Determine next sequence number
        final var maxSeq = jdbcClient.sql(
                "SELECT COALESCE(MAX(sequence), 0) FROM profile_search_message WHERE chat_id = :chatId")
                .param("chatId", chatId)
                .query(Integer.class)
                .single();

        final var message = new ProfileSearchMessage();
        message.setChatId(chatId);
        message.setRole(role);
        message.setSequence(maxSeq + 1);
        message.setContent(content);
        message.setJsonPayload(jsonPayload);
        final var saved = messageRepository.save(message);

        // Update changedDate in chat
        jdbcClient.sql("UPDATE profile_search_chat SET changed_date = :now WHERE id = :chatId")
                .param("now", LocalDateTime.now())
                .param("chatId", chatId)
                .update();

        // Generate title from first user message (first 60 chars)
        if (LlmService.ROLE_USER.equals(role)) {
            final var isFirstUserMessage = jdbcClient.sql(
                    "SELECT COUNT(*) FROM profile_search_message WHERE chat_id = :chatId AND role = 'user'")
                    .param("chatId", chatId)
                    .query(Long.class)
                    .single() == 1;

            if (isFirstUserMessage) {
                final var title = content.length() > 60 ? content.substring(0, 60) : content;
                jdbcClient.sql("UPDATE profile_search_chat SET title = :title WHERE id = :chatId")
                        .param("title", title)
                        .param("chatId", chatId)
                        .update();
            }
        }

        return saved;
    }
}
