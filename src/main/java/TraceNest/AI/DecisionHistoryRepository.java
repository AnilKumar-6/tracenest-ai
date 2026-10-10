
package TraceNest.AI;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DecisionHistoryRepository
        extends JpaRepository<DecisionHistory, Long> {

    List<DecisionHistory> findByDecisionIdOrderByChangedAtDesc(
            Long decisionId
    );
}
