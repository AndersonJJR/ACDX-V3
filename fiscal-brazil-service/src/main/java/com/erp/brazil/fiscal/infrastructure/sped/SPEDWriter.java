package com.erp.brazil.fiscal.infrastructure.sped;

import com.erp.brazil.fiscal.application.port.SPEDGenerationPort;
import com.erp.brazil.fiscal.domain.model.SPEDBlock;
import com.erp.brazil.fiscal.domain.model.SPEDType;
import com.erp.brazil.fiscal.domain.repository.NFERepository;
import com.erp.brazil.fiscal.domain.repository.NFSERepository;
import com.erp.brazil.fiscal.domain.repository.CTERepository;
import com.erp.brazil.fiscal.domain.repository.RetencaoFonteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SPEDWriter implements SPEDGenerationPort {

    private final NFERepository nfeRepository;
    private final NFSERepository nfseRepository;
    private final CTERepository cteRepository;
    private final RetencaoFonteRepository retencaoRepository;

    @Override
    public List<SPEDBlock> generate(String companyCnpj, SPEDType spedType, String referenceMonth, int year) {
        log.info("Generating SPED {} for company {} - {}/{}", spedType, companyCnpj, referenceMonth, year);

        List<SPEDBlock> blocks = new ArrayList<>();

        switch (spedType) {
            case EFD_ICMS_IPI -> blocks.addAll(generateEFDICMSIPI(companyCnpj, referenceMonth, year));
            case EFD_CONTRIBUICOES -> blocks.addAll(generateEFDContribuicoes(companyCnpj, referenceMonth, year));
            case ECD -> blocks.addAll(generateECD(companyCnpj, referenceMonth, year));
            case ECF -> blocks.addAll(generateECF(companyCnpj, referenceMonth, year));
        }

        return blocks;
    }

    private List<SPEDBlock> generateEFDICMSIPI(String companyCnpj, String referenceMonth, int year) {
        List<SPEDBlock> blocks = new ArrayList<>();

        // Block 0 - Opening
        blocks.add(createBlock("0", "0000", createReg0000(companyCnpj, referenceMonth)));

        // Block C - NF-e (Entradas/Saídas)
        List<com.erp.brazil.fiscal.domain.model.NFE> nfes = nfeRepository.findByEmitenteCnpjAndDataEmissaoBetween(
                companyCnpj,
                parseMonthStart(referenceMonth),
                parseMonthEnd(referenceMonth)
        );

        for (com.erp.brazil.fiscal.domain.model.NFE nfe : nfes) {
            blocks.add(createBlock("C", "C100", createRegC100(nfe)));
            blocks.add(createBlock("C", "C170", createRegC170(nfe)));
        }

        // Block E - Apuração ICMS/IPI
        blocks.add(createBlock("E", "E100", createRegE100(companyCnpj, referenceMonth)));
        blocks.add(createBlock("E", "E200", createRegE200(companyCnpj, referenceMonth)));

        // Block H - Inventário
        blocks.add(createBlock("H", "H005", createRegH005(companyCnpj, referenceMonth)));

        // Block 9 - Closing
        blocks.add(createBlock("9", "9900", createReg9900(blocks.size() + 1)));

        return blocks;
    }

    private List<SPEDBlock> generateEFDContribuicoes(String companyCnpj, String referenceMonth, int year) {
        List<SPEDBlock> blocks = new ArrayList<>();

        // Block 0 - Opening
        blocks.add(createBlock("0", "0000", createReg0000(companyCnpj, referenceMonth)));

        // Block M - PIS/COFINS
        List<com.erp.brazil.fiscal.domain.model.NFE> nfes = nfeRepository.findByEmitenteCnpjAndDataEmissaoBetween(
                companyCnpj,
                parseMonthStart(referenceMonth),
                parseMonthEnd(referenceMonth)
        );

        for (com.erp.brazil.fiscal.domain.model.NFE nfe : nfes) {
            blocks.add(createBlock("M", "M100", createRegM100(nfe)));
        }

        // Block 9 - Closing
        blocks.add(createBlock("9", "9900", createReg9900(blocks.size() + 1)));

        return blocks;
    }

    private List<SPEDBlock> generateECD(String companyCnpj, String referenceMonth, int year) {
        List<SPEDBlock> blocks = new ArrayList<>();

        // Block 0 - Opening
        blocks.add(createBlock("0", "0000", createReg0000(companyCnpj, referenceMonth)));

        // Block I - Livro Diário
        blocks.add(createBlock("I", "I010", createRegI010(companyCnpj, referenceMonth)));
        blocks.add(createBlock("I", "I050", createRegI050(companyCnpj, referenceMonth)));
        blocks.add(createBlock("I", "I051", createRegI051(companyCnpj, referenceMonth)));

        // Block J - Livro Diário (Partidas)
        blocks.add(createBlock("J", "J100", createRegJ100(companyCnpj, referenceMonth)));
        blocks.add(createBlock("J", "J150", createRegJ150(companyCnpj, referenceMonth)));

        // Block 9 - Closing
        blocks.add(createBlock("9", "9900", createReg9900(blocks.size() + 1)));

        return blocks;
    }

    private List<SPEDBlock> generateECF(String companyCnpj, String referenceMonth, int year) {
        List<SPEDBlock> blocks = new ArrayList<>();

        // Block 0 - Opening
        blocks.add(createBlock("0", "0000", createReg0000(companyCnpj, referenceMonth)));

        // Block L - Apuração Lucro Real
        blocks.add(createBlock("L", "L100", createRegL100(companyCnpj, referenceMonth)));
        blocks.add(createBlock("L", "L200", createRegL200(companyCnpj, referenceMonth)));
        blocks.add(createBlock("L", "L300", createRegL300(companyCnpj, referenceMonth)));

        // Block M - PIS/COFINS
        blocks.add(createBlock("M", "M100", createRegM100Ecf(companyCnpj, referenceMonth)));

        // Block 9 - Closing
        blocks.add(createBlock("9", "9900", createReg9900(blocks.size() + 1)));

        return blocks;
    }

    // Helper methods for creating register data (simplified)
    private String createReg0000(String cnpj, String referenceMonth) {
        return String.format("0000|%s|%s|%s|%s|%s|||", cnpj, "1", "0", "0", referenceMonth);
    }

    private String createRegC100(com.erp.brazil.fiscal.domain.model.NFE nfe) {
        return String.format("C100|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s||",
                nfe.getId(), "0", nfe.getSerie(), nfe.getNumero(),
                nfe.getDataEmissao().format(DateTimeFormatter.ofPattern("ddMMyyyy")),
                nfe.getValorTotal(), nfe.getValorIcms(), nfe.getValorIpi(),
                nfe.getChaveAcesso(), "1");
    }

    private String createRegC170(com.erp.brazil.fiscal.domain.model.NFE nfe) {
        return String.format("C170|%s|%s|%s|||||||", nfe.getId(), "001", "1");
    }

    private String createRegE100(String cnpj, String referenceMonth) {
        return String.format("E100|%s|%s|%s|%s||", cnpj, referenceMonth, "0", "0");
    }

    private String createRegE200(String cnpj, String referenceMonth) {
        return String.format("E200|%s|%s|%s|%s||", cnpj, referenceMonth, "0", "0");
    }

    private String createRegH005(String cnpj, String referenceMonth) {
        return String.format("H005|%s|%s||", cnpj, referenceMonth);
    }

    private String createReg9900(int totalLines) {
        return String.format("9900|%d", totalLines);
    }

    private String createRegM100(com.erp.brazil.fiscal.domain.model.NFE nfe) {
        return String.format("M100|%s|%s|%s|%s|%s|%s||",
                nfe.getId(), "0", nfe.getSerie(), nfe.getNumero(),
                nfe.getValorTotal(), nfe.getDataEmissao().format(DateTimeFormatter.ofPattern("ddMMyyyy")));
    }

    private String createRegI010(String cnpj, String referenceMonth) {
        return String.format("I010|%s|%s||", cnpj, referenceMonth);
    }

    private String createRegI050(String cnpj, String referenceMonth) {
        return String.format("I050|%s|%s||", cnpj, referenceMonth);
    }

    private String createRegI051(String cnpj, String referenceMonth) {
        return String.format("I051|%s|%s||", cnpj, referenceMonth);
    }

    private String createRegJ100(String cnpj, String referenceMonth) {
        return String.format("J100|%s|%s||", cnpj, referenceMonth);
    }

    private String createRegJ150(String cnpj, String referenceMonth) {
        return String.format("J150|%s|%s||", cnpj, referenceMonth);
    }

    private String createRegL100(String cnpj, String referenceMonth) {
        return String.format("L100|%s|%s||", cnpj, referenceMonth);
    }

    private String createRegL200(String cnpj, String referenceMonth) {
        return String.format("L200|%s|%s||", cnpj, referenceMonth);
    }

    private String createRegL300(String cnpj, String referenceMonth) {
        return String.format("L300|%s|%s||", cnpj, referenceMonth);
    }

    private String createRegM100Ecf(String cnpj, String referenceMonth) {
        return String.format("M100|%s|%s||", cnpj, referenceMonth);
    }

    private LocalDateTime parseMonthStart(String referenceMonth) {
        return LocalDate.parse(referenceMonth + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
    }

    private LocalDateTime parseMonthEnd(String referenceMonth) {
        return parseMonthStart(referenceMonth).plusMonths(1).minusDays(1).atTime(23, 59, 59);
    }

    private SPEDBlock createBlock(String block, String register, String data) {
        SPEDBlock spedBlock = SPEDBlock.builder()
                .id(UUID.randomUUID().toString())
                .block(block)
                .register(register)
                .data(data)
                .build();
        return spedBlock;
    }

    @Override
    public ValidationResult validate(SPEDBlock block) {
        // Simplified validation
        return ValidationResult.valid();
    }

    @Override
    public void transmit(SPEDBlock block) {
        log.info("Transmitting SPED block: {}-{}", block.getBlock(), block.getRegister());
        // Implementation would send to SPED system (Receita Federal)
    }
}