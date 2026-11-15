package com.erp.brazil.finance.domain.repository;

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

public interface FiscalYearRepository extends JpaRepository<FiscalYear, UUID> {

    Optional<FiscalYear> findByYear(Integer year);

    List<FiscalYear> findByOrderByYearDesc();

    List<FiscalYear> findByClosedFalseOrderByYearDesc();

    Optional<FiscalYear> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate date, LocalDate date2);

    @Query("SELECT f FROM FiscalYear f WHERE f.startDate <= :date AND f.endDate >= :date")
    Optional<FiscalYear> findByDate(@Param("date") LocalDate date);

    Page<FiscalYear> findAll(Pageable pageable);
}