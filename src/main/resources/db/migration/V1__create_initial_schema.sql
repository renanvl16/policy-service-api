-- Criação do esquema inicial do banco de dados

-- Tabela principal de solicitações de apólice
CREATE TABLE policy_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    product_id VARCHAR(255) NOT NULL,
    category VARCHAR(20) NOT NULL CHECK (category IN ('VIDA', 'AUTO', 'RESIDENCIAL', 'EMPRESARIAL')),
    sales_channel VARCHAR(20) NOT NULL CHECK (sales_channel IN ('MOBILE', 'WHATSAPP', 'WEBSITE', 'PRESENCIAL', 'TELEFONE')),
    payment_method VARCHAR(20) NOT NULL CHECK (payment_method IN ('CREDIT_CARD', 'DEBIT_ACCOUNT', 'BOLETO', 'PIX')),
    status VARCHAR(20) NOT NULL DEFAULT 'RECEIVED' CHECK (status IN ('RECEIVED', 'VALIDATED', 'PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at TIMESTAMP,
    total_monthly_premium_amount DECIMAL(19,2) NOT NULL CHECK (total_monthly_premium_amount > 0),
    insured_amount DECIMAL(19,2) NOT NULL CHECK (insured_amount > 0)
);

-- Índices para otimização de consultas
CREATE INDEX idx_policy_requests_customer_id ON policy_requests(customer_id);
CREATE INDEX idx_policy_requests_status ON policy_requests(status);
CREATE INDEX idx_policy_requests_created_at ON policy_requests(created_at);
CREATE INDEX idx_policy_requests_product_id ON policy_requests(product_id);

-- Tabela para coberturas das apólices
CREATE TABLE policy_coverages (
    policy_request_id UUID NOT NULL,
    coverage_name VARCHAR(255) NOT NULL,
    coverage_amount DECIMAL(19,2) NOT NULL CHECK (coverage_amount > 0),
    PRIMARY KEY (policy_request_id, coverage_name),
    FOREIGN KEY (policy_request_id) REFERENCES policy_requests(id) ON DELETE CASCADE
);

-- Tabela para assistências das apólices
CREATE TABLE policy_assistances (
    policy_request_id UUID NOT NULL,
    assistance_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (policy_request_id, assistance_name),
    FOREIGN KEY (policy_request_id) REFERENCES policy_requests(id) ON DELETE CASCADE
);

-- Tabela para histórico de alterações de status
CREATE TABLE status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_request_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('RECEIVED', 'VALIDATED', 'PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason VARCHAR(500),
    FOREIGN KEY (policy_request_id) REFERENCES policy_requests(id) ON DELETE CASCADE
);

-- Índice para otimizar consultas de histórico
CREATE INDEX idx_status_history_policy_request_id ON status_history(policy_request_id);
CREATE INDEX idx_status_history_timestamp ON status_history(timestamp);

-- Inserção do histórico inicial para todas as solicitações existentes
-- (será executado via trigger para novas inserções)

-- Trigger para inserir histórico inicial automaticamente
CREATE OR REPLACE FUNCTION insert_initial_history()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO status_history (policy_request_id, status, timestamp, reason)
    VALUES (NEW.id, NEW.status, NEW.created_at, 'Status inicial');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_insert_initial_history
    AFTER INSERT ON policy_requests
    FOR EACH ROW
    EXECUTE FUNCTION insert_initial_history();

-- Comentários nas tabelas para documentação
COMMENT ON TABLE policy_requests IS 'Tabela principal para armazenar solicitações de apólices de seguro';
COMMENT ON TABLE policy_coverages IS 'Tabela para armazenar as coberturas de cada solicitação de apólice';
COMMENT ON TABLE policy_assistances IS 'Tabela para armazenar as assistências de cada solicitação de apólice';
COMMENT ON TABLE status_history IS 'Tabela para armazenar o histórico de alterações de status das solicitações';

-- Comentários nas colunas principais
COMMENT ON COLUMN policy_requests.customer_id IS 'ID único do cliente solicitante';
COMMENT ON COLUMN policy_requests.product_id IS 'ID do produto de seguro solicitado';
COMMENT ON COLUMN policy_requests.category IS 'Categoria do seguro (VIDA, AUTO, RESIDENCIAL, EMPRESARIAL)';
COMMENT ON COLUMN policy_requests.status IS 'Status atual da solicitação';
COMMENT ON COLUMN policy_requests.insured_amount IS 'Valor do capital segurado';
COMMENT ON COLUMN policy_requests.total_monthly_premium_amount IS 'Valor mensal do prêmio';