package com.acme.policyapi.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestKafkaConfiguration {

    @Bean
    @Primary
    public KafkaTemplate<String, String> kafkaTemplate() {
        return mock(KafkaTemplate.class);
    }
}