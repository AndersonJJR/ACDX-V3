package com.erp.brazil.sales.presentation.rest;

import com.erp.brazil.sales.application.usecase.CalculateCommissionUseCase;
import com.erp.brazil.sales.domain.model.CommissionRule;
import com.erp.brazil.sales.domain.model.CommissionStatus;
import com.erp.brazil.sales.domain.repository.CommissionRuleRepository;
import com.erp.brazil.sales.domain.repository.CommissionPaymentRepository;
import com.erp.brazil.sales.presentation.dto.CommissionRuleDTO;
import com.erp.brazil.sales.presentation.dto.CommissionPaymentDTO;
import com.erp.brazil.sales.presentation.dto.ApiResponse;
import com.erp.brazil.sales.presentation.dto.PageResponse;
import com.erp.brazil.sales.presentation.dto.request.CreateCommissionRuleRequest;
import com.erp.brazil.sales.presentation.dto.request.UpdateCommissionRuleRequest;
import com.erp.brazil.sales.presentation.dto.request.CreateCommissionTierRequest;
import com.erp.brazil.sales.presentation.dto.request.CalculateCommissionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/commissions")
public class CommissionController {

    private final CommissionRuleRepository commissionRuleRepository;
    private final CommissionPaymentRepository commissionPaymentRepository;
    private final CalculateCommissionUseCase calculateCommissionUseCase;

    public CommissionController(CommissionRuleRepository commissionRuleRepository,
                                CommissionPaymentRepository commissionPaymentRepository,
                                CalculateCommissionUseCase calculateCommissionUseCase) {
        this.commissionRuleRepository = commissionRuleRepository;
        this.commissionPaymentRepository = commissionPaymentRepository;
        this.calculateCommissionUseCase = calculateCommissionUseCase;
    }

    @PostMapping("/rules")
    public ResponseEntity<ApiResponse<CommissionRuleDTO>> createRule(@RequestHeader("X-Company-ID") UUID companyId,
                                                                      @RequestBody CreateCommissionRuleRequest request) {
        CommissionRule rule = new CommissionRule();
        rule.setCompanyId(companyId);
        rule.setRuleCode(request.ruleCode());
        rule.setName(request.name());
        rule.setDescription(request.description());
        rule.setType(request.type());
        rule.setValue(request.value());
        rule.setMinAmount(request.minAmount());
        rule.setMaxAmount(request.maxAmount());
        rule.setProductCategoryId(request.productCategoryId());
        rule.setCustomerSegment(request.customerSegment());
        rule.setValidFrom(request.validFrom());
        rule.setValidUntil(request.validUntil());
        rule.setIsActive(request.isActive() != null ? request.isActive() : true);
        rule.setPriority(request.priority() != null ? request.priority() : 0);
        if (request.salesTeamId() != null) {
            rule.setSalesTeam(new com.erp.brazil.sales.domain.model.SalesTeam());
            rule.getSalesTeam().setId(request.salesTeamId());
        }
        if (request.salesPersonId() != null) {
            rule.setSalesPerson(new com.erp.brazil.sales.domain.model.SalesPerson());
            rule.getSalesPerson().setId(request.salesPersonId());
        }
        if (request.territoryId() != null) {
            rule.setTerritory(new com.erp.brazil.sales.domain.model.Territory());
            rule.getTerritory().setId(request.territoryId());
        }

        if (request.tiers() != null) {
            List<com.erp.brazil.sales.domain.model.CommissionTier> tiers = request.tiers().stream().map(t -> {
                com.erp.brazil.sales.domain.model.CommissionTier tier = new com.erp.brazil.sales.domain.model.CommissionTier();
                tier.setCommissionRule(rule);
                tier.setTierOrder(t.tierOrder());
                tier.setMinAmount(t.minAmount());
                tier.setMaxAmount(t.maxAmount());
                tier.setRate(t.rate());
                tier.setFixedAmount(t.fixedAmount());
                return tier;
            }).collect(Collectors.toList());
            rule.setTiers(tiers);
        }

        rule = commissionRuleRepository.save(rule);
        return ResponseEntity.ok(ApiResponse.ok(mapRuleToDTO(rule)));
    }

    @GetMapping("/rules")
    public ResponseEntity<ApiResponse<PageResponse<CommissionRuleDTO>>> listRules(@RequestHeader("X-Company-ID") UUID companyId,
                                                                                   @RequestParam(required = false) CommissionRule.RuleType type,
                                                                                   Pageable pageable) {
        Page<CommissionRule> page;
        if (type != null) {
            page = commissionRuleRepository.findByCompanyIdAndType(companyId, type, pageable);
        } else {
            page = commissionRuleRepository.findByCompanyId(companyId, pageable);
        }
        return ResponseEntity.ok(ApiResponse.ok(mapRulePageResponse(page)));
    }

    @GetMapping("/rules/{id}")
    public ResponseEntity<ApiResponse<CommissionRuleDTO>> getRule(@RequestHeader("X-Company-ID") UUID companyId,
                                                                   @PathVariable UUID id) {
        CommissionRule rule = commissionRuleRepository.findById(id)
                .filter(r -> r.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Commission rule not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(mapRuleToDTO(rule)));
    }

    @PutMapping("/rules/{id}")
    public ResponseEntity<ApiResponse<CommissionRuleDTO>> updateRule(@RequestHeader("X-Company-ID") UUID companyId,
                                                                      @PathVariable UUID id,
                                                                      @RequestBody UpdateCommissionRuleRequest request) {
        CommissionRule rule = commissionRuleRepository.findById(id)
                .filter(r -> r.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Commission rule not found: " + id));

        if (request.ruleCode() != null) rule.setRuleCode(request.ruleCode());
        if (request.name() != null) rule.setName(request.name());
        if (request.description() != null) rule.setDescription(request.description());
        if (request.type() != null) rule.setType(request.type());
        if (request.value() != null) rule.setValue(request.value());
        if (request.minAmount() != null) rule.setMinAmount(request.minAmount());
        if (request.maxAmount() != null) rule.setMaxAmount(request.maxAmount());
        if (request.productCategoryId() != null) rule.setProductCategoryId(request.productCategoryId());
        if (request.customerSegment() != null) rule.setCustomerSegment(request.customerSegment());
        if (request.validFrom() != null) rule.setValidFrom(request.validFrom());
        if (request.validUntil() != null) rule.setValidUntil(request.validUntil());
        if (request.isActive() != null) rule.setIsActive(request.isActive());
        if (request.priority() != null) rule.setPriority(request.priority());
        if (request.salesTeamId() != null) {
            rule.setSalesTeam(new com.erp.brazil.sales.domain.model.SalesTeam());
            rule.getSalesTeam().setId(request.salesTeamId());
        }
        if (request.salesPersonId() != null) {
            rule.setSalesPerson(new com.erp.brazil.sales.domain.model.SalesPerson());
            rule.getSalesPerson().setId(request.salesPersonId());
        }
        if (request.territoryId() != null) {
            rule.setTerritory(new com.erp.brazil.sales.domain.model.Territory());
            rule.getTerritory().setId(request.territoryId());
        }

        rule = commissionRuleRepository.save(rule);
        return ResponseEntity.ok(ApiResponse.ok(mapRuleToDTO(rule)));
    }

    @DeleteMapping("/rules/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@RequestHeader("X-Company-ID") UUID companyId,
                                                         @PathVariable UUID id) {
        CommissionRule rule = commissionRuleRepository.findById(id)
                .filter(r -> r.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Commission rule not found: " + id));
        commissionRuleRepository.delete(rule);
        return ResponseEntity.ok(ApiResponse.ok(null, "Commission rule deleted successfully"));
    }

    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<Void>> calculate(@RequestHeader("X-Company-ID") UUID companyId,
                                                        @RequestHeader("X-User-ID") UUID userId,
                                                        @RequestBody CalculateCommissionRequest request) {
        CalculateCommissionRequest req = new CalculateCommissionRequest(
                request.periodStart(), request.periodEnd(), userId
        );
        req = new CalculateCommissionRequest(request.periodStart(), request.periodEnd(), userId);
        calculateCommissionUseCase.execute(req);
        return ResponseEntity.ok(ApiResponse.ok(null, "Commission calculation completed"));
    }

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<PageResponse<CommissionPaymentDTO>>> listPayments(@RequestHeader("X-Company-ID") UUID companyId,
                                                                                         @RequestParam(required = false) CommissionStatus status,
                                                                                         @RequestParam(required = false) UUID salesPersonId,
                                                                                         @RequestParam(required = false) LocalDate periodStart,
                                                                                         @RequestParam(required = false) LocalDate periodEnd,
                                                                                         Pageable pageable) {
        Page<com.erp.brazil.sales.domain.model.CommissionPayment> page;
        if (salesPersonId != null && periodStart != null && periodEnd != null) {
            page = commissionPaymentRepository.findBySalesPersonAndPeriod(companyId, salesPersonId, periodStart, periodEnd, pageable);
        } else if (periodStart != null && periodEnd != null) {
            page = commissionPaymentRepository.findByCompanyIdAndPeriod(companyId, periodStart, periodEnd, pageable);
        } else {
            page = commissionPaymentRepository.findByCompanyId(companyId, pageable);
        }
        return ResponseEntity.ok(ApiResponse.ok(mapPaymentPageResponse(page)));
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<ApiResponse<CommissionPaymentDTO>> getPayment(@RequestHeader("X-Company-ID") UUID companyId,
                                                                         @PathVariable UUID id) {
        com.erp.brazil.sales.domain.model.CommissionPayment payment = commissionPaymentRepository.findById(id)
                .filter(p -> p.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Commission payment not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(mapPaymentToDTO(payment)));
    }

    private CommissionRuleDTO mapRuleToDTO(CommissionRule r) {
        List<CommissionRuleDTO.CommissionTierDTO> tiers = r.getTiers().stream().map(t ->
                new CommissionRuleDTO.CommissionTierDTO(
                        t.getId(), t.getCommissionRule().getId(), t.getTierOrder(),
                        t.getMinAmount(), t.getMaxAmount(), t.getRate(), t.getFixedAmount()
                )
        ).collect(Collectors.toList());

        return new CommissionRuleDTO(
                r.getId(), r.getCompanyId(), r.getRuleCode(), r.getName(), r.getDescription(),
                r.getType(), r.getValue(), r.getMinAmount(), r.getMaxAmount(),
                r.getProductCategoryId(), null, r.getCustomerSegment(),
                r.getValidFrom(), r.getValidUntil(), r.getIsActive(), r.getPriority(),
                r.getSalesTeam() != null ? r.getSalesTeam().getId() : null,
                r.getSalesTeam() != null ? r.getSalesTeam().getName() : null,
                r.getSalesPerson() != null ? r.getSalesPerson().getId() : null,
                r.getSalesPerson() != null ? r.getSalesPerson().getFullName() : null,
                r.getTerritory() != null ? r.getTerritory().getId() : null,
                r.getTerritory() != null ? r.getTerritory().getName() : null,
                r.getCreatedAt(), r.getUpdatedAt(), tiers
        );
    }

    private CommissionPaymentDTO mapPaymentToDTO(com.erp.brazil.sales.domain.model.CommissionPayment p) {
        return new CommissionPaymentDTO(
                p.getId(), p.getCompanyId(), p.getCommissionRule().getId(), p.getCommissionRule().getRuleCode(),
                p.getSalesPerson().getId(), p.getSalesPerson().getFullName(),
                p.getSalesOrder() != null ? p.getSalesOrder().getId() : null,
                p.getSalesOrder() != null ? p.getSalesOrder().getNumber() : null,
                p.getSalesInvoice() != null ? p.getSalesInvoice().getId() : null,
                p.getSalesInvoice() != null ? p.getSalesInvoice().getNumber() : null,
                p.getBaseAmount(), p.getCommissionRate(), p.getAmount(), p.getStatus(),
                p.getPeriodStart(), p.getPeriodEnd(), p.getCalculatedAt(), p.getCalculatedById(), null,
                p.getPaidAt(), p.getPaidById(), null, p.getNotes(),
                p.getCreatedAt(), p.getUpdatedAt()
        );
    }

    private PageResponse<CommissionRuleDTO> mapRulePageResponse(Page<CommissionRule> page) {
        return new PageResponse<>(
                page.getContent().stream().map(this::mapRuleToDTO).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast(), page.isEmpty()
        );
    }

    private PageResponse<CommissionPaymentDTO> mapPaymentPageResponse(Page<com.erp.brazil.sales.domain.model.CommissionPayment> page) {
        return new PageResponse<>(
                page.getContent().stream().map(this::mapPaymentToDTO).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast(), page.isEmpty()
        );
    }
}