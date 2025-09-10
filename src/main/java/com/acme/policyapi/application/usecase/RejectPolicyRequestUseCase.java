package com.acme.policyapi.application.usecase;

import com.acme.policyapi.domain.entity.PolicyRequest;
import com.acme.policyapi.domain.entity.PolicyRequestStatus;
import com.acme.policyapi.domain.repository.PolicyRequestRepository;
import com.acme.policyapi.infrastructure.messaging.PolicyEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RejectPolicyRequestUseCase {

    private final PolicyRequestRepository policyRequestRepository;
    private final PolicyEventPublisher eventPublisher;

    @Transactional
    public void execute(UUID policyRequestId, String reason) {
        log.info("Rejeitando solicitação: {} - Motivo: {}", policyRequestId, reason);
        
        PolicyRequest policyRequest = findPolicyRequestById(policyRequestId);
        policyRequest.updateStatus(PolicyRequestStatus.REJECTED, reason);
        policyRequestRepository.save(policyRequest);
        
        eventPublisher.publishPolicyRequestRejected(policyRequest);
    }

    private PolicyRequest findPolicyRequestById(UUID id) {
        return policyRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada: " + id));
    }
}