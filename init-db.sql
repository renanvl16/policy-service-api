-- Script de inicialização do banco de dados PostgreSQL
-- Este arquivo será executado automaticamente na primeira inicialização do container

-- Criar extensão para UUID se não existir
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Criar extensão para geração de UUIDs mais eficiente
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Configurações de performance para desenvolvimento
ALTER SYSTEM SET shared_preload_libraries = 'pg_stat_statements';
ALTER SYSTEM SET pg_stat_statements.track = 'all';
ALTER SYSTEM SET pg_stat_statements.max = 10000;

-- Configurações de log para auditoria
ALTER SYSTEM SET log_statement = 'mod';
ALTER SYSTEM SET log_duration = 'on';
ALTER SYSTEM SET log_min_duration_statement = 1000;

-- Recarregar configuração
SELECT pg_reload_conf();