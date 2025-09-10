package com.acme.policyapi.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PolicyRequestJpaRepository extends JpaRepository<PolicyRequestJpaEntity, UUID> {

    List<PolicyRequestJpaEntity> findByCustomerId(UUID customerId);

    @Query("SELECT pr FROM PolicyRequestJpaEntity pr LEFT JOIN FETCH pr.history WHERE pr.id = :id")
    Optional<PolicyRequestJpaEntity> findByIdWithHistory(@Param("id") UUID id);

    @Query("SELECT pr FROM PolicyRequestJpaEntity pr LEFT JOIN FETCH pr.history WHERE pr.customerId = :customerId")
    List<PolicyRequestJpaEntity> findByCustomerIdWithHistory(@Param("customerId") UUID customerId);
}