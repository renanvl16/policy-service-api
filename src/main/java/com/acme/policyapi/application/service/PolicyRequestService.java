package com.acme.policyapi.application.service;

import com.acme.policyapi.application.dto.PolicyRequestCreateDTO;
import com.acme.policyapi.application.dto.PolicyRequestResponseDTO;
import com.acme.policyapi.domain.entity.PolicyRequest;

import java.util.List;
import java.util.UUID;

/**
 * Interface para serviço de aplicação de solicitações de apólice.
 * 
 * @author Sistema ACME
 */
public interface PolicyRequestService {

    /**
     * Cria uma nova solicitação de apólice.
     * 
     * @param createDTO dados da solicitação
     * @return solicitação criada
     */
    PolicyRequestResponseDTO createPolicyRequest(PolicyRequestCreateDTO createDTO);

    /**
     * Busca uma solicitação por ID.
     * 
     * @param id ID da solicitação
     * @return solicitação encontrada
     */
    PolicyRequestResponseDTO findById(UUID id);

    /**
     * Busca solicitações por ID do cliente.
     * 
     * @param customerId ID do cliente
     * @return lista de solicitações do cliente
     */
    List<PolicyRequestResponseDTO> findByCustomerId(UUID customerId);

    /**
     * Processa uma solicitação através da análise de fraudes e validação.
     * 
     * @param policyRequestId ID da solicitação
     */
    void processRequest(UUID policyRequestId);

    /**
     * Cancela uma solicitação.
     * 
     * @param policyRequestId ID da solicitação
     * @param reason motivo do cancelamento
     */
    void cancelRequest(UUID policyRequestId, String reason);

    /**
     * Atualiza status para pendente após validação.
     * 
     * @param policyRequestId ID da solicitação
     */
    void setPending(UUID policyRequestId);

    /**
     * Aprova uma solicitação após confirmação de pagamento e subscrição.
     * 
     * @param policyRequestId ID da solicitação
     */
    void approveRequest(UUID policyRequestId);

    /**
     * Rejeita uma solicitação.
     * 
     * @param policyRequestId ID da solicitação
     * @param reason motivo da rejeição
     */
    void rejectRequest(UUID policyRequestId, String reason);
}