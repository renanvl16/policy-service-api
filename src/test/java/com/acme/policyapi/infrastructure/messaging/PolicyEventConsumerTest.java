package com.acme.policyapi.infrastructure.messaging;

import com.acme.policyapi.application.service.PolicyRequestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para PolicyEventConsumer.
 */
@ExtendWith(MockitoExtension.class)
class PolicyEventConsumerTest {

    @Mock
    private PolicyRequestService policyRequestService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private PolicyEventConsumer policyEventConsumer;

    private UUID testPolicyRequestId;
    private String testKey;

    @BeforeEach
    void setUp() {
        testPolicyRequestId = UUID.randomUUID();
        testKey = "test-key";
    }

    @Test
    void testHandlePaymentEventConfirmed() throws JsonProcessingException {
        // Arrange
        String message = "{\"eventType\":\"PAYMENT_CONFIRMED\"}";
        PolicyEventConsumer.PaymentEvent paymentEvent = createPaymentEvent("PAYMENT_CONFIRMED");
        
        when(objectMapper.readValue(message, PolicyEventConsumer.PaymentEvent.class))
            .thenReturn(paymentEvent);

        // Act
        policyEventConsumer.handlePaymentEvent(message, testKey, acknowledgment);

        // Assert
        verify(objectMapper).readValue(message, PolicyEventConsumer.PaymentEvent.class);
        verify(acknowledgment).acknowledge();
        verifyNoInteractions(policyRequestService); // Payment confirmed não chama service diretamente
    }

    @Test
    void testHandlePaymentEventRejected() throws JsonProcessingException {
        // Arrange
        String message = "{\"eventType\":\"PAYMENT_REJECTED\"}";
        PolicyEventConsumer.PaymentEvent paymentEvent = createPaymentEvent("PAYMENT_REJECTED");
        paymentEvent.setReason("Insufficient funds");
        
        when(objectMapper.readValue(message, PolicyEventConsumer.PaymentEvent.class))
            .thenReturn(paymentEvent);

        // Act
        policyEventConsumer.handlePaymentEvent(message, testKey, acknowledgment);

        // Assert
        verify(objectMapper).readValue(message, PolicyEventConsumer.PaymentEvent.class);
        verify(policyRequestService).rejectRequest(testPolicyRequestId, "Pagamento rejeitado: Insufficient funds");
        verify(acknowledgment).acknowledge();
    }

    @Test
    void testHandlePaymentEventUnknownType() throws JsonProcessingException {
        // Arrange
        String message = "{\"eventType\":\"UNKNOWN_EVENT\"}";
        PolicyEventConsumer.PaymentEvent paymentEvent = createPaymentEvent("UNKNOWN_EVENT");
        
        when(objectMapper.readValue(message, PolicyEventConsumer.PaymentEvent.class))
            .thenReturn(paymentEvent);

        // Act
        policyEventConsumer.handlePaymentEvent(message, testKey, acknowledgment);

        // Assert
        verify(objectMapper).readValue(message, PolicyEventConsumer.PaymentEvent.class);
        verify(acknowledgment).acknowledge();
        verifyNoInteractions(policyRequestService);
    }

    @Test
    void testHandlePaymentEventJsonProcessingException() throws JsonProcessingException {
        // Arrange
        String message = "invalid-json";
        when(objectMapper.readValue(message, PolicyEventConsumer.PaymentEvent.class))
            .thenThrow(new JsonProcessingException("Invalid JSON") {});

        // Act
        policyEventConsumer.handlePaymentEvent(message, testKey, acknowledgment);

        // Assert
        verify(objectMapper).readValue(message, PolicyEventConsumer.PaymentEvent.class);
        verifyNoInteractions(policyRequestService);
        verifyNoInteractions(acknowledgment); // Não deve fazer acknowledge em caso de erro
    }

    @Test
    void testHandlePaymentEventGeneralException() throws JsonProcessingException {
        // Arrange
        String message = "{\"eventType\":\"PAYMENT_REJECTED\"}";
        PolicyEventConsumer.PaymentEvent paymentEvent = createPaymentEvent("PAYMENT_REJECTED");
        
        when(objectMapper.readValue(message, PolicyEventConsumer.PaymentEvent.class))
            .thenReturn(paymentEvent);
        doThrow(new RuntimeException("Service error"))
            .when(policyRequestService).rejectRequest(any(UUID.class), anyString());

        // Act
        policyEventConsumer.handlePaymentEvent(message, testKey, acknowledgment);

        // Assert
        verify(objectMapper).readValue(message, PolicyEventConsumer.PaymentEvent.class);
        verify(policyRequestService).rejectRequest(any(UUID.class), anyString());
        verifyNoInteractions(acknowledgment); // Não deve fazer acknowledge em caso de erro
    }

    @Test
    void testHandleUnderwritingEventApproved() throws JsonProcessingException {
        // Arrange
        String message = "{\"eventType\":\"UNDERWRITING_APPROVED\"}";
        PolicyEventConsumer.UnderwritingEvent underwritingEvent = createUnderwritingEvent("UNDERWRITING_APPROVED");
        
        when(objectMapper.readValue(message, PolicyEventConsumer.UnderwritingEvent.class))
            .thenReturn(underwritingEvent);

        // Act
        policyEventConsumer.handleUnderwritingEvent(message, testKey, acknowledgment);

        // Assert
        verify(objectMapper).readValue(message, PolicyEventConsumer.UnderwritingEvent.class);
        verify(policyRequestService).approveRequest(testPolicyRequestId);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void testHandleUnderwritingEventRejected() throws JsonProcessingException {
        // Arrange
        String message = "{\"eventType\":\"UNDERWRITING_REJECTED\"}";
        PolicyEventConsumer.UnderwritingEvent underwritingEvent = createUnderwritingEvent("UNDERWRITING_REJECTED");
        underwritingEvent.setReason("High risk profile");
        
        when(objectMapper.readValue(message, PolicyEventConsumer.UnderwritingEvent.class))
            .thenReturn(underwritingEvent);

        // Act
        policyEventConsumer.handleUnderwritingEvent(message, testKey, acknowledgment);

        // Assert
        verify(objectMapper).readValue(message, PolicyEventConsumer.UnderwritingEvent.class);
        verify(policyRequestService).rejectRequest(testPolicyRequestId, "Subscrição rejeitada: High risk profile");
        verify(acknowledgment).acknowledge();
    }

    @Test
    void testHandleUnderwritingEventUnknownType() throws JsonProcessingException {
        // Arrange
        String message = "{\"eventType\":\"UNKNOWN_EVENT\"}";
        PolicyEventConsumer.UnderwritingEvent underwritingEvent = createUnderwritingEvent("UNKNOWN_EVENT");
        
        when(objectMapper.readValue(message, PolicyEventConsumer.UnderwritingEvent.class))
            .thenReturn(underwritingEvent);

        // Act
        policyEventConsumer.handleUnderwritingEvent(message, testKey, acknowledgment);

        // Assert
        verify(objectMapper).readValue(message, PolicyEventConsumer.UnderwritingEvent.class);
        verify(acknowledgment).acknowledge();
        verifyNoInteractions(policyRequestService);
    }

    @Test
    void testHandleUnderwritingEventJsonProcessingException() throws JsonProcessingException {
        // Arrange
        String message = "invalid-json";
        when(objectMapper.readValue(message, PolicyEventConsumer.UnderwritingEvent.class))
            .thenThrow(new JsonProcessingException("Invalid JSON") {});

        // Act
        policyEventConsumer.handleUnderwritingEvent(message, testKey, acknowledgment);

        // Assert
        verify(objectMapper).readValue(message, PolicyEventConsumer.UnderwritingEvent.class);
        verifyNoInteractions(policyRequestService);
        verifyNoInteractions(acknowledgment);
    }

    @Test
    void testHandleUnderwritingEventGeneralException() throws JsonProcessingException {
        // Arrange
        String message = "{\"eventType\":\"UNDERWRITING_APPROVED\"}";
        PolicyEventConsumer.UnderwritingEvent underwritingEvent = createUnderwritingEvent("UNDERWRITING_APPROVED");
        
        when(objectMapper.readValue(message, PolicyEventConsumer.UnderwritingEvent.class))
            .thenReturn(underwritingEvent);
        doThrow(new RuntimeException("Service error"))
            .when(policyRequestService).approveRequest(any(UUID.class));

        // Act
        policyEventConsumer.handleUnderwritingEvent(message, testKey, acknowledgment);

        // Assert
        verify(objectMapper).readValue(message, PolicyEventConsumer.UnderwritingEvent.class);
        verify(policyRequestService).approveRequest(testPolicyRequestId);
        verifyNoInteractions(acknowledgment);
    }

    @Test
    void testPaymentEventGettersAndSetters() {
        // Arrange
        PolicyEventConsumer.PaymentEvent paymentEvent = new PolicyEventConsumer.PaymentEvent();
        UUID policyRequestId = UUID.randomUUID();
        String eventType = "PAYMENT_CONFIRMED";
        String status = "CONFIRMED";
        String reason = "Payment successful";
        String paymentId = "PAY123";

        // Act
        paymentEvent.setPolicyRequestId(policyRequestId);
        paymentEvent.setEventType(eventType);
        paymentEvent.setStatus(status);
        paymentEvent.setReason(reason);
        paymentEvent.setPaymentId(paymentId);

        // Assert
        assertEquals(policyRequestId, paymentEvent.getPolicyRequestId());
        assertEquals(eventType, paymentEvent.getEventType());
        assertEquals(status, paymentEvent.getStatus());
        assertEquals(reason, paymentEvent.getReason());
        assertEquals(paymentId, paymentEvent.getPaymentId());
    }

    @Test
    void testPaymentEventWithNullValues() {
        // Arrange
        PolicyEventConsumer.PaymentEvent paymentEvent = new PolicyEventConsumer.PaymentEvent();

        // Assert
        assertNull(paymentEvent.getPolicyRequestId());
        assertNull(paymentEvent.getEventType());
        assertNull(paymentEvent.getStatus());
        assertNull(paymentEvent.getReason());
        assertNull(paymentEvent.getPaymentId());
    }

    @Test
    void testUnderwritingEventGettersAndSetters() {
        // Arrange
        PolicyEventConsumer.UnderwritingEvent underwritingEvent = new PolicyEventConsumer.UnderwritingEvent();
        UUID policyRequestId = UUID.randomUUID();
        String eventType = "UNDERWRITING_APPROVED";
        String status = "APPROVED";
        String reason = "Low risk profile";
        String underwriterId = "UW123";

        // Act
        underwritingEvent.setPolicyRequestId(policyRequestId);
        underwritingEvent.setEventType(eventType);
        underwritingEvent.setStatus(status);
        underwritingEvent.setReason(reason);
        underwritingEvent.setUnderwriterId(underwriterId);

        // Assert
        assertEquals(policyRequestId, underwritingEvent.getPolicyRequestId());
        assertEquals(eventType, underwritingEvent.getEventType());
        assertEquals(status, underwritingEvent.getStatus());
        assertEquals(reason, underwritingEvent.getReason());
        assertEquals(underwriterId, underwritingEvent.getUnderwriterId());
    }

    @Test
    void testUnderwritingEventWithNullValues() {
        // Arrange
        PolicyEventConsumer.UnderwritingEvent underwritingEvent = new PolicyEventConsumer.UnderwritingEvent();

        // Assert
        assertNull(underwritingEvent.getPolicyRequestId());
        assertNull(underwritingEvent.getEventType());
        assertNull(underwritingEvent.getStatus());
        assertNull(underwritingEvent.getReason());
        assertNull(underwritingEvent.getUnderwriterId());
    }

    @Test
    void testPaymentEventWithDifferentEventTypes() throws JsonProcessingException {
        // Teste diferentes tipos de eventos de pagamento
        String[] eventTypes = {"PAYMENT_CONFIRMED", "PAYMENT_REJECTED", "PAYMENT_PENDING", "PAYMENT_EXPIRED"};
        
        for (String eventType : eventTypes) {
            String message = String.format("{\"eventType\":\"%s\"}", eventType);
            PolicyEventConsumer.PaymentEvent paymentEvent = createPaymentEvent(eventType);
            
            when(objectMapper.readValue(message, PolicyEventConsumer.PaymentEvent.class))
                .thenReturn(paymentEvent);

            // Reset mocks
            reset(acknowledgment, policyRequestService);

            // Act
            policyEventConsumer.handlePaymentEvent(message, testKey, acknowledgment);

            // Assert baseado no tipo
            if ("PAYMENT_CONFIRMED".equals(eventType)) {
                verify(acknowledgment).acknowledge();
                verifyNoInteractions(policyRequestService);
            } else if ("PAYMENT_REJECTED".equals(eventType)) {
                verify(acknowledgment).acknowledge();
                verify(policyRequestService).rejectRequest(eq(testPolicyRequestId), anyString());
            } else {
                // Tipos desconhecidos
                verify(acknowledgment).acknowledge();
                verifyNoInteractions(policyRequestService);
            }
        }
    }

    @Test
    void testUnderwritingEventWithDifferentEventTypes() throws JsonProcessingException {
        // Teste diferentes tipos de eventos de underwriting
        String[] eventTypes = {"UNDERWRITING_APPROVED", "UNDERWRITING_REJECTED", "UNDERWRITING_PENDING"};
        
        for (String eventType : eventTypes) {
            String message = String.format("{\"eventType\":\"%s\"}", eventType);
            PolicyEventConsumer.UnderwritingEvent underwritingEvent = createUnderwritingEvent(eventType);
            
            when(objectMapper.readValue(message, PolicyEventConsumer.UnderwritingEvent.class))
                .thenReturn(underwritingEvent);

            // Reset mocks
            reset(acknowledgment, policyRequestService);

            // Act
            policyEventConsumer.handleUnderwritingEvent(message, testKey, acknowledgment);

            // Assert baseado no tipo
            if ("UNDERWRITING_APPROVED".equals(eventType)) {
                verify(acknowledgment).acknowledge();
                verify(policyRequestService).approveRequest(testPolicyRequestId);
            } else if ("UNDERWRITING_REJECTED".equals(eventType)) {
                verify(acknowledgment).acknowledge();
                verify(policyRequestService).rejectRequest(eq(testPolicyRequestId), anyString());
            } else {
                // Tipos desconhecidos
                verify(acknowledgment).acknowledge();
                verifyNoInteractions(policyRequestService);
            }
        }
    }

    @Test
    void testPaymentEventWithNullReason() throws JsonProcessingException {
        // Arrange
        String message = "{\"eventType\":\"PAYMENT_REJECTED\"}";
        PolicyEventConsumer.PaymentEvent paymentEvent = createPaymentEvent("PAYMENT_REJECTED");
        paymentEvent.setReason(null); // Reason é null
        
        when(objectMapper.readValue(message, PolicyEventConsumer.PaymentEvent.class))
            .thenReturn(paymentEvent);

        // Act
        policyEventConsumer.handlePaymentEvent(message, testKey, acknowledgment);

        // Assert
        verify(policyRequestService).rejectRequest(testPolicyRequestId, "Pagamento rejeitado: null");
        verify(acknowledgment).acknowledge();
    }

    @Test
    void testUnderwritingEventWithNullReason() throws JsonProcessingException {
        // Arrange
        String message = "{\"eventType\":\"UNDERWRITING_REJECTED\"}";
        PolicyEventConsumer.UnderwritingEvent underwritingEvent = createUnderwritingEvent("UNDERWRITING_REJECTED");
        underwritingEvent.setReason(null); // Reason é null
        
        when(objectMapper.readValue(message, PolicyEventConsumer.UnderwritingEvent.class))
            .thenReturn(underwritingEvent);

        // Act
        policyEventConsumer.handleUnderwritingEvent(message, testKey, acknowledgment);

        // Assert
        verify(policyRequestService).rejectRequest(testPolicyRequestId, "Subscrição rejeitada: null");
        verify(acknowledgment).acknowledge();
    }

    private PolicyEventConsumer.PaymentEvent createPaymentEvent(String eventType) {
        PolicyEventConsumer.PaymentEvent event = new PolicyEventConsumer.PaymentEvent();
        event.setPolicyRequestId(testPolicyRequestId);
        event.setEventType(eventType);
        event.setStatus("PROCESSED");
        event.setPaymentId("PAY123");
        return event;
    }

    private PolicyEventConsumer.UnderwritingEvent createUnderwritingEvent(String eventType) {
        PolicyEventConsumer.UnderwritingEvent event = new PolicyEventConsumer.UnderwritingEvent();
        event.setPolicyRequestId(testPolicyRequestId);
        event.setEventType(eventType);
        event.setStatus("PROCESSED");
        event.setUnderwriterId("UW123");
        return event;
    }
}