package com.acme.policyapi.domain.entity;

import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Entidade principal que representa uma solicitação de apólice de seguro.
 * 
 * @author Sistema ACME
 */
@Entity
@Table(name = "policy_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    @NotNull(message = "ID do cliente é obrigatório")
    private UUID customerId;

    @Column(name = "product_id", nullable = false)
    @NotBlank(message = "ID do produto é obrigatório")
    private String productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "Categoria é obrigatória")
    private InsuranceCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "sales_channel", nullable = false, length = 20)
    @NotNull(message = "Canal de vendas é obrigatório")
    private SalesChannel salesChannel;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    @NotNull(message = "Forma de pagamento é obrigatória")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private PolicyRequestStatus status = PolicyRequestStatus.RECEIVED;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "total_monthly_premium_amount", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Valor total do prêmio mensal é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor do prêmio deve ser maior que zero")
    private BigDecimal totalMonthlyPremiumAmount;

    @Column(name = "insured_amount", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Valor do capital segurado é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor do capital segurado deve ser maior que zero")
    private BigDecimal insuredAmount;

    @ElementCollection
    @CollectionTable(name = "policy_coverages", joinColumns = @JoinColumn(name = "policy_request_id"))
    @MapKeyColumn(name = "coverage_name")
    @Column(name = "coverage_amount", precision = 19, scale = 2)
    private Map<String, BigDecimal> coverages = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "policy_assistances", joinColumns = @JoinColumn(name = "policy_request_id"))
    @Column(name = "assistance_name")
    private List<String> assistances = new ArrayList<>();

    @OneToMany(mappedBy = "policyRequestId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("timestamp ASC")
    private List<StatusHistory> history = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = PolicyRequestStatus.RECEIVED;
        }
    }

    /**
     * Adiciona um novo status ao histórico da solicitação.
     * 
     * @param newStatus o novo status
     * @param reason motivo da alteração (opcional)
     */
    public void updateStatus(PolicyRequestStatus newStatus, String reason) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format("Transição inválida de %s para %s", this.status, newStatus)
            );
        }

        this.status = newStatus;
        
        if (newStatus.isFinalState()) {
            this.finishedAt = LocalDateTime.now();
        }

        StatusHistory historyEntry = new StatusHistory(this.id, newStatus, LocalDateTime.now(), reason);
        this.history.add(historyEntry);
    }

    /**
     * Verifica se a solicitação pode ser cancelada.
     * 
     * @return true se pode ser cancelada
     */
    public boolean canBeCancelled() {
        return !status.isFinalState() || status == PolicyRequestStatus.CANCELLED;
    }

    /**
     * Verifica se a solicitação foi aprovada.
     * 
     * @return true se foi aprovada
     */
    public boolean isApproved() {
        return status == PolicyRequestStatus.APPROVED;
    }
}