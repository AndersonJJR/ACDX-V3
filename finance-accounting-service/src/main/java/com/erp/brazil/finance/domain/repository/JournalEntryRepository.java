package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.FiscalPeriod;
import com.erp.brazil.finance.domain.model.JournalEntry;
import com.erp.brazil.finance.domain.model.JournalEntryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {

    Optional<JournalEntry> findByReferenceAndFiscalPeriod(String reference, FiscalPeriod fiscalPeriod);

    List<JournalEntry> findByFiscalPeriodOrderByEntryDateDescReferenceDesc(FiscalPeriod fiscalPeriod);

    List<JournalEntry> findByFiscalPeriodAndStatusOrderByEntryDateDesc(FiscalPeriod fiscalPeriod, JournalEntryStatus status);

    List<JournalEntry> findByFiscalPeriodAndEntryDateBetweenOrderByEntryDateDesc(
            FiscalPeriod fiscalPeriod, LocalDate startDate, LocalDate endDate);

    List<JournalEntry> findByFiscalPeriodAndStatusInOrderByEntryDateDesc(
            FiscalPeriod fiscalPeriod, List<JournalEntryStatus> statuses);

    List<JournalEntry> findByReversedFalseAndFiscalPeriodOrderByEntryDateDesc(FiscalPeriod fiscalPeriod);

    @Query("SELECT j FROM JournalEntry j WHERE j.fiscalPeriod = :fiscalPeriod AND j.status = :status ORDER BY j.entryDate DESC, j.reference DESC")
    Page<JournalEntry> findByFiscalPeriodAndStatus(FiscalPeriod fiscalPeriod, JournalEntryStatus status, Pageable pageable);

    @Query("SELECT j FROM JournalEntry j WHERE j.fiscalPeriod = :fiscalPeriod AND j.entryDate BETWEEN :startDate AND :endDate ORDER BY j.entryDate DESC, j.reference DESC")
    Page<JournalEntry> findByFiscalPeriodAndEntryDateBetween(FiscalPeriod fiscalPeriod, LocalDate startDate, LocalDate endDate, Pageable pageable);

    @Query("SELECT j FROM JournalEntry j WHERE j.fiscalPeriod.fiscalYear = :fiscalYear AND j.status = :status ORDER BY j.entryDate DESC, j.reference DESC")
    Page<JournalEntry> findByFiscalYearAndStatus(com.erp.brazil.finance.domain.model.FiscalYear fiscalYear, JournalEntryStatus status, Pageable pageable);

    boolean existsByReferenceAndFiscalPeriodAndIdNot(String reference, FiscalPeriod fiscalPeriod, UUID id);

    long countByFiscalPeriodAndStatus(FiscalPeriod fiscalPeriod, JournalEntryStatus status);
}