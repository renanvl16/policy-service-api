package com.acme.policyapi.domain.entity;

/**
 * Enumeração que representa os canais de venda disponíveis.
 * 
 * @author Sistema ACME
 */
public enum SalesChannel {
    
    /** Canal mobile */
    MOBILE("Mobile"),
    
    /** Canal WhatsApp */
    WHATSAPP("WhatsApp"),
    
    /** Canal web site */
    WEBSITE("Web Site"),
    
    /** Canal presencial */
    PRESENCIAL("Presencial"),
    
    /** Canal telefônico */
    TELEFONE("Telefone");
    
    private final String description;
    
    SalesChannel(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}