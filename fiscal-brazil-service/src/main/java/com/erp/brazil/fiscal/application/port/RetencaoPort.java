package com.erp.brazil.fiscal.application.port;

import com.erp.brazil.fiscal.domain.model.RetencaoFonte;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RetencaoPort {
    RetencaoFonte save(RetencaoFonte retencao);

    List<RetencaoFonte> findByCompanyCnpjAndDataPagamentoBetween(String companyCnpj, LocalDate start, LocalDate end);

    List<RetencaoFonte> findByCompanyCnpjAndTipoAndDataPagamentoBetween(String companyCnpj, com.erp.brazil.fiscal.domain.model.RetencaoTipo tipo, LocalDate start, LocalDate end);

    BigDecimal sumValorRetidoByCompanyAndTipoAndPeriod(String companyCnpj, com.erp.brazil.fiscal.domain.model.RetencaoTipo tipo, LocalDate start, LocalDate end);

    List<Object[]> sumValorRetidoByTipoAndPeriod(String companyCnpj, LocalDate start, LocalDate end);
}