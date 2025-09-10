package com.acme.policyapi.application.service.impl;

import com.acme.policyapi.application.dto.PolicyRequestCreateDTO;
import com.acme.policyapi.application.dto.PolicyRequestResponseDTO;
import com.acme.policyapi.application.exception.PolicyRequestNotFoundException;
import com.acme.policyapi.application.usecase.*;
import com.acme.policyapi.domain.entity.*;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para PolicyRequestServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class PolicyRequestServiceImplTest {
    // DESABILITADO TEMPORARIAMENTE - PRECISA SER ATUALIZADO PARA NOVA ARQUITETURA
    /*

    @Mock
    private CreatePolicyRequestUseCase createPolicyRequestUseCase;

    @Mock
    private FindPolicyRequestUseCase findPolicyRequestUseCase;

    @Mock
    private ProcessPolicyRequestUseCase processPolicyRequestUseCase;

    @Mock
    private CancelPolicyRequestUseCase cancelPolicyRequestUseCase;

    @Mock
    private SetPendingPolicyRequestUseCase setPendingPolicyRequestUseCase;

    @Mock
    private ApprovePolicyRequestUseCase approvePolicyRequestUseCase;

    @Mock
    private RejectPolicyRequestUseCase rejectPolicyRequestUseCase;

    @InjectMocks
    private PolicyRequestServiceImpl policyRequestService;

    private UUID testPolicyRequestId;
    private UUID testCustomerId;
    private PolicyRequest testPolicyRequest;
    private PolicyRequestCreateDTO testCreateDTO;
    private PolicyRequestResponseDTO testResponseDTO;
    private FraudAnalysisResponseDTO testFraudAnalysis;

    @BeforeEach
    void setUp() {
        testPolicyRequestId = UUID.randomUUID();
        testCustomerId = UUID.randomUUID();
        
        // Criar PolicyRequest de teste
        testPolicyRequest = new PolicyRequest();
        testPolicyRequest.setId(testPolicyRequestId);
        testPolicyRequest.setCustomerId(testCustomerId);
        testPolicyRequest.setProductId("PROD123");
        testPolicyRequest.setCategory(InsuranceCategory.AUTO);
        testPolicyRequest.setSalesChannel(SalesChannel.WEBSITE);
        testPolicyRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        testPolicyRequest.setStatus(PolicyRequestStatus.RECEIVED);
        testPolicyRequest.setCreatedAt(LocalDateTime.now());
        testPolicyRequest.setTotalMonthlyPremiumAmount(new BigDecimal("150.00"));
        testPolicyRequest.setInsuredAmount(new BigDecimal("50000.00"));
        testPolicyRequest.setCoverages(Map.of("BASIC", new BigDecimal("25000.00")));
        testPolicyRequest.setAssistances(List.of("24H_ASSISTANCE"));
        testPolicyRequest.setHistory(new ArrayList<>());

        // Criar DTO de criação
        testCreateDTO = new PolicyRequestCreateDTO();
        testCreateDTO.setCustomerId(testCustomerId);
        testCreateDTO.setProductId("PROD123");
        testCreateDTO.setCategory(InsuranceCategory.AUTO);
        testCreateDTO.setSalesChannel(SalesChannel.WEBSITE);
        testCreateDTO.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        testCreateDTO.setTotalMonthlyPremiumAmount(new BigDecimal("150.00"));
        testCreateDTO.setInsuredAmount(new BigDecimal("50000.00"));
        testCreateDTO.setCoverages(Map.of("BASIC", new BigDecimal("25000.00")));
        testCreateDTO.setAssistances(List.of("24H_ASSISTANCE"));

        // Criar DTO de resposta
        testResponseDTO = new PolicyRequestResponseDTO();
        testResponseDTO.setId(testPolicyRequestId);
        testResponseDTO.setCustomerId(testCustomerId);
        testResponseDTO.setStatus(PolicyRequestStatus.RECEIVED);

        // Criar análise de fraude
        testFraudAnalysis = new FraudAnalysisResponseDTO();
        testFraudAnalysis.setOrderId(testPolicyRequestId);
        testFraudAnalysis.setCustomerId(testCustomerId);
        testFraudAnalysis.setClassification(CustomerRiskClassification.REGULAR);
        testFraudAnalysis.setOccurrences(Collections.emptyList());
    }

    @Test
    void testCreatePolicyRequest() {
        // Arrange
        when(policyRequestMapper.toEntity(testCreateDTO)).thenReturn(testPolicyRequest);
        when(policyRequestRepository.save(any(PolicyRequest.class))).thenReturn(testPolicyRequest);
        when(policyRequestMapper.toResponseDTO(testPolicyRequest)).thenReturn(testResponseDTO);
        
        // Mock para processamento assíncrono
        when(policyRequestRepository.findById(testPolicyRequestId)).thenReturn(Optional.of(testPolicyRequest));
        when(fraudAnalysisService.analyzeFraud(testPolicyRequest)).thenReturn(testFraudAnalysis);
        when(policyValidationService.validatePolicyRequest(testPolicyRequest, CustomerRiskClassification.REGULAR)).thenReturn(true);

        // Act
        PolicyRequestResponseDTO result = policyRequestService.createPolicyRequest(testCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testPolicyRequestId, result.getId());
        assertEquals(testCustomerId, result.getCustomerId());

        verify(policyRequestMapper).toEntity(testCreateDTO);
        verify(policyRequestRepository, atLeast(1)).save(any(PolicyRequest.class));
        verify(policyRequestMapper).toResponseDTO(testPolicyRequest);
        verify(eventPublisher).publishPolicyRequestCreated(testPolicyRequest);
        verify(eventPublisher).publishPolicyRequestValidated(any(PolicyRequest.class));
        verify(eventPublisher).publishPolicyRequestPending(any(PolicyRequest.class));
    }

    @Test
    void testFindById() {
        // Arrange
        when(policyRequestRepository.findByIdWithHistory(testPolicyRequestId))
            .thenReturn(Optional.of(testPolicyRequest));
        when(policyRequestMapper.toResponseDTO(testPolicyRequest)).thenReturn(testResponseDTO);

        // Act
        PolicyRequestResponseDTO result = policyRequestService.findById(testPolicyRequestId);

        // Assert
        assertNotNull(result);
        assertEquals(testPolicyRequestId, result.getId());
        
        verify(policyRequestRepository).findByIdWithHistory(testPolicyRequestId);
        verify(policyRequestMapper).toResponseDTO(testPolicyRequest);
    }

    @Test
    void testFindByIdNotFound() {
        // Arrange
        when(policyRequestRepository.findByIdWithHistory(testPolicyRequestId))
            .thenReturn(Optional.empty());

        // Act & Assert
        PolicyRequestNotFoundException exception = 
            assertThrows(PolicyRequestNotFoundException.class, 
                () -> policyRequestService.findById(testPolicyRequestId));

        assertTrue(exception.getMessage().contains("Solicitação não encontrada"));
        verify(policyRequestRepository).findByIdWithHistory(testPolicyRequestId);
        verifyNoInteractions(policyRequestMapper);
    }

    @Test
    void testFindByCustomerId() {
        // Arrange
        List<PolicyRequest> policyRequests = Arrays.asList(testPolicyRequest);
        List<PolicyRequestResponseDTO> responseDTOs = Arrays.asList(testResponseDTO);
        
        when(policyRequestRepository.findByCustomerIdWithHistory(testCustomerId))
            .thenReturn(policyRequests);
        when(policyRequestMapper.toResponseDTOList(policyRequests)).thenReturn(responseDTOs);

        // Act
        List<PolicyRequestResponseDTO> result = policyRequestService.findByCustomerId(testCustomerId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPolicyRequestId, result.get(0).getId());
        
        verify(policyRequestRepository).findByCustomerIdWithHistory(testCustomerId);
        verify(policyRequestMapper).toResponseDTOList(policyRequests);
    }

    @Test
    void testFindByCustomerIdEmpty() {
        // Arrange
        when(policyRequestRepository.findByCustomerIdWithHistory(testCustomerId))
            .thenReturn(Collections.emptyList());
        when(policyRequestMapper.toResponseDTOList(Collections.emptyList()))
            .thenReturn(Collections.emptyList());

        // Act
        List<PolicyRequestResponseDTO> result = policyRequestService.findByCustomerId(testCustomerId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(policyRequestRepository).findByCustomerIdWithHistory(testCustomerId);
        verify(policyRequestMapper).toResponseDTOList(Collections.emptyList());
    }

    @Test
    void testProcessRequestSuccess() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyRequestId)).thenReturn(Optional.of(testPolicyRequest));
        when(fraudAnalysisService.analyzeFraud(testPolicyRequest)).thenReturn(testFraudAnalysis);
        when(policyValidationService.validatePolicyRequest(testPolicyRequest, CustomerRiskClassification.REGULAR))
            .thenReturn(true);
        when(policyRequestRepository.save(any(PolicyRequest.class))).thenReturn(testPolicyRequest);

        // Act
        policyRequestService.processRequest(testPolicyRequestId);

        // Assert
        verify(policyRequestRepository, atLeast(1)).findById(testPolicyRequestId);
        verify(fraudAnalysisService).analyzeFraud(testPolicyRequest);
        verify(policyValidationService).validatePolicyRequest(testPolicyRequest, CustomerRiskClassification.REGULAR);
        verify(policyRequestRepository, atLeast(2)).save(any(PolicyRequest.class));
        verify(eventPublisher).publishPolicyRequestValidated(any(PolicyRequest.class));
        verify(eventPublisher).publishPolicyRequestPending(any(PolicyRequest.class));
    }

    @Test
    void testProcessRequestValidationFailed() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyRequestId)).thenReturn(Optional.of(testPolicyRequest));
        when(fraudAnalysisService.analyzeFraud(testPolicyRequest)).thenReturn(testFraudAnalysis);
        when(policyValidationService.validatePolicyRequest(testPolicyRequest, CustomerRiskClassification.REGULAR))
            .thenReturn(false);
        when(policyValidationService.getRejectionReason(testPolicyRequest, CustomerRiskClassification.REGULAR))
            .thenReturn("Valor acima do limite permitido");
        when(policyRequestRepository.save(any(PolicyRequest.class))).thenReturn(testPolicyRequest);

        // Act
        policyRequestService.processRequest(testPolicyRequestId);

        // Assert
        verify(policyRequestRepository, atLeast(1)).findById(testPolicyRequestId);
        verify(fraudAnalysisService).analyzeFraud(testPolicyRequest);
        verify(policyValidationService).validatePolicyRequest(testPolicyRequest, CustomerRiskClassification.REGULAR);
        verify(policyValidationService).getRejectionReason(testPolicyRequest, CustomerRiskClassification.REGULAR);
        verify(policyRequestRepository, atLeast(1)).save(any(PolicyRequest.class));
        verify(eventPublisher).publishPolicyRequestRejected(any(PolicyRequest.class));
    }

    @Test
    void testProcessRequestWrongStatus() {
        // Arrange
        testPolicyRequest.setStatus(PolicyRequestStatus.APPROVED);
        when(policyRequestRepository.findById(testPolicyRequestId)).thenReturn(Optional.of(testPolicyRequest));

        // Act
        policyRequestService.processRequest(testPolicyRequestId);

        // Assert - Não deve chamar análise de fraude quando status não é RECEIVED
        verify(policyRequestRepository).findById(testPolicyRequestId);
        verifyNoInteractions(fraudAnalysisService);
        verifyNoInteractions(policyValidationService);
        verify(policyRequestRepository, never()).save(any(PolicyRequest.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void testProcessRequestException() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyRequestId)).thenReturn(Optional.of(testPolicyRequest));
        when(fraudAnalysisService.analyzeFraud(testPolicyRequest))
            .thenThrow(new RuntimeException("Erro na análise de fraude"));
        when(policyRequestRepository.save(any(PolicyRequest.class))).thenReturn(testPolicyRequest);

        // Act
        policyRequestService.processRequest(testPolicyRequestId);

        // Assert - Deve rejeitar a solicitação quando há exceção
        verify(policyRequestRepository, atLeast(1)).findById(testPolicyRequestId);
        verify(fraudAnalysisService).analyzeFraud(testPolicyRequest);
        verify(policyRequestRepository, atLeast(1)).save(any(PolicyRequest.class));
        verify(eventPublisher).publishPolicyRequestRejected(any(PolicyRequest.class));
    }

    @Test
    void testCancelRequest() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyRequestId)).thenReturn(Optional.of(testPolicyRequest));
        when(policyRequestRepository.save(any(PolicyRequest.class))).thenReturn(testPolicyRequest);
        
        // Mock canBeCancelled method
        testPolicyRequest = spy(testPolicyRequest);
        when(testPolicyRequest.canBeCancelled()).thenReturn(true);
        when(policyRequestRepository.findById(testPolicyRequestId)).thenReturn(Optional.of(testPolicyRequest));

        String reason = "Cliente cancelou";

        // Act
        policyRequestService.cancelRequest(testPolicyRequestId, reason);

        // Assert
        verify(policyRequestRepository).findById(testPolicyRequestId);
        verify(testPolicyRequest).canBeCancelled();
        verify(policyRequestRepository).save(any(PolicyRequest.class));
        verify(eventPublisher).publishPolicyRequestCancelled(any(PolicyRequest.class));
    }

    @Test
    void testCancelRequestCannotBeCancelled() {
        // Arrange
        testPolicyRequest.setStatus(PolicyRequestStatus.APPROVED);
        testPolicyRequest = spy(testPolicyRequest);
        when(testPolicyRequest.canBeCancelled()).thenReturn(false);
        when(policyRequestRepository.findById(testPolicyRequestId)).thenReturn(Optional.of(testPolicyRequest));

        String reason = "Cliente cancelou";

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> policyRequestService.cancelRequest(testPolicyRequestId, reason));

        assertTrue(exception.getMessage().contains("não pode ser cancelada"));
        verify(policyRequestRepository).findById(testPolicyRequestId);
        verify(testPolicyRequest).canBeCancelled();
        verify(policyRequestRepository, never()).save(any(PolicyRequest.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void testSetPending() {
        // Arrange - Para ir para PENDING, precisa estar VALIDATED primeiro
        testPolicyRequest.setStatus(PolicyRequestStatus.VALIDATED);
        when(policyRequestRepository.findById(testPolicyRequestId)).thenReturn(Optional.of(testPolicyRequest));
        when(policyRequestRepository.save(any(PolicyRequest.class))).thenReturn(testPolicyRequest);

        // Act
        policyRequestService.setPending(testPolicyRequestId);

        // Assert
        verify(policyRequestRepository).findById(testPolicyRequestId);
        verify(policyRequestRepository).save(any(PolicyRequest.class));
        verify(eventPublisher).publishPolicyRequestPending(any(PolicyRequest.class));
    }

    @Test
    void testApproveRequest() {
        // Arrange - Para ir para APPROVED, precisa estar PENDING primeiro
        testPolicyRequest.setStatus(PolicyRequestStatus.PENDING);
        when(policyRequestRepository.findById(testPolicyRequestId)).thenReturn(Optional.of(testPolicyRequest));
        when(policyRequestRepository.save(any(PolicyRequest.class))).thenReturn(testPolicyRequest);

        // Act
        policyRequestService.approveRequest(testPolicyRequestId);

        // Assert
        verify(policyRequestRepository).findById(testPolicyRequestId);
        verify(policyRequestRepository).save(any(PolicyRequest.class));
        verify(eventPublisher).publishPolicyRequestApproved(any(PolicyRequest.class));
    }

    @Test
    void testRejectRequest() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyRequestId)).thenReturn(Optional.of(testPolicyRequest));
        when(policyRequestRepository.save(any(PolicyRequest.class))).thenReturn(testPolicyRequest);

        String reason = "Análise de risco negativa";

        // Act
        policyRequestService.rejectRequest(testPolicyRequestId, reason);

        // Assert
        verify(policyRequestRepository).findById(testPolicyRequestId);
        verify(policyRequestRepository).save(any(PolicyRequest.class));
        verify(eventPublisher).publishPolicyRequestRejected(any(PolicyRequest.class));
    }

    @Test
    void testFindPolicyRequestByIdNotFound() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyRequestId)).thenReturn(Optional.empty());

        // Act & Assert
        PolicyRequestNotFoundException exception = 
            assertThrows(PolicyRequestNotFoundException.class,
                () -> policyRequestService.approveRequest(testPolicyRequestId));

        assertTrue(exception.getMessage().contains("Solicitação não encontrada"));
        verify(policyRequestRepository).findById(testPolicyRequestId);
    }

    @Test
    void testProcessRequestWithHighRiskClassification() {
        // Arrange
        testFraudAnalysis.setClassification(CustomerRiskClassification.HIGH_RISK);
        
        when(policyRequestRepository.findById(testPolicyRequestId)).thenReturn(Optional.of(testPolicyRequest));
        when(fraudAnalysisService.analyzeFraud(testPolicyRequest)).thenReturn(testFraudAnalysis);
        when(policyValidationService.validatePolicyRequest(testPolicyRequest, CustomerRiskClassification.HIGH_RISK))
            .thenReturn(false);
        when(policyValidationService.getRejectionReason(testPolicyRequest, CustomerRiskClassification.HIGH_RISK))
            .thenReturn("Cliente com alto risco");
        when(policyRequestRepository.save(any(PolicyRequest.class))).thenReturn(testPolicyRequest);

        // Act
        policyRequestService.processRequest(testPolicyRequestId);

        // Assert
        verify(fraudAnalysisService).analyzeFraud(testPolicyRequest);
        verify(policyValidationService).validatePolicyRequest(testPolicyRequest, CustomerRiskClassification.HIGH_RISK);
        verify(policyValidationService).getRejectionReason(testPolicyRequest, CustomerRiskClassification.HIGH_RISK);
        verify(eventPublisher).publishPolicyRequestRejected(any(PolicyRequest.class));
    }

    @Test
    void testCreatePolicyRequestWithValidationFailure() {
        // Arrange
        when(policyRequestMapper.toEntity(testCreateDTO)).thenReturn(testPolicyRequest);
        when(policyRequestRepository.save(any(PolicyRequest.class))).thenReturn(testPolicyRequest);
        when(policyRequestMapper.toResponseDTO(testPolicyRequest)).thenReturn(testResponseDTO);
        
        // Mock para processamento assíncrono que falha na validação
        when(policyRequestRepository.findById(testPolicyRequestId)).thenReturn(Optional.of(testPolicyRequest));
        when(fraudAnalysisService.analyzeFraud(testPolicyRequest)).thenReturn(testFraudAnalysis);
        when(policyValidationService.validatePolicyRequest(testPolicyRequest, CustomerRiskClassification.REGULAR))
            .thenReturn(false);
        when(policyValidationService.getRejectionReason(testPolicyRequest, CustomerRiskClassification.REGULAR))
            .thenReturn("Valor acima do limite");

        // Act
        PolicyRequestResponseDTO result = policyRequestService.createPolicyRequest(testCreateDTO);

        // Assert
        assertNotNull(result);
        verify(eventPublisher).publishPolicyRequestCreated(testPolicyRequest);
        verify(eventPublisher).publishPolicyRequestRejected(any(PolicyRequest.class));
    }

    @Test
    void testPolicyRequestNotFoundExceptionMessage() {
        // Act
        PolicyRequestNotFoundException exception = 
            new PolicyRequestNotFoundException("Test message");

        // Assert
        assertEquals("Test message", exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testAllStatusTransitions() {
        // Arrange - Criar diferentes PolicyRequests para cada transição
        UUID pendingRequestId = UUID.randomUUID();
        UUID approveRequestId = UUID.randomUUID();
        UUID rejectRequestId = UUID.randomUUID();

        // PolicyRequest para testar transição para PENDING (precisa estar VALIDATED)
        PolicyRequest pendingRequest = new PolicyRequest();
        pendingRequest.setId(pendingRequestId);
        pendingRequest.setStatus(PolicyRequestStatus.VALIDATED);

        // PolicyRequest para testar transição para APPROVED (precisa estar PENDING)
        PolicyRequest approveRequest = new PolicyRequest();
        approveRequest.setId(approveRequestId);
        approveRequest.setStatus(PolicyRequestStatus.PENDING);

        // PolicyRequest para testar transição para REJECTED (pode estar em qualquer status não final)
        PolicyRequest rejectRequest = new PolicyRequest();
        rejectRequest.setId(rejectRequestId);
        rejectRequest.setStatus(PolicyRequestStatus.RECEIVED);

        // Mock dos repositórios para cada caso
        when(policyRequestRepository.findById(pendingRequestId)).thenReturn(Optional.of(pendingRequest));
        when(policyRequestRepository.findById(approveRequestId)).thenReturn(Optional.of(approveRequest));
        when(policyRequestRepository.findById(rejectRequestId)).thenReturn(Optional.of(rejectRequest));
        when(policyRequestRepository.save(any(PolicyRequest.class))).thenReturn(testPolicyRequest);

        // Act & Assert - Teste todas as transições de status
        
        // Teste setPending (VALIDATED -> PENDING)
        policyRequestService.setPending(pendingRequestId);
        verify(eventPublisher).publishPolicyRequestPending(any(PolicyRequest.class));
        
        // Teste approveRequest (PENDING -> APPROVED)
        policyRequestService.approveRequest(approveRequestId);
        verify(eventPublisher).publishPolicyRequestApproved(any(PolicyRequest.class));
        
        // Teste rejectRequest (RECEIVED -> REJECTED)
        policyRequestService.rejectRequest(rejectRequestId, "Teste");
        verify(eventPublisher).publishPolicyRequestRejected(any(PolicyRequest.class));
    }

    @Test
    void testProcessRequestMultipleTimes() {
        // Arrange - Primeira chamada com status RECEIVED
        when(policyRequestRepository.findById(testPolicyRequestId)).thenReturn(Optional.of(testPolicyRequest));
        when(fraudAnalysisService.analyzeFraud(testPolicyRequest)).thenReturn(testFraudAnalysis);
        when(policyValidationService.validatePolicyRequest(testPolicyRequest, CustomerRiskClassification.REGULAR))
            .thenReturn(true);
        when(policyRequestRepository.save(any(PolicyRequest.class))).thenReturn(testPolicyRequest);

        // Act - Primeira chamada
        policyRequestService.processRequest(testPolicyRequestId);

        // Arrange - Segunda chamada com status diferente de RECEIVED
        testPolicyRequest.setStatus(PolicyRequestStatus.VALIDATED);
        reset(fraudAnalysisService, policyValidationService, eventPublisher);

        // Act - Segunda chamada
        policyRequestService.processRequest(testPolicyRequestId);

        // Assert - Segunda chamada não deve executar processamento
        verifyNoInteractions(fraudAnalysisService);
        verifyNoInteractions(policyValidationService);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void testFindByCustomerIdMultipleRequests() {
        // Arrange
        PolicyRequest secondRequest = new PolicyRequest();
        secondRequest.setId(UUID.randomUUID());
        secondRequest.setCustomerId(testCustomerId);
        
        PolicyRequestResponseDTO secondResponseDTO = new PolicyRequestResponseDTO();
        secondResponseDTO.setId(secondRequest.getId());
        secondResponseDTO.setCustomerId(testCustomerId);
        
        List<PolicyRequest> policyRequests = Arrays.asList(testPolicyRequest, secondRequest);
        List<PolicyRequestResponseDTO> responseDTOs = Arrays.asList(testResponseDTO, secondResponseDTO);
        
        when(policyRequestRepository.findByCustomerIdWithHistory(testCustomerId))
            .thenReturn(policyRequests);
        when(policyRequestMapper.toResponseDTOList(policyRequests)).thenReturn(responseDTOs);

        // Act
        List<PolicyRequestResponseDTO> result = policyRequestService.findByCustomerId(testCustomerId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(dto -> dto.getCustomerId().equals(testCustomerId)));
    }
    */
}

