-- V2__seed_warehouses.sql
-- Seed default warehouses and locations for development

-- Insert default warehouses for a sample company
-- Note: In production, company_id would be parameterized

-- Main warehouse
INSERT INTO warehouses (id, company_id, code, name, address, city, state, country, postal_code, is_active, is_default, allow_negative_stock, created_at, updated_at, version)
VALUES 
('11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'MAIN', 'Main Warehouse', 'Av. Principal, 1000', 'São Paulo', 'SP', 'BR', '01000-000', TRUE, TRUE, FALSE, NOW(), NOW(), 0),
('22222222-2222-2222-2222-222222222222', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'BRANCH1', 'Branch Warehouse 1', 'Rua das Filiais, 500', 'Rio de Janeiro', 'RJ', 'BR', '20000-000', TRUE, FALSE, FALSE, NOW(), NOW(), 0),
('33333333-3333-3333-3333-333333333333', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'BRANCH2', 'Branch Warehouse 2', 'Av. Comercial, 200', 'Belo Horizonte', 'MG', 'BR', '30000-000', TRUE, FALSE, FALSE, NOW(), NOW(), 0),
('44444444-4444-4444-4444-444444444444', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'TRANSIT', 'Transit Warehouse', 'Rodovia BR-116, Km 10', 'São Paulo', 'SP', 'BR', '01000-100', TRUE, FALSE, TRUE, NOW(), NOW(), 0),
('55555555-5555-5555-5555-555555555555', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'QUARANTINE', 'Quarantine Warehouse', 'Rua da Qualidade, 50', 'São Paulo', 'SP', 'BR', '01000-200', TRUE, FALSE, FALSE, NOW(), NOW(), 0);

-- Insert locations for Main Warehouse
INSERT INTO locations (id, company_id, warehouse_id, code, name, location_type, aisle, rack, shelf, bin, is_active, is_pickable, is_receivable, created_at, updated_at, version)
VALUES 
-- Receiving area
('aaaaaaaa-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'REC-01', 'Receiving Dock 1', 'RECEIVING', 'A', '01', '01', '01', TRUE, FALSE, TRUE, NOW(), NOW(), 0),
('aaaaaaaa-1112-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'REC-02', 'Receiving Dock 2', 'RECEIVING', 'A', '01', '02', '01', TRUE, FALSE, TRUE, NOW(), NOW(), 0),
-- Storage area
('aaaaaaaa-2111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'STO-A-01', 'Storage Aisle A Rack 1', 'STORAGE', 'A', '01', '01', '01', TRUE, TRUE, FALSE, NOW(), NOW(), 0),
('aaaaaaaa-2112-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'STO-A-02', 'Storage Aisle A Rack 2', 'STORAGE', 'A', '02', '01', '01', TRUE, TRUE, FALSE, NOW(), NOW(), 0),
('aaaaaaaa-2113-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'STO-B-01', 'Storage Aisle B Rack 1', 'STORAGE', 'B', '01', '01', '01', TRUE, TRUE, FALSE, NOW(), NOW(), 0),
('aaaaaaaa-2114-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'STO-B-02', 'Storage Aisle B Rack 2', 'STORAGE', 'B', '02', '01', '01', TRUE, TRUE, FALSE, NOW(), NOW(), 0),
-- Picking area
('aaaaaaaa-3111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'PICK-01', 'Picking Zone 1', 'PICKING', 'C', '01', '01', '01', TRUE, TRUE, FALSE, NOW(), NOW(), 0),
('aaaaaaaa-3112-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'PICK-02', 'Picking Zone 2', 'PICKING', 'C', '02', '01', '01', TRUE, TRUE, FALSE, NOW(), NOW(), 0),
-- Packing area
('aaaaaaaa-4111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'PACK-01', 'Packing Station 1', 'PACKING', 'D', '01', '01', '01', TRUE, TRUE, FALSE, NOW(), NOW(), 0),
-- Shipping area
('aaaaaaaa-5111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'SHIP-01', 'Shipping Dock 1', 'SHIPPING', 'E', '01', '01', '01', TRUE, FALSE, FALSE, NOW(), NOW(), 0),
-- Quarantine
('aaaaaaaa-6111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'QUAR-01', 'Quarantine Area', 'QUARANTINE', 'F', '01', '01', '01', TRUE, FALSE, FALSE, NOW(), NOW(), 0);

-- Insert locations for Branch Warehouse 1
INSERT INTO locations (id, company_id, warehouse_id, code, name, location_type, aisle, rack, shelf, bin, is_active, is_pickable, is_receivable, created_at, updated_at, version)
VALUES 
('bbbbbbbb-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222', 'REC-01', 'Receiving Dock', 'RECEIVING', 'A', '01', '01', '01', TRUE, FALSE, TRUE, NOW(), NOW(), 0),
('bbbbbbbb-2111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222', 'STO-01', 'Storage Area', 'STORAGE', 'A', '01', '01', '01', TRUE, TRUE, FALSE, NOW(), NOW(), 0),
('bbbbbbbb-3111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222', 'PICK-01', 'Picking Zone', 'PICKING', 'B', '01', '01', '01', TRUE, TRUE, FALSE, NOW(), NOW(), 0),
('bbbbbbbb-5111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222', 'SHIP-01', 'Shipping Dock', 'SHIPPING', 'C', '01', '01', '01', TRUE, FALSE, FALSE, NOW(), NOW(), 0);

-- Insert locations for Transit Warehouse
INSERT INTO locations (id, company_id, warehouse_id, code, name, location_type, aisle, rack, shelf, bin, is_active, is_pickable, is_receivable, created_at, updated_at, version)
VALUES 
('cccccccc-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '44444444-4444-4444-4444-444444444444', 'TRANSIT-IN', 'In Transit', 'TRANSIT', 'A', '01', '01', '01', TRUE, FALSE, TRUE, NOW(), NOW(), 0),
('cccccccc-2111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '44444444-4444-4444-4444-444444444444', 'TRANSIT-OUT', 'Out Transit', 'TRANSIT', 'A', '02', '01', '01', TRUE, TRUE, FALSE, NOW(), NOW(), 0);

-- Insert locations for Quarantine Warehouse
INSERT INTO locations (id, company_id, warehouse_id, code, name, location_type, aisle, rack, shelf, bin, is_active, is_pickable, is_receivable, created_at, updated_at, version)
VALUES 
('dddddddd-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '55555555-5555-5555-5555-555555555555', 'QUAR-01', 'Quality Hold', 'QUARANTINE', 'A', '01', '01', '01', TRUE, FALSE, FALSE, NOW(), NOW(), 0),
('dddddddd-2111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '55555555-5555-5555-5555-555555555555', 'QUAR-02', 'Rejected Items', 'QUARANTINE', 'A', '02', '01', '01', TRUE, FALSE, FALSE, NOW(), NOW(), 0);

-- Sample suppliers
INSERT INTO suppliers (id, company_id, supplier_code, supplier_name, supplier_type, cnpj, address, city, state, country, postal_code, contact_person, email, phone, payment_terms, currency, credit_limit, lead_time_days, is_active, is_approved, tax_regime, crt, created_at, updated_at, version)
VALUES 
('ffffffff-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'SUP-001', 'Fornecedor Alpha Ltda', 'PREFERRED', '12.345.678/0001-90', 'Rua dos Fornecedores, 100', 'São Paulo', 'SP', 'BR', '01000-000', 'João Silva', 'contato@alpha.com.br', '(11) 3333-4444', '30 dias', 'BRL', 100000.00, 7, TRUE, TRUE, 'LUCRO_PRESUMIDO', '2', NOW(), NOW(), 0),
('ffffffff-2222-2222-2222-222222222222', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'SUP-002', 'Beta Comércio e Indústria', 'REGULAR', '98.765.432/0001-10', 'Av. Industrial, 500', 'Rio de Janeiro', 'RJ', 'BR', '20000-000', 'Maria Santos', 'vendas@beta.com.br', '(21) 2222-3333', '45 dias', 'BRL', 50000.00, 10, TRUE, TRUE, 'LUCRO_REAL', '1', NOW(), NOW(), 0),
('ffffffff-3333-3333-3333-333333333333', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'SUP-003', 'Gamma Importação e Exportação', 'STRATEGIC', '11.222.333/0001-44', 'Rua do Comércio Exterior, 200', 'Santos', 'SP', 'BR', '11000-000', 'Carlos Oliveira', 'import@gamma.com.br', '(13) 3333-5555', '60 dias', 'USD', 200000.00, 15, TRUE, TRUE, 'LUCRO_REAL', '1', NOW(), NOW(), 0);