# Python Files Audit Report

**Date:** 2026-11-15  
**Repository:** erp-brazil monorepo  
**Auditor:** Automated cleanup subagent

---

## Summary

| Category | Count |
|----------|-------|
| Total .py files found | 74 |
| Project scripts (sales-crm-service) | 3 |
| Node.js dependencies (node_modules) | 71 |
| **Files kept/moved** | **1** |
| **Files deleted** | **2** |
| **Files ignored (deps)** | **71** |

---

## File Inventory & Decisions

### sales-crm-service/ (Project Scripts)

| File | Decision | Action Taken | Rationale |
|------|----------|--------------|-----------|
| `fix_pom.py` | **Delete** | Deleted | One-off script to fix XML encoding (`&` → `&`) in pom.xml. Served its purpose. Duplicate of fix_pom2.py. |
| `fix_pom2.py` | **Delete** | Deleted | Second iteration of the same fix. Redundant. |
| `remove_lombok.py` | **Keep & Move** | Moved to `scripts/remove_lombok.py` | Legitimate migration script to remove Lombok annotations from JPA entities and add explicit getters/setters. Has proper shebang, documentation, and reusable logic. |

### erp-frontend-web/node_modules/ (Dependencies)

All 71 Python files under `node_modules/` are **dependencies** from:
- `node-gyp/gyp/pylib/` (GYP build system Python library)
- `flatted/python/` (Flatted serialization library)

| Pattern | Decision | Action Taken | Rationale |
|---------|----------|--------------|-----------|
| `node_modules/**/*.py` | **Ignore** | No action needed (already in `.gitignore`) | These are transitive dependencies of Node.js tooling. `node_modules/` is already gitignored at root level. |

---

## Actions Performed

1. **Created** `sales-crm-service/scripts/` directory
2. **Moved** `remove_lombok.py` → `sales-crm-service/scripts/remove_lombok.py`
3. **Deleted** `fix_pom.py` and `fix_pom2.py`
4. **Updated** root `.gitignore` with Python artifact patterns:
   - `__pycache__/`
   - `*.py[cod]`, `*$py.class`, `*.pyd`
   - `.pytest_cache/`, `.coverage`, `htmlcov/`
   - `venv/`, `env/`, `.venv/`, `.env/`
   - `pip-wheel-metadata/`, `*.egg-info/`

---

## Verification

```bash
# Verify remaining .py files in project (excluding node_modules)
find /var/home/anderson/Projetos/ACDX_ERP/erp-brazil -name "*.py" -type f ! -path "*/node_modules/*"
```

Expected output:
```
/var/home/anderson/Projetos/ACDX_ERP/erp-brazil/sales-crm-service/scripts/remove_lombok.py
```

---

## Recommendations

1. **Future Python scripts** should be placed in `scripts/` or `tools/` directory within each service
2. **Add shebang** (`#!/usr/bin/env python3`) and **docstrings** to all project Python scripts
3. **Consider using Maven/Gradle plugins** instead of ad-hoc Python scripts for build-time tasks
4. **Run this audit periodically** to catch new temporary Python files