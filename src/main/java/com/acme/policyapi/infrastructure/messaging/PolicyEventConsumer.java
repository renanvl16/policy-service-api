package com.acme.policyapi.infrastructure.messaging;

import com.acme.policyapi.application.service.PolicyRequestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Consumidor de eventos relacionados a pagamentos e autorizações de subscrição.
 * 
 * @author Sistema ACME
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyEventConsumer {

    private final PolicyRequestService policyRequestService;
    private final ObjectMapper objectMapper;

    /**
     * Processa eventos de confirmação de pagamento.
     * 
     * @param message mensagem do evento
     * @param key chave da mensagem
     * @param acknowledgment confirmação de processamento
     */
    @KafkaListener(topics = "${kafka.topics.payments:payments.events}", 
                   groupId = "${kafka.consumer.group-id:policy-request-service}")
    public void handlePaymentEvent(@Payload String message, 
                                  @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                  Acknowledgment acknowledgment) {
        
        log.info("Recebendo evento de pagamento: key={}", key);
        
        try {
            PaymentEvent paymentEvent = objectMapper.readValue(message, PaymentEvent.class);
            
            log.info("Processando evento de pagamento: {} para solicitação {}", 
                     paymentEvent.getEventType(), paymentEvent.getPolicyRequestId());
            
            switch (paymentEvent.getEventType()) {
                case "PAYMENT_CONFIRMED" -> handlePaymentConfirmed(paymentEvent);
                case "PAYMENT_REJECTED" -> handlePaymentRejected(paymentEvent);
                default -> log.warn("Tipo de evento de pagamento desconhecido: {}", paymentEvent.getEventType());
            }
            
            acknowledgment.acknowledge();
            
        } catch (JsonProcessingException e) {
            log.error("Erro ao deserializar evento de pagamento: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Erro ao processar evento de pagamento: {}", e.getMessage(), e);
            // Em um cenário real, implementaríamos retry ou DLQ (Dead Letter Queue)
        }
    }

    /**
     * Processa eventos de autorização de subscrição.
     * 
     * @param message mensagem do evento
     * @param key chave da mensagem
     * @param acknowledgment confirmação de processamento
     */
    @KafkaListener(topics = "${kafka.topics.underwriting:underwriting.events}", 
                   groupId = "${kafka.consumer.group-id:policy-request-service}")
    public void handleUnderwritingEvent(@Payload String message,
                                       @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                       Acknowledgment acknowledgment) {
        
        log.info("Recebendo evento de subscrição: key={}", key);
        
        try {
            UnderwritingEvent underwritingEvent = objectMapper.readValue(message, UnderwritingEvent.class);
            
            log.info("Processando evento de subscrição: {} para solicitação {}", 
                     underwritingEvent.getEventType(), underwritingEvent.getPolicyRequestId());
            
            switch (underwritingEvent.getEventType()) {
                case "UNDERWRITING_APPROVED" -> handleUnderwritingApproved(underwritingEvent);
                case "UNDERWRITING_REJECTED" -> handleUnderwritingRejected(underwritingEvent);
                default -> log.warn("Tipo de evento de subscrição desconhecido: {}", underwritingEvent.getEventType());
            }
            
            acknowledgment.acknowledge();
            
        } catch (JsonProcessingException e) {
            log.error("Erro ao deserializar evento de subscrição: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Erro ao processar evento de subscrição: {}", e.getMessage(), e);
        }
    }

    private void handlePaymentConfirmed(PaymentEvent event) {
        log.info("Pagamento confirmado para solicitação: {}", event.getPolicyRequestId());
        // Lógica para lidar com confirmação de pagamento
        // Neste caso, verificamos se também temos autorização de subscrição
    }

    private void handlePaymentRejected(PaymentEvent event) {
        log.info("Pagamento rejeitado para solicitação: {}", event.getPolicyRequestId());
        policyRequestService.rejectRequest(event.getPolicyRequestId(), 
                                          "Pagamento rejeitado: " + event.getReason());
    }

    private void handleUnderwritingApproved(UnderwritingEvent event) {
        log.info("Subscrição aprovada para solicitação: {}", event.getPolicyRequestId());
        // Se chegou até aqui, assumimos que o pagamento também foi confirmado
        policyRequestService.approveRequest(event.getPolicyRequestId());
    }

    private void handleUnderwritingRejected(UnderwritingEvent event) {
        log.info("Subscrição rejeitada para solicitação: {}", event.getPolicyRequestId());
        policyRequestService.rejectRequest(event.getPolicyRequestId(),
                                          "Subscrição rejeitada: " + event.getReason());
    }

    /**
     * Evento de pagamento recebido via Kafka.
     */
    public static class PaymentEvent {
        private UUID policyRequestId;
        private String eventType;
        private String status;
        private String reason;
        private String paymentId;

        // Getters e setters
        public UUID getPolicyRequestId() { return policyRequestId; }
        public void setPolicyRequestId(UUID policyRequestId) { this.policyRequestId = policyRequestId; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    }

    /**
     * Evento de subscrição recebido via Kafka.
     */
    public static class UnderwritingEvent {
        private UUID policyRequestId;
        private String eventType;
        private String status;
        private String reason;
        private String underwriterId;

        // Getters e setters
        public UUID getPolicyRequestId() { return policyRequestId; }
        public void setPolicyRequestId(UUID policyRequestId) { this.policyRequestId = policyRequestId; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getUnderwriterId() { return underwriterId; }
        public void setUnderwriterId(String underwriterId) { this.underwriterId = underwriterId; }
    }
}