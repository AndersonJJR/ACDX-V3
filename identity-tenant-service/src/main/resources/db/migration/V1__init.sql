-- Flyway Migration V1: Initial Schema for Identity & Tenant Service
-- Compatible with MySQL 8.4

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- Companies (Tenants)
-- ============================================
CREATE TABLE IF NOT EXISTS companies (
    id CHAR(36) NOT NULL PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    trade_name VARCHAR(200),
    cnpj VARCHAR(18) NOT NULL,
    state_registration VARCHAR(20),
    municipal_registration VARCHAR(20),
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(2),
    cep VARCHAR(10),
    phone VARCHAR(100),
    email VARCHAR(200),
    website VARCHAR(200),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    settings TEXT,
    logo_url VARCHAR(500),
    primary_color VARCHAR(7),
    secondary_color VARCHAR(7),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME(6),
    deleted_by VARCHAR(100),
    
    UNIQUE KEY uk_company_cnpj (cnpj),
    UNIQUE KEY uk_company_code (code),
    KEY idx_company_status (status),
    KEY idx_company_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add tenant_id column to companies (for multi-tenancy)
ALTER TABLE companies ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(100) NOT NULL DEFAULT 'default' AFTER id;

-- ============================================
-- Users
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id CHAR(36) NOT NULL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    company_id VARCHAR(100) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(200) NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    cpf VARCHAR(14),
    email VARCHAR(200) NOT NULL,
    phone VARCHAR(20),
    mobile_phone VARCHAR(20),
    avatar_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    two_factor_secret VARCHAR(100),
    last_login_at DATETIME(6),
    last_login_ip VARCHAR(50),
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until DATETIME(6),
    password_changed_at DATETIME(6),
    must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
    preferences TEXT,
    language VARCHAR(100) NOT NULL DEFAULT 'pt-BR',
    timezone VARCHAR(50) NOT NULL DEFAULT 'America/Sao_Paulo',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME(6),
    deleted_by VARCHAR(100),
    
    UNIQUE KEY uk_user_tenant_username (tenant_id, username),
    UNIQUE KEY uk_user_tenant_email (tenant_id, email),
    UNIQUE KEY uk_user_tenant_cpf (tenant_id, cpf),
    KEY idx_user_company (company_id),
    KEY idx_user_status (status),
    KEY idx_user_email (email),
    KEY idx_user_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Roles
-- ============================================
CREATE TABLE IF NOT EXISTS roles (
    id CHAR(36) NOT NULL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    company_id VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    role_type VARCHAR(20) NOT NULL DEFAULT 'CUSTOM',
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    priority INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME(6),
    deleted_by VARCHAR(100),
    
    UNIQUE KEY uk_role_tenant_code (tenant_id, code),
    KEY idx_role_company (company_id),
    KEY idx_role_type (role_type),
    KEY idx_role_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Permissions
-- ============================================
CREATE TABLE IF NOT EXISTS permissions (
    id CHAR(36) NOT NULL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    resource VARCHAR(200) NOT NULL,
    action VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME(6),
    deleted_by VARCHAR(100),
    
    UNIQUE KEY uk_permission_tenant_code (tenant_id, code),
    KEY idx_permission_resource (resource),
    KEY idx_permission_action (action),
    KEY idx_permission_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Role-Permission Many-to-Many
-- ============================================
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id CHAR(36) NOT NULL,
    permission_id CHAR(36) NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    KEY idx_rp_permission (permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- User-Company-Roles
-- ============================================
CREATE TABLE IF NOT EXISTS user_company_roles (
    id CHAR(36) NOT NULL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    company_id VARCHAR(100) NOT NULL,
    role_id CHAR(36) NOT NULL,
    branch_id VARCHAR(100),
    department_id VARCHAR(100),
    assignment_type VARCHAR(20) NOT NULL DEFAULT 'DIRECT',
    valid_from DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    valid_until DATETIME(6),
    assigned_by VARCHAR(100),
    notes VARCHAR(500),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME(6),
    deleted_by VARCHAR(100),
    
    UNIQUE KEY uk_ucr_tenant_user_company_role_branch (tenant_id, user_id, company_id, role_id, branch_id),
    KEY idx_ucr_user (user_id),
    KEY idx_ucr_company (company_id),
    KEY idx_ucr_role (role_id),
    KEY idx_ucr_branch (branch_id),
    KEY idx_ucr_validity (valid_from, valid_until),
    KEY idx_ucr_tenant (tenant_id),
    CONSTRAINT fk_ucr_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ucr_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Branches (Filiais)
-- ============================================
CREATE TABLE IF NOT EXISTS branches (
    id CHAR(36) NOT NULL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    company_id VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    trade_name VARCHAR(200),
    cnpj VARCHAR(18),
    state_registration VARCHAR(20),
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(2),
    cep VARCHAR(10),
    phone VARCHAR(100),
    email VARCHAR(200),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_headquarters BOOLEAN NOT NULL DEFAULT FALSE,
    opening_date DATE,
    settings TEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME(6),
    deleted_by VARCHAR(100),
    
    UNIQUE KEY uk_branch_tenant_code (tenant_id, company_id, code),
    UNIQUE KEY uk_branch_tenant_cnpj (tenant_id, cnpj),
    KEY idx_branch_company (company_id),
    KEY idx_branch_status (status),
    KEY idx_branch_cnpj (cnpj),
    KEY idx_branch_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Departments
-- ============================================
CREATE TABLE IF NOT EXISTS departments (
    id CHAR(36) NOT NULL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    company_id VARCHAR(100) NOT NULL,
    branch_id VARCHAR(100),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    parent_id VARCHAR(100),
    level INT NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    manager_id VARCHAR(100),
    settings TEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME(6),
    deleted_by VARCHAR(100),
    
    UNIQUE KEY uk_dept_tenant_code (tenant_id, company_id, branch_id, code),
    KEY idx_dept_company (company_id),
    KEY idx_dept_branch (branch_id),
    KEY idx_dept_parent (parent_id),
    KEY idx_dept_status (is_active),
    KEY idx_dept_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Cost Centers
-- ============================================
CREATE TABLE IF NOT EXISTS cost_centers (
    id CHAR(36) NOT NULL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    company_id VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    parent_id VARCHAR(100),
    level INT NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    cost_center_type VARCHAR(20) NOT NULL DEFAULT 'STANDARD',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    manager_id VARCHAR(100),
    budget_owner_id VARCHAR(100),
    budget_amount DECIMAL(19,2),
    budget_period_start DATE,
    budget_period_end DATE,
    settings TEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME(6),
    deleted_by VARCHAR(100),
    
    UNIQUE KEY uk_cc_tenant_code (tenant_id, company_id, code),
    KEY idx_cc_company (company_id),
    KEY idx_cc_parent (parent_id),
    KEY idx_cc_status (is_active),
    KEY idx_cc_type (cost_center_type),
    KEY idx_cc_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Numbering Series
-- ============================================
CREATE TABLE IF NOT EXISTS numbering_series (
    id CHAR(36) NOT NULL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    company_id VARCHAR(100) NOT NULL,
    branch_id VARCHAR(100),
    code VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    prefix VARCHAR(20) DEFAULT '',
    suffix VARCHAR(20) DEFAULT '',
    current_number BIGINT NOT NULL DEFAULT 0,
    min_number BIGINT NOT NULL DEFAULT 1,
    max_number BIGINT,
    padding_length INT NOT NULL DEFAULT 6,
    format_template VARCHAR(100) DEFAULT '{prefix}{number:000000}{suffix}',
    reset_policy VARCHAR(20) NOT NULL DEFAULT 'NEVER',
    last_reset_at DATETIME(6),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    allows_manual BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME(6),
    deleted_by VARCHAR(100),
    
    UNIQUE KEY uk_ns_tenant_code (tenant_id, company_id, branch_id, code),
    KEY idx_ns_tenant_company (tenant_id, company_id),
    KEY idx_ns_code (code),
    KEY idx_ns_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Outbox Events (Transactional Outbox Pattern)
-- ============================================
CREATE TABLE IF NOT EXISTS outbox_events (
    id CHAR(36) NOT NULL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(200) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    payload JSON NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    last_error TEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    published_at DATETIME(6),
    correlation_id VARCHAR(100),
    causation_id VARCHAR(100),
    
    KEY idx_outbox_status_created (status, created_at),
    KEY idx_outbox_aggregate (aggregate_type, aggregate_id),
    KEY idx_outbox_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Boot Info
-- ============================================
CREATE TABLE IF NOT EXISTS boot_info (
    id VARCHAR(100) NOT NULL PRIMARY KEY,
    app_name VARCHAR(100) NOT NULL DEFAULT 'ERP Brazil',
    app_version VARCHAR(50) NOT NULL DEFAULT '1.0.0',
    build_number VARCHAR(50),
    build_date DATETIME(6),
    api_base_url VARCHAR(500),
    auth_base_url VARCHAR(500),
    cdn_base_url VARCHAR(500),
    default_language VARCHAR(10) NOT NULL DEFAULT 'pt-BR',
    supported_languages VARCHAR(500) NOT NULL DEFAULT 'pt-BR,en-US,es-ES',
    default_timezone VARCHAR(50) NOT NULL DEFAULT 'America/Sao_Paulo',
    supported_currencies VARCHAR(500) NOT NULL DEFAULT 'BRL,USD,EUR',
    features TEXT,
    modules TEXT,
    maintenance_mode BOOLEAN NOT NULL DEFAULT FALSE,
    maintenance_message VARCHAR(1000),
    session_timeout_minutes INT NOT NULL DEFAULT 480,
    max_file_upload_mb INT NOT NULL DEFAULT 50,
    allowed_file_types VARCHAR(500) NOT NULL DEFAULT 'pdf,xlsx,csv,xml,jpg,png',
    updated_at DATETIME(6),
    updated_by VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default boot info
INSERT INTO boot_info (id) VALUES ('system') ON DUPLICATE KEY UPDATE id=id;

-- ============================================
-- Quartz Scheduler Tables
-- ============================================
CREATE TABLE IF NOT EXISTS QRTZ_JOB_DETAILS (
    SCHED_NAME VARCHAR(120) NOT NULL,
    JOB_NAME VARCHAR(200) NOT NULL,
    JOB_GROUP VARCHAR(200) NOT NULL,
    DESCRIPTION VARCHAR(250),
    JOB_CLASS_NAME VARCHAR(250) NOT NULL,
    IS_DURABLE VARCHAR(1) NOT NULL,
    IS_NONCONCURRENT VARCHAR(1) NOT NULL,
    IS_UPDATE_DATA VARCHAR(1) NOT NULL,
    REQUESTS_RECOVERY VARCHAR(1) NOT NULL,
    JOB_DATA BLOB,
    PRIMARY KEY (SCHED_NAME, JOB_NAME, JOB_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS QRTZ_TRIGGERS (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    JOB_NAME VARCHAR(200) NOT NULL,
    JOB_GROUP VARCHAR(200) NOT NULL,
    DESCRIPTION VARCHAR(250),
    NEXT_FIRE_TIME BIGINT,
    PREV_FIRE_TIME BIGINT,
    PRIORITY INTEGER,
    TRIGGER_STATE VARCHAR(16) NOT NULL,
    TRIGGER_TYPE VARCHAR(8) NOT NULL,
    START_TIME BIGINT NOT NULL,
    END_TIME BIGINT,
    CALENDAR_NAME VARCHAR(200),
    MISFIRE_INSTR SMALLINT,
    JOB_DATA BLOB,
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME, JOB_NAME, JOB_GROUP) REFERENCES QRTZ_JOB_DETAILS(SCHED_NAME, JOB_NAME, JOB_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS QRTZ_SIMPLE_TRIGGERS (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    REPEAT_COUNT BIGINT NOT NULL,
    REPEAT_INTERVAL BIGINT NOT NULL,
    TIMES_TRIGGERED BIGINT NOT NULL,
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) REFERENCES QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS QRTZ_CRON_TRIGGERS (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    CRON_EXPRESSION VARCHAR(120) NOT NULL,
    TIME_ZONE_ID VARCHAR(80),
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) REFERENCES QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS QRTZ_SIMPROP_TRIGGERS (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    STR_PROP_1 VARCHAR(512),
    STR_PROP_2 VARCHAR(512),
    STR_PROP_3 VARCHAR(512),
    INT_PROP_1 INT,
    INT_PROP_2 INT,
    LONG_PROP_1 BIGINT,
    LONG_PROP_2 BIGINT,
    DEC_PROP_1 NUMERIC(13,4),
    DEC_PROP_2 NUMERIC(13,4),
    BOOL_PROP_1 VARCHAR(1),
    BOOL_PROP_2 VARCHAR(1),
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) REFERENCES QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS QRTZ_BLOB_TRIGGERS (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    BLOB_DATA BLOB,
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) REFERENCES QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS QRTZ_CALENDARS (
    SCHED_NAME VARCHAR(120) NOT NULL,
    CALENDAR_NAME VARCHAR(200) NOT NULL,
    CALENDAR BLOB NOT NULL,
    PRIMARY KEY (SCHED_NAME, CALENDAR_NAME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS QRTZ_PAUSED_TRIGGER_GRPS (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    PRIMARY KEY (SCHED_NAME, TRIGGER_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS QRTZ_FIRED_TRIGGERS (
    SCHED_NAME VARCHAR(120) NOT NULL,
    ENTRY_ID VARCHAR(95) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    INSTANCE_NAME VARCHAR(200) NOT NULL,
    FIRED_TIME BIGINT NOT NULL,
    SCHED_TIME BIGINT NOT NULL,
    PRIORITY INTEGER NOT NULL,
    STATE VARCHAR(16) NOT NULL,
    JOB_NAME VARCHAR(200),
    JOB_GROUP VARCHAR(200),
    IS_NONCONCURRENT VARCHAR(1),
    REQUESTS_RECOVERY VARCHAR(1),
    PRIMARY KEY (SCHED_NAME, ENTRY_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS QRTZ_SCHEDULER_STATE (
    SCHED_NAME VARCHAR(120) NOT NULL,
    INSTANCE_NAME VARCHAR(200) NOT NULL,
    LAST_CHECKIN_TIME BIGINT NOT NULL,
    CHECKIN_INTERVAL BIGINT NOT NULL,
    PRIMARY KEY (SCHED_NAME, INSTANCE_NAME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS QRTZ_LOCKS (
    SCHED_NAME VARCHAR(120) NOT NULL,
    LOCK_NAME VARCHAR(40) NOT NULL,
    PRIMARY KEY (SCHED_NAME, LOCK_NAME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;