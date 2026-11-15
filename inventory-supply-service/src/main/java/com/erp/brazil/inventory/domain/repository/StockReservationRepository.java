package com.erp.brazil.inventory.domain.repository;

import com.erp.brazil.inventory.domain.model.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, UUID> {

    List<StockReservation> findByCompanyIdAndItemIdAndWarehouseIdAndStatus(UUID companyId, UUID itemId, UUID warehouseId, StockReservation.ReservationStatus status);

    List<StockReservation> findByVoucherTypeAndVoucherNo(String voucherType, String voucherNo);

    @Query("SELECT COALESCE(SUM(sr.qtyReserved - sr.qtyDelivered), 0) FROM StockReservation sr WHERE sr.companyId = :companyId AND sr.itemId = :itemId AND sr.warehouseId = :warehouseId AND sr.status IN (:statuses)")
    BigDecimal getTotalReservedQty(@Param("companyId") UUID companyId, @Param("itemId") UUID itemId, @Param("warehouseId") UUID warehouseId, @Param("statuses") List<StockReservation.ReservationStatus> statuses);

    @Query("SELECT sr FROM StockReservation sr WHERE sr.companyId = :companyId AND sr.itemId = :itemId AND sr.warehouseId = :warehouseId AND sr.status = :status ORDER BY sr.createdAt ASC")
    List<StockReservation> findActiveReservationsFifo(@Param("companyId") UUID companyId, @Param("itemId") UUID itemId, @Param("warehouseId") UUID warehouseId, @Param("status") StockReservation.ReservationStatus status);

    List<StockReservation> findByCompanyIdAndExpiryDateBeforeAndStatus(UUID companyId, LocalDateTime expiryDate, StockReservation.ReservationStatus status);

    @Query("SELECT sr FROM StockReservation sr WHERE sr.companyId = :companyId AND sr.referenceDoctype = :refType AND sr.referenceName = :refName")
    List<StockReservation> findByReference(@Param("companyId") UUID companyId, @Param("refType") String refType, @Param("refName") String refName);

    boolean existsByVoucherTypeAndVoucherNoAndVoucherDetailNo(String voucherType, String voucherNo, String voucherDetailNo);
}