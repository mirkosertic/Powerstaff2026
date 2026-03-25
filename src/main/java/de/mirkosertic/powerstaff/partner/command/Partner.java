package de.mirkosertic.powerstaff.partner.command;

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

@Table("partner")
public class Partner {

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

    private String company;
    private String name1;
    private String name2;
    private String street;
    private String country;
    private String plz;
    private String city;

    @Column("contactforbidden")
    private boolean contactForbidden;

    @Column("show_again")
    private boolean showAgain;

    private String comments;

    @Column("debitor_nr")
    private String debitorNr;

    @Column("kreditor_nr")
    private String kreditorNr;

    public Long getId() { return id; }
    public void setId(final Long id) { this.id = id; }

    public Long getDbVersion() { return dbVersion; }
    public void setDbVersion(final Long dbVersion) { this.dbVersion = dbVersion; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(final LocalDateTime creationDate) { this.creationDate = creationDate; }

    public String getCreationUser() { return creationUser; }
    public void setCreationUser(final String creationUser) { this.creationUser = creationUser; }

    public LocalDateTime getChangedDate() { return changedDate; }
    public void setChangedDate(final LocalDateTime changedDate) { this.changedDate = changedDate; }

    public String getChangedUser() { return changedUser; }
    public void setChangedUser(final String changedUser) { this.changedUser = changedUser; }

    public String getCompany() { return company; }
    public void setCompany(final String company) { this.company = company; }

    public String getName1() { return name1; }
    public void setName1(final String name1) { this.name1 = name1; }

    public String getName2() { return name2; }
    public void setName2(final String name2) { this.name2 = name2; }

    public String getStreet() { return street; }
    public void setStreet(final String street) { this.street = street; }

    public String getCountry() { return country; }
    public void setCountry(final String country) { this.country = country; }

    public String getPlz() { return plz; }
    public void setPlz(final String plz) { this.plz = plz; }

    public String getCity() { return city; }
    public void setCity(final String city) { this.city = city; }

    public boolean isContactForbidden() { return contactForbidden; }
    public void setContactForbidden(final boolean contactForbidden) { this.contactForbidden = contactForbidden; }

    public boolean isShowAgain() { return showAgain; }
    public void setShowAgain(final boolean showAgain) { this.showAgain = showAgain; }

    public String getComments() { return comments; }
    public void setComments(final String comments) { this.comments = comments; }

    public String getDebitorNr() { return debitorNr; }
    public void setDebitorNr(final String debitorNr) { this.debitorNr = debitorNr; }

    public String getKreditorNr() { return kreditorNr; }
    public void setKreditorNr(final String kreditorNr) { this.kreditorNr = kreditorNr; }
}
