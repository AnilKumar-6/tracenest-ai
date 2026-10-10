package TraceNest.AI;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DecisionRepository extends JpaRepository<Decision, Long> {

    List<Decision> findByTitleContainingIgnoreCase(String title);

    List<Decision> findByStatusIgnoreCase(String status);

    long countByStatusIgnoreCase(String status);
}