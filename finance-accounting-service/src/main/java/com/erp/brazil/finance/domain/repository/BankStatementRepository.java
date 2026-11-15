package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.BankAccount;
import com.erp.brazil.finance.domain.model.BankStatement;
import com.erp.brazil.finance.domain.model.BankStatementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankStatementRepository extends JpaRepository<BankStatement, UUID> {

    Optional<BankStatement> findByBankAccountAndStatementDate(BankAccount bankAccount, LocalDate statementDate);

    List<BankStatement> findByBankAccountOrderByStatementDateDesc(BankAccount bankAccount);

    List<BankStatement> findByBankAccountAndStatus(BankAccount bankAccount, BankStatementStatus status);

    List<BankStatement> findByBankAccountAndStatementDateBetween(BankAccount bankAccount, LocalDate startDate, LocalDate endDate);

    @Query("SELECT b FROM BankStatement b WHERE b.bankAccount = :bankAccount AND b.statementDate BETWEEN :startDate AND :endDate ORDER BY b.statementDate DESC")
    Page<BankStatement> findByBankAccountAndDateRange(@Param("bankAccount") BankAccount bankAccount, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    Page<BankStatement> findByBankAccount(BankAccount bankAccount, Pageable pageable);

    Page<BankStatement> findByStatus(BankStatementStatus status, Pageable pageable);
}