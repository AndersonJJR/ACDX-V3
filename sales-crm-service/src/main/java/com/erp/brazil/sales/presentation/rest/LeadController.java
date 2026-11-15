package com.erp.brazil.sales.presentation.rest;

import com.erp.brazil.sales.application.usecase.ConvertLeadToOpportunityUseCase;
import com.erp.brazil.sales.domain.model.Lead;
import com.erp.brazil.sales.domain.model.LeadStatus;
import com.erp.brazil.sales.domain.repository.LeadRepository;
import com.erp.brazil.sales.domain.repository.PipelineStageRepository;
import com.erp.brazil.sales.presentation.dto.ApiResponse;
import com.erp.brazil.sales.presentation.dto.LeadDTO;
import com.erp.brazil.sales.presentation.dto.LeadKanbanDTO;
import com.erp.brazil.sales.presentation.dto.OpportunityDTO;
import com.erp.brazil.sales.presentation.dto.PageResponse;
import com.erp.brazil.sales.presentation.dto.request.ConvertLeadRequest;
import com.erp.brazil.sales.presentation.dto.request.CreateLeadRequest;
import com.erp.brazil.sales.presentation.dto.request.QualifyLeadRequest;
import com.erp.brazil.sales.presentation.dto.request.UpdateLeadRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/leads")
public class LeadController {

    private final LeadRepository leadRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final ConvertLeadToOpportunityUseCase convertLeadUseCase;

    public LeadController(LeadRepository leadRepository,
                          PipelineStageRepository pipelineStageRepository,
                          ConvertLeadToOpportunityUseCase convertLeadUseCase) {
        this.leadRepository = leadRepository;
        this.pipelineStageRepository = pipelineStageRepository;
        this.convertLeadUseCase = convertLeadUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LeadDTO>> create(@RequestHeader("X-Company-ID") UUID companyId,
                                                        @RequestBody CreateLeadRequest request) {
        Lead lead = new Lead();
        lead.setCompanyId(companyId);
        lead.setLeadCode(generateLeadCode(companyId));
        lead.setTitle(request.title());
        lead.setDescription(request.description());
        lead.setSource(request.source());
        lead.setNotes(request.notes());
        lead.setEstimatedValue(request.estimatedValue());
        lead.setProbability(request.probability());
        lead.setExpectedCloseDate(request.expectedCloseDate());
        lead.setStatus(LeadStatus.NEW);

        if (request.customerId() != null) {
            lead.setCustomer(new com.erp.brazil.sales.domain.model.Customer());
            lead.getCustomer().setId(request.customerId());
        }
        if (request.contactId() != null) {
            lead.setContact(new com.erp.brazil.sales.domain.model.Contact());
            lead.getContact().setId(request.contactId());
        }
        if (request.territoryId() != null) {
            lead.setTerritory(new com.erp.brazil.sales.domain.model.Territory());
            lead.getTerritory().setId(request.territoryId());
        }
        if (request.assignedSalesPersonId() != null) {
            lead.setAssignedSalesPerson(new com.erp.brazil.sales.domain.model.SalesPerson());
            lead.getAssignedSalesPerson().setId(request.assignedSalesPersonId());
        }
        if (request.pipelineId() != null) {
            lead.setPipeline(new com.erp.brazil.sales.domain.model.SalesPipeline());
            lead.getPipeline().setId(request.pipelineId());

            var stages = pipelineStageRepository.findByPipelineIdOrderByOrderIndexAsc(request.pipelineId());
            if (!stages.isEmpty()) {
                lead.setCurrentStage(stages.get(0));
            }
        }

        lead = leadRepository.save(lead);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(lead)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeadDTO>> getById(@RequestHeader("X-Company-ID") UUID companyId,
                                                        @PathVariable UUID id) {
        Lead lead = leadRepository.findById(id)
                .filter(l -> l.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(lead)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LeadDTO>> update(@RequestHeader("X-Company-ID") UUID companyId,
                                                        @PathVariable UUID id,
                                                        @RequestBody UpdateLeadRequest request) {
        Lead lead = leadRepository.findById(id)
                .filter(l -> l.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + id));

        if (request.title() != null) lead.setTitle(request.title());
        if (request.description() != null) lead.setDescription(request.description());
        if (request.source() != null) lead.setSource(request.source());
        if (request.notes() != null) lead.setNotes(request.notes());
        if (request.estimatedValue() != null) lead.setEstimatedValue(request.estimatedValue());
        if (request.probability() != null) lead.setProbability(request.probability());
        if (request.expectedCloseDate() != null) lead.setExpectedCloseDate(request.expectedCloseDate());
        if (request.customerId() != null) {
            lead.setCustomer(new com.erp.brazil.sales.domain.model.Customer());
            lead.getCustomer().setId(request.customerId());
        }
        if (request.contactId() != null) {
            lead.setContact(new com.erp.brazil.sales.domain.model.Contact());
            lead.getContact().setId(request.contactId());
        }
        if (request.territoryId() != null) {
            lead.setTerritory(new com.erp.brazil.sales.domain.model.Territory());
            lead.getTerritory().setId(request.territoryId());
        }
        if (request.assignedSalesPersonId() != null) {
            lead.setAssignedSalesPerson(new com.erp.brazil.sales.domain.model.SalesPerson());
            lead.getAssignedSalesPerson().setId(request.assignedSalesPersonId());
        }
        if (request.pipelineId() != null) {
            lead.setPipeline(new com.erp.brazil.sales.domain.model.SalesPipeline());
            lead.getPipeline().setId(request.pipelineId());
        }
        if (request.currentStageId() != null) {
            lead.setCurrentStage(new com.erp.brazil.sales.domain.model.PipelineStage());
            lead.getCurrentStage().setId(request.currentStageId());
        }

        lead = leadRepository.save(lead);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(lead)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@RequestHeader("X-Company-ID") UUID companyId,
                                                    @PathVariable UUID id) {
        Lead lead = leadRepository.findById(id)
                .filter(l -> l.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + id));
        leadRepository.delete(lead);
        return ResponseEntity.ok(ApiResponse.ok(null, "Lead deleted successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LeadDTO>>> list(@RequestHeader("X-Company-ID") UUID companyId,
                                                                    @RequestParam(required = false) LeadStatus status,
                                                                    Pageable pageable) {
        Page<Lead> page;
        if (status != null) {
            page = leadRepository.findByCompanyIdAndStatus(companyId, status, pageable);
        } else {
            page = leadRepository.findByCompanyId(companyId, pageable);
        }
        return ResponseEntity.ok(ApiResponse.ok(mapToPageResponse(page)));
    }

    @PostMapping("/{id}/qualify")
    public ResponseEntity<ApiResponse<LeadDTO>> qualify(@RequestHeader("X-Company-ID") UUID companyId,
                                                        @PathVariable UUID id,
                                                        @RequestBody QualifyLeadRequest request) {
        Lead lead = leadRepository.findById(id)
                .filter(l -> l.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + id));

        lead.setStatus(LeadStatus.QUALIFIED);
        lead.setQualifiedAt(LocalDateTime.now());
        if (request.score() != null) lead.setScore(request.score());
        if (request.notes() != null) lead.setNotes(request.notes());

        lead = leadRepository.save(lead);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(lead)));
    }

    @PostMapping("/{id}/convert")
    public ResponseEntity<ApiResponse<OpportunityDTO>> convert(@RequestHeader("X-Company-ID") UUID companyId,
                                                                @PathVariable UUID id,
                                                                @RequestBody ConvertLeadRequest request) {
        if (!companyId.equals(request.convertedById())) {
            // This should come from auth context in real implementation
        }
        OpportunityDTO opportunity = convertLeadUseCase.execute(id, request);
        return ResponseEntity.ok(ApiResponse.ok(opportunity));
    }

    @GetMapping("/kanban")
    public ResponseEntity<ApiResponse<LeadKanbanDTO>> getKanban(@RequestHeader("X-Company-ID") UUID companyId,
                                                                 @RequestParam UUID pipelineId) {
        var pipeline = new com.erp.brazil.sales.domain.model.SalesPipeline();
        pipeline.setId(pipelineId);

        var stages = pipelineStageRepository.findActiveByPipelineIdOrderByOrderIndex(pipelineId);
        var allLeads = leadRepository.findByCompanyIdAndStatusIn(companyId, 
                List.of(LeadStatus.NEW, LeadStatus.CONTACTED, LeadStatus.QUALIFIED, LeadStatus.PROPOSAL, LeadStatus.NEGOTIATION));

        List<LeadKanbanDTO.LeadKanbanColumnDTO> columns = stages.stream().map(stage -> {
            List<LeadKanbanDTO.LeadKanbanCardDTO> cards = allLeads.stream()
                    .filter(l -> l.getCurrentStage() != null && l.getCurrentStage().getId().equals(stage.getId()))
                    .map(l -> new LeadKanbanDTO.LeadKanbanCardDTO(
                            l.getId(), l.getLeadCode(), l.getTitle(),
                            l.getCustomer() != null ? l.getCustomer().getTradeName() : null,
                            l.getAssignedSalesPerson() != null ? l.getAssignedSalesPerson().getFullName() : null,
                            l.getScore(), l.getEstimatedValue(), l.getExpectedCloseDate()
                    )).toList();

            return new LeadKanbanDTO.LeadKanbanColumnDTO(
                    stage.getId(), stage.getName(), stage.getColor(), stage.getType(), stage.getOrderIndex(),
                    cards, cards.size()
            );
        }).toList();

        LeadKanbanDTO kanban = new LeadKanbanDTO(pipelineId, "Pipeline", columns);
        return ResponseEntity.ok(ApiResponse.ok(kanban));
    }

    private String generateLeadCode(UUID companyId) {
        long count = leadRepository.countByCompanyId(companyId);
        return String.format("LEAD-%06d", count + 1);
    }

    private LeadDTO mapToDTO(Lead l) {
        List<LeadDTO.LeadActivityDTO> activities = l.getActivities().stream().map(a ->
                new LeadDTO.LeadActivityDTO(
                        a.getId(), a.getType(), a.getSubject(), a.getDescription(),
                        a.getScheduledAt(), a.getCompletedAt(), a.getOutcome(),
                        a.getCreatedBy() != null ? a.getCreatedBy().getId() : null,
                        a.getCreatedBy() != null ? a.getCreatedBy().getFullName() : null,
                        a.getCreatedAt()
                )
        ).collect(Collectors.toList());

        return new LeadDTO(
                l.getId(), l.getCompanyId(), l.getLeadCode(), l.getTitle(), l.getDescription(),
                l.getSource(), l.getScore(), l.getStatus(), l.getEstimatedValue(), l.getProbability(),
                l.getExpectedCloseDate(), l.getQualifiedAt(), l.getConvertedAt(), l.getLostReason(),
                l.getNotes(),
                l.getCustomer() != null ? l.getCustomer().getId() : null,
                l.getCustomer() != null ? l.getCustomer().getTradeName() : null,
                l.getContact() != null ? l.getContact().getId() : null,
                l.getContact() != null ? l.getContact().getName() : null,
                l.getTerritory() != null ? l.getTerritory().getId() : null,
                l.getTerritory() != null ? l.getTerritory().getName() : null,
                l.getAssignedSalesPerson() != null ? l.getAssignedSalesPerson().getId() : null,
                l.getAssignedSalesPerson() != null ? l.getAssignedSalesPerson().getFullName() : null,
                l.getPipeline() != null ? l.getPipeline().getId() : null,
                l.getPipeline() != null ? l.getPipeline().getName() : null,
                l.getCurrentStage() != null ? l.getCurrentStage().getId() : null,
                l.getCurrentStage() != null ? l.getCurrentStage().getName() : null,
                l.getCreatedAt(), l.getUpdatedAt(), activities
        );
    }

    private PageResponse<LeadDTO> mapToPageResponse(Page<Lead> page) {
        return new PageResponse<>(
                page.getContent().stream().map(this::mapToDTO).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast(), page.isEmpty()
        );
    }
}