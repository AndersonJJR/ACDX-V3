# ERP Brazil - Documentação Técnica Completa

**Versão:** 1.0.0-SNAPSHOT  
**Data:** 15 de Novembro de 2026  
**Status:** Documentação do estado **ATUAL** da implementação (não especificação)

---

## Sumário

1. [Visão Geral do Sistema](#1-visão-geral-do-sistema)
2. [Arquitetura Técnica](#2-arquitetura-técnica)
3. [Serviços Backend](#3-serviços-backend)
4. [Frontend (erp-frontend-web)](#4-frontend-erp-frontend-web)
5. [Infraestrutura & Deploy](#5-infraestrutura--deploy)
6. [Segurança & Conformidade](#6-segurança--conformidade)
7. [O que está Completo vs Pendente](#7-o-que-está-completo-vs-pendente)
8. [Como Executar](#8-como-executar)
9. [Decisões Técnicas (ADRs)](#9-decisões-técnicas-adrs)

---

## 1. Visão Geral do Sistema

### 1.1 Propósito

O **ERP Brazil** é um sistema ERP multi-tenant, nativo para o mercado brasileiro, construído como uma arquitetura de microsserviços event-driven. O sistema atende empresas brasileiras de médio e grande porte com funcionalidades completas de:

- **Gestão Financeira e Contábil** (Plano de contas, lançamentos, períodos fiscais, ativos, conciliação bancária)
- **Fiscal Brasileiro** (NF-e, NFC-e, NFS-e, CT-e, SPED, Certificado Digital A1, retenções)
- **Estoque e Suprimentos** (Itens, variantes, lotes, séries, almoxarifados, compras, 3-way match)
- **Vendas e CRM** (Leads, oportunidades, cotações, pedidos, entregas, faturas, comissões)
- **Manufatura e Projetos** (BOM multi-nível, roteiros, ordens de produção, terceirização, qualidade)
- **RH e Auditoria** (Colaboradores, ponto eletrônico, férias, folha, holerites, LGPD, webhooks)

### 1.2 Stack Tecnológica

| Camada | Tecnologia | Versão |
|--------|------------|--------|
| **Linguagem** | Java | 21 |
| **Framework Backend** | Spring Boot | 3.4.0 |
| **Cloud/Native** | Spring Cloud | 2024.0.0 |
| **API Gateway** | Spring Cloud Gateway | 3.4.0 |
| **Banco Relacional** | MySQL | 8.4 |
| **Banco Documental** | MongoDB | 7.0 |
| **Cache/LOCK** | Redis | 7 |
| **Message Broker** | RabbitMQ | 3.13 |
| **Build** | Maven | 3.9+ |
| **Frontend** | Angular | 22 (standalone, signals) |
| **CSS** | Tailwind CSS | 4.x |
| **Observabilidade** | Prometheus, Grafana, Jaeger | Latest |

### 1.3 Estilo Arquitetural

- **Microsserviços** com banco de dados por serviço (Database per Service)
- **Event-Driven** via RabbitMQ (Transactional Outbox Pattern)
- **Multi-tenant** por `tenant_id` (company_id) em todas as entidades
- **Clean Architecture** (Domain → Application → Infrastructure → Presentation)
- **CQRS** leve (Use Cases = Commands/Queries)
- **API Gateway** centralizado (Spring Cloud Gateway)

---

## 2. Arquitetura Técnica

### 2.1 Visão Geral dos Microsserviços (8 Serviços)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            API GATEWAY (Porta 8080)                         │
│         JWT Validation │ Tenant Resolution │ Rate Limiting │ Circuit Breaker │
└─────────────────────────────────────────────────────────────────────────────┘
          │                    │                    │                    │
    ┌─────┴─────┐        ┌─────┴─────┐        ┌─────┴─────┐        ┌─────┴─────┐
    │ Identity  │        │  Finance  │        │  Fiscal   │        │    HR     │
    │  Tenant   │        │ Accounting│        │  Brazil   │        │  Audit    │
    │  Service  │        │  Service  │        │  Service  │        │  Service  │
    │  :8081    │        │  :8082    │        │  :8083    │        │  :8084    │
    └─────┬─────┘        └─────┬─────┘        └─────┬─────┘        └─────┬─────┘
          │                    │                    │                    │
    ┌─────┴─────┐        ┌─────┴─────┐        ┌─────┴─────┐        ┌─────┴─────┐
    │ Inventory │        │Manufactur.│        │  Sales    │        │ Frontend  │
    │  Supply   │        │ Projects  │        │   CRM     │        │  (Angular)│
    │  Service  │        │  Service  │        │  Service  │        │  :4200    │
    │  :8085    │        │  :8086    │        │  :8087    │        └───────────┘
    └───────────┘        └───────────┘        └───────────┘
```

### 2.2 Comunicação Entre Serviços

| Padrão | Uso | Tecnologia |
|--------|-----|------------|
| **Síncrono** | Consultas pontuais, validações | REST via API Gateway (Feign/OpenFeign) |
| **Assíncrono** | Eventos de domínio, integrações | RabbitMQ (Topic Exchange: `erp.events`) |
| **Outbox** | Confiabilidade transacional | Tabela `outbox_events` + Poller assíncrono |

### 2.3 Bancos de Dados

| Serviço | MySQL (Relacional) | MongoDB (Documental) |
|---------|-------------------|---------------------|
| identity-tenant-service | Companies, Users, Roles, Permissions, Branches, Departments, CostCenters | AuditEvent |
| finance-accounting-service | ChartOfAccounts, FiscalYear, FiscalPeriod, JournalEntry, JournalEntryLine | - |
| fiscal-brazil-service | NCM, CEST, CFOP, TaxRule, NFE, NFSE, NFCE, CTE, SPEDBlock, CertificadoDigital | - |
| inventory-supply-service | Warehouse, Location, Item, ItemVariant, ItemBatch, StockLedgerEntry, PurchaseOrder, Supplier | - |
| manufacturing-projects-service | BOM, Routing, ProductionOrder, WorkOrder, WorkCenter, Project, QualityInspection | - |
| sales-crm-service | Customer, Lead, Opportunity, Quotation, SalesOrder, DeliveryNote, SalesInvoice | - |
| hr-audit-service | Employee, EmploymentContract, Payroll, TimeRecord, LeaveRequest | AuditLog (capped, TTL 7 anos) |

### 2.4 Padrões de Implementação Comuns (erp-common-core)

O módulo **erp-common-core** fornece a base compartilhada para todos os serviços:

| Componente | Descrição |
|------------|-----------|
| **TenantContext** | ThreadLocal para `tenantId`, `userId`, `companyId` |
| **BaseEntity** | UUID, auditoria (createdAt/By, updatedAt/By), version (optimistic lock), soft delete |
| **Money (BRL)** | Value Object imutável com BigDecimal, operações aritméticas, formatação pt-BR |
| **NumberingSeries** | Numeração por tenant/company/branch com reset policies |
| **BrazilianValidators** | CPF, CNPJ, CEP, Phone, Inscrição Estadual (14 estados) + annotations |
| **DomainEvent / AuditEvent / OutboxEvent** | Base para event sourcing e auditoria |
| **JwtTokenProvider** | Access/Refresh tokens com claims: tenant, user, company, permissions |
| **DistributedLock (Redisson)** | Reentrant locks com auto-renewal (lease 30s) |
| **DomainEventPublisher** | Publicação assíncrona via RabbitMQ com routing keys |
| **OutboxEventPublisher** | Poller agendado (5s), retry (max 5), dead-letter |
| **GlobalExceptionHandler** | RFC 7807 ProblemDetail para todos os erros |
| **BaseController** | Contexto de tenant, respostas padronizadas (ok, created, paged, noContent) |

---

## 3. Serviços Backend

### 3.1 api-gateway

**Porta:** 8080  
**Status:** ✅ **IMPLEMENTADO COMPLETO**

#### Funcionalidades
- Roteamento para todos os 7 serviços backend
- Validação JWT (assinatura, expiração, claims)
- Resolução de tenant via header `X-Tenant-ID`
- Rate Limiting (Redis-backed, token bucket)
- Circuit Breaker (Resilience4j) por serviço
- Retry automático (GET/HEAD, 3 tentativas, backoff exponencial)
- CORS configurável
- Health checks agregados

#### Rotas Configuradas

| Rota | Serviço Destino | Path Prefix |
|------|-----------------|-------------|
| `identity-service` | identity-tenant-service | `/api/identity/**`, `/api/tenants/**`, `/api/companies/**`, `/api/users/**`, `/api/roles/**`, `/api/permissions/**`, `/api/branches/**`, `/api/departments/**`, `/api/cost-centers/**`, `/api/numbering-series/**`, `/api/auth/**`, `/api/bootinfo/**` |
| `finance-service` | finance-accounting-service | `/api/finance/**`, `/api/accounting/**`, `/api/fiscal/**`, `/api/tax/**`, `/api/bank/**`, `/api/payment/**`, `/api/receivable/**`, `/api/payable/**` |
| `fiscal-service` | fiscal-brazil-service | `/api/fiscal/**`, `/api/nfe/**`, `/api/nfce/**`, `/api/cte/**`, `/api/mdfe/**`, `/api/sped/**`, `/api/efd/**`, `/api/dfe/**` |
| `hr-service` | hr-audit-service | `/api/hr/**`, `/api/payroll/**`, `/api/employee/**`, `/api/contract/**`, `/api/benefits/**`, `/api/time/**`, `/api/audit/**` |
| `inventory-service` | inventory-supply-service | `/api/inventory/**`, `/api/warehouse/**`, `/api/product/**`, `/api/stock/**`, `/api/purchase/**`, `/api/supplier/**`, `/api/supply/**` |
| `manufacturing-service` | manufacturing-projects-service | `/api/manufacturing/**`, `/api/production/**`, `/api/bom/**`, `/api/routing/**`, `/api/work-order/**`, `/api/project/**`, `/api/mrp/**` |
| `sales-service` | sales-crm-service | `/api/sales/**`, `/api/crm/**`, `/api/customer/**`, `/api/lead/**`, `/api/opportunity/**`, `/api/quote/**`, `/api/order/**`, `/api/contract/**` |

#### Filtros Implementados
- `JwtAuthenticationFilter` - Valida token, extrai claims, define `X-User-ID`, `X-Tenant-ID`, `X-Company-ID`
- `TenantResolutionFilter` - Resolve tenant do header ou token, valida acesso
- `RateLimitingFilter` - Token bucket por IP/usuário/tenant

---

### 3.2 identity-tenant-service

**Porta:** 8081  
**Status:** ✅ **IMPLEMENTADO COMPLETO**

#### Domínio (Modelos)

| Entidade | Descrição | Campos Principais |
|----------|-----------|-------------------|
| **Company** | Tenant/Empresa | code, name, tradeName, CNPJ (validado), IE, IM, endereço, status (ACTIVE/INACTIVE/SUSPENDED/TRIAL/CANCELLED), settings JSON, branding (logo, cores) |
| **User** | Usuário do sistema | username, password (BCrypt), fullName, CPF (validado), email, phone, 2FA, status, lastLogin, failedAttempts, lockUntil, preferences JSON, language (pt-BR), timezone (America/Sao_Paulo) |
| **Role** | Perfil de acesso | code, name, description, roleType (SYSTEM/ADMIN/MANAGER/OPERATOR/VIEWER/CUSTOM), isSystem, isDefault, priority, permissions (ManyToMany) |
| **Permission** | Permissão granular | code, name, resource, action (ex: `company:create`, `journal-entry:post`), isSystem |
| **UserCompanyRole** | Vinculação User-Company-Role | userId, companyId, roleId, branchId (opcional), departmentId (opcional) |
| **Branch** | Filial | companyId, code, name, CNPJ, endereço, isHeadOffice |
| **Department** | Departamento | companyId, branchId, code, name, costCenterId |
| **CostCenter** | Centro de Custo | companyId, code, name, type (REVENUE/COST/PROFIT/INVESTMENT), parentId (hierárquico) |
| **BootInfo** | Configuração de inicialização | menus, modules, permissions, numberingSeries, fiscalConfig |
| **NumberingSeries** | Séries numéricas | prefix, suffix, currentNumber, minLength, resetPolicy (DAILY/MONTHLY/YEARLY/NEVER), formatTemplate |

#### Eventos Publicados
- `CompanyCreatedEvent` - Nova empresa criada
- `UserCreatedEvent` - Novo usuário criado
- `RoleCreatedEvent` - Nova role criada
- `NumberingSeriesUpdatedEvent` - Série numérica alterada

#### API REST Endpoints

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/api/auth/login` | Login (retorna access + refresh token) |
| `POST` | `/api/auth/refresh` | Renova access token |
| `POST` | `/api/auth/logout` | Invalida refresh token |
| `POST` | `/api/auth/forgot-password` | Solicita reset de senha |
| `POST` | `/api/auth/reset-password` | Reseta senha com token |
| `POST` | `/api/auth/change-password` | Altera senha (autenticado) |
| `GET` | `/api/auth/validate` | Valida token atual |
| `GET` | `/api/bootinfo` | Retorna configuração para frontend |
| `PUT` | `/api/bootinfo` | Atualiza configuração (admin) |
| `POST` | `/api/companies` | Cria empresa |
| `GET` | `/api/companies/{id}` | Busca empresa |
| `GET` | `/api/companies` | Lista empresas (paginado) |
| `PUT` | `/api/companies/{id}` | Atualiza empresa |
| `DELETE` | `/api/companies/{id}` | Soft delete empresa |
| `POST` | `/api/companies/{id}/activate` | Ativa empresa |
| `POST` | `/api/companies/{id}/deactivate` | Desativa empresa |
| `POST` | `/api/companies/{id}/suspend` | Suspende empresa |

#### Configuração
- `application.yml` base + `application-dev.yml` (H2/SQLite) + `application-prod.yml` (MySQL+Redis+Mongo+RabbitMQ)
- Flyway: `V1__init_identity.sql`, `V2__seed_roles_permissions.sql`

---

### 3.3 finance-accounting-service

**Porta:** 8082  
**Status:** ⚠️ **PARCIAL - APENAS MODELOS BÁSICOS (28.6%)**

#### Domínio Implementado (8 de 28 modelos)

| Modelo | Status | Arquivo |
|--------|--------|---------|
| `ChartOfAccounts` | ✅ | Plano de contas hierárquico (código, nome, tipo, natureza, nível, pai) |
| `AccountType` | ✅ | Enum: ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE |
| `AccountNature` | ✅ | Enum: DEBIT, CREDIT |
| `FiscalYear` | ✅ | Exercício fiscal (ano, data início/fim, status) |
| `FiscalPeriod` | ✅ | Período fiscal (mês, datas, locked/lockedAt/lockedBy) |
| `JournalEntry` | ✅ | Lançamento contábil (reference, data, período, status, totalDebit/totalCredit, `isBalanced()`) |
| `JournalEntryLine` | ✅ | Linha do lançamento (conta, débito/crédito, histórico, centro custo - **referencia CostCenter que NÃO EXISTE**) |
| `JournalEntryStatus` | ✅ | Enum: DRAFT, POSTED, REVERSED, CANCELLED |

#### Modelos FALTANDO (20)
- `CostCenter` (hierárquico) - **REFERENCIADO mas não existe**
- `GLEntry` (livro razão imutável)
- `ReversalEntry` (estorno vinculado)
- `Asset`, `AssetCategory`, `AssetDepreciationSchedule`, `DepreciationEntry`
- `BankAccount`, `BankStatement`, `BankStatementLine`, `Reconciliation`
- `PaymentTerm`, `PaymentMethod`
- `Vendor`, `Customer`
- `PayableInvoice`, `ReceivableInvoice`, `Payment`, `PaymentAllocation`

#### Use Cases - **TODOS FALTANDO (0/8)**
- `PostJournalEntryUseCase` (validação partida dobrada)
- `ReverseJournalEntryUseCase`
- `LockFiscalPeriodUseCase` / `UnlockFiscalPeriodUseCase`
- `ProcessBankStatementUseCase` (CNAB/OFX)
- `RunDepreciationUseCase` (async, Quartz)
- `GenerateTrialBalanceUseCase`, `GenerateBalanceSheetUseCase`, `GeneratePLUseCase`

#### Infraestrutura - **TODA FALTANDO**
- Repositórios JPA
- Flyway migrations (`V1__init_finance.sql`, `V2__chart_of_accounts_br.sql`)
- Event Publishers (`JournalEntryPostedEvent`, `PaymentProcessedEvent`, `FiscalPeriodLockedEvent`, `AssetDepreciationPostedEvent`)
- Configuração perfis (dev/prod)
- Security config

#### Configuração Atual
- Apenas `application.properties` com `spring.application.name=finance-accounting-service`
- POM usa `spring-boot-starter-parent` **INCORRETO** (deveria usar `erp-brazil-parent`)

---

### 3.4 fiscal-brazil-service

**Porta:** 8083  
**Status:** ⚠️ **PARCIAL - APENAS 4 MODELOS BÁSICOS (26.7%)**

#### Domínio Implementado (4 de 15+ modelos)

| Modelo | Status | Descrição |
|--------|--------|-----------|
| `TaxRegime` | ✅ | Enum: SIMPLES_NACIONAL, LUCRO_PRESUMIDO, LUCRO_REAL, MEI |
| `NCM` | ✅ | JPA Entity: código, descrição, capítulo, posição, subposição, item |
| `CEST` | ✅ | JPA Entity: código, descrição, segmento, ncmCode |
| `CFOP` | ✅ | JPA Entity: código, descrição, tipo, categoria, flags geração |

#### Modelos FALTANDO (11+)
- `TaxRule` (ICMS, IPI, PIS, COFINS, ISS - por estado/município)
- `TaxCalculationContext` / `TaxCalculationResult` (engine de cálculo)
- `NFE` (Nota Fiscal Eletrônica - mercadorias)
- `NFSE` (Nota Fiscal de Serviços Eletrônica)
- `NFCE` (Nota Fiscal Consumidor Eletrônica)
- `CTE` (Conhecimento de Transporte Eletrônico)
- `NFEEvent` (cancelamento, carta correção, etc.)
- `SPEDBlock` (ECD, ECF, EFD_ICMS_IPI, EFD_Contribuições)
- `RetencaoFonte` (IRRF, CSLL, PIS, COFINS, INSS)
- `CertificadoDigitalA1` (certificado A1 com criptografia AES-256-GCM)
- `IntegracaoFiscal` (configuração provedor SEFAZ)

#### Use Cases - **TODOS FALTANDO (0/7)**
- `CalculateTaxesUseCase` (rule chain engine)
- `ValidateNFEUseCase` (XSD + regras negócio)
- `EmitNFEUseCase` (async SEFAZ)
- `CancelNFEUseCase`
- `InutilizeNFEUseCase`
- `GenerateSPEDUseCase`
- `CalculateRetencoesUseCase`
- `MonitorCertificadoDigitalUseCase` (agendado)

#### Infraestrutura - **TODA FALTANDO**
- `SEFAZClient` (nfe4j ou custom + Resilience4j circuit breaker)
- `SPEDWriter`
- `CertificadoDigitalRepository` (AES-256-GCM)
- `TaxRuleRepository` (MySQL + Redis cache)
- `LeisSyncJob` (sync github.com/ctresb/leis)
- Flyway: `V1__init_fiscal.sql`, `V2__seed_ncm_cest_cfop.sql`, `V3__seed_tax_rules.sql`
- Configuração dev/prod

#### POM - **BEM CONFIGURADO**
Todas as dependências necessárias declaradas:
- Spring Web Services, Jackson XML, Bouncy Castle, Apache HttpClient 5
- Resilience4j, Quartz, Testcontainers

---

### 3.5 inventory-supply-service

**Porta:** 8085  
**Status:** ⚠️ **PARCIAL - 14/20 MODELOS (70%), SEM CAMADAS SUPERIORES**

#### Domínio Implementado (14 modelos)

| Modelo | Status | Principais Campos |
|--------|--------|-------------------|
| `Warehouse` | ✅ | code, name, address, type, isActive |
| `Location` | ✅ | Hierárquica (parentId), warehouseId, code, name, type |
| `Item` | ✅ | **Completo**: code, name, itemType (PRODUCT/SERVICE/RAW_MATERIAL/...), valuationMethod (FIFO/MOVING_AVERAGE/STANDARD), standardRate, hasBatch, hasSerialNo, ncmCode, cestCode, origemMercadoria, reorderLevel/Qty, leadTimeDays, shelfLifeDays |
| `ItemVariant` | ✅ | variantCode, attributes (JSON), barcode, weight/volume/dimensions |
| `ItemBatch` | ✅ | batchNo, manufacturingDate, expiryDate, quantity, valuationRate, qualityStatus |
| `ItemSerialNumber` | ✅ | serialNo, status (AVAILABLE/RESERVED/SOLD/MAINTENANCE/SCRAPPED), warehouseId, locationId, warrantyExpiryDate |
| `SerialAndBatchBundle` | ✅ | Agrupamento lote + série |
| `StockLedgerEntry` | ✅ | **IMUTÁVEL, PERPÉTUO**: itemId, variantId, batchId, serialId, warehouseId, locationId, qtyChange, valuationRate, runningBalance, partitionKey (company+date) |
| `StockReservation` | ✅ | itemId, variantId, batchId, warehouseId, qtyReserved, referenceType (SALES_ORDER/PRODUCTION_ORDER), referenceId, expiresAt |
| `Supplier` | ✅ | code, name, CNPJ, contact, paymentTerms, leadTimeDays, rating |
| `SupplierQuotation` | ✅ | supplierId, items[], validUntil, status |
| `PurchaseOrder` | ✅ | supplierId, items[], status (DRAFT/SENT/CONFIRMED/PARTIAL/RECEIVED/CANCELLED), expectedDate |
| `PurchaseReceipt` | ✅ | purchaseOrderId, items[], warehouseId, receivedDate, status |
| `PurchaseReceiptItem` | ✅ | purchaseReceiptId, purchaseOrderItemId, itemId, qtyReceived, qtyAccepted, batch/serial |

#### Modelos FALTANDO (6)
- `PurchaseInvoice` / `PurchaseInvoiceItem`
- `MaterialRequest` / `MaterialRequestItem`
- `RepostingJob`
- `ValuationMethod` como entidade standalone (atual é enum em Item)

#### Use Cases - **TODOS FALTANDO (0/6)**
- `ReserveStockUseCase` (optimistic lock + Redis distributed lock)
- `UnreserveStockUseCase`
- `ReceiveGoodsUseCase` (cria SLE + dispara 3-way match)
- `ProcessPurchaseInvoiceUseCase` (validação 3-way match)
- `RecalculateValuationUseCase` (Reposting, async, chunked, idempotente)
- `TransferStockUseCase`

#### Infraestrutura - **TODA FALTANDO**
- `SLERepository` (partitioned by company+date)
- `RedissonDistributedLock` (key: `lock:inventory:{itemId}:{warehouseId}`, lease 30s, auto-renewal)
- RabbitMQ Consumers: `RepostingJobProcessor`, `ThreeWayMatchProcessor`
- Flyway: `V1__init_inventory.sql`, `V2__seed_warehouses.sql`, `V3__partition_sle.sql`
- Configuração dev/prod

---

### 3.6 manufacturing-projects-service

**Porta:** 8086  
**Status:** ⚠️ **PARCIAL - 12/18 GRUPOS DE MODELOS (66%), SEM CAMADAS SUPERIORES**

#### Domínio Implementado (12 entidades)

| Modelo | Status | Detalhes |
|--------|--------|----------|
| `BOM` | ✅ | Multi-nível, phantom BOM, versioning, effective dates, status (DRAFT→ACTIVE→OBSOLETE→ARCHIVED) |
| `BOMItem` | ✅ | Componente, qtde, scrap%, phantom flag, opcional, position, referenceDesignators |
| `BOMOperation` | ✅ | WorkCenter/Station, setup/run/queue/move times, crew size, outsourcing, sequence |
| `WorkCenter` | ✅ | 8 tipos (MACHINE/LABOR/ASSEMBLY/INSPECTION/PACKAGING/WAREHOUSE/SUBCONTRACTING/OTHER), capacity/efficiency/utilization, cost rates, shift/calendar |
| `WorkStation` | ✅ | Hierárquica sob WorkCenter, capacity, efficiency, hourly rate, localização física (X/Y) |
| `Routing` | ✅ | 4 tipos (PRODUCTION/REWORK/PROTOTYPE/MAINTENANCE), tempo total auto-calc, versioning |
| `RoutingOperation` | ✅ | Sequence linking (next/prev), inspection/sampling plan refs, outsourcing, crew size |
| `ProductionOrder` | ✅ | 7 tipos (MTO/MTS/...), 8 statuses, prioridade, BOM/routing refs, SO/project linkage, custo, qtde |
| `ProductionOrderItem` | ✅ | Requerida/emitida/consumida/devolvida/sucateada, warehouse/location, BOM item traceability |
| `ProductionOrderOperation` | ✅ | Planned vs actual times, 10 statuses (WAITING_MATERIAL, WAITING_CAPACITY), subcontracting ref |
| `WorkOrder` | ✅ | Chão de fábrica, operador atribuído, time logs, operations, qtde tracking, prioridade, datas |
| `WorkOrderOperation` | ✅ | Tracking detalhado, operador, planned vs actual, status lifecycle |

#### Modelos FALTANDO (6 grupos)
| Grupo | Entidades Necessárias |
|-------|----------------------|
| **Subcontracting** | `SubcontractingOrder`, `SubcontractingReceipt` |
| **Projects** | `Project`, `ProjectTask`, `Timesheet` (horas faturáveis, aprovação) |
| **Quality** | `QualityInspection`, `NonConformance`, `CorrectiveAction` (sampling, AQL, RCA, CAPA) |
| **TimeLog** | `TimeLog` - **REFERENCIADO em WorkOrder mas ENTIDADE NÃO EXISTE** |
| **WorkOrderOperation** | Parcial - pode precisar sub-operations |

#### Use Cases - **TODOS FALTANDO (0/9)**
- `ExplodeBOMUseCase` (recursivo, phantom BOMs)
- `CreateProductionOrderUseCase` (de Sales Order ou MPS/MRP)
- `ReleaseProductionOrderUseCase` (reserva matéria-prima, cria work orders)
- `StartOperationUseCase` / `CompleteOperationUseCase`
- `ReportProductionUseCase` (finished goods + scrap)
- `SubcontractOutUseCase` / `ReceiveSubcontractedUseCase`
- `LogTimesheetUseCase` (projeto/tarefa, billable flag)
- `InspectQualityUseCase` (sampling plans, AQL)

#### Infraestrutura - **TODA FALTANDO**
- `BOMRepository` (recursive CTE queries para explosão multi-nível, phantom handling, cost rollup)
- `SchedulingEngine` (capacidade finita/infinita, work center load profiles, Gantt data)
- Flyway: `V1__init_manufacturing.sql`, `V2__seed_workcenters.sql`
- Configuração dev/prod

---

### 3.7 sales-crm-service

**Porta:** 8087  
**Status:** ⚠️ **PARCIAL - 8 ENTIDADES + 11 ENUMS (~20%), SEM CAMADAS SUPERIORES**

#### Domínio Implementado (8 entidades + 11 enums)

| Entidade | Status | Detalhes |
|----------|--------|----------|
| `Lead` | ✅ | activities, pipeline, territory, salesPerson, status, source, score |
| `LeadActivity` | ✅ | type, subject, description, dateTime, outcome |
| `Opportunity` | ✅ | activities, products, weightedAmount, probability, stage, territory, salesPerson |
| `OpportunityActivity` | ✅ | type, subject, description, dateTime |
| `OpportunityProduct` | ✅ | itemId, quantity, unitPrice, discount, tax, total |
| `Customer` | ✅ | **Completo**: addresses, contacts, leads, opportunities, quotations, salesOrders, LGPDConsents, territory, salesPerson, creditLimit, paymentTerms, priceList |
| `Contact` | ✅ | customerId, name, email, phone, role, isPrimary, LGPDConsents |
| `Address` | ✅ | **Brasileiro**: IBGE code, UF, CEP, logradouro, número, complemento, bairro, tipo (BILLING/SHIPPING/OTHER) |

#### Enums Implementados (11)
`LeadStatus`, `OpportunityStatus`, `QuotationStatus`, `SalesOrderStatus`, `DeliveryNoteStatus`, `InvoiceStatus`, `PipelineStageType`, `CommissionStatus`, `LGPDConsentStatus`, `LGPDConsentType`, `CustomerType`

#### Entidades FALTANDO (14+) - **REFERENCIADAS MAS NÃO EXISTEM**
- `Territory` - Referenciado em Lead, Opportunity, Customer
- `SalesPerson` - Referenciado em Lead, Opportunity, Customer
- `SalesPipeline` - Referenciado em Lead, Opportunity
- `PipelineStage` - Referenciado em Lead, Opportunity
- `Quotation` / `QuotationItem` - Referenciado em Customer, Opportunity
- `SalesOrder` / `SalesOrderItem` - Referenciado em Customer
- `DeliveryNote` / `DeliveryNoteItem`
- `SalesInvoice` / `SalesInvoiceItem`
- `CommissionRule` / `CommissionPayment`
- `SalesTeam`
- `LGPDConsent` - Referenciado em Customer, Contact

#### Use Cases - **TODOS FALTANDO (0/8)**
- `ConvertLeadToOpportunityUseCase`
- `CreateQuotationUseCase` (validade, versionamento)
- `ConvertQuotationToOrderUseCase` (reserva estoque via evento)
- `ConfirmSalesOrderUseCase`
- `CreateDeliveryNoteUseCase` (entrega parcial)
- `InvoiceSalesOrderUseCase` (chama fiscal-brazil-service via Feign)
- `CalculateCommissionUseCase` (agendado, assíncrono)
- Pipeline/Kanban use cases

#### Infraestrutura - **TODA FALTANDO**
- Repositórios JPA (0 criados)
- Feign Client para fiscal-brazil-service
- Event Publishers RabbitMQ (stock reservation, order confirmation, invoicing)
- MapStruct Mappers
- Security Config
- Flyway: `V1__init_sales.sql`, `V2__seed_pipelines.sql`
- Configuração dev/prod

---

### 3.8 hr-audit-service

**Porta:** 8084  
**Status:** ❌ **APENAS SCAFFOLDING (0%)**

#### Estado Atual
- Apenas `pom.xml` com dependências corretas
- `HrAuditServiceApplication.java` vazio
- `application.properties` com apenas `spring.application.name=hr-audit-service`
- Estrutura de pastas Clean Architecture **NÃO CRIADA**

#### Domínio Necessário (TODOS FALTANDO)
| Entidade | Descrição |
|----------|-----------|
| `Employee`, `EmploymentContract` | CLT regimes, dados pessoais, bancários (PIX), dependentes |
| `TimeRecord`, `TimeSheet` | Ponto eletrônico (biométrico, mobile, web), status, source |
| `LeaveRequest`, `LeaveBalance` | Férias, licenças, saldos, aprovação |
| `Payroll`, `PayrollItem` | Salário, INSS, IRRF, FGTS, benefícios, categorias |
| `Payslip` | Holerite, assinatura digital, PDF |
| `Benefit`, `Dependent` | Benefícios, dependentes |
| `AuditLog` (MongoDB) | eventId, timestamp, companyId, userId, entityType, entityId, action, oldValue/newValue (mascarados CPF), ipAddress, userAgent - **capped collection, TTL 7 anos** |
| `WebhookSubscription`, `WebhookDelivery` | Retry, DLQ |
| `Notification`, `NotificationTemplate` | Multi-canal (email, WhatsApp) |

#### Use Cases Necessários (TODOS FALTANDO)
- `ProcessPayrollUseCase` (mensal, assíncrono, cálculos complexos)
- `CalculateFGTSUseCase`, `CalculateINSSUseCase`, `CalculateIRRFUseCase`
- `GeneratePayslipUseCase` (PDF, certificado digital)
- `RecordTimeEntryUseCase` (biométrico, mobile, web)
- `ApproveLeaveRequestUseCase`
- `AuditEventUseCase` (auto-called via AOP)
- `SendNotificationUseCase` (async, template engine)
- `ProcessWebhookUseCase` (outbox pattern)

#### LGPD Compliance (OBRIGATÓRIO)
- AOP-based CPF/sensitive data masking automático
- DSAR endpoint: `GET /api/v1/lgpd/export/{userId}`
- Erasure endpoint: `POST /api/v1/lgpd/erase/{userId}`

#### Infraestrutura Necessária
- `MongoAuditRepository` (capped collection, TTL 7 anos)
- PDF Generator (Payslip) - OpenPDF
- Email/WhatsApp providers (pluggable)
- RabbitMQ (webhook delivery, notification queue)
- Flyway: `V1__init_hr.sql`, `V2__seed_benefits.sql`
- MongoDB AuditLog collection config

---

## 4. Frontend (erp-frontend-web)

**Framework:** Angular 22 (Standalone Components, Signals, New Control Flow)  
**CSS:** Tailwind CSS 4.x com Design Tokens customizados  
**Status:** ⚠️ **~5% COMPLETO - APENAS INFRAESTRUTURA CORE**

### 4.1 Estrutura Atual

```
src/app/
├── core/
│   ├── models/
│   │   └── user.model.ts          ✅ COMPLETO (296 linhas - User, Role, Permission, BootInfo, Tenant, Company, ModuleConfig, MenuItem, NumberingSeries, FiscalConfig, RFC7807Error, PaginatedResponse, Table/Column/Filter configs)
│   ├── services/
│   │   └── auth.service.ts        ✅ COMPLETO (Signals-based: token mgmt, boot info, permissions, roles, 2FA, session)
│   ├── validators/                ❌ VAZIO (CPF, CNPJ, CEP, Phone, IE)
│   ├── formatters/                ❌ VAZIO (BRL currency, dd/MM/yyyy, phone mask)
│   ├── guards/                    ❌ VAZIO (auth, permission, role, module-enabled)
│   └── interceptors/              ❌ VAZIO (auth header, RFC7807 error handling, loading)
├── shared/components/             ❌ 20+ DIRETÓRIOS VAZIOS
│   ├── data-table/                ❌ (server pagination, sorting, filtering)
│   ├── form-field/                ❌ (reactive forms + validators)
│   ├── modal/, toast/, breadcrumb/, sidebar/, tabs/, dropdown/, pagination/
│   ├── confirm-dialog/, file-upload/, color-picker/, avatar/, chip/, progress/, skeleton/, tooltip/
│   ├── directives/, pipes/, utils/
├── layouts/                       ❌ VAZIO
│   ├── admin-desk/                ❌ (Main ERP layout: header, sidebar, breadcrumb, router-outlet, user menu)
│   └── portal/                    ❌ (Customer/Supplier portal layout)
├── features/                      ❌ 7 MÓDULOS VAZIOS
│   ├── finance/                   ❌ (Chart of Accounts, Journal Entry, Fiscal Periods, Bank Reconciliation, Assets, Reports)
│   ├── inventory/                 ❌ (Items, Warehouses, Stock Balance, Valuation, Purchasing, Reposting)
│   ├── sales/                     ❌ (CRM Kanban, Quotation Builder, Sales Order, Delivery Note, Sales Invoice, Commissions)
│   ├── manufacturing/             ❌ (BOM Manager, Production Orders, Work Orders, Subcontracting, Projects, Quality)
│   ├── hr/                        ❌ (Employee Directory, Time Tracking, Leave, Payroll, Payslips, LGPD Audit)
│   ├── admin/                     ❌ (Companies, Users/Roles, Numbering Series, Fiscal Config, Audit Logs)
│   └── portal/                    ❌ (Customer Portal, Supplier Portal)
├── app.routes.ts                  ❌ VAZIO (exporta `Routes = []`)
├── app.config.ts                  ✅ Básico (router only)
└── main.ts / app.ts / index.html  ✅ Bootstrap básico
```

### 4.2 Funcionalidades Core Implementadas

| Componente | Status | Detalhes |
|------------|--------|----------|
| **User Model** | ✅ | 296 linhas, todos os contratos alinhados com backend |
| **AuthService** | ✅ | Signals (`signal`, `computed`, `effect`), token management, boot info loading, permission/role helpers, 2FA, session management |
| **Tailwind Config** | ✅ | Custom design tokens: colors (primary, secondary, success, warning, danger), spacing, borderRadius, boxShadow, fontFamily |
| **pt-BR Locale** | ⚠️ | Definido no model (`America/Sao_Paulo`, `pt-BR`) mas NÃO aplicado (LOCALE_ID, CurrencyPipe) |

### 4.3 Requisitos Técnicos - Compliance

| Requisito | Status | Observação |
|-----------|--------|------------|
| Standalone Components only | ⚠️ Parcial | AuthService usa `providedIn: 'root'`; sem NgModules; mas sem feature components para verificar |
| Signals para state management | ✅ | AuthService usa extensivamente |
| Reactive Forms com validadores BR | ❌ | Validators vazios; nenhum form implementado |
| pt-BR only (labels, messages) | ⚠️ Parcial | AuthService mensagens em pt-BR; sem UI components |
| Currency: BRL (R$) | ❌ | Formatters vazios; sem currency pipe |
| Date: dd/MM/yyyy | ❌ | Formatters vazios; sem date pipe |
| Timezone: America/Sao_Paulo | ⚠️ Model only | Definido no model mas não aplicado |
| Server-side pagination DataTables | ❌ | DataTable não implementado |
| RFC 7807 error handling | ❌ | ToastService referenciado mas não existe; sem interceptor |
| Lazy loading por feature route | ❌ | Routes array vazio |

### 4.4 Integração Backend - Pronto

| Requisito | Status |
|-----------|--------|
| BootInfo dinâmico (menus, permissions, modules) | ✅ Model ready - `BootInfo` interface inclui `menus: MenuItem[]`, `modules: ModuleConfig[]`; `AuthService` expõe `menus()`, `modules()`, `permissions()` como computed signals |
| Contratos alinhados Agent 01 | ✅ `user.model.ts` corresponde aos contratos backend (User, Role, Permission, Tenant, Company, ModuleConfig, MenuItem, NumberingSeries, FiscalConfig) |

### 4.5 Bloqueios Críticos
1. **ToastService missing** - `AuthService` importa e usa mas não existe → **quebra compilação**
2. **Nenhuma rota configurada** - App não navega
3. **Sem validadores** - Forms brasileiros impossíveis
4. **Sem shared components** - Feature modules sem building blocks

---

## 5. Infraestrutura & Deploy

### 5.1 Docker Compose - Desenvolvimento (`deploy/docker-compose.yml`)

**Status:** ✅ **IMPLEMENTADO**

| Serviço | Imagem | Porta | Healthcheck | Recursos |
|---------|--------|-------|-------------|----------|
| MySQL 8.4 | mysql:8.4 | 3306 | mysqladmin ping | 512M limit / 256M reservation |
| MongoDB 7.0 | mongo:7.0 | 27017 | mongosh ping | 512M / 256M |
| Redis 7 | redis:7-alpine | 6379 | redis-cli ping | 256M / 128M |
| RabbitMQ 3.13 | rabbitmq:3.13-management | 5672, 15672 | rabbitmq-diagnostics | 512M / 256M |
| pgAdmin 4 | dpage/pgadmin4:8 | 5050 | - | 256M (profile: tools) |
| Mongo Express | mongo-express:1.0 | 8081 | - | 128M (profile: tools) |
| Redis Commander | rediscommander/redis-commander | 8082 | - | 128M (profile: tools) |

**Volumes Persistentes:** `mysql_data`, `mongodb_data`, `redis_data`, `rabbitmq_data`, `pgadmin_data`

**Rede:** `erp-network-dev` (bridge)

### 5.2 Docker Compose - Produção (`deploy/docker-compose.prod.yml`)

**Status:** ✅ **IMPLEMENTADO**

| Serviço | Configurações de Produção |
|---------|---------------------------|
| MySQL | bind 127.0.0.1:3306, buffer pool 2G, binlog ROW, slow query log, restart policy, logging json-file 100m/5 |
| MongoDB | Replica Set `rs0`, wiredTiger 2GB, oplog 1GB, bind_ip_all, restart policy |
| Redis | Cluster mode, maxmemory 2GB, appendonly everysec, save policies, restart policy |
| RabbitMQ | Cluster, Erlang cookie, definitions.json, rabbitmq-prod.conf, enabled_plugins |
| MinIO | S3-compatible object storage, console :9001 |
| Prometheus | 30d retention, 10GB size, web.enable-lifecycle |
| Grafana | Admin creds via env, dashboards provisioning |
| Jaeger | Badger storage, OTLP enabled |
| Nginx | SSL termination :80/:443, nginx.conf, conf.d, ssl certs |

**Volumes:** mysql, mongodb, mongodb_config, redis, rabbitmq, minio, prometheus, grafana, jaeger, nginx_cache

**Rede:** `erp-network-prod` (bridge)

### 5.3 Environment Template (`deploy/env.deploy.template`)

**Status:** ✅ **IMPLEMENTADO** - 220 linhas com todas as variáveis

#### Categorias Principais

| Categoria | Variáveis Exemplos |
|-----------|-------------------|
| **Application** | `SPRING_PROFILES_ACTIVE=dev`, `APP_VERSION=1.0.0-SNAPSHOT`, portas dos 8 serviços |
| **MySQL** | `MYSQL_HOST=mysql`, `MYSQL_DATABASE=erp_dev`, `MYSQL_USER/PASSWORD`, `MYSQL_MAX_POOL_SIZE=20`, databases por serviço |
| **MongoDB** | `MONGO_HOST=mongodb`, `MONGO_URI=mongodb://admin:adminpassword@mongodb:27017/erp_dev?authSource=admin` |
| **Redis** | `REDIS_HOST=redis`, `REDIS_PORT=6379`, `REDIS_MAX_ACTIVE=20` |
| **RabbitMQ** | `RABBITMQ_HOST=rabbitmq`, `RABBITMQ_VHOST=/erp`, `RABBITMQ_CONCURRENCY=5` |
| **JWT Security** | `JWT_SECRET=...` (256 bits), `JWT_ACCESS_TOKEN_VALIDITY=3600`, `JWT_REFRESH_TOKEN_VALIDITY=604800`, `JWT_ISSUER=erp-brazil` |
| **CORS** | `CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost:3000` |
| **Events** | `EVENTS_EXCHANGE=erp.events`, `EVENTS_ROUTING_KEY_PREFIX=erp` |
| **Outbox** | `OUTBOX_BATCH_SIZE=100`, `OUTBOX_POLL_INTERVAL=5000` |
| **OpenAPI** | `OPENAPI_DEV_URL=http://localhost:8080`, `OPENAPI_PROD_URL=https://api.erp.brazil.com` |
| **Distributed Lock** | `LOCK_DEFAULT_WAIT_TIME=10`, `LOCK_DEFAULT_LEASE_TIME=30` |
| **Monitoring** | Prometheus 9090, Grafana 3000, Jaeger 6831 |
| **Email** | SMTP config (opcional) |
| **MinIO/S3** | `MINIO_HOST=minio`, `MINIO_ACCESS_KEY/SECRET_KEY`, `MINIO_BUCKET=erp-files` |
| **Feature Flags** | `FEATURE_MULTI_TENANT=true`, `FEATURE_AUDIT_LOG=true`, `FEATURE_OUTBOX_PATTERN=true`, `FEATURE_DISTRIBUTED_LOCK=true`, `FEATURE_RATE_LIMITING=true`, `FEATURE_CIRCUIT_BREAKER=true` |
| **Logging** | `LOG_LEVEL_ROOT=INFO`, `LOG_LEVEL_COM_ERP_BRAZIL=DEBUG`, `LOG_FILE_PATH=/var/log/erp` |

### 5.4 Perfis Maven

| Perfil | Ativação | Uso |
|--------|----------|-----|
| `dev` | Default (activeByDefault) | Desenvolvimento local |
| `prod` | `-Pprod` | Produção |
| `test` | `-Ptest` | Testes (Testcontainers) |

---

## 6. Segurança & Conformidade

### 6.1 Autenticação e Autorização (JWT + RBAC)

| Componente | Implementação |
|------------|---------------|
| **Access Token** | JWT RS256, validade 1h, claims: `sub` (userId), `tenant_id`, `company_id`, `permissions[]`, `roles[]` |
| **Refresh Token** | JWT, validade 7 dias, armazenado em Redis (revogável) |
| **Token Provider** | `JwtTokenProvider` (erp-common-core) - geração, validação, parsing |
| **Gateway Filter** | `JwtAuthenticationFilter` - valida em todas as rotas exceto `/health`, `/actuator/health` |
| **RBAC** | Permission-based: `resource:action` (ex: `journal-entry:post`), Roles agregam Permissions |
| **Guards Frontend** | `authGuard`, `permissionGuard`, `roleGuard`, `moduleGuard` (planejados, não implementados) |

### 6.2 Multi-Tenancy

- **Tenant Resolution**: Header `X-Tenant-ID` (gateway) → `TenantContext` (thread-local)
- **Database**: Coluna `tenant_id`/`company_id` em TODAS as entidades (BaseEntity não tem, mas serviços adicionam)
- **Isolamento**: Queries automaticamente filtradas por tenant via `TenantContext`
- **Frontend**: `TenantService` gerencia company/tenant ativo, `BootInfo` carrega configuração por tenant

### 6.3 LGPD (Lei Geral de Proteção de Dados)

| Requisito | Status | Implementação |
|-----------|--------|---------------|
| **Data Masking** | ❌ | AOP aspect planejado para CPF/sensitive fields (hr-audit-service) |
| **DSAR Export** | ❌ | `GET /api/v1/lgpd/export/{userId}` - não implementado |
| **Data Erasure** | ❌ | `POST /api/v1/lgpd/erase/{userId}` - não implementado |
| **Audit Trail** | ✅ Modelo | `AuditEvent` (MongoDB) com oldValue/newValue JSON, action enum |
| **Consent Tracking** | ⚠️ Model | `LGPDConsent` entity em sales-crm-service (não implementada) |
| **PII Encryption** | ❌ | `CertificadoDigitalA1` com AES-256-GCM planejado |

### 6.4 Certificado Digital A1 (ICP-Brasil)

- **Armazenamento**: `CertificadoDigitalA1` entity com campos criptografados (AES-256-GCM)
- **Monitoramento**: Job agendado (Quartz) para expiração (30/15/7/1 dias)
- **Uso**: Assinatura NF-e, NFS-e, CT-e, Payslip (hr-audit-service)
- **Status**: Modelo planejado, não implementado

### 6.5 Auditoria Completa

| Camada | Implementação |
|--------|---------------|
| **Entidades** | `BaseEntity` - createdAt/By, updatedAt/By, version, soft delete |
| **Eventos Domínio** | `DomainEvent` - correlationId, causationId, tenantId, userId, companyId |
| **Outbox** | `OutboxEvent` - transactional outbox pattern, retry, dead-letter |
| **MongoDB Audit** | `AuditEvent` - capped collection, TTL 7 anos, PII masked |
| **AOP Audit** | Planejado para hr-audit-service (auto-mask CPF) |

---

## 7. O que está Completo vs Pendente

### 7.1 Resumo por Componente

| Componente | % Completo | Status |
|------------|------------|--------|
| **erp-common-core** | 100% | ✅ COMPLETO - Todas as 4 camadas |
| **api-gateway** | 100% | ✅ COMPLETO - Rotas, filtros, circuit breaker |
| **identity-tenant-service** | 100% | ✅ COMPLETO - Domínio, API, eventos, Flyway |
| **finance-accounting-service** | 15% | ⚠️ PARCIAL - Apenas 8/28 modelos |
| **fiscal-brazil-service** | 10% | ⚠️ PARCIAL - Apenas 4/15+ modelos |
| **inventory-supply-service** | 35% | ⚠️ PARCIAL - 14/20 modelos, sem camadas superiores |
| **manufacturing-projects-service** | 30% | ⚠️ PARCIAL - 12/18 grupos modelos, sem camadas superiores |
| **sales-crm-service** | 20% | ⚠️ PARCIAL - 8 entidades + 11 enums, 14+ faltando |
| **hr-audit-service** | 5% | ❌ SCAFFOLDING APENAS - Apenas pom.xml |
| **erp-frontend-web** | 5% | ❌ CORE APENAS - AuthService + Model, sem features |

### 7.2 Gaps Críticos (Bloqueadores)

| Gap | Impacto | Serviços Afetados |
|-----|---------|-------------------|
| **Parent POM incorreto** | Version drift, não usa erp-common-core | Todos os 7 serviços backend |
| **erp-common-core não publicado** | Dependências não resolvem | Todos os serviços |
| **Flyway migrations ausentes** | Schema não criado | Finance, Fiscal, Inventory, Manufacturing, Sales, HR |
| **Use Cases ausentes** | Sem lógica de negócio | Finance, Fiscal, Inventory, Manufacturing, Sales, HR |
| **Repositories ausentes** | Sem persistência | Finance, Fiscal, Inventory, Manufacturing, Sales, HR |
| **Configuração dev/prod ausente** | Não roda em ambiente real | Finance, Fiscal, Inventory, Manufacturing, Sales, HR |
| **Frontend: ToastService missing** | Quebra compilação | erp-frontend-web |
| **Frontend: Rotas vazias** | App não navega | erp-frontend-web |
| **Frontend: Shared components vazios** | Impossível construir features | erp-frontend-web |

### 7.3 Estimativa de Esforço para Completar

| Componente | Horas Estimadas | Semanas (1 dev) |
|------------|-----------------|-----------------|
| finance-accounting-service | 80-120h | 2-3 |
| fiscal-brazil-service | 160-200h | 4-5 |
| inventory-supply-service | 80-120h | 2-3 |
| manufacturing-projects-service | 120-160h | 3-4 |
| sales-crm-service | 120-160h | 3-4 |
| hr-audit-service | 160-240h | 4-6 |
| erp-frontend-web | 200-300h | 5-8 (2-3 devs paralelos) |
| **TOTAL** | **920-1300h** | **~6-8 meses (equipe 3-4)** |

---

## 8. Como Executar

### 8.1 Pré-requisitos

- Java 21 (JDK)
- Maven 3.9+
- Node.js 20+ / npm 10+
- Docker 24+ / Docker Compose 2.24+
- Git

### 8.2 Desenvolvimento Local

```bash
# 1. Clonar repositório
cd /var/home/anderson/Projetos/ACDX_ERP/erp-brazil

# 2. Subir infraestrutura (MySQL, MongoDB, Redis, RabbitMQ)
cd deploy
docker-compose up -d

# 3. Verificar saúde dos containers
docker-compose ps
docker-compose logs -f mysql    # Aguardar "ready for connections"

# 4. Publicar erp-common-core no Maven local (OBRIGATÓRIO)
cd ../erp-common-core
./mvnw clean install -DskipTests

# 5. Corrigir parent POM nos 7 serviços (necessário antes de buildar)
# Editar pom.xml de cada serviço para usar:
# <parent>
#   <groupId>com.erp.brazil</groupId>
#   <artifactId>erp-brazil-parent</artifactId>
#   <version>1.0.0-SNAPSHOT</version>
#   <relativePath>../pom.xml</relativePath>
# </parent>

# 6. Buildar serviços (após correção do parent)
cd ../identity-tenant-service && ./mvnw clean compile
cd ../api-gateway && ./mvnw clean compile
# ... demais serviços

# 7. Executar serviços (ordem sugerida)
# Terminal 1: identity-tenant-service
./mvnw spring-boot:run -Dspring.profiles.active=dev

# Terminal 2: api-gateway
./mvnw spring-boot:run -Dspring.profiles.active=dev

# Terminal 3: demais serviços conforme necessidade

# 8. Frontend
cd ../erp-frontend-web
npm install
npm start   # http://localhost:4200
```

### 8.3 Perfis de Execução

| Perfil | Comando | Descrição |
|--------|---------|-----------|
| **dev** | `-Dspring.profiles.active=dev` | H2/SQLite em memória, logs DEBUG, hot reload |
| **prod** | `-Dspring.profiles.active=prod` | MySQL/MongoDB/Redis/RabbitMQ reais, logs WARN, otimizações |
| **test** | `-Dspring.profiles.active=test` | Testcontainers para testes de integração |

### 8.4 Produção

```bash
# 1. Preparar .env com variáveis de produção (baseado em env.deploy.template)
cd deploy
cp env.deploy.template .env
# Editar .env com senhas seguras, JWT_SECRET 256 bits, etc.

# 2. Subir stack completa
docker-compose -f docker-compose.prod.yml --env-file .env up -d

# 3. Verificar
docker-compose -f docker-compose.prod.yml ps
docker-compose -f docker-compose.prod.yml logs -f nginx

# 4. Acessar
# - API Gateway: https://api.erp.brazil.com (via Nginx SSL)
# - Frontend: https://erp.brazil.com (via Nginx SSL)
# - Grafana: http://localhost:3000 (apenas localhost)
# - Prometheus: http://localhost:9090
# - Jaeger: http://localhost:16686
# - RabbitMQ Management: http://localhost:15672
```

### 8.5 Testes

```bash
# Testes unitários (todos os módulos)
./mvnw test

# Testes de integração (perfil test)
./mvnw verify -Ptest

# Frontend
cd erp-frontend-web
npm test        # Unit tests (Jest)
npm run e2e     # E2E tests (Cypress/Playwright)
```

---

## 9. Decisões Técnicas (ADRs)

### ADR-001: Arquitetura de Microsserviços com Database per Service
**Data:** 2026-11-15  
**Status:** Aceito  
**Decisão:** Cada serviço possui seu próprio schema/database MySQL. Comunicação via API Gateway (sync) e RabbitMQ (async).  
**Justificativa:** Isolamento de falhas, deploy independente, escalabilidade por domínio, propriedade clara de dados.  
**Consequências:** Complexidade operacional maior, necessidade de saga pattern para transações distribuídas, eventual consistency.

### ADR-002: Transactional Outbox Pattern para Eventos
**Data:** 2026-11-15  
**Status:** Aceito  
**Decisão:** Eventos de domínio persistem na tabela `outbox_events` na mesma transação do negócio. Poller assíncrono publica no RabbitMQ.  
**Justificativa:** Garante atomicidade entre operação de negócio e publicação de evento. Evita dual-write problem.  
**Consequências:** Latência de publicação (~5s poll interval), necessidade de idempotência nos consumers, dead-letter handling.

### ADR-003: Multi-Tenancy via Coluna tenant_id + ThreadLocal
**Data:** 2026-11-15  
**Status:** Aceito  
**Decisão:** Todas as entidades carregam `tenant_id`/`company_id`. `TenantContext` (ThreadLocal) propaga contexto na request.  
**Justificativa:** Simples, performático, compatível com JPA/Hibernate filters, isolamento em nível de aplicação.  
**Consequências:** Cuidado com vazamento de ThreadLocal em async (usar `ContextPropagator`), queries manuais devem filtrar tenant.

### ADR-004: Money Value Object para BRL (BigDecimal)
**Data:** 2026-11-15  
**Status:** Aceito  
**Decisão:** `Money` imutável com `BigDecimal`, scale 2, `RoundingMode.HALF_EVEN`. Operações retornam novas instâncias.  
**Justificativa:** Precisão financeira obrigatória, imutabilidade evita bugs, formatação pt-BR nativa.  
**Consequências:** Overhead de objetos, não usar `double`/`float` em nenhum lugar do domínio financeiro.

### ADR-005: Brazilian Validators como Annotations + Implementação Centralizada
**Data:** 2026-11-15  
**Status:** Aceito  
**Decisão:** `@ValidCpf`, `@ValidCnpj`, `@ValidCep`, `@ValidPhone`, `@ValidStateRegistration` com validadores em `BrazilianValidators`.  
**Justificativa:** Reutilização cross-service, declarativo, integração Bean Validation, testes centralizados.  
**Consequências:** Dependência do erp-common-core, versão única de regras de validação.

### ADR-006: Spring Cloud Gateway como API Gateway Único
**Data:** 2026-11-15  
**Status:** Aceito  
**Decisão:** Gateway centralizado com filtros: JWT, Tenant Resolution, Rate Limiting, Circuit Breaker, Retry.  
**Justificativa:** Ponto único de entrada, cross-cutting concerns centralizados, service discovery via `lb://`.  
**Consequências:** Single point of failure (mitigado com replica), latência extra (~5-10ms), configuração complexa de rotas.

### ADR-007: Angular 22 Standalone + Signals + Tailwind 4
**Data:** 2026-11-15  
**Status:** Aceito  
**Decisão:** Standalone components only, Signals para state management, Tailwind CSS 4 com design tokens custom.  
**Justificativa:** Modern Angular (sem NgModules), reactivity fina com Signals, utility-first CSS consistente.  
**Consequências:** Curva de aprendizado Signals, migração de libs legadas, Tailwind 4 breaking changes.

### ADR-008: Certificado Digital A1 com AES-256-GCM
**Data:** 2026-11-15  
**Status:** Planejado  
**Decisão:** Armazenar certificado A1 (PFX) criptografado com AES-256-GCM no banco. Chave mestra em vault/env.  
**Justificativa:** Conformidade ICP-Brasil, proteção de chave privada, rotação de chave mestra.  
**Consequências:** Complexidade de key management, performance de encriptação/decriptação, auditoria de acesso.

### ADR-009: Stock Ledger Entry Imutável (Perpetual Inventory)
**Data:** 2026-11-15  
**Status:** Aceito  
**Decisão:** `StockLedgerEntry` nunca atualizado - apenas inserts. `runningBalance` calculado por partição (company+date).  
**Justificativa:** Auditoria completa, rastreabilidade total, performance de leitura (append-only), particionamento nativo.  
**Consequências:** Volume de dados maior, recálculo de valuation assíncrono (RepostingJob), complexidade de consultas de saldo atual.

### ADR-010: Clean Architecture com Use Cases (CQRS Leve)
**Data:** 2026-11-15  
**Status:** Aceito  
**Decisão:** Domain → Application (Use Cases: Command/Query/PagedQuery/CursorQuery) → Infrastructure → Presentation.  
**Justificativa:** Testabilidade, separação de responsabilidades, independência de framework, domain-driven.  
**Consequências:** Boilerplate inicial maior, curva de aprendizado, mapeamento DTO ↔ Entity necessário (MapStruct).

---

## Apêndice A: Estrutura de Diretórios do Monorepo

```
/var/home/anderson/Projetos/ACDX_ERP/erp-brazil/
├── pom.xml                                    # Parent POM (8 módulos)
├── mvnw / mvnw.cmd                            # Maven Wrapper
├── .mvn/wrapper/                              # Wrapper config
├── deploy/
│   ├── docker-compose.yml                     # Dev infrastructure
│   ├── docker-compose.prod.yml                # Prod infrastructure
│   ├── env.deploy.template                    # Environment variables template
│   ├── mysql/conf.d/, init/, init-prod/       # MySQL config/scripts
│   ├── mongodb/init/                          # MongoDB init scripts
│   ├── redis/redis.conf, redis-prod.conf      # Redis config
│   ├── rabbitmq/definitions.json, rabbitmq.conf, rabbitmq-prod.conf
│   ├── nginx/nginx.conf, conf.d/, ssl/        # Nginx config
│   ├── prometheus/prometheus.yml, rules/      # Prometheus config
│   └── grafana/dashboards/, provisioning/     # Grafana config
├── docs/
│   ├── architecture/                          # Verification reports (agents 01-08)
│   ├── api/                                   # OpenAPI specs (planejado)
│   └── runbooks/                              # Operational runbooks (planejado)
├── erp-common-core/                           # ✅ COMPLETO
│   ├── pom.xml
│   └── src/main/java/com/erp/brazil/common/
│       ├── domain/model/                      # BaseEntity, Money, TenantContext, NumberingSeries
│       ├── domain/event/                      # DomainEvent, AuditEvent, OutboxEvent
│       ├── domain/validation/                 # BrazilianValidators, annotations
│       ├── application/usecase/               # UseCase interfaces
│       ├── application/response/              # PageableResponse, CursorPage, ProblemDetail
│       ├── infrastructure/security/           # JwtTokenProvider, JwtAuthFilter, JwtUserDetails
│       ├── infrastructure/lock/               # DistributedLock (Redisson)
│       ├── infrastructure/event/              # DomainEventPublisher, OutboxEventPublisher
│       ├── infrastructure/config/             # OpenApiConfig, TestcontainersConfig
│       ├── infrastructure/audit/              # AuditAwareImpl
│       └── presentation/                      # BaseController, GlobalExceptionHandler
├── api-gateway/                               # ✅ COMPLETO
│   ├── pom.xml
│   └── src/main/java/com/erp/brazil/gateway/
│       ├── config/GatewayConfig.java          # Rotas + filtros
│       ├── filter/JwtAuthenticationFilter.java
│       ├── filter/TenantResolutionFilter.java
│       ├── filter/RateLimitingFilter.java
│       └── handler/GlobalErrorHandler.java
├── identity-tenant-service/                   # ✅ COMPLETO
│   ├── pom.xml
│   └── src/main/java/com/erp/brazil/identity/
│       ├── domain/model/                      # Company, User, Role, Permission, UserCompanyRole, Branch, Department, CostCenter, BootInfo, NumberingSeries
│       ├── domain/event/                      # CompanyCreatedEvent, UserCreatedEvent, RoleCreatedEvent
│       ├── application/service/               # AuthService, CompanyService, BootInfoService
│       ├── infrastructure/                    # SecurityConfig, JpaConfig, repositories
│       └── presentation/controller/           # AuthController, CompanyController, BootInfoController
├── finance-accounting-service/                # ⚠️ PARCIAL (8/28 modelos)
├── fiscal-brazil-service/                     # ⚠️ PARCIAL (4/15+ modelos)
├── inventory-supply-service/                  # ⚠️ PARCIAL (14/20 modelos)
├── manufacturing-projects-service/            # ⚠️ PARCIAL (12/18 grupos)
├── sales-crm-service/                         # ⚠️ PARCIAL (8 entidades + 11 enums)
├── hr-audit-service/                          # ❌ SCAFFOLDING APENAS
│   └── hr-audit-service/                      # Subdiretório duplicado
│       ├── pom.xml
│       └── src/main/java/com/erp/brazil/hr/   # Apenas Application class
└── erp-frontend-web/                          # ❌ ~5% (Core apenas)
    ├── package.json, angular.json, tailwind.config.js
    └── src/app/
        ├── core/                              # AuthService ✅, user.model.ts ✅, validators/formatters/guards/interceptors ❌
        ├── shared/components/                 # 20+ dirs vazios
        ├── layouts/                           # admin-desk/, portal/ vazios
        ├── features/                          # 7 módulos vazios
        └── app.routes.ts                      # Routes = []
```

---

## Apêndice B: Comandos Úteis

```bash
# Build todo o projeto (após corrigir parent POMs)
./mvnw clean install -DskipTests

# Build serviço específico
cd finance-accounting-service && ./mvnw clean compile

# Executar testes com Testcontainers
./mvnw verify -Ptest

# Gerar documentação OpenAPI (após implementar)
./mvnw spring-boot:run -Dspringdoc.api-docs.enabled=true

# Verificar dependências desatualizadas
./mvnw versions:display-dependency-updates

# Limpar containers Docker
docker-compose down -v
docker system prune -f

# Logs de um serviço específico
docker-compose logs -f mysql
docker-compose logs -f rabbitmq
```

---

**Fim da Documentação**

*Documento gerado automaticamente baseado na análise do código-fonte real do monorepo em 15/11/2026. Reflete o estado ATUAL da implementação, não a especificação original.*