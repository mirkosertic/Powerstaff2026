package de.mirkosertic.powerstaff.freelancer.query;

public record FreelancerContactView(
        Long id,
        String type,
        String value,
        Long freelancerId
) {
}
