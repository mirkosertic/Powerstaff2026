package de.mirkosertic.powerstaff.customer.command;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

interface KundeHistoryRepository extends CrudRepository<KundeHistory, Long> {

    List<KundeHistory> findByKundeId(Long kundeId);
}
