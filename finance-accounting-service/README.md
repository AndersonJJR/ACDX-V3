# Finance Accounting Service

ERP Brazil - Finance, Accounting & Assets Service

## Overview

This service provides comprehensive financial management capabilities including:

- **General Ledger**: Journal entries, chart of accounts, fiscal periods, GL entries
- **Accounts Payable**: Vendor management, payable invoices, payments
- **Accounts Receivable**: Customer management, receivable invoices, receipts
- **Bank Management**: Bank accounts, statements, reconciliation
- **Fixed Assets**: Asset lifecycle, depreciation schedules, depreciation methods
- **Financial Reports**: Trial balance, balance sheet, profit & loss statement
- **Cost Centers**: Cost center hierarchy and budgeting

## Architecture

Clean Architecture with the following layers:

```
src/main/java/com/erp/brazil/finance/
├── domain/
│   ├── model/          # JPA Entities (ChartOfAccounts, JournalEntry, Asset, etc.)
│   ├── repository/     # Repository interfaces
│   └── event/          # Domain events
├── application/
│   ├── usecase/        # Use cases (PostJournalEntry, RunDepreciation, etc.)
│   └── dto/            # Data Transfer Objects (records)
├── infrastructure/
│   ├── persistence/    # JPA implementations, Flyway migrations
│   ├── messaging/      # RabbitMQ event publishers/consumers
│   └── config/         # Configuration classes
└── presentation/
    └── rest/           # REST Controllers
```

## Technology Stack

- **Java 21**
- **Spring Boot 4.1.0**
- **Spring Data JPA** (MySQL/PostgreSQL)
- **Spring Data MongoDB** (for document storage)
- **Spring AMQP** (RabbitMQ)
- **Flyway** (Database migrations)
- **Quartz** (Job scheduling)
- **Spring Security** (OAuth2 Resource Server)
- **SpringDoc OpenAPI** (API documentation)
- **Testcontainers** (Integration tests)
- **JaCoCo** (Code coverage)

## Domain Model

### Core Entities

- **FiscalYear/FiscalPeriod**: Multi-year fiscal calendar with locking
- **ChartOfAccounts**: Hierarchical Brazilian chart of accounts (Plano de Contas)
- **JournalEntry/JournalEntryLine**: Double-entry bookkeeping
- **GLEntry**: General ledger entries with running balances
- **CostCenter**: Cost center hierarchy
- **Asset/AssetDepreciationSchedule/DepreciationEntry**: Fixed asset management
- **BankAccount/BankStatement/Reconciliation**: Banking operations
- **Vendor/Customer**: Business partner management
- **PayableInvoice/ReceivableInvoice**: Invoice management
- **Payment/PaymentAllocation**: Payment processing

### Brazilian Chart of Accounts

Includes standard Brazilian account structure (Plano de Contas Padrão):
- 1 - Ativo (Assets)
- 2 - Passivo (Liabilities)
- 3 - Patrimônio Líquido (Equity)
- 4 - Receitas (Revenues)
- 5 - Despesas/Custos (Expenses/Costs)
- 6 - Resultados (Results)

## API Endpoints

### Journal Entries
- `POST /api/v1/journal-entries` - Create journal entry
- `GET /api/v1/journal-entries/{id}` - Get journal entry
- `GET /api/v1/journal-entries` - List with filters
- `POST /api/v1/journal-entries/{id}/post` - Post draft entry
- `POST /api/v1/journal-entries/{id}/reverse` - Reverse posted entry

### Chart of Accounts
- `POST /api/v1/chart-of-accounts` - Create account
- `GET /api/v1/chart-of-accounts/{id}` - Get account
- `GET /api/v1/chart-of-accounts` - List accounts
- `GET /api/v1/chart-of-accounts/tree` - Get hierarchical tree

### Fiscal Periods
- `POST /api/v1/fiscal-periods` - Create period
- `GET /api/v1/fiscal-periods` - List periods
- `POST /api/v1/fiscal-periods/{id}/lock` - Lock period
- `POST /api/v1/fiscal-periods/{id}/unlock` - Unlock period

### Assets
- `POST /api/v1/assets` - Create asset
- `GET /api/v1/assets` - List assets
- `POST /api/v1/assets/{id}/dispose` - Dispose asset
- `POST /api/v1/assets/depreciation/run` - Run depreciation

### Bank Reconciliation
- `POST /api/v1/bank-reconciliation/statements/import` - Import statement
- `GET /api/v1/bank-reconciliation/statements` - List statements
- `POST /api/v1/bank-reconciliation/statements/{id}/reconcile` - Auto-reconcile

### Payables/Receivables
- `POST /api/v1/payables` - Create payable invoice
- `GET /api/v1/payables` - List payables
- `GET /api/v1/payables/overdue` - Get overdue
- `POST /api/v1/receivables` - Create receivable invoice
- `GET /api/v1/receivables` - List receivables

### Reports
- `GET /api/v1/reports/trial-balance` - Trial balance
- `GET /api/v1/reports/balance-sheet` - Balance sheet
- `GET /api/v1/reports/profit-loss` - Profit & Loss

## Configuration

### Profiles

- **dev**: SQLite, ddl-auto=create-drop, Flyway disabled
- **prod**: MySQL, Redis, MongoDB, RabbitMQ, Flyway enabled

### Key Properties

```yaml
app:
  finance:
    default-currency: BRL
    decimal-places: 2
    fiscal-year-start-month: 1
    fiscal-year-start-day: 1
  messaging:
    exchange: erp.events
    routing-key:
      journal-entry-posted: journal.entry.posted
      payment-processed: payment.processed
      fiscal-period-locked: fiscal.period.locked
      asset-depreciation-posted: asset.depreciation.posted
```

## Running Locally

### Prerequisites

- Java 21
- Maven 3.9+
- Docker (for Testcontainers)

### Development

```bash
# Run with dev profile (SQLite)
./mvnw spring-boot:run -Pdev

# Run with MySQL (requires Docker)
docker run -d --name mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=erp_finance -p 3306:3306 mysql:8.0
./mvnw spring-boot:run
```

### Testing

```bash
# Run unit tests
./mvnw test

# Run integration tests (requires Docker)
./mvnw verify

# Generate coverage report
./mvnw jacoco:report
```

## Database Migrations

Flyway migrations in `src/main/resources/db/migration/`:
- `V1__init_finance.sql` - Core schema
- `V2__chart_of_accounts_br.sql` - Brazilian chart of accounts template

## Event Publishing

Domain events published to RabbitMQ:
- `JournalEntryPostedEvent`
- `PaymentProcessedEvent`
- `FiscalPeriodLockedEvent`
- `AssetDepreciationPostedEvent`

Events consumed from other services:
- `InvoiceValidatedEvent` (from fiscal service)
- `GoodsReceivedEvent` (from inventory service)
- `PurchaseOrderApprovedEvent` (from procurement service)

## Code Quality

- All code in English
- Records for DTOs
- Sealed classes for domain hierarchies
- Pattern matching in use cases
- Virtual thread friendly (no blocking in controllers)
- RFC 7807 error handling
- Pagination on all list endpoints
- OpenAPI annotations
- Unit tests with >80% coverage target (JaCoCo)
- Integration tests with Testcontainers

## License

Proprietary - ERP Brazil