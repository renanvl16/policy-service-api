#!/bin/bash

# Script para iniciar todos os serviços do Policy Service API
# Autor: Sistema ACME

set -e  # Sair em caso de erro

echo "🚀 Iniciando Policy Service API..."
echo "======================================="

# Verificar se Docker está instalado
if ! command -v docker &> /dev/null; then
    echo "❌ Docker não está instalado. Por favor, instale o Docker primeiro."
    exit 1
fi

# Verificar se Docker Compose está instalado
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose não está instalado. Por favor, instale o Docker Compose primeiro."
    exit 1
fi

# Verificar se estamos no diretório correto
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ Arquivo docker-compose.yml não encontrado. Execute este script no diretório raiz do projeto."
    exit 1
fi

echo "✅ Verificações preliminares concluídas"
echo

# Parar containers existentes (se houver)
echo "🛑 Parando containers existentes..."
docker-compose down 2>/dev/null || true

# Limpar volumes órfãos (opcional)
echo "🧹 Limpando volumes órfãos..."
docker system prune -f --volumes 2>/dev/null || true

# Construir imagens se necessário
echo "🏗️  Construindo imagens..."
docker-compose build --no-cache policy-service-api

echo "🌟 Iniciando serviços..."
echo

# Iniciar dependências primeiro (PostgreSQL, Kafka)
echo "📦 Iniciando dependências (PostgreSQL, Kafka, Zookeeper)..."
docker-compose up -d postgres zookeeper kafka

echo "⏳ Aguardando inicialização das dependências..."
sleep 30

# Verificar se PostgreSQL está pronto
echo "🔍 Verificando PostgreSQL..."
until docker-compose exec -T postgres pg_isready -U policy_user -d policy_request_db; do
    echo "   Aguardando PostgreSQL..."
    sleep 5
done
echo "✅ PostgreSQL está pronto"

# Verificar se Kafka está pronto
echo "🔍 Verificando Kafka..."
until docker-compose exec -T kafka kafka-topics --bootstrap-server localhost:9092 --list &>/dev/null; do
    echo "   Aguardando Kafka..."
    sleep 5
done
echo "✅ Kafka está pronto"

# Iniciar aplicação principal
echo "🚀 Iniciando aplicação principal..."
docker-compose up -d policy-service-api

echo "⏳ Aguardando inicialização da aplicação..."
sleep 45

# Verificar se a aplicação está saudável
echo "🩺 Verificando saúde da aplicação..."
for i in {1..12}; do  # Tentar por 60 segundos (12 x 5s)
    if curl -f http://localhost:8080/actuator/health &>/dev/null; then
        echo "✅ Aplicação está saudável!"
        break
    else
        echo "   Tentativa $i/12 - Aguardando aplicação..."
        sleep 5
    fi
    
    if [ $i -eq 12 ]; then
        echo "❌ Aplicação não respondeu após 60 segundos. Verificando logs..."
        docker-compose logs --tail=20 policy-service-api
        exit 1
    fi
done

# Iniciar serviços de observabilidade (opcional)
echo "📊 Iniciando serviços de monitoramento..."
docker-compose up -d prometheus grafana kafka-ui

echo
echo "🎉 Todos os serviços foram iniciados com sucesso!"
echo "================================================"
echo
echo "📋 Serviços disponíveis:"
echo "   • API Principal: http://localhost:8080"
echo "   • Swagger UI: http://localhost:8080/swagger-ui.html"
echo "   • Health Check: http://localhost:8080/actuator/health"
echo "   • Grafana: http://localhost:3000 (admin/admin123)"
echo "   • Prometheus: http://localhost:9090"
echo "   • Kafka UI: http://localhost:8090"
echo
echo "📊 Status dos containers:"
docker-compose ps

echo
echo "📝 Para visualizar logs em tempo real:"
echo "   docker-compose logs -f policy-service-api"
echo
echo "🛑 Para parar todos os serviços:"
echo "   docker-compose down"
echo
echo "✨ Configuração concluída! Você pode começar a usar a API."