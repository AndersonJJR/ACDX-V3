package com.erp.brazil.sales.application.usecase;

import com.erp.brazil.sales.domain.model.*;
import com.erp.brazil.sales.domain.repository.*;
import com.erp.brazil.sales.infrastructure.event.CommissionCalculatedEvent;
import com.erp.brazil.sales.infrastructure.event.EventPublisher;
import com.erp.brazil.sales.presentation.dto.request.CalculateCommissionRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CalculateCommissionUseCase {

    private final CommissionRuleRepository commissionRuleRepository;
    private final CommissionTierRepository commissionTierRepository;
    private final CommissionPaymentRepository commissionPaymentRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesPersonRepository salesPersonRepository;
    private final EventPublisher eventPublisher;

    public CalculateCommissionUseCase(CommissionRuleRepository commissionRuleRepository,
                                      CommissionTierRepository commissionTierRepository,
                                      CommissionPaymentRepository commissionPaymentRepository,
                                      SalesInvoiceRepository salesInvoiceRepository,
                                      SalesOrderRepository salesOrderRepository,
                                      SalesPersonRepository salesPersonRepository,
                                      EventPublisher eventPublisher) {
        this.commissionRuleRepository = commissionRuleRepository;
        this.commissionTierRepository = commissionTierRepository;
        this.commissionPaymentRepository = commissionPaymentRepository;
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.salesPersonRepository = salesPersonRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void execute(CalculateCommissionRequest request) {
        List<SalesPerson> salesPersons = salesPersonRepository.findByCompanyIdAndIsActiveTrue(request.companyId());

        for (SalesPerson salesPerson : salesPersons) {
            calculateForSalesPerson(salesPerson, request.periodStart(), request.periodEnd(), request.calculatedById());
        }
    }

    @Transactional
    public void calculateForSalesPerson(SalesPerson salesPerson, LocalDate periodStart, LocalDate periodEnd, UUID calculatedById) {
        UUID companyId = salesPerson.getCompanyId();
        UUID salesPersonId = salesPerson.getId();
        UUID salesTeamId = salesPerson.getSalesTeam() != null ? salesPerson.getSalesTeam().getId() : null;
        UUID territoryId = salesPerson.getTerritory() != null ? salesPerson.getTerritory().getId() : null;

        List<CommissionRule> rules = commissionRuleRepository.findApplicableRulesForSalesPerson(
                companyId, periodEnd, salesPersonId, salesTeamId, territoryId);

        if (rules.isEmpty()) {
            return;
        }

        List<InvoiceStatus> invoicedStatuses = List.of(InvoiceStatus.AUTHORIZED);
        List<SalesInvoice> invoices = salesInvoiceRepository.findByCompanyIdAndStatusIn(companyId, invoicedStatuses)
                .stream()
                .filter(inv -> inv.getSalesOrder() != null && inv.getSalesOrder().getSalesPerson() != null 
                        && inv.getSalesOrder().getSalesPerson().getId().equals(salesPersonId))
                .filter(inv -> !inv.getIssueDate().isBefore(periodStart) && !inv.getIssueDate().isAfter(periodEnd))
                .toList();

        List<SalesOrderStatus> deliveredStatuses = List.of(SalesOrderStatus.DELIVERED, SalesOrderStatus.INVOICED);
        List<SalesOrder> orders = salesOrderRepository.findByCompanyIdAndStatusIn(companyId, deliveredStatuses)
                .stream()
                .filter(so -> so.getSalesPerson() != null && so.getSalesPerson().getId().equals(salesPersonId))
                .filter(so -> so.getConfirmedAt() != null && 
                    !so.getConfirmedAt().toLocalDate().isBefore(periodStart) && 
                    !so.getConfirmedAt().toLocalDate().isAfter(periodEnd))
                .toList();

        for (CommissionRule rule : rules) {
            BigDecimal totalCommission = BigDecimal.ZERO;

            if (rule.getType() == CommissionRule.RuleType.PERCENTAGE) {
                BigDecimal baseAmount = invoices.stream()
                        .map(SalesInvoice::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                baseAmount = baseAmount.add(orders.stream()
                        .map(SalesOrder::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

                if (rule.getMinAmount() != null && baseAmount.compareTo(rule.getMinAmount()) < 0) {
                    continue;
                }
                if (rule.getMaxAmount() != null && baseAmount.compareTo(rule.getMaxAmount()) > 0) {
                    baseAmount = rule.getMaxAmount();
                }

                totalCommission = baseAmount.multiply(rule.getValue()).divide(BigDecimal.valueOf(100));

            } else if (rule.getType() == CommissionRule.RuleType.FIXED) {
                totalCommission = rule.getValue();

            } else if (rule.getType() == CommissionRule.RuleType.TIERED) {
                List<CommissionTier> tiers = commissionTierRepository.findByCommissionRuleIdOrderByMinAmountAsc(rule.getId());
                BigDecimal baseAmount = invoices.stream()
                        .map(SalesInvoice::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                for (CommissionTier tier : tiers) {
                    BigDecimal tierMin = tier.getMinAmount() != null ? tier.getMinAmount() : BigDecimal.ZERO;
                    BigDecimal tierMax = tier.getMaxAmount() != null ? tier.getMaxAmount() : baseAmount;

                    if (baseAmount.compareTo(tierMin) > 0) {
                        BigDecimal taxableInTier = baseAmount.min(tierMax).subtract(tierMin);
                        if (taxableInTier.compareTo(BigDecimal.ZERO) > 0) {
                            if (tier.getRate() != null) {
                                totalCommission = totalCommission.add(taxableInTier.multiply(tier.getRate()).divide(BigDecimal.valueOf(100)));
                            }
                            if (tier.getFixedAmount() != null) {
                                totalCommission = totalCommission.add(tier.getFixedAmount());
                            }
                        }
                    }
                }
            }

            if (totalCommission.compareTo(BigDecimal.ZERO) > 0) {
                boolean exists = commissionPaymentRepository.existsBySalesPersonIdAndPeriodStartAndPeriodEndAndCommissionRuleId(
                        salesPersonId, periodStart, periodEnd, rule.getId());

                if (!exists) {
                    CommissionPayment payment = new CommissionPayment();
                    payment.setCompanyId(companyId);
                    payment.setCommissionRule(rule);
                    payment.setSalesPerson(salesPerson);
                    payment.setSalesOrder(orders.isEmpty() ? null : orders.get(0));
                    payment.setSalesInvoice(invoices.isEmpty() ? null : invoices.get(0));
                    payment.setBaseAmount(invoices.stream().map(SalesInvoice::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
                    payment.setCommissionRate(rule.getValue());
                    payment.setAmount(totalCommission);
                    payment.setStatus(CommissionStatus.PENDING);
                    payment.setPeriodStart(periodStart);
                    payment.setPeriodEnd(periodEnd);
                    payment.setCalculatedAt(LocalDateTime.now());
                    payment.setCalculatedById(calculatedById);
                    payment.setNotes("Auto-calculated for period " + periodStart + " to " + periodEnd);

                    commissionPaymentRepository.save(payment);

                    eventPublisher.publish(new CommissionCalculatedEvent(
                            payment.getId(),
                            companyId,
                            salesPersonId,
                            salesPerson.getFullName(),
                            rule.getId(),
                            rule.getRuleCode(),
                            totalCommission,
                            periodStart,
                            periodEnd,
                            LocalDateTime.now()
                    ));
                }
            }
        }
    }
}