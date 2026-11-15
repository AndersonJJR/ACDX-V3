package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.PaymentTerm;
import com.erp.brazil.finance.domain.model.PaymentTermType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentTermRepository extends JpaRepository<PaymentTerm, UUID> {

    Optional<PaymentTerm> findByCode(String code);

    Optional<PaymentTerm> findByDefaultTermTrue();

    List<PaymentTerm> findByActiveTrueOrderByCode();

    List<PaymentTerm> findByTermType(PaymentTermType termType);

    Page<PaymentTerm> findByActiveTrue(Pageable pageable);

    Page<PaymentTerm> findAll(Pageable pageable);

    boolean existsByCodeAndIdNot(String code, UUID id);
}