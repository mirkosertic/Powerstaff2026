package de.mirkosertic.powerstaff.freelancer.command;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

interface FreelancerTagRepository extends CrudRepository<FreelancerTag, Long> {

    List<FreelancerTag> findByFreelancerId(Long freelancerId);

    boolean existsByFreelancerIdAndTagId(Long freelancerId, Long tagId);

    void deleteByFreelancerIdAndTagId(Long freelancerId, Long tagId);
}
