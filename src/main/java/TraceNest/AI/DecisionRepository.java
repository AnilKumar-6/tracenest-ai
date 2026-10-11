
package TraceNest.AI;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DecisionRepository
        extends JpaRepository<Decision, Long> {

    List<Decision> findByTitleContainingIgnoreCase(String title);

    List<Decision> findByStatusIgnoreCase(String status);

    long countByStatusIgnoreCase(String status);

    @Query(
        value = """
            SELECT d FROM Decision d
            WHERE
                (
                    :search = ''
                    OR LOWER(d.title) LIKE CONCAT('%', LOWER(:search), '%')
                    OR LOWER(d.reason) LIKE CONCAT('%', LOWER(:search), '%')
                    OR CAST(d.id AS string) LIKE CONCAT('%', :search, '%')
                )
                AND
                (
                    :status = ''
                    OR UPPER(d.status) = UPPER(:status)
                )
            """,
        countQuery = """
            SELECT COUNT(d) FROM Decision d
            WHERE
                (
                    :search = ''
                    OR LOWER(d.title) LIKE CONCAT('%', LOWER(:search), '%')
                    OR LOWER(d.reason) LIKE CONCAT('%', LOWER(:search), '%')
                    OR CAST(d.id AS string) LIKE CONCAT('%', :search, '%')
                )
                AND
                (
                    :status = ''
                    OR UPPER(d.status) = UPPER(:status)
                )
            """
    )
    Page<Decision> findBySearchAndStatus(
            @Param("search") String search,
            @Param("status") String status,
            Pageable pageable);
}
