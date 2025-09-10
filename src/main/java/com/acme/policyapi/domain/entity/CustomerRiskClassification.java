package com.acme.policyapi.domain.entity;

import java.math.BigDecimal;

/**
 * Enumeração que representa as classificações de risco do cliente.
 * 
 * @author Sistema ACME
 */
public enum CustomerRiskClassification {
    
    /** Cliente com perfil de risco baixo */
    REGULAR("Regular"),
    
    /** Cliente com perfil de maior risco */
    HIGH_RISK("Alto Risco"),
    
    /** Cliente com bom relacionamento com a seguradora */
    PREFERENTIAL("Preferencial"),
    
    /** Cliente sem histórico ou com pouco histórico */
    NO_INFORMATION("Sem Informação");
    
    private final String description;
    
    CustomerRiskClassification(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Obtém o limite máximo de capital segurado para a categoria de seguro especificada.
     * 
     * @param category categoria do seguro
     * @return valor limite em reais
     */
    public BigDecimal getInsuredAmountLimit(InsuranceCategory category) {
        return switch (this) {
            case REGULAR -> getRegularLimits(category);
            case HIGH_RISK -> getHighRiskLimits(category);
            case PREFERENTIAL -> getPreferentialLimits(category);
            case NO_INFORMATION -> getNoInformationLimits(category);
        };
    }
    
    private BigDecimal getRegularLimits(InsuranceCategory category) {
        return switch (category) {
            case VIDA, RESIDENCIAL -> new BigDecimal("500000.00");
            case AUTO -> new BigDecimal("350000.00");
            default -> new BigDecimal("255000.00");
        };
    }
    
    private BigDecimal getHighRiskLimits(InsuranceCategory category) {
        return switch (category) {
            case AUTO -> new BigDecimal("250000.00");
            case RESIDENCIAL -> new BigDecimal("150000.00");
            default -> new BigDecimal("125000.00");
        };
    }
    
    private BigDecimal getPreferentialLimits(InsuranceCategory category) {
        return switch (category) {
            case VIDA -> new BigDecimal("800000.00");
            case AUTO -> new BigDecimal("450000.00");
            case RESIDENCIAL -> new BigDecimal("600000.00");
            default -> new BigDecimal("375000.00");
        };
    }
    
    private BigDecimal getNoInformationLimits(InsuranceCategory category) {
        return switch (category) {
            case VIDA, RESIDENCIAL -> new BigDecimal("200000.00");
            case AUTO -> new BigDecimal("75000.00");
            default -> new BigDecimal("55000.00");
        };
    }
}