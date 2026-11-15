package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "leave_type",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_leave_type_uuid", columnNames = "uuid"),
        @UniqueConstraint(name = "uk_leave_type_company_code", columnNames = {"company_id", "code"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveType extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

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
    @Column(name = "category", nullable = false, length = 20)
    private Category category;

    @Column(name = "max_days_per_year")
    private Integer maxDaysPerYear;

    @Column(name = "requires_approval")
    private boolean requiresApproval = true;

    @Column(name = "requires_document")
    private boolean requiresDocument = false;

    @Column(name = "paid")
    private boolean paid = true;

    @Column(name = "affects_benefits")
    private boolean affectsBenefits = false;

    @Size(max = 7)
    @Column(name = "color", length = 7)
    private String color;

    @Column(name = "is_active")
    private boolean isActive = true;

    public enum Category {
        VACATION, MEDICAL, MATERNITY, PATERNITY, MARRIAGE, BEREAVEMENT,
        STUDY, ELECTION, MILITARY, BLOOD_DONATION, UNPAID, COMPENSATORY, OTHER
    }
}