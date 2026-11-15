package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sales_teams")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SalesTeam extends BaseEntity {
    
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    
    @Column(name = "team_code", length = 20, unique = true, nullable = false)
    private String teamCode;
    
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private SalesPerson manager;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_team_id")
    private SalesTeam parentTeam;
    
    @OneToMany(mappedBy = "parentTeam", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SalesTeam> subTeams = new ArrayList<>();
    
    @OneToMany(mappedBy = "salesTeam", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SalesPerson> members = new ArrayList<>();
    
    @OneToMany(mappedBy = "salesTeam", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommissionRule> commissionRules = new ArrayList<>();
    
    @OneToMany(mappedBy = "salesTeam", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SalesPipeline> pipelines = new ArrayList<>();
}