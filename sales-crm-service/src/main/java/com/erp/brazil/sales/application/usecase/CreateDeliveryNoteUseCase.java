package com.erp.brazil.sales.application.usecase;

import com.erp.brazil.sales.domain.model.*;
import com.erp.brazil.sales.domain.repository.*;
import com.erp.brazil.sales.presentation.dto.request.CreateDeliveryNoteRequest;
import com.erp.brazil.sales.presentation.dto.DeliveryNoteDTO;
import com.erp.brazil.sales.infrastructure.event.DeliveryNoteCreatedEvent;
import com.erp.brazil.sales.infrastructure.event.EventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CreateDeliveryNoteUseCase {

    private final SalesOrderRepository salesOrderRepository;
    private final DeliveryNoteRepository deliveryNoteRepository;
    private final DeliveryNoteItemRepository deliveryNoteItemRepository;
    private final AddressRepository addressRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final EventPublisher eventPublisher;

    public CreateDeliveryNoteUseCase(SalesOrderRepository salesOrderRepository,
                                     DeliveryNoteRepository deliveryNoteRepository,
                                     DeliveryNoteItemRepository deliveryNoteItemRepository,
                                     AddressRepository addressRepository,
                                     SalesOrderItemRepository salesOrderItemRepository,
                                     EventPublisher eventPublisher) {
        this.salesOrderRepository = salesOrderRepository;
        this.deliveryNoteRepository = deliveryNoteRepository;
        this.deliveryNoteItemRepository = deliveryNoteItemRepository;
        this.addressRepository = addressRepository;
        this.salesOrderItemRepository = salesOrderItemRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public DeliveryNoteDTO execute(UUID salesOrderId, UUID createdById, CreateDeliveryNoteRequest request) {
        SalesOrder salesOrder = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Sales order not found: " + salesOrderId));

        if (salesOrder.getStatus() == SalesOrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot create delivery note for cancelled order");
        }

        Address shippingAddress = null;
        if (request.shippingAddressId() != null) {
            shippingAddress = addressRepository.findById(request.shippingAddressId())
                    .orElseThrow(() -> new IllegalArgumentException("Shipping address not found: " + request.shippingAddressId()));
        }

        String number = generateDeliveryNoteNumber(salesOrder.getCompanyId());

        DeliveryNote deliveryNote = new DeliveryNote();
        deliveryNote.setCompanyId(salesOrder.getCompanyId());
        deliveryNote.setNumber(number);
        deliveryNote.setSeries("1");
        deliveryNote.setSalesOrderId(salesOrder.getId());
        deliveryNote.setDeliveryDate(request.deliveryDate());
        deliveryNote.setStatus(DeliveryNoteStatus.DRAFT);
        deliveryNote.setDeliveryType(request.deliveryType());
        deliveryNote.setCarrier(request.carrier());
        deliveryNote.setTrackingCode(request.trackingCode());
        deliveryNote.setVehiclePlate(request.vehiclePlate());
        deliveryNote.setDriverName(request.driverName());
        deliveryNote.setDriverDocument(request.driverDocument());
        deliveryNote.setFreightValue(request.freightValue() != null ? request.freightValue() : BigDecimal.ZERO);
        deliveryNote.setInsuranceValue(request.insuranceValue() != null ? request.insuranceValue() : BigDecimal.ZERO);
        deliveryNote.setTotalWeight(request.totalWeight());
        deliveryNote.setTotalVolume(request.totalVolume());
        deliveryNote.setNotes(request.notes());
        deliveryNote.setShippingAddress(shippingAddress);
        deliveryNote.setCarrierId(request.carrierId());

        Map<UUID, SalesOrderItem> orderItemMap = salesOrder.getItems().stream()
                .collect(Collectors.toMap(SalesOrderItem::getId, item -> item));

        List<DeliveryNoteItem> items = request.items().stream().map(itemReq -> {
            SalesOrderItem orderItem = orderItemMap.get(itemReq.salesOrderItemId());
            if (orderItem == null) {
                throw new IllegalArgumentException("Sales order item not found: " + itemReq.salesOrderItemId());
            }

            BigDecimal pendingQty = orderItem.getQuantity().subtract(orderItem.getDeliveredQuantity());
            if (itemReq.quantity().compareTo(pendingQty) > 0) {
                throw new IllegalStateException("Delivery quantity exceeds pending quantity for item: " + orderItem.getProductName());
            }

            DeliveryNoteItem item = new DeliveryNoteItem();
            item.setDeliveryNote(deliveryNote);
            item.setLineNumber(itemReq.lineNumber());
            item.setSalesOrderItem(orderItem);
            item.setProductId(itemReq.productId());
            item.setProductCode(itemReq.productCode());
            item.setProductName(itemReq.productName());
            item.setQuantity(itemReq.quantity());
            item.setUnitOfMeasure(itemReq.unitOfMeasure());
            item.setUnitWeight(itemReq.unitWeight());
            item.setUnitVolume(itemReq.unitVolume());
            item.setBatchNumber(itemReq.batchNumber());
            item.setSerialNumbers(itemReq.serialNumbers());
            item.setNotes(itemReq.notes());

            orderItem.setDeliveredQuantity(orderItem.getDeliveredQuantity().add(itemReq.quantity()));

            return item;
        }).collect(Collectors.toList());

        deliveryNote.setItems(items);
        deliveryNote = deliveryNoteRepository.save(deliveryNote);

        salesOrderItemRepository.saveAll(items.stream().map(DeliveryNoteItem::getSalesOrderItem).toList());

        boolean fullyDelivered = salesOrder.getItems().stream()
                .allMatch(i -> i.getDeliveredQuantity().compareTo(i.getQuantity()) >= 0);

        if (fullyDelivered) {
            salesOrder.setStatus(SalesOrderStatus.DELIVERED);
        } else {
            salesOrder.setStatus(SalesOrderStatus.PARTIALLY_DELIVERED);
        }
        salesOrderRepository.save(salesOrder);

        eventPublisher.publish(new DeliveryNoteCreatedEvent(
                deliveryNote.getId(),
                deliveryNote.getCompanyId(),
                deliveryNote.getNumber(),
                deliveryNote.getSalesOrderId(),
                deliveryNote.getDeliveryDate(),
                deliveryNote.getItems().stream().map(i -> new DeliveryNoteCreatedEvent.DeliveryItemEvent(
                        i.getProductId(),
                        i.getQuantity()
                )).collect(Collectors.toList()),
                LocalDateTime.now()
        ));

        return mapToDTO(deliveryNote);
    }

    private String generateDeliveryNoteNumber(UUID companyId) {
        long count = deliveryNoteRepository.countByCompanyId(companyId);
        return String.format("DEL-%06d", count + 1);
    }

    private DeliveryNoteDTO mapToDTO(DeliveryNote dn) {
        List<DeliveryNoteDTO.DeliveryNoteItemDTO> itemDTOs = dn.getItems().stream().map(item ->
                new DeliveryNoteDTO.DeliveryNoteItemDTO(
                        item.getId(), item.getLineNumber(), item.getSalesOrderItem() != null ? item.getSalesOrderItem().getId() : null,
                        item.getProductId(), item.getProductCode(), item.getProductName(),
                        item.getQuantity(), item.getUnitOfMeasure(),
                        item.getUnitWeight(), item.getUnitVolume(),
                        item.getBatchNumber(), item.getSerialNumbers(), item.getNotes()
                )
        ).collect(Collectors.toList());

        return new DeliveryNoteDTO(
                dn.getId(), dn.getCompanyId(), dn.getNumber(), dn.getSeries(), dn.getSalesOrderId(),
                dn.getSalesOrder() != null ? dn.getSalesOrder().getNumber() : null,
                dn.getDeliveryDate(), dn.getStatus(), dn.getDeliveryType(), dn.getCarrier(),
                dn.getTrackingCode(), dn.getVehiclePlate(), dn.getDriverName(), dn.getDriverDocument(),
                dn.getFreightValue(), dn.getInsuranceValue(), dn.getTotalWeight(), dn.getTotalVolume(),
                dn.getNotes(), dn.getConfirmedAt(), dn.getConfirmedById(), null,
                dn.getCancelledAt(), dn.getCancelledById(), null, dn.getCancellationReason(),
                dn.getShippingAddress() != null ? dn.getShippingAddress().getId() : null,
                dn.getCarrierId(), dn.getCreatedAt(), dn.getUpdatedAt(), itemDTOs
        );
    }
}