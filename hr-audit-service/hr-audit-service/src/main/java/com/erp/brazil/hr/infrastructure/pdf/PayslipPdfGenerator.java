package com.erp.brazil.hr.infrastructure.pdf;

import com.erp.brazil.hr.domain.model.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class PayslipPdfGenerator {

    @Value("${app.pdf.payslip.watermark:CONFIDENCIAL}")
    private String watermark;

    @Value("${app.pdf.payslip.digital-signature.enabled:false}")
    private boolean digitalSignatureEnabled;

    public byte[] generate(Payslip payslip, List<PayrollItem> items, Company company) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            // Add watermark
            writer.setPageEvent(new WatermarkPageEvent(watermark));

            document.open();

            // Header
            addHeader(document, company, payslip);

            // Employee Info
            addEmployeeInfo(document, payslip);

            // Earnings table
            addEarningsTable(document, items);

            // Deductions table
            addDeductionsTable(document, items);

            // Employer costs table
            addEmployerCostsTable(document, items);

            // Totals
            addTotals(document, payslip);

            // Footer
            addFooter(document, payslip);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating payslip PDF", e);
            throw new RuntimeException("Failed to generate payslip PDF", e);
        }
    }

    private void addHeader(Document document, Company company, Payslip payslip) throws DocumentException {
        // Company logo area
        PdfPTable headerTable = new PdfPTable(3);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{2, 5, 2});

        // Company info
        PdfPCell companyCell = new PdfPCell();
        companyCell.setBorder(Rectangle.NO_BORDER);
        companyCell.addElement(new Paragraph(company.getDisplayName(), getFont(Font.BOLD, 16, new Color(0, 51, 102))));
        companyCell.addElement(new Paragraph(company.getCorporateName(), getFont(Font.NORMAL, 10, Color.GRAY)));
        companyCell.addElement(new Paragraph("CNPJ: " + company.getFormattedCnpj(), getFont(Font.NORMAL, 9, Color.GRAY)));
        headerTable.addCell(companyCell);

        // Title
        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.addElement(new Paragraph("HOLERITE", getFont(Font.BOLD, 18, new Color(0, 51, 102))));
        titleCell.addElement(new Paragraph("Comprovante de Pagamento", getFont(Font.NORMAL, 10, Color.GRAY)));
        headerTable.addCell(titleCell);

        // Period info
        PdfPCell periodCell = new PdfPCell();
        periodCell.setBorder(Rectangle.NO_BORDER);
        periodCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Payroll payroll = payslip.getPayroll();
        periodCell.addElement(new Paragraph("Referência: " + String.format("%02d/%d", payroll.getReferenceMonth(), payroll.getReferenceYear()),
            getFont(Font.BOLD, 12, Color.BLACK)));
        periodCell.addElement(new Paragraph("Emissão: " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            getFont(Font.NORMAL, 9, Color.GRAY)));
        headerTable.addCell(periodCell);

        document.add(headerTable);
        document.add(new Paragraph(" ")); // Spacer
    }

    private void addEmployeeInfo(Document document, Payslip payslip) throws DocumentException {
        Employee employee = payslip.getEmployee();
        EmploymentContract contract = employee.getEmploymentContract();

        PdfPTable infoTable = new PdfPTable(4);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{2, 3, 2, 3});

        addInfoCell(infoTable, "Matrícula:", employee.getEmployeeNumber(), true);
        addInfoCell(infoTable, "Nome:", employee.getFullName(), false);
        addInfoCell(infoTable, "CPF:", employee.getMaskedCpf(), true);
        addInfoCell(infoTable, "Cargo:", contract != null ? contract.getPositionTitle() : "-", false);
        addInfoCell(infoTable, "Admissão:", employee.getHireDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), true);
        addInfoCell(infoTable, "Departamento:", "-", false);
        addInfoCell(infoTable, "Contrato:", contract != null ? contract.getContractType().getDescription() : "-", true);
        addInfoCell(infoTable, "Jornada:", contract != null ? contract.getWorkJourney().getDescription() : "-", false);

        document.add(infoTable);
        document.add(new Paragraph(" "));
    }

    private void addInfoCell(PdfPTable table, String label, String value, boolean bold) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, getFont(Font.BOLD, 9, Color.GRAY)));
        labelCell.setBorder(Rectangle.BOTTOM);
        labelCell.setBorderColor(Color.LIGHT_GRAY);
        labelCell.setPadding(4);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, getFont(bold ? Font.BOLD : Font.NORMAL, 9, Color.BLACK)));
        valueCell.setBorder(Rectangle.BOTTOM);
        valueCell.setBorderColor(Color.LIGHT_GRAY);
        valueCell.setPadding(4);
        table.addCell(valueCell);
    }

    private void addEarningsTable(Document document, List<PayrollItem> items) throws DocumentException {
        List<PayrollItem> earnings = items.stream()
            .filter(i -> i.getItemType() == PayrollItem.ItemType.EARNING)
            .toList();

        if (earnings.isEmpty()) return;

        document.add(new Paragraph("PROVENTOS", getFont(Font.BOLD, 11, new Color(0, 100, 0))));
        PdfPTable table = createItemTable(earnings);
        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addDeductionsTable(Document document, List<PayrollItem> items) throws DocumentException {
        List<PayrollItem> deductions = items.stream()
            .filter(i -> i.getItemType() == PayrollItem.ItemType.DEDUCTION)
            .toList();

        if (deductions.isEmpty()) return;

        document.add(new Paragraph("DESCONTOS", getFont(Font.BOLD, 11, new Color(180, 0, 0))));
        PdfPTable table = createItemTable(deductions);
        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addEmployerCostsTable(Document document, List<PayrollItem> items) throws DocumentException {
        List<PayrollItem> employerCosts = items.stream()
            .filter(i -> i.getItemType() == PayrollItem.ItemType.EMPLOYER_COST)
            .toList();

        if (employerCosts.isEmpty()) return;

        document.add(new Paragraph("ENCARGOS PATRONAIS (INFORMATIVO)", getFont(Font.BOLD, 11, new Color(100, 100, 0))));
        PdfPTable table = createItemTable(employerCosts);
        document.add(table);
        document.add(new Paragraph(" "));
    }

    private PdfPTable createItemTable(List<PayrollItem> items) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{4, 1, 2, 2});

        // Header
        addTableHeader(table, "Descrição");
        addTableHeader(table, "Qtd");
        addTableHeader(table, "Vlr. Unit.");
        addTableHeader(table, "Total");

        // Rows
        for (PayrollItem item : items) {
            PdfPCell descCell = new PdfPCell(new Phrase(item.getDescription(), getFont(Font.NORMAL, 9)));
            descCell.setBorder(Rectangle.BOTTOM);
            descCell.setBorderColor(Color.LIGHT_GRAY);
            descCell.setPadding(4);
            table.addCell(descCell);

            PdfPCell qtyCell = new PdfPCell(new Phrase(item.getQuantity().toString(), getFont(Font.NORMAL, 9)));
            qtyCell.setBorder(Rectangle.BOTTOM);
            qtyCell.setBorderColor(Color.LIGHT_GRAY);
            qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            qtyCell.setPadding(4);
            table.addCell(qtyCell);

            PdfPCell unitCell = new PdfPCell(new Phrase(formatCurrency(item.getUnitPrice()), getFont(Font.NORMAL, 9)));
            unitCell.setBorder(Rectangle.BOTTOM);
            unitCell.setBorderColor(Color.LIGHT_GRAY);
            unitCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            unitCell.setPadding(4);
            table.addCell(unitCell);

            PdfPCell totalCell = new PdfPCell(new Phrase(formatCurrency(item.getTotalAmount()), getFont(Font.BOLD, 9)));
            totalCell.setBorder(Rectangle.BOTTOM);
            totalCell.setBorderColor(Color.LIGHT_GRAY);
            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalCell.setPadding(4);
            table.addCell(totalCell);
        }

        return table;
    }

    private void addTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, getFont(Font.BOLD, 9, Color.WHITE)));
        cell.setBackgroundColor(new Color(0, 51, 102));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addTotals(Document document, Payslip payslip) throws DocumentException {
        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(60);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalsTable.setWidths(new float[]{3, 2});

        addTotalRow(totalsTable, "Total de Proventos:", payslip.getGrossAmount(), false);
        addTotalRow(totalsTable, "Total de Descontos:", payslip.getGrossAmount().subtract(payslip.getNetAmount()), false);
        addTotalRow(totalsTable, "LÍQUIDO A RECEBER:", payslip.getNetAmount(), true);

        document.add(totalsTable);
        document.add(new Paragraph(" "));
    }

    private void addTotalRow(PdfPTable table, String label, BigDecimal value, boolean highlight) {
        Font font = highlight ? getFont(Font.BOLD, 12, new Color(0, 51, 102)) : getFont(Font.BOLD, 10, Color.BLACK);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(4);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(formatCurrency(value), font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(4);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private void addFooter(Document document, Payslip payslip) throws DocumentException {
        Paragraph footer = new Paragraph();
        footer.add(new Chunk("Este documento foi gerado eletronicamente. ", getFont(Font.ITALIC, 8, Color.GRAY)));
        footer.add(new Chunk("Acesso: ", getFont(Font.ITALIC, 8, Color.GRAY)));
        footer.add(new Chunk(payslip.getAccessToken() != null ? "https://portal.empresa.com.br/holerite/" + payslip.getAccessToken() : "Não disponível",
            getFont(Font.ITALIC, 8, new Color(0, 51, 102))));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        if (digitalSignatureEnabled && payslip.getDigitalSignature() != null) {
            Paragraph sig = new Paragraph();
            sig.add(new Chunk("Assinatura Digital: ", getFont(Font.BOLD, 8, Color.GRAY)));
            sig.add(new Chunk(payslip.getDigitalSignature(), getFont(Font.NORMAL, 8, Color.GRAY)));
            sig.setAlignment(Element.ALIGN_CENTER);
            document.add(sig);
        }
    }

    private Font getFont(int style, float size, Color color) {
        return new Font(Font.HELVETICA, size, style, color);
    }

    private Font getFont(int style, float size) {
        return getFont(style, size, Color.BLACK);
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) return "R$ 0,00";
        return String.format(Locale.forLanguageTag("pt-BR"), "R$ %,.2f", value);
    }

    // Watermark page event
    private static class WatermarkPageEvent extends PdfPageEventHelper {
        private final String watermark;

        public WatermarkPageEvent(String watermark) {
            this.watermark = watermark;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte canvas = writer.getDirectContentUnder();
            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                new Phrase(watermark, new Font(Font.HELVETICA, 60, Font.BOLD, new Color(200, 200, 200, 100))),
                297.5f, 421, 45);
        }
    }
}