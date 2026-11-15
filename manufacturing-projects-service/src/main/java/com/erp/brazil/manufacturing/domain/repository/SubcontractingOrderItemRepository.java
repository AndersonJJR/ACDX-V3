package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.SubcontractingOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubcontractingOrderItemRepository extends JpaRepository<SubcontractingOrderItem, UUID> {

    List<SubcontractingOrderItem> findBySubcontractingOrderId(UUID subcontractingOrderId);

    List<SubcontractingOrderItem> findBySubcontractingOrderIdAndComponentId(UUID subcontractingOrderId, UUID componentId);

    @Query("SELECT soi FROM SubcontractingOrderItem soi WHERE soi.subcontractingOrder.id = :orderId AND soi.quantity > soi.sentQuantity")
    List<SubcontractingOrderItem> findWithRemainingToSend(@Param("orderId") UUID orderId);

    @Query("SELECT soi FROM SubcontractingOrderItem soi WHERE soi.subcontractingOrder.id = :orderId AND soi.sentQuantity > soi.returnedQuantity + soi.consumedQuantity + soi.scrappedQuantity")
    List<SubcontractingOrderItem> findWithRemainingToReturn(@Param("orderId") UUID orderId);
}