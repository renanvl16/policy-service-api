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
public class ApprovePolicyRequestUseCase {

    private final PolicyRequestRepository policyRequestRepository;
    private final PolicyEventPublisher eventPublisher;

    @Transactional
    public void execute(UUID policyRequestId) {
        log.info("Aprovando solicitação: {}", policyRequestId);
        
        PolicyRequest policyRequest = findPolicyRequestById(policyRequestId);
        policyRequest.updateStatus(PolicyRequestStatus.APPROVED, "Pagamento confirmado e subscrição autorizada");
        policyRequestRepository.save(policyRequest);
        
        eventPublisher.publishPolicyRequestApproved(policyRequest);
    }

    private PolicyRequest findPolicyRequestById(UUID id) {
        return policyRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada: " + id));
    }
}