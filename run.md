# Guia de Execução - Policy Service API

Este guia detalha como executar o Policy Service API em diferentes ambientes.

## 🚀 Execução Rápida (Docker Compose)

### Pré-requisitos
- Docker 20.10+
- Docker Compose 2.0+
- 8GB RAM disponível
- Portas disponíveis: 8080, 5432, 9092, 3000, 9090, 8090

### 1. Inicialização Completa
```bash
# Clonar o projeto (se não tiver feito ainda)
git clone <repository-url>
cd policy-request-service

# Subir todos os serviços
docker-compose up -d

# Verificar status dos containers
docker-compose ps

# Aguardar inicialização (pode levar 2-3 minutos)
docker-compose logs -f policy-request-service
```

### 2. Verificar Funcionamento
```bash
# Health check da aplicação
curl http://localhost:8080/actuator/health

# Documentação da API
open http://localhost:8080/swagger-ui.html
```

### 3. Parar Serviços
```bash
# Parar containers mantendo dados
docker-compose stop

# Parar e remover containers (mantém volumes)
docker-compose down

# Remover tudo incluindo volumes (CUIDADO!)
docker-compose down -v
```

## 🛠️ Execução para Desenvolvimento

### Pré-requisitos
- Java 21
- Maven 3.9+
- Docker (para dependências)

### 1. Subir Dependências
```bash
# Apenas PostgreSQL e Kafka
docker-compose up -d postgres kafka zookeeper

# Aguardar inicialização
sleep 30
```

### 2. Executar Aplicação
```bash
# Executar com Maven
mvn spring-boot:run

# Ou compilar e executar JAR
mvn clean package -DskipTests
java -jar target/policy-request-service-1.0.0.jar

# Com profile específico
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### 3. Executar Testes
```bash
# Testes unitários
mvn test

# Testes de integração (com TestContainers)
mvn verify

# Gerar relatório de cobertura
mvn jacoco:report
open target/site/jacoco/index.html
```

## 🧪 Testando a API

### 1. Criar uma Solicitação
```bash
curl -X POST http://localhost:8080/api/v1/policy-requests \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "550e8400-e29b-41d4-a716-446655440001",
    "productId": "PROD-AUTO-BASIC",
    "category": "AUTO", 
    "salesChannel": "MOBILE",
    "paymentMethod": "CREDIT_CARD",
    "totalMonthlyPremiumAmount": 150.00,
    "insuredAmount": 250000.00,
    "coverages": {
      "Roubo": 100000.00,
      "Colisão": 150000.00
    },
    "assistances": ["Guincho 24h", "Chaveiro"]
  }'
```

### 2. Consultar Solicitação
```bash
# Usar o ID retornado na criação
POLICY_ID="<id-retornado>"
curl http://localhost:8080/api/v1/policy-requests/$POLICY_ID
```

### 3. Consultar por Cliente
```bash
CUSTOMER_ID="550e8400-e29b-41d4-a716-446655440001"
curl http://localhost:8080/api/v1/policy-requests/customer/$CUSTOMER_ID
```

### 4. Cancelar Solicitação
```bash
curl -X POST http://localhost:8080/api/v1/policy-requests/$POLICY_ID/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Cliente desistiu da contratação"
  }'
```

## 🏗️ Build e Deploy

### Build da Imagem Docker
```bash
# Build da aplicação
docker build -t policy-request-service:latest .

# Build com tag específica
docker build -t policy-request-service:1.0.0 .

# Build multi-arquitetura (ARM + AMD64)
docker buildx build --platform linux/amd64,linux/arm64 \
  -t policy-request-service:latest .
```

### Deploy em Ambiente de Produção
```bash
# Definir variáveis de ambiente
export DB_PASSWORD=sua_senha_segura
export KAFKA_BOOTSTRAP_SERVERS=kafka-prod:9092

# Executar com configurações de produção
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

## 🐞 Resolução de Problemas

### Problema: Porta 8080 já está em uso
```bash
# Verificar processo usando a porta
sudo lsof -i :8080

# Alterar porta da aplicação
export SERVER_PORT=8081
docker-compose up -d
```

### Problema: PostgreSQL não inicia
```bash
# Verificar logs do PostgreSQL
docker-compose logs postgres

# Remover volume corrompido
docker-compose down -v
docker volume rm policy-request-service_postgres_data
docker-compose up -d postgres
```

### Problema: Kafka não conecta
```bash
# Verificar se o Zookeeper está funcionando
docker-compose logs zookeeper

# Reiniciar Kafka
docker-compose restart kafka

# Aguardar inicialização
sleep 30
```

### Problema: Aplicação não inicia
```bash
# Verificar logs detalhados
docker-compose logs -f policy-request-service

# Verificar configurações
docker-compose exec policy-request-service env | grep SPRING

# Executar health check manual
docker-compose exec policy-request-service \
  curl http://localhost:8080/actuator/health
```

## 📊 Monitoramento

### Acessar Dashboards
```bash
# Grafana (admin/admin123)
open http://localhost:3000

# Prometheus
open http://localhost:9090

# Kafka UI
open http://localhost:8090
```

### Verificar Métricas
```bash
# Métricas da aplicação
curl http://localhost:8080/actuator/metrics

# Métricas Prometheus
curl http://localhost:8080/actuator/prometheus

# Health checks específicos
curl http://localhost:8080/actuator/health/db
curl http://localhost:8080/actuator/health/kafka
```

### Logs da Aplicação
```bash
# Logs em tempo real
docker-compose logs -f policy-request-service

# Logs específicos
docker-compose logs --since="1h" policy-request-service

# Buscar por erro
docker-compose logs policy-request-service | grep ERROR
```

## 🔧 Configurações Avançadas

### Variáveis de Ambiente Importantes
```bash
# Banco de dados
DB_USERNAME=policy_user
DB_PASSWORD=policy_password
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/policy_request_db

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
KAFKA_CONSUMER_GROUP=policy-request-service

# Aplicação
SPRING_PROFILES_ACTIVE=docker
LOG_LEVEL=INFO
SERVER_PORT=8080

# API de Fraudes
FRAUD_MOCK_ENABLED=true
FRAUD_API_URL=http://fraud-api:8080/analyze

# JVM
JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
```

### Profiles Disponíveis
- `default`: Configuração padrão
- `local`: Para desenvolvimento local
- `docker`: Para execução em container
- `test`: Para execução de testes
- `prod`: Para ambiente de produção

### Configurações de Performance
```bash
# Para ambiente com pouco recursos
export JAVA_OPTS="-XX:MaxRAMPercentage=50.0 -XX:+UseSerialGC"

# Para ambiente com muitos recursos
export JAVA_OPTS="-XX:MaxRAMPercentage=90.0 -XX:+UseG1GC -XX:G1HeapRegionSize=32m"
```

## 🗄️ Backup e Restore

### Backup do Banco
```bash
# Backup via Docker
docker-compose exec postgres pg_dump -U policy_user policy_request_db > backup.sql

# Restore
docker-compose exec -T postgres psql -U policy_user policy_request_db < backup.sql
```

### Backup de Tópicos Kafka
```bash
# Listar tópicos
docker-compose exec kafka kafka-topics \
  --bootstrap-server localhost:9092 --list

# Backup (consumir todas as mensagens)
docker-compose exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic policy-requests.events \
  --from-beginning > kafka-backup.json
```

## 📚 Recursos Adicionais

### Documentação
- **API**: http://localhost:8080/swagger-ui.html
- **Actuator**: http://localhost:8080/actuator
- **OpenAPI Spec**: http://localhost:8080/api-docs

### Comandos Úteis
```bash
# Ver todos os containers
docker ps -a

# Limpar containers parados
docker container prune

# Limpar imagens não utilizadas
docker image prune

# Ver uso de recursos
docker stats

# Executar comando dentro do container
docker-compose exec policy-request-service bash
```

### Scripts de Automação
```bash
# Script de deploy completo
#!/bin/bash
echo "Iniciando deploy do Policy Service API..."
docker-compose pull
docker-compose up -d
echo "Aguardando inicialização..."
sleep 60
curl -f http://localhost:8080/actuator/health || exit 1
echo "Deploy concluído com sucesso!"
```

---

Para mais informações, consulte o [README.md](README.md) principal.

**Suporte**: dev@acme.com