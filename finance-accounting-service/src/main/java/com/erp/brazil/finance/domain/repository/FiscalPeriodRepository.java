package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.FiscalPeriod;
import com.erp.brazil.finance.domain.model.FiscalYear;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FiscalPeriodRepository extends JpaRepository<FiscalPeriod, UUID> {

    Optional<FiscalPeriod> findByFiscalYearAndPeriodNumber(FiscalYear fiscalYear, Integer periodNumber);

    List<FiscalPeriod> findByFiscalYearOrderByPeriodNumber(FiscalYear fiscalYear);

    List<FiscalPeriod> findByFiscalYearAndLockedFalseOrderByPeriodNumber(FiscalYear fiscalYear);

    List<FiscalPeriod> findByFiscalYearAndClosedFalseOrderByPeriodNumber(FiscalYear fiscalYear);

    Optional<FiscalPeriod> findByFiscalYearAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            FiscalYear fiscalYear, LocalDate date, LocalDate date2);

    @Query("SELECT p FROM FiscalPeriod p WHERE p.fiscalYear = :fiscalYear AND p.startDate <= :date AND p.endDate >= :date")
    Optional<FiscalPeriod> findByFiscalYearAndDate(@Param("fiscalYear") FiscalYear fiscalYear, @Param("date") LocalDate date);

    Page<FiscalPeriod> findByFiscalYear(FiscalYear fiscalYear, Pageable pageable);

    Page<FiscalPeriod> findByLockedFalse(Pageable pageable);
}