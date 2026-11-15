-- V2__seed_pipelines.sql
-- Seed default sales pipeline with stages

-- This migration assumes a default company exists with ID '00000000-0000-0000-0000-000000000001'
-- In production, this should be run per company during onboarding

DO $$
DECLARE
    v_company_id UUID := '00000000-0000-0000-0000-000000000001'::UUID;
    v_pipeline_id UUID;
    v_stage_prospecting UUID;
    v_stage_qualification UUID;
    v_stage_proposal UUID;
    v_stage_negotiation UUID;
    v_stage_closed_won UUID;
    v_stage_closed_lost UUID;
BEGIN
    -- Insert default pipeline
    INSERT INTO sales_pipelines (id, company_id, code, name, description, is_active, is_default, created_at, updated_at)
    VALUES (
        uuid_generate_v4(), v_company_id, 'DEFAULT', 'Pipeline Padrão de Vendas',
        'Pipeline padrão para gestão de oportunidades de venda', true, true, NOW(), NOW()
    ) RETURNING id INTO v_pipeline_id;

    -- Insert stages
    INSERT INTO pipeline_stages (id, pipeline_id, code, name, description, order_index, type, probability, color, is_active, max_days_in_stage, allow_skip, created_at, updated_at)
    VALUES
    (uuid_generate_v4(), v_pipeline_id, 'PROSPECTING', 'Prospecção', 'Identificação e qualificação inicial de leads', 1, 'PROSPECTING', 10, '#3b82f6', true, 7, false, NOW(), NOW())
    RETURNING id INTO v_stage_prospecting;

    INSERT INTO pipeline_stages (id, pipeline_id, code, name, description, order_index, type, probability, color, is_active, max_days_in_stage, allow_skip, created_at, updated_at)
    VALUES
    (uuid_generate_v4(), v_pipeline_id, 'QUALIFICATION', 'Qualificação', 'Aprofundamento do entendimento da necessidade do cliente', 2, 'QUALIFICATION', 25, '#06b6d4', true, 14, false, NOW(), NOW())
    RETURNING id INTO v_stage_qualification;

    INSERT INTO pipeline_stages (id, pipeline_id, code, name, description, order_index, type, probability, color, is_active, max_days_in_stage, allow_skip, created_at, updated_at)
    VALUES
    (uuid_generate_v4(), v_pipeline_id, 'PROPOSAL', 'Proposta', 'Envio de proposta comercial formal', 3, 'PROPOSAL', 50, '#8b5cf6', true, 14, false, NOW(), NOW())
    RETURNING id INTO v_stage_proposal;

    INSERT INTO pipeline_stages (id, pipeline_id, code, name, description, order_index, type, probability, color, is_active, max_days_in_stage, allow_skip, created_at, updated_at)
    VALUES
    (uuid_generate_v4(), v_pipeline_id, 'NEGOTIATION', 'Negociação', 'Negociação de termos, preços e condições', 4, 'NEGOTIATION', 75, '#f59e0b', true, 21, false, NOW(), NOW())
    RETURNING id INTO v_stage_negotiation;

    INSERT INTO pipeline_stages (id, pipeline_id, code, name, description, order_index, type, probability, color, is_active, max_days_in_stage, allow_skip, created_at, updated_at)
    VALUES
    (uuid_generate_v4(), v_pipeline_id, 'CLOSED_WON', 'Fechado - Ganho', 'Oportunidade ganha - Contrato assinado', 5, 'CLOSED_WON', 100, '#22c55e', true, NULL, false, NOW(), NOW())
    RETURNING id INTO v_stage_closed_won;

    INSERT INTO pipeline_stages (id, pipeline_id, code, name, description, order_index, type, probability, color, is_active, max_days_in_stage, allow_skip, created_at, updated_at)
    VALUES
    (uuid_generate_v4(), v_pipeline_id, 'CLOSED_LOST', 'Fechado - Perdido', 'Oportunidade perdida - Cliente não fechou', 6, 'CLOSED_LOST', 0, '#ef4444', true, NULL, false, NOW(), NOW())
    RETURNING id INTO v_stage_closed_lost;

    RAISE NOTICE 'Default pipeline created with ID: %', v_pipeline_id;
    RAISE NOTICE 'Stages: Prospection=%, Qualification=%, Proposal=%, Negotiation=%, Closed Won=%, Closed Lost=%',
        v_stage_prospecting, v_stage_qualification, v_stage_proposal, v_stage_negotiation, v_stage_closed_won, v_stage_closed_lost;
END $$;

-- Create a second example pipeline for enterprise sales
DO $$
DECLARE
    v_company_id UUID := '00000000-0000-0000-0000-000000000001'::UUID;
    v_pipeline_id UUID;
BEGIN
    INSERT INTO sales_pipelines (id, company_id, code, name, description, is_active, is_default, created_at, updated_at)
    VALUES (
        uuid_generate_v4(), v_company_id, 'ENTERPRISE', 'Pipeline Enterprise',
        'Pipeline para vendas corporativas de alto valor com ciclo longo', true, false, NOW(), NOW()
    ) RETURNING id INTO v_pipeline_id;

    INSERT INTO pipeline_stages (id, pipeline_id, code, name, description, order_index, type, probability, color, is_active, max_days_in_stage, allow_skip, created_at, updated_at)
    VALUES
    (uuid_generate_v4(), v_pipeline_id, 'LEAD', 'Lead Qualificado', 'Lead qualificado pelo marketing (MQL)', 1, 'PROSPECTING', 5, '#3b82f6', true, 30, true, NOW(), NOW()),
    (uuid_generate_v4(), v_pipeline_id, 'DISCOVERY', 'Descoberta', 'Reunião de descoberta e entendimento de necessidades', 2, 'QUALIFICATION', 15, '#06b6d4', true, 30, false, NOW(), NOW()),
    (uuid_generate_v4(), v_pipeline_id, 'DEMO', 'Demonstração', 'Demonstração técnica da solução', 3, 'QUALIFICATION', 30, '#0891b2', true, 14, false, NOW(), NOW()),
    (uuid_generate_v4(), v_pipeline_id, 'TECHNICAL', 'Validação Técnica', 'Proof of Concept / Validação técnica', 4, 'PROPOSAL', 45, '#8b5cf6', true, 30, false, NOW(), NOW()),
    (uuid_generate_v4(), v_pipeline_id, 'COMMERCIAL', 'Proposta Comercial', 'Envio de proposta comercial com precificação', 5, 'PROPOSAL', 60, '#a855f7', true, 21, false, NOW(), NOW()),
    (uuid_generate_v4(), v_pipeline_id, 'NEGOTIATION', 'Negociação Contratual', 'Negociação de contrato, SLA e termos jurídicos', 6, 'NEGOTIATION', 75, '#f59e0b', true, 30, false, NOW(), NOW()),
    (uuid_generate_v4(), v_pipeline_id, 'APPROVAL', 'Aprovação Interna', 'Aprovação do cliente (comitê, diretoria)', 7, 'NEGOTIATION', 85, '#eab308', true, 30, true, NOW(), NOW()),
    (uuid_generate_v4(), v_pipeline_id, 'CLOSED_WON', 'Contrato Assinado', 'Contrato assinado - Venda realizada', 8, 'CLOSED_WON', 100, '#22c55e', true, NULL, false, NOW(), NOW()),
    (uuid_generate_v4(), v_pipeline_id, 'CLOSED_LOST', 'Perdido', 'Oportunidade perdida', 9, 'CLOSED_LOST', 0, '#ef4444', true, NULL, false, NOW(), NOW());

    RAISE NOTICE 'Enterprise pipeline created with ID: %', v_pipeline_id;
END $$;

-- Create a pipeline for inside sales / SMB
DO $$
DECLARE
    v_company_id UUID := '00000000-0000-0000-0000-000000000001'::UUID;
    v_pipeline_id UUID;
BEGIN
    INSERT INTO sales_pipelines (id, company_id, code, name, description, is_active, is_default, created_at, updated_at)
    VALUES (
        uuid_generate_v4(), v_company_id, 'INSIDE', 'Inside Sales / SMB',
        'Pipeline para vendas internas (inside sales) e pequenas/médias empresas', true, false, NOW(), NOW()
    ) RETURNING id INTO v_pipeline_id;

    INSERT INTO pipeline_stages (id, pipeline_id, code, name, description, order_index, type, probability, color, is_active, max_days_in_stage, allow_skip, created_at, updated_at)
    VALUES
    (uuid_generate_v4(), v_pipeline_id, 'NEW', 'Novo Contato', 'Lead recém-criado aguardando primeiro contato', 1, 'PROSPECTING', 10, '#3b82f6', true, 2, true, NOW(), NOW()),
    (uuid_generate_v4(), v_pipeline_id, 'CONTACTED', 'Contatado', 'Primeiro contato realizado', 2, 'QUALIFICATION', 20, '#06b6d4', true, 3, false, NOW(), NOW()),
    (uuid_generate_v4(), v_pipeline_id, 'INTERESTED', 'Interessado', 'Cliente demonstrou interesse na solução', 3, 'QUALIFICATION', 35, '#8b5cf6', true, 7, false, NOW(), NOW()),
    (uuid_generate_v4(), v_pipeline_id, 'PROPOSAL', 'Proposta Enviada', 'Proposta comercial enviada', 4, 'PROPOSAL', 55, '#a855f7', true, 5, false, NOW(), NOW()),
    (uuid_generate_v4(), v_pipeline_id, 'NEGOTIATION', 'Negociação', 'Negociação final de preço/termos', 5, 'NEGOTIATION', 75, '#f59e0b', true, 3, false, NOW(), NOW()),
    (uuid_generate_v4(), v_pipeline_id, 'CLOSED_WON', 'Fechado', 'Venda realizada', 6, 'CLOSED_WON', 100, '#22c55e', true, NULL, false, NOW(), NOW()),
    (uuid_generate_v4(), v_pipeline_id, 'CLOSED_LOST', 'Perdido', 'Oportunidade perdida', 7, 'CLOSED_LOST', 0, '#ef4444', true, NULL, false, NOW(), NOW());

    RAISE NOTICE 'Inside Sales pipeline created with ID: %', v_pipeline_id;
END $$;