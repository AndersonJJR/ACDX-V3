package com.erp.brazil.inventory.presentation.rest.controller;

import com.erp.brazil.inventory.domain.model.Item;
import com.erp.brazil.inventory.domain.model.ItemBatch;
import com.erp.brazil.inventory.domain.model.ItemSerialNumber;
import com.erp.brazil.inventory.domain.model.ItemVariant;
import com.erp.brazil.inventory.domain.repository.ItemBatchRepository;
import com.erp.brazil.inventory.domain.repository.ItemRepository;
import com.erp.brazil.inventory.domain.repository.ItemSerialNumberRepository;
import com.erp.brazil.inventory.domain.repository.ItemVariantRepository;
import com.erp.brazil.inventory.presentation.rest.dto.ItemResponse;
import com.erp.brazil.inventory.presentation.rest.dto.ItemBatchResponse;
import com.erp.brazil.inventory.presentation.rest.dto.ItemSerialNumberResponse;
import com.erp.brazil.inventory.presentation.rest.dto.ItemVariantResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
@Tag(name = "Items", description = "Item master data management")
public class ItemController {

    private final ItemRepository itemRepository;
    private final ItemVariantRepository itemVariantRepository;
    private final ItemBatchRepository itemBatchRepository;
    private final ItemSerialNumberRepository itemSerialNumberRepository;

    @GetMapping
    @Operation(summary = "List items with pagination")
    public ResponseEntity<Page<ItemResponse>> listItems(
            @Parameter(description = "Company ID") @RequestParam UUID companyId,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {

        Page<Item> items = itemRepository.findByCompanyIdAndIsActiveTrue(companyId, pageable);
        return ResponseEntity.ok(items.map(this::mapToResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get item by ID")
    public ResponseEntity<ItemResponse> getItem(@Parameter(description = "Item ID") @PathVariable UUID id) {
        return itemRepository.findById(id)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new item")
    public ResponseEntity<ItemResponse> createItem(@RequestBody ItemRequest request) {
        Item item = Item.builder()
                .companyId(request.companyId())
                .code(request.code())
                .name(request.name())
                .description(request.description())
                .itemType(Item.ItemType.valueOf(request.itemType()))
                .itemGroup(request.itemGroup())
                .brand(request.brand())
                .unitOfMeasure(request.unitOfMeasure())
                .valuationMethod(Item.ValuationMethod.valueOf(request.valuationMethod()))
                .standardRate(request.standardRate())
                .isActive(true)
                .isStockItem(request.isStockItem())
                .isPurchasable(request.isPurchasable())
                .isSellable(request.isSellable())
                .isManufactured(request.isManufactured())
                .hasBatch(request.hasBatch())
                .hasSerialNo(request.hasSerialNo())
                .batchNumberSeries(request.batchNumberSeries())
                .serialNumberSeries(request.serialNumberSeries())
                .defaultWarehouseId(request.defaultWarehouseId())
                .minOrderQty(request.minOrderQty())
                .maxOrderQty(request.maxOrderQty())
                .reorderLevel(request.reorderLevel())
                .reorderQty(request.reorderQty())
                .leadTimeDays(request.leadTimeDays())
                .shelfLifeDays(request.shelfLifeDays())
                .hsnCode(request.hsnCode())
                .ncmCode(request.ncmCode())
                .cestCode(request.cestCode())
                .origemMercadoria(request.origemMercadoria())
                .build();

        Item saved = itemRepository.save(item);
        return ResponseEntity.ok(mapToResponse(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update item")
    public ResponseEntity<ItemResponse> updateItem(
            @Parameter(description = "Item ID") @PathVariable UUID id,
            @RequestBody ItemRequest request) {

        return itemRepository.findById(id)
                .map(item -> {
                    item.setCode(request.code());
                    item.setName(request.name());
                    item.setDescription(request.description());
                    item.setItemType(Item.ItemType.valueOf(request.itemType()));
                    item.setItemGroup(request.itemGroup());
                    item.setBrand(request.brand());
                    item.setUnitOfMeasure(request.unitOfMeasure());
                    item.setValuationMethod(Item.ValuationMethod.valueOf(request.valuationMethod()));
                    item.setStandardRate(request.standardRate());
                    item.setIsStockItem(request.isStockItem());
                    item.setIsPurchasable(request.isPurchasable());
                    item.setIsSellable(request.isSellable());
                    item.setIsManufactured(request.isManufactured());
                    item.setHasBatch(request.hasBatch());
                    item.setHasSerialNo(request.hasSerialNo());
                    item.setBatchNumberSeries(request.batchNumberSeries());
                    item.setSerialNumberSeries(request.serialNumberSeries());
                    item.setDefaultWarehouseId(request.defaultWarehouseId());
                    item.setMinOrderQty(request.minOrderQty());
                    item.setMaxOrderQty(request.maxOrderQty());
                    item.setReorderLevel(request.reorderLevel());
                    item.setReorderQty(request.reorderQty());
                    item.setLeadTimeDays(request.leadTimeDays());
                    item.setShelfLifeDays(request.shelfLifeDays());
                    item.setHsnCode(request.hsnCode());
                    item.setNcmCode(request.ncmCode());
                    item.setCestCode(request.cestCode());
                    item.setOrigemMercadoria(request.origemMercadoria());

                    Item saved = itemRepository.save(item);
                    return ResponseEntity.ok(mapToResponse(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate item (soft delete)")
    public ResponseEntity<Void> deleteItem(@Parameter(description = "Item ID") @PathVariable UUID id) {
        return itemRepository.findById(id)
                .map(item -> {
                    item.setIsActive(false);
                    itemRepository.save(item);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/variants")
    @Operation(summary = "Get item variants")
    public ResponseEntity<List<ItemVariantResponse>> getVariants(
            @Parameter(description = "Item ID") @PathVariable UUID id,
            @Parameter(description = "Company ID") @RequestParam UUID companyId) {

        List<ItemVariant> variants = itemVariantRepository.findByCompanyIdAndItemId(companyId, id);
        return ResponseEntity.ok(variants.stream().map(this::mapVariantToResponse).collect(Collectors.toList()));
    }

    @GetMapping("/{id}/batches")
    @Operation(summary = "Get item batches")
    public ResponseEntity<List<ItemBatchResponse>> getBatches(
            @Parameter(description = "Item ID") @PathVariable UUID id,
            @Parameter(description = "Company ID") @RequestParam UUID companyId,
            @Parameter(description = "Item Variant ID (optional)") @RequestParam(required = false) UUID itemVariantId) {

        List<ItemBatch> batches;
        if (itemVariantId != null) {
            batches = itemBatchRepository.findByCompanyIdAndItemVariantId(companyId, itemVariantId);
        } else {
            batches = itemBatchRepository.findByCompanyIdAndItemId(companyId, id);
        }
        return ResponseEntity.ok(batches.stream().map(this::mapBatchToResponse).collect(Collectors.toList()));
    }

    @GetMapping("/{id}/serials")
    @Operation(summary = "Get item serial numbers")
    public ResponseEntity<List<ItemSerialNumberResponse>> getSerials(
            @Parameter(description = "Item ID") @PathVariable UUID id,
            @Parameter(description = "Company ID") @RequestParam UUID companyId,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status) {

        List<ItemSerialNumber> serials;
        if (status != null) {
            serials = itemSerialNumberRepository.findByCompanyIdAndItemIdAndStatus(
                    companyId, id, ItemSerialNumber.SerialStatus.valueOf(status));
        } else {
            serials = itemSerialNumberRepository.findByCompanyIdAndItemId(companyId, id);
        }
        return ResponseEntity.ok(serials.stream().map(this::mapSerialToResponse).collect(Collectors.toList()));
    }

    private ItemResponse mapToResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .companyId(item.getCompanyId())
                .code(item.getCode())
                .name(item.getName())
                .description(item.getDescription())
                .itemType(item.getItemType().name())
                .itemGroup(item.getItemGroup())
                .brand(item.getBrand())
                .unitOfMeasure(item.getUnitOfMeasure())
                .valuationMethod(item.getValuationMethod().name())
                .standardRate(item.getStandardRate())
                .isActive(item.getIsActive())
                .isStockItem(item.getIsStockItem())
                .isPurchasable(item.getIsPurchasable())
                .isSellable(item.getIsSellable())
                .isManufactured(item.getIsManufactured())
                .hasBatch(item.getHasBatch())
                .hasSerialNo(item.getHasSerialNo())
                .batchNumberSeries(item.getBatchNumberSeries())
                .serialNumberSeries(item.getSerialNumberSeries())
                .defaultWarehouseId(item.getDefaultWarehouseId())
                .minOrderQty(item.getMinOrderQty())
                .maxOrderQty(item.getMaxOrderQty())
                .reorderLevel(item.getReorderLevel())
                .reorderQty(item.getReorderQty())
                .leadTimeDays(item.getLeadTimeDays())
                .shelfLifeDays(item.getShelfLifeDays())
                .hsnCode(item.getHsnCode())
                .ncmCode(item.getNcmCode())
                .cestCode(item.getCestCode())
                .origemMercadoria(item.getOrigemMercadoria())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private ItemVariantResponse mapVariantToResponse(ItemVariant variant) {
        return ItemVariantResponse.builder()
                .id(variant.getId())
                .companyId(variant.getCompanyId())
                .itemId(variant.getItem().getId())
                .variantCode(variant.getVariantCode())
                .variantName(variant.getVariantName())
                .attributes(variant.getAttributes())
                .barcode(variant.getBarcode())
                .weight(variant.getWeight())
                .weightUom(variant.getWeightUom())
                .volume(variant.getVolume())
                .volumeUom(variant.getVolumeUom())
                .length(variant.getLength())
                .width(variant.getWidth())
                .height(variant.getHeight())
                .dimensionUom(variant.getDimensionUom())
                .standardRate(variant.getStandardRate())
                .isActive(variant.getIsActive())
                .createdAt(variant.getCreatedAt())
                .updatedAt(variant.getUpdatedAt())
                .build();
    }

    private ItemBatchResponse mapBatchToResponse(ItemBatch batch) {
        return ItemBatchResponse.builder()
                .id(batch.getId())
                .companyId(batch.getCompanyId())
                .itemId(batch.getItem().getId())
                .itemVariantId(batch.getItemVariantId())
                .batchNo(batch.getBatchNo())
                .manufacturingDate(batch.getManufacturingDate())
                .expiryDate(batch.getExpiryDate())
                .quantity(batch.getQuantity())
                .valuationRate(batch.getValuationRate())
                .qualityStatus(batch.getQualityStatus().name())
                .isActive(batch.getIsActive())
                .referenceDoctype(batch.getReferenceDoctype())
                .referenceName(batch.getReferenceName())
                .supplierId(batch.getSupplierId())
                .supplierBatchNo(batch.getSupplierBatchNo())
                .createdAt(batch.getCreatedAt())
                .updatedAt(batch.getUpdatedAt())
                .build();
    }

    private ItemSerialNumberResponse mapSerialToResponse(ItemSerialNumber serial) {
        return ItemSerialNumberResponse.builder()
                .id(serial.getId())
                .companyId(serial.getCompanyId())
                .itemId(serial.getItem().getId())
                .itemVariantId(serial.getItemVariantId())
                .batchId(serial.getBatchId())
                .serialNo(serial.getSerialNo())
                .status(serial.getStatus().name())
                .warehouseId(serial.getWarehouseId())
                .locationId(serial.getLocationId())
                .purchaseReceiptId(serial.getPurchaseReceiptId())
                .deliveryNoteId(serial.getDeliveryNoteId())
                .salesInvoiceId(serial.getSalesInvoiceId())
                .maintenanceStatus(serial.getMaintenanceStatus() != null ? serial.getMaintenanceStatus().name() : null)
                .warrantyExpiryDate(serial.getWarrantyExpiryDate())
                .amcExpiryDate(serial.getAmcExpiryDate())
                .createdAt(serial.getCreatedAt())
                .updatedAt(serial.getUpdatedAt())
                .build();
    }

    public record ItemRequest(
            UUID companyId,
            String code,
            String name,
            String description,
            String itemType,
            String itemGroup,
            String brand,
            String unitOfMeasure,
            String valuationMethod,
            java.math.BigDecimal standardRate,
            Boolean isStockItem,
            Boolean isPurchasable,
            Boolean isSellable,
            Boolean isManufactured,
            Boolean hasBatch,
            Boolean hasSerialNo,
            String batchNumberSeries,
            String serialNumberSeries,
            UUID defaultWarehouseId,
            java.math.BigDecimal minOrderQty,
            java.math.BigDecimal maxOrderQty,
            java.math.BigDecimal reorderLevel,
            java.math.BigDecimal reorderQty,
            Integer leadTimeDays,
            Integer shelfLifeDays,
            String hsnCode,
            String ncmCode,
            String cestCode,
            String origemMercadoria
    ) {}
}