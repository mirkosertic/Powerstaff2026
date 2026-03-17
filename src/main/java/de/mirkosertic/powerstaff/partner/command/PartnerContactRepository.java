package de.mirkosertic.powerstaff.partner.command;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

interface PartnerContactRepository extends CrudRepository<PartnerContact, Long> {

    List<PartnerContact> findByPartnerId(Long partnerId);
}
