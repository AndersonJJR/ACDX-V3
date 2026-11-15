-- V2__seed_benefits.sql
-- Seed default benefit types and common benefits

-- Insert default benefit types (these are reference data)
INSERT INTO benefit (uuid, company_id, code, name, description, benefit_type, provider_name, provider_cnpj, plan_name, plan_number, employee_contribution_pct, employer_contribution_pct, max_employee_value, max_employer_value, is_mandatory, is_taxable, requires_dependent, start_date, end_date, is_active, created_at, updated_at)
VALUES
-- Vale Transporte (mandatory for CLT)
('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 1, 'VT', 'Vale Transporte', 'Vale transporte para deslocamento casa-trabalho', 'TRANSPORT_VOUCHER', 'Empresa de Transporte Ltda', '12.345.678/0001-90', 'Plano Padrão', 'VT001', 6.00, 94.00, 300.00, 5000.00, TRUE, FALSE, FALSE, '2024-01-01', NULL, TRUE, NOW(), NOW()),

-- Vale Refeição
('b2c3d4e5-f6a7-8901-bcde-f12345678901', 1, 'VR', 'Vale Refeição', 'Vale refeição/alimentação', 'MEAL_VOUCHER', 'Sodexo', '12.345.678/0002-90', 'Refeição Pass', 'VR001', 0.00, 100.00, NULL, 25.00, FALSE, FALSE, FALSE, '2024-01-01', NULL, TRUE, NOW(), NOW()),

-- Vale Alimentação
('c3d4e5f6-a7b8-9012-cdef-123456789012', 1, 'VA', 'Vale Alimentação', 'Vale alimentação para supermercado', 'FOOD_VOUCHER', 'Ticket', '12.345.678/0003-90', 'Alimentação Pass', 'VA001', 0.00, 100.00, NULL, 30.00, FALSE, FALSE, FALSE, '2024-01-01', NULL, TRUE, NOW(), NOW()),

-- Plano de Saúde
('d4e5f6a7-b8c9-0123-def1-234567890123', 1, 'SAUDE', 'Plano de Saúde', 'Assistência médica hospitalar', 'HEALTH_INSURANCE', 'Amil', '12.345.678/0004-90', 'Plano Empresarial 100', 'SAUDE001', 10.00, 90.00, 500.00, 1500.00, FALSE, TRUE, TRUE, '2024-01-01', NULL, TRUE, NOW(), NOW()),

-- Plano Odontológico
('e5f6a7b8-c9d0-1234-ef12-345678901234', 1, 'ODONTO', 'Plano Odontológico', 'Assistência odontológica', 'DENTAL_INSURANCE', 'Bradesco Dental', '12.345.678/0005-90', 'Dental Empresarial', 'ODONTO001', 5.00, 95.00, 50.00, 150.00, FALSE, FALSE, TRUE, '2024-01-01', NULL, TRUE, NOW(), NOW()),

-- Seguro de Vida
('f6a7b8c9-d0e1-2345-f123-456789012345', 1, 'VIDA', 'Seguro de Vida', 'Seguro de vida em grupo', 'LIFE_INSURANCE', 'Porto Seguro', '12.345.678/0006-90', 'Vida em Grupo', 'VIDA001', 0.00, 100.00, NULL, 100.00, FALSE, FALSE, FALSE, '2024-01-01', NULL, TRUE, NOW(), NOW()),

-- Previdência Privada
('a7b8c9d0-e1f2-3456-1234-567890123456', 1, 'PREV', 'Previdência Privada', 'Plano de previdência complementar', 'PENSION', 'Brasilprev', '12.345.678/0007-90', 'PGBL Empresarial', 'PREV001', 5.00, 5.00, 1000.00, 1000.00, FALSE, FALSE, FALSE, '2024-01-01', NULL, TRUE, NOW(), NOW()),

-- Gympass / Wellness
('b8c9d0e1-f2a3-4567-2345-678901234567', 1, 'GYM', 'Gympass', 'Academia e bem-estar', 'GYM', 'Gympass', '12.345.678/0008-90', 'Corporativo', 'GYM001', 20.00, 80.00, 100.00, 200.00, FALSE, FALSE, FALSE, '2024-01-01', NULL, TRUE, NOW(), NOW()),

-- Auxílio Educação
('c9d0e1f2-a3b4-5678-3456-789012345678', 1, 'EDUC', 'Auxílio Educação', 'Auxílio para cursos e faculdade', 'EDUCATION', 'Instituição de Ensino', '12.345.678/0009-90', 'Convênio Educacional', 'EDUC001', 0.00, 100.00, NULL, 1000.00, FALSE, TRUE, FALSE, '2024-01-01', NULL, TRUE, NOW(), NOW()),

-- Auxílio Creche
('d0e1f2a3-b4c5-6789-4567-890123456789', 1, 'CRECHE', 'Auxílio Creche', 'Auxílio para creche/escola infantil', 'CHILDCARE', 'Creche Conveniada', '12.345.678/0010-90', 'Creche Parceira', 'CRECHE001', 0.00, 100.00, NULL, 800.00, FALSE, FALSE, TRUE, '2024-01-01', NULL, TRUE, NOW(), NOW()),

-- Vale Combustível
('e1f2a3b4-c5d6-7890-5678-901234567890', 1, 'COMB', 'Vale Combustível', 'Vale combustível para veículo próprio', 'FUEL', 'Shell', '12.345.678/0011-90', 'Shell Corporativo', 'COMB001', 0.00, 100.00, NULL, 500.00, FALSE, TRUE, FALSE, '2024-01-01', NULL, TRUE, NOW(), NOW()),

-- Farmácia
('f2a3b4c5-d6e7-8901-6789-012345678901', 1, 'FARMA', 'Vale Farmácia', 'Vale para medicamentos', 'PHARMACY', 'Drogasil', '12.345.678/0012-90', 'Farmácia Convênio', 'FARMA001', 0.00, 100.00, NULL, 200.00, FALSE, FALSE, TRUE, '2024-01-01', NULL, TRUE, NOW(), NOW()),

-- Ótica
('a3b4c5d6-e7f8-9012-7890-123456789012', 1, 'OTICA', 'Vale Ótica', 'Vale para óculos e lentes', 'OPTICAL', 'Óticas Carol', '12.345.678/0013-90', 'Ótica Convênio', 'OTICA001', 0.00, 100.00, NULL, 300.00, FALSE, FALSE, TRUE, '2024-01-01', NULL, TRUE, NOW(), NOW())

ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description),
    is_active = VALUES(is_active),
    updated_at = NOW();

-- Insert default leave types
INSERT INTO leave_type (uuid, company_id, code, name, description, category, max_days_per_year, requires_approval, requires_document, paid, affects_benefits, color, is_active, created_at, updated_at)
VALUES
('11111111-2222-3333-4444-555555555555', 1, 'FERIAS', 'Férias', 'Férias anuais remuneradas', 'VACATION', 30, TRUE, FALSE, TRUE, FALSE, '#4CAF50', TRUE, NOW(), NOW()),
('22222222-3333-4444-5555-666666666666', 1, 'LICENCA_MEDICA', 'Licença Médica', 'Afastamento por motivo de saúde', 'MEDICAL', NULL, TRUE, TRUE, TRUE, FALSE, '#F44336', TRUE, NOW(), NOW()),
('33333333-4444-5555-6666-777777777777', 1, 'LICENCA_MATERNIDADE', 'Licença Maternidade', 'Licença maternidade de 120 dias', 'MATERNITY', 120, TRUE, TRUE, TRUE, FALSE, '#E91E63', TRUE, NOW(), NOW()),
('44444444-5555-6666-7777-888888888888', 1, 'LICENCA_PATERNIDADE', 'Licença Paternidade', 'Licença paternidade de 5 dias', 'PATERNITY', 5, TRUE, FALSE, TRUE, FALSE, '#2196F3', TRUE, NOW(), NOW()),
('55555555-6666-7777-8888-999999999999', 1, 'LICENCA_CASAMENTO', 'Licença Casamento', 'Licença para casamento (3 dias)', 'MARRIAGE', 3, TRUE, TRUE, TRUE, FALSE, '#9C27B0', TRUE, NOW(), NOW()),
('66666666-7777-8888-9999-000000000000', 1, 'LICENCA_LUTO', 'Licença Luto', 'Licença por falecimento de familiar (2 dias)', 'BEREAVEMENT', 2, TRUE, TRUE, TRUE, FALSE, '#795548', TRUE, NOW(), NOW()),
('77777777-8888-9999-0000-111111111111', 1, 'LICENCA_ESTUDO', 'Licença Estudo', 'Licença para estudos/concurso', 'STUDY', NULL, TRUE, TRUE, FALSE, FALSE, '#607D8B', TRUE, NOW(), NOW()),
('88888888-9999-0000-1111-222222222222', 1, 'LICENCA_ELEITORAL', 'Licença Eleitoral', 'Licença para trabalho eleitoral', 'ELECTION', NULL, TRUE, TRUE, TRUE, FALSE, '#FF9800', TRUE, NOW(), NOW()),
('99999999-0000-1111-2222-333333333333', 1, 'LICENCA_MILITAR', 'Licença Militar', 'Licença para serviço militar obrigatório', 'MILITARY', NULL, TRUE, TRUE, FALSE, FALSE, '#3F51B5', TRUE, NOW(), NOW()),
('00000000-1111-2222-3333-444444444444', 1, 'DOACAO_SANGUE', 'Doação de Sangue', 'Licença para doação de sangue (1 dia por doação)', 'BLOOD_DONATION', NULL, TRUE, TRUE, TRUE, FALSE, '#F44336', TRUE, NOW(), NOW()),
('11111111-0000-2222-3333-444444444445', 1, 'SEM_REMUNERACAO', 'Licença Sem Remuneração', 'Licença não remunerada a pedido do empregado', 'UNPAID', NULL, TRUE, FALSE, FALSE, TRUE, '#9E9E9E', TRUE, NOW(), NOW()),
('22222222-1111-3333-4444-555555555556', 1, 'COMPENSATORIA', 'Folga Compensatória', 'Folga por banco de horas ou trabalho em feriado', 'COMPENSATORY', NULL, TRUE, FALSE, TRUE, FALSE, '#00BCD4', TRUE, NOW(), NOW()),
('33333333-2222-4444-5555-666666666667', 1, 'OUTROS', 'Outros Afastamentos', 'Outros tipos de afastamentos não categorizados', 'OTHER', NULL, TRUE, FALSE, FALSE, FALSE, '#757575', TRUE, NOW(), NOW())

ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    is_active = VALUES(is_active),
    updated_at = NOW();

-- Insert default notification templates
INSERT INTO notification_template (uuid, company_id, code, name, channel, subject, content_template, content_type, variables, locale, is_active, is_system, created_at, updated_at)
VALUES
-- Employee hired email
('t1t1t1t1-t2t2-t3t3-t4t4-t5t5t5t5t5t5', 1, 'EMPLOYEE_HIRED_EMAIL', 'Boas-vindas - Novo Funcionário', 'EMAIL', 'Bem-vindo à {{company_name}}, {{employee_name}}!',
'<html><body><h2>Bem-vindo à {{company_name}}!</h2><p>Olá {{employee_name}},</p><p>Seja bem-vindo à equipe! Sua matrícula é <strong>{{employee_number}}</strong> e sua data de admissão é {{hire_date}}.</p><p>Estamos felizes em tê-lo conosco.</p><p>Atenciosamente,<br>Equipe de RH</p></body></html>', 'HTML', '{"company_name": "ACDX ERP", "employee_name": "", "employee_number": "", "hire_date": ""}', 'pt_BR', TRUE, TRUE, NOW(), NOW()),

-- Payroll processed email
('t2t2t2t2-t3t3-t4t4-t5t5-t6t6t6t6t6t6', 1, 'PAYROLL_PROCESSED_EMAIL', 'Holerite Disponível', 'EMAIL', 'Seu holerite de {{month}}/{{year}} está disponível',
'<html><body><h2>Holerite Disponível</h2><p>Olá {{employee_name}},</p><p>Seu holerite referente a {{month}}/{{year}} foi processado e está disponível para consulta.</p><p><strong>Salário Bruto:</strong> {{gross_amount}}<br><strong>Salário Líquido:</strong> {{net_amount}}</p><p><a href="{{portal_url}}/payslips/{{payslip_id}}">Acessar Holerite</a></p><p>Atenciosamente,<br>Departamento Pessoal</p></body></html>', 'HTML', '{"employee_name": "", "month": "", "year": "", "gross_amount": "", "net_amount": "", "portal_url": "https://erp.company.com", "payslip_id": ""}', 'pt_BR', TRUE, TRUE, NOW(), NOW()),

-- Leave approved email
('t3t3t3t3-t4t4-t5t5-t6t6-t7t7t7t7t7t7', 1, 'LEAVE_APPROVED_EMAIL', 'Licença Aprovada', 'EMAIL', 'Sua licença {{leave_type}} foi aprovada',
'<html><body><h2>Licença Aprovada</h2><p>Olá {{employee_name}},</p><p>Sua solicitação de <strong>{{leave_type}}</strong> foi <span style="color: green;"><strong>APROVADA</strong></span>.</p><p><strong>Período:</strong> {{start_date}} a {{end_date}} ({{total_days}} dias)</p><p>Atenciosamente,<br>Equipe de RH</p></body></html>', 'HTML', '{"employee_name": "", "leave_type": "", "start_date": "", "end_date": "", "total_days": ""}', 'pt_BR', TRUE, TRUE, NOW(), NOW()),

-- Leave rejected email
('t4t4t4t4-t5t5-t6t6-t7t7-t8t8t8t8t8t8', 1, 'LEAVE_REJECTED_EMAIL', 'Licença Rejeitada', 'EMAIL', 'Sua licença {{leave_type}} foi rejeitada',
'<html><body><h2>Licença Rejeitada</h2><p>Olá {{employee_name}},</p><p>Sua solicitação de <strong>{{leave_type}}</strong> foi <span style="color: red;"><strong>REJEITADA</strong></span>.</p><p><strong>Motivo:</strong> {{rejection_reason}}</p><p>Em caso de dúvidas, entre em contato com o RH.</p></body></html>', 'HTML', '{"employee_name": "", "leave_type": "", "rejection_reason": ""}', 'pt_BR', TRUE, TRUE, NOW(), NOW()),

-- Time record reminder
('t5t5t5t5-t6t6-t7t7-t8t8-t9t9t9t9t9t9', 1, 'TIME_RECORD_REMINDER', 'Lembrete de Ponto', 'PUSH', 'Lembrete: Registre seu ponto', 'Não esqueça de registrar sua {{record_type}} de hoje ({{date}}) às {{time}}.', 'TEXT', '{"record_type": "", "date": "", "time": ""}', 'pt_BR', TRUE, TRUE, NOW(), NOW()),

-- Webhook test notification
('t6t6t6t6-t7t7-t8t8-t9t9-t0t0t0t0t0t0', 1, 'WEBHOOK_TEST', 'Teste de Webhook', 'EMAIL', 'Teste de Webhook - {{subscription_name}}',
'<html><body><h2>Teste de Webhook</h2><p>Este é um teste de entrega do webhook <strong>{{subscription_name}}</strong>.</p><p>Se você recebeu este e-mail, a configuração está funcionando corretamente.</p></body></html>', 'HTML', '{"subscription_name": ""}', 'pt_BR', TRUE, TRUE, NOW(), NOW())

ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    is_active = VALUES(is_active),
    updated_at = NOW();

-- Insert default company (if not exists)
INSERT INTO company (uuid, corporate_name, trade_name, cnpj, state_registration, municipal_registration, address_cep, address_street, address_number, address_complement, address_neighborhood, address_city, address_state, address_country, phone, email, website, cnae_primary, cnae_secondary, legal_nature, company_size, tax_regime, is_active, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000000001', 'ACDX ERP LTDA', 'ACDX ERP', '00.000.000/0001-00', '123.456.789.100', '12.345.678', '01000-000', 'Av. Paulista', '1000', '10º andar', 'Bela Vista', 'São Paulo', 'SP', 'BRASIL', '(11) 3000-0000', 'contato@erp-brazil.com', 'https://erp-brazil.com', '6201-5/01', '6202-3/00', '206-2', 'SMALL', 'SIMPLES_NACIONAL', TRUE, NOW(), NOW())

ON DUPLICATE KEY UPDATE
    corporate_name = VALUES(corporate_name),
    is_active = VALUES(is_active),
    updated_at = NOW();

-- Insert default departments
INSERT INTO department (uuid, company_id, code, name, description, parent_id, cost_center_id, manager_id, is_active, created_at, updated_at)
VALUES
('d1d1d1d1-d2d2-d3d3-d4d4-d5d5d5d5d5d5', 1, 'TECH', 'Tecnologia', 'Departamento de Tecnologia da Informação', NULL, NULL, NULL, TRUE, NOW(), NOW()),
('d2d2d2d2-d3d3-d4d4-d5d5-d6d6d6d6d6d6', 1, 'HR', 'Recursos Humanos', 'Departamento de Recursos Humanos', NULL, NULL, NULL, TRUE, NOW(), NOW()),
('d3d3d3d3-d4d4-d5d5-d6d6-d7d7d7d7d7d7', 1, 'FIN', 'Financeiro', 'Departamento Financeiro', NULL, NULL, NULL, TRUE, NOW(), NOW()),
('d4d4d4d4-d5d5-d6d6-d7d7-d8d8d8d8d8d8', 1, 'OPS', 'Operações', 'Departamento de Operações', NULL, NULL, NULL, TRUE, NOW(), NOW()),
('d5d5d5d5-d6d6-d7d7-d8d8-d9d9d9d9d9d9', 1, 'SALES', 'Vendas', 'Departamento Comercial', NULL, NULL, NULL, TRUE, NOW(), NOW())

ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    is_active = VALUES(is_active),
    updated_at = NOW();

-- Insert default cost centers
INSERT INTO cost_center (uuid, company_id, code, name, description, parent_id, responsible_id, budget_amount, is_active, created_at, updated_at)
VALUES
('c1c1c1c1-c2c2-c3c3-c4c4-c5c5c5c5c5c5', 1, 'CC001', 'Tecnologia', 'Centro de Custo de Tecnologia', NULL, NULL, 500000.00, TRUE, NOW(), NOW()),
('c2c2c2c2-c3c3-c4c4-c5c5-c6c6c6c6c6c6', 1, 'CC002', 'Recursos Humanos', 'Centro de Custo de RH', NULL, NULL, 200000.00, TRUE, NOW(), NOW()),
('c3c3c3c3-c4c4-c5c5-c6c6-c7c7c7c7c7c7', 1, 'CC003', 'Financeiro', 'Centro de Custo Financeiro', NULL, NULL, 300000.00, TRUE, NOW(), NOW()),
('c4c4c4c4-c5c5-c6c6-c7c7-c8c8c8c8c8c8', 1, 'CC004', 'Operações', 'Centro de Custo de Operações', NULL, NULL, 400000.00, TRUE, NOW(), NOW()),
('c5c5c5c5-c6c6-c7c7-c8c8-c9c9c9c9c9c9', 1, 'CC005', 'Vendas', 'Centro de Custo Comercial', NULL, NULL, 600000.00, TRUE, NOW(), NOW())

ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    is_active = VALUES(is_active),
    updated_at = NOW();

-- Insert default union
INSERT INTO union_entity (uuid, code, name, cnpj, address, city, state, phone, email, contribution_rate, is_active, created_at, updated_at)
VALUES
('u1u1u1u1-u2u2-u3u3-u4u4-u5u5u5u5u5u5', 'SINDPD', 'Sindicato dos Trabalhadores em Processamento de Dados', '12.345.678/0001-99', 'Rua dos Sindicatos, 100', 'São Paulo', 'SP', '(11) 3000-1000', 'contato@sindpd.org.br', 0.0100, TRUE, NOW(), NOW()),
('u2u2u2u2-u3u3-u4u4-u5u5-u6u6u6u6u6u6', 'SINTEC', 'Sindicato dos Técnicos de Informática', '12.345.678/0002-99', 'Av. Técnica, 200', 'São Paulo', 'SP', '(11) 3000-2000', 'contato@sintec.org.br', 0.0150, TRUE, NOW(), NOW())

ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    is_active = VALUES(is_active),
    updated_at = NOW();

-- Insert default collective agreement
INSERT INTO collective_agreement (uuid, union_id, code, name, start_date, end_date, base_salary_minimum, work_journey_hours, overtime_rate_daytime, overtime_rate_nighttime, overtime_rate_holiday, night_shift_additional, hazardous_additional, unhealthy_additional, document_url, is_active, created_at, updated_at)
SELECT
    'ca1ca1ca-ca2c-ca3c-ca4c-ca5ca5ca5ca5',
    u.id,
    'ACORDO_2024',
    'Acordo Coletivo 2024/2025',
    '2024-01-01',
    '2025-12-31',
    1500.00,
    44.00,
    50.00,
    75.00,
    100.00,
    20.00,
    30.00,
    40.00,
    'https://erp.company.com/docs/acordo_2024.pdf',
    TRUE,
    NOW(),
    NOW()
FROM union_entity u WHERE u.code = 'SINDPD'
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    is_active = VALUES(is_active),
    updated_at = NOW();

-- Insert national holidays for 2024
INSERT INTO holiday (uuid, company_id, name, date, type, is_recurring, month_day, state, city, is_working_day, created_at, updated_at)
VALUES
('h1h1h1h1-h2h2-h3h3-h4h4-h5h5h5h5h5h5', NULL, 'Confraternização Universal', '2024-01-01', 'NATIONAL', TRUE, '01-01', NULL, NULL, FALSE, NOW(), NOW()),
('h2h2h2h2-h3h3-h4h4-h5h5-h6h6h6h6h6h6', NULL, 'Tiradentes', '2024-04-21', 'NATIONAL', TRUE, '04-21', NULL, NULL, FALSE, NOW(), NOW()),
('h3h3h3h3-h4h4-h5h5-h6h6-h7h7h7h7h7h7', NULL, 'Dia do Trabalhador', '2024-05-01', 'NATIONAL', TRUE, '05-01', NULL, NULL, FALSE, NOW(), NOW()),
('h4h4h4h4-h5h5-h6h6-h7h7-h8h8h8h8h8h8', NULL, 'Independência do Brasil', '2024-09-07', 'NATIONAL', TRUE, '09-07', NULL, NULL, FALSE, NOW(), NOW()),
('h5h5h5h5-h6h6-h7h7-h8h8-h9h9h9h9h9h9', NULL, 'Nossa Senhora Aparecida', '2024-10-12', 'NATIONAL', TRUE, '10-12', NULL, NULL, FALSE, NOW(), NOW()),
('h6h6h6h6-h7h7-h8h8-h9h9-h0h0h0h0h0h0', NULL, 'Finados', '2024-11-02', 'NATIONAL', TRUE, '11-02', NULL, NULL, FALSE, NOW(), NOW()),
('h7h7h7h7-h8h8-h9h9-h0h0-h1h1h1h1h1h1', NULL, 'Proclamação da República', '2024-11-15', 'NATIONAL', TRUE, '11-15', NULL, NULL, FALSE, NOW(), NOW()),
('h8h8h8h8-h9h9-h0h0-h1h1-h2h2h2h2h2h2', NULL, 'Natal', '2024-12-25', 'NATIONAL', TRUE, '12-25', NULL, NULL, FALSE, NOW(), NOW()),

-- São Paulo state holidays
('h9h9h9h9-h0h0-h1h1-h2h2-h3h3h3h3h3h3', NULL, 'Revolução Constitucionalista', '2024-07-09', 'STATE', TRUE, '07-09', 'SP', NULL, FALSE, NOW(), NOW()),

-- São Paulo city holidays
('h0h0h0h0-h1h1-h2h2-h3h3-h4h4h4h4h4h4', NULL, 'Aniversário de São Paulo', '2024-01-25', 'MUNICIPAL', TRUE, '01-25', 'SP', 'São Paulo', FALSE, NOW(), NOW()),
('h1h1h1h1-h0h0-h2h2-h3h3-h4h4h4h4h4h5', NULL, 'Corpus Christi', '2024-05-30', 'MUNICIPAL', FALSE, NULL, 'SP', 'São Paulo', FALSE, NOW(), NOW()),
('h2h2h2h2-h1h1-h3h3-h4h4-h5h5h5h5h5h6', NULL, 'Consciência Negra', '2024-11-20', 'MUNICIPAL', TRUE, '11-20', 'SP', 'São Paulo', FALSE, NOW(), NOW())

ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    is_working_day = VALUES(is_working_day),
    updated_at = NOW();