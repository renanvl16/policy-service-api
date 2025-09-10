package com.acme.policyapi.application.dto;

import com.acme.policyapi.domain.entity.CustomerRiskClassification;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de resposta da API de análise de fraudes.
 * 
 * @author Sistema ACME
 */
@Getter
@Setter
public class FraudAnalysisResponseDTO {

    private UUID orderId;
    private UUID customerId;
    private LocalDateTime analyzedAt;
    private CustomerRiskClassification classification;
    private List<OccurrenceDTO> occurrences;
}