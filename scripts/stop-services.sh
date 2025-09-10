#!/bin/bash

# Script para parar todos os serviços do Policy Service API
# Autor: Sistema ACME

set -e

echo "🛑 Parando Policy Service API..."
echo "==================================="

# Verificar se estamos no diretório correto
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ Arquivo docker-compose.yml não encontrado. Execute este script no diretório raiz do projeto."
    exit 1
fi

# Mostrar status atual
echo "📊 Status atual dos containers:"
docker-compose ps
echo

# Parar todos os serviços
echo "🛑 Parando todos os containers..."
docker-compose down

echo "✅ Todos os containers foram parados."
echo

# Oferecer opção de limpeza
read -p "🧹 Deseja remover os volumes de dados? (y/N): " -r
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🗑️  Removendo volumes..."
    docker-compose down -v
    echo "✅ Volumes removidos."
else
    echo "📦 Volumes preservados para próxima execução."
fi

echo
echo "🧽 Limpando containers e imagens órfãs..."
docker system prune -f

echo
echo "✨ Limpeza concluída!"
echo
echo "🚀 Para iniciar novamente:"
echo "   ./scripts/start-services.sh"
echo "   ou"
echo "   docker-compose up -d"