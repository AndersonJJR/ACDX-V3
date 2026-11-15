package com.erp.brazil.fiscal.application.port;

import com.erp.brazil.fiscal.domain.model.NFE;
import com.erp.brazil.fiscal.domain.model.NFSE;
import com.erp.brazil.fiscal.domain.model.CTE;
import com.erp.brazil.fiscal.domain.model.SPEDBlock;
import com.erp.brazil.fiscal.domain.model.TaxCalculationResult;
import com.erp.brazil.fiscal.domain.model.RetencaoFonte;

public interface EventPublisherPort {
    void publishNFEEmitted(NFE nfe);

    void publishNFECancelled(NFE nfe);

    void publishNFSEEmitted(NFSE nfse);

    void publishNFSECancelled(NFSE nfse);

    void publishCTEEmitted(CTE cte);

    void publishCTECancelled(CTE cte);

    void publishSPEDGenerated(SPEDBlock spedBlock);

    void publishTaxCalculated(String companyCnpj, TaxCalculationResult result);

    void publishRetencaoCalculated(RetencaoFonte retencao);
}