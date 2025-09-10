package com.acme.policyapi.application.service.impl;

import com.acme.policyapi.application.dto.PolicyRequestCreateDTO;
import com.acme.policyapi.application.dto.PolicyRequestResponseDTO;
import com.acme.policyapi.application.usecase.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyRequestServiceImplSimpleTest {

    @Mock
    private CreatePolicyRequestUseCase createPolicyRequestUseCase;

    @Mock
    private FindPolicyRequestUseCase findPolicyRequestUseCase;

    @Mock
    private ProcessPolicyRequestUseCase processPolicyRequestUseCase;

    @Mock
    private CancelPolicyRequestUseCase cancelPolicyRequestUseCase;

    @Mock
    private SetPendingPolicyRequestUseCase setPendingPolicyRequestUseCase;

    @Mock
    private ApprovePolicyRequestUseCase approvePolicyRequestUseCase;

    @Mock
    private RejectPolicyRequestUseCase rejectPolicyRequestUseCase;

    @InjectMocks
    private PolicyRequestServiceImpl policyRequestService;

    @Test
    void testCreatePolicyRequest() {
        PolicyRequestCreateDTO createDTO = new PolicyRequestCreateDTO();
        PolicyRequestResponseDTO responseDTO = new PolicyRequestResponseDTO();
        
        when(createPolicyRequestUseCase.execute(any(PolicyRequestCreateDTO.class)))
                .thenReturn(responseDTO);

        policyRequestService.createPolicyRequest(createDTO);

        verify(createPolicyRequestUseCase, times(1)).execute(createDTO);
    }

    @Test
    void testFindById() {
        UUID id = UUID.randomUUID();
        PolicyRequestResponseDTO responseDTO = new PolicyRequestResponseDTO();
        
        when(findPolicyRequestUseCase.findById(any(UUID.class)))
                .thenReturn(responseDTO);

        policyRequestService.findById(id);

        verify(findPolicyRequestUseCase, times(1)).findById(id);
    }

    @Test
    void testProcessRequest() {
        UUID id = UUID.randomUUID();
        
        policyRequestService.processRequest(id);

        verify(processPolicyRequestUseCase, times(1)).execute(id);
    }

    @Test
    void testCancelRequest() {
        UUID id = UUID.randomUUID();
        String reason = "Test reason";
        
        policyRequestService.cancelRequest(id, reason);

        verify(cancelPolicyRequestUseCase, times(1)).execute(id, reason);
    }

    @Test
    void testSetPending() {
        UUID id = UUID.randomUUID();
        
        policyRequestService.setPending(id);

        verify(setPendingPolicyRequestUseCase, times(1)).execute(id);
    }

    @Test
    void testApproveRequest() {
        UUID id = UUID.randomUUID();
        
        policyRequestService.approveRequest(id);

        verify(approvePolicyRequestUseCase, times(1)).execute(id);
    }

    @Test
    void testRejectRequest() {
        UUID id = UUID.randomUUID();
        String reason = "Test reason";
        
        policyRequestService.rejectRequest(id, reason);

        verify(rejectPolicyRequestUseCase, times(1)).execute(id, reason);
    }
}