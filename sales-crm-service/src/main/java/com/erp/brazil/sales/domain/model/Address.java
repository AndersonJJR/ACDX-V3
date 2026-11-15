package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "addresses")
public class Address extends BaseEntity {
    
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

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
    
    public Address() {}
    
    public Address(UUID companyId, String street, String number, String complement, String neighborhood, 
                   String city, String state, String zipCode, String country, AddressType addressType, 
                   Boolean isDefault, String ibgeCode) {
        this.companyId = companyId;
        this.street = street;
        this.number = number;
        this.complement = complement;
        this.neighborhood = neighborhood;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
        this.addressType = addressType;
        this.isDefault = isDefault;
        this.ibgeCode = ibgeCode;
    }
    
    public UUID getCompanyId() {
        return companyId;
    }
    
    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }
    
    public String getStreet() {
        return street;
    }
    
    public void setStreet(String street) {
        this.street = street;
    }
    
    public String getNumber() {
        return number;
    }
    
    public void setNumber(String number) {
        this.number = number;
    }
    
    public String getComplement() {
        return complement;
    }
    
    public void setComplement(String complement) {
        this.complement = complement;
    }
    
    public String getNeighborhood() {
        return neighborhood;
    }
    
    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getZipCode() {
        return zipCode;
    }
    
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public AddressType getAddressType() {
        return addressType;
    }
    
    public void setAddressType(AddressType addressType) {
        this.addressType = addressType;
    }
    
    public Boolean isDefault() {
        return isDefault;
    }
    
    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public String getIbgeCode() {
        return ibgeCode;
    }
    
    public void setIbgeCode(String ibgeCode) {
        this.ibgeCode = ibgeCode;
    }
    
    public enum AddressType {
        BILLING,
        SHIPPING,
        COMMERCIAL,
        RESIDENTIAL,
        OTHER
    }
}