package com.erp.brazil.fiscal.infrastructure.sped;

import com.erp.brazil.fiscal.domain.model.NFE;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SPEDWriterTest {

    private SPEDWriter spedWriter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        spedWriter = new SPEDWriter();
    }

    @Test
    void writeEFD_ICMS_IPI_CreatesFile() throws IOException {
        // Given
        List<NFE> nfes = List.of(createSampleNFE());
        String companyId = "company-1";
        String spedId = "sped-1";
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        // When
        String filePath = spedWriter.writeEFD_ICMS_IPI(spedId, companyId, nfes, startDate, endDate, tempDir.toString());

        // Then
        assertNotNull(filePath);
        Path path = Path.of(filePath);
        assertTrue(path.toFile().exists());
        assertTrue(path.toFile().length() > 0);

        // Verify content
        String content = java.nio.file.Files.readString(path, java.nio.charset.StandardCharsets.ISO_8859_1);
        assertTrue(content.contains("|0000|")); // Header
        assertTrue(content.contains("|0150|")); // Participantes
        assertTrue(content.contains("|0200|")); // Itens
        assertTrue(content.contains("|C100|")); // NF-e
        assertTrue(content.contains("|C170|")); // Itens NF-e
        assertTrue(content.contains("|9999|")); // Trailer
    }

    @Test
    void writeECD_CreatesFile() throws IOException {
        // Given
        List<NFE> nfes = List.of(createSampleNFE());
        String companyId = "company-1";
        String spedId = "sped-1";
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        // When
        String filePath = spedWriter.writeECD(spedId, companyId, nfes, startDate, endDate, tempDir.toString());

        // Then
        assertNotNull(filePath);
        Path path = Path.of(filePath);
        assertTrue(path.toFile().exists());

        String content = java.nio.file.Files.readString(path, java.nio.charset.StandardCharsets.ISO_8859_1);
        assertTrue(content.contains("|0000|"));
        assertTrue(content.contains("|9999|"));
    }

    @Test
    void writeECF_CreatesFile() throws IOException {
        // Given
        List<NFE> nfes = List.of(createSampleNFE());
        String companyId = "company-1";
        String spedId = "sped-1";
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        // When
        String filePath = spedWriter.writeECF(spedId, companyId, nfes, startDate, endDate, tempDir.toString());

        // Then
        assertNotNull(filePath);
        Path path = Path.of(filePath);
        assertTrue(path.toFile().exists());

        String content = java.nio.file.Files.readString(path, java.nio.charset.StandardCharsets.ISO_8859_1);
        assertTrue(content.contains("|0000|"));
        assertTrue(content.contains("|9999|"));
    }

    @Test
    void writeEFD_Contribuicoes_CreatesFile() throws IOException {
        // Given
        List<NFE> nfes = List.of(createSampleNFE());
        String companyId = "company-1";
        String spedId = "sped-1";
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        // When
        String filePath = spedWriter.writeEFD_Contribuicoes(spedId, companyId, nfes, startDate, endDate, tempDir.toString());

        // Then
        assertNotNull(filePath);
        Path path = Path.of(filePath);
        assertTrue(path.toFile().exists());

        String content = java.nio.file.Files.readString(path, java.nio.charset.StandardCharsets.ISO_8859_1);
        assertTrue(content.contains("|0000|"));
        assertTrue(content.contains("|9999|"));
    }

    @Test
    void writeEFD_ICMS_IPI_WithMultipleNFEs_WritesAllRecords() throws IOException {
        // Given
        List<NFE> nfes = List.of(
                createSampleNFE("1", "1", "1000.00"),
                createSampleNFE("1", "2", "2000.00"),
                createSampleNFE("1", "3", "3000.00")
        );
        String companyId = "company-1";
        String spedId = "sped-1";
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        // When
        String filePath = spedWriter.writeEFD_ICMS_IPI(spedId, companyId, nfes, startDate, endDate, tempDir.toString());

        // Then
        String content = java.nio.file.Files.readString(Path.of(filePath), java.nio.charset.StandardCharsets.ISO_8859_1);

        // Should have 3 C100 records (one per NF-e)
        long c100Count = content.lines().filter(l -> l.startsWith("|C100|")).count();
        assertEquals(3, c100Count);

        // Should have 3 C170 records (one per NF-e, assuming 1 item each)
        long c170Count = content.lines().filter(l -> l.startsWith("|C170|")).count();
        assertEquals(3, c170Count);
    }

    @Test
    void writeEFD_ICMS_IPI_FormatsValuesCorrectly() throws IOException {
        // Given
        List<NFE> nfes = List.of(createSampleNFE());
        String companyId = "company-1";
        String spedId = "sped-1";
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        // When
        String filePath = spedWriter.writeEFD_ICMS_IPI(spedId, companyId, nfes, startDate, endDate, tempDir.toString());

        // Then
        String content = java.nio.file.Files.readString(Path.of(filePath), java.nio.charset.StandardCharsets.ISO_8859_1);

        // Check date format (ddMMyyyy)
        assertTrue(content.contains("|0000|0|15012024|31012024|"));

        // Check value format (comma as decimal separator)
        assertTrue(content.contains("1000,00")); // vNF
        assertTrue(content.contains("180,00"));  // vICMS
        assertTrue(content.contains("50,00"));   // vIPI
        assertTrue(content.contains("16,50"));   // vPIS
        assertTrue(content.contains("76,00"));   // vCOFINS
    }

    @Test
    void writeEFD_ICMS_IPI_HandlesEmptyNFEList() throws IOException {
        // Given
        List<NFE> nfes = List.of();
        String companyId = "company-1";
        String spedId = "sped-1";
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        // When
        String filePath = spedWriter.writeEFD_ICMS_IPI(spedId, companyId, nfes, startDate, endDate, tempDir.toString());

        // Then
        String content = java.nio.file.Files.readString(Path.of(filePath), java.nio.charset.StandardCharsets.ISO_8859_1);

        // Should still have header and trailer
        assertTrue(content.contains("|0000|"));
        assertTrue(content.contains("|9999|"));

        // But no C100 records
        long c100Count = content.lines().filter(l -> l.startsWith("|C100|")).count();
        assertEquals(0, c100Count);
    }

    private NFE createSampleNFE() {
        return createSampleNFE("1", "1", "1000.00");
    }

    private NFE createSampleNFE(String series, String number, String totalValue) {
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
                new BigDecimal("10"), new BigDecimal("100.00"), new BigDecimal(totalValue),
                null, null, "UN", "UNIDADE", new BigDecimal("10"), new BigDecimal("100.00"),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                icms, ipi, pis, cofins, iss
        );

        BigDecimal total = new BigDecimal(totalValue);
        BigDecimal icmsValue = total.multiply(new BigDecimal("0.18")).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal ipiValue = total.multiply(new BigDecimal("0.05")).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal pisValue = total.multiply(new BigDecimal("0.0165")).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal cofinsValue = total.multiply(new BigDecimal("0.076")).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal vNF = total.add(icmsValue).add(ipiValue).add(pisValue).add(cofinsValue);

        NFE.Totals totals = new NFE.Totals(
                total, icmsValue, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, total, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, ipiValue, BigDecimal.ZERO,
                pisValue, cofinsValue, BigDecimal.ZERO, vNF, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );

        NFE.Transport transport = new NFE.Transport(
                "0", null, null, null, null, null, null, null, null, null, null, null, null
        );

        NFE.Payment payment = new NFE.Payment(
                "1", List.of()
        );

        return new NFE(
                "nfe-" + number, "company-1", "3524011234567800019555001000000001" + number + "234567890",
                null, "AUTHORIZED", NFE.NFEType.SAIDA, series, number,
                LocalDateTime.of(2024, 1, 15, 10, 0), LocalDateTime.of(2024, 1, 15, 10, 0),
                emitter, recipient, List.of(item), totals,
                transport, payment, "Observação teste", null, null,
                LocalDateTime.now(), LocalDateTime.now(), null, null
        );
    }
}