-- V1__init_sales.sql
-- Initial schema for Sales & CRM Service

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Territories table
CREATE TABLE territories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    state VARCHAR(2),
    cities TEXT,
    zip_code_ranges TEXT,
    active BOOLEAN DEFAULT true,
    parent_territory_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_territory_code_company UNIQUE (code, company_id),
    CONSTRAINT fk_territory_parent FOREIGN KEY (parent_territory_id) REFERENCES territories(id)
);

CREATE INDEX idx_territory_company ON territories(company_id);
CREATE INDEX idx_territory_parent ON territories(parent_territory_id);

-- Sales Teams table
CREATE TABLE sales_teams (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    manager_id UUID,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_sales_team_code_company UNIQUE (code, company_id)
);

CREATE INDEX idx_sales_team_company ON sales_teams(company_id);
CREATE INDEX idx_sales_team_manager ON sales_teams(manager_id);

-- Sales Team Members (many-to-many)
CREATE TABLE sales_team_members (
    sales_team_id UUID NOT NULL,
    sales_person_id UUID NOT NULL,
    PRIMARY KEY (sales_team_id, sales_person_id),
    CONSTRAINT fk_stm_team FOREIGN KEY (sales_team_id) REFERENCES sales_teams(id) ON DELETE CASCADE,
    CONSTRAINT fk_stm_person FOREIGN KEY (sales_person_id) REFERENCES sales_persons(id) ON DELETE CASCADE
);

-- Sales Persons table
CREATE TABLE sales_persons (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL,
    employee_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(100),
    full_name VARCHAR(200) NOT NULL,
    email VARCHAR(150),
    phone VARCHAR(20),
    mobile VARCHAR(20),
    commission_rate DECIMAL(5,2) DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    hire_date DATE,
    termination_date DATE,
    territory_id UUID,
    sales_team_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_sales_person_employee_company UNIQUE (employee_id, company_id),
    CONSTRAINT uk_sales_person_user UNIQUE (user_id),
    CONSTRAINT fk_sales_person_territory FOREIGN KEY (territory_id) REFERENCES territories(id),
    CONSTRAINT fk_sales_person_team FOREIGN KEY (sales_team_id) REFERENCES sales_teams(id)
);

CREATE INDEX idx_sales_person_company ON sales_persons(company_id);
CREATE INDEX idx_sales_person_territory ON sales_persons(territory_id);
CREATE INDEX idx_sales_person_team ON sales_persons(sales_team_id);

-- Sales Pipelines table
CREATE TABLE sales_pipelines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    is_default BOOLEAN DEFAULT false,
    sales_team_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_pipeline_code_company UNIQUE (code, company_id),
    CONSTRAINT fk_pipeline_team FOREIGN KEY (sales_team_id) REFERENCES sales_teams(id)
);

CREATE INDEX idx_pipeline_company ON sales_pipelines(company_id);
CREATE INDEX idx_pipeline_team ON sales_pipelines(sales_team_id);

-- Pipeline Stages table
CREATE TABLE pipeline_stages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pipeline_id UUID NOT NULL,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    order_index INTEGER NOT NULL,
    type VARCHAR(20) NOT NULL,
    probability INTEGER DEFAULT 0,
    color VARCHAR(7) DEFAULT '#6366f1',
    is_active BOOLEAN DEFAULT true,
    max_days_in_stage INTEGER,
    allow_skip BOOLEAN DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_stage_pipeline FOREIGN KEY (pipeline_id) REFERENCES sales_pipelines(id) ON DELETE CASCADE,
    CONSTRAINT uk_stage_pipeline_order UNIQUE (pipeline_id, order_index)
);

CREATE INDEX idx_stage_pipeline ON pipeline_stages(pipeline_id);

-- Customers table
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL,
    customer_code VARCHAR(20) NOT NULL,
    trade_name VARCHAR(200),
    legal_name VARCHAR(200) NOT NULL,
    customer_type VARCHAR(20) NOT NULL,
    document_type VARCHAR(4) NOT NULL,
    document_number VARCHAR(18) NOT NULL,
    state_registration VARCHAR(20),
    municipal_registration VARCHAR(20),
    email VARCHAR(150),
    phone VARCHAR(20),
    website VARCHAR(200),
    credit_limit DECIMAL(15,2) DEFAULT 0,
    payment_terms_days INTEGER DEFAULT 30,
    payment_term_id UUID,
    price_list_id UUID,
    is_active BOOLEAN DEFAULT true,
    is_blocked BOOLEAN DEFAULT false,
    block_reason TEXT,
    notes TEXT,
    territory_id UUID,
    sales_person_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_customer_code_company UNIQUE (customer_code, company_id),
    CONSTRAINT uk_customer_document UNIQUE (document_number),
    CONSTRAINT fk_customer_territory FOREIGN KEY (territory_id) REFERENCES territories(id),
    CONSTRAINT fk_customer_sales_person FOREIGN KEY (sales_person_id) REFERENCES sales_persons(id)
);

CREATE INDEX idx_customer_company ON customers(company_id);
CREATE INDEX idx_customer_document ON customers(document_number);
CREATE INDEX idx_customer_territory ON customers(territory_id);
CREATE INDEX idx_customer_sales_person ON customers(sales_person_id);

-- Contacts table
CREATE TABLE contacts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    email VARCHAR(150),
    phone VARCHAR(20),
    mobile VARCHAR(20),
    position VARCHAR(100),
    department VARCHAR(100),
    is_primary BOOLEAN DEFAULT false,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_contact_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

CREATE INDEX idx_contact_customer ON contacts(customer_id);

-- Addresses table
CREATE TABLE addresses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID NOT NULL,
    address_type VARCHAR(20) NOT NULL,
    zip_code VARCHAR(10) NOT NULL,
    street VARCHAR(200) NOT NULL,
    number VARCHAR(20),
    complement VARCHAR(100),
    neighborhood VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(2) NOT NULL,
    country VARCHAR(2) DEFAULT 'BR',
    ibge_code VARCHAR(7),
    is_default BOOLEAN DEFAULT false,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_address_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

CREATE INDEX idx_address_customer ON addresses(customer_id);
CREATE INDEX idx_address_customer_type ON addresses(customer_id, address_type);

-- Leads table
CREATE TABLE leads (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL,
    lead_code VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    source VARCHAR(50),
    score INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'NEW',
    estimated_value DECIMAL(15,2),
    probability INTEGER DEFAULT 0,
    expected_close_date TIMESTAMP,
    qualified_at TIMESTAMP,
    converted_at TIMESTAMP,
    lost_reason TEXT,
    notes TEXT,
    customer_id UUID,
    contact_id UUID,
    territory_id UUID,
    assigned_sales_person_id UUID,
    pipeline_id UUID,
    current_stage_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_lead_code_company UNIQUE (lead_code, company_id),
    CONSTRAINT fk_lead_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_lead_contact FOREIGN KEY (contact_id) REFERENCES contacts(id),
    CONSTRAINT fk_lead_territory FOREIGN KEY (territory_id) REFERENCES territories(id),
    CONSTRAINT fk_lead_sales_person FOREIGN KEY (assigned_sales_person_id) REFERENCES sales_persons(id),
    CONSTRAINT fk_lead_pipeline FOREIGN KEY (pipeline_id) REFERENCES sales_pipelines(id),
    CONSTRAINT fk_lead_stage FOREIGN KEY (current_stage_id) REFERENCES pipeline_stages(id)
);

CREATE INDEX idx_lead_company ON leads(company_id);
CREATE INDEX idx_lead_status ON leads(status);
CREATE INDEX idx_lead_sales_person ON leads(assigned_sales_person_id);
CREATE INDEX idx_lead_pipeline_stage ON leads(pipeline_id, current_stage_id);

-- Lead Activities table
CREATE TABLE lead_activities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    lead_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    subject VARCHAR(200),
    description TEXT,
    scheduled_at TIMESTAMP,
    completed_at TIMESTAMP,
    outcome TEXT,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_lead_activity_lead FOREIGN KEY (lead_id) REFERENCES leads(id) ON DELETE CASCADE,
    CONSTRAINT fk_lead_activity_created_by FOREIGN KEY (created_by_id) REFERENCES sales_persons(id)
);

CREATE INDEX idx_lead_activity_lead ON lead_activities(lead_id);

-- Opportunities table
CREATE TABLE opportunities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL,
    opportunity_code VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'OPEN',
    amount DECIMAL(15,2),
    probability INTEGER DEFAULT 10,
    weighted_amount DECIMAL(15,2),
    expected_close_date TIMESTAMP,
    actual_close_date TIMESTAMP,
    lost_reason TEXT,
    lost_to_competitor VARCHAR(100),
    notes TEXT,
    lead_id UUID,
    customer_id UUID NOT NULL,
    contact_id UUID,
    territory_id UUID,
    assigned_sales_person_id UUID,
    pipeline_id UUID,
    current_stage_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_opportunity_code_company UNIQUE (opportunity_code, company_id),
    CONSTRAINT fk_opportunity_lead FOREIGN KEY (lead_id) REFERENCES leads(id),
    CONSTRAINT fk_opportunity_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_opportunity_contact FOREIGN KEY (contact_id) REFERENCES contacts(id),
    CONSTRAINT fk_opportunity_territory FOREIGN KEY (territory_id) REFERENCES territories(id),
    CONSTRAINT fk_opportunity_sales_person FOREIGN KEY (assigned_sales_person_id) REFERENCES sales_persons(id),
    CONSTRAINT fk_opportunity_pipeline FOREIGN KEY (pipeline_id) REFERENCES sales_pipelines(id),
    CONSTRAINT fk_opportunity_stage FOREIGN KEY (current_stage_id) REFERENCES pipeline_stages(id)
);

CREATE INDEX idx_opportunity_company ON opportunities(company_id);
CREATE INDEX idx_opportunity_status ON opportunities(status);
CREATE INDEX idx_opportunity_customer ON opportunities(customer_id);
CREATE INDEX idx_opportunity_sales_person ON opportunities(assigned_sales_person_id);
CREATE INDEX idx_opportunity_pipeline_stage ON opportunities(pipeline_id, current_stage_id);

-- Opportunity Activities table
CREATE TABLE opportunity_activities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    opportunity_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    subject VARCHAR(200),
    description TEXT,
    scheduled_at TIMESTAMP,
    completed_at TIMESTAMP,
    outcome TEXT,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_opp_activity_opportunity FOREIGN KEY (opportunity_id) REFERENCES opportunities(id) ON DELETE CASCADE,
    CONSTRAINT fk_opp_activity_created_by FOREIGN KEY (created_by_id) REFERENCES sales_persons(id)
);

CREATE INDEX idx_opp_activity_opportunity ON opportunity_activities(opportunity_id);

-- Opportunity Products table
CREATE TABLE opportunity_products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    opportunity_id UUID NOT NULL,
    product_id UUID NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    quantity DECIMAL(15,3) NOT NULL,
    unit_price DECIMAL(15,4) NOT NULL,
    discount_percent DECIMAL(5,2) DEFAULT 0,
    total_amount DECIMAL(15,2),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_opp_product_opportunity FOREIGN KEY (opportunity_id) REFERENCES opportunities(id) ON DELETE CASCADE
);

CREATE INDEX idx_opp_product_opportunity ON opportunity_products(opportunity_id);

-- Quotations table
CREATE TABLE quotations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL,
    number VARCHAR(20) NOT NULL,
    series VARCHAR(10) DEFAULT '1',
    version INTEGER DEFAULT 1,
    parent_quotation_id UUID,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'DRAFT',
    valid_from DATE NOT NULL,
    valid_until DATE NOT NULL,
    subtotal DECIMAL(15,2) DEFAULT 0,
    discount_percent DECIMAL(5,2) DEFAULT 0,
    discount_amount DECIMAL(15,2) DEFAULT 0,
    tax_amount DECIMAL(15,2) DEFAULT 0,
    total_amount DECIMAL(15,2) DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'BRL',
    payment_terms VARCHAR(100),
    delivery_terms VARCHAR(100),
    notes TEXT,
    internal_notes TEXT,
    sent_at TIMESTAMP,
    accepted_at TIMESTAMP,
    rejected_at TIMESTAMP,
    rejection_reason TEXT,
    opportunity_id UUID,
    customer_id UUID NOT NULL,
    contact_id UUID,
    billing_address_id UUID,
    shipping_address_id UUID,
    created_by_id UUID,
    approved_by_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_quotation_number_company UNIQUE (number, company_id),
    CONSTRAINT fk_quotation_parent FOREIGN KEY (parent_quotation_id) REFERENCES quotations(id),
    CONSTRAINT fk_quotation_opportunity FOREIGN KEY (opportunity_id) REFERENCES opportunities(id),
    CONSTRAINT fk_quotation_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_quotation_contact FOREIGN KEY (contact_id) REFERENCES contacts(id),
    CONSTRAINT fk_quotation_billing_addr FOREIGN KEY (billing_address_id) REFERENCES addresses(id),
    CONSTRAINT fk_quotation_shipping_addr FOREIGN KEY (shipping_address_id) REFERENCES addresses(id),
    CONSTRAINT fk_quotation_created_by FOREIGN KEY (created_by_id) REFERENCES sales_persons(id),
    CONSTRAINT fk_quotation_approved_by FOREIGN KEY (approved_by_id) REFERENCES sales_persons(id)
);

CREATE INDEX idx_quotation_company ON quotations(company_id);
CREATE INDEX idx_quotation_status ON quotations(status);
CREATE INDEX idx_quotation_customer ON quotations(customer_id);
CREATE INDEX idx_quotation_opportunity ON quotations(opportunity_id);
CREATE INDEX idx_quotation_parent ON quotations(parent_quotation_id);

-- Quotation Items table
CREATE TABLE quotation_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    quotation_id UUID NOT NULL,
    line_number INTEGER NOT NULL,
    product_id UUID NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    description TEXT,
    quantity DECIMAL(15,3) NOT NULL,
    unit_of_measure VARCHAR(20) NOT NULL,
    unit_price DECIMAL(15,4) NOT NULL,
    discount_percent DECIMAL(5,2) DEFAULT 0,
    discount_amount DECIMAL(15,2) DEFAULT 0,
    tax_percent DECIMAL(5,2) DEFAULT 0,
    tax_amount DECIMAL(15,2) DEFAULT 0,
    total_amount DECIMAL(15,2) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_quotation_item_quotation FOREIGN KEY (quotation_id) REFERENCES quotations(id) ON DELETE CASCADE
);

CREATE INDEX idx_quotation_item_quotation ON quotation_items(quotation_id);

-- Quotation Versions table
CREATE TABLE quotation_versions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    quotation_id UUID NOT NULL,
    version INTEGER NOT NULL,
    number VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    change_reason TEXT,
    created_by_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_quotation_version_quotation FOREIGN KEY (quotation_id) REFERENCES quotations(id) ON DELETE CASCADE,
    CONSTRAINT uk_quotation_version UNIQUE (quotation_id, version)
);

CREATE INDEX idx_quotation_version_quotation ON quotation_versions(quotation_id);

-- Sales Orders table
CREATE TABLE sales_orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL,
    number VARCHAR(20) NOT NULL,
    series VARCHAR(10) DEFAULT '1',
    quotation_id UUID,
    status VARCHAR(20) DEFAULT 'DRAFT',
    order_date DATE NOT NULL,
    delivery_date DATE,
    confirmed_at TIMESTAMP,
    confirmed_by_id UUID,
    subtotal DECIMAL(15,2) DEFAULT 0,
    discount_percent DECIMAL(5,2) DEFAULT 0,
    discount_amount DECIMAL(15,2) DEFAULT 0,
    tax_amount DECIMAL(15,2) DEFAULT 0,
    total_amount DECIMAL(15,2) DEFAULT 0,
    shipping_amount DECIMAL(15,2) DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'BRL',
    payment_terms VARCHAR(100),
    delivery_terms VARCHAR(100),
    notes TEXT,
    internal_notes TEXT,
    cancelled_at TIMESTAMP,
    cancelled_by_id UUID,
    cancellation_reason TEXT,
    opportunity_id UUID,
    customer_id UUID NOT NULL,
    contact_id UUID,
    billing_address_id UUID,
    shipping_address_id UUID,
    sales_person_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_sales_order_number_company UNIQUE (number, company_id),
    CONSTRAINT fk_sales_order_quotation FOREIGN KEY (quotation_id) REFERENCES quotations(id),
    CONSTRAINT fk_sales_order_opportunity FOREIGN KEY (opportunity_id) REFERENCES opportunities(id),
    CONSTRAINT fk_sales_order_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_sales_order_contact FOREIGN KEY (contact_id) REFERENCES contacts(id),
    CONSTRAINT fk_sales_order_billing_addr FOREIGN KEY (billing_address_id) REFERENCES addresses(id),
    CONSTRAINT fk_sales_order_shipping_addr FOREIGN KEY (shipping_address_id) REFERENCES addresses(id),
    CONSTRAINT fk_sales_order_sales_person FOREIGN KEY (sales_person_id) REFERENCES sales_persons(id)
);

CREATE INDEX idx_sales_order_company ON sales_orders(company_id);
CREATE INDEX idx_sales_order_status ON sales_orders(status);
CREATE INDEX idx_sales_order_customer ON sales_orders(customer_id);
CREATE INDEX idx_sales_order_quotation ON sales_orders(quotation_id);

-- Sales Order Items table
CREATE TABLE sales_order_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sales_order_id UUID NOT NULL,
    line_number INTEGER NOT NULL,
    product_id UUID NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    description TEXT,
    quantity DECIMAL(15,3) NOT NULL,
    unit_of_measure VARCHAR(20) NOT NULL,
    unit_price DECIMAL(15,4) NOT NULL,
    discount_percent DECIMAL(5,2) DEFAULT 0,
    discount_amount DECIMAL(15,2) DEFAULT 0,
    tax_percent DECIMAL(5,2) DEFAULT 0,
    tax_amount DECIMAL(15,2) DEFAULT 0,
    total_amount DECIMAL(15,2) NOT NULL,
    delivered_quantity DECIMAL(15,3) DEFAULT 0,
    warehouse_id UUID,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_sales_order_item_order FOREIGN KEY (sales_order_id) REFERENCES sales_orders(id) ON DELETE CASCADE
);

CREATE INDEX idx_sales_order_item_order ON sales_order_items(sales_order_id);

-- Sales Order History table
CREATE TABLE sales_order_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sales_order_id UUID NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    changed_by_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sales_order_history_order FOREIGN KEY (sales_order_id) REFERENCES sales_orders(id) ON DELETE CASCADE
);

CREATE INDEX idx_sales_order_history_order ON sales_order_history(sales_order_id);

-- Delivery Notes table
CREATE TABLE delivery_notes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL,
    number VARCHAR(20) NOT NULL,
    series VARCHAR(10) DEFAULT '1',
    sales_order_id UUID NOT NULL,
    delivery_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'DRAFT',
    delivery_type VARCHAR(20),
    carrier VARCHAR(100),
    tracking_code VARCHAR(100),
    vehicle_plate VARCHAR(10),
    driver_name VARCHAR(100),
    driver_document VARCHAR(20),
    freight_value DECIMAL(15,2) DEFAULT 0,
    insurance_value DECIMAL(15,2) DEFAULT 0,
    total_weight DECIMAL(10,3),
    total_volume DECIMAL(10,3),
    notes TEXT,
    confirmed_at TIMESTAMP,
    confirmed_by_id UUID,
    cancelled_at TIMESTAMP,
    cancelled_by_id UUID,
    cancellation_reason TEXT,
    shipping_address_id UUID,
    carrier_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_delivery_note_number_company UNIQUE (number, company_id),
    CONSTRAINT fk_delivery_note_order FOREIGN KEY (sales_order_id) REFERENCES sales_orders(id),
    CONSTRAINT fk_delivery_note_shipping_addr FOREIGN KEY (shipping_address_id) REFERENCES addresses(id)
);

CREATE INDEX idx_delivery_note_company ON delivery_notes(company_id);
CREATE INDEX idx_delivery_note_status ON delivery_notes(status);
CREATE INDEX idx_delivery_note_order ON delivery_notes(sales_order_id);

-- Delivery Note Items table
CREATE TABLE delivery_note_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    delivery_note_id UUID NOT NULL,
    line_number INTEGER NOT NULL,
    sales_order_item_id UUID NOT NULL,
    product_id UUID NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    quantity DECIMAL(15,3) NOT NULL,
    unit_of_measure VARCHAR(20) NOT NULL,
    unit_weight DECIMAL(10,3),
    unit_volume DECIMAL(10,3),
    batch_number VARCHAR(50),
    serial_numbers TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_delivery_note_item_note FOREIGN KEY (delivery_note_id) REFERENCES delivery_notes(id) ON DELETE CASCADE,
    CONSTRAINT fk_delivery_note_item_order_item FOREIGN KEY (sales_order_item_id) REFERENCES sales_order_items(id)
);

CREATE INDEX idx_delivery_note_item_note ON delivery_note_items(delivery_note_id);
CREATE INDEX idx_delivery_note_item_order_item ON delivery_note_items(sales_order_item_id);

-- Sales Invoices table
CREATE TABLE sales_invoices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL,
    number VARCHAR(20) NOT NULL,
    series VARCHAR(10) DEFAULT '1',
    access_key VARCHAR(50),
    xml_path TEXT,
    status VARCHAR(20) DEFAULT 'DRAFT',
    issue_date DATE NOT NULL,
    operation_date DATE,
    due_date DATE,
    invoice_type VARCHAR(20),
    nature_of_operation VARCHAR(60),
    subtotal DECIMAL(15,2) DEFAULT 0,
    discount_amount DECIMAL(15,2) DEFAULT 0,
    tax_icms DECIMAL(15,2) DEFAULT 0,
    tax_ipi DECIMAL(15,2) DEFAULT 0,
    tax_pis DECIMAL(15,2) DEFAULT 0,
    tax_cofins DECIMAL(15,2) DEFAULT 0,
    tax_iss DECIMAL(15,2) DEFAULT 0,
    total_tax DECIMAL(15,2) DEFAULT 0,
    freight_value DECIMAL(15,2) DEFAULT 0,
    insurance_value DECIMAL(15,2) DEFAULT 0,
    other_expenses DECIMAL(15,2) DEFAULT 0,
    total_amount DECIMAL(15,2) DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'BRL',
    authorized_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancelled_by_id UUID,
    cancellation_reason TEXT,
    xml_content TEXT,
    pdf_content BYTEA,
    notes TEXT,
    sales_order_id UUID,
    delivery_note_id UUID,
    customer_id UUID NOT NULL,
    billing_address_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_sales_invoice_number_company UNIQUE (number, company_id),
    CONSTRAINT uk_sales_invoice_access_key UNIQUE (access_key),
    CONSTRAINT fk_sales_invoice_order FOREIGN KEY (sales_order_id) REFERENCES sales_orders(id),
    CONSTRAINT fk_sales_invoice_delivery_note FOREIGN KEY (delivery_note_id) REFERENCES delivery_notes(id),
    CONSTRAINT fk_sales_invoice_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_sales_invoice_billing_addr FOREIGN KEY (billing_address_id) REFERENCES addresses(id)
);

CREATE INDEX idx_sales_invoice_company ON sales_invoices(company_id);
CREATE INDEX idx_sales_invoice_status ON sales_invoices(status);
CREATE INDEX idx_sales_invoice_customer ON sales_invoices(customer_id);
CREATE INDEX idx_sales_invoice_access_key ON sales_invoices(access_key);
CREATE INDEX idx_sales_invoice_due_date ON sales_invoices(due_date);

-- Sales Invoice Items table
CREATE TABLE sales_invoice_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sales_invoice_id UUID NOT NULL,
    line_number INTEGER NOT NULL,
    product_id UUID NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    description TEXT,
    quantity DECIMAL(15,3) NOT NULL,
    unit_of_measure VARCHAR(20) NOT NULL,
    unit_price DECIMAL(15,4) NOT NULL,
    discount_amount DECIMAL(15,2) DEFAULT 0,
    tax_icms DECIMAL(15,2) DEFAULT 0,
    tax_ipi DECIMAL(15,2) DEFAULT 0,
    tax_pis DECIMAL(15,2) DEFAULT 0,
    tax_cofins DECIMAL(15,2) DEFAULT 0,
    tax_iss DECIMAL(15,2) DEFAULT 0,
    total_tax DECIMAL(15,2) DEFAULT 0,
    total_amount DECIMAL(15,2) NOT NULL,
    cfop VARCHAR(10),
    ncm VARCHAR(10),
    cest VARCHAR(10),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_sales_invoice_item_invoice FOREIGN KEY (sales_invoice_id) REFERENCES sales_invoices(id) ON DELETE CASCADE
);

CREATE INDEX idx_sales_invoice_item_invoice ON sales_invoice_items(sales_invoice_id);

-- Payments table
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sales_invoice_id UUID NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    due_date DATE,
    paid_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING',
    transaction_id VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_payment_invoice FOREIGN KEY (sales_invoice_id) REFERENCES sales_invoices(id) ON DELETE CASCADE
);

CREATE INDEX idx_payment_invoice ON payments(sales_invoice_id);

-- Commission Rules table
CREATE TABLE commission_rules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL,
    rule_code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    rule_type VARCHAR(20) DEFAULT 'PERCENTAGE',
    value DECIMAL(15,2),
    min_amount DECIMAL(15,2),
    max_amount DECIMAL(15,2),
    product_category_id UUID,
    customer_segment VARCHAR(50),
    valid_from DATE NOT NULL,
    valid_until DATE,
    is_active BOOLEAN DEFAULT true,
    priority INTEGER DEFAULT 0,
    sales_team_id UUID,
    sales_person_id UUID,
    territory_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_commission_rule_code_company UNIQUE (rule_code, company_id),
    CONSTRAINT fk_commission_rule_team FOREIGN KEY (sales_team_id) REFERENCES sales_teams(id),
    CONSTRAINT fk_commission_rule_person FOREIGN KEY (sales_person_id) REFERENCES sales_persons(id),
    CONSTRAINT fk_commission_rule_territory FOREIGN KEY (territory_id) REFERENCES territories(id)
);

CREATE INDEX idx_commission_rule_company ON commission_rules(company_id);
CREATE INDEX idx_commission_rule_team ON commission_rules(sales_team_id);
CREATE INDEX idx_commission_rule_person ON commission_rules(sales_person_id);
CREATE INDEX idx_commission_rule_territory ON commission_rules(territory_id);

-- Commission Tiers table
CREATE TABLE commission_tiers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    commission_rule_id UUID NOT NULL,
    tier_order INTEGER NOT NULL,
    min_amount DECIMAL(15,2),
    max_amount DECIMAL(15,2),
    rate DECIMAL(5,2),
    fixed_amount DECIMAL(15,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_commission_tier_rule FOREIGN KEY (commission_rule_id) REFERENCES commission_rules(id) ON DELETE CASCADE
);

CREATE INDEX idx_commission_tier_rule ON commission_tiers(commission_rule_id);

-- Commission Payments table
CREATE TABLE commission_payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL,
    commission_rule_id UUID NOT NULL,
    sales_person_id UUID NOT NULL,
    sales_order_id UUID,
    sales_invoice_id UUID,
    base_amount DECIMAL(15,2) NOT NULL,
    commission_rate DECIMAL(5,2) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    calculated_at TIMESTAMP NOT NULL,
    calculated_by_id UUID,
    paid_at TIMESTAMP,
    paid_by_id UUID,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_commission_payment_rule FOREIGN KEY (commission_rule_id) REFERENCES commission_rules(id),
    CONSTRAINT fk_commission_payment_person FOREIGN KEY (sales_person_id) REFERENCES sales_persons(id),
    CONSTRAINT fk_commission_payment_order FOREIGN KEY (sales_order_id) REFERENCES sales_orders(id),
    CONSTRAINT fk_commission_payment_invoice FOREIGN KEY (sales_invoice_id) REFERENCES sales_invoices(id)
);

CREATE INDEX idx_commission_payment_company ON commission_payments(company_id);
CREATE INDEX idx_commission_payment_person ON commission_payments(sales_person_id);
CREATE INDEX idx_commission_payment_period ON commission_payments(period_start, period_end);
CREATE INDEX idx_commission_payment_status ON commission_payments(status);

-- LGPD Consents table
CREATE TABLE lgpd_consents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL,
    entity_type VARCHAR(20) NOT NULL,
    entity_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'GRANTED',
    granted_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    expires_at TIMESTAMP,
    legal_basis VARCHAR(100) NOT NULL,
    purpose TEXT NOT NULL,
    version VARCHAR(50),
    ip_address VARCHAR(45),
    user_agent TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_lgpd_consent_entity ON lgpd_consents(entity_type, entity_id);
CREATE INDEX idx_lgpd_consent_company ON lgpd_consents(company_id);