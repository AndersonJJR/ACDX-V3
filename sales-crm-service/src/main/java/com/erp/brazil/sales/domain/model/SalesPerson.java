package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sales_persons")
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
        private List<Customer> customers = new ArrayList<>();

    @OneToMany(mappedBy = "salesPerson", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Lead> leads = new ArrayList<>();

    @OneToMany(mappedBy = "salesPerson", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Opportunity> opportunities = new ArrayList<>();

    @OneToMany(mappedBy = "preparedBy", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Quotation> preparedQuotations = new ArrayList<>();

    @OneToMany(mappedBy = "approvedBy", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Quotation> approvedQuotations = new ArrayList<>();

    @OneToMany(mappedBy = "salesPerson", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<SalesOrder> salesOrders = new ArrayList<>();

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<LeadActivity> leadActivities = new ArrayList<>();

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<OpportunityActivity> opportunityActivities = new ArrayList<>();

    @OneToMany(mappedBy = "salesPerson", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<CommissionRule> commissionRules = new ArrayList<>();

    @OneToMany(mappedBy = "salesPerson", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<CommissionPayment> commissionPayments = new ArrayList<>();

    public UUID getCompanyId() { return companyId; }
    public String getEmployeeId() { return employeeId; }
    public String getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getMobile() { return mobile; }
    public BigDecimal getCommissionRate() { return commissionRate; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public java.time.LocalDate getHireDate() { return hireDate; }
    public java.time.LocalDate getTerminationDate() { return terminationDate; }
    public Territory getTerritory() { return territory; }
    public void setTerritory(Territory territory) { this.territory = territory; }
    public SalesTeam getSalesTeam() { return salesTeam; }
    public void setSalesTeam(SalesTeam salesTeam) { this.salesTeam = salesTeam; }
    public List<Customer> getCustomers() { return customers; }
    public List<Lead> getLeads() { return leads; }
    public List<Opportunity> getOpportunities() { return opportunities; }
    public List<Quotation> getPreparedQuotations() { return preparedQuotations; }
    public List<Quotation> getApprovedQuotations() { return approvedQuotations; }
    public List<SalesOrder> getSalesOrders() { return salesOrders; }
    public List<LeadActivity> getLeadActivities() { return leadActivities; }
    public List<OpportunityActivity> getOpportunityActivities() { return opportunityActivities; }
    public List<CommissionRule> getCommissionRules() { return commissionRules; }
    public List<CommissionPayment> getCommissionPayments() { return commissionPayments; }
}