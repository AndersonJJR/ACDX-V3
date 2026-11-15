package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.DeliveryNoteItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeliveryNoteItemRepository extends JpaRepository<DeliveryNoteItem, UUID> {

    List<DeliveryNoteItem> findByDeliveryNoteId(UUID deliveryNoteId);

    void deleteByDeliveryNoteId(UUID deliveryNoteId);
}