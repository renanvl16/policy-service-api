package com.acme.policyapi.application.dto;

import com.acme.policyapi.domain.entity.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO de resposta para consulta de solicitação de apólice.
 * 
 * @author Sistema ACME
 */
@Getter
@Setter
public class PolicyRequestResponseDTO {

    private UUID id;
    private UUID customerId;
    private String productId;
    private InsuranceCategory category;
    private SalesChannel salesChannel;
    private PaymentMethod paymentMethod;
    private PolicyRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;
    private BigDecimal totalMonthlyPremiumAmount;
    private BigDecimal insuredAmount;
    private Map<String, BigDecimal> coverages;
    private List<String> assistances;
    private List<StatusHistoryDTO> history;
}