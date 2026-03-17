package de.mirkosertic.powerstaff.freelancer.command;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Aggregate Root des Freiberufler-Moduls.
 * Nur die für die Partnerz-Zuordnung relevanten Felder sind hier gemappt.
 * Die vollständige Implementierung folgt in Phase 3 (TASKS.md).
 */
@Table("freelancer")
class Freelancer {

    @Id
    private Long id;

    private String code;

    private String company;

    @Column("partner_id")
    private Long partnerId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public Long getPartnerId() { return partnerId; }
    public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }
}
