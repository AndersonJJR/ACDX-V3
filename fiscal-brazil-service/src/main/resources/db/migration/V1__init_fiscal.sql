-- V1__init_fiscal.sql
-- Initial fiscal schema for Brazilian tax compliance
-- Compatible with MySQL 8.0+

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- NCM (Nomenclatura Comum do Mercosul)
-- ============================================
CREATE TABLE fiscal_ncm (
    code VARCHAR(8) NOT NULL PRIMARY KEY,
    description VARCHAR(500) NOT NULL,
    chapter VARCHAR(2),
    position VARCHAR(4),
    subposition VARCHAR(6),
    item VARCHAR(8),
    is_active BOOLEAN DEFAULT TRUE,
    version VARCHAR(20),
    last_sync DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_ncm_chapter (chapter),
    INDEX idx_ncm_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- CEST (Código Especificador da Substituição Tributária)
-- ============================================
CREATE TABLE fiscal_cest (
    code VARCHAR(7) NOT NULL PRIMARY KEY,
    description VARCHAR(500) NOT NULL,
    segment VARCHAR(100),
    ncm_code VARCHAR(8),
    is_active BOOLEAN DEFAULT TRUE,
    version VARCHAR(20),
    last_sync DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_cest_ncm (ncm_code),
    INDEX idx_cest_segment (segment),
    INDEX idx_cest_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- CFOP (Código Fiscal de Operações e Prestações)
-- ============================================
CREATE TABLE fiscal_cfop (
    code VARCHAR(4) NOT NULL PRIMARY KEY,
    description VARCHAR(500) NOT NULL,
    type VARCHAR(20),
    category VARCHAR(50),
    generates_icms BOOLEAN DEFAULT TRUE,
    generates_ipi BOOLEAN DEFAULT FALSE,
    generates_pis_cofins BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    version VARCHAR(20),
    last_sync DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_cfop_type (type),
    INDEX idx_cfop_category (category),
    INDEX idx_cfop_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Tax Rules (base table for inheritance)
-- ============================================
CREATE TABLE fiscal_tax_rule (
    id VARCHAR(50) NOT NULL PRIMARY KEY,
    tax_type VARCHAR(20) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    uf_origin VARCHAR(2),
    uf_destination VARCHAR(2),
    ncm_code VARCHAR(8),
    cest_code VARCHAR(7),
    cfop_code VARCHAR(4),
    tax_regime VARCHAR(20),
    effective_from DATE NOT NULL,
    effective_to DATE,
    priority INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    version VARCHAR(20),
    last_sync DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tax_rule_type (tax_type),
    INDEX idx_tax_rule_ncm (ncm_code),
    INDEX idx_tax_rule_cest (cest_code),
    INDEX idx_tax_rule_cfop (cfop_code),
    INDEX idx_tax_rule_uf_origin (uf_origin),
    INDEX idx_tax_rule_uf_dest (uf_destination),
    INDEX idx_tax_rule_regime (tax_regime),
    INDEX idx_tax_rule_effective (effective_from, effective_to),
    INDEX idx_tax_rule_active (is_active),
    INDEX idx_tax_rule_priority (priority DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- ICMS Rules
-- ============================================
CREATE TABLE fiscal_icms_rule (
    id VARCHAR(50) NOT NULL PRIMARY KEY,
    modalidade_bc VARCHAR(2),
    modalidade_bc_st VARCHAR(2),
    origem VARCHAR(1),
    cst VARCHAR(3),
    csosn VARCHAR(3),
    aliquota DECIMAL(5,2),
    aliquota_st DECIMAL(5,2),
    aliquota_fcp DECIMAL(5,2),
    aliquota_fcp_st DECIMAL(5,2),
    reducao_bc DECIMAL(5,2),
    reducao_bc_st DECIMAL(5,2),
    mva_ajustado DECIMAL(5,2),
    pauta_fiscal DECIMAL(15,2),
    is_st BOOLEAN DEFAULT FALSE,
    is_fcp BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (id) REFERENCES fiscal_tax_rule(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- IPI Rules
-- ============================================
CREATE TABLE fiscal_ipi_rule (
    id VARCHAR(50) NOT NULL PRIMARY KEY,
    cst VARCHAR(2),
    aliquota DECIMAL(5,2),
    valor_fixo DECIMAL(15,4),
    unidade VARCHAR(10),
    calculation_type VARCHAR(20),
    FOREIGN KEY (id) REFERENCES fiscal_tax_rule(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- PIS Rules
-- ============================================
CREATE TABLE fiscal_pis_rule (
    id VARCHAR(50) NOT NULL PRIMARY KEY,
    cst VARCHAR(2),
    aliquota DECIMAL(5,4),
    valor_aliquota DECIMAL(15,4),
    calculation_type VARCHAR(20),
    FOREIGN KEY (id) REFERENCES fiscal_tax_rule(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- COFINS Rules
-- ============================================
CREATE TABLE fiscal_cofins_rule (
    id VARCHAR(50) NOT NULL PRIMARY KEY,
    cst VARCHAR(2),
    aliquota DECIMAL(5,4),
    valor_aliquota DECIMAL(15,4),
    calculation_type VARCHAR(20),
    FOREIGN KEY (id) REFERENCES fiscal_tax_rule(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- ISS Rules
-- ============================================
CREATE TABLE fiscal_iss_rule (
    id VARCHAR(50) NOT NULL PRIMARY KEY,
    municipality_code VARCHAR(7),
    service_code VARCHAR(10),
    cst VARCHAR(2),
    aliquota DECIMAL(5,2),
    deduction DECIMAL(15,2),
    is_exempt BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (id) REFERENCES fiscal_tax_rule(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- NF-e (Nota Fiscal Eletrônica)
-- ============================================
CREATE TABLE fiscal_nfe (
    id VARCHAR(50) NOT NULL PRIMARY KEY,
    company_id VARCHAR(50) NOT NULL,
    access_key VARCHAR(44) UNIQUE NOT NULL,
    protocol VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    type VARCHAR(10) NOT NULL,
    series VARCHAR(3),
    number VARCHAR(9),
    emission_date DATETIME NOT NULL,
    sa_entrada_date DATETIME,
    emitter_json JSON NOT NULL,
    recipient_json JSON NOT NULL,
    items_json JSON NOT NULL,
    totals_json JSON NOT NULL,
    transport_json JSON,
    payment_json JSON,
    additional_info TEXT,
    xml_content LONGTEXT,
    pdf_content LONGTEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    cancelled_at DATETIME,
    cancellation_reason VARCHAR(500),
    INDEX idx_nfe_company (company_id),
    INDEX idx_nfe_access_key (access_key),
    INDEX idx_nfe_protocol (protocol),
    INDEX idx_nfe_status (status),
    INDEX idx_nfe_emission_date (emission_date),
    INDEX idx_nfe_company_date (company_id, emission_date),
    INDEX idx_nfe_series_number (company_id, series, number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- NF-e Events
-- ============================================
CREATE TABLE fiscal_nfe_event (
    id VARCHAR(50) NOT NULL PRIMARY KEY,
    nfe_id VARCHAR(50) NOT NULL,
    access_key VARCHAR(44) NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    protocol VARCHAR(50),
    event_date DATETIME NOT NULL,
    company_id VARCHAR(50) NOT NULL,
    justification VARCHAR(500),
    xml_content LONGTEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (nfe_id) REFERENCES fiscal_nfe(id) ON DELETE CASCADE,
    INDEX idx_nfe_event_nfe (nfe_id),
    INDEX idx_nfe_event_access_key (access_key),
    INDEX idx_nfe_event_type (event_type),
    INDEX idx_nfe_event_date (event_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- SPED Blocks
-- ============================================
CREATE TABLE fiscal_sped (
    id VARCHAR(50) NOT NULL PRIMARY KEY,
    company_id VARCHAR(50) NOT NULL,
    period VARCHAR(6) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    version VARCHAR(10),
    profile VARCHAR(10),
    layout VARCHAR(10),
    scenario VARCHAR(10),
    file_path VARCHAR(500),
    file_hash VARCHAR(64),
    generated_at DATETIME,
    validated_at DATETIME,
    submitted_at DATETIME,
    error_message TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_sped_company (company_id),
    INDEX idx_sped_period (period),
    INDEX idx_sped_type (type),
    INDEX idx_sped_status (status),
    INDEX idx_sped_company_period (company_id, period),
    INDEX idx_sped_dates (start_date, end_date),
    UNIQUE KEY uk_sped_company_period_type (company_id, period, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- SPED Registers (detail lines)
-- ============================================
CREATE TABLE fiscal_sped_register (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    sped_id VARCHAR(50) NOT NULL,
    register_code VARCHAR(10) NOT NULL,
    parent_register VARCHAR(10),
    line_number INT NOT NULL,
    content TEXT NOT NULL,
    is_valid BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sped_id) REFERENCES fiscal_sped(id) ON DELETE CASCADE,
    INDEX idx_sped_reg_sped (sped_id),
    INDEX idx_sped_reg_code (register_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Certificado Digital A1
-- ============================================
CREATE TABLE fiscal_certificado_a1 (
    id VARCHAR(50) NOT NULL PRIMARY KEY,
    company_id VARCHAR(50) NOT NULL,
    alias VARCHAR(100) NOT NULL,
    pfx_encrypted LONGTEXT NOT NULL,
    password_encrypted VARCHAR(500) NOT NULL,
    cnpj VARCHAR(14) NOT NULL,
    company_name VARCHAR(200) NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    serial_number VARCHAR(100),
    issuer VARCHAR(200),
    subject VARCHAR(500),
    thumbprint VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_used_at DATETIME,
    renewal_alert_days INT DEFAULT 30,
    INDEX idx_cert_company (company_id),
    INDEX idx_cert_cnpj (cnpj),
    INDEX idx_cert_expiry (expiry_date),
    INDEX idx_cert_status (status),
    INDEX idx_cert_thumbprint (thumbprint),
    UNIQUE KEY uk_cert_company_cnpj (company_id, cnpj)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Integração Fiscal
-- ============================================
CREATE TABLE fiscal_integracao (
    id VARCHAR(50) NOT NULL PRIMARY KEY,
    company_id VARCHAR(50) NOT NULL,
    type VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    config_json JSON NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIGURING',
    environment VARCHAR(20) NOT NULL,
    endpoint_url VARCHAR(500),
    certificate_id VARCHAR(50),
    last_sync DATETIME,
    last_sync_status VARCHAR(50),
    last_error TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_int_company (company_id),
    INDEX idx_int_type (type),
    INDEX idx_int_status (status),
    INDEX idx_int_certificate (certificate_id),
    UNIQUE KEY uk_int_company_type_name (company_id, type, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Retenção na Fonte
-- ============================================
CREATE TABLE fiscal_retencao_fonte (
    id VARCHAR(50) NOT NULL PRIMARY KEY,
    company_id VARCHAR(50) NOT NULL,
    document_id VARCHAR(50) NOT NULL,
    document_type VARCHAR(20) NOT NULL,
    competence_date DATE NOT NULL,
    provider_cnpj_cpf VARCHAR(14) NOT NULL,
    provider_name VARCHAR(200) NOT NULL,
    base_value DECIMAL(15,2) NOT NULL,
    calculated_value DECIMAL(15,2) NOT NULL,
    type VARCHAR(10) NOT NULL,
    aliquota DECIMAL(5,4),
    deducao DECIMAL(15,2),
    nature_code VARCHAR(20),
    contribution_code VARCHAR(10),
    calculated_at DATETIME NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ret_company (company_id),
    INDEX idx_ret_document (document_id),
    INDEX idx_ret_provider (provider_cnpj_cpf),
    INDEX idx_ret_competence (competence_date),
    INDEX idx_ret_type (type),
    INDEX idx_ret_company_competence (company_id, competence_date),
    INDEX idx_ret_calculated (calculated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Quartz Scheduler Tables (for clustering)
-- ============================================
CREATE TABLE QRTZ_JOB_DETAILS (
    SCHED_NAME VARCHAR(120) NOT NULL,
    JOB_NAME VARCHAR(200) NOT NULL,
    JOB_GROUP VARCHAR(200) NOT NULL,
    DESCRIPTION VARCHAR(250) NULL,
    JOB_CLASS_NAME VARCHAR(250) NOT NULL,
    IS_DURABLE VARCHAR(1) NOT NULL,
    IS_NONCONCURRENT VARCHAR(1) NOT NULL,
    IS_UPDATE_DATA VARCHAR(1) NOT NULL,
    REQUESTS_RECOVERY VARCHAR(1) NOT NULL,
    JOB_DATA BLOB NULL,
    PRIMARY KEY (SCHED_NAME,JOB_NAME,JOB_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE QRTZ_TRIGGERS (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    JOB_NAME VARCHAR(200) NOT NULL,
    JOB_GROUP VARCHAR(200) NOT NULL,
    DESCRIPTION VARCHAR(250) NULL,
    NEXT_FIRE_TIME BIGINT(13) NULL,
    PREV_FIRE_TIME BIGINT(13) NULL,
    PRIORITY INTEGER NULL,
    TRIGGER_STATE VARCHAR(16) NOT NULL,
    TRIGGER_TYPE VARCHAR(8) NOT NULL,
    START_TIME BIGINT(13) NOT NULL,
    END_TIME BIGINT(13) NULL,
    CALENDAR_NAME VARCHAR(200) NULL,
    MISFIRE_INSTR SMALLINT(2) NULL,
    JOB_DATA BLOB NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,JOB_NAME,JOB_GROUP) REFERENCES QRTZ_JOB_DETAILS(SCHED_NAME,JOB_NAME,JOB_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE QRTZ_SIMPLE_TRIGGERS (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    REPEAT_COUNT BIGINT(7) NOT NULL,
    REPEAT_INTERVAL BIGINT(12) NOT NULL,
    TIMES_TRIGGERED BIGINT(10) NOT NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP) REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE QRTZ_CRON_TRIGGERS (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    CRON_EXPRESSION VARCHAR(200) NOT NULL,
    TIME_ZONE_ID VARCHAR(80),
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP) REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE QRTZ_SIMPROP_TRIGGERS (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    STR_PROP_1 VARCHAR(512) NULL,
    STR_PROP_2 VARCHAR(512) NULL,
    STR_PROP_3 VARCHAR(512) NULL,
    INT_PROP_1 INT NULL,
    INT_PROP_2 INT NULL,
    LONG_PROP_1 BIGINT NULL,
    LONG_PROP_2 BIGINT NULL,
    DEC_PROP_1 NUMERIC(13,4) NULL,
    DEC_PROP_2 NUMERIC(13,4) NULL,
    BOOL_PROP_1 VARCHAR(1) NULL,
    BOOL_PROP_2 VARCHAR(1) NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP) REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE QRTZ_BLOB_TRIGGERS (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    BLOB_DATA BLOB NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP) REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE QRTZ_CALENDARS (
    SCHED_NAME VARCHAR(120) NOT NULL,
    CALENDAR_NAME VARCHAR(200) NOT NULL,
    CALENDAR BLOB NOT NULL,
    PRIMARY KEY (SCHED_NAME,CALENDAR_NAME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE QRTZ_PAUSED_TRIGGER_GRPS (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE QRTZ_FIRED_TRIGGERS (
    SCHED_NAME VARCHAR(120) NOT NULL,
    ENTRY_ID VARCHAR(95) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    INSTANCE_NAME VARCHAR(200) NOT NULL,
    FIRED_TIME BIGINT(13) NOT NULL,
    SCHED_TIME BIGINT(13) NOT NULL,
    PRIORITY INTEGER NOT NULL,
    STATE VARCHAR(16) NOT NULL,
    JOB_NAME VARCHAR(200) NULL,
    JOB_GROUP VARCHAR(200) NULL,
    IS_NONCONCURRENT VARCHAR(1) NULL,
    REQUESTS_RECOVERY VARCHAR(1) NULL,
    PRIMARY KEY (SCHED_NAME,ENTRY_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE QRTZ_SCHEDULER_STATE (
    SCHED_NAME VARCHAR(120) NOT NULL,
    INSTANCE_NAME VARCHAR(200) NOT NULL,
    LAST_CHECKIN_TIME BIGINT(13) NOT NULL,
    CHECKIN_INTERVAL BIGINT(13) NOT NULL,
    PRIMARY KEY (SCHED_NAME,INSTANCE_NAME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE QRTZ_LOCKS (
    SCHED_NAME VARCHAR(120) NOT NULL,
    LOCK_NAME VARCHAR(40) NOT NULL,
    PRIMARY KEY (SCHED_NAME,LOCK_NAME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;