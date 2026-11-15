# Inventory & Supply Chain Service

ERP Brazil - Inventory & Supply Chain Service implementing perpetual inventory, stock ledger entries (SLE), distributed locks, 3-way matching, and purchasing workflows.

## Features

### Core Inventory
- **Perpetual Inventory**: Real-time stock tracking with Stock Ledger Entries (SLE)
- **Multiple Valuation Methods**: FIFO, Moving Average, Standard Cost
- **Batch & Serial Number Tracking**: Full traceability
- **Multi-Warehouse**: Inter-warehouse transfers with in-transit tracking
- **Stock Reservations**: Distributed locking with Redisson for concurrency control

### Purchasing Workflow
- **Purchase Orders**: Complete PO lifecycle (Draft → Approved → Received → Billed)
- **Purchase Receipts**: Goods receipt with quality inspection
- **Purchase Invoices**: 3-way matching (PO vs Receipt vs Invoice)
- **Supplier Management**: Quotations, approval workflow

### Advanced Features
- **Reposting/Valuation Recalculation**: Async, chunked, idempotent
- **Material Requests**: Internal material issuance
- **Event-Driven Architecture**: RabbitMQ for cross-service communication
- **Partitioned SLE Table**: Monthly partitions for performance

## Architecture

```
inventory-supply-service/
├── domain/
│   ├── model/          # 20 JPA Entities
│   └── repository/     # 17 Spring Data JPA Repositories
├── application/
│   └── usecase/        # 6 Use Cases
├── infrastructure/
│   ├── config/         # Redisson, RabbitMQ, Flyway
│   ├── lock/           # RedissonDistributedLock
│   ├── messaging/      # Event consumers & publishers
│   └── repository/     # Custom repository implementations
└── presentation/
    └── rest/           # 7 REST Controllers
```

## Domain Models (20)

1. **Warehouse** - Physical/virtual storage locations
2. **Location** - Bins within warehouses (RECEIVING, STORAGE, PICKING, etc.)
3. **Item** - Master product data with valuation method
4. **ItemVariant** - Product variants (size, color, etc.)
5. **ItemBatch** - Batch/lot tracking with expiry
6. **ItemSerialNumber** - Individual serial tracking
7. **SerialAndBatchBundle** - Serial-batch relationships
8. **StockLedgerEntry** - Immutable stock movement records
9. **StockReservation** - Reserved quantities with expiry
10. **Supplier** - Vendor master data
11. **SupplierQuotation** - Supplier quotes
12. **PurchaseOrder** - Purchase orders
13. **PurchaseOrderItem** - PO line items
14. **PurchaseReceipt** - Goods receipts
15. **PurchaseReceiptItem** - Receipt line items
16. **PurchaseInvoice** - Supplier invoices
17. **PurchaseInvoiceItem** - Invoice line items with 3-way match
18. **MaterialRequest** - Internal material requests
19. **MaterialRequestItem** - MR line items
20. **RepostingJob** - Valuation recalculation jobs

## Use Cases (6)

1. **ReserveStockUseCase** - Distributed lock + stock check + reservation
2. **UnreserveStockUseCase** - Release reservations
3. **ReceiveGoodsUseCase** - Create SLE + update batches/serials
4. **ProcessPurchaseInvoiceUseCase** - 3-way match (qty + price)
5. **RecalculateValuationUseCase** - Async FIFO/Moving Average recalc
6. **TransferStockUseCase** - Inter-warehouse transfers

## REST Endpoints

### Stock
- `GET /api/v1/stock/balance` - Current stock balance
- `GET /api/v1/stock/ledger` - Stock ledger entries (paginated)
- `GET /api/v1/stock/valuation-report` - Valuation report

### Items
- `GET/POST/PUT/DELETE /api/v1/items` - Item CRUD
- `GET /api/v1/items/{id}/variants` - Item variants
- `GET /api/v1/items/{id}/batches` - Item batches
- `GET /api/v1/items/{id}/serials` - Item serial numbers

### Warehouses
- `GET/POST/PUT/DELETE /api/v1/warehouses` - Warehouse CRUD
- `GET /api/v1/warehouses/{id}/locations` - Warehouse locations

### Purchase Orders
- `GET/POST/PUT /api/v1/purchase-orders` - PO CRUD
- `POST /api/v1/purchase-orders/{id}/approve` - Approve PO
- `GET /api/v1/purchase-orders/{id}/receipts` - PO receipts

### Purchase Receipts
- `GET/POST/PUT /api/v1/purchase-receipts` - Receipt CRUD
- `POST /api/v1/purchase-receipts/{id}/receive` - Receive goods

### Purchase Invoices
- `GET/POST/PUT /api/v1/purchase-invoices` - Invoice CRUD
- `POST /api/v1/purchase-invoices/{id}/match` - Run 3-way match

### Reposting
- `POST /api/v1/reposting/trigger` - Trigger valuation recalc
- `GET /api/v1/reposting/{id}/status` - Job status
- `GET /api/v1/reposting/history` - Job history

## Event Publishing

| Event | Routing Key | Description |
|-------|-------------|-------------|
| StockReservedEvent | stock.reserved | Stock reserved for sales order |
| StockUnreservedEvent | stock.unreserved | Stock reservation released |
| GoodsReceivedEvent | goods.received | Goods received (triggers 3-way match) |
| PurchaseInvoiceMatchedEvent | invoice.matched | 3-way match successful |
| PurchaseInvoiceMismatchEvent | invoice.mismatch | 3-way match failed |
| ValuationRecalculatedEvent | valuation.recalculated | Reposting completed |
| StockTransferredEvent | stock.transferred | Inter-warehouse transfer |

## Event Consumption

| Event | Queue | Handler |
|-------|-------|---------|
| SalesOrderConfirmedEvent | sales.order.confirmed | ReserveStockUseCase |
| SalesOrderCancelledEvent | sales.order.cancelled | UnreserveStockUseCase |
| ProductionOrderCompletedEvent | production.order.completed | ReceiveGoodsUseCase |
| RepostingTriggerEvent | inventory.reposting.trigger | RecalculateValuationUseCase |
| PurchaseInvoiceCreatedEvent | finance.purchase_invoice.created | ProcessPurchaseInvoiceUseCase |

## Configuration

### Profiles
- **dev**: SQLite, Flyway disabled, SQL logging
- **prod**: MySQL + Redis + MongoDB + RabbitMQ, Flyway enabled

### Key Properties
```yaml
app:
  inventory:
    default-valuation-method: MOVING_AVERAGE
    allow-negative-stock: false
    reservation-expiry-days: 30
    lock-lease-seconds: 30
  three-way-match:
    qty-tolerance: 0.01
    rate-tolerance: 0.01
  reposting:
    chunk-size: 1000
```

## Database Partitioning

The `stock_ledger_entries` table is partitioned by `company_id + posting_date` (monthly partitions) for query performance on large datasets.

```sql
PARTITION BY LIST COLUMNS (partition_key) (
    PARTITION p_202401 VALUES IN ('company-id_202401'),
    PARTITION p_202402 VALUES IN ('company-id_202402'),
    ...
    PARTITION p_future VALUES IN (DEFAULT)
)
```

## Running the Service

### Development
```bash
# Using Maven
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Using Docker
docker-compose -f docker-compose.dev.yml up
```

### Production
```bash
# Build
./mvnw clean package -Pprod

# Run
java -jar target/inventory-supply-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Environment Variables
```bash
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/inventory_db
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=secret
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_PORT=5672
SPRING_MONGODB_URI=mongodb://localhost:27017/inventory_audit
SPRING_SECURITY_OAUTH2_ISSUER_URI=http://keycloak:8080/realms/erp
```

## API Documentation

Swagger UI: `http://localhost:8083/api/swagger-ui.html`
OpenAPI Spec: `http://localhost:8083/api/v3/api-docs`

## Testing

```bash
# Unit tests
./mvnw test

# Integration tests with Testcontainers
./mvnw verify -Pintegration-tests
```

## Technology Stack

- **Java 21** - Records, Sealed Classes, Pattern Matching
- **Spring Boot 3.3** - Web, Data JPA, Security, AMQP, Redis, MongoDB, Flyway
- **MySQL 8.0** - Partitioned tables
- **Redis** - Redisson distributed locks
- **RabbitMQ** - Event messaging
- **MongoDB** - Audit logs
- **Flyway** - Database migrations
- **MapStruct** - DTO mapping
- **Lombok** - Boilerplate reduction
- **SpringDoc** - OpenAPI 3 documentation