package de.mirkosertic.powerstaff.project.command;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("remembered_project")
public class RememberedProject implements Persistable<String> {

    @Id
    @Column("user_id")
    private String userId;

    @Column("project_id")
    private Long projectId;

    /**
     * Steuert ob Spring Data JDBC INSERT oder UPDATE ausführt.
     * Da userId ein String-PK ist, würde isNew() ohne dieses Flag immer false liefern
     * (Spring Data prüft ob PK null ist – bei Strings nie der Fall).
     */
    @Transient
    private boolean isNew = false;

    public RememberedProject() {}

    public RememberedProject(String userId, Long projectId, boolean isNew) {
        this.userId = userId;
        this.projectId = projectId;
        this.isNew = isNew;
    }

    @Override
    public String getId() { return userId; }

    @Override
    public boolean isNew() { return isNew; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
}
