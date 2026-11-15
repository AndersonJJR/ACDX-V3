package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.BankStatementLine;
import com.erp.brazil.finance.domain.model.GLEntry;
import com.erp.brazil.finance.domain.model.Reconciliation;
import com.erp.brazil.finance.domain.model.ReconciliationItem;
import com.erp.brazil.finance.domain.model.ReconciliationItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReconciliationItemRepository extends JpaRepository<ReconciliationItem, UUID> {

    List<ReconciliationItem> findByReconciliation(Reconciliation reconciliation);

    List<ReconciliationItem> findByReconciliationAndStatus(Reconciliation reconciliation, ReconciliationItemStatus status);

    Optional<ReconciliationItem> findByBankStatementLine(BankStatementLine bankStatementLine);

    Optional<ReconciliationItem> findByGLEntry(GLEntry glEntry);

    List<ReconciliationItem> findByStatus(ReconciliationItemStatus status);

    Page<ReconciliationItem> findByReconciliation(Reconciliation reconciliation, Pageable pageable);
}