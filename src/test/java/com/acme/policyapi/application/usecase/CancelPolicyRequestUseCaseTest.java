package com.acme.policyapi.application.usecase;

import com.acme.policyapi.domain.entity.PolicyRequest;
import com.acme.policyapi.domain.entity.PolicyRequestStatus;
import com.acme.policyapi.domain.entity.InsuranceCategory;
import com.acme.policyapi.domain.entity.SalesChannel;
import com.acme.policyapi.domain.entity.PaymentMethod;
import com.acme.policyapi.domain.entity.StatusHistory;
import com.acme.policyapi.domain.repository.PolicyRequestRepository;
import com.acme.policyapi.infrastructure.messaging.PolicyEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelPolicyRequestUseCaseTest {

    @Mock
    private PolicyRequestRepository policyRequestRepository;

    @Mock
    private PolicyEventPublisher eventPublisher;

    @InjectMocks
    private CancelPolicyRequestUseCase cancelPolicyRequestUseCase;

    private UUID testPolicyId;
    private UUID testCustomerId;
    private PolicyRequest policyRequest;
    private String cancellationReason;

    @BeforeEach
    void setUp() {
        testPolicyId = UUID.randomUUID();
        testCustomerId = UUID.randomUUID();
        cancellationReason = "Requested by customer";
        
        setupPolicyRequest();
    }

    private void setupPolicyRequest() {
        policyRequest = new PolicyRequest();
        policyRequest.setId(testPolicyId);
        policyRequest.setCustomerId(testCustomerId);
        policyRequest.setProductId("PROD123");
        policyRequest.setCategory(InsuranceCategory.AUTO);
        policyRequest.setSalesChannel(SalesChannel.WEBSITE);
        policyRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        policyRequest.setStatus(PolicyRequestStatus.RECEIVED);
        policyRequest.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        policyRequest.setTotalMonthlyPremiumAmount(new BigDecimal("150.00"));
        policyRequest.setInsuredAmount(new BigDecimal("50000.00"));

        // Setup collections
        Map<String, BigDecimal> coverages = new HashMap<>();
        coverages.put("COLLISION", new BigDecimal("25000.00"));
        policyRequest.setCoverages(coverages);

        List<String> assistances = Arrays.asList("24h Assistance");
        policyRequest.setAssistances(assistances);

        // Setup history
        List<StatusHistory> history = new ArrayList<>();
        StatusHistory historyItem = new StatusHistory(
            testPolicyId, 
            PolicyRequestStatus.RECEIVED, 
            LocalDateTime.of(2025, 1, 1, 10, 0), 
            "Initial creation"
        );
        history.add(historyItem);
        policyRequest.setHistory(history);
    }

    @Test
    void testExecuteSuccess() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));
        when(policyRequestRepository.save(any(PolicyRequest.class)))
                .thenReturn(policyRequest);

        // Act
        cancelPolicyRequestUseCase.execute(testPolicyId, cancellationReason);

        // Assert
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestCancelled(policyRequest);
    }

    @Test
    void testExecutePolicyRequestNotFound() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> cancelPolicyRequestUseCase.execute(testPolicyId, cancellationReason)
        );

        assertEquals("Solicitação não encontrada: " + testPolicyId, exception.getMessage());
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(policyRequestRepository, never()).save(any());
        verify(eventPublisher, never()).publishPolicyRequestCancelled(any());
    }

    @Test
    void testExecuteCannotBeCancelled() {
        // Arrange - Set policy to a status that cannot be cancelled
        policyRequest.setStatus(PolicyRequestStatus.APPROVED);
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act & Assert - Should throw IllegalStateException from canBeCancelled check OR from status transition
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> cancelPolicyRequestUseCase.execute(testPolicyId, cancellationReason)
        );

        assertTrue(exception.getMessage().contains("não pode ser cancelada") || 
                   exception.getMessage().contains("Transição inválida"));
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(policyRequestRepository, never()).save(any());
        verify(eventPublisher, never()).publishPolicyRequestCancelled(any());
    }

    @Test
    void testExecuteWithReceivedStatus() {
        // Arrange - Policy with RECEIVED status (can be cancelled)
        policyRequest.setStatus(PolicyRequestStatus.RECEIVED);
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        cancelPolicyRequestUseCase.execute(testPolicyId, cancellationReason);

        // Assert
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestCancelled(policyRequest);
    }

    @Test
    void testExecuteWithValidatedStatus() {
        // Arrange - Policy with VALIDATED status (can be cancelled)
        policyRequest.setStatus(PolicyRequestStatus.VALIDATED);
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        cancelPolicyRequestUseCase.execute(testPolicyId, cancellationReason);

        // Assert
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestCancelled(policyRequest);
    }

    @Test
    void testExecuteWithPendingStatus() {
        // Arrange - Policy with PENDING status (can be cancelled)
        policyRequest.setStatus(PolicyRequestStatus.PENDING);
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        cancelPolicyRequestUseCase.execute(testPolicyId, cancellationReason);

        // Assert
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestCancelled(policyRequest);
    }

    @Test
    void testExecuteWithRejectedStatusCannotBeCancelled() {
        // Arrange - Policy with REJECTED status (cannot be cancelled)
        policyRequest.setStatus(PolicyRequestStatus.REJECTED);
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act & Assert - Should throw IllegalStateException (final state)
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> cancelPolicyRequestUseCase.execute(testPolicyId, cancellationReason)
        );

        assertTrue(exception.getMessage().contains("não pode ser cancelada") || 
                   exception.getMessage().contains("Transição inválida"));
        verify(policyRequestRepository, never()).save(any());
        verify(eventPublisher, never()).publishPolicyRequestCancelled(any());
    }

    @Test
    void testExecuteWithCancelledStatusCannotBeCancelled() {
        // Arrange - Policy with CANCELLED status (already final state)
        policyRequest.setStatus(PolicyRequestStatus.CANCELLED);
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act & Assert - Should throw IllegalStateException (already cancelled/final state)
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> cancelPolicyRequestUseCase.execute(testPolicyId, cancellationReason)
        );

        assertTrue(exception.getMessage().contains("não pode ser cancelada") || 
                   exception.getMessage().contains("Transição inválida"));
        verify(policyRequestRepository, never()).save(any());
        verify(eventPublisher, never()).publishPolicyRequestCancelled(any());
    }

    @Test
    void testExecuteWithDifferentReasons() {
        // Test only one valid reason with valid status
        String reason = "Customer request";
        
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        cancelPolicyRequestUseCase.execute(testPolicyId, reason);
        
        // Assert
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestCancelled(policyRequest);
    }

    @Test
    void testExecuteWithEmptyReason() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        cancelPolicyRequestUseCase.execute(testPolicyId, "");

        // Assert - Empty reason should still work
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestCancelled(policyRequest);
    }

    @Test
    void testExecuteWithNullReason() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        cancelPolicyRequestUseCase.execute(testPolicyId, null);

        // Assert - Null reason should still work
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestCancelled(policyRequest);
    }

    @Test
    void testExecuteOrderOfOperations() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        cancelPolicyRequestUseCase.execute(testPolicyId, cancellationReason);

        // Assert - Verify order of operations
        var inOrder = inOrder(policyRequestRepository, eventPublisher);
        inOrder.verify(policyRequestRepository).findById(testPolicyId);
        inOrder.verify(policyRequestRepository).save(policyRequest);
        inOrder.verify(eventPublisher).publishPolicyRequestCancelled(policyRequest);
    }

    @Test
    void testExecuteLogging() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        cancelPolicyRequestUseCase.execute(testPolicyId, cancellationReason);

        // Assert - Verify repository interactions (log content testing would need additional setup)
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(policyRequestRepository, times(1)).save(policyRequest);
    }

    @Test
    void testExecuteWithComplexPolicyRequest() {
        // Arrange - Complex policy request with multiple coverages and assistances
        Map<String, BigDecimal> complexCoverages = new HashMap<>();
        complexCoverages.put("COLLISION", new BigDecimal("25000.00"));
        complexCoverages.put("COMPREHENSIVE", new BigDecimal("30000.00"));
        complexCoverages.put("LIABILITY", new BigDecimal("15000.00"));
        policyRequest.setCoverages(complexCoverages);

        List<String> multipleAssistances = Arrays.asList(
            "24h Assistance", "Towing Service", "Emergency Repair"
        );
        policyRequest.setAssistances(multipleAssistances);

        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        cancelPolicyRequestUseCase.execute(testPolicyId, cancellationReason);

        // Assert
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestCancelled(policyRequest);
    }

    @Test
    void testExecuteWithDifferentInsuranceCategories() {
        // Test with different insurance categories
        InsuranceCategory[] categories = {
            InsuranceCategory.AUTO, InsuranceCategory.VIDA, 
            InsuranceCategory.RESIDENCIAL, InsuranceCategory.EMPRESARIAL
        };
        
        for (InsuranceCategory category : categories) {
            // Reset policy to RECEIVED status for each test (can be cancelled)
            policyRequest.setStatus(PolicyRequestStatus.RECEIVED);
            policyRequest.setCategory(category);
            when(policyRequestRepository.findById(testPolicyId))
                    .thenReturn(Optional.of(policyRequest));

            cancelPolicyRequestUseCase.execute(testPolicyId, cancellationReason);
            
            // Reset for next iteration
            reset(policyRequestRepository, eventPublisher);
        }
        
        // Assert - Each category should work with valid transition
    }

    @Test
    void testExecuteTransactionalBehavior() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));
        when(policyRequestRepository.save(any()))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert - Should propagate the exception (transactional rollback)
        assertThrows(
            RuntimeException.class,
            () -> cancelPolicyRequestUseCase.execute(testPolicyId, cancellationReason)
        );

        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, never()).publishPolicyRequestCancelled(any());
    }
}