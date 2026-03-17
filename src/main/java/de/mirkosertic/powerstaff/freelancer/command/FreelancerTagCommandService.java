package de.mirkosertic.powerstaff.freelancer.command;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FreelancerTagCommandService {

    private final FreelancerTagRepository tagRepository;

    public FreelancerTagCommandService(FreelancerTagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    /**
     * Ordnet einen Tag einem Freiberufler zu.
     * Wirft {@link DuplicateTagException} wenn der Tag bereits zugeordnet ist.
     */
    public FreelancerTag addTag(long freelancerId, long tagId) {
        if (tagRepository.existsByFreelancerIdAndTagId(freelancerId, tagId)) {
            throw new DuplicateTagException(freelancerId, tagId);
        }
        var tag = new FreelancerTag();
        tag.setFreelancerId(freelancerId);
        tag.setTagId(tagId);
        try {
            return tagRepository.save(tag);
        } catch (DataIntegrityViolationException e) {
            // Race condition: Duplicate zwischen existsBy-Check und save abfangen
            throw new DuplicateTagException(freelancerId, tagId);
        }
    }

    /**
     * Entfernt eine Tag-Zuordnung anhand der Zuordnungs-ID.
     */
    public void removeTag(long freelancerTagId) {
        tagRepository.deleteById(freelancerTagId);
    }
}
