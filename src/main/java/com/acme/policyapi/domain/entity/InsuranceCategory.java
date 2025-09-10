package com.acme.policyapi.domain.entity;

/**
 * Enumeração que representa as categorias de seguro disponíveis.
 * 
 * @author Sistema ACME
 */
public enum InsuranceCategory {
    
    /** Seguro de vida */
    VIDA("Vida"),
    
    /** Seguro automotivo */
    AUTO("Auto"),
    
    /** Seguro residencial */
    RESIDENCIAL("Residencial"),
    
    /** Seguro empresarial */
    EMPRESARIAL("Empresarial");
    
    private final String description;
    
    InsuranceCategory(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}