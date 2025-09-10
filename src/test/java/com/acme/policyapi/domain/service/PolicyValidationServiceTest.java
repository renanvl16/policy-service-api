package com.acme.policyapi.domain.service;

import com.acme.policyapi.domain.entity.CustomerRiskClassification;
import com.acme.policyapi.domain.entity.InsuranceCategory;
import com.acme.policyapi.domain.entity.PolicyRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para o PolicyValidationService.
 * 
 * @author Sistema ACME
 */
class PolicyValidationServiceTest {

    private PolicyValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new PolicyValidationService();
    }

    @ParameterizedTest
    @MethodSource("provideValidPolicyRequests")
    void testValidatePolicyRequestValid(CustomerRiskClassification riskClassification,
                                       InsuranceCategory category,
                                       BigDecimal insuredAmount,
                                       boolean expectedResult) {
        // Arrange
        PolicyRequest policyRequest = createPolicyRequest(category, insuredAmount);

        // Act
        boolean result = validationService.validatePolicyRequest(policyRequest, riskClassification);

        // Assert
        assertEquals(expectedResult, result);
    }

    static Stream<Arguments> provideValidPolicyRequests() {
        return Stream.of(
            // REGULAR customer - valid cases
            Arguments.of(CustomerRiskClassification.REGULAR, InsuranceCategory.VIDA, new BigDecimal("499999.99"), true),
            Arguments.of(CustomerRiskClassification.REGULAR, InsuranceCategory.AUTO, new BigDecimal("349999.99"), true),
            Arguments.of(CustomerRiskClassification.REGULAR, InsuranceCategory.EMPRESARIAL, new BigDecimal("254999.99"), true),

            // REGULAR customer - invalid cases  
            Arguments.of(CustomerRiskClassification.REGULAR, InsuranceCategory.VIDA, new BigDecimal("500000.01"), false),
            Arguments.of(CustomerRiskClassification.REGULAR, InsuranceCategory.AUTO, new BigDecimal("350000.01"), false),

            // HIGH_RISK customer - valid cases
            Arguments.of(CustomerRiskClassification.HIGH_RISK, InsuranceCategory.AUTO, new BigDecimal("249999.99"), true),
            Arguments.of(CustomerRiskClassification.HIGH_RISK, InsuranceCategory.RESIDENCIAL, new BigDecimal("149999.99"), true),

            // HIGH_RISK customer - invalid cases
            Arguments.of(CustomerRiskClassification.HIGH_RISK, InsuranceCategory.AUTO, new BigDecimal("250000.01"), false),
            Arguments.of(CustomerRiskClassification.HIGH_RISK, InsuranceCategory.RESIDENCIAL, new BigDecimal("150000.01"), false),

            // PREFERENTIAL customer - valid cases
            Arguments.of(CustomerRiskClassification.PREFERENTIAL, InsuranceCategory.VIDA, new BigDecimal("799999.99"), true),
            Arguments.of(CustomerRiskClassification.PREFERENTIAL, InsuranceCategory.AUTO, new BigDecimal("449999.99"), true),

            // PREFERENTIAL customer - invalid cases
            Arguments.of(CustomerRiskClassification.PREFERENTIAL, InsuranceCategory.VIDA, new BigDecimal("800000.01"), false),

            // NO_INFORMATION customer - valid cases
            Arguments.of(CustomerRiskClassification.NO_INFORMATION, InsuranceCategory.VIDA, new BigDecimal("199999.99"), true),
            Arguments.of(CustomerRiskClassification.NO_INFORMATION, InsuranceCategory.AUTO, new BigDecimal("74999.99"), true),

            // NO_INFORMATION customer - invalid cases
            Arguments.of(CustomerRiskClassification.NO_INFORMATION, InsuranceCategory.VIDA, new BigDecimal("200000.01"), false),
            Arguments.of(CustomerRiskClassification.NO_INFORMATION, InsuranceCategory.AUTO, new BigDecimal("75000.01"), false)
        );
    }

    @Test
    void testGetRejectionReasonForValidRequest() {
        // Arrange
        PolicyRequest policyRequest = createPolicyRequest(InsuranceCategory.AUTO, new BigDecimal("100000.00"));
        CustomerRiskClassification classification = CustomerRiskClassification.REGULAR;

        // Act
        String reason = validationService.getRejectionReason(policyRequest, classification);

        // Assert
        assertNull(reason);
    }

    @Test
    void testGetRejectionReasonForInvalidRequest() {
        // Arrange
        PolicyRequest policyRequest = createPolicyRequest(InsuranceCategory.AUTO, new BigDecimal("400000.00"));
        CustomerRiskClassification classification = CustomerRiskClassification.REGULAR;

        // Act
        String reason = validationService.getRejectionReason(policyRequest, classification);

        // Assert
        assertNotNull(reason);
        assertTrue(reason.contains("R$ 400000.00"));
        assertTrue(reason.contains("excede o limite"));
        assertTrue(reason.contains("Regular"));
        assertTrue(reason.contains("Auto"));
        assertTrue(reason.contains("R$ 350000.00"));
    }

    @Test
    void testValidatePolicyRequestOnExactLimit() {
        // Arrange
        PolicyRequest policyRequest = createPolicyRequest(InsuranceCategory.AUTO, new BigDecimal("350000.00"));
        CustomerRiskClassification classification = CustomerRiskClassification.REGULAR;

        // Act
        boolean result = validationService.validatePolicyRequest(policyRequest, classification);

        // Assert
        assertTrue(result);
    }

    @Test
    void testValidatePolicyRequestJustOverLimit() {
        // Arrange
        PolicyRequest policyRequest = createPolicyRequest(InsuranceCategory.AUTO, new BigDecimal("350000.01"));
        CustomerRiskClassification classification = CustomerRiskClassification.REGULAR;

        // Act
        boolean result = validationService.validatePolicyRequest(policyRequest, classification);

        // Assert
        assertFalse(result);
    }

    @Test
    void testValidateAllCategoriesForAllClassifications() {
        for (CustomerRiskClassification classification : CustomerRiskClassification.values()) {
            for (InsuranceCategory category : InsuranceCategory.values()) {
                // Test with amount just below limit
                BigDecimal limit = classification.getInsuredAmountLimit(category);
                BigDecimal belowLimit = limit.subtract(new BigDecimal("0.01"));
                
                PolicyRequest validRequest = createPolicyRequest(category, belowLimit);
                assertTrue(validationService.validatePolicyRequest(validRequest, classification),
                          String.format("Should be valid: %s/%s with amount %s (limit: %s)", 
                                      classification, category, belowLimit, limit));

                // Test with amount just above limit
                BigDecimal aboveLimit = limit.add(new BigDecimal("0.01"));
                PolicyRequest invalidRequest = createPolicyRequest(category, aboveLimit);
                assertFalse(validationService.validatePolicyRequest(invalidRequest, classification),
                           String.format("Should be invalid: %s/%s with amount %s (limit: %s)", 
                                       classification, category, aboveLimit, limit));
            }
        }
    }

    private PolicyRequest createPolicyRequest(InsuranceCategory category, BigDecimal insuredAmount) {
        PolicyRequest policyRequest = new PolicyRequest();
        policyRequest.setId(UUID.randomUUID());
        policyRequest.setCustomerId(UUID.randomUUID());
        policyRequest.setCategory(category);
        policyRequest.setInsuredAmount(insuredAmount);
        return policyRequest;
    }
}