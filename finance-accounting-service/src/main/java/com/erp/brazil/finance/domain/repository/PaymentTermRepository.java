package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.PaymentTerm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTermRepository extends JpaRepository<PaymentTerm, UUID> {

    Optional<PaymentTerm> findByCodeAndCompanyId(String code, UUID companyId);

    List<PaymentTerm> findByCompanyIdOrderByCode(UUID companyId);

    List<PaymentTerm> findByCompanyIdAndActiveTrueOrderByCode(UUID companyId);

    Page<PaymentTerm> findByCompanyId(UUID companyId, Pageable pageable);

    boolean existsByCodeAndCompanyId(String code, UUID companyId);
}