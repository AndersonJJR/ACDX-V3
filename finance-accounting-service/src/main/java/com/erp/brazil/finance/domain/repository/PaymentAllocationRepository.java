package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.Payment;
import com.erp.brazil.finance.domain.model.PaymentAllocation;
import com.erp.brazil.finance.domain.model.PaymentAllocationStatus;
import com.erp.brazil.finance.domain.model.PayableInvoice;
import com.erp.brazil.finance.domain.model.ReceivableInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocation, UUID> {

    List<PaymentAllocation> findByPayment(Payment payment);

    List<PaymentAllocation> findByPaymentAndStatus(Payment payment, PaymentAllocationStatus status);

    Optional<PaymentAllocation> findByPayableInvoice(PayableInvoice payableInvoice);

    Optional<PaymentAllocation> findByReceivableInvoice(ReceivableInvoice receivableInvoice);

    List<PaymentAllocation> findByPayableInvoiceAndStatus(PayableInvoice payableInvoice, PaymentAllocationStatus status);

    List<PaymentAllocation> findByReceivableInvoiceAndStatus(ReceivableInvoice receivableInvoice, PaymentAllocationStatus status);

    List<PaymentAllocation> findByStatus(PaymentAllocationStatus status);

    Page<PaymentAllocation> findByPayment(Payment payment, Pageable pageable);

    Page<PaymentAllocation> findByPayableInvoice(PayableInvoice payableInvoice, Pageable pageable);

    Page<PaymentAllocation> findByReceivableInvoice(ReceivableInvoice receivableInvoice, Pageable pageable);
}