package de.mirkosertic.powerstaff.shared.command;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("historytype")
public class HistoryType {

    @Id
    private Long id;

    private String description;

    public HistoryType() {
    }

    public HistoryType(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
