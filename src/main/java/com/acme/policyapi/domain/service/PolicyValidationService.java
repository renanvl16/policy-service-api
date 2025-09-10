package com.acme.policyapi.domain.service;

import com.acme.policyapi.domain.entity.CustomerRiskClassification;
import com.acme.policyapi.domain.entity.InsuranceCategory;
import com.acme.policyapi.domain.entity.PolicyRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Serviço responsável por aplicar regras de validação de apólices baseadas 
 * na classificação de risco do cliente.
 * 
 * @author Sistema ACME
 */
@Service
@Slf4j
public class PolicyValidationService {

    /**
     * Valida se uma solicitação atende aos critérios baseados na classificação de risco.
     * 
     * @param policyRequest a solicitação a ser validada
     * @param riskClassification classificação de risco do cliente
     * @return true se a solicitação é válida, false caso contrário
     */
    public boolean validatePolicyRequest(PolicyRequest policyRequest, CustomerRiskClassification riskClassification) {
        log.info("Validando solicitação {} para cliente com classificação {}", 
                 policyRequest.getId(), riskClassification);
        
        BigDecimal insuredAmount = policyRequest.getInsuredAmount();
        InsuranceCategory category = policyRequest.getCategory();
        
        BigDecimal limit = riskClassification.getInsuredAmountLimit(category);
        
        boolean isValid = insuredAmount.compareTo(limit) <= 0;
        
        log.info("Valor segurado: R$ {}, Limite para {}/{}: R$ {}, Válido: {}", 
                 insuredAmount, riskClassification, category, limit, isValid);
        
        return isValid;
    }

    /**
     * Obtém o motivo da rejeição baseado na validação.
     * 
     * @param policyRequest a solicitação
     * @param riskClassification classificação de risco
     * @return motivo da rejeição ou null se válida
     */
    public String getRejectionReason(PolicyRequest policyRequest, CustomerRiskClassification riskClassification) {
        if (validatePolicyRequest(policyRequest, riskClassification)) {
            return null;
        }
        
        BigDecimal limit = riskClassification.getInsuredAmountLimit(policyRequest.getCategory());
        return String.format("Valor do capital segurado (R$ %s) excede o limite para cliente %s na categoria %s (R$ %s)",
                           policyRequest.getInsuredAmount(), 
                           riskClassification.getDescription(),
                           policyRequest.getCategory().getDescription(),
                           limit);
    }
}