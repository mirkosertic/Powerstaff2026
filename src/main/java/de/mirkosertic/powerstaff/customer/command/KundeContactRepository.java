package de.mirkosertic.powerstaff.customer.command;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

interface KundeContactRepository extends CrudRepository<KundeContact, Long> {

    List<KundeContact> findByKundeId(Long kundeId);
}
