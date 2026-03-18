package de.mirkosertic.powerstaff.customer.query;

import java.time.LocalDateTime;

public record KundeDetailView(
        Long id,
        Long dbVersion,
        LocalDateTime creationDate,
        String creationUser,
        LocalDateTime changedDate,
        String changedUser,
        String company,
        String name1,
        String name2,
        String street,
        String country,
        String plz,
        String city,
        boolean contactForbidden,
        boolean showAgain,
        String comments,
        String debitorNr,
        String kreditorNr
) {
}
