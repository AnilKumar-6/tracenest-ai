
package TraceNest.AI;

import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://tracenest-ai.vercel.app"
})
@RestController
@RequestMapping("/api/decisions")
public class DecisionHistoryController {

    private final DecisionService decisionService;

    public DecisionHistoryController(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @GetMapping("/{id}/history")
    public List<DecisionHistory> getDecisionHistory(
            @PathVariable Long id, Principal principal) {
        return decisionService.getDecisionHistory(
                id, principal.getName());
    }
}
