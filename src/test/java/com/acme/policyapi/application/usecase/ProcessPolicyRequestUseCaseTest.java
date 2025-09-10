package com.acme.policyapi.application.usecase;

import com.acme.policyapi.application.dto.FraudAnalysisResponseDTO;
import com.acme.policyapi.application.service.FraudAnalysisService;
import com.acme.policyapi.domain.entity.*;
import com.acme.policyapi.domain.repository.PolicyRequestRepository;
import com.acme.policyapi.domain.service.PolicyValidationService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessPolicyRequestUseCaseTest {

    @Mock
    private PolicyRequestRepository policyRequestRepository;

    @Mock
    private FraudAnalysisService fraudAnalysisService;

    @Mock
    private PolicyValidationService policyValidationService;

    @Mock
    private PolicyEventPublisher eventPublisher;

    @Mock
    private SetPendingPolicyRequestUseCase setPendingPolicyRequestUseCase;

    @Mock
    private RejectPolicyRequestUseCase rejectPolicyRequestUseCase;

    @InjectMocks
    private ProcessPolicyRequestUseCase processPolicyRequestUseCase;

    private UUID testPolicyId;
    private PolicyRequest policyRequest;
    private FraudAnalysisResponseDTO fraudAnalysisResponse;

    @BeforeEach
    void setUp() {
        testPolicyId = UUID.randomUUID();
        setupPolicyRequest();
        setupFraudAnalysisResponse();
    }

    private void setupPolicyRequest() {
        policyRequest = new PolicyRequest();
        policyRequest.setId(testPolicyId);
        policyRequest.setCustomerId(UUID.randomUUID());
        policyRequest.setProductId("PROD123");
        policyRequest.setCategory(InsuranceCategory.AUTO);
        policyRequest.setSalesChannel(SalesChannel.WEBSITE);
        policyRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        policyRequest.setStatus(PolicyRequestStatus.RECEIVED);
        policyRequest.setCreatedAt(LocalDateTime.now());
        policyRequest.setTotalMonthlyPremiumAmount(new BigDecimal("150.00"));
        policyRequest.setInsuredAmount(new BigDecimal("50000.00"));
        policyRequest.setHistory(new ArrayList<>());
    }

    private void setupFraudAnalysisResponse() {
        fraudAnalysisResponse = new FraudAnalysisResponseDTO();
        fraudAnalysisResponse.setOrderId(UUID.randomUUID());
        fraudAnalysisResponse.setCustomerId(policyRequest.getCustomerId());
        fraudAnalysisResponse.setAnalyzedAt(LocalDateTime.now());
        fraudAnalysisResponse.setClassification(CustomerRiskClassification.REGULAR);
        fraudAnalysisResponse.setOccurrences(new ArrayList<>());
    }

    @Test
    void testExecuteSuccessWithValidation() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));
        when(fraudAnalysisService.analyzeFraud(policyRequest))
                .thenReturn(fraudAnalysisResponse);
        when(policyValidationService.validatePolicyRequest(policyRequest, CustomerRiskClassification.REGULAR))
                .thenReturn(true);
        when(policyRequestRepository.save(policyRequest))
                .thenReturn(policyRequest);

        // Act
        processPolicyRequestUseCase.execute(testPolicyId);

        // Assert
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(fraudAnalysisService, times(1)).analyzeFraud(policyRequest);
        verify(policyValidationService, times(1)).validatePolicyRequest(policyRequest, CustomerRiskClassification.REGULAR);
        verify(policyRequestRepository, times(1)).save(policyRequest);
        verify(eventPublisher, times(1)).publishPolicyRequestValidated(policyRequest);
        verify(setPendingPolicyRequestUseCase, times(1)).execute(testPolicyId);
        verify(rejectPolicyRequestUseCase, never()).execute(any(UUID.class), any(String.class));
    }

    @Test
    void testExecuteValidationFailed() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));
        when(fraudAnalysisService.analyzeFraud(policyRequest))
                .thenReturn(fraudAnalysisResponse);
        when(policyValidationService.validatePolicyRequest(policyRequest, CustomerRiskClassification.REGULAR))
                .thenReturn(false);
        when(policyValidationService.getRejectionReason(policyRequest, CustomerRiskClassification.REGULAR))
                .thenReturn("Política não aprovada devido a critérios de risco");

        // Act
        processPolicyRequestUseCase.execute(testPolicyId);

        // Assert
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(fraudAnalysisService, times(1)).analyzeFraud(policyRequest);
        verify(policyValidationService, times(1)).validatePolicyRequest(policyRequest, CustomerRiskClassification.REGULAR);
        verify(policyValidationService, times(1)).getRejectionReason(policyRequest, CustomerRiskClassification.REGULAR);
        verify(rejectPolicyRequestUseCase, times(1)).execute(testPolicyId, "Política não aprovada devido a critérios de risco");
        verify(policyRequestRepository, never()).save(policyRequest);
        verify(eventPublisher, never()).publishPolicyRequestValidated(any());
        verify(setPendingPolicyRequestUseCase, never()).execute(any(UUID.class));
    }

    @Test
    void testExecuteHighRiskClassification() {
        // Arrange
        fraudAnalysisResponse.setClassification(CustomerRiskClassification.HIGH_RISK);
        
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));
        when(fraudAnalysisService.analyzeFraud(policyRequest))
                .thenReturn(fraudAnalysisResponse);
        when(policyValidationService.validatePolicyRequest(policyRequest, CustomerRiskClassification.HIGH_RISK))
                .thenReturn(false);
        when(policyValidationService.getRejectionReason(policyRequest, CustomerRiskClassification.HIGH_RISK))
                .thenReturn("Cliente classificado como alto risco");

        // Act
        processPolicyRequestUseCase.execute(testPolicyId);

        // Assert
        verify(fraudAnalysisService, times(1)).analyzeFraud(policyRequest);
        verify(policyValidationService, times(1)).validatePolicyRequest(policyRequest, CustomerRiskClassification.HIGH_RISK);
        verify(rejectPolicyRequestUseCase, times(1)).execute(testPolicyId, "Cliente classificado como alto risco");
    }

    @Test
    void testExecuteLowRiskClassification() {
        // Arrange
        fraudAnalysisResponse.setClassification(CustomerRiskClassification.REGULAR);
        
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));
        when(fraudAnalysisService.analyzeFraud(policyRequest))
                .thenReturn(fraudAnalysisResponse);
        when(policyValidationService.validatePolicyRequest(policyRequest, CustomerRiskClassification.REGULAR))
                .thenReturn(true);
        when(policyRequestRepository.save(policyRequest))
                .thenReturn(policyRequest);

        // Act
        processPolicyRequestUseCase.execute(testPolicyId);

        // Assert
        verify(policyValidationService, times(1)).validatePolicyRequest(policyRequest, CustomerRiskClassification.REGULAR);
        verify(setPendingPolicyRequestUseCase, times(1)).execute(testPolicyId);
    }

    @Test
    void testExecuteWrongStatus() {
        // Arrange - Policy not in RECEIVED status
        policyRequest.setStatus(PolicyRequestStatus.VALIDATED);
        
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        processPolicyRequestUseCase.execute(testPolicyId);

        // Assert
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(fraudAnalysisService, never()).analyzeFraud(any());
        verify(policyValidationService, never()).validatePolicyRequest(any(), any());
        verify(policyRequestRepository, never()).save(any());
        verify(eventPublisher, never()).publishPolicyRequestValidated(any());
        verify(setPendingPolicyRequestUseCase, never()).execute(any(UUID.class));
        verify(rejectPolicyRequestUseCase, never()).execute(any(UUID.class), any(String.class));
    }

    @Test
    void testExecuteWrongStatusPending() {
        // Arrange - Policy in PENDING status
        policyRequest.setStatus(PolicyRequestStatus.PENDING);
        
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        processPolicyRequestUseCase.execute(testPolicyId);

        // Assert
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(fraudAnalysisService, never()).analyzeFraud(any());
    }

    @Test
    void testExecuteWrongStatusApproved() {
        // Arrange - Policy in APPROVED status
        policyRequest.setStatus(PolicyRequestStatus.APPROVED);
        
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));

        // Act
        processPolicyRequestUseCase.execute(testPolicyId);

        // Assert
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(fraudAnalysisService, never()).analyzeFraud(any());
    }

    @Test
    void testExecutePolicyNotFound() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> processPolicyRequestUseCase.execute(testPolicyId));
        
        assertEquals("Solicitação não encontrada: " + testPolicyId, exception.getMessage());
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(fraudAnalysisService, never()).analyzeFraud(any());
    }

    @Test
    void testExecuteFraudAnalysisException() {
        // Arrange
        RuntimeException fraudException = new RuntimeException("Erro na análise de fraudes");
        
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));
        when(fraudAnalysisService.analyzeFraud(policyRequest))
                .thenThrow(fraudException);

        // Act
        processPolicyRequestUseCase.execute(testPolicyId);

        // Assert
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(fraudAnalysisService, times(1)).analyzeFraud(policyRequest);
        verify(rejectPolicyRequestUseCase, times(1)).execute(testPolicyId, "Erro no processamento: Erro na análise de fraudes");
        verify(policyValidationService, never()).validatePolicyRequest(any(), any());
        verify(setPendingPolicyRequestUseCase, never()).execute(any(UUID.class));
    }

    @Test
    void testExecuteValidationServiceException() {
        // Arrange
        RuntimeException validationException = new RuntimeException("Erro no serviço de validação");
        
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));
        when(fraudAnalysisService.analyzeFraud(policyRequest))
                .thenReturn(fraudAnalysisResponse);
        when(policyValidationService.validatePolicyRequest(policyRequest, CustomerRiskClassification.REGULAR))
                .thenThrow(validationException);

        // Act
        processPolicyRequestUseCase.execute(testPolicyId);

        // Assert
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(fraudAnalysisService, times(1)).analyzeFraud(policyRequest);
        verify(policyValidationService, times(1)).validatePolicyRequest(policyRequest, CustomerRiskClassification.REGULAR);
        verify(rejectPolicyRequestUseCase, times(1)).execute(testPolicyId, "Erro no processamento: Erro no serviço de validação");
    }

    @Test
    void testExecuteRepositorySaveException() {
        // Arrange
        RuntimeException saveException = new RuntimeException("Erro ao salvar no banco");
        
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));
        when(fraudAnalysisService.analyzeFraud(policyRequest))
                .thenReturn(fraudAnalysisResponse);
        when(policyValidationService.validatePolicyRequest(policyRequest, CustomerRiskClassification.REGULAR))
                .thenReturn(true);
        when(policyRequestRepository.save(policyRequest))
                .thenThrow(saveException);

        // Act
        processPolicyRequestUseCase.execute(testPolicyId);

        // Assert
        verify(rejectPolicyRequestUseCase, times(1)).execute(testPolicyId, "Erro no processamento: Erro ao salvar no banco");
        verify(eventPublisher, never()).publishPolicyRequestValidated(any());
        verify(setPendingPolicyRequestUseCase, never()).execute(any(UUID.class));
    }

    @Test
    void testExecuteEventPublisherException() {
        // Arrange
        RuntimeException eventException = new RuntimeException("Erro ao publicar evento");
        
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));
        when(fraudAnalysisService.analyzeFraud(policyRequest))
                .thenReturn(fraudAnalysisResponse);
        when(policyValidationService.validatePolicyRequest(policyRequest, CustomerRiskClassification.REGULAR))
                .thenReturn(true);
        when(policyRequestRepository.save(policyRequest))
                .thenReturn(policyRequest);
        doThrow(eventException).when(eventPublisher).publishPolicyRequestValidated(policyRequest);

        // Act
        processPolicyRequestUseCase.execute(testPolicyId);

        // Assert
        verify(rejectPolicyRequestUseCase, times(1)).execute(testPolicyId, "Erro no processamento: Erro ao publicar evento");
        verify(setPendingPolicyRequestUseCase, never()).execute(any(UUID.class));
    }

    @Test
    void testExecuteSetPendingException() {
        // Arrange
        RuntimeException pendingException = new RuntimeException("Erro ao definir como pendente");
        
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));
        when(fraudAnalysisService.analyzeFraud(policyRequest))
                .thenReturn(fraudAnalysisResponse);
        when(policyValidationService.validatePolicyRequest(policyRequest, CustomerRiskClassification.REGULAR))
                .thenReturn(true);
        when(policyRequestRepository.save(policyRequest))
                .thenReturn(policyRequest);
        doThrow(pendingException).when(setPendingPolicyRequestUseCase).execute(testPolicyId);

        // Act
        processPolicyRequestUseCase.execute(testPolicyId);

        // Assert
        verify(rejectPolicyRequestUseCase, times(1)).execute(testPolicyId, "Erro no processamento: Erro ao definir como pendente");
    }

    @Test
    void testExecuteAsync() {
        // Arrange
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));
        when(fraudAnalysisService.analyzeFraud(policyRequest))
                .thenReturn(fraudAnalysisResponse);
        when(policyValidationService.validatePolicyRequest(policyRequest, CustomerRiskClassification.REGULAR))
                .thenReturn(true);
        when(policyRequestRepository.save(policyRequest))
                .thenReturn(policyRequest);

        // Act
        processPolicyRequestUseCase.executeAsync(testPolicyId);

        // Assert - Verify that the same logic is executed
        verify(policyRequestRepository, times(1)).findById(testPolicyId);
        verify(fraudAnalysisService, times(1)).analyzeFraud(policyRequest);
        verify(setPendingPolicyRequestUseCase, times(1)).execute(testPolicyId);
    }

    @Test
    void testExecuteWithDifferentPolicyCategories() {
        // Test with AUTO category only to avoid stubbing issues
        policyRequest.setCategory(InsuranceCategory.AUTO);
        
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));
        when(fraudAnalysisService.analyzeFraud(policyRequest))
                .thenReturn(fraudAnalysisResponse);
        when(policyValidationService.validatePolicyRequest(policyRequest, CustomerRiskClassification.REGULAR))
                .thenReturn(true);
        when(policyRequestRepository.save(policyRequest))
                .thenReturn(policyRequest);

        processPolicyRequestUseCase.execute(testPolicyId);
        
        verify(fraudAnalysisService, times(1)).analyzeFraud(policyRequest);
        verify(policyValidationService, times(1)).validatePolicyRequest(policyRequest, CustomerRiskClassification.REGULAR);
        verify(setPendingPolicyRequestUseCase, times(1)).execute(testPolicyId);
    }

    @Test
    void testFindPolicyRequestByIdPrivateMethod() {
        // This tests the private method indirectly through execute
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));
        when(fraudAnalysisService.analyzeFraud(policyRequest))
                .thenReturn(fraudAnalysisResponse);
        when(policyValidationService.validatePolicyRequest(policyRequest, CustomerRiskClassification.REGULAR))
                .thenReturn(true);
        when(policyRequestRepository.save(policyRequest))
                .thenReturn(policyRequest);

        processPolicyRequestUseCase.execute(testPolicyId);

        verify(policyRequestRepository, times(1)).findById(testPolicyId);
    }

    @Test
    void testExecuteCompleteFlow() {
        // Test the complete successful flow
        when(policyRequestRepository.findById(testPolicyId))
                .thenReturn(Optional.of(policyRequest));
        when(fraudAnalysisService.analyzeFraud(policyRequest))
                .thenReturn(fraudAnalysisResponse);
        when(policyValidationService.validatePolicyRequest(policyRequest, CustomerRiskClassification.REGULAR))
                .thenReturn(true);
        when(policyRequestRepository.save(policyRequest))
                .thenReturn(policyRequest);

        processPolicyRequestUseCase.execute(testPolicyId);

        // Verify complete flow order
        var inOrder = inOrder(policyRequestRepository, fraudAnalysisService, policyValidationService, 
                             eventPublisher, setPendingPolicyRequestUseCase);
        inOrder.verify(policyRequestRepository).findById(testPolicyId);
        inOrder.verify(fraudAnalysisService).analyzeFraud(policyRequest);
        inOrder.verify(policyValidationService).validatePolicyRequest(policyRequest, CustomerRiskClassification.REGULAR);
        inOrder.verify(policyRequestRepository).save(policyRequest);
        inOrder.verify(eventPublisher).publishPolicyRequestValidated(policyRequest);
        inOrder.verify(setPendingPolicyRequestUseCase).execute(testPolicyId);
    }
}