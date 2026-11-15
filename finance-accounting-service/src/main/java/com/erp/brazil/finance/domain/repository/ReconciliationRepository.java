package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.BankAccount;
import com.erp.brazil.finance.domain.model.Reconciliation;
import com.erp.brazil.finance.domain.model.ReconciliationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReconciliationRepository extends JpaRepository<Reconciliation, UUID> {

    List<Reconciliation> findByBankAccountOrderByReconciliationDateDesc(BankAccount bankAccount);

    List<Reconciliation> findByBankAccountAndStatus(BankAccount bankAccount, ReconciliationStatus status);

    List<Reconciliation> findByBankAccountAndReconciliationDateBetween(BankAccount bankAccount, LocalDate startDate, LocalDate endDate);

    Optional<Reconciliation> findByBankAccountAndReconciliationDate(BankAccount bankAccount, LocalDate reconciliationDate);

    Page<Reconciliation> findByBankAccount(BankAccount bankAccount, Pageable pageable);

    Page<Reconciliation> findByStatus(ReconciliationStatus status, Pageable pageable);
}