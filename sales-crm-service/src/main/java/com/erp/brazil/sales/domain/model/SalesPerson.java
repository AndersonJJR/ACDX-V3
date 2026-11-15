package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sales_persons")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SalesPerson extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "employee_id", length = 50, unique = true, nullable = false)
    private String employeeId;

    @Column(name = "user_id", length = 100, unique = true)
    private String userId;

    @Column(name = "full_name", length = 200, nullable = false)
    private String fullName;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "mobile", length = 20)
    private String mobile;

    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate = BigDecimal.ZERO;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "hire_date")
    private java.time.LocalDate hireDate;

    @Column(name = "termination_date")
    private java.time.LocalDate terminationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "territory_id")
    private Territory territory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_team_id")
    private SalesTeam salesTeam;

    @OneToMany(mappedBy = "salesPerson", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Customer> customers = new ArrayList<>();

    @OneToMany(mappedBy = "salesPerson", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Lead> leads = new ArrayList<>();

    @OneToMany(mappedBy = "salesPerson", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Opportunity> opportunities = new ArrayList<>();

    @OneToMany(mappedBy = "preparedBy", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Quotation> preparedQuotations = new ArrayList<>();

    @OneToMany(mappedBy = "approvedBy", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Quotation> approvedQuotations = new ArrayList<>();

    @OneToMany(mappedBy = "salesPerson", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SalesOrder> salesOrders = new ArrayList<>();

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LeadActivity> leadActivities = new ArrayList<>();

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OpportunityActivity> opportunityActivities = new ArrayList<>();

    @OneToMany(mappedBy = "salesPerson", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommissionRule> commissionRules = new ArrayList<>();

    @OneToMany(mappedBy = "salesPerson", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommissionPayment> commissionPayments = new ArrayList<>();
}