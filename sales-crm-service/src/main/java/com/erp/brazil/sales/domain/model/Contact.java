package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "contacts")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
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
    
    // LGPD Consent tracking
    @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<LGPDConsent> lgpdConsents = new java.util.ArrayList<>();
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
}