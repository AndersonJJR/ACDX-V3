package com.erp.brazil.inventory.domain.repository;

import com.erp.brazil.inventory.domain.model.StockReservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, UUID> {

    Optional<StockReservation> findByCompanyIdAndVoucher(UUID companyId, String voucherType, String voucherNo);

    List<StockReservation> findByCompanyIdAndItemIdAndWarehouseId(UUID companyId, UUID itemId, UUID warehouseId);

    Page<StockReservation> findByCompanyIdAndItemIdAndWarehouseId(UUID companyId, UUID itemId, UUID warehouseId, Pageable pageable);

    List<StockReservation> findByCompanyIdAndVoucherTypeAndVoucherNo(UUID companyId, String voucherType, String voucherNo);

    List<StockReservation> findByCompanyIdAndStatus(UUID companyId, StockReservation.ReservationStatus status);

    List<StockReservation> findByCompanyIdAndExpiryDateBefore(UUID companyId, LocalDateTime expiryDate);

    List<StockReservation> findActiveByItemAndWarehouse(@Param("companyId") UUID companyId, @Param("itemId") UUID itemId, @Param("warehouseId") UUID warehouseId);

    @Query("SELECT COALESCE(SUM(sr.qtyReserved - sr.qtyDelivered), 0) FROM StockReservation sr WHERE sr.companyId = :companyId AND sr.itemId = :itemId AND sr.warehouseId = :warehouseId AND sr.status IN (:statuses)")
    java.math.BigDecimal getTotalReservedQty(@Param("companyId") UUID companyId, @Param("itemId") UUID itemId, @Param("warehouseId") UUID warehouseId, @Param("statuses") List<StockReservation.ReservationStatus> statuses);

    @Modifying
    @Transactional
    @Query("UPDATE StockReservation sr SET sr.qtyDelivered = sr.qtyDelivered + :deliveredQty, sr.status = CASE WHEN sr.qtyDelivered + :deliveredQty >= sr.qtyReserved THEN 'DELIVERED' WHEN sr.qtyDelivered + :deliveredQty > 0 THEN 'PARTIALLY_DELIVERED' ELSE sr.status END WHERE sr.id = :reservationId")
    int updateDeliveredQty(@Param("reservationId") UUID reservationId, @Param("deliveredQty") java.math.BigDecimal deliveredQty);

    @Modifying
    @Transactional
    @Query("UPDATE StockReservation sr SET sr.status = :status WHERE sr.companyId = :companyId AND sr.voucherType = :voucherType AND sr.voucherNo = :voucherNo")
    int cancelByVoucher(@Param("companyId") UUID companyId, @Param("voucherType") String voucherType, @Param("voucherNo") String voucherNo, @Param("status") StockReservation.ReservationStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE StockReservation sr SET sr.status = 'EXPIRED' WHERE sr.expiryDate IS NOT NULL AND sr.expiryDate < :now AND sr.status = 'ACTIVE'")
    int expireReservations(@Param("now") LocalDateTime now);

    @Query("SELECT sr FROM StockReservation sr WHERE sr.companyId = :companyId AND sr.itemId = :itemId AND sr.warehouseId = :warehouseId AND sr.batchId = :batchId AND sr.status = 'ACTIVE' ORDER BY sr.createdAt ASC")
    List<StockReservation> findActiveByBatch(@Param("companyId") UUID companyId, @Param("itemId") UUID itemId, @Param("warehouseId") UUID warehouseId, @Param("batchId") UUID batchId);

    @Query("SELECT sr FROM StockReservation sr WHERE sr.companyId = :companyId AND sr.itemId = :itemId AND sr.warehouseId = :warehouseId AND sr.serialNumberId = :serialNumberId AND sr.status = 'ACTIVE'")
    Optional<StockReservation> findActiveBySerialNumber(@Param("companyId") UUID companyId, @Param("itemId") UUID itemId, @Param("warehouseId") UUID warehouseId, @Param("serialNumberId") UUID serialNumberId);
}