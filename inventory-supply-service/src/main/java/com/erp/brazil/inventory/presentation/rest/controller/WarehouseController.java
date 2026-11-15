package com.erp.brazil.inventory.presentation.rest.controller;

import com.erp.brazil.inventory.domain.model.Location;
import com.erp.brazil.inventory.domain.model.Warehouse;
import com.erp.brazil.inventory.domain.repository.LocationRepository;
import com.erp.brazil.inventory.domain.repository.WarehouseRepository;
import com.erp.brazil.inventory.presentation.rest.dto.LocationResponse;
import com.erp.brazil.inventory.presentation.rest.dto.WarehouseResponse;
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
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
@Tag(name = "Warehouses", description = "Warehouse and location management")
public class WarehouseController {

    private final WarehouseRepository warehouseRepository;
    private final LocationRepository locationRepository;

    @GetMapping
    @Operation(summary = "List warehouses")
    public ResponseEntity<Page<WarehouseResponse>> listWarehouses(
            @Parameter(description = "Company ID") @RequestParam UUID companyId,
            @Parameter(description = "Active only") @RequestParam(defaultValue = "true") boolean activeOnly,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {

        Page<Warehouse> warehouses = activeOnly
                ? warehouseRepository.findByCompanyIdAndIsActiveTrue(companyId, pageable)
                : warehouseRepository.findAll(pageable);

        return ResponseEntity.ok(warehouses.map(this::mapToResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get warehouse by ID")
    public ResponseEntity<WarehouseResponse> getWarehouse(@Parameter(description = "Warehouse ID") @PathVariable UUID id) {
        return warehouseRepository.findById(id)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new warehouse")
    public ResponseEntity<WarehouseResponse> createWarehouse(@RequestBody WarehouseRequest request) {
        Warehouse warehouse = Warehouse.builder()
                .companyId(request.companyId())
                .code(request.code())
                .name(request.name())
                .address(request.address())
                .city(request.city())
                .state(request.state())
                .country(request.country())
                .postalCode(request.postalCode())
                .isActive(true)
                .isDefault(request.isDefault() != null ? request.isDefault() : false)
                .allowNegativeStock(request.allowNegativeStock() != null ? request.allowNegativeStock() : false)
                .build();

        Warehouse saved = warehouseRepository.save(warehouse);
        return ResponseEntity.ok(mapToResponse(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update warehouse")
    public ResponseEntity<WarehouseResponse> updateWarehouse(
            @Parameter(description = "Warehouse ID") @PathVariable UUID id,
            @RequestBody WarehouseRequest request) {

        return warehouseRepository.findById(id)
                .map(warehouse -> {
                    warehouse.setCode(request.code());
                    warehouse.setName(request.name());
                    warehouse.setAddress(request.address());
                    warehouse.setCity(request.city());
                    warehouse.setState(request.state());
                    warehouse.setCountry(request.country());
                    warehouse.setPostalCode(request.postalCode());
                    warehouse.setIsActive(request.isActive());
                    warehouse.setIsDefault(request.isDefault());
                    warehouse.setAllowNegativeStock(request.allowNegativeStock());

                    Warehouse saved = warehouseRepository.save(warehouse);
                    return ResponseEntity.ok(mapToResponse(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate warehouse (soft delete)")
    public ResponseEntity<Void> deleteWarehouse(@Parameter(description = "Warehouse ID") @PathVariable UUID id) {
        return warehouseRepository.findById(id)
                .map(warehouse -> {
                    warehouse.setIsActive(false);
                    warehouseRepository.save(warehouse);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/locations")
    @Operation(summary = "Get warehouse locations")
    public ResponseEntity<List<LocationResponse>> getLocations(
            @Parameter(description = "Warehouse ID") @PathVariable UUID id,
            @Parameter(description = "Active only") @RequestParam(defaultValue = "true") boolean activeOnly) {

        List<Location> locations = activeOnly
                ? locationRepository.findByWarehouseIdAndIsActiveTrue(id)
                : locationRepository.findByWarehouseId(id);

        return ResponseEntity.ok(locations.stream().map(this::mapLocationToResponse).collect(Collectors.toList()));
    }

    private WarehouseResponse mapToResponse(Warehouse warehouse) {
        return WarehouseResponse.builder()
                .id(warehouse.getId())
                .companyId(warehouse.getCompanyId())
                .code(warehouse.getCode())
                .name(warehouse.getName())
                .address(warehouse.getAddress())
                .city(warehouse.getCity())
                .state(warehouse.getState())
                .country(warehouse.getCountry())
                .postalCode(warehouse.getPostalCode())
                .isActive(warehouse.getIsActive())
                .isDefault(warehouse.getIsDefault())
                .allowNegativeStock(warehouse.getAllowNegativeStock())
                .createdAt(warehouse.getCreatedAt())
                .updatedAt(warehouse.getUpdatedAt())
                .build();
    }

    private LocationResponse mapLocationToResponse(Location location) {
        return LocationResponse.builder()
                .id(location.getId())
                .companyId(location.getCompanyId())
                .warehouseId(location.getWarehouse().getId())
                .code(location.getCode())
                .name(location.getName())
                .locationType(location.getLocationType().name())
                .aisle(location.getAisle())
                .rack(location.getRack())
                .shelf(location.getShelf())
                .bin(location.getBin())
                .isActive(location.getIsActive())
                .isPickable(location.getIsPickable())
                .isReceivable(location.getIsReceivable())
                .createdAt(location.getCreatedAt())
                .updatedAt(location.getUpdatedAt())
                .build();
    }

    public record WarehouseRequest(
            UUID companyId,
            String code,
            String name,
            String address,
            String city,
            String state,
            String country,
            String postalCode,
            Boolean isDefault,
            Boolean allowNegativeStock,
            Boolean isActive
    ) {}
}