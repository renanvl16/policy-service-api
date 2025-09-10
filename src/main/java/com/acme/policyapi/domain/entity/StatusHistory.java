package com.acme.policyapi.domain.entity;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade que representa o histórico de alterações de status de uma solicitação.
 * 
 * @author Sistema ACME
 */
@Entity
@Table(name = "status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "policy_request_id", nullable = false)
    private UUID policyRequestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PolicyRequestStatus status;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 500)
    private String reason;

    public StatusHistory(UUID policyRequestId, PolicyRequestStatus status, LocalDateTime timestamp) {
        this.policyRequestId = policyRequestId;
        this.status = status;
        this.timestamp = timestamp;
    }

    public StatusHistory(UUID policyRequestId, PolicyRequestStatus status, LocalDateTime timestamp, String reason) {
        this(policyRequestId, status, timestamp);
        this.reason = reason;
    }
}