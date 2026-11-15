-- V2__chart_of_accounts_br.sql
-- Brazilian Chart of Accounts (Plano de Contas Padrão Brasileiro)
-- Based on Brazilian accounting standards (CPC, NBC TG 1000)

-- This migration inserts a standard Brazilian chart of accounts structure
-- for a fiscal year. The fiscal_year_id must be replaced with the actual UUID
-- of the fiscal year being set up.

-- NOTE: This is a template. In production, you would:
-- 1. Create a fiscal year first
-- 2. Replace :fiscal_year_id placeholder with the actual UUID
-- 3. Run this migration

-- For now, we'll insert with a placeholder that will be updated by the application
-- The application should replace :fiscal_year_id with the actual fiscal year UUID

-- Header Accounts (Non-detail)
-- Assets
INSERT INTO chart_of_accounts (id, code, name, description, account_type, account_nature, is_active, is_detail, level, parent_id, fiscal_year_id, created_at, version)
VALUES
-- 1 - Ativo
(UUID_TO_BIN(UUID()), '1', 'ATIVO', 'Ativo Total', 'ASSET', 'DEBIT', TRUE, FALSE, 1, NULL, :fiscal_year_id, CURDATE(), 0),
-- 1.1 - Ativo Circulante
(UUID_TO_BIN(UUID()), '1.1', 'ATIVO CIRCULANTE', 'Ativo Circulante', 'ASSET', 'DEBIT', TRUE, FALSE, 2, (SELECT id FROM chart_of_accounts WHERE code = '1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.1.1 - Disponibilidades
(UUID_TO_BIN(UUID()), '1.1.1', 'DISPONIBILIDADES', 'Caixa e Bancos', 'ASSET', 'DEBIT', TRUE, FALSE, 3, (SELECT id FROM chart_of_accounts WHERE code = '1.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.1.1.01 - Caixa
(UUID_TO_BIN(UUID()), '1.1.1.01', 'CAIXA', 'Caixa', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.1.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.1.1.02 - Bancos Conta Corrente
(UUID_TO_BIN(UUID()), '1.1.1.02', 'BANCOS CONTA CORRENTE', 'Bancos Conta Corrente', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.1.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.1.1.03 - Aplicações Financeiras
(UUID_TO_BIN(UUID()), '1.1.1.03', 'APLICAÇÕES FINANCEIRAS', 'Aplicações Financeiras de Liquidez Imediata', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.1.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.1.2 - Contas a Receber
(UUID_TO_BIN(UUID()), '1.1.2', 'CONTAS A RECEBER', 'Contas a Receber de Curto Prazo', 'ASSET', 'DEBIT', TRUE, FALSE, 3, (SELECT id FROM chart_of_accounts WHERE code = '1.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.1.2.01 - Clientes
(UUID_TO_BIN(UUID()), '1.1.2.01', 'CLIENTES', 'Clientes - Contas a Receber', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.1.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.1.2.02 - Duplicatas a Receber
(UUID_TO_BIN(UUID()), '1.1.2.02', 'DUPLICATAS A RECEBER', 'Duplicatas a Receber', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.1.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.1.2.03 - Adiantamentos a Fornecedores
(UUID_TO_BIN(UUID()), '1.1.2.03', 'ADIANTAMENTOS A FORNECEDORES', 'Adiantamentos a Fornecedores', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.1.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.1.2.04 - Devedores Diversos
(UUID_TO_BIN(UUID()), '1.1.2.04', 'DEVEDORES DIVERSOS', 'Devedores Diversos', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.1.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.1.2.05 - Provisão para Devedores Duvidosos
(UUID_TO_BIN(UUID()), '1.1.2.05', 'PROVISÃO PARA DEVEDORES DUVIDOSOS', 'Provisão para Devedores Duvidosos (PECLD)', 'ASSET', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.1.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.1.3 - Estoques
(UUID_TO_BIN(UUID()), '1.1.3', 'ESTOQUES', 'Estoques', 'ASSET', 'DEBIT', TRUE, FALSE, 3, (SELECT id FROM chart_of_accounts WHERE code = '1.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.1.3.01 - Mercadorias para Revenda
(UUID_TO_BIN(UUID()), '1.1.3.01', 'MERCADORIAS PARA REVENDA', 'Mercadorias para Revenda', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.1.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.1.3.02 - Matérias-Primas
(UUID_TO_BIN(UUID()), '1.1.3.02', 'MATÉRIAS-PRIMAS', 'Matérias-Primas', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.1.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.1.3.03 - Produtos em Processo
(UUID_TO_BIN(UUID()), '1.1.3.03', 'PRODUTOS EM PROCESSO', 'Produtos em Processo', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.1.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.1.3.04 - Produtos Acabados
(UUID_TO_BIN(UUID()), '1.1.3.04', 'PRODUTOS ACABADOS', 'Produtos Acabados', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.1.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.1.3.05 - Material de Uso e Consumo
(UUID_TO_BIN(UUID()), '1.1.3.05', 'MATERIAL DE USO E CONSUMO', 'Material de Uso e Consumo', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.1.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.1.3.06 - Provisão para Perdas em Estoques
(UUID_TO_BIN(UUID()), '1.1.3.06', 'PROVISÃO PARA PERDAS EM ESTOQUES', 'Provisão para Perdas em Estoques', 'ASSET', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.1.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.1.4 - Tributos a Recuperar
(UUID_TO_BIN(UUID()), '1.1.4', 'TRIBUTOS A RECUPERAR', 'Tributos a Recuperar de Curto Prazo', 'ASSET', 'DEBIT', TRUE, FALSE, 3, (SELECT id FROM chart_of_accounts WHERE code = '1.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.1.4.01 - ICMS a Recuperar
(UUID_TO_BIN(UUID()), '1.1.4.01', 'ICMS A RECUPERAR', 'ICMS a Recuperar', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.1.4' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.1.4.02 - IPI a Recuperar
(UUID_TO_BIN(UUID()), '1.1.4.02', 'IPI A RECUPERAR', 'IPI a Recuperar', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.1.4' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.1.4.03 - PIS/COFINS a Recuperar
(UUID_TO_BIN(UUID()), '1.1.4.03', 'PIS/COFINS A RECUPERAR', 'PIS/COFINS a Recuperar', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.1.4' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.1.4.04 - IRPJ/CSLL a Recuperar
(UUID_TO_BIN(UUID()), '1.1.4.04', 'IRPJ/CSLL A RECUPERAR', 'IRPJ/CSLL a Recuperar', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.1.4' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.1.5 - Despesas Antecipadas
(UUID_TO_BIN(UUID()), '1.1.5', 'DESPESAS ANTECIPADAS', 'Despesas Antecipadas', 'ASSET', 'DEBIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '1.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.1.6 - Outros Ativos Circulantes
(UUID_TO_BIN(UUID()), '1.1.6', 'OUTROS ATIVOS CIRCULANTES', 'Outros Ativos Circulantes', 'ASSET', 'DEBIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '1.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),

-- 1.2 - Ativo Não Circulante
(UUID_TO_BIN(UUID()), '1.2', 'ATIVO NÃO CIRCULANTE', 'Ativo Não Circulante', 'ASSET', 'DEBIT', TRUE, FALSE, 2, (SELECT id FROM chart_of_accounts WHERE code = '1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.1 - Realizável a Longo Prazo
(UUID_TO_BIN(UUID()), '1.2.1', 'REALIZÁVEL A LONGO PRAZO', 'Realizável a Longo Prazo', 'ASSET', 'DEBIT', TRUE, FALSE, 3, (SELECT id FROM chart_of_accounts WHERE code = '1.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.1.01 - Contas a Receber LP
(UUID_TO_BIN(UUID()), '1.2.1.01', 'CONTAS A RECEBER LP', 'Contas a Receber a Longo Prazo', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.1.02 - Tributos a Recuperar LP
(UUID_TO_BIN(UUID()), '1.2.1.02', 'TRIBUTOS A RECUPERAR LP', 'Tributos a Recuperar a Longo Prazo', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.1.03 - Depósitos Judiciais
(UUID_TO_BIN(UUID()), '1.2.1.03', 'DEPÓSITOS JUDICIAIS', 'Depósitos Judiciais', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.1.04 - Adiantamentos LP
(UUID_TO_BIN(UUID()), '1.2.1.04', 'ADIANTAMENTOS LP', 'Adiantamentos a Longo Prazo', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.1.05 - Provisão para Devedores Duvidosos LP
(UUID_TO_BIN(UUID()), '1.2.1.05', 'PROVISÃO PARA DEVEDORES DUVIDOSOS LP', 'Provisão para Devedores Duvidosos LP', 'ASSET', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.2 - Investimentos
(UUID_TO_BIN(UUID()), '1.2.2', 'INVESTIMENTOS', 'Investimentos', 'ASSET', 'DEBIT', TRUE, FALSE, 3, (SELECT id FROM chart_of_accounts WHERE code = '1.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.2.01 - Participações Societárias
(UUID_TO_BIN(UUID()), '1.2.2.01', 'PARTICIPAÇÕES SOCIETÁRIAS', 'Participações Societárias', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.2.02 - Títulos e Valores Mobiliários
(UUID_TO_BIN(UUID()), '1.2.2.02', 'TÍTULOS E VALORES MOBILIÁRIOS', 'Títulos e Valores Mobiliários', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.3 - Imobilizado
(UUID_TO_BIN(UUID()), '1.2.3', 'IMOBILIZADO', 'Imobilizado', 'ASSET', 'DEBIT', TRUE, FALSE, 3, (SELECT id FROM chart_of_accounts WHERE code = '1.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.3.01 - Terrenos
(UUID_TO_BIN(UUID()), '1.2.3.01', 'TERRENOS', 'Terrenos', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.3.02 - Edifícios e Construções
(UUID_TO_BIN(UUID()), '1.2.3.02', 'EDIFÍCIOS E CONSTRUÇÕES', 'Edifícios e Construções', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.3.03 - Máquinas e Equipamentos
(UUID_TO_BIN(UUID()), '1.2.3.03', 'MÁQUINAS E EQUIPAMENTOS', 'Máquinas e Equipamentos', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.3.04 - Veículos
(UUID_TO_BIN(UUID()), '1.2.3.04', 'VEÍCULOS', 'Veículos', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.3.05 - Móveis e Utensílios
(UUID_TO_BIN(UUID()), '1.2.3.05', 'MÓVEIS E UTENSÍLIOS', 'Móveis e Utensílios', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.3.06 - Equipamentos de Informática
(UUID_TO_BIN(UUID()), '1.2.3.06', 'EQUIPAMENTOS DE INFORMÁTICA', 'Equipamentos de Informática', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.3.07 - Ferramentas
(UUID_TO_BIN(UUID()), '1.2.3.07', 'FERRAMENTAS', 'Ferramentas', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.3.08 - Obras em Andamento
(UUID_TO_BIN(UUID()), '1.2.3.08', 'OBRAS EM ANDAMENTO', 'Obras em Andamento', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.3.09 - Bens de Arrendamento Mercantil
(UUID_TO_BIN(UUID()), '1.2.3.09', 'BENS DE ARRENDAMENTO MERCANTIL', 'Bens de Arrendamento Mercantil', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.3.10 - Depreciação Acumulada - Edifícios
(UUID_TO_BIN(UUID()), '1.2.3.10', 'DEPRECIAÇÃO ACUMULADA - EDIFÍCIOS', 'Depreciação Acumulada - Edifícios', 'ASSET', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.3.11 - Depreciação Acumulada - Máquinas
(UUID_TO_BIN(UUID()), '1.2.3.11', 'DEPRECIAÇÃO ACUMULADA - MÁQUINAS', 'Depreciação Acumulada - Máquinas e Equipamentos', 'ASSET', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.3.12 - Depreciação Acumulada - Veículos
(UUID_TO_BIN(UUID()), '1.2.3.12', 'DEPRECIAÇÃO ACUMULADA - VEÍCULOS', 'Depreciação Acumulada - Veículos', 'ASSET', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.3.13 - Depreciação Acumulada - Móveis
(UUID_TO_BIN(UUID()), '1.2.3.13', 'DEPRECIAÇÃO ACUMULADA - MÓVEIS', 'Depreciação Acumulada - Móveis e Utensílios', 'ASSET', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.3.14 - Depreciação Acumulada - Informática
(UUID_TO_BIN(UUID()), '1.2.3.14', 'DEPRECIAÇÃO ACUMULADA - INFORMÁTICA', 'Depreciação Acumulada - Equipamentos de Informática', 'ASSET', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.3.15 - Depreciação Acumulada - Ferramentas
(UUID_TO_BIN(UUID()), '1.2.3.15', 'DEPRECIAÇÃO ACUMULADA - FERRAMENTAS', 'Depreciação Acumulada - Ferramentas', 'ASSET', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.3.16 - Depreciação Acumulada - Arrendamento
(UUID_TO_BIN(UUID()), '1.2.3.16', 'DEPRECIAÇÃO ACUMULADA - ARRENDAMENTO', 'Depreciação Acumulada - Bens de Arrendamento Mercantil', 'ASSET', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.4 - Intangível
(UUID_TO_BIN(UUID()), '1.2.4', 'INTANGÍVEL', 'Intangível', 'ASSET', 'DEBIT', TRUE, FALSE, 3, (SELECT id FROM chart_of_accounts WHERE code = '1.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.4.01 - Software
(UUID_TO_BIN(UUID()), '1.2.4.01', 'SOFTWARE', 'Software', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.4' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.4.02 - Marcas e Patentes
(UUID_TO_BIN(UUID()), '1.2.4.02', 'MARCAS E PATENTES', 'Marcas e Patentes', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.4' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.4.03 - Goodwill (Ágio)
(UUID_TO_BIN(UUID()), '1.2.4.03', 'GOODWILL (ÁGIO)', 'Goodwill (Ágio)', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.4' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.4.04 - Direitos de Uso
(UUID_TO_BIN(UUID()), '1.2.4.04', 'DIREITOS DE USO', 'Direitos de Uso', 'ASSET', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.4' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.4.05 - Amortização Acumulada - Software
(UUID_TO_BIN(UUID()), '1.2.4.05', 'AMORTIZAÇÃO ACUMULADA - SOFTWARE', 'Amortização Acumulada - Software', 'ASSET', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.4' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.4.06 - Amortização Acumulada - Marcas
(UUID_TO_BIN(UUID()), '1.2.4.06', 'AMORTIZAÇÃO ACUMULADA - MARCAS', 'Amortização Acumulada - Marcas e Patentes', 'ASSET', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.4' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.4.07 - Amortização Acumulada - Goodwill
(UUID_TO_BIN(UUID()), '1.2.4.07', 'AMORTIZAÇÃO ACUMULADA - GOODWILL', 'Amortização Acumulada - Goodwill', 'ASSET', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.4' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 1.2.4.08 - Amortização Acumulada - Direitos
(UUID_TO_BIN(UUID()), '1.2.4.08', 'AMORTIZAÇÃO ACUMULADA - DIREITOS', 'Amortização Acumulada - Direitos de Uso', 'ASSET', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '1.2.4' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),

-- 2 - Passivo
(UUID_TO_BIN(UUID()), '2', 'PASSIVO', 'Passivo Total', 'LIABILITY', 'CREDIT', TRUE, FALSE, 1, NULL, :fiscal_year_id, CURDATE(), 0),
-- 2.1 - Passivo Circulante
(UUID_TO_BIN(UUID()), '2.1', 'PASSIVO CIRCULANTE', 'Passivo Circulante', 'LIABILITY', 'CREDIT', TRUE, FALSE, 2, (SELECT id FROM chart_of_accounts WHERE code = '2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.1 - Fornecedores
(UUID_TO_BIN(UUID()), '2.1.1', 'FORNECEDORES', 'Fornecedores', 'LIABILITY', 'CREDIT', TRUE, FALSE, 3, (SELECT id FROM chart_of_accounts WHERE code = '2.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.1.01 - Fornecedores Nacionais
(UUID_TO_BIN(UUID()), '2.1.1.01', 'FORNECEDORES NACIONAIS', 'Fornecedores Nacionais', 'LIABILITY', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '2.1.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.1.02 - Fornecedores Estrangeiros
(UUID_TO_BIN(UUID()), '2.1.1.02', 'FORNECEDORES ESTRANGEIROS', 'Fornecedores Estrangeiros', 'LIABILITY', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '2.1.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.1.03 - Duplicatas a Pagar
(UUID_TO_BIN(UUID()), '2.1.1.03', 'DUPLICATAS A PAGAR', 'Duplicatas a Pagar', 'LIABILITY', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '2.1.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.1.04 - Adiantamentos de Clientes
(UUID_TO_BIN(UUID()), '2.1.1.04', 'ADIANTAMENTOS DE CLIENTES', 'Adiantamentos de Clientes', 'LIABILITY', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '2.1.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.2 - Empréstimos e Financiamentos CP
(UUID_TO_BIN(UUID()), '2.1.2', 'EMPRÉSTIMOS E FINANCIAMENTOS CP', 'Empréstimos e Financiamentos de Curto Prazo', 'LIABILITY', 'CREDIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '2.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.3 - Obrigações Fiscais
(UUID_TO_BIN(UUID()), '2.1.3', 'OBRIGAÇÕES FISCAIS', 'Obrigações Fiscais', 'LIABILITY', 'CREDIT', TRUE, FALSE, 3, (SELECT id FROM chart_of_accounts WHERE code = '2.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.3.01 - ICMS a Recolher
(UUID_TO_BIN(UUID()), '2.1.3.01', 'ICMS A RECOLHER', 'ICMS a Recolher', 'LIABILITY', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '2.1.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.3.02 - IPI a Recolher
(UUID_TO_BIN(UUID()), '2.1.3.02', 'IPI A RECOLHER', 'IPI a Recolher', 'LIABILITY', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '2.1.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.3.03 - PIS/COFINS a Recolher
(UUID_TO_BIN(UUID()), '2.1.3.03', 'PIS/COFINS A RECOLHER', 'PIS/COFINS a Recolher', 'LIABILITY', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '2.1.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.3.04 - IRPJ/CSLL a Recolher
(UUID_TO_BIN(UUID()), '2.1.3.04', 'IRPJ/CSLL A RECOLHER', 'IRPJ/CSLL a Recolher', 'LIABILITY', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '2.1.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.3.05 - INSS a Recolher
(UUID_TO_BIN(UUID()), '2.1.3.05', 'INSS A RECOLHER', 'INSS a Recolher', 'LIABILITY', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '2.1.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.3.06 - FGTS a Recolher
(UUID_TO_BIN(UUID()), '2.1.3.06', 'FGTS A RECOLHER', 'FGTS a Recolher', 'LIABILITY', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '2.1.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.3.07 - IRRF a Recolher
(UUID_TO_BIN(UUID()), '2.1.3.07', 'IRRF A RECOLHER', 'IRRF a Recolher', 'LIABILITY', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '2.1.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.3.08 - ISS a Recolher
(UUID_TO_BIN(UUID()), '2.1.3.08', 'ISS A RECOLHER', 'ISS a Recolher', 'LIABILITY', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '2.1.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.3.09 - Simples Nacional a Recolher
(UUID_TO_BIN(UUID()), '2.1.3.09', 'SIMPLES NACIONAL A RECOLHER', 'Simples Nacional a Recolher', 'LIABILITY', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '2.1.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.4 - Obrigações Trabalhistas
(UUID_TO_BIN(UUID()), '2.1.4', 'OBRIGAÇÕES TRABALHISTAS', 'Obrigações Trabalhistas', 'LIABILITY', 'CREDIT', TRUE, FALSE, 3, (SELECT id FROM chart_of_accounts WHERE code = '2.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.4.01 - Salários a Pagar
(UUID_TO_BIN(UUID()), '2.1.4.01', 'SALÁRIOS A PAGAR', 'Salários a Pagar', 'LIABILITY', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '2.1.4' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.4.02 - Férias a Pagar
(UUID_TO_BIN(UUID()), '2.1.4.02', 'FÉRIAS A PAGAR', 'Férias a Pagar', 'LIABILITY', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '2.1.4' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.4.03 - 13º Salário a Pagar
(UUID_TO_BIN(UUID()), '2.1.4.03', '13º SALÁRIO A PAGAR', '13º Salário a Pagar', 'LIABILITY', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '2.1.4' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.4.04 - INSS Patronal a Pagar
(UUID_TO_BIN(UUID()), '2.1.4.04', 'INSS PATRONAL A PAGAR', 'INSS Patronal a Pagar', 'LIABILITY', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '2.1.4' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.4.05 - FGTS a Pagar
(UUID_TO_BIN(UUID()), '2.1.4.05', 'FGTS A PAGAR', 'FGTS a Pagar', 'LIABILITY', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '2.1.4' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.4.06 - Provisão para Férias
(UUID_TO_BIN(UUID()), '2.1.4.06', 'PROVISÃO PARA FÉRIAS', 'Provisão para Férias', 'LIABILITY', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '2.1.4' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.4.07 - Provisão para 13º Salário
(UUID_TO_BIN(UUID()), '2.1.4.07', 'PROVISÃO PARA 13º SALÁRIO', 'Provisão para 13º Salário', 'LIABILITY', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '2.1.4' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.5 - Obrigações Diversas
(UUID_TO_BIN(UUID()), '2.1.5', 'OBRIGAÇÕES DIVERSAS', 'Obrigações Diversas', 'LIABILITY', 'CREDIT', TRUE, FALSE, 3, (SELECT id FROM chart_of_accounts WHERE code = '2.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.5.01 - Dividendos a Pagar
(UUID_TO_BIN(UUID()), '2.1.5.01', 'DIVIDENDOS A PAGAR', 'Dividendos a Pagar', 'LIABILITY', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '2.1.5' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.5.02 - Aluguéis a Pagar
(UUID_TO_BIN(UUID()), '2.1.5.02', 'ALUGUÉIS A PAGAR', 'Aluguéis a Pagar', 'LIABILITY', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '2.1.5' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.5.03 - Provisões Diversas
(UUID_TO_BIN(UUID()), '2.1.5.03', 'PROVISÕES DIVERSAS', 'Provisões Diversas', 'LIABILITY', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '2.1.5' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.1.5.04 - Outras Obrigações
(UUID_TO_BIN(UUID()), '2.1.5.04', 'OUTRAS OBRIGAÇÕES', 'Outras Obrigações', 'LIABILITY', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '2.1.5' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),

-- 2.2 - Passivo Não Circulante
(UUID_TO_BIN(UUID()), '2.2', 'PASSIVO NÃO CIRCULANTE', 'Passivo Não Circulante', 'LIABILITY', 'CREDIT', TRUE, FALSE, 2, (SELECT id FROM chart_of_accounts WHERE code = '2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.2.1 - Empréstimos e Financiamentos LP
(UUID_TO_BIN(UUID()), '2.2.1', 'EMPRÉSTIMOS E FINANCIAMENTOS LP', 'Empréstimos e Financiamentos a Longo Prazo', 'LIABILITY', 'CREDIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '2.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.2.2 - Obrigações Fiscais LP
(UUID_TO_BIN(UUID()), '2.2.2', 'OBRIGAÇÕES FISCAIS LP', 'Obrigações Fiscais a Longo Prazo', 'LIABILITY', 'CREDIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '2.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.2.3 - Provisões LP
(UUID_TO_BIN(UUID()), '2.2.3', 'PROVISÕES LP', 'Provisões a Longo Prazo', 'LIABILITY', 'CREDIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '2.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.2.4 - Obrigações Trabalhistas LP
(UUID_TO_BIN(UUID()), '2.2.4', 'OBRIGAÇÕES TRABALHISTAS LP', 'Obrigações Trabalhistas a Longo Prazo', 'LIABILITY', 'CREDIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '2.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.2.5 - Arrendamento Mercantil a Pagar LP
(UUID_TO_BIN(UUID()), '2.2.5', 'ARRENDAMENTO MERCANTIL A PAGAR LP', 'Arrendamento Mercantil a Pagar a Longo Prazo', 'LIABILITY', 'CREDIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '2.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 2.2.6 - Outras Obrigações LP
(UUID_TO_BIN(UUID()), '2.2.6', 'OUTRAS OBRIGAÇÕES LP', 'Outras Obrigações a Longo Prazo', 'LIABILITY', 'CREDIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '2.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),

-- 3 - Patrimônio Líquido
(UUID_TO_BIN(UUID()), '3', 'PATRIMÔNIO LÍQUIDO', 'Patrimônio Líquido', 'EQUITY', 'CREDIT', TRUE, FALSE, 1, NULL, :fiscal_year_id, CURDATE(), 0),
-- 3.1 - Capital Social
(UUID_TO_BIN(UUID()), '3.1', 'CAPITAL SOCIAL', 'Capital Social', 'EQUITY', 'CREDIT', TRUE, FALSE, 2, (SELECT id FROM chart_of_accounts WHERE code = '3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 3.1.01 - Capital Social Realizado
(UUID_TO_BIN(UUID()), '3.1.01', 'CAPITAL SOCIAL REALIZADO', 'Capital Social Realizado', 'EQUITY', 'CREDIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '3.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 3.1.02 - Capital Social a Realizar
(UUID_TO_BIN(UUID()), '3.1.02', 'CAPITAL SOCIAL A REALIZAR', 'Capital Social a Realizar', 'EQUITY', 'DEBIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '3.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 3.2 - Reservas de Capital
(UUID_TO_BIN(UUID()), '3.2', 'RESERVAS DE CAPITAL', 'Reservas de Capital', 'EQUITY', 'CREDIT', TRUE, FALSE, 2, (SELECT id FROM chart_of_accounts WHERE code = '3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 3.2.01 - Ágio na Emissão de Ações
(UUID_TO_BIN(UUID()), '3.2.01', 'ÁGIO NA EMISSÃO DE AÇÕES', 'Ágio na Emissão de Ações', 'EQUITY', 'CREDIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '3.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 3.2.02 - Reservas de Incentivos Fiscais
(UUID_TO_BIN(UUID()), '3.2.02', 'RESERVAS DE INCENTIVOS FISCAIS', 'Reservas de Incentivos Fiscais', 'EQUITY', 'CREDIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '3.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 3.3 - Reservas de Lucros
(UUID_TO_BIN(UUID()), '3.3', 'RESERVAS DE LUCROS', 'Reservas de Lucros', 'EQUITY', 'CREDIT', TRUE, FALSE, 2, (SELECT id FROM chart_of_accounts WHERE code = '3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 3.3.01 - Reserva Legal
(UUID_TO_BIN(UUID()), '3.3.01', 'RESERVA LEGAL', 'Reserva Legal', 'EQUITY', 'CREDIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '3.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 3.3.02 - Reserva Estatutária
(UUID_TO_BIN(UUID()), '3.3.02', 'RESERVA ESTATUTÁRIA', 'Reserva Estatutária', 'EQUITY', 'CREDIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '3.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 3.3.03 - Reserva para Contingências
(UUID_TO_BIN(UUID()), '3.3.03', 'RESERVA PARA CONTINGÊNCIAS', 'Reserva para Contingências', 'EQUITY', 'CREDIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '3.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 3.3.04 - Reserva de Expansão
(UUID_TO_BIN(UUID()), '3.3.04', 'RESERVA DE EXPANSÃO', 'Reserva de Expansão', 'EQUITY', 'CREDIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '3.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 3.3.05 - Lucros a Realizar
(UUID_TO_BIN(UUID()), '3.3.05', 'LUCROS A REALIZAR', 'Lucros a Realizar', 'EQUITY', 'CREDIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '3.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 3.4 - Ajustes de Avaliação Patrimonial
(UUID_TO_BIN(UUID()), '3.4', 'AJUSTES DE AVALIAÇÃO PATRIMONIAL', 'Ajustes de Avaliação Patrimonial', 'EQUITY', 'CREDIT', TRUE, TRUE, 2, (SELECT id FROM chart_of_accounts WHERE code = '3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 3.5 - Prejuízos Acumulados
(UUID_TO_BIN(UUID()), '3.5', 'PREJUÍZOS ACUMULADOS', 'Prejuízos Acumulados', 'EQUITY', 'DEBIT', TRUE, TRUE, 2, (SELECT id FROM chart_of_accounts WHERE code = '3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 3.6 - Ações em Tesouraria
(UUID_TO_BIN(UUID()), '3.6', 'AÇÕES EM TESOURARIA', 'Ações em Tesouraria', 'EQUITY', 'DEBIT', TRUE, TRUE, 2, (SELECT id FROM chart_of_accounts WHERE code = '3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),

-- 4 - Receitas
(UUID_TO_BIN(UUID()), '4', 'RECEITAS', 'Receitas', 'REVENUE', 'CREDIT', TRUE, FALSE, 1, NULL, :fiscal_year_id, CURDATE(), 0),
-- 4.1 - Receita Bruta
(UUID_TO_BIN(UUID()), '4.1', 'RECEITA BRUTA', 'Receita Bruta', 'REVENUE', 'CREDIT', TRUE, FALSE, 2, (SELECT id FROM chart_of_accounts WHERE code = '4' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 4.1.1 - Vendas de Mercadorias
(UUID_TO_BIN(UUID()), '4.1.1', 'VENDAS DE MERCADORIAS', 'Vendas de Mercadorias', 'REVENUE', 'CREDIT', TRUE, FALSE, 3, (SELECT id FROM chart_of_accounts WHERE code = '4.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 4.1.1.01 - Vendas de Mercadorias - Mercado Interno
(UUID_TO_BIN(UUID()), '4.1.1.01', 'VENDAS MERCADORIAS - MERCADO INTERNO', 'Vendas de Mercadorias - Mercado Interno', 'REVENUE', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '4.1.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 4.1.1.02 - Vendas de Mercadorias - Exportação
(UUID_TO_BIN(UUID()), '4.1.1.02', 'VENDAS MERCADORIAS - EXPORTAÇÃO', 'Vendas de Mercadorias - Exportação', 'REVENUE', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '4.1.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 4.1.2 - Vendas de Produtos
(UUID_TO_BIN(UUID()), '4.1.2', 'VENDAS DE PRODUTOS', 'Vendas de Produtos', 'REVENUE', 'CREDIT', TRUE, FALSE, 3, (SELECT id FROM chart_of_accounts WHERE code = '4.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 4.1.2.01 - Vendas de Produtos - Mercado Interno
(UUID_TO_BIN(UUID()), '4.1.2.01', 'VENDAS PRODUTOS - MERCADO INTERNO', 'Vendas de Produtos - Mercado Interno', 'REVENUE', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '4.1.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 4.1.2.02 - Vendas de Produtos - Exportação
(UUID_TO_BIN(UUID()), '4.1.2.02', 'VENDAS PRODUTOS - EXPORTAÇÃO', 'Vendas de Produtos - Exportação', 'REVENUE', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '4.1.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 4.1.3 - Prestação de Serviços
(UUID_TO_BIN(UUID()), '4.1.3', 'PRESTAÇÃO DE SERVIÇOS', 'Prestação de Serviços', 'REVENUE', 'CREDIT', TRUE, FALSE, 3, (SELECT id FROM chart_of_accounts WHERE code = '4.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 4.1.3.01 - Serviços - Mercado Interno
(UUID_TO_BIN(UUID()), '4.1.3.01', 'SERVIÇOS - MERCADO INTERNO', 'Serviços - Mercado Interno', 'REVENUE', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '4.1.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 4.1.3.02 - Serviços - Exportação
(UUID_TO_BIN(UUID()), '4.1.3.02', 'SERVIÇOS - EXPORTAÇÃO', 'Serviços - Exportação', 'REVENUE', 'CREDIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '4.1.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 4.1.4 - Outras Receitas Operacionais
(UUID_TO_BIN(UUID()), '4.1.4', 'OUTRAS RECEITAS OPERACIONAIS', 'Outras Receitas Operacionais', 'REVENUE', 'CREDIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '4.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 4.2 - Deduções da Receita
(UUID_TO_BIN(UUID()), '4.2', 'DEDUÇÕES DA RECEITA', 'Deduções da Receita', 'REVENUE', 'DEBIT', TRUE, FALSE, 2, (SELECT id FROM chart_of_accounts WHERE code = '4' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 4.2.1 - Devoluções de Vendas
(UUID_TO_BIN(UUID()), '4.2.1', 'DEVOLUÇÕES DE VENDAS', 'Devoluções de Vendas', 'REVENUE', 'DEBIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '4.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 4.2.2 - Descontos Concedidos
(UUID_TO_BIN(UUID()), '4.2.2', 'DESCONTOS CONCEDIDOS', 'Descontos Concedidos', 'REVENUE', 'DEBIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '4.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 4.2.3 - Impostos sobre Vendas
(UUID_TO_BIN(UUID()), '4.2.3', 'IMPOSTOS SOBRE VENDAS', 'Impostos sobre Vendas', 'REVENUE', 'DEBIT', TRUE, FALSE, 3, (SELECT id FROM chart_of_accounts WHERE code = '4.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 4.2.3.01 - ICMS sobre Vendas
(UUID_TO_BIN(UUID()), '4.2.3.01', 'ICMS SOBRE VENDAS', 'ICMS sobre Vendas', 'REVENUE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '4.2.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 4.2.3.02 - IPI sobre Vendas
(UUID_TO_BIN(UUID()), '4.2.3.02', 'IPI SOBRE VENDAS', 'IPI sobre Vendas', 'REVENUE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '4.2.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 4.2.3.03 - PIS/COFINS sobre Vendas
(UUID_TO_BIN(UUID()), '4.2.3.03', 'PIS/COFINS SOBRE VENDAS', 'PIS/COFINS sobre Vendas', 'REVENUE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '4.2.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 4.2.3.04 - ISS sobre Serviços
(UUID_TO_BIN(UUID()), '4.2.3.04', 'ISS SOBRE SERVIÇOS', 'ISS sobre Serviços', 'REVENUE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '4.2.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 4.3 - Receitas Financeiras
(UUID_TO_BIN(UUID()), '4.3', 'RECEITAS FINANCEIRAS', 'Receitas Financeiras', 'REVENUE', 'CREDIT', TRUE, FALSE, 2, (SELECT id FROM chart_of_accounts WHERE code = '4' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 4.3.01 - Juros Ativos
(UUID_TO_BIN(UUID()), '4.3.01', 'JUROS ATIVOS', 'Juros Ativos', 'REVENUE', 'CREDIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '4.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 4.3.02 - Variação Cambial Ativa
(UUID_TO_BIN(UUID()), '4.3.02', 'VARIAÇÃO CAMBIAL ATIVA', 'Variação Cambial Ativa', 'REVENUE', 'CREDIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '4.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 4.3.03 - Descontos Obtidos
(UUID_TO_BIN(UUID()), '4.3.03', 'DESCONTOS OBTIDOS', 'Descontos Obtidos', 'REVENUE', 'CREDIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '4.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 4.3.04 - Rendimentos de Aplicações
(UUID_TO_BIN(UUID()), '4.3.04', 'RENDIMENTOS DE APLICAÇÕES', 'Rendimentos de Aplicações Financeiras', 'REVENUE', 'CREDIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '4.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 4.4 - Outras Receitas
(UUID_TO_BIN(UUID()), '4.4', 'OUTRAS RECEITAS', 'Outras Receitas', 'REVENUE', 'CREDIT', TRUE, TRUE, 2, (SELECT id FROM chart_of_accounts WHERE code = '4' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),

-- 5 - Despesas/Custos
(UUID_TO_BIN(UUID()), '5', 'DESPESAS E CUSTOS', 'Despesas e Custos', 'EXPENSE', 'DEBIT', TRUE, FALSE, 1, NULL, :fiscal_year_id, CURDATE(), 0),
-- 5.1 - Custos dos Produtos/Serviços Vendidos (CPV/CSV)
(UUID_TO_BIN(UUID()), '5.1', 'CUSTOS DOS PRODUTOS/SERVIÇOS VENDIDOS', 'Custos dos Produtos/Serviços Vendidos', 'EXPENSE', 'DEBIT', TRUE, FALSE, 2, (SELECT id FROM chart_of_accounts WHERE code = '5' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.1.1 - Custo das Mercadorias Vendidas (CMV)
(UUID_TO_BIN(UUID()), '5.1.1', 'CUSTO DAS MERCADORIAS VENDIDAS (CMV)', 'Custo das Mercadorias Vendidas', 'EXPENSE', 'DEBIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '5.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.1.2 - Custo dos Produtos Vendidos (CPV)
(UUID_TO_BIN(UUID()), '5.1.2', 'CUSTO DOS PRODUTOS VENDIDOS (CPV)', 'Custo dos Produtos Vendidos', 'EXPENSE', 'DEBIT', TRUE, FALSE, 3, (SELECT id FROM chart_of_accounts WHERE code = '5.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.1.2.01 - Matérias-Primas Consumidas
(UUID_TO_BIN(UUID()), '5.1.2.01', 'MATÉRIAS-PRIMAS CONSUMIDAS', 'Matérias-Primas Consumidas', 'EXPENSE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '5.1.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.1.2.02 - Mão de Obra Direta
(UUID_TO_BIN(UUID()), '5.1.2.02', 'MÃO DE OBRA DIRETA', 'Mão de Obra Direta', 'EXPENSE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '5.1.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.1.2.03 - Encargos de Fabricação
(UUID_TO_BIN(UUID()), '5.1.2.03', 'ENCARGOS DE FABRICAÇÃO', 'Encargos de Fabricação', 'EXPENSE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '5.1.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.1.2.04 - Depreciação Industrial
(UUID_TO_BIN(UUID()), '5.1.2.04', 'DEPRECIAÇÃO INDUSTRIAL', 'Depreciação Industrial', 'EXPENSE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '5.1.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.1.3 - Custo dos Serviços Prestados (CSP)
(UUID_TO_BIN(UUID()), '5.1.3', 'CUSTO DOS SERVIÇOS PRESTADOS (CSP)', 'Custo dos Serviços Prestados', 'EXPENSE', 'DEBIT', TRUE, FALSE, 3, (SELECT id FROM chart_of_accounts WHERE code = '5.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.1.3.01 - Mão de Obra de Serviços
(UUID_TO_BIN(UUID()), '5.1.3.01', 'MÃO DE OBRA DE SERVIÇOS', 'Mão de Obra de Serviços', 'EXPENSE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '5.1.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.1.3.02 - Materiais de Serviços
(UUID_TO_BIN(UUID()), '5.1.3.02', 'MATERIAIS DE SERVIÇOS', 'Materiais de Serviços', 'EXPENSE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '5.1.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.1.3.03 - Terceirização de Serviços
(UUID_TO_BIN(UUID()), '5.1.3.03', 'TERCEIRIZAÇÃO DE SERVIÇOS', 'Terceirização de Serviços', 'EXPENSE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '5.1.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.2 - Despesas Operacionais
(UUID_TO_BIN(UUID()), '5.2', 'DESPESAS OPERACIONAIS', 'Despesas Operacionais', 'EXPENSE', 'DEBIT', TRUE, FALSE, 2, (SELECT id FROM chart_of_accounts WHERE code = '5' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.2.1 - Despesas com Vendas
(UUID_TO_BIN(UUID()), '5.2.1', 'DESPESAS COM VENDAS', 'Despesas com Vendas', 'EXPENSE', 'DEBIT', TRUE, FALSE, 3, (SELECT id FROM chart_of_accounts WHERE code = '5.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.2.1.01 - Comissões de Vendas
(UUID_TO_BIN(UUID()), '5.2.1.01', 'COMISSÕES DE VENDAS', 'Comissões de Vendas', 'EXPENSE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '5.2.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.2.1.02 - Propaganda e Publicidade
(UUID_TO_BIN(UUID()), '5.2.1.02', 'PROPAGANDA E PUBLICIDADE', 'Propaganda e Publicidade', 'EXPENSE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '5.2.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.2.1.03 - Frete sobre Vendas
(UUID_TO_BIN(UUID()), '5.2.1.03', 'FRETE SOBRE VENDAS', 'Frete sobre Vendas', 'EXPENSE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '5.2.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.2.1.04 - Despesas com Viagens de Vendas
(UUID_TO_BIN(UUID()), '5.2.1.04', 'DESPESAS COM VIAGENS DE VENDAS', 'Despesas com Viagens de Vendas', 'EXPENSE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '5.2.1' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.2.2 - Despesas Administrativas
(UUID_TO_BIN(UUID()), '5.2.2', 'DESPESAS ADMINISTRATIVAS', 'Despesas Administrativas', 'EXPENSE', 'DEBIT', TRUE, FALSE, 3, (SELECT id FROM chart_of_accounts WHERE code = '5.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.2.2.01 - Pessoal Administrativo
(UUID_TO_BIN(UUID()), '5.2.2.01', 'PESSOAL ADMINISTRATIVO', 'Pessoal Administrativo', 'EXPENSE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '5.2.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.2.2.02 - Aluguel e Condomínio
(UUID_TO_BIN(UUID()), '5.2.2.02', 'ALUGUEL E CONDOMÍNIO', 'Aluguel e Condomínio', 'EXPENSE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '5.2.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.2.2.03 - Energia Elétrica
(UUID_TO_BIN(UUID()), '5.2.2.03', 'ENERGIA ELÉTRICA', 'Energia Elétrica', 'EXPENSE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '5.2.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.2.2.04 - Água e Esgoto
(UUID_TO_BIN(UUID()), '5.2.2.04', 'ÁGUA E ESGOTO', 'Água e Esgoto', 'EXPENSE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '5.2.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.2.2.05 - Telefone e Internet
(UUID_TO_BIN(UUID()), '5.2.2.05', 'TELEFONE E INTERNET', 'Telefone e Internet', 'EXPENSE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '5.2.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.2.2.06 - Material de Expediente
(UUID_TO_BIN(UUID()), '5.2.2.06', 'MATERIAL DE EXPEDIENTE', 'Material de Expediente', 'EXPENSE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '5.2.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.2.2.07 - Manutenção e Conservação
(UUID_TO_BIN(UUID()), '5.2.2.07', 'MANUTENÇÃO E CONSERVAÇÃO', 'Manutenção e Conservação', 'EXPENSE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '5.2.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.2.2.08 - Seguros
(UUID_TO_BIN(UUID()), '5.2.2.08', 'SEGUROS', 'Seguros', 'EXPENSE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '5.2.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.2.2.09 - Honorários Contábeis e Jurídicos
(UUID_TO_BIN(UUID()), '5.2.2.09', 'HONORÁRIOS CONTÁBEIS E JURÍDICOS', 'Honorários Contábeis e Jurídicos', 'EXPENSE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '5.2.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.2.2.10 - Depreciação Administrativa
(UUID_TO_BIN(UUID()), '5.2.2.10', 'DEPRECIAÇÃO ADMINISTRATIVA', 'Depreciação Administrativa', 'EXPENSE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '5.2.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.2.2.11 - Amortização Administrativa
(UUID_TO_BIN(UUID()), '5.2.2.11', 'AMORTIZAÇÃO ADMINISTRATIVA', 'Amortização Administrativa', 'EXPENSE', 'DEBIT', TRUE, TRUE, 4, (SELECT id FROM chart_of_accounts WHERE code = '5.2.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.2.3 - Despesas Gerais
(UUID_TO_BIN(UUID()), '5.2.3', 'DESPESAS GERAIS', 'Despesas Gerais', 'EXPENSE', 'DEBIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '5.2' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.3 - Despesas Financeiras
(UUID_TO_BIN(UUID()), '5.3', 'DESPESAS FINANCEIRAS', 'Despesas Financeiras', 'EXPENSE', 'DEBIT', TRUE, FALSE, 2, (SELECT id FROM chart_of_accounts WHERE code = '5' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.3.01 - Juros Passivos
(UUID_TO_BIN(UUID()), '5.3.01', 'JUROS PASSIVOS', 'Juros Passivos', 'EXPENSE', 'DEBIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '5.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.3.02 - Variação Cambial Passiva
(UUID_TO_BIN(UUID()), '5.3.02', 'VARIAÇÃO CAMBIAL PASSIVA', 'Variação Cambial Passiva', 'EXPENSE', 'DEBIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '5.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.3.03 - Descontos Concedidos Financeiros
(UUID_TO_BIN(UUID()), '5.3.03', 'DESCONTOS CONCEDIDOS FINANCEIROS', 'Descontos Concedidos Financeiros', 'EXPENSE', 'DEBIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '5.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.3.04 - Tarifas Bancárias
(UUID_TO_BIN(UUID()), '5.3.04', 'TARIFAS BANCÁRIAS', 'Tarifas Bancárias', 'EXPENSE', 'DEBIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '5.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.3.05 - IOF
(UUID_TO_BIN(UUID()), '5.3.05', 'IOF', 'IOF', 'EXPENSE', 'DEBIT', TRUE, TRUE, 3, (SELECT id FROM chart_of_accounts WHERE code = '5.3' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),
-- 5.4 - Outras Despesas
(UUID_TO_BIN(UUID()), '5.4', 'OUTRAS DESPESAS', 'Outras Despesas', 'EXPENSE', 'DEBIT', TRUE, TRUE, 2, (SELECT id FROM chart_of_accounts WHERE code = '5' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0),

-- 6 - Resultados
(UUID_TO_BIN(UUID()), '6', 'RESULTADOS', 'Resultados', 'EQUITY', 'CREDIT', TRUE, FALSE, 1, NULL, :fiscal_year_id, CURDATE(), 0),
-- 6.1 - Resultado do Exercício
(UUID_TO_BIN(UUID()), '6.1', 'RESULTADO DO EXERCÍCIO', 'Resultado do Exercício', 'EQUITY', 'CREDIT', TRUE, TRUE, 2, (SELECT id FROM chart_of_accounts WHERE code = '6' AND fiscal_year_id = :fiscal_year_id), :fiscal_year_id, CURDATE(), 0);