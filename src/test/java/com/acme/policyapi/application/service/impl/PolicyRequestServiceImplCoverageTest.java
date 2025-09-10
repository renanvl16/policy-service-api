package com.acme.policyapi.application.service.impl;

import com.acme.policyapi.application.exception.PolicyRequestNotFoundException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PolicyRequestServiceImplCoverageTest {
    @Test
    void testPolicyRequestNotFoundExceptionConstructor() {
        String msg = "not found";
        PolicyRequestNotFoundException ex = new PolicyRequestNotFoundException(msg);
        assertEquals(msg, ex.getMessage());
    }
}
