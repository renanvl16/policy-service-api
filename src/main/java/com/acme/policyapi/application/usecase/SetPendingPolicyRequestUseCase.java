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
public class SetPendingPolicyRequestUseCase {

    private final PolicyRequestRepository policyRequestRepository;
    private final PolicyEventPublisher eventPublisher;

    @Transactional
    public void execute(UUID policyRequestId) {
        log.info("Alterando solicitação para pendente: {}", policyRequestId);
        
        PolicyRequest policyRequest = findPolicyRequestById(policyRequestId);
        policyRequest.updateStatus(PolicyRequestStatus.PENDING, "Aguardando pagamento e autorização de subscrição");
        policyRequestRepository.save(policyRequest);
        
        eventPublisher.publishPolicyRequestPending(policyRequest);
    }

    private PolicyRequest findPolicyRequestById(UUID id) {
        return policyRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada: " + id));
    }
}