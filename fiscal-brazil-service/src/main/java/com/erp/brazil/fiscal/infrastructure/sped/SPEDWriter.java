package com.erp.brazil.fiscal.infrastructure.sped;

import com.erp.brazil.fiscal.domain.model.NFE;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SPED Writer for generating fixed-width/CSV files for each SPED block.
 * Supports ECD, ECF, EFD-ICMS-IPI, EFD-Contribuições.
 */
@Component
public class SPEDWriter {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("ddMMyyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");

    public String writeECD(String spedId, String companyId, List<NFE> nfes,
                           LocalDate startDate, LocalDate endDate, String outputDir) throws IOException {
        Path filePath = Paths.get(outputDir, "ECD_" + companyId + "_" + spedId + ".txt");
        Files.createDirectories(filePath.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.ISO_8859_1)) {
            // Registro 0000 - Abertura do Arquivo Digital
            writer.write("|0000|0|" + DATE_FMT.format(startDate) + "|" + DATE_FMT.format(endDate) + "|"
                    + companyId + "|" + getCompanyName(companyId) + "|||0|0|0|0|0|0|0|0|0|0|0|0|\n");

            // Registro 0001 - Abertura do Bloco 0
            writer.write("|0001|0|\n");

            // Registro 0150 - Tabela de Cadastro do Participante
            writeParticipantes(writer, nfes);

            // Registro 0190 - Unidades de Medida
            writer.write("|0190|UN|UNIDADE|\n");
            writer.write("|0190|KG|QUILOGRAMA|\n");
            writer.write("|0190|L|LITRO|\n");
            writer.write("|0190|M|METRO|\n");

            // Registro 0200 - Identificação dos Itens (Produtos e Serviços)
            writeItens(writer, nfes);

            // Registro 0990 - Encerramento do Bloco 0
            writer.write("|0990|\n");

            // Bloco C - Documentos Fiscais I - Mercadorias (ICMS/IPI)
            writeBlocoC(writer, nfes);

            // Bloco E - Apuração do ICMS e do IPI
            writeBlocoE(writer, nfes);

            // Bloco H - Inventário Físico
            writeBlocoH(writer, nfes);

            // Bloco 1 - Documentos Fiscais II - Serviços (ISS)
            writeBloco1(writer, nfes);

            // Registro 9999 - Encerramento do Arquivo Digital
            writer.write("|9999|\n");
        }

        return filePath.toString();
    }

    public String writeECF(String spedId, String companyId, List<NFE> nfes,
                           LocalDate startDate, LocalDate endDate, String outputDir) throws IOException {
        Path filePath = Paths.get(outputDir, "ECF_" + companyId + "_" + spedId + ".txt");
        Files.createDirectories(filePath.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.ISO_8859_1)) {
            // Registro 0000 - Abertura
            writer.write("|0000|0|" + DATE_FMT.format(startDate) + "|" + DATE_FMT.format(endDate) + "|"
                    + companyId + "|" + getCompanyName(companyId) + "|||0|0|0|0|0|0|0|0|0|0|0|0|\n");

            // Registro 0001 - Abertura do Bloco 0
            writer.write("|0001|0|\n");

            // Registro 0150 - Participantes
            writeParticipantes(writer, nfes);

            // Registro 0200 - Itens
            writeItens(writer, nfes);

            // Registro 0990 - Encerramento Bloco 0
            writer.write("|0990|\n");

            // Bloco J - Escrituração do Lucro Real
            writeBlocoJ(writer, nfes);

            // Bloco L - Lucro Presumido/Arbitrado
            writeBlocoL(writer, nfes);

            // Bloco M - Resultados e Apurações
            writeBlocoM(writer, nfes);

            // Registro 9999 - Encerramento
            writer.write("|9999|\n");
        }

        return filePath.toString();
    }

    public String writeEFD_ICMS_IPI(String spedId, String companyId, List<NFE> nfes,
                                     LocalDate startDate, LocalDate endDate, String outputDir) throws IOException {
        Path filePath = Paths.get(outputDir, "EFD_ICMS_IPI_" + companyId + "_" + spedId + ".txt");
        Files.createDirectories(filePath.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.ISO_8859_1)) {
            // Registro 0000 - Abertura
            writer.write("|0000|0|" + DATE_FMT.format(startDate) + "|" + DATE_FMT.format(endDate) + "|"
                    + companyId + "|" + getCompanyName(companyId) + "|||0|0|0|0|0|0|0|0|0|0|0|0|\n");

            // Registro 0001 - Abertura do Bloco 0
            writer.write("|0001|0|\n");

            // Registro 0150 - Tabela de Cadastro do Participante
            writeParticipantes(writer, nfes);

            // Registro 0190 - Unidades de Medida
            writer.write("|0190|UN|UNIDADE|\n");
            writer.write("|0190|KG|QUILOGRAMA|\n");
            writer.write("|0190|L|LITRO|\n");
            writer.write("|0190|M|METRO|\n");
            writer.write("|0190|CX|CAIXA|\n");

            // Registro 0200 - Identificação dos Itens
            writeItens(writer, nfes);

            // Registro 0990 - Encerramento do Bloco 0
            writer.write("|0990|\n");

            // Bloco C - Documentos Fiscais I - Mercadorias (ICMS/IPI)
            writeBlocoC(writer, nfes);

            // Bloco D - Documentos Fiscais II - Serviços (ISS)
            writeBlocoD(writer, nfes);

            // Bloco E - Apuração do ICMS e do IPI
            writeBlocoE(writer, nfes);

            // Bloco G - Controle de Crédito de ICMS do Ativo Permanente - CIAP
            writeBlocoG(writer, nfes);

            // Bloco H - Inventário Físico
            writeBlocoH(writer, nfes);

            // Bloco 1 - Documentos Fiscais III - Serviços de Transporte (ICMS)
            writeBloco1(writer, nfes);

            // Registro 9999 - Encerramento do Arquivo Digital
            writer.write("|9999|\n");
        }

        return filePath.toString();
    }

    public String writeEFD_Contribuicoes(String spedId, String companyId, List<NFE> nfes,
                                          LocalDate startDate, LocalDate endDate, String outputDir) throws IOException {
        Path filePath = Paths.get(outputDir, "EFD_Contribuicoes_" + companyId + "_" + spedId + ".txt");
        Files.createDirectories(filePath.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.ISO_8859_1)) {
            // Registro 0000 - Abertura
            writer.write("|0000|0|" + DATE_FMT.format(startDate) + "|" + DATE_FMT.format(endDate) + "|"
                    + companyId + "|" + getCompanyName(companyId) + "|||0|0|0|0|0|0|0|0|0|0|0|0|\n");

            // Registro 0001 - Abertura do Bloco 0
            writer.write("|0001|0|\n");

            // Registro 0150 - Tabela de Cadastro do Participante
            writeParticipantes(writer, nfes);

            // Registro 0190 - Unidades de Medida
            writer.write("|0190|UN|UNIDADE|\n");
            writer.write("|0190|KG|QUILOGRAMA|\n");
            writer.write("|0190|L|LITRO|\n");

            // Registro 0200 - Identificação dos Itens
            writeItens(writer, nfes);

            // Registro 0990 - Encerramento do Bloco 0
            writer.write("|0990|\n");

            // Bloco P - Apuração da Contribuição para o PIS/PASEP e da COFINS
            writeBlocoP(writer, nfes);

            // Bloco M - Informações Complementares
            writeBlocoM(writer, nfes);

            // Bloco 1 - Documentos Fiscais - Serviços (ISS)
            writeBloco1(writer, nfes);

            // Registro 9999 - Encerramento do Arquivo Digital
            writer.write("|9999|\n");
        }

        return filePath.toString();
    }

    private void writeParticipantes(BufferedWriter writer, List<NFE> nfes) throws IOException {
        // Collect unique participants (emitters and recipients)
        var participants = new java.util.LinkedHashMap<String, NFE.Emitter>();
        for (NFE nfe : nfes) {
            participants.put(nfe.emitter().cnpj(), nfe.emitter());
            participants.put(nfe.recipient().cnpjCpf(), nfe.recipient());
        }

        int index = 1;
        for (var entry : participants.entrySet()) {
            var emitter = entry.getValue();
            writer.write("|0150|" + index + "|" + entry.getKey() + "|"
                    + (emitter.ie() != null ? emitter.ie() : "") + "|"
                    + (emitter.companyName() != null ? emitter.companyName() : "") + "|"
                    + (emitter.address() != null ? emitter.address().street() : "") + "|"
                    + (emitter.address() != null ? emitter.address().number() : "") + "|"
                    + (emitter.address() != null ? emitter.address().complement() : "") + "|"
                    + (emitter.address() != null ? emitter.address().neighborhood() : "") + "|"
                    + (emitter.address() != null ? emitter.address().cityCode() : "") + "|"
                    + (emitter.address() != null ? emitter.address().uf() : "") + "|"
                    + (emitter.address() != null ? emitter.address().cep() : "") + "|"
                    + (emitter.cnae() != null ? emitter.cnae() : "") + "|\n");
            index++;
        }
    }

    private void writeItens(BufferedWriter writer, List<NFE> nfes) throws IOException {
        var items = new java.util.LinkedHashMap<String, NFE.Item>();
        for (NFE nfe : nfes) {
            for (NFE.Item item : nfe.items()) {
                String key = item.ncm() + "|" + item.cfop() + "|" + item.ucom();
                if (!items.containsKey(key)) {
                    items.put(key, item);
                }
            }
        }

        int index = 1;
        for (var item : items.values()) {
            writer.write("|0200|" + index + "|" + (item.ncm() != null ? item.ncm() : "") + "|"
                    + (item.cest() != null ? item.cest() : "") + "|"
                    + (item.description() != null ? item.description() : "ITEM") + "|"
                    + (item.ucom() != null ? item.ucom() : "UN") + "|"
                    + (item.uTrib() != null ? item.uTrib() : item.ucom()) + "|"
                    + (item.cfop() != null ? item.cfop() : "") + "|\n");
            index++;
        }
    }

    private void writeBlocoC(BufferedWriter writer, List<NFE> nfes) throws IOException {
        // Registro C100 - Notas Fiscais (modelo 55)
        for (NFE nfe : nfes) {
            writer.write("|C100|" + (nfe.type() == NFE.NFEType.ENTRADA ? "0" : "1") + "|"
                    + nfe.series() + "|" + nfe.number() + "|"
                    + DATE_FMT.format(nfe.emissionDate().toLocalDate()) + "|"
                    + (nfe.emissionDate().format(DATETIME_FMT)) + "|"
                    + nfe.emitter().cnpj() + "|"
                    + nfe.recipient().cnpjCpf() + "|"
                    + formatValue(nfe.totals().vNF()) + "|"
                    + formatValue(nfe.totals().vBC()) + "|"
                    + formatValue(nfe.totals().vICMS()) + "|"
                    + formatValue(nfe.totals().vBCST()) + "|"
                    + formatValue(nfe.totals().vST()) + "|"
                    + formatValue(nfe.totals().vIPI()) + "|"
                    + formatValue(nfe.totals().vPIS()) + "|"
                    + formatValue(nfe.totals().vCOFINS()) + "|\n");

            // Registro C170 - Itens da NF-e
            for (int i = 0; i < nfe.items().size(); i++) {
                NFE.Item item = nfe.items().get(i);
                writer.write("|C170|" + (i + 1) + "|"
                        + item.ncm() + "|"
                        + item.cfop() + "|"
                        + item.ucom() + "|"
                        + formatValue(item.qCom()) + "|"
                        + formatValue(item.vUnCom()) + "|"
                        + formatValue(item.vProd()) + "|"
                        + formatValue(item.vDesc()) + "|"
                        + (item.icms() != null ? item.icms().cst() : "") + "|"
                        + formatValue(item.icms() != null ? item.icms().vBC() : BigDecimal.ZERO) + "|"
                        + formatValue(item.icms() != null ? item.icms().pICMS() : BigDecimal.ZERO) + "|"
                        + formatValue(item.icms() != null ? item.icms().vICMS() : BigDecimal.ZERO) + "|"
                        + (item.ipi() != null ? item.ipi().cst() : "") + "|"
                        + formatValue(item.ipi() != null ? item.ipi().vBC() : BigDecimal.ZERO) + "|"
                        + formatValue(item.ipi() != null ? item.ipi().pIPI() : BigDecimal.ZERO) + "|"
                        + formatValue(item.ipi() != null ? item.ipi().vIPI() : BigDecimal.ZERO) + "|\n");
            }
        }
    }

    private void writeBlocoD(BufferedWriter writer, List<NFE> nfes) throws IOException {
        // Registro D100 - Notas Fiscais de Serviços
        for (NFE nfe : nfes) {
            if (nfe.totals().vServ() != null && nfe.totals().vServ().compareTo(BigDecimal.ZERO) > 0) {
                writer.write("|D100|" + (nfe.type() == NFE.NFEType.ENTRADA ? "0" : "1") + "|"
                        + nfe.series() + "|" + nfe.number() + "|"
                        + DATE_FMT.format(nfe.emissionDate().toLocalDate()) + "|"
                        + nfe.emitter().cnpj() + "|"
                        + nfe.recipient().cnpjCpf() + "|"
                        + formatValue(nfe.totals().vServ()) + "|"
                        + formatValue(nfe.totals().vBCServ()) + "|"
                        + formatValue(nfe.totals().vISS()) + "|\n");
            }
        }
    }

    private void writeBlocoE(BufferedWriter writer, List<NFE> nfes) throws IOException {
        // Registro E100 - Apuração do ICMS
        writer.write("|E100|" + DATE_FMT.format(nfes.get(0).emissionDate().toLocalDate()) + "|"
                + "0|" + formatValue(BigDecimal.ZERO) + "|"
                + formatValue(BigDecimal.ZERO) + "|"
                + formatValue(BigDecimal.ZERO) + "|\n");
    }

    private void writeBlocoG(BufferedWriter writer, List<NFE> nfes) throws IOException {
        // Registro G100 - CIAP
        writer.write("|G100|\n");
    }

    private void writeBlocoH(BufferedWriter writer, List<NFE> nfes) throws IOException {
        // Registro H001 - Abertura do Bloco H
        writer.write("|H001|0|\n");

        // Registro H010 - Inventário
        writer.write("|H010|1|" + DATE_FMT.format(LocalDate.now()) + "|01||\n");

        // Registro H990 - Encerramento do Bloco H
        writer.write("|H990|\n");
    }

    private void writeBloco1(BufferedWriter writer, List<NFE> nfes) throws IOException {
        // Registro 1001 - Abertura do Bloco 1
        writer.write("|1001|0|\n");

        // Registro 1400 - Documentos Fiscais - Serviços
        for (NFE nfe : nfes) {
            if (nfe.totals().vServ() != null && nfe.totals().vServ().compareTo(BigDecimal.ZERO) > 0) {
                writer.write("|1400|" + nfe.series() + "|" + nfe.number() + "|"
                        + DATE_FMT.format(nfe.emissionDate().toLocalDate()) + "|"
                        + nfe.emitter().cnpj() + "|"
                        + nfe.recipient().cnpjCpf() + "|"
                        + formatValue(nfe.totals().vServ()) + "|"
                        + formatValue(nfe.totals().vBCServ()) + "|"
                        + formatValue(nfe.totals().vISS()) + "|\n");
            }
        }

        // Registro 1990 - Encerramento do Bloco 1
        writer.write("|1990|\n");
    }

    private void writeBlocoJ(BufferedWriter writer, List<NFE> nfes) throws IOException {
        // Bloco J - Escrituração do Lucro Real
        writer.write("|J001|0|\n");
        writer.write("|J990|\n");
    }

    private void writeBlocoL(BufferedWriter writer, List<NFE> nfes) throws IOException {
        // Bloco L - Lucro Presumido/Arbitrado
        writer.write("|L001|0|\n");
        writer.write("|L990|\n");
    }

    private void writeBlocoM(BufferedWriter writer, List<NFE> nfes) throws IOException {
        // Bloco M - Informações Complementares
        writer.write("|M001|0|\n");
        writer.write("|M990|\n");
    }

    private void writeBlocoP(BufferedWriter writer, List<NFE> nfes) throws IOException {
        // Bloco P - Apuração da Contribuição para o PIS/PASEP e da COFINS
        writer.write("|P001|0|\n");
        writer.write("|P990|\n");
    }

    private String formatValue(BigDecimal value) {
        if (value == null) return "";
        return value.setScale(2, java.math.RoundingMode.HALF_UP).toString().replace(".", ",");
    }

    private String getCompanyName(String companyId) {
        // In production, fetch from company service
        return "EMPRESA " + companyId;
    }
}