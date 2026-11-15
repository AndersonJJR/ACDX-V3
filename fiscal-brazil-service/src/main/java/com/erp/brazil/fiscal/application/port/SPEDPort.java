package com.erp.brazil.fiscal.application.port;

import com.erp.brazil.fiscal.domain.model.SPEDBlock;
import com.erp.brazil.fiscal.domain.model.SPEDType;

import java.util.List;

public interface SPEDPort {
    SPEDBlock save(SPEDBlock block);

    List<SPEDBlock> findByCompanyCnpjAndReferenceMonthAndSpedType(String companyCnpj, String referenceMonth, SPEDType spedType);

    List<SPEDBlock> findByCompanyCnpjAndSpedTypeOrderByReferenceMonthDesc(String companyCnpj, SPEDType spedType);

    void deleteByCompanyCnpjAndSpedTypeAndReferenceMonth(String companyCnpj, SPEDType spedType, String referenceMonth);
}