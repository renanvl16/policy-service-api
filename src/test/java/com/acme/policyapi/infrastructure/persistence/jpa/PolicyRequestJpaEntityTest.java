package com.acme.policyapi.infrastructure.persistence.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PolicyRequestJpaEntityTest {

    private PolicyRequestJpaEntity entity;

    @BeforeEach
    void setUp() {
        entity = new PolicyRequestJpaEntity();
    }

    @Test
    void testDefaultConstructor() {
        PolicyRequestJpaEntity newEntity = new PolicyRequestJpaEntity();
        
        assertNull(newEntity.getId());
        assertNull(newEntity.getCustomerId());
        assertNull(newEntity.getProductId());
        assertNull(newEntity.getCategory());
        assertNull(newEntity.getSalesChannel());
        assertNull(newEntity.getPaymentMethod());
        assertNull(newEntity.getStatus());
        assertNull(newEntity.getCreatedAt());
        assertNull(newEntity.getFinishedAt());
        assertNull(newEntity.getTotalMonthlyPremiumAmount());
        assertNull(newEntity.getInsuredAmount());
        assertNotNull(newEntity.getCoverages());
        assertTrue(newEntity.getCoverages().isEmpty());
        assertNotNull(newEntity.getAssistances());
        assertTrue(newEntity.getAssistances().isEmpty());
        assertNotNull(newEntity.getHistory());
        assertTrue(newEntity.getHistory().isEmpty());
    }

    @Test
    void testAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String productId = "PROD123";
        String category = "AUTO";
        String salesChannel = "ONLINE";
        String paymentMethod = "CREDIT_CARD";
        String status = "RECEIVED";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime finishedAt = LocalDateTime.now().plusHours(1);
        BigDecimal totalMonthlyPremiumAmount = new BigDecimal("150.00");
        BigDecimal insuredAmount = new BigDecimal("50000.00");
        Map<String, BigDecimal> coverages = new HashMap<>();
        coverages.put("COLLISION", new BigDecimal("25000.00"));
        List<String> assistances = Arrays.asList("24h Assistance", "Towing");
        List<StatusHistoryJpaEntity> history = new ArrayList<>();

        PolicyRequestJpaEntity allArgsEntity = new PolicyRequestJpaEntity(
                id, customerId, productId, category, salesChannel, paymentMethod, status,
                createdAt, finishedAt, totalMonthlyPremiumAmount, insuredAmount,
                coverages, assistances, history
        );

        assertEquals(id, allArgsEntity.getId());
        assertEquals(customerId, allArgsEntity.getCustomerId());
        assertEquals(productId, allArgsEntity.getProductId());
        assertEquals(category, allArgsEntity.getCategory());
        assertEquals(salesChannel, allArgsEntity.getSalesChannel());
        assertEquals(paymentMethod, allArgsEntity.getPaymentMethod());
        assertEquals(status, allArgsEntity.getStatus());
        assertEquals(createdAt, allArgsEntity.getCreatedAt());
        assertEquals(finishedAt, allArgsEntity.getFinishedAt());
        assertEquals(totalMonthlyPremiumAmount, allArgsEntity.getTotalMonthlyPremiumAmount());
        assertEquals(insuredAmount, allArgsEntity.getInsuredAmount());
        assertEquals(coverages, allArgsEntity.getCoverages());
        assertEquals(assistances, allArgsEntity.getAssistances());
        assertEquals(history, allArgsEntity.getHistory());
    }

    @Test
    void testBuilder() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String productId = "PROD456";
        BigDecimal premiumAmount = new BigDecimal("200.00");

        PolicyRequestJpaEntity builtEntity = PolicyRequestJpaEntity.builder()
                .id(id)
                .customerId(customerId)
                .productId(productId)
                .category("LIFE")
                .salesChannel("PHONE")
                .paymentMethod("DEBIT_CARD")
                .status("VALIDATED")
                .totalMonthlyPremiumAmount(premiumAmount)
                .insuredAmount(new BigDecimal("100000.00"))
                .build();

        assertEquals(id, builtEntity.getId());
        assertEquals(customerId, builtEntity.getCustomerId());
        assertEquals(productId, builtEntity.getProductId());
        assertEquals("LIFE", builtEntity.getCategory());
        assertEquals("PHONE", builtEntity.getSalesChannel());
        assertEquals("DEBIT_CARD", builtEntity.getPaymentMethod());
        assertEquals("VALIDATED", builtEntity.getStatus());
        assertEquals(premiumAmount, builtEntity.getTotalMonthlyPremiumAmount());
        assertEquals(new BigDecimal("100000.00"), builtEntity.getInsuredAmount());
    }

    @Test
    void testGettersAndSetters() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String productId = "PROD789";
        String category = "HOME";
        String salesChannel = "BRANCH";
        String paymentMethod = "BANK_TRANSFER";
        String status = "PENDING";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime finishedAt = LocalDateTime.now().plusDays(1);
        BigDecimal totalMonthlyPremiumAmount = new BigDecimal("75.50");
        BigDecimal insuredAmount = new BigDecimal("25000.00");

        entity.setId(id);
        entity.setCustomerId(customerId);
        entity.setProductId(productId);
        entity.setCategory(category);
        entity.setSalesChannel(salesChannel);
        entity.setPaymentMethod(paymentMethod);
        entity.setStatus(status);
        entity.setCreatedAt(createdAt);
        entity.setFinishedAt(finishedAt);
        entity.setTotalMonthlyPremiumAmount(totalMonthlyPremiumAmount);
        entity.setInsuredAmount(insuredAmount);

        assertEquals(id, entity.getId());
        assertEquals(customerId, entity.getCustomerId());
        assertEquals(productId, entity.getProductId());
        assertEquals(category, entity.getCategory());
        assertEquals(salesChannel, entity.getSalesChannel());
        assertEquals(paymentMethod, entity.getPaymentMethod());
        assertEquals(status, entity.getStatus());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(finishedAt, entity.getFinishedAt());
        assertEquals(totalMonthlyPremiumAmount, entity.getTotalMonthlyPremiumAmount());
        assertEquals(insuredAmount, entity.getInsuredAmount());
    }

    @Test
    void testCoveragesCollection() {
        Map<String, BigDecimal> coverages = new HashMap<>();
        coverages.put("COMPREHENSIVE", new BigDecimal("30000.00"));
        coverages.put("COLLISION", new BigDecimal("20000.00"));
        coverages.put("LIABILITY", new BigDecimal("15000.00"));

        entity.setCoverages(coverages);

        assertEquals(3, entity.getCoverages().size());
        assertEquals(new BigDecimal("30000.00"), entity.getCoverages().get("COMPREHENSIVE"));
        assertEquals(new BigDecimal("20000.00"), entity.getCoverages().get("COLLISION"));
        assertEquals(new BigDecimal("15000.00"), entity.getCoverages().get("LIABILITY"));
    }

    @Test
    void testAssistancesCollection() {
        List<String> assistances = Arrays.asList(
                "24h Roadside Assistance",
                "Rental Car Coverage",
                "Emergency Medical",
                "Glass Repair"
        );

        entity.setAssistances(assistances);

        assertEquals(4, entity.getAssistances().size());
        assertTrue(entity.getAssistances().contains("24h Roadside Assistance"));
        assertTrue(entity.getAssistances().contains("Rental Car Coverage"));
        assertTrue(entity.getAssistances().contains("Emergency Medical"));
        assertTrue(entity.getAssistances().contains("Glass Repair"));
    }

    @Test
    void testHistoryCollection() {
        List<StatusHistoryJpaEntity> history = new ArrayList<>();
        
        StatusHistoryJpaEntity history1 = new StatusHistoryJpaEntity();
        history1.setStatus("RECEIVED");
        history1.setTimestamp(LocalDateTime.now());
        
        StatusHistoryJpaEntity history2 = new StatusHistoryJpaEntity();
        history2.setStatus("VALIDATED");
        history2.setTimestamp(LocalDateTime.now().plusMinutes(30));
        
        history.add(history1);
        history.add(history2);

        entity.setHistory(history);

        assertEquals(2, entity.getHistory().size());
        assertEquals("RECEIVED", entity.getHistory().get(0).getStatus());
        assertEquals("VALIDATED", entity.getHistory().get(1).getStatus());
    }

    @Test
    void testPrePersistWithNullValues() {
        assertNull(entity.getCreatedAt());
        assertNull(entity.getStatus());

        entity.prePersist();

        assertNotNull(entity.getCreatedAt());
        assertEquals("RECEIVED", entity.getStatus());
    }

    @Test
    void testPrePersistWithExistingValues() {
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
        String existingStatus = "VALIDATED";

        entity.setCreatedAt(existingCreatedAt);
        entity.setStatus(existingStatus);

        entity.prePersist();

        assertEquals(existingCreatedAt, entity.getCreatedAt());
        assertEquals(existingStatus, entity.getStatus());
    }

    @Test
    void testBuilderWithDefaultCollections() {
        PolicyRequestJpaEntity entityWithDefaults = PolicyRequestJpaEntity.builder()
                .id(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .productId("TEST_PRODUCT")
                .build();

        assertNotNull(entityWithDefaults.getCoverages());
        assertTrue(entityWithDefaults.getCoverages().isEmpty());
        assertNotNull(entityWithDefaults.getAssistances());
        assertTrue(entityWithDefaults.getAssistances().isEmpty());
        assertNotNull(entityWithDefaults.getHistory());
        assertTrue(entityWithDefaults.getHistory().isEmpty());
    }

    @Test
    void testNullSafetyForCollections() {
        entity.setCoverages(null);
        entity.setAssistances(null);
        entity.setHistory(null);

        assertNull(entity.getCoverages());
        assertNull(entity.getAssistances());
        assertNull(entity.getHistory());
    }

    @Test
    void testBuilderWithCustomCollections() {
        Map<String, BigDecimal> customCoverages = Map.of("FIRE", new BigDecimal("10000"));
        List<String> customAssistances = List.of("Emergency Support");

        PolicyRequestJpaEntity entity = PolicyRequestJpaEntity.builder()
                .id(UUID.randomUUID())
                .coverages(customCoverages)
                .assistances(customAssistances)
                .build();

        assertEquals(customCoverages, entity.getCoverages());
        assertEquals(customAssistances, entity.getAssistances());
    }
}