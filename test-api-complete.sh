#!/bin/bash

# Script completo para testar todos os fluxos da API Policy Service
# Teste de integração end-to-end

set -e

API_BASE_URL="http://localhost:8080/api/v1"
CONTENT_TYPE="application/json"

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para logging
log() {
    echo -e "${BLUE}[$(date '+%H:%M:%S')]${NC} $1"
}

success() {
    echo -e "${GREEN}✓${NC} $1"
}

error() {
    echo -e "${RED}✗${NC} $1"
}

warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# Função para fazer requisições HTTP
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    local expected_status=$4
    
    log "Fazendo requisição: $method $endpoint"
    
    if [[ -n "$data" ]]; then
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X "$method" \
            -H "Content-Type: $CONTENT_TYPE" \
            -d "$data" \
            "$API_BASE_URL$endpoint")
    else
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X "$method" \
            "$API_BASE_URL$endpoint")
    fi
    
    body=$(echo "$response" | sed -E 's/HTTPSTATUS:[0-9]{3}$//')
    status=$(echo "$response" | tr -d '\n' | sed -E 's/.*HTTPSTATUS:([0-9]{3})$/\1/')
    
    echo "Response Body: $body"
    echo "HTTP Status: $status"
    
    if [[ "$status" == "$expected_status" ]]; then
        success "Status esperado: $expected_status"
    else
        error "Status esperado: $expected_status, obtido: $status"
        return 1
    fi
    
    echo "$body"
}

# Função para extrair ID da resposta JSON
extract_id() {
    echo "$1" | grep -o '"id":"[^"]*"' | cut -d'"' -f4
}

# Aguardar API estar disponível
wait_for_api() {
    log "Aguardando API estar disponível..."
    for i in {1..30}; do
        if curl -s "$API_BASE_URL/../../actuator/health" >/dev/null 2>&1; then
            success "API está disponível!"
            return 0
        fi
        echo -n "."
        sleep 2
    done
    error "API não ficou disponível em 60 segundos"
    exit 1
}

# Variáveis globais para armazenar IDs
POLICY_ID_1=""
POLICY_ID_2=""
POLICY_ID_3=""
CUSTOMER_ID="123e4567-e89b-12d3-a456-426614174000"

echo "=============================================="
echo "🚀 INICIANDO TESTES COMPLETOS DA API"
echo "=============================================="

wait_for_api

echo
echo "=============================================="
echo "📝 1. TESTES DE CRIAÇÃO DE SOLICITAÇÕES"
echo "=============================================="

# 1.1 Criar solicitação válida - Seguro de Vida
log "1.1 - Criando solicitação de Seguro de Vida"
create_data_vida='{
    "customerId": "'$CUSTOMER_ID'",
    "productId": "VIDA-001",
    "category": "VIDA",
    "salesChannel": "WEBSITE",
    "paymentMethod": "CREDIT_CARD",
    "totalMonthlyPremiumAmount": 150.00,
    "insuredAmount": 50000.00,
    "coverages": {
        "MORTE_NATURAL": 50000.00,
        "MORTE_ACIDENTAL": 100000.00,
        "INVALIDEZ_PERMANENTE": 25000.00
    },
    "assistances": ["ASSISTENCIA_FUNERAL", "ASSISTENCIA_JURIDICA"]
}'

response=$(make_request "POST" "/policy-requests" "$create_data_vida" "201")
POLICY_ID_1=$(extract_id "$response")
success "Solicitação de Vida criada: $POLICY_ID_1"

# 1.2 Criar solicitação válida - Seguro de Carro
log "1.2 - Criando solicitação de Seguro de Carro"
create_data_auto='{
    "customerId": "'$CUSTOMER_ID'",
    "productId": "AUTO-002",
    "category": "AUTO",
    "salesChannel": "TELEFONE",
    "paymentMethod": "DEBIT_ACCOUNT",
    "totalMonthlyPremiumAmount": 320.50,
    "insuredAmount": 80000.00,
    "coverages": {
        "CASCO": 80000.00,
        "TERCEIROS": 100000.00,
        "ROUBO_FURTO": 80000.00
    },
    "assistances": ["GUINCHO", "CARRO_RESERVA", "CHAVEIRO"]
}'

response=$(make_request "POST" "/policy-requests" "$create_data_auto" "201")
POLICY_ID_2=$(extract_id "$response")
success "Solicitação de Auto criada: $POLICY_ID_2"

# 1.3 Criar solicitação válida - Seguro Empresarial
log "1.3 - Criando solicitação de Seguro Empresarial"
create_data_empresarial='{
    "customerId": "'$CUSTOMER_ID'",
    "productId": "EMP-003",
    "category": "EMPRESARIAL",
    "salesChannel": "AGENTE",
    "paymentMethod": "BANK_SLIP",
    "totalMonthlyPremiumAmount": 850.75,
    "insuredAmount": 200000.00,
    "coverages": {
        "INCENDIO": 200000.00,
        "RESPONSABILIDADE_CIVIL": 500000.00,
        "EQUIPAMENTOS": 50000.00
    },
    "assistances": ["CONSULTORIA_JURIDICA", "ASSISTENCIA_TECNICA"]
}'

response=$(make_request "POST" "/policy-requests" "$create_data_empresarial" "201")
POLICY_ID_3=$(extract_id "$response")
success "Solicitação Empresarial criada: $POLICY_ID_3"

echo
echo "=============================================="
echo "🔍 2. TESTES DE CONSULTA"
echo "=============================================="

# 2.1 Consultar por ID específico
log "2.1 - Consultando solicitação por ID: $POLICY_ID_1"
make_request "GET" "/policy-requests/$POLICY_ID_1" "" "200"
success "Consulta por ID realizada com sucesso"

# 2.2 Consultar por Customer ID
log "2.2 - Consultando solicitações do cliente: $CUSTOMER_ID"
make_request "GET" "/policy-requests/customer/$CUSTOMER_ID" "" "200"
success "Consulta por Customer ID realizada com sucesso"

# 2.3 Tentar consultar ID inexistente
log "2.3 - Testando consulta de ID inexistente"
fake_id="00000000-0000-0000-0000-000000000000"
make_request "GET" "/policy-requests/$fake_id" "" "404" || warning "Erro esperado para ID inexistente"

echo
echo "=============================================="
echo "❌ 3. TESTES DE CANCELAMENTO"
echo "=============================================="

# 3.1 Cancelar solicitação com motivo
log "3.1 - Cancelando solicitação com motivo"
cancel_data='{
    "reason": "Cliente desistiu da contratação"
}'
make_request "POST" "/policy-requests/$POLICY_ID_2/cancel" "$cancel_data" "200"
success "Cancelamento com motivo realizado com sucesso"

# 3.2 Cancelar solicitação sem motivo
log "3.2 - Cancelando solicitação sem motivo"
make_request "POST" "/policy-requests/$POLICY_ID_3/cancel" "" "200"
success "Cancelamento sem motivo realizado com sucesso"

# 3.3 Tentar cancelar solicitação já cancelada
log "3.3 - Tentando cancelar solicitação já cancelada (deve falhar)"
make_request "POST" "/policy-requests/$POLICY_ID_2/cancel" "$cancel_data" "400" || warning "Erro esperado ao cancelar solicitação já cancelada"

echo
echo "=============================================="
echo "🚫 4. TESTES DE VALIDAÇÃO E CASOS DE ERRO"
echo "=============================================="

# 4.1 Criar solicitação com dados inválidos - customerId nulo
log "4.1 - Testando criação com customerId nulo"
invalid_data_1='{
    "customerId": null,
    "productId": "VIDA-001",
    "category": "VIDA",
    "salesChannel": "WEBSITE",
    "paymentMethod": "CREDIT_CARD",
    "totalMonthlyPremiumAmount": 150.00,
    "insuredAmount": 50000.00,
    "coverages": {"MORTE_NATURAL": 50000.00},
    "assistances": ["ASSISTENCIA_FUNERAL"]
}'
make_request "POST" "/policy-requests" "$invalid_data_1" "400" || warning "Erro esperado para customerId nulo"

# 4.2 Criar solicitação com valor negativo
log "4.2 - Testando criação com valor negativo"
invalid_data_2='{
    "customerId": "'$CUSTOMER_ID'",
    "productId": "VIDA-001",
    "category": "VIDA",
    "salesChannel": "WEBSITE",
    "paymentMethod": "CREDIT_CARD",
    "totalMonthlyPremiumAmount": -150.00,
    "insuredAmount": 50000.00,
    "coverages": {"MORTE_NATURAL": 50000.00},
    "assistances": ["ASSISTENCIA_FUNERAL"]
}'
make_request "POST" "/policy-requests" "$invalid_data_2" "400" || warning "Erro esperado para valor negativo"

# 4.3 Criar solicitação sem coberturas
log "4.3 - Testando criação sem coberturas"
invalid_data_3='{
    "customerId": "'$CUSTOMER_ID'",
    "productId": "VIDA-001",
    "category": "VIDA",
    "salesChannel": "WEBSITE",
    "paymentMethod": "CREDIT_CARD",
    "totalMonthlyPremiumAmount": 150.00,
    "insuredAmount": 50000.00,
    "coverages": {},
    "assistances": ["ASSISTENCIA_FUNERAL"]
}'
make_request "POST" "/policy-requests" "$invalid_data_3" "400" || warning "Erro esperado para coberturas vazias"

echo
echo "=============================================="
echo "🔄 5. TESTE DE WORKFLOW COMPLETO"
echo "=============================================="

# 5.1 Criar nova solicitação para workflow
log "5.1 - Criando nova solicitação para workflow completo"
workflow_data='{
    "customerId": "'$CUSTOMER_ID'",
    "productId": "VIDA-WORKFLOW",
    "category": "VIDA",
    "salesChannel": "WEBSITE",
    "paymentMethod": "PIX",
    "totalMonthlyPremiumAmount": 200.00,
    "insuredAmount": 75000.00,
    "coverages": {
        "MORTE_NATURAL": 75000.00,
        "INVALIDEZ_PERMANENTE": 30000.00
    },
    "assistances": ["ASSISTENCIA_FUNERAL"]
}'

response=$(make_request "POST" "/policy-requests" "$workflow_data" "201")
WORKFLOW_ID=$(extract_id "$response")
success "Solicitação para workflow criada: $WORKFLOW_ID"

# 5.2 Verificar status inicial
log "5.2 - Verificando status inicial (RECEIVED)"
response=$(make_request "GET" "/policy-requests/$WORKFLOW_ID" "" "200")
echo "$response" | grep -q "RECEIVED" && success "Status inicial RECEIVED confirmado"

# 5.3 Aguardar um pouco para processamento assíncrono
log "5.3 - Aguardando processamento assíncrono..."
sleep 5

# 5.4 Verificar se o status mudou
log "5.4 - Verificando mudança de status após processamento"
response=$(make_request "GET" "/policy-requests/$WORKFLOW_ID" "" "200")
echo "Status atual: $response"

echo
echo "=============================================="
echo "📊 6. VERIFICAÇÃO DE HEALTH CHECK"
echo "=============================================="

# 6.1 Health check da aplicação
log "6.1 - Verificando health check"
health_response=$(curl -s "$API_BASE_URL/../../actuator/health")
echo "Health Check: $health_response"
echo "$health_response" | grep -q "UP" && success "Aplicação está UP"

# 6.2 Verificar métricas
log "6.2 - Verificando métricas disponíveis"
metrics_response=$(curl -s "$API_BASE_URL/../../actuator/metrics")
echo "Métricas disponíveis: $(echo "$metrics_response" | grep -o '"[^"]*"' | head -10 | tr '\n' ' ')"

echo
echo "=============================================="
echo "📝 7. RESUMO FINAL DOS TESTES"
echo "=============================================="

echo
log "Solicitações criadas durante os testes:"
echo "  - Seguro de Vida: $POLICY_ID_1"
echo "  - Seguro de Auto: $POLICY_ID_2 (cancelada)"
echo "  - Seguro Empresarial: $POLICY_ID_3 (cancelada)"
echo "  - Workflow Test: $WORKFLOW_ID"
echo

success "TODOS OS TESTES FORAM EXECUTADOS!"
echo "Verifique os logs acima para identificar eventuais falhas."
echo

# 7.1 Consulta final de todas as solicitações do cliente
log "7.1 - Consulta final de todas as solicitações do cliente"
final_response=$(make_request "GET" "/policy-requests/customer/$CUSTOMER_ID" "" "200")
echo
success "TESTE COMPLETO FINALIZADO COM SUCESSO! 🎉"
echo "=============================================="