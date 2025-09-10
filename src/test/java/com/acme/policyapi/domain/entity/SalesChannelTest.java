package com.acme.policyapi.domain.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para SalesChannel.
 */
class SalesChannelTest {

    @Test
    void testAllEnumValues() {
        // Arrange & Act
        SalesChannel[] channels = SalesChannel.values();

        // Assert
        assertEquals(5, channels.length);
        assertTrue(containsValue(channels, SalesChannel.MOBILE));
        assertTrue(containsValue(channels, SalesChannel.WHATSAPP));
        assertTrue(containsValue(channels, SalesChannel.WEBSITE));
        assertTrue(containsValue(channels, SalesChannel.PRESENCIAL));
        assertTrue(containsValue(channels, SalesChannel.TELEFONE));
    }

    @Test
    void testMobileChannel() {
        // Act
        SalesChannel channel = SalesChannel.MOBILE;

        // Assert
        assertEquals("Mobile", channel.getDescription());
        assertEquals("MOBILE", channel.name());
    }

    @Test
    void testWhatsAppChannel() {
        // Act
        SalesChannel channel = SalesChannel.WHATSAPP;

        // Assert
        assertEquals("WhatsApp", channel.getDescription());
        assertEquals("WHATSAPP", channel.name());
    }

    @Test
    void testWebsiteChannel() {
        // Act
        SalesChannel channel = SalesChannel.WEBSITE;

        // Assert
        assertEquals("Web Site", channel.getDescription());
        assertEquals("WEBSITE", channel.name());
    }

    @Test
    void testPresencialChannel() {
        // Act
        SalesChannel channel = SalesChannel.PRESENCIAL;

        // Assert
        assertEquals("Presencial", channel.getDescription());
        assertEquals("PRESENCIAL", channel.name());
    }

    @Test
    void testTelefoneChannel() {
        // Act
        SalesChannel channel = SalesChannel.TELEFONE;

        // Assert
        assertEquals("Telefone", channel.getDescription());
        assertEquals("TELEFONE", channel.name());
    }

    @ParameterizedTest
    @EnumSource(SalesChannel.class)
    void testGetDescriptionNotNull(SalesChannel channel) {
        // Assert
        assertNotNull(channel.getDescription());
        assertFalse(channel.getDescription().isEmpty());
        assertFalse(channel.getDescription().isBlank());
    }

    @ParameterizedTest
    @EnumSource(SalesChannel.class)
    void testNameNotNull(SalesChannel channel) {
        // Assert
        assertNotNull(channel.name());
        assertFalse(channel.name().isEmpty());
        assertFalse(channel.name().isBlank());
    }

    @Test
    void testValueOfValidString() {
        // Act & Assert
        assertEquals(SalesChannel.MOBILE, SalesChannel.valueOf("MOBILE"));
        assertEquals(SalesChannel.WHATSAPP, SalesChannel.valueOf("WHATSAPP"));
        assertEquals(SalesChannel.WEBSITE, SalesChannel.valueOf("WEBSITE"));
        assertEquals(SalesChannel.PRESENCIAL, SalesChannel.valueOf("PRESENCIAL"));
        assertEquals(SalesChannel.TELEFONE, SalesChannel.valueOf("TELEFONE"));
    }

    @Test
    void testValueOfInvalidString() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            SalesChannel.valueOf("INVALID_CHANNEL"));
    }

    @Test
    void testValueOfNullString() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> 
            SalesChannel.valueOf(null));
    }

    @Test
    void testValueOfEmptyString() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            SalesChannel.valueOf(""));
    }

    @Test
    void testValueOfCaseInvariant() {
        // Act & Assert - Deve ser case sensitive
        assertThrows(IllegalArgumentException.class, () -> 
            SalesChannel.valueOf("mobile"));
        assertThrows(IllegalArgumentException.class, () -> 
            SalesChannel.valueOf("Mobile"));
        assertThrows(IllegalArgumentException.class, () -> 
            SalesChannel.valueOf("whatsapp"));
        assertThrows(IllegalArgumentException.class, () -> 
            SalesChannel.valueOf("website"));
    }

    @Test
    void testEnumOrdinal() {
        // Assert - Testa a ordem dos enums
        assertEquals(0, SalesChannel.MOBILE.ordinal());
        assertEquals(1, SalesChannel.WHATSAPP.ordinal());
        assertEquals(2, SalesChannel.WEBSITE.ordinal());
        assertEquals(3, SalesChannel.PRESENCIAL.ordinal());
        assertEquals(4, SalesChannel.TELEFONE.ordinal());
    }

    @Test
    void testEnumCompareTo() {
        // Assert - Testa comparação entre enums
        assertTrue(SalesChannel.MOBILE.compareTo(SalesChannel.WHATSAPP) < 0);
        assertTrue(SalesChannel.WHATSAPP.compareTo(SalesChannel.WEBSITE) < 0);
        assertTrue(SalesChannel.WEBSITE.compareTo(SalesChannel.PRESENCIAL) < 0);
        assertTrue(SalesChannel.PRESENCIAL.compareTo(SalesChannel.TELEFONE) < 0);
        assertEquals(0, SalesChannel.MOBILE.compareTo(SalesChannel.MOBILE));
    }

    @Test
    void testEnumEquals() {
        // Assert
        assertEquals(SalesChannel.MOBILE, SalesChannel.MOBILE);
        assertNotEquals(SalesChannel.MOBILE, SalesChannel.WEBSITE);
        assertNotEquals(SalesChannel.MOBILE, null);
        assertNotEquals(SalesChannel.MOBILE, "MOBILE");
    }

    @Test
    void testEnumHashCode() {
        // Assert - HashCode deve ser consistente
        assertEquals(SalesChannel.MOBILE.hashCode(), SalesChannel.MOBILE.hashCode());
        assertEquals(SalesChannel.WHATSAPP.hashCode(), SalesChannel.WHATSAPP.hashCode());
    }

    @Test
    void testEnumToString() {
        // Assert
        assertEquals("MOBILE", SalesChannel.MOBILE.toString());
        assertEquals("WHATSAPP", SalesChannel.WHATSAPP.toString());
        assertEquals("WEBSITE", SalesChannel.WEBSITE.toString());
        assertEquals("PRESENCIAL", SalesChannel.PRESENCIAL.toString());
        assertEquals("TELEFONE", SalesChannel.TELEFONE.toString());
    }

    @Test
    void testDescriptionUniqueness() {
        // Arrange
        SalesChannel[] channels = SalesChannel.values();
        
        // Assert - Todas as descrições devem ser únicas
        for (int i = 0; i < channels.length; i++) {
            for (int j = i + 1; j < channels.length; j++) {
                assertNotEquals(channels[i].getDescription(), channels[j].getDescription(),
                    "Descrições devem ser únicas: " + channels[i] + " e " + channels[j]);
            }
        }
    }

    @Test
    void testNameUniqueness() {
        // Arrange
        SalesChannel[] channels = SalesChannel.values();
        
        // Assert - Todos os nomes devem ser únicos
        for (int i = 0; i < channels.length; i++) {
            for (int j = i + 1; j < channels.length; j++) {
                assertNotEquals(channels[i].name(), channels[j].name(),
                    "Nomes devem ser únicos: " + channels[i] + " e " + channels[j]);
            }
        }
    }

    @Test
    void testDescriptionLength() {
        // Assert - Descrições devem ter tamanho razoável
        for (SalesChannel channel : SalesChannel.values()) {
            assertTrue(channel.getDescription().length() > 0);
            assertTrue(channel.getDescription().length() <= 50, 
                "Descrição muito longa: " + channel.getDescription());
        }
    }

    @Test
    void testDescriptionContainsValidCharacters() {
        // Assert - Descrições podem conter letras, espaços e caracteres especiais
        for (SalesChannel channel : SalesChannel.values()) {
            String description = channel.getDescription();
            // Permite letras, espaços e alguns caracteres especiais como em "Web Site"
            assertTrue(description.matches("[a-zA-ZÀ-ÿ\\s]+"), 
                "Descrição contém caracteres inválidos: " + description);
        }
    }

    @Test
    void testEnumIsSerializable() {
        // Assert - Enums são serializáveis por padrão em Java
        assertTrue(java.io.Serializable.class.isAssignableFrom(SalesChannel.class));
    }

    @Test
    void testEnumImplementsComparable() {
        // Assert - Enums implementam Comparable por padrão
        assertTrue(Comparable.class.isAssignableFrom(SalesChannel.class));
    }

    @Test
    void testDigitalChannels() {
        // Assert - Testa canais digitais
        SalesChannel[] digitalChannels = {
            SalesChannel.MOBILE, 
            SalesChannel.WHATSAPP, 
            SalesChannel.WEBSITE
        };
        
        for (SalesChannel channel : digitalChannels) {
            assertNotNull(channel);
            assertNotNull(channel.getDescription());
            // Canais digitais são mais modernos e eficientes
            assertTrue(channel.getDescription().length() > 0);
        }
    }

    @Test
    void testTraditionalChannels() {
        // Assert - Testa canais tradicionais
        SalesChannel[] traditionalChannels = {
            SalesChannel.PRESENCIAL, 
            SalesChannel.TELEFONE
        };
        
        for (SalesChannel channel : traditionalChannels) {
            assertNotNull(channel);
            assertNotNull(channel.getDescription());
            assertTrue(channel.getDescription().length() > 0);
        }
    }

    @Test
    void testSocialMediaChannels() {
        // Assert - Testa canais de mídia social
        assertEquals("WhatsApp", SalesChannel.WHATSAPP.getDescription());
        assertTrue(SalesChannel.WHATSAPP.name().equals("WHATSAPP"));
    }

    @Test
    void testWebChannels() {
        // Assert - Testa canais web
        SalesChannel[] webChannels = {SalesChannel.WEBSITE, SalesChannel.MOBILE};
        
        for (SalesChannel channel : webChannels) {
            assertNotNull(channel.getDescription());
            // Canais web devem ter descrições claras
            assertFalse(channel.getDescription().isEmpty());
        }
    }

    @Test
    void testDirectContactChannels() {
        // Assert - Testa canais de contato direto
        SalesChannel[] directChannels = {SalesChannel.PRESENCIAL, SalesChannel.TELEFONE};
        
        for (SalesChannel channel : directChannels) {
            assertNotNull(channel.getDescription());
            // Canais diretos permitem interação humana
            assertTrue(channel.getDescription().length() > 0);
        }
    }

    @Test
    void testDescriptionFormatting() {
        // Assert - Testa formatação das descrições
        for (SalesChannel channel : SalesChannel.values()) {
            String description = channel.getDescription();
            
            // Primeira letra deve ser maiúscula
            assertTrue(Character.isUpperCase(description.charAt(0)),
                "Descrição deve começar com maiúscula: " + description);
            
            // Não deve começar ou terminar com espaço
            assertFalse(description.startsWith(" "), "Descrição não deve começar com espaço");
            assertFalse(description.endsWith(" "), "Descrição não deve terminar com espaço");
        }
    }

    @Test
    void testChannelAvailability() {
        // Assert - Todos os canais devem estar disponíveis
        for (SalesChannel channel : SalesChannel.values()) {
            assertNotNull(channel);
            assertNotNull(channel.getDescription());
            assertNotNull(channel.name());
            
            // Canal deve ter uma representação string válida
            assertFalse(channel.toString().isEmpty());
        }
    }

    @Test
    void testMultiChannelSupport() {
        // Assert - Sistema deve suportar múltiplos canais
        assertTrue(SalesChannel.values().length >= 3, 
            "Sistema deve suportar múltiplos canais de venda");
        
        // Deve incluir pelo menos um canal digital e um tradicional
        boolean hasDigital = false;
        boolean hasTraditional = false;
        
        for (SalesChannel channel : SalesChannel.values()) {
            if (channel == SalesChannel.MOBILE || channel == SalesChannel.WEBSITE || channel == SalesChannel.WHATSAPP) {
                hasDigital = true;
            }
            if (channel == SalesChannel.PRESENCIAL || channel == SalesChannel.TELEFONE) {
                hasTraditional = true;
            }
        }
        
        assertTrue(hasDigital, "Deve ter pelo menos um canal digital");
        assertTrue(hasTraditional, "Deve ter pelo menos um canal tradicional");
    }

    private boolean containsValue(SalesChannel[] array, SalesChannel value) {
        for (SalesChannel channel : array) {
            if (channel == value) {
                return true;
            }
        }
        return false;
    }
}