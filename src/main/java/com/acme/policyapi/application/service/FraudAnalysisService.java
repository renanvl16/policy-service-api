package com.acme.policyapi.application.service;

import com.acme.policyapi.application.dto.FraudAnalysisResponseDTO;
import com.acme.policyapi.domain.entity.PolicyRequest;

import java.util.UUID;

/**
 * Interface para serviço de análise de fraudes.
 * 
 * @author Sistema ACME
 */
public interface FraudAnalysisService {

    /**
     * Solicita análise de fraudes para uma solicitação de apólice.
     * 
     * @param policyRequest a solicitação a ser analisada
     * @return resultado da análise de fraudes
     */
    FraudAnalysisResponseDTO analyzeFraud(PolicyRequest policyRequest);

    /**
     * Solicita análise de fraudes por IDs.
     * 
     * @param orderId ID da solicitação
     * @param customerId ID do cliente
     * @return resultado da análise de fraudes
     */
    FraudAnalysisResponseDTO analyzeFraud(UUID orderId, UUID customerId);
}