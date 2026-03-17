package de.mirkosertic.powerstaff.freelancer.command;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

interface FreelancerHistoryRepository extends CrudRepository<FreelancerHistory, Long> {

    List<FreelancerHistory> findByFreelancerId(Long freelancerId);
}
