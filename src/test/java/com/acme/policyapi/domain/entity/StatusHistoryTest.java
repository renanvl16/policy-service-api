package com.acme.policyapi.domain.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para StatusHistory.
 */
class StatusHistoryTest {

    @Test
    void testDefaultConstructor() {
        // Act
        StatusHistory history = new StatusHistory();

        // Assert
        assertNull(history.getId());
        assertNull(history.getPolicyRequestId());
        assertNull(history.getStatus());
        assertNull(history.getTimestamp());
        assertNull(history.getReason());
    }

    @Test
    void testAllArgsConstructor() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID policyRequestId = UUID.randomUUID();
        PolicyRequestStatus status = PolicyRequestStatus.APPROVED;
        LocalDateTime timestamp = LocalDateTime.now();
        String reason = "Approved automatically";

        // Act
        StatusHistory history = new StatusHistory(id, policyRequestId, status, timestamp, reason);

        // Assert
        assertEquals(id, history.getId());
        assertEquals(policyRequestId, history.getPolicyRequestId());
        assertEquals(status, history.getStatus());
        assertEquals(timestamp, history.getTimestamp());
        assertEquals(reason, history.getReason());
    }

    @Test
    void testConstructorWithoutReason() {
        // Arrange
        UUID policyRequestId = UUID.randomUUID();
        PolicyRequestStatus status = PolicyRequestStatus.VALIDATED;
        LocalDateTime timestamp = LocalDateTime.now();

        // Act
        StatusHistory history = new StatusHistory(policyRequestId, status, timestamp);

        // Assert
        assertNull(history.getId()); // ID não definido neste construtor
        assertEquals(policyRequestId, history.getPolicyRequestId());
        assertEquals(status, history.getStatus());
        assertEquals(timestamp, history.getTimestamp());
        assertNull(history.getReason()); // Reason não definido
    }

    @Test
    void testConstructorWithReason() {
        // Arrange
        UUID policyRequestId = UUID.randomUUID();
        PolicyRequestStatus status = PolicyRequestStatus.REJECTED;
        LocalDateTime timestamp = LocalDateTime.now();
        String reason = "Risk assessment failed";

        // Act
        StatusHistory history = new StatusHistory(policyRequestId, status, timestamp, reason);

        // Assert
        assertNull(history.getId()); // ID não definido neste construtor
        assertEquals(policyRequestId, history.getPolicyRequestId());
        assertEquals(status, history.getStatus());
        assertEquals(timestamp, history.getTimestamp());
        assertEquals(reason, history.getReason());
    }

    @Test
    void testGettersAndSetters() {
        // Arrange
        StatusHistory history = new StatusHistory();
        UUID id = UUID.randomUUID();
        UUID policyRequestId = UUID.randomUUID();
        PolicyRequestStatus status = PolicyRequestStatus.PENDING;
        LocalDateTime timestamp = LocalDateTime.now();
        String reason = "Waiting for payment confirmation";

        // Act
        history.setId(id);
        history.setPolicyRequestId(policyRequestId);
        history.setStatus(status);
        history.setTimestamp(timestamp);
        history.setReason(reason);

        // Assert
        assertEquals(id, history.getId());
        assertEquals(policyRequestId, history.getPolicyRequestId());
        assertEquals(status, history.getStatus());
        assertEquals(timestamp, history.getTimestamp());
        assertEquals(reason, history.getReason());
    }

    @ParameterizedTest
    @EnumSource(PolicyRequestStatus.class)
    void testAllPolicyRequestStatuses(PolicyRequestStatus status) {
        // Arrange
        UUID policyRequestId = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.now();

        // Act
        StatusHistory history = new StatusHistory(policyRequestId, status, timestamp);

        // Assert
        assertEquals(status, history.getStatus());
        assertEquals(policyRequestId, history.getPolicyRequestId());
        assertEquals(timestamp, history.getTimestamp());
    }

    @Test
    void testNullValues() {
        // Arrange & Act
        StatusHistory history = new StatusHistory(null, null, null, null);

        // Assert
        assertNull(history.getPolicyRequestId());
        assertNull(history.getStatus());
        assertNull(history.getTimestamp());
        assertNull(history.getReason());
    }

    @Test
    void testConstructorChaining() {
        // Arrange
        UUID policyRequestId = UUID.randomUUID();
        PolicyRequestStatus status = PolicyRequestStatus.CANCELLED;
        LocalDateTime timestamp = LocalDateTime.now();
        String reason = "Customer cancelled";

        // Act
        StatusHistory history = new StatusHistory(policyRequestId, status, timestamp, reason);

        // Assert - Verifica se o construtor está encadeando corretamente
        assertEquals(policyRequestId, history.getPolicyRequestId());
        assertEquals(status, history.getStatus());
        assertEquals(timestamp, history.getTimestamp());
        assertEquals(reason, history.getReason());
    }

    @Test
    void testTimestampPrecision() {
        // Arrange
        UUID policyRequestId = UUID.randomUUID();
        LocalDateTime specificTime = LocalDateTime.of(2023, 12, 25, 14, 30, 45, 123456789);
        
        StatusHistory history = new StatusHistory(policyRequestId, PolicyRequestStatus.RECEIVED, specificTime);

        // Assert
        assertEquals(specificTime, history.getTimestamp());
        assertEquals(2023, history.getTimestamp().getYear());
        assertEquals(12, history.getTimestamp().getMonthValue());
        assertEquals(25, history.getTimestamp().getDayOfMonth());
        assertEquals(14, history.getTimestamp().getHour());
        assertEquals(30, history.getTimestamp().getMinute());
        assertEquals(45, history.getTimestamp().getSecond());
        assertEquals(123456789, history.getTimestamp().getNano());
    }

    @Test
    void testReasonLength() {
        // Arrange
        UUID policyRequestId = UUID.randomUUID();
        PolicyRequestStatus status = PolicyRequestStatus.REJECTED;
        LocalDateTime timestamp = LocalDateTime.now();
        
        // Testa razões de diferentes tamanhos
        String shortReason = "No";
        String mediumReason = "Risk assessment failed due to multiple factors";
        String longReason = "A".repeat(500); // Máximo permitido pela anotação @Column(length = 500)
        
        // Act & Assert
        StatusHistory history1 = new StatusHistory(policyRequestId, status, timestamp, shortReason);
        assertEquals(shortReason, history1.getReason());
        
        StatusHistory history2 = new StatusHistory(policyRequestId, status, timestamp, mediumReason);
        assertEquals(mediumReason, history2.getReason());
        
        StatusHistory history3 = new StatusHistory(policyRequestId, status, timestamp, longReason);
        assertEquals(longReason, history3.getReason());
        assertEquals(500, history3.getReason().length());
    }

    @Test
    void testReasonWithSpecialCharacters() {
        // Arrange
        UUID policyRequestId = UUID.randomUUID();
        String reasonWithSpecialChars = "Rejected due to: high-risk profile (85% risk), insufficient documentation & invalid payment method";
        
        StatusHistory history = new StatusHistory(policyRequestId, PolicyRequestStatus.REJECTED, LocalDateTime.now(), reasonWithSpecialChars);

        // Assert
        assertEquals(reasonWithSpecialChars, history.getReason());
    }

    @Test
    void testReasonWithUnicodeCharacters() {
        // Arrange
        UUID policyRequestId = UUID.randomUUID();
        String reasonWithUnicode = "Rejeitado devido à análise de risco: perfil não adequado às políticas da companhia";
        
        StatusHistory history = new StatusHistory(policyRequestId, PolicyRequestStatus.REJECTED, LocalDateTime.now(), reasonWithUnicode);

        // Assert
        assertEquals(reasonWithUnicode, history.getReason());
    }

    @Test
    void testEmptyReason() {
        // Arrange
        UUID policyRequestId = UUID.randomUUID();
        String emptyReason = "";
        
        StatusHistory history = new StatusHistory(policyRequestId, PolicyRequestStatus.APPROVED, LocalDateTime.now(), emptyReason);

        // Assert
        assertEquals(emptyReason, history.getReason());
        assertTrue(history.getReason().isEmpty());
    }

    @Test
    void testWhitespaceReason() {
        // Arrange
        UUID policyRequestId = UUID.randomUUID();
        String whitespaceReason = "   ";
        
        StatusHistory history = new StatusHistory(policyRequestId, PolicyRequestStatus.VALIDATED, LocalDateTime.now(), whitespaceReason);

        // Assert
        assertEquals(whitespaceReason, history.getReason());
        assertTrue(history.getReason().isBlank());
    }

    @Test
    void testMultilineReason() {
        // Arrange
        UUID policyRequestId = UUID.randomUUID();
        String multilineReason = "First line\nSecond line\nThird line";
        
        StatusHistory history = new StatusHistory(policyRequestId, PolicyRequestStatus.CANCELLED, LocalDateTime.now(), multilineReason);

        // Assert
        assertEquals(multilineReason, history.getReason());
        assertTrue(history.getReason().contains("\n"));
    }

    @Test
    void testDifferentUUIDs() {
        // Arrange
        UUID policyRequestId1 = UUID.randomUUID();
        UUID policyRequestId2 = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.now();
        
        StatusHistory history1 = new StatusHistory(policyRequestId1, PolicyRequestStatus.PENDING, timestamp);
        StatusHistory history2 = new StatusHistory(policyRequestId2, PolicyRequestStatus.PENDING, timestamp);

        // Assert
        assertNotEquals(policyRequestId1, policyRequestId2);
        assertNotEquals(history1.getPolicyRequestId(), history2.getPolicyRequestId());
    }

    @Test
    void testTimestampOrdering() {
        // Arrange
        UUID policyRequestId = UUID.randomUUID();
        LocalDateTime time1 = LocalDateTime.of(2023, 1, 1, 10, 0, 0);
        LocalDateTime time2 = LocalDateTime.of(2023, 1, 1, 11, 0, 0);
        LocalDateTime time3 = LocalDateTime.of(2023, 1, 1, 12, 0, 0);
        
        StatusHistory history1 = new StatusHistory(policyRequestId, PolicyRequestStatus.RECEIVED, time1);
        StatusHistory history2 = new StatusHistory(policyRequestId, PolicyRequestStatus.VALIDATED, time2);
        StatusHistory history3 = new StatusHistory(policyRequestId, PolicyRequestStatus.APPROVED, time3);

        // Assert
        assertTrue(history1.getTimestamp().isBefore(history2.getTimestamp()));
        assertTrue(history2.getTimestamp().isBefore(history3.getTimestamp()));
        assertTrue(history1.getTimestamp().isBefore(history3.getTimestamp()));
    }

    @Test
    void testStatusTransitionHistory() {
        // Arrange & Act - Simula um histórico completo de transições
        UUID policyRequestId = UUID.randomUUID();
        LocalDateTime baseTime = LocalDateTime.of(2023, 1, 1, 9, 0, 0);
        
        StatusHistory[] transitions = {
            new StatusHistory(policyRequestId, PolicyRequestStatus.RECEIVED, baseTime, "Initial request"),
            new StatusHistory(policyRequestId, PolicyRequestStatus.VALIDATED, baseTime.plusMinutes(30), "Fraud check passed"),
            new StatusHistory(policyRequestId, PolicyRequestStatus.PENDING, baseTime.plusHours(1), "Waiting for payment"),
            new StatusHistory(policyRequestId, PolicyRequestStatus.APPROVED, baseTime.plusHours(2), "Payment confirmed")
        };

        // Assert
        for (int i = 0; i < transitions.length; i++) {
            assertEquals(policyRequestId, transitions[i].getPolicyRequestId());
            assertNotNull(transitions[i].getStatus());
            assertNotNull(transitions[i].getTimestamp());
            assertNotNull(transitions[i].getReason());
            
            if (i > 0) {
                assertTrue(transitions[i-1].getTimestamp().isBefore(transitions[i].getTimestamp()));
            }
        }
    }

    @Test
    void testEntityAnnotations() {
        // Assert - Testa se as anotações estão presentes (reflexão simples)
        Class<StatusHistory> clazz = StatusHistory.class;
        
        // Verifica se a classe tem as anotações esperadas
        assertTrue(clazz.isAnnotationPresent(jakarta.persistence.Entity.class));
        assertTrue(clazz.isAnnotationPresent(jakarta.persistence.Table.class));
        
        // Verifica o nome da tabela
        jakarta.persistence.Table tableAnnotation = clazz.getAnnotation(jakarta.persistence.Table.class);
        assertEquals("status_history", tableAnnotation.name());
    }
}