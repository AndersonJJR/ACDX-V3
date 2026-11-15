package com.erp.brazil.fiscal.application.usecase;

import com.erp.brazil.fiscal.domain.model.*;
import com.erp.brazil.fiscal.domain.repository.TaxRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculateTaxesUseCaseTest {

    @Mock
    private TaxRuleRepository taxRuleRepository;

    private CalculateTaxesUseCase calculateTaxesUseCase;

    @BeforeEach
    void setUp() {
        calculateTaxesUseCase = new CalculateTaxesUseCase(taxRuleRepository, null);
    }

    @Test
    void calculate_WithValidContext_ReturnsTaxResult() {
        // Given
        TaxCalculationContext context = new TaxCalculationContext(
                "company-1", "SP", "SP", "09011100", "0900100", "1101",
                new BigDecimal("1000.00"), new BigDecimal("10"),
                TaxCalculationContext.OperationType.SALE,
                "LUCRO_REAL", "3550308", "6201"
        );

        ICMSRule icmsRule = mock(ICMSRule.class);
        when(icmsRule.calculate(any(), any())).thenReturn(new BigDecimal("180.00"));
        when(icmsRule.getTaxType()).thenReturn("ICMS");
        when(icmsRule.isSt()).thenReturn(false);
        when(icmsRule.isFcp()).thenReturn(false);
        when(icmsRule.getAliquota()).thenReturn(new BigDecimal("18.00"));
        when(icmsRule.getReducaoBc()).thenReturn(BigDecimal.ZERO);
        when(icmsRule.getCst()).thenReturn("00");
        when(icmsRule.getName()).thenReturn("ICMS SP-SP");

        IPIRule ipiRule = mock(IPIRule.class);
        when(ipiRule.calculate(any(), any())).thenReturn(new BigDecimal("50.00"));
        when(ipiRule.getTaxType()).thenReturn("IPI");
        when(ipiRule.getAliquota()).thenReturn(new BigDecimal("5.00"));
        when(ipiRule.getReducaoBc()).thenReturn(BigDecimal.ZERO);
        when(ipiRule.getCst()).thenReturn("00");
        when(ipiRule.getName()).thenReturn("IPI Café");

        PISCOFINSRule pisRule = mock(PISCOFINSRule.class);
        when(pisRule.calculate(any(), any())).thenReturn(new BigDecimal("16.50"));
        when(pisRule.getTaxType()).thenReturn("PIS");
        when(pisRule.isPis()).thenReturn(true);
        when(pisRule.getAliquota()).thenReturn(new BigDecimal("1.65"));
        when(pisRule.getReducaoBc()).thenReturn(BigDecimal.ZERO);
        when(pisRule.getCst()).thenReturn("01");
        when(pisRule.getName()).thenReturn("PIS CST 01");

        PISCOFINSRule cofinsRule = mock(PISCOFINSRule.class);
        when(cofinsRule.calculate(any(), any())).thenReturn(new BigDecimal("76.00"));
        when(cofinsRule.getTaxType()).thenReturn("COFINS");
        when(cofinsRule.isPis()).thenReturn(false);
        when(cofinsRule.getAliquota()).thenReturn(new BigDecimal("7.60"));
        when(cofinsRule.getReducaoBc()).thenReturn(BigDecimal.ZERO);
        when(cofinsRule.getCst()).thenReturn("01");
        when(cofinsRule.getName()).thenReturn("COFINS CST 01");

        when(taxRuleRepository.findApplicableRules(
                eq("ICMS"), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(icmsRule));
        when(taxRuleRepository.findApplicableRules(
                eq("IPI"), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(ipiRule));
        when(taxRuleRepository.findApplicableRules(
                eq("PIS"), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(pisRule));
        when(taxRuleRepository.findApplicableRules(
                eq("COFINS"), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(cofinsRule));
        when(taxRuleRepository.findApplicableRules(
                eq("ISS"), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        // When
        TaxCalculationResult result = calculateTaxesUseCase.execute(context);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("180.00"), result.icms());
        assertEquals(new BigDecimal("50.00"), result.ipi());
        assertEquals(new BigDecimal("16.50"), result.pis());
        assertEquals(new BigDecimal("76.00"), result.cofins());
        assertEquals(BigDecimal.ZERO, result.iss());
        assertEquals(new BigDecimal("322.50"), result.total());
        assertEquals(4, result.baseCalculations().size());
    }

    @Test
    void calculate_WithSimplesNacional_ReturnsSimplesTaxes() {
        // Given
        TaxCalculationContext context = new TaxCalculationContext(
                "company-1", "SP", "SP", "09011100", "0900100", "1101",
                new BigDecimal("1000.00"), new BigDecimal("10"),
                TaxCalculationContext.OperationType.SALE,
                "SIMPLES_NACIONAL", "3550308", "6201"
        );

        ICMSRule icmsRule = mock(ICMSRule.class);
        when(icmsRule.calculate(any(), any())).thenReturn(new BigDecimal("40.00")); // 4% Simples
        when(icmsRule.getTaxType()).thenReturn("ICMS");
        when(icmsRule.isSt()).thenReturn(false);
        when(icmsRule.isFcp()).thenReturn(false);
        when(icmsRule.getAliquota()).thenReturn(new BigDecimal("4.00"));
        when(icmsRule.getReducaoBc()).thenReturn(BigDecimal.ZERO);
        when(icmsRule.getCsosn()).thenReturn("101");
        when(icmsRule.getName()).thenReturn("ICMS Simples");

        when(taxRuleRepository.findApplicableRules(
                eq("ICMS"), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(icmsRule));
        when(taxRuleRepository.findApplicableRules(
                eq("IPI"), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(taxRuleRepository.findApplicableRules(
                eq("PIS"), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(taxRuleRepository.findApplicableRules(
                eq("COFINS"), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(taxRuleRepository.findApplicableRules(
                eq("ISS"), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        // When
        TaxCalculationResult result = calculateTaxesUseCase.execute(context);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("40.00"), result.icms());
        assertEquals(BigDecimal.ZERO, result.ipi());
        assertEquals(BigDecimal.ZERO, result.pis());
        assertEquals(BigDecimal.ZERO, result.cofins());
        assertEquals(new BigDecimal("40.00"), result.total());
    }

    @Test
    void calculate_WithISS_ReturnsISSValue() {
        // Given
        TaxCalculationContext context = new TaxCalculationContext(
                "company-1", "SP", "SP", null, null, null,
                new BigDecimal("1000.00"), new BigDecimal("1"),
                TaxCalculationContext.OperationType.SERVICE,
                "LUCRO_REAL", "3550308", "6201"
        );

        ISSRule issRule = mock(ISSRule.class);
        when(issRule.calculate(any(), any())).thenReturn(new BigDecimal("20.00"));
        when(issRule.getTaxType()).thenReturn("ISS");
        when(issRule.getAliquota()).thenReturn(new BigDecimal("2.00"));
        when(issRule.getReducaoBc()).thenReturn(BigDecimal.ZERO);
        when(issRule.getCst()).thenReturn("01");
        when(issRule.getName()).thenReturn("ISS SP Serviços");

        when(taxRuleRepository.findApplicableRules(
                eq("ICMS"), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(taxRuleRepository.findApplicableRules(
                eq("IPI"), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(taxRuleRepository.findApplicableRules(
                eq("PIS"), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(taxRuleRepository.findApplicableRules(
                eq("COFINS"), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(taxRuleRepository.findApplicableRules(
                eq("ISS"), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(issRule));

        // When
        TaxCalculationResult result = calculateTaxesUseCase.execute(context);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.icms());
        assertEquals(BigDecimal.ZERO, result.ipi());
        assertEquals(BigDecimal.ZERO, result.pis());
        assertEquals(BigDecimal.ZERO, result.cofins());
        assertEquals(new BigDecimal("20.00"), result.iss());
        assertEquals(new BigDecimal("20.00"), result.total());
    }

    @Test
    void calculate_WithEmptyRules_ReturnsZeroTaxes() {
        // Given
        TaxCalculationContext context = new TaxCalculationContext(
                "company-1", "SP", "SP", "99999999", "9999999", "9999",
                new BigDecimal("1000.00"), new BigDecimal("10"),
                TaxCalculationContext.OperationType.SALE,
                "LUCRO_REAL", "3550308", "6201"
        );

        when(taxRuleRepository.findApplicableRules(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        // When
        TaxCalculationResult result = calculateTaxesUseCase.execute(context);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.total());
        assertTrue(result.baseCalculations().isEmpty());
    }

    @Test
    void calculate_WithST_ReturnsSTValue() {
        // Given
        TaxCalculationContext context = new TaxCalculationContext(
                "company-1", "SP", "RJ", "09011100", "0900100", "1101",
                new BigDecimal("1000.00"), new BigDecimal("10"),
                TaxCalculationContext.OperationType.SALE,
                "LUCRO_REAL", "3550308", "6201"
        );

        ICMSRule icmsRule = mock(ICMSRule.class);
        when(icmsRule.calculate(any(), any())).thenReturn(new BigDecimal("300.00")); // Includes ST
        when(icmsRule.getTaxType()).thenReturn("ICMS");
        when(icmsRule.isSt()).thenReturn(true);
        when(icmsRule.isFcp()).thenReturn(true);
        when(icmsRule.getAliquota()).thenReturn(new BigDecimal("12.00"));
        when(icmsRule.getAliquotaSt()).thenReturn(new BigDecimal("18.00"));
        when(icmsRule.getAliquotaFcp()).thenReturn(new BigDecimal("2.00"));
        when(icmsRule.getReducaoBc()).thenReturn(BigDecimal.ZERO);
        when(icmsRule.getReducaoBcSt()).thenReturn(BigDecimal.ZERO);
        when(icmsRule.getMvaAjustado()).thenReturn(new BigDecimal("40.00"));
        when(icmsRule.getCst()).thenReturn("00");
        when(icmsRule.getName()).thenReturn("ICMS SP-RJ ST");

        when(taxRuleRepository.findApplicableRules(
                eq("ICMS"), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(icmsRule));
        when(taxRuleRepository.findApplicableRules(
                eq("IPI"), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(taxRuleRepository.findApplicableRules(
                eq("PIS"), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(taxRuleRepository.findApplicableRules(
                eq("COFINS"), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(taxRuleRepository.findApplicableRules(
                eq("ISS"), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        // When
        TaxCalculationResult result = calculateTaxesUseCase.execute(context);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("300.00"), result.icms());
        // ST and FCP are included in the ICMS calculation
        assertTrue(result.baseCalculations().stream()
                .anyMatch(bc -> bc.taxType().equals("ICMS")));
    }
}