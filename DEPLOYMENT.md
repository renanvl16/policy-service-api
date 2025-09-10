# Guia de Deploy - Policy Service API

Este documento fornece instruções detalhadas para deploy do Policy Service API em diferentes ambientes.

## 🏗️ Pré-requisitos

### Ambiente Local
- Docker 20.10+
- Docker Compose 2.0+
- Java 21 (para desenvolvimento)
- Maven 3.9+ (para desenvolvimento)

### Ambiente de Produção
- Docker Swarm ou Kubernetes
- PostgreSQL 15+ (managed service recomendado)
- Apache Kafka (managed service recomendado)
- Load Balancer (Nginx/HAProxy)
- Sistema de monitoramento (Prometheus/Grafana)

## 🚀 Deploy Local (Desenvolvimento)

### Opção 1: Docker Compose (Recomendado)
```bash
# Clonar repositório
git clone <repository-url>
cd policy-request-service

# Iniciar todos os serviços
./scripts/start-services.sh

# Ou manualmente
docker-compose up -d
```

### Opção 2: Execução Híbrida
```bash
# Subir apenas dependências
docker-compose up -d postgres kafka zookeeper

# Executar aplicação localmente
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## 🌩️ Deploy em Cloud

### AWS

#### Usando ECS (Elastic Container Service)
```yaml
# ecs-task-definition.json
{
  "family": "policy-request-service",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "1024",
  "memory": "2048",
  "executionRoleArn": "arn:aws:iam::ACCOUNT:role/ecsTaskExecutionRole",
  "taskRoleArn": "arn:aws:iam::ACCOUNT:role/ecsTaskRole",
  "containerDefinitions": [
    {
      "name": "policy-request-service",
      "image": "policy-request-service:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "prod"
        },
        {
          "name": "SPRING_DATASOURCE_URL", 
          "value": "jdbc:postgresql://rds-endpoint:5432/policy_db"
        }
      ],
      "secrets": [
        {
          "name": "DB_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:region:account:secret:db-password"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/policy-request-service",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"],
        "interval": 30,
        "timeout": 5,
        "retries": 3
      }
    }
  ]
}
```

#### Usando RDS e MSK
```bash
# Criar RDS PostgreSQL
aws rds create-db-instance \
    --db-instance-identifier policy-request-db \
    --db-instance-class db.t3.micro \
    --engine postgres \
    --engine-version 15.4 \
    --master-username admin \
    --master-user-password <password> \
    --allocated-storage 20

# Criar MSK Kafka Cluster
aws kafka create-cluster \
    --cluster-name policy-request-kafka \
    --broker-node-group-info file://broker-info.json \
    --kafka-version 3.4.0
```

### Azure

#### Usando Container Instances
```bash
# Criar Resource Group
az group create --name policy-request-rg --location eastus

# Criar Azure Database para PostgreSQL
az postgres server create \
    --resource-group policy-request-rg \
    --name policy-request-db \
    --location eastus \
    --admin-user admin \
    --admin-password <password> \
    --sku-name GP_Gen5_2

# Deploy da aplicação
az container create \
    --resource-group policy-request-rg \
    --name policy-request-service \
    --image policy-request-service:latest \
    --cpu 2 \
    --memory 4 \
    --ports 8080 \
    --environment-variables \
        SPRING_PROFILES_ACTIVE=prod \
        SPRING_DATASOURCE_URL=jdbc:postgresql://policy-request-db.postgres.database.azure.com:5432/policy_db
```

### Google Cloud Platform

#### Usando Cloud Run
```bash
# Build e push da imagem
gcloud builds submit --tag gcr.io/PROJECT-ID/policy-request-service

# Deploy no Cloud Run
gcloud run deploy policy-request-service \
    --image gcr.io/PROJECT-ID/policy-request-service \
    --platform managed \
    --region us-central1 \
    --allow-unauthenticated \
    --set-env-vars SPRING_PROFILES_ACTIVE=prod \
    --set-cloudsql-instances PROJECT-ID:REGION:INSTANCE-NAME \
    --memory 2Gi \
    --cpu 2
```

## 🐳 Kubernetes

### Namespace e ConfigMap
```yaml
# namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: policy-request-service

---
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: policy-request-config
  namespace: policy-request-service
data:
  SPRING_PROFILES_ACTIVE: "prod"
  SERVER_PORT: "8080"
  LOG_LEVEL: "INFO"
```

### Secret para Credenciais
```yaml
# secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: policy-request-secrets
  namespace: policy-request-service
type: Opaque
data:
  DB_PASSWORD: <base64-encoded-password>
  KAFKA_PASSWORD: <base64-encoded-password>
```

### Deployment
```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: policy-request-service
  namespace: policy-request-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: policy-request-service
  template:
    metadata:
      labels:
        app: policy-request-service
    spec:
      containers:
      - name: policy-request-service
        image: policy-request-service:latest
        ports:
        - containerPort: 8080
        envFrom:
        - configMapRef:
            name: policy-request-config
        - secretRef:
            name: policy-request-secrets
        env:
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:postgresql://postgres-service:5432/policy_db"
        - name: KAFKA_BOOTSTRAP_SERVERS
          value: "kafka-service:9092"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
```

### Service e Ingress
```yaml
# service.yaml
apiVersion: v1
kind: Service
metadata:
  name: policy-request-service
  namespace: policy-request-service
spec:
  selector:
    app: policy-request-service
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: ClusterIP

---
# ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: policy-request-ingress
  namespace: policy-request-service
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-prod
spec:
  tls:
  - hosts:
    - api.acme.com
    secretName: policy-request-tls
  rules:
  - host: api.acme.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: policy-request-service
            port:
              number: 80
```

## 📊 Monitoramento em Produção

### Prometheus Configuration
```yaml
# prometheus-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-config
data:
  prometheus.yml: |
    global:
      scrape_interval: 15s
    scrape_configs:
    - job_name: 'policy-request-service'
      kubernetes_sd_configs:
      - role: endpoints
        namespaces:
          names:
          - policy-request-service
      relabel_configs:
      - source_labels: [__meta_kubernetes_service_name]
        action: keep
        regex: policy-request-service
```

### Grafana Dashboard
```json
{
  "dashboard": {
    "id": null,
    "title": "Policy Service API",
    "panels": [
      {
        "title": "Request Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_requests_total[5m])",
            "legendFormat": "{{method}} {{status}}"
          }
        ]
      },
      {
        "title": "Response Time", 
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))",
            "legendFormat": "95th percentile"
          }
        ]
      }
    ]
  }
}
```

## 🔐 Segurança

### SSL/TLS
```nginx
# nginx.conf
server {
    listen 443 ssl http2;
    server_name api.acme.com;
    
    ssl_certificate /etc/ssl/certs/acme.crt;
    ssl_certificate_key /etc/ssl/private/acme.key;
    
    location / {
        proxy_pass http://policy-request-service:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### Network Policies (Kubernetes)
```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: policy-request-network-policy
  namespace: policy-request-service
spec:
  podSelector:
    matchLabels:
      app: policy-request-service
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to:
    - namespaceSelector:
        matchLabels:
          name: database
    ports:
    - protocol: TCP
      port: 5432
```

## 📈 Escalabilidade

### Horizontal Pod Autoscaler
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: policy-request-hpa
  namespace: policy-request-service
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: policy-request-service
  minReplicas: 3
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

## 🧪 Estratégias de Deploy

### Blue-Green Deployment
```bash
# Deploy versão green
kubectl set image deployment/policy-request-service \
    policy-request-service=policy-request-service:v2.0.0

# Aguardar e verificar saúde
kubectl rollout status deployment/policy-request-service

# Se houver problema, fazer rollback
kubectl rollout undo deployment/policy-request-service
```

### Canary Deployment
```yaml
apiVersion: argoproj.io/v1alpha1
kind: Rollout
metadata:
  name: policy-request-rollout
spec:
  replicas: 10
  strategy:
    canary:
      steps:
      - setWeight: 10
      - pause: {duration: 10m}
      - setWeight: 30
      - pause: {duration: 10m}
      - setWeight: 50
      - pause: {duration: 10m}
  selector:
    matchLabels:
      app: policy-request-service
  template:
    # ... template spec
```

## 🔍 Troubleshooting

### Logs Centralizados
```yaml
# fluentd-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: fluentd-config
data:
  fluent.conf: |
    <source>
      @type tail
      path /var/log/containers/*policy-request-service*.log
      pos_file /var/log/fluentd-containers.log.pos
      tag kubernetes.*
      format json
    </source>
    
    <match kubernetes.**>
      @type elasticsearch
      host elasticsearch-service
      port 9200
      index_name policy-request-logs
    </match>
```

### Health Checks
```bash
# Verificar saúde da aplicação
curl -f https://api.acme.com/actuator/health

# Verificar métricas
curl https://api.acme.com/actuator/metrics

# Verificar logs
kubectl logs -f deployment/policy-request-service
```

## 📋 Checklist de Deploy

### Pré-Deploy
- [ ] Testes passando (unit + integration)
- [ ] Cobertura de código >90%
- [ ] Imagem Docker construída e testada
- [ ] Variáveis de ambiente configuradas
- [ ] Secrets configurados
- [ ] Banco de dados migrado

### Deploy
- [ ] Deploy realizado com sucesso
- [ ] Health checks passando
- [ ] Logs sem erros críticos
- [ ] Métricas sendo coletadas
- [ ] Testes de smoke executados

### Pós-Deploy
- [ ] Monitoramento ativo
- [ ] Alertas configurados
- [ ] Backup funcionando
- [ ] Documentação atualizada
- [ ] Equipe notificada

---

Para mais informações sobre configuração específica, consulte:
- [README.md](README.md) - Documentação geral
- [run.md](run.md) - Guia de execução local
- [CHANGELOG.md](CHANGELOG.md) - Histórico de versões