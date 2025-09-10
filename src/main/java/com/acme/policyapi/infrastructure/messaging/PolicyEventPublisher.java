package com.acme.policyapi.infrastructure.messaging;

import com.acme.policyapi.domain.entity.PolicyRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável por publicar eventos de solicitações de apólice no Kafka.
 * 
 * @author Sistema ACME
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.policy-requests.events:policy-requests.events}")
    private String policyEventsTopicName;

    /**
     * Publica evento de criação de solicitação.
     * 
     * @param policyRequest a solicitação criada
     */
    public void publishPolicyRequestCreated(PolicyRequest policyRequest) {
        PolicyEvent event = new PolicyEvent(
            policyRequest.getId(),
            policyRequest.getCustomerId(),
            policyRequest.getProductId(),
            policyRequest.getStatus(),
            "POLICY_REQUEST_CREATED"
        );
        
        publishEvent(event, "Solicitação criada");
    }

    /**
     * Publica evento de validação de solicitação.
     * 
     * @param policyRequest a solicitação validada
     */
    public void publishPolicyRequestValidated(PolicyRequest policyRequest) {
        PolicyEvent event = new PolicyEvent(
            policyRequest.getId(),
            policyRequest.getCustomerId(),
            policyRequest.getProductId(),
            policyRequest.getStatus(),
            "POLICY_REQUEST_VALIDATED"
        );
        
        publishEvent(event, "Solicitação validada");
    }

    /**
     * Publica evento de solicitação pendente.
     * 
     * @param policyRequest a solicitação pendente
     */
    public void publishPolicyRequestPending(PolicyRequest policyRequest) {
        PolicyEvent event = new PolicyEvent(
            policyRequest.getId(),
            policyRequest.getCustomerId(),
            policyRequest.getProductId(),
            policyRequest.getStatus(),
            "POLICY_REQUEST_PENDING"
        );
        
        publishEvent(event, "Solicitação pendente");
    }

    /**
     * Publica evento de aprovação de solicitação.
     * 
     * @param policyRequest a solicitação aprovada
     */
    public void publishPolicyRequestApproved(PolicyRequest policyRequest) {
        PolicyEvent event = new PolicyEvent(
            policyRequest.getId(),
            policyRequest.getCustomerId(),
            policyRequest.getProductId(),
            policyRequest.getStatus(),
            "POLICY_REQUEST_APPROVED"
        );
        
        publishEvent(event, "Solicitação aprovada");
    }

    /**
     * Publica evento de rejeição de solicitação.
     * 
     * @param policyRequest a solicitação rejeitada
     */
    public void publishPolicyRequestRejected(PolicyRequest policyRequest) {
        String reason = getLatestHistoryReason(policyRequest);
        
        PolicyEvent event = new PolicyEvent(
            policyRequest.getId(),
            policyRequest.getCustomerId(),
            policyRequest.getProductId(),
            policyRequest.getStatus(),
            null,
            reason,
            "POLICY_REQUEST_REJECTED"
        );
        
        publishEvent(event, "Solicitação rejeitada");
    }

    /**
     * Publica evento de cancelamento de solicitação.
     * 
     * @param policyRequest a solicitação cancelada
     */
    public void publishPolicyRequestCancelled(PolicyRequest policyRequest) {
        String reason = getLatestHistoryReason(policyRequest);
        
        PolicyEvent event = new PolicyEvent(
            policyRequest.getId(),
            policyRequest.getCustomerId(),
            policyRequest.getProductId(),
            policyRequest.getStatus(),
            null,
            reason,
            "POLICY_REQUEST_CANCELLED"
        );
        
        publishEvent(event, "Solicitação cancelada");
    }

    /**
     * Publica um evento no tópico Kafka.
     * 
     * @param event o evento a ser publicado
     * @param description descrição do evento para log
     */
    private void publishEvent(PolicyEvent event, String description) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String key = event.getPolicyRequestId().toString();
            
            kafkaTemplate.send(policyEventsTopicName, key, eventJson);
            
            log.info("{} - Event published: {} for policy request {}", 
                     description, event.getEventType(), event.getPolicyRequestId());
            
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar evento: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Erro ao publicar evento: {}", e.getMessage(), e);
        }
    }

    /**
     * Obtém o motivo do último histórico da solicitação.
     */
    private String getLatestHistoryReason(PolicyRequest policyRequest) {
        if (policyRequest.getHistory() != null && !policyRequest.getHistory().isEmpty()) {
            int lastIndex = policyRequest.getHistory().size() - 1;
            return policyRequest.getHistory().get(lastIndex).getReason();
        }
        return null;
    }
}