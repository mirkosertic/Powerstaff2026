package de.mirkosertic.powerstaff.project.api;

import de.mirkosertic.powerstaff.project.command.ProjectHistory;
import de.mirkosertic.powerstaff.project.command.ProjectHistoryCommandService;
import de.mirkosertic.powerstaff.project.query.ProjectHistoryQueryService;
import de.mirkosertic.powerstaff.project.query.ProjectHistoryView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/project/{projectId}/history")
public class ProjectHistoryController {

    private final ProjectHistoryCommandService commandService;
    private final ProjectHistoryQueryService queryService;

    public ProjectHistoryController(ProjectHistoryCommandService commandService,
                                    ProjectHistoryQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    record HistoryRequest(String description) {}

    @PostMapping
    public ResponseEntity<List<ProjectHistoryView>> create(@PathVariable Long projectId,
                                                           @RequestBody HistoryRequest request) {
        var history = new ProjectHistory();
        history.setProjectId(projectId);
        history.setDescription(request.description());
        commandService.save(history);
        return ResponseEntity.ok(queryService.findByProjectId(projectId));
    }

    @PutMapping("/{historyId}")
    public ResponseEntity<Map<String, Boolean>> update(@PathVariable Long projectId,
                                                       @PathVariable Long historyId,
                                                       @RequestBody HistoryRequest request) {
        var history = commandService.findById(historyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        history.setDescription(request.description());
        commandService.save(history);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @DeleteMapping("/{historyId}")
    public ResponseEntity<Map<String, Boolean>> delete(@PathVariable Long projectId,
                                                       @PathVariable Long historyId) {
        commandService.delete(historyId);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
