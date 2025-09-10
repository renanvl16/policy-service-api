package com.acme.policyapi.domain.entity;

/**
 * Enumeração que representa os estados possíveis de uma solicitação de apólice.
 * 
 * Os estados seguem o ciclo de vida definido:
 * RECEIVED -> VALIDATED -> PENDING -> APPROVED
 *          \-> REJECTED            \-> REJECTED
 *          \-> CANCELLED           \-> CANCELLED
 * 
 * @author Sistema ACME
 */
public enum PolicyRequestStatus {
    
    /** Estado inicial quando uma solicitação é criada */
    RECEIVED("Recebido"),
    
    /** Estado após validação bem-sucedida pela API de Fraudes */
    VALIDATED("Validado"),
    
    /** Estado aguardando confirmação de pagamento e autorização de subscrição */
    PENDING("Pendente"),
    
    /** Estado final após aprovação completa */
    APPROVED("Aprovado"),
    
    /** Estado final para solicitações rejeitadas */
    REJECTED("Rejeitado"),
    
    /** Estado final para solicitações canceladas */
    CANCELLED("Cancelado");
    
    private final String description;
    
    PolicyRequestStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Verifica se o estado atual permite transição para o novo estado.
     * 
     * @param newStatus o novo estado desejado
     * @return true se a transição é válida, false caso contrário
     */
    public boolean canTransitionTo(PolicyRequestStatus newStatus) {
        return switch (this) {
            case RECEIVED -> newStatus == VALIDATED || newStatus == REJECTED || newStatus == CANCELLED;
            case VALIDATED -> newStatus == PENDING || newStatus == REJECTED || newStatus == CANCELLED;
            case PENDING -> newStatus == APPROVED || newStatus == REJECTED || newStatus == CANCELLED;
            case APPROVED, REJECTED, CANCELLED -> false; // Estados finais
        };
    }
    
    /**
     * Verifica se o estado é final (não permite mais transições).
     * 
     * @return true se o estado é final
     */
    public boolean isFinalState() {
        return this == APPROVED || this == REJECTED || this == CANCELLED;
    }
}