package com.erp.brazil.sales.presentation.rest;

import com.erp.brazil.sales.domain.model.Territory;
import com.erp.brazil.sales.domain.repository.TerritoryRepository;
import com.erp.brazil.sales.presentation.dto.ApiResponse;
import com.erp.brazil.sales.presentation.dto.PageResponse;
import com.erp.brazil.sales.presentation.dto.TerritoryDTO;
import com.erp.brazil.sales.presentation.dto.request.CreateTerritoryRequest;
import com.erp.brazil.sales.presentation.dto.request.UpdateTerritoryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/territories")
public class TerritoryController {

    private final TerritoryRepository territoryRepository;

    public TerritoryController(TerritoryRepository territoryRepository) {
        this.territoryRepository = territoryRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TerritoryDTO>> create(@RequestHeader("X-Company-ID") UUID companyId,
                                                             @RequestBody CreateTerritoryRequest request) {
        Territory territory = new Territory();
        territory.setCompanyId(companyId);
        territory.setCode(request.code());
        territory.setName(request.name());
        territory.setDescription(request.description());
        territory.setState(request.state());
        territory.setCities(request.cities() != null ? String.join(",", request.cities()) : null);
        territory.setZipCodeRanges(request.zipCodeRanges() != null ? String.join(",", request.zipCodeRanges()) : null);
        territory.setActive(request.active() != null ? request.active() : true);
        if (request.parentTerritoryId() != null) {
            territory.setParentTerritory(new Territory());
            territory.getParentTerritory().setId(request.parentTerritoryId());
        }

        territory = territoryRepository.save(territory);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(territory)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TerritoryDTO>> getById(@RequestHeader("X-Company-ID") UUID companyId,
                                                              @PathVariable UUID id) {
        Territory territory = territoryRepository.findById(id)
                .filter(t -> t.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Territory not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(territory)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TerritoryDTO>> update(@RequestHeader("X-Company-ID") UUID companyId,
                                                             @PathVariable UUID id,
                                                             @RequestBody UpdateTerritoryRequest request) {
        Territory territory = territoryRepository.findById(id)
                .filter(t -> t.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Territory not found: " + id));

        if (request.code() != null) territory.setCode(request.code());
        if (request.name() != null) territory.setName(request.name());
        if (request.description() != null) territory.setDescription(request.description());
        if (request.state() != null) territory.setState(request.state());
        if (request.cities() != null) territory.setCities(String.join(",", request.cities()));
        if (request.zipCodeRanges() != null) territory.setZipCodeRanges(String.join(",", request.zipCodeRanges()));
        if (request.active() != null) territory.setActive(request.active());
        if (request.parentTerritoryId() != null) {
            territory.setParentTerritory(new Territory());
            territory.getParentTerritory().setId(request.parentTerritoryId());
        }

        territory = territoryRepository.save(territory);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(territory)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@RequestHeader("X-Company-ID") UUID companyId,
                                                     @PathVariable UUID id) {
        Territory territory = territoryRepository.findById(id)
                .filter(t -> t.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Territory not found: " + id));
        territoryRepository.delete(territory);
        return ResponseEntity.ok(ApiResponse.ok(null, "Territory deleted successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TerritoryDTO>>> list(@RequestHeader("X-Company-ID") UUID companyId,
                                                                         @RequestParam(required = false) Boolean active,
                                                                         Pageable pageable) {
        Page<Territory> page;
        if (active != null && active) {
            page = territoryRepository.findByCompanyIdAndActiveTrue(companyId, pageable);
        } else {
            page = territoryRepository.findByCompanyId(companyId, pageable);
        }
        return ResponseEntity.ok(ApiResponse.ok(mapToPageResponse(page)));
    }

    private TerritoryDTO mapToDTO(Territory t) {
        List<TerritoryDTO> subTerritories = t.getSubTerritories().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return new TerritoryDTO(
                t.getId(), t.getCompanyId(), t.getCode(), t.getName(), t.getDescription(),
                t.getState(), t.getCities() != null ? List.of(t.getCities().split(",")) : java.util.List.of(),
                t.getZipCodeRanges() != null ? List.of(t.getZipCodeRanges().split(",")) : java.util.List.of(),
                t.getActive(), t.getParentTerritory() != null ? t.getParentTerritory().getId() : null,
                t.getParentTerritory() != null ? t.getParentTerritory().getName() : null,
                subTerritories,
                (long) t.getCustomers().size(),
                (long) t.getLeads().size(),
                (long) t.getOpportunities().size(),
                (long) t.getSalesPersons().size(),
                t.getCreatedAt(), t.getUpdatedAt()
        );
    }

    private PageResponse<TerritoryDTO> mapToPageResponse(Page<Territory> page) {
        return new PageResponse<>(
                page.getContent().stream().map(this::mapToDTO).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast(), page.isEmpty()
        );
    }
}