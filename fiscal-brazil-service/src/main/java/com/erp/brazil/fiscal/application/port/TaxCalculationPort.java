package com.erp.brazil.fiscal.application.port;

import com.erp.brazil.fiscal.domain.model.TaxCalculationContext;
import com.erp.brazil.fiscal.domain.model.TaxCalculationResult;

public interface TaxCalculationPort {
    TaxCalculationResult calculate(TaxCalculationContext context);
}