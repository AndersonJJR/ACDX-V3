package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {

    List<Contact> findByCustomerId(UUID customerId);

    Optional<Contact> findByCustomerIdAndIsPrimaryTrue(UUID customerId);
}