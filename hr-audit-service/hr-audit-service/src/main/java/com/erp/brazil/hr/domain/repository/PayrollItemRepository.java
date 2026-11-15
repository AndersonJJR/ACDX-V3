package com.erp.brazil.hr.domain.repository;

import com.erp.brazil.hr.domain.model.PayrollItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollItemRepository extends JpaRepository<PayrollItem, Long> {
    Optional<PayrollItem> findByUuid(UUID uuid);
    List<PayrollItem> findByPayrollId(Long payrollId);
    List<PayrollItem> findByPayrollIdAndEmployeeId(Long payrollId, Long employeeId);
    List<PayrollItem> findByEmployeeId(Long employeeId);
    List<PayrollItem> findByPayrollIdAndItemType(Long payrollId, PayrollItem.ItemType itemType);
    List<PayrollItem> findByPayrollIdAndCategory(Long payrollId, PayrollItem.Category category);
    List<PayrollItem> findByPayrollIdAndTaxableInssTrue(Long payrollId);
    List<PayrollItem> findByPayrollIdAndTaxableIrrfTrue(Long payrollId);
    List<PayrollItem> findByPayrollIdAndTaxableFgtsTrue(Long payrollId);

    @Query("SELECT SUM(i.totalAmount) FROM PayrollItem i WHERE i.payrollId = :payrollId AND i.itemType = :itemType")
    java.math.BigDecimal sumByPayrollIdAndItemType(@Param("payrollId") Long payrollId, @Param("itemType") PayrollItem.ItemType itemType);

    @Query("SELECT SUM(i.totalAmount) FROM PayrollItem i WHERE i.payrollId = :payrollId AND i.category = :category")
    java.math.BigDecimal sumByPayrollIdAndCategory(@Param("payrollId") Long payrollId, @Param("category") PayrollItem.Category category);

    @Query("SELECT i FROM PayrollItem i WHERE i.payrollId = :payrollId ORDER BY i.displayOrder, i.category")
    List<PayrollItem> findByPayrollIdOrderByDisplayOrder(@Param("payrollId") Long payrollId);
}