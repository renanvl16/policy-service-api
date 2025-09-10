package com.acme.policyapi.domain.repository;

import com.acme.policyapi.domain.entity.PolicyRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para operações de persistência de solicitações de apólice.
 * 
 * @author Sistema ACME
 */
public interface PolicyRequestRepository {

    PolicyRequest save(PolicyRequest policyRequest);
    
    Optional<PolicyRequest> findById(UUID id);
    
    void deleteById(UUID id);
    
    void deleteAll();
    
    boolean existsById(UUID id);

    /**
     * Busca solicitações por ID do cliente.
     * 
     * @param customerId ID do cliente
     * @return lista de solicitações do cliente
     */
    List<PolicyRequest> findByCustomerId(UUID customerId);

    /**
     * Busca solicitação por ID com histórico carregado.
     * 
     * @param id ID da solicitação
     * @return solicitação com histórico
     */
    Optional<PolicyRequest> findByIdWithHistory(UUID id);

    /**
     * Busca solicitações por ID do cliente com histórico carregado.
     * 
     * @param customerId ID do cliente
     * @return lista de solicitações com histórico
     */
    List<PolicyRequest> findByCustomerIdWithHistory(UUID customerId);
}