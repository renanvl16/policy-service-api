package com.acme.policyapi.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "policy_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyRequestJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    @NotNull
    private UUID customerId;

    @Column(name = "product_id", nullable = false)
    @NotBlank
    private String productId;

    @Column(nullable = false, length = 20)
    @NotNull
    private String category;

    @Column(name = "sales_channel", nullable = false, length = 20)
    @NotNull
    private String salesChannel;

    @Column(name = "payment_method", nullable = false, length = 20)
    @NotNull
    private String paymentMethod;

    @Column(nullable = false, length = 20)
    @NotNull
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "total_monthly_premium_amount", nullable = false, precision = 19, scale = 2)
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal totalMonthlyPremiumAmount;

    @Column(name = "insured_amount", nullable = false, precision = 19, scale = 2)
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal insuredAmount;

    @ElementCollection
    @CollectionTable(name = "policy_coverages", joinColumns = @JoinColumn(name = "policy_request_id"))
    @MapKeyColumn(name = "coverage_name")
    @Column(name = "coverage_amount", precision = 19, scale = 2)
    @Builder.Default
    private Map<String, BigDecimal> coverages = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "policy_assistances", joinColumns = @JoinColumn(name = "policy_request_id"))
    @Column(name = "assistance_name")
    @Builder.Default
    private List<String> assistances = new ArrayList<>();

    @OneToMany(mappedBy = "policyRequestId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("timestamp ASC")
    @Builder.Default
    private List<StatusHistoryJpaEntity> history = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "RECEIVED";
        }
    }
}