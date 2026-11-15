package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.DeliveryNote;
import com.erp.brazil.sales.domain.model.DeliveryNoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryNoteRepository extends JpaRepository<DeliveryNote, UUID> {

    Optional<DeliveryNote> findByNumberAndCompanyId(String number, UUID companyId);

    List<DeliveryNote> findByCompanyIdAndStatus(UUID companyId, DeliveryNoteStatus status);

    List<DeliveryNote> findBySalesOrderId(UUID salesOrderId);

    @Query("SELECT dn FROM DeliveryNote dn WHERE dn.companyId = :companyId AND dn.status IN (:statuses) ORDER BY dn.deliveryDate DESC")
    List<DeliveryNote> findByCompanyIdAndStatusIn(@Param("companyId") UUID companyId, @Param("statuses") List<DeliveryNoteStatus> statuses);

    @Query("SELECT dn FROM DeliveryNote dn WHERE dn.companyId = :companyId AND dn.deliveryDate BETWEEN :start AND :end ORDER BY dn.deliveryDate")
    List<DeliveryNote> findByDeliveryDateBetween(@Param("companyId") UUID companyId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT dn FROM DeliveryNote dn WHERE dn.companyId = :companyId ORDER BY dn.deliveryDate DESC")
    Page<DeliveryNote> findByCompanyId(@Param("companyId") UUID companyId, Pageable pageable);

    boolean existsByNumberAndCompanyIdAndIdNot(String number, UUID companyId, UUID id);
}