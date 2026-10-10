
package TraceNest.AI;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import TraceNest.AI.dto.DecisionDashboardResponse;

@ExtendWith(MockitoExtension.class)
class DecisionServiceTest {

    @Mock
    private DecisionRepository decisionRepository;

    @Mock
    private DecisionHistoryRepository historyRepository;

    @InjectMocks
    private DecisionService decisionService;

    private Decision decision;

    @BeforeEach
    void setUp() {
        decision = new Decision();
        decision.setTitle("Test Decision");
        decision.setReason("Testing TraceNest AI");
        decision.setStatus("PENDING");
    }

    @Test
    void getAllDecisionsReturnsAllDecisions() {
        when(decisionRepository.findAll()).thenReturn(List.of(decision));

        List<Decision> result = decisionService.getAllDecisions();

        assertEquals(1, result.size());
        assertEquals("Test Decision", result.get(0).getTitle());
    }

    @Test
    void getDecisionByIdReturnsDecisionWhenFound() {
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(decision));

        Decision result = decisionService.getDecisionById(1L);

        assertEquals("Test Decision", result.getTitle());
    }

    @Test
    void getDecisionByIdThrowsWhenNotFound() {
        when(decisionRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> decisionService.getDecisionById(999L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void createDecisionSavesDecisionAndHistory() {
        decision.setId(1L);
        when(decisionRepository.save(decision)).thenReturn(decision);

        Decision result = decisionService.createDecision(decision);

        assertNotNull(result);
        assertEquals("Test Decision", result.getTitle());
        verify(historyRepository).save(any(DecisionHistory.class));
    }

    @Test
    void updateDecisionUpdatesFieldsAndRecordsHistory() {
        Decision updatedDecision = new Decision();
        updatedDecision.setTitle("Updated Decision");
        updatedDecision.setReason("Updated reason");
        updatedDecision.setStatus("IN_PROGRESS");

        when(decisionRepository.findById(1L)).thenReturn(Optional.of(decision));
        when(decisionRepository.save(decision)).thenReturn(decision);

        Decision result = decisionService.updateDecision(1L, updatedDecision);

        assertEquals("Updated Decision", result.getTitle());
        assertEquals("Updated reason", result.getReason());
        assertEquals("IN_PROGRESS", result.getStatus());
        verify(historyRepository).save(any(DecisionHistory.class));
    }

    @Test
    void updateDecisionThrowsWhenNotFound() {
        when(decisionRepository.findById(999L)).thenReturn(Optional.empty());

        Decision updatedDecision = new Decision();
        updatedDecision.setTitle("Updated Decision");
        updatedDecision.setReason("Updated reason");
        updatedDecision.setStatus("IN_PROGRESS");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> decisionService.updateDecision(999L, updatedDecision));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(historyRepository, never()).save(any(DecisionHistory.class));
    }

    @Test
    void deleteDecisionDeletesAndRecordsHistory() {
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(decision));

        decisionService.deleteDecision(1L);

        verify(historyRepository).save(any(DecisionHistory.class));
        verify(decisionRepository).delete(decision);
    }

    @Test
    void deleteDecisionThrowsWhenNotFound() {
        when(decisionRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> decisionService.deleteDecision(999L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(historyRepository, never()).save(any(DecisionHistory.class));
    }

    @Test
    void searchDecisionsByTitleReturnsMatchingDecisions() {
        when(decisionRepository.findByTitleContainingIgnoreCase("Test"))
                .thenReturn(List.of(decision));

        List<Decision> result = decisionService.searchDecisionsByTitle("Test");

        assertEquals(1, result.size());
        verify(decisionRepository).findByTitleContainingIgnoreCase("Test");
    }

    @Test
    void getDecisionsByStatusReturnsMatchingDecisions() {
        when(decisionRepository.findByStatusIgnoreCase("PENDING"))
                .thenReturn(List.of(decision));

        List<Decision> result = decisionService.getDecisionsByStatus("PENDING");

        assertEquals(1, result.size());
        verify(decisionRepository).findByStatusIgnoreCase("PENDING");
    }

    @Test
    void getDecisionsPaginatedReturnsPage() {
        Page<Decision> page = new PageImpl<>(List.of(decision));
        PageRequest pageable = PageRequest.of(0, 10);

        when(decisionRepository.findAll(pageable)).thenReturn(page);

        Page<Decision> result = decisionService.getDecisionsPaginated(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getDecisionDashboardReturnsCounts() {
        when(decisionRepository.count()).thenReturn(5L);
        when(decisionRepository.countByStatusIgnoreCase("PENDING")).thenReturn(2L);
        when(decisionRepository.countByStatusIgnoreCase("IN_PROGRESS")).thenReturn(2L);
        when(decisionRepository.countByStatusIgnoreCase("COMPLETED")).thenReturn(1L);

        DecisionDashboardResponse result = decisionService.getDecisionDashboard();

        assertNotNull(result);
        verify(decisionRepository).count();
        verify(decisionRepository).countByStatusIgnoreCase("PENDING");
        verify(decisionRepository).countByStatusIgnoreCase("IN_PROGRESS");
        verify(decisionRepository).countByStatusIgnoreCase("COMPLETED");
    }
}
