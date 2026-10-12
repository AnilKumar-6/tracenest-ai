
package TraceNest.AI;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DecisionRepository extends JpaRepository<Decision, Long> {

    List<Decision> findByOwner_EmailIgnoreCase(String email);

    Optional<Decision> findByIdAndOwner_EmailIgnoreCase(
            Long id, String email);

    List<Decision> findByOwner_EmailIgnoreCaseAndTitleContainingIgnoreCase(
            String email, String title);

    List<Decision> findByOwner_EmailIgnoreCaseAndStatusIgnoreCase(
            String email, String status);

    long countByOwner_EmailIgnoreCase(String email);

    long countByOwner_EmailIgnoreCaseAndStatusIgnoreCase(
            String email, String status);

    @Query(
        value = """
            SELECT d FROM Decision d
            WHERE LOWER(d.owner.email) = LOWER(:email)
              AND (
                    :search = ''
                    OR LOWER(d.title) LIKE CONCAT('%', LOWER(:search), '%')
                    OR LOWER(d.reason) LIKE CONCAT('%', LOWER(:search), '%')
                    OR CAST(d.id AS string) LIKE CONCAT('%', :search, '%')
              )
              AND (
                    :status = ''
                    OR UPPER(d.status) = UPPER(:status)
              )
            """,
        countQuery = """
            SELECT COUNT(d) FROM Decision d
            WHERE LOWER(d.owner.email) = LOWER(:email)
              AND (
                    :search = ''
                    OR LOWER(d.title) LIKE CONCAT('%', LOWER(:search), '%')
                    OR LOWER(d.reason) LIKE CONCAT('%', LOWER(:search), '%')
                    OR CAST(d.id AS string) LIKE CONCAT('%', :search, '%')
              )
              AND (
                    :status = ''
                    OR UPPER(d.status) = UPPER(:status)
              )
            """
    )
    Page<Decision> findByOwnerAndSearchAndStatus(
            @Param("email") String email,
            @Param("search") String search,
            @Param("status") String status,
            Pageable pageable);
}
