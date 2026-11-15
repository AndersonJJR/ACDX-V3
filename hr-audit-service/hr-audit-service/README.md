# HR Audit Service - ERP Brazil

A comprehensive HR, Audit & Notifications service for Brazilian ERP systems, implementing CLT (Consolidação das Leis do Trabalho) compliance, LGPD (Lei Geral de Proteção de Dados) requirements, and Brazilian payroll calculations.

## Features

### Core HR Modules
- **Employee Management**: Complete employee lifecycle (hire, transfer, termination)
- **Employment Contracts**: CLT indefinite, fixed-term, temporary, apprentice, internship, statutory, cooperative
- **Time Tracking (Ponto Eletrônico)**: Biometric, mobile app, web portal, GPS, face recognition
- **Time Sheets (Fechamento de Ponto)**: Overtime, night shift, absences, late arrivals
- **Leave Management**: Vacation, medical, maternity/paternity, marriage, bereavement, study, election, military, blood donation, unpaid, compensatory
- **Leave Balances**: Accrual, usage, carry-over, expiration tracking

### Payroll (Folha de Pagamento)
- **Monthly Payroll Processing**: Async via Quartz scheduler
- **INSS Calculation**: 2024 progressive brackets (7.5%/9%/12%/14% up to ceiling R$ 7.786,02)
- **IRRF Calculation**: 2024 progressive brackets with dependent deductions (R$ 189,59 per dependent)
- **FGTS Calculation**: 8% monthly deposit, 40% resignation fine
- **Additional Calculations**: Night shift (20%), hazardous (30%), unhealthy (40%), overtime (50%/75%/100%)
- **Benefits**: Transport voucher (6% employee), meal/food voucher, health/dental/life insurance, pension, gym, education, childcare, fuel, pharmacy, optical
- **Payslip Generation (Holerite)**: PDF with Brazilian layout standards, digital signature (A1 certificate)

### Audit & Compliance
- **Audit Log**: MongoDB capped collection (1GB, 10M docs), TTL 7 years (2555 days)
- **Automatic Masking**: CPF, email, phone, bank account, PIX key, address, mother/father names
- **LGPD Compliance**: DSAR export endpoint, Right to Erasure endpoint
- **AOP-based Auditing**: Intercepts all repository/service write operations

### Notifications
- **Multi-channel**: Email (SMTP), WhatsApp (Zenvia/Twilio/Meta), Push (FCM/OneSignal), SMS, In-App
- **Template Engine**: Variable substitution, HTML/TEXT/MARKDOWN support, multi-locale (pt_BR default)
- **Queue**: Redis-based with priority, scheduling, retry with exponential backoff, DLQ after max retries

### Webhooks
- **Outbox Pattern**: Reliable delivery with persistence
- **Retry Policy**: Exponential backoff (1s, 2s, 4s, 8s, 16s... max 5 min), max 5 attempts
- **Dead Letter Queue**: After 5 failures, subscription auto-disabled
- **HMAC Signature**: SHA256 verification

### Events
- **Consumed**: All domain events → AuditEventUseCase
- **Published**: EmployeeHiredEvent, EmployeeTerminatedEvent, PayrollProcessedEvent, LeaveApprovedEvent, PayrollPeriodClosedEvent

## Architecture

```
src/main/java/com/erp/brazil/hr/
├── domain/
│   ├── model/          # JPA Entities & MongoDB Documents
│   ├── repository/     # Spring Data JPA & MongoDB Repositories
│   └── event/          # Domain Events
├── application/
│   ├── usecase/        # Business Logic (Use Cases)
│   ├── dto/            # Data Transfer Objects
│   └── mapper/         # MapStruct Mappers
├── infrastructure/
│   ├── persistence/    # MongoDB, Redis, Quartz Config
│   ├── messaging/      # RabbitMQ, Webhook Delivery
│   ├── pdf/            # Payslip PDF Generator (iText/OpenPDF)
│   ├── notification/   # Email, WhatsApp, Push Providers
│   ├── audit/          # AOP Audit Aspect, Data Masking
│   ├── config/         # Async, Quartz, OpenAPI, Security
│   ├── exception/      # RFC 7807 ProblemDetail Handler
│   └── security/       # JWT, CORS, Method Security
└── presentation/
    └── rest/           # REST Controllers, DTOs
```

## Technology Stack

- **Java 21** (Virtual Threads ready)
- **Spring Boot 3.2+**
- **Spring Data JPA** (MySQL/PostgreSQL) + **Spring Data MongoDB** + **Spring Data Redis**
- **Spring AMQP** (RabbitMQ)
- **Spring Security** (JWT/OAuth2 Resource Server)
- **Spring Quartz** (Clustered Scheduler)
- **Flyway** (Database Migrations)
- **MapStruct** (DTO Mapping)
- **OpenPDF/iText** (PDF Generation)
- **Bouncy Castle** (Digital Signatures)
- **SpringDoc OpenAPI** (Swagger UI)
- **Lombok** (Boilerplate reduction)
- **Testcontainers** (Integration Testing)

## Configuration Profiles

- **dev**: SQLite, embedded MongoDB/Redis/RabbitMQ, Mailhog
- **prod**: MySQL, MongoDB, Redis, RabbitMQ, SSL/TLS, Prometheus metrics

## API Endpoints

### Employees
- `POST /api/v1/employees` - Create employee
- `GET /api/v1/employees/{id}` - Get employee
- `GET /api/v1/employees?companyId=1` - List employees (paginated)
- `GET /api/v1/employees/search?companyId=1&term=joao` - Search employees
- `GET /api/v1/employees/{id}/contracts` - Get employment contracts
- `PUT /api/v1/employees/{id}` - Update employee
- `DELETE /api/v1/employees/{id}?reason=...` - Terminate employee

### Time Records (Ponto Eletrônico)
- `POST /api/v1/time-records/record` - Record time entry
- `GET /api/v1/time-records/{employeeId}/timesheet?start=...&end=...` - Get time records
- `GET /api/v1/time-records/{employeeId}/timesheet/{year}/{month}` - Get calculated timesheet
- `PUT /api/v1/time-records/{id}/adjust` - Manual adjustment
- `PUT /api/v1/time-records/{id}/approve` - Approve record

### Leave Management
- `POST /api/v1/leave/requests` - Create leave request
- `GET /api/v1/leave/requests/{id}` - Get leave request
- `GET /api/v1/leave/requests` - List leave requests (paginated)
- `PUT /api/v1/leave/requests/{id}/approve` - Approve request
- `PUT /api/v1/leave/requests/{id}/reject` - Reject request
- `PUT /api/v1/leave/requests/{id}/cancel` - Cancel request
- `GET /api/v1/leave/balances/{employeeId}` - Get leave balances

### Payroll
- `POST /api/v1/payroll` - Create payroll period
- `GET /api/v1/payroll/{id}` - Get payroll
- `GET /api/v1/payroll?companyId=1` - List payrolls (paginated)
- `POST /api/v1/payroll/{id}/process` - Process payroll (async)
- `POST /api/v1/payroll/{id}/process-sync` - Process payroll (sync)
- `GET /api/v1/payroll/{id}/status` - Get processing status
- `GET /api/v1/payroll/{id}/items` - Get payroll items
- `PUT /api/v1/payroll/{id}/approve` - Approve payroll

### Payslips (Holerites)
- `GET /api/v1/payslips/{id}` - Get payslip
- `GET /api/v1/payslips/{id}/download` - Download PDF
- `GET /api/v1/payslips/{employeeId}/list` - List employee payslips
- `POST /api/v1/payslips/{id}/sign` - Digital sign
- `GET /api/v1/payslips/{id}/access/{token}` - Access via token

### Audit Logs
- `GET /api/v1/audit/search` - Search audit logs (paginated)
- `GET /api/v1/audit/entity/{entityType}/{entityId}` - Entity history
- `GET /api/v1/audit/export/{userId}` - LGPD DSAR export
- `POST /api/v1/audit/erase/{userId}` - LGPD Right to Erasure

### Webhooks
- `POST /api/v1/webhooks/subscriptions` - Create subscription
- `GET /api/v1/webhooks/subscriptions/{id}` - Get subscription
- `GET /api/v1/webhooks/subscriptions?companyId=1` - List subscriptions
- `PUT /api/v1/webhooks/subscriptions/{id}` - Update subscription
- `DELETE /api/v1/webhooks/subscriptions/{id}` - Delete subscription
- `POST /api/v1/webhooks/test/{id}` - Test webhook
- `GET /api/v1/webhooks/deliveries?subscriptionId=1` - List deliveries
- `POST /api/v1/webhooks/deliveries/{id}/retry` - Retry delivery

### Notifications
- `POST /api/v1/notifications/templates` - Create template
- `GET /api/v1/notifications/templates` - List templates
- `POST /api/v1/notifications` - Send notification (template)
- `POST /api/v1/notifications/send` - Send direct notification
- `GET /api/v1/notifications` - List notifications (paginated)
- `POST /api/v1/notifications/{id}/retry` - Retry failed
- `DELETE /api/v1/notifications/{id}` - Cancel queued

## Database Migrations

- `V1__init_hr.sql` - Complete HR schema (20+ tables)
- `V2__seed_benefits.sql` - Default benefits, leave types, templates, company, departments, unions, holidays

## Running the Service

### Development
```bash
# Using Maven
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Using Docker Compose (includes MySQL, MongoDB, Redis, RabbitMQ)
docker-compose up -d
```

### Production
```bash
# Build
./mvnw clean package -Pprod

# Run
java -jar target/hr-audit-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

### Environment Variables
```bash
# Database
DATABASE_URL=jdbc:mysql://host:3306/erp_hr_audit
DB_USERNAME=user
DB_PASSWORD=pass

# MongoDB
MONGODB_URI=mongodb://host:27017/erp_hr_audit

# Redis
REDIS_HOST=host
REDIS_PORT=6379
REDIS_PASSWORD=pass

# RabbitMQ
RABBITMQ_HOST=host
RABBITMQ_PORT=5672
RABBITMQ_USER=user
RABBITMQ_PASSWORD=pass

# Mail
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=user@gmail.com
MAIL_PASSWORD=app-password

# Security
JWT_SECRET=your-256-bit-secret
OAUTH2_ISSUER_URI=https://auth.empresa.com.br
OAUTH2_JWK_SET_URI=https://auth.empresa.com.br/.well-known/jwks.json

# WhatsApp
WHATSAPP_API_URL=https://graph.facebook.com/v18.0
WHATSAPP_API_TOKEN=token
WHATSAPP_PHONE_NUMBER_ID=id

# Payslip Digital Signature
PAYSLIP_DIGITAL_SIGNATURE_ENABLED=true
PAYSLIP_KEYSTORE_PATH=/path/to/keystore.p12
PAYSLIP_KEYSTORE_PASSWORD=password
PAYSLIP_KEY_ALIAS=alias
PAYSLIP_KEY_PASSWORD=password
```

## Testing

```bash
# Unit tests
./mvnw test

# Integration tests with Testcontainers
./mvnw verify -Pintegration-tests
```

## Swagger UI

Available at: `http://localhost:8080/api/swagger-ui.html`

## Actuator Endpoints

- Health: `/actuator/health`
- Metrics: `/actuator/metrics`
- Prometheus: `/actuator/prometheus`
- Flyway: `/actuator/flyway`
- Quartz: `/actuator/quartz`

## LGPD Compliance

The service implements:
- **Data Minimization**: Only necessary data collected
- **Purpose Limitation**: Data used only for HR/payroll purposes
- **Storage Limitation**: Audit logs auto-expire after 7 years
- **Integrity & Confidentiality**: Automatic masking of sensitive fields
- **Data Subject Rights**: 
  - Right to Access (DSAR) - `/api/v1/audit/export/{userId}`
  - Right to Erasure - `/api/v1/audit/erase/{userId}`
  - Right to Portability - JSON export

## License

Proprietary - ACDX ERP