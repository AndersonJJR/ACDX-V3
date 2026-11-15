package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.PayrollItem;
import com.erp.brazil.hr.domain.model.PayrollItemCategory;
import com.erp.brazil.hr.domain.model.PayrollItemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollItemRepository extends JpaRepository<PayrollItem, Long> {

    List<PayrollItem> findByPayrollId(Long payrollId);

    Page<PayrollItem> findByPayrollId(Long payrollId, Pageable pageable);

    List<PayrollItem> findByPayrollIdAndEmployeeId(Long payrollId, Long employeeId);

    List<PayrollItem> findByPayrollIdAndItemType(Long payrollId, PayrollItemType itemType);

    List<PayrollItem> findByPayrollIdAndCategory(Long payrollId, PayrollItemCategory category);

    List<PayrollItem> findByEmployeeIdAndPayrollIdIn(Long employeeId, List<Long> payrollIds);

    @Query("SELECT SUM(pi.totalAmount) FROM PayrollItem pi WHERE pi.payrollId = :payrollId AND pi.itemType = :itemType")
    BigDecimal sumByPayrollIdAndItemType(@Param("payrollId") Long payrollId,
                                          @Param("itemType") PayrollItemType itemType);

    @Query("SELECT SUM(pi.totalAmount) FROM PayrollItem pi WHERE pi.payrollId = :payrollId AND pi.category = :category")
    BigDecimal sumByPayrollIdAndCategory(@Param("payrollId") Long payrollId,
                                          @Param("category") PayrollItemCategory category);

    @Query("SELECT SUM(pi.totalAmount) FROM PayrollItem pi WHERE pi.payrollId = :payrollId AND pi.taxableInss = true")
    BigDecimal sumTaxableInssByPayrollId(@Param("payrollId") Long payrollId);

    @Query("SELECT SUM(pi.totalAmount) FROM PayrollItem pi WHERE pi.payrollId = :payrollId AND pi.taxableIrrf = true")
    BigDecimal sumTaxableIrrfByPayrollId(@Param("payrollId") Long payrollId);

    @Query("SELECT SUM(pi.totalAmount) FROM PayrollItem pi WHERE pi.payrollId = :payrollId AND pi.taxableFgts = true")
    BigDecimal sumTaxableFgtsByPayrollId(@Param("payrollId") Long payrollId);

    @Query("SELECT pi FROM PayrollItem pi WHERE pi.payrollId = :payrollId AND pi.employeeId = :employeeId ORDER BY pi.displayOrder")
    List<PayrollItem> findByPayrollIdAndEmployeeIdOrdered(@Param("payrollId") Long payrollId,
                                                           @Param("employeeId") Long employeeId);

    void deleteByPayrollId(Long payrollId);
}