
package TraceNest.AI;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/decisions")
public class DecisionHistoryController {

    private final DecisionRepository decisionRepository;
    private final DecisionHistoryRepository historyRepository;

    public DecisionHistoryController(
            DecisionRepository decisionRepository,
            DecisionHistoryRepository historyRepository) {
        this.decisionRepository = decisionRepository;
        this.historyRepository = historyRepository;
    }

    @GetMapping("/{id}/history")
    public List<DecisionHistory> getDecisionHistory(
            @PathVariable Long id) {

        if (!decisionRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Decision not found");
        }

        return historyRepository
                .findByDecisionIdOrderByChangedAtDesc(id);
    }
}
