package de.mirkosertic.powerstaff.profilesearch.command;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("profile_search_chat")
public class ProfileSearchChat {

    @Id
    private Long id;

    @CreatedDate
    @InsertOnlyProperty
    @Column("creation_date")
    private LocalDateTime creationDate;

    @CreatedBy
    @InsertOnlyProperty
    @Column("creation_user")
    private String creationUser;

    @LastModifiedDate
    @Column("changed_date")
    private LocalDateTime changedDate;

    private String title;

    @Column("project_id")
    private Long projectId;

    public Long getId() { return id; }
    public void setId(final Long id) { this.id = id; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(final LocalDateTime creationDate) { this.creationDate = creationDate; }

    public String getCreationUser() { return creationUser; }
    public void setCreationUser(final String creationUser) { this.creationUser = creationUser; }

    public LocalDateTime getChangedDate() { return changedDate; }
    public void setChangedDate(final LocalDateTime changedDate) { this.changedDate = changedDate; }

    public String getTitle() { return title; }
    public void setTitle(final String title) { this.title = title; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(final Long projectId) { this.projectId = projectId; }
}
