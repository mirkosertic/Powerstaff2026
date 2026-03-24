package de.mirkosertic.powerstaff.freelancer.command;

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

@Table("freelancer")
public class Freelancer {

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

    private String titel;
    private String name1;
    private String name2;
    private String company;
    private String street;
    private String country;
    private String plz;
    private String city;
    private String nationalitaet;
    private String geburtsdatum;

    @Column("partner_id")
    private Long partnerId;

    @Column("contactforbidden")
    private boolean contactForbidden;

    @Column("show_again")
    private boolean showAgain;

    private String comments;
    private String einsatzdetails;

    @Column("contact_person")
    private String contactPerson;

    @Column("contact_type")
    private String contactType;

    @Column("contact_reason")
    private String contactReason;

    @Column("last_contact_date")
    private LocalDateTime lastContactDate;

    private String kontaktart;

    @Column("availability_as_date")
    private LocalDate availabilityAsDate;

    @Column("salary_long")
    private Long salaryLong;

    @Column("salary_per_day_long")
    private Long salaryPerDayLong;

    @Column("salary_remote")
    private Long salaryRemote;

    @Column("salary_partner_long")
    private Long salaryPartnerLong;

    @Column("salary_partner_per_day_long")
    private Long salaryPartnerPerDayLong;

    private boolean datenschutz;

    @Column("debitor_nr")
    private String debitorNr;

    @Column("gulp_id")
    private String gulpId;

    private String code;
    private String skills;

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

    public String getTitel() { return titel; }
    public void setTitel(String titel) { this.titel = titel; }

    public String getName1() { return name1; }
    public void setName1(String name1) { this.name1 = name1; }

    public String getName2() { return name2; }
    public void setName2(String name2) { this.name2 = name2; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPlz() { return plz; }
    public void setPlz(String plz) { this.plz = plz; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getNationalitaet() { return nationalitaet; }
    public void setNationalitaet(String nationalitaet) { this.nationalitaet = nationalitaet; }

    public String getGeburtsdatum() { return geburtsdatum; }
    public void setGeburtsdatum(String geburtsdatum) { this.geburtsdatum = geburtsdatum; }

    public Long getPartnerId() { return partnerId; }
    public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }

    public boolean isContactForbidden() { return contactForbidden; }
    public void setContactForbidden(boolean contactForbidden) { this.contactForbidden = contactForbidden; }

    public boolean isShowAgain() { return showAgain; }
    public void setShowAgain(boolean showAgain) { this.showAgain = showAgain; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public String getEinsatzdetails() { return einsatzdetails; }
    public void setEinsatzdetails(String einsatzdetails) { this.einsatzdetails = einsatzdetails; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getContactType() { return contactType; }
    public void setContactType(String contactType) { this.contactType = contactType; }

    public String getContactReason() { return contactReason; }
    public void setContactReason(String contactReason) { this.contactReason = contactReason; }

    public LocalDateTime getLastContactDate() { return lastContactDate; }
    public void setLastContactDate(LocalDateTime lastContactDate) { this.lastContactDate = lastContactDate; }

    public String getKontaktart() { return kontaktart; }
    public void setKontaktart(String kontaktart) { this.kontaktart = kontaktart; }

    public LocalDate getAvailabilityAsDate() { return availabilityAsDate; }
    public void setAvailabilityAsDate(LocalDate availabilityAsDate) { this.availabilityAsDate = availabilityAsDate; }

    public Long getSalaryLong() { return salaryLong; }
    public void setSalaryLong(Long salaryLong) { this.salaryLong = salaryLong; }

    public Long getSalaryPerDayLong() { return salaryPerDayLong; }
    public void setSalaryPerDayLong(Long salaryPerDayLong) { this.salaryPerDayLong = salaryPerDayLong; }

    public Long getSalaryRemote() { return salaryRemote; }
    public void setSalaryRemote(Long salaryRemote) { this.salaryRemote = salaryRemote; }

    public Long getSalaryPartnerLong() { return salaryPartnerLong; }
    public void setSalaryPartnerLong(Long salaryPartnerLong) { this.salaryPartnerLong = salaryPartnerLong; }

    public Long getSalaryPartnerPerDayLong() { return salaryPartnerPerDayLong; }
    public void setSalaryPartnerPerDayLong(Long salaryPartnerPerDayLong) { this.salaryPartnerPerDayLong = salaryPartnerPerDayLong; }

    public boolean isDatenschutz() { return datenschutz; }
    public void setDatenschutz(boolean datenschutz) { this.datenschutz = datenschutz; }

    public String getDebitorNr() { return debitorNr; }
    public void setDebitorNr(String debitorNr) { this.debitorNr = debitorNr; }

    public String getGulpId() { return gulpId; }
    public void setGulpId(String gulpId) { this.gulpId = gulpId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
}
