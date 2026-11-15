package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "leave_type",
    uniqueConstraints = @UniqueConstraint(name = "uk_leave_type_company_code", columnNames = {"company_id", "code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveType extends BaseEntity {

    @NotNull
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @NotBlank
    @Size(max = 20)
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private LeaveCategory category;

    @Column(name = "max_days_per_year")
    private Integer maxDaysPerYear;

    @Column(name = "requires_approval")
    private Boolean requiresApproval = true;

    @Column(name = "requires_document")
    private Boolean requiresDocument = false;

    @Column(name = "paid")
    private Boolean paid = true;

    @Column(name = "affects_benefits")
    private Boolean affectsBenefits = false;

    @Size(max = 7)
    @Column(name = "color", length = 7)
    private String color;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public boolean isPaidByDefault() {
        return paid != null && paid;
    }

    public boolean requiresDocumentByDefault() {
        return requiresDocument != null && requiresDocument;
    }
}