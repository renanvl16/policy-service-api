package com.acme.policyapi.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a entidade PolicyRequest.
 * 
 * @author Sistema ACME
 */
class PolicyRequestTest {

    private PolicyRequest policyRequest;
    private UUID requestId;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        
        policyRequest = new PolicyRequest();
        policyRequest.setId(requestId);
        policyRequest.setCustomerId(customerId);
        policyRequest.setProductId("PROD-123");
        policyRequest.setCategory(InsuranceCategory.AUTO);
        policyRequest.setSalesChannel(SalesChannel.MOBILE);
        policyRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        policyRequest.setStatus(PolicyRequestStatus.RECEIVED);
        policyRequest.setCreatedAt(LocalDateTime.now());
        policyRequest.setTotalMonthlyPremiumAmount(new BigDecimal("150.00"));
        policyRequest.setInsuredAmount(new BigDecimal("250000.00"));
        policyRequest.setCoverages(Map.of("Roubo", new BigDecimal("100000.00")));
        policyRequest.setAssistances(List.of("Guincho 24h"));
    }

    @Test
    void testUpdateStatusFromReceivedToValidated() {
        // Arrange
        PolicyRequestStatus newStatus = PolicyRequestStatus.VALIDATED;
        String reason = "Aprovado na análise de fraudes";

        // Act
        policyRequest.updateStatus(newStatus, reason);

        // Assert
        assertEquals(newStatus, policyRequest.getStatus());
        assertNull(policyRequest.getFinishedAt());
        assertEquals(1, policyRequest.getHistory().size());
        
        StatusHistory history = policyRequest.getHistory().get(0);
        assertEquals(requestId, history.getPolicyRequestId());
        assertEquals(newStatus, history.getStatus());
        assertEquals(reason, history.getReason());
        assertNotNull(history.getTimestamp());
    }

    @Test
    void testUpdateStatusToFinalState() {
        // Arrange
        policyRequest.setStatus(PolicyRequestStatus.PENDING);
        PolicyRequestStatus finalStatus = PolicyRequestStatus.APPROVED;

        // Act
        policyRequest.updateStatus(finalStatus, "Pagamento confirmado");

        // Assert
        assertEquals(finalStatus, policyRequest.getStatus());
        assertNotNull(policyRequest.getFinishedAt());
        assertTrue(policyRequest.isApproved());
    }

    @Test
    void testInvalidTransition() {
        // Arrange
        PolicyRequestStatus invalidStatus = PolicyRequestStatus.APPROVED;

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> policyRequest.updateStatus(invalidStatus, "Invalid transition")
        );

        assertTrue(exception.getMessage().contains("Transição inválida"));
        assertEquals(PolicyRequestStatus.RECEIVED, policyRequest.getStatus());
    }

    @Test
    void testCanBeCancelledWhenNotFinal() {
        // Arrange
        policyRequest.setStatus(PolicyRequestStatus.PENDING);

        // Act & Assert
        assertTrue(policyRequest.canBeCancelled());
    }

    @Test
    void testCannotBeCancelledWhenApproved() {
        // Arrange
        policyRequest.setStatus(PolicyRequestStatus.APPROVED);

        // Act & Assert
        assertFalse(policyRequest.canBeCancelled());
    }

    @Test
    void testIsApprovedWhenStatusIsApproved() {
        // Arrange
        policyRequest.setStatus(PolicyRequestStatus.APPROVED);

        // Act & Assert
        assertTrue(policyRequest.isApproved());
    }

    @Test
    void testIsNotApprovedWhenStatusIsNotApproved() {
        // Arrange
        policyRequest.setStatus(PolicyRequestStatus.PENDING);

        // Act & Assert
        assertFalse(policyRequest.isApproved());
    }

    @Test
    void testPrePersist() {
        // Arrange
        PolicyRequest newRequest = new PolicyRequest();
        newRequest.setCustomerId(UUID.randomUUID());

        // Act
        newRequest.prePersist();

        // Assert
        assertNotNull(newRequest.getCreatedAt());
        assertEquals(PolicyRequestStatus.RECEIVED, newRequest.getStatus());
    }

    @Test
    void testPrePersistDoesNotOverrideExistingValues() {
        // Arrange
        LocalDateTime existingDate = LocalDateTime.now().minusDays(1);
        policyRequest.setCreatedAt(existingDate);
        policyRequest.setStatus(PolicyRequestStatus.VALIDATED);

        // Act
        policyRequest.prePersist();

        // Assert
        assertEquals(existingDate, policyRequest.getCreatedAt());
        assertEquals(PolicyRequestStatus.VALIDATED, policyRequest.getStatus());
    }
}