
package TraceNest.AI;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DecisionService {

    private final DecisionRepository decisionRepository;

    public DecisionService(DecisionRepository decisionRepository) {
        this.decisionRepository = decisionRepository;
    }

    public List<Decision> getAllDecisions() {
        return decisionRepository.findAll();
    }
    
public Decision getDecisionById(Long id) {
    return decisionRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Decision not found"));
}


    public Decision createDecision(Decision decision) {
        return decisionRepository.save(decision);
    }

    public Decision updateDecision(Long id, Decision updatedDecision) {
        Decision existingDecision = decisionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Decision not found"));

        existingDecision.setTitle(updatedDecision.getTitle());
        existingDecision.setReason(updatedDecision.getReason());
        existingDecision.setStatus(updatedDecision.getStatus());

        return decisionRepository.save(existingDecision);
    }

    public void deleteDecision(Long id) {
        if (!decisionRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Decision not found");
        }

        decisionRepository.deleteById(id);
    }
}


