package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.Holiday;
import com.erp.brazil.hr.domain.model.HolidayType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    List<Holiday> findByDate(LocalDate date);

    List<Holiday> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<Holiday> findByCompanyId(Long companyId);

    List<Holiday> findByCompanyIdAndDateBetween(Long companyId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT h FROM Holiday h WHERE h.date = :date AND (h.companyId IS NULL OR h.companyId = :companyId) AND h.type IN :types")
    List<Holiday> findApplicableHolidays(@Param("date") LocalDate date,
                                          @Param("companyId") Long companyId,
                                          @Param("types") List<HolidayType> types);

    @Query("SELECT h FROM Holiday h WHERE h.date BETWEEN :startDate AND :endDate AND (h.companyId IS NULL OR h.companyId = :companyId)")
    List<Holiday> findByPeriodAndCompany(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          @Param("companyId") Long companyId);

    @Query("SELECT h FROM Holiday h WHERE h.isRecurring = true AND h.monthDay = :monthDay AND (h.companyId IS NULL OR h.companyId = :companyId)")
    List<Holiday> findRecurringByMonthDay(@Param("monthDay") String monthDay,
                                           @Param("companyId") Long companyId);

    boolean existsByDateAndCompanyId(LocalDate date, Long companyId);
}