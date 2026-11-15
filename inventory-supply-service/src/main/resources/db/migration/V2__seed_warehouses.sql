-- V2__seed_warehouses.sql
-- Seed initial warehouses for development

-- Insert default company (if not exists)
INSERT IGNORE INTO companies (id, code, name, cnpj, address, city, state, country, is_active, created_at, updated_at)
VALUES ('00000000-0000-0000-0000-000000000001', 'ACME', 'ACME Corporation', '00.000.000/0001-00', 'Av. Principal, 1000', 'São Paulo', 'SP', 'BR', TRUE, NOW(), NOW());

-- Insert default warehouses
INSERT IGNORE INTO warehouses (id, company_id, code, name, address, city, state, country, is_active, is_default, allow_negative_stock, created_at, updated_at)
VALUES 
('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 'WH-MAIN', 'Main Warehouse', 'Av. Industrial, 500', 'São Paulo', 'SP', 'BR', TRUE, TRUE, FALSE, NOW(), NOW()),
('10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', 'WH-SP', 'São Paulo Branch', 'Rua das Flores, 100', 'São Paulo', 'SP', 'BR', TRUE, FALSE, FALSE, NOW(), NOW()),
('10000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000001', 'WH-RJ', 'Rio de Janeiro Branch', 'Av. Atlântica, 200', 'Rio de Janeiro', 'RJ', 'BR', TRUE, FALSE, FALSE, NOW(), NOW()),
('10000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000001', 'WH-MG', 'Minas Gerais Branch', 'Av. Amazonas, 300', 'Belo Horizonte', 'MG', 'BR', TRUE, FALSE, FALSE, NOW(), NOW()),
('10000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000001', 'WH-TRANSIT', 'In-Transit Warehouse', 'Virtual Location', 'Virtual', 'SP', 'BR', TRUE, FALSE, TRUE, NOW(), NOW());

-- Insert default locations for Main Warehouse
INSERT IGNORE INTO locations (id, company_id, warehouse_id, code, name, location_type, aisle, rack, shelf, bin, is_active, is_pickable, is_receivable, created_at, updated_at)
VALUES
('20000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'RECV-01', 'Receiving Dock 1', 'RECEIVING', 'A', '01', '01', '01', TRUE, FALSE, TRUE, NOW(), NOW()),
('20000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'STOR-A-01', 'Storage Aisle A Rack 1', 'STORAGE', 'A', '01', '01', '01', TRUE, TRUE, FALSE, NOW(), NOW()),
('20000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'STOR-A-02', 'Storage Aisle A Rack 2', 'STORAGE', 'A', '01', '02', '01', TRUE, TRUE, FALSE, NOW(), NOW()),
('20000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'STOR-B-01', 'Storage Aisle B Rack 1', 'STORAGE', 'B', '01', '01', '01', TRUE, TRUE, FALSE, NOW(), NOW()),
('20000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'PICK-01', 'Picking Zone 1', 'PICKING', 'C', '01', '01', '01', TRUE, TRUE, FALSE, NOW(), NOW()),
('20000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'PACK-01', 'Packing Zone 1', 'PACKING', 'D', '01', '01', '01', TRUE, TRUE, FALSE, NOW(), NOW()),
('20000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'SHIP-01', 'Shipping Dock 1', 'SHIPPING', 'E', '01', '01', '01', TRUE, FALSE, FALSE, NOW(), NOW()),
('20000000-0000-0000-0000-000000000008', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'QUAR-01', 'Quarantine Area', 'QUARANTINE', 'F', '01', '01', '01', TRUE, FALSE, TRUE, NOW(), NOW()),
('20000000-0000-0000-0000-000000000009', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'DAMG-01', 'Damaged Goods Area', 'DAMAGED', 'F', '01', '02', '01', TRUE, FALSE, FALSE, NOW(), NOW());

-- Insert default locations for Transit Warehouse
INSERT IGNORE INTO locations (id, company_id, warehouse_id, code, name, location_type, is_active, is_pickable, is_receivable, created_at, updated_at)
VALUES
('20000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000005', 'TRANSIT', 'In-Transit Location', 'TRANSIT', TRUE, TRUE, TRUE, NOW(), NOW());

-- Insert sample items
INSERT IGNORE INTO items (id, company_id, code, name, description, item_type, item_group, brand, unit_of_measure, valuation_method, standard_rate, is_active, is_stock_item, is_purchasable, is_sellable, has_batch, has_serial_no, created_at, updated_at)
VALUES
('30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 'ITEM-001', 'Notebook Dell Inspiron 15', 'Notebook Dell Inspiron 15 8GB RAM 256GB SSD', 'PRODUCT', 'ELECTRONICS', 'DELL', 'UN', 'MOVING_AVERAGE', 3500.00, TRUE, TRUE, TRUE, TRUE, FALSE, FALSE, NOW(), NOW()),
('30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', 'ITEM-002', 'Mouse Logitech MX Master 3', 'Mouse sem fio Logitech MX Master 3', 'PRODUCT', 'ELECTRONICS', 'LOGITECH', 'UN', 'MOVING_AVERAGE', 450.00, TRUE, TRUE, TRUE, TRUE, FALSE, FALSE, NOW(), NOW()),
('30000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000001', 'ITEM-003', 'Teclado Mecânico Keychron K2', 'Teclado mecânico Keychron K2 75% RGB', 'PRODUCT', 'ELECTRONICS', 'KEYCHRON', 'UN', 'FIFO', 650.00, TRUE, TRUE, TRUE, TRUE, FALSE, FALSE, NOW(), NOW()),
('30000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000001', 'ITEM-004', 'Monitor LG 27" 4K', 'Monitor LG 27 polegadas 4K IPS', 'PRODUCT', 'ELECTRONICS', 'LG', 'UN', 'MOVING_AVERAGE', 1800.00, TRUE, TRUE, TRUE, TRUE, TRUE, FALSE, NOW(), NOW()),
('30000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000001', 'ITEM-005', 'Cabo HDMI 2m', 'Cabo HDMI 2 metros 4K', 'PRODUCT', 'ELECTRONICS', 'GENERIC', 'UN', 'STANDARD', 35.00, TRUE, TRUE, TRUE, TRUE, FALSE, FALSE, NOW(), NOW()),
('30000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000001', 'RAW-001', 'Aço Inox 304 - Chapa 2mm', 'Matéria-prima: Chapa de aço inox 304 espessura 2mm', 'RAW_MATERIAL', 'METALS', 'GENERIC', 'KG', 'FIFO', 45.00, TRUE, TRUE, TRUE, FALSE, TRUE, FALSE, NOW(), NOW()),
('30000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-000000000001', 'RAW-002', 'Plástico ABS - Granulado', 'Matéria-prima: Granulado de plástico ABS', 'RAW_MATERIAL', 'PLASTICS', 'GENERIC', 'KG', 'MOVING_AVERAGE', 12.00, TRUE, TRUE, TRUE, FALSE, TRUE, FALSE, NOW(), NOW()),
('30000000-0000-0000-0000-000000000008', '00000000-0000-0000-0000-000000000001', 'SVC-001', 'Serviço de Montagem', 'Serviço de montagem de equipamentos', 'SERVICE', 'SERVICES', 'INTERNAL', 'HR', 'STANDARD', 150.00, TRUE, FALSE, FALSE, TRUE, FALSE, FALSE, NOW(), NOW());

-- Insert sample suppliers
INSERT IGNORE INTO suppliers (id, company_id, supplier_code, supplier_name, supplier_type, cnpj, address, city, state, country, contact_person, email, phone, payment_terms, currency, credit_limit, lead_time_days, is_active, is_approved, tax_regime, crt, created_at, updated_at)
VALUES
('40000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 'SUP-001', 'Dell Technologies Brasil', 'STRATEGIC', '00.000.000/0001-01', 'Av. das Nações Unidas, 12901', 'São Paulo', 'SP', 'BR', 'João Silva', 'joao.silva@dell.com', '(11) 4003-9000', '30 dias', 'BRL', 500000.00, 15, TRUE, TRUE, 'LUCRO_REAL', '1', NOW(), NOW()),
('40000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', 'SUP-002', 'Logitech Brasil', 'PREFERRED', '00.000.000/0001-02', 'Av. Paulista, 1000', 'São Paulo', 'SP', 'BR', 'Maria Santos', 'maria.santos@logitech.com', '(11) 3000-4000', '45 dias', 'BRL', 200000.00, 20, TRUE, TRUE, 'LUCRO_REAL', '1', NOW(), NOW()),
('40000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000001', 'SUP-003', 'Aços Brasil S.A.', 'REGULAR', '00.000.000/0001-03', 'Av. Industrial, 5000', 'Contagem', 'MG', 'BR', 'Carlos Oliveira', 'carlos@acosbrasil.com.br', '(31) 3000-5000', '30 dias', 'BRL', 300000.00, 10, TRUE, TRUE, 'LUCRO_PRESUMIDO', '3', NOW(), NOW()),
('40000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000001', 'SUP-004', 'Plásticos Nordeste Ltda', 'REGULAR', '00.000.000/0001-04', 'Av. das Indústrias, 200', 'Recife', 'PE', 'BR', 'Ana Costa', 'ana@plasticosnordeste.com.br', '(81) 3000-6000', '60 dias', 'BRL', 150000.00, 25, TRUE, TRUE, 'SIMPLES_NACIONAL', '1', NOW(), NOW());