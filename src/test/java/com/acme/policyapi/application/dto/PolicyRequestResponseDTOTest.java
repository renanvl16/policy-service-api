package com.acme.policyapi.application.dto;

import com.acme.policyapi.domain.entity.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para PolicyRequestResponseDTO.
 */
class PolicyRequestResponseDTOTest {

    @Test
    void testGettersAndSetters() {
        PolicyRequestResponseDTO dto = new PolicyRequestResponseDTO();
        
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String productId = "PROD001";
        InsuranceCategory category = InsuranceCategory.AUTO;
        SalesChannel salesChannel = SalesChannel.WEBSITE;
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;
        PolicyRequestStatus status = PolicyRequestStatus.PENDING;
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime finishedAt = LocalDateTime.now().plusDays(1);
        BigDecimal totalMonthlyPremiumAmount = new BigDecimal("150.00");
        BigDecimal insuredAmount = new BigDecimal("50000.00");
        Map<String, BigDecimal> coverages = Map.of("BASIC", new BigDecimal("25000.00"));
        List<String> assistances = List.of("24H_ASSISTANCE");
        List<StatusHistoryDTO> history = createStatusHistory();

        dto.setId(id);
        dto.setCustomerId(customerId);
        dto.setProductId(productId);
        dto.setCategory(category);
        dto.setSalesChannel(salesChannel);
        dto.setPaymentMethod(paymentMethod);
        dto.setStatus(status);
        dto.setCreatedAt(createdAt);
        dto.setFinishedAt(finishedAt);
        dto.setTotalMonthlyPremiumAmount(totalMonthlyPremiumAmount);
        dto.setInsuredAmount(insuredAmount);
        dto.setCoverages(coverages);
        dto.setAssistances(assistances);
        dto.setHistory(history);

        assertEquals(id, dto.getId());
        assertEquals(customerId, dto.getCustomerId());
        assertEquals(productId, dto.getProductId());
        assertEquals(category, dto.getCategory());
        assertEquals(salesChannel, dto.getSalesChannel());
        assertEquals(paymentMethod, dto.getPaymentMethod());
        assertEquals(status, dto.getStatus());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(finishedAt, dto.getFinishedAt());
        assertEquals(totalMonthlyPremiumAmount, dto.getTotalMonthlyPremiumAmount());
        assertEquals(insuredAmount, dto.getInsuredAmount());
        assertEquals(coverages, dto.getCoverages());
        assertEquals(assistances, dto.getAssistances());
        assertEquals(history, dto.getHistory());
    }

    @Test
    void testAllEnumValues() {
        PolicyRequestResponseDTO dto = new PolicyRequestResponseDTO();
        
        // Teste todas as categorias
        for (InsuranceCategory category : InsuranceCategory.values()) {
            dto.setCategory(category);
            assertEquals(category, dto.getCategory());
        }
        
        // Teste todos os canais de vendas
        for (SalesChannel channel : SalesChannel.values()) {
            dto.setSalesChannel(channel);
            assertEquals(channel, dto.getSalesChannel());
        }
        
        // Teste todos os métodos de pagamento
        for (PaymentMethod method : PaymentMethod.values()) {
            dto.setPaymentMethod(method);
            assertEquals(method, dto.getPaymentMethod());
        }
        
        // Teste todos os status
        for (PolicyRequestStatus status : PolicyRequestStatus.values()) {
            dto.setStatus(status);
            assertEquals(status, dto.getStatus());
        }
    }

    @Test
    void testNullValues() {
        PolicyRequestResponseDTO dto = new PolicyRequestResponseDTO();
        
        // Todos os campos podem ser nulos
        assertNull(dto.getId());
        assertNull(dto.getCustomerId());
        assertNull(dto.getProductId());
        assertNull(dto.getCategory());
        assertNull(dto.getSalesChannel());
        assertNull(dto.getPaymentMethod());
        assertNull(dto.getStatus());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getFinishedAt());
        assertNull(dto.getTotalMonthlyPremiumAmount());
        assertNull(dto.getInsuredAmount());
        assertNull(dto.getCoverages());
        assertNull(dto.getAssistances());
        assertNull(dto.getHistory());
    }

    @Test
    void testEmptyCollections() {
        PolicyRequestResponseDTO dto = new PolicyRequestResponseDTO();
        
        Map<String, BigDecimal> emptyCoverages = new HashMap<>();
        List<String> emptyAssistances = new ArrayList<>();
        List<StatusHistoryDTO> emptyHistory = new ArrayList<>();

        dto.setCoverages(emptyCoverages);
        dto.setAssistances(emptyAssistances);
        dto.setHistory(emptyHistory);
        
        assertEquals(emptyCoverages, dto.getCoverages());
        assertEquals(emptyAssistances, dto.getAssistances());
        assertEquals(emptyHistory, dto.getHistory());
        
        assertTrue(dto.getCoverages().isEmpty());
        assertTrue(dto.getAssistances().isEmpty());
        assertTrue(dto.getHistory().isEmpty());
    }

    @Test
    void testStatusHistoryDTO() {
        StatusHistoryDTO historyDTO = new StatusHistoryDTO();

        PolicyRequestStatus status = PolicyRequestStatus.APPROVED;
        LocalDateTime timestamp = LocalDateTime.now();
        String reason = "Aprovado automaticamente";
        
        historyDTO.setStatus(status);
        historyDTO.setTimestamp(timestamp);
        historyDTO.setReason(reason);
        
        assertEquals(status, historyDTO.getStatus());
        assertEquals(timestamp, historyDTO.getTimestamp());
        assertEquals(reason, historyDTO.getReason());
    }

    @Test
    void testStatusHistoryDTONullValues() {
        StatusHistoryDTO historyDTO = new StatusHistoryDTO();

        assertNull(historyDTO.getStatus());
        assertNull(historyDTO.getTimestamp());
        assertNull(historyDTO.getReason());
    }

    @Test
    void testStatusHistoryDTOAllStatuses() {
        StatusHistoryDTO historyDTO = new StatusHistoryDTO();

        for (PolicyRequestStatus status : PolicyRequestStatus.values()) {
            historyDTO.setStatus(status);
            assertEquals(status, historyDTO.getStatus());
        }
    }

    @Test
    void testComplexCoveragesMap() {
        PolicyRequestResponseDTO dto = new PolicyRequestResponseDTO();
        
        Map<String, BigDecimal> coverages = new HashMap<>();
        coverages.put("COBERTURA_BASICA", new BigDecimal("50000.00"));
        coverages.put("COBERTURA_AMPLIADA", new BigDecimal("75000.00"));
        coverages.put("COBERTURA_TOTAL", new BigDecimal("100000.00"));
        
        dto.setCoverages(coverages);
        
        assertEquals(3, dto.getCoverages().size());
        assertEquals(new BigDecimal("50000.00"), dto.getCoverages().get("COBERTURA_BASICA"));
        assertEquals(new BigDecimal("75000.00"), dto.getCoverages().get("COBERTURA_AMPLIADA"));
        assertEquals(new BigDecimal("100000.00"), dto.getCoverages().get("COBERTURA_TOTAL"));
    }

    @Test
    void testComplexAssistancesList() {
        PolicyRequestResponseDTO dto = new PolicyRequestResponseDTO();
        
        List<String> assistances = Arrays.asList(
            "ASSISTENCIA_24H",
            "GUINCHO",
            "CHAVEIRO",
            "ELETRICISTA",
            "BORRACHEIRO"
        );
        
        dto.setAssistances(assistances);
        
        assertEquals(5, dto.getAssistances().size());
        assertTrue(dto.getAssistances().contains("ASSISTENCIA_24H"));
        assertTrue(dto.getAssistances().contains("GUINCHO"));
        assertTrue(dto.getAssistances().contains("CHAVEIRO"));
        assertTrue(dto.getAssistances().contains("ELETRICISTA"));
        assertTrue(dto.getAssistances().contains("BORRACHEIRO"));
    }

    @Test
    void testComplexHistoryList() {
        PolicyRequestResponseDTO dto = new PolicyRequestResponseDTO();
        
        List<StatusHistoryDTO> history = Arrays.asList(
            createStatusHistoryDTO(PolicyRequestStatus.PENDING, "Solicitação criada"),
            createStatusHistoryDTO(PolicyRequestStatus.VALIDATED, "Em análise"),
            createStatusHistoryDTO(PolicyRequestStatus.APPROVED, "Aprovado")
        );
        
        dto.setHistory(history);
        
        assertEquals(3, dto.getHistory().size());
        assertEquals(PolicyRequestStatus.PENDING, dto.getHistory().get(0).getStatus());
        assertEquals(PolicyRequestStatus.VALIDATED, dto.getHistory().get(1).getStatus());
        assertEquals(PolicyRequestStatus.APPROVED, dto.getHistory().get(2).getStatus());
    }

    private PolicyRequestResponseDTO createTestDTO() {
        PolicyRequestResponseDTO dto = new PolicyRequestResponseDTO();
        dto.setId(UUID.fromString("12345678-1234-1234-1234-123456789abc"));
        dto.setCustomerId(UUID.fromString("87654321-4321-4321-4321-cba987654321"));
        dto.setProductId("TEST_PRODUCT");
        dto.setCategory(InsuranceCategory.AUTO);
        dto.setSalesChannel(SalesChannel.WEBSITE);
        dto.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        dto.setStatus(PolicyRequestStatus.APPROVED);
        dto.setCreatedAt(LocalDateTime.of(2023, 1, 1, 10, 0, 0));
        dto.setFinishedAt(LocalDateTime.of(2023, 1, 2, 10, 0, 0));
        dto.setTotalMonthlyPremiumAmount(new BigDecimal("150.00"));
        dto.setInsuredAmount(new BigDecimal("50000.00"));
        dto.setCoverages(Map.of("BASIC", new BigDecimal("25000.00")));
        dto.setAssistances(List.of("24H_ASSISTANCE"));
        dto.setHistory(createStatusHistory());
        return dto;
    }

    private List<StatusHistoryDTO> createStatusHistory() {
        List<StatusHistoryDTO> history = new ArrayList<>();

        StatusHistoryDTO history1 = new StatusHistoryDTO();
        history1.setStatus(PolicyRequestStatus.RECEIVED);
        history1.setTimestamp(LocalDateTime.now().minusDays(1));
        history1.setReason("Initial request");

        StatusHistoryDTO history2 = new StatusHistoryDTO();
        history2.setStatus(PolicyRequestStatus.PENDING);
        history2.setTimestamp(LocalDateTime.now());
        history2.setReason("Under review");

        history.add(history1);
        history.add(history2);

        return history;
    }

    private StatusHistoryDTO createStatusHistoryDTO() {
        StatusHistoryDTO historyDTO = new StatusHistoryDTO();
        historyDTO.setStatus(PolicyRequestStatus.VALIDATED);
        historyDTO.setTimestamp(LocalDateTime.now());
        historyDTO.setReason("Test reason");
        return historyDTO;
    }

    private StatusHistoryDTO createStatusHistoryDTO(PolicyRequestStatus status, String reason) {
        StatusHistoryDTO historyDTO = new StatusHistoryDTO();
        historyDTO.setStatus(status);
        historyDTO.setTimestamp(LocalDateTime.now());
        historyDTO.setReason(reason);
        return historyDTO;
    }
}