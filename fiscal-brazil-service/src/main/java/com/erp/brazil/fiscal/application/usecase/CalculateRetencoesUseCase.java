package com.erp.brazil.fiscal.application.usecase;

import com.erp.brazil.fiscal.application.dto.EventDTO;
import com.erp.brazil.fiscal.application.dto.RetencaoDTO;
import com.erp.brazil.fiscal.domain.model.RetencaoFonte;
import com.erp.brazil.fiscal.domain.repository.RetencaoFonteRepository;
import com.erp.brazil.fiscal.infrastructure.messaging.EventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Use case for calculating source withholdings (retenções na fonte) on service invoices.
 * Calculates IRRF, CSLL, PIS, COFINS, INSS based on service type and provider regime.
 */
@Service
@Transactional
public class CalculateRetencoesUseCase {

    private final RetencaoFonteRepository retencaoRepository;
    private final EventPublisher eventPublisher;

    // Alíquotas padrão (configuráveis via properties/database)
    private static final BigDecimal IRRF_ALIQUOTA_PADRAO = new BigDecimal("1.50");
    private static final BigDecimal CSLL_ALIQUOTA = new BigDecimal("1.00");
    private static final BigDecimal PIS_ALIQUOTA = new BigDecimal("0.65");
    private static final BigDecimal COFINS_ALIQUOTA = new BigDecimal("3.00");
    private static final BigDecimal INSS_ALIQUOTA = new BigDecimal("11.00");

    // Códigos de serviço que não sofrem retenção
    private static final Set<String> SERVICO_ISENTO_IRRF = Set.of("0101", "0102", "0201", "0202", "0301");
    private static final Set<String> SERVICO_ISENTO_CSLL = Set.of("0101", "0102");
    private static final Set<String> SERVICO_ISENTO_PIS_COFINS = Set.of("0101", "0102", "0201");

    public CalculateRetencoesUseCase(RetencaoFonteRepository retencaoRepository, EventPublisher eventPublisher) {
        this.retencaoRepository = retencaoRepository;
        this.eventPublisher = eventPublisher;
    }

    public RetencaoDTO.CalculateResponse execute(RetencaoDTO.CalculateRequest request) {
        var invoice = request.invoice();
        var types = request.types() != null ? request.types() :
                List.of(RetencaoDTO.CalculateRequest.RetencaoType.values());

        BigDecimal baseValue = calculateBaseValue(invoice);
        List<RetencaoDTO.RetencaoItem> retencoes = new ArrayList<>();
        BigDecimal totalRetido = BigDecimal.ZERO;

        // Calculate each retention type
        for (var type : types) {
            var item = calculateRetencao(type, invoice, baseValue);
            if (item != null && item.calculatedValue().compareTo(BigDecimal.ZERO) > 0) {
                retencoes.add(item);
                totalRetido = totalRetido.add(item.calculatedValue());

                // Persist retention record
                persistRetencao(type, invoice, item);
            }
        }

        BigDecimal netValue = baseValue.subtract(totalRetido);

        var response = new RetencaoDTO.CalculateResponse(
                UUID.randomUUID().toString(),
                baseValue,
                retencoes,
                totalRetido.setScale(2, RoundingMode.HALF_UP),
                netValue.setScale(2, RoundingMode.HALF_UP),
                LocalDateTime.now(),
                Map.of("invoiceId", invoice.documentId(), "provider", invoice.providerCnpjCpf())
        );

        // Publish event
        publishEvent(response, invoice);

        return response;
    }

    private BigDecimal calculateBaseValue(RetencaoDTO.ServiceInvoice invoice) {
        BigDecimal base = invoice.grossValue();
        if (invoice.deductions() != null) {
            base = base.subtract(invoice.deductions());
        }
        return base.max(BigDecimal.ZERO);
    }

    private RetencaoDTO.RetencaoItem calculateRetencao(RetencaoDTO.CalculateRequest.RetencaoType type,
                                                        RetencaoDTO.ServiceInvoice invoice,
                                                        BigDecimal baseValue) {
        return switch (type) {
            case IRRF -> calculateIrrf(invoice, baseValue);
            case CSLL -> calculateCsll(invoice, baseValue);
            case PIS -> calculatePis(invoice, baseValue);
            case COFINS -> calculateCofins(invoice, baseValue);
            case INSS -> calculateInss(invoice, baseValue);
        };
    }

    private RetencaoDTO.RetencaoItem calculateIrrf(RetencaoDTO.ServiceInvoice invoice, BigDecimal baseValue) {
        // Verifica isenção por código de serviço
        if (SERVICO_ISENTO_IRRF.contains(invoice.serviceCode())) {
            return new RetencaoDTO.RetencaoItem("IRRF", baseValue, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, null, "Serviço isento de IRRF (código " + invoice.serviceCode() + ")");
        }

        // Verifica isenção por regime tributário
        if (invoice.isSimplesNacional() && "MEI".equals(invoice.taxRegime())) {
            return new RetencaoDTO.RetencaoItem("IRRF", baseValue, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, null, "MEI isento de IRRF");
        }

        BigDecimal aliquota = getIrrfAliquota(invoice.serviceCode(), invoice.providerState());
        BigDecimal calculated = baseValue.multiply(aliquota).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return new RetencaoDTO.RetencaoItem("IRRF", baseValue, aliquota, BigDecimal.ZERO,
                calculated.setScale(2, RoundingMode.HALF_UP),
                getIrrfNatureCode(invoice.serviceCode()), "IRRF sobre serviços");
    }

    private RetencaoDTO.RetencaoItem calculateCsll(RetencaoDTO.ServiceInvoice invoice, BigDecimal baseValue) {
        if (SERVICO_ISENTO_CSLL.contains(invoice.serviceCode())) {
            return new RetencaoDTO.RetencaoItem("CSLL", baseValue, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, null, "Serviço isento de CSLL");
        }

        if (invoice.isSimplesNacional()) {
            return new RetencaoDTO.RetencaoItem("CSLL", baseValue, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, null, "Simples Nacional isento de CSLL");
        }

        BigDecimal calculated = baseValue.multiply(CSLL_ALIQUOTA).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return new RetencaoDTO.RetencaoItem("CSLL", baseValue, CSLL_ALIQUOTA, BigDecimal.ZERO,
                calculated, null, "CSLL sobre serviços");
    }

    private RetencaoDTO.RetencaoItem calculatePis(RetencaoDTO.ServiceInvoice invoice, BigDecimal baseValue) {
        if (SERVICO_ISENTO_PIS_COFINS.contains(invoice.serviceCode())) {
            return new RetencaoDTO.RetencaoItem("PIS", baseValue, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, null, "Serviço isento de PIS");
        }

        if (invoice.isSimplesNacional()) {
            return new RetencaoDTO.RetencaoItem("PIS", baseValue, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, null, "Simples Nacional isento de PIS");
        }

        BigDecimal calculated = baseValue.multiply(PIS_ALIQUOTA).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return new RetencaoDTO.RetencaoItem("PIS", baseValue, PIS_ALIQUOTA, BigDecimal.ZERO,
                calculated, null, "PIS sobre serviços");
    }

    private RetencaoDTO.RetencaoItem calculateCofins(RetencaoDTO.ServiceInvoice invoice, BigDecimal baseValue) {
        if (SERVICO_ISENTO_PIS_COFINS.contains(invoice.serviceCode())) {
            return new RetencaoDTO.RetencaoItem("COFINS", baseValue, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, null, "Serviço isento de COFINS");
        }

        if (invoice.isSimplesNacional()) {
            return new RetencaoDTO.RetencaoItem("COFINS", baseValue, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, null, "Simples Nacional isento de COFINS");
        }

        BigDecimal calculated = baseValue.multiply(COFINS_ALIQUOTA).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return new RetencaoDTO.RetencaoItem("COFINS", baseValue, COFINS_ALIQUOTA, BigDecimal.ZERO,
                calculated, null, "COFINS sobre serviços");
    }

    private RetencaoDTO.RetencaoItem calculateInss(RetencaoDTO.ServiceInvoice invoice, BigDecimal baseValue) {
        // INSS retido apenas para cessão de mão de obra e empreitada
        if (!isCessaoMaoObra(invoice.serviceCode()) && !isEmpreitada(invoice.serviceCode())) {
            return new RetencaoDTO.RetencaoItem("INSS", baseValue, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, null, "Serviço não sujeito a retenção de INSS");
        }

        // Teto do INSS (atualizado anualmente)
        BigDecimal tetoInss = new BigDecimal("7507.49"); // Valor 2024
        BigDecimal baseInss = baseValue.min(tetoInss);

        BigDecimal calculated = baseInss.multiply(INSS_ALIQUOTA).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return new RetencaoDTO.RetencaoItem("INSS", baseInss, INSS_ALIQUOTA, BigDecimal.ZERO,
                calculated, getInssNatureCode(invoice.serviceCode()), "INSS sobre cessão de mão de obra/empreitada");
    }

    private BigDecimal getIrrfAliquota(String serviceCode, String providerState) {
        // Alíquotas variam por tipo de serviço e UF
        // Serviços profissionais: 1.5%
        // Serviços de construção: 4.8%
        // Transporte: 1%
        return IRRF_ALIQUOTA_PADRAO;
    }

    private String getIrrfNatureCode(String serviceCode) {
        return "IRRF-SERV-" + serviceCode;
    }

    private String getInssNatureCode(String serviceCode) {
        return isCessaoMaoObra(serviceCode) ? "INSS-CMO" : "INSS-EMP";
    }

    private boolean isCessaoMaoObra(String serviceCode) {
        return serviceCode.startsWith("07") || serviceCode.startsWith("08") || serviceCode.startsWith("09");
    }

    private boolean isEmpreitada(String serviceCode) {
        return serviceCode.startsWith("03") || serviceCode.startsWith("04") || serviceCode.startsWith("05");
    }

    private void persistRetencao(RetencaoDTO.CalculateRequest.RetencaoType type,
                                  RetencaoDTO.ServiceInvoice invoice,
                                  RetencaoDTO.RetencaoItem item) {
        RetencaoFonte retencao = switch (type) {
            case IRRF -> new RetencaoFonte.IRRF(
                    UUID.randomUUID().toString(),
                    invoice.documentId(), // companyId would be passed separately
                    invoice.documentId(),
                    invoice.documentType(),
                    invoice.competenceDate(),
                    invoice.providerCnpjCpf(),
                    invoice.providerName(),
                    item.baseValue(),
                    item.aliquota(),
                    item.calculatedValue(),
                    item.deducao() != null ? item.deducao() : BigDecimal.ZERO,
                    item.natureCode(),
                    LocalDateTime.now()
            );
            case CSLL -> new RetencaoFonte.CSLL(
                    UUID.randomUUID().toString(),
                    invoice.documentId(),
                    invoice.documentId(),
                    invoice.documentType(),
                    invoice.competenceDate(),
                    invoice.providerCnpjCpf(),
                    invoice.providerName(),
                    item.baseValue(),
                    item.aliquota(),
                    item.calculatedValue(),
                    LocalDateTime.now()
            );
            case PIS -> new RetencaoFonte.PIS(
                    UUID.randomUUID().toString(),
                    invoice.documentId(),
                    invoice.documentId(),
                    invoice.documentType(),
                    invoice.competenceDate(),
                    invoice.providerCnpjCpf(),
                    invoice.providerName(),
                    item.baseValue(),
                    item.aliquota(),
                    item.calculatedValue(),
                    LocalDateTime.now()
            );
            case COFINS -> new RetencaoFonte.COFINS(
                    UUID.randomUUID().toString(),
                    invoice.documentId(),
                    invoice.documentId(),
                    invoice.documentType(),
                    invoice.competenceDate(),
                    invoice.providerCnpjCpf(),
                    invoice.providerName(),
                    item.baseValue(),
                    item.aliquota(),
                    item.calculatedValue(),
                    LocalDateTime.now()
            );
            case INSS -> new RetencaoFonte.INSS(
                    UUID.randomUUID().toString(),
                    invoice.documentId(),
                    invoice.documentId(),
                    invoice.documentType(),
                    invoice.competenceDate(),
                    invoice.providerCnpjCpf(),
                    invoice.providerName(),
                    item.baseValue(),
                    item.aliquota(),
                    item.calculatedValue(),
                    item.natureCode(),
                    LocalDateTime.now()
            );
        };
        retencaoRepository.save(retencao);
    }

    private void publishEvent(RetencaoDTO.CalculateResponse response, RetencaoDTO.ServiceInvoice invoice) {
        var event = new EventDTO.RetencaoCalculatedEvent(
                UUID.randomUUID().toString(),
                response.calculationId(),
                invoice.documentId(), // companyId
                invoice.documentId(),
                invoice.documentType(),
                invoice.competenceDate(),
                invoice.providerCnpjCpf(),
                response.totalRetido(),
                response.netValue(),
                response.retencoes().stream()
                        .map(r -> new EventDTO.RetencaoCalculatedEvent.RetencaoItem(
                                r.type(), r.baseValue(), r.aliquota(), r.calculatedValue()
                        ))
                        .toList(),
                LocalDateTime.now()
        );
        eventPublisher.publish(event);
    }
}