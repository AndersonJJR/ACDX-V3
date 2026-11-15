package com.erp.brazil.inventory.presentation.rest.controller;

import com.erp.brazil.inventory.domain.model.StockLedgerEntry;
import com.erp.brazil.inventory.domain.repository.ItemRepository;
import com.erp.brazil.inventory.domain.repository.StockLedgerEntryRepository;
import com.erp.brazil.inventory.domain.repository.WarehouseRepository;
import com.erp.brazil.inventory.presentation.rest.dto.StockBalanceResponse;
import com.erp.brazil.inventory.presentation.rest.dto.StockLedgerEntryResponse;
import com.erp.brazil.inventory.presentation.rest.dto.ValuationReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
@Tag(name = "Stock", description = "Stock balance, ledger, and valuation endpoints")
public class StockController {

    private final StockLedgerEntryRepository stockLedgerEntryRepository;
    private final ItemRepository itemRepository;
    private final WarehouseRepository warehouseRepository;

    @GetMapping("/balance")
    @Operation(summary = "Get stock balance for an item in a warehouse")
    public ResponseEntity<StockBalanceResponse> getStockBalance(
            @Parameter(description = "Item ID") @RequestParam UUID itemId,
            @Parameter(description = "Warehouse ID") @RequestParam UUID warehouseId,
            @Parameter(description = "Item Variant ID (optional)") @RequestParam(required = false) UUID itemVariantId) {

        // Get current stock quantity
        BigDecimal stockQty = stockLedgerEntryRepository.getCurrentStockQty(
                getCompanyId(itemId), itemId, warehouseId);

        // Get reserved quantity
        BigDecimal reservedQty = stockLedgerEntryRepository.getCurrentStockQty(
                getCompanyId(itemId), itemId, warehouseId); // Would need StockReservationRepository

        // For now, return basic balance
        // In a full implementation, you'd inject StockReservationRepository
        ItemRepository itemRepo = itemRepository;
        var itemOpt = itemRepo.findById(itemId);
        var warehouseOpt = warehouseRepository.findById(warehouseId);

        if (itemOpt.isEmpty() || warehouseOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var item = itemOpt.get();
        var warehouse = warehouseOpt.get();

        BigDecimal availableQty = stockQty; // Would subtract reservedQty
        BigDecimal valuationRate = item.getStandardRate();
        BigDecimal stockValue = availableQty.multiply(valuationRate);

        StockBalanceResponse response = StockBalanceResponse.builder()
                .itemId(item.getId())
                .itemCode(item.getCode())
                .itemName(item.getName())
                .itemVariantId(itemVariantId)
                .warehouseId(warehouse.getId())
                .warehouseCode(warehouse.getCode())
                .warehouseName(warehouse.getName())
                .actualQty(stockQty)
                .reservedQty(BigDecimal.ZERO) // Placeholder
                .availableQty(availableQty)
                .orderedQty(BigDecimal.ZERO) // Placeholder
                .projectedQty(availableQty)
                .valuationRate(valuationRate)
                .stockValue(stockValue)
                .valuationMethod(item.getValuationMethod().name())
                .lastUpdated(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/ledger")
    @Operation(summary = "Get stock ledger entries with pagination and filters")
    public ResponseEntity<Page<StockLedgerEntryResponse>> getStockLedger(
            @Parameter(description = "Item ID") @RequestParam(required = false) UUID itemId,
            @Parameter(description = "Warehouse ID") @RequestParam(required = false) UUID warehouseId,
            @Parameter(description = "Voucher Type") @RequestParam(required = false) String voucherType,
            @Parameter(description = "Voucher No") @RequestParam(required = false) String voucherNo,
            @Parameter(description = "Start Date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End Date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {

        UUID companyId = getCompanyIdFromContext(); // Would come from security context

        Page<StockLedgerEntry> entries;

        if (itemId != null && warehouseId != null) {
            entries = stockLedgerEntryRepository.findByCompanyIdAndItemIdAndWarehouseId(
                    companyId, itemId, warehouseId, pageable);
        } else if (voucherType != null && voucherNo != null) {
            entries = stockLedgerEntryRepository.findByCompanyIdAndVoucher(companyId, voucherType, voucherNo, pageable);
        } else if (startDate != null && endDate != null) {
            // Would need a custom query for date range without item/warehouse
            entries = Page.empty(pageable);
        } else {
            return ResponseEntity.badRequest().build();
        }

        Page<StockLedgerEntryResponse> response = entries.map(this::mapToResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/valuation-report")
    @Operation(summary = "Get stock valuation report for a warehouse")
    public ResponseEntity<ValuationReportResponse> getValuationReport(
            @Parameter(description = "Warehouse ID") @RequestParam UUID warehouseId,
            @Parameter(description = "As of Date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {

        UUID companyId = getCompanyIdFromContext();
        var warehouseOpt = warehouseRepository.findById(warehouseId);

        if (warehouseOpt.isEmpty() || !warehouseOpt.get().getCompanyId().equals(companyId)) {
            return ResponseEntity.notFound().build();
        }

        // This would need a custom query to get all items with stock in the warehouse
        // For now, return a basic structure
        ValuationReportResponse response = ValuationReportResponse.builder()
                .companyId(companyId)
                .warehouseId(warehouseId)
                .warehouseName(warehouseOpt.get().getName())
                .asOfDate(asOfDate != null ? asOfDate.atStartOfDay() : LocalDateTime.now())
                .totalStockValue(BigDecimal.ZERO)
                .totalQty(BigDecimal.ZERO)
                .totalItems(0)
                .items(List.of())
                .build();

        return ResponseEntity.ok(response);
    }

    private UUID getCompanyId(UUID itemId) {
        return itemRepository.findById(itemId)
                .map(item -> item.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));
    }

    private UUID getCompanyIdFromContext() {
        // In a real implementation, this would come from the security context
        throw new UnsupportedOperationException("CompanyId resolution from security context not implemented");
    }

    private StockLedgerEntryResponse mapToResponse(StockLedgerEntry entry) {
        return StockLedgerEntryResponse.builder()
                .id(entry.getId())
                .companyId(entry.getCompanyId())
                .itemId(entry.getItemId())
                .itemVariantId(entry.getItemVariantId())
                .warehouseId(entry.getWarehouseId())
                .locationId(entry.getLocationId())
                .batchId(entry.getBatchId())
                .serialNumberId(entry.getSerialNumberId())
                .qtyChange(entry.getQtyChange())
                .valuationRate(entry.getValuationRate())
                .stockValue(entry.getStockValue())
                .stockQtyAfter(entry.getStockQtyAfter())
                .stockValueAfter(entry.getStockValueAfter())
                .voucherType(entry.getVoucherType())
                .voucherNo(entry.getVoucherNo())
                .voucherDetailNo(entry.getVoucherDetailNo())
                .postingDate(entry.getPostingDate())
                .postingTime(entry.getPostingTime())
                .actualQty(entry.getActualQty())
                .incomingRate(entry.getIncomingRate())
                .outgoingRate(entry.getOutgoingRate())
                .valuationMethod(entry.getValuationMethod() != null ? entry.getValuationMethod().name() : null)
                .isCancelled(entry.getIsCancelled())
                .isReposted(entry.getIsReposted())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}