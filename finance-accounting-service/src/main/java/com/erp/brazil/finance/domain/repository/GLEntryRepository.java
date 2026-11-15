package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.ChartOfAccounts;
import com.erp.brazil.finance.domain.model.FiscalPeriod;
import com.erp.brazil.finance.domain.model.GLEntry;
import com.erp.brazil.finance.domain.model.JournalEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GLEntryRepository extends JpaRepository<GLEntry, UUID> {

    List<GLEntry> findByAccountOrderByEntryDateAsc(ChartOfAccounts account);

    List<GLEntry> findByAccountAndFiscalPeriodOrderByEntryDateAsc(ChartOfAccounts account, FiscalPeriod fiscalPeriod);

    List<GLEntry> findByFiscalPeriodOrderByEntryDateAsc(FiscalPeriod fiscalPeriod);

    List<GLEntry> findByJournalEntryOrderById(JournalEntry journalEntry);

    @Query("SELECT g FROM GLEntry g WHERE g.account = :account AND g.fiscalPeriod = :fiscalPeriod ORDER BY g.entryDate ASC, g.id ASC")
    List<GLEntry> findByAccountAndFiscalPeriodOrdered(@Param("account") ChartOfAccounts account, @Param("fiscalPeriod") FiscalPeriod fiscalPeriod);

    @Query("SELECT COALESCE(SUM(CASE WHEN g.entryType = 'DEBIT' THEN g.amount ELSE 0 END), 0) - COALESCE(SUM(CASE WHEN g.entryType = 'CREDIT' THEN g.amount ELSE 0 END), 0) FROM GLEntry g WHERE g.account = :account AND g.fiscalPeriod = :fiscalPeriod")
    BigDecimal getBalanceForAccountInPeriod(@Param("account") ChartOfAccounts account, @Param("fiscalPeriod") FiscalPeriod fiscalPeriod);

    @Query("SELECT COALESCE(SUM(CASE WHEN g.entryType = 'DEBIT' THEN g.amount ELSE 0 END), 0) - COALESCE(SUM(CASE WHEN g.entryType = 'CREDIT' THEN g.amount ELSE 0 END), 0) FROM GLEntry g WHERE g.account = :account AND g.entryDate <= :date")
    BigDecimal getRunningBalanceForAccount(@Param("account") ChartOfAccounts account, @Param("date") LocalDate date);

    @Query("SELECT g FROM GLEntry g WHERE g.account = :account AND g.entryDate BETWEEN :startDate AND :endDate ORDER BY g.entryDate ASC, g.id ASC")
    List<GLEntry> findByAccountAndDateRange(@Param("account") ChartOfAccounts account, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    Page<GLEntry> findByAccount(ChartOfAccounts account, Pageable pageable);

    Page<GLEntry> findByFiscalPeriod(FiscalPeriod fiscalPeriod, Pageable pageable);

    Optional<GLEntry> findTopByAccountOrderByEntryDateDescIdDesc(ChartOfAccounts account);

    List<GLEntry> findByCostCenterId(UUID costCenterId);
}