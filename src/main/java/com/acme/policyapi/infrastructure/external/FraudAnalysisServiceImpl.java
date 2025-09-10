package com.acme.policyapi.infrastructure.external;

import com.acme.policyapi.application.dto.FraudAnalysisResponseDTO;
import com.acme.policyapi.application.dto.OccurrenceDTO;
import com.acme.policyapi.application.service.FraudAnalysisService;
import com.acme.policyapi.domain.entity.CustomerRiskClassification;
import com.acme.policyapi.domain.entity.PolicyRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Implementação do serviço de análise de fraudes que integra com API externa (mock).
 * 
 * @author Sistema ACME
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FraudAnalysisServiceImpl implements FraudAnalysisService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    @Value("${fraud-analysis.api.url:http://localhost:9999/fraud-analysis}")
    private String fraudAnalysisApiUrl;

    @Value("${fraud-analysis.mock.enabled:true}")
    private boolean mockEnabled;

    @Override
    public FraudAnalysisResponseDTO analyzeFraud(PolicyRequest policyRequest) {
        return analyzeFraud(policyRequest.getId(), policyRequest.getCustomerId());
    }

    @Override
    public FraudAnalysisResponseDTO analyzeFraud(UUID orderId, UUID customerId) {
        log.info("Iniciando análise de fraudes para ordem {} e cliente {}", orderId, customerId);
        
        if (mockEnabled) {
            return createMockResponse(orderId, customerId);
        }
        
        try {
            String url = String.format("%s/analyze?orderId=%s&customerId=%s", 
                                     fraudAnalysisApiUrl, orderId, customerId);
            
            FraudAnalysisResponseDTO response = restTemplate.getForObject(url, FraudAnalysisResponseDTO.class);
            
            log.info("Análise de fraudes concluída: {} - Classificação: {}", 
                     orderId, response.getClassification());
            
            return response;
            
        } catch (Exception e) {
            log.error("Erro ao consultar API de fraudes: {}", e.getMessage(), e);
            // Fallback para classificação padrão em caso de erro
            return createFallbackResponse(orderId, customerId, e.getMessage());
        }
    }

    /**
     * Cria uma resposta mock para desenvolvimento e testes.
     * 
     * @param orderId ID da ordem
     * @param customerId ID do cliente
     * @return resposta mock
     */
    private FraudAnalysisResponseDTO createMockResponse(UUID orderId, UUID customerId) {
        log.debug("Gerando resposta mock para análise de fraudes");
        
        FraudAnalysisResponseDTO response = new FraudAnalysisResponseDTO();
        response.setOrderId(orderId);
        response.setCustomerId(customerId);
        response.setAnalyzedAt(LocalDateTime.now());
        
        // Simula classificações com diferentes probabilidades
        CustomerRiskClassification[] classifications = CustomerRiskClassification.values();
        double[] probabilities = {0.5, 0.2, 0.2, 0.1}; // REGULAR, HIGH_RISK, PREFERENTIAL, NO_INFORMATION
        
        CustomerRiskClassification classification = selectRandomClassification(classifications, probabilities);
        response.setClassification(classification);
        
        // Adiciona ocorrências baseadas na classificação
        response.setOccurrences(generateMockOccurrences(customerId, classification));
        
        log.info("Mock: Análise de fraudes concluída: {} - Classificação: {}", orderId, classification);
        
        return response;
    }

    /**
     * Cria resposta de fallback em caso de erro na API.
     */
    private FraudAnalysisResponseDTO createFallbackResponse(UUID orderId, UUID customerId, String errorMessage) {
        log.warn("Usando classificação de fallback devido a erro na API: {}", errorMessage);
        
        FraudAnalysisResponseDTO response = new FraudAnalysisResponseDTO();
        response.setOrderId(orderId);
        response.setCustomerId(customerId);
        response.setAnalyzedAt(LocalDateTime.now());
        response.setClassification(CustomerRiskClassification.NO_INFORMATION); // Classificação mais restritiva por segurança
        response.setOccurrences(List.of());
        
        return response;
    }

    /**
     * Seleciona classificação aleatória baseada em probabilidades.
     */
    private CustomerRiskClassification selectRandomClassification(
            CustomerRiskClassification[] classifications, double[] probabilities) {
        
        double randomValue = random.nextDouble();
        double cumulative = 0.0;
        
        for (int i = 0; i < classifications.length; i++) {
            cumulative += probabilities[i];
            if (randomValue <= cumulative) {
                return classifications[i];
            }
        }
        
        return CustomerRiskClassification.REGULAR; // Fallback
    }

    /**
     * Gera ocorrências mock baseadas na classificação de risco.
     */
    private List<OccurrenceDTO> generateMockOccurrences(
            UUID customerId, CustomerRiskClassification classification) {
        
        if (classification == CustomerRiskClassification.HIGH_RISK) {
            // Cliente de alto risco tem algumas ocorrências
            OccurrenceDTO occurrence = new OccurrenceDTO();
            occurrence.setId(UUID.randomUUID().toString());
            occurrence.setProductId(78900069L);
            occurrence.setType("FRAUD");
            occurrence.setDescription("Attempted Fraudulent transaction");
            occurrence.setCreatedAt(LocalDateTime.now().minusDays(30));
            occurrence.setUpdatedAt(LocalDateTime.now().minusDays(30));
            
            return List.of(occurrence);
        }
        
        return List.of(); // Outras classificações não têm ocorrências
    }
}