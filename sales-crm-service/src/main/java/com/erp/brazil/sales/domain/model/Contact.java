package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "contacts")
public class Contact extends BaseEntity {
    
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "first_name", length = 100, nullable = false)
    private String firstName;
    
    @Column(name = "last_name", length = 100, nullable = false)
    private String lastName;
    
    @Column(name = "email", length = 150)
    private String email;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "mobile", length = 20)
    private String mobile;
    
    @Column(name = "position", length = 100)
    private String position;
    
    @Column(name = "department", length = 100)
    private String department;
    
    @Column(name = "is_primary")
    private Boolean isPrimary = false;
    
    @Column(name = "receives_marketing")
    private Boolean receivesMarketing = false;
    
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id")
    private Address address;
    
    @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LGPDConsent> lgpdConsents = new ArrayList<>();
    
    public Contact() {}
    
    public UUID getCompanyId() {
        return companyId;
    }
    
    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getMobile() {
        return mobile;
    }
    
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    
    public String getPosition() {
        return position;
    }
    
    public void setPosition(String position) {
        this.position = position;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public Boolean isPrimary() {
        return isPrimary;
    }
    
    public void setPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
    
    public Boolean getReceivesMarketing() {
        return receivesMarketing;
    }
    
    public void setReceivesMarketing(Boolean receivesMarketing) {
        this.receivesMarketing = receivesMarketing;
    }
    
    public LocalDate getBirthDate() {
        return birthDate;
    }
    
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public Address getAddress() {
        return address;
    }
    
    public void setAddress(Address address) {
        this.address = address;
    }
    
    public List<LGPDConsent> getLgpdConsents() {
        return lgpdConsents;
    }
    
    public void setLgpdConsents(List<LGPDConsent> lgpdConsents) {
        this.lgpdConsents = lgpdConsents;
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
}