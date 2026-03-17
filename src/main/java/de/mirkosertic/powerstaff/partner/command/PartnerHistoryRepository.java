package de.mirkosertic.powerstaff.partner.command;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

interface PartnerHistoryRepository extends CrudRepository<PartnerHistory, Long> {

    List<PartnerHistory> findByPartnerId(Long partnerId);
}
