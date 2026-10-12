
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import TraceNest.AI.dto.DecisionDashboardResponse;

@ExtendWith(MockitoExtension.class)
class DecisionServiceTest {

    private static final String USER_EMAIL = "anil@example.com";
    private static final String OTHER_EMAIL = "other@example.com";

    @Mock
    private DecisionRepository decisionRepository;

    @Mock
    private DecisionHistoryRepository historyRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private DecisionService decisionService;

    private Decision decision;
    private AppUser user;

    @BeforeEach
    void setUp() {
        user = new AppUser(USER_EMAIL, "encoded-password");

        decision = new Decision();
        decision.setTitle("Test Decision");
        decision.setReason("Testing TraceNest AI");
        decision.setStatus("PENDING");
        decision.setOwner(user);
    }

    @Test
    void getAllDecisionsReturnsOnlyCurrentUsersDecisions() {
        when(decisionRepository.findByOwner_EmailIgnoreCase(USER_EMAIL))
                .thenReturn(List.of(decision));

        List<Decision> result =
                decisionService.getAllDecisions(USER_EMAIL);

        assertEquals(1, result.size());
        assertEquals("Test Decision", result.get(0).getTitle());

        verify(decisionRepository)
                .findByOwner_EmailIgnoreCase(USER_EMAIL);
    }

    @Test
    void getDecisionByIdReturnsOwnedDecision() {
        when(decisionRepository
                .findByIdAndOwner_EmailIgnoreCase(1L, USER_EMAIL))
                .thenReturn(Optional.of(decision));

        Decision result =
                decisionService.getDecisionById(1L, USER_EMAIL);

        assertEquals("Test Decision", result.getTitle());
    }

    @Test
    void getDecisionByIdReturnsNotFoundForUnownedDecision() {
        when(decisionRepository
                .findByIdAndOwner_EmailIgnoreCase(999L, USER_EMAIL))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> decisionService.getDecisionById(999L, USER_EMAIL));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void createDecisionAssignsCurrentUserAndRecordsHistory() {
        when(appUserRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(user));

        when(decisionRepository.save(any(Decision.class)))
                .thenAnswer(invocation -> {
                    Decision saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        Decision result =
                decisionService.createDecision(decision, USER_EMAIL);

        assertNotNull(result);
        assertEquals("Test Decision", result.getTitle());
        assertEquals(USER_EMAIL, result.getOwner().getEmail());

        verify(decisionRepository).save(any(Decision.class));
        verify(historyRepository).save(any(DecisionHistory.class));
    }

    @Test
    void updateDecisionUpdatesOwnedDecisionAndRecordsHistory() {
        Decision updated = new Decision();
        updated.setTitle("Updated Decision");
        updated.setReason("Updated reason");
        updated.setStatus("IN_PROGRESS");

        when(decisionRepository
                .findByIdAndOwner_EmailIgnoreCase(1L, USER_EMAIL))
                .thenReturn(Optional.of(decision));

        when(decisionRepository.save(decision)).thenReturn(decision);

        Decision result =
                decisionService.updateDecision(1L, updated, USER_EMAIL);

        assertEquals("Updated Decision", result.getTitle());
        assertEquals("Updated reason", result.getReason());
        assertEquals("IN_PROGRESS", result.getStatus());
        assertEquals(USER_EMAIL, result.getOwner().getEmail());

        verify(historyRepository).save(any(DecisionHistory.class));
    }

    @Test
    void updateDecisionCannotUpdateUnownedDecision() {
        when(decisionRepository
                .findByIdAndOwner_EmailIgnoreCase(999L, USER_EMAIL))
                .thenReturn(Optional.empty());

        Decision updated = new Decision();
        updated.setTitle("Updated Decision");
        updated.setReason("Updated reason");
        updated.setStatus("IN_PROGRESS");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> decisionService.updateDecision(
                        999L, updated, USER_EMAIL));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(decisionRepository, never()).save(any(Decision.class));
        verify(historyRepository, never()).save(any(DecisionHistory.class));
    }

    @Test
    void deleteDecisionDeletesOwnedDecisionAndRecordsHistory() {
        when(decisionRepository
                .findByIdAndOwner_EmailIgnoreCase(1L, USER_EMAIL))
                .thenReturn(Optional.of(decision));

        decisionService.deleteDecision(1L, USER_EMAIL);

        verify(historyRepository).save(any(DecisionHistory.class));
        verify(decisionRepository).delete(decision);
    }

    @Test
    void deleteDecisionCannotDeleteUnownedDecision() {
        when(decisionRepository
                .findByIdAndOwner_EmailIgnoreCase(999L, USER_EMAIL))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> decisionService.deleteDecision(999L, USER_EMAIL));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(decisionRepository, never()).delete(any(Decision.class));
        verify(historyRepository, never()).save(any(DecisionHistory.class));
    }

    @Test
    void searchDecisionsByTitleScopesResultsToCurrentUser() {
        when(decisionRepository
                .findByOwner_EmailIgnoreCaseAndTitleContainingIgnoreCase(
                        USER_EMAIL, "Test"))
                .thenReturn(List.of(decision));

        List<Decision> result =
                decisionService.searchDecisionsByTitle("Test", USER_EMAIL);

        assertEquals(1, result.size());

        verify(decisionRepository)
                .findByOwner_EmailIgnoreCaseAndTitleContainingIgnoreCase(
                        USER_EMAIL, "Test");
    }

    @Test
    void getDecisionsByStatusScopesResultsToCurrentUser() {
        when(decisionRepository
                .findByOwner_EmailIgnoreCaseAndStatusIgnoreCase(
                        USER_EMAIL, "PENDING"))
                .thenReturn(List.of(decision));

        List<Decision> result =
                decisionService.getDecisionsByStatus("PENDING", USER_EMAIL);

        assertEquals(1, result.size());

        verify(decisionRepository)
                .findByOwner_EmailIgnoreCaseAndStatusIgnoreCase(
                        USER_EMAIL, "PENDING");
    }

    @Test
    void getDecisionsPaginatedReturnsOwnerScopedPage() {
        Page<Decision> page = new PageImpl<>(List.of(decision));
        PageRequest pageable = PageRequest.of(0, 10);

        when(decisionRepository.findByOwnerAndSearchAndStatus(
                USER_EMAIL, "", "", pageable))
                .thenReturn(page);

        Page<Decision> result = decisionService.getDecisionsPaginated(
                pageable, null, null, USER_EMAIL);

        assertEquals(1, result.getTotalElements());

        verify(decisionRepository).findByOwnerAndSearchAndStatus(
                USER_EMAIL, "", "", pageable);
    }

    @Test
    void getDecisionDashboardReturnsCurrentUsersCounts() {
        when(decisionRepository.countByOwner_EmailIgnoreCase(USER_EMAIL))
                .thenReturn(5L);

        when(decisionRepository
                .countByOwner_EmailIgnoreCaseAndStatusIgnoreCase(
                        USER_EMAIL, "PENDING"))
                .thenReturn(2L);

        when(decisionRepository
                .countByOwner_EmailIgnoreCaseAndStatusIgnoreCase(
                        USER_EMAIL, "IN_PROGRESS"))
                .thenReturn(2L);

        when(decisionRepository
                .countByOwner_EmailIgnoreCaseAndStatusIgnoreCase(
                        USER_EMAIL, "COMPLETED"))
                .thenReturn(1L);

        DecisionDashboardResponse result =
                decisionService.getDecisionDashboard(USER_EMAIL);

        assertNotNull(result);

        verify(decisionRepository)
                .countByOwner_EmailIgnoreCase(USER_EMAIL);
    }

    @Test
    void getDecisionHistoryRequiresOwnership() {
        when(decisionRepository
                .findByIdAndOwner_EmailIgnoreCase(1L, USER_EMAIL))
                .thenReturn(Optional.of(decision));

        when(historyRepository.findByDecisionIdOrderByChangedAtDesc(1L))
                .thenReturn(List.of());

        List<DecisionHistory> result =
                decisionService.getDecisionHistory(1L, USER_EMAIL);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void getDecisionHistoryCannotAccessUnownedDecision() {
        when(decisionRepository
                .findByIdAndOwner_EmailIgnoreCase(1L, USER_EMAIL))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> decisionService.getDecisionHistory(1L, USER_EMAIL));

        verify(historyRepository, never())
                .findByDecisionIdOrderByChangedAtDesc(1L);
    }
}
