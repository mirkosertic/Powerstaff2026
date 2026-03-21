package de.mirkosertic.powerstaff.shared.command;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

interface ProjectPositionStatusRepository extends CrudRepository<ProjectPositionStatus, Long> {

    Optional<ProjectPositionStatus> findByDefaultStatusTrue();
}
