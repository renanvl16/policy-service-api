# Policy Service API

Microsserviço para gerenciamento de solicitações de apólices de seguro da ACME Seguros, implementado com arquitetura orientada a eventos (EDA).

## Diagrama da Arquitetura

```
                    EDA - Policy Service API Architecture
    
    ┌─────────────────────┐       ┌─────────────────────────────────┐       ┌─────────────────────┐
    │                     │       │                                 │       │                     │
    │    Client Apps      │──────▶│     Policy Service API      │──────▶│    Frauds API       │
    │ (Mobile/Web/        │       │       (Spring Boot 3)           │◀──────│      (Mock)         │
    │   WhatsApp)         │       │  Controllers + Service Layer    │       │    (WireMock)       │
    │                     │       │   Domain/Rules/Events           │       │                     │
    └─────────────────────┘       │  Producers/Consumers            │       └─────────────────────┘
                                  └─────────────┬───────────────────┘
                                                │        ▲
                            ┌───────────────────┼────────┼───────────────────┐
                            │                   │        │                   │
                            ▼                   ▼        │                   ▼
    ┌─────────────────────┐       ┌─────────────────────┐         ┌─────────────────────┐
    │                     │       │                     │         │                     │
    │       Kafka         │       │     PostgreSQL      │         │   Observability     │
    │   (payments,        │       │       (JPA)         │         │ Actuator->Prometheus│
    │  underwriting,      │       │                     │         │    ->Grafana        │
    │policy-requests.     │       │                     │         │                     │
    │    events)          │       │                     │         │                     │
    └─────────────────────┘       └─────────────────────┘         └─────────────────────┘
```

## Visão Geral

Este serviço gerencia o ciclo de vida completo das solicitações de apólices de seguro, desde a criação até a aprovação final, incluindo:

- ✅ Recebimento e validação de solicitações via API REST
- 🔍 Análise de fraudes e classificação de risco
- ⚡ Processamento orientado a eventos com Kafka
- 🎯 Aplicação de regras de negócio por tipo de cliente
- 📊 Controle de estados e histórico completo
- 🚀 API documentada com OpenAPI/Swagger

## Arquitetura

### Padrões Implementados
- **Arquitetura Hexagonal**: Separação clara entre domínio, aplicação e infraestrutura
- **Event-Driven Architecture**: Comunicação assíncrona via eventos Kafka
- **CQRS**: Separação de comandos e consultas
- **Domain-Driven Design**: Modelagem rica do domínio de seguros

### Stack Tecnológica
- **Java 21** + **Spring Boot 3.2**
- **PostgreSQL 15** para persistência
- **Apache Kafka** para mensageria
- **Docker** + **Docker Compose**
- **Maven** para build
- **JUnit 5** + **Cucumber** para testes
- **TestContainers** para testes de integração
- **Swagger/OpenAPI** para documentação da API

## Funcionalidades Principais

### 1. Gestão de Solicitações
- Criação de solicitações com validação completa
- Consulta por ID da solicitação ou cliente
- Cancelamento de solicitações (exceto aprovadas)
- Histórico completo de alterações

### 2. Estados do Ciclo de Vida
```
RECEIVED → VALIDATED → PENDING → APPROVED
    ↓           ↓          ↓
CANCELLED   CANCELLED  CANCELLED/REJECTED
    ↓           ↓          
REJECTED    REJECTED
```

### 3. Classificações de Risco
- **REGULAR**: Cliente padrão com limites normais
- **HIGH_RISK**: Cliente de alto risco com limites reduzidos  
- **PREFERENTIAL**: Cliente preferencial com limites elevados
- **NO_INFORMATION**: Cliente sem histórico com limites conservadores

### 4. Regras de Validação
Cada tipo de cliente possui limites específicos por categoria de seguro:

| Classificação | Vida/Residencial | Auto | Outros |
|---------------|------------------|------|---------|
| REGULAR | R$ 500.000 | R$ 350.000 | R$ 255.000 |
| HIGH_RISK | R$ 125.000 | R$ 250.000 | R$ 125.000 |
| PREFERENTIAL | R$ 800.000 | R$ 450.000 | R$ 375.000 |
| NO_INFORMATION | R$ 200.000 | R$ 75.000 | R$ 55.000 |

## Estrutura do Projeto

```
src/
├── main/java/com/acme/policyrequest/
│   ├── domain/              # Entidades e regras de negócio
│   │   ├── entity/         # Entidades do domínio
│   │   ├── repository/     # Interfaces de repositório
│   │   └── service/        # Serviços de domínio
│   ├── application/         # Casos de uso e DTOs
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── service/        # Serviços de aplicação
│   │   └── usecase/        # Casos de uso
│   └── infrastructure/      # Camada de infraestrutura
│       ├── rest/           # Controllers REST
│       ├── persistence/    # Implementações JPA
│       ├── messaging/      # Produtores/Consumidores Kafka
│       ├── external/       # Integrações externas
│       └── config/         # Configurações
└── test/                    # Testes unitários e integração
    ├── java/               # Testes JUnit
    └── resources/features/ # Testes Cucumber (BDD)
```

## Configuração e Execução

### Pré-requisitos
- Docker 20.10+
- Docker Compose 2.0+
- Java 21 (para desenvolvimento local)
- Maven 3.9+ (para desenvolvimento local)

### Execução com Docker Compose (Recomendado)
```bash
# Clonar o repositório
git clone <repository-url>
cd policy-service-api

# Iniciar todos os serviços
docker-compose up -d

# Verificar logs
docker-compose logs -f policy-service-api

# Parar serviços
docker-compose down
```

### Execução Local (Desenvolvimento)
```bash
# Iniciar dependências (PostgreSQL + Kafka)
docker-compose up -d postgres kafka zookeeper

# Executar aplicação localmente
mvn spring-boot:run

# Ou com profile específico
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### URLs dos Serviços

| Serviço | URL | Descrição |
|---------|-----|-----------|
| API Principal | http://localhost:8080 | Endpoints REST da aplicação |
| Swagger UI | http://localhost:8080/swagger-ui.html | Documentação interativa da API |
| Health Check | http://localhost:8080/actuator/health | Status da aplicação |
| Kafka UI | http://localhost:8090 | Interface para monitorar Kafka |
| Grafana | http://localhost:3000 | Dashboard de monitoramento (admin/admin123) |
| Prometheus | http://localhost:9090 | Métricas da aplicação |

## Testes

### Executar Testes Unitários
```bash
mvn test
```

### Executar Testes de Integração (Cucumber)
```bash
mvn verify -P integration-test
```

### Relatório de Cobertura
```bash
mvn jacoco:report
# Relatório disponível em: target/site/jacoco/index.html
```

### Testes com TestContainers
Os testes de integração utilizam TestContainers para criar ambientes isolados com PostgreSQL e Kafka reais.

## API Endpoints

### Solicitações de Apólice

#### Criar Solicitação
```http
POST /api/v1/policy-requests
Content-Type: application/json

{
  "customer_id": "adc56d77-348c-4bf0-908f-22d402ee715c",
  "product_id": "1b2da7cc-b367-4196-8a78-9cfeec21f587",
  "category": "AUTO",
  "salesChannel": "MOBILE",
  "paymentMethod": "CREDIT_CARD",
  "total_monthly_premium_amount": 75.25,
  "insured_amount": 275000.50,
  "coverages": {
    "Roubo": 100000.25,
    "Perda Total": 100000.25
  },
  "assistances": ["Guincho até 250km", "Chaveiro 24h"]
}
```

#### Consultar Solicitação
```http
GET /api/v1/policy-requests/{id}
```

#### Consultar por Cliente
```http
GET /api/v1/policy-requests/customer/{customerId}
```

#### Cancelar Solicitação
```http
POST /api/v1/policy-requests/{id}/cancel
Content-Type: application/json

{
  "reason": "Cancelamento solicitado pelo cliente"
}
```

## Eventos Kafka

### Tópicos Utilizados

| Tópico | Descrição | Formato |
|--------|-----------|---------|
| `policy-requests.events` | Eventos de mudança de estado | JSON |
| `payments.events` | Eventos de pagamento | JSON |
| `underwriting.events` | Eventos de subscrição | JSON |

### Exemplo de Evento
```json
{
  "policyRequestId": "89846cee-c6d5-4320-92e9-16e122d5c672",
  "customerId": "adc56d77-348c-4bf0-908f-22d402ee715c",
  "productId": "1b2da7cc-b367-4196-8a78-9cfeec21f587",
  "status": "APPROVED",
  "previousStatus": "PENDING",
  "reason": "Pagamento confirmado e subscrição autorizada",
  "timestamp": "2024-01-15T10:30:00Z",
  "eventType": "POLICY_REQUEST_APPROVED"
}
```

## Monitoramento e Observabilidade

### Métricas Disponíveis
- Contadores de solicitações por status
- Tempo de processamento por etapa
- Taxa de aprovação/rejeição
- Métricas de integração com API de fraudes
- Métricas de consumo/produção Kafka

### Health Checks
- `/actuator/health` - Status geral da aplicação
- `/actuator/health/db` - Status do banco de dados
- `/actuator/health/kafka` - Status do Kafka

### Logs
- Logs estruturados em JSON
- Correlation IDs para rastreamento
- Diferentes níveis por ambiente

## Decisões Arquiteturais

### Por que Arquitetura Hexagonal?
- **Testabilidade**: Facilita testes unitários isolando o domínio
- **Flexibilidade**: Permite trocar implementações de infraestrutura
- **Manutenibilidade**: Separação clara de responsabilidades

### Por que Event-Driven Architecture?
- **Desacoplamento**: Serviços independentes comunicam via eventos
- **Escalabilidade**: Processamento assíncrono melhora performance
- **Resiliência**: Eventos podem ser reprocessados em caso de falha

### Por que PostgreSQL?
- **ACID**: Garantias transacionais essenciais para domínio financeiro
- **JSON Support**: Flexibilidade para campos como coberturas
- **Performance**: Excelente performance para consultas relacionais

### Por que Kafka?
- **Alta disponibilidade**: Garantia de entrega de eventos
- **Ordenação**: Manutenção da ordem dos eventos por partição
- **Escalabilidade**: Suporte a alto volume de mensagens

## Premissas e Limitações

### Premissas Assumidas
1. **API de Fraudes Externa**: Assumimos que existe uma API externa real (mock implementado)
2. **Eventos de Pagamento**: Sistemas externos publicam eventos de confirmação
3. **IDs de Cliente**: Clientes são gerenciados por sistema externo
4. **Produtos Pré-cadastrados**: IDs de produtos são válidos quando informados

### Limitações Conhecidas
1. **Processamento Síncrono**: Por simplicidade, análise de fraudes é síncrona
2. **Sem Retry Policy**: Falhas na API de fraudes resultam em classificação conservadora
3. **Eventos Simplificados**: Estrutura de eventos pode precisar de mais campos
4. **Sem Versionamento**: API não possui versionamento de schema

## Próximos Passos

### Melhorias Técnicas
- [ ] Implementar Circuit Breaker para API de fraudes
- [ ] Adicionar retry policy com backoff exponencial
- [ ] Implementar cache para consultas frequentes
- [ ] Adicionar rate limiting nos endpoints
- [ ] Implementar saga pattern para transações distribuídas

### Funcionalidades
- [ ] Notificações por email/SMS para clientes
- [ ] Dashboard administrativo para operadores
- [ ] Relatórios e analytics de solicitações
- [ ] API de webhook para sistemas externos
- [ ] Integração com sistema de cobrança

### DevOps
- [ ] Pipeline CI/CD completo
- [ ] Deploy em Kubernetes
- [ ] Monitoring avançado com alertas
- [ ] Backup automatizado do banco
- [ ] Disaster recovery plan

## Contribuição

### Como Contribuir
1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

### Padrões de Código
- Usar anotações do Lombok apenas quando necessário
- Manter cobertura de testes > 90%
- Documentar métodos públicos com JavaDoc
- Seguir convenções do Spring Boot
- Commits em inglês, documentação em português

## Licença

Este projeto está licenciado sob a Licença Proprietária da ACME Seguros.

## Contato

**Equipe de Desenvolvimento ACME**
- Email: dev@acme.com
- Documentação: https://docs.acme.com/policy-service

---

*Gerado pela equipe de desenvolvimento ACME © 2024*