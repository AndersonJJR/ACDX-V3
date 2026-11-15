package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "holiday",
    uniqueConstraints = @UniqueConstraint(name = "uk_holiday_uuid", columnNames = "uuid"),
    indexes = {
        @Index(name = "idx_holiday_date", columnList = "date"),
        @Index(name = "idx_holiday_company", columnList = "company_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holiday extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private Type type;

    @Column(name = "is_recurring")
    private boolean isRecurring = false;

    @Size(max = 5)
    @Column(name = "month_day", length = 5)
    private String monthDay;

    @Size(max = 2)
    @Column(name = "state", length = 2)
    private String state;

    @Size(max = 100)
    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "is_working_day")
    private boolean isWorkingDay = false;

    public boolean appliesToCompany(Long companyId) {
        if (companyId == null) return false;
        // National holidays apply to all
        if (type == Type.NATIONAL) return true;
        // Company-specific holidays
        if (type == Type.COMPANY && company != null && company.getId().equals(companyId)) return true;
        // State holidays
        if (type == Type.STATE && company != null && companyId.equals(company.getId())) return true;
        // Municipal holidays
        if (type == Type.MUNICIPAL && company != null && companyId.equals(company.getId())) return true;
        return false;
    }

    public enum Type {
        NATIONAL, STATE, MUNICIPAL, COMPANY, RELIGIOUS, OPTIONAL
    }
}