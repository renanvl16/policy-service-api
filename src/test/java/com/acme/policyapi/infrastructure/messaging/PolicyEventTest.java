package com.acme.policyapi.infrastructure.messaging;

import com.acme.policyapi.domain.entity.PolicyRequestStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para PolicyEvent.
 */
class PolicyEventTest {

    @Test
    void testDefaultConstructor() {
        // Act
        PolicyEvent event = new PolicyEvent();

        // Assert
        assertNull(event.getPolicyRequestId());
        assertNull(event.getCustomerId());
        assertNull(event.getProductId());
        assertNull(event.getStatus());
        assertNull(event.getPreviousStatus());
        assertNull(event.getReason());
        assertNull(event.getTimestamp());
        assertNull(event.getEventType());
    }

    @Test
    void testAllArgsConstructor() {
        // Arrange
        UUID policyRequestId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String productId = "PROD123";
        PolicyRequestStatus status = PolicyRequestStatus.APPROVED;
        PolicyRequestStatus previousStatus = PolicyRequestStatus.PENDING;
        String reason = "Test reason";
        LocalDateTime timestamp = LocalDateTime.now();
        String eventType = "TEST_EVENT";

        // Act
        PolicyEvent event = new PolicyEvent(
            policyRequestId, 
            customerId, 
            productId, 
            status, 
            previousStatus, 
            reason, 
            timestamp, 
            eventType
        );

        // Assert
        assertEquals(policyRequestId, event.getPolicyRequestId());
        assertEquals(customerId, event.getCustomerId());
        assertEquals(productId, event.getProductId());
        assertEquals(status, event.getStatus());
        assertEquals(previousStatus, event.getPreviousStatus());
        assertEquals(reason, event.getReason());
        assertEquals(timestamp, event.getTimestamp());
        assertEquals(eventType, event.getEventType());
    }

    @Test
    void testBasicConstructor() {
        // Arrange
        UUID policyRequestId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String productId = "PROD123";
        PolicyRequestStatus status = PolicyRequestStatus.VALIDATED;
        String eventType = "POLICY_REQUEST_VALIDATED";

        LocalDateTime beforeCall = LocalDateTime.now();

        // Act
        PolicyEvent event = new PolicyEvent(policyRequestId, customerId, productId, status, eventType);

        LocalDateTime afterCall = LocalDateTime.now();

        // Assert
        assertEquals(policyRequestId, event.getPolicyRequestId());
        assertEquals(customerId, event.getCustomerId());
        assertEquals(productId, event.getProductId());
        assertEquals(status, event.getStatus());
        assertEquals(eventType, event.getEventType());
        
        // Timestamp deve ser definido automaticamente
        assertNotNull(event.getTimestamp());
        assertTrue(event.getTimestamp().isAfter(beforeCall.minusSeconds(1)));
        assertTrue(event.getTimestamp().isBefore(afterCall.plusSeconds(1)));
        
        // Campos não definidos devem ser null
        assertNull(event.getPreviousStatus());
        assertNull(event.getReason());
    }

    @Test
    void testExtendedConstructor() {
        // Arrange
        UUID policyRequestId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String productId = "PROD123";
        PolicyRequestStatus status = PolicyRequestStatus.REJECTED;
        PolicyRequestStatus previousStatus = PolicyRequestStatus.PENDING;
        String reason = "Risk assessment failed";
        String eventType = "POLICY_REQUEST_REJECTED";

        LocalDateTime beforeCall = LocalDateTime.now();

        // Act
        PolicyEvent event = new PolicyEvent(policyRequestId, customerId, productId, status, previousStatus, reason, eventType);

        LocalDateTime afterCall = LocalDateTime.now();

        // Assert
        assertEquals(policyRequestId, event.getPolicyRequestId());
        assertEquals(customerId, event.getCustomerId());
        assertEquals(productId, event.getProductId());
        assertEquals(status, event.getStatus());
        assertEquals(previousStatus, event.getPreviousStatus());
        assertEquals(reason, event.getReason());
        assertEquals(eventType, event.getEventType());
        
        // Timestamp deve ser definido automaticamente
        assertNotNull(event.getTimestamp());
        assertTrue(event.getTimestamp().isAfter(beforeCall.minusSeconds(1)));
        assertTrue(event.getTimestamp().isBefore(afterCall.plusSeconds(1)));
    }

    @Test
    void testGettersAndSetters() {
        // Arrange
        PolicyEvent event = new PolicyEvent();
        UUID policyRequestId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String productId = "PROD456";
        PolicyRequestStatus status = PolicyRequestStatus.CANCELLED;
        PolicyRequestStatus previousStatus = PolicyRequestStatus.VALIDATED;
        String reason = "Customer cancellation";
        LocalDateTime timestamp = LocalDateTime.now();
        String eventType = "POLICY_REQUEST_CANCELLED";

        // Act
        event.setPolicyRequestId(policyRequestId);
        event.setCustomerId(customerId);
        event.setProductId(productId);
        event.setStatus(status);
        event.setPreviousStatus(previousStatus);
        event.setReason(reason);
        event.setTimestamp(timestamp);
        event.setEventType(eventType);

        // Assert
        assertEquals(policyRequestId, event.getPolicyRequestId());
        assertEquals(customerId, event.getCustomerId());
        assertEquals(productId, event.getProductId());
        assertEquals(status, event.getStatus());
        assertEquals(previousStatus, event.getPreviousStatus());
        assertEquals(reason, event.getReason());
        assertEquals(timestamp, event.getTimestamp());
        assertEquals(eventType, event.getEventType());
    }

    @Test
    void testAllPolicyRequestStatuses() {
        // Test that all enum values can be used
        for (PolicyRequestStatus status : PolicyRequestStatus.values()) {
            PolicyEvent event = new PolicyEvent();
            event.setStatus(status);
            event.setPreviousStatus(status);
            
            assertEquals(status, event.getStatus());
            assertEquals(status, event.getPreviousStatus());
        }
    }

    @Test
    void testNullValues() {
        // Arrange & Act
        PolicyEvent event = new PolicyEvent(null, null, null, null, null);

        // Assert
        assertNull(event.getPolicyRequestId());
        assertNull(event.getCustomerId());
        assertNull(event.getProductId());
        assertNull(event.getStatus());
        assertNull(event.getEventType());
        assertNotNull(event.getTimestamp()); // Timestamp ainda é definido
    }

    @Test
    void testExtendedConstructorWithNullValues() {
        // Arrange
        UUID policyRequestId = UUID.randomUUID();
        String eventType = "TEST_EVENT";

        // Act
        PolicyEvent event = new PolicyEvent(policyRequestId, null, null, null, null, null, eventType);

        // Assert
        assertEquals(policyRequestId, event.getPolicyRequestId());
        assertEquals(eventType, event.getEventType());
        assertNull(event.getCustomerId());
        assertNull(event.getProductId());
        assertNull(event.getStatus());
        assertNull(event.getPreviousStatus());
        assertNull(event.getReason());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void testDifferentEventTypes() {
        // Test different event types
        String[] eventTypes = {
            "POLICY_REQUEST_CREATED",
            "POLICY_REQUEST_VALIDATED", 
            "POLICY_REQUEST_PENDING",
            "POLICY_REQUEST_APPROVED",
            "POLICY_REQUEST_REJECTED",
            "POLICY_REQUEST_CANCELLED"
        };

        for (String eventType : eventTypes) {
            PolicyEvent event = new PolicyEvent();
            event.setEventType(eventType);
            assertEquals(eventType, event.getEventType());
        }
    }

    @Test
    void testEmptyStringValues() {
        // Arrange
        PolicyEvent event = new PolicyEvent();

        // Act
        event.setProductId("");
        event.setReason("");
        event.setEventType("");

        // Assert
        assertEquals("", event.getProductId());
        assertEquals("", event.getReason());
        assertEquals("", event.getEventType());
    }

    @Test
    void testLongStringValues() {
        // Arrange
        PolicyEvent event = new PolicyEvent();
        String longString = "A".repeat(1000); // String muito longa

        // Act
        event.setProductId(longString);
        event.setReason(longString);
        event.setEventType(longString);

        // Assert
        assertEquals(longString, event.getProductId());
        assertEquals(longString, event.getReason());
        assertEquals(longString, event.getEventType());
    }

    @Test
    void testTimestampPrecision() {
        // Arrange
        LocalDateTime specificTime = LocalDateTime.of(2023, 12, 25, 10, 30, 45, 123456789);
        
        PolicyEvent event = new PolicyEvent();
        event.setTimestamp(specificTime);

        // Assert
        assertEquals(specificTime, event.getTimestamp());
        assertEquals(2023, event.getTimestamp().getYear());
        assertEquals(12, event.getTimestamp().getMonthValue());
        assertEquals(25, event.getTimestamp().getDayOfMonth());
        assertEquals(10, event.getTimestamp().getHour());
        assertEquals(30, event.getTimestamp().getMinute());
        assertEquals(45, event.getTimestamp().getSecond());
        assertEquals(123456789, event.getTimestamp().getNano());
    }

    @Test
    void testConstructorChaining() {
        // Test that the extended constructor properly chains to the basic constructor
        UUID policyRequestId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String productId = "PROD123";
        PolicyRequestStatus status = PolicyRequestStatus.RECEIVED;
        PolicyRequestStatus previousStatus = PolicyRequestStatus.VALIDATED;
        String reason = "Test chaining";
        String eventType = "TEST_CHAINING";

        // Act
        PolicyEvent event = new PolicyEvent(policyRequestId, customerId, productId, status, previousStatus, reason, eventType);

        // Assert - verify all fields from basic constructor are set
        assertEquals(policyRequestId, event.getPolicyRequestId());
        assertEquals(customerId, event.getCustomerId());
        assertEquals(productId, event.getProductId());
        assertEquals(status, event.getStatus());
        assertEquals(eventType, event.getEventType());
        assertNotNull(event.getTimestamp()); // Set by basic constructor
        
        // Assert - verify additional fields are set
        assertEquals(previousStatus, event.getPreviousStatus());
        assertEquals(reason, event.getReason());
    }
}