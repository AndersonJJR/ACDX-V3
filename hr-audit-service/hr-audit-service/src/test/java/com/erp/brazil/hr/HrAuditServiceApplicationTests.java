package com.erp.brazil.hr;

import com.erp.brazil.hr.application.usecase.CalculateFGTSUseCase;
import com.erp.brazil.hr.application.usecase.CalculateINSSUseCase;
import com.erp.brazil.hr.application.usecase.CalculateIRRFUseCase;
import com.erp.brazil.hr.domain.model.Employee;
import com.erp.brazil.hr.infrastructure.aop.DataMaskingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class HrAuditServiceApplicationTests {

    @Autowired
    private CalculateINSSUseCase calculateINSSUseCase;

    @Autowired
    private CalculateIRRFUseCase calculateIRRFUseCase;

    @Autowired
    private CalculateFGTSUseCase calculateFGTSUseCase;

    @Autowired
    private DataMaskingService dataMaskingService;

    @Test
    void contextLoads() {
        // Basic context load test
    }

    @Test
    void testINSSCalculation() {
        // Test 2024 INSS brackets
        // Up to 1,412.00: 7.5%
        // 1,412.01 to 2,666.68: 9%
        // 2,666.69 to 4,000.03: 12%
        // 4,000.04 to 7,786.02: 14%
        // Ceiling: 908.86

        // Test bracket 1
        BigDecimal inss1 = calculateINSSUseCase.calculate(new BigDecimal("1412.00"));
        assertEquals(0, new BigDecimal("105.90").compareTo(inss1));

        // Test bracket 2
        BigDecimal inss2 = calculateINSSUseCase.calculate(new BigDecimal("2666.68"));
        assertEquals(0, new BigDecimal("218.40").compareTo(inss2));

        // Test bracket 3
        BigDecimal inss3 = calculateINSSUseCase.calculate(new BigDecimal("4000.03"));
        assertEquals(0, new BigDecimal("378.69").compareTo(inss3));

        // Test ceiling
        BigDecimal inssCeiling = calculateINSSUseCase.calculate(new BigDecimal("10000.00"));
        assertEquals(0, new BigDecimal("908.86").compareTo(inssCeiling));

        // Test employer INSS (20%)
        BigDecimal employerInss = calculateINSSUseCase.calculateEmployer(new BigDecimal("5000.00"));
        assertEquals(0, new BigDecimal("1000.00").compareTo(employerInss));
    }

    @Test
    void testIRRFCalculation() {
        // Test 2024 IRRF brackets
        // Up to 2,259.20: exempt
        // 2,259.21 to 2,826.65: 7.5% - 169.44
        // 2,826.66 to 3,751.05: 15% - 381.44
        // 3,751.06 to 4,664.68: 22.5% - 662.77
        // Above 4,664.68: 27.5% - 896.00
        // Dependent deduction: 189.59

        // Test exempt
        BigDecimal irrf1 = calculateIRRFUseCase.calculate(new BigDecimal("2000.00"), 0);
        assertEquals(0, BigDecimal.ZERO.compareTo(irrf1));

        // Test with dependents
        BigDecimal irrf2 = calculateIRRFUseCase.calculate(new BigDecimal("5000.00"), 2);
        // Should be calculated with dependent deduction
        assertTrue(irrf2.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testFGTSModel() {
        // Test FGTS 8%
        BigDecimal fgts = calculateFGTSUseCase.calculateMonthlyDeposit(new BigDecimal("5000.00"));
        assertEquals(0, new BigDecimal("400.00").compareTo(fgts));

        // Test resignation fine (40%)
        BigDecimal fine = calculateFGTSUseCase.calculateResignationFine(new BigDecimal("5000.00"), 12);
        assertEquals(0, new BigDecimal("1920.00").compareTo(fine));
    }

    @Test
    void testDataMasking() {
        Map<String, Object> data = new HashMap<>();
        data.put("cpf", "123.456.789-00");
        data.put("email", "joao.silva@empresa.com.br");
        data.put("phone", "(11) 99999-8888");
        data.put("bankAccount", "12345-6");
        data.put("pixKey", "123.456.789-00");
        data.put("address", "Rua das Flores, 123, São Paulo, SP");
        data.put("motherName", "Maria Silva");
        data.put("fatherName", "José Silva");
        data.put("name", "João Silva");

        Map<String, Object> masked = dataMaskingService.maskSensitiveData(data);

        // Verify masking
        assertTrue(masked.get("cpf").toString().contains("***"));
        assertTrue(masked.get("email").toString().contains("***"));
        assertTrue(masked.get("phone").toString().startsWith("***"));
        assertTrue(masked.get("bankAccount").toString().endsWith("45-6"));
        assertTrue(masked.get("pixKey").toString().contains("***"));
        assertTrue(masked.get("address").toString().contains("..."));
        assertTrue(masked.get("motherName").toString().contains("***"));
        assertTrue(masked.get("fatherName").toString().contains("***"));

        // Non-sensitive fields should remain
        assertEquals("João Silva", masked.get("name"));
    }

    @Test
    void testEmployeeModel() {
        Employee employee = Employee.builder()
                .companyId(1L)
                .employeeNumber("EMP001")
                .fullName("João Silva")
                .cpf("123.456.789-00")
                .birthDate(java.time.LocalDate.of(1990, 1, 15))
                .gender(Employee.Gender.M)
                .maritalStatus(Employee.MaritalStatus.SINGLE)
                .email("joao@empresa.com.br")
                .hireDate(java.time.LocalDate.of(2020, 1, 1))
                .status(Employee.EmployeeStatus.ACTIVE)
                .build();

        assertEquals("123.456.789-00", employee.getCpf());
        assertEquals("***.***.***-**", employee.getMaskedCpf());
        assertTrue(employee.getMaskedEmail().contains("***"));
        assertEquals(34, employee.getAge()); // As of 2024
        assertTrue(employee.isActive());
    }

    @Test
    void testEmployeeStatusTransitions() {
        Employee active = Employee.builder()
                .status(Employee.EmployeeStatus.ACTIVE)
                .build();
        assertTrue(active.isActive());

        Employee terminated = Employee.builder()
                .status(Employee.EmployeeStatus.TERMINATED)
                .build();
        assertFalse(terminated.isActive());

        Employee onLeave = Employee.builder()
                .status(Employee.EmployeeStatus.ON_LEAVE)
                .build();
        assertFalse(onLeave.isActive());
    }
}