package com.erp.brazil.sales.presentation.rest;

import com.erp.brazil.sales.domain.model.SalesTeam;
import com.erp.brazil.sales.domain.model.SalesPerson;
import com.erp.brazil.sales.domain.repository.SalesTeamRepository;
import com.erp.brazil.sales.domain.repository.SalesPersonRepository;
import com.erp.brazil.sales.presentation.dto.ApiResponse;
import com.erp.brazil.sales.presentation.dto.PageResponse;
import com.erp.brazil.sales.presentation.dto.SalesPersonDTO;
import com.erp.brazil.sales.presentation.dto.SalesTeamDTO;
import com.erp.brazil.sales.presentation.dto.request.CreateSalesTeamRequest;
import com.erp.brazil.sales.presentation.dto.request.UpdateSalesTeamRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sales-teams")
public class SalesTeamController {

    private final SalesTeamRepository salesTeamRepository;
    private final SalesPersonRepository salesPersonRepository;

    public SalesTeamController(SalesTeamRepository salesTeamRepository,
                               SalesPersonRepository salesPersonRepository) {
        this.salesTeamRepository = salesTeamRepository;
        this.salesPersonRepository = salesPersonRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SalesTeamDTO>> create(@RequestHeader("X-Company-ID") UUID companyId,
                                                             @RequestBody CreateSalesTeamRequest request) {
        SalesTeam team = new SalesTeam();
        team.setCompanyId(companyId);
        team.setCode(request.code());
        team.setName(request.name());
        team.setDescription(request.description());
        team.setIsActive(request.isActive() != null ? request.isActive() : true);
        if (request.managerId() != null) {
            team.setManager(new SalesPerson());
            team.getManager().setId(request.managerId());
        }
        if (request.memberIds() != null) {
            List<SalesPerson> members = salesPersonRepository.findAllById(request.memberIds());
            team.setMembers(members);
        }

        team = salesTeamRepository.save(team);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(team)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SalesTeamDTO>> getById(@RequestHeader("X-Company-ID") UUID companyId,
                                                              @PathVariable UUID id) {
        SalesTeam team = salesTeamRepository.findById(id)
                .filter(t -> t.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Sales team not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(team)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SalesTeamDTO>> update(@RequestHeader("X-Company-ID") UUID companyId,
                                                             @PathVariable UUID id,
                                                             @RequestBody UpdateSalesTeamRequest request) {
        SalesTeam team = salesTeamRepository.findById(id)
                .filter(t -> t.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Sales team not found: " + id));

        if (request.code() != null) team.setCode(request.code());
        if (request.name() != null) team.setName(request.name());
        if (request.description() != null) team.setDescription(request.description());
        if (request.managerId() != null) {
            team.setManager(new SalesPerson());
            team.getManager().setId(request.managerId());
        }
        if (request.memberIds() != null) {
            List<SalesPerson> members = salesPersonRepository.findAllById(request.memberIds());
            team.setMembers(members);
        }
        if (request.isActive() != null) team.setIsActive(request.isActive());

        team = salesTeamRepository.save(team);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(team)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@RequestHeader("X-Company-ID") UUID companyId,
                                                     @PathVariable UUID id) {
        SalesTeam team = salesTeamRepository.findById(id)
                .filter(t -> t.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Sales team not found: " + id));
        salesTeamRepository.delete(team);
        return ResponseEntity.ok(ApiResponse.ok(null, "Sales team deleted successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SalesTeamDTO>>> list(@RequestHeader("X-Company-ID") UUID companyId,
                                                                         @RequestParam(required = false) Boolean active,
                                                                         Pageable pageable) {
        Page<SalesTeam> page;
        if (active != null && active) {
            page = salesTeamRepository.findByCompanyIdAndIsActiveTrue(companyId, pageable);
        } else {
            page = salesTeamRepository.findByCompanyId(companyId, pageable);
        }
        return ResponseEntity.ok(ApiResponse.ok(mapToPageResponse(page)));
    }

    private SalesTeamDTO mapToDTO(SalesTeam t) {
        List<SalesPersonDTO> members = t.getMembers().stream()
                .map(sp -> new SalesPersonDTO(
                        sp.getId(), sp.getCompanyId(), sp.getEmployeeId(), sp.getUserId(), sp.getFullName(),
                        sp.getEmail(), sp.getPhone(), sp.getMobile(), sp.getCommissionRate(), sp.getIsActive(),
                        sp.getHireDate(), sp.getTerminationDate(),
                        sp.getTerritory() != null ? sp.getTerritory().getId() : null,
                        sp.getTerritory() != null ? sp.getTerritory().getName() : null,
                        sp.getSalesTeam() != null ? sp.getSalesTeam().getId() : null,
                        sp.getSalesTeam() != null ? sp.getSalesTeam().getName() : null,
                        sp.getCreatedAt(), sp.getUpdatedAt()
                ))
                .collect(Collectors.toList());

        return new SalesTeamDTO(
                t.getId(), t.getCompanyId(), t.getCode(), t.getName(), t.getDescription(),
                t.getManager() != null ? t.getManager().getId() : null,
                t.getManager() != null ? t.getManager().getFullName() : null,
                t.getIsActive(), t.getCreatedAt(), t.getUpdatedAt(), members
        );
    }

    private PageResponse<SalesTeamDTO> mapToPageResponse(Page<SalesTeam> page) {
        return new PageResponse<>(
                page.getContent().stream().map(this::mapToDTO).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast(), page.isEmpty()
        );
    }
}