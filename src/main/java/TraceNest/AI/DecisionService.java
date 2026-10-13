
package TraceNest.AI;

import java.util.List;
import java.util.Locale;

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
    private final AppUserRepository appUserRepository;

    public DecisionService(
            DecisionRepository decisionRepository,
            DecisionHistoryRepository historyRepository,
            AppUserRepository appUserRepository) {
        this.decisionRepository = decisionRepository;
        this.historyRepository = historyRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public List<Decision> getAllDecisions(String email) {
        return decisionRepository.findByOwner_EmailIgnoreCase(normalizeEmail(email));
    }

    @Transactional(readOnly = true)
    public Decision getDecisionById(Long id, String email) {
        return findOwnedDecision(id, email);
    }

    @Transactional
    public Decision createDecision(Decision decision, String email) {
        AppUser owner = findUser(email);
        decision.setOwner(owner);
        decision.setId(null);

        Decision saved = decisionRepository.save(decision);

        historyRepository.save(new DecisionHistory(
                saved.getId(),
                "CREATED",
                "Decision created: " + saved.getTitle()));

        return saved;
    }

    @Transactional
    public Decision updateDecision(Long id, Decision updatedDecision, String email) {
        Decision existing = findOwnedDecision(id, email);

        String oldTitle = existing.getTitle();
        String oldReason = existing.getReason();
        String oldStatus = existing.getStatus();

        existing.setTitle(updatedDecision.getTitle());
        existing.setReason(updatedDecision.getReason());
        existing.setStatus(updatedDecision.getStatus());

        Decision saved = decisionRepository.save(existing);

        String details = "Updated decision. "
                + "Title: " + oldTitle + " -> " + saved.getTitle()
                + "; Reason: " + oldReason + " -> " + saved.getReason()
                + "; Status: " + oldStatus + " -> " + saved.getStatus();

        historyRepository.save(new DecisionHistory(
                saved.getId(), "UPDATED", details));

        return saved;
    }

    @Transactional
    public void deleteDecision(Long id, String email) {
        Decision decision = findOwnedDecision(id, email);

        historyRepository.save(new DecisionHistory(
                decision.getId(),
                "DELETED",
                "Decision deleted: " + decision.getTitle()));

        decisionRepository.delete(decision);
    }

    @Transactional(readOnly = true)
    public List<DecisionHistory> getDecisionHistory(Long decisionId, String email) {
        findOwnedDecision(decisionId, email);

        return historyRepository
                .findByDecisionIdOrderByChangedAtDesc(decisionId);
    }

    @Transactional(readOnly = true)
    public List<Decision> searchDecisionsByTitle(String title, String email) {
        return decisionRepository
                .findByOwner_EmailIgnoreCaseAndTitleContainingIgnoreCase(
                        normalizeEmail(email), title);
    }

    @Transactional(readOnly = true)
    public List<Decision> getDecisionsByStatus(String status, String email) {
        return decisionRepository
                .findByOwner_EmailIgnoreCaseAndStatusIgnoreCase(
                        normalizeEmail(email), status);
    }

    @Transactional(readOnly = true)
    public Page<Decision> getDecisionsPaginated(
            Pageable pageable, String search, String status, String email) {

        String searchTerm = search == null ? "" : search.trim();

        String statusFilter =
                status == null || status.isBlank()
                        || status.equalsIgnoreCase("ALL")
                        ? ""
                        : status.trim().toUpperCase(Locale.ROOT);

        if (!statusFilter.isEmpty()
                && !List.of("PENDING", "IN_PROGRESS", "COMPLETED", "CANCELLED")
                        .contains(statusFilter)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid status. Allowed values: PENDING, IN_PROGRESS, COMPLETED, CANCELLED");
        }

        return decisionRepository.findByOwnerAndSearchAndStatus(
                normalizeEmail(email), searchTerm, statusFilter, pageable);
    }

    @Transactional(readOnly = true)
    public DecisionDashboardResponse getDecisionDashboard(String email) {
        String ownerEmail = normalizeEmail(email);

        long total = decisionRepository.countByOwner_EmailIgnoreCase(ownerEmail);
        long pending = decisionRepository
                .countByOwner_EmailIgnoreCaseAndStatusIgnoreCase(ownerEmail, "PENDING");
        long inProgress = decisionRepository
                .countByOwner_EmailIgnoreCaseAndStatusIgnoreCase(ownerEmail, "IN_PROGRESS");
        long completed = decisionRepository
                .countByOwner_EmailIgnoreCaseAndStatusIgnoreCase(ownerEmail, "COMPLETED");

        return new DecisionDashboardResponse(
                total, pending, inProgress, completed);
    }

    private Decision findOwnedDecision(Long id, String email) {
        return decisionRepository
                .findByIdAndOwner_EmailIgnoreCase(id, normalizeEmail(email))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Decision not found"));
    }

    private AppUser findUser(String email) {
        return appUserRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Authenticated user email is missing");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
