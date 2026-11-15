package com.erp.brazil.fiscal.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Sealed interface for source withholding (retenção na fonte).
 */
public sealed interface RetencaoFonte permits RetencaoFonte.IRRF, RetencaoFonte.CSLL, RetencaoFonte.PIS, RetencaoFonte.COFINS, RetencaoFonte.INSS {

    String getId();
    String getCompanyId();
    String getDocumentId();
    String getDocumentType();
    LocalDate getCompetenceDate();
    String getProviderCnpjCpf();
    String getProviderName();
    BigDecimal getBaseValue();
    BigDecimal getCalculatedValue();
    LocalDateTime getCalculatedAt();

    record IRRF(
            String id,
            String companyId,
            String documentId,
            String documentType,
            LocalDate competenceDate,
            String providerCnpjCpf,
            String providerName,
            BigDecimal baseValue,
            BigDecimal aliquota,
            BigDecimal calculatedValue,
            BigDecimal deducao,
            String natureCode,
            LocalDateTime calculatedAt
    ) implements RetencaoFonte {}

    record CSLL(
            String id,
            String companyId,
            String documentId,
            String documentType,
            LocalDate competenceDate,
            String providerCnpjCpf,
            String providerName,
            BigDecimal baseValue,
            BigDecimal aliquota,
            BigDecimal calculatedValue,
            LocalDateTime calculatedAt
    ) implements RetencaoFonte {}

    record PIS(
            String id,
            String companyId,
            String documentId,
            String documentType,
            LocalDate competenceDate,
            String providerCnpjCpf,
            String providerName,
            BigDecimal baseValue,
            BigDecimal aliquota,
            BigDecimal calculatedValue,
            LocalDateTime calculatedAt
    ) implements RetencaoFonte {}

    record COFINS(
            String id,
            String companyId,
            String documentId,
            String documentType,
            LocalDate competenceDate,
            String providerCnpjCpf,
            String providerName,
            BigDecimal baseValue,
            BigDecimal aliquota,
            BigDecimal calculatedValue,
            LocalDateTime calculatedAt
    ) implements RetencaoFonte {}

    record INSS(
            String id,
            String companyId,
            String documentId,
            String documentType,
            LocalDate competenceDate,
            String providerCnpjCpf,
            String providerName,
            BigDecimal baseValue,
            BigDecimal aliquota,
            BigDecimal calculatedValue,
            String contributionCode,
            LocalDateTime calculatedAt
    ) implements RetencaoFonte {}

    public enum RetencaoType {
        IRRF, CSLL, PIS, COFINS, INSS
    }
}