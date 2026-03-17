package de.mirkosertic.powerstaff.shared.command;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("tags")
public class Tag {

    @Id
    private Long id;

    @Column("tagname")
    private String tagname;

    private String type;

    public Tag() {
    }

    public Tag(String tagname, String type) {
        this.tagname = tagname;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public String getTagname() {
        return tagname;
    }

    public void setTagname(String tagname) {
        this.tagname = tagname;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
