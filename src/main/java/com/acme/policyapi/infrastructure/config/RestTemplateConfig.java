package com.acme.policyapi.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;

/**
 * Configuração do RestTemplate para chamadas HTTP externas.
 * 
 * @author Sistema ACME
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Configura RestTemplate com timeouts apropriados.
     * 
     * @param builder builder do RestTemplate
     * @return RestTemplate configurado
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }
}