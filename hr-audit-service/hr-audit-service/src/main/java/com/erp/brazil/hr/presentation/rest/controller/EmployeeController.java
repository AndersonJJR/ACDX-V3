package com.erp.brazil.hr.presentation.rest.controller;


import com.erp.brazil.hr.domain.model.*;
import com.erp.brazil.hr.domain.repository.*;
import com.erp.brazil.hr.presentation.rest.dto.EmployeeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Employee management APIs")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final EmploymentContractRepository contractRepository;
    private final DepartmentRepository departmentRepository;
    private final CostCenterRepository costCenterRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN')")
    @Operation(summary = "List employees")
    public ResponseEntity<EmployeeDto.PageResponse> list(
            @RequestParam Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        Page<Employee> employees;

        if (status != null) {
            employees = employeeRepository.findByCompanyIdAndStatus(companyId, status, pageable);
        } else if (search != null && !search.isBlank()) {
            // Would need custom query for search
            employees = employeeRepository.findByCompanyId(companyId, pageable);
        } else {
            employees = employeeRepository.findByCompanyId(companyId, pageable);
        }

        List<EmployeeDto.Response> content = employees.getContent().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());

        return ResponseEntity.ok(EmployeeDto.PageResponse.builder()
            .content(content)
            .page(employees.getNumber())
            .size(employees.getSize())
            .totalElements(employees.getTotalElements())
            .totalPages(employees.getTotalPages())
            .first(employees.isFirst())
            .last(employees.isLast())
            .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<EmployeeDto.Response> get(@PathVariable Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        return ResponseEntity.ok(toResponse(employee));
    }

    @GetMapping("/uuid/{uuid}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Get employee by UUID")
    public ResponseEntity<EmployeeDto.Response> getByUuid(@PathVariable String uuid) {
        Employee employee = employeeRepository.findByUuid(uuid)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        return ResponseEntity.ok(toResponse(employee));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Create employee")
    public ResponseEntity<EmployeeDto.Response> create(@Valid @RequestBody EmployeeDto.Request request) {
        if (employeeRepository.existsByCpfAndCompanyId(request.getCpf(), request.getCompanyId())) {
            throw new IllegalArgumentException("CPF already registered for this company");
        }

        Employee employee = new Employee();
        employee.setCompanyId(request.getCompanyId());
        employee.setEmployeeNumber(request.getEmployeeNumber());
        employee.setFullName(request.getFullName());
        employee.setCpf(request.getCpf());
        employee.setRg(request.getRg());
        employee.setBirthDate(request.getBirthDate());
        employee.setGender(request.getGender());
        employee.setMaritalStatus(request.getMaritalStatus());
        employee.setNationality(request.getNationality() != null ? request.getNationality() : "BRASILEIRA");
        employee.setNaturalness(request.getNaturalness());
        employee.setMotherName(request.getMotherName());
        employee.setFatherName(request.getFatherName());
        employee.setEmail(request.getEmail());
        employee.setPersonalPhone(request.getPersonalPhone());
        employee.setCorporatePhone(request.getCorporatePhone());
        employee.setAddressCep(request.getAddressCep());
        employee.setAddressStreet(request.getAddressStreet());
        employee.setAddressNumber(request.getAddressNumber());
        employee.setAddressComplement(request.getAddressComplement());
        employee.setAddressNeighborhood(request.getAddressNeighborhood());
        employee.setAddressCity(request.getAddressCity());
        employee.setAddressState(request.getAddressState());
        employee.setAddressCountry(request.getAddressCountry() != null ? request.getAddressCountry() : "BRASIL");
        employee.setBankCode(request.getBankCode());
        employee.setBankBranch(request.getBankBranch());
        employee.setBankAccount(request.getBankAccount());
        employee.setBankAccountType(request.getBankAccountType());
        employee.setPixKey(request.getPixKey());
        employee.setEmergencyContactName(request.getEmergencyContactName());
        employee.setEmergencyContactPhone(request.getEmergencyContactPhone());
        employee.setEmergencyContactRelationship(request.getEmergencyContactRelationship());
        employee.setHireDate(request.getHireDate());
        employee.setStatus(EmployeeStatus.ACTIVE);

        Employee saved = employeeRepository.save(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Update employee")
    public ResponseEntity<EmployeeDto.Response> update(@PathVariable Long id, @Valid @RequestBody EmployeeDto.Request request) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        employee.setFullName(request.getFullName());
        employee.setRg(request.getRg());
        employee.setBirthDate(request.getBirthDate());
        employee.setGender(request.getGender());
        employee.setMaritalStatus(request.getMaritalStatus());
        employee.setNationality(request.getNationality());
        employee.setNaturalness(request.getNaturalness());
        employee.setMotherName(request.getMotherName());
        employee.setFatherName(request.getFatherName());
        employee.setEmail(request.getEmail());
        employee.setPersonalPhone(request.getPersonalPhone());
        employee.setCorporatePhone(request.getCorporatePhone());
        employee.setAddressCep(request.getAddressCep());
        employee.setAddressStreet(request.getAddressStreet());
        employee.setAddressNumber(request.getAddressNumber());
        employee.setAddressComplement(request.getAddressComplement());
        employee.setAddressNeighborhood(request.getAddressNeighborhood());
        employee.setAddressCity(request.getAddressCity());
        employee.setAddressState(request.getAddressState());
        employee.setAddressCountry(request.getAddressCountry());
        employee.setBankCode(request.getBankCode());
        employee.setBankBranch(request.getBankBranch());
        employee.setBankAccount(request.getBankAccount());
        employee.setBankAccountType(request.getBankAccountType());
        employee.setPixKey(request.getPixKey());
        employee.setEmergencyContactName(request.getEmergencyContactName());
        employee.setEmergencyContactPhone(request.getEmergencyContactPhone());
        employee.setEmergencyContactRelationship(request.getEmergencyContactRelationship());

        Employee saved = employeeRepository.save(employee);
        return ResponseEntity.ok(toResponse(saved));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Terminate employee (soft delete)")
    public ResponseEntity<Void> terminate(@PathVariable Long id, @RequestParam String reason) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        employee.setStatus(EmployeeStatus.TERMINATED);
        employee.setTerminationDate(LocalDate.now());
        employee.setTerminationReason(reason);
        employeeRepository.save(employee);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/contract")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Get employee contract")
    public ResponseEntity<EmployeeDto.ContractResponse> getContract(@PathVariable Long id) {
        EmploymentContract contract = contractRepository.findByEmployeeId(id)
            .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        return ResponseEntity.ok(toContractResponse(contract));
    }

    @PostMapping("/{id}/contract")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Create/Update employee contract")
    public ResponseEntity<EmployeeDto.ContractResponse> createContract(@PathVariable Long id, @Valid @RequestBody EmployeeDto.ContractRequest request) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        EmploymentContract contract = contractRepository.findByEmployeeId(id)
            .orElseGet(() -> {
                EmploymentContract c = new EmploymentContract();
                c.setEmployee(employee);
                return c;
            });

        contract.setContractType(request.getContractType());
        contract.setWorkJourney(request.getWorkJourney());
        contract.setWeeklyHours(request.getWeeklyHours() != null ? request.getWeeklyHours() : BigDecimal.valueOf(44));
        contract.setDailyHours(request.getDailyHours() != null ? request.getDailyHours() : BigDecimal.valueOf(8));
        contract.setSalaryBase(request.getSalaryBase());
        contract.setPositionTitle(request.getPositionTitle());
        contract.setPositionCbo(request.getPositionCbo());
        contract.setDepartmentId(request.getDepartmentId());
        contract.setCostCenterId(request.getCostCenterId());
        contract.setReportingToId(request.getReportingToId());
        contract.setWorkLocation(request.getWorkLocation() != null ? request.getWorkLocation() : WorkLocation.ONSITE);
        contract.setWorkCity(request.getWorkCity());
        contract.setWorkState(request.getWorkState());
        contract.setUnionId(request.getUnionId());
        contract.setCollectiveAgreementId(request.getCollectiveAgreementId());
        contract.setProbationEndDate(request.getProbationEndDate());
        contract.setContractStartDate(request.getContractStartDate());
        contract.setContractEndDate(request.getContractEndDate());
        contract.setFgtsOptIn(request.getFgtsOptIn() != null ? request.getFgtsOptIn() : true);
        contract.setFgtsAccount(request.getFgtsAccount());
        contract.setBenefitTransportVoucher(request.getBenefitTransportVoucher() != null ? request.getBenefitTransportVoucher() : true);
        contract.setBenefitMealVoucher(request.getBenefitMealVoucher() != null ? request.getBenefitMealVoucher() : true);
        contract.setBenefitHealthInsurance(request.getBenefitHealthInsurance() != null ? request.getBenefitHealthInsurance() : false);
        contract.setBenefitDentalInsurance(request.getBenefitDentalInsurance() != null ? request.getBenefitDentalInsurance() : false);
        contract.setBenefitLifeInsurance(request.getBenefitLifeInsurance() != null ? request.getBenefitLifeInsurance() : false);

        EmploymentContract saved = contractRepository.save(contract);
        return ResponseEntity.ok(toContractResponse(saved));
    }

    @GetMapping("/birthdays")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Get employees with birthday today")
    public ResponseEntity<List<EmployeeDto.Response>> getBirthdays(@RequestParam Long companyId) {
        LocalDate today = LocalDate.now();
        List<Employee> employees = employeeRepository.findBirthdaysByCompanyAndDay(companyId, today.getMonthValue(), today.getDayOfMonth());
        return ResponseEntity.ok(employees.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/contracts-expiring")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Get employees with contracts expiring soon")
    public ResponseEntity<List<EmployeeDto.Response>> getContractsExpiring(@RequestParam Long companyId, @RequestParam int days) {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(days);
        List<Employee> employees = employeeRepository.findContractsExpiringBetween(companyId, start, end);
        return ResponseEntity.ok(employees.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    private EmployeeDto.Response toResponse(Employee employee) {
        EmploymentContract contract = contractRepository.findByEmployeeId(employee.getId()).orElse(null);
        return EmployeeDto.Response.builder()
            .id(employee.getId())
            .uuid(employee.getUuid())
            .employeeNumber(employee.getEmployeeNumber())
            .fullName(employee.getFullName())
            .cpf(employee.getCpf())
            .maskedCpf(employee.getMaskedCpf())
            .email(employee.getEmail())
            .maskedEmail(employee.getMaskedEmail())
            .birthDate(employee.getBirthDate())
            .gender(employee.getGender())
            .maritalStatus(employee.getMaritalStatus())
            .status(employee.getStatus())
            .hireDate(employee.getHireDate())
            .terminationDate(employee.getTerminationDate())
            .positionTitle(contract != null ? contract.getPositionTitle() : null)
            .departmentName(null) // Would need department lookup
            .salaryBase(contract != null ? contract.getSalaryBase() : null)
            .contractType(contract != null ? contract.getContractType() : null)
            .workLocation(contract != null ? contract.getWorkLocation() : null)
            .age(employee.getAge())
            .tenureYears(employee.getTenureYears())
            .active(employee.isActive())
            .build();
    }

    private EmployeeDto.ContractResponse toContractResponse(EmploymentContract contract) {
        return EmployeeDto.ContractResponse.builder()
            .id(contract.getId())
            .uuid(contract.getUuid())
            .contractType(contract.getContractType())
            .workJourney(contract.getWorkJourney())
            .weeklyHours(contract.getWeeklyHours())
            .dailyHours(contract.getDailyHours())
            .salaryBase(contract.getSalaryBase())
            .salaryHourly(contract.getSalaryHourly())
            .positionTitle(contract.getPositionTitle())
            .positionCbo(contract.getPositionCbo())
            .departmentId(contract.getDepartmentId())
            .departmentName(null)
            .costCenterId(contract.getCostCenterId())
            .costCenterName(null)
            .reportingToId(contract.getReportingToId())
            .reportingToName(null)
            .workLocation(contract.getWorkLocation())
            .workCity(contract.getWorkCity())
            .workState(contract.getWorkState())
            .unionId(contract.getUnionId())
            .collectiveAgreementId(contract.getCollectiveAgreementId())
            .probationEndDate(contract.getProbationEndDate())
            .contractStartDate(contract.getContractStartDate())
            .contractEndDate(contract.getContractEndDate())
            .renewalCount(contract.getRenewalCount())
            .fgtsOptIn(contract.getFgtsOptIn())
            .fgtsAccount(contract.getFgtsAccount())
            .benefitTransportVoucher(contract.getBenefitTransportVoucher())
            .benefitMealVoucher(contract.getBenefitMealVoucher())
            .benefitHealthInsurance(contract.getBenefitHealthInsurance())
            .benefitDentalInsurance(contract.getBenefitDentalInsurance())
            .benefitLifeInsurance(contract.getBenefitLifeInsurance())
            .active(contract.isActive())
            .onProbation(contract.isOnProbation())
            .noticePeriodDays(contract.getNoticePeriodDays())
            .build();
    }
}