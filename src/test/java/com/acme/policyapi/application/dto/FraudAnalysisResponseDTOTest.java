package com.acme.policyapi.application.dto;

import com.acme.policyapi.domain.entity.CustomerRiskClassification;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para FraudAnalysisResponseDTO.
 */
class FraudAnalysisResponseDTOTest {

    @Test
    void testGettersAndSetters() {
        FraudAnalysisResponseDTO dto = new FraudAnalysisResponseDTO();
        
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LocalDateTime analyzedAt = LocalDateTime.now();
        CustomerRiskClassification classification = CustomerRiskClassification.REGULAR;
        List<OccurrenceDTO> occurrences = createOccurrenceList();

        dto.setOrderId(orderId);
        dto.setCustomerId(customerId);
        dto.setAnalyzedAt(analyzedAt);
        dto.setClassification(classification);
        dto.setOccurrences(occurrences);

        assertEquals(orderId, dto.getOrderId());
        assertEquals(customerId, dto.getCustomerId());
        assertEquals(analyzedAt, dto.getAnalyzedAt());
        assertEquals(classification, dto.getClassification());
        assertEquals(occurrences, dto.getOccurrences());
    }

    @Test
    void testAllClassifications() {
        FraudAnalysisResponseDTO dto = new FraudAnalysisResponseDTO();
        
        for (CustomerRiskClassification classification : CustomerRiskClassification.values()) {
            dto.setClassification(classification);
            assertEquals(classification, dto.getClassification());
        }
    }

    @Test
    void testNullValues() {
        FraudAnalysisResponseDTO dto = new FraudAnalysisResponseDTO();
        
        assertNull(dto.getOrderId());
        assertNull(dto.getCustomerId());
        assertNull(dto.getAnalyzedAt());
        assertNull(dto.getClassification());
        assertNull(dto.getOccurrences());
    }

    @Test
    void testEmptyOccurrences() {
        FraudAnalysisResponseDTO dto = new FraudAnalysisResponseDTO();
        List<OccurrenceDTO> emptyOccurrences = new ArrayList<>();
        
        dto.setOccurrences(emptyOccurrences);
        
        assertEquals(emptyOccurrences, dto.getOccurrences());
        assertTrue(dto.getOccurrences().isEmpty());
    }

    @Test
    void testOccurrenceDTOGettersAndSetters() {
        OccurrenceDTO occurrenceDTO = new OccurrenceDTO();
        
        String id = "OCC001";
        Long productId = 12345L;
        String type = "SUSPICIOUS_BEHAVIOR";
        String description = "Multiple attempts in short time";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now().plusHours(1);

        occurrenceDTO.setId(id);
        occurrenceDTO.setProductId(productId);
        occurrenceDTO.setType(type);
        occurrenceDTO.setDescription(description);
        occurrenceDTO.setCreatedAt(createdAt);
        occurrenceDTO.setUpdatedAt(updatedAt);

        assertEquals(id, occurrenceDTO.getId());
        assertEquals(productId, occurrenceDTO.getProductId());
        assertEquals(type, occurrenceDTO.getType());
        assertEquals(description, occurrenceDTO.getDescription());
        assertEquals(createdAt, occurrenceDTO.getCreatedAt());
        assertEquals(updatedAt, occurrenceDTO.getUpdatedAt());
    }

    @Test
    void testOccurrenceDTONullValues() {
        OccurrenceDTO occurrenceDTO = new OccurrenceDTO();
        
        assertNull(occurrenceDTO.getId());
        assertNull(occurrenceDTO.getProductId());
        assertNull(occurrenceDTO.getType());
        assertNull(occurrenceDTO.getDescription());
        assertNull(occurrenceDTO.getCreatedAt());
        assertNull(occurrenceDTO.getUpdatedAt());
    }

    @Test
    void testComplexOccurrencesList() {
        FraudAnalysisResponseDTO dto = new FraudAnalysisResponseDTO();
        
        List<OccurrenceDTO> occurrences = Arrays.asList(
            createOccurrenceDTO("OCC001", 1L, "FRAUD_ATTEMPT", "Multiple login attempts"),
            createOccurrenceDTO("OCC002", 2L, "SUSPICIOUS_PATTERN", "Unusual behavior detected"),
            createOccurrenceDTO("OCC003", 3L, "HIGH_RISK_PROFILE", "Customer in blacklist")
        );
        
        dto.setOccurrences(occurrences);
        
        assertEquals(3, dto.getOccurrences().size());
        assertEquals("OCC001", dto.getOccurrences().get(0).getId());
        assertEquals("FRAUD_ATTEMPT", dto.getOccurrences().get(0).getType());
        assertEquals("OCC002", dto.getOccurrences().get(1).getId());
        assertEquals("SUSPICIOUS_PATTERN", dto.getOccurrences().get(1).getType());
        assertEquals("OCC003", dto.getOccurrences().get(2).getId());
        assertEquals("HIGH_RISK_PROFILE", dto.getOccurrences().get(2).getType());
    }

    @Test
    void testDifferentProductIds() {
        OccurrenceDTO occurrence = new OccurrenceDTO();
        
        // Teste valores diferentes de productId
        Long[] productIds = {0L, 1L, 999L, 12345L, Long.MAX_VALUE};
        
        for (Long productId : productIds) {
            occurrence.setProductId(productId);
            assertEquals(productId, occurrence.getProductId());
        }
    }

    @Test
    void testDifferentOccurrenceTypes() {
        OccurrenceDTO occurrence = new OccurrenceDTO();
        
        String[] types = {
            "FRAUD_ATTEMPT",
            "SUSPICIOUS_BEHAVIOR", 
            "IDENTITY_THEFT",
            "DOCUMENT_FRAUD",
            "PAYMENT_FRAUD",
            "ACCOUNT_TAKEOVER"
        };
        
        for (String type : types) {
            occurrence.setType(type);
            assertEquals(type, occurrence.getType());
        }
    }

    @Test
    void testTimeSequence() {
        FraudAnalysisResponseDTO dto = new FraudAnalysisResponseDTO();
        LocalDateTime baseTime = LocalDateTime.of(2023, 1, 1, 10, 0, 0);
        
        dto.setAnalyzedAt(baseTime);
        assertEquals(baseTime, dto.getAnalyzedAt());
        
        OccurrenceDTO occurrence = new OccurrenceDTO();
        occurrence.setCreatedAt(baseTime.minusHours(1));
        occurrence.setUpdatedAt(baseTime);
        
        assertTrue(occurrence.getCreatedAt().isBefore(occurrence.getUpdatedAt()));
        assertEquals(baseTime, occurrence.getUpdatedAt());
    }

    @Test
    void testClassificationWithOccurrences() {
        FraudAnalysisResponseDTO dto = new FraudAnalysisResponseDTO();
        
        // Teste HIGH_RISK com múltiplas ocorrências
        dto.setClassification(CustomerRiskClassification.HIGH_RISK);
        dto.setOccurrences(Arrays.asList(
            createOccurrenceDTO("HIGH001", 1L, "FRAUD_DETECTED", "Definitive fraud"),
            createOccurrenceDTO("HIGH002", 2L, "BLACKLIST", "Customer blacklisted")
        ));
        
        assertEquals(CustomerRiskClassification.HIGH_RISK, dto.getClassification());
        assertEquals(2, dto.getOccurrences().size());
        
        // Teste REGULAR com poucas ou nenhuma ocorrência
        dto.setClassification(CustomerRiskClassification.REGULAR);
        dto.setOccurrences(Collections.emptyList());
        
        assertEquals(CustomerRiskClassification.REGULAR, dto.getClassification());
        assertTrue(dto.getOccurrences().isEmpty());
        
        // Teste PREFERENTIAL com nenhuma ocorrência
        dto.setClassification(CustomerRiskClassification.PREFERENTIAL);
        dto.setOccurrences(null);
        
        assertEquals(CustomerRiskClassification.PREFERENTIAL, dto.getClassification());
        assertNull(dto.getOccurrences());
    }

    @Test
    void testEdgeCaseValues() {
        OccurrenceDTO occurrence = new OccurrenceDTO();
        
        // Teste strings vazias
        occurrence.setId("");
        occurrence.setType("");
        occurrence.setDescription("");
        
        assertEquals("", occurrence.getId());
        assertEquals("", occurrence.getType());
        assertEquals("", occurrence.getDescription());
        
        // Teste productId com valor 0
        occurrence.setProductId(0L);
        assertEquals(Long.valueOf(0L), occurrence.getProductId());
        
        // Teste com valores extremos de data
        LocalDateTime minTime = LocalDateTime.MIN;
        LocalDateTime maxTime = LocalDateTime.MAX;
        
        occurrence.setCreatedAt(minTime);
        occurrence.setUpdatedAt(maxTime);
        
        assertEquals(minTime, occurrence.getCreatedAt());
        assertEquals(maxTime, occurrence.getUpdatedAt());
    }

    private FraudAnalysisResponseDTO createTestDTO() {
        FraudAnalysisResponseDTO dto = new FraudAnalysisResponseDTO();
        dto.setOrderId(UUID.fromString("12345678-1234-1234-1234-123456789abc"));
        dto.setCustomerId(UUID.fromString("87654321-4321-4321-4321-cba987654321"));
        dto.setAnalyzedAt(LocalDateTime.of(2023, 1, 1, 14, 30, 0));
        dto.setClassification(CustomerRiskClassification.REGULAR);
        dto.setOccurrences(createOccurrenceList());
        return dto;
    }

    private List<OccurrenceDTO> createOccurrenceList() {
        return List.of(createOccurrenceDTO());
    }

    private OccurrenceDTO createOccurrenceDTO() {
        return createOccurrenceDTO("OCC001", 12345L, "TEST_TYPE", "Test description");
    }

    private OccurrenceDTO createOccurrenceDTO(String id, Long productId, String type, String description) {
        OccurrenceDTO occurrence = new OccurrenceDTO();
        occurrence.setId(id);
        occurrence.setProductId(productId);
        occurrence.setType(type);
        occurrence.setDescription(description);
        occurrence.setCreatedAt(LocalDateTime.of(2023, 1, 1, 10, 0, 0));
        occurrence.setUpdatedAt(LocalDateTime.of(2023, 1, 1, 11, 0, 0));
        return occurrence;
    }
}