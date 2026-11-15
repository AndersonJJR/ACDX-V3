package com.erp.brazil.identity.domain.event;

import com.erp.brazil.common.domain.event.DomainEvent;
import lombok.Getter;
import lombok.Setter
import lombok.ToString;

import java.io.Serializable;

/**
 * Event published when a new user is created.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class UserCreatedEvent extends DomainEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String username;
    private String fullName;
    private String email;
    private String cpf;
    private String phone;
    private String companyId;
    private String tenantId;

    public UserCreatedEvent() {
        super();
    }

    public UserCreatedEvent(String userId, String username, String fullName, String email,
                             String cpf, String phone, String companyId, String tenantId) {
        this();
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.cpf = cpf;
        this.phone = phone;
        this.companyId = companyId;
        this.tenantId = tenantId;
    }

    @Override
    public String getAggregateId() {
        return userId;
    }

    @Override
    public String getAggregateType() {
        return "User";
    }
}