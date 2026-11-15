package com.erp.brazil.inventory.application.usecase;

import com.erp.brazil.inventory.domain.model.StockReservation;
import com.erp.brazil.inventory.domain.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnreserveStockUseCase {

    private final StockReservationRepository reservationRepository;

    @Transactional
    public void unreserveByVoucher(String voucherType, String voucherNo) {
        List<StockReservation> reservations = reservationRepository.findByVoucherTypeAndVoucherNo(voucherType, voucherNo);
        for (StockReservation reservation : reservations) {
            if (reservation.getStatus() == StockReservation.ReservationStatus.ACTIVE ||
                reservation.getStatus() == StockReservation.ReservationStatus.PARTIALLY_DELIVERED) {
                reservation.setStatus(StockReservation.ReservationStatus.CANCELLED);
                reservationRepository.save(reservation);
                log.info("Unreserved stock for voucher: {} - {}", voucherType, voucherNo);
            }
        }
    }

    @Transactional
    public void unreserveByReference(UUID companyId, String referenceDoctype, String referenceName) {
        List<StockReservation> reservations = reservationRepository.findByReference(
                companyId,
                referenceDoctype,
                referenceName);
        for (StockReservation reservation : reservations) {
            if (reservation.getStatus() == StockReservation.ReservationStatus.ACTIVE ||
                reservation.getStatus() == StockReservation.ReservationStatus.PARTIALLY_DELIVERED) {
                reservation.setStatus(StockReservation.ReservationStatus.CANCELLED);
                reservationRepository.save(reservation);
            }
        }
    }

    @Transactional
    public void cancelReservation(UUID reservationId) {
        StockReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        if (reservation.getStatus() == StockReservation.ReservationStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel already delivered reservation");
        }

        reservation.setStatus(StockReservation.ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }
}