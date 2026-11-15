package com.erp.brazil.sales.presentation.rest;

import com.erp.brazil.sales.domain.model.PipelineStageType;
import com.erp.brazil.sales.domain.model.SalesPipeline;
import com.erp.brazil.sales.domain.model.PipelineStage;
import com.erp.brazil.sales.domain.repository.PipelineStageRepository;
import com.erp.brazil.sales.domain.repository.SalesPipelineRepository;
import com.erp.brazil.sales.presentation.dto.ApiResponse;
import com.erp.brazil.sales.presentation.dto.KanbanColumnDTO;
import com.erp.brazil.sales.presentation.dto.KanbanOpportunityDTO;
import com.erp.brazil.sales.presentation.dto.PageResponse;
import com.erp.brazil.sales.presentation.dto.PipelineDTO;
import com.erp.brazil.sales.presentation.dto.PipelineKanbanDTO;
import com.erp.brazil.sales.presentation.dto.request.CreatePipelineRequest;
import com.erp.brazil.sales.presentation.dto.request.CreatePipelineStageRequest;
import com.erp.brazil.sales.presentation.dto.request.UpdatePipelineRequest;
import com.erp.brazil.sales.presentation.dto.request.UpdatePipelineStageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/pipelines")
public class PipelineController {

    private final SalesPipelineRepository pipelineRepository;
    private final PipelineStageRepository pipelineStageRepository;

    public PipelineController(SalesPipelineRepository pipelineRepository,
                              PipelineStageRepository pipelineStageRepository) {
        this.pipelineRepository = pipelineRepository;
        this.pipelineStageRepository = pipelineStageRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PipelineDTO>> create(@RequestHeader("X-Company-ID") UUID companyId,
                                                            @RequestBody CreatePipelineRequest request) {
        SalesPipeline pipeline = new SalesPipeline();
        pipeline.setCompanyId(companyId);
        pipeline.setCode(request.code());
        pipeline.setName(request.name());
        pipeline.setDescription(request.description());
        pipeline.setIsActive(request.isActive() != null ? request.isActive() : true);
        pipeline.setIsDefault(request.isDefault() != null ? request.isDefault() : false);
        if (request.salesTeamId() != null) {
            pipeline.setSalesTeam(new com.erp.brazil.sales.domain.model.SalesTeam());
            pipeline.getSalesTeam().setId(request.salesTeamId());
        }

        if (request.stages() != null) {
            List<PipelineStage> stages = request.stages().stream().map(s -> {
                PipelineStage stage = new PipelineStage();
                stage.setPipeline(pipeline);
                stage.setCode(s.code());
                stage.setName(s.name());
                stage.setDescription(s.description());
                stage.setOrderIndex(s.orderIndex());
                stage.setType(s.type());
                stage.setProbability(s.probability() != null ? s.probability() : 0);
                stage.setColor(s.color() != null ? s.color() : "#6366f1");
                stage.setIsActive(s.isActive() != null ? s.isActive() : true);
                stage.setMaxDaysInStage(s.maxDaysInStage());
                stage.setAllowSkip(s.allowSkip() != null ? s.allowSkip() : false);
                return stage;
            }).collect(Collectors.toList());
            pipeline.setStages(stages);
        }

        pipeline = pipelineRepository.save(pipeline);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(pipeline)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PipelineDTO>> getById(@RequestHeader("X-Company-ID") UUID companyId,
                                                             @PathVariable UUID id) {
        SalesPipeline pipeline = pipelineRepository.findById(id)
                .filter(p -> p.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Pipeline not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(pipeline)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PipelineDTO>> update(@RequestHeader("X-Company-ID") UUID companyId,
                                                            @PathVariable UUID id,
                                                            @RequestBody UpdatePipelineRequest request) {
        SalesPipeline pipeline = pipelineRepository.findById(id)
                .filter(p -> p.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Pipeline not found: " + id));

        if (request.code() != null) pipeline.setCode(request.code());
        if (request.name() != null) pipeline.setName(request.name());
        if (request.description() != null) pipeline.setDescription(request.description());
        if (request.isActive() != null) pipeline.setIsActive(request.isActive());
        if (request.isDefault() != null) pipeline.setIsDefault(request.isDefault());
        if (request.salesTeamId() != null) {
            pipeline.setSalesTeam(new com.erp.brazil.sales.domain.model.SalesTeam());
            pipeline.getSalesTeam().setId(request.salesTeamId());
        }

        pipeline = pipelineRepository.save(pipeline);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(pipeline)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@RequestHeader("X-Company-ID") UUID companyId,
                                                     @PathVariable UUID id) {
        SalesPipeline pipeline = pipelineRepository.findById(id)
                .filter(p -> p.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Pipeline not found: " + id));
        pipelineRepository.delete(pipeline);
        return ResponseEntity.ok(ApiResponse.ok(null, "Pipeline deleted successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PipelineDTO>>> list(@RequestHeader("X-Company-ID") UUID companyId,
                                                                        Pageable pageable) {
        Page<SalesPipeline> page = pipelineRepository.findByCompanyId(companyId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(mapToPageResponse(page)));
    }

    @PostMapping("/{pipelineId}/stages")
    public ResponseEntity<ApiResponse<PipelineDTO.PipelineStageDTO>> addStage(@RequestHeader("X-Company-ID") UUID companyId,
                                                                               @PathVariable UUID pipelineId,
                                                                               @RequestBody CreatePipelineStageRequest request) {
        SalesPipeline pipeline = pipelineRepository.findById(pipelineId)
                .filter(p -> p.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Pipeline not found: " + pipelineId));

        PipelineStage stage = new PipelineStage();
        stage.setPipeline(pipeline);
        stage.setCode(request.code());
        stage.setName(request.name());
        stage.setDescription(request.description());
        stage.setOrderIndex(request.orderIndex());
        stage.setType(request.type());
        stage.setProbability(request.probability() != null ? request.probability() : 0);
        stage.setColor(request.color() != null ? request.color() : "#6366f1");
        stage.setIsActive(request.isActive() != null ? request.isActive() : true);
        stage.setMaxDaysInStage(request.maxDaysInStage());
        stage.setAllowSkip(request.allowSkip() != null ? request.allowSkip() : false);

        stage = pipelineStageRepository.save(stage);
        return ResponseEntity.ok(ApiResponse.ok(mapStageToDTO(stage)));
    }

    @PutMapping("/stages/{stageId}")
    public ResponseEntity<ApiResponse<PipelineDTO.PipelineStageDTO>> updateStage(@RequestHeader("X-Company-ID") UUID companyId,
                                                                                  @PathVariable UUID stageId,
                                                                                  @RequestBody UpdatePipelineStageRequest request) {
        PipelineStage stage = pipelineStageRepository.findById(stageId)
                .filter(s -> s.getPipeline().getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Stage not found: " + stageId));

        if (request.code() != null) stage.setCode(request.code());
        if (request.name() != null) stage.setName(request.name());
        if (request.description() != null) stage.setDescription(request.description());
        if (request.orderIndex() != null) stage.setOrderIndex(request.orderIndex());
        if (request.type() != null) stage.setType(request.type());
        if (request.probability() != null) stage.setProbability(request.probability());
        if (request.color() != null) stage.setColor(request.color());
        if (request.isActive() != null) stage.setIsActive(request.isActive());
        if (request.maxDaysInStage() != null) stage.setMaxDaysInStage(request.maxDaysInStage());
        if (request.allowSkip() != null) stage.setAllowSkip(request.allowSkip());

        stage = pipelineStageRepository.save(stage);
        return ResponseEntity.ok(ApiResponse.ok(mapStageToDTO(stage)));
    }

    @DeleteMapping("/stages/{stageId}")
    public ResponseEntity<ApiResponse<Void>> deleteStage(@RequestHeader("X-Company-ID") UUID companyId,
                                                          @PathVariable UUID stageId) {
        PipelineStage stage = pipelineStageRepository.findById(stageId)
                .filter(s -> s.getPipeline().getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Stage not found: " + stageId));
        pipelineStageRepository.delete(stage);
        return ResponseEntity.ok(ApiResponse.ok(null, "Stage deleted successfully"));
    }

    @GetMapping("/{id}/kanban")
    public ResponseEntity<ApiResponse<PipelineKanbanDTO>> getKanban(@RequestHeader("X-Company-ID") UUID companyId,
                                                                     @PathVariable UUID id) {
        SalesPipeline pipeline = pipelineRepository.findById(id)
                .filter(p -> p.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Pipeline not found: " + id));

        var stages = pipelineStageRepository.findActiveByPipelineIdOrderByOrderIndex(id);
        var openStatuses = List.of(
                com.erp.brazil.sales.domain.model.OpportunityStatus.OPEN,
                com.erp.brazil.sales.domain.model.OpportunityStatus.IN_PROGRESS,
                com.erp.brazil.sales.domain.model.OpportunityStatus.QUOTATION_SENT,
                com.erp.brazil.sales.domain.model.OpportunityStatus.NEGOTIATION
        );

        List<KanbanDTO.KanbanColumnDTO> columns = stages.stream().map(stage -> {
            List<KanbanDTO.KanbanOpportunityDTO> cards = pipeline.getOpportunities().stream()
                    .filter(o -> openStatuses.contains(o.getStatus()))
                    .filter(o -> o.getCurrentStage() != null && o.getCurrentStage().getId().equals(stage.getId()))
                    .map(o -> new KanbanDTO.KanbanOpportunityDTO(
                            o.getId(), o.getOpportunityCode(), o.getTitle(), o.getAmount(),
                            o.getProbability(), o.getWeightedAmount(), o.getExpectedCloseDate(),
                            o.getCustomer() != null ? o.getCustomer().getTradeName() : null,
                            o.getAssignedSalesPerson() != null ? o.getAssignedSalesPerson().getFullName() : null,
                            stage.getId(), stage.getName(), stage.getColor()
                    )).toList();

            BigDecimal totalAmount = cards.stream().map(KanbanDTO.KanbanOpportunityDTO::amount).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            BigDecimal totalWeighted = cards.stream().map(KanbanDTO.KanbanOpportunityDTO::weightedAmount).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

            return new KanbanDTO.KanbanColumnDTO(
                    stage.getId(), stage.getName(), stage.getColor(), stage.getType(), stage.getOrderIndex(),
                    cards, cards.size(), totalAmount, totalWeighted
            );
        }).toList();

        PipelineKanbanDTO kanban = new PipelineKanbanDTO(id, pipeline.getName(), columns);
        return ResponseEntity.ok(ApiResponse.ok(kanban));
    }

    private PipelineDTO mapToDTO(SalesPipeline p) {
        List<PipelineDTO.PipelineStageDTO> stages = p.getStages().stream().map(s ->
                new PipelineDTO.PipelineStageDTO(
                        s.getId(), s.getPipeline().getId(), s.getCode(), s.getName(), s.getDescription(),
                        s.getOrderIndex(), s.getType(), s.getProbability(), s.getColor(), s.getIsActive(),
                        s.getMaxDaysInStage(), s.getAllowSkip(),
                        (long) s.getPipeline().getOpportunities().stream()
                                .filter(o -> o.getCurrentStage() != null && o.getCurrentStage().getId().equals(s.getId()))
                                .count(),
                        s.getPipeline().getOpportunities().stream()
                                .filter(o -> o.getCurrentStage() != null && o.getCurrentStage().getId().equals(s.getId()))
                                .map(com.erp.brazil.sales.domain.model.Opportunity::getAmount)
                                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                )
        ).collect(Collectors.toList());

        return new PipelineDTO(
                p.getId(), p.getCompanyId(), p.getCode(), p.getName(), p.getDescription(),
                p.getIsActive(), p.getIsDefault(),
                p.getSalesTeam() != null ? p.getSalesTeam().getId() : null,
                p.getSalesTeam() != null ? p.getSalesTeam().getName() : null,
                p.getCreatedAt(), p.getUpdatedAt(), stages
        );
    }

    private PipelineDTO.PipelineStageDTO mapStageToDTO(PipelineStage s) {
        return new PipelineDTO.PipelineStageDTO(
                s.getId(), s.getPipeline().getId(), s.getCode(), s.getName(), s.getDescription(),
                s.getOrderIndex(), s.getType(), s.getProbability(), s.getColor(), s.getIsActive(),
                s.getMaxDaysInStage(), s.getAllowSkip(), 0L, java.math.BigDecimal.ZERO
        );
    }

    private PageResponse<PipelineDTO> mapToPageResponse(Page<SalesPipeline> page) {
        return new PageResponse<>(
                page.getContent().stream().map(this::mapToDTO).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast(), page.isEmpty()
        );
    }
}