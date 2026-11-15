# Security Audit Report - ERP Brazil Monorepo

**Audit Date:** November 15, 2026
**Repository:** `/var/home/anderson/Projetos/ACDX_ERP/erp-brazil`
**Auditor:** Security Audit Subagent

---

## Executive Summary

| Check | Status |
|-------|--------|
| **Overall Security Posture** | ⚠️ **CONDITIONAL PASS** |
| Sensitive Secrets in Code | ❌ **VIOLATIONS FOUND** (2 critical, 1 medium) |
| .gitignore Coverage | ✅ **ADEQUATE** (with minor gaps) |
| Config Externalization | ✅ **MOSTLY COMPLIANT** (95%+) |
| Test Data Handling | ⚠️ **NEEDS REVIEW** |

**Risk Level:** MEDIUM - Two critical hardcoded secrets found in production code paths that must be remediated before production deployment.

---

## 1. Violations Found

### 🔴 CRITICAL: Hardcoded JWT Secret Fallback in Production Code Path

**File:** `erp-common-core/src/main/java/com/erp/brazil/common/infrastructure/security/JwtTokenProvider.java`
**Lines:** 37, 53-56

```java
@Value("${app.jwt.secret:change-this-secret-in-production-min-256-bits}")
private String secret;

@PostConstruct
public void init() {
    if (secret == null || secret.length() < 32) {
        log.warn("JWT secret is too short or not configured. Using default for development only!");
        this.secret = "change-this-secret-in-production-min-256-bits-change-this-secret-in-production";  // HARDCODED
    }
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
}
```

**Issue:** The `@PostConstruct` method **overwrites** the injected value with a hardcoded secret if the injected value is null or too short. This means:
- If `JWT_SECRET` env var is not set, the hardcoded secret is used
- If `JWT_SECRET` is set but < 32 chars, the hardcoded secret is used
- The warning log is insufficient protection

**Impact:** All JWT tokens signed with this known secret can be forged by attackers, compromising authentication across all services.

**Remediation:**
```java
@PostConstruct
public void init() {
    if (secret == null || secret.length() < 32) {
        if (isProductionProfile()) {
            throw new IllegalStateException("JWT_SECRET must be set and >= 32 characters in production");
        }
        log.warn("JWT secret not configured or too short. Using insecure default for DEVELOPMENT ONLY!");
        this.secret = "dev-only-insecure-secret-do-not-use-in-production-min-256-bits";
    }
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
}

private boolean isProductionProfile() {
    return Arrays.asList(environment.getActiveProfiles()).contains("prod");
}
```

---

### 🔴 CRITICAL: Hardcoded Master Encryption Key for Certificado Digital A1

**File:** `fiscal-brazil-service/src/main/java/com/erp/brazil/fiscal/infrastructure/certificado/CertificadoDigitalService.java`
**Lines:** 52-55

```java
private SecretKey generateOrLoadEncryptionKey() {
    try {
        String masterPassword = System.getenv("MASTER_ENCRYPTION_KEY");
        if (masterPassword == null || masterPassword.isBlank()) {
            masterPassword = "erp-brazil-fiscal-master-key-2024-change-in-production";  // HARDCODED
        }
        // ... derives AES-256 key from this password
    }
}
```

**Issue:** The master encryption key for encrypting **Certificado Digital A1 PFX files and passwords** falls back to a hardcoded value if `MASTER_ENCRYPTION_KEY` environment variable is not set.

**Impact:**
- All stored PFX certificates and their passwords are encrypted with a known key
- Attacker with database access can decrypt all certificates
- Certificado Digital A1 private keys compromised → fraudulent NF-e emission
- Legal liability for certificate misuse (ICP-Brasil regulations)

**Remediation:**
```java
private SecretKey generateOrLoadEncryptionKey() {
    String masterPassword = System.getenv("MASTER_ENCRYPTION_KEY");
    if (masterPassword == null || masterPassword.isBlank()) {
        if (isProductionProfile()) {
            throw new IllegalStateException(
                "MASTER_ENCRYPTION_KEY environment variable is REQUIRED in production. " +
                "Generate with: openssl rand -base64 32"
            );
        }
        log.warn("MASTER_ENCRYPTION_KEY not set. Using insecure default for DEVELOPMENT ONLY!");
        masterPassword = "dev-only-insecure-master-key-change-in-production-32chars";
    }
    // ... rest unchanged
}
```

---

### 🟡 MEDIUM: Test Files Contain Hardcoded Test Passwords/PFX Content

**File:** `fiscal-brazil-service/src/test/java/com/erp/brazil/fiscal/infrastructure/certificado/CertificadoDigitalServiceTest.java`
**Lines:** 41, 155, 186, 199

```java
String password = "senha123";           // Line 41
String newPassword = "newsenha123";     // Line 155
String originalPfx = "original-pfx-content";  // Line 186
String originalPassword = "senha123";   // Line 199
```

**Issue:** Test files contain hardcoded passwords and PFX content. While these are test values and not production secrets, they:
- Normalize poor security practices
- Could be accidentally copied to production code
- Appear in git history and CI logs

**Remediation:** Use test constants with clear naming:
```java
private static final String TEST_PASSWORD = "test-password-do-not-use-in-prod";
private static final String TEST_PFX_CONTENT = "test-pfx-content-do-not-use-in-prod";
```

---

## 2. .gitignore Coverage Assessment

### Root `.gitignore` ✅ COMPREHENSIVE

| Category | Coverage | Notes |
|----------|----------|-------|
| Maven/Gradle | ✅ | `target/`, `build/`, `.gradle/`, `*.jar/war/ear` |
| IDE | ✅ | `.idea/`, `*.iml`, `.vscode/`, `*.swp/swo` |
| OS | ✅ | `.DS_Store`, `Thumbs.db` |
| Environment | ✅ | `.env`, `.env.*`, `*.env`, `.env.local`, `.env.development`, `.env.production` |
| **Exception** | ✅ | `!env.deploy.template` correctly allows template |
| Logs | ✅ | `*.log`, `logs/` |
| Node | ✅ | `node_modules/`, `npm-debug.log*`, `dist/` |
| **Keys/Certs** | ✅ | `*.pem`, `*.key`, `*.crt`, `*.pfx`, `*.p12` |
| Database | ✅ | `*.sqlite`, `*.db` |
| Terraform | ✅ | `*.tfstate*`, `.terraform/` |
| Test/Coverage | ✅ | `coverage/`, `*.lcov` |

### Service-Level `.gitignore` Files ⚠️ INCONSISTENT

| Service | Coverage | Gaps |
|---------|----------|------|
| `erp-common-core` | Uses root | - |
| `api-gateway` | Uses root | - |
| `identity-tenant-service` | Uses root | - |
| `fiscal-brazil-service` | ⚠️ Partial | Missing: `.env*`, `*.pem/key/crt/pfx`, `node_modules`, `logs/` |
| `inventory-supply-service` | ⚠️ Partial | Missing: `.env*`, `*.pem/key/crt/pfx`, `node_modules`, `logs/` |
| `sales-crm-service` | ⚠️ Partial | Missing: `.env*`, `*.pem/key/crt/pfx`, `node_modules`, `logs/` |
| `manufacturing-projects-service` | ⚠️ Partial | Missing: `.env*`, `*.pem/key/crt/pfx`, `node_modules`, `logs/` |
| `finance-accounting-service` | ⚠️ Partial | Missing: `.env*`, `*.pem/key/crt/pfx`, `node_modules`, `logs/` |
| `hr-audit-service` | ✅ Good | Covers env, SSL/keys, DB, logs, IDE |
| `erp-frontend-web` | ✅ Good | Angular-specific, covers dist, node_modules |

**Recommendation:** Standardize all service `.gitignore` files to include at minimum:
```gitignore
# Environment
.env
.env.*
*.env
.env.local
.env.development
.env.production
!env.deploy.template

# Keys/Certs
*.pem
*.key
*.crt
*.pfx
*.p12
*.jks
*.keystore

# Logs
*.log
logs/

# Node (if applicable)
node_modules/
npm-debug.log*
dist/
```

---

## 3. Config Externalization Verification

### ✅ PASS: Application Configuration Files Use Placeholders

All `application.yml`, `application-dev.yml`, `application-prod.yml` files properly externalize secrets:

| Config File | Database Credentials | Redis | RabbitMQ | JWT | SMTP | WhatsApp | SSL/Keystore |
|-------------|---------------------|-------|----------|-----|------|----------|--------------|
| erp-common-core/* | `${MYSQL_*}` | `${REDIS_*}` | `${RABBITMQ_*}` | `${JWT_SECRET}` | - | - | - |
| api-gateway/* | - | `${REDIS_*}` | - | `${JWT_SECRET}` | - | - | - |
| identity-tenant-service/* | `${MYSQL_*}` | `${REDIS_*}` | `${RABBITMQ_*}` | `${JWT_SECRET}` | - | - | - |
| fiscal-brazil-service/* | `${MYSQL_*}` | `${REDIS_*}` | `${RABBITMQ_*}` | - | - | - | `${MASTER_ENCRYPTION_KEY}` |
| finance-accounting-service/* | `${DATABASE_*}` | - | `${RABBITMQ_*}` | `${OAUTH2_*}` | - | - | - |
| inventory-supply-service/* | `${SPRING_DATASOURCE_*}` | `${SPRING_REDIS_*}` | `${SPRING_RABBITMQ_*}` | `${SPRING_SECURITY_*}` | - | - | - |
| sales-crm-service/* | - | `${REDIS_*}` | `${RABBITMQ_*}` | - | - | - | - |
| manufacturing-projects-service/* | `${DB_*}` | - | `${RABBITMQ_*}` | `${KEYCLOAK_*}` | - | - | - |
| hr-audit-service/* | `${DB_*}` | `${REDIS_*}` | `${RABBITMQ_*}` | `${OAUTH2_*}` | `${MAIL_*}` | `${WHATSAPP_*}` | `${SSL_*}` |

### ✅ PASS: Docker Compose Files Use Environment Variables

| File | Status |
|------|--------|
| `deploy/docker-compose.yml` | ✅ All secrets via `${VAR:-default}` (dev defaults acceptable) |
| `deploy/docker-compose.prod.yml` | ✅ All secrets via `${VAR}` (no defaults - **required**) |

### ✅ PASS: Environment Template File

`deploy/env.deploy.template` - Comprehensive template with:
- All required variables documented
- Clear comments for production overrides
- JWT secret generation instruction: `openssl rand -base64 64`
- No actual secrets committed

---

## 4. Additional Findings

### 📋 INFO: Hardcoded URLs in Base Config (Non-Secrets)

**File:** `hr-audit-service/hr-audit-service/src/main/resources/application.yml:182`
```yaml
url: ${WHATSAPP_API_URL:https://api.zenvia.com/v2}
```
**Assessment:** Acceptable - this is a public API endpoint URL, not a secret. Token is properly externalized via `${WHATSAPP_API_TOKEN:}`.

### 📋 INFO: README Example Values (Non-Secrets)

Example values in README files are clearly placeholders:
- `MYSQL_ROOT_PASSWORD=root`
- `SPRING_DATASOURCE_PASSWORD=secret`
- `MAIL_PASSWORD=app-password`
- `PAYSLIP_KEYSTORE_PASSWORD=password`

**Assessment:** Acceptable documentation practice.

### 📋 INFO: No Private Keys/Certificates in Repository

Confirmed: No `BEGIN PRIVATE KEY`, `BEGIN CERTIFICATE`, or actual PFX content found in tracked files. The `.gitignore` properly excludes `*.pfx`, `*.pem`, `*.key`, `*.crt`, `*.p12`, `*.jks`.

---

## 5. Remediation Checklist

### Immediate (Before Any Production Deploy)

- [ ] **CRITICAL** Fix `JwtTokenProvider.java` - throw exception in production if JWT_SECRET not set or < 32 chars
- [ ] **CRITICAL** Fix `CertificadoDigitalService.java` - throw exception in production if MASTER_ENCRYPTION_KEY not set
- [ ] Generate production secrets:
  ```bash
  # JWT Secret (256+ bits / 32+ chars)
  openssl rand -base64 64
  
  # Master Encryption Key (32+ chars)
  openssl rand -base64 32
  
  # Database passwords
  openssl rand -base64 24
  ```
- [ ] Add `SPRING_PROFILES_ACTIVE=prod` to production environment

### Short-term

- [ ] Standardize all service `.gitignore` files to match root coverage
- [ ] Replace test hardcoded values with clearly-named test constants
- [ ] Add CI check: `git-secrets` or `truffleHog` scan on PRs
- [ ] Document secret rotation procedure

### Ongoing

- [ ] Use HashiCorp Vault / AWS Secrets Manager / Azure Key Vault for production secrets
- [ ] Implement secret scanning in CI/CD pipeline
- [ ] Regular secret rotation (90 days for JWT, 180 days for DB passwords)

---

## 6. Compliance Notes

| Standard | Status | Notes |
|----------|--------|-------|
| **LGPD (Brazil)** | ⚠️ Conditional | Certificado Digital encryption key must be externalized |
| **PCI-DSS** | ⚠️ Conditional | No card data in this repo, but JWT secret affects auth |
| **ICP-Brasil (Certificado Digital)** | ❌ Non-compliant | Hardcoded master key violates key management requirements |
| **OWASP ASVS 4.0** | ⚠️ Level 1 gaps | V7: Cryptography (hardcoded keys), V4: Access Control (JWT forging) |

---

## 7. Files Scanned Summary

| Category | Files Checked |
|----------|---------------|
| Root config | `.gitignore`, `pom.xml`, `deploy/*.yml`, `deploy/env.deploy.template` |
| Service configs | 9 services × 3-4 profile files each = ~35 files |
| Java source | Security-related classes (JWT, Certificado, SSL, Config) |
| Test files | `CertificadoDigitalServiceTest.java` |
| Frontend | `erp-frontend-web/.gitignore`, `package-lock.json` (integrity hashes only) |
| Documentation | All `README.md`, `HELP.md`, `IMPLEMENTATION_SUMMARY.md` |

---

## 8. Conclusion

The ERP Brazil monorepo demonstrates **good security hygiene** in configuration management:
- ✅ Comprehensive `.gitignore` at root level
- ✅ Consistent use of `${ENV_VAR}` placeholders in all config files
- ✅ Production docker-compose requires all secrets via env vars (no defaults)
- ✅ No actual private keys, certificates, or PFX files committed
- ✅ Environment template provided for deployment

**However, two critical hardcoded fallbacks in production code paths violate the principle of "fail closed" and must be fixed immediately:**

1. **JWT Secret** - Enables token forgery across all services
2. **Certificado Digital Master Key** - Enables decryption of all A1 certificates (legal/regulatory risk)

**Once these two issues are remediated, the repository achieves a strong security posture suitable for production deployment.**

---

*Report generated by automated security audit. Manual review of remediation implementation recommended.*