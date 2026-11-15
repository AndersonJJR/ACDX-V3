package com.erp.brazil.fiscal.application.port;

import com.erp.brazil.fiscal.domain.model.RetencaoFonte;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RetencaoCalculationPort {
    List<RetencaoFonte> calculate(
            String companyCnpj,
            String fornecedorCnpj,
            String fornecedorNome,
            String documentoTipo,
            String documentoNumero,
            String documentoSerie,
            String documentoChave,
            LocalDate dataEmissao,
            LocalDate dataPagamento,
            BigDecimal valorBruto,
            String cnaeCodigo,
            String descricaoServico,
            boolean optanteSimples
    );
}