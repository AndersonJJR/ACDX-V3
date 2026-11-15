package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.Vendor;
import com.erp.brazil.finance.domain.model.VendorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VendorRepository extends JpaRepository<Vendor, UUID> {

    Optional<Vendor> findByCode(String code);

    Optional<Vendor> findByDocumentNumber(String documentNumber);

    List<Vendor> findByActiveTrueOrderByName();

    List<Vendor> findByStatus(VendorStatus status);

    List<Vendor> findByNameContainingIgnoreCase(String name);

    @Query("SELECT v FROM Vendor v WHERE v.name ILIKE %:name% OR v.tradeName ILIKE %:name% OR v.documentNumber LIKE %:name%")
    Page<Vendor> searchByNameOrDocument(@Param("name") String name, Pageable pageable);

    Page<Vendor> findByStatus(VendorStatus status, Pageable pageable);

    Page<Vendor> findAll(Pageable pageable);

    boolean existsByCodeAndIdNot(String code, UUID id);

    boolean existsByDocumentNumberAndIdNot(String documentNumber, UUID id);
}