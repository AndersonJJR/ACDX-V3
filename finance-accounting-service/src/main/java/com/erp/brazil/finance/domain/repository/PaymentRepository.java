package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.BankAccount;
import com.erp.brazil.finance.domain.model.Customer;
import com.erp.brazil.finance.domain.model.Payment;
import com.erp.brazil.finance.domain.model.PaymentStatus;
import com.erp.brazil.finance.domain.model.PaymentType;
import com.erp.brazil.finance.domain.model.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByReference(String reference);

    List<Payment> findByVendorOrderByPaymentDateDesc(Vendor vendor);

    List<Payment> findByCustomerOrderByPaymentDateDesc(Customer customer);

    List<Payment> findByBankAccountOrderByPaymentDateDesc(BankAccount bankAccount);

    List<Payment> findByPaymentType(PaymentType paymentType);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);

    List<Payment> findByVendorAndStatus(Vendor vendor, PaymentStatus status);

    List<Payment> findByCustomerAndStatus(Customer customer, PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate AND p.status = :status ORDER BY p.paymentDate DESC")
    Page<Payment> findByDateRangeAndStatus(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("status") PaymentStatus status, Pageable pageable);

    Page<Payment> findByVendor(Vendor vendor, Pageable pageable);

    Page<Payment> findByCustomer(Customer customer, Pageable pageable);

    Page<Payment> findByBankAccount(BankAccount bankAccount, Pageable pageable);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    Page<Payment> findAll(Pageable pageable);

    boolean existsByReferenceAndIdNot(String reference, UUID id);
}