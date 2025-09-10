package com.acme.policyapi.application.service.impl;

import com.acme.policyapi.application.dto.PolicyRequestCreateDTO;
import com.acme.policyapi.application.dto.PolicyRequestResponseDTO;
import com.acme.policyapi.application.service.PolicyRequestService;
import com.acme.policyapi.application.usecase.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Implementação do serviço de aplicação para solicitações de apólice.
 * 
 * @author Sistema ACME
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyRequestServiceImpl implements PolicyRequestService {

    private final CreatePolicyRequestUseCase createPolicyRequestUseCase;
    private final FindPolicyRequestUseCase findPolicyRequestUseCase;
    private final ProcessPolicyRequestUseCase processPolicyRequestUseCase;
    private final CancelPolicyRequestUseCase cancelPolicyRequestUseCase;
    private final SetPendingPolicyRequestUseCase setPendingPolicyRequestUseCase;
    private final ApprovePolicyRequestUseCase approvePolicyRequestUseCase;
    private final RejectPolicyRequestUseCase rejectPolicyRequestUseCase;

    @Override
    public PolicyRequestResponseDTO createPolicyRequest(PolicyRequestCreateDTO createDTO) {
        return createPolicyRequestUseCase.execute(createDTO);
    }

    @Override
    public PolicyRequestResponseDTO findById(UUID id) {
        return findPolicyRequestUseCase.findById(id);
    }

    @Override
    public List<PolicyRequestResponseDTO> findByCustomerId(UUID customerId) {
        return findPolicyRequestUseCase.findByCustomerId(customerId);
    }

    @Override
    public void processRequest(UUID policyRequestId) {
        processPolicyRequestUseCase.execute(policyRequestId);
    }

    @Override
    public void cancelRequest(UUID policyRequestId, String reason) {
        cancelPolicyRequestUseCase.execute(policyRequestId, reason);
    }

    @Override
    public void setPending(UUID policyRequestId) {
        setPendingPolicyRequestUseCase.execute(policyRequestId);
    }

    @Override
    public void approveRequest(UUID policyRequestId) {
        approvePolicyRequestUseCase.execute(policyRequestId);
    }

    @Override
    public void rejectRequest(UUID policyRequestId, String reason) {
        rejectPolicyRequestUseCase.execute(policyRequestId, reason);
    }
}