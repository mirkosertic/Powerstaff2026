package de.mirkosertic.powerstaff.project.command;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("project_position")
public class ProjectPosition {

    @Id
    private Long id;

    @Version
    @Column("db_version")
    private Long dbVersion;

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

    @Column("project_id")
    private Long projectId;

    @Column("freelancer_id")
    private Long freelancerId;

    @Column("status_id")
    private Long statusId;

    private String konditionen;

    private String kommentar;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDbVersion() { return dbVersion; }
    public void setDbVersion(Long dbVersion) { this.dbVersion = dbVersion; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }

    public String getCreationUser() { return creationUser; }
    public void setCreationUser(String creationUser) { this.creationUser = creationUser; }

    public LocalDateTime getChangedDate() { return changedDate; }
    public void setChangedDate(LocalDateTime changedDate) { this.changedDate = changedDate; }

    public String getChangedUser() { return changedUser; }
    public void setChangedUser(String changedUser) { this.changedUser = changedUser; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getFreelancerId() { return freelancerId; }
    public void setFreelancerId(Long freelancerId) { this.freelancerId = freelancerId; }

    public Long getStatusId() { return statusId; }
    public void setStatusId(Long statusId) { this.statusId = statusId; }

    public String getKonditionen() { return konditionen; }
    public void setKonditionen(String konditionen) { this.konditionen = konditionen; }

    public String getKommentar() { return kommentar; }
    public void setKommentar(String kommentar) { this.kommentar = kommentar; }
}
