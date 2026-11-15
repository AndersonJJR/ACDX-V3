package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.QuotationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuotationItemRepository extends JpaRepository<QuotationItem, UUID> {

    List<QuotationItem> findByQuotationId(UUID quotationId);

    void deleteByQuotationId(UUID quotationId);
}