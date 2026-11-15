package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.Payment;
import com.erp.brazil.finance.domain.model.PaymentMethod;
import com.erp.brazil.finance.domain.model.PaymentStatus;
import com.erp.brazil.finance.domain.model.PaymentType;
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
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByCompanyIdAndTypeOrderByPaymentDateDesc(UUID companyId, PaymentType type);

    List<Payment> findByCompanyIdAndTypeAndStatusOrderByPaymentDateDesc(UUID companyId, PaymentType type, PaymentStatus status);

    List<Payment> findByMethodAndPaymentDateBetweenOrderByPaymentDateDesc(PaymentMethod method, LocalDate startDate, LocalDate endDate);

    List<Payment> findByCompanyIdAndPaymentDateBetweenOrderByPaymentDateDesc(UUID companyId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT p FROM Payment p WHERE p.companyId = :companyId AND p.type = :type AND p.status = 'PENDING' ORDER BY p.paymentDate ASC")
    List<Payment> findPendingPaymentsByType(@Param("companyId") UUID companyId, @Param("type") PaymentType type);

    Page<Payment> findByCompanyIdAndType(UUID companyId, PaymentType type, Pageable pageable);

    Page<Payment> findByCompanyId(UUID companyId, Pageable pageable);

    boolean existsByTransactionId(String transactionId);
}