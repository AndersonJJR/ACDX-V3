package com.erp.brazil.fiscal.application.usecase;

import com.erp.brazil.fiscal.domain.model.*;
import com.erp.brazil.fiscal.domain.repository.TaxRuleRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Use case for calculating taxes using rule chain pattern.
 * Supports ICMS, IPI, PIS, COFINS, ISS with caching.
 */
@Service
@Transactional(readOnly = true)
public class CalculateTaxesUseCase {

    private final TaxRuleRepository taxRuleRepository;
    private final TaxRuleCache taxRuleCache;

    public CalculateTaxesUseCase(TaxRuleRepository taxRuleRepository, TaxRuleCache taxRuleCache) {
        this.taxRuleRepository = taxRuleRepository;
        this.taxRuleCache = taxRuleCache;
    }

    public TaxCalculationResult execute(TaxCalculationContext context) {
        var rules = getApplicableRules(context);
        return calculateWithRules(context, rules);
    }

    public TaxCalculationResult executeForInvoice(TaxCalculationContext context, String taxRegime) {
        var ctx = new TaxCalculationContext(
                context.companyId(),
                context.originState(),
                context.destinationState(),
                context.ncm(),
                context.cest(),
                context.cfop(),
                context.amount(),
                context.quantity(),
                context.operationType(),
                taxRegime,
                context.municipalityCode(),
                context.cnaeCode()
        );
        return execute(ctx);
    }

    @Cacheable(value = "tax-rules", key = "#context.ncm + ':' + #context.originState + ':' + #context.destinationState + ':' + #context.cfop + ':' + (#context.taxRegime ?: 'ALL')")
    protected List<TaxRule> getApplicableRules(TaxCalculationContext context) {
        var date = LocalDate.now();
        var allRules = new ArrayList<TaxRule>();

        // ICMS rules
        allRules.addAll(taxRuleRepository.findApplicableRules(
                "ICMS", date, context.ncm(), context.cest(), context.cfop(),
                context.originState(), context.destinationState(), context.taxRegime()
        ));

        // IPI rules
        allRules.addAll(taxRuleRepository.findApplicableRules(
                "IPI", date, context.ncm(), context.cest(), context.cfop(),
                context.originState(), context.destinationState(), context.taxRegime()
        ));

        // PIS rules
        allRules.addAll(taxRuleRepository.findApplicableRules(
                "PIS", date, context.ncm(), context.cest(), context.cfop(),
                context.originState(), context.destinationState(), context.taxRegime()
        ));

        // COFINS rules
        allRules.addAll(taxRuleRepository.findApplicableRules(
                "COFINS", date, context.ncm(), context.cest(), context.cfop(),
                context.originState(), context.destinationState(), context.taxRegime()
        ));

        // ISS rules (for services)
        if (context.municipalityCode() != null) {
            allRules.addAll(taxRuleRepository.findApplicableRules(
                    "ISS", date, context.ncm(), context.cest(), context.cfop(),
                    context.originState(), context.destinationState(), context.taxRegime()
            ));
        }

        return allRules.stream()
                .sorted(Comparator.comparingInt(TaxRule::getPriority).reversed())
                .toList();
    }

    private TaxCalculationResult calculateWithRules(TaxCalculationContext context, List<TaxRule> rules) {
        var baseCalculations = new ArrayList<TaxCalculationResult.BaseCalculation>();
        BigDecimal icms = BigDecimal.ZERO;
        BigDecimal icmsSt = BigDecimal.ZERO;
        BigDecimal fcp = BigDecimal.ZERO;
        BigDecimal ipi = BigDecimal.ZERO;
        BigDecimal pis = BigDecimal.ZERO;
        BigDecimal cofins = BigDecimal.ZERO;
        BigDecimal iss = BigDecimal.ZERO;
        BigDecimal irpj = BigDecimal.ZERO;
        BigDecimal csll = BigDecimal.ZERO;
        BigDecimal inss = BigDecimal.ZERO;

        for (TaxRule rule : rules) {
            try {
                BigDecimal calculated = rule.calculate(context.amount(), context);
                if (calculated == null || calculated.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                var baseCalc = new TaxCalculationResult.BaseCalculation(
                        rule.getTaxType(),
                        context.amount(),
                        getAliquota(rule),
                        getReduction(rule),
                        calculated,
                        getCst(rule),
                        rule.getName()
                );
                baseCalculations.add(baseCalc);

                // Accumulate by tax type using pattern matching
                switch (rule) {
                    case ICMSRule icmsRule -> {
                        icms = icms.add(calculated);
                        if (icmsRule.isSt()) {
                            icmsSt = icmsSt.add(calculated);
                        }
                        if (icmsRule.isFcp()) {
                            fcp = fcp.add(calculated);
                        }
                    }
                    case IPIRule ipiRule -> ipi = ipi.add(calculated);
                    case PISCOFINSRule pisCofinsRule -> {
                        if (pisCofinsRule.isPis()) {
                            pis = pis.add(calculated);
                        } else {
                            cofins = cofins.add(calculated);
                        }
                    }
                    case ISSRule issRule -> iss = iss.add(calculated);
                    default -> {}
                }
            } catch (Exception e) {
                // Log error but continue with other rules
                // In production, use proper logging framework
                System.err.println("Error calculating tax rule " + rule.getId() + ": " + e.getMessage());
            }
        }

        BigDecimal total = icms.add(icmsSt).add(fcp).add(ipi).add(pis).add(cofins).add(iss).add(irpj).add(csll).add(inss);

        return new TaxCalculationResult(
                icms, icmsSt, fcp, ipi, pis, cofins, iss, irpj, csll, inss,
                total.setScale(2, RoundingMode.HALF_UP),
                baseCalculations,
                Map.of("rulesApplied", rules.size(), "context", context)
        );
    }

    private BigDecimal getAliquota(TaxRule rule) {
        return switch (rule) {
            case ICMSRule r -> r.aliquota();
            case IPIRule r -> r.aliquota();
            case PISCOFINSRule r -> r.aliquota();
            case ISSRule r -> r.aliquota();
            default -> BigDecimal.ZERO;
        };
    }

    private BigDecimal getReduction(TaxRule rule) {
        return switch (rule) {
            case ICMSRule r -> r.reducaoBc();
            default -> BigDecimal.ZERO;
        };
    }

    private String getCst(TaxRule rule) {
        return switch (rule) {
            case ICMSRule r -> r.cst() != null ? r.cst() : r.csosn();
            case IPIRule r -> r.cst();
            case PISCOFINSRule r -> r.cst();
            case ISSRule r -> r.cst();
            default -> null;
        };
    }

    // Cache abstraction for Redis
    public interface TaxRuleCache {
        List<TaxRule> get(String key);
        void put(String key, List<TaxRule> rules);
        void evict(String key);
    }
}