package de.mirkosertic.powerstaff.freelancer.command;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("freelancer_history")
class FreelancerHistory {

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

    @LastModifiedBy
    @Column("changed_user")
    private String changedUser;

    private String description;

    @Column("type_id")
    private Long typeId;

    @Column("freelancer_id")
    private Long freelancerId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }

    public String getCreationUser() { return creationUser; }
    public void setCreationUser(String creationUser) { this.creationUser = creationUser; }

    public LocalDateTime getChangedDate() { return changedDate; }
    public void setChangedDate(LocalDateTime changedDate) { this.changedDate = changedDate; }

    public String getChangedUser() { return changedUser; }
    public void setChangedUser(String changedUser) { this.changedUser = changedUser; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getTypeId() { return typeId; }
    public void setTypeId(Long typeId) { this.typeId = typeId; }

    public Long getFreelancerId() { return freelancerId; }
    public void setFreelancerId(Long freelancerId) { this.freelancerId = freelancerId; }
}
