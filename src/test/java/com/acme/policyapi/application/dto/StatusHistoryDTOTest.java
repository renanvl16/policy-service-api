package com.acme.policyapi.application.dto;

import com.acme.policyapi.domain.entity.PolicyRequestStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para StatusHistoryDTO.
 */
class StatusHistoryDTOTest {

    @Test
    void testGettersAndSetters() {
        // Arrange
        StatusHistoryDTO dto = new StatusHistoryDTO();
        PolicyRequestStatus status = PolicyRequestStatus.APPROVED;
        LocalDateTime timestamp = LocalDateTime.now();
        String reason = "Aprovado automaticamente";

        // Act
        dto.setStatus(status);
        dto.setTimestamp(timestamp);
        dto.setReason(reason);

        // Assert
        assertEquals(status, dto.getStatus());
        assertEquals(timestamp, dto.getTimestamp());
        assertEquals(reason, dto.getReason());
    }

    @Test
    void testNullValues() {
        // Arrange
        StatusHistoryDTO dto = new StatusHistoryDTO();

        // Assert - Todos os campos podem ser nulos
        assertNull(dto.getStatus());
        assertNull(dto.getTimestamp());
        assertNull(dto.getReason());
    }

    @Test
    void testAllPolicyRequestStatuses() {
        // Arrange
        StatusHistoryDTO dto = new StatusHistoryDTO();

        // Act & Assert - Teste todos os status possíveis
        for (PolicyRequestStatus status : PolicyRequestStatus.values()) {
            dto.setStatus(status);
            assertEquals(status, dto.getStatus());
        }
    }

    @Test
    void testNullReason() {
        // Arrange
        StatusHistoryDTO dto = new StatusHistoryDTO();
        dto.setStatus(PolicyRequestStatus.CANCELLED);
        dto.setTimestamp(LocalDateTime.now());
        dto.setReason(null);

        // Assert
        assertEquals(PolicyRequestStatus.CANCELLED, dto.getStatus());
        assertNotNull(dto.getTimestamp());
        assertNull(dto.getReason());
    }

    @Test
    void testTimestampPrecision() {
        // Arrange
        StatusHistoryDTO dto = new StatusHistoryDTO();
        LocalDateTime timestamp = LocalDateTime.of(2023, 12, 25, 14, 30, 45, 123456789);

        // Act
        dto.setTimestamp(timestamp);

        // Assert
        assertEquals(timestamp, dto.getTimestamp());
        assertEquals(123456789, dto.getTimestamp().getNano());
    }

    @Test
    void testReasonVariations() {
        // Arrange
        StatusHistoryDTO dto = new StatusHistoryDTO();
        String[] reasons = {
            "Aprovado automaticamente",
            "Rejeitado por análise de fraude",
            "Cancelado pelo cliente",
            "Pendente de documentação",
            "",
            "   ",
            null
        };

        // Act & Assert
        for (String reason : reasons) {
            dto.setReason(reason);
            assertEquals(reason, dto.getReason());
        }
    }

    @Test
    void testStatusTransitionHistory() {
        // Arrange - Simula um histórico de transições
        StatusHistoryDTO[] history = {
            createStatusHistoryDTO(PolicyRequestStatus.RECEIVED, "Solicitação recebida"),
            createStatusHistoryDTO(PolicyRequestStatus.VALIDATED, "Validação concluída"),
            createStatusHistoryDTO(PolicyRequestStatus.PENDING, "Aguardando documentos"),
            createStatusHistoryDTO(PolicyRequestStatus.APPROVED, "Aprovado")
        };

        // Assert
        assertEquals(4, history.length);
        assertEquals(PolicyRequestStatus.RECEIVED, history[0].getStatus());
        assertEquals(PolicyRequestStatus.VALIDATED, history[1].getStatus());
        assertEquals(PolicyRequestStatus.PENDING, history[2].getStatus());
        assertEquals(PolicyRequestStatus.APPROVED, history[3].getStatus());
    }

    private StatusHistoryDTO createStatusHistoryDTO() {
        StatusHistoryDTO dto = new StatusHistoryDTO();
        dto.setStatus(PolicyRequestStatus.VALIDATED);
        dto.setTimestamp(LocalDateTime.now());
        dto.setReason("Test reason");
        return dto;
    }

    private StatusHistoryDTO createStatusHistoryDTO(PolicyRequestStatus status, String reason) {
        StatusHistoryDTO dto = new StatusHistoryDTO();
        dto.setStatus(status);
        dto.setTimestamp(LocalDateTime.now());
        dto.setReason(reason);
        return dto;
    }
}
