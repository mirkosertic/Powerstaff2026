package de.mirkosertic.powerstaff.profilesearch.command;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("profile_search_message")
public class ProfileSearchMessage {

    @Id
    private Long id;

    @CreatedDate
    @InsertOnlyProperty
    @Column("creation_date")
    private LocalDateTime creationDate;

    @Column("chat_id")
    private Long chatId;

    private String role;

    private int sequence;

    private String content;

    @Column("json_payload")
    private String jsonPayload;

    public Long getId() { return id; }
    public void setId(final Long id) { this.id = id; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(final LocalDateTime creationDate) { this.creationDate = creationDate; }

    public Long getChatId() { return chatId; }
    public void setChatId(final Long chatId) { this.chatId = chatId; }

    public String getRole() { return role; }
    public void setRole(final String role) { this.role = role; }

    public int getSequence() { return sequence; }
    public void setSequence(final int sequence) { this.sequence = sequence; }

    public String getContent() { return content; }
    public void setContent(final String content) { this.content = content; }

    public String getJsonPayload() { return jsonPayload; }
    public void setJsonPayload(final String jsonPayload) { this.jsonPayload = jsonPayload; }
}
