package com.acme.policyapi.infrastructure.rest;

import com.acme.policyapi.application.dto.PolicyRequestCreateDTO;
import com.acme.policyapi.application.dto.PolicyRequestResponseDTO;
import com.acme.policyapi.application.service.PolicyRequestService;
import com.acme.policyapi.domain.entity.PolicyRequestStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Controller REST para gerenciar solicitações de apólices de seguro.
 * 
 * @author Sistema ACME
 */
@RestController
@RequestMapping("/api/v1/policy-requests")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Policy Requests", description = "API para gerenciamento de solicitações de apólices de seguro")
public class PolicyRequestController {

    private final PolicyRequestService policyRequestService;

    @Operation(summary = "Criar nova solicitação de apólice", 
               description = "Cria uma nova solicitação de apólice de seguro e retorna o ID da solicitação com data/hora")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Solicitação criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping
    public ResponseEntity<PolicyRequestCreatedResponse> createPolicyRequest(
            @Valid @RequestBody PolicyRequestCreateDTO createDTO) {
        
        log.info("Recebendo nova solicitação de apólice para cliente {}", createDTO.getCustomerId());
        
        PolicyRequestResponseDTO response = policyRequestService.createPolicyRequest(createDTO);
        
        PolicyRequestCreatedResponse createdResponse = PolicyRequestCreatedResponse.builder()
                .id(response.getId())
                .createdAt(response.getCreatedAt())
                .status(response.getStatus())
                .build();
        
        log.info("Solicitação {} criada com sucesso", response.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdResponse);
    }

    @Operation(summary = "Consultar solicitação por ID", 
               description = "Retorna os detalhes completos de uma solicitação incluindo histórico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Solicitação encontrada"),
        @ApiResponse(responseCode = "404", description = "Solicitação não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PolicyRequestResponseDTO> findById(
            @Parameter(description = "ID da solicitação", required = true)
            @PathVariable UUID id) {
        
        log.debug("Consultando solicitação por ID: {}", id);
        
        PolicyRequestResponseDTO response = policyRequestService.findById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Consultar solicitações por ID do cliente", 
               description = "Retorna todas as solicitações de um cliente específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de solicitações retornada"),
        @ApiResponse(responseCode = "404", description = "Cliente não possui solicitações")
    })
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PolicyRequestResponseDTO>> findByCustomerId(
            @Parameter(description = "ID do cliente", required = true)
            @PathVariable UUID customerId) {
        
        log.debug("Consultando solicitações do cliente: {}", customerId);
        
        List<PolicyRequestResponseDTO> responses = policyRequestService.findByCustomerId(customerId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Cancelar solicitação", 
               description = "Cancela uma solicitação de apólice (exceto se já foi aprovada)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Solicitação cancelada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Solicitação não pode ser cancelada"),
        @ApiResponse(responseCode = "404", description = "Solicitação não encontrada")
    })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelRequest(
            @Parameter(description = "ID da solicitação", required = true)
            @PathVariable UUID id,
            @RequestBody(required = false) CancellationRequest cancellationRequest) {
        
        String reason = (cancellationRequest != null) ? cancellationRequest.getReason() : "Cancelamento solicitado";
        
        log.info("Cancelando solicitação: {} - Motivo: {}", id, reason);
        
        policyRequestService.cancelRequest(id, reason);
        return ResponseEntity.ok().build();
    }

    @Data
    @Builder
    public static class PolicyRequestCreatedResponse {
        private UUID id;
        private LocalDateTime createdAt;
        private PolicyRequestStatus status;
    }

    @Data
    public static class CancellationRequest {
        private String reason;
    }
}