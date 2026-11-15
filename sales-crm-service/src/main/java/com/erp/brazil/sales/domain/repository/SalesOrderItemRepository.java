package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.SalesOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SalesOrderItemRepository extends JpaRepository<SalesOrderItem, UUID> {

    List<SalesOrderItem> findBySalesOrderId(UUID salesOrderId);

    void deleteBySalesOrderId(UUID salesOrderId);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(oi.quantity - oi.deliveredQuantity) FROM SalesOrderItem oi WHERE oi.salesOrder.id = :salesOrderId")
    java.math.BigDecimal sumPendingQuantityBySalesOrder(@org.springframework.data.repository.query.Param("salesOrderId") UUID salesOrderId);
}