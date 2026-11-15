package com.erp.brazil.fiscal.application.usecase;

import com.erp.brazil.fiscal.domain.model.NFE;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.ValidationEventHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Use case for validating NF-e XML against XSD schemas and business rules.
 */
@Service
public class ValidateNFEUseCase {

    private static final String NFE_XSD_PATH = "classpath:xsd/nfe/v4.00/";
    private Schema nfeSchema;
    private JAXBContext jaxbContext;

    public ValidateNFEUseCase() {
        initializeSchema();
        initializeJaxbContext();
    }

    private void initializeSchema() {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            // Load main NF-e schema
            var schemaFile = ResourceUtils.getFile(NFE_XSD_PATH + "nfe_v4.00.xsd");
            nfeSchema = factory.newSchema(schemaFile);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load NF-e XSD schema", e);
        }
    }

    private void initializeJaxbContext() {
        try {
            jaxbContext = JAXBContext.newInstance(NFE.class);
        } catch (JAXBException e) {
            throw new IllegalStateException("Failed to initialize JAXB context for NF-e", e);
        }
    }

    public ValidationResult validate(String xmlContent) {
        var result = new ValidationResult();
        result.setXmlContent(xmlContent);

        // 1. XSD Schema Validation
        validateXsd(xmlContent, result);

        // 2. Business Rules Validation
        if (result.isValid()) {
            validateBusinessRules(xmlContent, result);
        }

        // 3. Digital Signature Validation (placeholder)
        if (result.isValid()) {
            validateSignature(xmlContent, result);
        }

        return result;
    }

    public ValidationResult validate(NFE nfe) {
        try {
            var marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8");

            var stringWriter = new StringWriter();
            marshaller.marshal(nfe, stringWriter);
            return validate(stringWriter.toString());
        } catch (JAXBException e) {
            var result = new ValidationResult();
            result.setValid(false);
            result.addError("JAXB_MARSHAL_ERROR", "Failed to marshal NF-e: " + e.getMessage());
            return result;
        }
    }

    private void validateXsd(String xmlContent, ValidationResult result) {
        try {
            Validator validator = nfeSchema.newValidator();
            validator.setErrorHandler(new ValidationErrorHandler(result));
            validator.validate(new StreamSource(new StringReader(xmlContent)));
        } catch (SAXException | IOException e) {
            result.setValid(false);
            result.addError("XSD_VALIDATION_ERROR", e.getMessage());
        }
    }

    private void validateBusinessRules(String xmlContent, ValidationResult result) {
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setEventHandler(new ValidationEventHandler() {
                @Override
                public boolean handleEvent(ValidationEvent event) {
                    result.addError("BUSINESS_RULE_" + event.getSeverity(),
                            event.getMessage() + " at line " + event.getLocator().getLineNumber());
                    return true;
                }
            });

            NFE nfe = (NFE) unmarshaller.unmarshal(new StringReader(xmlContent));
            validateNfeBusinessRules(nfe, result);
        } catch (JAXBException e) {
            result.setValid(false);
            result.addError("UNMARSHAL_ERROR", "Failed to unmarshal NF-e: " + e.getMessage());
        }
    }

    private void validateNfeBusinessRules(NFE nfe, ValidationResult result) {
        // Validate required fields
        if (nfe.accessKey() == null || nfe.accessKey().isBlank()) {
            result.addError("MISSING_ACCESS_KEY", "Access key is required");
        }

        if (nfe.companyId() == null || nfe.companyId().isBlank()) {
            result.addError("MISSING_COMPANY_ID", "Company ID is required");
        }

        if (nfe.emitter() == null || nfe.emitter().cnpj() == null || nfe.emitter().cnpj().isBlank()) {
            result.addError("MISSING_EMITTER_CNPJ", "Emitter CNPJ is required");
        }

        if (nfe.recipient() == null || nfe.recipient().cnpjCpf() == null || nfe.recipient().cnpjCpf().isBlank()) {
            result.addError("MISSING_RECIPIENT_CNPJ_CPF", "Recipient CNPJ/CPF is required");
        }

        if (nfe.items() == null || nfe.items().isEmpty()) {
            result.addError("NO_ITEMS", "NF-e must have at least one item");
        } else {
            for (int i = 0; i < nfe.items().size(); i++) {
                var item = nfe.items().get(i);
                if (item.ncm() == null || item.ncm().isBlank()) {
                    result.addError("MISSING_NCM_ITEM_" + (i + 1), "NCM is required for item " + (i + 1));
                }
                if (item.cfop() == null || item.cfop().isBlank()) {
                    result.addError("MISSING_CFOP_ITEM_" + (i + 1), "CFOP is required for item " + (i + 1));
                }
                if (item.vProd() == null || item.vProd().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    result.addError("INVALID_VPROD_ITEM_" + (i + 1), "Product value must be positive for item " + (i + 1));
                }
            }
        }

        // Validate totals consistency
        if (nfe.totals() != null) {
            validateTotals(nfe, result);
        }

        // Validate ICMS for each item
        if (nfe.items() != null) {
            for (int i = 0; i < nfe.items().size(); i++) {
                var item = nfe.items().get(i);
                if (item.icms() != null) {
                    validateIcms(item.icms(), i + 1, result);
                }
            }
        }
    }

    private void validateTotals(NFE nfe, ValidationResult result) {
        var totals = nfe.totals();
        var items = nfe.items();

        if (items != null) {
            var calcVProd = items.stream()
                    .map(NFE.Item::vProd)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

            if (totals.vProd() != null && totals.vProd().subtract(calcVProd).abs().compareTo(new java.math.BigDecimal("0.01")) > 0) {
                result.addWarning("TOTAL_VPROD_MISMATCH", "Calculated vProd differs from declared total");
            }
        }

        if (totals.vNF() != null) {
            var calcVNF = (totals.vProd() != null ? totals.vProd() : java.math.BigDecimal.ZERO)
                    .add(totals.vFrete() != null ? totals.vFrete() : java.math.BigDecimal.ZERO)
                    .add(totals.vSeg() != null ? totals.vSeg() : java.math.BigDecimal.ZERO)
                    .add(totals.vDesc() != null ? totals.vDesc() : java.math.BigDecimal.ZERO)
                    .add(totals.vII() != null ? totals.vII() : java.math.BigDecimal.ZERO)
                    .add(totals.vIPI() != null ? totals.vIPI() : java.math.BigDecimal.ZERO)
                    .add(totals.vPIS() != null ? totals.vPIS() : java.math.BigDecimal.ZERO)
                    .add(totals.vCOFINS() != null ? totals.vCOFINS() : java.math.BigDecimal.ZERO)
                    .add(totals.vOutro() != null ? totals.vOutro() : java.math.BigDecimal.ZERO);

            if (totals.vNF().subtract(calcVNF).abs().compareTo(new java.math.BigDecimal("0.01")) > 0) {
                result.addWarning("TOTAL_VNF_MISMATCH", "Calculated vNF differs from declared total");
            }
        }
    }

    private void validateIcms(NFE.ICMS icms, int itemNumber, ValidationResult result) {
        if (icms.cst() == null && icms.csosn() == null) {
            result.addError("MISSING_CST_CSOSN_ITEM_" + itemNumber, "CST or CSOSN is required for ICMS");
        }

        if (icms.cst() != null && !isValidCst(icms.cst())) {
            result.addError("INVALID_CST_ITEM_" + itemNumber, "Invalid CST: " + icms.cst());
        }

        if (icms.csosn() != null && !isValidCsosn(icms.csosn())) {
            result.addError("INVALID_CSOSN_ITEM_" + itemNumber, "Invalid CSOSN: " + icms.csosn());
        }
    }

    private boolean isValidCst(String cst) {
        return switch (cst) {
            case "00", "10", "20", "30", "40", "41", "50", "51", "60", "70", "90" -> true;
            default -> false;
        };
    }

    private boolean isValidCsosn(String csosn) {
        return switch (csosn) {
            case "101", "102", "103", "201", "202", "203", "300", "400", "500", "900" -> true;
            default -> false;
        };
    }

    private void validateSignature(String xmlContent, ValidationResult result) {
        // Placeholder for XML-DSig validation using Bouncy Castle
        // In production, implement proper signature verification
        if (!xmlContent.contains("<Signature")) {
            result.addWarning("MISSING_SIGNATURE", "XML does not contain digital signature");
        }
    }

    public static class ValidationResult {
        private boolean valid = true;
        private String xmlContent;
        private final List<ValidationIssue> errors = new ArrayList<>();
        private final List<ValidationIssue> warnings = new ArrayList<>();

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public String getXmlContent() { return xmlContent; }
        public void setXmlContent(String xmlContent) { this.xmlContent = xmlContent; }
        public List<ValidationIssue> getErrors() { return errors; }
        public List<ValidationIssue> getWarnings() { return warnings; }

        public void addError(String code, String message) {
            errors.add(new ValidationIssue(code, message, ValidationSeverity.ERROR));
            valid = false;
        }

        public void addWarning(String code, String message) {
            warnings.add(new ValidationIssue(code, message, ValidationSeverity.WARNING));
        }

        public record ValidationIssue(String code, String message, ValidationSeverity severity) {}
        public enum ValidationSeverity { ERROR, WARNING }
    }

    private static class ValidationErrorHandler implements org.xml.sax.ErrorHandler {
        private final ValidationResult result;

        public ValidationErrorHandler(ValidationResult result) {
            this.result = result;
        }

        @Override
        public void warning(SAXParseException exception) {
            result.addWarning("XSD_WARNING", exception.getMessage() + " at line " + exception.getLineNumber());
        }

        @Override
        public void error(SAXParseException exception) {
            result.addError("XSD_ERROR", exception.getMessage() + " at line " + exception.getLineNumber());
        }

        @Override
        public void fatalError(SAXParseException exception) {
            result.addError("XSD_FATAL", exception.getMessage() + " at line " + exception.getLineNumber());
        }
    }
}