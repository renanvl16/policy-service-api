package com.acme.policyapi.domain.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para PaymentMethod.
 */
class PaymentMethodTest {

    @Test
    void testAllEnumValues() {
        // Arrange & Act
        PaymentMethod[] methods = PaymentMethod.values();

        // Assert
        assertEquals(4, methods.length);
        assertTrue(containsValue(methods, PaymentMethod.CREDIT_CARD));
        assertTrue(containsValue(methods, PaymentMethod.DEBIT_ACCOUNT));
        assertTrue(containsValue(methods, PaymentMethod.BOLETO));
        assertTrue(containsValue(methods, PaymentMethod.PIX));
    }

    @Test
    void testCreditCardMethod() {
        // Act
        PaymentMethod method = PaymentMethod.CREDIT_CARD;

        // Assert
        assertEquals("Cartão de Crédito", method.getDescription());
        assertEquals("CREDIT_CARD", method.name());
    }

    @Test
    void testDebitAccountMethod() {
        // Act
        PaymentMethod method = PaymentMethod.DEBIT_ACCOUNT;

        // Assert
        assertEquals("Débito em Conta", method.getDescription());
        assertEquals("DEBIT_ACCOUNT", method.name());
    }

    @Test
    void testBoletoMethod() {
        // Act
        PaymentMethod method = PaymentMethod.BOLETO;

        // Assert
        assertEquals("Boleto", method.getDescription());
        assertEquals("BOLETO", method.name());
    }

    @Test
    void testPixMethod() {
        // Act
        PaymentMethod method = PaymentMethod.PIX;

        // Assert
        assertEquals("PIX", method.getDescription());
        assertEquals("PIX", method.name());
    }

    @ParameterizedTest
    @EnumSource(PaymentMethod.class)
    void testGetDescriptionNotNull(PaymentMethod method) {
        // Assert
        assertNotNull(method.getDescription());
        assertFalse(method.getDescription().isEmpty());
        assertFalse(method.getDescription().isBlank());
    }

    @ParameterizedTest
    @EnumSource(PaymentMethod.class)
    void testNameNotNull(PaymentMethod method) {
        // Assert
        assertNotNull(method.name());
        assertFalse(method.name().isEmpty());
        assertFalse(method.name().isBlank());
    }

    @Test
    void testValueOfValidString() {
        // Act & Assert
        assertEquals(PaymentMethod.CREDIT_CARD, PaymentMethod.valueOf("CREDIT_CARD"));
        assertEquals(PaymentMethod.DEBIT_ACCOUNT, PaymentMethod.valueOf("DEBIT_ACCOUNT"));
        assertEquals(PaymentMethod.BOLETO, PaymentMethod.valueOf("BOLETO"));
        assertEquals(PaymentMethod.PIX, PaymentMethod.valueOf("PIX"));
    }

    @Test
    void testValueOfInvalidString() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            PaymentMethod.valueOf("INVALID_METHOD"));
    }

    @Test
    void testValueOfNullString() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> 
            PaymentMethod.valueOf(null));
    }

    @Test
    void testValueOfEmptyString() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            PaymentMethod.valueOf(""));
    }

    @Test
    void testValueOfCaseInvariant() {
        // Act & Assert - Deve ser case sensitive
        assertThrows(IllegalArgumentException.class, () -> 
            PaymentMethod.valueOf("credit_card"));
        assertThrows(IllegalArgumentException.class, () -> 
            PaymentMethod.valueOf("Credit_Card"));
        assertThrows(IllegalArgumentException.class, () -> 
            PaymentMethod.valueOf("pix"));
        assertThrows(IllegalArgumentException.class, () -> 
            PaymentMethod.valueOf("boleto"));
    }

    @Test
    void testEnumOrdinal() {
        // Assert - Testa a ordem dos enums
        assertEquals(0, PaymentMethod.CREDIT_CARD.ordinal());
        assertEquals(1, PaymentMethod.DEBIT_ACCOUNT.ordinal());
        assertEquals(2, PaymentMethod.BOLETO.ordinal());
        assertEquals(3, PaymentMethod.PIX.ordinal());
    }

    @Test
    void testEnumCompareTo() {
        // Assert - Testa comparação entre enums
        assertTrue(PaymentMethod.CREDIT_CARD.compareTo(PaymentMethod.DEBIT_ACCOUNT) < 0);
        assertTrue(PaymentMethod.DEBIT_ACCOUNT.compareTo(PaymentMethod.BOLETO) < 0);
        assertTrue(PaymentMethod.BOLETO.compareTo(PaymentMethod.PIX) < 0);
        assertEquals(0, PaymentMethod.CREDIT_CARD.compareTo(PaymentMethod.CREDIT_CARD));
    }

    @Test
    void testEnumEquals() {
        // Assert
        assertEquals(PaymentMethod.CREDIT_CARD, PaymentMethod.CREDIT_CARD);
        assertNotEquals(PaymentMethod.CREDIT_CARD, PaymentMethod.BOLETO);
        assertNotEquals(PaymentMethod.CREDIT_CARD, null);
        assertNotEquals(PaymentMethod.CREDIT_CARD, "CREDIT_CARD");
    }

    @Test
    void testEnumHashCode() {
        // Assert - HashCode deve ser consistente
        assertEquals(PaymentMethod.CREDIT_CARD.hashCode(), PaymentMethod.CREDIT_CARD.hashCode());
        assertEquals(PaymentMethod.PIX.hashCode(), PaymentMethod.PIX.hashCode());
    }

    @Test
    void testEnumToString() {
        // Assert
        assertEquals("CREDIT_CARD", PaymentMethod.CREDIT_CARD.toString());
        assertEquals("DEBIT_ACCOUNT", PaymentMethod.DEBIT_ACCOUNT.toString());
        assertEquals("BOLETO", PaymentMethod.BOLETO.toString());
        assertEquals("PIX", PaymentMethod.PIX.toString());
    }

    @Test
    void testDescriptionUniqueness() {
        // Arrange
        PaymentMethod[] methods = PaymentMethod.values();
        
        // Assert - Todas as descrições devem ser únicas
        for (int i = 0; i < methods.length; i++) {
            for (int j = i + 1; j < methods.length; j++) {
                assertNotEquals(methods[i].getDescription(), methods[j].getDescription(),
                    "Descrições devem ser únicas: " + methods[i] + " e " + methods[j]);
            }
        }
    }

    @Test
    void testNameUniqueness() {
        // Arrange
        PaymentMethod[] methods = PaymentMethod.values();
        
        // Assert - Todos os nomes devem ser únicos
        for (int i = 0; i < methods.length; i++) {
            for (int j = i + 1; j < methods.length; j++) {
                assertNotEquals(methods[i].name(), methods[j].name(),
                    "Nomes devem ser únicos: " + methods[i] + " e " + methods[j]);
            }
        }
    }

    @Test
    void testDescriptionLength() {
        // Assert - Descrições devem ter tamanho razoável
        for (PaymentMethod method : PaymentMethod.values()) {
            assertTrue(method.getDescription().length() > 0);
            assertTrue(method.getDescription().length() <= 50, 
                "Descrição muito longa: " + method.getDescription());
        }
    }

    @Test
    void testDescriptionContainsValidCharacters() {
        // Assert - Descrições devem conter apenas letras, espaços e caracteres acentuados
        for (PaymentMethod method : PaymentMethod.values()) {
            String description = method.getDescription();
            assertTrue(description.matches("[a-zA-ZÀ-ÿ\\s]+"), 
                "Descrição contém caracteres inválidos: " + description);
        }
    }

    @Test
    void testEnumIsSerializable() {
        // Assert - Enums são serializáveis por padrão em Java
        assertTrue(java.io.Serializable.class.isAssignableFrom(PaymentMethod.class));
    }

    @Test
    void testEnumImplementsComparable() {
        // Assert - Enums implementam Comparable por padrão
        assertTrue(Comparable.class.isAssignableFrom(PaymentMethod.class));
    }

    @Test
    void testDigitalPaymentMethods() {
        // Assert - Testa métodos de pagamento digitais
        PaymentMethod[] digitalMethods = {PaymentMethod.CREDIT_CARD, PaymentMethod.DEBIT_ACCOUNT, PaymentMethod.PIX};
        
        for (PaymentMethod method : digitalMethods) {
            assertNotNull(method);
            assertNotNull(method.getDescription());
            // Métodos digitais tendem a ter processamento mais rápido
            assertTrue(method.getDescription().length() > 0);
        }
    }

    @Test
    void testTraditionalPaymentMethods() {
        // Assert - Testa métodos de pagamento tradicionais
        PaymentMethod[] traditionalMethods = {PaymentMethod.BOLETO};
        
        for (PaymentMethod method : traditionalMethods) {
            assertNotNull(method);
            assertNotNull(method.getDescription());
            assertTrue(method.getDescription().length() > 0);
        }
    }

    @Test
    void testBrazilianSpecificMethods() {
        // Assert - Testa métodos específicos do Brasil
        assertEquals("PIX", PaymentMethod.PIX.getDescription());
        assertEquals("Boleto", PaymentMethod.BOLETO.getDescription());
        
        // PIX e Boleto são específicos do mercado brasileiro
        assertTrue(PaymentMethod.PIX.name().equals("PIX"));
        assertTrue(PaymentMethod.BOLETO.name().equals("BOLETO"));
    }

    @Test
    void testInternationalMethods() {
        // Assert - Testa métodos internacionais
        assertTrue(PaymentMethod.CREDIT_CARD.getDescription().contains("Crédito"));
        assertTrue(PaymentMethod.DEBIT_ACCOUNT.getDescription().contains("Débito"));
    }

    @Test
    void testDescriptionFormatting() {
        // Assert - Testa formatação das descrições
        for (PaymentMethod method : PaymentMethod.values()) {
            String description = method.getDescription();
            
            // Primeira letra deve ser maiúscula
            assertTrue(Character.isUpperCase(description.charAt(0)),
                "Descrição deve começar com maiúscula: " + description);
            
            // Não deve começar ou terminar com espaço
            assertFalse(description.startsWith(" "), "Descrição não deve começar com espaço");
            assertFalse(description.endsWith(" "), "Descrição não deve terminar com espaço");
        }
    }

    private boolean containsValue(PaymentMethod[] array, PaymentMethod value) {
        for (PaymentMethod method : array) {
            if (method == value) {
                return true;
            }
        }
        return false;
    }
}