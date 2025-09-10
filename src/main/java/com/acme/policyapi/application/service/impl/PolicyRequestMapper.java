package com.acme.policyapi.application.service.impl;

import com.acme.policyapi.application.dto.PolicyRequestCreateDTO;
import com.acme.policyapi.application.dto.PolicyRequestResponseDTO;
import com.acme.policyapi.application.dto.StatusHistoryDTO;
import com.acme.policyapi.domain.entity.PolicyRequest;
import com.acme.policyapi.domain.entity.StatusHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper para conversão entre entidades e DTOs de solicitações de apólice.
 * 
 * @author Sistema ACME
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@Component
public interface PolicyRequestMapper {

    /**
     * Converte DTO de criação para entidade.
     * 
     * @param createDTO DTO de criação
     * @return entidade de solicitação
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "finishedAt", ignore = true)
    @Mapping(target = "history", ignore = true)
    PolicyRequest toEntity(PolicyRequestCreateDTO createDTO);

    /**
     * Converte entidade para DTO de resposta.
     * 
     * @param policyRequest entidade de solicitação
     * @return DTO de resposta
     */
    PolicyRequestResponseDTO toResponseDTO(PolicyRequest policyRequest);

    /**
     * Converte lista de entidades para lista de DTOs de resposta.
     * 
     * @param policyRequests lista de entidades
     * @return lista de DTOs de resposta
     */
    List<PolicyRequestResponseDTO> toResponseDTOList(List<PolicyRequest> policyRequests);

    /**
     * Converte histórico de status para DTO.
     * 
     * @param statusHistory histórico de status
     * @return DTO do histórico
     */
    StatusHistoryDTO toStatusHistoryDTO(StatusHistory statusHistory);
}