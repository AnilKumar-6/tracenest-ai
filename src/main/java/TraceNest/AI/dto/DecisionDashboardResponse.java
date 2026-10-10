package TraceNest.AI.dto;
import TraceNest.AI.dto.DecisionDashboardResponse;
public class DecisionDashboardResponse {

    private long totalDecisions;
    private long pendingDecisions;
    private long inProgressDecisions;
    private long completedDecisions;

    public DecisionDashboardResponse(
            long totalDecisions,
            long pendingDecisions,
            long inProgressDecisions,
            long completedDecisions) {

        this.totalDecisions = totalDecisions;
        this.pendingDecisions = pendingDecisions;
        this.inProgressDecisions = inProgressDecisions;
        this.completedDecisions = completedDecisions;
    }

    public long getTotalDecisions() {
        return totalDecisions;
    }

    public long getPendingDecisions() {
        return pendingDecisions;
    }

    public long getInProgressDecisions() {
        return inProgressDecisions;
    }

    public long getCompletedDecisions() {
        return completedDecisions;
    }
    
}