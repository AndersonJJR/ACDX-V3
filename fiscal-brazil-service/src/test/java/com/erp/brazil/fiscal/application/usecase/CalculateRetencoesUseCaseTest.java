package com.erp.brazil.fiscal.application.usecase;

import com.erp.brazil.fiscal.application.dto.RetencaoDTO;
import com.erp.brazil.fiscal.domain.model.RetencaoFonte;
import com.erp.brazil.fiscal.domain.repository.RetencaoFonteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculateRetencoesUseCaseTest {

    @Mock
    private RetencaoFonteRepository retencaoRepository;

    private CalculateRetencoesUseCase calculateRetencoesUseCase;

    @BeforeEach
    void setUp() {
        calculateRetencoesUseCase = new CalculateRetencoesUseCase(retencaoRepository, null);
    }

    @Test
    void calculate_WithNormalService_ReturnsAllRetencoes() {
        // Given
        RetencaoDTO.ServiceInvoice invoice = new RetencaoDTO.ServiceInvoice(
                "doc-1", "NFS-e", LocalDate.now(),
                "12345678000195", "Fornecedor LTDA",
                "3550308", "SP", "0101", "6201",
                new BigDecimal("10000.00"), BigDecimal.ZERO,
                "LUCRO_REAL", false,
                Map.of()
        );

        RetencaoDTO.CalculateRequest request = new RetencaoDTO.CalculateRequest(
                invoice, List.of()
        );

        // When
        RetencaoDTO.CalculateResponse response = calculateRetencoesUseCase.execute(request);

        // Then
        assertNotNull(response);
        assertEquals(new BigDecimal("10000.00"), response.baseValue());
        assertEquals(5, response.retencoes().size()); // IRRF, CSLL, PIS, COFINS, INSS

        BigDecimal totalExpected = new BigDecimal("150.00")  // IRRF 1.5%
                .add(new BigDecimal("100.00"))  // CSLL 1%
                .add(new BigDecimal("65.00"))   // PIS 0.65%
                .add(new BigDecimal("300.00"))  // COFINS 3%
                .add(new BigDecimal("1100.00")); // INSS 11%

        assertEquals(totalExpected, response.totalRetido());
        assertEquals(new BigDecimal("10000.00").subtract(totalExpected), response.netValue());
    }

    @Test
    void calculate_WithSimplesNacional_ReturnsOnlyIRRFAndINSS() {
        // Given
        RetencaoDTO.ServiceInvoice invoice = new RetencaoDTO.ServiceInvoice(
                "doc-1", "NFS-e", LocalDate.now(),
                "12345678000195", "Fornecedor MEI",
                "3550308", "SP", "0101", "6201",
                new BigDecimal("10000.00"), BigDecimal.ZERO,
                "SIMPLES_NACIONAL", true,
                Map.of()
        );

        RetencaoDTO.CalculateRequest request = new RetencaoDTO.CalculateRequest(
                invoice, List.of()
        );

        // When
        RetencaoDTO.CalculateResponse response = calculateRetencoesUseCase.execute(request);

        // Then
        assertNotNull(response);
        assertEquals(2, response.retencoes().size()); // Only IRRF and INSS for Simples

        // IRRF should be calculated (1.5%)
        assertTrue(response.retencoes().stream()
                .anyMatch(r -> r.type().equals("IRRF") && r.calculatedValue().compareTo(BigDecimal.ZERO) > 0));

        // INSS should be calculated for service 0101 (cessão de mão de obra)
        assertTrue(response.retencoes().stream()
                .anyMatch(r -> r.type().equals("INSS") && r.calculatedValue().compareTo(BigDecimal.ZERO) > 0));

        // CSLL, PIS, COFINS should be zero for Simples
        assertTrue(response.retencoes().stream()
                .filter(r -> List.of("CSLL", "PIS", "COFINS").contains(r.type()))
                .allMatch(r -> r.calculatedValue().compareTo(BigDecimal.ZERO) == 0));
    }

    @Test
    void calculate_WithMEI_ReturnsOnlyINSS() {
        // Given
        RetencaoDTO.ServiceInvoice invoice = new RetencaoDTO.ServiceInvoice(
                "doc-1", "NFS-e", LocalDate.now(),
                "12345678000195", "Fornecedor MEI",
                "3550308", "SP", "0101", "6201",
                new BigDecimal("10000.00"), BigDecimal.ZERO,
                "MEI", true,
                Map.of()
        );

        RetencaoDTO.CalculateRequest request = new RetencaoDTO.CalculateRequest(
                invoice, List.of()
        );

        // When
        RetencaoDTO.CalculateResponse response = calculateRetencoesUseCase.execute(request);

        // Then
        // MEI is exempt from IRRF, CSLL, PIS, COFINS
        // But INSS is still due for cessão de mão de obra
        assertEquals(1, response.retencoes().size());
        assertEquals("INSS", response.retencoes().get(0).type());
        assertTrue(response.retencoes().get(0).calculatedValue().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void calculate_WithIsentoService_ReturnsZeroIRRF() {
        // Given - Service code 0101 is isento de IRRF
        RetencaoDTO.ServiceInvoice invoice = new RetencaoDTO.ServiceInvoice(
                "doc-1", "NFS-e", LocalDate.now(),
                "12345678000195", "Fornecedor LTDA",
                "3550308", "SP", "0101", "6201",
                new BigDecimal("10000.00"), BigDecimal.ZERO,
                "LUCRO_REAL", false,
                Map.of()
        );

        RetencaoDTO.CalculateRequest request = new RetencaoDTO.CalculateRequest(
                invoice, List.of()
        );

        // When
        RetencaoDTO.CalculateResponse response = calculateRetencoesUseCase.execute(request);

        // Then
        RetencaoDTO.RetencaoItem irrfItem = response.retencoes().stream()
                .filter(r -> r.type().equals("IRRF"))
                .findFirst()
                .orElseThrow();

        // Service 0101 is in SERVICO_ISENTO_IRRF
        assertEquals(BigDecimal.ZERO, irrfItem.calculatedValue());
        assertTrue(irrfItem.description().contains("isento"));
    }

    @Test
    void calculate_WithDeductions_ReducesBaseValue() {
        // Given
        RetencaoDTO.ServiceInvoice invoice = new RetencaoDTO.ServiceInvoice(
                "doc-1", "NFS-e", LocalDate.now(),
                "12345678000195", "Fornecedor LTDA",
                "3550308", "SP", "0101", "6201",
                new BigDecimal("10000.00"), new BigDecimal("2000.00"), // 2000 deductions
                "LUCRO_REAL", false,
                Map.of()
        );

        RetencaoDTO.CalculateRequest request = new RetencaoDTO.CalculateRequest(
                invoice, List.of()
        );

        // When
        RetencaoDTO.CalculateResponse response = calculateRetencoesUseCase.execute(request);

        // Then
        assertEquals(new BigDecimal("8000.00"), response.baseValue()); // 10000 - 2000

        // IRRF should be calculated on 8000 (1.5% = 120)
        RetencaoDTO.RetencaoItem irrfItem = response.retencoes().stream()
                .filter(r -> r.type().equals("IRRF"))
                .findFirst()
                .orElseThrow();

        assertEquals(new BigDecimal("120.00"), irrfItem.calculatedValue());
    }

    @Test
    void calculate_WithINSSTeto_AppliesTeto() {
        // Given - Very high value to test INSS teto
        RetencaoDTO.ServiceInvoice invoice = new RetencaoDTO.ServiceInvoice(
                "doc-1", "NFS-e", LocalDate.now(),
                "12345678000195", "Fornecedor LTDA",
                "3550308", "SP", "0701", "6201", // 0701 = cessão de mão de obra
                new BigDecimal("100000.00"), BigDecimal.ZERO,
                "LUCRO_REAL", false,
                Map.of()
        );

        RetencaoDTO.CalculateRequest request = new RetencaoDTO.CalculateRequest(
                invoice, List.of()
        );

        // When
        RetencaoDTO.CalculateResponse response = calculateRetencoesUseCase.execute(request);

        // Then
        RetencaoDTO.RetencaoItem inssItem = response.retencoes().stream()
                .filter(r -> r.type().equals("INSS"))
                .findFirst()
                .orElseThrow();

        // Teto INSS 2024 = 7507.49, 11% = 825.82
        assertEquals(new BigDecimal("825.82"), inssItem.calculatedValue());
        assertEquals(new BigDecimal("7507.49"), inssItem.baseValue()); // Base limited to teto
    }

    @Test
    void calculate_PersistsRetencoesToRepository() {
        // Given
        RetencaoDTO.ServiceInvoice invoice = new RetencaoDTO.ServiceInvoice(
                "doc-1", "NFS-e", LocalDate.now(),
                "12345678000195", "Fornecedor LTDA",
                "3550308", "SP", "0101", "6201",
                new BigDecimal("10000.00"), BigDecimal.ZERO,
                "LUCRO_REAL", false,
                Map.of()
        );

        RetencaoDTO.CalculateRequest request = new RetencaoDTO.CalculateRequest(
                invoice, List.of()
        );

        ArgumentCaptor<RetencaoFonte> captor = ArgumentCaptor.forClass(RetencaoFonte.class);

        // When
        calculateRetencoesUseCase.execute(request);

        // Then
        verify(retencaoRepository, times(5)).save(captor.capture());

        List<RetencaoFonte> saved = captor.getAllValues();
        assertEquals(5, saved.size());

        // Verify types
        long irrfCount = saved.stream().filter(r -> r instanceof RetencaoFonte.IRRF).count();
        long csllCount = saved.stream().filter(r -> r instanceof RetencaoFonte.CSLL).count();
        long pisCount = saved.stream().filter(r -> r instanceof RetencaoFonte.PIS).count();
        long cofinsCount = saved.stream().filter(r -> r instanceof RetencaoFonte.COFINS).count();
        long inssCount = saved.stream().filter(r -> r instanceof RetencaoFonte.INSS).count();

        assertEquals(1, irrfCount);
        assertEquals(1, csllCount);
        assertEquals(1, pisCount);
        assertEquals(1, cofinsCount);
        assertEquals(1, inssCount);
    }

    @Test
    void calculate_WithSpecificTypes_OnlyCalculatesRequestedTypes() {
        // Given
        RetencaoDTO.ServiceInvoice invoice = new RetencaoDTO.ServiceInvoice(
                "doc-1", "NFS-e", LocalDate.now(),
                "12345678000195", "Fornecedor LTDA",
                "3550308", "SP", "0101", "6201",
                new BigDecimal("10000.00"), BigDecimal.ZERO,
                "LUCRO_REAL", false,
                Map.of()
        );

        RetencaoDTO.CalculateRequest request = new RetencaoDTO.CalculateRequest(
                invoice,
                List.of(RetencaoDTO.CalculateRequest.RetencaoType.IRRF, RetencaoDTO.CalculateRequest.RetencaoType.CSLL)
        );

        // When
        RetencaoDTO.CalculateResponse response = calculateRetencoesUseCase.execute(request);

        // Then
        assertEquals(2, response.retencoes().size());
        assertTrue(response.retencoes().stream().allMatch(
                r -> List.of("IRRF", "CSLL").contains(r.type())));
    }
}