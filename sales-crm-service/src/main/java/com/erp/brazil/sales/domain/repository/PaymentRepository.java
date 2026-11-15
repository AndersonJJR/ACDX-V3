package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.Payment;
import com.erp.brazil.sales.domain.model.Payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findBySalesInvoiceId(UUID salesInvoiceId);

    List<Payment> findBySalesInvoiceIdAndStatus(UUID salesInvoiceId, PaymentStatus status);

    void deleteBySalesInvoiceId(UUID salesInvoiceId);
}