package com.acme.policyapi.application.usecase;

import com.acme.policyapi.application.dto.PolicyRequestCreateDTO;
import com.acme.policyapi.application.dto.PolicyRequestResponseDTO;
import com.acme.policyapi.application.service.impl.PolicyRequestMapper;
import com.acme.policyapi.domain.entity.PolicyRequest;
import com.acme.policyapi.domain.repository.PolicyRequestRepository;
import com.acme.policyapi.infrastructure.messaging.PolicyEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreatePolicyRequestUseCase {

    private final PolicyRequestRepository policyRequestRepository;
    private final PolicyEventPublisher eventPublisher;
    private final PolicyRequestMapper policyRequestMapper;
    private final ProcessPolicyRequestUseCase processPolicyRequestUseCase;

    @Transactional
    public PolicyRequestResponseDTO execute(PolicyRequestCreateDTO createDTO) {
        log.info("Executando criação de solicitação de apólice para cliente {}", createDTO.getCustomerId());
        
        PolicyRequest policyRequest = policyRequestMapper.toEntity(createDTO);
        policyRequest = policyRequestRepository.save(policyRequest);
        
        log.info("Solicitação {} criada com sucesso", policyRequest.getId());
        
        eventPublisher.publishPolicyRequestCreated(policyRequest);
        
        processPolicyRequestUseCase.executeAsync(policyRequest.getId());
        
        return policyRequestMapper.toResponseDTO(policyRequest);
    }
}