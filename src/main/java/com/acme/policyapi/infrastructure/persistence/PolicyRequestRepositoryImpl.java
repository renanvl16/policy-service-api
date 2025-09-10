package com.acme.policyapi.infrastructure.persistence;

import com.acme.policyapi.domain.entity.*;
import com.acme.policyapi.domain.repository.PolicyRequestRepository;
import com.acme.policyapi.infrastructure.persistence.jpa.PolicyRequestJpaEntity;
import com.acme.policyapi.infrastructure.persistence.jpa.PolicyRequestJpaRepository;
import com.acme.policyapi.infrastructure.persistence.jpa.StatusHistoryJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PolicyRequestRepositoryImpl implements PolicyRequestRepository {

    private final PolicyRequestJpaRepository jpaRepository;

    @Override
    public List<PolicyRequest> findByCustomerId(UUID customerId) {
        return jpaRepository.findByCustomerId(customerId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PolicyRequest> findByIdWithHistory(UUID id) {
        return jpaRepository.findByIdWithHistory(id)
                .map(this::toDomainWithHistory);
    }

    @Override
    public List<PolicyRequest> findByCustomerIdWithHistory(UUID customerId) {
        return jpaRepository.findByCustomerIdWithHistory(customerId)
                .stream()
                .map(this::toDomainWithHistory)
                .collect(Collectors.toList());
    }

    @Override
    public PolicyRequest save(PolicyRequest policyRequest) {
        PolicyRequestJpaEntity jpaEntity = toJpaEntity(policyRequest);
        PolicyRequestJpaEntity saved = jpaRepository.save(jpaEntity);
        return toDomainWithHistory(saved);
    }

    @Override
    public Optional<PolicyRequest> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }

    private PolicyRequest toDomain(PolicyRequestJpaEntity jpaEntity) {
        PolicyRequest domain = new PolicyRequest();
        domain.setId(jpaEntity.getId());
        domain.setCustomerId(jpaEntity.getCustomerId());
        domain.setProductId(jpaEntity.getProductId());
        domain.setCategory(InsuranceCategory.valueOf(jpaEntity.getCategory()));
        domain.setSalesChannel(SalesChannel.valueOf(jpaEntity.getSalesChannel()));
        domain.setPaymentMethod(PaymentMethod.valueOf(jpaEntity.getPaymentMethod()));
        domain.setStatus(PolicyRequestStatus.valueOf(jpaEntity.getStatus()));
        domain.setCreatedAt(jpaEntity.getCreatedAt());
        domain.setFinishedAt(jpaEntity.getFinishedAt());
        domain.setTotalMonthlyPremiumAmount(jpaEntity.getTotalMonthlyPremiumAmount());
        domain.setInsuredAmount(jpaEntity.getInsuredAmount());
        domain.setCoverages(jpaEntity.getCoverages());
        domain.setAssistances(jpaEntity.getAssistances());
        return domain;
    }

    private PolicyRequest toDomainWithHistory(PolicyRequestJpaEntity jpaEntity) {
        PolicyRequest domain = toDomain(jpaEntity);
        
        List<StatusHistory> history = jpaEntity.getHistory().stream()
                .map(this::toDomainHistory)
                .collect(Collectors.toList());
        
        domain.setHistory(history);
        return domain;
    }

    private StatusHistory toDomainHistory(StatusHistoryJpaEntity jpaEntity) {
        return new StatusHistory(
            jpaEntity.getPolicyRequestId(),
            PolicyRequestStatus.valueOf(jpaEntity.getStatus()),
            jpaEntity.getTimestamp(),
            jpaEntity.getReason()
        );
    }

    private PolicyRequestJpaEntity toJpaEntity(PolicyRequest domain) {
        PolicyRequestJpaEntity jpaEntity = PolicyRequestJpaEntity.builder()
                .id(domain.getId())
                .customerId(domain.getCustomerId())
                .productId(domain.getProductId())
                .category(domain.getCategory().name())
                .salesChannel(domain.getSalesChannel().name())
                .paymentMethod(domain.getPaymentMethod().name())
                .status(domain.getStatus().name())
                .createdAt(domain.getCreatedAt())
                .finishedAt(domain.getFinishedAt())
                .totalMonthlyPremiumAmount(domain.getTotalMonthlyPremiumAmount())
                .insuredAmount(domain.getInsuredAmount())
                .coverages(domain.getCoverages())
                .assistances(domain.getAssistances())
                .build();

        List<StatusHistoryJpaEntity> historyEntities = domain.getHistory().stream()
                .map(this::toJpaHistoryEntity)
                .collect(Collectors.toList());
        
        jpaEntity.setHistory(historyEntities);
        return jpaEntity;
    }

    private StatusHistoryJpaEntity toJpaHistoryEntity(StatusHistory domain) {
        return StatusHistoryJpaEntity.builder()
                .policyRequestId(domain.getPolicyRequestId())
                .status(domain.getStatus().name())
                .timestamp(domain.getTimestamp())
                .reason(domain.getReason())
                .build();
    }
}