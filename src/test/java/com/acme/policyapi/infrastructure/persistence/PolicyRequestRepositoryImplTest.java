package com.acme.policyapi.infrastructure.persistence;

import com.acme.policyapi.domain.entity.*;
import com.acme.policyapi.infrastructure.persistence.jpa.PolicyRequestJpaEntity;
import com.acme.policyapi.infrastructure.persistence.jpa.PolicyRequestJpaRepository;
import com.acme.policyapi.infrastructure.persistence.jpa.StatusHistoryJpaEntity;
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
class PolicyRequestRepositoryImplTest {

    @Mock
    private PolicyRequestJpaRepository jpaRepository;

    @InjectMocks
    private PolicyRequestRepositoryImpl repository;

    private PolicyRequestJpaEntity jpaEntity;
    private PolicyRequest domainEntity;
    private UUID testId;
    private UUID testCustomerId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testCustomerId = UUID.randomUUID();
        
        setupJpaEntity();
        setupDomainEntity();
    }

    private void setupJpaEntity() {
        jpaEntity = PolicyRequestJpaEntity.builder()
                .id(testId)
                .customerId(testCustomerId)
                .productId("PROD123")
                .category("AUTO")
                .salesChannel("WEBSITE")
                .paymentMethod("CREDIT_CARD")
                .status("RECEIVED")
                .createdAt(LocalDateTime.of(2025, 1, 1, 10, 0))
                .totalMonthlyPremiumAmount(new BigDecimal("150.00"))
                .insuredAmount(new BigDecimal("50000.00"))
                .build();

        // Configurar coleções
        Map<String, BigDecimal> coverages = new HashMap<>();
        coverages.put("COLLISION", new BigDecimal("25000.00"));
        jpaEntity.setCoverages(coverages);

        List<String> assistances = Arrays.asList("24h Assistance");
        jpaEntity.setAssistances(assistances);

        List<StatusHistoryJpaEntity> history = new ArrayList<>();
        StatusHistoryJpaEntity historyItem = StatusHistoryJpaEntity.builder()
                .id(UUID.randomUUID())
                .policyRequestId(testId)
                .status("RECEIVED")
                .timestamp(LocalDateTime.of(2025, 1, 1, 10, 0))
                .reason("Initial creation")
                .build();
        history.add(historyItem);
        jpaEntity.setHistory(history);
    }

    private void setupDomainEntity() {
        domainEntity = new PolicyRequest();
        domainEntity.setId(testId);
        domainEntity.setCustomerId(testCustomerId);
        domainEntity.setProductId("PROD123");
        domainEntity.setCategory(InsuranceCategory.AUTO);
        domainEntity.setSalesChannel(SalesChannel.WEBSITE);
        domainEntity.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        domainEntity.setStatus(PolicyRequestStatus.RECEIVED);
        domainEntity.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        domainEntity.setTotalMonthlyPremiumAmount(new BigDecimal("150.00"));
        domainEntity.setInsuredAmount(new BigDecimal("50000.00"));

        Map<String, BigDecimal> coverages = new HashMap<>();
        coverages.put("COLLISION", new BigDecimal("25000.00"));
        domainEntity.setCoverages(coverages);

        List<String> assistances = Arrays.asList("24h Assistance");
        domainEntity.setAssistances(assistances);
    }

    @Test
    void testFindByCustomerId() {
        List<PolicyRequestJpaEntity> jpaEntities = Arrays.asList(jpaEntity);
        when(jpaRepository.findByCustomerId(testCustomerId)).thenReturn(jpaEntities);

        List<PolicyRequest> result = repository.findByCustomerId(testCustomerId);

        assertNotNull(result);
        assertEquals(1, result.size());
        PolicyRequest resultEntity = result.get(0);
        assertEquals(testId, resultEntity.getId());
        assertEquals(testCustomerId, resultEntity.getCustomerId());
        assertEquals("PROD123", resultEntity.getProductId());
        assertEquals(InsuranceCategory.AUTO, resultEntity.getCategory());
        
        verify(jpaRepository, times(1)).findByCustomerId(testCustomerId);
    }

    @Test
    void testFindByCustomerIdEmpty() {
        when(jpaRepository.findByCustomerId(testCustomerId)).thenReturn(Collections.emptyList());

        List<PolicyRequest> result = repository.findByCustomerId(testCustomerId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(jpaRepository, times(1)).findByCustomerId(testCustomerId);
    }

    @Test
    void testFindByIdWithHistory() {
        when(jpaRepository.findByIdWithHistory(testId)).thenReturn(Optional.of(jpaEntity));

        Optional<PolicyRequest> result = repository.findByIdWithHistory(testId);

        assertTrue(result.isPresent());
        PolicyRequest resultEntity = result.get();
        assertEquals(testId, resultEntity.getId());
        assertEquals(1, resultEntity.getHistory().size());
        assertEquals(PolicyRequestStatus.RECEIVED, resultEntity.getHistory().get(0).getStatus());
        
        verify(jpaRepository, times(1)).findByIdWithHistory(testId);
    }

    @Test
    void testFindByIdWithHistoryNotFound() {
        when(jpaRepository.findByIdWithHistory(testId)).thenReturn(Optional.empty());

        Optional<PolicyRequest> result = repository.findByIdWithHistory(testId);

        assertFalse(result.isPresent());
        verify(jpaRepository, times(1)).findByIdWithHistory(testId);
    }

    @Test
    void testFindByCustomerIdWithHistory() {
        List<PolicyRequestJpaEntity> jpaEntities = Arrays.asList(jpaEntity);
        when(jpaRepository.findByCustomerIdWithHistory(testCustomerId)).thenReturn(jpaEntities);

        List<PolicyRequest> result = repository.findByCustomerIdWithHistory(testCustomerId);

        assertNotNull(result);
        assertEquals(1, result.size());
        PolicyRequest resultEntity = result.get(0);
        assertEquals(testId, resultEntity.getId());
        assertEquals(1, resultEntity.getHistory().size());
        
        verify(jpaRepository, times(1)).findByCustomerIdWithHistory(testCustomerId);
    }

    @Test
    void testSave() {
        when(jpaRepository.save(any(PolicyRequestJpaEntity.class))).thenReturn(jpaEntity);

        PolicyRequest result = repository.save(domainEntity);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals(testCustomerId, result.getCustomerId());
        verify(jpaRepository, times(1)).save(any(PolicyRequestJpaEntity.class));
    }

    @Test
    void testFindById() {
        when(jpaRepository.findById(testId)).thenReturn(Optional.of(jpaEntity));

        Optional<PolicyRequest> result = repository.findById(testId);

        assertTrue(result.isPresent());
        assertEquals(testId, result.get().getId());
        verify(jpaRepository, times(1)).findById(testId);
    }

    @Test
    void testFindByIdNotFound() {
        when(jpaRepository.findById(testId)).thenReturn(Optional.empty());

        Optional<PolicyRequest> result = repository.findById(testId);

        assertFalse(result.isPresent());
        verify(jpaRepository, times(1)).findById(testId);
    }

    @Test
    void testDeleteById() {
        repository.deleteById(testId);

        verify(jpaRepository, times(1)).deleteById(testId);
    }

    @Test
    void testDeleteAll() {
        repository.deleteAll();

        verify(jpaRepository, times(1)).deleteAll();
    }

    @Test
    void testExistsById() {
        when(jpaRepository.existsById(testId)).thenReturn(true);

        boolean result = repository.existsById(testId);

        assertTrue(result);
        verify(jpaRepository, times(1)).existsById(testId);
    }

    @Test
    void testExistsByIdFalse() {
        when(jpaRepository.existsById(testId)).thenReturn(false);

        boolean result = repository.existsById(testId);

        assertFalse(result);
        verify(jpaRepository, times(1)).existsById(testId);
    }

    @Test
    void testToDomainWithAllFields() {
        // Configurar JPA entity com todos os campos
        jpaEntity.setFinishedAt(LocalDateTime.of(2025, 1, 2, 15, 30));
        
        when(jpaRepository.findById(testId)).thenReturn(Optional.of(jpaEntity));

        Optional<PolicyRequest> result = repository.findById(testId);

        assertTrue(result.isPresent());
        PolicyRequest domain = result.get();
        assertEquals(testId, domain.getId());
        assertEquals(testCustomerId, domain.getCustomerId());
        assertEquals("PROD123", domain.getProductId());
        assertEquals(InsuranceCategory.AUTO, domain.getCategory());
        assertEquals(SalesChannel.WEBSITE, domain.getSalesChannel());
        assertEquals(PaymentMethod.CREDIT_CARD, domain.getPaymentMethod());
        assertEquals(PolicyRequestStatus.RECEIVED, domain.getStatus());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), domain.getCreatedAt());
        assertEquals(LocalDateTime.of(2025, 1, 2, 15, 30), domain.getFinishedAt());
        assertEquals(new BigDecimal("150.00"), domain.getTotalMonthlyPremiumAmount());
        assertEquals(new BigDecimal("50000.00"), domain.getInsuredAmount());
        assertEquals(1, domain.getCoverages().size());
        assertEquals(1, domain.getAssistances().size());
    }

    @Test
    void testToDomainWithEmptyCollections() {
        jpaEntity.setCoverages(new HashMap<>());
        jpaEntity.setAssistances(new ArrayList<>());
        jpaEntity.setHistory(new ArrayList<>());

        when(jpaRepository.findById(testId)).thenReturn(Optional.of(jpaEntity));

        Optional<PolicyRequest> result = repository.findById(testId);

        assertTrue(result.isPresent());
        PolicyRequest domain = result.get();
        assertNotNull(domain.getCoverages());
        assertTrue(domain.getCoverages().isEmpty());
        assertNotNull(domain.getAssistances());
        assertTrue(domain.getAssistances().isEmpty());
    }

    @Test
    void testToJpaEntityConversion() {
        // Adicionar histórico ao domínio
        List<StatusHistory> domainHistory = new ArrayList<>();
        StatusHistory historyItem = new StatusHistory(testId, PolicyRequestStatus.RECEIVED, 
            LocalDateTime.of(2025, 1, 1, 10, 0), "Initial creation");
        domainHistory.add(historyItem);
        domainEntity.setHistory(domainHistory);

        when(jpaRepository.save(any(PolicyRequestJpaEntity.class))).thenReturn(jpaEntity);

        PolicyRequest result = repository.save(domainEntity);

        assertNotNull(result);
        verify(jpaRepository, times(1)).save(any(PolicyRequestJpaEntity.class));
    }

    @Test
    void testMultipleHistoryItemsConversion() {
        // Configurar múltiplos itens de histórico
        List<StatusHistoryJpaEntity> jpaHistory = new ArrayList<>();
        
        StatusHistoryJpaEntity history1 = StatusHistoryJpaEntity.builder()
                .policyRequestId(testId)
                .status("RECEIVED")
                .timestamp(LocalDateTime.of(2025, 1, 1, 10, 0))
                .reason("Initial creation")
                .build();
                
        StatusHistoryJpaEntity history2 = StatusHistoryJpaEntity.builder()
                .policyRequestId(testId)
                .status("VALIDATED")
                .timestamp(LocalDateTime.of(2025, 1, 1, 11, 0))
                .reason("Validation complete")
                .build();

        jpaHistory.add(history1);
        jpaHistory.add(history2);
        jpaEntity.setHistory(jpaHistory);

        when(jpaRepository.findByIdWithHistory(testId)).thenReturn(Optional.of(jpaEntity));

        Optional<PolicyRequest> result = repository.findByIdWithHistory(testId);

        assertTrue(result.isPresent());
        PolicyRequest domain = result.get();
        assertEquals(2, domain.getHistory().size());
        assertEquals(PolicyRequestStatus.RECEIVED, domain.getHistory().get(0).getStatus());
        assertEquals(PolicyRequestStatus.VALIDATED, domain.getHistory().get(1).getStatus());
    }

    @Test
    void testEnumConversions() {
        // Testar todas as conversões de enum
        jpaEntity.setCategory("VIDA");
        jpaEntity.setSalesChannel("TELEFONE");
        jpaEntity.setPaymentMethod("DEBIT_ACCOUNT");
        jpaEntity.setStatus("APPROVED");

        when(jpaRepository.findById(testId)).thenReturn(Optional.of(jpaEntity));

        Optional<PolicyRequest> result = repository.findById(testId);

        assertTrue(result.isPresent());
        PolicyRequest domain = result.get();
        assertEquals(InsuranceCategory.VIDA, domain.getCategory());
        assertEquals(SalesChannel.TELEFONE, domain.getSalesChannel());
        assertEquals(PaymentMethod.DEBIT_ACCOUNT, domain.getPaymentMethod());
        assertEquals(PolicyRequestStatus.APPROVED, domain.getStatus());
    }

    @Test
    void testComplexCoveragesConversion() {
        Map<String, BigDecimal> complexCoverages = new HashMap<>();
        complexCoverages.put("COMPREHENSIVE", new BigDecimal("30000.00"));
        complexCoverages.put("COLLISION", new BigDecimal("20000.00"));
        complexCoverages.put("LIABILITY", new BigDecimal("15000.00"));
        complexCoverages.put("PERSONAL_INJURY", new BigDecimal("10000.00"));
        
        jpaEntity.setCoverages(complexCoverages);

        when(jpaRepository.findById(testId)).thenReturn(Optional.of(jpaEntity));

        Optional<PolicyRequest> result = repository.findById(testId);

        assertTrue(result.isPresent());
        PolicyRequest domain = result.get();
        assertEquals(4, domain.getCoverages().size());
        assertEquals(new BigDecimal("30000.00"), domain.getCoverages().get("COMPREHENSIVE"));
        assertEquals(new BigDecimal("20000.00"), domain.getCoverages().get("COLLISION"));
        assertEquals(new BigDecimal("15000.00"), domain.getCoverages().get("LIABILITY"));
        assertEquals(new BigDecimal("10000.00"), domain.getCoverages().get("PERSONAL_INJURY"));
    }

    @Test
    void testMultipleAssistancesConversion() {
        List<String> multipleAssistances = Arrays.asList(
            "24h Roadside Assistance",
            "Rental Car Coverage", 
            "Emergency Medical",
            "Glass Repair",
            "Locksmith Service"
        );
        
        jpaEntity.setAssistances(multipleAssistances);

        when(jpaRepository.findById(testId)).thenReturn(Optional.of(jpaEntity));

        Optional<PolicyRequest> result = repository.findById(testId);

        assertTrue(result.isPresent());
        PolicyRequest domain = result.get();
        assertEquals(5, domain.getAssistances().size());
        assertTrue(domain.getAssistances().contains("24h Roadside Assistance"));
        assertTrue(domain.getAssistances().contains("Rental Car Coverage"));
        assertTrue(domain.getAssistances().contains("Emergency Medical"));
        assertTrue(domain.getAssistances().contains("Glass Repair"));
        assertTrue(domain.getAssistances().contains("Locksmith Service"));
    }
}