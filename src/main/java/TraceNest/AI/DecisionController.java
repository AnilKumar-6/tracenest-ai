
package TraceNest.AI;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
public class DecisionController {

    private final DecisionRepository decisionRepository;

    public DecisionController(DecisionRepository decisionRepository) {
        this.decisionRepository = decisionRepository;
    }

    @GetMapping("/api/decisions")
    public List<Decision> getAllDecisions() {
        return decisionRepository.findAll();
    }

@PostMapping("/api/decisions")
@ResponseStatus(HttpStatus.CREATED)
public Decision createDecision(@Valid @RequestBody Decision decision) {
    return decisionRepository.save(decision);
}
}