package com.acme.policyapi.domain.entity;

/**
 * Enumeração que representa os métodos de pagamento disponíveis.
 * 
 * @author Sistema ACME
 */
public enum PaymentMethod {
    
    /** Pagamento via cartão de crédito */
    CREDIT_CARD("Cartão de Crédito"),
    
    /** Débito em conta corrente */
    DEBIT_ACCOUNT("Débito em Conta"),
    
    /** Boleto bancário */
    BOLETO("Boleto"),
    
    /** PIX */
    PIX("PIX");
    
    private final String description;
    
    PaymentMethod(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}