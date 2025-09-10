package com.acme.policyapi.application.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para OccurrenceDTO.
 */
class OccurrenceDTOTest {

    @Test
    void testGettersAndSetters() {
        // Arrange
        OccurrenceDTO dto = new OccurrenceDTO();
        String id = "OCC001";
        Long productId = 12345L;
        String type = "FRAUD_ALERT";
        String description = "Transação suspeita detectada";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now().plusHours(1);

        // Act
        dto.setId(id);
        dto.setProductId(productId);
        dto.setType(type);
        dto.setDescription(description);
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);

        // Assert
        assertEquals(id, dto.getId());
        assertEquals(productId, dto.getProductId());
        assertEquals(type, dto.getType());
        assertEquals(description, dto.getDescription());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(updatedAt, dto.getUpdatedAt());
    }

    @Test
    void testNullValues() {
        // Arrange
        OccurrenceDTO dto = new OccurrenceDTO();

        // Assert - Todos os campos podem ser nulos
        assertNull(dto.getId());
        assertNull(dto.getProductId());
        assertNull(dto.getType());
        assertNull(dto.getDescription());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
    }

    @Test
    void testDifferentTypes() {
        // Arrange
        OccurrenceDTO dto = new OccurrenceDTO();
        String[] types = {"FRAUD_ALERT", "RISK_WARNING", "IDENTITY_VERIFICATION", "PAYMENT_ISSUE"};

        // Act & Assert
        for (String type : types) {
            dto.setType(type);
            assertEquals(type, dto.getType());
        }
    }

    @Test
    void testLongProductIds() {
        // Arrange
        OccurrenceDTO dto = new OccurrenceDTO();
        Long[] productIds = {1L, 999999L, Long.MAX_VALUE, Long.MIN_VALUE};

        // Act & Assert
        for (Long productId : productIds) {
            dto.setProductId(productId);
            assertEquals(productId, dto.getProductId());
        }
    }

    @Test
    void testTimestampOrder() {
        // Arrange
        OccurrenceDTO dto = new OccurrenceDTO();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = createdAt.plusMinutes(30);

        // Act
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);

        // Assert
        assertTrue(dto.getUpdatedAt().isAfter(dto.getCreatedAt()));
    }

    private OccurrenceDTO createOccurrenceDTO() {
        OccurrenceDTO dto = new OccurrenceDTO();
        dto.setId("TEST001");
        dto.setProductId(100L);
        dto.setType("TEST_TYPE");
        dto.setDescription("Test description");
        dto.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0, 0));
        dto.setUpdatedAt(LocalDateTime.of(2025, 1, 1, 12, 0, 0));
        return dto;
    }
}
