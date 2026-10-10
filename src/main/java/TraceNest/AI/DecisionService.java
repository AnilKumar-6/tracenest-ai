
package TraceNest.AI;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import TraceNest.AI.dto.DecisionDashboardResponse;

@Service
public class DecisionService {

    private final DecisionRepository decisionRepository;
    private final DecisionHistoryRepository historyRepository;

    public DecisionService(
            DecisionRepository decisionRepository,
            DecisionHistoryRepository historyRepository) {
        this.decisionRepository = decisionRepository;
        this.historyRepository = historyRepository;
    }

    public List<Decision> getAllDecisions() {
        return decisionRepository.findAll();
    }

    public Decision getDecisionById(Long id) {
        return decisionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Decision not found"));
    }

    @Transactional
    public Decision createDecision(Decision decision) {
        Decision savedDecision = decisionRepository.save(decision);

        historyRepository.save(new DecisionHistory(
                savedDecision.getId(),
                "CREATED",
                "Decision created: " + savedDecision.getTitle()
        ));

        return savedDecision;
    }

    @Transactional
    public Decision updateDecision(Long id, Decision updatedDecision) {
        Decision existingDecision = decisionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Decision not found"));

        String oldTitle = existingDecision.getTitle();
        String oldReason = existingDecision.getReason();
        String oldStatus = existingDecision.getStatus();

        existingDecision.setTitle(updatedDecision.getTitle());
        existingDecision.setReason(updatedDecision.getReason());
        existingDecision.setStatus(updatedDecision.getStatus());

        Decision savedDecision = decisionRepository.save(existingDecision);

        String details = "Updated decision. "
                + "Title: " + oldTitle + " -> " + savedDecision.getTitle()
                + "; Reason: " + oldReason + " -> " + savedDecision.getReason()
                + "; Status: " + oldStatus + " -> " + savedDecision.getStatus();

        historyRepository.save(new DecisionHistory(
                savedDecision.getId(),
                "UPDATED",
                details
        ));

        return savedDecision;
    }

    @Transactional
    public void deleteDecision(Long id) {
        Decision decision = decisionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Decision not found"));

        historyRepository.save(new DecisionHistory(
                decision.getId(),
                "DELETED",
                "Decision deleted: " + decision.getTitle()
        ));

        decisionRepository.delete(decision);
    }

    public List<Decision> searchDecisionsByTitle(String title) {
        return decisionRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Decision> getDecisionsByStatus(String status) {
        return decisionRepository.findByStatusIgnoreCase(status);
    }

    public Page<Decision> getDecisionsPaginated(Pageable pageable) {
        return decisionRepository.findAll(pageable);
    }

    public DecisionDashboardResponse getDecisionDashboard() {
        long total = decisionRepository.count();
        long pending = decisionRepository.countByStatusIgnoreCase("PENDING");
        long inProgress = decisionRepository.countByStatusIgnoreCase("IN_PROGRESS");
        long completed = decisionRepository.countByStatusIgnoreCase("COMPLETED");

        return new DecisionDashboardResponse(
                total, pending, inProgress, completed
        );
    }
}
