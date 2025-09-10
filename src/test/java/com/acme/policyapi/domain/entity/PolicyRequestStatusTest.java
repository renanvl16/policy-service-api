package com.acme.policyapi.domain.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para o enum PolicyRequestStatus.
 * 
 * @author Sistema ACME
 */
class PolicyRequestStatusTest {

    @Test
    void testGetDescription() {
        assertEquals("Recebido", PolicyRequestStatus.RECEIVED.getDescription());
        assertEquals("Validado", PolicyRequestStatus.VALIDATED.getDescription());
        assertEquals("Pendente", PolicyRequestStatus.PENDING.getDescription());
        assertEquals("Aprovado", PolicyRequestStatus.APPROVED.getDescription());
        assertEquals("Rejeitado", PolicyRequestStatus.REJECTED.getDescription());
        assertEquals("Cancelado", PolicyRequestStatus.CANCELLED.getDescription());
    }

    @ParameterizedTest
    @MethodSource("provideValidTransitions")
    void testValidTransitions(PolicyRequestStatus from, PolicyRequestStatus to, boolean expected) {
        assertEquals(expected, from.canTransitionTo(to));
    }

    static Stream<Arguments> provideValidTransitions() {
        return Stream.of(
            // From RECEIVED
            Arguments.of(PolicyRequestStatus.RECEIVED, PolicyRequestStatus.VALIDATED, true),
            Arguments.of(PolicyRequestStatus.RECEIVED, PolicyRequestStatus.REJECTED, true),
            Arguments.of(PolicyRequestStatus.RECEIVED, PolicyRequestStatus.CANCELLED, true),
            Arguments.of(PolicyRequestStatus.RECEIVED, PolicyRequestStatus.PENDING, false),
            Arguments.of(PolicyRequestStatus.RECEIVED, PolicyRequestStatus.APPROVED, false),

            // From VALIDATED
            Arguments.of(PolicyRequestStatus.VALIDATED, PolicyRequestStatus.PENDING, true),
            Arguments.of(PolicyRequestStatus.VALIDATED, PolicyRequestStatus.REJECTED, true),
            Arguments.of(PolicyRequestStatus.VALIDATED, PolicyRequestStatus.CANCELLED, true),
            Arguments.of(PolicyRequestStatus.VALIDATED, PolicyRequestStatus.RECEIVED, false),
            Arguments.of(PolicyRequestStatus.VALIDATED, PolicyRequestStatus.APPROVED, false),

            // From PENDING
            Arguments.of(PolicyRequestStatus.PENDING, PolicyRequestStatus.APPROVED, true),
            Arguments.of(PolicyRequestStatus.PENDING, PolicyRequestStatus.REJECTED, true),
            Arguments.of(PolicyRequestStatus.PENDING, PolicyRequestStatus.CANCELLED, true),
            Arguments.of(PolicyRequestStatus.PENDING, PolicyRequestStatus.RECEIVED, false),
            Arguments.of(PolicyRequestStatus.PENDING, PolicyRequestStatus.VALIDATED, false),

            // From final states
            Arguments.of(PolicyRequestStatus.APPROVED, PolicyRequestStatus.REJECTED, false),
            Arguments.of(PolicyRequestStatus.APPROVED, PolicyRequestStatus.CANCELLED, false),
            Arguments.of(PolicyRequestStatus.REJECTED, PolicyRequestStatus.APPROVED, false),
            Arguments.of(PolicyRequestStatus.REJECTED, PolicyRequestStatus.CANCELLED, false),
            Arguments.of(PolicyRequestStatus.CANCELLED, PolicyRequestStatus.APPROVED, false),
            Arguments.of(PolicyRequestStatus.CANCELLED, PolicyRequestStatus.REJECTED, false)
        );
    }

    @Test
    void testIsFinalState() {
        // Final states
        assertTrue(PolicyRequestStatus.APPROVED.isFinalState());
        assertTrue(PolicyRequestStatus.REJECTED.isFinalState());
        assertTrue(PolicyRequestStatus.CANCELLED.isFinalState());

        // Non-final states
        assertFalse(PolicyRequestStatus.RECEIVED.isFinalState());
        assertFalse(PolicyRequestStatus.VALIDATED.isFinalState());
        assertFalse(PolicyRequestStatus.PENDING.isFinalState());
    }

    @Test
    void testAllStatusesHaveDescriptions() {
        for (PolicyRequestStatus status : PolicyRequestStatus.values()) {
            assertNotNull(status.getDescription());
            assertFalse(status.getDescription().isEmpty());
        }
    }
}