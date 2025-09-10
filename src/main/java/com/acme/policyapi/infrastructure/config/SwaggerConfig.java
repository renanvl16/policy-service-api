package com.acme.policyapi.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração da documentação OpenAPI/Swagger para a API de solicitações de apólice.
 * 
 * @author Sistema ACME
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * Configura a documentação OpenAPI.
     * 
     * @return configuração do OpenAPI
     */
    @Bean
    public OpenAPI policyRequestOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .servers(createServers());
    }

    /**
     * Cria as informações básicas da API.
     * 
     * @return informações da API
     */
    private Info createApiInfo() {
        return new Info()
                .title("Policy Request Service API")
                .description("""
                    API para gerenciamento de solicitações de apólices de seguro da ACME.
                    
                    Esta API implementa um microsserviço orientado a eventos (EDA) que gerencia 
                    o ciclo de vida completo das solicitações de apólice, incluindo:
                    
                    - Criação e consulta de solicitações
                    - Análise de fraudes e validação de regras de negócio
                    - Gerenciamento de estados (RECEIVED → VALIDATED → PENDING → APPROVED/REJECTED/CANCELLED)
                    - Integração com sistemas de pagamento e subscrição
                    - Publicação de eventos para notificação de outros serviços
                    
                    ## Estados da Solicitação
                    - **RECEIVED**: Solicitação criada, aguardando análise
                    - **VALIDATED**: Aprovada pela análise de fraudes
                    - **PENDING**: Aguardando confirmação de pagamento e subscrição
                    - **APPROVED**: Aprovada e pronta para emissão da apólice
                    - **REJECTED**: Rejeitada por não atender aos critérios
                    - **CANCELLED**: Cancelada pelo cliente
                    
                    ## Classificações de Risco
                    - **REGULAR**: Cliente padrão com limites normais
                    - **HIGH_RISK**: Cliente de alto risco com limites reduzidos
                    - **PREFERENTIAL**: Cliente preferencial com limites elevados
                    - **NO_INFORMATION**: Cliente sem histórico com limites conservadores
                    """)
                .version("1.0.0")
                .contact(createContact())
                .license(createLicense());
    }

    /**
     * Cria informações de contato.
     * 
     * @return informações de contato
     */
    private Contact createContact() {
        return new Contact()
                .name("Equipe de Desenvolvimento ACME")
                .email("dev@acme.com")
                .url("https://acme.com");
    }

    /**
     * Cria informações de licença.
     * 
     * @return informações de licença
     */
    private License createLicense() {
        return new License()
                .name("Proprietary")
                .url("https://acme.com/license");
    }

    /**
     * Cria lista de servidores disponíveis.
     * 
     * @return lista de servidores
     */
    private List<Server> createServers() {
        Server localServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Servidor de desenvolvimento local");

        Server productionServer = new Server()
                .url("https://api.acme.com/policy-service")
                .description("Servidor de produção");

        return List.of(localServer, productionServer);
    }
}