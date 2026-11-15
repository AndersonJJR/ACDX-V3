package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "holiday",
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

    @Column(name = "company_id")
    private Long companyId;

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
    private HolidayType type;

    @Column(name = "is_recurring")
    private Boolean isRecurring = false;

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
    private Boolean isWorkingDay = false;

    public boolean isNational() {
        return type == HolidayType.NATIONAL;
    }

    public boolean appliesToCompany(Long companyId) {
        if (this.companyId == null) return true; // National holiday
        return this.companyId.equals(companyId);
    }

    public boolean appliesToLocation(Long companyId, String state, String city) {
        if (!appliesToCompany(companyId)) return false;
        if (type == HolidayType.STATE && this.state != null) {
            return this.state.equals(state);
        }
        if (type == HolidayType.MUNICIPAL && this.city != null) {
            return this.city.equals(city);
        }
        return true;
    }
}