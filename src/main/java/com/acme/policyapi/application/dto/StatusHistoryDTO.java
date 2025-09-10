package com.acme.policyapi.application.dto;

import com.acme.policyapi.domain.entity.PolicyRequestStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO para representar o histórico de status de uma solicitação de apólice.
 *
 * @author Sistema ACME
 */
@Getter
@Setter
public class StatusHistoryDTO {
    private PolicyRequestStatus status;
    private LocalDateTime timestamp;
    private String reason;
}
