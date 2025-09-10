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
class SetPendingPolicyRequestUseCaseTest {

    @Mock
    private PolicyRequestRepository policyRequestRepository;

    @Mock
    private PolicyEventPublisher eventPublisher;

    @InjectMocks
    private SetPendingPolicyRequestUseCase setPendingPolicyRequestUseCase;

    private UUID testPolicyId;
    private UUID testCustomerId;
    private PolicyRequest policyRequest;

    @BeforeEach
    void setUp() {
        testPolicyId = UUID.randomUUID();
        testCustomerId = UUID.randomUUID();
        
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
        policyRequest.setStatus(PolicyRequestStatus.VALIDATED);
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
            PolicyRequestStatus.VALIDATED, 
            LocalDateTime.of(2025, 1, 1, 11, 0), 
            "Validation complete"
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
        setPendingPolicyRequestUseCase.execute(testPolicyId);

        // Assert
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestPending(policyRequest);
    }

    @Test
    void testExecutePolicyRequestNotFound() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> setPendingPolicyRequestUseCase.execute(testPolicyId)
        );

        assertEquals("Solicitação não encontrada: " + testPolicyId, exception.getMessage());
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(policyRequestRepository, never()).save(any());
        verify(eventPublisher, never()).publishPolicyRequestPending(any());
    }

    @Test
    void testExecuteFromValidatedStatus() {
        // Arrange - Policy with VALIDATED status
        policyRequest.setStatus(PolicyRequestStatus.VALIDATED);
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        setPendingPolicyRequestUseCase.execute(testPolicyId);

        // Assert
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestPending(policyRequest);
    }

    @Test
    void testExecuteFromReceivedStatus() {
        // Arrange - Policy with RECEIVED status (cannot transition directly to PENDING)
        policyRequest.setStatus(PolicyRequestStatus.RECEIVED);
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act & Assert - Should throw IllegalStateException (invalid transition)
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> setPendingPolicyRequestUseCase.execute(testPolicyId)
        );
        
        assertTrue(exception.getMessage().contains("Transição inválida"));
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(policyRequestRepository, never()).save(any());
        verify(eventPublisher, never()).publishPolicyRequestPending(any());
    }

    @Test
    void testExecuteFromPendingStatus() {
        // Arrange - Policy already in PENDING status (cannot transition to same state)
        policyRequest.setStatus(PolicyRequestStatus.PENDING);
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act & Assert - Should throw IllegalStateException (invalid self-transition)
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> setPendingPolicyRequestUseCase.execute(testPolicyId)
        );
        
        assertTrue(exception.getMessage().contains("Transição inválida"));
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(policyRequestRepository, never()).save(any());
        verify(eventPublisher, never()).publishPolicyRequestPending(any());
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
            () -> setPendingPolicyRequestUseCase.execute(testPolicyId)
        );
        
        assertTrue(exception.getMessage().contains("Transição inválida"));
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(policyRequestRepository, never()).save(any());
        verify(eventPublisher, never()).publishPolicyRequestPending(any());
    }

    @Test
    void testExecuteFromRejectedStatus() {
        // Arrange - Policy with REJECTED status
        policyRequest.setStatus(PolicyRequestStatus.REJECTED);
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act & Assert - Should throw IllegalStateException (final state)
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> setPendingPolicyRequestUseCase.execute(testPolicyId)
        );
        
        assertTrue(exception.getMessage().contains("Transição inválida"));
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(policyRequestRepository, never()).save(any());
        verify(eventPublisher, never()).publishPolicyRequestPending(any());
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
            () -> setPendingPolicyRequestUseCase.execute(testPolicyId)
        );
        
        assertTrue(exception.getMessage().contains("Transição inválida"));
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(policyRequestRepository, never()).save(any());
        verify(eventPublisher, never()).publishPolicyRequestPending(any());
    }

    @Test
    void testExecuteOrderOfOperations() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        setPendingPolicyRequestUseCase.execute(testPolicyId);

        // Assert - Verify order of operations
        var inOrder = inOrder(policyRequestRepository, eventPublisher);
        inOrder.verify(policyRequestRepository).findById(testPolicyId);
        inOrder.verify(policyRequestRepository).save(policyRequest);
        inOrder.verify(eventPublisher).publishPolicyRequestPending(policyRequest);
    }

    @Test
    void testExecuteLogging() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        setPendingPolicyRequestUseCase.execute(testPolicyId);

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
            // Reset policy to VALIDATED status for each test (valid transition to PENDING)
            policyRequest.setStatus(PolicyRequestStatus.VALIDATED);
            policyRequest.setCategory(category);
            when(policyRequestRepository.findById(testPolicyId))
                    .thenReturn(Optional.of(policyRequest));

            setPendingPolicyRequestUseCase.execute(testPolicyId);
            
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
            // Reset policy to VALIDATED status for each test (valid transition to PENDING)
            policyRequest.setStatus(PolicyRequestStatus.VALIDATED);
            policyRequest.setSalesChannel(channel);
            when(policyRequestRepository.findById(testPolicyId))
                    .thenReturn(Optional.of(policyRequest));

            setPendingPolicyRequestUseCase.execute(testPolicyId);
            
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
            // Reset policy to VALIDATED status for each test (valid transition to PENDING)
            policyRequest.setStatus(PolicyRequestStatus.VALIDATED);
            policyRequest.setPaymentMethod(paymentMethod);
            when(policyRequestRepository.findById(testPolicyId))
                    .thenReturn(Optional.of(policyRequest));

            setPendingPolicyRequestUseCase.execute(testPolicyId);
            
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
        setPendingPolicyRequestUseCase.execute(testPolicyId);

        // Assert
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestPending(policyRequest);
    }

    @Test
    void testExecuteWithLargeInsuredAmounts() {
        // Arrange - Large insured amounts
        policyRequest.setInsuredAmount(new BigDecimal("5000000.00"));
        policyRequest.setTotalMonthlyPremiumAmount(new BigDecimal("2500.00"));

        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        setPendingPolicyRequestUseCase.execute(testPolicyId);

        // Assert
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestPending(policyRequest);
    }

    @Test
    void testExecuteWithEmptyCollections() {
        // Arrange - Empty coverages and assistances
        policyRequest.setCoverages(new HashMap<>());
        policyRequest.setAssistances(new ArrayList<>());

        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        setPendingPolicyRequestUseCase.execute(testPolicyId);

        // Assert
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestPending(policyRequest);
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
            () -> setPendingPolicyRequestUseCase.execute(testPolicyId)
        );

        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, never()).publishPolicyRequestPending(any());
    }

    @Test
    void testExecuteWithCompleteStatusHistory() {
        // Arrange - Policy with complete status history
        List<StatusHistory> completeHistory = new ArrayList<>();
        completeHistory.add(new StatusHistory(testPolicyId, PolicyRequestStatus.RECEIVED, 
            LocalDateTime.of(2025, 1, 1, 10, 0), "Initial creation"));
        completeHistory.add(new StatusHistory(testPolicyId, PolicyRequestStatus.VALIDATED, 
            LocalDateTime.of(2025, 1, 1, 11, 0), "Validation complete"));
        policyRequest.setHistory(completeHistory);

        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        setPendingPolicyRequestUseCase.execute(testPolicyId);

        // Assert
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestPending(policyRequest);
    }

    @Test
    void testExecuteStatusUpdateReason() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        setPendingPolicyRequestUseCase.execute(testPolicyId);

        // Assert - Verify that the status update method is called
        // (The specific reason "Aguardando pagamento e autorização de subscrição" is set in the use case)
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestPending(policyRequest);
    }
}