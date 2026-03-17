package de.mirkosertic.powerstaff.freelancer.command;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

interface FreelancerRepository extends CrudRepository<Freelancer, Long> {

    Optional<Freelancer> findByCode(String code);
}
