package com.erp.brazil.identity.domain.event;

import com.erp.brazil.common.domain.event.DomainEvent;
import lombok.Getter;
import lombok.Setter
import lombok.ToString;

import java.io.Serializable;

/**
 * Event published when a new company is created.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class CompanyCreatedEvent extends DomainEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String companyId;
    private String code;
    private String name;
    private String tradeName;
    private String cnpj;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String cep;
    private String tenantId;

    public CompanyCreatedEvent() {
        super();
    }

    public CompanyCreatedEvent(String companyId, String code, String name, String tradeName,
                                String cnpj, String email, String phone, String address,
                                String city, String state, String cep, String tenantId) {
        this();
        this.companyId = companyId;
        this.code = code;
        this.name = name;
        this.tradeName = tradeName;
        this.cnpj = cnpj;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.state = state;
        this.cep = cep;
        this.tenantId = tenantId;
    }

    @Override
    public String getAggregateId() {
        return companyId;
    }

    @Override
    public String getAggregateType() {
        return "Company";
    }
}