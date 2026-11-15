package com.erp.brazil.hr.presentation.rest;

import com.erp.brazil.hr.application.dto.EmployeeDTO;
import com.erp.brazil.hr.application.usecase.ProcessWebhookUseCase;
import com.erp.brazil.hr.domain.model.Employee;
import com.erp.brazil.hr.domain.repository.EmployeeRepository;
import com.erp.brazil.hr.domain.repository.EmploymentContractRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employees", description = "Employee management API")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final EmploymentContractRepository contractRepository;
    private final ProcessWebhookUseCase webhookUseCase;

    public EmployeeController(EmployeeRepository employeeRepository,
                               EmploymentContractRepository contractRepository,
                               ProcessWebhookUseCase webhookUseCase) {
        this.employeeRepository = employeeRepository;
        this.contractRepository = contractRepository;
        this.webhookUseCase = webhookUseCase;
    }

    @PostMapping
    @Operation(summary = "Create a new employee")
    public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeDTO dto) {
        Employee employee = dto.toDomain();
        Employee saved = employeeRepository.save(employee);
        webhookUseCase.triggerEvent("EMPLOYEE_HIRED", Map.of("employee", saved), saved.companyId());
        return ResponseEntity.status(HttpStatus.CREATED).body(EmployeeDTO.from(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by UUID")
    public ResponseEntity<EmployeeDTO> getById(@PathVariable UUID id) {
        return employeeRepository.findByUuid(id)
            .map(EmployeeDTO::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List employees with pagination")
    public ResponseEntity<Page<EmployeeDTO>> list(
            @Parameter(description = "Company ID") @RequestParam Long companyId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<EmployeeDTO> page = employeeRepository.findByCompanyId(companyId, pageable)
            .map(EmployeeDTO::from);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/search")
    @Operation(summary = "Search employees by name, CPF, email, or employee number")
    public ResponseEntity<List<EmployeeDTO>> search(
            @Parameter(description = "Company ID") @RequestParam Long companyId,
            @Parameter(description = "Search term") @RequestParam String term) {
        List<EmployeeDTO> results = employeeRepository.search(companyId, term)
            .stream().map(EmployeeDTO::from).toList();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}/contracts")
    @Operation(summary = "Get employee's employment contracts")
    public ResponseEntity<List<EmploymentContractDTO>> getContracts(@PathVariable UUID id) {
        Employee employee = employeeRepository.findByUuid(id)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));
        List<EmploymentContractDTO> contracts = contractRepository.findByEmployeeId(employee.id())
            .stream().map(EmploymentContractDTO::from).toList();
        return ResponseEntity.ok(contracts);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update employee")
    public ResponseEntity<EmployeeDTO> update(@PathVariable UUID id, @Valid @RequestBody EmployeeDTO dto) {
        Employee existing = employeeRepository.findByUuid(id)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));

        Employee updated = new Employee(
            existing.id(), existing.uuid(), dto.companyId(), dto.personId(), dto.employeeNumber(),
            dto.hireDate(), dto.terminationDate(), dto.status(), dto.contractType(),
            dto.fullName(), dto.cpf(), dto.email(), dto.birthDate()
        );

        Employee saved = employeeRepository.save(updated);
        webhookUseCase.triggerEvent("EMPLOYEE_UPDATED", Map.of("employee", saved), saved.companyId());
        return ResponseEntity.ok(EmployeeDTO.from(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Terminate employee (soft delete)")
    public ResponseEntity<Void> terminate(@PathVariable UUID id,
                                           @RequestParam String reason,
                                           @RequestParam(required = false) java.time.LocalDate terminationDate) {
        Employee employee = employeeRepository.findByUuid(id)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));

        Employee terminated = new Employee(
            employee.id(), employee.uuid(), employee.companyId(), employee.personId(),
            employee.employeeNumber(), employee.hireDate(),
            terminationDate != null ? terminationDate : java.time.LocalDate.now(),
            Employee.EmployeeStatus.TERMINATED, employee.contractType(),
            employee.fullName(), employee.cpf(), employee.email(), employee.birthDate()
        );

        employeeRepository.save(terminated);
        webhookUseCase.triggerEvent("EMPLOYEE_TERMINATED", Map.of("employee", terminated), terminated.companyId());
        return ResponseEntity.noContent().build();
    }
}