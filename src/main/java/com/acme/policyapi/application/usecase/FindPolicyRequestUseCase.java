package com.acme.policyapi.application.usecase;

import com.acme.policyapi.application.dto.PolicyRequestResponseDTO;
import com.acme.policyapi.application.exception.PolicyRequestNotFoundException;
import com.acme.policyapi.application.service.impl.PolicyRequestMapper;
import com.acme.policyapi.domain.entity.PolicyRequest;
import com.acme.policyapi.domain.repository.PolicyRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class FindPolicyRequestUseCase {

    private final PolicyRequestRepository policyRequestRepository;
    private final PolicyRequestMapper policyRequestMapper;

    @Transactional(readOnly = true)
    public PolicyRequestResponseDTO findById(UUID id) {
        log.debug("Buscando solicitação por ID: {}", id);
        
        PolicyRequest policyRequest = policyRequestRepository.findByIdWithHistory(id)
                .orElseThrow(() -> new PolicyRequestNotFoundException("Solicitação não encontrada: " + id));
        
        return policyRequestMapper.toResponseDTO(policyRequest);
    }

    @Transactional(readOnly = true)
    public List<PolicyRequestResponseDTO> findByCustomerId(UUID customerId) {
        log.debug("Buscando solicitações do cliente: {}", customerId);
        
        List<PolicyRequest> policyRequests = policyRequestRepository.findByCustomerIdWithHistory(customerId);
        return policyRequestMapper.toResponseDTOList(policyRequests);
    }

}