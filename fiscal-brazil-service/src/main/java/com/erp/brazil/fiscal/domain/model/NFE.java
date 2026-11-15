package com.erp.brazil.fiscal.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * NF-e (Nota Fiscal Eletrônica) - Electronic Invoice for goods.
 */
public record NFE(
        String id,
        String companyId,
        String accessKey,
        String protocol,
        String status,
        NFEType type,
        String series,
        String number,
        LocalDateTime emissionDate,
        LocalDateTime saEntradaDate,
        Emitter emitter,
        Recipient recipient,
        List<Item> items,
        Totals totals,
        Transport transport,
        Payment payment,
        String additionalInfo,
        String xmlContent,
        String pdfContent,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime cancelledAt,
        String cancellationReason
) {
    public enum NFEType {
        ENTRADA, SAIDA
    }

    public record Emitter(
            String cnpj,
            String ie,
            String iest,
            String im,
            String cnae,
            String crt,
            String companyName,
            String tradeName,
            Address address
    ) {}

    public record Recipient(
            String cnpjCpf,
            String ie,
            String isuf,
            String im,
            String cnae,
            String email,
            String companyName,
            String tradeName,
            Address address,
            String foreignCountryCode,
            String foreignCountryName
    ) {}

    public record Address(
            String street,
            String number,
            String complement,
            String neighborhood,
            String cityCode,
            String cityName,
            String uf,
            String cep,
            String countryCode,
            String countryName,
            String phone
    ) {}

    public record Item(
            int itemNumber,
            String ncm,
            String cest,
            String cfop,
            String ucom,
            String ucomDesc,
            BigDecimal qCom,
            BigDecimal vUnCom,
            BigDecimal vProd,
            String cEan,
            String cEanTrib,
            String uTrib,
            String uTribDesc,
            BigDecimal qTrib,
            BigDecimal vUnTrib,
            BigDecimal vFrete,
            BigDecimal vSeg,
            BigDecimal vDesc,
            BigDecimal vOutro,
            ICMS icms,
            IPI ipi,
            PIS pis,
            COFINS cofins,
            ISS iss
    ) {}

    public record Totals(
            BigDecimal vBC,
            BigDecimal vICMS,
            BigDecimal vICMSDeson,
            BigDecimal vFCP,
            BigDecimal vBCST,
            BigDecimal vST,
            BigDecimal vFCPST,
            BigDecimal vFCPSTRet,
            BigDecimal vProd,
            BigDecimal vFrete,
            BigDecimal vSeg,
            BigDecimal vDesc,
            BigDecimal vII,
            BigDecimal vIPI,
            BigDecimal vIPIDevol,
            BigDecimal vPIS,
            BigDecimal vCOFINS,
            BigDecimal vOutro,
            BigDecimal vNF,
            BigDecimal vTotTrib,
            BigDecimal vServ,
            BigDecimal vBCServ,
            BigDecimal vISS,
            BigDecimal vISSRet
    ) {}

    public record Transport(
            String modFrete,
            String transportaCnpjCpf,
            String transportaNome,
            String transportaIE,
            String transportaEnder,
            String transportaMun,
            String transportaUF,
            String veicPlaca,
            String veicUF,
            String veicRNTC,
            String reboquePlaca,
            String reboqueUF,
            String reboqueRNTC,
            Volume volume
    ) {}

    public record Volume(
            String qVol,
            String esp,
            String marca,
            String nVol,
            BigDecimal pesoL,
            BigDecimal pesoB
    ) {}

    public record Payment(
            String indPag,
            List<PaymentDetail> details
    ) {}

    public record PaymentDetail(
            String tPag,
            BigDecimal vPag,
            String cnpj,
            String tBand,
            String cAut,
            String tpIntegra
    ) {}

    public record ICMS(
            String orig,
            String cst,
            String csosn,
            BigDecimal vBC,
            BigDecimal pICMS,
            BigDecimal vICMS,
            BigDecimal vBCST,
            BigDecimal pST,
            BigDecimal vICMSST,
            BigDecimal vFCP,
            BigDecimal pFCP,
            BigDecimal vFCPST,
            BigDecimal pFCPST,
            BigDecimal pRedBC,
            BigDecimal pRedBCST,
            BigDecimal vBCFCP,
            BigDecimal pFCPSTRet,
            BigDecimal vFCPSTRet
    ) {}

    public record IPI(
            String clEnq,
            String cnpjProd,
            String cSelo,
            String qSelo,
            String cEnq,
            String cst,
            BigDecimal vBC,
            BigDecimal pIPI,
            BigDecimal qUnid,
            BigDecimal vUnid,
            BigDecimal vIPI
    ) {}

    public record PIS(
            String cst,
            BigDecimal vBC,
            BigDecimal pPIS,
            BigDecimal vPIS,
            BigDecimal qBCProd,
            BigDecimal vAliqProd
    ) {}

    public record COFINS(
            String cst,
            BigDecimal vBC,
            BigDecimal pCOFINS,
            BigDecimal vCOFINS,
            BigDecimal qBCProd,
            BigDecimal vAliqProd
    ) {}

    public record ISS(
            String cst,
            BigDecimal vBC,
            BigDecimal pISS,
            BigDecimal vISS,
            String municipio,
            String cMunFG
    ) {}
}