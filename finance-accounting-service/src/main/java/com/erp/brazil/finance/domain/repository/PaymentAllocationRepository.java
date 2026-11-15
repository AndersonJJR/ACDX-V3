package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.Payment;
import com.erp.brazil.finance.domain.model.PaymentAllocation;
import com.erp.brazil.finance.domain.model.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocation, UUID> {

    List<PaymentAllocation> findByPayment(Payment payment);

    List<PaymentAllocation> findByInvoiceIdAndInvoiceType(UUID invoiceId, PaymentType invoiceType);

    @Query("SELECT SUM(pa.amount) FROM PaymentAllocation pa WHERE pa.invoiceId = :invoiceId AND pa.invoiceType = :invoiceType")
    java.math.BigDecimal sumAllocatedByInvoice(@Param("invoiceId") UUID invoiceId, @Param("invoiceType") PaymentType invoiceType);

    Optional<PaymentAllocation> findByPaymentAndInvoiceIdAndInvoiceType(Payment payment, UUID invoiceId, PaymentType invoiceType);
}