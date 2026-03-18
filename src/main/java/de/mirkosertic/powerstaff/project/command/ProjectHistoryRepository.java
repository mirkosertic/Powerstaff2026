package de.mirkosertic.powerstaff.project.command;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

interface ProjectHistoryRepository extends CrudRepository<ProjectHistory, Long> {

    List<ProjectHistory> findByProjectId(Long projectId);
}
