package de.mirkosertic.powerstaff.customer.command;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("kunde_contact")
class KundeContact {

    @Id
    private Long id;

    @CreatedDate
    @Column("creation_date")
    private LocalDateTime creationDate;

    @CreatedBy
    @Column("creation_user")
    private String creationUser;

    @LastModifiedDate
    @Column("changed_date")
    private LocalDateTime changedDate;

    @LastModifiedBy
    @Column("changed_user")
    private String changedUser;

    private String type;
    private String value;

    @Column("kunde_id")
    private Long kundeId;

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

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public Long getKundeId() { return kundeId; }
    public void setKundeId(Long kundeId) { this.kundeId = kundeId; }
}
