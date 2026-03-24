package de.mirkosertic.powerstaff.shared.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class StammdatenCommandService {

    private final HistoryTypeRepository historyTypeRepository;
    private final ProjectPositionStatusRepository projectPositionStatusRepository;
    private final TagRepository tagRepository;

    public StammdatenCommandService(
            HistoryTypeRepository historyTypeRepository,
            ProjectPositionStatusRepository projectPositionStatusRepository,
            TagRepository tagRepository) {
        this.historyTypeRepository = historyTypeRepository;
        this.projectPositionStatusRepository = projectPositionStatusRepository;
        this.tagRepository = tagRepository;
    }

    public HistoryType saveHistoryType(HistoryType ht) {
        return historyTypeRepository.save(ht);
    }

    public void deleteHistoryType(Long id) {
        historyTypeRepository.deleteById(id);
    }

    public Optional<HistoryType> findHistoryTypeById(Long id) {
        return historyTypeRepository.findById(id);
    }

    @Transactional
    public ProjectPositionStatus saveProjectPositionStatus(ProjectPositionStatus pps) {
        if (pps.isDefaultStatus()) {
            projectPositionStatusRepository.findByDefaultStatusTrue().ifPresent(existing -> {
                if (!existing.getId().equals(pps.getId())) {
                    existing.setDefaultStatus(false);
                    projectPositionStatusRepository.save(existing);
                }
            });
        }
        return projectPositionStatusRepository.save(pps);
    }

    public Optional<ProjectPositionStatus> findProjectPositionStatusById(Long id) {
        return projectPositionStatusRepository.findById(id);
    }

    public Optional<ProjectPositionStatus> findDefaultProjectPositionStatus() {
        return projectPositionStatusRepository.findByDefaultStatusTrue();
    }

    public void deleteProjectPositionStatus(Long id) {
        projectPositionStatusRepository.deleteById(id);
    }

    public Tag saveTag(Tag tag) {
        return tagRepository.save(tag);
    }

    public void deleteTag(Long id) {
        tagRepository.deleteById(id);
    }

    public Optional<Tag> findTagById(Long id) {
        return tagRepository.findById(id);
    }
}
