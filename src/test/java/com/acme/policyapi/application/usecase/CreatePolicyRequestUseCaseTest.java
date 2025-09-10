package com.acme.policyapi.application.usecase;

import com.acme.policyapi.application.dto.PolicyRequestCreateDTO;
import com.acme.policyapi.application.dto.PolicyRequestResponseDTO;
import com.acme.policyapi.application.service.impl.PolicyRequestMapper;
import com.acme.policyapi.domain.entity.*;
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
class CreatePolicyRequestUseCaseTest {

    @Mock
    private PolicyRequestRepository policyRequestRepository;

    @Mock
    private PolicyEventPublisher eventPublisher;

    @Mock
    private PolicyRequestMapper policyRequestMapper;

    @Mock
    private ProcessPolicyRequestUseCase processPolicyRequestUseCase;

    @InjectMocks
    private CreatePolicyRequestUseCase createPolicyRequestUseCase;

    private PolicyRequestCreateDTO createDTO;
    private PolicyRequest policyRequest;
    private PolicyRequestResponseDTO responseDTO;
    private UUID testCustomerId;
    private UUID testPolicyId;

    @BeforeEach
    void setUp() {
        testCustomerId = UUID.randomUUID();
        testPolicyId = UUID.randomUUID();
        
        setupCreateDTO();
        setupPolicyRequest();
        setupResponseDTO();
    }

    private void setupCreateDTO() {
        createDTO = new PolicyRequestCreateDTO();
        createDTO.setCustomerId(testCustomerId);
        createDTO.setProductId("PROD123");
        createDTO.setCategory(InsuranceCategory.AUTO);
        createDTO.setSalesChannel(SalesChannel.WEBSITE);
        createDTO.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        createDTO.setTotalMonthlyPremiumAmount(new BigDecimal("150.00"));
        createDTO.setInsuredAmount(new BigDecimal("50000.00"));
        
        Map<String, BigDecimal> coverages = new HashMap<>();
        coverages.put("COLLISION", new BigDecimal("25000.00"));
        createDTO.setCoverages(coverages);
        
        List<String> assistances = Arrays.asList("24h Assistance");
        createDTO.setAssistances(assistances);
    }

    private void setupPolicyRequest() {
        policyRequest = new PolicyRequest();
        policyRequest.setId(testPolicyId);
        policyRequest.setCustomerId(testCustomerId);
        policyRequest.setProductId("PROD123");
        policyRequest.setCategory(InsuranceCategory.AUTO);
        policyRequest.setSalesChannel(SalesChannel.WEBSITE);
        policyRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        policyRequest.setStatus(PolicyRequestStatus.RECEIVED);
        policyRequest.setCreatedAt(LocalDateTime.now());
        policyRequest.setTotalMonthlyPremiumAmount(new BigDecimal("150.00"));
        policyRequest.setInsuredAmount(new BigDecimal("50000.00"));
        
        Map<String, BigDecimal> coverages = new HashMap<>();
        coverages.put("COLLISION", new BigDecimal("25000.00"));
        policyRequest.setCoverages(coverages);
        
        List<String> assistances = Arrays.asList("24h Assistance");
        policyRequest.setAssistances(assistances);
        
        policyRequest.setHistory(new ArrayList<>());
    }

    private void setupResponseDTO() {
        responseDTO = new PolicyRequestResponseDTO();
        responseDTO.setId(testPolicyId);
        responseDTO.setCustomerId(testCustomerId);
        responseDTO.setProductId("PROD123");
        responseDTO.setCategory(InsuranceCategory.AUTO);
        responseDTO.setSalesChannel(SalesChannel.WEBSITE);
        responseDTO.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        responseDTO.setStatus(PolicyRequestStatus.RECEIVED);
        responseDTO.setTotalMonthlyPremiumAmount(new BigDecimal("150.00"));
        responseDTO.setInsuredAmount(new BigDecimal("50000.00"));
    }

    @Test
    void testExecuteSuccess() {
        // Arrange
        when(policyRequestMapper.toEntity(createDTO)).thenReturn(policyRequest);
        when(policyRequestRepository.save(policyRequest)).thenReturn(policyRequest);
        when(policyRequestMapper.toResponseDTO(policyRequest)).thenReturn(responseDTO);

        // Act
        PolicyRequestResponseDTO result = createPolicyRequestUseCase.execute(createDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testPolicyId, result.getId());
        assertEquals(testCustomerId, result.getCustomerId());
        assertEquals("PROD123", result.getProductId());
        assertEquals(InsuranceCategory.AUTO, result.getCategory());
        assertEquals(SalesChannel.WEBSITE, result.getSalesChannel());
        assertEquals(PaymentMethod.CREDIT_CARD, result.getPaymentMethod());
        assertEquals(PolicyRequestStatus.RECEIVED, result.getStatus());

        // Verify interactions
        verify(policyRequestMapper, times(1)).toEntity(createDTO);
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestCreated(policyRequest);
        verify(processPolicyRequestUseCase, times(1)).executeAsync(testPolicyId);
        verify(policyRequestMapper, times(1)).toResponseDTO(policyRequest);
    }

    @Test
    void testExecuteWithAllFields() {
        // Arrange - Create DTO with all optional fields
        // createDTO.setFinishedAt(LocalDateTime.now().plusDays(1)); // finishedAt não está disponível no CreateDTO
        
        Map<String, BigDecimal> completeCoverages = new HashMap<>();
        completeCoverages.put("COLLISION", new BigDecimal("25000.00"));
        completeCoverages.put("COMPREHENSIVE", new BigDecimal("30000.00"));
        completeCoverages.put("LIABILITY", new BigDecimal("15000.00"));
        createDTO.setCoverages(completeCoverages);
        
        List<String> completeAssistances = Arrays.asList(
            "24h Assistance", "Towing Service", "Emergency Repair"
        );
        createDTO.setAssistances(completeAssistances);

        // policyRequest.setFinishedAt(createDTO.getFinishedAt()); // finishedAt não está disponível no CreateDTO
        policyRequest.setCoverages(completeCoverages);
        policyRequest.setAssistances(completeAssistances);

        when(policyRequestMapper.toEntity(createDTO)).thenReturn(policyRequest);
        when(policyRequestRepository.save(policyRequest)).thenReturn(policyRequest);
        when(policyRequestMapper.toResponseDTO(policyRequest)).thenReturn(responseDTO);

        // Act
        PolicyRequestResponseDTO result = createPolicyRequestUseCase.execute(createDTO);

        // Assert
        assertNotNull(result);
        verify(policyRequestMapper, times(1)).toEntity(createDTO);
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestCreated(policyRequest);
        verify(processPolicyRequestUseCase, times(1)).executeAsync(testPolicyId);
        verify(policyRequestMapper, times(1)).toResponseDTO(policyRequest);
    }

    @Test
    void testExecuteWithDifferentInsuranceCategories() {
        // Test with VIDA category
        createDTO.setCategory(InsuranceCategory.VIDA);
        policyRequest.setCategory(InsuranceCategory.VIDA);

        when(policyRequestMapper.toEntity(createDTO)).thenReturn(policyRequest);
        when(policyRequestRepository.save(policyRequest)).thenReturn(policyRequest);
        when(policyRequestMapper.toResponseDTO(policyRequest)).thenReturn(responseDTO);

        PolicyRequestResponseDTO result = createPolicyRequestUseCase.execute(createDTO);

        assertNotNull(result);
        verify(policyRequestRepository, times(1)).save(policyRequest);
    }

    @Test
    void testExecuteWithDifferentSalesChannels() {
        // Test with TELEFONE channel
        createDTO.setSalesChannel(SalesChannel.TELEFONE);
        policyRequest.setSalesChannel(SalesChannel.TELEFONE);

        when(policyRequestMapper.toEntity(createDTO)).thenReturn(policyRequest);
        when(policyRequestRepository.save(policyRequest)).thenReturn(policyRequest);
        when(policyRequestMapper.toResponseDTO(policyRequest)).thenReturn(responseDTO);

        PolicyRequestResponseDTO result = createPolicyRequestUseCase.execute(createDTO);

        assertNotNull(result);
        verify(policyRequestRepository, times(1)).save(policyRequest);
    }

    @Test
    void testExecuteWithDifferentPaymentMethods() {
        // Test with PIX payment
        createDTO.setPaymentMethod(PaymentMethod.PIX);
        policyRequest.setPaymentMethod(PaymentMethod.PIX);

        when(policyRequestMapper.toEntity(createDTO)).thenReturn(policyRequest);
        when(policyRequestRepository.save(policyRequest)).thenReturn(policyRequest);
        when(policyRequestMapper.toResponseDTO(policyRequest)).thenReturn(responseDTO);

        PolicyRequestResponseDTO result = createPolicyRequestUseCase.execute(createDTO);

        assertNotNull(result);
        verify(policyRequestRepository, times(1)).save(policyRequest);
    }

    @Test
    void testExecuteWithEmptyCollections() {
        // Arrange - Empty coverages and assistances
        createDTO.setCoverages(new HashMap<>());
        createDTO.setAssistances(new ArrayList<>());
        
        policyRequest.setCoverages(new HashMap<>());
        policyRequest.setAssistances(new ArrayList<>());

        when(policyRequestMapper.toEntity(createDTO)).thenReturn(policyRequest);
        when(policyRequestRepository.save(policyRequest)).thenReturn(policyRequest);
        when(policyRequestMapper.toResponseDTO(policyRequest)).thenReturn(responseDTO);

        // Act
        PolicyRequestResponseDTO result = createPolicyRequestUseCase.execute(createDTO);

        // Assert
        assertNotNull(result);
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestCreated(policyRequest);
        verify(processPolicyRequestUseCase, times(1)).executeAsync(testPolicyId);
    }

    @Test
    void testExecuteWithLargeCoverageValues() {
        // Arrange - Large coverage values
        Map<String, BigDecimal> largeCoverages = new HashMap<>();
        largeCoverages.put("COLLISION", new BigDecimal("1000000.00"));
        largeCoverages.put("COMPREHENSIVE", new BigDecimal("2000000.00"));
        createDTO.setCoverages(largeCoverages);
        
        createDTO.setInsuredAmount(new BigDecimal("5000000.00"));
        createDTO.setTotalMonthlyPremiumAmount(new BigDecimal("2500.00"));
        
        policyRequest.setCoverages(largeCoverages);
        policyRequest.setInsuredAmount(new BigDecimal("5000000.00"));
        policyRequest.setTotalMonthlyPremiumAmount(new BigDecimal("2500.00"));

        when(policyRequestMapper.toEntity(createDTO)).thenReturn(policyRequest);
        when(policyRequestRepository.save(policyRequest)).thenReturn(policyRequest);
        when(policyRequestMapper.toResponseDTO(policyRequest)).thenReturn(responseDTO);

        // Act
        PolicyRequestResponseDTO result = createPolicyRequestUseCase.execute(createDTO);

        // Assert
        assertNotNull(result);
        verify(policyRequestRepository, times(1)).save(policyRequest);
    }

    @Test
    void testExecuteWithMultipleAssistances() {
        // Arrange - Multiple assistances
        List<String> multipleAssistances = Arrays.asList(
            "24h Roadside Assistance",
            "Towing Service", 
            "Emergency Repair",
            "Rental Car Coverage",
            "Glass Repair",
            "Locksmith Service"
        );
        createDTO.setAssistances(multipleAssistances);
        policyRequest.setAssistances(multipleAssistances);

        when(policyRequestMapper.toEntity(createDTO)).thenReturn(policyRequest);
        when(policyRequestRepository.save(policyRequest)).thenReturn(policyRequest);
        when(policyRequestMapper.toResponseDTO(policyRequest)).thenReturn(responseDTO);

        // Act
        PolicyRequestResponseDTO result = createPolicyRequestUseCase.execute(createDTO);

        // Assert
        assertNotNull(result);
        verify(policyRequestRepository, times(1)).save(policyRequest);
    }

    @Test
    void testExecuteEventPublishingOrder() {
        // Arrange
        when(policyRequestMapper.toEntity(createDTO)).thenReturn(policyRequest);
        when(policyRequestRepository.save(policyRequest)).thenReturn(policyRequest);
        when(policyRequestMapper.toResponseDTO(policyRequest)).thenReturn(responseDTO);

        // Act
        createPolicyRequestUseCase.execute(createDTO);

        // Assert - Verify order of operations
        var inOrder = inOrder(policyRequestMapper, policyRequestRepository, eventPublisher, processPolicyRequestUseCase);
        inOrder.verify(policyRequestMapper).toEntity(createDTO);
        inOrder.verify(policyRequestRepository).save(policyRequest);
        inOrder.verify(eventPublisher).publishPolicyRequestCreated(policyRequest);
        inOrder.verify(processPolicyRequestUseCase).executeAsync(testPolicyId);
        inOrder.verify(policyRequestMapper).toResponseDTO(policyRequest);
    }

    @Test
    void testExecuteLogging() {
        // Arrange
        when(policyRequestMapper.toEntity(createDTO)).thenReturn(policyRequest);
        when(policyRequestRepository.save(policyRequest)).thenReturn(policyRequest);
        when(policyRequestMapper.toResponseDTO(policyRequest)).thenReturn(responseDTO);

        // Act
        createPolicyRequestUseCase.execute(createDTO);

        // Assert - Verify that both logs would be called (we can't easily test log content without additional setup)
        verify(policyRequestMapper, times(1)).toEntity(createDTO);
        verify(policyRequestRepository, times(1)).save(policyRequest);
    }

    @Test
    void testExecuteWithNullId() {
        // Arrange - PolicyRequest with null ID initially, gets ID after save
        PolicyRequest unsavedPolicyRequest = new PolicyRequest();
        unsavedPolicyRequest.setCustomerId(testCustomerId);
        // ... other fields but no ID

        PolicyRequest savedPolicyRequest = new PolicyRequest();
        savedPolicyRequest.setId(testPolicyId); // ID assigned by repository
        savedPolicyRequest.setCustomerId(testCustomerId);
        // ... other fields

        when(policyRequestMapper.toEntity(createDTO)).thenReturn(unsavedPolicyRequest);
        when(policyRequestRepository.save(unsavedPolicyRequest)).thenReturn(savedPolicyRequest);
        when(policyRequestMapper.toResponseDTO(savedPolicyRequest)).thenReturn(responseDTO);

        // Act
        PolicyRequestResponseDTO result = createPolicyRequestUseCase.execute(createDTO);

        // Assert
        assertNotNull(result);
        verify(processPolicyRequestUseCase, times(1)).executeAsync(testPolicyId);
    }
}