-- Flyway Migration V2: Seed Data for Identity & Tenant Service
-- Compatible with MySQL 8.4

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- Default System Tenant
-- ============================================
INSERT INTO companies (id, tenant_id, code, name, trade_name, cnpj, email, phone, address, city, state, cep, status, created_by)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'system',
    'SYSTEM',
    'ERP Brazil System',
    'ERP Brazil',
    '00.000.000/0001-00',
    'system@erp.brazil.com',
    '(11) 99999-9999',
    'Av. Paulista, 1000',
    'São Paulo',
    'SP',
    '01310-100',
    'ACTIVE',
    'system'
) ON DUPLICATE KEY UPDATE id=id;

-- ============================================
-- Default Demo Company
-- ============================================
INSERT INTO companies (id, tenant_id, code, name, trade_name, cnpj, email, phone, address, city, state, cep, status, created_by)
VALUES (
    '00000000-0000-0000-0000-000000000002',
    'demo',
    'DEMO',
    'Empresa Demo Ltda',
    'Demo ERP',
    '12.345.678/0001-90',
    'demo@erp.brazil.com',
    '(11) 88888-8888',
    'Rua das Flores, 123',
    'São Paulo',
    'SP',
    '01234-567',
    'ACTIVE',
    'system'
) ON DUPLICATE KEY UPDATE id=id;

-- ============================================
-- System Permissions
-- ============================================
-- Super Admin permissions
INSERT INTO permissions (id, tenant_id, code, name, resource, action, description, is_system, created_by)
VALUES 
    ('perm-001', 'system', 'SUPER_ADMIN_ALL', 'Super Admin - All Access', '*', '*', 'Full system access', TRUE, 'system'),
    ('perm-002', 'system', 'COMPANY_ADMIN_ALL', 'Company Admin - All Access', 'company', '*', 'Full company management', TRUE, 'system'),
    ('perm-003', 'system', 'USER_ADMIN_ALL', 'User Admin - All Access', 'user', '*', 'Full user management', TRUE, 'system'),
    ('perm-004', 'system', 'ROLE_ADMIN_ALL', 'Role Admin - All Access', 'role', '*', 'Full role management', TRUE, 'system')
ON DUPLICATE KEY UPDATE id=id;

-- Identity module permissions
INSERT INTO permissions (id, tenant_id, code, name, resource, action, description, is_system, created_by)
VALUES 
    ('perm-010', 'system', 'IDENTITY_COMPANY_CREATE', 'Create Company', 'identity.company', 'create', 'Create new companies', TRUE, 'system'),
    ('perm-011', 'system', 'IDENTITY_COMPANY_READ', 'Read Company', 'identity.company', 'read', 'View company details', TRUE, 'system'),
    ('perm-012', 'system', 'IDENTITY_COMPANY_UPDATE', 'Update Company', 'identity.company', 'update', 'Update company details', TRUE, 'system'),
    ('perm-013', 'system', 'IDENTITY_COMPANY_DELETE', 'Delete Company', 'identity.company', 'delete', 'Delete companies', TRUE, 'system'),
    ('perm-014', 'system', 'IDENTITY_COMPANY_LIST', 'List Companies', 'identity.company', 'list', 'List all companies', TRUE, 'system'),
    ('perm-015', 'system', 'IDENTITY_COMPANY_ACTIVATE', 'Activate Company', 'identity.company', 'activate', 'Activate companies', TRUE, 'system'),
    ('perm-016', 'system', 'IDENTITY_COMPANY_DEACTIVATE', 'Deactivate Company', 'identity.company', 'deactivate', 'Deactivate companies', TRUE, 'system'),
    ('perm-017', 'system', 'IDENTITY_COMPANY_SUSPEND', 'Suspend Company', 'identity.company', 'suspend', 'Suspend companies', TRUE, 'system'),
    ('perm-020', 'system', 'IDENTITY_USER_CREATE', 'Create User', 'identity.user', 'create', 'Create new users', TRUE, 'system'),
    ('perm-021', 'system', 'IDENTITY_USER_READ', 'Read User', 'identity.user', 'read', 'View user details', TRUE, 'system'),
    ('perm-022', 'system', 'IDENTITY_USER_UPDATE', 'Update User', 'identity.user', 'update', 'Update user details', TRUE, 'system'),
    ('perm-023', 'system', 'IDENTITY_USER_DELETE', 'Delete User', 'identity.user', 'delete', 'Delete users', TRUE, 'system'),
    ('perm-024', 'system', 'IDENTITY_USER_LIST', 'List Users', 'identity.user', 'list', 'List all users', TRUE, 'system'),
    ('perm-025', 'system', 'IDENTITY_USER_PASSWORD_RESET', 'Reset User Password', 'identity.user', 'password_reset', 'Reset user passwords', TRUE, 'system'),
    ('perm-026', 'system', 'IDENTITY_USER_ROLE_ASSIGN', 'Assign User Roles', 'identity.user', 'role_assign', 'Assign roles to users', TRUE, 'system'),
    ('perm-030', 'system', 'IDENTITY_ROLE_CREATE', 'Create Role', 'identity.role', 'create', 'Create new roles', TRUE, 'system'),
    ('perm-031', 'system', 'IDENTITY_ROLE_READ', 'Read Role', 'identity.role', 'read', 'View role details', TRUE, 'system'),
    ('perm-032', 'system', 'IDENTITY_ROLE_UPDATE', 'Update Role', 'identity.role', 'update', 'Update role details', TRUE, 'system'),
    ('perm-033', 'system', 'IDENTITY_ROLE_DELETE', 'Delete Role', 'identity.role', 'delete', 'Delete roles', TRUE, 'system'),
    ('perm-034', 'system', 'IDENTITY_ROLE_LIST', 'List Roles', 'identity.role', 'list', 'List all roles', TRUE, 'system'),
    ('perm-035', 'system', 'IDENTITY_ROLE_PERMISSION_ASSIGN', 'Assign Role Permissions', 'identity.role', 'permission_assign', 'Assign permissions to roles', TRUE, 'system'),
    ('perm-040', 'system', 'IDENTITY_PERMISSION_CREATE', 'Create Permission', 'identity.permission', 'create', 'Create new permissions', TRUE, 'system'),
    ('perm-041', 'system', 'IDENTITY_PERMISSION_READ', 'Read Permission', 'identity.permission', 'read', 'View permission details', TRUE, 'system'),
    ('perm-042', 'system', 'IDENTITY_PERMISSION_UPDATE', 'Update Permission', 'identity.permission', 'update', 'Update permission details', TRUE, 'system'),
    ('perm-043', 'system', 'IDENTITY_PERMISSION_DELETE', 'Delete Permission', 'identity.permission', 'delete', 'Delete permissions', TRUE, 'system'),
    ('perm-044', 'system', 'IDENTITY_PERMISSION_LIST', 'List Permissions', 'identity.permission', 'list', 'List all permissions', TRUE, 'system'),
    ('perm-050', 'system', 'IDENTITY_BRANCH_CREATE', 'Create Branch', 'identity.branch', 'create', 'Create new branches', TRUE, 'system'),
    ('perm-051', 'system', 'IDENTITY_BRANCH_READ', 'Read Branch', 'identity.branch', 'read', 'View branch details', TRUE, 'system'),
    ('perm-052', 'system', 'IDENTITY_BRANCH_UPDATE', 'Update Branch', 'identity.branch', 'update', 'Update branch details', TRUE, 'system'),
    ('perm-053', 'system', 'IDENTITY_BRANCH_DELETE', 'Delete Branch', 'identity.branch', 'delete', 'Delete branches', TRUE, 'system'),
    ('perm-054', 'system', 'IDENTITY_BRANCH_LIST', 'List Branches', 'identity.branch', 'list', 'List all branches', TRUE, 'system'),
    ('perm-060', 'system', 'IDENTITY_DEPARTMENT_CREATE', 'Create Department', 'identity.department', 'create', 'Create new departments', TRUE, 'system'),
    ('perm-061', 'system', 'IDENTITY_DEPARTMENT_READ', 'Read Department', 'identity.department', 'read', 'View department details', TRUE, 'system'),
    ('perm-062', 'system', 'IDENTITY_DEPARTMENT_UPDATE', 'Update Department', 'identity.department', 'update', 'Update department details', TRUE, 'system'),
    ('perm-063', 'system', 'IDENTITY_DEPARTMENT_DELETE', 'Delete Department', 'identity.department', 'delete', 'Delete departments', TRUE, 'system'),
    ('perm-064', 'system', 'IDENTITY_DEPARTMENT_LIST', 'List Departments', 'identity.department', 'list', 'List all departments', TRUE, 'system'),
    ('perm-070', 'system', 'IDENTITY_COSTCENTER_CREATE', 'Create Cost Center', 'identity.costcenter', 'create', 'Create new cost centers', TRUE, 'system'),
    ('perm-071', 'system', 'IDENTITY_COSTCENTER_READ', 'Read Cost Center', 'identity.costcenter', 'read', 'View cost center details', TRUE, 'system'),
    ('perm-072', 'system', 'IDENTITY_COSTCENTER_UPDATE', 'Update Cost Center', 'identity.costcenter', 'update', 'Update cost center details', TRUE, 'system'),
    ('perm-073', 'system', 'IDENTITY_COSTCENTER_DELETE', 'Delete Cost Center', 'identity.costcenter', 'delete', 'Delete cost centers', TRUE, 'system'),
    ('perm-074', 'system', 'IDENTITY_COSTCENTER_LIST', 'List Cost Centers', 'identity.costcenter', 'list', 'List all cost centers', TRUE, 'system'),
    ('perm-080', 'system', 'IDENTITY_NUMBERING_CREATE', 'Create Numbering Series', 'identity.numbering', 'create', 'Create numbering series', TRUE, 'system'),
    ('perm-081', 'system', 'IDENTITY_NUMBERING_READ', 'Read Numbering Series', 'identity.numbering', 'read', 'View numbering series', TRUE, 'system'),
    ('perm-082', 'system', 'IDENTITY_NUMBERING_UPDATE', 'Update Numbering Series', 'identity.numbering', 'update', 'Update numbering series', TRUE, 'system'),
    ('perm-083', 'system', 'IDENTITY_NUMBERING_DELETE', 'Delete Numbering Series', 'identity.numbering', 'delete', 'Delete numbering series', TRUE, 'system'),
    ('perm-084', 'system', 'IDENTITY_NUMBERING_LIST', 'List Numbering Series', 'identity.numbering', 'list', 'List all numbering series', TRUE, 'system'),
    ('perm-085', 'system', 'IDENTITY_NUMBERING_GENERATE', 'Generate Number', 'identity.numbering', 'generate', 'Generate next number', TRUE, 'system')
ON DUPLICATE KEY UPDATE id=id;

-- ============================================
-- System Roles
-- ============================================
INSERT INTO roles (id, tenant_id, company_id, code, name, description, role_type, is_system, is_default, priority, created_by)
VALUES 
    ('role-001', 'system', '00000000-0000-0000-0000-000000000001', 'SUPER_ADMIN', 'Super Administrator', 'Full system access across all tenants', 'SYSTEM', TRUE, FALSE, 100, 'system'),
    ('role-002', 'system', '00000000-0000-0000-0000-000000000001', 'COMPANY_ADMIN', 'Company Administrator', 'Full access within a company', 'ADMIN', TRUE, FALSE, 90, 'system'),
    ('role-003', 'system', '00000000-0000-0000-0000-000000000001', 'USER_ADMIN', 'User Administrator', 'User and role management within company', 'ADMIN', TRUE, FALSE, 80, 'system'),
    ('role-004', 'system', '00000000-0000-0000-0000-000000000001', 'MANAGER', 'Manager', 'Department/branch management access', 'MANAGER', TRUE, FALSE, 70, 'system'),
    ('role-005', 'system', '00000000-0000-0000-0000-000000000001', 'OPERATOR', 'Operator', 'Operational task execution', 'OPERATOR', TRUE, FALSE, 50, 'system'),
    ('role-006', 'system', '00000000-0000-0000-0000-000000000001', 'VIEWER', 'Viewer', 'Read-only access', 'VIEWER', TRUE, TRUE, 10, 'system')
ON DUPLICATE KEY UPDATE id=id;

-- ============================================
-- Role-Permission Assignments
-- ============================================
-- SUPER_ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'role-001', id FROM permissions WHERE tenant_id = 'system'
ON DUPLICATE KEY UPDATE role_id=role_id;

-- COMPANY_ADMIN gets company-level permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'role-002', id FROM permissions WHERE tenant_id = 'system' AND code LIKE 'IDENTITY_%' AND code NOT LIKE '%_DELETE'
ON DUPLICATE KEY UPDATE role_id=role_id;

-- USER_ADMIN gets user/role permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'role-003', id FROM permissions WHERE tenant_id = 'system' AND (code LIKE 'IDENTITY_USER_%' OR code LIKE 'IDENTITY_ROLE_%')
ON DUPLICATE KEY UPDATE role_id=role_id;

-- MANAGER gets operational permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'role-004', id FROM permissions WHERE tenant_id = 'system' AND code LIKE 'IDENTITY_%' AND action IN ('read', 'list', 'create', 'update')
ON DUPLICATE KEY UPDATE role_id=role_id;

-- VIEWER gets read-only permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'role-005', id FROM permissions WHERE tenant_id = 'system' AND action IN ('read', 'list')
ON DUPLICATE KEY UPDATE role_id=role_id;

-- ============================================
-- System User (Super Admin)
-- Password: Admin@123 (BCrypt encoded)
-- ============================================
INSERT INTO users (id, tenant_id, company_id, username, password, full_name, email, status, email_verified, created_by)
VALUES (
    'user-001',
    'system',
    '00000000-0000-0000-0000-000000000001',
    'superadmin',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/RK.PZvO.S',
    'Super Administrator',
    'superadmin@erp.brazil.com',
    'ACTIVE',
    TRUE,
    'system'
) ON DUPLICATE KEY UPDATE id=id;

-- Demo User
-- Password: Demo@123 (BCrypt encoded)
INSERT INTO users (id, tenant_id, company_id, username, password, full_name, email, status, email_verified, created_by)
VALUES (
    'user-002',
    'demo',
    '00000000-0000-0000-0000-000000000002',
    'demo',
    '$2a$12$9B5XyKq8vGJ9mN2pQ3rR6uV5wX8yZ1aA3bC4dE5fG6hH7iJ8kL9mN',
    'Demo User',
    'demo@erp.brazil.com',
    'ACTIVE',
    TRUE,
    'system'
) ON DUPLICATE KEY UPDATE id=id;

-- ============================================
-- User-Role Assignments
-- ============================================
-- Super Admin -> SUPER_ADMIN role
INSERT INTO user_company_roles (id, tenant_id, user_id, company_id, role_id, assignment_type, assigned_by)
VALUES (
    'ucr-001',
    'system',
    'user-001',
    '00000000-0000-0000-0000-000000000001',
    'role-001',
    'DIRECT',
    'system'
) ON DUPLICATE KEY UPDATE id=id;

-- Demo User -> COMPANY_ADMIN role
INSERT INTO user_company_roles (id, tenant_id, user_id, company_id, role_id, assignment_type, assigned_by)
VALUES (
    'ucr-002',
    'demo',
    'user-002',
    '00000000-0000-0000-0000-000000000002',
    'role-002',
    'DIRECT',
    'system'
) ON DUPLICATE KEY UPDATE id=id;

-- ============================================
-- Default Branches for Demo Company
-- ============================================
INSERT INTO branches (id, tenant_id, company_id, code, name, trade_name, cnpj, address, city, state, cep, phone, email, status, is_headquarters, created_by)
VALUES (
    'branch-001',
    'demo',
    '00000000-0000-0000-0000-000000000002',
    'MATRIZ',
    'Matriz São Paulo',
    'Matriz',
    '12.345.678/0001-90',
    'Rua das Flores, 123',
    'São Paulo',
    'SP',
    '01234-567',
    '(11) 88888-8888',
    'matriz@demo.com.br',
    'ACTIVE',
    TRUE,
    'system'
) ON DUPLICATE KEY UPDATE id=id;

INSERT INTO branches (id, tenant_id, company_id, code, name, trade_name, cnpj, address, city, state, cep, phone, email, status, is_headquarters, created_by)
VALUES (
    'branch-002',
    'demo',
    '00000000-0000-0000-0000-000000000002',
    'FILIAL01',
    'Filial Rio de Janeiro',
    'Filial RJ',
    '12.345.678/0002-70',
    'Av. Copacabana, 500',
    'Rio de Janeiro',
    'RJ',
    '22010-000',
    '(21) 77777-7777',
    'filial.rj@demo.com.br',
    'ACTIVE',
    FALSE,
    'system'
) ON DUPLICATE KEY UPDATE id=id;

-- ============================================
-- Default Departments for Demo Company
-- ============================================
INSERT INTO departments (id, tenant_id, company_id, branch_id, code, name, description, level, sort_order, is_active, manager_id, created_by)
VALUES 
    ('dept-001', 'demo', '00000000-0000-0000-0000-000000000002', 'branch-001', 'DIR', 'Diretoria', 'Diretoria Geral', 0, 1, TRUE, 'user-002', 'system'),
    ('dept-002', 'demo', '00000000-0000-0000-0000-000000000002', 'branch-001', 'ADM', 'Administrativo', 'Departamento Administrativo', 1, 2, TRUE, NULL, 'system'),
    ('dept-003', 'demo', '00000000-0000-0000-0000-000000000002', 'branch-001', 'FIN', 'Financeiro', 'Departamento Financeiro', 1, 3, TRUE, NULL, 'system'),
    ('dept-004', 'demo', '00000000-0000-0000-0000-000000000002', 'branch-001', 'RH', 'Recursos Humanos', 'Departamento de RH', 1, 4, TRUE, NULL, 'system'),
    ('dept-005', 'demo', '00000000-0000-0000-0000-000000000002', 'branch-001', 'TI', 'Tecnologia da Informação', 'Departamento de TI', 1, 5, TRUE, NULL, 'system'),
    ('dept-006', 'demo', '00000000-0000-0000-0000-000000000002', 'branch-001', 'COM', 'Comercial', 'Departamento Comercial', 1, 6, TRUE, NULL, 'system')
ON DUPLICATE KEY UPDATE id=id;

-- ============================================
-- Default Cost Centers for Demo Company
-- ============================================
INSERT INTO cost_centers (id, tenant_id, company_id, code, name, description, level, sort_order, cost_center_type, is_active, created_by)
VALUES 
    ('cc-001', 'demo', '00000000-0000-0000-0000-000000000002', 'CC001', 'Administração', 'Centro de Custo Administrativo', 0, 1, 'STANDARD', TRUE, 'system'),
    ('cc-002', 'demo', '00000000-0000-0000-0000-000000000002', 'CC002', 'Vendas', 'Centro de Custo de Vendas', 0, 2, 'STANDARD', TRUE, 'system'),
    ('cc-003', 'demo', '00000000-0000-0000-0000-000000000002', 'CC003', 'Produção', 'Centro de Custo de Produção', 0, 3, 'STANDARD', TRUE, 'system'),
    ('cc-004', 'demo', '00000000-0000-0000-0000-000000000002', 'CC004', 'TI', 'Centro de Custo de TI', 0, 4, 'STANDARD', TRUE, 'system'),
    ('cc-005', 'demo', '00000000-0000-0000-0000-000000000002', 'CC005', 'RH', 'Centro de Custo de RH', 0, 5, 'STANDARD', TRUE, 'system')
ON DUPLICATE KEY UPDATE id=id;

-- ============================================
-- Default Numbering Series for Demo Company
-- ============================================
INSERT INTO numbering_series (id, tenant_id, company_id, branch_id, code, description, prefix, suffix, current_number, min_number, max_number, padding_length, format_template, reset_policy, is_active, allows_manual, created_by)
VALUES 
    ('ns-001', 'demo', '00000000-0000-0000-0000-000000000002', NULL, 'NFE', 'Nota Fiscal Eletrônica', 'NFE', '', 0, 1, 999999999, 9, '{prefix}{number:000000000}', 'YEARLY', TRUE, FALSE, 'system'),
    ('ns-002', 'demo', '00000000-0000-0000-0000-000000000002', NULL, 'NFCE', 'Nota Fiscal Consumidor Eletrônica', 'NFC', '', 0, 1, 999999999, 9, '{prefix}{number:000000000}', 'YEARLY', TRUE, FALSE, 'system'),
    ('ns-003', 'demo', '00000000-0000-0000-0000-000000000002', NULL, 'PED', 'Pedido de Venda', 'PED', '', 0, 1, 999999, 6, '{prefix}{number:000000}', 'NEVER', TRUE, FALSE, 'system'),
    ('ns-004', 'demo', '00000000-0000-0000-0000-000000000002', NULL, 'OS', 'Ordem de Serviço', 'OS', '', 0, 1, 999999, 6, '{prefix}{number:000000}', 'NEVER', TRUE, FALSE, 'system'),
    ('ns-005', 'demo', '00000000-0000-0000-0000-000000000002', NULL, 'CTR', 'Contrato', 'CTR', '', 0, 1, 99999, 5, '{prefix}{number:00000}', 'NEVER', TRUE, FALSE, 'system')
ON DUPLICATE KEY UPDATE id=id;

SET FOREIGN_KEY_CHECKS = 1;