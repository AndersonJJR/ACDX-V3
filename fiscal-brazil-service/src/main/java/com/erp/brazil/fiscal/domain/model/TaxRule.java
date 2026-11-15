package com.erp.brazil.fiscal.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Sealed interface representing a tax calculation rule.
 * Implementations: ICMSRule, IPIRule, PISCOFINSRule, ISSRule
 */
public sealed interface TaxRule permits ICMSRule, IPIRule, PISCOFINSRule, ISSRule {

    String getId();
    String getTaxType();
    String getName();
    String getDescription();
    String getUfOrigin();
    String getUfDestination();
    String getNcmCode();
    String getCestCode();
    String getCfopCode();
    String getTaxRegime();
    LocalDate getEffectiveFrom();
    LocalDate getEffectiveTo();
    int getPriority();
    boolean isActive();
    String getVersion();
    java.time.LocalDateTime getLastSync();

    BigDecimal calculate(BigDecimal baseValue, TaxCalculationContext context);
}