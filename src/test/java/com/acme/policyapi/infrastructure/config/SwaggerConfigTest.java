package com.acme.policyapi.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class SwaggerConfigTest {
    @Test
    void testPolicyRequestOpenAPINotNull() {
        SwaggerConfig config = new SwaggerConfig();
        ReflectionTestUtils.setField(config, "serverPort", "8080");
        OpenAPI openAPI = config.policyRequestOpenAPI();
        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertNotNull(openAPI.getServers());
    }

    @Test
    void testCreateApiInfoAndServers() {
        SwaggerConfig config = new SwaggerConfig();
        ReflectionTestUtils.setField(config, "serverPort", "8080");
        OpenAPI openAPI = config.policyRequestOpenAPI();
        assertEquals("Policy Request Service API", openAPI.getInfo().getTitle());
        assertFalse(openAPI.getServers().isEmpty());
    }
}
