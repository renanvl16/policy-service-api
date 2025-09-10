package com.acme.policyapi.domain.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para o enum CustomerRiskClassification.
 * 
 * @author Sistema ACME
 */
class CustomerRiskClassificationTest {

    @Test
    void testGetDescription() {
        assertEquals("Regular", CustomerRiskClassification.REGULAR.getDescription());
        assertEquals("Alto Risco", CustomerRiskClassification.HIGH_RISK.getDescription());
        assertEquals("Preferencial", CustomerRiskClassification.PREFERENTIAL.getDescription());
        assertEquals("Sem Informação", CustomerRiskClassification.NO_INFORMATION.getDescription());
    }

    @ParameterizedTest
    @MethodSource("provideInsuredAmountLimits")
    void testGetInsuredAmountLimit(CustomerRiskClassification classification, 
                                  InsuranceCategory category, 
                                  BigDecimal expectedLimit) {
        assertEquals(expectedLimit, classification.getInsuredAmountLimit(category));
    }

    static Stream<Arguments> provideInsuredAmountLimits() {
        return Stream.of(
            // REGULAR limits
            Arguments.of(CustomerRiskClassification.REGULAR, InsuranceCategory.VIDA, new BigDecimal("500000.00")),
            Arguments.of(CustomerRiskClassification.REGULAR, InsuranceCategory.RESIDENCIAL, new BigDecimal("500000.00")),
            Arguments.of(CustomerRiskClassification.REGULAR, InsuranceCategory.AUTO, new BigDecimal("350000.00")),
            Arguments.of(CustomerRiskClassification.REGULAR, InsuranceCategory.EMPRESARIAL, new BigDecimal("255000.00")),

            // HIGH_RISK limits
            Arguments.of(CustomerRiskClassification.HIGH_RISK, InsuranceCategory.AUTO, new BigDecimal("250000.00")),
            Arguments.of(CustomerRiskClassification.HIGH_RISK, InsuranceCategory.RESIDENCIAL, new BigDecimal("150000.00")),
            Arguments.of(CustomerRiskClassification.HIGH_RISK, InsuranceCategory.VIDA, new BigDecimal("125000.00")),
            Arguments.of(CustomerRiskClassification.HIGH_RISK, InsuranceCategory.EMPRESARIAL, new BigDecimal("125000.00")),

            // PREFERENTIAL limits
            Arguments.of(CustomerRiskClassification.PREFERENTIAL, InsuranceCategory.VIDA, new BigDecimal("800000.00")),
            Arguments.of(CustomerRiskClassification.PREFERENTIAL, InsuranceCategory.AUTO, new BigDecimal("450000.00")),
            Arguments.of(CustomerRiskClassification.PREFERENTIAL, InsuranceCategory.RESIDENCIAL, new BigDecimal("600000.00")),
            Arguments.of(CustomerRiskClassification.PREFERENTIAL, InsuranceCategory.EMPRESARIAL, new BigDecimal("375000.00")),

            // NO_INFORMATION limits
            Arguments.of(CustomerRiskClassification.NO_INFORMATION, InsuranceCategory.VIDA, new BigDecimal("200000.00")),
            Arguments.of(CustomerRiskClassification.NO_INFORMATION, InsuranceCategory.RESIDENCIAL, new BigDecimal("200000.00")),
            Arguments.of(CustomerRiskClassification.NO_INFORMATION, InsuranceCategory.AUTO, new BigDecimal("75000.00")),
            Arguments.of(CustomerRiskClassification.NO_INFORMATION, InsuranceCategory.EMPRESARIAL, new BigDecimal("55000.00"))
        );
    }

    @Test
    void testRegularCustomerLimitsConsistency() {
        CustomerRiskClassification regular = CustomerRiskClassification.REGULAR;
        
        // VIDA e RESIDENCIAL devem ter o mesmo limite
        assertEquals(regular.getInsuredAmountLimit(InsuranceCategory.VIDA),
                    regular.getInsuredAmountLimit(InsuranceCategory.RESIDENCIAL));
        
        // AUTO deve ter limite menor que VIDA/RESIDENCIAL
        assertTrue(regular.getInsuredAmountLimit(InsuranceCategory.AUTO)
                  .compareTo(regular.getInsuredAmountLimit(InsuranceCategory.VIDA)) < 0);
        
        // EMPRESARIAL deve ter limite menor que AUTO
        assertTrue(regular.getInsuredAmountLimit(InsuranceCategory.EMPRESARIAL)
                  .compareTo(regular.getInsuredAmountLimit(InsuranceCategory.AUTO)) < 0);
    }

    @Test
    void testHighRiskCustomerLimitsAreLowest() {
        CustomerRiskClassification highRisk = CustomerRiskClassification.HIGH_RISK;
        CustomerRiskClassification regular = CustomerRiskClassification.REGULAR;
        
        for (InsuranceCategory category : InsuranceCategory.values()) {
            BigDecimal highRiskLimit = highRisk.getInsuredAmountLimit(category);
            BigDecimal regularLimit = regular.getInsuredAmountLimit(category);
            
            assertTrue(highRiskLimit.compareTo(regularLimit) <= 0,
                      String.format("High risk limit for %s should be <= regular limit", category));
        }
    }

    @Test
    void testPreferentialCustomerLimitsAreHighest() {
        CustomerRiskClassification preferential = CustomerRiskClassification.PREFERENTIAL;
        CustomerRiskClassification regular = CustomerRiskClassification.REGULAR;
        
        for (InsuranceCategory category : InsuranceCategory.values()) {
            BigDecimal preferentialLimit = preferential.getInsuredAmountLimit(category);
            BigDecimal regularLimit = regular.getInsuredAmountLimit(category);
            
            assertTrue(preferentialLimit.compareTo(regularLimit) >= 0,
                      String.format("Preferential limit for %s should be >= regular limit", category));
        }
    }

    @Test
    void testNoInformationCustomerLimitsAreConservative() {
        CustomerRiskClassification noInfo = CustomerRiskClassification.NO_INFORMATION;
        CustomerRiskClassification regular = CustomerRiskClassification.REGULAR;
        
        for (InsuranceCategory category : InsuranceCategory.values()) {
            BigDecimal noInfoLimit = noInfo.getInsuredAmountLimit(category);
            BigDecimal regularLimit = regular.getInsuredAmountLimit(category);
            
            assertTrue(noInfoLimit.compareTo(regularLimit) <= 0,
                      String.format("No information limit for %s should be <= regular limit", category));
        }
    }

    @Test
    void testAllLimitsArePositive() {
        for (CustomerRiskClassification classification : CustomerRiskClassification.values()) {
            for (InsuranceCategory category : InsuranceCategory.values()) {
                BigDecimal limit = classification.getInsuredAmountLimit(category);
                assertTrue(limit.compareTo(BigDecimal.ZERO) > 0,
                          String.format("Limit for %s/%s should be positive", classification, category));
            }
        }
    }
}