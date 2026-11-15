package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.QuotationVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuotationVersionRepository extends JpaRepository<QuotationVersion, UUID> {

    List<QuotationVersion> findByQuotationIdOrderByVersionDesc(UUID quotationId);

    Optional<QuotationVersion> findByQuotationIdAndVersion(UUID quotationId, Integer version);
}