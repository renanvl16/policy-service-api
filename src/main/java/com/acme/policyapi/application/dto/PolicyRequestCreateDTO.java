package com.acme.policyapi.application.dto;

import com.acme.policyapi.domain.entity.InsuranceCategory;
import com.acme.policyapi.domain.entity.PaymentMethod;
import com.acme.policyapi.domain.entity.SalesChannel;
import lombok.Data;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO para criação de uma nova solicitação de apólice.
 * 
 * @author Sistema ACME
 */
@Getter
@Setter
public class PolicyRequestCreateDTO {

    @NotNull(message = "ID do cliente é obrigatório")
    private UUID customerId;

    @NotBlank(message = "ID do produto é obrigatório")
    private String productId;

    @NotNull(message = "Categoria é obrigatória")
    private InsuranceCategory category;

    @NotNull(message = "Canal de vendas é obrigatório")
    private SalesChannel salesChannel;

    @NotNull(message = "Forma de pagamento é obrigatória")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Valor total do prêmio mensal é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor do prêmio deve ser maior que zero")
    private BigDecimal totalMonthlyPremiumAmount;

    @NotNull(message = "Valor do capital segurado é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor do capital segurado deve ser maior que zero")
    private BigDecimal insuredAmount;

    @NotNull(message = "Coberturas são obrigatórias")
    @Size(min = 1, message = "Deve haver pelo menos uma cobertura")
    private Map<String, BigDecimal> coverages;

    @NotNull(message = "Assistências são obrigatórias")
    private List<String> assistances;
}