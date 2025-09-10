# language: pt
Funcionalidade: Ciclo de vida das solicitações de apólice
  Como um sistema de seguros
  Eu quero gerenciar o ciclo de vida completo das solicitações de apólice
  Para garantir o processamento adequado e controle de estados

  Contexto:
    Dado que o serviço de solicitações de apólice está em execução
    E que a API de fraudes mock está disponível
    E que o banco de dados está limpo

  Esquema do Cenário: Criar solicitação de apólice com sucesso
    Dado que eu tenho uma solicitação de apólice válida para categoria "<categoria>"
    E com valor segurado de "<valorSegurado>" reais
    E para cliente com classificação de risco "<classificacao>"
    Quando eu envio a solicitação para criação
    Então a solicitação deve ser criada com sucesso
    E deve retornar o ID da solicitação e data/hora de criação
    E o status inicial deve ser "RECEIVED"

    Exemplos:
      | categoria    | valorSegurado | classificacao    |
      | AUTO         | 250000.00     | REGULAR          |
      | VIDA         | 400000.00     | REGULAR          |
      | RESIDENCIAL  | 300000.00     | PREFERENTIAL     |
      | EMPRESARIAL  | 200000.00     | REGULAR          |

  Cenário: Processar solicitação com classificação regular válida
    Dado que eu tenho uma solicitação de apólice para categoria "AUTO"
    E com valor segurado de "250000.00" reais
    E para cliente com classificação de risco "REGULAR"
    Quando eu envio a solicitação para criação
    E aguardo o processamento da análise de fraudes
    Então a solicitação deve ter status "VALIDATED"
    E em seguida deve ter status "PENDING"
    E deve ter histórico das mudanças de status

  Cenário: Rejeitar solicitação por valor acima do limite
    Dado que eu tenho uma solicitação de apólice para categoria "AUTO" 
    E com valor segurado de "400000.00" reais
    E para cliente com classificação de risco "REGULAR"
    Quando eu envio a solicitação para criação
    E aguardo o processamento da análise de fraudes
    Então a solicitação deve ter status "REJECTED"
    E deve ter uma razão de rejeição informando excesso de limite

  Cenário: Cancelar solicitação pendente
    Dado que eu tenho uma solicitação de apólice criada e pendente
    Quando eu solicito o cancelamento da solicitação com motivo "Desistência do cliente"
    Então a solicitação deve ter status "CANCELLED"
    E deve ter o motivo do cancelamento no histórico

  Cenário: Não permitir cancelamento de solicitação aprovada
    Dado que eu tenho uma solicitação de apólice aprovada
    Quando eu tento cancelar a solicitação
    Então deve retornar erro indicando que não é possível cancelar

  Cenário: Consultar solicitação por ID
    Dado que eu tenho uma solicitação de apólice criada
    Quando eu consulto a solicitação pelo ID
    Então deve retornar os detalhes completos da solicitação
    E deve incluir o histórico de mudanças de status

  Cenário: Consultar solicitações por ID do cliente
    Dado que eu tenho 3 solicitações de apólice para o mesmo cliente
    Quando eu consulto as solicitações pelo ID do cliente
    Então deve retornar todas as 3 solicitações
    E cada solicitação deve ter seus detalhes completos

  Esquema do Cenário: Validar limites por classificação de risco
    Dado que eu tenho uma solicitação de apólice para categoria "<categoria>"
    E com valor segurado de "<valorSegurado>" reais
    E para cliente com classificação de risco "<classificacao>"
    Quando eu envio a solicitação para criação
    E aguardo o processamento da análise de fraudes
    Então a solicitação deve ter status "<statusEsperado>"

    Exemplos:
      | categoria    | valorSegurado | classificacao    | statusEsperado |
      | AUTO         | 350000.00     | REGULAR          | PENDING        |
      | AUTO         | 350000.01     | REGULAR          | REJECTED       |
      | AUTO         | 250000.00     | HIGH_RISK        | PENDING        |
      | AUTO         | 250000.01     | HIGH_RISK        | REJECTED       |
      | VIDA         | 800000.00     | PREFERENTIAL     | PENDING        |
      | VIDA         | 800000.01     | PREFERENTIAL     | REJECTED       |
      | AUTO         | 75000.00      | NO_INFORMATION   | PENDING        |
      | AUTO         | 75000.01      | NO_INFORMATION   | REJECTED       |

  Cenário: Validar campos obrigatórios na criação
    Quando eu envio uma solicitação sem o ID do cliente
    Então deve retornar erro de validação indicando campo obrigatório
    E a solicitação não deve ser criada