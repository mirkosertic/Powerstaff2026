package de.mirkosertic.powerstaff.freelancer.query;

import de.mirkosertic.powerstaff.shared.TagType;

public record TagInfo(
        Long id,
        String name,
        TagType type
) {
}
