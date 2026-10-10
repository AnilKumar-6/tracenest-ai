
package TraceNest.AI;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import TraceNest.AI.dto.DecisionDashboardResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/decisions")
public class DecisionController {

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("id", "title", "reason", "status");

    private static final Set<String> ALLOWED_STATUSES =
            Set.of("PENDING", "IN_PROGRESS", "COMPLETED");

    private final DecisionService decisionService;

    public DecisionController(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @GetMapping
    public List<Decision> getAllDecisions() {
        return decisionService.getAllDecisions();
    }

    @GetMapping("/{id}")
    public Decision getDecisionById(@PathVariable Long id) {
        validateId(id);
        return decisionService.getDecisionById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Decision createDecision(
            @Valid @RequestBody DecisionRequest request) {

        Decision decision = new Decision();
        decision.setTitle(request.getTitle().trim());
        decision.setReason(request.getReason().trim());
        decision.setStatus(request.getStatus());

        return decisionService.createDecision(decision);
    }

    @PutMapping("/{id}")
    public Decision updateDecision(
            @PathVariable Long id,
            @Valid @RequestBody DecisionRequest request) {

        validateId(id);

        Decision updatedDecision = new Decision();
        updatedDecision.setTitle(request.getTitle().trim());
        updatedDecision.setReason(request.getReason().trim());
        updatedDecision.setStatus(request.getStatus());

        return decisionService.updateDecision(id, updatedDecision);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDecision(@PathVariable Long id) {
        validateId(id);
        decisionService.deleteDecision(id);
    }

    @GetMapping("/dashboard")
    public DecisionDashboardResponse getDecisionDashboard() {
        return decisionService.getDecisionDashboard();
    }

    @GetMapping("/search")
    public List<Decision> searchDecisions(
            @RequestParam String title) {

        if (title.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Search title cannot be blank");
        }

        return decisionService.searchDecisionsByTitle(title.trim());
    }

    @GetMapping("/filter")
    public List<Decision> filterDecisionsByStatus(
            @RequestParam String status) {

        if (status.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status cannot be blank");
        }

        String normalizedStatus = status.trim().toUpperCase();

        if (!ALLOWED_STATUSES.contains(normalizedStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid status. Allowed values: PENDING, IN_PROGRESS, COMPLETED");
        }

        return decisionService.getDecisionsByStatus(normalizedStatus);
    }

    @GetMapping("/page")
    public Page<Decision> getDecisionsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page number cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page size must be between 1 and 100");
        }

        if (sortBy == null
                || !ALLOWED_SORT_FIELDS.contains(sortBy.trim())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid sort field. Allowed fields: id, title, reason, status");
        }

        if (direction == null
                || (!direction.equalsIgnoreCase("asc")
                && !direction.equalsIgnoreCase("desc"))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sort direction must be asc or desc");
        }

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy.trim()).descending()
                : Sort.by(sortBy.trim()).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return decisionService.getDecisionsPaginated(pageable);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Decision ID must be a positive number");
        }
    }
}
