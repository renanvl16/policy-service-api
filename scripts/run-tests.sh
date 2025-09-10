#!/bin/bash

# Script para executar todos os testes do Policy Service API
# Autor: Sistema ACME

set -e

echo "🧪 Executando testes do Policy Service API..."
echo "=============================================="

# Verificar se Maven está instalado
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven não está instalado. Por favor, instale o Maven primeiro."
    exit 1
fi

# Verificar se estamos no diretório correto
if [ ! -f "pom.xml" ]; then
    echo "❌ Arquivo pom.xml não encontrado. Execute este script no diretório raiz do projeto."
    exit 1
fi

echo "✅ Verificações preliminares concluídas"
echo

# Limpar compilações anteriores
echo "🧹 Limpando compilações anteriores..."
mvn clean

# Executar testes unitários
echo "🔬 Executando testes unitários..."
mvn test

echo "✅ Testes unitários concluídos!"
echo

# Executar testes de integração
echo "🔗 Executando testes de integração com TestContainers..."
mvn verify -P integration-test

echo "✅ Testes de integração concluídos!"
echo

# Gerar relatório de cobertura
echo "📊 Gerando relatório de cobertura..."
mvn jacoco:report

# Verificar se o relatório foi gerado
if [ -f "target/site/jacoco/index.html" ]; then
    echo "✅ Relatório de cobertura gerado em: target/site/jacoco/index.html"
    
    # Tentar abrir o relatório (funciona no macOS e Linux)
    if command -v open &> /dev/null; then
        echo "🌐 Abrindo relatório de cobertura..."
        open target/site/jacoco/index.html
    elif command -v xdg-open &> /dev/null; then
        echo "🌐 Abrindo relatório de cobertura..."
        xdg-open target/site/jacoco/index.html
    else
        echo "📂 Para ver o relatório, abra: target/site/jacoco/index.html"
    fi
else
    echo "⚠️  Relatório de cobertura não foi gerado"
fi

echo
echo "📈 Resumo da execução dos testes:"
echo "================================="

# Contar testes executados (aproximado)
if [ -d "target/surefire-reports" ]; then
    UNIT_TESTS=$(find target/surefire-reports -name "*.xml" | wc -l)
    echo "   • Testes unitários: $UNIT_TESTS suítes executadas"
fi

if [ -d "target/failsafe-reports" ]; then
    INTEGRATION_TESTS=$(find target/failsafe-reports -name "*.xml" | wc -l)
    echo "   • Testes integração: $INTEGRATION_TESTS suítes executadas"
fi

# Verificar cobertura (se houver ferramenta para extrair)
if [ -f "target/site/jacoco/index.html" ]; then
    echo "   • Relatório de cobertura: Gerado com sucesso"
fi

echo
echo "🎯 Meta de cobertura: 90%"
echo "📊 Para ver cobertura detalhada, abra: target/site/jacoco/index.html"
echo

# Executar análise de qualidade (se disponível)
if command -v sonar-scanner &> /dev/null; then
    read -p "🔍 Executar análise SonarQube? (y/N): " -r
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "🔎 Executando análise SonarQube..."
        sonar-scanner
    fi
fi

echo
echo "✨ Todos os testes foram executados com sucesso!"
echo
echo "📝 Próximos passos:"
echo "   • Revisar relatório de cobertura"
echo "   • Verificar se meta de 90% foi atingida"
echo "   • Analisar testes que falharam (se houver)"
echo
echo "🚀 Para executar a aplicação:"
echo "   ./scripts/start-services.sh"