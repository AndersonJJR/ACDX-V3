package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.SalesOrderHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SalesOrderHistoryRepository extends JpaRepository<SalesOrderHistory, UUID> {

    List<SalesOrderHistory> findBySalesOrderIdOrderByCreatedAtDesc(UUID salesOrderId);

    Page<SalesOrderHistory> findBySalesOrderId(UUID salesOrderId, Pageable pageable);
}