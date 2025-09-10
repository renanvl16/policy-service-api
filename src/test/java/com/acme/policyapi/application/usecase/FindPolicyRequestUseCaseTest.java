package com.acme.policyapi.application.usecase;

import com.acme.policyapi.application.dto.PolicyRequestResponseDTO;
import com.acme.policyapi.application.dto.StatusHistoryDTO;
import com.acme.policyapi.application.exception.PolicyRequestNotFoundException;
import com.acme.policyapi.application.service.impl.PolicyRequestMapper;
import com.acme.policyapi.domain.entity.*;
import com.acme.policyapi.domain.repository.PolicyRequestRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindPolicyRequestUseCaseTest {

    @Mock
    private PolicyRequestRepository policyRequestRepository;

    @Mock
    private PolicyRequestMapper policyRequestMapper;

    @InjectMocks
    private FindPolicyRequestUseCase findPolicyRequestUseCase;

    private UUID testPolicyId;
    private UUID testCustomerId;
    private PolicyRequest policyRequest;
    private PolicyRequestResponseDTO responseDTO;
    private List<PolicyRequest> policyRequestList;
    private List<PolicyRequestResponseDTO> responseDTOList;

    @BeforeEach
    void setUp() {
        testPolicyId = UUID.randomUUID();
        testCustomerId = UUID.randomUUID();
        
        setupPolicyRequest();
        setupResponseDTO();
        setupLists();
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
        policyRequest.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        policyRequest.setTotalMonthlyPremiumAmount(new BigDecimal("150.00"));
        policyRequest.setInsuredAmount(new BigDecimal("50000.00"));

        // Setup collections
        Map<String, BigDecimal> coverages = new HashMap<>();
        coverages.put("COLLISION", new BigDecimal("25000.00"));
        policyRequest.setCoverages(coverages);

        List<String> assistances = Arrays.asList("24h Assistance");
        policyRequest.setAssistances(assistances);

        // Setup history
        List<StatusHistory> history = new ArrayList<>();
        StatusHistory historyItem = new StatusHistory(
            testPolicyId, 
            PolicyRequestStatus.RECEIVED, 
            LocalDateTime.of(2025, 1, 1, 10, 0), 
            "Initial creation"
        );
        history.add(historyItem);
        policyRequest.setHistory(history);
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
        responseDTO.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        responseDTO.setTotalMonthlyPremiumAmount(new BigDecimal("150.00"));
        responseDTO.setInsuredAmount(new BigDecimal("50000.00"));

        // Setup history DTO
        List<StatusHistoryDTO> historyDTOs = new ArrayList<>();
        StatusHistoryDTO historyDTO = new StatusHistoryDTO();
        historyDTO.setStatus(PolicyRequestStatus.RECEIVED);
        historyDTO.setTimestamp(LocalDateTime.of(2025, 1, 1, 10, 0));
        historyDTO.setReason("Initial creation");
        historyDTOs.add(historyDTO);
        responseDTO.setHistory(historyDTOs);
    }

    private void setupLists() {
        policyRequestList = Arrays.asList(policyRequest);
        responseDTOList = Arrays.asList(responseDTO);
    }

    @Test
    void testFindByIdSuccess() {
        // Arrange
        when(policyRequestRepository.findByIdWithHistory(testPolicyId))
                .thenReturn(Optional.of(policyRequest));
        when(policyRequestMapper.toResponseDTO(policyRequest))
                .thenReturn(responseDTO);

        // Act
        PolicyRequestResponseDTO result = findPolicyRequestUseCase.findById(testPolicyId);

        // Assert
        assertNotNull(result);
        assertEquals(testPolicyId, result.getId());
        assertEquals(testCustomerId, result.getCustomerId());
        assertEquals("PROD123", result.getProductId());
        assertEquals(InsuranceCategory.AUTO, result.getCategory());
        assertEquals(SalesChannel.WEBSITE, result.getSalesChannel());
        assertEquals(PaymentMethod.CREDIT_CARD, result.getPaymentMethod());
        assertEquals(PolicyRequestStatus.RECEIVED, result.getStatus());
        assertEquals(new BigDecimal("150.00"), result.getTotalMonthlyPremiumAmount());
        assertEquals(new BigDecimal("50000.00"), result.getInsuredAmount());
        assertNotNull(result.getHistory());
        assertEquals(1, result.getHistory().size());

        verify(policyRequestRepository, times(1)).findByIdWithHistory(testPolicyId);
        verify(policyRequestMapper, times(1)).toResponseDTO(policyRequest);
    }

    @Test
    void testFindByIdNotFound() {
        // Arrange
        when(policyRequestRepository.findByIdWithHistory(testPolicyId))
                .thenReturn(Optional.empty());

        // Act & Assert
        PolicyRequestNotFoundException exception = assertThrows(
            PolicyRequestNotFoundException.class,
            () -> findPolicyRequestUseCase.findById(testPolicyId)
        );

        assertEquals("Solicitação não encontrada: " + testPolicyId, exception.getMessage());
        verify(policyRequestRepository, times(1)).findByIdWithHistory(testPolicyId);
        verify(policyRequestMapper, never()).toResponseDTO(any());
    }

    @Test
    void testFindByIdWithCompleteHistory() {
        // Arrange - Multiple history items
        List<StatusHistory> completeHistory = new ArrayList<>();
        completeHistory.add(new StatusHistory(testPolicyId, PolicyRequestStatus.RECEIVED, 
            LocalDateTime.of(2025, 1, 1, 10, 0), "Initial creation"));
        completeHistory.add(new StatusHistory(testPolicyId, PolicyRequestStatus.VALIDATED, 
            LocalDateTime.of(2025, 1, 1, 11, 0), "Validation complete"));
        completeHistory.add(new StatusHistory(testPolicyId, PolicyRequestStatus.PENDING, 
            LocalDateTime.of(2025, 1, 1, 12, 0), "Waiting for payment"));

        policyRequest.setHistory(completeHistory);

        when(policyRequestRepository.findByIdWithHistory(testPolicyId))
                .thenReturn(Optional.of(policyRequest));
        when(policyRequestMapper.toResponseDTO(policyRequest))
                .thenReturn(responseDTO);

        // Act
        PolicyRequestResponseDTO result = findPolicyRequestUseCase.findById(testPolicyId);

        // Assert
        assertNotNull(result);
        verify(policyRequestRepository, times(1)).findByIdWithHistory(testPolicyId);
        verify(policyRequestMapper, times(1)).toResponseDTO(policyRequest);
    }

    @Test
    void testFindByIdWithDifferentStatuses() {
        // Test with APPROVED status
        policyRequest.setStatus(PolicyRequestStatus.APPROVED);
        policyRequest.setFinishedAt(LocalDateTime.of(2025, 1, 2, 15, 30));

        responseDTO.setStatus(PolicyRequestStatus.APPROVED);
        responseDTO.setFinishedAt(LocalDateTime.of(2025, 1, 2, 15, 30));

        when(policyRequestRepository.findByIdWithHistory(testPolicyId))
                .thenReturn(Optional.of(policyRequest));
        when(policyRequestMapper.toResponseDTO(policyRequest))
                .thenReturn(responseDTO);

        PolicyRequestResponseDTO result = findPolicyRequestUseCase.findById(testPolicyId);

        assertNotNull(result);
        assertEquals(PolicyRequestStatus.APPROVED, result.getStatus());
    }

    @Test
    void testFindByCustomerIdSuccess() {
        // Arrange
        when(policyRequestRepository.findByCustomerIdWithHistory(testCustomerId))
                .thenReturn(policyRequestList);
        when(policyRequestMapper.toResponseDTOList(policyRequestList))
                .thenReturn(responseDTOList);

        // Act
        List<PolicyRequestResponseDTO> result = findPolicyRequestUseCase.findByCustomerId(testCustomerId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        PolicyRequestResponseDTO resultDTO = result.get(0);
        assertEquals(testPolicyId, resultDTO.getId());
        assertEquals(testCustomerId, resultDTO.getCustomerId());
        assertEquals("PROD123", resultDTO.getProductId());

        verify(policyRequestRepository, times(1)).findByCustomerIdWithHistory(testCustomerId);
        verify(policyRequestMapper, times(1)).toResponseDTOList(policyRequestList);
    }

    @Test
    void testFindByCustomerIdEmpty() {
        // Arrange
        List<PolicyRequest> emptyList = Collections.emptyList();
        List<PolicyRequestResponseDTO> emptyDTOList = Collections.emptyList();

        when(policyRequestRepository.findByCustomerIdWithHistory(testCustomerId))
                .thenReturn(emptyList);
        when(policyRequestMapper.toResponseDTOList(emptyList))
                .thenReturn(emptyDTOList);

        // Act
        List<PolicyRequestResponseDTO> result = findPolicyRequestUseCase.findByCustomerId(testCustomerId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(policyRequestRepository, times(1)).findByCustomerIdWithHistory(testCustomerId);
        verify(policyRequestMapper, times(1)).toResponseDTOList(emptyList);
    }

    @Test
    void testFindByCustomerIdMultipleRequests() {
        // Arrange - Multiple policy requests
        UUID secondPolicyId = UUID.randomUUID();
        PolicyRequest secondPolicyRequest = new PolicyRequest();
        secondPolicyRequest.setId(secondPolicyId);
        secondPolicyRequest.setCustomerId(testCustomerId);
        secondPolicyRequest.setProductId("PROD456");
        secondPolicyRequest.setCategory(InsuranceCategory.VIDA);
        secondPolicyRequest.setSalesChannel(SalesChannel.TELEFONE);
        secondPolicyRequest.setPaymentMethod(PaymentMethod.PIX);
        secondPolicyRequest.setStatus(PolicyRequestStatus.APPROVED);
        secondPolicyRequest.setCreatedAt(LocalDateTime.of(2025, 1, 2, 14, 0));
        secondPolicyRequest.setTotalMonthlyPremiumAmount(new BigDecimal("200.00"));
        secondPolicyRequest.setInsuredAmount(new BigDecimal("75000.00"));
        secondPolicyRequest.setHistory(new ArrayList<>());

        List<PolicyRequest> multipleRequests = Arrays.asList(policyRequest, secondPolicyRequest);

        PolicyRequestResponseDTO secondResponseDTO = new PolicyRequestResponseDTO();
        secondResponseDTO.setId(secondPolicyId);
        secondResponseDTO.setCustomerId(testCustomerId);
        secondResponseDTO.setProductId("PROD456");
        secondResponseDTO.setCategory(InsuranceCategory.VIDA);

        List<PolicyRequestResponseDTO> multipleResponseDTOs = Arrays.asList(responseDTO, secondResponseDTO);

        when(policyRequestRepository.findByCustomerIdWithHistory(testCustomerId))
                .thenReturn(multipleRequests);
        when(policyRequestMapper.toResponseDTOList(multipleRequests))
                .thenReturn(multipleResponseDTOs);

        // Act
        List<PolicyRequestResponseDTO> result = findPolicyRequestUseCase.findByCustomerId(testCustomerId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify first request
        assertEquals(testPolicyId, result.get(0).getId());
        assertEquals(testCustomerId, result.get(0).getCustomerId());
        assertEquals("PROD123", result.get(0).getProductId());
        
        // Verify second request
        assertEquals(secondPolicyId, result.get(1).getId());
        assertEquals(testCustomerId, result.get(1).getCustomerId());
        assertEquals("PROD456", result.get(1).getProductId());

        verify(policyRequestRepository, times(1)).findByCustomerIdWithHistory(testCustomerId);
        verify(policyRequestMapper, times(1)).toResponseDTOList(multipleRequests);
    }

    @Test
    void testFindByCustomerIdWithDifferentCategories() {
        // Arrange - Policy request with RESIDENCIAL category
        policyRequest.setCategory(InsuranceCategory.RESIDENCIAL);
        responseDTO.setCategory(InsuranceCategory.RESIDENCIAL);

        when(policyRequestRepository.findByCustomerIdWithHistory(testCustomerId))
                .thenReturn(policyRequestList);
        when(policyRequestMapper.toResponseDTOList(policyRequestList))
                .thenReturn(responseDTOList);

        // Act
        List<PolicyRequestResponseDTO> result = findPolicyRequestUseCase.findByCustomerId(testCustomerId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(policyRequestRepository, times(1)).findByCustomerIdWithHistory(testCustomerId);
    }

    @Test
    void testFindByCustomerIdWithDifferentPaymentMethods() {
        // Arrange - Policy request with BOLETO payment
        policyRequest.setPaymentMethod(PaymentMethod.BOLETO);
        responseDTO.setPaymentMethod(PaymentMethod.BOLETO);

        when(policyRequestRepository.findByCustomerIdWithHistory(testCustomerId))
                .thenReturn(policyRequestList);
        when(policyRequestMapper.toResponseDTOList(policyRequestList))
                .thenReturn(responseDTOList);

        // Act
        List<PolicyRequestResponseDTO> result = findPolicyRequestUseCase.findByCustomerId(testCustomerId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(policyRequestRepository, times(1)).findByCustomerIdWithHistory(testCustomerId);
    }

    @Test
    void testFindByIdWithComplexCoverages() {
        // Arrange - Complex coverages
        Map<String, BigDecimal> complexCoverages = new HashMap<>();
        complexCoverages.put("COLLISION", new BigDecimal("25000.00"));
        complexCoverages.put("COMPREHENSIVE", new BigDecimal("30000.00"));
        complexCoverages.put("LIABILITY", new BigDecimal("15000.00"));
        complexCoverages.put("PERSONAL_INJURY", new BigDecimal("10000.00"));
        
        policyRequest.setCoverages(complexCoverages);

        when(policyRequestRepository.findByIdWithHistory(testPolicyId))
                .thenReturn(Optional.of(policyRequest));
        when(policyRequestMapper.toResponseDTO(policyRequest))
                .thenReturn(responseDTO);

        // Act
        PolicyRequestResponseDTO result = findPolicyRequestUseCase.findById(testPolicyId);

        // Assert
        assertNotNull(result);
        verify(policyRequestMapper, times(1)).toResponseDTO(policyRequest);
    }

    @Test
    void testFindByIdLogging() {
        // Arrange
        when(policyRequestRepository.findByIdWithHistory(testPolicyId))
                .thenReturn(Optional.of(policyRequest));
        when(policyRequestMapper.toResponseDTO(policyRequest))
                .thenReturn(responseDTO);

        // Act
        findPolicyRequestUseCase.findById(testPolicyId);

        // Assert - Verify repository interaction (log content testing would need additional setup)
        verify(policyRequestRepository, times(1)).findByIdWithHistory(testPolicyId);
    }

    @Test
    void testFindByCustomerIdLogging() {
        // Arrange
        when(policyRequestRepository.findByCustomerIdWithHistory(testCustomerId))
                .thenReturn(policyRequestList);
        when(policyRequestMapper.toResponseDTOList(policyRequestList))
                .thenReturn(responseDTOList);

        // Act
        findPolicyRequestUseCase.findByCustomerId(testCustomerId);

        // Assert - Verify repository interaction (log content testing would need additional setup)
        verify(policyRequestRepository, times(1)).findByCustomerIdWithHistory(testCustomerId);
    }

    @Test
    void testFindByIdWithAllSalesChannels() {
        // Test each sales channel
        SalesChannel[] channels = {SalesChannel.WEBSITE, SalesChannel.MOBILE, SalesChannel.WHATSAPP, 
                                   SalesChannel.PRESENCIAL, SalesChannel.TELEFONE};
        
        for (SalesChannel channel : channels) {
            policyRequest.setSalesChannel(channel);
            responseDTO.setSalesChannel(channel);
            
            when(policyRequestRepository.findByIdWithHistory(testPolicyId))
                    .thenReturn(Optional.of(policyRequest));
            when(policyRequestMapper.toResponseDTO(policyRequest))
                    .thenReturn(responseDTO);

            PolicyRequestResponseDTO result = findPolicyRequestUseCase.findById(testPolicyId);
            assertNotNull(result);
        }

        verify(policyRequestRepository, times(5)).findByIdWithHistory(testPolicyId);
    }
}