package com.acme.policyapi.infrastructure.messaging;

import com.acme.policyapi.domain.entity.PolicyRequest;
import com.acme.policyapi.domain.entity.PolicyRequestStatus;
import com.acme.policyapi.domain.entity.StatusHistory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para PolicyEventPublisher.
 */
@ExtendWith(MockitoExtension.class)
class PolicyEventPublisherTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PolicyEventPublisher policyEventPublisher;

    private PolicyRequest testPolicyRequest;
    private String testTopicName = "test-topic";

    @BeforeEach
    void setUp() {
        // Definir o nome do tópico usando ReflectionTestUtils
        ReflectionTestUtils.setField(policyEventPublisher, "policyEventsTopicName", testTopicName);

        // Criar PolicyRequest de teste
        testPolicyRequest = new PolicyRequest();
        testPolicyRequest.setId(UUID.randomUUID());
        testPolicyRequest.setCustomerId(UUID.randomUUID());
        testPolicyRequest.setProductId("PROD123");
        testPolicyRequest.setStatus(PolicyRequestStatus.RECEIVED);
        testPolicyRequest.setHistory(new ArrayList<>());
    }

    @Test
    void testPublishPolicyRequestCreated() throws JsonProcessingException {
        // Arrange
        String expectedJson = "{\"eventType\":\"POLICY_REQUEST_CREATED\"}";
        when(objectMapper.writeValueAsString(any(PolicyEvent.class))).thenReturn(expectedJson);

        // Act
        policyEventPublisher.publishPolicyRequestCreated(testPolicyRequest);

        // Assert
        ArgumentCaptor<PolicyEvent> eventCaptor = ArgumentCaptor.forClass(PolicyEvent.class);
        verify(objectMapper).writeValueAsString(eventCaptor.capture());

        PolicyEvent capturedEvent = eventCaptor.getValue();
        assertEquals(testPolicyRequest.getId(), capturedEvent.getPolicyRequestId());
        assertEquals(testPolicyRequest.getCustomerId(), capturedEvent.getCustomerId());
        assertEquals(testPolicyRequest.getProductId(), capturedEvent.getProductId());
        assertEquals(testPolicyRequest.getStatus(), capturedEvent.getStatus());
        assertEquals("POLICY_REQUEST_CREATED", capturedEvent.getEventType());
        assertNotNull(capturedEvent.getTimestamp());

        verify(kafkaTemplate).send(testTopicName, testPolicyRequest.getId().toString(), expectedJson);
    }

    @Test
    void testPublishPolicyRequestValidated() throws JsonProcessingException {
        // Arrange
        testPolicyRequest.setStatus(PolicyRequestStatus.VALIDATED);
        String expectedJson = "{\"eventType\":\"POLICY_REQUEST_VALIDATED\"}";
        when(objectMapper.writeValueAsString(any(PolicyEvent.class))).thenReturn(expectedJson);

        // Act
        policyEventPublisher.publishPolicyRequestValidated(testPolicyRequest);

        // Assert
        ArgumentCaptor<PolicyEvent> eventCaptor = ArgumentCaptor.forClass(PolicyEvent.class);
        verify(objectMapper).writeValueAsString(eventCaptor.capture());

        PolicyEvent capturedEvent = eventCaptor.getValue();
        assertEquals(testPolicyRequest.getId(), capturedEvent.getPolicyRequestId());
        assertEquals(PolicyRequestStatus.VALIDATED, capturedEvent.getStatus());
        assertEquals("POLICY_REQUEST_VALIDATED", capturedEvent.getEventType());

        verify(kafkaTemplate).send(testTopicName, testPolicyRequest.getId().toString(), expectedJson);
    }

    @Test
    void testPublishPolicyRequestPending() throws JsonProcessingException {
        // Arrange
        testPolicyRequest.setStatus(PolicyRequestStatus.PENDING);
        String expectedJson = "{\"eventType\":\"POLICY_REQUEST_PENDING\"}";
        when(objectMapper.writeValueAsString(any(PolicyEvent.class))).thenReturn(expectedJson);

        // Act
        policyEventPublisher.publishPolicyRequestPending(testPolicyRequest);

        // Assert
        ArgumentCaptor<PolicyEvent> eventCaptor = ArgumentCaptor.forClass(PolicyEvent.class);
        verify(objectMapper).writeValueAsString(eventCaptor.capture());

        PolicyEvent capturedEvent = eventCaptor.getValue();
        assertEquals(testPolicyRequest.getId(), capturedEvent.getPolicyRequestId());
        assertEquals(PolicyRequestStatus.PENDING, capturedEvent.getStatus());
        assertEquals("POLICY_REQUEST_PENDING", capturedEvent.getEventType());

        verify(kafkaTemplate).send(testTopicName, testPolicyRequest.getId().toString(), expectedJson);
    }

    @Test
    void testPublishPolicyRequestApproved() throws JsonProcessingException {
        // Arrange
        testPolicyRequest.setStatus(PolicyRequestStatus.APPROVED);
        String expectedJson = "{\"eventType\":\"POLICY_REQUEST_APPROVED\"}";
        when(objectMapper.writeValueAsString(any(PolicyEvent.class))).thenReturn(expectedJson);

        // Act
        policyEventPublisher.publishPolicyRequestApproved(testPolicyRequest);

        // Assert
        ArgumentCaptor<PolicyEvent> eventCaptor = ArgumentCaptor.forClass(PolicyEvent.class);
        verify(objectMapper).writeValueAsString(eventCaptor.capture());

        PolicyEvent capturedEvent = eventCaptor.getValue();
        assertEquals(testPolicyRequest.getId(), capturedEvent.getPolicyRequestId());
        assertEquals(PolicyRequestStatus.APPROVED, capturedEvent.getStatus());
        assertEquals("POLICY_REQUEST_APPROVED", capturedEvent.getEventType());

        verify(kafkaTemplate).send(testTopicName, testPolicyRequest.getId().toString(), expectedJson);
    }

    @Test
    void testPublishPolicyRequestRejected() throws JsonProcessingException {
        // Arrange
        testPolicyRequest.setStatus(PolicyRequestStatus.REJECTED);
        List<StatusHistory> history = new ArrayList<>();
        history.add(createStatusHistory(PolicyRequestStatus.REJECTED, "Rejected due to high risk"));
        testPolicyRequest.setHistory(history);

        String expectedJson = "{\"eventType\":\"POLICY_REQUEST_REJECTED\"}";
        when(objectMapper.writeValueAsString(any(PolicyEvent.class))).thenReturn(expectedJson);

        // Act
        policyEventPublisher.publishPolicyRequestRejected(testPolicyRequest);

        // Assert
        ArgumentCaptor<PolicyEvent> eventCaptor = ArgumentCaptor.forClass(PolicyEvent.class);
        verify(objectMapper).writeValueAsString(eventCaptor.capture());

        PolicyEvent capturedEvent = eventCaptor.getValue();
        assertEquals(testPolicyRequest.getId(), capturedEvent.getPolicyRequestId());
        assertEquals(PolicyRequestStatus.REJECTED, capturedEvent.getStatus());
        assertEquals("POLICY_REQUEST_REJECTED", capturedEvent.getEventType());
        assertEquals("Rejected due to high risk", capturedEvent.getReason());

        verify(kafkaTemplate).send(testTopicName, testPolicyRequest.getId().toString(), expectedJson);
    }

    @Test
    void testPublishPolicyRequestRejectedWithoutHistory() throws JsonProcessingException {
        // Arrange
        testPolicyRequest.setStatus(PolicyRequestStatus.REJECTED);
        testPolicyRequest.setHistory(null); // Sem histórico

        String expectedJson = "{\"eventType\":\"POLICY_REQUEST_REJECTED\"}";
        when(objectMapper.writeValueAsString(any(PolicyEvent.class))).thenReturn(expectedJson);

        // Act
        policyEventPublisher.publishPolicyRequestRejected(testPolicyRequest);

        // Assert
        ArgumentCaptor<PolicyEvent> eventCaptor = ArgumentCaptor.forClass(PolicyEvent.class);
        verify(objectMapper).writeValueAsString(eventCaptor.capture());

        PolicyEvent capturedEvent = eventCaptor.getValue();
        assertEquals("POLICY_REQUEST_REJECTED", capturedEvent.getEventType());
        assertNull(capturedEvent.getReason()); // Reason deve ser null quando não há histórico

        verify(kafkaTemplate).send(testTopicName, testPolicyRequest.getId().toString(), expectedJson);
    }

    @Test
    void testPublishPolicyRequestRejectedWithEmptyHistory() throws JsonProcessingException {
        // Arrange
        testPolicyRequest.setStatus(PolicyRequestStatus.REJECTED);
        testPolicyRequest.setHistory(new ArrayList<>()); // Histórico vazio

        String expectedJson = "{\"eventType\":\"POLICY_REQUEST_REJECTED\"}";
        when(objectMapper.writeValueAsString(any(PolicyEvent.class))).thenReturn(expectedJson);

        // Act
        policyEventPublisher.publishPolicyRequestRejected(testPolicyRequest);

        // Assert
        ArgumentCaptor<PolicyEvent> eventCaptor = ArgumentCaptor.forClass(PolicyEvent.class);
        verify(objectMapper).writeValueAsString(eventCaptor.capture());

        PolicyEvent capturedEvent = eventCaptor.getValue();
        assertEquals("POLICY_REQUEST_REJECTED", capturedEvent.getEventType());
        assertNull(capturedEvent.getReason()); // Reason deve ser null quando histórico está vazio

        verify(kafkaTemplate).send(testTopicName, testPolicyRequest.getId().toString(), expectedJson);
    }

    @Test
    void testPublishPolicyRequestCancelled() throws JsonProcessingException {
        // Arrange
        testPolicyRequest.setStatus(PolicyRequestStatus.CANCELLED);
        List<StatusHistory> history = new ArrayList<>();
        history.add(createStatusHistory(PolicyRequestStatus.CANCELLED, "Cancelled by customer"));
        testPolicyRequest.setHistory(history);

        String expectedJson = "{\"eventType\":\"POLICY_REQUEST_CANCELLED\"}";
        when(objectMapper.writeValueAsString(any(PolicyEvent.class))).thenReturn(expectedJson);

        // Act
        policyEventPublisher.publishPolicyRequestCancelled(testPolicyRequest);

        // Assert
        ArgumentCaptor<PolicyEvent> eventCaptor = ArgumentCaptor.forClass(PolicyEvent.class);
        verify(objectMapper).writeValueAsString(eventCaptor.capture());

        PolicyEvent capturedEvent = eventCaptor.getValue();
        assertEquals(testPolicyRequest.getId(), capturedEvent.getPolicyRequestId());
        assertEquals(PolicyRequestStatus.CANCELLED, capturedEvent.getStatus());
        assertEquals("POLICY_REQUEST_CANCELLED", capturedEvent.getEventType());
        assertEquals("Cancelled by customer", capturedEvent.getReason());

        verify(kafkaTemplate).send(testTopicName, testPolicyRequest.getId().toString(), expectedJson);
    }

    @Test
    void testPublishPolicyRequestCancelledWithoutHistory() throws JsonProcessingException {
        // Arrange
        testPolicyRequest.setStatus(PolicyRequestStatus.CANCELLED);
        testPolicyRequest.setHistory(null); // Sem histórico

        String expectedJson = "{\"eventType\":\"POLICY_REQUEST_CANCELLED\"}";
        when(objectMapper.writeValueAsString(any(PolicyEvent.class))).thenReturn(expectedJson);

        // Act
        policyEventPublisher.publishPolicyRequestCancelled(testPolicyRequest);

        // Assert
        ArgumentCaptor<PolicyEvent> eventCaptor = ArgumentCaptor.forClass(PolicyEvent.class);
        verify(objectMapper).writeValueAsString(eventCaptor.capture());

        PolicyEvent capturedEvent = eventCaptor.getValue();
        assertEquals("POLICY_REQUEST_CANCELLED", capturedEvent.getEventType());
        assertNull(capturedEvent.getReason()); // Reason deve ser null quando não há histórico

        verify(kafkaTemplate).send(testTopicName, testPolicyRequest.getId().toString(), expectedJson);
    }

    @Test
    void testPublishEventWithJsonProcessingException() throws JsonProcessingException {
        // Arrange
        when(objectMapper.writeValueAsString(any(PolicyEvent.class)))
            .thenThrow(new JsonProcessingException("Serialization error") {});

        // Act
        policyEventPublisher.publishPolicyRequestCreated(testPolicyRequest);

        // Assert
        verify(objectMapper).writeValueAsString(any(PolicyEvent.class));
        verifyNoInteractions(kafkaTemplate); // KafkaTemplate não deve ser chamado em caso de erro de serialização
    }

    @Test
    void testPublishEventWithKafkaException() throws JsonProcessingException {
        // Arrange
        String expectedJson = "{\"eventType\":\"POLICY_REQUEST_CREATED\"}";
        when(objectMapper.writeValueAsString(any(PolicyEvent.class))).thenReturn(expectedJson);
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Kafka error"));

        // Act
        policyEventPublisher.publishPolicyRequestCreated(testPolicyRequest);

        // Assert
        verify(objectMapper).writeValueAsString(any(PolicyEvent.class));
        verify(kafkaTemplate).send(testTopicName, testPolicyRequest.getId().toString(), expectedJson);
        // O método deve capturar e logar a exceção, mas não relançar
    }

    @Test
    void testGetLatestHistoryReasonWithMultipleEntries() throws JsonProcessingException {
        // Arrange
        testPolicyRequest.setStatus(PolicyRequestStatus.REJECTED);
        List<StatusHistory> history = new ArrayList<>();
        history.add(createStatusHistory(PolicyRequestStatus.RECEIVED, "Initial"));
        history.add(createStatusHistory(PolicyRequestStatus.VALIDATED, "Validated"));
        history.add(createStatusHistory(PolicyRequestStatus.REJECTED, "Final rejection reason"));
        testPolicyRequest.setHistory(history);

        String expectedJson = "{\"eventType\":\"POLICY_REQUEST_REJECTED\"}";
        when(objectMapper.writeValueAsString(any(PolicyEvent.class))).thenReturn(expectedJson);

        // Act
        policyEventPublisher.publishPolicyRequestRejected(testPolicyRequest);

        // Assert
        ArgumentCaptor<PolicyEvent> eventCaptor = ArgumentCaptor.forClass(PolicyEvent.class);
        verify(objectMapper).writeValueAsString(eventCaptor.capture());

        PolicyEvent capturedEvent = eventCaptor.getValue();
        assertEquals("Final rejection reason", capturedEvent.getReason());
    }

    @Test
    void testAllEventTypesHaveCorrectEventType() throws JsonProcessingException {
        // Arrange
        String expectedJson = "{\"eventType\":\"TEST\"}";
        when(objectMapper.writeValueAsString(any(PolicyEvent.class))).thenReturn(expectedJson);

        // Act & Assert for each event type
        testEventType(() -> policyEventPublisher.publishPolicyRequestCreated(testPolicyRequest), "POLICY_REQUEST_CREATED");
        testEventType(() -> policyEventPublisher.publishPolicyRequestValidated(testPolicyRequest), "POLICY_REQUEST_VALIDATED");
        testEventType(() -> policyEventPublisher.publishPolicyRequestPending(testPolicyRequest), "POLICY_REQUEST_PENDING");
        testEventType(() -> policyEventPublisher.publishPolicyRequestApproved(testPolicyRequest), "POLICY_REQUEST_APPROVED");
        testEventType(() -> policyEventPublisher.publishPolicyRequestRejected(testPolicyRequest), "POLICY_REQUEST_REJECTED");
        testEventType(() -> policyEventPublisher.publishPolicyRequestCancelled(testPolicyRequest), "POLICY_REQUEST_CANCELLED");
    }

    @Test
    void testEventTimestampIsSet() throws JsonProcessingException {
        // Arrange
        String expectedJson = "{\"eventType\":\"POLICY_REQUEST_CREATED\"}";
        when(objectMapper.writeValueAsString(any(PolicyEvent.class))).thenReturn(expectedJson);

        LocalDateTime beforeCall = LocalDateTime.now();

        // Act
        policyEventPublisher.publishPolicyRequestCreated(testPolicyRequest);

        LocalDateTime afterCall = LocalDateTime.now();

        // Assert
        ArgumentCaptor<PolicyEvent> eventCaptor = ArgumentCaptor.forClass(PolicyEvent.class);
        verify(objectMapper).writeValueAsString(eventCaptor.capture());

        PolicyEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent.getTimestamp());
        assertTrue(capturedEvent.getTimestamp().isAfter(beforeCall.minusSeconds(1)));
        assertTrue(capturedEvent.getTimestamp().isBefore(afterCall.plusSeconds(1)));
    }

    @Test
    void testKafkaKeyIsCorrect() throws JsonProcessingException {
        // Arrange
        String expectedJson = "{\"eventType\":\"POLICY_REQUEST_CREATED\"}";
        when(objectMapper.writeValueAsString(any(PolicyEvent.class))).thenReturn(expectedJson);

        // Act
        policyEventPublisher.publishPolicyRequestCreated(testPolicyRequest);

        // Assert
        verify(kafkaTemplate).send(eq(testTopicName), eq(testPolicyRequest.getId().toString()), eq(expectedJson));
    }

    @Test
    void testDifferentTopicName() throws JsonProcessingException {
        // Arrange
        String customTopic = "custom-policy-events";
        ReflectionTestUtils.setField(policyEventPublisher, "policyEventsTopicName", customTopic);
        
        String expectedJson = "{\"eventType\":\"POLICY_REQUEST_CREATED\"}";
        when(objectMapper.writeValueAsString(any(PolicyEvent.class))).thenReturn(expectedJson);

        // Act
        policyEventPublisher.publishPolicyRequestCreated(testPolicyRequest);

        // Assert
        verify(kafkaTemplate).send(eq(customTopic), anyString(), eq(expectedJson));
    }

    private void testEventType(Runnable publishMethod, String expectedEventType) throws JsonProcessingException {
        // Reset mocks
        reset(objectMapper, kafkaTemplate);
        
        String expectedJson = "{\"eventType\":\"" + expectedEventType + "\"}";
        when(objectMapper.writeValueAsString(any(PolicyEvent.class))).thenReturn(expectedJson);

        // Act
        publishMethod.run();

        // Assert
        ArgumentCaptor<PolicyEvent> eventCaptor = ArgumentCaptor.forClass(PolicyEvent.class);
        verify(objectMapper).writeValueAsString(eventCaptor.capture());
        
        PolicyEvent capturedEvent = eventCaptor.getValue();
        assertEquals(expectedEventType, capturedEvent.getEventType());
    }

    private StatusHistory createStatusHistory(PolicyRequestStatus status, String reason) {
        StatusHistory history = new StatusHistory();
        history.setStatus(status);
        history.setReason(reason);
        history.setTimestamp(LocalDateTime.now());
        return history;
    }
}