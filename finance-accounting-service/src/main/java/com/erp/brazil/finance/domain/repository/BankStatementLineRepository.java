package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.BankStatement;
import com.erp.brazil.finance.domain.model.BankStatementLine;
import com.erp.brazil.finance.domain.model.BankStatementLineStatus;
import com.erp.brazil.finance.domain.model.GLEntry;
import com.erp.brazil.finance.domain.model.Reconciliation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankStatementLineRepository extends JpaRepository<BankStatementLine, UUID> {

    List<BankStatementLine> findByStatementOrderByLineNumber(BankStatement statement);

    List<BankStatementLine> findByStatementAndStatus(BankStatement statement, BankStatementLineStatus status);

    List<BankStatementLine> findByReconciliation(Reconciliation reconciliation);

    List<BankStatementLine> findByGLEntry(GLEntry glEntry);

    List<BankStatementLine> findByStatus(BankStatementLineStatus status);

    List<BankStatementLine> findByTransactionDateBetween(LocalDate startDate, LocalDate endDate);

    Optional<BankStatementLine> findByStatementAndLineNumber(BankStatement statement, Integer lineNumber);

    Page<BankStatementLine> findByStatement(BankStatement statement, Pageable pageable);

    Page<BankStatementLine> findByStatus(BankStatementLineStatus status, Pageable pageable);

    Page<BankStatementLine> findByReconciliationIsNullAndStatus(BankStatementLineStatus status, Pageable pageable);
}