package com.acme.policyapi.application.usecase;

import com.acme.policyapi.application.dto.FraudAnalysisResponseDTO;
import com.acme.policyapi.application.service.FraudAnalysisService;
import com.acme.policyapi.domain.entity.PolicyRequest;
import com.acme.policyapi.domain.entity.PolicyRequestStatus;
import com.acme.policyapi.domain.repository.PolicyRequestRepository;
import com.acme.policyapi.domain.service.PolicyValidationService;
import com.acme.policyapi.infrastructure.messaging.PolicyEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessPolicyRequestUseCase {

    private final PolicyRequestRepository policyRequestRepository;
    private final FraudAnalysisService fraudAnalysisService;
    private final PolicyValidationService policyValidationService;
    private final PolicyEventPublisher eventPublisher;
    private final SetPendingPolicyRequestUseCase setPendingPolicyRequestUseCase;
    private final RejectPolicyRequestUseCase rejectPolicyRequestUseCase;

    @Transactional
    public void execute(UUID policyRequestId) {
        log.info("Processando solicitação: {}", policyRequestId);
        
        PolicyRequest policyRequest = findPolicyRequestById(policyRequestId);
        
        if (policyRequest.getStatus() != PolicyRequestStatus.RECEIVED) {
            log.warn("Solicitação {} não está no status RECEIVED. Status atual: {}", 
                     policyRequestId, policyRequest.getStatus());
            return;
        }
        
        try {
            FraudAnalysisResponseDTO fraudAnalysis = fraudAnalysisService.analyzeFraud(policyRequest);
            
            boolean isValid = policyValidationService.validatePolicyRequest(
                policyRequest, fraudAnalysis.getClassification());
            
            if (isValid) {
                policyRequest.updateStatus(PolicyRequestStatus.VALIDATED, "Validado pela análise de fraudes");
                policyRequestRepository.save(policyRequest);
                eventPublisher.publishPolicyRequestValidated(policyRequest);
                
                setPendingPolicyRequestUseCase.execute(policyRequestId);
            } else {
                String reason = policyValidationService.getRejectionReason(policyRequest, fraudAnalysis.getClassification());
                rejectPolicyRequestUseCase.execute(policyRequestId, reason);
            }
            
        } catch (Exception e) {
            log.error("Erro ao processar solicitação {}: {}", policyRequestId, e.getMessage(), e);
            rejectPolicyRequestUseCase.execute(policyRequestId, "Erro no processamento: " + e.getMessage());
        }
    }

    @Async
    public void executeAsync(UUID policyRequestId) {
        execute(policyRequestId);
    }

    private PolicyRequest findPolicyRequestById(UUID id) {
        return policyRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada: " + id));
    }
}