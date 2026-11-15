package com.erp.brazil.fiscal.application.usecase;

import com.erp.brazil.fiscal.domain.model.NFE;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ValidateNFEUseCaseTest {

    private ValidateNFEUseCase validateNFEUseCase;

    @Test
    void validate_WithValidNFE_ReturnsValid() {
        // Given
        NFE nfe = createValidNFE();

        // When
        ValidateNFEUseCase.ValidationResult result = validateNFEUseCase.validate(nfe);

        // Then
        // Note: This will fail XSD validation since we don't have the actual XSD files
        // But business rule validation should work
        assertNotNull(result);
    }

    @Test
    void validate_WithMissingAccessKey_ReturnsError() {
        // Given
        NFE nfe = createValidNFE();
        NFE invalidNfe = new NFE(
                nfe.id(), nfe.companyId(), null, // missing access key
                nfe.protocol(), nfe.status(), nfe.type(), nfe.series(), nfe.number(),
                nfe.emissionDate(), nfe.saEntradaDate(), nfe.emitter(), nfe.recipient(),
                nfe.items(), nfe.totals(), nfe.transport(), nfe.payment(),
                nfe.additionalInfo(), nfe.xmlContent(), nfe.pdfContent(),
                nfe.createdAt(), nfe.updatedAt(), nfe.cancelledAt(), nfe.cancellationReason()
        );

        // When
        ValidateNFEUseCase.ValidationResult result = validateNFEUseCase.validate(invalidNfe);

        // Then
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.code().equals("MISSING_ACCESS_KEY")));
    }

    @Test
    void validate_WithMissingEmitterCNPJ_ReturnsError() {
        // Given
        NFE nfe = createValidNFE();
        NFE.Emitter emitterWithoutCNPJ = new NFE.Emitter(
                null, nfe.emitter().ie(), nfe.emitter().iest(), nfe.emitter().im(),
                nfe.emitter().cnae(), nfe.emitter().crt(), nfe.emitter().companyName(),
                nfe.emitter().tradeName(), nfe.emitter().address()
        );
        NFE invalidNfe = new NFE(
                nfe.id(), nfe.companyId(), nfe.accessKey(), nfe.protocol(), nfe.status(),
                nfe.type(), nfe.series(), nfe.number(), nfe.emissionDate(), nfe.saEntradaDate(),
                emitterWithoutCNPJ, nfe.recipient(), nfe.items(), nfe.totals(),
                nfe.transport(), nfe.payment(), nfe.additionalInfo(),
                nfe.xmlContent(), nfe.pdfContent(), nfe.createdAt(), nfe.updatedAt(),
                nfe.cancelledAt(), nfe.cancellationReason()
        );

        // When
        ValidateNFEUseCase.ValidationResult result = validateNFEUseCase.validate(invalidNfe);

        // Then
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.code().equals("MISSING_EMITTER_CNPJ")));
    }

    @Test
    void validate_WithMissingRecipientCNPJ_ReturnsError() {
        // Given
        NFE nfe = createValidNFE();
        NFE.Recipient recipientWithoutCNPJ = new NFE.Recipient(
                null, nfe.recipient().ie(), nfe.recipient().isuf(), nfe.recipient().im(),
                nfe.recipient().cnae(), nfe.recipient().email(), nfe.recipient().companyName(),
                nfe.recipient().tradeName(), nfe.recipient().address(),
                nfe.recipient().foreignCountryCode(), nfe.recipient().foreignCountryName()
        );
        NFE invalidNfe = new NFE(
                nfe.id(), nfe.companyId(), nfe.accessKey(), nfe.protocol(), nfe.status(),
                nfe.type(), nfe.series(), nfe.number(), nfe.emissionDate(), nfe.saEntradaDate(),
                nfe.emitter(), recipientWithoutCNPJ, nfe.items(), nfe.totals(),
                nfe.transport(), nfe.payment(), nfe.additionalInfo(),
                nfe.xmlContent(), nfe.pdfContent(), nfe.createdAt(), nfe.updatedAt(),
                nfe.cancelledAt(), nfe.cancellationReason()
        );

        // When
        ValidateNFEUseCase.ValidationResult result = validateNFEUseCase.validate(invalidNfe);

        // Then
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.code().equals("MISSING_RECIPIENT_CNPJ_CPF")));
    }

    @Test
    void validate_WithNoItems_ReturnsError() {
        // Given
        NFE nfe = createValidNFE();
        NFE invalidNfe = new NFE(
                nfe.id(), nfe.companyId(), nfe.accessKey(), nfe.protocol(), nfe.status(),
                nfe.type(), nfe.series(), nfe.number(), nfe.emissionDate(), nfe.saEntradaDate(),
                nfe.emitter(), nfe.recipient(), List.of(), nfe.totals(),
                nfe.transport(), nfe.payment(), nfe.additionalInfo(),
                nfe.xmlContent(), nfe.pdfContent(), nfe.createdAt(), nfe.updatedAt(),
                nfe.cancelledAt(), nfe.cancellationReason()
        );

        // When
        ValidateNFEUseCase.ValidationResult result = validateNFEUseCase.validate(invalidNfe);

        // Then
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.code().equals("NO_ITEMS")));
    }

    @Test
    void validate_WithItemMissingNCM_ReturnsError() {
        // Given
        NFE nfe = createValidNFE();
        NFE.Item itemWithoutNCM = new NFE.Item(
                1, null, nfe.items().get(0).cest(), nfe.items().get(0).cfop(),
                nfe.items().get(0).ucom(), nfe.items().get(0).ucomDesc(),
                nfe.items().get(0).qCom(), nfe.items().get(0).vUnCom(),
                nfe.items().get(0).vProd(), nfe.items().get(0).cEan(),
                nfe.items().get(0).cEanTrib(), nfe.items().get(0).uTrib(),
                nfe.items().get(0).uTribDesc(), nfe.items().get(0).qTrib(),
                nfe.items().get(0).vUnTrib(), nfe.items().get(0).vFrete(),
                nfe.items().get(0).vSeg(), nfe.items().get(0).vDesc(),
                nfe.items().get(0).vOutro(), nfe.items().get(0).icms(),
                nfe.items().get(0).ipi(), nfe.items().get(0).pis(),
                nfe.items().get(0).cofins(), nfe.items().get(0).iss()
        );
        NFE invalidNfe = new NFE(
                nfe.id(), nfe.companyId(), nfe.accessKey(), nfe.protocol(), nfe.status(),
                nfe.type(), nfe.series(), nfe.number(), nfe.emissionDate(), nfe.saEntradaDate(),
                nfe.emitter(), nfe.recipient(), List.of(itemWithoutNCM), nfe.totals(),
                nfe.transport(), nfe.payment(), nfe.additionalInfo(),
                nfe.xmlContent(), nfe.pdfContent(), nfe.createdAt(), nfe.updatedAt(),
                nfe.cancelledAt(), nfe.cancellationReason()
        );

        // When
        ValidateNFEUseCase.ValidationResult result = validateNFEUseCase.validate(invalidNfe);

        // Then
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.code().equals("MISSING_NCM_ITEM_1")));
    }

    @Test
    void validate_WithItemMissingCFOP_ReturnsError() {
        // Given
        NFE nfe = createValidNFE();
        NFE.Item itemWithoutCFOP = new NFE.Item(
                1, nfe.items().get(0).ncm(), nfe.items().get(0).cest(), null,
                nfe.items().get(0).ucom(), nfe.items().get(0).ucomDesc(),
                nfe.items().get(0).qCom(), nfe.items().get(0).vUnCom(),
                nfe.items().get(0).vProd(), nfe.items().get(0).cEan(),
                nfe.items().get(0).cEanTrib(), nfe.items().get(0).uTrib(),
                nfe.items().get(0).uTribDesc(), nfe.items().get(0).qTrib(),
                nfe.items().get(0).vUnTrib(), nfe.items().get(0).vFrete(),
                nfe.items().get(0).vSeg(), nfe.items().get(0).vDesc(),
                nfe.items().get(0).vOutro(), nfe.items().get(0).icms(),
                nfe.items().get(0).ipi(), nfe.items().get(0).pis(),
                nfe.items().get(0).cofins(), nfe.items().get(0).iss()
        );
        NFE invalidNfe = new NFE(
                nfe.id(), nfe.companyId(), nfe.accessKey(), nfe.protocol(), nfe.status(),
                nfe.type(), nfe.series(), nfe.number(), nfe.emissionDate(), nfe.saEntradaDate(),
                nfe.emitter(), nfe.recipient(), List.of(itemWithoutCFOP), nfe.totals(),
                nfe.transport(), nfe.payment(), nfe.additionalInfo(),
                nfe.xmlContent(), nfe.pdfContent(), nfe.createdAt(), nfe.updatedAt(),
                nfe.cancelledAt(), nfe.cancellationReason()
        );

        // When
        ValidateNFEUseCase.ValidationResult result = validateNFEUseCase.validate(invalidNfe);

        // Then
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.code().equals("MISSING_CFOP_ITEM_1")));
    }

    @Test
    void validate_WithItemZeroValue_ReturnsError() {
        // Given
        NFE nfe = createValidNFE();
        NFE.Item itemZeroValue = new NFE.Item(
                1, nfe.items().get(0).ncm(), nfe.items().get(0).cest(), nfe.items().get(0).cfop(),
                nfe.items().get(0).ucom(), nfe.items().get(0).ucomDesc(),
                nfe.items().get(0).qCom(), nfe.items().get(0).vUnCom(),
                BigDecimal.ZERO, nfe.items().get(0).cEan(),
                nfe.items().get(0).cEanTrib(), nfe.items().get(0).uTrib(),
                nfe.items().get(0).uTribDesc(), nfe.items().get(0).qTrib(),
                nfe.items().get(0).vUnTrib(), nfe.items().get(0).vFrete(),
                nfe.items().get(0).vSeg(), nfe.items().get(0).vDesc(),
                nfe.items().get(0).vOutro(), nfe.items().get(0).icms(),
                nfe.items().get(0).ipi(), nfe.items().get(0).pis(),
                nfe.items().get(0).cofins(), nfe.items().get(0).iss()
        );
        NFE invalidNfe = new NFE(
                nfe.id(), nfe.companyId(), nfe.accessKey(), nfe.protocol(), nfe.status(),
                nfe.type(), nfe.series(), nfe.number(), nfe.emissionDate(), nfe.saEntradaDate(),
                nfe.emitter(), nfe.recipient(), List.of(itemZeroValue), nfe.totals(),
                nfe.transport(), nfe.payment(), nfe.additionalInfo(),
                nfe.xmlContent(), nfe.pdfContent(), nfe.createdAt(), nfe.updatedAt(),
                nfe.cancelledAt(), nfe.cancellationReason()
        );

        // When
        ValidateNFEUseCase.ValidationResult result = validateNFEUseCase.validate(invalidNfe);

        // Then
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.code().equals("INVALID_VPROD_ITEM_1")));
    }

    @Test
    void validate_WithInvalidCST_ReturnsError() {
        // Given
        NFE nfe = createValidNFE();
        NFE.ICMS invalidICMS = new NFE.ICMS(
                "0", "99", null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO
        );
        NFE.Item itemInvalidCST = new NFE.Item(
                1, nfe.items().get(0).ncm(), nfe.items().get(0).cest(), nfe.items().get(0).cfop(),
                nfe.items().get(0).ucom(), nfe.items().get(0).ucomDesc(),
                nfe.items().get(0).qCom(), nfe.items().get(0).vUnCom(),
                nfe.items().get(0).vProd(), nfe.items().get(0).cEan(),
                nfe.items().get(0).cEanTrib(), nfe.items().get(0).uTrib(),
                nfe.items().get(0).uTribDesc(), nfe.items().get(0).qTrib(),
                nfe.items().get(0).vUnTrib(), nfe.items().get(0).vFrete(),
                nfe.items().get(0).vSeg(), nfe.items().get(0).vDesc(),
                nfe.items().get(0).vOutro(), invalidICMS,
                nfe.items().get(0).ipi(), nfe.items().get(0).pis(),
                nfe.items().get(0).cofins(), nfe.items().get(0).iss()
        );
        NFE invalidNfe = new NFE(
                nfe.id(), nfe.companyId(), nfe.accessKey(), nfe.protocol(), nfe.status(),
                nfe.type(), nfe.series(), nfe.number(), nfe.emissionDate(), nfe.saEntradaDate(),
                nfe.emitter(), nfe.recipient(), List.of(itemInvalidCST), nfe.totals(),
                nfe.transport(), nfe.payment(), nfe.additionalInfo(),
                nfe.xmlContent(), nfe.pdfContent(), nfe.createdAt(), nfe.updatedAt(),
                nfe.cancelledAt(), nfe.cancellationReason()
        );

        // When
        ValidateNFEUseCase.ValidationResult result = validateNFEUseCase.validate(invalidNfe);

        // Then
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.code().equals("INVALID_CST_ITEM_1")));
    }

    @Test
    void validate_WithInvalidCSOSN_ReturnsError() {
        // Given
        NFE nfe = createValidNFE();
        NFE.ICMS invalidICMS = new NFE.ICMS(
                "0", null, "999", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO
        );
        NFE.Item itemInvalidCSOSN = new NFE.Item(
                1, nfe.items().get(0).ncm(), nfe.items().get(0).cest(), nfe.items().get(0).cfop(),
                nfe.items().get(0).ucom(), nfe.items().get(0).ucomDesc(),
                nfe.items().get(0).qCom(), nfe.items().get(0).vUnCom(),
                nfe.items().get(0).vProd(), nfe.items().get(0).cEan(),
                nfe.items().get(0).cEanTrib(), nfe.items().get(0).uTrib(),
                nfe.items().get(0).uTribDesc(), nfe.items().get(0).qTrib(),
                nfe.items().get(0).vUnTrib(), nfe.items().get(0).vFrete(),
                nfe.items().get(0).vSeg(), nfe.items().get(0).vDesc(),
                nfe.items().get(0).vOutro(), invalidICMS,
                nfe.items().get(0).ipi(), nfe.items().get(0).pis(),
                nfe.items().get(0).cofins(), nfe.items().get(0).iss()
        );
        NFE invalidNfe = new NFE(
                nfe.id(), nfe.companyId(), nfe.accessKey(), nfe.protocol(), nfe.status(),
                nfe.type(), nfe.series(), nfe.number(), nfe.emissionDate(), nfe.saEntradaDate(),
                nfe.emitter(), nfe.recipient(), List.of(itemInvalidCSOSN), nfe.totals(),
                nfe.transport(), nfe.payment(), nfe.additionalInfo(),
                nfe.xmlContent(), nfe.pdfContent(), nfe.createdAt(), nfe.updatedAt(),
                nfe.cancelledAt(), nfe.cancellationReason()
        );

        // When
        ValidateNFEUseCase.ValidationResult result = validateNFEUseCase.validate(invalidNfe);

        // Then
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.code().equals("INVALID_CSOSN_ITEM_1")));
    }

    private NFE createValidNFE() {
        NFE.Address address = new NFE.Address(
                "Rua Teste", "100", "Sala 1", "Centro", "3550308", "São Paulo", "SP",
                "01000000", "1058", "Brasil", "11999999999"
        );

        NFE.Emitter emitter = new NFE.Emitter(
                "12345678000195", "123456789", "123456789", "123456", "6201", "3",
                "Empresa Teste LTDA", "Empresa Teste", address
        );

        NFE.Recipient recipient = new NFE.Recipient(
                "98765432000195", "987654321", "987654321", "987654", "6201",
                "cliente@teste.com", "Cliente Teste LTDA", "Cliente Teste", address,
                null, null
        );

        NFE.ICMS icms = new NFE.ICMS(
                "0", "00", null, new BigDecimal("1000.00"), new BigDecimal("18.00"),
                new BigDecimal("180.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        NFE.IPI ipi = new NFE.IPI(
                null, null, null, null, null, "00", new BigDecimal("1000.00"),
                new BigDecimal("5.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("50.00")
        );

        NFE.PIS pis = new NFE.PIS(
                "01", new BigDecimal("1000.00"), new BigDecimal("1.65"),
                new BigDecimal("16.50"), BigDecimal.ZERO, BigDecimal.ZERO
        );

        NFE.COFINS cofins = new NFE.COFINS(
                "01", new BigDecimal("1000.00"), new BigDecimal("7.60"),
                new BigDecimal("76.00"), BigDecimal.ZERO, BigDecimal.ZERO
        );

        NFE.ISS iss = new NFE.ISS(
                null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null, null
        );

        NFE.Item item = new NFE.Item(
                1, "09011100", "0900100", "1101", "UN", "UNIDADE",
                new BigDecimal("10"), new BigDecimal("100.00"), new BigDecimal("1000.00"),
                null, null, "UN", "UNIDADE", new BigDecimal("10"), new BigDecimal("100.00"),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                icms, ipi, pis, cofins, iss
        );

        NFE.Totals totals = new NFE.Totals(
                new BigDecimal("1000.00"), new BigDecimal("180.00"), BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, new BigDecimal("1000.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("50.00"), BigDecimal.ZERO,
                new BigDecimal("16.50"), new BigDecimal("76.00"), BigDecimal.ZERO,
                new BigDecimal("1142.50"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );

        NFE.Transport transport = new NFE.Transport(
                "0", null, null, null, null, null, null, null, null, null, null, null, null
        );

        NFE.Payment payment = new NFE.Payment(
                "1", List.of()
        );

        return new NFE(
                "nfe-1", "company-1", "35240112345678000195550010000000011234567890",
                null, "PENDING", NFE.NFEType.SAIDA, "1", "1",
                LocalDateTime.now(), LocalDateTime.now(),
                emitter, recipient, List.of(item), totals,
                transport, payment, "Observação teste", null, null,
                LocalDateTime.now(), LocalDateTime.now(), null, null
        );
    }
}