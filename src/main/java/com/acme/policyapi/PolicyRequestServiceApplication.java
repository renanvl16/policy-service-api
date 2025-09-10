package com.acme.policyapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Aplicação principal do microsserviço de solicitações de apólices de seguro.
 * 
 * @author Sistema ACME
 */
@SpringBootApplication
@EnableKafka
public class PolicyRequestServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PolicyRequestServiceApplication.class, args);
    }
}