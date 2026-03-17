package de.mirkosertic.powerstaff.freelancer.command;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

interface FreelancerContactRepository extends CrudRepository<FreelancerContact, Long> {

    List<FreelancerContact> findByFreelancerId(Long freelancerId);
}
