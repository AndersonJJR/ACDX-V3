package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> findByCustomerId(UUID customerId);

    List<Address> findByCustomerIdAndAddressType(UUID customerId, Address.AddressType addressType);

    Optional<Address> findByCustomerIdAndAddressTypeAndIsDefaultTrue(UUID customerId, Address.AddressType addressType);
}