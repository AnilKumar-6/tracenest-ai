
package TraceNest.AI;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "decision_history")
public class DecisionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long decisionId;

    @Column(nullable = false)
    private String action;

    @Column(length = 2000)
    private String details;

    @Column(nullable = false, updatable = false)
    private LocalDateTime changedAt;

    public DecisionHistory() {
    }

    public DecisionHistory(Long decisionId, String action, String details) {
        this.decisionId = decisionId;
        this.action = action;
        this.details = details;
        this.changedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getDecisionId() {
        return decisionId;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    @jakarta.persistence.PrePersist
    protected void onCreate() {
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
    }
}

