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

import java.time.LocalDate;
import java.time.LocalDateTime;


@Table("project")
public class Project {

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

    @Column("project_number")
    private String projectNumber;

    @Column("entry_date")
    private LocalDate entryDate;

    @Column("start_date")
    private LocalDate startDate;

    private String duration;

    private int status = 1;

    @Column("visible_on_web_site")
    private boolean visibleOnWebSite;

    @Column("description_short")
    private String descriptionShort;

    @Column("description_long")
    private String descriptionLong;

    private String skills;

    private String workplace;

    @Column("customer_id")
    private Long customerId;

    @Column("partner_id")
    private Long partnerId;

    @Column("stundensatz_vk")
    private Long stundensatzVk;

    @Column("debitor_nr")
    private String debitorNr;

    @Column("kreditor_nr")
    private String kreditorNr;

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

    public String getProjectNumber() { return projectNumber; }
    public void setProjectNumber(String projectNumber) { this.projectNumber = projectNumber; }

    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public boolean isVisibleOnWebSite() { return visibleOnWebSite; }
    public void setVisibleOnWebSite(boolean visibleOnWebSite) { this.visibleOnWebSite = visibleOnWebSite; }

    public String getDescriptionShort() { return descriptionShort; }
    public void setDescriptionShort(String descriptionShort) { this.descriptionShort = descriptionShort; }

    public String getDescriptionLong() { return descriptionLong; }
    public void setDescriptionLong(String descriptionLong) { this.descriptionLong = descriptionLong; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getWorkplace() { return workplace; }
    public void setWorkplace(String workplace) { this.workplace = workplace; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getPartnerId() { return partnerId; }
    public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }

    public Long getStundensatzVk() { return stundensatzVk; }
    public void setStundensatzVk(Long stundensatzVk) { this.stundensatzVk = stundensatzVk; }

    public String getDebitorNr() { return debitorNr; }
    public void setDebitorNr(String debitorNr) { this.debitorNr = debitorNr; }

    public String getKreditorNr() { return kreditorNr; }
    public void setKreditorNr(String kreditorNr) { this.kreditorNr = kreditorNr; }
}
