package com.acme.policyapi.application.service.impl;

import com.acme.policyapi.application.dto.PolicyRequestCreateDTO;
import com.acme.policyapi.application.dto.PolicyRequestResponseDTO;
import com.acme.policyapi.application.dto.StatusHistoryDTO;
import com.acme.policyapi.domain.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para PolicyRequestMapper.
 */
class PolicyRequestMapperTest {

    private PolicyRequestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(PolicyRequestMapper.class);
    }

    @Test
    void testToEntityFromCreateDTO() {
        // Arrange
        PolicyRequestCreateDTO createDTO = new PolicyRequestCreateDTO();
        createDTO.setCustomerId(UUID.randomUUID());
        createDTO.setProductId("PROD123");
        createDTO.setCategory(InsuranceCategory.AUTO);
        createDTO.setTotalMonthlyPremiumAmount(new BigDecimal("150.00"));
        createDTO.setInsuredAmount(new BigDecimal("50000.00"));
        createDTO.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        createDTO.setSalesChannel(SalesChannel.MOBILE);
        createDTO.setCoverages(Map.of("BASIC", new BigDecimal("25000.00")));
        createDTO.setAssistances(List.of("24H_ASSISTANCE"));

        // Act
        PolicyRequest entity = mapper.toEntity(createDTO);

        // Assert
        assertNotNull(entity);
        assertEquals(createDTO.getCustomerId(), entity.getCustomerId());
        assertEquals(createDTO.getProductId(), entity.getProductId());
        assertEquals(createDTO.getCategory(), entity.getCategory());
        assertEquals(createDTO.getTotalMonthlyPremiumAmount(), entity.getTotalMonthlyPremiumAmount());
        assertEquals(createDTO.getInsuredAmount(), entity.getInsuredAmount());
        assertEquals(createDTO.getPaymentMethod(), entity.getPaymentMethod());
        assertEquals(createDTO.getSalesChannel(), entity.getSalesChannel());
        assertEquals(createDTO.getCoverages(), entity.getCoverages());
        assertEquals(createDTO.getAssistances(), entity.getAssistances());

        // Campos ignorados devem ser null ou ter valores padrão
        assertNull(entity.getId());
        assertEquals(PolicyRequestStatus.RECEIVED, entity.getStatus()); // Status tem valor padrão
        assertNull(entity.getCreatedAt());
        assertNull(entity.getFinishedAt());
        assertNotNull(entity.getHistory()); // History é inicializado como ArrayList vazio
        assertTrue(entity.getHistory().isEmpty()); // Deve ser uma lista vazia
    }

    @Test
    void testToEntityWithNullCreateDTO() {
        // Act & Assert
        assertNull(mapper.toEntity(null));
    }

    @Test
    void testToEntityWithMinimalCreateDTO() {
        // Arrange
        PolicyRequestCreateDTO createDTO = new PolicyRequestCreateDTO();
        createDTO.setCustomerId(UUID.randomUUID());
        createDTO.setProductId("PROD456");
        createDTO.setCategory(InsuranceCategory.VIDA);
        createDTO.setTotalMonthlyPremiumAmount(new BigDecimal("200.00"));
        createDTO.setInsuredAmount(new BigDecimal("100000.00"));
        createDTO.setCoverages(Map.of("BASIC", new BigDecimal("50000.00")));
        createDTO.setAssistances(List.of("ASSISTANCE"));

        // Act
        PolicyRequest entity = mapper.toEntity(createDTO);

        // Assert
        assertNotNull(entity);
        assertEquals(createDTO.getCustomerId(), entity.getCustomerId());
        assertEquals(createDTO.getProductId(), entity.getProductId());
        assertEquals(createDTO.getCategory(), entity.getCategory());
        assertEquals(createDTO.getTotalMonthlyPremiumAmount(), entity.getTotalMonthlyPremiumAmount());
        assertEquals(createDTO.getInsuredAmount(), entity.getInsuredAmount());
        assertEquals(createDTO.getCoverages(), entity.getCoverages());
        assertEquals(createDTO.getAssistances(), entity.getAssistances());
        assertNull(entity.getPaymentMethod());
        assertNull(entity.getSalesChannel());
    }

    @Test
    void testToResponseDTO() {
        // Arrange
        PolicyRequest entity = new PolicyRequest();
        entity.setId(UUID.randomUUID());
        entity.setCustomerId(UUID.randomUUID());
        entity.setProductId("PROD789");
        entity.setCategory(InsuranceCategory.RESIDENCIAL);
        entity.setTotalMonthlyPremiumAmount(new BigDecimal("300.00"));
        entity.setInsuredAmount(new BigDecimal("250000.00"));
        entity.setPaymentMethod(PaymentMethod.BOLETO);
        entity.setSalesChannel(SalesChannel.WEBSITE);
        entity.setCoverages(Map.of("BASIC", new BigDecimal("125000.00")));
        entity.setAssistances(List.of("HOME_ASSISTANCE"));
        entity.setStatus(PolicyRequestStatus.APPROVED);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setFinishedAt(LocalDateTime.now().plusHours(2));

        // Act
        PolicyRequestResponseDTO dto = mapper.toResponseDTO(entity);

        // Assert
        assertNotNull(dto);
        assertEquals(entity.getId(), dto.getId());
        assertEquals(entity.getCustomerId(), dto.getCustomerId());
        assertEquals(entity.getProductId(), dto.getProductId());
        assertEquals(entity.getCategory(), dto.getCategory());
        assertEquals(entity.getTotalMonthlyPremiumAmount(), dto.getTotalMonthlyPremiumAmount());
        assertEquals(entity.getInsuredAmount(), dto.getInsuredAmount());
        assertEquals(entity.getPaymentMethod(), dto.getPaymentMethod());
        assertEquals(entity.getSalesChannel(), dto.getSalesChannel());
        assertEquals(entity.getCoverages(), dto.getCoverages());
        assertEquals(entity.getAssistances(), dto.getAssistances());
        assertEquals(entity.getStatus(), dto.getStatus());
        assertEquals(entity.getCreatedAt(), dto.getCreatedAt());
        assertEquals(entity.getFinishedAt(), dto.getFinishedAt());
    }

    @Test
    void testToResponseDTOWithNullEntity() {
        // Act & Assert
        assertNull(mapper.toResponseDTO(null));
    }

    @Test
    void testToResponseDTOWithHistory() {
        // Arrange
        PolicyRequest entity = new PolicyRequest();
        entity.setId(UUID.randomUUID());
        entity.setCustomerId(UUID.randomUUID());
        entity.setProductId("PROD999");
        entity.setCategory(InsuranceCategory.EMPRESARIAL);
        entity.setTotalMonthlyPremiumAmount(new BigDecimal("500.00"));
        entity.setInsuredAmount(new BigDecimal("500000.00"));
        entity.setStatus(PolicyRequestStatus.VALIDATED);

        // Cria histórico
        List<StatusHistory> history = new ArrayList<>();
        StatusHistory history1 = new StatusHistory();
        history1.setId(UUID.randomUUID());
        history1.setPolicyRequestId(entity.getId());
        history1.setStatus(PolicyRequestStatus.RECEIVED);
        history1.setTimestamp(LocalDateTime.now().minusHours(1));
        history1.setReason("Initial request");

        StatusHistory history2 = new StatusHistory();
        history2.setId(UUID.randomUUID());
        history2.setPolicyRequestId(entity.getId());
        history2.setStatus(PolicyRequestStatus.VALIDATED);
        history2.setTimestamp(LocalDateTime.now());
        history2.setReason("Fraud check passed");

        history.add(history1);
        history.add(history2);
        entity.setHistory(history);

        // Act
        PolicyRequestResponseDTO dto = mapper.toResponseDTO(entity);

        // Assert
        assertNotNull(dto);
        assertNotNull(dto.getHistory());
        assertEquals(2, dto.getHistory().size());
        
        StatusHistoryDTO historyDTO1 = dto.getHistory().getFirst();
        assertEquals(history1.getStatus(), historyDTO1.getStatus());
        assertEquals(history1.getTimestamp(), historyDTO1.getTimestamp());
        assertEquals(history1.getReason(), historyDTO1.getReason());

        StatusHistoryDTO historyDTO2 = dto.getHistory().get(1);
        assertEquals(history2.getStatus(), historyDTO2.getStatus());
        assertEquals(history2.getTimestamp(), historyDTO2.getTimestamp());
        assertEquals(history2.getReason(), historyDTO2.getReason());
    }

    @Test
    void testToResponseDTOList() {
        // Arrange
        List<PolicyRequest> entities = new ArrayList<>();
        
        PolicyRequest entity1 = new PolicyRequest();
        entity1.setId(UUID.randomUUID());
        entity1.setCustomerId(UUID.randomUUID());
        entity1.setProductId("PROD111");
        entity1.setCategory(InsuranceCategory.AUTO);
        entity1.setTotalMonthlyPremiumAmount(new BigDecimal("120.00"));
        entity1.setInsuredAmount(new BigDecimal("30000.00"));
        entity1.setStatus(PolicyRequestStatus.PENDING);

        PolicyRequest entity2 = new PolicyRequest();
        entity2.setId(UUID.randomUUID());
        entity2.setCustomerId(UUID.randomUUID());
        entity2.setProductId("PROD222");
        entity2.setCategory(InsuranceCategory.VIDA);
        entity2.setTotalMonthlyPremiumAmount(new BigDecimal("250.00"));
        entity2.setInsuredAmount(new BigDecimal("150000.00"));
        entity2.setStatus(PolicyRequestStatus.APPROVED);

        entities.add(entity1);
        entities.add(entity2);

        // Act
        List<PolicyRequestResponseDTO> dtoList = mapper.toResponseDTOList(entities);

        // Assert
        assertNotNull(dtoList);
        assertEquals(2, dtoList.size());
        
        PolicyRequestResponseDTO dto1 = dtoList.getFirst();
        assertEquals(entity1.getId(), dto1.getId());
        assertEquals(entity1.getCustomerId(), dto1.getCustomerId());
        assertEquals(entity1.getProductId(), dto1.getProductId());
        assertEquals(entity1.getCategory(), dto1.getCategory());
        assertEquals(entity1.getTotalMonthlyPremiumAmount(), dto1.getTotalMonthlyPremiumAmount());
        assertEquals(entity1.getInsuredAmount(), dto1.getInsuredAmount());
        assertEquals(entity1.getStatus(), dto1.getStatus());

        PolicyRequestResponseDTO dto2 = dtoList.get(1);
        assertEquals(entity2.getId(), dto2.getId());
        assertEquals(entity2.getCustomerId(), dto2.getCustomerId());
        assertEquals(entity2.getProductId(), dto2.getProductId());
        assertEquals(entity2.getCategory(), dto2.getCategory());
        assertEquals(entity2.getTotalMonthlyPremiumAmount(), dto2.getTotalMonthlyPremiumAmount());
        assertEquals(entity2.getInsuredAmount(), dto2.getInsuredAmount());
        assertEquals(entity2.getStatus(), dto2.getStatus());
    }

    @Test
    void testToResponseDTOListWithNullList() {
        // Act & Assert
        assertNull(mapper.toResponseDTOList(null));
    }

    @Test
    void testToResponseDTOListWithEmptyList() {
        // Arrange
        List<PolicyRequest> entities = new ArrayList<>();

        // Act
        List<PolicyRequestResponseDTO> dtoList = mapper.toResponseDTOList(entities);

        // Assert
        assertNotNull(dtoList);
        assertTrue(dtoList.isEmpty());
    }

    @Test
    void testToResponseDTOListWithNullElements() {
        // Arrange
        List<PolicyRequest> entities = new ArrayList<>();
        entities.add(null);
        entities.add(createValidPolicyRequest());

        // Act
        List<PolicyRequestResponseDTO> dtoList = mapper.toResponseDTOList(entities);

        // Assert
        assertNotNull(dtoList);
        assertEquals(2, dtoList.size());
        assertNull(dtoList.getFirst());
        assertNotNull(dtoList.get(1));
    }

    @Test
    void testToStatusHistoryDTO() {
        // Arrange
        StatusHistory statusHistory = new StatusHistory();
        statusHistory.setId(UUID.randomUUID());
        statusHistory.setPolicyRequestId(UUID.randomUUID());
        statusHistory.setStatus(PolicyRequestStatus.REJECTED);
        statusHistory.setTimestamp(LocalDateTime.now());
        statusHistory.setReason("Risk assessment failed");

        // Act
        StatusHistoryDTO dto = mapper.toStatusHistoryDTO(statusHistory);

        // Assert
        assertNotNull(dto);
        assertEquals(statusHistory.getStatus(), dto.getStatus());
        assertEquals(statusHistory.getTimestamp(), dto.getTimestamp());
        assertEquals(statusHistory.getReason(), dto.getReason());
    }

    @Test
    void testToStatusHistoryDTOWithNullStatusHistory() {
        // Act & Assert
        assertNull(mapper.toStatusHistoryDTO(null));
    }

    @Test
    void testToStatusHistoryDTOWithNullReason() {
        // Arrange
        StatusHistory statusHistory = new StatusHistory();
        statusHistory.setId(UUID.randomUUID());
        statusHistory.setPolicyRequestId(UUID.randomUUID());
        statusHistory.setStatus(PolicyRequestStatus.CANCELLED);
        statusHistory.setTimestamp(LocalDateTime.now());
        statusHistory.setReason(null);

        // Act
        StatusHistoryDTO dto = mapper.toStatusHistoryDTO(statusHistory);

        // Assert
        assertNotNull(dto);
        assertEquals(statusHistory.getStatus(), dto.getStatus());
        assertEquals(statusHistory.getTimestamp(), dto.getTimestamp());
        assertNull(dto.getReason());
    }

    @Test
    void testMappingAllInsuranceCategories() {
        // Test all enum values are mapped correctly
        for (InsuranceCategory category : InsuranceCategory.values()) {
            PolicyRequestCreateDTO createDTO = new PolicyRequestCreateDTO();
            createDTO.setCustomerId(UUID.randomUUID());
            createDTO.setProductId("PROD_" + category.name());
            createDTO.setCategory(category);
            createDTO.setTotalMonthlyPremiumAmount(new BigDecimal("100.00"));
            createDTO.setInsuredAmount(new BigDecimal("100000.00"));
            createDTO.setCoverages(Map.of("BASIC", new BigDecimal("50000.00")));
            createDTO.setAssistances(List.of("ASSISTANCE"));

            PolicyRequest entity = mapper.toEntity(createDTO);
            assertEquals(category, entity.getCategory());

            // Test reverse mapping
            entity.setId(UUID.randomUUID());
            entity.setStatus(PolicyRequestStatus.RECEIVED);
            
            PolicyRequestResponseDTO dto = mapper.toResponseDTO(entity);
            assertEquals(category, dto.getCategory());
        }
    }

    @Test
    void testMappingAllPaymentMethods() {
        // Test all enum values are mapped correctly
        for (PaymentMethod method : PaymentMethod.values()) {
            PolicyRequestCreateDTO createDTO = new PolicyRequestCreateDTO();
            createDTO.setCustomerId(UUID.randomUUID());
            createDTO.setProductId("PROD_" + method.name());
            createDTO.setCategory(InsuranceCategory.AUTO);
            createDTO.setPaymentMethod(method);
            createDTO.setTotalMonthlyPremiumAmount(new BigDecimal("150.00"));
            createDTO.setInsuredAmount(new BigDecimal("50000.00"));
            createDTO.setCoverages(Map.of("BASIC", new BigDecimal("25000.00")));
            createDTO.setAssistances(List.of("24H_ASSISTANCE"));

            PolicyRequest entity = mapper.toEntity(createDTO);
            assertEquals(method, entity.getPaymentMethod());

            // Test reverse mapping
            entity.setId(UUID.randomUUID());
            entity.setStatus(PolicyRequestStatus.RECEIVED);
            
            PolicyRequestResponseDTO dto = mapper.toResponseDTO(entity);
            assertEquals(method, dto.getPaymentMethod());
        }
    }

    @Test
    void testMappingAllSalesChannels() {
        // Test all enum values are mapped correctly
        for (SalesChannel channel : SalesChannel.values()) {
            PolicyRequestCreateDTO createDTO = new PolicyRequestCreateDTO();
            createDTO.setCustomerId(UUID.randomUUID());
            createDTO.setProductId("PROD_" + channel.name());
            createDTO.setCategory(InsuranceCategory.RESIDENCIAL);
            createDTO.setSalesChannel(channel);
            createDTO.setTotalMonthlyPremiumAmount(new BigDecimal("200.00"));
            createDTO.setInsuredAmount(new BigDecimal("200000.00"));
            createDTO.setCoverages(Map.of("BASIC", new BigDecimal("100000.00")));
            createDTO.setAssistances(List.of("HOME_ASSISTANCE"));

            PolicyRequest entity = mapper.toEntity(createDTO);
            assertEquals(channel, entity.getSalesChannel());

            // Test reverse mapping
            entity.setId(UUID.randomUUID());
            entity.setStatus(PolicyRequestStatus.RECEIVED);
            
            PolicyRequestResponseDTO dto = mapper.toResponseDTO(entity);
            assertEquals(channel, dto.getSalesChannel());
        }
    }

    @Test
    void testMappingAllPolicyRequestStatuses() {
        // Test all enum values are mapped correctly in response
        for (PolicyRequestStatus status : PolicyRequestStatus.values()) {
            PolicyRequest entity = createValidPolicyRequest();
            entity.setStatus(status);

            PolicyRequestResponseDTO dto = mapper.toResponseDTO(entity);
            assertEquals(status, dto.getStatus());
        }
    }

    @Test
    void testMappingBigDecimalPrecision() {
        // Arrange
        PolicyRequestCreateDTO createDTO = new PolicyRequestCreateDTO();
        createDTO.setCustomerId(UUID.randomUUID());
        createDTO.setProductId("PROD_PRECISION");
        createDTO.setCategory(InsuranceCategory.EMPRESARIAL);
        createDTO.setTotalMonthlyPremiumAmount(new BigDecimal("123.45"));
        createDTO.setInsuredAmount(new BigDecimal("123456.789"));
        createDTO.setCoverages(Map.of("BASIC", new BigDecimal("61728.394")));
        createDTO.setAssistances(List.of("BUSINESS_ASSISTANCE"));

        // Act
        PolicyRequest entity = mapper.toEntity(createDTO);

        // Assert
        assertNotNull(entity.getTotalMonthlyPremiumAmount());
        assertNotNull(entity.getInsuredAmount());
        assertEquals(0, createDTO.getTotalMonthlyPremiumAmount().compareTo(entity.getTotalMonthlyPremiumAmount()));
        assertEquals(0, createDTO.getInsuredAmount().compareTo(entity.getInsuredAmount()));
        assertEquals(createDTO.getTotalMonthlyPremiumAmount().scale(), entity.getTotalMonthlyPremiumAmount().scale());
        assertEquals(createDTO.getInsuredAmount().scale(), entity.getInsuredAmount().scale());
    }

    @Test
    void testMappingUUIDIntegrity() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        PolicyRequestCreateDTO createDTO = new PolicyRequestCreateDTO();
        createDTO.setCustomerId(customerId);
        createDTO.setProductId("PROD_UUID");
        createDTO.setCategory(InsuranceCategory.AUTO);
        createDTO.setTotalMonthlyPremiumAmount(new BigDecimal("140.00"));
        createDTO.setInsuredAmount(new BigDecimal("40000.00"));
        createDTO.setCoverages(Map.of("BASIC", new BigDecimal("20000.00")));
        createDTO.setAssistances(List.of("AUTO_ASSISTANCE"));

        // Act
        PolicyRequest entity = mapper.toEntity(createDTO);

        // Assert
        assertEquals(customerId, entity.getCustomerId());
        assertEquals(customerId.toString(), entity.getCustomerId().toString());

        // Test reverse mapping
        UUID entityId = UUID.randomUUID();
        entity.setId(entityId);
        entity.setStatus(PolicyRequestStatus.RECEIVED);
        
        PolicyRequestResponseDTO dto = mapper.toResponseDTO(entity);
        assertEquals(entityId, dto.getId());
        assertEquals(customerId, dto.getCustomerId());
    }

    @Test
    void testMappingCollectionFields() {
        // Arrange
        Map<String, BigDecimal> coverages = Map.of(
            "BASIC", new BigDecimal("30000.00"),
            "PREMIUM", new BigDecimal("30000.00")
        );
        List<String> assistances = List.of("24H_ASSISTANCE", "EMERGENCY_SERVICE");

        PolicyRequestCreateDTO createDTO = new PolicyRequestCreateDTO();
        createDTO.setCustomerId(UUID.randomUUID());
        createDTO.setProductId("PROD_COLLECTIONS");
        createDTO.setCategory(InsuranceCategory.AUTO);
        createDTO.setTotalMonthlyPremiumAmount(new BigDecimal("180.00"));
        createDTO.setInsuredAmount(new BigDecimal("60000.00"));
        createDTO.setCoverages(coverages);
        createDTO.setAssistances(assistances);

        // Act
        PolicyRequest entity = mapper.toEntity(createDTO);

        // Assert
        assertEquals(coverages, entity.getCoverages());
        assertEquals(assistances, entity.getAssistances());

        // Test reverse mapping
        entity.setId(UUID.randomUUID());
        entity.setStatus(PolicyRequestStatus.RECEIVED);
        
        PolicyRequestResponseDTO dto = mapper.toResponseDTO(entity);
        assertEquals(coverages, dto.getCoverages());
        assertEquals(assistances, dto.getAssistances());
    }

    @Test
    void testMappingLocalDateTimeFields() {
        // Arrange
        PolicyRequest entity = createValidPolicyRequest();
        LocalDateTime createdAt = LocalDateTime.of(2023, 12, 25, 10, 30, 45);
        LocalDateTime finishedAt = LocalDateTime.of(2023, 12, 25, 14, 15, 30);
        
        entity.setCreatedAt(createdAt);
        entity.setFinishedAt(finishedAt);

        // Act
        PolicyRequestResponseDTO dto = mapper.toResponseDTO(entity);

        // Assert
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(finishedAt, dto.getFinishedAt());
    }

    @Test
    void testCompleteRoundTripMapping() {
        // Arrange - Create DTO
        PolicyRequestCreateDTO createDTO = new PolicyRequestCreateDTO();
        createDTO.setCustomerId(UUID.randomUUID());
        createDTO.setProductId("PROD_ROUNDTRIP");
        createDTO.setCategory(InsuranceCategory.VIDA);
        createDTO.setTotalMonthlyPremiumAmount(new BigDecimal("220.00"));
        createDTO.setInsuredAmount(new BigDecimal("200000.00"));
        createDTO.setPaymentMethod(PaymentMethod.PIX);
        createDTO.setSalesChannel(SalesChannel.WHATSAPP);
        createDTO.setCoverages(Map.of("LIFE", new BigDecimal("200000.00")));
        createDTO.setAssistances(List.of("LIFE_ASSISTANCE"));

        // Act - DTO to Entity
        PolicyRequest entity = mapper.toEntity(createDTO);
        
        // Complete entity with fields ignored in mapping
        entity.setId(UUID.randomUUID());
        entity.setStatus(PolicyRequestStatus.APPROVED);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setFinishedAt(LocalDateTime.now().plusHours(1));

        // Act - Entity to Response DTO
        PolicyRequestResponseDTO responseDTO = mapper.toResponseDTO(entity);

        // Assert - Verify all mapped fields
        assertEquals(entity.getId(), responseDTO.getId());
        assertEquals(createDTO.getCustomerId(), responseDTO.getCustomerId());
        assertEquals(createDTO.getProductId(), responseDTO.getProductId());
        assertEquals(createDTO.getCategory(), responseDTO.getCategory());
        assertEquals(0, createDTO.getTotalMonthlyPremiumAmount().compareTo(responseDTO.getTotalMonthlyPremiumAmount()));
        assertEquals(0, createDTO.getInsuredAmount().compareTo(responseDTO.getInsuredAmount()));
        assertEquals(createDTO.getPaymentMethod(), responseDTO.getPaymentMethod());
        assertEquals(createDTO.getSalesChannel(), responseDTO.getSalesChannel());
        assertEquals(createDTO.getCoverages(), responseDTO.getCoverages());
        assertEquals(createDTO.getAssistances(), responseDTO.getAssistances());
        assertEquals(entity.getStatus(), responseDTO.getStatus());
        assertEquals(entity.getCreatedAt(), responseDTO.getCreatedAt());
        assertEquals(entity.getFinishedAt(), responseDTO.getFinishedAt());
    }

    private PolicyRequest createValidPolicyRequest() {
        PolicyRequest entity = new PolicyRequest();
        entity.setId(UUID.randomUUID());
        entity.setCustomerId(UUID.randomUUID());
        entity.setProductId("PROD_VALID");
        entity.setCategory(InsuranceCategory.AUTO);
        entity.setTotalMonthlyPremiumAmount(new BigDecimal("135.00"));
        entity.setInsuredAmount(new BigDecimal("45000.00"));
        entity.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        entity.setSalesChannel(SalesChannel.MOBILE);
        entity.setCoverages(Map.of("BASIC", new BigDecimal("22500.00")));
        entity.setAssistances(List.of("AUTO_ASSISTANCE"));
        entity.setStatus(PolicyRequestStatus.RECEIVED);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
}
