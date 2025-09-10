package com.acme.policyapi.infrastructure.messaging;

import com.acme.policyapi.domain.entity.PolicyRequestStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento base para notificações de alterações em solicitações de apólice.
 * 
 * @author Sistema ACME
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PolicyEvent {

    private UUID policyRequestId;
    private UUID customerId;
    private String productId;
    private PolicyRequestStatus status;
    private PolicyRequestStatus previousStatus;
    private String reason;
    private LocalDateTime timestamp;
    private String eventType;

    public PolicyEvent(UUID policyRequestId, UUID customerId, String productId, 
                      PolicyRequestStatus status, String eventType) {
        this.policyRequestId = policyRequestId;
        this.customerId = customerId;
        this.productId = productId;
        this.status = status;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }

    public PolicyEvent(UUID policyRequestId, UUID customerId, String productId,
                      PolicyRequestStatus status, PolicyRequestStatus previousStatus,
                      String reason, String eventType) {
        this(policyRequestId, customerId, productId, status, eventType);
        this.previousStatus = previousStatus;
        this.reason = reason;
    }
}