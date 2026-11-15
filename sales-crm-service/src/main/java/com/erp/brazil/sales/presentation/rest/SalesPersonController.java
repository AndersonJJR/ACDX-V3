package com.erp.brazil.sales.presentation.rest;

import com.erp.brazil.sales.domain.model.SalesPerson;
import com.erp.brazil.sales.domain.repository.SalesPersonRepository;
import com.erp.brazil.sales.domain.repository.TerritoryRepository;
import com.erp.brazil.sales.domain.repository.SalesTeamRepository;
import com.erp.brazil.sales.presentation.dto.ApiResponse;
import com.erp.brazil.sales.presentation.dto.PageResponse;
import com.erp.brazil.sales.presentation.dto.SalesPersonDTO;
import com.erp.brazil.sales.presentation.dto.request.CreateSalesPersonRequest;
import com.erp.brazil.sales.presentation.dto.request.UpdateSalesPersonRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales-persons")
public class SalesPersonController {

    private final SalesPersonRepository salesPersonRepository;
    private final TerritoryRepository territoryRepository;
    private final SalesTeamRepository salesTeamRepository;

    public SalesPersonController(SalesPersonRepository salesPersonRepository,
                                 TerritoryRepository territoryRepository,
                                 SalesTeamRepository salesTeamRepository) {
        this.salesPersonRepository = salesPersonRepository;
        this.territoryRepository = territoryRepository;
        this.salesTeamRepository = salesTeamRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SalesPersonDTO>> create(@RequestHeader("X-Company-ID") UUID companyId,
                                                               @RequestBody CreateSalesPersonRequest request) {
        SalesPerson sp = new SalesPerson();
        sp.setCompanyId(companyId);
        sp.setEmployeeId(request.employeeId());
        sp.setUserId(request.userId());
        sp.setFullName(request.fullName());
        sp.setEmail(request.email());
        sp.setPhone(request.phone());
        sp.setMobile(request.mobile());
        sp.setCommissionRate(request.commissionRate() != null ? request.commissionRate() : BigDecimal.ZERO);
        sp.setIsActive(request.isActive() != null ? request.isActive() : true);
        sp.setHireDate(request.hireDate() != null ? request.hireDate() : LocalDate.now());
        if (request.territoryId() != null) {
            sp.setTerritory(new com.erp.brazil.sales.domain.model.Territory());
            sp.getTerritory().setId(request.territoryId());
        }
        if (request.salesTeamId() != null) {
            sp.setSalesTeam(new com.erp.brazil.sales.domain.model.SalesTeam());
            sp.getSalesTeam().setId(request.salesTeamId());
        }

        sp = salesPersonRepository.save(sp);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(sp)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SalesPersonDTO>> getById(@RequestHeader("X-Company-ID") UUID companyId,
                                                                @PathVariable UUID id) {
        SalesPerson sp = salesPersonRepository.findById(id)
                .filter(s -> s.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Sales person not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(sp)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SalesPersonDTO>> update(@RequestHeader("X-Company-ID") UUID companyId,
                                                               @PathVariable UUID id,
                                                               @RequestBody UpdateSalesPersonRequest request) {
        SalesPerson sp = salesPersonRepository.findById(id)
                .filter(s -> s.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Sales person not found: " + id));

        if (request.employeeId() != null) sp.setEmployeeId(request.employeeId());
        if (request.userId() != null) sp.setUserId(request.userId());
        if (request.fullName() != null) sp.setFullName(request.fullName());
        if (request.email() != null) sp.setEmail(request.email());
        if (request.phone() != null) sp.setPhone(request.phone());
        if (request.mobile() != null) sp.setMobile(request.mobile());
        if (request.commissionRate() != null) sp.setCommissionRate(request.commissionRate());
        if (request.isActive() != null) sp.setIsActive(request.isActive());
        if (request.hireDate() != null) sp.setHireDate(request.hireDate());
        if (request.terminationDate() != null) sp.setTerminationDate(request.terminationDate());
        if (request.territoryId() != null) {
            sp.setTerritory(new com.erp.brazil.sales.domain.model.Territory());
            sp.getTerritory().setId(request.territoryId());
        }
        if (request.salesTeamId() != null) {
            sp.setSalesTeam(new com.erp.brazil.sales.domain.model.SalesTeam());
            sp.getSalesTeam().setId(request.salesTeamId());
        }

        sp = salesPersonRepository.save(sp);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(sp)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@RequestHeader("X-Company-ID") UUID companyId,
                                                     @PathVariable UUID id) {
        SalesPerson sp = salesPersonRepository.findById(id)
                .filter(s -> s.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Sales person not found: " + id));
        salesPersonRepository.delete(sp);
        return ResponseEntity.ok(ApiResponse.ok(null, "Sales person deleted successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SalesPersonDTO>>> list(@RequestHeader("X-Company-ID") UUID companyId,
                                                                           @RequestParam(required = false) Boolean active,
                                                                           Pageable pageable) {
        Page<SalesPerson> page;
        if (active != null && active) {
            page = salesPersonRepository.findByCompanyIdAndIsActiveTrue(companyId, pageable);
        } else {
            page = salesPersonRepository.findByCompanyId(companyId, pageable);
        }
        return ResponseEntity.ok(ApiResponse.ok(mapToPageResponse(page)));
    }

    private SalesPersonDTO mapToDTO(SalesPerson sp) {
        return new SalesPersonDTO(
                sp.getId(), sp.getCompanyId(), sp.getEmployeeId(), sp.getUserId(), sp.getFullName(),
                sp.getEmail(), sp.getPhone(), sp.getMobile(), sp.getCommissionRate(), sp.getIsActive(),
                sp.getHireDate(), sp.getTerminationDate(),
                sp.getTerritory() != null ? sp.getTerritory().getId() : null,
                sp.getTerritory() != null ? sp.getTerritory().getName() : null,
                sp.getSalesTeam() != null ? sp.getSalesTeam().getId() : null,
                sp.getSalesTeam() != null ? sp.getSalesTeam().getName() : null,
                sp.getCreatedAt(), sp.getUpdatedAt()
        );
    }

    private PageResponse<SalesPersonDTO> mapToPageResponse(Page<SalesPerson> page) {
        return new PageResponse<>(
                page.getContent().stream().map(this::mapToDTO).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast(), page.isEmpty()
        );
    }
}