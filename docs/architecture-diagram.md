# Diagrama da Arquitetura - Policy Service API

## Arquitetura EDA (Event-Driven Architecture)

```mermaid
graph TD
    %% Cliente Apps
    subgraph "Client Layer"
        CA[Client Apps<br/>Mobile/Web/WhatsApp]
    end

    %% Policy Service API Principal
    subgraph "Policy Service API"
        subgraph "Infrastructure Layer"
            REST[REST Controllers<br/>PolicyRequestController]
            MSG[Message Producers<br/>PolicyEventPublisher]
            EXT[External Integrations<br/>FraudAnalysisService]
        end
        
        subgraph "Application Layer"
            SVC[Application Services<br/>PolicyRequestService]
            DTO[DTOs & Mappers<br/>PolicyRequestMapper]
        end
        
        subgraph "Domain Layer"
            ENT[Domain Entities<br/>PolicyRequest, StatusHistory]
            REPO[Repository Interfaces<br/>PolicyRequestRepository]
            RULES[Business Rules<br/>PolicyValidationService]
        end
    end

    %% Serviços Externos
    subgraph "External Services"
        FRAUD[Frauds API Mock<br/>WireMock]
    end

    %% Infraestrutura
    subgraph "Data & Messaging"
        DB[(PostgreSQL<br/>JPA)]
        KAFKA[Apache Kafka<br/>Events & Messages]
    end

    %% Observabilidade
    subgraph "Observability"
        PROM[Prometheus<br/>Metrics]
        GRAF[Grafana<br/>Dashboards]
        ACT[Spring Actuator<br/>Health Checks]
    end

    %% Conexões
    CA --> REST
    REST --> SVC
    SVC --> DTO
    SVC --> RULES
    SVC --> REPO
    SVC --> EXT
    SVC --> MSG
    
    EXT --> FRAUD
    REPO --> DB
    MSG --> KAFKA
    
    REST --> ACT
    ACT --> PROM
    PROM --> GRAF

    %% Eventos Kafka
    KAFKA -.-> |payments.events<br/>underwriting.events| SVC
    MSG -.-> |policy-requests.events| KAFKA

    %% Estilos
    classDef clientStyle fill:#e1f5fe,stroke:#01579b,stroke-width:2px
    classDef serviceStyle fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    classDef dataStyle fill:#e8f5e8,stroke:#1b5e20,stroke-width:2px
    classDef obsStyle fill:#fff3e0,stroke:#e65100,stroke-width:2px
    classDef extStyle fill:#fce4ec,stroke:#880e4f,stroke-width:2px

    class CA clientStyle
    class REST,SVC,DTO,ENT,REPO,RULES,MSG serviceStyle
    class DB,KAFKA dataStyle
    class PROM,GRAF,ACT obsStyle
    class FRAUD extStyle
```

## Fluxo de Dados Principal

```mermaid
sequenceDiagram
    participant Client as Client Apps
    participant API as REST API
    participant Service as Policy Service
    participant Fraud as Fraud API
    participant DB as PostgreSQL
    participant Kafka as Apache Kafka
    
    Client->>API: POST /policy-requests
    API->>Service: createPolicyRequest()
    Service->>DB: save(PolicyRequest)
    DB-->>Service: PolicyRequest saved
    Service->>Kafka: publish CREATED event
    
    Service->>Fraud: analyzeFraud()
    Fraud-->>Service: RiskClassification
    Service->>Service: validateRules()
    
    alt Valid Request
        Service->>DB: updateStatus(VALIDATED)
        Service->>Kafka: publish VALIDATED event
        Service->>DB: updateStatus(PENDING)
        Service->>Kafka: publish PENDING event
    else Invalid Request
        Service->>DB: updateStatus(REJECTED)
        Service->>Kafka: publish REJECTED event
    end
    
    API-->>Client: PolicyRequest created
    
    Note over Kafka: External systems consume events<br/>for payments & underwriting
    
    Kafka->>Service: payment confirmed
    Kafka->>Service: underwriting approved
    Service->>DB: updateStatus(APPROVED)
    Service->>Kafka: publish APPROVED event
```

## Estados da Máquina de Estados

```mermaid
stateDiagram-v2
    [*] --> RECEIVED : Create Request
    
    RECEIVED --> VALIDATED : Fraud Analysis OK
    RECEIVED --> REJECTED : Validation Failed
    RECEIVED --> CANCELLED : User Cancellation
    
    VALIDATED --> PENDING : Auto Transition
    VALIDATED --> REJECTED : Business Rules Failed
    VALIDATED --> CANCELLED : User Cancellation
    
    PENDING --> APPROVED : Payment + Underwriting OK
    PENDING --> REJECTED : Payment/Underwriting Failed
    PENDING --> CANCELLED : User Cancellation
    
    APPROVED --> [*]
    REJECTED --> [*]
    CANCELLED --> [*]
    
    note right of RECEIVED
        Initial state after creation
        Triggers fraud analysis
    end note
    
    note right of PENDING
        Awaiting external confirmations
        - Payment processing
        - Underwriting approval
    end note
```

## Componentes por Camada

### Infrastructure Layer
- **REST Controllers**: Endpoints da API REST
- **Message Producers**: Publicação de eventos Kafka
- **External Integrations**: Clientes para APIs externas
- **JPA Repositories**: Implementações de persistência

### Application Layer  
- **Application Services**: Orquestração de casos de uso
- **DTOs**: Objetos de transferência de dados
- **Mappers**: Conversão entre DTOs e entidades

### Domain Layer
- **Entities**: Modelagem rica do domínio
- **Repository Interfaces**: Contratos de persistência  
- **Domain Services**: Regras de negócio complexas
- **Value Objects**: Objetos de valor imutáveis

## Tecnologias por Componente

| Componente | Tecnologia | Propósito |
|------------|------------|-----------|
| **API REST** | Spring Web | Exposição de endpoints HTTP |
| **Persistence** | Spring Data JPA + PostgreSQL | Persistência transacional |
| **Messaging** | Spring Kafka | Comunicação assíncrona |
| **Validation** | Bean Validation | Validação de entrada |
| **Documentation** | OpenAPI 3 + Swagger | Documentação interativa |
| **Testing** | JUnit 5 + Cucumber + TestContainers | Testes automatizados |
| **Observability** | Actuator + Prometheus + Grafana | Monitoramento e métricas |
| **Containerization** | Docker + Docker Compose | Orquestração de containers |

## Padrões de Arquitetura Aplicados

- **Hexagonal Architecture**: Isolamento do domínio
- **Event-Driven Architecture**: Comunicação via eventos  
- **CQRS**: Separação de comandos e consultas
- **Domain-Driven Design**: Modelagem orientada ao domínio
- **Repository Pattern**: Abstração de persistência
- **Factory Pattern**: Criação de objetos complexos
- **Observer Pattern**: Notificação de mudanças de estado