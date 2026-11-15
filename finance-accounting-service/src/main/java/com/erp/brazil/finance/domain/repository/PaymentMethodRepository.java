package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.PaymentMethod;
import com.erp.brazil.finance.domain.model.PaymentMethodType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {

    Optional<PaymentMethod> findByCode(String code);

    Optional<PaymentMethod> findByDefaultMethodTrue();

    List<PaymentMethod> findByActiveTrueOrderByCode();

    List<PaymentMethod> findByMethodType(PaymentMethodType methodType);

    Page<PaymentMethod> findByActiveTrue(Pageable pageable);

    Page<PaymentMethod> findAll(Pageable pageable);

    boolean existsByCodeAndIdNot(String code, UUID id);
}