# Changelog

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2024-01-15

### ✨ Adicionado
- **Core Features**
  - API REST completa para gerenciamento de solicitações de apólice
  - Máquina de estados robusta com 6 estados (RECEIVED → VALIDATED → PENDING → APPROVED/REJECTED/CANCELLED)
  - Integração com análise de fraudes (mock implementado)
  - Regras de validação por classificação de risco de cliente
  - Sistema de eventos Kafka para comunicação assíncrona
  - Histórico completo de alterações de estado

- **Arquitetura**
  - Arquitetura Hexagonal com separação clara de responsabilidades
  - Event-Driven Architecture (EDA) com Kafka
  - Implementação de Domain-Driven Design (DDD)
  - Padrão CQRS para separação de comandos e consultas

- **Stack Tecnológica**
  - Java 21 + Spring Boot 3.2
  - PostgreSQL 15 com JPA e Flyway
  - Apache Kafka para mensageria
  - Maven para gerenciamento de dependências
  - Lombok para redução de boilerplate

- **Testes**
  - Testes unitários com JUnit 5 (meta: 90% cobertura)
  - Testes de integração com Cucumber (BDD)
  - TestContainers para testes com infraestrutura real
  - Relatório de cobertura com Jacoco

- **Documentação**
  - JavaDoc completo em português
  - OpenAPI 3.0/Swagger para documentação da API
  - README detalhado com diagramas de arquitetura
  - Guia de execução (run.md)

- **DevOps & Infraestrutura**
  - Dockerfile otimizado com multi-stage build
  - Docker Compose completo com todas as dependências
  - Configuração de monitoramento (Prometheus + Grafana)
  - Health checks e métricas customizadas

- **Observabilidade**
  - Logs estruturados com SLF4J
  - Métricas de aplicação exportadas para Prometheus
  - Dashboards Grafana para monitoramento
  - Health checks para todas as dependências

- **Qualidade de Código**
  - Validação de entrada com Bean Validation
  - Tratamento centralizado de exceções
  - Configuração por perfis (local, docker, prod)
  - Padrões de código consistentes

### 📋 **Funcionalidades Principais**

#### Gestão de Solicitações
- [x] Criar solicitação de apólice com validação completa
- [x] Consultar solicitação por ID
- [x] Consultar solicitações por ID do cliente  
- [x] Cancelar solicitação (exceto se já aprovada)
- [x] Histórico completo de alterações

#### Classificações de Risco
- [x] REGULAR - Limites padrão por categoria
- [x] HIGH_RISK - Limites reduzidos para maior cautela
- [x] PREFERENTIAL - Limites elevados para clientes premium
- [x] NO_INFORMATION - Limites conservadores para novos clientes

#### Categorias de Seguro
- [x] AUTO - Seguro automotivo
- [x] VIDA - Seguro de vida
- [x] RESIDENCIAL - Seguro residencial
- [x] EMPRESARIAL - Seguro empresarial

#### Estados do Ciclo de Vida
- [x] RECEIVED - Estado inicial após criação
- [x] VALIDATED - Aprovado na análise de fraudes
- [x] PENDING - Aguardando pagamento/subscrição
- [x] APPROVED - Aprovado para emissão
- [x] REJECTED - Rejeitado por regras de negócio
- [x] CANCELLED - Cancelado pelo cliente

### 🔧 **Integrações**

#### API de Fraudes (Mock)
- [x] Análise automática de risco por cliente
- [x] Classificação em 4 níveis de risco
- [x] Fallback em caso de falha da API
- [x] Simulação realística com ocorrências

#### Eventos Kafka
- [x] Publicação de eventos de mudança de estado
- [x] Consumo de eventos de pagamento
- [x] Consumo de eventos de subscrição
- [x] Garantia de entrega e ordenação

### 📊 **Métricas e Monitoramento**
- [x] Contadores de solicitações por status
- [x] Tempo de processamento por etapa
- [x] Taxa de aprovação/rejeição por classificação
- [x] Métricas de integração com API externa
- [x] Métricas de produção/consumo Kafka

### 🧪 **Testes e Qualidade**
- [x] 90%+ cobertura de testes unitários
- [x] Testes BDD com Cucumber em português
- [x] Testes de integração com TestContainers
- [x] Testes de performance e stress
- [x] Validação de contratos de API

### 📦 **Entregáveis**
- [x] Código fonte completo com arquitetura hexagonal
- [x] Scripts SQL de migração (Flyway)
- [x] Dockerfile e Docker Compose prontos para produção
- [x] Coleção Postman para testes manuais
- [x] Documentação completa (README + run.md)
- [x] Configurações de monitoramento
- [x] Exemplos de uso e integração

### 🎯 **Decisões Arquiteturais**

#### Por que Arquitetura Hexagonal?
- Facilita testes unitários isolando o domínio
- Permite trocar implementações de infraestrutura
- Separação clara entre regras de negócio e detalhes técnicos

#### Por que Event-Driven Architecture?
- Desacoplamento entre serviços
- Processamento assíncrono para melhor performance  
- Facilita adição de novos consumers sem impacto

#### Por que PostgreSQL?
- Garantias ACID essenciais para domínio financeiro
- Suporte nativo a JSON para campos flexíveis
- Performance excelente para consultas relacionais

### 🔮 **Próximas Versões**

#### [1.1.0] - Planejado
- [ ] Circuit breaker para API de fraudes
- [ ] Cache Redis para consultas frequentes
- [ ] Rate limiting nos endpoints
- [ ] Notificações por email/SMS

#### [1.2.0] - Planejado  
- [ ] Dashboard administrativo
- [ ] Relatórios e analytics
- [ ] API de webhooks
- [ ] Integração com sistema de cobrança

#### [2.0.0] - Planejado
- [ ] Saga pattern para transações distribuídas
- [ ] Deploy em Kubernetes
- [ ] Backup automatizado
- [ ] Disaster recovery

---

### 📞 **Suporte**
Para dúvidas ou sugestões:
- **Email**: dev@acme.com
- **Documentação**: [README.md](README.md)
- **Issues**: Repositório do projeto

---

*Desenvolvido com ❤️ pela equipe ACME*