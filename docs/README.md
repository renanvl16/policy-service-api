# Documentação Técnica - Policy Service API

Esta documentação contém informações técnicas detalhadas sobre a arquitetura e implementação do Policy Service API.

## Índice

- [Diagrama de Arquitetura Detalhado](architecture-diagram.md)
- [Fluxos de Dados](data-flows.md)
- [Estados e Transições](state-machine.md)
- [Integração com APIs Externas](external-integrations.md)
- [Eventos Kafka](kafka-events.md)
- [Guias de Desenvolvimento](development-guides.md)

## Visão Geral da Arquitetura

O Policy Service API implementa uma arquitetura orientada a eventos (EDA) com os seguintes componentes principais:

### Camadas da Aplicação

#### 1. Infrastructure Layer (Infraestrutura)
Responsável pela comunicação com o mundo externo:
- **REST Controllers**: Endpoints HTTP para clientes
- **Message Producers/Consumers**: Integração com Kafka
- **External Clients**: Integração com APIs de terceiros
- **JPA Repositories**: Persistência no banco de dados

#### 2. Application Layer (Aplicação)
Orquestra os casos de uso do sistema:
- **Application Services**: Coordenação de operações de negócio
- **DTOs**: Objetos de transferência de dados
- **Mappers**: Conversão entre diferentes representações

#### 3. Domain Layer (Domínio)
Contém as regras de negócio centrais:
- **Entities**: Entidades ricas do domínio
- **Value Objects**: Objetos imutáveis com comportamento
- **Domain Services**: Regras de negócio complexas
- **Repository Interfaces**: Contratos de persistência

## Componentes Principais

### PolicyRequest (Entidade Principal)
A entidade central que representa uma solicitação de apólice:
- Controla o ciclo de vida através de estados
- Mantém histórico completo de alterações
- Aplica regras de transição de estados
- Integra com análise de fraudes

### Estado Machine
Implementação robusta de máquina de estados:
```
RECEIVED -> VALIDATED -> PENDING -> APPROVED
    |           |          |
    v           v          v
CANCELLED   CANCELLED  REJECTED/CANCELLED
    |           |
    v           v
REJECTED    REJECTED
```

### Event Publishing
Sistema de eventos para comunicação assíncrona:
- Eventos publicados a cada mudança de estado
- Integração com sistemas de pagamento e subscrição
- Garantia de entrega e ordenação através do Kafka

## Tecnologias e Ferramentas

### Core Framework
- **Spring Boot 3.2**: Framework principal
- **Java 21**: Linguagem de programação
- **Maven**: Gerenciamento de dependências

### Persistência
- **PostgreSQL 15**: Banco de dados principal  
- **Spring Data JPA**: Abstração de persistência
- **Flyway**: Migrações de banco de dados

### Mensageria
- **Apache Kafka**: Streaming de eventos
- **Spring Kafka**: Integração com Spring

### Testes
- **JUnit 5**: Testes unitários
- **Cucumber**: Testes BDD
- **TestContainers**: Testes de integração
- **Jacoco**: Cobertura de código

### Observabilidade
- **Spring Actuator**: Health checks e métricas
- **Prometheus**: Coleta de métricas
- **Grafana**: Visualização de dashboards

### Desenvolvimento
- **Lombok**: Redução de boilerplate
- **MapStruct**: Mapeamento automático
- **OpenAPI 3**: Documentação de API

## Padrões de Design Aplicados

### Arquiteturais
- **Hexagonal Architecture**: Isolamento do domínio
- **Event-Driven Architecture**: Comunicação via eventos
- **CQRS**: Separação de comandos e consultas
- **Domain-Driven Design**: Modelagem rica do domínio

### Código
- **Repository Pattern**: Abstração de persistência
- **Factory Pattern**: Criação de objetos
- **Observer Pattern**: Notificação de eventos
- **Strategy Pattern**: Algoritmos de validação
- **State Pattern**: Controle de estados

## Qualidade e Boas Práticas

### Cobertura de Testes
- Meta: >90% de cobertura de código
- Testes unitários para toda lógica de negócio
- Testes de integração para fluxos completos
- Testes BDD para validação de requisitos

### Documentação
- JavaDoc em português para todas as classes públicas
- README detalhado com guias de execução
- API documentada com OpenAPI/Swagger
- Diagramas de arquitetura atualizados

### Observabilidade
- Logs estruturados com correlation IDs
- Métricas customizadas de negócio
- Health checks para todas as dependências
- Dashboards para monitoramento operacional

## Próximos Passos

Para informações específicas sobre cada aspecto do sistema, consulte os documentos específicos na pasta `docs/`.