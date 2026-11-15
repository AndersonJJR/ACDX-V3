package com.erp.brazil.fiscal.application.port;

import com.erp.brazil.fiscal.domain.model.NFE;

import java.util.Optional;

public interface NFEPort {
    NFE save(NFE nfe);

    Optional<NFE> findById(String id);

    Optional<NFE> findByChaveAcesso(String chaveAcesso);
}