package de.mirkosertic.powerstaff.freelancer.command;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class FreelancerCommandService {

    private final FreelancerRepository freelancerRepository;
    private final JdbcClient jdbcClient;

    public FreelancerCommandService(FreelancerRepository freelancerRepository,
                                    JdbcClient jdbcClient) {
        this.freelancerRepository = freelancerRepository;
        this.jdbcClient = jdbcClient;
    }

    /**
     * Speichert Freiberufler-Stammdaten.
     */
    public Freelancer save(Freelancer freelancer) {
        return freelancerRepository.save(freelancer);
    }

    @Transactional(readOnly = true)
    public Optional<Freelancer> findById(long id) {
        return freelancerRepository.findById(id);
    }

    /**
     * Sucht einen Freiberufler anhand seines anonymisierten Codes.
     * Liefert ein öffentliches Lookup-Ergebnis für Cross-Modul-Nutzung.
     */
    @Transactional(readOnly = true)
    public Optional<FreelancerLookupResult> findByCode(String code) {
        return freelancerRepository.findByCode(code)
                .map(f -> new FreelancerLookupResult(f.getId(), f.getPartnerId(), f.getCompany()));
    }

    /**
     * Ordnet einen Freiberufler einem Partner zu.
     * Targeted UPDATE – kein vollständiges Aggregate-Save, um den Audit-Trail zu schützen.
     */
    public void assignToPartner(long freelancerId, long partnerId) {
        jdbcClient.sql("UPDATE freelancer SET partner_id = :partnerId WHERE id = :freelancerId")
                .param("partnerId", partnerId)
                .param("freelancerId", freelancerId)
                .update();
    }

    /**
     * Löst die Zuordnung eines Freiberuflers zu einem Partner auf.
     * Der WHERE-Clause auf partner_id verhindert, dass fremde Zuordnungen versehentlich gelöst werden.
     */
    public void removeFromPartner(long freelancerId, long partnerId) {
        jdbcClient.sql("UPDATE freelancer SET partner_id = NULL WHERE id = :freelancerId AND partner_id = :partnerId")
                .param("freelancerId", freelancerId)
                .param("partnerId", partnerId)
                .update();
    }

}
