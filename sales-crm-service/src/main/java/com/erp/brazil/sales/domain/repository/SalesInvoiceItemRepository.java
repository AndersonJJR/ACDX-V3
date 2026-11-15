package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.SalesInvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SalesInvoiceItemRepository extends JpaRepository<SalesInvoiceItem, UUID> {

    List<SalesInvoiceItem> findBySalesInvoiceId(UUID salesInvoiceId);

    void deleteBySalesInvoiceId(UUID salesInvoiceId);
}