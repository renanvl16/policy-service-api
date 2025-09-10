package com.acme.policyapi.infrastructure.persistence.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StatusHistoryJpaEntityTest {

    private StatusHistoryJpaEntity entity;

    @BeforeEach
    void setUp() {
        entity = new StatusHistoryJpaEntity();
    }

    @Test
    void testDefaultConstructor() {
        StatusHistoryJpaEntity newEntity = new StatusHistoryJpaEntity();
        
        assertNull(newEntity.getId());
        assertNull(newEntity.getPolicyRequestId());
        assertNull(newEntity.getStatus());
        assertNull(newEntity.getTimestamp());
        assertNull(newEntity.getReason());
    }

    @Test
    void testAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        UUID policyRequestId = UUID.randomUUID();
        String status = "VALIDATED";
        LocalDateTime timestamp = LocalDateTime.of(2025, 1, 15, 10, 30, 0);
        String reason = "Documentos aprovados";

        StatusHistoryJpaEntity allArgsEntity = new StatusHistoryJpaEntity(
                id, policyRequestId, status, timestamp, reason
        );

        assertEquals(id, allArgsEntity.getId());
        assertEquals(policyRequestId, allArgsEntity.getPolicyRequestId());
        assertEquals(status, allArgsEntity.getStatus());
        assertEquals(timestamp, allArgsEntity.getTimestamp());
        assertEquals(reason, allArgsEntity.getReason());
    }

    @Test
    void testBuilder() {
        UUID id = UUID.randomUUID();
        UUID policyRequestId = UUID.randomUUID();
        String status = "PENDING";
        LocalDateTime timestamp = LocalDateTime.of(2025, 1, 20, 14, 45, 30);
        String reason = "Aguardando pagamento";

        StatusHistoryJpaEntity builtEntity = StatusHistoryJpaEntity.builder()
                .id(id)
                .policyRequestId(policyRequestId)
                .status(status)
                .timestamp(timestamp)
                .reason(reason)
                .build();

        assertEquals(id, builtEntity.getId());
        assertEquals(policyRequestId, builtEntity.getPolicyRequestId());
        assertEquals(status, builtEntity.getStatus());
        assertEquals(timestamp, builtEntity.getTimestamp());
        assertEquals(reason, builtEntity.getReason());
    }

    @Test
    void testGettersAndSetters() {
        UUID id = UUID.randomUUID();
        UUID policyRequestId = UUID.randomUUID();
        String status = "APPROVED";
        LocalDateTime timestamp = LocalDateTime.now();
        String reason = "Todos os critérios atendidos";

        entity.setId(id);
        entity.setPolicyRequestId(policyRequestId);
        entity.setStatus(status);
        entity.setTimestamp(timestamp);
        entity.setReason(reason);

        assertEquals(id, entity.getId());
        assertEquals(policyRequestId, entity.getPolicyRequestId());
        assertEquals(status, entity.getStatus());
        assertEquals(timestamp, entity.getTimestamp());
        assertEquals(reason, entity.getReason());
    }

    @Test
    void testSettersWithNullValues() {
        entity.setId(null);
        entity.setPolicyRequestId(null);
        entity.setStatus(null);
        entity.setTimestamp(null);
        entity.setReason(null);

        assertNull(entity.getId());
        assertNull(entity.getPolicyRequestId());
        assertNull(entity.getStatus());
        assertNull(entity.getTimestamp());
        assertNull(entity.getReason());
    }

    @Test
    void testBuilderWithPartialValues() {
        UUID policyRequestId = UUID.randomUUID();
        String status = "REJECTED";
        LocalDateTime timestamp = LocalDateTime.now();

        StatusHistoryJpaEntity partialEntity = StatusHistoryJpaEntity.builder()
                .policyRequestId(policyRequestId)
                .status(status)
                .timestamp(timestamp)
                .build();

        assertNull(partialEntity.getId()); // não foi setado
        assertEquals(policyRequestId, partialEntity.getPolicyRequestId());
        assertEquals(status, partialEntity.getStatus());
        assertEquals(timestamp, partialEntity.getTimestamp());
        assertNull(partialEntity.getReason()); // não foi setado
    }

    @Test
    void testAllPossibleStatusValues() {
        String[] statuses = {
            "RECEIVED", "VALIDATED", "PENDING", "APPROVED", "REJECTED", "CANCELLED"
        };

        for (String status : statuses) {
            entity.setStatus(status);
            assertEquals(status, entity.getStatus());
        }
    }

    @Test
    void testReasonLengthVariations() {
        // Teste com motivo curto
        String shortReason = "OK";
        entity.setReason(shortReason);
        assertEquals(shortReason, entity.getReason());

        // Teste com motivo longo (próximo ao limite de 500 chars)
        String longReason = "A".repeat(400) + " - Motivo detalhado para rejeição da apólice";
        entity.setReason(longReason);
        assertEquals(longReason, entity.getReason());

        // Teste com motivo vazio
        String emptyReason = "";
        entity.setReason(emptyReason);
        assertEquals(emptyReason, entity.getReason());
    }

    @Test
    void testTimestampPrecision() {
        LocalDateTime preciseTimestamp = LocalDateTime.of(2025, 3, 15, 9, 45, 30, 123456789);
        
        entity.setTimestamp(preciseTimestamp);
        
        assertEquals(preciseTimestamp, entity.getTimestamp());
        assertEquals(2025, entity.getTimestamp().getYear());
        assertEquals(3, entity.getTimestamp().getMonthValue());
        assertEquals(15, entity.getTimestamp().getDayOfMonth());
        assertEquals(9, entity.getTimestamp().getHour());
        assertEquals(45, entity.getTimestamp().getMinute());
        assertEquals(30, entity.getTimestamp().getSecond());
        assertEquals(123456789, entity.getTimestamp().getNano());
    }

    @Test
    void testBuilderChaining() {
        UUID id = UUID.randomUUID();
        UUID policyRequestId = UUID.randomUUID();

        StatusHistoryJpaEntity chainedEntity = StatusHistoryJpaEntity.builder()
                .id(id)
                .policyRequestId(policyRequestId)
                .status("RECEIVED")
                .timestamp(LocalDateTime.now())
                .reason("Solicitação recebida pelo sistema")
                .build();

        assertNotNull(chainedEntity);
        assertEquals(id, chainedEntity.getId());
        assertEquals(policyRequestId, chainedEntity.getPolicyRequestId());
        assertEquals("RECEIVED", chainedEntity.getStatus());
        assertNotNull(chainedEntity.getTimestamp());
        assertEquals("Solicitação recebida pelo sistema", chainedEntity.getReason());
    }

    @Test
    void testMultipleInstancesIndependence() {
        StatusHistoryJpaEntity entity1 = new StatusHistoryJpaEntity();
        StatusHistoryJpaEntity entity2 = new StatusHistoryJpaEntity();

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        entity1.setId(id1);
        entity1.setStatus("APPROVED");
        
        entity2.setId(id2);
        entity2.setStatus("REJECTED");

        assertEquals(id1, entity1.getId());
        assertEquals("APPROVED", entity1.getStatus());
        
        assertEquals(id2, entity2.getId());
        assertEquals("REJECTED", entity2.getStatus());
        
        // Verificar que as instâncias são independentes
        assertNotEquals(entity1.getId(), entity2.getId());
        assertNotEquals(entity1.getStatus(), entity2.getStatus());
    }

    @Test
    void testBuilderWithNullValues() {
        StatusHistoryJpaEntity nullEntity = StatusHistoryJpaEntity.builder()
                .id(null)
                .policyRequestId(null)
                .status(null)
                .timestamp(null)
                .reason(null)
                .build();

        assertNull(nullEntity.getId());
        assertNull(nullEntity.getPolicyRequestId());
        assertNull(nullEntity.getStatus());
        assertNull(nullEntity.getTimestamp());
        assertNull(nullEntity.getReason());
    }

    @Test
    void testReasonSpecialCharacters() {
        String specialCharReason = "Motivo com acentuação: não aprovado! @#$%^&*()_+-={}[]|\\:;\"'<>?,./ 中文 العربية";
        
        entity.setReason(specialCharReason);
        
        assertEquals(specialCharReason, entity.getReason());
    }
}