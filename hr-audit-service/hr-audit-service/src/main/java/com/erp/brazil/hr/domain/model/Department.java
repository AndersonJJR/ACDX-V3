package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "department",
    uniqueConstraints = @UniqueConstraint(name = "uk_department_company_code", columnNames = {"company_id", "code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department extends BaseEntity {

    @NotNull
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @NotBlank
    @Size(max = 20)
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @NotBlank
    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Department parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Department> children = new ArrayList<>();

    @Column(name = "cost_center_id")
    private Long costCenterId;

    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public String getFullPath() {
        if (parent == null) {
            return name;
        }
        return parent.getFullPath() + " > " + name;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public List<Department> getAllChildren() {
        List<Department> all = new ArrayList<>();
        collectChildren(this, all);
        return all;
    }

    private void collectChildren(Department dept, List<Department> all) {
        for (Department child : dept.getChildren()) {
            all.add(child);
            collectChildren(child, all);
        }
    }
}