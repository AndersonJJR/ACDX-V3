package com.erp.brazil.hr.infrastructure.pdf;

import com.erp.brazil.hr.domain.model.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Component
public class PayslipPDFGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MM/yyyy");
    private final Path outputDir = Paths.get("/tmp/payslips");

    public PayslipPDFGenerator() {
        try {
            Files.createDirectories(outputDir);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create payslip output directory", e);
        }
    }

    public String generate(Long payrollId, Long employeeId, List<PayrollItem> items) {
        String fileName = String.format("payslip_%d_%d_%s.pdf", payrollId, employeeId, UUID.randomUUID().toString().substring(0, 8));
        Path filePath = outputDir.resolve(fileName);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            addHeader(document, payrollId, employeeId, items);
            addEmployeeInfo(document);
            addEarningsSection(document, items);
            addDeductionsSection(document, items);
            addEmployerCostsSection(document, items);
            addTotals(document, items);
            addFooter(document);

            document.close();
            Files.write(filePath, baos.toByteArray());
            return filePath.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate payslip PDF", e);
        }
    }

    private void addHeader(Document document, Long payrollId, Long employeeId, List<PayrollItem> items) throws DocumentException {
        Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
        Font subFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

        Paragraph title = new Paragraph("HOLERITE / PAYSLIP", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph ref = new Paragraph("Referência: " + MONTH_FMT.format(LocalDate.now()), subFont);
        ref.setAlignment(Element.ALIGN_CENTER);
        document.add(ref);

        document.add(new Paragraph(" "));
    }

    private void addEmployeeInfo(Document document) throws DocumentException {
        Font labelFont = new Font(Font.HELVETICA, 9, Font.BOLD);
        Font valueFont = new Font(Font.HELVETICA, 9, Font.NORMAL);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 3, 2, 3});

        addCell(table, "Empresa:", "ACDX ERP LTDA", labelFont, valueFont);
        addCell(table, "CNPJ:", "00.000.000/0001-00", labelFont, valueFont);
        addCell(table, "Funcionário:", "João da Silva", labelFont, valueFont);
        addCell(table, "CPF:", "***.***.***-**", labelFont, valueFont);
        addCell(table, "Matrícula:", "000123", labelFont, valueFont);
        addCell(table, "Cargo:", "Desenvolvedor Senior", labelFont, valueFont);
        addCell(table, "Depto:", "Tecnologia", labelFont, valueFont);
        addCell(table, "Admissão:", "01/01/2020", labelFont, valueFont);

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addEarningsSection(Document document, List<PayrollItem> items) throws DocumentException {
        Font sectionFont = new Font(Font.HELVETICA, 11, Font.BOLD);
        Font headerFont = new Font(Font.HELVETICA, 9, Font.BOLD);
        Font cellFont = new Font(Font.HELVETICA, 8, Font.NORMAL);

        Paragraph section = new Paragraph("PROVENTOS / EARNINGS", sectionFont);
        document.add(section);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 3, 1, 1.5});

        addHeaderCell(table, "Código", headerFont);
        addHeaderCell(table, "Descrição", headerFont);
        addHeaderCell(table, "Referência", headerFont);
        addHeaderCell(table, "Valor", headerFont);

        BigDecimal totalEarnings = BigDecimal.ZERO;
        for (PayrollItem item : items) {
            if (item.itemType() == PayrollItem.ItemType.EARNING) {
                addCell(table, item.code(), item.description(), cellFont);
                addCell(table, formatValue(item.referenceValue()), formatValue(item.totalAmount()), cellFont);
                totalEarnings = totalEarnings.add(item.totalAmount());
            }
        }

        addTotalRow(table, "TOTAL PROVENTOS", totalEarnings, headerFont);
        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addDeductionsSection(Document document, List<PayrollItem> items) throws DocumentException {
        Font sectionFont = new Font(Font.HELVETICA, 11, Font.BOLD);
        Font headerFont = new Font(Font.HELVETICA, 9, Font.BOLD);
        Font cellFont = new Font(Font.HELVETICA, 8, Font.NORMAL);

        Paragraph section = new Paragraph("DESCONTOS / DEDUCTIONS", sectionFont);
        document.add(section);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 3, 1, 1.5});

        addHeaderCell(table, "Código", headerFont);
        addHeaderCell(table, "Descrição", headerFont);
        addHeaderCell(table, "Referência", headerFont);
        addHeaderCell(table, "Valor", headerFont);

        BigDecimal totalDeductions = BigDecimal.ZERO;
        for (PayrollItem item : items) {
            if (item.itemType() == PayrollItem.ItemType.DEDUCTION) {
                addCell(table, item.code(), item.description(), cellFont);
                addCell(table, formatValue(item.referenceValue()), formatValue(item.totalAmount()), cellFont);
                totalDeductions = totalDeductions.add(item.totalAmount());
            }
        }

        addTotalRow(table, "TOTAL DESCONTOS", totalDeductions, headerFont);
        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addEmployerCostsSection(Document document, List<PayrollItem> items) throws DocumentException {
        Font sectionFont = new Font(Font.HELVETICA, 11, Font.BOLD);
        Font headerFont = new Font(Font.HELVETICA, 9, Font.BOLD);
        Font cellFont = new Font(Font.HELVETICA, 8, Font.NORMAL);

        Paragraph section = new Paragraph("ENCARGOS EMPREGADOR / EMPLOYER COSTS (Informativo)", sectionFont);
        document.add(section);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 3, 1, 1.5});

        addHeaderCell(table, "Código", headerFont);
        addHeaderCell(table, "Descrição", headerFont);
        addHeaderCell(table, "Referência", headerFont);
        addHeaderCell(table, "Valor", headerFont);

        BigDecimal totalCosts = BigDecimal.ZERO;
        for (PayrollItem item : items) {
            if (item.itemType() == PayrollItem.ItemType.EMPLOYER_COST) {
                addCell(table, item.code(), item.description(), cellFont);
                addCell(table, formatValue(item.referenceValue()), formatValue(item.totalAmount()), cellFont);
                totalCosts = totalCosts.add(item.totalAmount());
            }
        }

        addTotalRow(table, "TOTAL ENCARGOS", totalCosts, headerFont);
        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addTotals(Document document, List<PayrollItem> items) throws DocumentException {
        Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font valueFont = new Font(Font.HELVETICA, 10, Font.BOLD);

        BigDecimal gross = items.stream()
            .filter(i -> i.itemType() == PayrollItem.ItemType.EARNING)
            .map(PayrollItem::totalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal deductions = items.stream()
            .filter(i -> i.itemType() == PayrollItem.ItemType.DEDUCTION)
            .map(PayrollItem::totalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal net = gross.subtract(deductions);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 1});

        addTotalCell(table, "TOTAL BRUTO / GROSS TOTAL", formatValue(gross), headerFont, valueFont);
        addTotalCell(table, "TOTAL DESCONTOS / TOTAL DEDUCTIONS", formatValue(deductions), headerFont, valueFont);
        addTotalCell(table, "LÍQUIDO A RECEBER / NET PAY", formatValue(net), headerFont, valueFont);

        document.add(table);
    }

    private void addFooter(Document document) throws DocumentException {
        Font smallFont = new Font(Font.HELVETICA, 7, Font.ITALIC);
        document.add(new Paragraph(" "));
        Paragraph footer = new Paragraph(
            "Este documento é uma representação simplificada do holerite. " +
            "Valores sujeitos a alterações conforme legislação trabalhista vigente. " +
            "Documento gerado eletronicamente sem necessidade de assinatura física conforme MP 2.200-2/2001.",
            smallFont
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private void addCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(2);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(2);
        table.addCell(valueCell);
    }

    private void addCell(PdfPTable table, String code, String desc, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(code, font));
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderWidth(0.5f);
        cell.setPadding(3);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(desc, font));
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderWidth(0.5f);
        cell.setPadding(3);
        cell.setColspan(2);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase("", font));
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderWidth(0.5f);
        cell.setPadding(3);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String code, String desc, String ref, String value, Font font) {
        addCell(table, code, font);
        addCell(table, desc, font);
        addCell(table, ref, font);
        addCell(table, value, font);
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderWidth(0.5f);
        cell.setPadding(3);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(230, 230, 230));
        cell.setBorder(Rectangle.BOX);
        cell.setBorderWidth(0.5f);
        cell.setPadding(3);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addTotalRow(PdfPTable table, String label, BigDecimal value, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(label, font));
        cell.setColspan(3);
        cell.setBorder(Rectangle.TOP);
        cell.setBorderWidth(1f);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(formatValue(value), font));
        cell.setBorder(Rectangle.TOP);
        cell.setBorderWidth(1f);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
    }

    private void addTotalCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell(new Phrase(label, labelFont));
        cell.setBorder(Rectangle.TOP);
        cell.setBorderWidth(label.contains("LÍQUIDO") ? 1.5f : 0.5f);
        cell.setPadding(5);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(value, valueFont));
        cell.setBorder(Rectangle.TOP);
        cell.setBorderWidth(label.contains("LÍQUIDO") ? 1.5f : 0.5f);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
    }

    private String formatValue(BigDecimal value) {
        if (value == null) return "0,00";
        return String.format("%,.2f", value).replace(",", "X").replace(".", ",").replace("X", ".");
    }
}