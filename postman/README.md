# Coleção Postman - Policy Service API

Esta pasta contém as coleções e ambientes do Postman para testar o Policy Service API.

## Arquivos Incluídos

- `Policy-Request-Service.postman_collection.json` - Coleção principal com todos os testes
- `Policy-Request-Service.postman_environment.json` - Ambiente local com variáveis pré-configuradas

## Como Importar no Postman

### 1. Importar a Coleção
1. Abra o Postman
2. Clique em **Import** no canto superior esquerdo
3. Selecione o arquivo `Policy-Request-Service.postman_collection.json`
4. Confirme a importação

### 2. Importar o Ambiente
1. No Postman, vá em **Environments** na barra lateral
2. Clique em **Import**
3. Selecione o arquivo `Policy-Request-Service.postman_environment.json`
4. Selecione o ambiente "Policy Service API - Local" no dropdown

## Estrutura da Coleção

### 📋 **Health Checks**
- Verificação de saúde geral da aplicação
- Status do banco de dados
- Métricas disponíveis

### 📖 **Documentação API**
- Especificação OpenAPI da API

### 🏗️ **Gestão de Solicitações**
- Criar solicitação AUTO (Cliente Regular)
- Criar solicitação VIDA (Cliente Preferencial) 
- Criar solicitação RESIDENCIAL (Cliente Regular)
- Criar solicitação EMPRESARIAL

### ⚠️ **Cenários de Validação**
- Teste com valor acima do limite
- Validação de campos obrigatórios
- Valores negativos e inválidos

### 🔍 **Consultas**
- Consultar solicitação por ID
- Consultar solicitações por cliente
- Teste de solicitação inexistente

### ❌ **Cancelamentos**
- Cancelar com motivo específico
- Cancelar sem motivo
- Teste de cancelamento inválido

### 🔄 **Casos de Teste Específicos**
- Fluxo completo AUTO Regular
- Aguardar processamento
- Verificar mudanças de status

### ⚡ **Performance e Stress Tests**
- Criação simultânea de múltiplas solicitações
- Testes de tempo de resposta

## Variáveis de Ambiente

| Variável | Valor Padrão | Descrição |
|----------|--------------|-----------|
| `base_url` | http://localhost:8080 | URL base da API |
| `customer_id_regular` | 550e8400-e29b-41d4-a716-446655440001 | Cliente regular |
| `customer_id_preferential` | 550e8400-e29b-41d4-a716-446655440002 | Cliente preferencial |
| `customer_id_high_risk` | 550e8400-e29b-41d4-a716-446655440003 | Cliente alto risco |
| `customer_id_no_info` | 550e8400-e29b-41d4-a716-446655440004 | Cliente sem informação |
| `policy_id` | (dinâmico) | ID da solicitação criada |

## Como Executar os Testes

### 1. Testes Individuais
- Selecione um request na coleção
- Clique em **Send**
- Verifique os resultados na aba **Test Results**

### 2. Executar Toda a Coleção
1. Clique com botão direito na coleção
2. Selecione **Run collection**
3. Configure as opções desejadas:
   - **Iterations**: 1 (para execução única)
   - **Delay**: 500ms (para aguardar processamento)
   - **Data**: Nenhum arquivo necessário
4. Clique em **Run Policy Service API**

### 3. Testes de Performance
1. Selecione o request "Criar 10 Solicitações Simultâneas"
2. Vá em **Runner** (Collection Runner)
3. Configure:
   - **Iterations**: 10
   - **Delay**: 100ms
   - **Keep variable values**: Ativado
4. Execute o teste

## Cenários de Teste Cobertos

### ✅ **Casos de Sucesso**
- [x] Criar solicitações para diferentes categorias de seguro
- [x] Consultar solicitações existentes
- [x] Cancelar solicitações válidas
- [x] Processar análise de fraudes automática

### ⚠️ **Casos de Validação**
- [x] Valores acima dos limites por tipo de cliente
- [x] Campos obrigatórios ausentes
- [x] Valores negativos ou inválidos
- [x] Solicitações inexistentes

### 🔄 **Fluxos Completos**
- [x] Ciclo de vida completo de uma solicitação
- [x] Transições de estado automáticas
- [x] Histórico de alterações

### 📊 **Testes de Performance**
- [x] Múltiplas requisições simultâneas
- [x] Tempo de resposta da API
- [x] Processamento assíncrono

## Scripts de Teste Automáticos

A coleção inclui scripts JavaScript que:

- **Validam automaticamente** status codes esperados
- **Extraem e armazenam** IDs de solicitações criadas
- **Verificam estrutura** das respostas JSON
- **Testam regras de negócio** automaticamente
- **Medem tempo de resposta** das requisições

## Dicas de Uso

### 🚀 **Execução Rápida**
1. Inicie a aplicação: `docker-compose up -d`
2. Aguarde 30-60 segundos para inicialização completa
3. Execute o health check primeiro
4. Execute os testes na ordem sugerida

### 🔧 **Troubleshooting**
- Se testes falharem, verifique se a aplicação está rodando
- Aguarde processamento entre criação e consulta de solicitações
- Verifique se o ambiente correto está selecionado
- Consulte os logs da aplicação em caso de erro 500

### 📋 **Boas Práticas**
- Execute health check antes dos demais testes
- Use delay entre requests para aguardar processamento assíncrono
- Monitore métricas durante testes de performance
- Verifique logs da aplicação para debug detalhado

## Exemplos de Uso

### Teste Manual Básico
```bash
# 1. Criar solicitação
POST /api/v1/policy-requests

# 2. Consultar por ID (usar ID retornado)
GET /api/v1/policy-requests/{id}

# 3. Cancelar se necessário
POST /api/v1/policy-requests/{id}/cancel
```

### Teste de Validação
```bash
# Testar limite excedido para cliente regular AUTO
# Valor: R$ 400.000 > Limite R$ 350.000
# Resultado esperado: Status REJECTED após processamento
```

Para mais informações, consulte a [documentação principal](../README.md) do projeto.