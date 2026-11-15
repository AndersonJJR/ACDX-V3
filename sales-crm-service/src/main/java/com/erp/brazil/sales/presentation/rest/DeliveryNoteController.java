package com.erp.brazil.sales.presentation.rest;

import com.erp.brazil.sales.application.usecase.CreateDeliveryNoteUseCase;
import com.erp.brazil.sales.domain.model.DeliveryNote;
import com.erp.brazil.sales.domain.model.DeliveryNoteStatus;
import com.erp.brazil.sales.domain.repository.DeliveryNoteRepository;
import com.erp.brazil.sales.domain.repository.SalesOrderRepository;
import com.erp.brazil.sales.domain.repository.SalesOrderItemRepository;
import com.erp.brazil.sales.presentation.dto.ApiResponse;
import com.erp.brazil.sales.presentation.dto.DeliveryNoteDTO;
import com.erp.brazil.sales.presentation.dto.PageResponse;
import com.erp.brazil.sales.presentation.dto.request.CancelDeliveryNoteRequest;
import com.erp.brazil.sales.presentation.dto.request.ConfirmDeliveryNoteRequest;
import com.erp.brazil.sales.presentation.dto.request.CreateDeliveryNoteItemRequest;
import com.erp.brazil.sales.presentation.dto.request.CreateDeliveryNoteRequest;
import com.erp.brazil.sales.presentation.dto.request.DeliverItemQuantityRequest;
import com.erp.brazil.sales.presentation.dto.request.DeliverItemsRequest;
import com.erp.brazil.sales.presentation.dto.request.UpdateDeliveryNoteRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/delivery-notes")
public class DeliveryNoteController {

    private final DeliveryNoteRepository deliveryNoteRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final CreateDeliveryNoteUseCase createDeliveryNoteUseCase;

    public DeliveryNoteController(DeliveryNoteRepository deliveryNoteRepository,
                                  SalesOrderRepository salesOrderRepository,
                                  SalesOrderItemRepository salesOrderItemRepository,
                                  CreateDeliveryNoteUseCase createDeliveryNoteUseCase) {
        this.deliveryNoteRepository = deliveryNoteRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderItemRepository = salesOrderItemRepository;
        this.createDeliveryNoteUseCase = createDeliveryNoteUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeliveryNoteDTO>> create(@RequestHeader("X-Company-ID") UUID companyId,
                                                                @RequestHeader("X-User-ID") UUID userId,
                                                                @RequestBody CreateDeliveryNoteRequest request) {
        DeliveryNoteDTO deliveryNote = createDeliveryNoteUseCase.execute(request.salesOrderId(), userId, request);
        return ResponseEntity.ok(ApiResponse.ok(deliveryNote));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryNoteDTO>> getById(@RequestHeader("X-Company-ID") UUID companyId,
                                                                 @PathVariable UUID id) {
        DeliveryNote dn = deliveryNoteRepository.findById(id)
                .filter(d -> d.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Delivery note not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(dn)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryNoteDTO>> update(@RequestHeader("X-Company-ID") UUID companyId,
                                                                @PathVariable UUID id,
                                                                @RequestBody UpdateDeliveryNoteRequest request) {
        DeliveryNote dn = deliveryNoteRepository.findById(id)
                .filter(d -> d.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Delivery note not found: " + id));

        if (dn.getStatus() != DeliveryNoteStatus.DRAFT) {
            throw new IllegalStateException("Can only update delivery notes in DRAFT status");
        }

        if (request.deliveryDate() != null) dn.setDeliveryDate(request.deliveryDate());
        if (request.deliveryType() != null) dn.setDeliveryType(request.deliveryType());
        if (request.carrier() != null) dn.setCarrier(request.carrier());
        if (request.trackingCode() != null) dn.setTrackingCode(request.trackingCode());
        if (request.vehiclePlate() != null) dn.setVehiclePlate(request.vehiclePlate());
        if (request.driverName() != null) dn.setDriverName(request.driverName());
        if (request.driverDocument() != null) dn.setDriverDocument(request.driverDocument());
        if (request.freightValue() != null) dn.setFreightValue(request.freightValue());
        if (request.insuranceValue() != null) dn.setInsuranceValue(request.insuranceValue());
        if (request.totalWeight() != null) dn.setTotalWeight(request.totalWeight());
        if (request.totalVolume() != null) dn.setTotalVolume(request.totalVolume());
        if (request.notes() != null) dn.setNotes(request.notes());
        if (request.shippingAddressId() != null) {
            dn.setShippingAddress(new com.erp.brazil.sales.domain.model.Address());
            dn.getShippingAddress().setId(request.shippingAddressId());
        }
        if (request.carrierId() != null) dn.setCarrierId(request.carrierId());

        dn = deliveryNoteRepository.save(dn);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(dn)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@RequestHeader("X-Company-ID") UUID companyId,
                                                     @PathVariable UUID id) {
        DeliveryNote dn = deliveryNoteRepository.findById(id)
                .filter(d -> d.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Delivery note not found: " + id));

        if (dn.getStatus() != DeliveryNoteStatus.DRAFT) {
            throw new IllegalStateException("Can only delete delivery notes in DRAFT status");
        }

        deliveryNoteRepository.delete(dn);
        return ResponseEntity.ok(ApiResponse.ok(null, "Delivery note deleted successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DeliveryNoteDTO>>> list(@RequestHeader("X-Company-ID") UUID companyId,
                                                                            @RequestParam(required = false) DeliveryNoteStatus status,
                                                                            Pageable pageable) {
        Page<DeliveryNote> page;
        if (status != null) {
            page = deliveryNoteRepository.findByCompanyIdAndStatus(companyId, status, pageable);
        } else {
            page = deliveryNoteRepository.findByCompanyId(companyId, pageable);
        }
        return ResponseEntity.ok(ApiResponse.ok(mapToPageResponse(page)));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<DeliveryNoteDTO>> confirm(@RequestHeader("X-Company-ID") UUID companyId,
                                                                 @PathVariable UUID id,
                                                                 @RequestBody ConfirmDeliveryNoteRequest request) {
        DeliveryNote dn = deliveryNoteRepository.findById(id)
                .filter(d -> d.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Delivery note not found: " + id));

        if (!dn.canBeConfirmed()) {
            throw new IllegalStateException("Delivery note cannot be confirmed");
        }

        dn.setStatus(DeliveryNoteStatus.CONFIRMED);
        dn.setConfirmedAt(java.time.LocalDateTime.now());
        dn.setConfirmedById(request.confirmedById());

        dn = deliveryNoteRepository.save(dn);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(dn)));
    }

    @PostMapping("/{id}/deliver")
    public ResponseEntity<ApiResponse<DeliveryNoteDTO>> deliver(@RequestHeader("X-Company-ID") UUID companyId,
                                                                 @PathVariable UUID id,
                                                                 @RequestBody DeliverItemsRequest request) {
        DeliveryNote dn = deliveryNoteRepository.findById(id)
                .filter(d -> d.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Delivery note not found: " + id));

        if (dn.getStatus() != DeliveryNoteStatus.CONFIRMED) {
            throw new IllegalStateException("Delivery note must be confirmed before delivery");
        }

        for (DeliverItemQuantityRequest itemReq : request.items()) {
            var item = dn.getItems().stream()
                    .filter(i -> i.getId().equals(itemReq.itemId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Delivery note item not found: " + itemReq.itemId()));

            var orderItem = item.getSalesOrderItem();
            BigDecimal pendingQty = orderItem.getQuantity().subtract(orderItem.getDeliveredQuantity());
            if (itemReq.quantity().compareTo(pendingQty) > 0) {
                throw new IllegalStateException("Delivery quantity exceeds pending quantity for item: " + orderItem.getProductName());
            }

            orderItem.setDeliveredQuantity(orderItem.getDeliveredQuantity().add(itemReq.quantity()));
        }

        salesOrderItemRepository.saveAll(dn.getItems().stream().map(DeliveryNoteDTO.DeliveryNoteItemDTO::getSalesOrderItemId).map(salesOrderItemRepository::findById).map(opt -> opt.orElseThrow()).toList());

        boolean fullyDelivered = dn.getSalesOrder().getItems().stream()
                .allMatch(i -> i.getDeliveredQuantity().compareTo(i.getQuantity()) >= 0);

        if (fullyDelivered) {
            dn.setStatus(DeliveryNoteStatus.DELIVERED);
        } else {
            dn.setStatus(DeliveryNoteStatus.PARTIALLY_DELIVERED);
        }

        dn = deliveryNoteRepository.save(dn);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(dn)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<DeliveryNoteDTO>> cancel(@RequestHeader("X-Company-ID") UUID companyId,
                                                                @PathVariable UUID id,
                                                                @RequestBody CancelDeliveryNoteRequest request) {
        DeliveryNote dn = deliveryNoteRepository.findById(id)
                .filter(d -> d.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Delivery note not found: " + id));

        dn.setStatus(DeliveryNoteStatus.CANCELLED);
        dn.setCancelledAt(java.time.LocalDateTime.now());
        dn.setCancelledById(request.cancelledById());
        dn.setCancellationReason(request.cancellationReason());

        dn = deliveryNoteRepository.save(dn);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(dn)));
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

    private PageResponse<DeliveryNoteDTO> mapToPageResponse(Page<DeliveryNote> page) {
        return new PageResponse<>(
                page.getContent().stream().map(this::mapToDTO).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast(), page.isEmpty()
        );
    }
}