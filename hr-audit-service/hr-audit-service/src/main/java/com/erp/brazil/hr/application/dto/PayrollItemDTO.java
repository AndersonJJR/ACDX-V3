package com.erp.brazil.hr.application.dto;

import com.erp.brazil.hr.domain.model.PayrollItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

public record PayrollItemDTO(
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) UUID uuid,
    Long payrollId,
    Long employeeId,
    PayrollItem.ItemType itemType,
    PayrollItem.Category category,
    String code,
    String description,
    BigDecimal referenceValue,
    BigDecimal quantity,
    BigDecimal unitPrice,
    BigDecimal totalAmount,
    boolean taxableInss,
    boolean taxableIrrf,
    boolean taxableFgts,
    boolean isFixed,
    String calculationBasis,
    int displayOrder
) {
    public static PayrollItemDTO from(PayrollItem item) {
        return new PayrollItemDTO(
            item.uuid(), item.payrollId(), item.employeeId(), item.itemType(), item.category(),
            item.code(), item.description(), item.referenceValue(), item.quantity(), item.unitPrice(),
            item.totalAmount(), item.taxableInss(), item.taxableIrrf(), item.taxableFgts(),
            item.isFixed(), item.calculationBasis(), item.displayOrder()
        );
    }
}