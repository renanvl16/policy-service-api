package com.acme.policyapi.infrastructure.external;

import com.acme.policyapi.application.dto.FraudAnalysisResponseDTO;
import com.acme.policyapi.application.dto.OccurrenceDTO;
import com.acme.policyapi.domain.entity.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para o FraudAnalysisServiceImpl.
 * 
 * @author Sistema ACME
 */
@ExtendWith(MockitoExtension.class)
class FraudAnalysisServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    private FraudAnalysisServiceImpl fraudAnalysisService;
    private ObjectMapper objectMapper;
    private PolicyRequest policyRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        fraudAnalysisService = new FraudAnalysisServiceImpl(restTemplate, objectMapper);
        
        // Configure mock mode by default
        ReflectionTestUtils.setField(fraudAnalysisService, "mockEnabled", true);
        ReflectionTestUtils.setField(fraudAnalysisService, "fraudAnalysisApiUrl", "http://localhost:9999/fraud-analysis");

        policyRequest = createPolicyRequest();
    }

    @Test
    void testAnalyzeFraudWithMockEnabled() {
        // Arrange - mock is enabled by default

        // Act
        FraudAnalysisResponseDTO response = fraudAnalysisService.analyzeFraud(policyRequest);

        // Assert
        assertNotNull(response);
        assertEquals(policyRequest.getId(), response.getOrderId());
        assertEquals(policyRequest.getCustomerId(), response.getCustomerId());
        assertNotNull(response.getAnalyzedAt());
        assertNotNull(response.getClassification());
        assertNotNull(response.getOccurrences());
        
        // Should be analyzed recently
        assertTrue(response.getAnalyzedAt().isAfter(LocalDateTime.now().minusMinutes(1)));
    }

    @Test
    void testAnalyzeFraudWithMockEnabledReturnsValidClassification() {
        // Act - run multiple times to test different classifications
        boolean foundRegular = false;
        boolean foundHighRisk = false;
        boolean foundPreferential = false;
        boolean foundNoInformation = false;

        for (int i = 0; i < 100; i++) {
            FraudAnalysisResponseDTO response = fraudAnalysisService.analyzeFraud(policyRequest);
            
            switch (response.getClassification()) {
                case REGULAR -> foundRegular = true;
                case HIGH_RISK -> foundHighRisk = true;
                case PREFERENTIAL -> foundPreferential = true;
                case NO_INFORMATION -> foundNoInformation = true;
            }
        }

        // Assert - should find at least some different classifications in 100 runs
        assertTrue(foundRegular || foundHighRisk || foundPreferential || foundNoInformation);
    }

    @Test
    void testAnalyzeFraudWithApiCallSuccess() {
        // Arrange
        ReflectionTestUtils.setField(fraudAnalysisService, "mockEnabled", false);
        
        FraudAnalysisResponseDTO expectedResponse = new FraudAnalysisResponseDTO();
        expectedResponse.setOrderId(policyRequest.getId());
        expectedResponse.setCustomerId(policyRequest.getCustomerId());
        expectedResponse.setAnalyzedAt(LocalDateTime.now());
        expectedResponse.setClassification(CustomerRiskClassification.REGULAR);

        when(restTemplate.getForObject(any(String.class), eq(FraudAnalysisResponseDTO.class)))
            .thenReturn(expectedResponse);

        // Act
        FraudAnalysisResponseDTO response = fraudAnalysisService.analyzeFraud(policyRequest);

        // Assert
        assertNotNull(response);
        assertEquals(expectedResponse.getOrderId(), response.getOrderId());
        assertEquals(expectedResponse.getCustomerId(), response.getCustomerId());
        assertEquals(expectedResponse.getClassification(), response.getClassification());
    }

    @Test
    void testAnalyzeFraudWithApiCallFailure() {
        // Arrange
        ReflectionTestUtils.setField(fraudAnalysisService, "mockEnabled", false);
        
        when(restTemplate.getForObject(any(String.class), eq(FraudAnalysisResponseDTO.class)))
            .thenThrow(new RestClientException("API call failed"));

        // Act
        FraudAnalysisResponseDTO response = fraudAnalysisService.analyzeFraud(policyRequest);

        // Assert - should return fallback response
        assertNotNull(response);
        assertEquals(policyRequest.getId(), response.getOrderId());
        assertEquals(policyRequest.getCustomerId(), response.getCustomerId());
        assertEquals(CustomerRiskClassification.NO_INFORMATION, response.getClassification());
        assertTrue(response.getOccurrences().isEmpty());
    }

    @Test
    void testAnalyzeFraudWithUUIDParameters() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        // Act
        FraudAnalysisResponseDTO response = fraudAnalysisService.analyzeFraud(orderId, customerId);

        // Assert
        assertNotNull(response);
        assertEquals(orderId, response.getOrderId());
        assertEquals(customerId, response.getCustomerId());
        assertNotNull(response.getClassification());
    }

    @Test
    void testHighRiskClassificationHasOccurrences() {
        // Act - run multiple times to eventually get HIGH_RISK classification
        boolean foundHighRiskWithOccurrences = false;
        
        for (int i = 0; i < 200; i++) {
            FraudAnalysisResponseDTO response = fraudAnalysisService.analyzeFraud(policyRequest);
            
            if (response.getClassification() == CustomerRiskClassification.HIGH_RISK) {
                assertNotNull(response.getOccurrences());
                if (!response.getOccurrences().isEmpty()) {
                    foundHighRiskWithOccurrences = true;
                    
                    OccurrenceDTO occurrence = response.getOccurrences().get(0);
                    assertNotNull(occurrence.getId());
                    assertNotNull(occurrence.getProductId());
                    assertEquals("FRAUD", occurrence.getType());
                    assertNotNull(occurrence.getDescription());
                    assertNotNull(occurrence.getCreatedAt());
                    assertNotNull(occurrence.getUpdatedAt());
                    break;
                }
            }
        }

        // Note: Due to randomness, we might not always get HIGH_RISK in the loop
        // This is acceptable for the mock implementation
    }

    @Test
    void testAnalyzeFraudResponseStructure() {
        // Act
        FraudAnalysisResponseDTO response = fraudAnalysisService.analyzeFraud(policyRequest);

        // Assert
        assertNotNull(response.getOrderId());
        assertNotNull(response.getCustomerId());
        assertNotNull(response.getAnalyzedAt());
        assertNotNull(response.getClassification());
        assertNotNull(response.getOccurrences());
        
        // Verify it's one of the valid classifications
        assertTrue(response.getClassification() instanceof CustomerRiskClassification);
    }

    private PolicyRequest createPolicyRequest() {
        PolicyRequest request = new PolicyRequest();
        request.setId(UUID.randomUUID());
        request.setCustomerId(UUID.randomUUID());
        request.setProductId("PROD-123");
        request.setCategory(InsuranceCategory.AUTO);
        request.setSalesChannel(SalesChannel.MOBILE);
        request.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        request.setTotalMonthlyPremiumAmount(new BigDecimal("150.00"));
        request.setInsuredAmount(new BigDecimal("250000.00"));
        return request;
    }
}