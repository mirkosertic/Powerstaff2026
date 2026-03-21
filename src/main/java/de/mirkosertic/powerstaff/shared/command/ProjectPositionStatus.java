package de.mirkosertic.powerstaff.shared.command;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("project_position_status")
public class ProjectPositionStatus {

    @Id
    private Long id;

    private String description;

    private String color;

    @Column("color_text")
    private String colorText;

    @Column("is_default")
    private boolean defaultStatus;

    public ProjectPositionStatus() {
    }

    public ProjectPositionStatus(String description, String color, String colorText) {
        this.description = description;
        this.color = color;
        this.colorText = colorText;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getColorText() {
        return colorText;
    }

    public void setColorText(String colorText) {
        this.colorText = colorText;
    }

    public boolean isDefaultStatus() {
        return defaultStatus;
    }

    public void setDefaultStatus(boolean defaultStatus) {
        this.defaultStatus = defaultStatus;
    }
}
