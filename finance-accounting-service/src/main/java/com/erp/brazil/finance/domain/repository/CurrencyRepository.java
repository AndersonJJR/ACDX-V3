package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.Currency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CurrencyRepository extends JpaRepository<Currency, UUID> {

    Optional<Currency> findByCode(String code);

    Optional<Currency> findByIsoCode(String isoCode);

    Optional<Currency> findByBaseCurrencyTrue();

    List<Currency> findByActiveTrueOrderByCode();

    Page<Currency> findByActiveTrue(Pageable pageable);

    Page<Currency> findAll(Pageable pageable);

    boolean existsByCodeAndIdNot(String code, UUID id);

    boolean existsByIsoCodeAndIdNot(String isoCode, UUID id);
}