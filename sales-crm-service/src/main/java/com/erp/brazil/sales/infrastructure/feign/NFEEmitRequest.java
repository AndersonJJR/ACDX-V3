package com.erp.brazil.sales.infrastructure.feign;

import java.util.List;

public record NFEEmitRequest(
        String companyId,
        String number,
        String series,
        String invoiceType,
        String natureOfOperation,
        String issueDate,
        String operationDate,
        String customerDocumentType,
        String customerDocumentNumber,
        String customerLegalName,
        String customerTradeName,
        String customerZipCode,
        String customerStreet,
        String customerNumber,
        String customerNeighborhood,
        String customerCity,
        String customerState,
        List<Item> items,
        java.math.BigDecimal totalAmount
) {
    public record Item(
            String productCode,
            String productName,
            String ncm,
            String cfop,
            String unitOfMeasure,
            java.math.BigDecimal quantity,
            java.math.BigDecimal unitPrice,
            java.math.BigDecimal discountAmount,
            java.math.BigDecimal taxIcms,
            java.math.BigDecimal taxIpi,
            java.math.BigDecimal taxPis,
            java.math.BigDecimal taxCofins,
            java.math.BigDecimal taxIss
    ) {}
}