package com.acme.policyapi.infrastructure.rest;

import com.acme.policyapi.application.dto.PolicyRequestCreateDTO;
import com.acme.policyapi.application.dto.PolicyRequestResponseDTO;
import com.acme.policyapi.application.service.PolicyRequestService;
import com.acme.policyapi.domain.entity.PolicyRequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.springframework.http.HttpStatus;

class PolicyRequestControllerTest {
    @Mock
    private PolicyRequestService policyRequestService;

    @InjectMocks
    private PolicyRequestController controller;

    private UUID id;
    private PolicyRequestCreateDTO createDTO;
    private PolicyRequestResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        id = UUID.randomUUID();
        createDTO = new PolicyRequestCreateDTO();
        createDTO.setCustomerId(UUID.randomUUID());
        responseDTO = new PolicyRequestResponseDTO();
        responseDTO.setId(id);
        responseDTO.setCreatedAt(LocalDateTime.now());
        responseDTO.setStatus(PolicyRequestStatus.RECEIVED);
    }

    @Test
    void testCreatePolicyRequest() {
        when(policyRequestService.createPolicyRequest(any())).thenReturn(responseDTO);
        ResponseEntity<PolicyRequestController.PolicyRequestCreatedResponse> resp = controller.createPolicyRequest(createDTO);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        PolicyRequestController.PolicyRequestCreatedResponse body = resp.getBody();
        assertNotNull(body);
        assertEquals(id, body.getId());
    }

    @Test
    void testFindById() {
        when(policyRequestService.findById(id)).thenReturn(responseDTO);
        ResponseEntity<PolicyRequestResponseDTO> resp = controller.findById(id);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        PolicyRequestResponseDTO body = resp.getBody();
        assertNotNull(body);
        assertEquals(id, body.getId());
    }

    @Test
    void testFindByCustomerId() {
        when(policyRequestService.findByCustomerId(any())).thenReturn(Collections.singletonList(responseDTO));
        ResponseEntity<List<PolicyRequestResponseDTO>> resp = controller.findByCustomerId(createDTO.getCustomerId());
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        List<PolicyRequestResponseDTO> body = resp.getBody();
        assertNotNull(body);
        assertFalse(body.isEmpty());
    }

    @Test
    void testCancelRequestWithReason() {
        PolicyRequestController.CancellationRequest req = new PolicyRequestController.CancellationRequest();
        req.setReason("Teste");
        doNothing().when(policyRequestService).cancelRequest(any(), any());
        ResponseEntity<Void> resp = controller.cancelRequest(id, req);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        verify(policyRequestService).cancelRequest(id, "Teste");
    }

    @Test
    void testCancelRequestWithoutReason() {
        doNothing().when(policyRequestService).cancelRequest(any(), any());
        ResponseEntity<Void> resp = controller.cancelRequest(id, null);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        verify(policyRequestService).cancelRequest(id, "Cancelamento solicitado");
    }

    @Test
    void testPolicyRequestCreatedResponseBuilder() {
        LocalDateTime now = LocalDateTime.now();
        PolicyRequestController.PolicyRequestCreatedResponse resp = PolicyRequestController.PolicyRequestCreatedResponse.builder()
                .id(id)
                .createdAt(now)
                .status(PolicyRequestStatus.APPROVED)
                .build();
        assertEquals(id, resp.getId());
        assertEquals(now, resp.getCreatedAt());
        assertEquals(PolicyRequestStatus.APPROVED, resp.getStatus());
    }

    @Test
    void testCancellationRequestSetAndGet() {
        PolicyRequestController.CancellationRequest req = new PolicyRequestController.CancellationRequest();
        req.setReason("Motivo X");
        assertEquals("Motivo X", req.getReason());
    }
}
