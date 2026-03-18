package de.mirkosertic.powerstaff.project.command;

import org.springframework.data.repository.CrudRepository;

interface RememberedProjectRepository extends CrudRepository<RememberedProject, String> {
}
