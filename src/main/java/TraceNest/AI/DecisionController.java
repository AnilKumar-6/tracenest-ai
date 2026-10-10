
package TraceNest.AI;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
public class DecisionController {

    private final DecisionService decisionService;

    public DecisionController(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @GetMapping("/api/decisions")
    public List<Decision> getAllDecisions() {
        return decisionService.getAllDecisions();
    }

   @PostMapping("/api/decisions")
@ResponseStatus(HttpStatus.CREATED)
public Decision createDecision(
        @Valid @RequestBody DecisionRequest request) {

    Decision decision = new Decision();
    decision.setTitle(request.getTitle());
    decision.setReason(request.getReason());
    decision.setStatus(request.getStatus());

    return decisionService.createDecision(decision);
}

@PutMapping("/api/decisions/{id}")
public Decision updateDecision(
        @PathVariable Long id,
        @Valid @RequestBody DecisionRequest request) {

    Decision updatedDecision = new Decision();
    updatedDecision.setTitle(request.getTitle());
    updatedDecision.setReason(request.getReason());
    updatedDecision.setStatus(request.getStatus());

    return decisionService.updateDecision(id, updatedDecision);
}

    @DeleteMapping("/api/decisions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDecision(@PathVariable Long id) {
        decisionService.deleteDecision(id);
    }
    
@GetMapping("/api/decisions/{id}")
public Decision getDecisionById(@PathVariable Long id) {
    return decisionService.getDecisionById(id);
}

}
