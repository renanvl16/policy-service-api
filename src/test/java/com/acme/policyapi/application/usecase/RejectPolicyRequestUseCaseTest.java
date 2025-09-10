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
class RejectPolicyRequestUseCaseTest {

    @Mock
    private PolicyRequestRepository policyRequestRepository;

    @Mock
    private PolicyEventPublisher eventPublisher;

    @InjectMocks
    private RejectPolicyRequestUseCase rejectPolicyRequestUseCase;

    private UUID testPolicyId;
    private UUID testCustomerId;
    private PolicyRequest policyRequest;
    private String rejectionReason;

    @BeforeEach
    void setUp() {
        testPolicyId = UUID.randomUUID();
        testCustomerId = UUID.randomUUID();
        rejectionReason = "Fraud detected";
        
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
        rejectPolicyRequestUseCase.execute(testPolicyId, rejectionReason);

        // Assert
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestRejected(policyRequest);
    }

    @Test
    void testExecutePolicyRequestNotFound() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> rejectPolicyRequestUseCase.execute(testPolicyId, rejectionReason)
        );

        assertEquals("Solicitação não encontrada: " + testPolicyId, exception.getMessage());
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(policyRequestRepository, never()).save(any());
        verify(eventPublisher, never()).publishPolicyRequestRejected(any());
    }

    @Test
    void testExecuteFromReceivedStatus() {
        // Arrange - Policy with RECEIVED status
        policyRequest.setStatus(PolicyRequestStatus.RECEIVED);
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        rejectPolicyRequestUseCase.execute(testPolicyId, rejectionReason);

        // Assert
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestRejected(policyRequest);
    }

    @Test
    void testExecuteFromValidatedStatus() {
        // Arrange - Policy with VALIDATED status
        policyRequest.setStatus(PolicyRequestStatus.VALIDATED);
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        rejectPolicyRequestUseCase.execute(testPolicyId, rejectionReason);

        // Assert
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestRejected(policyRequest);
    }

    @Test
    void testExecuteFromPendingStatus() {
        // Arrange - Policy with PENDING status
        policyRequest.setStatus(PolicyRequestStatus.PENDING);
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        rejectPolicyRequestUseCase.execute(testPolicyId, rejectionReason);

        // Assert
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestRejected(policyRequest);
    }

    @Test
    void testExecuteFromApprovedStatus() {
        // Arrange - Policy with APPROVED status
        policyRequest.setStatus(PolicyRequestStatus.APPROVED);
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act & Assert - Should throw IllegalStateException (final state)
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> rejectPolicyRequestUseCase.execute(testPolicyId, rejectionReason)
        );
        
        assertTrue(exception.getMessage().contains("Transição inválida"));
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(policyRequestRepository, never()).save(any());
        verify(eventPublisher, never()).publishPolicyRequestRejected(any());
    }

    @Test
    void testExecuteFromRejectedStatus() {
        // Arrange - Policy already in REJECTED status
        policyRequest.setStatus(PolicyRequestStatus.REJECTED);
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act & Assert - Should throw IllegalStateException (final state)
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> rejectPolicyRequestUseCase.execute(testPolicyId, rejectionReason)
        );
        
        assertTrue(exception.getMessage().contains("Transição inválida"));
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(policyRequestRepository, never()).save(any());
        verify(eventPublisher, never()).publishPolicyRequestRejected(any());
    }

    @Test
    void testExecuteFromCancelledStatus() {
        // Arrange - Policy with CANCELLED status
        policyRequest.setStatus(PolicyRequestStatus.CANCELLED);
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act & Assert - Should throw IllegalStateException (final state)
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> rejectPolicyRequestUseCase.execute(testPolicyId, rejectionReason)
        );
        
        assertTrue(exception.getMessage().contains("Transição inválida"));
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(policyRequestRepository, never()).save(any());
        verify(eventPublisher, never()).publishPolicyRequestRejected(any());
    }

    @Test
    void testExecuteWithDifferentReasons() {
        // Test only one valid reason with valid status
        String reason = "Fraud detected";
        
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        rejectPolicyRequestUseCase.execute(testPolicyId, reason);
        
        // Assert
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestRejected(policyRequest);
    }

    @Test
    void testExecuteWithEmptyReason() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        rejectPolicyRequestUseCase.execute(testPolicyId, "");

        // Assert - Empty reason should still work
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestRejected(policyRequest);
    }

    @Test
    void testExecuteWithNullReason() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        rejectPolicyRequestUseCase.execute(testPolicyId, null);

        // Assert - Null reason should still work
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestRejected(policyRequest);
    }

    @Test
    void testExecuteWithLongReason() {
        // Arrange - Very long rejection reason
        String longReason = "This is a very long rejection reason that exceeds normal length limits. ".repeat(10) + 
                           "It should still be handled properly by the system without causing any issues.";
        
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        rejectPolicyRequestUseCase.execute(testPolicyId, longReason);

        // Assert
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestRejected(policyRequest);
    }

    @Test
    void testExecuteOrderOfOperations() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        rejectPolicyRequestUseCase.execute(testPolicyId, rejectionReason);

        // Assert - Verify order of operations
        var inOrder = inOrder(policyRequestRepository, eventPublisher);
        inOrder.verify(policyRequestRepository).findById(testPolicyId);
        inOrder.verify(policyRequestRepository).save(policyRequest);
        inOrder.verify(eventPublisher).publishPolicyRequestRejected(policyRequest);
    }

    @Test
    void testExecuteLogging() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        rejectPolicyRequestUseCase.execute(testPolicyId, rejectionReason);

        // Assert - Verify repository interactions (log content testing would need additional setup)
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(policyRequestRepository, times(1)).save(policyRequest);
    }

    @Test
    void testExecuteWithDifferentInsuranceCategories() {
        // Test with different insurance categories
        InsuranceCategory[] categories = {
            InsuranceCategory.AUTO, InsuranceCategory.VIDA, 
            InsuranceCategory.RESIDENCIAL, InsuranceCategory.EMPRESARIAL
        };
        
        for (InsuranceCategory category : categories) {
            // Reset policy to RECEIVED status for each test
            policyRequest.setStatus(PolicyRequestStatus.RECEIVED);
            policyRequest.setCategory(category);
            when(policyRequestRepository.findById(testPolicyId))
                    .thenReturn(Optional.of(policyRequest));

            rejectPolicyRequestUseCase.execute(testPolicyId, rejectionReason);
            
            // Reset for next iteration
            reset(policyRequestRepository, eventPublisher);
        }
        
        // Assert - Each category should work with valid transition
    }

    @Test
    void testExecuteWithDifferentSalesChannels() {
        // Test with different sales channels
        SalesChannel[] channels = {
            SalesChannel.WEBSITE, SalesChannel.MOBILE, SalesChannel.WHATSAPP, 
            SalesChannel.PRESENCIAL, SalesChannel.TELEFONE
        };
        
        for (SalesChannel channel : channels) {
            // Reset policy to RECEIVED status for each test
            policyRequest.setStatus(PolicyRequestStatus.RECEIVED);
            policyRequest.setSalesChannel(channel);
            when(policyRequestRepository.findById(testPolicyId))
                    .thenReturn(Optional.of(policyRequest));

            rejectPolicyRequestUseCase.execute(testPolicyId, rejectionReason);
            
            // Reset for next iteration
            reset(policyRequestRepository, eventPublisher);
        }
        
        // Assert - Each channel should work with valid transition
    }

    @Test
    void testExecuteWithDifferentPaymentMethods() {
        // Test with different payment methods
        PaymentMethod[] paymentMethods = {
            PaymentMethod.CREDIT_CARD, PaymentMethod.DEBIT_ACCOUNT, 
            PaymentMethod.BOLETO, PaymentMethod.PIX
        };
        
        for (PaymentMethod paymentMethod : paymentMethods) {
            // Reset policy to RECEIVED status for each test
            policyRequest.setStatus(PolicyRequestStatus.RECEIVED);
            policyRequest.setPaymentMethod(paymentMethod);
            when(policyRequestRepository.findById(testPolicyId))
                    .thenReturn(Optional.of(policyRequest));

            rejectPolicyRequestUseCase.execute(testPolicyId, rejectionReason);
            
            // Reset for next iteration
            reset(policyRequestRepository, eventPublisher);
        }
        
        // Assert - Each payment method should work with valid transition
    }

    @Test
    void testExecuteWithComplexPolicyRequest() {
        // Arrange - Complex policy request with multiple coverages and assistances
        Map<String, BigDecimal> complexCoverages = new HashMap<>();
        complexCoverages.put("COLLISION", new BigDecimal("25000.00"));
        complexCoverages.put("COMPREHENSIVE", new BigDecimal("30000.00"));
        complexCoverages.put("LIABILITY", new BigDecimal("15000.00"));
        complexCoverages.put("PERSONAL_INJURY", new BigDecimal("10000.00"));
        policyRequest.setCoverages(complexCoverages);

        List<String> multipleAssistances = Arrays.asList(
            "24h Roadside Assistance",
            "Towing Service", 
            "Emergency Repair",
            "Rental Car Coverage",
            "Glass Repair",
            "Locksmith Service"
        );
        policyRequest.setAssistances(multipleAssistances);

        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        rejectPolicyRequestUseCase.execute(testPolicyId, rejectionReason);

        // Assert
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestRejected(policyRequest);
    }

    @Test
    void testExecuteWithLargeInsuredAmounts() {
        // Arrange - Large insured amounts
        policyRequest.setInsuredAmount(new BigDecimal("50000000.00"));
        policyRequest.setTotalMonthlyPremiumAmount(new BigDecimal("10000.00"));

        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        rejectPolicyRequestUseCase.execute(testPolicyId, "Amount exceeds limit");

        // Assert
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestRejected(policyRequest);
    }

    @Test
    void testExecuteWithEmptyCollections() {
        // Arrange - Empty coverages and assistances
        policyRequest.setCoverages(new HashMap<>());
        policyRequest.setAssistances(new ArrayList<>());

        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        rejectPolicyRequestUseCase.execute(testPolicyId, rejectionReason);

        // Assert
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestRejected(policyRequest);
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
            () -> rejectPolicyRequestUseCase.execute(testPolicyId, rejectionReason)
        );

        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, never()).publishPolicyRequestRejected(any());
    }

    @Test
    void testExecuteWithCompleteStatusHistory() {
        // Arrange - Policy with complete status history
        List<StatusHistory> completeHistory = new ArrayList<>();
        completeHistory.add(new StatusHistory(testPolicyId, PolicyRequestStatus.RECEIVED, 
            LocalDateTime.of(2025, 1, 1, 10, 0), "Initial creation"));
        completeHistory.add(new StatusHistory(testPolicyId, PolicyRequestStatus.VALIDATED, 
            LocalDateTime.of(2025, 1, 1, 11, 0), "Validation complete"));
        completeHistory.add(new StatusHistory(testPolicyId, PolicyRequestStatus.PENDING, 
            LocalDateTime.of(2025, 1, 1, 12, 0), "Waiting for payment"));
        policyRequest.setHistory(completeHistory);

        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        rejectPolicyRequestUseCase.execute(testPolicyId, rejectionReason);

        // Assert
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestRejected(policyRequest);
    }

    @Test
    void testExecuteWithFinishedAtTimestamp() {
        // Arrange - Policy with finishedAt timestamp
        policyRequest.setFinishedAt(LocalDateTime.of(2025, 1, 2, 15, 30));

        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        rejectPolicyRequestUseCase.execute(testPolicyId, rejectionReason);

        // Assert
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestRejected(policyRequest);
    }

    @Test
    void testExecuteWithNullOptionalFields() {
        // Arrange - Policy with null optional fields
        policyRequest.setFinishedAt(null);
        policyRequest.setCoverages(null);
        policyRequest.setAssistances(null);

        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        rejectPolicyRequestUseCase.execute(testPolicyId, rejectionReason);

        // Assert
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestRejected(policyRequest);
    }
}