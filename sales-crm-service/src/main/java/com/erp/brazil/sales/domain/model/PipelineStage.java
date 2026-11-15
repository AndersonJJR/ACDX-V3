package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pipeline_stages")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStage extends BaseEntity {

    @Column(name = "code", length = 20, nullable = false)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "stage_order", nullable = false)
    private Integer order;

    @Column(name = "probability", nullable = false)
    private Integer probability = 0;

    @Column(name = "is_won")
    private Boolean isWon = false;

    @Column(name = "is_lost")
    private Boolean isLost = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "stage_type")
    @Enumerated(EnumType.STRING)
    private PipelineStageType stageType = PipelineStageType.OPPORTUNITY;

    @Column(name = "days_stagnant_limit")
    private Integer daysStagnantLimit = 30;

    @Column(name = "auto_advance_days")
    private Integer autoAdvanceDays;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id", nullable = false)
    private SalesPipeline pipeline;

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Lead> leads = new ArrayList<>();

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Opportunity> opportunities = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PipelineStage that)) return false;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}