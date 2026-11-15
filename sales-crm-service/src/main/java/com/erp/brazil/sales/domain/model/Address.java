package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Address extends BaseEntity {
    
    @Column(name = "street", length = 200)
    private String street;
    
    @Column(name = "number", length = 20)
    private String number;
    
    @Column(name = "complement", length = 100)
    private String complement;
    
    @Column(name = "neighborhood", length = 100)
    private String neighborhood;
    
    @Column(name = "city", length = 100, nullable = false)
    private String city;
    
    @Column(name = "state", length = 2, nullable = false)
    private String state;
    
    @Column(name = "zip_code", length = 9, nullable = false)
    private String zipCode;
    
    @Column(name = "country", length = 50)
    private String country = "Brasil";
    
    @Column(name = "address_type", length = 20)
    @Enumerated(EnumType.STRING)
    private AddressType addressType;
    
    @Column(name = "is_default")
    private Boolean isDefault = false;
    
    @Column(name = "ibge_code", length = 7)
    private String ibgeCode;
    
    public enum AddressType {
        BILLING,
        SHIPPING,
        COMMERCIAL,
        RESIDENTIAL,
        OTHER
    }
}