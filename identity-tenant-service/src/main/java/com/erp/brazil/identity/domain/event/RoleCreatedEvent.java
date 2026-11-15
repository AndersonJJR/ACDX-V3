package com.erp.brazil.identity.domain.event;

import com.erp.brazil.common.domain.event.DomainEvent;
import lombok.Getter;
import lombok.Setter
import lombok.ToString;

import java.io.Serializable;

/**
 * Event published when a new role is created.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class RoleCreatedEvent extends DomainEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String roleId;
    private String code;
    private String name;
    private String description;
    private String roleType;
    private String companyId;
    private String tenantId;

    public RoleCreatedEvent() {
        super();
    }

    public RoleCreatedEvent(String roleId, String code, String name, String description,
                             String roleType, String companyId, String tenantId) {
        this();
        this.roleId = roleId;
        this.code = code;
        this.name = name;
        this.description = description;
        this.roleType = roleType;
        this.companyId = companyId;
        this.tenantId = tenantId;
    }

    @Override
    public String getAggregateId() {
        return roleId;
    }

    @Override
    public String getAggregateType() {
        return "Role";
    }
}