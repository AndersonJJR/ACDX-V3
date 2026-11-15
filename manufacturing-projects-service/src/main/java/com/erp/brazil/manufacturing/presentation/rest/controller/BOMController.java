package com.erp.brazil.manufacturing.presentation.rest.controller;

import com.erp.brazil.manufacturing.application.usecase.ExplodeBOMUseCase;
import com.erp.brazil.manufacturing.domain.model.BOM;
import com.erp.brazil.manufacturing.domain.model.BOM.BOMStatus;
import com.erp.brazil.manufacturing.domain.model.BOM.BOMType;
import com.erp.brazil.manufacturing.domain.repository.BOMRepository;
import com.erp.brazil.manufacturing.presentation.rest.dto.BOMDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/boms")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "BOM", description = "Bill of Materials management")
public class BOMController {

    private final BOMRepository bomRepository;
    private final ExplodeBOMUseCase explodeBOMUseCase;

    @PostMapping
    @Operation(summary = "Create a new BOM")
    public ResponseEntity<BOMDto> createBOM(@Valid @RequestBody BOMDto.CreateBOMRequest request) {
        log.info("Creating BOM for product {}", request.getProductId());

        BOM bom = BOM.builder()
                .productId(request.getProductId())
                .type(BOMType.valueOf(request.getType()))
                .status(BOMStatus.DRAFT)
                .version(request.getVersion())
                .description(request.getDescription())
                .baseQuantity(request.getBaseQuantity() != null ? request.getBaseQuantity() : BigDecimal.ONE)
                .scrapPercentage(request.getScrapPercentage() != null ? request.getScrapPercentage() : BigDecimal.ZERO)
                .isPhantom(request.getIsPhantom() != null ? request.getIsPhantom() : false)
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .build();

        if (request.getItems() != null) {
            for (BOMDto.CreateBOMItemRequest itemReq : request.getItems()) {
                var item = com.erp.brazil.manufacturing.domain.model.BOMItem.builder()
                        .bom(bom)
                        .componentId(itemReq.getComponentId())
                        .componentType(itemReq.getComponentType())
                        .quantity(itemReq.getQuantity())
                        .scrapPercentage(itemReq.getScrapPercentage() != null ? itemReq.getScrapPercentage() : BigDecimal.ZERO)
                        .position(itemReq.getPosition() != null ? itemReq.getPosition() : 0)
                        .unitOfMeasure(itemReq.getUnitOfMeasure())
                        .isPhantom(itemReq.getIsPhantom() != null ? itemReq.getIsPhantom() : false)
                        .isOptional(itemReq.getIsOptional() != null ? itemReq.getIsOptional() : false)
                        .referenceDesignator(itemReq.getReferenceDesignator())
                        .findNumber(itemReq.getFindNumber())
                        .notes(itemReq.getNotes())
                        .effectiveFrom(itemReq.getEffectiveFrom())
                        .effectiveTo(itemReq.getEffectiveTo())
                        .build();
                bom.addItem(item);
            }
        }

        if (request.getOperations() != null) {
            for (BOMDto.CreateBOMOperationRequest opReq : request.getOperations()) {
                var op = com.erp.brazil.manufacturing.domain.model.BOMOperation.builder()
                        .bom(bom)
                        .sequence(opReq.getSequence())
                        .workCenterId(opReq.getWorkCenterId())
                        .workStationId(opReq.getWorkStationId())
                        .operationName(opReq.getOperationName())
                        .description(opReq.getDescription())
                        .setupTimeMinutes(opReq.getSetupTimeMinutes() != null ? opReq.getSetupTimeMinutes() : BigDecimal.ZERO)
                        .runTimeMinutes(opReq.getRunTimeMinutes() != null ? opReq.getRunTimeMinutes() : BigDecimal.ZERO)
                        .queueTimeMinutes(opReq.getQueueTimeMinutes() != null ? opReq.getQueueTimeMinutes() : BigDecimal.ZERO)
                        .moveTimeMinutes(opReq.getMoveTimeMinutes() != null ? opReq.getMoveTimeMinutes() : BigDecimal.ZERO)
                        .crewSize(opReq.getCrewSize() != null ? opReq.getCrewSize() : 1)
                        .isOutsourced(opReq.getIsOutsourced() != null ? opReq.getIsOutsourced() : false)
                        .vendorId(opReq.getVendorId())
                        .notes(opReq.getNotes())
                        .build();
                bom.addOperation(op);
            }
        }

        bomRepository.save(bom);
        return ResponseEntity.status(HttpStatus.CREATED).body(BOMDto.fromEntity(bom));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get BOM by ID")
    public ResponseEntity<BOMDto> getBOM(@PathVariable UUID id) {
        BOM bom = bomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("BOM not found: " + id));
        return ResponseEntity.ok(BOMDto.fromEntity(bom));
    }

    @GetMapping
    @Operation(summary = "List BOMs with pagination")
    public ResponseEntity<Page<BOMDto>> listBOMs(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<BOM> boms;
        if (productId != null && status != null) {
            boms = bomRepository.findByProductIdAndStatusOrderByVersionDesc(productId, BOMStatus.valueOf(status));
        } else if (productId != null) {
            boms = bomRepository.findByProductId(productId, pageable);
        } else if (status != null) {
            boms = bomRepository.findByStatus(BOMStatus.valueOf(status), pageable);
        } else {
            boms = bomRepository.findAll(pageable);
        }
        return ResponseEntity.ok(boms.map(BOMDto::fromEntity));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update BOM")
    public ResponseEntity<BOMDto> updateBOM(@PathVariable UUID id, @Valid @RequestBody BOMDto.CreateBOMRequest request) {
        BOM bom = bomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("BOM not found: " + id));

        bom.setType(BOMType.valueOf(request.getType()));
        bom.setVersion(request.getVersion());
        bom.setDescription(request.getDescription());
        bom.setBaseQuantity(request.getBaseQuantity());
        bom.setScrapPercentage(request.getScrapPercentage());
        bom.setIsPhantom(request.getIsPhantom());
        bom.setEffectiveFrom(request.getEffectiveFrom());
        bom.setEffectiveTo(request.getEffectiveTo());

        bomRepository.save(bom);
        return ResponseEntity.ok(BOMDto.fromEntity(bom));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate BOM")
    public ResponseEntity<BOMDto> activateBOM(@PathVariable UUID id) {
        BOM bom = bomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("BOM not found: " + id));
        bom.setStatus(BOMStatus.ACTIVE);
        bomRepository.save(bom);
        return ResponseEntity.ok(BOMDto.fromEntity(bom));
    }

    @PatchMapping("/{id}/obsolete")
    @Operation(summary = "Mark BOM as obsolete")
    public ResponseEntity<BOMDto> obsoleteBOM(@PathVariable UUID id) {
        BOM bom = bomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("BOM not found: " + id));
        bom.setStatus(BOMStatus.OBSOLETE);
        bomRepository.save(bom);
        return ResponseEntity.ok(BOMDto.fromEntity(bom));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete BOM (only if draft)")
    public ResponseEntity<Void> deleteBOM(@PathVariable UUID id) {
        BOM bom = bomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("BOM not found: " + id));
        if (bom.getStatus() != BOMStatus.DRAFT) {
            throw new IllegalStateException("Can only delete draft BOMs");
        }
        bomRepository.delete(bom);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/explode")
    @Operation(summary = "Explode BOM to flat list of components")
    public ResponseEntity<List<BOMDto.BOMExplosionResultDto>> explodeBOM(
            @PathVariable UUID id,
            @Valid @RequestBody BOMDto.ExplodeBOMRequest request) {
        var results = explodeBOMUseCase.execute(new ExplodeBOMUseCase.ExplodeBOMCommand(
                id,
                request.getProductionQuantity() != null ? request.getProductionQuantity() : BigDecimal.ONE,
                request.getIncludePhantoms() != null ? request.getIncludePhantoms() : false
        ));

        List<BOMDto.BOMExplosionResultDto> dtos = results.stream().map(r ->
                BOMDto.BOMExplosionResultDto.builder()
                        .componentId(r.componentId())
                        .componentType(r.componentType())
                        .quantityPerUnit(r.quantityPerUnit())
                        .totalQuantity(r.totalQuantity())
                        .level(r.level())
                        .path(r.path())
                        .isPhantom(r.isPhantom())
                        .bomId(r.bomId())
                        .position(r.position())
                        .unitOfMeasure(r.unitOfMeasure())
                        .build()
        ).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}/tree")
    @Operation(summary = "Get BOM explosion as tree structure")
    public ResponseEntity<List<BOMDto.BOMExplosionResultDto>> getBOMTree(@PathVariable UUID id) {
        var results = explodeBOMUseCase.explodeBOMTree(id);

        List<BOMDto.BOMExplosionResultDto> dtos = results.stream().map(r ->
                BOMDto.BOMExplosionResultDto.builder()
                        .componentId(r.componentId())
                        .componentType(r.componentType())
                        .quantityPerUnit(r.quantityPerUnit())
                        .totalQuantity(r.totalQuantity())
                        .level(r.level())
                        .path(r.path())
                        .isPhantom(r.isPhantom())
                        .bomId(r.bomId())
                        .position(r.position())
                        .unitOfMeasure(r.unitOfMeasure())
                        .build()
        ).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/product/{productId}/explode")
    @Operation(summary = "Explode BOM by product ID")
    public ResponseEntity<List<BOMDto.BOMExplosionResultDto>> explodeBOMByProduct(
            @PathVariable UUID productId,
            @RequestParam BigDecimal quantity,
            @RequestParam(defaultValue = "false") boolean includePhantoms) {
        var results = explodeBOMUseCase.explodeByProduct(productId, quantity, includePhantoms);

        List<BOMDto.BOMExplosionResultDto> dtos = results.stream().map(r ->
                BOMDto.BOMExplosionResultDto.builder()
                        .componentId(r.componentId())
                        .componentType(r.componentType())
                        .quantityPerUnit(r.quantityPerUnit())
                        .totalQuantity(r.totalQuantity())
                        .level(r.level())
                        .path(r.path())
                        .isPhantom(r.isPhantom())
                        .bomId(r.bomId())
                        .position(r.position())
                        .unitOfMeasure(r.unitOfMeasure())
                        .build()
        ).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}