package com.acme.policyapi.application.dto;

import com.acme.policyapi.domain.entity.InsuranceCategory;
import com.acme.policyapi.domain.entity.PaymentMethod;
import com.acme.policyapi.domain.entity.SalesChannel;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para PolicyRequestCreateDTO.
 */
class PolicyRequestCreateDTOTest {

    private Validator validator;
    private PolicyRequestCreateDTO validDTO;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        // Criar um DTO válido para usar como base
        validDTO = new PolicyRequestCreateDTO();
        validDTO.setCustomerId(UUID.randomUUID());
        validDTO.setProductId("PROD001");
        validDTO.setCategory(InsuranceCategory.AUTO);
        validDTO.setSalesChannel(SalesChannel.WEBSITE);
        validDTO.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        validDTO.setTotalMonthlyPremiumAmount(new BigDecimal("150.00"));
        validDTO.setInsuredAmount(new BigDecimal("50000.00"));
        
        Map<String, BigDecimal> coverages = new HashMap<>();
        coverages.put("COBERTURA_BASICA", new BigDecimal("30000.00"));
        validDTO.setCoverages(coverages);
        
        List<String> assistances = Arrays.asList("ASSISTENCIA_24H", "GUINCHO");
        validDTO.setAssistances(assistances);
    }

    @Test
    void testValidDTO() {
        Set<ConstraintViolation<PolicyRequestCreateDTO>> violations = validator.validate(validDTO);
        assertTrue(violations.isEmpty(), "DTO válido não deve ter violações");
    }

    @Test
    void testGettersAndSetters() {
        PolicyRequestCreateDTO dto = new PolicyRequestCreateDTO();
        UUID customerId = UUID.randomUUID();
        String productId = "TEST_PRODUCT";
        InsuranceCategory category = InsuranceCategory.VIDA;
        SalesChannel salesChannel = SalesChannel.PRESENCIAL;
        PaymentMethod paymentMethod = PaymentMethod.BOLETO;
        BigDecimal premium = new BigDecimal("200.50");
        BigDecimal insuredAmount = new BigDecimal("100000.00");
        Map<String, BigDecimal> coverages = Map.of("TEST_COVERAGE", new BigDecimal("50000.00"));
        List<String> assistances = List.of("TEST_ASSISTANCE");

        dto.setCustomerId(customerId);
        dto.setProductId(productId);
        dto.setCategory(category);
        dto.setSalesChannel(salesChannel);
        dto.setPaymentMethod(paymentMethod);
        dto.setTotalMonthlyPremiumAmount(premium);
        dto.setInsuredAmount(insuredAmount);
        dto.setCoverages(coverages);
        dto.setAssistances(assistances);

        assertEquals(customerId, dto.getCustomerId());
        assertEquals(productId, dto.getProductId());
        assertEquals(category, dto.getCategory());
        assertEquals(salesChannel, dto.getSalesChannel());
        assertEquals(paymentMethod, dto.getPaymentMethod());
        assertEquals(premium, dto.getTotalMonthlyPremiumAmount());
        assertEquals(insuredAmount, dto.getInsuredAmount());
        assertEquals(coverages, dto.getCoverages());
        assertEquals(assistances, dto.getAssistances());
    }

    @Test
    void testCustomerIdNotNull() {
        validDTO.setCustomerId(null);
        Set<ConstraintViolation<PolicyRequestCreateDTO>> violations = validator.validate(validDTO);
        
        assertEquals(1, violations.size());
        ConstraintViolation<PolicyRequestCreateDTO> violation = violations.iterator().next();
        assertEquals("ID do cliente é obrigatório", violation.getMessage());
        assertEquals("customerId", violation.getPropertyPath().toString());
    }

    @Test
    void testProductIdNotBlank() {
        validDTO.setProductId("");
        Set<ConstraintViolation<PolicyRequestCreateDTO>> violations = validator.validate(validDTO);
        
        assertEquals(1, violations.size());
        ConstraintViolation<PolicyRequestCreateDTO> violation = violations.iterator().next();
        assertEquals("ID do produto é obrigatório", violation.getMessage());
    }

    @Test
    void testProductIdNull() {
        validDTO.setProductId(null);
        Set<ConstraintViolation<PolicyRequestCreateDTO>> violations = validator.validate(validDTO);
        
        assertEquals(1, violations.size());
        ConstraintViolation<PolicyRequestCreateDTO> violation = violations.iterator().next();
        assertEquals("ID do produto é obrigatório", violation.getMessage());
    }

    @Test
    void testCategoryNotNull() {
        validDTO.setCategory(null);
        Set<ConstraintViolation<PolicyRequestCreateDTO>> violations = validator.validate(validDTO);
        
        assertEquals(1, violations.size());
        ConstraintViolation<PolicyRequestCreateDTO> violation = violations.iterator().next();
        assertEquals("Categoria é obrigatória", violation.getMessage());
    }

    @Test
    void testSalesChannelNotNull() {
        validDTO.setSalesChannel(null);
        Set<ConstraintViolation<PolicyRequestCreateDTO>> violations = validator.validate(validDTO);
        
        assertEquals(1, violations.size());
        ConstraintViolation<PolicyRequestCreateDTO> violation = violations.iterator().next();
        assertEquals("Canal de vendas é obrigatório", violation.getMessage());
    }

    @Test
    void testPaymentMethodNotNull() {
        validDTO.setPaymentMethod(null);
        Set<ConstraintViolation<PolicyRequestCreateDTO>> violations = validator.validate(validDTO);
        
        assertEquals(1, violations.size());
        ConstraintViolation<PolicyRequestCreateDTO> violation = violations.iterator().next();
        assertEquals("Forma de pagamento é obrigatória", violation.getMessage());
    }

    @Test
    void testTotalMonthlyPremiumAmountNotNull() {
        validDTO.setTotalMonthlyPremiumAmount(null);
        Set<ConstraintViolation<PolicyRequestCreateDTO>> violations = validator.validate(validDTO);
        
        assertEquals(1, violations.size());
        ConstraintViolation<PolicyRequestCreateDTO> violation = violations.iterator().next();
        assertEquals("Valor total do prêmio mensal é obrigatório", violation.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.00", "-10.50", "-0.01"})
    void testTotalMonthlyPremiumAmountMinValue(String value) {
        validDTO.setTotalMonthlyPremiumAmount(new BigDecimal(value));
        Set<ConstraintViolation<PolicyRequestCreateDTO>> violations = validator.validate(validDTO);
        
        assertEquals(1, violations.size());
        ConstraintViolation<PolicyRequestCreateDTO> violation = violations.iterator().next();
        assertEquals("Valor do prêmio deve ser maior que zero", violation.getMessage());
    }

    @Test
    void testTotalMonthlyPremiumAmountValidValue() {
        validDTO.setTotalMonthlyPremiumAmount(new BigDecimal("0.01"));
        Set<ConstraintViolation<PolicyRequestCreateDTO>> violations = validator.validate(validDTO);
        
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInsuredAmountNotNull() {
        validDTO.setInsuredAmount(null);
        Set<ConstraintViolation<PolicyRequestCreateDTO>> violations = validator.validate(validDTO);
        
        assertEquals(1, violations.size());
        ConstraintViolation<PolicyRequestCreateDTO> violation = violations.iterator().next();
        assertEquals("Valor do capital segurado é obrigatório", violation.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.00", "-1000.00", "-0.01"})
    void testInsuredAmountMinValue(String value) {
        validDTO.setInsuredAmount(new BigDecimal(value));
        Set<ConstraintViolation<PolicyRequestCreateDTO>> violations = validator.validate(validDTO);
        
        assertEquals(1, violations.size());
        ConstraintViolation<PolicyRequestCreateDTO> violation = violations.iterator().next();
        assertEquals("Valor do capital segurado deve ser maior que zero", violation.getMessage());
    }

    @Test
    void testInsuredAmountValidValue() {
        validDTO.setInsuredAmount(new BigDecimal("0.01"));
        Set<ConstraintViolation<PolicyRequestCreateDTO>> violations = validator.validate(validDTO);
        
        assertTrue(violations.isEmpty());
    }

    @Test
    void testCoveragesNotNull() {
        validDTO.setCoverages(null);
        Set<ConstraintViolation<PolicyRequestCreateDTO>> violations = validator.validate(validDTO);
        
        assertEquals(1, violations.size());
        ConstraintViolation<PolicyRequestCreateDTO> violation = violations.iterator().next();
        assertEquals("Coberturas são obrigatórias", violation.getMessage());
    }

    @Test
    void testCoveragesMinSize() {
        validDTO.setCoverages(new HashMap<>());
        Set<ConstraintViolation<PolicyRequestCreateDTO>> violations = validator.validate(validDTO);
        
        assertEquals(1, violations.size());
        ConstraintViolation<PolicyRequestCreateDTO> violation = violations.iterator().next();
        assertEquals("Deve haver pelo menos uma cobertura", violation.getMessage());
    }

    @Test
    void testAssistancesNotNull() {
        validDTO.setAssistances(null);
        Set<ConstraintViolation<PolicyRequestCreateDTO>> violations = validator.validate(validDTO);
        
        assertEquals(1, violations.size());
        ConstraintViolation<PolicyRequestCreateDTO> violation = violations.iterator().next();
        assertEquals("Assistências são obrigatórias", violation.getMessage());
    }

    @Test
    void testAssistancesEmptyListIsValid() {
        validDTO.setAssistances(new ArrayList<>());
        Set<ConstraintViolation<PolicyRequestCreateDTO>> violations = validator.validate(validDTO);
        
        assertTrue(violations.isEmpty());
    }

    @Test
    void testAllInsuranceCategories() {
        for (InsuranceCategory category : InsuranceCategory.values()) {
            validDTO.setCategory(category);
            Set<ConstraintViolation<PolicyRequestCreateDTO>> violations = validator.validate(validDTO);
            assertTrue(violations.isEmpty(), "Categoria " + category + " deve ser válida");
        }
    }

    @Test
    void testAllSalesChannels() {
        for (SalesChannel channel : SalesChannel.values()) {
            validDTO.setSalesChannel(channel);
            Set<ConstraintViolation<PolicyRequestCreateDTO>> violations = validator.validate(validDTO);
            assertTrue(violations.isEmpty(), "Canal " + channel + " deve ser válido");
        }
    }

    @Test
    void testAllPaymentMethods() {
        for (PaymentMethod method : PaymentMethod.values()) {
            validDTO.setPaymentMethod(method);
            Set<ConstraintViolation<PolicyRequestCreateDTO>> violations = validator.validate(validDTO);
            assertTrue(violations.isEmpty(), "Método " + method + " deve ser válido");
        }
    }

    @Test
    void testMultipleViolations() {
        PolicyRequestCreateDTO invalidDTO = new PolicyRequestCreateDTO();
        // Deixar todos os campos obrigatórios nulos/vazios
        
        Set<ConstraintViolation<PolicyRequestCreateDTO>> violations = validator.validate(invalidDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.size() >= 7); // Pelo menos 7 campos obrigatórios
    }

    private PolicyRequestCreateDTO createTestDTO() {
        PolicyRequestCreateDTO dto = new PolicyRequestCreateDTO();
        dto.setCustomerId(UUID.fromString("12345678-1234-1234-1234-123456789abc"));
        dto.setProductId("TEST_PRODUCT");
        dto.setCategory(InsuranceCategory.AUTO);
        dto.setSalesChannel(SalesChannel.WEBSITE);
        dto.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        dto.setTotalMonthlyPremiumAmount(new BigDecimal("150.00"));
        dto.setInsuredAmount(new BigDecimal("50000.00"));
        dto.setCoverages(Map.of("BASIC", new BigDecimal("25000.00")));
        dto.setAssistances(List.of("24H_ASSISTANCE"));
        return dto;
    }
}