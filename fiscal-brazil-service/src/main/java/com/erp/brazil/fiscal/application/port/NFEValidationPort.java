package com.erp.brazil.fiscal.application.port;

import com.erp.brazil.fiscal.domain.model.NFE;
import com.erp.brazil.fiscal.application.usecase.ValidateNFEUseCase;

public interface NFEValidationPort {
    ValidateNFEUseCase.NFEValidationResult validate(NFE nfe);
}