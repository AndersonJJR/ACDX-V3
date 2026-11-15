package com.erp.brazil.fiscal.application.port;

import com.erp.brazil.fiscal.domain.model.SPEDBlock;
import com.erp.brazil.fiscal.domain.model.SPEDType;

import java.util.List;

public interface SPEDGenerationPort {
    List<SPEDBlock> generate(String companyCnpj, SPEDType spedType, String referenceMonth, int year);

    ValidationResult validate(SPEDBlock block);

    void transmit(SPEDBlock block);

    record ValidationResult(
            boolean valid,
            List<String> errors
    ) {
        public static ValidationResult valid() {
            return new ValidationResult(true, List.of());
        }

        public static ValidationResult invalid(List<String> errors) {
            return new ValidationResult(false, errors);
        }
    }
}