package com.acme.policyapi.domain.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para InsuranceCategory.
 */
class InsuranceCategoryTest {

    @Test
    void testAllEnumValues() {
        // Arrange & Act
        InsuranceCategory[] categories = InsuranceCategory.values();

        // Assert
        assertEquals(4, categories.length);
        assertTrue(containsValue(categories, InsuranceCategory.VIDA));
        assertTrue(containsValue(categories, InsuranceCategory.AUTO));
        assertTrue(containsValue(categories, InsuranceCategory.RESIDENCIAL));
        assertTrue(containsValue(categories, InsuranceCategory.EMPRESARIAL));
    }

    @Test
    void testVidaCategory() {
        // Act
        InsuranceCategory category = InsuranceCategory.VIDA;

        // Assert
        assertEquals("Vida", category.getDescription());
        assertEquals("VIDA", category.name());
    }

    @Test
    void testAutoCategory() {
        // Act
        InsuranceCategory category = InsuranceCategory.AUTO;

        // Assert
        assertEquals("Auto", category.getDescription());
        assertEquals("AUTO", category.name());
    }

    @Test
    void testResidencialCategory() {
        // Act
        InsuranceCategory category = InsuranceCategory.RESIDENCIAL;

        // Assert
        assertEquals("Residencial", category.getDescription());
        assertEquals("RESIDENCIAL", category.name());
    }

    @Test
    void testEmpresarialCategory() {
        // Act
        InsuranceCategory category = InsuranceCategory.EMPRESARIAL;

        // Assert
        assertEquals("Empresarial", category.getDescription());
        assertEquals("EMPRESARIAL", category.name());
    }

    @ParameterizedTest
    @EnumSource(InsuranceCategory.class)
    void testGetDescriptionNotNull(InsuranceCategory category) {
        // Assert
        assertNotNull(category.getDescription());
        assertFalse(category.getDescription().isEmpty());
        assertFalse(category.getDescription().isBlank());
    }

    @ParameterizedTest
    @EnumSource(InsuranceCategory.class)
    void testNameNotNull(InsuranceCategory category) {
        // Assert
        assertNotNull(category.name());
        assertFalse(category.name().isEmpty());
        assertFalse(category.name().isBlank());
    }

    @Test
    void testValueOfValidString() {
        // Act & Assert
        assertEquals(InsuranceCategory.VIDA, InsuranceCategory.valueOf("VIDA"));
        assertEquals(InsuranceCategory.AUTO, InsuranceCategory.valueOf("AUTO"));
        assertEquals(InsuranceCategory.RESIDENCIAL, InsuranceCategory.valueOf("RESIDENCIAL"));
        assertEquals(InsuranceCategory.EMPRESARIAL, InsuranceCategory.valueOf("EMPRESARIAL"));
    }

    @Test
    void testValueOfInvalidString() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            InsuranceCategory.valueOf("INVALID"));
    }

    @Test
    void testValueOfNullString() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> 
            InsuranceCategory.valueOf(null));
    }

    @Test
    void testValueOfEmptyString() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            InsuranceCategory.valueOf(""));
    }

    @Test
    void testValueOfCaseInvariant() {
        // Act & Assert - Deve ser case sensitive
        assertThrows(IllegalArgumentException.class, () -> 
            InsuranceCategory.valueOf("vida"));
        assertThrows(IllegalArgumentException.class, () -> 
            InsuranceCategory.valueOf("Vida"));
        assertThrows(IllegalArgumentException.class, () -> 
            InsuranceCategory.valueOf("auto"));
    }

    @Test
    void testEnumOrdinal() {
        // Assert - Testa a ordem dos enums
        assertEquals(0, InsuranceCategory.VIDA.ordinal());
        assertEquals(1, InsuranceCategory.AUTO.ordinal());
        assertEquals(2, InsuranceCategory.RESIDENCIAL.ordinal());
        assertEquals(3, InsuranceCategory.EMPRESARIAL.ordinal());
    }

    @Test
    void testEnumCompareTo() {
        // Assert - Testa comparação entre enums
        assertTrue(InsuranceCategory.VIDA.compareTo(InsuranceCategory.AUTO) < 0);
        assertTrue(InsuranceCategory.AUTO.compareTo(InsuranceCategory.RESIDENCIAL) < 0);
        assertTrue(InsuranceCategory.RESIDENCIAL.compareTo(InsuranceCategory.EMPRESARIAL) < 0);
        assertEquals(0, InsuranceCategory.VIDA.compareTo(InsuranceCategory.VIDA));
    }

    @Test
    void testEnumEquals() {
        // Assert
        assertEquals(InsuranceCategory.VIDA, InsuranceCategory.VIDA);
        assertNotEquals(InsuranceCategory.VIDA, InsuranceCategory.AUTO);
        assertNotEquals(InsuranceCategory.VIDA, null);
        assertNotEquals(InsuranceCategory.VIDA, "VIDA");
    }

    @Test
    void testEnumHashCode() {
        // Assert - HashCode deve ser consistente
        assertEquals(InsuranceCategory.VIDA.hashCode(), InsuranceCategory.VIDA.hashCode());
        assertEquals(InsuranceCategory.AUTO.hashCode(), InsuranceCategory.AUTO.hashCode());
    }

    @Test
    void testEnumToString() {
        // Assert
        assertEquals("VIDA", InsuranceCategory.VIDA.toString());
        assertEquals("AUTO", InsuranceCategory.AUTO.toString());
        assertEquals("RESIDENCIAL", InsuranceCategory.RESIDENCIAL.toString());
        assertEquals("EMPRESARIAL", InsuranceCategory.EMPRESARIAL.toString());
    }

    @Test
    void testDescriptionUniqueness() {
        // Arrange
        InsuranceCategory[] categories = InsuranceCategory.values();
        
        // Assert - Todas as descrições devem ser únicas
        for (int i = 0; i < categories.length; i++) {
            for (int j = i + 1; j < categories.length; j++) {
                assertNotEquals(categories[i].getDescription(), categories[j].getDescription(),
                    "Descrições devem ser únicas: " + categories[i] + " e " + categories[j]);
            }
        }
    }

    @Test
    void testNameUniqueness() {
        // Arrange
        InsuranceCategory[] categories = InsuranceCategory.values();
        
        // Assert - Todos os nomes devem ser únicos
        for (int i = 0; i < categories.length; i++) {
            for (int j = i + 1; j < categories.length; j++) {
                assertNotEquals(categories[i].name(), categories[j].name(),
                    "Nomes devem ser únicos: " + categories[i] + " e " + categories[j]);
            }
        }
    }

    @Test
    void testDescriptionLength() {
        // Assert - Descrições devem ter tamanho razoável
        for (InsuranceCategory category : InsuranceCategory.values()) {
            assertTrue(category.getDescription().length() > 0);
            assertTrue(category.getDescription().length() <= 50, 
                "Descrição muito longa: " + category.getDescription());
        }
    }

    @Test
    void testDescriptionDoesNotContainSpecialCharacters() {
        // Assert - Descrições devem conter apenas letras e espaços
        for (InsuranceCategory category : InsuranceCategory.values()) {
            String description = category.getDescription();
            assertTrue(description.matches("[a-zA-ZÀ-ÿ\\s]+"), 
                "Descrição contém caracteres inválidos: " + description);
        }
    }

    @Test
    void testEnumIsSerializable() {
        // Assert - Enums são serializáveis por padrão em Java
        assertTrue(java.io.Serializable.class.isAssignableFrom(InsuranceCategory.class));
    }

    @Test
    void testEnumImplementsComparable() {
        // Assert - Enums implementam Comparable por padrão
        assertTrue(Comparable.class.isAssignableFrom(InsuranceCategory.class));
    }

    private boolean containsValue(InsuranceCategory[] array, InsuranceCategory value) {
        for (InsuranceCategory category : array) {
            if (category == value) {
                return true;
            }
        }
        return false;
    }
}