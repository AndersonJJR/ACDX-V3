package com.erp.brazil.sales.presentation.rest;

import com.erp.brazil.sales.domain.model.Opportunity;
import com.erp.brazil.sales.domain.model.OpportunityStatus;
import com.erp.brazil.sales.domain.repository.OpportunityRepository;
import com.erp.brazil.sales.domain.repository.PipelineStageRepository;
import com.erp.brazil.sales.presentation.dto.ApiResponse;
import com.erp.brazil.sales.presentation.dto.OpportunityDTO;
import com.erp.brazil.sales.presentation.dto.PageResponse;
import com.erp.brazil.sales.presentation.dto.PipelineKanbanDTO;
import com.erp.brazil.sales.presentation.dto.request.AdvanceStageRequest;
import com.erp.brazil.sales.presentation.dto.request.CloseOpportunityRequest;
import com.erp.brazil.sales.presentation.dto.request.CreateOpportunityProductRequest;
import com.erp.brazil.sales.presentation.dto.request.CreateOpportunityRequest;
import com.erp.brazil.sales.presentation.dto.request.UpdateOpportunityRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/opportunities")
public class OpportunityController {

    private final OpportunityRepository opportunityRepository;
    private final PipelineStageRepository pipelineStageRepository;

    public OpportunityController(OpportunityRepository opportunityRepository,
                                  PipelineStageRepository pipelineStageRepository) {
        this.opportunityRepository = opportunityRepository;
        this.pipelineStageRepository = pipelineStageRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OpportunityDTO>> create(@RequestHeader("X-Company-ID") UUID companyId,
                                                               @RequestBody CreateOpportunityRequest request) {
        Opportunity opp = new Opportunity();
        opp.setCompanyId(companyId);
        opp.setOpportunityCode(generateOpportunityCode(companyId));
        opp.setTitle(request.title());
        opp.setDescription(request.description());
        opp.setAmount(request.amount());
        opp.setProbability(request.probability() != null ? request.probability() : 10);
        opp.setExpectedCloseDate(request.expectedCloseDate());
        opp.setNotes(request.notes());
        opp.setStatus(OpportunityStatus.OPEN);

        if (request.leadId() != null) {
            opp.setLead(new com.erp.brazil.sales.domain.model.Lead());
            opp.getLead().setId(request.leadId());
        }
        opp.setCustomer(new com.erp.brazil.sales.domain.model.Customer());
        opp.getCustomer().setId(request.customerId());
        if (request.contactId() != null) {
            opp.setContact(new com.erp.brazil.sales.domain.model.Contact());
            opp.getContact().setId(request.contactId());
        }
        if (request.territoryId() != null) {
            opp.setTerritory(new com.erp.brazil.sales.domain.model.Territory());
            opp.getTerritory().setId(request.territoryId());
        }
        if (request.assignedSalesPersonId() != null) {
            opp.setAssignedSalesPerson(new com.erp.brazil.sales.domain.model.SalesPerson());
            opp.getAssignedSalesPerson().setId(request.assignedSalesPersonId());
        }
        if (request.pipelineId() != null) {
            opp.setPipeline(new com.erp.brazil.sales.domain.model.SalesPipeline());
            opp.getPipeline().setId(request.pipelineId());
        }
        if (request.currentStageId() != null) {
            opp.setCurrentStage(new com.erp.brazil.sales.domain.model.PipelineStage());
            opp.getCurrentStage().setId(request.currentStageId());
        }

        if (request.products() != null) {
            List<com.erp.brazil.sales.domain.model.OpportunityProduct> products = request.products().stream().map(p -> {
                com.erp.brazil.sales.domain.model.OpportunityProduct product = new com.erp.brazil.sales.domain.model.OpportunityProduct();
                product.setOpportunity(opp);
                product.setProductId(p.productId());
                product.setProductCode(p.productCode());
                product.setProductName(p.productName());
                product.setQuantity(p.quantity());
                product.setUnitPrice(p.unitPrice());
                product.setDiscountPercent(p.discountPercent());
                product.setNotes(p.notes());
                return product;
            }).toList();
            opp.setProducts(products);
        }

        opp = opportunityRepository.save(opp);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(opp)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OpportunityDTO>> getById(@RequestHeader("X-Company-ID") UUID companyId,
                                                                @PathVariable UUID id) {
        Opportunity opp = opportunityRepository.findById(id)
                .filter(o -> o.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Opportunity not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(opp)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OpportunityDTO>> update(@RequestHeader("X-Company-ID") UUID companyId,
                                                               @PathVariable UUID id,
                                                               @RequestBody UpdateOpportunityRequest request) {
        Opportunity opp = opportunityRepository.findById(id)
                .filter(o -> o.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Opportunity not found: " + id));

        if (request.title() != null) opp.setTitle(request.title());
        if (request.description() != null) opp.setDescription(request.description());
        if (request.amount() != null) opp.setAmount(request.amount());
        if (request.probability() != null) opp.setProbability(request.probability());
        if (request.expectedCloseDate() != null) opp.setExpectedCloseDate(request.expectedCloseDate());
        if (request.notes() != null) opp.setNotes(request.notes());
        if (request.customerId() != null) {
            opp.setCustomer(new com.erp.brazil.sales.domain.model.Customer());
            opp.getCustomer().setId(request.customerId());
        }
        if (request.contactId() != null) {
            opp.setContact(new com.erp.brazil.sales.domain.model.Contact());
            opp.getContact().setId(request.contactId());
        }
        if (request.territoryId() != null) {
            opp.setTerritory(new com.erp.brazil.sales.domain.model.Territory());
            opp.getTerritory().setId(request.territoryId());
        }
        if (request.assignedSalesPersonId() != null) {
            opp.setAssignedSalesPerson(new com.erp.brazil.sales.domain.model.SalesPerson());
            opp.getAssignedSalesPerson().setId(request.assignedSalesPersonId());
        }
        if (request.pipelineId() != null) {
            opp.setPipeline(new com.erp.brazil.sales.domain.model.SalesPipeline());
            opp.getPipeline().setId(request.pipelineId());
        }
        if (request.currentStageId() != null) {
            opp.setCurrentStage(new com.erp.brazil.sales.domain.model.PipelineStage());
            opp.getCurrentStage().setId(request.currentStageId());
        }

        opp = opportunityRepository.save(opp);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(opp)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@RequestHeader("X-Company-ID") UUID companyId,
                                                    @PathVariable UUID id) {
        Opportunity opp = opportunityRepository.findById(id)
                .filter(o -> o.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Opportunity not found: " + id));
        opportunityRepository.delete(opp);
        return ResponseEntity.ok(ApiResponse.ok(null, "Opportunity deleted successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OpportunityDTO>>> list(@RequestHeader("X-Company-ID") UUID companyId,
                                                                           @RequestParam(required = false) OpportunityStatus status,
                                                                           Pageable pageable) {
        Page<Opportunity> page;
        if (status != null) {
            page = opportunityRepository.findByCompanyIdAndStatus(companyId, status, pageable);
        } else {
            page = opportunityRepository.findByCompanyId(companyId, pageable);
        }
        return ResponseEntity.ok(ApiResponse.ok(mapToPageResponse(page)));
    }

    @PostMapping("/{id}/advance-stage")
    public ResponseEntity<ApiResponse<OpportunityDTO>> advanceStage(@RequestHeader("X-Company-ID") UUID companyId,
                                                                     @PathVariable UUID id,
                                                                     @RequestBody AdvanceStageRequest request) {
        Opportunity opp = opportunityRepository.findById(id)
                .filter(o -> o.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Opportunity not found: " + id));

        var newStage = pipelineStageRepository.findById(request.newStageId())
                .orElseThrow(() -> new IllegalArgumentException("Stage not found: " + request.newStageId()));

        opp.setCurrentStage(newStage);
        if (request.probability() != null) {
            opp.setProbability(request.probability());
        } else {
            opp.setProbability(newStage.getProbability() != null ? newStage.getProbability() : opp.getProbability());
        }
        if (request.notes() != null) {
            opp.setNotes((opp.getNotes() != null ? opp.getNotes() + "\n" : "") + request.notes());
        }

        opp = opportunityRepository.save(opp);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(opp)));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<OpportunityDTO>> close(@RequestHeader("X-Company-ID") UUID companyId,
                                                              @PathVariable UUID id,
                                                              @RequestBody CloseOpportunityRequest request) {
        Opportunity opp = opportunityRepository.findById(id)
                .filter(o -> o.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Opportunity not found: " + id));

        if (request.won()) {
            opp.setStatus(OpportunityStatus.WON);
            opp.setActualCloseDate(LocalDateTime.now());
        } else {
            opp.setStatus(OpportunityStatus.LOST);
            opp.setActualCloseDate(LocalDateTime.now());
            opp.setLostReason(request.lostReason());
            opp.setLostToCompetitor(request.lostToCompetitor());
        }
        if (request.notes() != null) {
            opp.setNotes((opp.getNotes() != null ? opp.getNotes() + "\n" : "") + request.notes());
        }

        opp = opportunityRepository.save(opp);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(opp)));
    }

    @GetMapping("/pipeline")
    public ResponseEntity<ApiResponse<PipelineKanbanDTO>> getPipeline(@RequestHeader("X-Company-ID") UUID companyId,
                                                                       @RequestParam UUID pipelineId) {
        var pipeline = new com.erp.brazil.sales.domain.model.SalesPipeline();
        pipeline.setId(pipelineId);

        var stages = pipelineStageRepository.findActiveByPipelineIdOrderByOrderIndex(pipelineId);
        var openStatuses = List.of(OpportunityStatus.OPEN, OpportunityStatus.IN_PROGRESS, 
                OpportunityStatus.QUOTATION_SENT, OpportunityStatus.NEGOTIATION);

        List<KanbanDTO.KanbanColumnDTO> columns = stages.stream().map(stage -> {
            List<KanbanDTO.KanbanOpportunityDTO> cards = opportunityRepository.findKanbanDataByPipeline(pipelineId, openStatuses)
                    .stream()
                    .filter(o -> o.stageId().equals(stage.getId()))
                    .toList();

            BigDecimal totalAmount = cards.stream().map(KanbanDTO.KanbanOpportunityDTO::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalWeighted = cards.stream().map(KanbanDTO.KanbanOpportunityDTO::weightedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

            return new KanbanDTO.KanbanColumnDTO(
                    stage.getId(), stage.getName(), stage.getColor(), stage.getType(), stage.getOrderIndex(),
                    cards, cards.size(), totalAmount, totalWeighted
            );
        }).toList();

        PipelineKanbanDTO kanban = new PipelineKanbanDTO(pipelineId, "Pipeline", columns);
        return ResponseEntity.ok(ApiResponse.ok(kanban));
    }

    private String generateOpportunityCode(UUID companyId) {
        long count = opportunityRepository.countByCompanyId(companyId);
        return String.format("OPP-%06d", count + 1);
    }

    private OpportunityDTO mapToDTO(Opportunity o) {
        List<OpportunityDTO.OpportunityActivityDTO> activities = o.getActivities().stream().map(a ->
                new OpportunityDTO.OpportunityActivityDTO(
                        a.getId(), a.getType(), a.getSubject(), a.getDescription(),
                        a.getScheduledAt(), a.getCompletedAt(), a.getOutcome(),
                        a.getCreatedBy() != null ? a.getCreatedBy().getId() : null,
                        a.getCreatedBy() != null ? a.getCreatedBy().getFullName() : null,
                        a.getCreatedAt()
                )
        ).collect(Collectors.toList());

        List<OpportunityDTO.OpportunityProductDTO> products = o.getProducts().stream().map(p ->
                new OpportunityDTO.OpportunityProductDTO(
                        p.getId(), p.getProductId(), p.getProductCode(), p.getProductName(),
                        p.getQuantity(), p.getUnitPrice(), p.getDiscountPercent(), p.getTotalAmount(), p.getNotes()
                )
        ).collect(Collectors.toList());

        return new OpportunityDTO(
                o.getId(), o.getCompanyId(), o.getOpportunityCode(), o.getTitle(), o.getDescription(),
                o.getStatus(), o.getAmount(), o.getProbability(), o.getWeightedAmount(),
                o.getExpectedCloseDate(), o.getActualCloseDate(), o.getLostReason(), o.getLostToCompetitor(),
                o.getNotes(),
                o.getLead() != null ? o.getLead().getId() : null,
                o.getLead() != null ? o.getLead().getLeadCode() : null,
                o.getCustomer() != null ? o.getCustomer().getId() : null,
                o.getCustomer() != null ? o.getCustomer().getTradeName() : null,
                o.getContact() != null ? o.getContact().getId() : null,
                o.getContact() != null ? o.getContact().getName() : null,
                o.getTerritory() != null ? o.getTerritory().getId() : null,
                o.getTerritory() != null ? o.getTerritory().getName() : null,
                o.getAssignedSalesPerson() != null ? o.getAssignedSalesPerson().getId() : null,
                o.getAssignedSalesPerson() != null ? o.getAssignedSalesPerson().getFullName() : null,
                o.getPipeline() != null ? o.getPipeline().getId() : null,
                o.getPipeline() != null ? o.getPipeline().getName() : null,
                o.getCurrentStage() != null ? o.getCurrentStage().getId() : null,
                o.getCurrentStage() != null ? o.getCurrentStage().getName() : null,
                o.getCreatedAt(), o.getUpdatedAt(), activities, products
        );
    }

    private PageResponse<OpportunityDTO> mapToPageResponse(Page<Opportunity> page) {
        return new PageResponse<>(
                page.getContent().stream().map(this::mapToDTO).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast(), page.isEmpty()
        );
    }
}