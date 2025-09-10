package com.acme.policyapi.application.service.impl;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PolicyRequestMapperCoverageTest {
    @Test
    void testPolicyRequestMapperIsInterface() {
        assertTrue(PolicyRequestMapper.class.isInterface());
    }
}
