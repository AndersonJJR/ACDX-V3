package com.erp.brazil.identity.presentation.controller;

import com.erp.brazil.common.application.response.PageableResponse;
import com.erp.brazil.common.presentation.controller.BaseController;
import com.erp.brazil.identity.application.service.CompanyService;
import com.erp.brazil.identity.domain.model.Company;
import com.erp.brazil.identity.presentation.request.CompanyRequest;
import com.erp.brazil.identity.presentation.response.CompanyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Company Controller.
 * Manages company (tenant) CRUD operations.
 */
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Tag(name = "Companies", description = "Company/Tenant management endpoints")
public class CompanyController extends BaseController {

    private final CompanyService companyService;

    @PostMapping
    @Operation(summary = "Create company", description = "Creates a new company/tenant")
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CompanyRequest request) {
        Company company = companyService.createCompany(request.toEntity(), getUserId());
        return created("/api/companies/" + company.getId(), CompanyResponse.from(company));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get company by ID", description = "Returns company details")
    public ResponseEntity<CompanyResponse> getCompany(@PathVariable String id) {
        Company company = companyService.getCompany(id);
        return ok(CompanyResponse.from(company));
    }

    @GetMapping
    @Operation(summary = "List companies", description = "Returns paginated list of companies")
    public ResponseEntity<PageableResponse<CompanyResponse>> listCompanies(
            @PageableDefault(size = 20) Pageable pageable) {
        PageableResponse<Company> page = companyService.listCompanies(pageable);
        return paged(page.map(CompanyResponse::from));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update company", description = "Updates company details")
    public ResponseEntity<CompanyResponse> updateCompany(@PathVariable String id,
                                                          @Valid @RequestBody CompanyRequest request) {
        Company company = companyService.updateCompany(id, request.toEntity(), getUserId());
        return ok(CompanyResponse.from(company));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete company", description = "Soft deletes a company")
    public ResponseEntity<Void> deleteCompany(@PathVariable String id) {
        companyService.deleteCompany(id, getUserId());
        return noContent();
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate company", description = "Activates a company")
    public ResponseEntity<CompanyResponse> activateCompany(@PathVariable String id) {
        Company company = companyService.activateCompany(id, getUserId());
        return ok(CompanyResponse.from(company));
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate company", description = "Deactivates a company")
    public ResponseEntity<CompanyResponse> deactivateCompany(@PathVariable String id) {
        Company company = companyService.deactivateCompany(id, getUserId());
        return ok(CompanyResponse.from(company));
    }

    @PostMapping("/{id}/suspend")
    @Operation(summary = "Suspend company", description = "Suspends a company")
    public ResponseEntity<CompanyResponse> suspendCompany(@PathVariable String id) {
        Company company = companyService.suspendCompany(id, getUserId());
        return ok(CompanyResponse.from(company));
    }
}